/*
This file is part of jpcsp.

Jpcsp is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Jpcsp is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Jpcsp.  If not, see <http://www.gnu.org/licenses/>.
 */
package jpcsp.media;

import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import jpcsp.State;
import jpcsp.HLE.Modules;
import jpcsp.HLE.kernel.types.SceMpegAu;
import jpcsp.HLE.modules.sceMpeg;
import jpcsp.HLE.modules.sceDisplay;
import jpcsp.connector.Connector;
import jpcsp.memory.IMemoryWriter;
import jpcsp.memory.MemoryWriter;
import jpcsp.util.Debug;
import jpcsp.util.FIFOByteBuffer;

import com.xuggle.ferry.Logger;
import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.IVideoResampler;
import com.xuggle.xuggler.io.IURLProtocolHandler;
import com.xuggle.xuggler.video.ConverterFactory;
import com.xuggle.xuggler.video.IConverter;

public class MediaEngine {
	private static org.apache.log4j.Logger log = Modules.log;
    protected static final int AVSEEK_FLAG_BACKWARD = 1; // seek backward
    protected static final int AVSEEK_FLAG_BYTE     = 2; // seeking based on position in bytes
    protected static final int AVSEEK_FLAG_ANY      = 4; // seek to any frame, even non-keyframes
    protected static final int AVSEEK_FLAG_FRAME    = 8; // seeking based on frame number
	private static boolean initialized = false;
    private IContainer container;
    private int numStreams;
    private IStreamCoder videoCoder;
    private IStreamCoder audioCoder;
    private int videoStreamID;
    private int audioStreamID;
    private BufferedImage currentImg;
    private FIFOByteBuffer decodedAudioSamples;
    private int currentSamplesSize = 1024;  // Default size.
    private IVideoPicture videoPicture;
    private IAudioSamples audioSamples;
    private IConverter videoConverter;
    private IVideoResampler videoResampler;
    private int[] videoImagePixels;
    private int bufferAddress;
    private int bufferSize;
    private int bufferMpegOffset;
    private byte[] bufferData;
    private StreamState videoStreamState;
    private StreamState audioStreamState;
    private List<IPacket> freePackets = new LinkedList<IPacket>();
    private ExternalDecoder externalDecoder = new ExternalDecoder();
    private byte[] tempBuffer;

    // External audio loading vars.
    private IContainer extContainer;

    public MediaEngine() {
    	if (!initialized) {
	        // Disable Xuggler's logging, since we do our own.
	        Logger.setGlobalIsLogging(Logger.Level.LEVEL_DEBUG, false);
	        Logger.setGlobalIsLogging(Logger.Level.LEVEL_ERROR, false);
	        Logger.setGlobalIsLogging(Logger.Level.LEVEL_INFO, false);
	        Logger.setGlobalIsLogging(Logger.Level.LEVEL_TRACE, false);
	        Logger.setGlobalIsLogging(Logger.Level.LEVEL_WARN, false);
            initialized = true;
    	}
    }

    public IContainer getContainer() {
        return container;
    }

    public IContainer getExtContainer() {
        return extContainer;
    }

    public int getNumStreams() {
        return numStreams;
    }

    public IStreamCoder getVideoCoder() {
        return videoCoder;
    }

    public IStreamCoder getAudioCoder() {
        return audioCoder;
    }

    public int getVideoStreamID() {
        return videoStreamID;
    }

    public int getAudioStreamID() {
        return audioStreamID;
    }

    public BufferedImage getCurrentImg() {
        return currentImg;
    }

    public int getCurrentAudioSamples(byte[] buffer) {
    	if (decodedAudioSamples == null) {
    		return 0;
    	}

    	int length = Math.min(buffer.length, decodedAudioSamples.length());
    	if (length > 0) {
    		ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, 0, length);
    		length = decodedAudioSamples.readByteBuffer(byteBuffer);
    	}

