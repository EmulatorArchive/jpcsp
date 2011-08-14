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
import jpcsp.HLE.kernel.managers.IntrManager;
import jpcsp.HLE.kernel.types.SceKernelErrors;
import jpcsp.HLE.Modules;
import jpcsp.HLE.modules.HLEModule;
import jpcsp.HLE.modules.HLEModuleFunction;
import jpcsp.HLE.modules.HLEModuleManager;
import jpcsp.Memory;

import org.apache.log4j.Logger;

public class sceNpService implements HLEModule {

    protected static Logger log = Modules.getLogger("sceNpService");

    @Override
    public String getName() {
        return "sceNpService";
    }

    @Override
    public void installModule(HLEModuleManager mm, int version) { mm.installModuleWithAnnotations(this, version); }

    @Override
    public void uninstallModule(HLEModuleManager mm, int version) { mm.uninstallModuleWithAnnotations(this, version); }

    private int npManagerMemSize;            // Memory allocated by the NP Manager utility.
    private int npManagerMaxMemSize;  // Maximum memory used by the NP Manager utility.
    private int npManagerFreeMemSize;        // Free memory available to use by the NP Manager utility.

    public void sceNpService_0F8F5821(Processor processor) {
        CpuState cpu = processor.cpu;

        int poolSize = cpu.gpr[4];
        int stackSize = cpu.gpr[5];
        int threadPriority = cpu.gpr[6];

        log.warn("IGNORING: sceNpService_0F8F5821 (poolsize=0x" + Integer.toHexString(poolSize)
                + ", stackSize=0x" + Integer.toHexString(stackSize)
                + ", threadPriority=0x" + Integer.toHexString(threadPriority) + ")");

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_KERNEL_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        npManagerMemSize = poolSize;
        npManagerMaxMemSize = poolSize / 2;    // Dummy
        npManagerFreeMemSize = poolSize - 16;         // Dummy.
        cpu.gpr[2] = 0;
    }

    public void sceNpService_00ACFAC3(Processor processor) {
        CpuState cpu = processor.cpu;
        Memory mem = Memory.getInstance();

        int memStatAddr = cpu.gpr[4];

        log.warn("PARTIAL: sceNpService_00ACFAC3 (memStatAddr=0x" + Integer.toHexString(memStatAddr) + ")");

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_KERNEL_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        if (Memory.isAddressGood(memStatAddr)) {
            mem.write32(memStatAddr, npManagerMemSize);
            mem.write32(memStatAddr + 4, npManagerMaxMemSize);
            mem.write32(memStatAddr + 8, npManagerFreeMemSize);
        }
        cpu.gpr[2] = 0;
    }
    @HLEFunction(nid = 0x0F8F5821, version = 150) public HLEModuleFunction sceNpService_0F8F5821Function;

    @HLEFunction(nid = 0x00ACFAC3, version = 150) public HLEModuleFunction sceNpService_00ACFAC3Function;

}