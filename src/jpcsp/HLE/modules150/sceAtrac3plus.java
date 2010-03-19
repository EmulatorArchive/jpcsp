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

import jpcsp.HLE.modules.HLEModule;
import jpcsp.HLE.modules.HLEModuleFunction;
import jpcsp.HLE.modules.HLEModuleManager;

import jpcsp.Memory;
import jpcsp.Processor;

import jpcsp.Allegrex.CpuState; // New-Style Processor
import jpcsp.HLE.Modules;

public class sceAtrac3plus implements HLEModule {
    @Override
    public String getName() { return "sceAtrac3plus"; }

    @Override
    public void installModule(HLEModuleManager mm, int version) {
        if (version >= 150) {

            mm.addFunction(sceAtracStartEntryFunction, 0xD1F59FDB);
            mm.addFunction(sceAtracEndEntryFunction, 0xD5C28CC0);
            mm.addFunction(sceAtracGetAtracIDFunction, 0x780F88D1);
            mm.addFunction(sceAtracReleaseAtracIDFunction, 0x61EB33F5);
            mm.addFunction(sceAtracSetDataFunction, 0x0E2A73AB);
            mm.addFunction(sceAtracSetHalfwayBufferFunction, 0x3F6E26B5);
            mm.addFunction(sceAtracSetDataAndGetIDFunction, 0x7A20E7AF);
            mm.addFunction(sceAtracSetHalfwayBufferAndGetIDFunction, 0x0FAE370E);
            mm.addFunction(sceAtracDecodeDataFunction, 0x6A8C3CD5);
            mm.addFunction(sceAtracGetRemainFrameFunction, 0x9AE849A7);
            mm.addFunction(sceAtracGetStreamDataInfoFunction, 0x5D268707);
            mm.addFunction(sceAtracAddStreamDataFunction, 0x7DB31251);
            mm.addFunction(sceAtracGetSecondBufferInfoFunction, 0x83E85EA0);
            mm.addFunction(sceAtracSetSecondBufferFunction, 0x83BF7AFD);
            mm.addFunction(sceAtracIsSecondBufferNeededFunction, 0xECA32A99); // 2.5
            mm.addFunction(sceAtracGetNextDecodePositionFunction, 0xE23E3A35);
            mm.addFunction(sceAtracGetSoundSampleFunction, 0xA2BBA8BE);
            mm.addFunction(sceAtracGetChannelFunction, 0x31668BAA);
            mm.addFunction(sceAtracGetMaxSampleFunction, 0xD6A5F2F7);
            mm.addFunction(sceAtracGetNextSampleFunction, 0x36FAABFB);
            mm.addFunction(sceAtracGetBitrateFunction, 0xA554A158);
            mm.addFunction(sceAtracGetLoopStatusFunction, 0xFAA4F89B);
            mm.addFunction(sceAtracSetLoopNumFunction, 0x868120B5);
            mm.addFunction(sceAtracGetBufferInfoForResetingFunction, 0xCA3CA3D2);
            mm.addFunction(sceAtracResetPlayPositionFunction, 0x644E5607);
            mm.addFunction(sceAtracGetInternalErrorInfoFunction, 0xE88F759B);
            mm.addFunction(sceAtracReinitFunction, 0x132F1ECA); // 2.5

            atracID = 0;
        }
    }

