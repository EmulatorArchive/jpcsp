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
package jpcsp.HLE.modules150;

import static jpcsp.Allegrex.Common._v0;
import static jpcsp.graphics.GeCommands.TPSM_PIXEL_STORAGE_MODE_16BIT_BGR5650;
import static jpcsp.graphics.GeCommands.TPSM_PIXEL_STORAGE_MODE_32BIT_ABGR8888;
import static jpcsp.util.Utilities.endianSwap32;
import static jpcsp.util.Utilities.readUnaligned32;

import jpcsp.HLE.CanBeNull;
import jpcsp.HLE.CheckArgument;
import jpcsp.HLE.HLEFunction;
import jpcsp.HLE.HLELogging;
import jpcsp.HLE.HLEUnimplemented;
import jpcsp.HLE.SceKernelErrorException;
import jpcsp.HLE.TPointer;
import jpcsp.HLE.TPointer32;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.TimeZone;

import jpcsp.Emulator;
import jpcsp.Memory;
import jpcsp.Processor;
import jpcsp.Allegrex.CpuState;
import jpcsp.HLE.Modules;
import jpcsp.HLE.VFS.IVirtualFile;
import jpcsp.HLE.kernel.managers.SceUidManager;
import jpcsp.HLE.kernel.types.IAction;
import jpcsp.HLE.kernel.types.SceKernelErrors;
import jpcsp.HLE.kernel.types.SceMpegAu;
import jpcsp.HLE.kernel.types.SceMpegRingbuffer;
import jpcsp.HLE.modules.HLEModule;
import jpcsp.HLE.modules150.IoFileMgrForUser.IIoListener;
import jpcsp.connector.MpegCodec;
import jpcsp.filesystems.SeekableDataInput;
import jpcsp.filesystems.umdiso.UmdIsoFile;
import jpcsp.filesystems.umdiso.UmdIsoReader;
import jpcsp.graphics.VideoEngine;
import jpcsp.media.MediaEngine;
import jpcsp.media.PacketChannel;
import jpcsp.memory.IMemoryWriter;
import jpcsp.memory.MemoryWriter;
import jpcsp.settings.AbstractBoolSettingsListener;
import jpcsp.util.Debug;
import jpcsp.util.Utilities;

import org.apache.log4j.Logger;

@HLELogging
public class sceMpeg extends HLEModule {
    public static Logger log = Modules.getLogger("sceMpeg");

    private class EnableConnectorSettingsListener extends AbstractBoolSettingsListener {
		@Override
		protected void settingsValueChanged(boolean value) {
			setEnableConnector(value);
		}
    }

    private class EnableMediaEngineSettingsListener extends AbstractBoolSettingsListener {
		@Override
		protected void settingsValueChanged(boolean value) {
			setEnableMediaEngine(value);
		}
    }

    @Override
    public String getName() {
        return "sceMpeg";
    }

    @Override
    public void start() {
        setSettingsListener("emu.useConnector", new EnableConnectorSettingsListener());
        setSettingsListener("emu.useMediaEngine", new EnableMediaEngineSettingsListener());

        mpegHandle = 0;
        isCurrentMpegAnalyzed = false;
        mpegRingbuffer = null;
        mpegRingbufferAddr = null;
        avcAuAddr = 0;
        atracAuAddr = 0;
        atracStreamsMap = new HashMap<Integer, Integer>();
        avcStreamsMap = new HashMap<Integer, Integer>();
        pcmStreamsMap = new HashMap<Integer, Integer>();
        mpegAtracAu = new SceMpegAu();
        mpegAvcAu = new SceMpegAu();
        afterRingbufferPutCallback = new AfterRingbufferPutCallback();
        if (isEnableConnector()) {
            mpegCodec = new MpegCodec();
        }
        if (checkMediaEngineState()) {
            me = new MediaEngine();
            meChannel = null;
        }

        encodedVideoFramesYCbCr = new HashMap<Integer, byte[]>();
        audioDecodeBuffer = new byte[MPEG_ATRAC_ES_OUTPUT_SIZE];
        allocatedEsBuffers = new boolean[2];
        streamMap = new HashMap<Integer, StreamInfo>();

        super.start();
    }

    public static boolean useMpegCodec = false;
    public static boolean enableMediaEngine = false;

    // MPEG statics.
    public static final int PSMF_MAGIC = 0x464D5350;
    public static final int PSMF_VERSION_0012 = 0x32313030;
    public static final int PSMF_VERSION_0013 = 0x33313030;
    public static final int PSMF_VERSION_0014 = 0x34313030;
    public static final int PSMF_VERSION_0015 = 0x35313030;
    public static final int PSMF_MAGIC_OFFSET = 0x0;
    public static final int PSMF_STREAM_VERSION_OFFSET = 0x4;
    public static final int PSMF_STREAM_OFFSET_OFFSET = 0x8;
    public static final int PSMF_STREAM_SIZE_OFFSET = 0xC;
    public static final int PSMF_FIRST_TIMESTAMP_OFFSET = 0x56;
    public static final int PSMF_LAST_TIMESTAMP_OFFSET = 0x5C;
    protected static final int MPEG_MEMSIZE = 0x10000;          // 64k.
    public static final int atracDecodeDelay = 3000;         // Microseconds
    public static final int avcDecodeDelay = 5400;           // Microseconds
    public static final int mpegDecodeErrorDelay = 100;      // Delay in Microseconds in case of decode error
    public static final int mpegTimestampPerSecond = 90000; // How many MPEG Timestamp units in a second.
    public static final int videoTimestampStep = 3003;      // Value based on pmfplayer (mpegTimestampPerSecond / 29.970 (fps)).
    public static final int audioTimestampStep = 4180;      // For audio play at 44100 Hz (2048 samples / 44100 * mpegTimestampPerSecond == 4180)
    //public static final int audioFirstTimestamp = 89249;    // The first MPEG audio AU has always this timestamp
    public static final int audioFirstTimestamp = 90000;    // The first MPEG audio AU has always this timestamp
    public static final long UNKNOWN_TIMESTAMP = -1;

    // At least 2048 bytes of MPEG data is provided when analysing the MPEG header
    public static final int MPEG_HEADER_BUFFER_MINIMUM_SIZE = 2048;

    // MPEG processing vars.
    protected int mpegHandle;
    protected SceMpegRingbuffer mpegRingbuffer;
    protected AfterRingbufferPutCallback afterRingbufferPutCallback;
    protected TPointer mpegRingbufferAddr;
    protected int mpegStreamSize;
    protected SceMpegAu mpegAtracAu;
    protected SceMpegAu mpegAvcAu;
    protected long lastAtracSystemTime;
    protected long lastAvcSystemTime;
    protected int avcAuAddr;
    protected int atracAuAddr;
    protected boolean endOfAudioReached;
    protected boolean endOfVideoReached;
    protected long mpegLastTimestamp;
    protected long mpegFirstTimestamp;
    protected Date mpegFirstDate;
    protected Date mpegLastDate;
    protected int videoFrameCount;
    protected int audioFrameCount;
    protected int videoPixelMode;
    protected int avcDetailFrameWidth;
    protected int avcDetailFrameHeight;
    protected int defaultFrameWidth;
    protected boolean isCurrentMpegAnalyzed;;
    public int maxAheadTimestamp = 40000;
    // MPEG AVC elementary stream.
    protected static final int MPEG_AVC_ES_SIZE = 2048;          // MPEG packet size.
    // MPEG ATRAC elementary stream.
    protected static final int MPEG_ATRAC_ES_SIZE = 2112;
    public    static final int MPEG_ATRAC_ES_OUTPUT_SIZE = 8192;
    // MPEG PCM elementary stream.
    protected static final int MPEG_PCM_ES_SIZE = 320;
    protected static final int MPEG_PCM_ES_OUTPUT_SIZE = 320;
    // MPEG analysis results.
    public static final int MPEG_VERSION_0012 = 0;
    public static final int MPEG_VERSION_0013 = 1;
    public static final int MPEG_VERSION_0014 = 2;
    public static final int MPEG_VERSION_0015 = 3;
    protected int mpegVersion;
    protected int mpegRawVersion;
    protected int mpegMagic;
    protected int mpegOffset;
    protected int mpegStreamAddr;
    // MPEG streams.
    public static final int MPEG_AVC_STREAM = 0;
    public static final int MPEG_ATRAC_STREAM = 1;
    public static final int MPEG_PCM_STREAM = 2;
    public static final int MPEG_DATA_STREAM = 3;      // Arbitrary user defined type. Can represent audio or video.
    public static final int MPEG_AUDIO_STREAM = 15;
    protected static final int MPEG_AU_MODE_DECODE = 0;
    protected static final int MPEG_AU_MODE_SKIP = 1;
    protected HashMap<Integer, Integer> atracStreamsMap;
    protected HashMap<Integer, Integer> avcStreamsMap;
    protected HashMap<Integer, Integer> pcmStreamsMap;
    protected boolean isAtracRegistered = false;
    protected boolean isAvcRegistered = false;
    protected boolean isPcmRegistered = false;
    protected boolean ignoreAtrac = false;
    protected boolean ignoreAvc = false;
    protected boolean ignorePcm = false;
    // MPEG decoding results.
    protected static final int MPEG_AVC_DECODE_SUCCESS = 1;       // Internal value.
    protected static final int MPEG_AVC_DECODE_ERROR_FATAL = -8;
    protected int avcDecodeResult;
    protected int avcFrameStatus;
    protected MpegCodec mpegCodec;
    protected MediaEngine me;
    protected PacketChannel meChannel;
    protected HashMap<Integer, byte[]> encodedVideoFramesYCbCr;
    protected byte[] audioDecodeBuffer;
    protected boolean[] allocatedEsBuffers;
    protected HashMap<Integer, StreamInfo> streamMap;
    protected static final String streamPurpose = "sceMpeg-Stream";
    private MpegIoListener ioListener;
    private boolean insideRingbufferPut;

    private class StreamInfo {
    	private int uid;
    	private int type;

    	public StreamInfo(int type) {
    		this.type = type;
    		uid = SceUidManager.getNewUid(streamPurpose);
    		streamMap.put(uid, this);
    	}

    	public int getUid() {
    		return uid;
    	}

    	public int getType() {
    		return type;
    	}

    	public void release() {
    		SceUidManager.releaseUid(uid, streamPurpose);
    		streamMap.remove(uid);
    		uid = -1;
    		type = -1;
    	}
    }

