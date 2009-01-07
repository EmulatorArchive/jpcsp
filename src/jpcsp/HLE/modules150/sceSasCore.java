/* This autogenerated file is part of jpcsp. */
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

import jpcsp.HLE.Modules;
import jpcsp.HLE.ThreadMan;
import jpcsp.HLE.modules.HLEModule;
import jpcsp.HLE.modules.HLEModuleFunction;
import jpcsp.HLE.modules.HLEModuleManager;

import jpcsp.Memory;
import jpcsp.Processor;

import jpcsp.Allegrex.CpuState; // New-Style Processor

public class sceSasCore implements HLEModule {

    @Override
    public String getName() {
        return "sceSasCore";
    }

    @Override
    public void installModule(HLEModuleManager mm, int version) {
        if (version >= 150) {

            mm.addFunction(__sceSasSetADSRFunction, 0x019B25EB);
            mm.addFunction(__sceSasRevParamFunction, 0x267A6DD2);
            mm.addFunction(__sceSasGetPauseFlagFunction, 0x2C8E6AB3);
            mm.addFunction(__sceSasRevTypeFunction, 0x33D4AB37);
            mm.addFunction(__sceSasInitFunction, 0x42778A9F);
            mm.addFunction(__sceSasSetVolumeFunction, 0x440CA7D8);
            mm.addFunction(__sceSasCoreWithMixFunction, 0x50A14DFC);
            mm.addFunction(__sceSasSetSLFunction, 0x5F9529F6);
            mm.addFunction(__sceSasGetEndFlagFunction, 0x68A46B95);
            mm.addFunction(__sceSasGetEnvelopeHeightFunction, 0x74AE582A);
            mm.addFunction(__sceSasSetKeyOnFunction, 0x76F01ACA);
            mm.addFunction(__sceSasSetPauseFunction, 0x787D04D5);
            mm.addFunction(__sceSasSetVoiceFunction, 0x99944089);
            mm.addFunction(__sceSasSetADSRmodeFunction, 0x9EC3676A);
            mm.addFunction(__sceSasSetKeyOffFunction, 0xA0CF2FA4);
            mm.addFunction(__sceSasSetTrianglarWaveFunction, 0xA232CBE6);
            mm.addFunction(__sceSasCoreFunction, 0xA3589D81);
            mm.addFunction(__sceSasSetPitchFunction, 0xAD84D37F);
            mm.addFunction(__sceSasSetNoiseFunction, 0xB7660A23);
            mm.addFunction(__sceSasGetGrainFunction, 0xBD11B7C2);
            mm.addFunction(__sceSasSetSimpleADSRFunction, 0xCBCD4F79);
            mm.addFunction(__sceSasSetGrainFunction, 0xD1E0A01E);
            mm.addFunction(__sceSasRevEVOLFunction, 0xD5A229C9);
            mm.addFunction(__sceSasSetSteepWaveFunction, 0xD5EBBBCD);
            mm.addFunction(__sceSasGetOutputmodeFunction, 0xE175EF66);
            mm.addFunction(__sceSasSetOutputmodeFunction, 0xE855BF76);
            mm.addFunction(__sceSasRevVONFunction, 0xF983B186);
        }

        sasCoreHandle = -1;
    }

