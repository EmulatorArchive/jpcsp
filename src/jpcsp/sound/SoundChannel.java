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
package jpcsp.sound;

import java.nio.ByteBuffer;

import jpcsp.HLE.Modules;

import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;

public class SoundChannel {
	public static final int FORMAT_MONO = 0x10;
	public static final int FORMAT_STEREO = 0x00;
    //
    // The PSP is using a buffer equal to the sampleSize.
    // However, the audio data is not always streamed as fast on Jpcsp as on
    // a real PSP which can lead to buffer underflows,
    // causing discontinuities in the audio that are perceived as "clicks".
    //
    // So, we allocate several buffers of sampleSize: 10 buffers is an
    // empirical value.
    // This has the disadvantage to introduce a small delay when playing
    // a new sound: a PSP application is typically sending continuously
    // sound data, even when nothing can be heard ("0" values are sent).
    // And we have first to play these buffered blanks before hearing
    // the real sound itself.
	private static final int NUMBER_BLOCKING_BUFFERS = 10;
	private static final int DEFAULT_VOLUME = 0x8000;
	private static final int DEFAULT_SAMPLE_RATE = 48000;
	private SoundBufferManager soundBufferManager;
	private int index;
	private boolean reserved;
	private int leftVolume;
	private int rightVolume;
    private int alSource;
    private int sampleRate;
    private int sampleLength;
    private int format;

    public static void init() {
		if (!AL.isCreated()) {
			try {
				AL.create();
			} catch (LWJGLException e) {
				Modules.log.error(e);
			}
		}
        // Add a shutdown hook to automatically remove OpenAL's .dll from Java's
        // heap to avoid crashing when exiting the emulator.
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                if (AL.isCreated()) {
                    AL.destroy();
                }
            }
        }));
    }

    public SoundChannel(int index) {
		soundBufferManager = SoundBufferManager.getInstance();
		this.index = index;
		reserved = false;
		leftVolume = DEFAULT_VOLUME;
		rightVolume = DEFAULT_VOLUME;
		alSource = AL10.alGenSources();
		sampleRate = DEFAULT_SAMPLE_RATE;

		AL10.alSourcei(alSource, AL10.AL_LOOPING, AL10.AL_FALSE);
	}

	public int getIndex() {
		return index;
	}

	public boolean isReserved() {
		return reserved;
	}

	public void setReserved(boolean reserved) {
		this.reserved = reserved;
	}

	public int getLeftVolume() {
		return leftVolume;
	}

	public void setLeftVolume(int leftVolume) {
		this.leftVolume = leftVolume;
	}

	public int getRightVolume() {
		return rightVolume;
	}

	public void setRightVolume(int rightVolume) {
		this.rightVolume = rightVolume;
	}

	public int getSampleLength() {
		return sampleLength;
	}

	public void setSampleLength(int sampleLength) {
		this.sampleLength = sampleLength;
	}

	public int getFormat() {
		return format;
	}

	public void setFormat(int format) {
		this.format = format;
	}

	public boolean isFormatStereo() {
		return (format & FORMAT_MONO) == FORMAT_STEREO;
	}

	public boolean isFormatMono() {
		return (format & FORMAT_MONO) == FORMAT_MONO;
	}

	public int getSampleRate() {
		return sampleRate;
	}

	public void setSampleRate(int sampleRate) {
		this.sampleRate = sampleRate;
	}

	private void alSourcePlay() {
		int state = AL10.alGetSourcei(alSource, AL10.AL_SOURCE_STATE);
		if (state != AL10.AL_PLAYING) {
			AL10.alSourcePlay(alSource);
		}
    }

    private void alSourceQueueBuffer(byte[] buffer) {
    	int alBuffer = soundBufferManager.getBuffer();
    	ByteBuffer directBuffer = soundBufferManager.getDirectBuffer(buffer.length);
		directBuffer.clear();
		directBuffer.limit(buffer.length);
		directBuffer.put(buffer);
		directBuffer.rewind();
		int alFormat = isFormatStereo() ? AL10.AL_FORMAT_STEREO16 : AL10.AL_FORMAT_MONO16;
		AL10.alBufferData(alBuffer, alFormat, directBuffer, getSampleRate());
		AL10.alSourceQueueBuffers(alSource, alBuffer);
		soundBufferManager.releaseDirectBuffer(directBuffer);
		alSourcePlay();
		checkFreeBuffers();

		if (Modules.log.isDebugEnabled()) {
			Modules.log.debug(String.format("alSourceQueueBuffer buffer=%d, %s", alBuffer, toString()));
		}
    }

    private void checkFreeBuffers() {
    	soundBufferManager.checkFreeBuffers(alSource);
    }

    public void release() {
    	AL10.alSourceStop(alSource);
    	checkFreeBuffers();
    }

    public void play(byte[] buffer) {
    	alSourceQueueBuffer(buffer);
    }

    private int getWaitingBuffers() {
    	checkFreeBuffers();

    	return AL10.alGetSourcei(alSource, AL10.AL_BUFFERS_QUEUED);
    }

    private int getSourceSampleOffset() {
    	int sampleOffset = AL10.alGetSourcei(alSource, AL11.AL_SAMPLE_OFFSET);
    	if (isFormatStereo()) {
    		sampleOffset /= 2;
    	}

    	return sampleOffset;
    }

    public boolean isOutputBlocking() {
    	return getWaitingBuffers() >= NUMBER_BLOCKING_BUFFERS;
    }

    public int getUnblockOutputDelayMicros() {
    	// Return the delay required for the processing of the playing buffer
    	if (isEnded()) {
    		return 0;
    	}
    	float delaySecs = (getSampleLength() - getSourceSampleOffset()) / (float) getSampleRate();
    	int delayMicros = (int) (delaySecs * 1000000);

    	return delayMicros;
    }

    public int getRestLength() {
    	int restLength = getWaitingBuffers() * getSampleLength();
    	if (!isEnded()) {
    		restLength += getSampleLength() - getSourceSampleOffset();
    	}

    	return restLength;
    }

    public boolean isEnded() {
    	checkFreeBuffers();

    	int state = AL10.alGetSourcei(alSource, AL10.AL_SOURCE_STATE);
		if (state == AL10.AL_PLAYING) {
			return false;
		}

		return true;
    }

    @Override
	public String toString() {
		StringBuilder s = new StringBuilder();

		s.append(String.format("SoundChannel[%d](", index));
		s.append(String.format("sourceSampleOffset=%d", getSourceSampleOffset()));
		s.append(String.format(", restLength=%d", getRestLength()));
		s.append(String.format(", buffers queued=%d", getWaitingBuffers()));
		s.append(String.format(", isOutputBlock=%b", isOutputBlocking()));
		s.append(String.format(", %s", isFormatStereo() ? "Stereo" : "Mono"));
		s.append(String.format(", reserved=%b", reserved));
		s.append(String.format(", sampleLength=%d", getSampleLength()));
		s.append(String.format(", sampleRate=%d", getSampleRate()));
		s.append(")");

		return s.toString();
	}

    public static short adjustSample(short sample, int volume) {
        return (short) ((((int) sample) * volume) >> 16);
    }

    public static void storeSample(short sample, byte[] data, int index) {
    	data[index] = (byte) sample;
    	data[index + 1] = (byte) (sample >> 8);
    }
}