    @Override
    public void uninstallModule(HLEModuleManager mm, int version) {
        if (version >= 150) {

            mm.removeFunction(sceAtracStartEntryFunction);
            mm.removeFunction(sceAtracEndEntryFunction);
            mm.removeFunction(sceAtracGetAtracIDFunction);
            mm.removeFunction(sceAtracReleaseAtracIDFunction);
            mm.removeFunction(sceAtracSetDataFunction);
            mm.removeFunction(sceAtracSetHalfwayBufferFunction);
            mm.removeFunction(sceAtracSetDataAndGetIDFunction);
            mm.removeFunction(sceAtracSetHalfwayBufferAndGetIDFunction);
            mm.removeFunction(sceAtracDecodeDataFunction);
            mm.removeFunction(sceAtracGetRemainFrameFunction);
            mm.removeFunction(sceAtracGetStreamDataInfoFunction);
            mm.removeFunction(sceAtracAddStreamDataFunction);
            mm.removeFunction(sceAtracGetSecondBufferInfoFunction);
            mm.removeFunction(sceAtracSetSecondBufferFunction);
            mm.removeFunction(sceAtracIsSecondBufferNeededFunction);
            mm.removeFunction(sceAtracGetNextDecodePositionFunction);
            mm.removeFunction(sceAtracGetSoundSampleFunction);
            mm.removeFunction(sceAtracGetChannelFunction);
            mm.removeFunction(sceAtracGetMaxSampleFunction);
            mm.removeFunction(sceAtracGetNextSampleFunction);
            mm.removeFunction(sceAtracGetBitrateFunction);
            mm.removeFunction(sceAtracGetLoopStatusFunction);
            mm.removeFunction(sceAtracSetLoopNumFunction);
            mm.removeFunction(sceAtracGetBufferInfoForResetingFunction);
            mm.removeFunction(sceAtracResetPlayPositionFunction);
            mm.removeFunction(sceAtracGetInternalErrorInfoFunction);
            mm.removeFunction(sceAtracReinitFunction);
            
        }
    }

    protected static final int PSP_MODE_AT_3_Plus = 0x00001000;
    protected static final int PSP_MODE_AT_3      = 0x00001001;

    private static int atracID;