    @Override
    public void uninstallModule(HLEModuleManager mm, int version) {
        if (version >= 150) {

            mm.removeFunction(__sceSasSetADSRFunction);
            mm.removeFunction(__sceSasRevParamFunction);
            mm.removeFunction(__sceSasGetPauseFlagFunction);
            mm.removeFunction(__sceSasRevTypeFunction);
            mm.removeFunction(__sceSasInitFunction);
            mm.removeFunction(__sceSasSetVolumeFunction);
            mm.removeFunction(__sceSasCoreWithMixFunction);
            mm.removeFunction(__sceSasSetSLFunction);
            mm.removeFunction(__sceSasGetEndFlagFunction);
            mm.removeFunction(__sceSasGetEnvelopeHeightFunction);
            mm.removeFunction(__sceSasSetKeyOnFunction);
            mm.removeFunction(__sceSasSetPauseFunction);
            mm.removeFunction(__sceSasSetVoiceFunction);
            mm.removeFunction(__sceSasSetADSRmodeFunction);
            mm.removeFunction(__sceSasSetKeyOffFunction);
            mm.removeFunction(__sceSasSetTrianglarWaveFunction);
            mm.removeFunction(__sceSasCoreFunction);
            mm.removeFunction(__sceSasSetPitchFunction);
            mm.removeFunction(__sceSasSetNoiseFunction);
            mm.removeFunction(__sceSasGetGrainFunction);
            mm.removeFunction(__sceSasSetSimpleADSRFunction);
            mm.removeFunction(__sceSasSetGrainFunction);
            mm.removeFunction(__sceSasRevEVOLFunction);
            mm.removeFunction(__sceSasSetSteepWaveFunction);
            mm.removeFunction(__sceSasGetOutputmodeFunction);
            mm.removeFunction(__sceSasSetOutputmodeFunction);
            mm.removeFunction(__sceSasRevVONFunction);

        }
    }

    protected int sasCoreHandle;

    protected String makeLogParams(CpuState cpu) {
        return String.format("%08x %08x %08x %08x",
            cpu.gpr[4], cpu.gpr[5], cpu.gpr[6], cpu.gpr[7]);
    }

    /** If sasCore isn't a valid handle this function will print a log message and set $v0 to -1.
     * @return true if sasCore is good. */
    protected boolean isSasHandleGood(int sasCore, String functionName, CpuState cpu) {
        if (sasCore == sasCoreHandle)
            return true;

        Modules.log.warn(functionName + " bad sasCore handle 0x" + Integer.toHexString(sasCore));
        cpu.gpr[2] = -1;
        return false;
    }

    public void __sceSasSetADSR(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        // Processor cpu = processor; // Old-Style Processor
        Memory mem = Processor.memory;

        /* put your own code here instead */

        int sasCore = cpu.gpr[4];
        //int unk1 = cpu.gpr[5]; // 0, 2
        //int unk2 = cpu.gpr[6]; // 8, f
        //int unk3 = cpu.gpr[7]; // 0, 0x40000000
        //int unk4 = cpu.gpr[8]; // 64
        //int unk5 = cpu.gpr[9]; // 64

        Modules.log.warn("Unimplemented NID function __sceSasSetADSR [0x019B25EB] " + makeLogParams(cpu));

        cpu.gpr[2] = 0xDEADC0DE;

    // cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result >>> 32); cpu.fpr[0] = result;
    }

    public void __sceSasRevParam(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        // Processor cpu = processor; // Old-Style Processor
        Memory mem = Processor.memory;

        /* put your own code here instead */

        int sasCore = cpu.gpr[4];
        //int unk1 = cpu.gpr[5]; // 0
        //int unk2 = cpu.gpr[6]; // 0

        Modules.log.warn("Unimplemented NID function __sceSasRevParam [0x267A6DD2] " + makeLogParams(cpu));

        cpu.gpr[2] = 0;
    }

    // we could do some trickery in here too
    // 2C8E6AB3
    public void __sceSasGetPauseFlag(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        // Processor cpu = processor; // Old-Style Processor
        Memory mem = Processor.memory;

        int sasCore = cpu.gpr[4];
        //int unk1 = cpu.gpr[5]; // set to 1
        //int unk2 = cpu.gpr[6]; // looks like a heap address, but so far 0x10000 aligned
        // 99% sure there are no more parameters
        // probably matches __sceSasGetEndFlag

        Modules.log.debug("IGNORING:__sceSasGetPauseFlag(sasCore=0x" + Integer.toHexString(sasCore) + ") " + makeLogParams(cpu));

        if (isSasHandleGood(sasCore, "__sceSasGetPauseFlag", cpu)) {
            // Fake all channels NOT paused
            cpu.gpr[2] = 0x0;
        }
    }

