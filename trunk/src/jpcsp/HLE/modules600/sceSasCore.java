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
package jpcsp.HLE.modules600;

import jpcsp.HLE.HLEFunction;
import jpcsp.Processor;
import jpcsp.Allegrex.CpuState;
import jpcsp.HLE.kernel.managers.IntrManager;
import jpcsp.HLE.kernel.types.SceKernelErrors;
import jpcsp.HLE.modules.HLEModuleFunction;
import jpcsp.HLE.modules.HLEModuleManager;

public class sceSasCore extends jpcsp.HLE.modules500.sceSasCore {

    @Override
    public void installModule(HLEModuleManager mm, int version) { mm.installModuleWithAnnotations(this, version); }

    @Override
    public void uninstallModule(HLEModuleManager mm, int version) { mm.uninstallModuleWithAnnotations(this, version); }

    public void __sceSasSetVoiceATRAC3(Processor processor) {
        CpuState cpu = processor.cpu;

        int sasCore = cpu.gpr[4];
        int voice = cpu.gpr[5];
        int atrac3Addr = cpu.gpr[6];

        log.warn("UNIMPLEMENTED: __sceSasSetVoiceATRAC3: sasCore=0x" + Integer.toHexString(sasCore)
                + ", voice=" + voice
                + ", atrac3Addr=0x" + Integer.toHexString(atrac3Addr));

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_KERNEL_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        cpu.gpr[2] = 0;
    }

    public void __sceSasConcatenateATRAC3(Processor processor) {
        CpuState cpu = processor.cpu;

        int sasCore = cpu.gpr[4];
        int voice = cpu.gpr[5];
        int atrac3Addr = cpu.gpr[6];
        int unk1 = cpu.gpr[7];

        log.warn("UNIMPLEMENTED: __sceSasConcatenateATRAC3: sasCore=0x" + Integer.toHexString(sasCore)
                + ", voice=" + voice
                + ", atrac3Addr=0x" + Integer.toHexString(atrac3Addr)
                + ", unk1=0x" + Integer.toHexString(unk1));

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_KERNEL_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        cpu.gpr[2] = 0;
    }

    public void __sceSasUnsetATRAC3(Processor processor) {
        CpuState cpu = processor.cpu;

        int sasCore = cpu.gpr[4];
        int voice = cpu.gpr[5];
        int atrac3Addr = cpu.gpr[6];

        log.warn("UNIMPLEMENTED: __sceSasUnsetATRAC3: sasCore=0x" + Integer.toHexString(sasCore)
                + ", voice=" + voice
                + ", atrac3Addr=0x" + Integer.toHexString(atrac3Addr));

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_KERNEL_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        cpu.gpr[2] = 0;
    }
    @HLEFunction(nid = 0x4AA9EAD6, version = 600)
    public final HLEModuleFunction __sceSasSetVoiceATRAC3Function = new HLEModuleFunction("sceSasCore", "__sceSasSetVoiceATRAC3") {

        @Override
        public final void execute(Processor processor) {
            __sceSasSetVoiceATRAC3(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceSasCoreModule.__sceSasSetVoiceATRAC3(processor);";
        }
    };
    @HLEFunction(nid = 0x7497EA85, version = 600)
    public final HLEModuleFunction __sceSasConcatenateATRAC3Function = new HLEModuleFunction("sceSasCore", "__sceSasConcatenateATRAC3") {

        @Override
        public final void execute(Processor processor) {
            __sceSasConcatenateATRAC3(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceSasCoreModule.__sceSasConcatenateATRAC3(processor);";
        }
    };
    @HLEFunction(nid = 0xF6107F00, version = 600)
    public final HLEModuleFunction __sceSasUnsetATRAC3Function = new HLEModuleFunction("sceSasCore", "__sceSasUnsetATRAC3") {

        @Override
        public final void execute(Processor processor) {
            __sceSasUnsetATRAC3(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceSasCoreModule.__sceSasUnsetATRAC3(processor);";
        }
    };
}