    private static class MpegIoListener implements IIoListener {
		@Override
		public void sceIoSync(int result, int device_addr, String device, int unknown) {
		}

		@Override
		public void sceIoPollAsync(int result, int uid, int res_addr) {
		}

		@Override
		public void sceIoWaitAsync(int result, int uid, int res_addr) {
		}

		@Override
		public void sceIoOpen(int result, int filename_addr, String filename, int flags, int permissions, String mode) {
		}

		@Override
		public void sceIoClose(int result, int uid) {
		}

		@Override
		public void sceIoWrite(int result, int uid, int data_addr, int size, int bytesWritten) {
		}

		@Override
		public void sceIoRead(int result, int uid, int data_addr, int size, int bytesRead, long position, SeekableDataInput dataInput, IVirtualFile vFile) {
			Modules.sceMpegModule.onIoRead(dataInput, vFile, bytesRead);
		}

		@Override
		public void sceIoCancel(int result, int uid) {
		}

		@Override
		public void sceIoSeek32(int result, int uid, int offset, int whence) {
		}

		@Override
		public void sceIoSeek64(long result, int uid, long offset, int whence) {
		}

		@Override
		public void sceIoMkdir(int result, int path_addr, String path, int permissions) {
		}

		@Override
		public void sceIoRmdir(int result, int path_addr, String path) {
		}

		@Override
		public void sceIoChdir(int result, int path_addr, String path) {
		}

		@Override
		public void sceIoDopen(int result, int path_addr, String path) {
		}

		@Override
		public void sceIoDread(int result, int uid, int dirent_addr) {
		}

		@Override
		public void sceIoDclose(int result, int uid) {
		}

		@Override
		public void sceIoDevctl(int result, int device_addr, String device, int cmd, int indata_addr, int inlen, int outdata_addr, int outlen) {
		}

		@Override
		public void sceIoIoctl(int result, int uid, int cmd, int indata_addr, int inlen, int outdata_addr, int outlen) {
		}

		@Override
		public void sceIoAssign(int result, int dev1_addr, String dev1, int dev2_addr, String dev2, int dev3_addr, String dev3, int mode, int unk1, int unk2) {
		}

		@Override
		public void sceIoGetStat(int result, int path_addr, String path, int stat_addr) {
		}

		@Override
		public void sceIoRemove(int result, int path_addr, String path) {
		}

		@Override
		public void sceIoChstat(int result, int path_addr, String path, int stat_addr, int bits) {
		}

		@Override
		public void sceIoRename(int result, int path_addr, String path, int new_path_addr, String newpath) {
		}
    }


    public class AfterRingbufferPutCallback implements IAction {
        @Override
        public void execute() {
            hleMpegRingbufferPostPut();
        }
    }

    protected void hleMpegRingbufferPostPut() {
        CpuState cpu = Emulator.getProcessor().cpu;

        int packetsAdded = cpu.gpr[_v0];
        mpegRingbuffer.read(mpegRingbufferAddr);

        if (packetsAdded > 0) {
        	if (log.isTraceEnabled()) {
        		log.trace(String.format("hleMpegRingbufferPostPut:%s", Utilities.getMemoryDump(mpegRingbuffer.data, packetsAdded * mpegRingbuffer.packetSize, 4, 16)));
        	}
        	if (checkMediaEngineState() && (meChannel != null)) {
                meChannel.write(mpegRingbuffer.data, packetsAdded * mpegRingbuffer.packetSize);
            } else if (isEnableConnector()) {
                mpegCodec.writeVideo(mpegRingbuffer.data, packetsAdded * mpegRingbuffer.packetSize);
            }
            if (packetsAdded > mpegRingbuffer.packetsFree) {
                log.warn("sceMpegRingbufferPut clamping packetsAdded old=" + packetsAdded + " new=" + mpegRingbuffer.packetsFree);
                packetsAdded = mpegRingbuffer.packetsFree;
            }
            mpegRingbuffer.packetsRead += packetsAdded;
            mpegRingbuffer.packetsWritten += packetsAdded;
            mpegRingbuffer.packetsFree -= packetsAdded;
            mpegRingbuffer.write(mpegRingbufferAddr);
        }

        insideRingbufferPut = false;
        if (log.isDebugEnabled()) {
            log.debug(String.format("sceMpegRingbufferPut packetsAdded=%d, packetsRead=%d", packetsAdded, mpegRingbuffer.packetsRead));
        }

        cpu.gpr[_v0] = packetsAdded;
    }

    public static boolean isEnableConnector() {
        return useMpegCodec;
    }

    private static void setEnableConnector(boolean useConnector) {
        sceMpeg.useMpegCodec = useConnector;
        if (useConnector) {
            log.info("Using JPCSP connector");
        }
    }

    public static boolean checkMediaEngineState() {
        return enableMediaEngine;
    }

    private static void setEnableMediaEngine(boolean enableMediaEngine) {
        sceMpeg.enableMediaEngine = enableMediaEngine;
        if (enableMediaEngine) {
            log.info("Media Engine enabled");
        }
    }

    protected Date convertTimestampToDate(long timestamp) {
        long millis = timestamp / (mpegTimestampPerSecond / 1000);
        return new Date(millis);
    }

    protected StreamInfo getStreamInfo(int uid) {
    	return streamMap.get(uid);
    }

    protected int getMpegHandle(int mpegAddr) {
        if (Memory.isAddressGood(mpegAddr)) {
            return Processor.memory.read32(mpegAddr);
        }

        return -1;
    }

    public int checkMpegHandle(int mpeg) {
    	if (getMpegHandle(mpeg) != mpegHandle) {
            log.warn(String.format("checkMpegHandler bad mpeg handle 0x%08X", mpeg));
    		throw new SceKernelErrorException(-1);
    	}
    	return mpeg;
    }

    protected void writeTimestamp(Memory mem, int address, long ts) {
        mem.write32(address, (int) ((ts >> 32) & 0x1));
        mem.write32(address + 4, (int) ts);
    }

    protected boolean isCurrentMpegAnalyzed() {
        return isCurrentMpegAnalyzed;
    }

    public void setCurrentMpegAnalyzed(boolean status) {
        isCurrentMpegAnalyzed = status;
    }

    private void unregisterIoListener() {
    	if (ioListener != null) {
    		Modules.IoFileMgrForUserModule.unregisterIoListener(ioListener);
    		ioListener = null;
    	}
    }

    protected void onIoRead(SeekableDataInput dataInput, IVirtualFile vFile, int bytesRead) {
    	// if we are in the first sceMpegRingbufferPut and the MPEG header has not yet
    	// been analyzed, try to read the MPEG header.
		if (!isCurrentMpegAnalyzed() && insideRingbufferPut) {
			if (dataInput instanceof UmdIsoFile) {
		    	// Assume the MPEG header is located in the sector just before the
				// data currently read
				UmdIsoFile umdIsoFile = (UmdIsoFile) dataInput;
				int headerSectorNumber = umdIsoFile.getCurrentSectorNumber();
				headerSectorNumber -= bytesRead / UmdIsoFile.sectorLength;
				// One sector before the data current read
				headerSectorNumber--;

				UmdIsoReader umdIsoReader = Modules.IoFileMgrForUserModule.getIsoReader();
				if (umdIsoReader != null) {
					try {
						// Read the MPEG header sector and analyze it
						byte[] headerSector = umdIsoReader.readSector(headerSectorNumber);
						int tmpAddress = mpegRingbuffer.dataUpperBound - headerSector.length;
						IMemoryWriter memoryWriter = MemoryWriter.getMemoryWriter(tmpAddress, headerSector.length, 1);
						for (int i = 0; i < headerSector.length; i++) {
							memoryWriter.writeNext(headerSector[i] & 0xFF);
						}
						memoryWriter.flush();
						Memory mem = Memory.getInstance();
						if (mem.read32(tmpAddress + PSMF_MAGIC_OFFSET) == PSMF_MAGIC) {
							analyseMpeg(tmpAddress);
						}

						// We do no longer need the IoListener...
						if (isCurrentMpegAnalyzed()) {
							unregisterIoListener();
						}
					} catch (IOException e) {
						if (log.isDebugEnabled()) {
							log.debug("onIoRead", e);
						}
					}
				}
			}
		}
	}

    public static int getMpegVersion(int mpegRawVersion) {
        switch (mpegRawVersion) {
	        case PSMF_VERSION_0012: return MPEG_VERSION_0012;
	        case PSMF_VERSION_0013: return MPEG_VERSION_0013;
	        case PSMF_VERSION_0014: return MPEG_VERSION_0014;
	        case PSMF_VERSION_0015: return MPEG_VERSION_0015;
        }

        return -1;
    }

    protected void analyseMpeg(int bufferAddr) {
        Memory mem = Memory.getInstance();

        mpegStreamAddr = bufferAddr;
        mpegMagic = mem.read32(bufferAddr + PSMF_MAGIC_OFFSET);
        mpegRawVersion = mem.read32(bufferAddr + PSMF_STREAM_VERSION_OFFSET);
        mpegVersion = getMpegVersion(mpegRawVersion);
        mpegOffset = endianSwap32(mem.read32(bufferAddr + PSMF_STREAM_OFFSET_OFFSET));
        mpegStreamSize = endianSwap32(mem.read32(bufferAddr + PSMF_STREAM_SIZE_OFFSET));
        mpegFirstTimestamp = endianSwap32(readUnaligned32(mem, bufferAddr + PSMF_FIRST_TIMESTAMP_OFFSET));
        mpegLastTimestamp = endianSwap32(readUnaligned32(mem, bufferAddr + PSMF_LAST_TIMESTAMP_OFFSET));
        mpegFirstDate = convertTimestampToDate(mpegFirstTimestamp);
        mpegLastDate = convertTimestampToDate(mpegLastTimestamp);
        avcDetailFrameWidth = (mem.read8(bufferAddr + 142) * 0x10);
        avcDetailFrameHeight = (mem.read8(bufferAddr + 143) * 0x10);
        avcDecodeResult = MPEG_AVC_DECODE_SUCCESS;
        avcFrameStatus = 0;
        if ((mpegRingbuffer != null) && !isCurrentMpegAnalyzed()) {
            mpegRingbuffer.reset();
            mpegRingbuffer.write(mpegRingbufferAddr);
        }
        mpegAtracAu.dts = UNKNOWN_TIMESTAMP;
        mpegAtracAu.pts = 0;
        mpegAvcAu.dts = 0;
        mpegAvcAu.pts = 0;
        videoFrameCount = 0;
        audioFrameCount = 0;
        endOfAudioReached = false;
        endOfVideoReached = false;
        if (!isCurrentMpegAnalyzed() && mpegStreamSize > 0 && mpegOffset > 0 && mpegOffset <= mpegStreamSize) {
            if (checkMediaEngineState()) {
            	me.init(bufferAddr, mpegStreamSize, mpegOffset);
            	meChannel = new PacketChannel();
                meChannel.write(bufferAddr, MPEG_HEADER_BUFFER_MINIMUM_SIZE);
            } else if (isEnableConnector()) {
                mpegCodec.init(mpegVersion, mpegStreamSize, mpegLastTimestamp);
                mpegCodec.writeVideo(bufferAddr, MPEG_HEADER_BUFFER_MINIMUM_SIZE);
            }

            // Mpeg header has already been initialized/processed
            setCurrentMpegAnalyzed(true);
        }

        if (log.isDebugEnabled()) {
	    	log.debug(String.format("Stream offset: %d, Stream size: 0x%X", mpegOffset, mpegStreamSize));
	    	log.debug(String.format("First timestamp: %d, Last timestamp: %d", mpegFirstTimestamp, mpegLastTimestamp));
	        if (log.isTraceEnabled()) {
	        	log.trace(String.format("%s", Utilities.getMemoryDump(bufferAddr, MPEG_HEADER_BUFFER_MINIMUM_SIZE, 4, 16)));
	        }
        }
    }