    public void __sceSasRevType(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        // Processor cpu = processor; // Old-Style Processor
        Memory mem = Processor.memory;

        /* put your own code here instead */

        int sasCore = cpu.gpr[4];

        // -1 = any?
        // 0 = ?
        // 1 = ?
        // 2 = ?
        // 3 = ?
        // 4 = ?
        int type = cpu.gpr[5];
        //int unk2 = cpu.gpr[6]; // unused or 1 or the return code from some other function (0xdeadc0de)
        //int unk3 = cpu.gpr[7]; // unused or 0, 1, 0x1000

        Modules.log.warn("IGNORING:__sceSasRevType(type=" + type + ") " + makeLogParams(cpu));

        cpu.gpr[2] = 0;
    }

    public void __sceSasInit(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        // Processor cpu = processor; // Old-Style Processor

        // TODO there may be more parameters
        // PARTIAL:__sceSasInit 08a8bd00 00000100 00000020
        int sasCore = cpu.gpr[4];

        //Modules.log.debug("PARTIAL:__sceSasInit() " + makeLogParams(cpu));
        Modules.log.debug("PARTIAL:__sceSasInit(sasCore=0x" + Integer.toHexString(sasCore) + ")");

        // we'll support only 1 sascore instance at a time, we can fix this later if needed
        if (sasCoreHandle != -1) {
            Modules.log.warn("UNIMPLEMENTED:__sceSasInit multiple instances not yet supported");
            cpu.gpr[2] = -1;
        } else {
            sasCoreHandle = sasCore;
            cpu.gpr[2] = 0;
        }
    }

    public void __sceSasSetVolume(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        // Processor cpu = processor; // Old-Style Processor
        Memory mem = Processor.memory;

        /* put your own code here instead */

        int sasCore = cpu.gpr[4];
        int voice = cpu.gpr[5];
        //int unk2 = cpu.gpr[6]; // left channel volume 0 - 0x1000
        //int unk3 = cpu.gpr[7]; // right channel volume 0 - 0x1000
        // may be more parameters

        Modules.log.warn("Unimplemented NID function __sceSasSetVolume [0x440CA7D8] " + makeLogParams(cpu));

        cpu.gpr[2] = 0xDEADC0DE;

    // cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result >>> 32); cpu.fpr[0] = result;
    }

    // 50A14DFC
    public void __sceSasCoreWithMix(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        // Processor cpu = processor; // Old-Style Processor
        Memory mem = Processor.memory;

        int sasCore = cpu.gpr[4];
        //int unk1 = cpu.gpr[5]; // looks like a heap address
        //int unk2 = cpu.gpr[6]; // looks like a bitfield

        Modules.log.debug("IGNORING:__sceSasCoreWithMix " + makeLogParams(cpu));

        if (isSasHandleGood(sasCore, "__sceSasCoreWithMix", cpu)) {
            // nothing to do ... ?
            cpu.gpr[2] = 0;
        }
    }

    public void __sceSasSetSL(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        // Processor cpu = processor; // Old-Style Processor
        Memory mem = Processor.memory;

        /* put your own code here instead */

        // int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
        // float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

        Modules.log.warn("Unimplemented NID function __sceSasSetSL [0x5F9529F6] " + makeLogParams(cpu));

        cpu.gpr[2] = 0xDEADC0DE;

    // cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result >>> 32); cpu.fpr[0] = result;
    }

