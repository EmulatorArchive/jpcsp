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

import jpcsp.HLE.HLEFunction;
import jpcsp.Emulator;
import jpcsp.Memory;
import jpcsp.Processor;
import jpcsp.Allegrex.CpuState;
import jpcsp.HLE.Modules;
import jpcsp.HLE.kernel.managers.IntrManager;
import jpcsp.HLE.kernel.types.IAction;
import jpcsp.HLE.kernel.types.SceKernelErrors;
import jpcsp.HLE.kernel.types.SceKernelThreadInfo;
import jpcsp.HLE.modules.HLEModule;
import jpcsp.HLE.modules.HLEModuleFunction;
import jpcsp.HLE.modules.HLEModuleManager;
import jpcsp.HLE.modules.HLEStartModule;
import jpcsp.hardware.Audio;
import jpcsp.memory.IMemoryReader;
import jpcsp.memory.MemoryReader;
import jpcsp.sound.AudioBlockingOutputAction;
import jpcsp.sound.SoundChannel;

import org.apache.log4j.Logger;

public class sceAudio implements HLEModule, HLEStartModule {
    public static Logger log = Modules.getLogger("sceAudio");

    @Override
    public String getName() {
        return "sceAudio";
    }

	@Override
	public void start() {
		SoundChannel.init();

        // The audio driver is capable of handling PCM and VAG (ADPCM) playback,
        // but it uses the same channels for this processing.
        // E.g.: Use channels 0 to 4 to playback 4 VAG files or use channels 0 to 2
        // to playback raw PCM data.
        // Note: Currently, working with pspPCMChannels only is enough.
        pspPCMChannels = new SoundChannel[PSP_AUDIO_CHANNEL_MAX];
        for (int channel = 0; channel < pspPCMChannels.length; channel++) {
            pspPCMChannels[channel] = new SoundChannel(channel);
        }
        pspSRCChannel = new SoundChannel(8);  // Use a special channel 8 to handle SRC functions.
	}

	@Override
	public void stop() {
	}

	protected static final int PSP_AUDIO_VOLUME_MAX = 0x8000;
    protected static final int PSP_AUDIO_CHANNEL_MAX = 8;
    protected static final int PSP_AUDIO_SAMPLE_MIN = 64;
    protected static final int PSP_AUDIO_SAMPLE_MAX = 65472;
    protected static final int PSP_AUDIO_FORMAT_STEREO = 0;
    protected static final int PSP_AUDIO_FORMAT_MONO = 0x10;

    protected SoundChannel[] pspPCMChannels;
    protected SoundChannel pspSRCChannel;

    protected boolean disableChReserve;
    protected boolean disableBlockingAudio;

    public void setChReserveEnabled(boolean enabled) {
        disableChReserve = !enabled;
        log.info("Audio ChReserve disabled: " + disableChReserve);
    }

    public void setBlockingEnabled(boolean enabled) {
        disableBlockingAudio = !enabled;
        log.info("Audio Blocking disabled: " + disableBlockingAudio);
    }

    protected int doAudioOutput(SoundChannel channel, int pvoid_buf) {
        int ret = -1;

        if (channel.isReserved()) {
        	if (log.isDebugEnabled()) {
        		log.debug(String.format("doAudioOutput(%s, 0x%08X)", channel.toString(), pvoid_buf));
        	}
            int bytesPerSample = channel.isFormatStereo() ? 4 : 2;
            int nbytes = bytesPerSample * channel.getSampleLength();
            byte[] data = new byte[nbytes];

            IMemoryReader memoryReader = MemoryReader.getMemoryReader(pvoid_buf, nbytes, 2);
            if (channel.isFormatMono()) {
                int volume = Audio.getVolume(channel.getLeftVolume());
                for (int i = 0; i < nbytes; i += 2) {
                    short sample = (short) memoryReader.readNext();

                    sample = SoundChannel.adjustSample(sample, volume);

                    SoundChannel.storeSample(sample, data, i);
                }
            } else {
                int leftVolume = Audio.getVolume(channel.getLeftVolume());
                int rightVolume = Audio.getVolume(channel.getRightVolume());
                for (int i = 0; i < nbytes; i += 4) {
                    short lsample = (short) memoryReader.readNext();
                    short rsample = (short) memoryReader.readNext();

                    lsample = SoundChannel.adjustSample(lsample, leftVolume);
                    rsample = SoundChannel.adjustSample(rsample, rightVolume);

                    SoundChannel.storeSample(lsample, data, i);
                    SoundChannel.storeSample(rsample, data, i + 2);
                }
            }
            channel.play(data);
            ret = channel.getSampleLength();
        } else {
            log.warn("doAudioOutput: channel " + channel.getIndex() + " not reserved");
        }
        return ret;
    }