    public static int getMaxAheadTimestamp(int packets) {
        return Math.max(40000, packets * 700); // Empiric value based on tests using JpcspConnector
    }

    private void generateFakeMPEGVideo(int dest_addr, int frameWidth) {
    	generateFakeImage(dest_addr, frameWidth, avcDetailFrameWidth, avcDetailFrameHeight, videoPixelMode);
    }

    public static void generateFakeImage(int dest_addr, int frameWidth, int imageWidth, int imageHeight, int pixelMode) {
        Memory mem = Memory.getInstance();

        Random random = new Random();
        final int pixelSize = 3;
        final int bytesPerPixel = sceDisplay.getPixelFormatBytes(pixelMode);
        for (int y = 0; y < imageHeight - pixelSize + 1; y += pixelSize) {
            int address = dest_addr + y * frameWidth * bytesPerPixel;
            final int width = Math.min(imageWidth, frameWidth);
            for (int x = 0; x < width; x += pixelSize) {
                int n = random.nextInt(256);
                int color = 0xFF000000 | (n << 16) | (n << 8) | n;
                int pixelColor = Debug.getPixelColor(color, pixelMode);
                if (bytesPerPixel == 4) {
                    for (int i = 0; i < pixelSize; i++) {
                        for (int j = 0; j < pixelSize; j++) {
                            mem.write32(address + (i * frameWidth + j) * 4, pixelColor);
                        }
                    }
                } else if (bytesPerPixel == 2) {
                    for (int i = 0; i < pixelSize; i++) {
                        for (int j = 0; j < pixelSize; j++) {
                            mem.write16(address + (i * frameWidth + j) * 2, (short) pixelColor);
                        }
                    }
                }
                address += pixelSize * bytesPerPixel;
            }
        }
    }

    public static void delayThread(long startMicros, int delayMicros) {
    	long now = Emulator.getClock().microTime();
    	int threadDelayMicros = delayMicros - (int) (now - startMicros);
    	delayThread(threadDelayMicros);
    }

    public static void delayThread(int delayMicros) {
    	if (delayMicros > 0) {
    		Modules.ThreadManForUserModule.hleKernelDelayThread(delayMicros, false);
    	} else {
    		Modules.ThreadManForUserModule.hleRescheduleCurrentThread();
    	}
    }

    protected void updateAvcDts() {
        mpegAvcAu.dts = mpegAvcAu.pts - videoTimestampStep; // DTS is always 1 frame before PTS
    }

    protected void finishMpeg() {
        if (checkMediaEngineState()) {
            me.finish();
            if (meChannel != null) {
            	meChannel.clear();
            }
        } else if (isEnableConnector()) {
            mpegCodec.finish();
        }
        setCurrentMpegAnalyzed(false);
        unregisterIoListener();
        VideoEngine.getInstance().resetVideoTextures();
    }

    /**
     * sceMpegQueryStreamOffset
     * 
     * @param mpeg
     * @param bufferAddr
     * @param offsetAddr
     * 
     * @return
     */
    @HLEFunction(nid = 0x21FF80E4, version = 150, checkInsideInterrupt = true)
    public int sceMpegQueryStreamOffset(@CheckArgument("checkMpegHandle") int mpeg, TPointer bufferAddr, TPointer32 offsetAddr) {
        analyseMpeg(bufferAddr.getAddress());

        // Check magic.
        if (mpegMagic != PSMF_MAGIC) {
            log.warn("sceMpegQueryStreamOffset bad magic " + String.format("0x%08X", mpegMagic));
            offsetAddr.setValue(0);
            return SceKernelErrors.ERROR_MPEG_INVALID_VALUE;
        }

        // Check version.
        if (mpegVersion < 0) {
            log.warn("sceMpegQueryStreamOffset bad version " + String.format("0x%08X", mpegRawVersion));
            offsetAddr.setValue(0);
            return SceKernelErrors.ERROR_MPEG_BAD_VERSION;
        }

        // Check offset.
        if ((mpegOffset & 2047) != 0 || mpegOffset == 0) {
            log.warn("sceMpegQueryStreamOffset bad offset " + String.format("0x%08X", mpegOffset));
            offsetAddr.setValue(0);
            return SceKernelErrors.ERROR_MPEG_INVALID_VALUE;
        }

    	offsetAddr.setValue(mpegOffset);
        return 0;
    }

    /**
     * sceMpegQueryStreamSize
     * 
     * @param bufferAddr
     * @param sizeAddr
     * 
     * @return
     */
    @HLEFunction(nid = 0x611E9E11, version = 150, checkInsideInterrupt = true)
    public int sceMpegQueryStreamSize(TPointer bufferAddr, TPointer32 sizeAddr) {
        analyseMpeg(bufferAddr.getAddress());
        
        // Check magic.
        if (mpegMagic != PSMF_MAGIC) {
            log.warn(String.format("sceMpegQueryStreamSize bad magic 0x%08X", mpegMagic));
            return -1;
        }

        // Check alignment.
        if ((mpegStreamSize & 2047) != 0) {
        	sizeAddr.setValue(0);
            return SceKernelErrors.ERROR_MPEG_INVALID_VALUE;
        }
        
    	sizeAddr.setValue(mpegStreamSize);
        return 0;
    }

    /**
     * sceMpegInit
     * 
     * @return
     */
    @HLELogging(level="info")
    @HLEFunction(nid = 0x682A619B, version = 150, checkInsideInterrupt = true)
    public int sceMpegInit() {
        if (checkMediaEngineState()) {
            meChannel = null;
        }
        return 0;
    }

    /**
     * sceMpegFinish
     * 
     * @return
     */
    @HLELogging(level="info")
    @HLEFunction(nid = 0x874624D6, version = 150, checkInsideInterrupt = true)
    public int sceMpegFinish() {
        finishMpeg();

        return 0;
    }

    /**
     * sceMpegQueryMemSize
     * 
     * @param mode
     * 
     * @return
     */
    @HLEFunction(nid = 0xC132E22F, version = 150, checkInsideInterrupt = true)
    public int sceMpegQueryMemSize(int mode) {
        // Mode = 0 -> 64k (constant).
        return MPEG_MEMSIZE;
    }

    /**
     * sceMpegCreate
     * 
     * @param mpeg
     * @param data
     * @param size
     * @param ringbufferAddr
     * @param frameWidth
     * @param mode
     * @param ddrtop
     * 
     * @return
     */
    @HLEFunction(nid = 0xD8C5F121, version = 150, checkInsideInterrupt = true)
    public int sceMpegCreate(TPointer mpeg, TPointer data, int size, @CanBeNull TPointer ringbufferAddr, int frameWidth, int mode, int ddrtop) {
        Memory mem = Processor.memory;

        // Check size.
        if (size < MPEG_MEMSIZE) {
            log.warn("sceMpegCreate bad size " + size);
            return SceKernelErrors.ERROR_MPEG_NO_MEMORY;
        }
        
        // Update the ring buffer struct.
        if (ringbufferAddr.isNotNull()) {
        	mpegRingbuffer = SceMpegRingbuffer.fromMem(mem, ringbufferAddr.getAddress());
	        if (mpegRingbuffer.packetSize == 0) {
	        	mpegRingbuffer.packetsFree = 0;
	        } else {
	        	mpegRingbuffer.packetsFree = (mpegRingbuffer.dataUpperBound - mpegRingbuffer.data) / mpegRingbuffer.packetSize;
	        }
	        mpegRingbuffer.mpeg = mpeg.getAddress();
	        mpegRingbuffer.write(mem, ringbufferAddr.getAddress());
        }

        // Write mpeg system handle.
        mpegHandle = data.getAddress() + 0x30;
        mpeg.setValue32(mpegHandle);

        // Initialize fake mpeg struct.
        Utilities.writeStringZ(mem, mpegHandle, "LIBMPEG.001");
        mem.write32(mpegHandle + 12, -1);
        mem.write32(mpegHandle + 16, ringbufferAddr.getAddress());
        if (mpegRingbuffer != null) {
        	mem.write32(mpegHandle + 20, mpegRingbuffer.dataUpperBound);
        }

        // Initialize mpeg values.
        mpegRingbufferAddr = ringbufferAddr;
        videoFrameCount = 0;
        audioFrameCount = 0;
        videoPixelMode = TPSM_PIXEL_STORAGE_MODE_32BIT_ABGR8888;
        defaultFrameWidth = frameWidth;
        insideRingbufferPut = false;

        return 0;
    }

    /**
     * sceMpegDelete
     * 
     * @param mpeg
     * 
     * @return
     */
    @HLEFunction(nid = 0x606A4649, version = 150, checkInsideInterrupt = true)
    public int sceMpegDelete(@CheckArgument("checkMpegHandle") int mpeg) {
        finishMpeg();

        return 0;
    }