    // 68A46B95
    public void __sceSasGetEndFlag(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        // Processor cpu = processor; // Old-Style Processor

        int sasCore = cpu.gpr[4];
        //int unk1 = cpu.gpr[5]; // set to 1
        //int unk2 = cpu.gpr[6]; // looks like a heap address, but so far 0x10000 aligned
        // 99% sure there are no more parameters

        Modules.log.debug("IGNORING:__sceSasGetEndFlag(sasCore=0x" + Integer.toHexString(sasCore) + ") " + makeLogParams(cpu));

        if (isSasHandleGood(sasCore, "__sceSasGetEndFlag", cpu)) {
            // Fake all channels finished
            cpu.gpr[2] = 0xFFFFFFFF;
        }
    }

    public void __sceSasGetEnvelopeHeight(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        // Processor cpu = processor; // Old-Style Processor
        Memory mem = Processor.memory;

        /* put your own code here instead */

        int sasCore = cpu.gpr[4];
        int channel = cpu.gpr[5];
        //int unk1 = cpu.gpr[6]; // set to 1
        // 99% sure there are no more parameters

        Modules.log.warn("Unimplemented NID function __sceSasGetEnvelopeHeight [0x74AE582A] " + makeLogParams(cpu));

        cpu.gpr[2] = 0xDEADC0DE;

    // cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result >>> 32); cpu.fpr[0] = result;
    }

    public void __sceSasSetKeyOn(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        // Processor cpu = processor; // Old-Style Processor
        Memory mem = Processor.memory;

        /* put your own code here instead */

        int sasCore = cpu.gpr[4];
        int voice = cpu.gpr[5];
        //int unk2 = cpu.gpr[6]; // 1
        //int unk3 = cpu.gpr[7]; // 0x6e4/0x1000
        // may be more parameters

        Modules.log.warn("Unimplemented NID function __sceSasSetKeyOn [0x76F01ACA] " + makeLogParams(cpu));

        cpu.gpr[2] = 0xDEADC0DE;

    // cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result >>> 32); cpu.fpr[0] = result;
    }

    public void __sceSasSetPause(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        // Processor cpu = processor; // Old-Style Processor
        Memory mem = Processor.memory;

        /* put your own code here instead */

        // int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
        // float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

        Modules.log.warn("Unimplemented NID function __sceSasSetPause [0x787D04D5] " + makeLogParams(cpu));

        cpu.gpr[2] = 0xDEADC0DE;

    // cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result >>> 32); cpu.fpr[0] = result;
    }

    public void __sceSasSetVoice(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        // Processor cpu = processor; // Old-Style Processor
        Memory mem = Processor.memory;

        /* put your own code here instead */

        int sasCore = cpu.gpr[4];
        int voice = cpu.gpr[5];
        //int unk2 = cpu.gpr[6]; // heap address (may be uncached)
        //int unk3 = cpu.gpr[7]; // some size 0x48d0/0x200/0x400 or unused

        Modules.log.warn("Unimplemented NID function __sceSasSetVoice [0x99944089] " + makeLogParams(cpu));

        cpu.gpr[2] = 0xDEADC0DE;

    // cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result >>> 32); cpu.fpr[0] = result;
    }

    public void __sceSasSetADSRmode(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        // Processor cpu = processor; // Old-Style Processor
        Memory mem = Processor.memory;

        /* put your own code here instead */

        int sasCore = cpu.gpr[4];
        //int unk1 = cpu.gpr[5]; // 0
        //int unk2 = cpu.gpr[6]; // 8
        //int unk3 = cpu.gpr[7]; // 0

        Modules.log.warn("Unimplemented NID function __sceSasSetADSRmode [0x9EC3676A] " + makeLogParams(cpu));

        cpu.gpr[2] = 0xDEADC0DE;

    // cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result >>> 32); cpu.fpr[0] = result;
    }

    public void __sceSasSetKeyOff(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        // Processor cpu = processor; // Old-Style Processor
        Memory mem = Processor.memory;

        /* put your own code here instead */

        // int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
        // float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

        Modules.log.warn("Unimplemented NID function __sceSasSetKeyOff [0xA0CF2FA4] " + makeLogParams(cpu));

        cpu.gpr[2] = 0xDEADC0DE;

    // cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result >>> 32); cpu.fpr[0] = result;
    }

