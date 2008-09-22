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

import jpcsp.HLE.modules.HLEModule;
import jpcsp.HLE.modules.HLEModuleFunction;
import jpcsp.HLE.modules.HLEModuleManager;

import jpcsp.Memory;
import jpcsp.Processor;

import jpcsp.Allegrex.CpuState; // New-Style Processor

public class sceDisplay implements HLEModule {

    @Override
    public String getName() {
        return "sceDisplay";
    }

    @Override
    public void installModule(HLEModuleManager mm, int version) {
        if (version >= 150) {

            mm.addFunction(sceDisplaySetModeFunction, 0x0E20F177);
            mm.addFunction(sceDisplayGetModeFunction, 0xDEA197D4);
            mm.addFunction(sceDisplayGetFramePerSecFunction, 0xDBA6C4C4);
            mm.addFunction(sceDisplaySetHoldModeFunction, 0x7ED59BC4);
            mm.addFunction(sceDisplaySetResumeModeFunction, 0xA544C486);
            mm.addFunction(sceDisplaySetFrameBufFunction, 0x289D82FE);
            mm.addFunction(sceDisplayGetFrameBufFunction, 0xEEDA2E54);
            mm.addFunction(sceDisplayIsForegroundFunction, 0xB4F378FA);
            mm.addFunction(sceDisplayGetBrightnessFunction, 0x31C4BAA8);
            mm.addFunction(sceDisplayGetVcountFunction, 0x9C6EAAD7);
            mm.addFunction(sceDisplayIsVblankFunction, 0x4D4E10EC);
            mm.addFunction(sceDisplayWaitVblankFunction, 0x36CDFADE);
            mm.addFunction(sceDisplayWaitVblankCBFunction, 0x8EB9EC49);
            mm.addFunction(sceDisplayWaitVblankStartFunction, 0x984C27E7);
            mm.addFunction(sceDisplayWaitVblankStartCBFunction, 0x46F186C3);
            mm.addFunction(sceDisplayGetCurrentHcountFunction, 0x773DD3A3);
            mm.addFunction(sceDisplayGetAccumulatedHcountFunction, 0x210EAB3A);
            mm.addFunction(sceDisplayAdjustAccumulatedHcountFunction, 0xA83EF139);

        }
    }

    @Override
    public void uninstallModule(HLEModuleManager mm, int version) {
        if (version >= 150) {

            mm.removeFunction(sceDisplaySetModeFunction);
            mm.removeFunction(sceDisplayGetModeFunction);
            mm.removeFunction(sceDisplayGetFramePerSecFunction);
            mm.removeFunction(sceDisplaySetHoldModeFunction);
            mm.removeFunction(sceDisplaySetResumeModeFunction);
            mm.removeFunction(sceDisplaySetFrameBufFunction);
            mm.removeFunction(sceDisplayGetFrameBufFunction);
            mm.removeFunction(sceDisplayIsForegroundFunction);
            mm.removeFunction(sceDisplayGetBrightnessFunction);
            mm.removeFunction(sceDisplayGetVcountFunction);
            mm.removeFunction(sceDisplayIsVblankFunction);
            mm.removeFunction(sceDisplayWaitVblankFunction);
            mm.removeFunction(sceDisplayWaitVblankCBFunction);
            mm.removeFunction(sceDisplayWaitVblankStartFunction);
            mm.removeFunction(sceDisplayWaitVblankStartCBFunction);
            mm.removeFunction(sceDisplayGetCurrentHcountFunction);
            mm.removeFunction(sceDisplayGetAccumulatedHcountFunction);
            mm.removeFunction(sceDisplayAdjustAccumulatedHcountFunction);

        }
    }

    public void sceDisplaySetMode(Processor processor) {
        // CpuState cpu = processor.cpu; // New-Style Processor
        Processor cpu = processor; // Old-Style Processor
        Memory mem = Processor.memory;

        /* put your own code here instead */

        // int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
        // float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

        System.out.println("Unimplemented NID function sceDisplaySetMode [0x0E20F177]");

        cpu.gpr[2] = 0xDEADC0DE;

    // cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32); cpu.fpr[0] = result;
    }

    public void sceDisplayGetMode(Processor processor) {
        // CpuState cpu = processor.cpu; // New-Style Processor
        Processor cpu = processor; // Old-Style Processor
        Memory mem = Processor.memory;

        /* put your own code here instead */

        // int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
        // float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

        System.out.println("Unimplemented NID function sceDisplayGetMode [0xDEA197D4]");

        cpu.gpr[2] = 0xDEADC0DE;

    // cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32); cpu.fpr[0] = result;
    }