    /**
     * sceMpegRegistStream
     * 
     * @param mpeg
     * @param stream_type
     * @param stream_num
     * 
     * @return
     */
    @HLEFunction(nid = 0x42560F23, version = 150, checkInsideInterrupt = true)
    public int sceMpegRegistStream(@CheckArgument("checkMpegHandle") int mpeg, int stream_type, int stream_num) {
    	StreamInfo info = new StreamInfo(stream_type);
    	int uid = info.getUid();
        // Register the respective stream.
        switch (stream_type) {
            case MPEG_AVC_STREAM:
                isAvcRegistered = true;
                avcStreamsMap.put(uid, stream_num);
                break;
            case MPEG_AUDIO_STREAM:  // Unknown purpose. Use Atrac anyway.
            case MPEG_ATRAC_STREAM:
                isAtracRegistered = true;
                atracStreamsMap.put(uid, stream_num);
                break;
            case MPEG_PCM_STREAM:
                isPcmRegistered = true;
                pcmStreamsMap.put(uid, stream_num);
                break;
            default:
                log.warn("sceMpegRegistStream unknown stream type=" + stream_type);
                break;
        }

        return uid;
    }

    /**
     * sceMpegUnRegistStream
     * 
     * @param mpeg
     * @param streamUid
     * 
     * @return
     */
    @HLEFunction(nid = 0x591A4AA2, version = 150, checkInsideInterrupt = true)
    public int sceMpegUnRegistStream(@CheckArgument("checkMpegHandle") int mpeg, int streamUid) {
    	StreamInfo info = getStreamInfo(streamUid);
    	if (info == null) {
            log.warn("sceMpegUnRegistStream unknown stream=0x" + Integer.toHexString(streamUid));
            return -1;
    	}

    	// Unregister the respective stream.
        switch (info.getType()) {
            case MPEG_AVC_STREAM:
                isAvcRegistered = false;
                avcStreamsMap.remove(streamUid);
                break;
            case MPEG_AUDIO_STREAM:  // Unknown purpose. Use Atrac anyway.
            case MPEG_ATRAC_STREAM:
                isAtracRegistered = false;
                atracStreamsMap.remove(streamUid);
                break;
            case MPEG_PCM_STREAM:
                isPcmRegistered = false;
                pcmStreamsMap.remove(streamUid);
                break;
            default:
                log.warn("sceMpegUnRegistStream unknown stream=0x" + Integer.toHexString(streamUid));
                break;
        }
        info.release();
        setCurrentMpegAnalyzed(false);
        return 0;
    }

    /**
     * sceMpegMallocAvcEsBuf
     * 
     * @param mpeg
     * 
     * @return
     */
    @HLEFunction(nid = 0xA780CF7E, version = 150, checkInsideInterrupt = true)
    public int sceMpegMallocAvcEsBuf(@CheckArgument("checkMpegHandle") int mpeg) {
        // sceMpegMallocAvcEsBuf does not allocate any memory.
    	// It returns 0x00000001 for the first call,
    	// 0x00000002 for the second call
    	// and 0x00000000 for subsequent calls.
    	int esBufferId = 0;
    	for (int i = 0; i < allocatedEsBuffers.length; i++) {
    		if (!allocatedEsBuffers[i]) {
    			esBufferId = i + 1;
    			allocatedEsBuffers[i] = true;
    			break;
    		}
    	}

    	return esBufferId;
    }

    /**
     * sceMpegFreeAvcEsBuf
     * 
     * @param mpeg
     * @param esBuf
     * 
     * @return
     */
    @HLEFunction(nid = 0xCEB870B1, version = 150, checkInsideInterrupt = true)
    public int sceMpegFreeAvcEsBuf(@CheckArgument("checkMpegHandle") int mpeg, int esBuf) {
        if (esBuf == 0) {
            log.warn("sceMpegFreeAvcEsBuf(mpeg=0x" + Integer.toHexString(mpeg) + ", esBuf=0x" + Integer.toHexString(esBuf) + ") bad esBuf handle");
            return SceKernelErrors.ERROR_MPEG_INVALID_VALUE;
        }
        
    	if (esBuf >= 1 && esBuf <= allocatedEsBuffers.length) {
    		allocatedEsBuffers[esBuf - 1] = false;
    	}
    	return 0;
    }

    /**
     * sceMpegQueryAtracEsSize
     * 
     * @param mpeg
     * @param esSize_addr
     * @param outSize_addr
     * 
     * @return
     */
    @HLEFunction(nid = 0xF8DCB679, version = 150, checkInsideInterrupt = true)
    public int sceMpegQueryAtracEsSize(@CheckArgument("checkMpegHandle") int mpeg, @CanBeNull TPointer32 esSizeAddr, @CanBeNull TPointer32 outSizeAddr) {
        esSizeAddr.setValue(MPEG_ATRAC_ES_SIZE);
        outSizeAddr.setValue(MPEG_ATRAC_ES_OUTPUT_SIZE);

        return 0;
    }

    /**
     * sceMpegQueryPcmEsSize
     * 
     * @param mpeg
     * @param esSize_addr
     * @param outSize_addr
     * 
     * @return
     */
    @HLEFunction(nid = 0xC02CF6B5, version = 150, checkInsideInterrupt = true)
    public int sceMpegQueryPcmEsSize(@CheckArgument("checkMpegHandle") int mpeg, TPointer32 esSizeAddr, TPointer32 outSizeAddr) {
        esSizeAddr.setValue(MPEG_PCM_ES_SIZE);
        outSizeAddr.setValue(MPEG_PCM_ES_OUTPUT_SIZE);

        return 0;
    }

    /**
     * sceMpegInitAu
     * 
     * @param mpeg
     * @param buffer_addr
     * @param auAddr
     * 
     * @return
     */
    @HLEFunction(nid = 0x167AFD9E, version = 150, checkInsideInterrupt = true)
    public int sceMpegInitAu(@CheckArgument("checkMpegHandle") int mpeg, int buffer_addr, TPointer auAddr) {
        // Check if sceMpegInitAu is being called for AVC or ATRAC
        // and write the proper AU (access unit) struct.
        if (buffer_addr >= 1 && buffer_addr <= allocatedEsBuffers.length && allocatedEsBuffers[buffer_addr - 1]) {
        	mpegAvcAu.esBuffer = buffer_addr;
        	mpegAvcAu.esSize = MPEG_AVC_ES_SIZE;
        	mpegAvcAu.write(auAddr.getMemory(), auAddr.getAddress());
        } else {
        	mpegAtracAu.esBuffer = buffer_addr;
        	mpegAtracAu.esSize = MPEG_ATRAC_ES_SIZE;
        	mpegAtracAu.write(auAddr.getMemory(), auAddr.getAddress());
        }
        return 0;
    }

    /**
     * sceMpegChangeGetAvcAuMode
     * 
     * @param mpeg
     * @param stream_addr
     * @param mode
     * 
     * @return
     */
    @HLEUnimplemented
    @HLEFunction(nid = 0x234586AE, version = 150, checkInsideInterrupt = true)
    public int sceMpegChangeGetAvcAuMode(int mpeg, int stream_addr, int mode) {
        return 0;
    }

    /**
     * sceMpegChangeGetAuMode
     * 
     * @param mpeg
     * @param streamUid
     * @param mode
     * 
     * @return
     */
    @HLEFunction(nid = 0x9DCFB7EA, version = 150, checkInsideInterrupt = true)
    public int sceMpegChangeGetAuMode(int mpeg, int streamUid, int mode) {
        StreamInfo info = getStreamInfo(streamUid);
        if (info != null) {
	        switch (info.getType()) {
	            case MPEG_AVC_STREAM:
	                if(mode == MPEG_AU_MODE_DECODE) {
	                    ignoreAvc = false;
	                } else if (mode == MPEG_AU_MODE_SKIP) {
	                    ignoreAvc = true;
	                }
	                break;
	            case MPEG_AUDIO_STREAM:
	            case MPEG_ATRAC_STREAM:
	                if(mode == MPEG_AU_MODE_DECODE) {
	                    ignoreAtrac = false;
	                } else if (mode == MPEG_AU_MODE_SKIP) {
	                    ignoreAvc = true;
	                }
	                break;
	            case MPEG_PCM_STREAM:
	                if(mode == MPEG_AU_MODE_DECODE) {
	                    ignorePcm = false;
	                } else if (mode == MPEG_AU_MODE_SKIP) {
	                    ignorePcm = true;
	                }
	                break;
	            default:
	                log.warn("sceMpegChangeGetAuMode unknown stream=0x" + Integer.toHexString(streamUid));
	                break;
	        }
        } else {
            log.warn("sceMpegChangeGetAuMode unknown stream=0x" + Integer.toHexString(streamUid));
        }
        return 0;
    }

    /**
     * sceMpegGetAvcAu
     * 
     * @param mpeg
     * @param streamUid
     * @param au_addr
     * @param attr_addr
     * 
     * @return
     */
    @HLEFunction(nid = 0xFE246728, version = 150, checkInsideInterrupt = true)
    public int sceMpegGetAvcAu(@CheckArgument("checkMpegHandle") int mpeg, int streamUid, TPointer auAddr, @CanBeNull TPointer32 attrAddr) {
        if (mpegRingbuffer != null) {
            mpegRingbuffer.read(mpegRingbufferAddr);
        }
        
        if (mpegRingbuffer.packetsRead == 0 || (!checkMediaEngineState() && mpegRingbuffer.isEmpty())) {
            delayThread(mpegDecodeErrorDelay);
            return SceKernelErrors.ERROR_MPEG_NO_DATA; // No more data in ringbuffer.
        }
        
        // @NOTE: Shouldn't this be negated?
        if (Memory.isAddressGood(streamUid)) {
            log.warn("sceMpegGetAvcAu didn't get a fake stream");
            return SceKernelErrors.ERROR_MPEG_INVALID_ADDR;
        }
        
        if (!streamMap.containsKey(streamUid)) {
            log.warn(String.format("sceMpegGetAvcAu bad stream 0x%X", streamUid));
            return -1;
        }

        if ((mpegAvcAu.pts > mpegAtracAu.pts + maxAheadTimestamp) && isAtracRegistered) {
            // Video is ahead of audio, deliver no video data to wait for audio.
            if (log.isDebugEnabled()) {
                log.debug(String.format("sceMpegGetAvcAu video ahead of audio: %d - %d", mpegAvcAu.pts, mpegAtracAu.pts));
            }
            delayThread(mpegDecodeErrorDelay);
            return SceKernelErrors.ERROR_MPEG_NO_DATA; // No more data in ringbuffer.
        }
        
        int result = 0;
        // Update the video timestamp (AVC).
        if (!ignoreAvc) {
        	// Read Au of next Avc frame
            if (checkMediaEngineState()) {
            	Emulator.getClock().pause();
                if (me.getContainer() == null) {
                    me.init(meChannel, true, true);
                }
            	if (!me.readVideoAu(mpegAvcAu)) {
            		// end of video reached only when last timestamp has been reached
            		if (mpegLastTimestamp <= 0 || mpegAvcAu.pts >= mpegLastTimestamp) {
            			endOfVideoReached = true;
            		}
            		// No more data in ringbuffer.
        			result = SceKernelErrors.ERROR_MPEG_NO_DATA;
            	} else {
            		endOfVideoReached = false;
            	}
            	Emulator.getClock().resume();
            } else if (isEnableConnector()) {
            	if (!mpegCodec.readVideoAu(mpegAvcAu, videoFrameCount)) {
            		// Avc Au was not updated by the MpegCodec
                    mpegAvcAu.pts += videoTimestampStep;
            	}
        		updateAvcDts();
            }
        	mpegAvcAu.write(auAddr);
        	if (log.isDebugEnabled()) {
        		log.debug(String.format("sceMpegGetAvcAu returning 0x%08X, AvcAu=%s", result, mpegAvcAu.toString()));
        	}
        }

        attrAddr.setValue(1); // Unknown.

        if (result != 0) {
        	delayThread(mpegDecodeErrorDelay);
        }

        return result;
    }