    	return length;
    }

    public int getAudioSamplesSize() {
        return currentSamplesSize;
    }

    public void setAudioSamplesSize(int newSize) {
        currentSamplesSize = newSize;
    }

    public void release(IPacket packet) {
    	if (packet != null) {
    		freePackets.add(packet);
    	}
    }

    public IPacket getPacket() {
    	if (!freePackets.isEmpty()) {
    		return freePackets.remove(0);
    	}

    	return IPacket.make();
    }

    private void readAu(StreamState state, SceMpegAu au) {
    	if (state == null) {
    		au.dts = 0;
    		au.pts = 0;
    	} else {
    		while (true) {
    			if (!getNextPacket(state)) {
    				if (state == videoStreamState) {
    					state.incrementTimestamps(sceMpeg.videoTimestampStep);
    				} else if (state == audioStreamState) {
    					state.incrementTimestamps(sceMpeg.audioTimestampStep);
    				}
    				break;
    			}

    			state.updateTimestamps();
    			if (state.getPts() >= 90000) {
    				break;
    			}

    			decodePacket(state, 0);
    		}
    		state.getTimestamps(au);
    	}
    }

    public void readVideoAu(SceMpegAu au) {
    	readAu(videoStreamState, au);

    	// On PSP, video DTS is always 1 frame behind PTS
    	if (au.pts >= sceMpeg.videoTimestampStep) {
    		au.dts = au.pts - sceMpeg.videoTimestampStep;
    	}
    }

    public void readAudioAu(SceMpegAu au) {
    	readAu(audioStreamState, au);

    	// On PSP, audio DTS is always set to -1
    	au.dts = sceMpeg.UNKNOWN_TIMESTAMP;
    }

    public void getCurrentAudioAu(SceMpegAu au) {
    	if (audioStreamState != null) {
    		audioStreamState.getTimestamps(au);
    	} else {
    		au.pts += sceMpeg.audioTimestampStep;
    	}

    	// On PSP, audio DTS is always set to -1
    	au.dts = sceMpeg.UNKNOWN_TIMESTAMP;
    }

    public void getCurrentVideoAu(SceMpegAu au) {
    	if (videoStreamState != null) {
    		videoStreamState.getTimestamps(au);
    	} else {
    		au.pts += sceMpeg.videoTimestampStep;
    	}

    	// On PSP, video DTS is always 1 frame behind PTS
    	if (au.pts >= sceMpeg.videoTimestampStep) {
    		au.dts = au.pts - sceMpeg.videoTimestampStep;
    	}
    }

    private int read32(byte[] data, int offset) {
    	int n1 = data[offset] & 0xFF;
    	int n2 = data[offset + 1] & 0xFF;
    	int n3 = data[offset + 2] & 0xFF;
    	int n4 = data[offset + 3] & 0xFF;

    	return (n4 << 24) | (n3 << 16) | (n2 << 8) | n1;
    }

    public void init(byte[] bufferData) {
    	this.bufferData = bufferData;
    	this.bufferAddress = 0;
    	this.bufferSize = sceMpeg.endianSwap32(read32(bufferData, sceMpeg.PSMF_STREAM_SIZE_OFFSET));
    	this.bufferMpegOffset = sceMpeg.endianSwap32(read32(bufferData, sceMpeg.PSMF_STREAM_OFFSET_OFFSET));
    	init();
    }

    public void init(int bufferAddress, int bufferSize, int bufferMpegOffset) {
    	this.bufferAddress = bufferAddress;
    	this.bufferSize = bufferSize;
    	this.bufferMpegOffset = bufferMpegOffset;
    	this.bufferData = null;
    	init();
    }

    public void init() {
    	finish();
        videoStreamID = -1;
        audioStreamID = -1;
    }

    /*
     * Split version of decodeAndPlay.
     *
     * This method is to be used when the video and audio frames
     * are decoded and played step by step (sceMpeg case).
     * The sceMpeg functions must call init() first for each MPEG stream and then
     * keep calling step() until the video is finished and finish() is called.
     */
    public void init(IURLProtocolHandler channel, boolean decodeVideo, boolean decodeAudio) {
    	init();

    	container = IContainer.make();

        // Keep trying to read
        container.setReadRetryCount(-1);

        if (container.open(channel, IContainer.Type.READ, null) < 0) {
            log.error("MediaEngine: Invalid container format!");
        }

        numStreams = container.getNumStreams();

        for (int i = 0; i < numStreams; i++) {
            IStream stream = container.getStream(i);
            IStreamCoder coder = stream.getStreamCoder();

            if (videoStreamID == -1 && coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
                videoStreamID = i;
                videoCoder = coder;
            } else if (audioStreamID == -1 && coder.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO) {
                audioStreamID = i;
                audioCoder = coder;
            }
        }

        if (decodeVideo) {
            if (videoStreamID == -1) {
                log.error("MediaEngine: No video streams found!");
            } else if (videoCoder.open() < 0) {
            	videoCoder.delete();
            	videoCoder = null;
                log.error("MediaEngine: Can't open video decoder!");
            } else {
            	videoConverter = ConverterFactory.createConverter(ConverterFactory.XUGGLER_BGR_24, videoCoder.getPixelType(), videoCoder.getWidth(), videoCoder.getHeight());
                videoPicture = IVideoPicture.make(videoCoder.getPixelType(), videoCoder.getWidth(), videoCoder.getHeight());
            	if (videoCoder.getPixelType() != IPixelFormat.Type.BGR24) {
	                videoResampler = IVideoResampler.make(videoCoder.getWidth(),
	                        videoCoder.getHeight(), IPixelFormat.Type.BGR24,
	                        videoCoder.getWidth(), videoCoder.getHeight(),
	                        videoCoder.getPixelType());
                	videoPicture = IVideoPicture.make(videoResampler.getOutputPixelFormat(), videoPicture.getWidth(), videoPicture.getHeight());
            	}
                videoStreamState = new StreamState(this, videoStreamID, container, 0);
            }
        }

        if (decodeAudio) {
            if (audioStreamID == -1) {
            	// Try to use an external audio file instead
            	if (!initExtAudio()) {
            		log.error("MediaEngine: No audio streams found!");
            		audioStreamState = new StreamState(this, -1, null, 0);
            	}
            } else if (audioCoder.open() < 0) {
            	audioCoder.delete();
            	audioCoder = null;
                log.error("MediaEngine: Can't open audio decoder!");
            } else {
        		audioSamples = IAudioSamples.make(getAudioSamplesSize(), audioCoder.getChannels());
        		decodedAudioSamples = new FIFOByteBuffer();
                audioStreamState = new StreamState(this, audioStreamID, container, 0);
            }
        }
    }

    private boolean getNextPacket(StreamState state) {
    	if (state.isPacketEmpty()) {
    		// Retrieve the next packet for the stream.
    		// First try if there is a pending packet for this stream.
    		state.releasePacket();
    		IPacket packet = state.getNextPacket();
    		if (packet != null) {
    			// use the pending packet
    			state.setPacket(packet);
    		} else {
    			// There is no pending packet, read packets from the container
    			// until a packet for this stream is found.
    			IContainer container = state.getContainer();
    			if (container == null) {
    				return false;
    			}
    			while (state.isPacketEmpty()) {
    				packet = getPacket();
			        if (container.readNextPacket(packet) < 0) {
			        	// No more packets available in the container...
			        	release(packet);
			        	return false;
			        }

			        // Process the packet
			        int streamIndex = packet.getStreamIndex();
			        if (packet.getSize() <= 0) {
			        	// Empty packet, drop it
			        	release(packet);
			        } else if (state.isStream(container, streamIndex)) {
			        	// This is the kind of packet we are looking for
			        	state.setPacket(packet);
			        } else if (videoCoder != null && videoStreamState.isStream(container, streamIndex)) {
			        	// We are currently not interested in video packets,
			        	// add this packet to the video pending packets
			        	videoStreamState.addPacket(packet);
			        } else if (audioCoder != null && audioStreamState.isStream(container, streamIndex)) {
			        	// We are currently not interested in audio packets,
			        	// add this packet to the audio pending packets
			        	audioStreamState.addPacket(packet);
			        } else {
			        	// Packet with unknown stream index, ignore it
			        	release(packet);
			        }
    			}
    		}
    	}

    	return true;
    }

    private boolean decodePacket(StreamState state, int requiredAudioBytes) {
    	boolean complete = false;
        if (state == videoStreamState) {
        	if (videoCoder == null) {
        		// No video coder, skip all the video packets
        		state.releasePacket();
        		complete = true;
        	} else {
        		// Decode the current video packet
        		// and check if we have a complete video sample
        		complete = decodeVideoPacket(state);
            }
        } else if (state == audioStreamState) {
        	if (audioCoder == null) {
        		// No audio coder, skip all the audio packets
        		state.releasePacket();
        		complete = true;
        	} else {
        		// Decode the current audio packet
        		// and check if we have a complete audio sample,
        		// with the minimum required sample bytes
        		if (decodeAudioPacket(state)) {
        			if (decodedAudioSamples.length() >= requiredAudioBytes) {
    					complete = true;
        			}
        		}
            }
        }

        return complete;
    }

    private boolean decodeVideoPacket(StreamState state) {
    	boolean complete = false;
        while (!state.isPacketEmpty()) {
        	int decodedBytes = videoCoder.decodeVideo(videoPicture, state.getPacket(), state.getOffset());

            if (decodedBytes < 0) {
            	// An error occured with this packet, skip it
            	state.releasePacket();
            	break;
            }

            state.updateTimestamps();
            state.consume(decodedBytes);

        	if (videoPicture.isComplete()) {
            	if (videoConverter != null) {
            		currentImg = videoConverter.toImage(videoPicture);
            	}
            	complete = true;
            	break;
            }
        }

        return complete;
    }

    private boolean decodeAudioPacket(StreamState state) {
    	boolean complete = false;
        while (!state.isPacketEmpty()) {
        	int decodedBytes = audioCoder.decodeAudio(audioSamples, state.getPacket(), state.getOffset());

            if (decodedBytes < 0) {
            	// An error occured with this packet, skip it
            	state.releasePacket();
            	break;
            }

            state.updateTimestamps();
            state.consume(decodedBytes);

        	if (audioSamples.isComplete()) {
                updateSoundSamples(audioSamples);
                complete = true;
                break;
            }
        }

        return complete;
    }

    public static String getExtAudioBasePath(int mpegStreamSize) {
    	return String.format("%s%s/Mpeg-%d/", Connector.baseDirectory, State.discId, mpegStreamSize);
    }

    public static String getExtAudioPath(int mpegStreamSize, String suffix) {
        return String.format("%sExtAudio.%s", getExtAudioBasePath(mpegStreamSize), suffix);
    }

    public boolean stepVideo() {
    	return step(videoStreamState, 0);
    }

    public boolean stepAudio(int requiredAudioBytes) {
    	return step(audioStreamState, requiredAudioBytes);
    }

    private boolean step(StreamState state, int requiredAudioBytes) {
    	boolean complete = false;

    	if (state != null) {
	    	while (!complete) {
	    		if (!getNextPacket(state)) {
		        	break;
	    		}

	    		complete = decodePacket(state, requiredAudioBytes);
	    	}
    	}

        return complete;
    }

    private File getExtAudioFile() {
        String supportedFormats[] = {"wav", "mp3", "at3", "raw", "wma", "flac", "m4a"};
        for (int i = 0; i < supportedFormats.length; i++) {
            File f = new File(getExtAudioPath(bufferSize, supportedFormats[i]));
            if (f.exists()) {
            	return f;
            }
        }

        return null;
    }

    private boolean initExtAudio() {
    	boolean useExtAudio = false;

    	File extAudioFile = getExtAudioFile();
    	if (extAudioFile == null && ExternalDecoder.isEnabled()) {
    		// Try to decode the audio using the external decoder
    		if (bufferData != null) {
    			externalDecoder.decodeExtAudio(bufferData, bufferSize, bufferMpegOffset);
    		} else {
    			externalDecoder.decodeExtAudio(bufferAddress, bufferSize, bufferMpegOffset);
    		}
			extAudioFile = getExtAudioFile();
    	}

    	if (extAudioFile != null) {
    		useExtAudio = initExtAudio(extAudioFile.toString());
    	}

        return useExtAudio;
    }

    // Override audio data line with one from an external file.
    private boolean initExtAudio(String file) {
        extContainer = IContainer.make();

        if (log.isDebugEnabled()) {
        	log.debug(String.format("initExtAudio %s", file));
        }

        if (extContainer.open(file, IContainer.Type.READ, null) < 0) {
            log.error("MediaEngine: Invalid file or container format: " + file);
            extContainer.close();
            extContainer = null;
            return false;
        }

        int extNumStreams = extContainer.getNumStreams();

        audioStreamID = -1;
        audioCoder = null;

        for (int i = 0; i < extNumStreams; i++) {
            IStream stream = extContainer.getStream(i);
            IStreamCoder coder = stream.getStreamCoder();

            if (audioStreamID == -1 && coder.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO) {
                audioStreamID = i;
                audioCoder = coder;
            }
        }

        if (audioStreamID == -1) {
            log.error("MediaEngine: No audio streams found!");
            extContainer.close();
            extContainer = null;
            return false;
        } else if (audioCoder.open() < 0) {
            log.error("MediaEngine: Can't open audio decoder!");
            extContainer.close();
            extContainer = null;
            return false;
        }

		audioSamples = IAudioSamples.make(getAudioSamplesSize(), audioCoder.getChannels());
		decodedAudioSamples = new FIFOByteBuffer();

		// External audio is starting at timestamp 0,
		// but the PSP audio is starting at timestamp 89249:
		// offset the external audio timestamp by this value.
		audioStreamState = new StreamState(this, audioStreamID, extContainer, sceMpeg.audioFirstTimestamp);
        audioStreamState.setTimestamps(sceMpeg.mpegTimestampPerSecond);

        log.info(String.format("Using external audio '%s'", file));

        return true;
    }

    // Cleanup function.
    public void finish() {
    	if (container != null) {
    		container.close();
        	container = null;
    	}
    	if (videoStreamState != null) {
    		videoStreamState.finish();
    		videoStreamState = null;
    	}
    	if (audioStreamState != null) {
        	audioStreamState.finish();
    		audioStreamState = null;
    	}
    	while (!freePackets.isEmpty()) {
    		IPacket packet = getPacket();
    		packet.delete();
    	}
    	if (videoCoder != null) {
    		videoCoder.close();
    		videoCoder = null;
    	}
    	if (audioCoder != null) {
    		audioCoder.close();
    		audioCoder = null;
    	}
    	if (videoConverter != null) {
    		videoConverter.delete();
    		videoConverter = null;
    	}
    	if (videoPicture != null) {
    		videoPicture.delete();
    		videoPicture = null;
    	}
    	if (audioSamples != null) {
    		audioSamples.delete();
    		audioSamples = null;
    	}
    	if (videoResampler != null) {
    		videoResampler.delete();
    		videoResampler = null;
    	}
    	if (extContainer != null) {
    		extContainer.close();
    		extContainer = null;
    	}
    	if (decodedAudioSamples != null) {
    		decodedAudioSamples.delete();
            decodedAudioSamples = null;
    	}
    	tempBuffer = null;
    }

    /**
     * Add the audio samples to the decoded audio samples buffer.
     * 
     * @param samples          the samples to be added
     */
    private void updateSoundSamples(IAudioSamples samples) {
    	int sampleSizes = samples.getSize();
    	if (tempBuffer == null || sampleSizes > tempBuffer.length) {
    		tempBuffer = new byte[sampleSizes];
    	}
    	samples.get(0, tempBuffer, 0, sampleSizes);

        decodedAudioSamples.write(tempBuffer, 0, sampleSizes);
    }

    // This function is time critical and has to execute under
    // sceMpeg.avcDecodeDelay to match the PSP and allow a fluid video rendering
    public void writeVideoImage(int dest_addr, int frameWidth, int videoPixelMode) {
        final int bytesPerPixel = sceDisplay.getPixelFormatBytes(videoPixelMode);
        // Get the current generated image, convert it to pixels and write it
        // to memory.
        if (getCurrentImg() != null) {
            // Override the base dimensions with the image's real dimensions.
            int width = getCurrentImg().getWidth();
            int height = getCurrentImg().getHeight();
            int imageSize = height * width;
            BufferedImage image = getCurrentImg();
            if (image.getColorModel() instanceof ComponentColorModel && image.getRaster().getDataBuffer() instanceof DataBufferByte) {
            	// Optimized version for most common case: 1 pixel stored in 3 bytes in BGR format
            	byte[] imageData = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
            	if (videoPixelMode == sceDisplay.PSP_DISPLAY_PIXEL_FORMAT_8888) {
            		// Fastest version for pixel format 8888
	                IMemoryWriter memoryWriter = MemoryWriter.getMemoryWriter(dest_addr, bytesPerPixel);
	            	for (int y = 0, i = 0; y < height; y++) {
		                for (int x = 0; x < width; x++) {
		                    int b = imageData[i++] & 0xFF;
		                    int g = imageData[i++] & 0xFF;
		                    int r = imageData[i++] & 0xFF;
		                    int colorABGR = 0xFF000000 | b << 16 | g << 8 | r;
		                    memoryWriter.writeNext(colorABGR);
		                }
    	                memoryWriter.skip(frameWidth - width);
	            	}
	                memoryWriter.flush();
            	} else {
            		// Slower version for pixel format other than 8888
	                IMemoryWriter memoryWriter = MemoryWriter.getMemoryWriter(dest_addr, bytesPerPixel);
	            	for (int y = 0, i = 0; y < height; y++) {
		                for (int x = 0; x < width; x++) {
		                    int b = imageData[i++] & 0xFF;
		                    int g = imageData[i++] & 0xFF;
		                    int r = imageData[i++] & 0xFF;
		                    int colorABGR = 0xFF000000 | b << 16 | g << 8 | r;
		                    int pixelColor = Debug.getPixelColor(colorABGR, videoPixelMode);
		                    memoryWriter.writeNext(pixelColor);
		                }
    	                memoryWriter.skip(frameWidth - width);
	            	}
	                memoryWriter.flush();
            	}
            } else {
            	// Non-optimized version supporting any image format,
            	// but very slow (due to BufferImage.getRGB() call)
	            if (videoImagePixels == null || videoImagePixels.length < imageSize) {
	            	videoImagePixels = new int[imageSize];
	            }
	            // getRGB is very slow...
				videoImagePixels = image.getRGB(0, 0, width, height, videoImagePixels, 0, width);
                IMemoryWriter memoryWriter = MemoryWriter.getMemoryWriter(dest_addr, bytesPerPixel);
	            for (int y = 0; y < height; y++) {
	                for (int x = 0; x < width; x++) {
	                    int colorARGB = videoImagePixels[y * width + x];
	                    // Convert from ARGB to ABGR.
	                    int a = (colorARGB >>> 24) & 0xFF;
	                    int r = (colorARGB >>> 16) & 0xFF;
	                    int g = (colorARGB >>> 8) & 0xFF;
	                    int b = colorARGB & 0xFF;
	                    int colorABGR = a << 24 | b << 16 | g << 8 | r;
	                    int pixelColor = Debug.getPixelColor(colorABGR, videoPixelMode);
	                    memoryWriter.writeNext(pixelColor);
	                }
	                memoryWriter.skip(frameWidth - width);
	            }
                memoryWriter.flush();
            }
        }
    }

    public void writeVideoImageWithRange(int dest_addr, int frameWidth, int videoPixelMode, int x, int y, int w, int h) {
        final int bytesPerPixel = sceDisplay.getPixelFormatBytes(videoPixelMode);
        // Get the current generated image, convert it to pixels and write it
        // to memory.
        if (getCurrentImg() != null) {
            // Override the base dimensions with the image's real dimensions.
            int width = getCurrentImg().getWidth();
            int height = getCurrentImg().getHeight();
            int imageSize = height * width;
            if (videoImagePixels == null || videoImagePixels.length < imageSize) {
                videoImagePixels = new int[imageSize];

            }
            videoImagePixels = getCurrentImg().getRGB(0, 0, width, height, videoImagePixels, 0, width);
            for (int i = y; i < height; i++) {
                int address = dest_addr + i * frameWidth * bytesPerPixel;
                IMemoryWriter memoryWriter = MemoryWriter.getMemoryWriter(address, bytesPerPixel);
                for (int j = x; j < width; j++) {
                    int colorARGB = videoImagePixels[i * width + j];
                    // Convert from ARGB to ABGR.
                    int a = (colorARGB >>> 24) & 0xFF;
                    int r = (colorARGB >>> 16) & 0xFF;
                    int g = (colorARGB >>> 8) & 0xFF;
                    int b = colorARGB & 0xFF;
                    int colorABGR = a << 24 | b << 16 | g << 8 | r;
                    int pixelColor = Debug.getPixelColor(colorABGR, videoPixelMode);
                    memoryWriter.writeNext(pixelColor);
                }
            }
        }
    }

    public void audioResetPlayPosition(int sample) {
    	if (container != null && audioStreamID != -1) {
    		if (container.seekKeyFrame(audioStreamID, sample, AVSEEK_FLAG_ANY | AVSEEK_FLAG_FRAME) < 0) {
    			log.warn(String.format("Could not reset audio play position to %d", sample));
    		}
    	}
    }
}