    protected void blockThreadOutput(SoundChannel channel, int addr, int leftVolume, int rightVolume) {
        ThreadManForUser threadMan = Modules.ThreadManForUserModule;
    	blockThreadOutput(threadMan.getCurrentThreadID(), channel, addr, leftVolume, rightVolume);
    	threadMan.hleBlockCurrentThread();
    }

    protected void blockThreadOutput(int threadId, SoundChannel channel, int addr, int leftVolume, int rightVolume) {
    	IAction action = new AudioBlockingOutputAction(threadId, channel, addr, leftVolume, rightVolume);
    	int delayMicros = channel.getUnblockOutputDelayMicros(addr == 0);
    	long schedule = Emulator.getClock().microTime() + delayMicros;
    	Emulator.getScheduler().addAction(schedule, action);
    }

    public void hleAudioBlockingOutput(int threadId, SoundChannel channel, int addr, int leftVolume, int rightVolume) {
    	if (log.isDebugEnabled()) {
    		log.debug(String.format("hleAudioBlockingOutput %s", channel.toString()));
    	}

    	if (addr == 0) {
    		// Waiting for complete audio drain
    		if (!channel.isDrained()) {
            	blockThreadOutput(threadId, channel, addr, leftVolume, rightVolume);
    		} else {
                ThreadManForUser threadMan = Modules.ThreadManForUserModule;
                SceKernelThreadInfo thread = threadMan.getThreadById(threadId);
                if (thread != null) {
                	thread.cpuContext.gpr[2] = 0;
                    threadMan.hleUnblockThread(threadId);
                }
    		}
    	} else if (!channel.isOutputBlocking()) {
            ThreadManForUser threadMan = Modules.ThreadManForUserModule;
            SceKernelThreadInfo thread = threadMan.getThreadById(threadId);
            if (thread != null) {
                changeChannelVolume(channel, leftVolume, rightVolume);
                int ret = doAudioOutput(channel, addr);
                thread.cpuContext.gpr[2] = ret;
                threadMan.hleUnblockThread(threadId);
            }
        } else {
        	blockThreadOutput(threadId, channel, addr, leftVolume, rightVolume);
        }
    }

    protected int changeChannelVolume(SoundChannel channel, int leftvol, int rightvol) {
        int ret = -1;

        if (channel.isReserved()) {
            channel.setLeftVolume(leftvol);
            channel.setRightVolume(rightvol);
            ret = 0;
        }

        return ret;
    }

    protected int hleAudioGetChannelRestLen(SoundChannel channel) {
        int len = channel.getRestLength();

        if (log.isDebugEnabled()) {
            log.debug(String.format("hleAudioGetChannelRestLen(%d) = %d", channel.getIndex(), len));
        }

        return len;
    }

    protected void hleAudioSRCChReserve(Processor processor, int samplecount, int freq, int format) {
        CpuState cpu = processor.cpu;

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_KERNEL_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }

        if (disableChReserve) {
            log.warn("IGNORED hleAudioSRCChReserve samplecount= " + samplecount + " freq= " + freq + " format=" + format);
            cpu.gpr[2] = -1;
        } else {
            if (!pspSRCChannel.isReserved()) {
            	pspSRCChannel.setSampleRate(freq);
                pspSRCChannel.setReserved(true);
                pspSRCChannel.setSampleLength(samplecount);
                pspSRCChannel.setFormat(format);
            }
        }
        cpu.gpr[2] = 0;
    }

    @HLEFunction(nid = 0x80F1F7E0, version = 150, moduleName = "sceAudio_driver")
    public void sceAudioInit(Processor processor) {
        CpuState cpu = processor.cpu;

        System.out.println("Unimplemented NID function sceAudioInit [0x80F1F7E0]");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    @HLEFunction(nid = 0x210567F7, version = 150, moduleName = "sceAudio_driver")
    public void sceAudioEnd(Processor processor) {
        CpuState cpu = processor.cpu;

        System.out.println("Unimplemented NID function sceAudioEnd [0x210567F7]");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    @HLEFunction(nid = 0xA2BEAA6C, version = 150, moduleName = "sceAudio_driver")
    public void sceAudioSetFrequency(Processor processor) {
        CpuState cpu = processor.cpu;

        int frequency = cpu.gpr[4];

        if (frequency == 44100 || frequency == 48000) {
        	for (int i = 0; i < pspPCMChannels.length; i++) {
        		pspPCMChannels[i].setSampleRate(frequency);
        	}
            cpu.gpr[2] = 0;
        } else {
            cpu.gpr[2] = -1;
        }
    }

    @HLEFunction(nid = 0xB61595C0, version = 150, moduleName = "sceAudio_driver")
    public void sceAudioLoopbackTest(Processor processor) {
        CpuState cpu = processor.cpu;

        System.out.println("Unimplemented NID function sceAudioLoopbackTest [0xB61595C0]");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    @HLEFunction(nid = 0x927AC32B, version = 150, moduleName = "sceAudio_driver")
    public void sceAudioSetVolumeOffset(Processor processor) {
        CpuState cpu = processor.cpu;

        System.out.println("Unimplemented NID function sceAudioSetVolumeOffset [0x927AC32B]");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    @HLEFunction(nid = 0x8C1009B2, version = 150)
    public void sceAudioOutput(Processor processor) {
        CpuState cpu = processor.cpu;

        int channel = cpu.gpr[4];
        int vol = cpu.gpr[5];
        int pvoid_buf = cpu.gpr[6];

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_KERNEL_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        if (!Memory.isAddressGood(pvoid_buf)) {
            log.warn("sceAudioOutput bad pointer " + String.format("0x%08X", pvoid_buf));
            cpu.gpr[2] = SceKernelErrors.ERROR_AUDIO_PRIV_REQUIRED;
        } else {
            if (!pspPCMChannels[channel].isOutputBlocking()) {
                changeChannelVolume(pspPCMChannels[channel], vol, vol);
                cpu.gpr[2] = doAudioOutput(pspPCMChannels[channel], pvoid_buf);
                Modules.ThreadManForUserModule.hleRescheduleCurrentThread();
            } else {
                cpu.gpr[2] = SceKernelErrors.ERROR_AUDIO_CHANNEL_BUSY;
            }
        }
    }

    @HLEFunction(nid = 0x136CAF51, version = 150)
    public void sceAudioOutputBlocking(Processor processor) {
        CpuState cpu = processor.cpu;

        int channel = cpu.gpr[4];
        int vol = cpu.gpr[5];
        int pvoid_buf = cpu.gpr[6];

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_KERNEL_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }

        if (pvoid_buf == 0) {
            if (!pspPCMChannels[channel].isDrained()) {
                if (log.isDebugEnabled()) {
                    log.debug("sceAudioOutputBlocking[pvoid_buf==0] blocking " + pspPCMChannels[channel].toString());
                }
                blockThreadOutput(pspPCMChannels[channel], pvoid_buf, vol, vol);
            } else {
                cpu.gpr[2] = 0;
            }
        } else if (!Memory.isAddressGood(pvoid_buf)) {
            log.warn("sceAudioOutputBlocking bad pointer " + String.format("0x%08X", pvoid_buf));
            cpu.gpr[2] = SceKernelErrors.ERROR_AUDIO_PRIV_REQUIRED;
        } else {
            if (!pspPCMChannels[channel].isOutputBlocking() || disableBlockingAudio) {
                if (log.isDebugEnabled()) {
                    log.debug("sceAudioOutputBlocking[not blocking] " + pspPCMChannels[channel].toString());
                }
                changeChannelVolume(pspPCMChannels[channel], vol, vol);
                cpu.gpr[2] = doAudioOutput(pspPCMChannels[channel], pvoid_buf);
                if (log.isDebugEnabled()) {
                    log.debug("sceAudioOutputBlocking[not blocking] returning " + cpu.gpr[2] + " (" + pspPCMChannels[channel].toString() + ")");
                }
                Modules.ThreadManForUserModule.hleRescheduleCurrentThread();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("sceAudioOutputBlocking[blocking] " + pspPCMChannels[channel].toString());
                }
                blockThreadOutput(pspPCMChannels[channel], pvoid_buf, vol, vol);
            }
        }
    }

    @HLEFunction(nid = 0xE2D56B2D, version = 150)
    public void sceAudioOutputPanned(Processor processor) {
        CpuState cpu = processor.cpu;

        int channel = cpu.gpr[4];
        int leftvol = cpu.gpr[5];
        int rightvol = cpu.gpr[6];
        int pvoid_buf = cpu.gpr[7];

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_KERNEL_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        if (!Memory.isAddressGood(pvoid_buf)) {
            log.warn("sceAudioOutputPanned bad pointer " + String.format("0x%08X", pvoid_buf));
            cpu.gpr[2] = SceKernelErrors.ERROR_AUDIO_PRIV_REQUIRED;
        } else {
            if (!pspPCMChannels[channel].isOutputBlocking()) {
                changeChannelVolume(pspPCMChannels[channel], leftvol, rightvol);
                cpu.gpr[2] = doAudioOutput(pspPCMChannels[channel], pvoid_buf);
                Modules.ThreadManForUserModule.hleRescheduleCurrentThread();
            } else {
                cpu.gpr[2] = SceKernelErrors.ERROR_AUDIO_CHANNEL_BUSY;
            }
        }
    }

    @HLEFunction(nid = 0x13F592BC, version = 150)
    public void sceAudioOutputPannedBlocking(Processor processor) {
        CpuState cpu = processor.cpu;

        int channel = cpu.gpr[4];
        int leftvol = cpu.gpr[5];
        int rightvol = cpu.gpr[6];
        int pvoid_buf = cpu.gpr[7];

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_KERNEL_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }

        if (pvoid_buf == 0) {
            // Tested on PSP:
            // An output adress of 0 is actually a special code for the PSP.
            // It means that we must stall processing until all the previous
        	// unplayed samples' data is output.
            if (!pspPCMChannels[channel].isDrained()) {
                if (log.isDebugEnabled()) {
                    log.debug("sceAudioOutputPannedBlocking[pvoid_buf==0] blocking " + pspPCMChannels[channel].toString());
                }
                blockThreadOutput(pspPCMChannels[channel], pvoid_buf, leftvol, rightvol);
            } else {
                cpu.gpr[2] = 0;
            }
        } else if (!Memory.isAddressGood(pvoid_buf)) {
            log.warn("sceAudioOutputPannedBlocking bad pointer " + String.format("0x%08X", pvoid_buf));
            cpu.gpr[2] = SceKernelErrors.ERROR_AUDIO_PRIV_REQUIRED;
        } else {
            if (!pspPCMChannels[channel].isOutputBlocking() || disableBlockingAudio) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("sceAudioOutputPannedBlocking[not blocking] leftVol=%d, rightVol=%d, channel=%s", leftvol, rightvol, pspPCMChannels[channel].toString()));
                }
                changeChannelVolume(pspPCMChannels[channel], leftvol, rightvol);
                cpu.gpr[2] = doAudioOutput(pspPCMChannels[channel], pvoid_buf);
                Modules.ThreadManForUserModule.hleRescheduleCurrentThread();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("sceAudioOutputPannedBlocking[blocking] leftVol=%d, rightVol=%d, channel=%s", leftvol, rightvol, pspPCMChannels[channel].toString()));
                }
                blockThreadOutput(pspPCMChannels[channel], pvoid_buf, leftvol, rightvol);
            }
        }
    }

    @HLEFunction(nid = 0x5EC81C55, version = 150)
    public void sceAudioChReserve(Processor processor) {
        CpuState cpu = processor.cpu;

        int channel = cpu.gpr[4];
        int samplecount = cpu.gpr[5];
        int format = cpu.gpr[6];

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_KERNEL_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        if (disableChReserve) {
            log.warn("IGNORED sceAudioChReserve channel= " + channel + " samplecount = " + samplecount + " format = " + format);
            cpu.gpr[2] = -1;
        } else {
            log.debug("sceAudioChReserve channel= " + channel + " samplecount = " + samplecount + " format = " + format);

            if (channel != -1) {
                if (pspPCMChannels[channel].isReserved()) {
                    log.warn("sceAudioChReserve failed - channel " + channel + " already in use");
                    channel = -1;
                }
            } else {
                for (int i = 0; i < pspPCMChannels.length; i++) {
                    if (!pspPCMChannels[i].isReserved()) {
                        channel = i;
                        break;
                    }
                }
                if (channel == -1) {
                    log.warn("sceAudioChReserve failed - no free channels available");
                }
            }

            if (channel != -1) {
                pspPCMChannels[channel].setReserved(true);
                pspPCMChannels[channel].setSampleLength(samplecount);
                pspPCMChannels[channel].setFormat(format);
            }

            cpu.gpr[2] = channel;
        }
    }

    @HLEFunction(nid = 0x41EFADE7, version = 150)
    public void sceAudioOneshotOutput(Processor processor) {
        CpuState cpu = processor.cpu;

        System.out.println("Unimplemented NID function sceAudioOneshotOutput [0x41EFADE7]");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    @HLEFunction(nid = 0x6FC46853, version = 150)
    public void sceAudioChRelease(Processor processor) {
        CpuState cpu = processor.cpu;

        int channel = cpu.gpr[4];

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_KERNEL_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        if (pspPCMChannels[channel].isReserved()) {
            pspPCMChannels[channel].release();
            pspPCMChannels[channel].setReserved(false);
            cpu.gpr[2] = 0;
        } else {
            cpu.gpr[2] = -1;
        }
    }

    @HLEFunction(nid = 0xB011922F, version = 150)
    public void sceAudioGetChannelRestLength(Processor processor) {
        CpuState cpu = processor.cpu;

        int channel = cpu.gpr[4];

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_KERNEL_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        cpu.gpr[2] = hleAudioGetChannelRestLen(pspPCMChannels[channel]);
    }

    @HLEFunction(nid = 0xCB2E439E, version = 150)
    public void sceAudioSetChannelDataLen(Processor processor) {
        CpuState cpu = processor.cpu;

        int channel = cpu.gpr[4];
        int samplecount = cpu.gpr[5];

        if (log.isDebugEnabled()) {
            log.debug(String.format("sceAudioSetChannelDataLen channel=%d, sampleCount=%d", channel, samplecount));
        }

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_KERNEL_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        pspPCMChannels[channel].setSampleLength(samplecount);
        cpu.gpr[2] = 0;
    }

    @HLEFunction(nid = 0x95FD0C2D, version = 150)
    public void sceAudioChangeChannelConfig(Processor processor) {
        CpuState cpu = processor.cpu;

        int channel = cpu.gpr[4];
        int format = cpu.gpr[5];

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_KERNEL_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        pspPCMChannels[channel].setFormat(format);
        cpu.gpr[2] = 0;
    }

    @HLEFunction(nid = 0xB7E1D8E7, version = 150)
    public void sceAudioChangeChannelVolume(Processor processor) {
        CpuState cpu = processor.cpu;

        int channel = cpu.gpr[4];
        int leftvol = cpu.gpr[5];
        int rightvol = cpu.gpr[6];

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_KERNEL_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        cpu.gpr[2] = changeChannelVolume(pspPCMChannels[channel], leftvol, rightvol);
    }

    @HLEFunction(nid = 0x01562BA3, version = 150)
    public void sceAudioOutput2Reserve(Processor processor) {
        CpuState cpu = processor.cpu;

        int samplecount = cpu.gpr[4];

        if (log.isDebugEnabled()) {
    		log.debug(String.format("sceAudioOutput2Reserve sampleCount=%d", samplecount));
        }

        hleAudioSRCChReserve(processor, samplecount, 44100, SoundChannel.FORMAT_STEREO);
    }

    @HLEFunction(nid = 0x43196845, version = 150)
    public void sceAudioOutput2Release(Processor processor) {
        sceAudioSRCChRelease(processor);
    }

    @HLEFunction(nid = 0x2D53F36E, version = 150)
    public void sceAudioOutput2OutputBlocking(Processor processor) {
        sceAudioSRCOutputBlocking(processor);
    }

    @HLEFunction(nid = 0x647CEF33, version = 150)
    public void sceAudioOutput2GetRestSample(Processor processor) {
        CpuState cpu = processor.cpu;

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_KERNEL_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        cpu.gpr[2] = hleAudioGetChannelRestLen(pspSRCChannel);
    }

    @HLEFunction(nid = 0x63F2889C, version = 150)
    public void sceAudioOutput2ChangeLength(Processor processor) {
        CpuState cpu = processor.cpu;

        int samplecount = cpu.gpr[4];

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_KERNEL_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        if (pspSRCChannel.isReserved()) {
            pspSRCChannel.setSampleLength(samplecount);
            cpu.gpr[2] = 0;
        } else {
            cpu.gpr[2] = -1;
        }
    }

    @HLEFunction(nid = 0x38553111, version = 150)
    public void sceAudioSRCChReserve(Processor processor) {
        CpuState cpu = processor.cpu;

        int samplecount = cpu.gpr[4];
        int freq = cpu.gpr[5];
        int format = cpu.gpr[6];

        if (log.isDebugEnabled()) {
    		log.debug(String.format("sceAudioSRCChReserve sampleCount=%d, freq=%d, format=%d", samplecount, freq, format));
        }

        hleAudioSRCChReserve(processor, samplecount, freq, format);
    }

    @HLEFunction(nid = 0x5C37C0AE, version = 150)
    public void sceAudioSRCChRelease(Processor processor) {
        CpuState cpu = processor.cpu;

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_KERNEL_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        if (pspSRCChannel.isReserved()) {
            pspSRCChannel.release();
            pspSRCChannel.setReserved(false);
        }

        cpu.gpr[2] = 0;
    }

    @HLEFunction(nid = 0xE0727056, version = 150)
    public void sceAudioSRCOutputBlocking(Processor processor) {
        CpuState cpu = processor.cpu;

        int vol = cpu.gpr[4];
        int buf = cpu.gpr[5];

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_KERNEL_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        if (buf == 0) {
            // Tested on PSP:
            // SRC audio also delays when buf == 0, in order to drain all
            // audio samples from the audio driver.
            if (!pspSRCChannel.isDrained()) {
                if (log.isDebugEnabled()) {
                    log.debug("sceAudioSRCOutputBlocking[pvoid_buf==0] blocking " + pspSRCChannel);
                }
                blockThreadOutput(pspSRCChannel, buf, vol, vol);
            } else {
                cpu.gpr[2] = 0;
            }
        } else if (!Memory.isAddressGood(buf)) {
            log.warn("sceAudioSRCOutputBlocking bad pointer " + String.format("0x%08X", buf));
            cpu.gpr[2] = SceKernelErrors.ERROR_AUDIO_PRIV_REQUIRED;
        } else {
            if (!pspSRCChannel.isOutputBlocking() || disableBlockingAudio) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("sceAudioSRCOutputBlocking[not blocking] 0x%08X to %s", buf, pspSRCChannel.toString()));
                }
                changeChannelVolume(pspSRCChannel, vol, vol);
                cpu.gpr[2] = doAudioOutput(pspSRCChannel, buf);
                Modules.ThreadManForUserModule.hleRescheduleCurrentThread();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("sceAudioSRCOutputBlocking[blocking] 0x%08X to %s", buf, pspSRCChannel.toString()));
                }
                blockThreadOutput(pspSRCChannel, buf, vol, vol);
            }
        }
    }

    @HLEFunction(nid = 0x086E5895, version = 150)
    public void sceAudioInputBlocking(Processor processor) {
        CpuState cpu = processor.cpu;

        System.out.println("Unimplemented NID function sceAudioInputBlocking [0x086E5895]");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    @HLEFunction(nid = 0x6D4BEC68, version = 150)
    public void sceAudioInput(Processor processor) {
        CpuState cpu = processor.cpu;

        System.out.println("Unimplemented NID function sceAudioInput [0x6D4BEC68]");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    @HLEFunction(nid = 0xA708C6A6, version = 150)
    public void sceAudioGetInputLength(Processor processor) {
        CpuState cpu = processor.cpu;

        System.out.println("Unimplemented NID function sceAudioGetInputLength [0xA708C6A6]");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    @HLEFunction(nid = 0x87B2E651, version = 150)
    public void sceAudioWaitInputEnd(Processor processor) {
        CpuState cpu = processor.cpu;

        System.out.println("Unimplemented NID function sceAudioWaitInputEnd [0x87B2E651]");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    @HLEFunction(nid = 0x7DE61688, version = 150)
    public void sceAudioInputInit(Processor processor) {
        CpuState cpu = processor.cpu;

        System.out.println("Unimplemented NID function sceAudioInputInit [0x7DE61688]");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    @HLEFunction(nid = 0xE926D3FB, version = 150)
    public void sceAudioInputInitEx(Processor processor) {
        CpuState cpu = processor.cpu;

        System.out.println("Unimplemented NID function sceAudioInputInitEx [0xE926D3FB]");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    @HLEFunction(nid = 0xA633048E, version = 150)
    public void sceAudioPollInputEnd(Processor processor) {
        CpuState cpu = processor.cpu;

        System.out.println("Unimplemented NID function sceAudioPollInputEnd [0xA633048E]");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    @HLEFunction(nid = 0xE9D97901, version = 150)
    public void sceAudioGetChannelRestLen(Processor processor) {
        CpuState cpu = processor.cpu;

        int channel = cpu.gpr[4];

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_KERNEL_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        cpu.gpr[2] = hleAudioGetChannelRestLen(pspPCMChannels[channel]);
    }

}