    /**
     * sceMpegGetPcmAu
     * 
     * @param mpeg
     * @param streamUid
     * @param au_addr
     * @param attr_addr
     * 
     * @return
     */
    @HLEFunction(nid = 0x8C1E027D, version = 150, checkInsideInterrupt = true)
    public int sceMpegGetPcmAu(@CheckArgument("checkMpegHandle") int mpeg, int streamUid, TPointer auAddr, @CanBeNull TPointer32 attrAddr) {
        if (mpegRingbuffer != null) {
            mpegRingbuffer.read(mpegRingbufferAddr);
        }
        
        if (mpegRingbuffer.packetsRead == 0 || (!checkMediaEngineState() && mpegRingbuffer.isEmpty())) {
            delayThread(mpegDecodeErrorDelay);
            return SceKernelErrors.ERROR_MPEG_NO_DATA; // No more data in ringbuffer.
        }
        
        // Should be negated?
        if (Memory.isAddressGood(streamUid)) {
            log.warn("sceMpegGetPcmAu didn't get a fake stream");
            return SceKernelErrors.ERROR_MPEG_INVALID_ADDR;
        } 
        
        if (!streamMap.containsKey(streamUid)) {
            log.warn(String.format("sceMpegGetPcmAu bad streamUid 0x%08X", streamUid));
            return -1;
        }
        int result = 0;
        // Update the audio timestamp (Atrac).
        if (!ignorePcm) {
        	// Read Au of next Atrac frame
            if (checkMediaEngineState()) {
            	Emulator.getClock().pause();
            	if (me.getContainer() == null) {
            		me.init(meChannel, true, true);
            	}
            	if (!me.readAudioAu(mpegAtracAu)) {
            		result = SceKernelErrors.ERROR_MPEG_NO_DATA; // No more data in ringbuffer.
            	}
            	Emulator.getClock().resume();
            } else if (isEnableConnector() && mpegCodec.readAudioAu(mpegAtracAu, audioFrameCount)) {
        		// Atrac Au updated by the MpegCodec
        	}
        	mpegAtracAu.write(auAddr);
        	if (log.isDebugEnabled()) {
        		log.debug(String.format("sceMpegGetPcmAu returning AtracAu=%s", mpegAtracAu.toString()));
        	}
        }
        // Bitfield used to store data attributes.
        // Uses same bitfield as the one in the PSMF header.
    	int attr = 1 << 7; // Sampling rate (1 = 44.1kHz).
    	attr |= 2;         // Number of channels (1 - MONO / 2 - STEREO).
        attrAddr.setValue(attr);

        if (result != 0) {
        	delayThread(mpegDecodeErrorDelay);
        }
        return result;
    }

    /**
     * sceMpegGetAtracAu
     * 
     * @param mpeg
     * @param streamUid
     * @param auAddr
     * @param attrAddr
     * 
     * @return
     */
    @HLEFunction(nid = 0xE1CE83A7, version = 150, checkInsideInterrupt = true)
    public int sceMpegGetAtracAu(@CheckArgument("checkMpegHandle") int mpeg, int streamUid, TPointer auAddr, @CanBeNull TPointer32 attrAddr) {
        if (mpegRingbuffer != null) {
            mpegRingbuffer.read( mpegRingbufferAddr);
        }

        if (mpegRingbuffer.packetsRead == 0 || (!checkMediaEngineState() && mpegRingbuffer.isEmpty())) {
            log.debug("sceMpegGetAtracAu ringbuffer empty");
            delayThread(mpegDecodeErrorDelay);
            return SceKernelErrors.ERROR_MPEG_NO_DATA; // No more data in ringbuffer.
        }

        if (Memory.isAddressGood(streamUid)) {
            log.warn("sceMpegGetAtracAu didn't get a fake stream");
            return SceKernelErrors.ERROR_MPEG_INVALID_ADDR;
        }

        if (!streamMap.containsKey(streamUid)) {
            log.warn("sceMpegGetAtracAu bad address " + String.format("0x%08X 0x%08X", streamUid, auAddr));
            return -1;
        }

    	if (endOfAudioReached && endOfVideoReached) {
    		if (log.isDebugEnabled()) {
    			log.debug("sceMpegGetAtracAu end of audio and video reached");
    		}

    		// Consume all the remaining packets, if any.
    		if (mpegRingbuffer.packetsFree < mpegRingbuffer.packets) {
    			mpegRingbuffer.packetsFree = mpegRingbuffer.packets;
    			mpegRingbuffer.write(mpegRingbufferAddr);
    		}

    		return SceKernelErrors.ERROR_MPEG_NO_DATA; // No more data in ringbuffer.
    	}
    	
    	if ((mpegAtracAu.pts > mpegAvcAu.pts + maxAheadTimestamp) && isAvcRegistered && !endOfAudioReached) {
            // Audio is ahead of video, deliver no audio data to wait for video.
        	// This error is not returned when the end of audio has been reached (Patapon 3).
            if (log.isDebugEnabled()) {
                log.debug(String.format("sceMpegGetAtracAu audio ahead of video: %d - %d", mpegAtracAu.pts, mpegAvcAu.pts));
            }
            delayThread(mpegDecodeErrorDelay);
            return SceKernelErrors.ERROR_MPEG_NO_DATA; // No more data in ringbuffer.
        }
    	
        // Update the audio timestamp (Atrac).
        int result = 0;
        if (!ignoreAtrac) {
        	// Read Au of next Atrac frame
            if (checkMediaEngineState()) {
            	Emulator.getClock().pause();
            	if (me.getContainer() == null) {
            		me.init(meChannel, true, true);
            	}
            	if (!me.readAudioAu(mpegAtracAu)) {
            		endOfAudioReached = true;
            		// If the audio could not be decoded or the
            		// end of audio has been reached (Patapon 3),
            		// simulate a successful return
            	} else {
            		endOfAudioReached = false;
            	}
            	Emulator.getClock().resume();
            } else if (isEnableConnector() && mpegCodec.readAudioAu(mpegAtracAu, audioFrameCount)) {
        		// Atrac Au updated by the MpegCodec
        	}
        	mpegAtracAu.write(auAddr);
        	if (log.isDebugEnabled()) {
        		log.debug(String.format("sceMpegGetAtracAu returning 0x%08X, AtracAu=%s", result, mpegAtracAu.toString()));
        	}
        }

        // Bitfield used to store data attributes.
        attrAddr.setValue(0);     // Pointer to ATRAC3plus stream (from PSMF file).

        if (result != 0) {
        	delayThread(mpegDecodeErrorDelay);
        }
        
        return result;
    }

    /**
     * sceMpegFlushStream
     * 
     * @param mpeg
     * @param stream_addr
     * 
     * @return
     */
    @HLEFunction(nid = 0x500F0429, version = 150, checkInsideInterrupt = true)
    public int sceMpegFlushStream(int mpeg, int stream_addr) {
        finishMpeg();
        return 0;
    }

    /**
     * sceMpegFlushAllStream
     * 
     * @param mpeg
     * 
     * @return
     */
    @HLEFunction(nid = 0x707B7629, version = 150, checkInsideInterrupt = true)
    public int sceMpegFlushAllStream(int mpeg) {
        // Finish the Mpeg only if we are not at the start of a new video,
        // otherwise the analyzed video could be lost.
        if (videoFrameCount > 0 || audioFrameCount > 0) {
        	finishMpeg();
        }

        return 0;
    }

