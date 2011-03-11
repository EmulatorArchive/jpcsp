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

public class sceNetAdhocDiscover implements HLEModule {

    protected static Logger log = Modules.getLogger("sceNetAdhocDiscover");

    @Override
    public String getName() {
        return "sceNetAdhocDiscover";
    }

    @Override
    public void installModule(HLEModuleManager mm, int version) {
        if (version >= 150) {

            mm.addFunction(0x941B3877, sceNetAdhocDiscoverInitStartFunction);
            mm.addFunction(0x52DE1B97, sceNetAdhocDiscoverUpdateFunction);
            mm.addFunction(0x944DDBC6, sceNetAdhocDiscoverGetStatusFunction);
            mm.addFunction(0xA2246614, sceNetAdhocDiscoverTermFunction);
            mm.addFunction(0xF7D13214, sceNetAdhocDiscoverStopFunction);
            mm.addFunction(0xA423A21B, sceNetAdhocDiscoverRequestSuspendFunction);

        }
    }

    @Override
    public void uninstallModule(HLEModuleManager mm, int version) {
        if (version >= 150) {

            mm.removeFunction(sceNetAdhocDiscoverInitStartFunction);
            mm.removeFunction(sceNetAdhocDiscoverUpdateFunction);
            mm.removeFunction(sceNetAdhocDiscoverGetStatusFunction);
            mm.removeFunction(sceNetAdhocDiscoverTermFunction);
            mm.removeFunction(sceNetAdhocDiscoverStopFunction);
            mm.removeFunction(sceNetAdhocDiscoverRequestSuspendFunction);

        }
    }

    public void sceNetAdhocDiscoverInitStart(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceNetAdhocDiscoverInitStart");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceNetAdhocDiscoverUpdate(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceNetAdhocDiscoverUpdate");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceNetAdhocDiscoverGetStatus(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceNetAdhocDiscoverGetStatus");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceNetAdhocDiscoverTerm(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceNetAdhocDiscoverTerm");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceNetAdhocDiscoverStop(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceNetAdhocDiscoverStop");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceNetAdhocDiscoverRequestSuspend(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceNetAdhocDiscoverRequestSuspend");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public final HLEModuleFunction sceNetAdhocDiscoverInitStartFunction = new HLEModuleFunction("sceNetAdhocDiscover", "sceNetAdhocDiscoverInitStart") {

        @Override
        public final void execute(Processor processor) {
            sceNetAdhocDiscoverInitStart(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetAdhocDiscoverModule.sceNetAdhocDiscoverInitStart(processor);";
        }
    };

    public final HLEModuleFunction sceNetAdhocDiscoverUpdateFunction = new HLEModuleFunction("sceNetAdhocDiscover", "sceNetAdhocDiscoverUpdate") {

        @Override
        public final void execute(Processor processor) {
            sceNetAdhocDiscoverUpdate(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetAdhocDiscoverModule.sceNetAdhocDiscoverUpdate(processor);";
        }
    };

    public final HLEModuleFunction sceNetAdhocDiscoverGetStatusFunction = new HLEModuleFunction("sceNetAdhocDiscover", "sceNetAdhocDiscoverGetStatus") {

        @Override
        public final void execute(Processor processor) {
            sceNetAdhocDiscoverGetStatus(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetAdhocDiscoverModule.sceNetAdhocDiscoverGetStatus(processor);";
        }
    };

    public final HLEModuleFunction sceNetAdhocDiscoverTermFunction = new HLEModuleFunction("sceNetAdhocDiscover", "sceNetAdhocDiscoverTerm") {

        @Override
        public final void execute(Processor processor) {
            sceNetAdhocDiscoverTerm(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetAdhocDiscoverModule.sceNetAdhocDiscoverTerm(processor);";
        }
    };

    public final HLEModuleFunction sceNetAdhocDiscoverStopFunction = new HLEModuleFunction("sceNetAdhocDiscover", "sceNetAdhocDiscoverStop") {

        @Override
        public final void execute(Processor processor) {
            sceNetAdhocDiscoverStop(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetAdhocDiscoverModule.sceNetAdhocDiscoverStop(processor);";
        }
    };

    public final HLEModuleFunction sceNetAdhocDiscoverRequestSuspendFunction = new HLEModuleFunction("sceNetAdhocDiscover", "sceNetAdhocDiscoverRequestSuspend") {

        @Override
        public final void execute(Processor processor) {
            sceNetAdhocDiscoverRequestSuspend(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetAdhocDiscoverModule.sceNetAdhocDiscoverRequestSuspend(processor);";
        }
    };
}