    public void __sceSasSetTrianglarWave(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        // Processor cpu = processor; // Old-Style Processor
        Memory mem = Processor.memory;

        /* put your own code here instead */

        // int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
        // float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

        Modules.log.warn("Unimplemented NID function __sceSasSetTrianglarWave [0xA232CBE6] " + makeLogParams(cpu));

        cpu.gpr[2] = 0xDEADC0DE;

    // cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result >>> 32); cpu.fpr[0] = result;
    }

    // A3589D81
    public void __sceSasCore(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        // Processor cpu = processor; // Old-Style Processor

        int sasCore = cpu.gpr[4];
        //int unk1 = cpu.gpr[5]; // looks like a heap address, bss
        //int unk2 = cpu.gpr[6]; // looks like a heap address, dynamic
        //int unk3 = cpu.gpr[7]; // 80420000 internal error code

        Modules.log.debug("IGNORING:__sceSasCore " + makeLogParams(cpu));

        if (isSasHandleGood(sasCore, "__sceSasCore", cpu)) {
            // noxa/pspplayer blocks in __sceSasCore
            // some games protect __sceSasCore with locks, suggesting it may context switch
            ThreadMan.getInstance().yieldCurrentThread();
            cpu.gpr[2] = 0;
        }
    }

    public void __sceSasSetPitch(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        // Processor cpu = processor; // Old-Style Processor
        Memory mem = Processor.memory;

        /* put your own code here instead */

        int sasCore = cpu.gpr[4];
        int voice = cpu.gpr[5];
        //int unk2 = cpu.gpr[6]; // 0x6e4/0x800/0x1000
        //int unk3 = cpu.gpr[7]; // 0x6e4/0x800/0x1000
        // may be more parameters

        Modules.log.warn("Unimplemented NID function __sceSasSetPitch [0xAD84D37F] " + makeLogParams(cpu));

        cpu.gpr[2] = 0xDEADC0DE;

    // cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result >>> 32); cpu.fpr[0] = result;
    }

    public void __sceSasSetNoise(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        // Processor cpu = processor; // Old-Style Processor
        Memory mem = Processor.memory;

        /* put your own code here instead */

        // int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
        // float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

        Modules.log.warn("Unimplemented NID function __sceSasSetNoise [0xB7660A23] " + makeLogParams(cpu));

        cpu.gpr[2] = 0xDEADC0DE;

    // cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result >>> 32); cpu.fpr[0] = result;
    }

    public void __sceSasGetGrain(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        // Processor cpu = processor; // Old-Style Processor
        Memory mem = Processor.memory;

        /* put your own code here instead */

        // int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
        // float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

        Modules.log.warn("Unimplemented NID function __sceSasGetGrain [0xBD11B7C2] " + makeLogParams(cpu));

        cpu.gpr[2] = 0xDEADC0DE;

    // cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result >>> 32); cpu.fpr[0] = result;
    }

    public void __sceSasSetSimpleADSR(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        // Processor cpu = processor; // Old-Style Processor
        Memory mem = Processor.memory;

        /* put your own code here instead */

        int sasCore = cpu.gpr[4];
        int voice = cpu.gpr[5];
        //int unk1 = cpu.gpr[6]; // 0xff
        //int unk2 = cpu.gpr[7]; // 0x1fc6
        // may be more parameters

        Modules.log.warn("Unimplemented NID function __sceSasSetSimpleADSR [0xCBCD4F79] " + makeLogParams(cpu));

        cpu.gpr[2] = 0xDEADC0DE;

    // cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result >>> 32); cpu.fpr[0] = result;
    }

