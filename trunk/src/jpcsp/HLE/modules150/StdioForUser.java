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

import jpcsp.Processor;
import jpcsp.Allegrex.CpuState;
import jpcsp.HLE.Modules;
import jpcsp.HLE.modules.HLEModule;
import jpcsp.HLE.modules.HLEModuleFunction;
import jpcsp.HLE.modules.HLEModuleManager;

import org.apache.log4j.Logger;

public class StdioForUser implements HLEModule {
    private static Logger log = Modules.getLogger("StdioForUser");

    @Override
    public String getName() {
        return "StdioForUser";
    }

    @Override
    public void installModule(HLEModuleManager mm, int version) {
        if (version >= 150) {

            mm.addFunction(0x3054D478, sceKernelStdioReadFunction);
            mm.addFunction(0x0CBB0571, sceKernelStdioLseekFunction);
            mm.addFunction(0xA46785C9, sceKernelStdioSendCharFunction);
            mm.addFunction(0xA3B931DB, sceKernelStdioWriteFunction);
            mm.addFunction(0x9D061C19, sceKernelStdioCloseFunction);
            mm.addFunction(0x924ABA61, sceKernelStdioOpenFunction);
            mm.addFunction(0x172D316E, sceKernelStdinFunction);
            mm.addFunction(0xA6BAB2E9, sceKernelStdoutFunction);
            mm.addFunction(0xF78BA90A, sceKernelStderrFunction);

        }
    }

    @Override
    public void uninstallModule(HLEModuleManager mm, int version) {
        if (version >= 150) {

            mm.removeFunction(sceKernelStdioReadFunction);
            mm.removeFunction(sceKernelStdioLseekFunction);
            mm.removeFunction(sceKernelStdioSendCharFunction);
            mm.removeFunction(sceKernelStdioWriteFunction);
            mm.removeFunction(sceKernelStdioCloseFunction);
            mm.removeFunction(sceKernelStdioOpenFunction);
            mm.removeFunction(sceKernelStdinFunction);
            mm.removeFunction(sceKernelStdoutFunction);
            mm.removeFunction(sceKernelStderrFunction);

        }
    }

    public void sceKernelStdioRead(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("Unimplemented NID function sceKernelStdioRead [0x3054D478]");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceKernelStdioLseek(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("Unimplemented NID function sceKernelStdioLseek [0x0CBB0571]");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceKernelStdioSendChar(Processor processor) {
        CpuState cpu = processor.cpu; 

        log.warn("Unimplemented NID function sceKernelStdioSendChar [0xA46785C9]");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceKernelStdioWrite(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("Unimplemented NID function sceKernelStdioWrite [0xA3B931DB]");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceKernelStdioClose(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("Unimplemented NID function sceKernelStdioClose [0x9D061C19]");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceKernelStdioOpen(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("Unimplemented NID function sceKernelStdioOpen [0x924ABA61]");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceKernelStdin(Processor processor) {
        CpuState cpu = processor.cpu;

        cpu.gpr[2] = 3;
    }

    public void sceKernelStdout(Processor processor) {
        CpuState cpu = processor.cpu;

        cpu.gpr[2] = 1;
    }

    public void sceKernelStderr(Processor processor) {
        CpuState cpu = processor.cpu;

        cpu.gpr[2] = 2;
    }
    public final HLEModuleFunction sceKernelStdioReadFunction = new HLEModuleFunction("StdioForUser", "sceKernelStdioRead") {

        @Override
        public final void execute(Processor processor) {
            sceKernelStdioRead(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.StdioForUserModule.sceKernelStdioRead(processor);";
        }
    };
    public final HLEModuleFunction sceKernelStdioLseekFunction = new HLEModuleFunction("StdioForUser", "sceKernelStdioLseek") {

        @Override
        public final void execute(Processor processor) {
            sceKernelStdioLseek(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.StdioForUserModule.sceKernelStdioLseek(processor);";
        }
    };
    public final HLEModuleFunction sceKernelStdioSendCharFunction = new HLEModuleFunction("StdioForUser", "sceKernelStdioSendChar") {

        @Override
        public final void execute(Processor processor) {
            sceKernelStdioSendChar(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.StdioForUserModule.sceKernelStdioSendChar(processor);";
        }
    };
    public final HLEModuleFunction sceKernelStdioWriteFunction = new HLEModuleFunction("StdioForUser", "sceKernelStdioWrite") {

        @Override
        public final void execute(Processor processor) {
            sceKernelStdioWrite(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.StdioForUserModule.sceKernelStdioWrite(processor);";
        }
    };
    public final HLEModuleFunction sceKernelStdioCloseFunction = new HLEModuleFunction("StdioForUser", "sceKernelStdioClose") {

        @Override
        public final void execute(Processor processor) {
            sceKernelStdioClose(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.StdioForUserModule.sceKernelStdioClose(processor);";
        }
    };
    public final HLEModuleFunction sceKernelStdioOpenFunction = new HLEModuleFunction("StdioForUser", "sceKernelStdioOpen") {

        @Override
        public final void execute(Processor processor) {
            sceKernelStdioOpen(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.StdioForUserModule.sceKernelStdioOpen(processor);";
        }
    };
    public final HLEModuleFunction sceKernelStdinFunction = new HLEModuleFunction("StdioForUser", "sceKernelStdin") {

        @Override
        public final void execute(Processor processor) {
            sceKernelStdin(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.StdioForUserModule.sceKernelStdin(processor);";
        }
    };
    public final HLEModuleFunction sceKernelStdoutFunction = new HLEModuleFunction("StdioForUser", "sceKernelStdout") {

        @Override
        public final void execute(Processor processor) {
            sceKernelStdout(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.StdioForUserModule.sceKernelStdout(processor);";
        }
    };
    public final HLEModuleFunction sceKernelStderrFunction = new HLEModuleFunction("StdioForUser", "sceKernelStderr") {

        @Override
        public final void execute(Processor processor) {
            sceKernelStderr(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.StdioForUserModule.sceKernelStderr(processor);";
        }
    };
}