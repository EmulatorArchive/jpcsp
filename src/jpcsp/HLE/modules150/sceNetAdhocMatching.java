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
import jpcsp.Processor;
import jpcsp.Allegrex.CpuState;
import jpcsp.HLE.Modules;
import jpcsp.HLE.modules.HLEModule;
import jpcsp.HLE.modules.HLEModuleFunction;
import jpcsp.HLE.modules.HLEModuleManager;

import org.apache.log4j.Logger;

public class sceNetAdhocMatching implements HLEModule {

    protected static Logger log = Modules.getLogger("sceNetAdhocMatching");

    @Override
    public String getName() {
        return "sceNetAdhocMatching";
    }

    @Override
    public void installModule(HLEModuleManager mm, int version) { mm.installModuleWithAnnotations(this, version); }

    @Override
    public void uninstallModule(HLEModuleManager mm, int version) { mm.uninstallModuleWithAnnotations(this, version); }

    public void sceNetAdhocMatchingInit(Processor processor) {
        CpuState cpu = processor.cpu;

        int poolSize = cpu.gpr[4];

        log.warn("IGNORING: sceNetAdhocMatchingInit: poolSize=0x" + Integer.toHexString(poolSize));

        cpu.gpr[2] = 0;
    }

    public void sceNetAdhocMatchingTerm(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("IGNORING: sceNetAdhocMatchingTerm");

        cpu.gpr[2] = 0;
    }

    public void sceNetAdhocMatchingCreate(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceNetAdhocMatchingCreate");

        cpu.gpr[2] = 1;
    }

    public void sceNetAdhocMatchingStart(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceNetAdhocMatchingStart");

        cpu.gpr[2] = 0;
    }

    public void sceNetAdhocMatchingStop(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceNetAdhocMatchingStop");

        cpu.gpr[2] = 0;
    }

    public void sceNetAdhocMatchingDelete(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceNetAdhocMatchingDelete");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceNetAdhocMatchingSendData(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceNetAdhocMatchingSendData");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceNetAdhocMatchingAbortSendData(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceNetAdhocMatchingAbortSendData");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceNetAdhocMatchingSelectTarget(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceNetAdhocMatchingSelectTarget");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceNetAdhocMatchingCancelTarget(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceNetAdhocMatchingCancelTarget");

        cpu.gpr[2] = 0xDEADC0DE;
    }


    public void sceNetAdhocMatchingCancelTargetWithOpt(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceNetAdhocMatchingCancelTargetWithOpt");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceNetAdhocMatchingGetHelloOpt(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceNetAdhocMatchingGetHelloOpt");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceNetAdhocMatchingSetHelloOpt(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceNetAdhocMatchingSetHelloOpt");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceNetAdhocMatchingGetMembers(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceNetAdhocMatchingGetMembers");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceNetAdhocMatchingGetPoolStat(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceNetAdhocMatchingGetPoolStat");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceNetAdhocMatchingGetPoolMaxAlloc(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceNetAdhocMatchingGetPoolMaxAlloc");

