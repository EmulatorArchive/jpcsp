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

public class Kernel_Library implements HLEModule {

    @Override
    public String getName() {
        return "Kernel_Library";
    }

    @Override
    public void installModule(HLEModuleManager mm, int version) {
        if (version >= 150) {

            mm.addFunction(sceKernelCpuSuspendIntrFunction, 0x092968F4);
            mm.addFunction(sceKernelCpuResumeIntrFunction, 0x5F10D406);
            mm.addFunction(sceKernelCpuResumeIntrWithSyncFunction, 0x3B84732D);
            mm.addFunction(sceKernelIsCpuIntrSuspendedFunction, 0x47A0B729);
            mm.addFunction(sceKernelIsCpuIntrEnableFunction, 0xB55249D2);

        }
    }

    @Override
    public void uninstallModule(HLEModuleManager mm, int version) {
        if (version >= 150) {

            mm.removeFunction(sceKernelCpuSuspendIntrFunction);
            mm.removeFunction(sceKernelCpuResumeIntrFunction);
            mm.removeFunction(sceKernelCpuResumeIntrWithSyncFunction);
            mm.removeFunction(sceKernelIsCpuIntrSuspendedFunction);
            mm.removeFunction(sceKernelIsCpuIntrEnableFunction);

        }
    }

    private int m_suspended = 0;
    
    public void sceKernelCpuSuspendIntr(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor

        cpu.gpr[2] = m_suspended;

        m_suspended++;
    }

    public void sceKernelCpuResumeIntr(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor

        --m_suspended;
    }

    public void sceKernelCpuResumeIntrWithSync(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor

        --m_suspended;
    }

    public void sceKernelIsCpuIntrSuspended(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor

        cpu.gpr[2] = cpu.gpr[4];
    }

    public void sceKernelIsCpuIntrEnable(Processor processor) {
        CpuState cpu = processor.cpu; // New-Style Processor
        
        cpu.gpr[2] = m_suspended;
    }
    public final HLEModuleFunction sceKernelCpuSuspendIntrFunction = new HLEModuleFunction("Kernel_Library", "sceKernelCpuSuspendIntr") {

        @Override
        public final void execute(Processor processor) {
            sceKernelCpuSuspendIntr(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.Kernel_LibraryModule.sceKernelCpuSuspendIntr(processor);";
        }
    };
    public final HLEModuleFunction sceKernelCpuResumeIntrFunction = new HLEModuleFunction("Kernel_Library", "sceKernelCpuResumeIntr") {

        @Override
        public final void execute(Processor processor) {
            sceKernelCpuResumeIntr(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.Kernel_LibraryModule.sceKernelCpuResumeIntr(processor);";
        }
    };
    public final HLEModuleFunction sceKernelCpuResumeIntrWithSyncFunction = new HLEModuleFunction("Kernel_Library", "sceKernelCpuResumeIntrWithSync") {

        @Override
        public final void execute(Processor processor) {
            sceKernelCpuResumeIntrWithSync(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.Kernel_LibraryModule.sceKernelCpuResumeIntrWithSync(processor);";
        }
    };
    public final HLEModuleFunction sceKernelIsCpuIntrSuspendedFunction = new HLEModuleFunction("Kernel_Library", "sceKernelIsCpuIntrSuspended") {

        @Override
        public final void execute(Processor processor) {
            sceKernelIsCpuIntrSuspended(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.Kernel_LibraryModule.sceKernelIsCpuIntrSuspended(processor);";
        }
    };
    public final HLEModuleFunction sceKernelIsCpuIntrEnableFunction = new HLEModuleFunction("Kernel_Library", "sceKernelIsCpuIntrEnable") {

        @Override
        public final void execute(Processor processor) {
            sceKernelIsCpuIntrEnable(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.Kernel_LibraryModule.sceKernelIsCpuIntrEnable(processor);";
        }
    };
};