    /**
     * sceMpegAvcDecode
     * 
     * @param mpeg
     * @param au_addr
     * @param frameWidth
     * @param buffer_addr
     * @param init_addr
     * 
     * @return
     */
    @HLEFunction(nid = 0x0E3C2E9D, version = 150, checkInsideInterrupt = true)
    public int sceMpegAvcDecode(@CheckArgument("checkMpegHandle") int mpeg, TPointer32 auAddr, int frameWidth, TPointer32 bufferAddr, TPointer32 initAddr) {
        // When frameWidth is 0, take the frameWidth specified at sceMpegCreate.
        if (frameWidth == 0) {
            if (defaultFrameWidth == 0) {
                frameWidth = avcDetailFrameWidth;
            } else {
                frameWidth = defaultFrameWidth;
            }
        }
        if (mpegRingbuffer != null) {
            mpegRingbuffer.read(mpegRingbufferAddr);
        }

        if (mpegRingbuffer == null) {
            log.warn("sceMpegAvcDecode ringbuffer not created");
            return -1;
        }

        if (mpegRingbuffer.packetsRead == 0 || (!checkMediaEngineState() && mpegRingbuffer.isEmpty())) {
            log.debug("sceMpegAvcDecode ringbuffer empty");
            return SceKernelErrors.ERROR_AVC_VIDEO_FATAL;
        }

        int au = auAddr.getValue();
        int buffer = bufferAddr.getValue();
        int init = initAddr.getValue();
        if (log.isDebugEnabled()) {
            log.debug(String.format("sceMpegAvcDecode *au=0x%08X, *buffer=0x%08X, init=%d", au, buffer, init));
        }

        final int width = Math.min(480, frameWidth);
        final int height = 272;

        // Do not cache the video image as a texture in the VideoEngine to allow fluid rendering
        VideoEngine.getInstance().addVideoTexture(buffer, buffer + height * frameWidth * sceDisplay.getPixelFormatBytes(videoPixelMode));

        long startTime = Emulator.getClock().microTime();

        int packetsInRingbuffer = mpegRingbuffer.packets - mpegRingbuffer.packetsFree;
        int processedPackets = mpegRingbuffer.packetsRead - packetsInRingbuffer;
        int processedSize = processedPackets * mpegRingbuffer.packetSize;

        // let's go with 3 packets per frame for now
        int packetsConsumed = 3;
        if (mpegStreamSize > 0 && mpegLastTimestamp > 0) {
            // Try a better approximation of the packets consumed based on the timestamp
            int processedSizeBasedOnTimestamp = (int) ((((float) mpegAvcAu.pts) / mpegLastTimestamp) * mpegStreamSize);
            if (processedSizeBasedOnTimestamp < processedSize) {
                packetsConsumed = 0;
            } else {
                packetsConsumed = (processedSizeBasedOnTimestamp - processedSize) / mpegRingbuffer.packetSize;
                if (packetsConsumed > 10) {
                    packetsConsumed = 10;
                }
            }
            if (log.isDebugEnabled()) {
                log.debug(String.format("sceMpegAvcDecode consumed %d %d/%d %d", processedSizeBasedOnTimestamp, processedSize, mpegStreamSize, packetsConsumed));
            }
        }

        if (checkMediaEngineState()) {
        	// Suspend the emulator clock to perform time consuming HLE operation,
        	// in order to improve the timing compatibility with the PSP.
        	Emulator.getClock().pause();
            if (me.stepVideo()) {
            	me.writeVideoImage(buffer, frameWidth, videoPixelMode);
            	packetsConsumed = meChannel.getReadLength() / mpegRingbuffer.packetSize;

            	// The MediaEngine is already consuming all the remaining
            	// packets when approaching the end of the video. The PSP
            	// is only consuming the last packet when reaching the end,
            	// not before.
            	// Consuming all the remaining packets?
            	if (mpegRingbuffer.packetsFree + packetsConsumed >= mpegRingbuffer.packets) {
            		// Having not yet reached the last timestamp?
            		if (mpegLastTimestamp > 0 && mpegAvcAu.pts < mpegLastTimestamp) {
            			// Do not yet consume all the remaining packets.
            			packetsConsumed = 0;
            		}
            	}

            	meChannel.setReadLength(meChannel.getReadLength() - packetsConsumed * mpegRingbuffer.packetSize);
            } else {
            	// Consume all the remaining packets
            	packetsConsumed = mpegRingbuffer.packets - mpegRingbuffer.packetsFree;
            }
        	Emulator.getClock().resume();
            avcFrameStatus = 1;
        } else if (isEnableConnector() && mpegCodec.readVideoFrame(buffer, frameWidth, width, height, videoPixelMode, videoFrameCount)) {
            packetsConsumed = mpegCodec.getPacketsConsumed();
            avcFrameStatus = 1;
        } else {
            mpegAvcAu.pts += videoTimestampStep;
            updateAvcDts();

            // Generate static.
            generateFakeMPEGVideo(buffer, frameWidth);
            if (isEnableConnector()) {
                mpegCodec.postFakedVideo(buffer, frameWidth, videoPixelMode);
            }
            Date currentDate = convertTimestampToDate(mpegAvcAu.pts);
            if (log.isDebugEnabled()) {
                log.debug(String.format("currentDate: %s", currentDate.toString()));
            }
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            Debug.printFramebuffer(buffer, frameWidth, 10, avcDetailFrameHeight - 22, 0xFFFFFFFF, 0xFF000000, videoPixelMode, 1, " This is a faked MPEG video. ");
            String displayedString;
            if (mpegLastDate != null) {
                displayedString = String.format(" %s / %s ", dateFormat.format(currentDate), dateFormat.format(mpegLastDate));
                Debug.printFramebuffer(buffer, frameWidth, 10, 10, 0xFFFFFFFF, 0xFF000000, videoPixelMode, 2, displayedString);
            }
            if (mpegStreamSize > 0) {
                displayedString = String.format(" %d/%d (%.0f%%) ", processedSize, mpegStreamSize, processedSize * 100f / mpegStreamSize);
            } else {
                displayedString = String.format(" %d ", processedSize);
            }
            Debug.printFramebuffer(buffer, frameWidth, 10, 30, 0xFFFFFFFF, 0xFF000000, videoPixelMode, 2, displayedString);
            if (log.isDebugEnabled()) {
                log.debug(String.format("sceMpegAvcDecode currentTimestamp=%d", mpegAvcAu.pts));
            }
            avcFrameStatus = 1;
        }

        videoFrameCount++;

        if (log.isDebugEnabled()) {
            log.debug(String.format("sceMpegAvcDecode currentTimestamp=%d", mpegAvcAu.pts));
        }
        if (mpegRingbuffer.packetsFree < mpegRingbuffer.packets && packetsConsumed > 0) {
            mpegRingbuffer.packetsFree = Math.min(mpegRingbuffer.packets, mpegRingbuffer.packetsFree + packetsConsumed);
            if (log.isDebugEnabled()) {
                log.debug(String.format("sceMpegAvcDecode consumed %d packets, remaining %d packets", packetsConsumed, mpegRingbuffer.packets - mpegRingbuffer.packetsFree));
            }
            mpegRingbuffer.write(mpegRingbufferAddr);
        }

        // Correct decoding.
        avcDecodeResult = MPEG_AVC_DECODE_SUCCESS;

        // Save the current frame's status (0 - not showing / 1 - showing).
        initAddr.setValue(avcFrameStatus);

        delayThread(startTime, avcDecodeDelay);

        return 0;
    }

    /**
     * sceMpegAvcDecodeDetail
     * 
     * @param mpeg
     * @param detailPointer
     * 
     * @return
     */
    @HLEFunction(nid = 0x0F6C18D7, version = 150, checkInsideInterrupt = true)
    public int sceMpegAvcDecodeDetail(@CheckArgument("checkMpegHandle") int mpeg, TPointer detailPointer) {
        detailPointer.setValue32( 0, avcDecodeResult     ); // Stores the result.
        detailPointer.setValue32( 4, videoFrameCount     ); // Last decoded frame.
        detailPointer.setValue32( 8, avcDetailFrameWidth ); // Frame width.
        detailPointer.setValue32(12, avcDetailFrameHeight); // Frame height.
        detailPointer.setValue32(16, 0                   ); // Frame crop rect (left).
        detailPointer.setValue32(20, 0                   ); // Frame crop rect (right).
        detailPointer.setValue32(24, 0                   ); // Frame crop rect (top).
        detailPointer.setValue32(28, 0                   ); // Frame crop rect (bottom).
        detailPointer.setValue32(32, avcFrameStatus      ); // Status of the last decoded frame.

        return 0;
    }

    /**
     * sceMpegAvcDecodeMode
     * 
     * @param mpeg
     * @param mode_addr
     * 
     * @return
     */
    @HLEFunction(nid = 0xA11C7026, version = 150, checkInsideInterrupt = true)
    public int sceMpegAvcDecodeMode(@CheckArgument("checkMpegHandle") int mpeg, TPointer32 modeAddr) {
        // -1 is a default value.
        int mode = modeAddr.getValue(0);
        int pixelMode = modeAddr.getValue(4);
        if (pixelMode >= TPSM_PIXEL_STORAGE_MODE_16BIT_BGR5650 && pixelMode <= TPSM_PIXEL_STORAGE_MODE_32BIT_ABGR8888) {
            videoPixelMode = pixelMode;
        } else {
            log.warn("sceMpegAvcDecodeMode mode=0x" + mode + " pixel mode=" + pixelMode + ": unknown mode");
        }
        return 0;
    }

    /**
     * sceMpegAvcDecodeStop
     * 
     * @param mpeg
     * @param frameWidth
     * @param buffer_addr
     * @param status_addr
     * 
     * @return
     */
    @HLEFunction(nid = 0x740FCCD1, version = 150, checkInsideInterrupt = true)
    public int sceMpegAvcDecodeStop(@CheckArgument("checkMpegHandle") int mpeg, int frameWidth, TPointer bufferAddr, TPointer32 statusAddr) {
        // No last frame generated
        statusAddr.setValue(0);

        return 0;
    }

    /**
     * sceMpegAvcDecodeFlush
     * 
     * @param mpeg
     * 
     * @return
     */
    @HLEFunction(nid = 0x4571CC64, version = 150, checkInsideInterrupt = true)
    public int sceMpegAvcDecodeFlush(int mpeg) {
        // Finish the Mpeg only if we are not at the start of a new video,
        // otherwise the analyzed video could be lost.
        if (videoFrameCount > 0 || audioFrameCount > 0) {
        	finishMpeg();
        }

        return 0;
    }

    /**
     * sceMpegAvcQueryYCbCrSize
     * 
     * @param mpeg
     * @param mode         - 1 -> Loaded from file. 2 -> Loaded from memory.
     * @param width        - 480.
     * @param height       - 272.
     * @param resultAddr   - Where to store the result.
     * 
     * @return
     */
    @HLEFunction(nid = 0x211A057C, version = 150, checkInsideInterrupt = true)
    public int sceMpegAvcQueryYCbCrSize(@CheckArgument("checkMpegHandle") int mpeg, int mode, int width, int height, TPointer32 resultAddr) {
        if ((width & 15) != 0 || (height & 15) != 0 || width > 480 || height > 272) {
            log.warn("sceMpegAvcQueryYCbCrSize invalid size width=" + width + ", height=" + height);
        	return SceKernelErrors.ERROR_MPEG_INVALID_VALUE;
        }

    	// Write the size of the buffer used by sceMpegAvcDecodeYCbCr
		int size = (width / 2) * (height / 2) * 6 + 128;
        resultAddr.setValue(size);

        return 0;
    }

    /**
     * sceMpegAvcInitYCbCr
     * 
     * @param mpeg
     * @param mode
     * @param width
     * @param height
     * @param ycbcr_addr
     * 
     * @return
     */
    @HLEFunction(nid = 0x67179B1B, version = 150, checkInsideInterrupt = true)
    public int sceMpegAvcInitYCbCr(@CheckArgument("checkMpegHandle") int mpeg, int mode, int width, int height, int ycbcr_addr) {
        encodedVideoFramesYCbCr.remove(ycbcr_addr);
        return 0;
    }