        cpu.gpr[2] = 0xDEADC0DE;
    }
    @HLEFunction(nid = 0x2A2A1E07, version = 150)
    public final HLEModuleFunction sceNetAdhocMatchingInitFunction = new HLEModuleFunction("sceNetAdhocMatching", "sceNetAdhocMatchingInit") {

        @Override
        public final void execute(Processor processor) {
            sceNetAdhocMatchingInit(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetAdhocMatchingModule.sceNetAdhocMatchingInit(processor);";
        }
    };
    @HLEFunction(nid = 0x7945ECDA, version = 150)
    public final HLEModuleFunction sceNetAdhocMatchingTermFunction = new HLEModuleFunction("sceNetAdhocMatching", "sceNetAdhocMatchingTerm") {

        @Override
        public final void execute(Processor processor) {
            sceNetAdhocMatchingTerm(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetAdhocMatchingModule.sceNetAdhocMatchingTerm(processor);";
        }
    };
    @HLEFunction(nid = 0xCA5EDA6F, version = 150)
    public final HLEModuleFunction sceNetAdhocMatchingCreateFunction = new HLEModuleFunction("sceNetAdhocMatching", "sceNetAdhocMatchingCreate") {

        @Override
        public final void execute(Processor processor) {
            sceNetAdhocMatchingCreate(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetAdhocMatchingModule.sceNetAdhocMatchingCreate(processor);";
        }
    };
    @HLEFunction(nid = 0x93EF3843, version = 150)
    public final HLEModuleFunction sceNetAdhocMatchingStartFunction = new HLEModuleFunction("sceNetAdhocMatching", "sceNetAdhocMatchingStart") {

        @Override
        public final void execute(Processor processor) {
            sceNetAdhocMatchingStart(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetAdhocMatchingModule.sceNetAdhocMatchingStart(processor);";
        }
    };
    @HLEFunction(nid = 0x32B156B3, version = 150)
    public final HLEModuleFunction sceNetAdhocMatchingStopFunction = new HLEModuleFunction("sceNetAdhocMatching", "sceNetAdhocMatchingStop") {

        @Override
        public final void execute(Processor processor) {
            sceNetAdhocMatchingStop(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetAdhocMatchingModule.sceNetAdhocMatchingStop(processor);";
        }
    };
    @HLEFunction(nid = 0xF16EAF4F, version = 150)
    public final HLEModuleFunction sceNetAdhocMatchingDeleteFunction = new HLEModuleFunction("sceNetAdhocMatching", "sceNetAdhocMatchingDelete") {

        @Override
        public final void execute(Processor processor) {
            sceNetAdhocMatchingDelete(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetAdhocMatchingModule.sceNetAdhocMatchingDelete(processor);";
        }
    };
    @HLEFunction(nid = 0xF79472D7, version = 150)
    public final HLEModuleFunction sceNetAdhocMatchingSendDataFunction = new HLEModuleFunction("sceNetAdhocMatching", "sceNetAdhocMatchingSendData") {

        @Override
        public final void execute(Processor processor) {
            sceNetAdhocMatchingSendData(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetAdhocMatchingModule.sceNetAdhocMatchingSendData(processor);";
        }
    };
    @HLEFunction(nid = 0xEC19337D, version = 150)
    public final HLEModuleFunction sceNetAdhocMatchingAbortSendDataFunction = new HLEModuleFunction("sceNetAdhocMatching", "sceNetAdhocMatchingAbortSendData") {

        @Override
        public final void execute(Processor processor) {
            sceNetAdhocMatchingAbortSendData(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetAdhocMatchingModule.sceNetAdhocMatchingAbortSendData(processor);";
        }
    };
    @HLEFunction(nid = 0x5E3D4B79, version = 150)
    public final HLEModuleFunction sceNetAdhocMatchingSelectTargetFunction = new HLEModuleFunction("sceNetAdhocMatching", "sceNetAdhocMatchingSelectTarget") {

        @Override
        public final void execute(Processor processor) {
            sceNetAdhocMatchingSelectTarget(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetAdhocMatchingModule.sceNetAdhocMatchingSelectTarget(processor);";
        }
    };
    @HLEFunction(nid = 0xEA3C6108, version = 150)
    public final HLEModuleFunction sceNetAdhocMatchingCancelTargetFunction = new HLEModuleFunction("sceNetAdhocMatching", "sceNetAdhocMatchingCancelTarget") {

        @Override
        public final void execute(Processor processor) {
            sceNetAdhocMatchingCancelTarget(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetAdhocMatchingModule.sceNetAdhocMatchingCancelTarget(processor);";
        }
    };
    @HLEFunction(nid = 0x8F58BEDF, version = 150)
    public final HLEModuleFunction sceNetAdhocMatchingCancelTargetWithOptFunction = new HLEModuleFunction("sceNetAdhocMatching", "sceNetAdhocMatchingCancelTargetWithOpt") {

        @Override
        public final void execute(Processor processor) {
            sceNetAdhocMatchingCancelTargetWithOpt(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetAdhocMatchingModule.sceNetAdhocMatchingCancelTargetWithOpt(processor);";
        }
    };
    @HLEFunction(nid = 0xB5D96C2A, version = 150)
    public final HLEModuleFunction sceNetAdhocMatchingGetHelloOptFunction = new HLEModuleFunction("sceNetAdhocMatching", "sceNetAdhocMatchingGetHelloOpt") {

        @Override
        public final void execute(Processor processor) {
            sceNetAdhocMatchingGetHelloOpt(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetAdhocMatchingModule.sceNetAdhocMatchingGetHelloOpt(processor);";
        }
    };
    @HLEFunction(nid = 0xB58E61B7, version = 150)
    public final HLEModuleFunction sceNetAdhocMatchingSetHelloOptFunction = new HLEModuleFunction("sceNetAdhocMatching", "sceNetAdhocMatchingSetHelloOpt") {

        @Override
        public final void execute(Processor processor) {
            sceNetAdhocMatchingSetHelloOpt(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetAdhocMatchingModule.sceNetAdhocMatchingSetHelloOpt(processor);";
        }
    };
    @HLEFunction(nid = 0xC58BCD9E, version = 150)
    public final HLEModuleFunction sceNetAdhocMatchingGetMembersFunction = new HLEModuleFunction("sceNetAdhocMatching", "sceNetAdhocMatchingGetMembers") {

        @Override
        public final void execute(Processor processor) {
            sceNetAdhocMatchingGetMembers(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetAdhocMatchingModule.sceNetAdhocMatchingGetMembers(processor);";
        }
    };
    @HLEFunction(nid = 0x9C5CFB7D, version = 150)
    public final HLEModuleFunction sceNetAdhocMatchingGetPoolStatFunction = new HLEModuleFunction("sceNetAdhocMatching", "sceNetAdhocMatchingGetPoolStat") {

        @Override
        public final void execute(Processor processor) {
            sceNetAdhocMatchingGetPoolStat(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetAdhocMatchingModule.sceNetAdhocMatchingGetPoolStat(processor);";
        }
    };
    @HLEFunction(nid = 0x40F8F435, version = 150)
    public final HLEModuleFunction sceNetAdhocMatchingGetPoolMaxAllocFunction = new HLEModuleFunction("sceNetAdhocMatching", "sceNetAdhocMatchingGetPoolMaxAlloc") {

        @Override
        public final void execute(Processor processor) {
            sceNetAdhocMatchingGetPoolMaxAlloc(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetAdhocMatchingModule.sceNetAdhocMatchingGetPoolMaxAlloc(processor);";
        }
    };
}