    public void __sceSasSetGrain(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        // Processor cpu = processor; // Old-Style Processor
        Memory mem = Processor.memory;

        /* put your own code here instead */

        // int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
        // float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

        Modules.log.warn("Unimplemented NID function __sceSasSetGrain [0xD1E0A01E] " + makeLogParams(cpu));

        cpu.gpr[2] = 0xDEADC0DE;

    // cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result >>> 32); cpu.fpr[0] = result;
    }

    public void __sceSasRevEVOL(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        // Processor cpu = processor; // Old-Style Processor
        Memory mem = Processor.memory;

        /* put your own code here instead */

        int sasCore = cpu.gpr[4];
        // left/right channel volume?
        //int unk1 = cpu.gpr[5]; // left channel volume 0 - 0x1000
        //int unk2 = cpu.gpr[6]; // right channel volume 0 - 0x1000
        // 99% sure there are no more parameters

        Modules.log.warn("Unimplemented NID function __sceSasRevEVOL [0xD5A229C9] " + makeLogParams(cpu));

        cpu.gpr[2] = 0;

    // cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result >>> 32); cpu.fpr[0] = result;
    }

    public void __sceSasSetSteepWave(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        // Processor cpu = processor; // Old-Style Processor
        Memory mem = Processor.memory;

        /* put your own code here instead */

        // int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
        // float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

        Modules.log.warn("Unimplemented NID function __sceSasSetSteepWave [0xD5EBBBCD] " + makeLogParams(cpu));

        cpu.gpr[2] = 0xDEADC0DE;

    // cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result >>> 32); cpu.fpr[0] = result;
    }

    public void __sceSasGetOutputmode(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        // Processor cpu = processor; // Old-Style Processor
        Memory mem = Processor.memory;

        int sasCore = cpu.gpr[4];
        //int unk1 = cpu.gpr[5]; // set to 1
        // 99% sure there are no more parameters

        Modules.log.warn("Unimplemented NID function __sceSasGetOutputmode [0xE175EF66] " + makeLogParams(cpu));

        cpu.gpr[2] = 0xDEADC0DE;

    // cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result >>> 32); cpu.fpr[0] = result;
    }

    public void __sceSasSetOutputmode(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        // Processor cpu = processor; // Old-Style Processor
        Memory mem = Processor.memory;

        /* put your own code here instead */

        // int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
        // float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

        Modules.log.warn("Unimplemented NID function __sceSasSetOutputmode [0xE855BF76] " + makeLogParams(cpu));

        cpu.gpr[2] = 0xDEADC0DE;

    // cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result >>> 32); cpu.fpr[0] = result;
    }