    /**
     * sceMpegAvcDecodeYCbCr
     * 
     * @param mpeg
     * @param au_addr
     * @param buffer_addr
     * @param init_addr
     * 
     * @return
     */
    @HLEFunction(nid = 0xF0EB1125, version = 150, checkInsideInterrupt = true)
    public int sceMpegAvcDecodeYCbCr(@CheckArgument("checkMpegHandle") int mpeg, TPointer auAddr, TPointer32 bufferAddr, TPointer32 initAddr) {
        if (mpegRingbuffer != null) {
            mpegRingbuffer.read(mpegRingbufferAddr);
        }

        if (mpegRingbuffer == null) {
            log.warn("sceMpegAvcDecodeYCbCr ringbuffer not created");
            return -1;
        }

        if (mpegRingbuffer.packetsRead == 0 || (!checkMediaEngineState() && mpegRingbuffer.isEmpty())) {
            log.debug("sceMpegAvcDecodeYCbCr ringbuffer empty");
            return SceKernelErrors.ERROR_AVC_VIDEO_FATAL;
        }

        // Decode the video data in YCbCr mode.
    	long startTime = Emulator.getClock().microTime();

        int packetsInRingbuffer = mpegRingbuffer.packets - mpegRingbuffer.packetsFree;
        int processedPackets = mpegRingbuffer.packetsRead - packetsInRingbuffer;
        int processedSize = processedPackets * mpegRingbuffer.packetSize;

        // let's go with 3 packets per frame for now
        int packetsConsumed = 3;
        if (mpegStreamSize > 0 && mpegLastTimestamp > 0) {
            // Try a better approximation of the packets consumed based on the timestamp
            int processedSizeBasedOnTimestamp = (int) ((((float) mpegAvcAu.pts) / mpegLastTimestamp) * mpegStreamSize);
            if (processedSizeBasedOnTimestamp < processedSize) {
                packetsConsumed = 0;
            } else {
                packetsConsumed = (processedSizeBasedOnTimestamp - processedSize) / mpegRingbuffer.packetSize;
                if (packetsConsumed > 10) {
                    packetsConsumed = 10;
                }
            }
            if (log.isDebugEnabled()) {
                log.debug(String.format("sceMpegAvcDecodeYCbCr consumed %d %d/%d %d", processedSizeBasedOnTimestamp, processedSize, mpegStreamSize, packetsConsumed));
            }
        }

        // sceMpegAvcDecodeYCbCr() is performing the video decoding and
        // sceMpegAvcCsc() is transforming the YCbCr image into ABGR.
        // Both the MediaEngine and the JpcspConnector are supporting
        // this 2 steps approach.
        if (checkMediaEngineState()) {
            if (me.stepVideo()) {
            	packetsConsumed = meChannel.getReadLength() / mpegRingbuffer.packetSize;
            	meChannel.setReadLength(meChannel.getReadLength() - packetsConsumed * mpegRingbuffer.packetSize);
            } else {
            	// Consume all the remaining packets
            	packetsConsumed = mpegRingbuffer.packets - mpegRingbuffer.packetsFree;
            }
            avcFrameStatus = 1;
        } else if (isEnableConnector()) {
        	// Store the encoded video frame for real decoding in sceMpegAvcCsc()
        	byte[] encodedVideoFrame = mpegCodec.readEncodedVideoFrame(videoFrameCount);
        	if (encodedVideoFrame != null) {
            	int buffer = bufferAddr.getValue();
            	encodedVideoFramesYCbCr.put(buffer, encodedVideoFrame);
        	}
        	packetsConsumed = 0;
        	avcFrameStatus = 1;
        } else {
            mpegAvcAu.pts += videoTimestampStep;
            updateAvcDts();

            packetsConsumed = 0;
            avcFrameStatus = 1;
        }

        videoFrameCount++;

        if (log.isDebugEnabled()) {
            log.debug(String.format("sceMpegAvcDecodeYCbCr currentTimestamp=%d", mpegAvcAu.pts));
        }

        if (mpegRingbuffer.packetsFree < mpegRingbuffer.packets && packetsConsumed > 0) {
            mpegRingbuffer.packetsFree = Math.min(mpegRingbuffer.packets, mpegRingbuffer.packetsFree + packetsConsumed);
            mpegRingbuffer.write(mpegRingbufferAddr);
        }

        // Correct decoding.
        avcDecodeResult = MPEG_AVC_DECODE_SUCCESS;
        // Save the current frame's status (0 - not showing / 1 - showing).
        initAddr.setValue(avcFrameStatus);
        delayThread(startTime, avcDecodeDelay);

        return 0;
    }

    /**
     * sceMpegAvcDecodeStopYCbCr
     * 
     * @param mpeg
     * @param buffer_addr
     * @param status_addr
     * 
     * @return
     */
    @HLEFunction(nid = 0xF2930C9C, version = 150, checkInsideInterrupt = true)
    public int sceMpegAvcDecodeStopYCbCr(@CheckArgument("checkMpegHandle") int mpeg, TPointer bufferAddr, TPointer32 statusAddr) {
        // No last frame generated
        statusAddr.setValue(0);

        return 0;
    }

    /**
     * sceMpegAvcCsc
     * 
     * @param mpeg          -
     * @param source_addr   - YCbCr data.
     * @param range_addr    - YCbCr range.
     * @param frameWidth    -
     * @param dest_addr     - Converted data (RGB).
     * 
     * @return
     */
    @HLEFunction(nid = 0x31BD0272, version = 150, checkInsideInterrupt = true)
    public int sceMpegAvcCsc(@CheckArgument("checkMpegHandle") int mpeg, TPointer sourceAddr, TPointer32 rangeAddr, int frameWidth, TPointer destAddr) {
        // When frameWidth is 0, take the frameWidth specified at sceMpegCreate.
        if (frameWidth == 0) {
            if (defaultFrameWidth == 0) {
                frameWidth = avcDetailFrameWidth;
            } else {
                frameWidth = defaultFrameWidth;
            }
        }
        if (mpegRingbuffer != null) {
            mpegRingbuffer.read(mpegRingbufferAddr);
        }

        if (mpegRingbuffer == null) {
            log.warn("sceMpegAvcCsc ringbuffer not created");
            return -1;
        }

        if (mpegRingbuffer.packetsRead == 0 || (!checkMediaEngineState() && mpegRingbuffer.isEmpty())) {
            log.debug("sceMpegAvcCsc ringbuffer empty");
            return SceKernelErrors.ERROR_AVC_VIDEO_FATAL;
        }

        int rangeWidthStart = rangeAddr.getValue(0);
        int rangeHeightStart = rangeAddr.getValue(4);
        int rangeWidthEnd = rangeAddr.getValue(8);
        int rangeHeightEnd = rangeAddr.getValue(12);
        if (log.isDebugEnabled()) {
            log.debug(String.format("sceMpegAvcCsc range - x=%d, y=%d, xLen=%d, yLen=%d", rangeWidthStart, rangeHeightStart, rangeWidthEnd, rangeHeightEnd));
        }

        // Do not cache the video image as a texture in the VideoEngine to allow fluid rendering
        VideoEngine.getInstance().addVideoTexture(destAddr.getAddress(), destAddr.getAddress() + (rangeHeightStart + rangeHeightEnd) * frameWidth * sceDisplay.getPixelFormatBytes(videoPixelMode));

        // sceMpegAvcDecodeYCbCr() is performing the video decoding and
        // sceMpegAvcCsc() is transforming the YCbCr image into ABGR.
        // Currently, only the MediaEngine is supporting these 2 steps approach.
        // The other methods (JpcspConnector and Faked video) are performing
        // both steps together: this is done in here.
        if (checkMediaEngineState()) {
            if (me.getContainer() != null) {
                me.writeVideoImageWithRange(destAddr.getAddress(), frameWidth, videoPixelMode, rangeWidthStart, rangeHeightStart, rangeWidthEnd, rangeHeightEnd);
            }
        } else {
        	long startTime = Emulator.getClock().microTime();

            int packetsInRingbuffer = mpegRingbuffer.packets - mpegRingbuffer.packetsFree;
            int processedPackets = mpegRingbuffer.packetsRead - packetsInRingbuffer;
            int processedSize = processedPackets * mpegRingbuffer.packetSize;

            // let's go with 3 packets per frame for now
            int packetsConsumed = 3;
            if (mpegStreamSize > 0 && mpegLastTimestamp > 0) {
                // Try a better approximation of the packets consumed based on the timestamp
                int processedSizeBasedOnTimestamp = (int) ((((float) mpegAvcAu.pts) / mpegLastTimestamp) * mpegStreamSize);
                if (processedSizeBasedOnTimestamp < processedSize) {
                    packetsConsumed = 0;
                } else {
                    packetsConsumed = (processedSizeBasedOnTimestamp - processedSize) / mpegRingbuffer.packetSize;
                    if (packetsConsumed > 10) {
                        packetsConsumed = 10;
                    }
                }
                if (log.isDebugEnabled()) {
                    log.debug(String.format("sceMpegAvcCsc consumed %d %d/%d %d", processedSizeBasedOnTimestamp, processedSize, mpegStreamSize, packetsConsumed));
                }
            }

            if (isEnableConnector() && encodedVideoFramesYCbCr.containsKey(sourceAddr.getAddress())) {
            	byte[] encodedVideoFrame = encodedVideoFramesYCbCr.get(sourceAddr.getAddress());
                mpegCodec.decodeVideoFrame(encodedVideoFrame, destAddr.getAddress(), frameWidth, rangeWidthEnd, rangeHeightEnd, videoPixelMode, videoFrameCount);
                packetsConsumed = mpegCodec.getPacketsConsumed();
            } else {
                // Generate static.
                generateFakeMPEGVideo(destAddr.getAddress(), frameWidth);
                if (isEnableConnector()) {
                    mpegCodec.postFakedVideo(destAddr.getAddress(), frameWidth, videoPixelMode);
                }
                Date currentDate = convertTimestampToDate(mpegAvcAu.pts);
                if (log.isDebugEnabled()) {
                    log.debug(String.format("currentDate: %s", currentDate.toString()));
                }
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                Debug.printFramebuffer(destAddr.getAddress(), frameWidth, 10, 250, 0xFFFFFFFF, 0xFF000000, videoPixelMode, 1, " This is a faked MPEG video (in YCbCr mode). ");
                String displayedString;
                if (mpegLastDate != null) {
                    displayedString = String.format(" %s / %s ", dateFormat.format(currentDate), dateFormat.format(mpegLastDate));
                    Debug.printFramebuffer(destAddr.getAddress(), frameWidth, 10, 10, 0xFFFFFFFF, 0xFF000000, videoPixelMode, 2, displayedString);
                }
                if (mpegStreamSize > 0) {
                    displayedString = String.format(" %d/%d (%.0f%%) ", processedSize, mpegStreamSize, processedSize * 100f / mpegStreamSize);
                } else {
                    displayedString = String.format(" %d ", processedSize);
                }
                Debug.printFramebuffer(destAddr.getAddress(), frameWidth, 10, 30, 0xFFFFFFFF, 0xFF000000, videoPixelMode, 2, displayedString);
                if (log.isDebugEnabled()) {
                    log.debug(String.format("sceMpegAvcCsc currentTimestamp=%d", mpegAvcAu.pts));
                }
            }

            if (log.isDebugEnabled()) {
                log.debug(String.format("sceMpegAvcCsc currentTimestamp=%d", mpegAvcAu.pts));
            }

            if (mpegRingbuffer.packetsFree < mpegRingbuffer.packets && packetsConsumed > 0) {
                mpegRingbuffer.packetsFree = Math.min(mpegRingbuffer.packets, mpegRingbuffer.packetsFree + packetsConsumed);
                mpegRingbuffer.write(mpegRingbufferAddr);
            }
            delayThread(startTime, avcDecodeDelay);
        }
        return 0;
    }