    public void sceDisplayGetFramePerSec(Processor processor) {
        // CpuState cpu = processor.cpu; // New-Style Processor
        Processor cpu = processor; // Old-Style Processor
        Memory mem = Processor.memory;

        /* put your own code here instead */

        // int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
        // float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

        System.out.println("Unimplemented NID function sceDisplayGetFramePerSec [0xDBA6C4C4]");

        cpu.gpr[2] = 0xDEADC0DE;

    // cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32); cpu.fpr[0] = result;
    }

    public void sceDisplaySetHoldMode(Processor processor) {
        // CpuState cpu = processor.cpu; // New-Style Processor
        Processor cpu = processor; // Old-Style Processor
        Memory mem = Processor.memory;

        /* put your own code here instead */

        // int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
        // float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

        System.out.println("Unimplemented NID function sceDisplaySetHoldMode [0x7ED59BC4]");

        cpu.gpr[2] = 0xDEADC0DE;

    // cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32); cpu.fpr[0] = result;
    }

    public void sceDisplaySetResumeMode(Processor processor) {
        // CpuState cpu = processor.cpu; // New-Style Processor
        Processor cpu = processor; // Old-Style Processor
        Memory mem = Processor.memory;

        /* put your own code here instead */

        // int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
        // float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

        System.out.println("Unimplemented NID function sceDisplaySetResumeMode [0xA544C486]");

        cpu.gpr[2] = 0xDEADC0DE;

    // cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32); cpu.fpr[0] = result;
    }

    public void sceDisplaySetFrameBuf(Processor processor) {
        // CpuState cpu = processor.cpu; // New-Style Processor
        Processor cpu = processor; // Old-Style Processor
        Memory mem = Processor.memory;

        /* put your own code here instead */

        // int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
        // float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

        System.out.println("Unimplemented NID function sceDisplaySetFrameBuf [0x289D82FE]");

        cpu.gpr[2] = 0xDEADC0DE;

    // cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32); cpu.fpr[0] = result;
    }

    public void sceDisplayGetFrameBuf(Processor processor) {
        // CpuState cpu = processor.cpu; // New-Style Processor
        Processor cpu = processor; // Old-Style Processor
        Memory mem = Processor.memory;

        /* put your own code here instead */

        // int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
        // float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

        System.out.println("Unimplemented NID function sceDisplayGetFrameBuf [0xEEDA2E54]");

        cpu.gpr[2] = 0xDEADC0DE;

    // cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32); cpu.fpr[0] = result;
    }

    public void sceDisplayIsForeground(Processor processor) {
        // CpuState cpu = processor.cpu; // New-Style Processor
        Processor cpu = processor; // Old-Style Processor
        Memory mem = Processor.memory;

        /* put your own code here instead */

        // int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
        // float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

        System.out.println("Unimplemented NID function sceDisplayIsForeground [0xB4F378FA]");

        cpu.gpr[2] = 0xDEADC0DE;

    // cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32); cpu.fpr[0] = result;
    }

    public void sceDisplayGetBrightness(Processor processor) {
        // CpuState cpu = processor.cpu; // New-Style Processor
        Processor cpu = processor; // Old-Style Processor
        Memory mem = Processor.memory;

        /* put your own code here instead */

        // int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
        // float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

        System.out.println("Unimplemented NID function sceDisplayGetBrightness [0x31C4BAA8]");

        cpu.gpr[2] = 0xDEADC0DE;

    // cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32); cpu.fpr[0] = result;
    }

    public void sceDisplayGetVcount(Processor processor) {
        // CpuState cpu = processor.cpu; // New-Style Processor
        Processor cpu = processor; // Old-Style Processor
        Memory mem = Processor.memory;

        /* put your own code here instead */

        // int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
        // float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

        System.out.println("Unimplemented NID function sceDisplayGetVcount [0x9C6EAAD7]");

        cpu.gpr[2] = 0xDEADC0DE;

    // cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32); cpu.fpr[0] = result;
    }

    public void sceDisplayIsVblank(Processor processor) {
        // CpuState cpu = processor.cpu; // New-Style Processor
        Processor cpu = processor; // Old-Style Processor
        Memory mem = Processor.memory;

        /* put your own code here instead */

        // int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
        // float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

        System.out.println("Unimplemented NID function sceDisplayIsVblank [0x4D4E10EC]");

        cpu.gpr[2] = 0xDEADC0DE;

    // cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32); cpu.fpr[0] = result;
    }

    public void sceDisplayWaitVblank(Processor processor) {
        // CpuState cpu = processor.cpu; // New-Style Processor
        Processor cpu = processor; // Old-Style Processor
        Memory mem = Processor.memory;

        /* put your own code here instead */

        // int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
        // float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

        System.out.println("Unimplemented NID function sceDisplayWaitVblank [0x36CDFADE]");

        cpu.gpr[2] = 0xDEADC0DE;

    // cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32); cpu.fpr[0] = result;
    }