    public void __sceSasRevVON(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        // Processor cpu = processor; // Old-Style Processor
        Memory mem = Processor.memory;

        /* put your own code here instead */

        int sasCore = cpu.gpr[4];
        //int unk1 = cpu.gpr[5]; // set to 1
        //int unk2 = cpu.gpr[6]; // 0 or 1
        // 99% sure there are no more parameters

        Modules.log.warn("Unimplemented NID function __sceSasRevVON [0xF983B186] " + makeLogParams(cpu));

        cpu.gpr[2] = 0;

    // cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result >>> 32); cpu.fpr[0] = result;
    }
    public final HLEModuleFunction __sceSasSetADSRFunction = new HLEModuleFunction("sceSasCore", "__sceSasSetADSR") {

        @Override
        public final void execute(Processor processor) {
            __sceSasSetADSR(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceSasCoreModule.__sceSasSetADSR(processor);";
        }
    };
    public final HLEModuleFunction __sceSasRevParamFunction = new HLEModuleFunction("sceSasCore", "__sceSasRevParam") {

        @Override
        public final void execute(Processor processor) {
            __sceSasRevParam(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceSasCoreModule.__sceSasRevParam(processor);";
        }
    };
    public final HLEModuleFunction __sceSasGetPauseFlagFunction = new HLEModuleFunction("sceSasCore", "__sceSasGetPauseFlag") {

        @Override
        public final void execute(Processor processor) {
            __sceSasGetPauseFlag(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceSasCoreModule.__sceSasGetPauseFlag(processor);";
        }
    };
    public final HLEModuleFunction __sceSasRevTypeFunction = new HLEModuleFunction("sceSasCore", "__sceSasRevType") {

        @Override
        public final void execute(Processor processor) {
            __sceSasRevType(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceSasCoreModule.__sceSasRevType(processor);";
        }
    };
    public final HLEModuleFunction __sceSasInitFunction = new HLEModuleFunction("sceSasCore", "__sceSasInit") {

        @Override
        public final void execute(Processor processor) {
            __sceSasInit(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceSasCoreModule.__sceSasInit(processor);";
        }
    };
    public final HLEModuleFunction __sceSasSetVolumeFunction = new HLEModuleFunction("sceSasCore", "__sceSasSetVolume") {

        @Override
        public final void execute(Processor processor) {
            __sceSasSetVolume(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceSasCoreModule.__sceSasSetVolume(processor);";
        }
    };
    public final HLEModuleFunction __sceSasCoreWithMixFunction = new HLEModuleFunction("sceSasCore", "__sceSasCoreWithMix") {

        @Override
        public final void execute(Processor processor) {
            __sceSasCoreWithMix(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceSasCoreModule.__sceSasCoreWithMix(processor);";
        }
    };
    public final HLEModuleFunction __sceSasSetSLFunction = new HLEModuleFunction("sceSasCore", "__sceSasSetSL") {

        @Override
        public final void execute(Processor processor) {
            __sceSasSetSL(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceSasCoreModule.__sceSasSetSL(processor);";
        }
    };
    public final HLEModuleFunction __sceSasGetEndFlagFunction = new HLEModuleFunction("sceSasCore", "__sceSasGetEndFlag") {

        @Override
        public final void execute(Processor processor) {
            __sceSasGetEndFlag(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceSasCoreModule.__sceSasGetEndFlag(processor);";
        }
    };
    public final HLEModuleFunction __sceSasGetEnvelopeHeightFunction = new HLEModuleFunction("sceSasCore", "__sceSasGetEnvelopeHeight") {

        @Override
        public final void execute(Processor processor) {
            __sceSasGetEnvelopeHeight(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceSasCoreModule.__sceSasGetEnvelopeHeight(processor);";
        }
    };
    public final HLEModuleFunction __sceSasSetKeyOnFunction = new HLEModuleFunction("sceSasCore", "__sceSasSetKeyOn") {

        @Override
        public final void execute(Processor processor) {
            __sceSasSetKeyOn(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceSasCoreModule.__sceSasSetKeyOn(processor);";
        }
    };
    public final HLEModuleFunction __sceSasSetPauseFunction = new HLEModuleFunction("sceSasCore", "__sceSasSetPause") {

        @Override
        public final void execute(Processor processor) {
            __sceSasSetPause(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceSasCoreModule.__sceSasSetPause(processor);";
        }
    };
    public final HLEModuleFunction __sceSasSetVoiceFunction = new HLEModuleFunction("sceSasCore", "__sceSasSetVoice") {

        @Override
        public final void execute(Processor processor) {
            __sceSasSetVoice(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceSasCoreModule.__sceSasSetVoice(processor);";
        }
    };
    public final HLEModuleFunction __sceSasSetADSRmodeFunction = new HLEModuleFunction("sceSasCore", "__sceSasSetADSRmode") {

        @Override
        public final void execute(Processor processor) {
            __sceSasSetADSRmode(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceSasCoreModule.__sceSasSetADSRmode(processor);";
        }
    };
    public final HLEModuleFunction __sceSasSetKeyOffFunction = new HLEModuleFunction("sceSasCore", "__sceSasSetKeyOff") {

        @Override
        public final void execute(Processor processor) {
            __sceSasSetKeyOff(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceSasCoreModule.__sceSasSetKeyOff(processor);";
        }
    };
    public final HLEModuleFunction __sceSasSetTrianglarWaveFunction = new HLEModuleFunction("sceSasCore", "__sceSasSetTrianglarWave") {

        @Override
        public final void execute(Processor processor) {
            __sceSasSetTrianglarWave(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceSasCoreModule.__sceSasSetTrianglarWave(processor);";
        }
    };
    public final HLEModuleFunction __sceSasCoreFunction = new HLEModuleFunction("sceSasCore", "__sceSasCore") {

        @Override
        public final void execute(Processor processor) {
            __sceSasCore(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceSasCoreModule.__sceSasCore(processor);";
        }
    };
    public final HLEModuleFunction __sceSasSetPitchFunction = new HLEModuleFunction("sceSasCore", "__sceSasSetPitch") {

        @Override
        public final void execute(Processor processor) {
            __sceSasSetPitch(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceSasCoreModule.__sceSasSetPitch(processor);";
        }
    };
    public final HLEModuleFunction __sceSasSetNoiseFunction = new HLEModuleFunction("sceSasCore", "__sceSasSetNoise") {

        @Override
        public final void execute(Processor processor) {
            __sceSasSetNoise(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceSasCoreModule.__sceSasSetNoise(processor);";
        }
    };
    public final HLEModuleFunction __sceSasGetGrainFunction = new HLEModuleFunction("sceSasCore", "__sceSasGetGrain") {

        @Override
        public final void execute(Processor processor) {
            __sceSasGetGrain(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceSasCoreModule.__sceSasGetGrain(processor);";
        }
    };
    public final HLEModuleFunction __sceSasSetSimpleADSRFunction = new HLEModuleFunction("sceSasCore", "__sceSasSetSimpleADSR") {

        @Override
        public final void execute(Processor processor) {
            __sceSasSetSimpleADSR(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceSasCoreModule.__sceSasSetSimpleADSR(processor);";
        }
    };
    public final HLEModuleFunction __sceSasSetGrainFunction = new HLEModuleFunction("sceSasCore", "__sceSasSetGrain") {

        @Override
        public final void execute(Processor processor) {
            __sceSasSetGrain(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceSasCoreModule.__sceSasSetGrain(processor);";
        }
    };
    public final HLEModuleFunction __sceSasRevEVOLFunction = new HLEModuleFunction("sceSasCore", "__sceSasRevEVOL") {

        @Override
        public final void execute(Processor processor) {
            __sceSasRevEVOL(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceSasCoreModule.__sceSasRevEVOL(processor);";
        }
    };
    public final HLEModuleFunction __sceSasSetSteepWaveFunction = new HLEModuleFunction("sceSasCore", "__sceSasSetSteepWave") {

        @Override
        public final void execute(Processor processor) {
            __sceSasSetSteepWave(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceSasCoreModule.__sceSasSetSteepWave(processor);";
        }
    };
    public final HLEModuleFunction __sceSasGetOutputmodeFunction = new HLEModuleFunction("sceSasCore", "__sceSasGetOutputmode") {

        @Override
        public final void execute(Processor processor) {
            __sceSasGetOutputmode(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceSasCoreModule.__sceSasGetOutputmode(processor);";
        }
    };
    public final HLEModuleFunction __sceSasSetOutputmodeFunction = new HLEModuleFunction("sceSasCore", "__sceSasSetOutputmode") {

        @Override
        public final void execute(Processor processor) {
            __sceSasSetOutputmode(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceSasCoreModule.__sceSasSetOutputmode(processor);";
        }
    };
    public final HLEModuleFunction __sceSasRevVONFunction = new HLEModuleFunction("sceSasCore", "__sceSasRevVON") {

        @Override
        public final void execute(Processor processor) {
            __sceSasRevVON(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceSasCoreModule.__sceSasRevVON(processor);";
        }
    };
};