    /**
     * sceMpegAtracDecode
     * 
     * @param mpeg
     * @param au_addr
     * @param buffer_addr
     * @param init
     * 
     * @return
     */
    @HLEFunction(nid = 0x800C44DF, version = 150, checkInsideInterrupt = true)
    public int sceMpegAtracDecode(@CheckArgument("checkMpegHandle") int mpeg, TPointer auAddr, TPointer bufferAddr, int init) {
        Memory mem = Processor.memory;

    	long startTime = Emulator.getClock().microTime();
        mpegAtracAu.pts += audioTimestampStep;

        // External audio setup.
        if (checkMediaEngineState()) {
        	Emulator.getClock().pause();
        	int bytes = 0;
        	if (me.stepAudio(MPEG_ATRAC_ES_OUTPUT_SIZE)) {
                bytes = me.getCurrentAudioSamples(audioDecodeBuffer);
                mem.copyToMemory(bufferAddr.getAddress(), ByteBuffer.wrap(audioDecodeBuffer, 0, bytes), bytes);
        	}
        	// Fill the rest of the buffer with 0's
        	bufferAddr.clear(bytes, MPEG_ATRAC_ES_OUTPUT_SIZE - bytes);
        	Emulator.getClock().resume();
        } else if (isEnableConnector() && mpegCodec.readAudioFrame(bufferAddr.getAddress(), audioFrameCount)) {
            mpegAtracAu.pts = mpegCodec.getMpegAtracCurrentTimestamp();
        } else {
            bufferAddr.clear(MPEG_ATRAC_ES_OUTPUT_SIZE);
        }
        audioFrameCount++;
        if (log.isDebugEnabled()) {
            log.debug(String.format("sceMpegAtracDecode currentTimestamp=%d", mpegAtracAu.pts));
        }

        delayThread(startTime, atracDecodeDelay);

        return 0;
    }

    protected int getPacketsFromSize(int size) {
    	int packets = size / (2048 + 104);

    	return packets;
    }

    private int getSizeFromPackets(int packets) {
        int size = (packets * 104) + (packets * 2048);

        return size;
    }

    /**
     * sceMpegRingbufferQueryMemSize
     * 
     * @param packets
     * 
     * @return
     */
    @HLEFunction(nid = 0xD7A29F46, version = 150, checkInsideInterrupt = true)
    public int sceMpegRingbufferQueryMemSize(int packets) {
        return getSizeFromPackets(packets);
    }

    /**
     * sceMpegRingbufferConstruct
     * 
     * @param ringbuffer_addr
     * @param packets
     * @param data
     * @param size
     * @param callback_addr
     * @param callback_args
     * 
     * @return
     */
    @HLEFunction(nid = 0x37295ED8, version = 150, checkInsideInterrupt = true)
    public int sceMpegRingbufferConstruct(TPointer ringbufferAddr, int packets, TPointer data, int size, @CanBeNull TPointer callbackAddr, int callbackArgs) {
        if (size < getSizeFromPackets(packets)) {
            log.warn(String.format("sceMpegRingbufferConstruct insufficient space: size=%d, packets=%d", size, packets));
            return SceKernelErrors.ERROR_MPEG_NO_MEMORY;
        }

        SceMpegRingbuffer ringbuffer = new SceMpegRingbuffer(packets, data.getAddress(), size, callbackAddr.getAddress(), callbackArgs);
        ringbuffer.write(ringbufferAddr);
        maxAheadTimestamp = getMaxAheadTimestamp(packets);

        return 0;
    }

    /**
     * sceMpegRingbufferDestruct
     * 
     * @param ringbuffer_addr
     * 
     * @return
     */
    @HLEFunction(nid = 0x13407F13, version = 150, checkInsideInterrupt = true)
    public int sceMpegRingbufferDestruct(TPointer ringbufferAddr) {
        return 0;
    }

    /**
     * sceMpegRingbufferPut
     * 
     * @param _mpegRingbufferAddr
     * @param numPackets
     * @param available
     * 
     * @return
     */
    @HLEFunction(nid = 0xB240A59E, version = 150, checkInsideInterrupt = true)
    public int sceMpegRingbufferPut(TPointer ringbufferAddr, int numPackets, int available) {
        mpegRingbufferAddr = ringbufferAddr;

        if (numPackets < 0) {
            return 0;
        }

        int numberPackets = Math.min(available, numPackets);

        if (!isCurrentMpegAnalyzed()) {
        	// The MPEG header has not yet been analyzed, try to read it using an IoListener...
        	if (ioListener == null) {
        		ioListener = new MpegIoListener();
        		Modules.IoFileMgrForUserModule.registerIoListener(ioListener);
        	}
        }

        // Note: we can read more packets than available in the Mpeg stream: the application
        // can loop the video by putting previous packets back into the ringbuffer.

        mpegRingbuffer.read(mpegRingbufferAddr);
        insideRingbufferPut = true;
        Modules.ThreadManForUserModule.executeCallback(null, mpegRingbuffer.callback_addr, afterRingbufferPutCallback, false, mpegRingbuffer.data, numberPackets, mpegRingbuffer.callback_args);

        return Emulator.getProcessor().cpu.gpr[_v0];
    }

    /**
     * sceMpegRingbufferAvailableSize
     * 
     * @param _mpegRingbufferAddr
     * 
     * @return
     */
    @HLEFunction(nid = 0xB5F6DC87, version = 150, checkInsideInterrupt = true)
    public int sceMpegRingbufferAvailableSize(TPointer ringbufferAddr) {
        mpegRingbufferAddr = ringbufferAddr;

    	mpegRingbuffer.read(mpegRingbufferAddr);
        if (log.isDebugEnabled()) {
            log.debug(String.format("sceMpegRingbufferAvailableSize returning %d", mpegRingbuffer.packetsFree));
        }

        return mpegRingbuffer.packetsFree;
    }

    /**
     * sceMpegNextAvcRpAu
     * 
     * @param p1
     * @param p2
     * @param p3
     * @param p4
     * 
     * @return
     */
    @HLEUnimplemented
    @HLEFunction(nid = 0x3C37A7A6, version = 150, checkInsideInterrupt = true)
    public int sceMpegNextAvcRpAu(int p1, int p2, int p3, int p4) {
        return 0;
    }

    @HLEUnimplemented
    @HLEFunction(nid = 0x01977054, version = 150)
    public int sceMpegGetUserdataAu() {
        return 0;
    }

    @HLEUnimplemented
    @HLEFunction(nid = 0xC45C99CC, version = 150)
    public int sceMpegQueryUserdataEsSize() {
        return 0;
    }

    @HLEUnimplemented
    @HLEFunction(nid = 0x0558B075, version = 150)
    public int sceMpegAvcCopyYCbCr(@CheckArgument("checkMpegHandle") int mpeg, TPointer sourceAddr, TPointer YCbCrAddr) {
        return 0;
    }

    @HLEUnimplemented
    @HLEFunction(nid = 0x11F95CF1, version = 150)
    public int sceMpegGetAvcNalAu() {
        return 0;
    }

    @HLEUnimplemented
    @HLEFunction(nid = 0x921FCCCF, version = 150)
    public int sceMpegGetAvcEsAu() {
        return 0;
    }

    @HLEUnimplemented
    @HLEFunction(nid = 0x6F314410, version = 150)
    public int sceMpegAvcDecodeGetDecodeSEI() {
        return 0;
    }

    @HLEUnimplemented
    @HLEFunction(nid = 0xAB0E9556, version = 150)
    public int sceMpegAvcDecodeDetailIndex() {
        return 0;
    }

    @HLEUnimplemented
    @HLEFunction(nid = 0xCF3547A2, version = 150)
    public int sceMpegAvcDecodeDetail2() {
        return 0;
    }

    @HLEUnimplemented
    @HLEFunction(nid = 0xF5E7EA31, version = 150)
    public int sceMpegAvcConvertToYuv420(int mpeg, TPointer bufferOutput, TPointer unknown1, int unknown2) {
        return 0;
    }

    @HLEUnimplemented
    @HLEFunction(nid = 0xD1CE4950, version = 150)
    public int sceMpegAvcCscMode() {
        return 0;
    }

    @HLEUnimplemented
    @HLEFunction(nid = 0xDBB60658, version = 150)
    public int sceMpegFlushAu() {
        return 0;
    }

    @HLEUnimplemented
    @HLEFunction(nid = 0xE95838F6, version = 150)
    public int sceMpegAvcCscInfo() {
        return 0;
    }

    @HLEUnimplemented
    @HLEFunction(nid = 0x11CAB459, version = 150)
    public int sceMpeg_11CAB459() {
        return 0;
    }

    @HLEUnimplemented
    @HLEFunction(nid = 0xB27711A8, version = 150)
    public int sceMpeg_B27711A8() {
        return 0;
    }

    @HLEUnimplemented
    @HLEFunction(nid = 0xD4DD6E75, version = 150)
    public int sceMpeg_D4DD6E75() {
        return 0;
    }

    @HLEUnimplemented
    @HLEFunction(nid = 0xC345DED2, version = 150)
    public int sceMpeg_C345DED2() {
        return 0;
    }

    @HLEUnimplemented
    @HLEFunction(nid = 0x988E9E12, version = 150)
    public int sceMpeg_988E9E12() {
        return 0;
    }
}