    public void sceDisplayWaitVblankCB(Processor processor) {
        // CpuState cpu = processor.cpu; // New-Style Processor
        Processor cpu = processor; // Old-Style Processor
        Memory mem = Processor.memory;

        /* put your own code here instead */

        // int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
        // float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

        System.out.println("Unimplemented NID function sceDisplayWaitVblankCB [0x8EB9EC49]");

        cpu.gpr[2] = 0xDEADC0DE;

    // cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32); cpu.fpr[0] = result;
    }

    public void sceDisplayWaitVblankStart(Processor processor) {
        // CpuState cpu = processor.cpu; // New-Style Processor
        Processor cpu = processor; // Old-Style Processor
        Memory mem = Processor.memory;

        /* put your own code here instead */

        // int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
        // float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

        System.out.println("Unimplemented NID function sceDisplayWaitVblankStart [0x984C27E7]");

        cpu.gpr[2] = 0xDEADC0DE;

    // cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32); cpu.fpr[0] = result;
    }

    public void sceDisplayWaitVblankStartCB(Processor processor) {
        // CpuState cpu = processor.cpu; // New-Style Processor
        Processor cpu = processor; // Old-Style Processor
        Memory mem = Processor.memory;

        /* put your own code here instead */

        // int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
        // float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

        System.out.println("Unimplemented NID function sceDisplayWaitVblankStartCB [0x46F186C3]");

        cpu.gpr[2] = 0xDEADC0DE;

    // cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32); cpu.fpr[0] = result;
    }

    public void sceDisplayGetCurrentHcount(Processor processor) {
        // CpuState cpu = processor.cpu; // New-Style Processor
        Processor cpu = processor; // Old-Style Processor
        Memory mem = Processor.memory;

        /* put your own code here instead */

        // int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
        // float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

        System.out.println("Unimplemented NID function sceDisplayGetCurrentHcount [0x773DD3A3]");

        cpu.gpr[2] = 0xDEADC0DE;

    // cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32); cpu.fpr[0] = result;
    }

    public void sceDisplayGetAccumulatedHcount(Processor processor) {
        // CpuState cpu = processor.cpu; // New-Style Processor
        Processor cpu = processor; // Old-Style Processor
        Memory mem = Processor.memory;

        /* put your own code here instead */

        // int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
        // float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

        System.out.println("Unimplemented NID function sceDisplayGetAccumulatedHcount [0x210EAB3A]");

        cpu.gpr[2] = 0xDEADC0DE;

    // cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32); cpu.fpr[0] = result;
    }