    public void sceAtracStartEntry(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        Memory mem = Processor.memory;

        Modules.log.warn("Unimplemented NID function sceAtracStartEntry [0xD1F59FDB]");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceAtracEndEntry(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        Memory mem = Processor.memory;

        Modules.log.warn("Unimplemented NID function sceAtracEndEntry [0xD5C28CC0]");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceAtracGetAtracID(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        Memory mem = Processor.memory;

        int codecType = cpu.gpr[4];//noxa:codecType, //0x1000(AT3PLUS), 0x1001(AT3).

        Modules.log.warn("PARTIAL:sceAtracGetAtracID: atracID = 0x" + Integer.toHexString(codecType));

        if(codecType == 0x1000)
            atracID = 0;
        else if (codecType == 0x1001)
            atracID = 1;

        cpu.gpr[2] = atracID; // return the attracID or 0?
    }
    
    public void sceAtracReleaseAtracID(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        Memory mem = Processor.memory;

        int atID = cpu.gpr[4];
        atracID = atID;

        Modules.log.warn("Skipping:sceAtracReleaseAtracID: atracID = " + atID);
        
        cpu.gpr[2] = 0;
    }

    public void sceAtracSetData(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        Memory mem = Processor.memory;

        int atID = cpu.gpr[4];
        int bufAddr = cpu.gpr[5];
        int bufSize = cpu.gpr[6];//16384

        Modules.log.warn("Unimplemented sceAtracSetData: atID = " + atID
                + ", bufAddr = 0x" + Integer.toHexString(bufAddr)
                + ", bufSize = 0x" + Integer.toHexString(bufSize));

        mem.write32(bufAddr, 0x0000);
        mem.write32(bufSize, 0x4000);

        cpu.gpr[2] = 0;
    }
    
    public void sceAtracSetHalfwayBuffer(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        Memory mem = Processor.memory;

        Modules.log.warn("Unimplemented NID function sceAtracSetHalfwayBuffer [0x3F6E26B5]");
        
        cpu.gpr[2] = 0;
    }

    public void sceAtracSetDataAndGetID(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        Memory mem = Processor.memory;

        int buffer = cpu.gpr[4];
        int bufferSize = cpu.gpr[5];

        Modules.log.warn("Skipping:sceAtracSetDataAndGetID buffer = 0x" + Integer.toHexString(buffer)
                + " bufferSize = 0x" + Integer.toHexString(bufferSize));

        mem.write32(buffer, 0x0000);
        mem.write32(bufferSize, 0x4000);

        cpu.gpr[2] = 0;
    }

    public void sceAtracSetHalfwayBufferAndGetID(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        Memory mem = Processor.memory;

        Modules.log.warn("Unimplemented NID function sceAtracSetHalfwayBufferAndGetID [0x0FAE370E]");
        
        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceAtracDecodeData(Processor processor) {
    	CpuState cpu = processor.cpu;
        Memory mem = Processor.memory;

        int atID = cpu.gpr[4];
        int SamplesAdrr = cpu.gpr[5];
        int SamplesNbr = cpu.gpr[6];
        int outEnd = cpu.gpr[7];
        int RemainFrames = cpu.gpr[8];

        // Do something to decode here!
        mem.write32(SamplesNbr  , 0x4000);//mem.write32(SamplesNbr  , "decodedSamples");
        mem.write32(SamplesNbr  , 0x0010);//16?
        mem.write32(outEnd      , 0x0000);
        mem.write32(RemainFrames, -1);

        Modules.log.warn("Unimplemented sceAtracDecodeData: "
                + String.format("atracID=0x%08x, SamplesAdrr=%d, SamplesNbr=0x%08x, outEnd=0x%08x, RemainFrames=%d",
                atID, SamplesAdrr, SamplesNbr, outEnd, RemainFrames));

        
        cpu.gpr[2] = 0;
    }

    public void sceAtracGetRemainFrame(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        Memory mem = Processor.memory;

        int atID = cpu.gpr[4];
        int RemainFrames = cpu.gpr[5];
        
        mem.write32(RemainFrames, 0);
        
        Modules.log.warn("Unimplemented sceAtracGetRemainFrame: atracID = " + atID
                + "RemainFrames = 0x" + Integer.toHexString(RemainFrames));

        cpu.gpr[2] = 0;
    }
    
    public void sceAtracGetStreamDataInfo(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        Memory mem = Processor.memory;

        int atID = cpu.gpr[4];
        int writePointer = cpu.gpr[5];
        int availableBytes = cpu.gpr[6];
        int readOffset = cpu.gpr[7];

        // Do something HERE!!
        // seek, seek, seek, seek,...., seek.

        Modules.log.warn("Unimplemented sceAtracGetStreamDataInfo: "
                + String.format("atID=0x%08x, writePointer=0x%08x, availableBytes=0x%08x, readOffset=0x%08x",
                atID, writePointer, availableBytes, readOffset));

        cpu.gpr[2] = 0xDEADC0DE;// must be 0!!
    }

    public void sceAtracAddStreamData(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        Memory mem = Processor.memory;

        int atID = cpu.gpr[4];
        int bytesToAdd = cpu.gpr[5];

        Modules.log.warn("Unimplemented sceAtracAddStreamData: atracID = " + atID
                + ", bytesToAdd = 0x" + Integer.toHexString(bytesToAdd));
        
        cpu.gpr[2] = 0;
    }

    public void sceAtracGetSecondBufferInfo(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        Memory mem = Processor.memory;
        
        int unknown = cpu.gpr[4]; // atracID?
        int outPosition = cpu.gpr[5];
        int outBytes = cpu.gpr[6];
        
        Modules.log.warn("Skipping:sceAtracGetSecondBufferInfo outPos=0x" + Integer.toHexString(outPosition)
                + " outBytes=0x" + Integer.toHexString(outBytes));

        mem.write32(outPosition, 0);
        mem.write32(outBytes, 0x4000);

        cpu.gpr[2] = 0;
    }

    public void sceAtracIsSecondBufferNeeded(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        Memory mem = Processor.memory;

        //like sceAtracSetData ?
        Modules.log.warn("Unimplemented NID function sceAtracIsSecondBufferNeeded [0xECA32A99] "
            + String.format("%08x %08x %08x %08x %08x %08x",
            cpu.gpr[4], cpu.gpr[5], cpu.gpr[6], cpu.gpr[7], cpu.gpr[8], cpu.gpr[9]));

        cpu.gpr[2] = 0;
    }
    
    public void sceAtracSetSecondBuffer(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        Memory mem = Processor.memory;
        
        Modules.log.warn("Skipping:sceAtracSetSecondBuffer");

        cpu.gpr[2] = 0;
    }
    
    public void sceAtracGetNextDecodePosition(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        Memory mem = Processor.memory;

        Modules.log.warn("Unimplemented NID function sceAtracGetNextDecodePosition [0xE23E3A35]");

        cpu.gpr[2] = 0xDEADC0DE;
    }
    
    public void sceAtracGetSoundSample(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        Memory mem = Processor.memory;

        //int atId = cpu.gpr[4]; // ??
        int EndSample = cpu.gpr[5];
        int LoopStartSample = cpu.gpr[6];
        int LoopEndSample = cpu.gpr[7];

        Modules.log.warn("Skipping:sceAtracGetSoundSample EndSample= " + EndSample 
                + ", LoopStartSample = 0x" + Integer.toHexString(LoopStartSample)
                + ", LoopEndSample = 0x" + Integer.toHexString(LoopEndSample));
                
        mem.write32(EndSample, 0x10000);
        mem.write32(LoopStartSample, -1);
        mem.write32(LoopEndSample, -1);
		
        cpu.gpr[2] = 0;
    }

    public void sceAtracGetChannel(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        Memory mem = Processor.memory;
        
        // int atID = cpu.gpr[4]; // ??
        // int channel = cpu.gpr[5]; // ??
        Modules.log.warn("Unimplemented NID function sceAtracGetChannel [0x31668BAA]");

        cpu.gpr[2] = 0;
    }

    public void sceAtracGetMaxSample(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        Memory mem = Processor.memory;
        
        int atID = cpu.gpr[4];
        int maxSamples = cpu.gpr[5];

        Modules.log.warn("Unimplemented sceAtracGetMaxSample: atracID = " + atID + ", maxSamples = " + maxSamples);
        
        cpu.gpr[2] = 0;
    }

    public void sceAtracGetNextSample(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        Memory mem = Processor.memory;

        int atID = cpu.gpr[4];
        int nbrSamples = cpu.gpr[5];
        
        Modules.log.warn("Unimplemented sceAtracGetNextSample: atracID = " + atID + ", nbrSamples = " + nbrSamples);
        
        cpu.gpr[2] = 0;
    }

    public void sceAtracGetBitrate(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        Memory mem = Processor.memory;

        int atID = cpu.gpr[4];
        int bitrate = cpu.gpr[5];

        Modules.log.warn("Unimplemented sceAtracGetBitrate: atracID = " + atID + ", bitrate = " + bitrate);

        cpu.gpr[2] = 0;
    }
    public void sceAtracGetLoopStatus(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        Memory mem = Processor.memory;

        Modules.log.warn("Unimplemented NID function sceAtracGetLoopStatus [0xFAA4F89B]");
        
        cpu.gpr[2] = 0;
    }

    public void sceAtracSetLoopNum(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        Memory mem = Processor.memory;

        int atID = cpu.gpr[4];
        int loopNbr = cpu.gpr[5];

        Modules.log.warn("Unimplemented sceAtracSetLoopNum: atracID = " + atID + ", loopNbr= " + loopNbr);

        cpu.gpr[2] = 0;
    }

    public void sceAtracGetBufferInfoForReseting(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        Memory mem = Processor.memory;

        Modules.log.warn("Unimplemented NID function sceAtracGetBufferInfoForReseting [0xCA3CA3D2]");

        cpu.gpr[2] = 0xDEADC0DE;
    }
    
    public void sceAtracResetPlayPosition(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        Memory mem = Processor.memory;

        Modules.log.warn("Unimplemented NID function sceAtracResetPlayPosition [0x644E5607]");

        cpu.gpr[2] = 0;
    }
    
    public void sceAtracGetInternalErrorInfo(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        Memory mem = Processor.memory;

        Modules.log.warn("Unimplemented NID function sceAtracGetInternalErrorInfo [0xE88F759B]");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceAtracReinit(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        Memory mem = Processor.memory;

        Modules.log.warn("Unimplemented NID function sceAtracReinit [0x132F1ECA] "
            + String.format("%08x %08x %08x %08x %08x %08x",
            cpu.gpr[4], cpu.gpr[5], cpu.gpr[6], cpu.gpr[7], cpu.gpr[8], cpu.gpr[9]));

        cpu.gpr[2] = atracID;
    }

    public final HLEModuleFunction sceAtracStartEntryFunction = new HLEModuleFunction("sceAtrac3plus", "sceAtracStartEntry") {
        @Override
        public final void execute(Processor processor) {
            sceAtracStartEntry(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAtrac3plusModule.sceAtracStartEntry(processor);";
        }
    };

    public final HLEModuleFunction sceAtracEndEntryFunction = new HLEModuleFunction("sceAtrac3plus", "sceAtracEndEntry") {
        @Override
        public final void execute(Processor processor) {
            sceAtracEndEntry(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAtrac3plusModule.sceAtracEndEntry(processor);";
        }
    };

    public final HLEModuleFunction sceAtracGetAtracIDFunction = new HLEModuleFunction("sceAtrac3plus", "sceAtracGetAtracID") {
        @Override
        public final void execute(Processor processor) {
            sceAtracGetAtracID(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAtrac3plusModule.sceAtracGetAtracID(processor);";
        }
    };
    
    public final HLEModuleFunction sceAtracReleaseAtracIDFunction = new HLEModuleFunction("sceAtrac3plus", "sceAtracReleaseAtracID") {
        @Override
        public final void execute(Processor processor) {
            sceAtracReleaseAtracID(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAtrac3plusModule.sceAtracReleaseAtracID(processor);";
        }
    };

    public final HLEModuleFunction sceAtracSetDataFunction = new HLEModuleFunction("sceAtrac3plus", "sceAtracSetData") {
        @Override
        public final void execute(Processor processor) {
            sceAtracSetData(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAtrac3plusModule.sceAtracSetData(processor);";
        }
    };

    public final HLEModuleFunction sceAtracSetHalfwayBufferFunction = new HLEModuleFunction("sceAtrac3plus", "sceAtracSetHalfwayBuffer") {
        @Override
        public final void execute(Processor processor) {
            sceAtracSetHalfwayBuffer(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAtrac3plusModule.sceAtracSetHalfwayBuffer(processor);";
        }
    };

    public final HLEModuleFunction sceAtracSetDataAndGetIDFunction = new HLEModuleFunction("sceAtrac3plus", "sceAtracSetDataAndGetID") {
        @Override
        public final void execute(Processor processor) {
            sceAtracSetDataAndGetID(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAtrac3plusModule.sceAtracSetDataAndGetID(processor);";
        }
    };
    
    public final HLEModuleFunction sceAtracSetHalfwayBufferAndGetIDFunction = new HLEModuleFunction("sceAtrac3plus", "sceAtracSetHalfwayBufferAndGetID") {
        @Override
        public final void execute(Processor processor) {
            sceAtracSetHalfwayBufferAndGetID(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAtrac3plusModule.sceAtracSetHalfwayBufferAndGetID(processor);";
        }
    };

    public final HLEModuleFunction sceAtracDecodeDataFunction = new HLEModuleFunction("sceAtrac3plus", "sceAtracDecodeData") {
        @Override
        public final void execute(Processor processor) {
            sceAtracDecodeData(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAtrac3plusModule.sceAtracDecodeData(processor);";
        }
    };

    public final HLEModuleFunction sceAtracGetRemainFrameFunction = new HLEModuleFunction("sceAtrac3plus", "sceAtracGetRemainFrame") {
        @Override
        public final void execute(Processor processor) {
            sceAtracGetRemainFrame(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAtrac3plusModule.sceAtracGetRemainFrame(processor);";
        }
    };

    public final HLEModuleFunction sceAtracGetStreamDataInfoFunction = new HLEModuleFunction("sceAtrac3plus", "sceAtracGetStreamDataInfo") {
        @Override
        public final void execute(Processor processor) {
            sceAtracGetStreamDataInfo(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAtrac3plusModule.sceAtracGetStreamDataInfo(processor);";
        }
    };
    
    public final HLEModuleFunction sceAtracAddStreamDataFunction = new HLEModuleFunction("sceAtrac3plus", "sceAtracAddStreamData") {
        @Override
        public final void execute(Processor processor) {
            sceAtracAddStreamData(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAtrac3plusModule.sceAtracAddStreamData(processor);";
        }
    };
    
    public final HLEModuleFunction sceAtracGetSecondBufferInfoFunction = new HLEModuleFunction("sceAtrac3plus", "sceAtracGetSecondBufferInfo") {
        @Override
        public final void execute(Processor processor) {
            sceAtracGetSecondBufferInfo(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAtrac3plusModule.sceAtracGetSecondBufferInfo(processor);";
        }
    };
    
    public final HLEModuleFunction sceAtracSetSecondBufferFunction = new HLEModuleFunction("sceAtrac3plus", "sceAtracSetSecondBuffer") {
        @Override
        public final void execute(Processor processor) {
            sceAtracSetSecondBuffer(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAtrac3plusModule.sceAtracSetSecondBuffer(processor);";
        }
    };

    public final HLEModuleFunction sceAtracIsSecondBufferNeededFunction = new HLEModuleFunction("sceAtrac3plus", "sceAtracIsSecondBufferNeeded") {
        @Override
        public final void execute(Processor processor) {
            sceAtracIsSecondBufferNeeded(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAtrac3plusModule.sceAtracIsSecondBufferNeeded(processor);";
        }
    };
    
    public final HLEModuleFunction sceAtracGetNextDecodePositionFunction = new HLEModuleFunction("sceAtrac3plus", "sceAtracGetNextDecodePosition") {
        @Override
        public final void execute(Processor processor) {
            sceAtracGetNextDecodePosition(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAtrac3plusModule.sceAtracGetNextDecodePosition(processor);";
        }
    };
    
    public final HLEModuleFunction sceAtracGetSoundSampleFunction = new HLEModuleFunction("sceAtrac3plus", "sceAtracGetSoundSample") {
        @Override
        public final void execute(Processor processor) {
            sceAtracGetSoundSample(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAtrac3plusModule.sceAtracGetSoundSample(processor);";
        }
    };

    public final HLEModuleFunction sceAtracGetChannelFunction = new HLEModuleFunction("sceAtrac3plus", "sceAtracGetChannel") {
        @Override
        public final void execute(Processor processor) {
            sceAtracGetChannel(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAtrac3plusModule.sceAtracGetChannel(processor);";
        }
    };

    public final HLEModuleFunction sceAtracGetMaxSampleFunction = new HLEModuleFunction("sceAtrac3plus", "sceAtracGetMaxSample") {
        @Override
        public final void execute(Processor processor) {
            sceAtracGetMaxSample(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAtrac3plusModule.sceAtracGetMaxSample(processor);";
        }
    };
    
    public final HLEModuleFunction sceAtracGetNextSampleFunction = new HLEModuleFunction("sceAtrac3plus", "sceAtracGetNextSample") {
        @Override
        public final void execute(Processor processor) {
            sceAtracGetNextSample(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAtrac3plusModule.sceAtracGetNextSample(processor);";
        }
    };
    
    public final HLEModuleFunction sceAtracGetBitrateFunction = new HLEModuleFunction("sceAtrac3plus", "sceAtracGetBitrate") {
        @Override
        public final void execute(Processor processor) {
            sceAtracGetBitrate(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAtrac3plusModule.sceAtracGetBitrate(processor);";
        }
    };

    public final HLEModuleFunction sceAtracGetLoopStatusFunction = new HLEModuleFunction("sceAtrac3plus", "sceAtracGetLoopStatus") {
        @Override
        public final void execute(Processor processor) {
            sceAtracGetLoopStatus(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAtrac3plusModule.sceAtracGetLoopStatus(processor);";
        }
    };

    public final HLEModuleFunction sceAtracSetLoopNumFunction = new HLEModuleFunction("sceAtrac3plus", "sceAtracSetLoopNum") {
        @Override
        public final void execute(Processor processor) {
            sceAtracSetLoopNum(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAtrac3plusModule.sceAtracSetLoopNum(processor);";
        }
    };

    public final HLEModuleFunction sceAtracGetBufferInfoForResetingFunction = new HLEModuleFunction("sceAtrac3plus", "sceAtracGetBufferInfoForReseting") {
        @Override
        public final void execute(Processor processor) {
            sceAtracGetBufferInfoForReseting(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAtrac3plusModule.sceAtracGetBufferInfoForReseting(processor);";
        }
    };

    public final HLEModuleFunction sceAtracResetPlayPositionFunction = new HLEModuleFunction("sceAtrac3plus", "sceAtracResetPlayPosition") {
        @Override
        public final void execute(Processor processor) {
            sceAtracResetPlayPosition(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAtrac3plusModule.sceAtracResetPlayPosition(processor);";
        }
    };
    
    public final HLEModuleFunction sceAtracGetInternalErrorInfoFunction = new HLEModuleFunction("sceAtrac3plus", "sceAtracGetInternalErrorInfo") {
        @Override
        public final void execute(Processor processor) {
            sceAtracGetInternalErrorInfo(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAtrac3plusModule.sceAtracGetInternalErrorInfo(processor);";
        }
    };

    public final HLEModuleFunction sceAtracReinitFunction = new HLEModuleFunction("sceAtrac3plus", "sceAtracReinit") {
        @Override
        public final void execute(Processor processor) {
            sceAtracReinit(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceAtrac3plusModule.sceAtracReinit(processor);";
        }
    };
};