    public void sceDisplayAdjustAccumulatedHcount(Processor processor) {
        // CpuState cpu = processor.cpu; // New-Style Processor
        Processor cpu = processor; // Old-Style Processor
        Memory mem = Processor.memory;

        /* put your own code here instead */

        // int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
        // float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

        System.out.println("Unimplemented NID function sceDisplayAdjustAccumulatedHcount [0xA83EF139]");

        cpu.gpr[2] = 0xDEADC0DE;

    // cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32); cpu.fpr[0] = result;
    }
    public final HLEModuleFunction sceDisplaySetModeFunction = new HLEModuleFunction("sceDisplay", "sceDisplaySetMode") {

        @Override
        public final void execute(Processor processor) {
            sceDisplaySetMode(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceDisplayModule.sceDisplaySetMode(processor);";
        }
    };
    public final HLEModuleFunction sceDisplayGetModeFunction = new HLEModuleFunction("sceDisplay", "sceDisplayGetMode") {

        @Override
        public final void execute(Processor processor) {
            sceDisplayGetMode(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceDisplayModule.sceDisplayGetMode(processor);";
        }
    };
    public final HLEModuleFunction sceDisplayGetFramePerSecFunction = new HLEModuleFunction("sceDisplay", "sceDisplayGetFramePerSec") {

        @Override
        public final void execute(Processor processor) {
            sceDisplayGetFramePerSec(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceDisplayModule.sceDisplayGetFramePerSec(processor);";
        }
    };
    public final HLEModuleFunction sceDisplaySetHoldModeFunction = new HLEModuleFunction("sceDisplay", "sceDisplaySetHoldMode") {

        @Override
        public final void execute(Processor processor) {
            sceDisplaySetHoldMode(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceDisplayModule.sceDisplaySetHoldMode(processor);";
        }
    };
    public final HLEModuleFunction sceDisplaySetResumeModeFunction = new HLEModuleFunction("sceDisplay", "sceDisplaySetResumeMode") {

        @Override
        public final void execute(Processor processor) {
            sceDisplaySetResumeMode(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceDisplayModule.sceDisplaySetResumeMode(processor);";
        }
    };
    public final HLEModuleFunction sceDisplaySetFrameBufFunction = new HLEModuleFunction("sceDisplay", "sceDisplaySetFrameBuf") {

        @Override
        public final void execute(Processor processor) {
            sceDisplaySetFrameBuf(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceDisplayModule.sceDisplaySetFrameBuf(processor);";
        }
    };
    public final HLEModuleFunction sceDisplayGetFrameBufFunction = new HLEModuleFunction("sceDisplay", "sceDisplayGetFrameBuf") {

        @Override
        public final void execute(Processor processor) {
            sceDisplayGetFrameBuf(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceDisplayModule.sceDisplayGetFrameBuf(processor);";
        }
    };
    public final HLEModuleFunction sceDisplayIsForegroundFunction = new HLEModuleFunction("sceDisplay", "sceDisplayIsForeground") {

        @Override
        public final void execute(Processor processor) {
            sceDisplayIsForeground(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceDisplayModule.sceDisplayIsForeground(processor);";
        }
    };
    public final HLEModuleFunction sceDisplayGetBrightnessFunction = new HLEModuleFunction("sceDisplay", "sceDisplayGetBrightness") {

        @Override
        public final void execute(Processor processor) {
            sceDisplayGetBrightness(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceDisplayModule.sceDisplayGetBrightness(processor);";
        }
    };
    public final HLEModuleFunction sceDisplayGetVcountFunction = new HLEModuleFunction("sceDisplay", "sceDisplayGetVcount") {

        @Override
        public final void execute(Processor processor) {
            sceDisplayGetVcount(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceDisplayModule.sceDisplayGetVcount(processor);";
        }
    };
    public final HLEModuleFunction sceDisplayIsVblankFunction = new HLEModuleFunction("sceDisplay", "sceDisplayIsVblank") {

        @Override
        public final void execute(Processor processor) {
            sceDisplayIsVblank(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceDisplayModule.sceDisplayIsVblank(processor);";
        }
    };
    public final HLEModuleFunction sceDisplayWaitVblankFunction = new HLEModuleFunction("sceDisplay", "sceDisplayWaitVblank") {

        @Override
        public final void execute(Processor processor) {
            sceDisplayWaitVblank(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceDisplayModule.sceDisplayWaitVblank(processor);";
        }
    };
    public final HLEModuleFunction sceDisplayWaitVblankCBFunction = new HLEModuleFunction("sceDisplay", "sceDisplayWaitVblankCB") {

        @Override
        public final void execute(Processor processor) {
            sceDisplayWaitVblankCB(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceDisplayModule.sceDisplayWaitVblankCB(processor);";
        }
    };
    public final HLEModuleFunction sceDisplayWaitVblankStartFunction = new HLEModuleFunction("sceDisplay", "sceDisplayWaitVblankStart") {

        @Override
        public final void execute(Processor processor) {
            sceDisplayWaitVblankStart(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceDisplayModule.sceDisplayWaitVblankStart(processor);";
        }
    };
    public final HLEModuleFunction sceDisplayWaitVblankStartCBFunction = new HLEModuleFunction("sceDisplay", "sceDisplayWaitVblankStartCB") {

        @Override
        public final void execute(Processor processor) {
            sceDisplayWaitVblankStartCB(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceDisplayModule.sceDisplayWaitVblankStartCB(processor);";
        }
    };
    public final HLEModuleFunction sceDisplayGetCurrentHcountFunction = new HLEModuleFunction("sceDisplay", "sceDisplayGetCurrentHcount") {

        @Override
        public final void execute(Processor processor) {
            sceDisplayGetCurrentHcount(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceDisplayModule.sceDisplayGetCurrentHcount(processor);";
        }
    };
    public final HLEModuleFunction sceDisplayGetAccumulatedHcountFunction = new HLEModuleFunction("sceDisplay", "sceDisplayGetAccumulatedHcount") {

        @Override
        public final void execute(Processor processor) {
            sceDisplayGetAccumulatedHcount(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceDisplayModule.sceDisplayGetAccumulatedHcount(processor);";
        }
    };
    public final HLEModuleFunction sceDisplayAdjustAccumulatedHcountFunction = new HLEModuleFunction("sceDisplay", "sceDisplayAdjustAccumulatedHcount") {

        @Override
        public final void execute(Processor processor) {
            sceDisplayAdjustAccumulatedHcount(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceDisplayModule.sceDisplayAdjustAccumulatedHcount(processor);";
        }
    };
};
