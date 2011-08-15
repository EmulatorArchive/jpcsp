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
package jpcsp.HLE.modules500;

import jpcsp.HLE.HLEFunction;
import java.util.HashMap;

import static jpcsp.HLE.modules150.SysMemUserForUser.PSP_SMEM_Low;
import static jpcsp.HLE.modules150.SysMemUserForUser.PSP_SMEM_High;
import jpcsp.Memory;
import jpcsp.Processor;
import jpcsp.Allegrex.CpuState;
import jpcsp.HLE.kernel.managers.IntrManager;
import jpcsp.HLE.kernel.types.SceKernelErrors;
import jpcsp.HLE.Modules;
import jpcsp.HLE.modules.HLEModule;
import jpcsp.HLE.modules.HLEModuleFunction;
import jpcsp.HLE.modules.HLEModuleManager;
import jpcsp.HLE.modules.SysMemUserForUser;
import jpcsp.HLE.modules150.SysMemUserForUser.SysMemInfo;
import jpcsp.util.Utilities;

import org.apache.log4j.Logger;

public class sceHeap implements HLEModule {

    protected static Logger log = Modules.getLogger("sceHeap");

    @Override
    public String getName() {
        return "sceHeap";
    }

    protected final static int PSP_HEAP_ATTR_ADDR_HIGH = 0x4000;       // Create the heap in high memory.
    protected final static int PSP_HEAP_ATTR_EXT =       0x8000;       // Automatically extend the heap's memory.
    private HashMap<Integer, SysMemInfo> heapMap = new HashMap<Integer, SysMemInfo>();
    private HashMap<Integer, SysMemInfo> heapMemMap = new HashMap<Integer, SysMemInfo>();

    @HLEFunction(nid = 0x0E875980, version = 500)
    public void sceHeapReallocHeapMemory(Processor processor) {
        CpuState cpu = processor.cpu;

        int heap_addr = cpu.gpr[4];
        int mem_addr = cpu.gpr[5];
        int memSize = cpu.gpr[6];

        log.warn("UNIMPLEMENTED: sceHeapReallocHeapMemory heap_addr=0x" + Integer.toHexString(heap_addr)
                + ", mem_addr=0x" + Integer.toHexString(mem_addr)
                + ", memSize=0x" + Integer.toHexString(memSize));

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_KERNEL_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }

        cpu.gpr[2] = 0xDEADC0DE;
    }

    @HLEFunction(nid = 0x1C84B58D, version = 500)
    public void sceHeapReallocHeapMemoryWithOption(Processor processor) {
        CpuState cpu = processor.cpu;

        int heap_addr = cpu.gpr[4];
        int mem_addr = cpu.gpr[5];
        int memSize = cpu.gpr[6];
        int param_addr = cpu.gpr[7];

        log.warn("UNIMPLEMENTED: sceHeapReallocHeapMemory heap_addr=0x" + Integer.toHexString(heap_addr)
                + ", mem_addr=0x" + Integer.toHexString(mem_addr)
                + ", memSize=0x" + Integer.toHexString(memSize)
                + ", param_addr=0x" + Integer.toHexString(param_addr));

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_KERNEL_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }

        cpu.gpr[2] = 0xDEADC0DE;
    }

    @HLEFunction(nid = 0x2ABADC63, version = 500)
    public void sceHeapFreeHeapMemory(Processor processor) {
        CpuState cpu = processor.cpu;

        int heap_addr = cpu.gpr[4];
        int mem_addr = cpu.gpr[5];

        if (log.isDebugEnabled()) {
            log.debug("sceHeapFreeHeapMemory heap_addr=0x" + Integer.toHexString(heap_addr)
                    + ", mem_addr=0x" + Integer.toHexString(mem_addr));
        }

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_KERNEL_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        // Try to free memory back to the heap.
        if (heapMemMap.containsKey(mem_addr)) {
            Modules.SysMemUserForUserModule.free(heapMemMap.get(mem_addr));
            cpu.gpr[2] = 0;
        } else if (heapMap.containsKey(heap_addr)){
            cpu.gpr[2] = SceKernelErrors.ERROR_INVALID_ID;
        } else {
            cpu.gpr[2] = SceKernelErrors.ERROR_INVALID_POINTER;
        }
    }

    @HLEFunction(nid = 0x2A0C2009, version = 500)
    public void sceHeapGetMallinfo(Processor processor) {
        CpuState cpu = processor.cpu;

        int heap_addr = cpu.gpr[4];
        int info_addr = cpu.gpr[5];

        log.warn("UNIMPLEMENTED: sceHeapGetMallinfo heap_addr=0x" + Integer.toHexString(heap_addr)
                + ", info_addr=0x" + Integer.toHexString(info_addr));

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_KERNEL_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }

        cpu.gpr[2] = 0xDEADC0DE;
    }

    @HLEFunction(nid = 0x2B7299D8, version = 500)
    public void sceHeapAllocHeapMemoryWithOption(Processor processor) {
        CpuState cpu = processor.cpu;
        Memory mem = Memory.getInstance();

        int heap_addr = cpu.gpr[4];
        int memSize = cpu.gpr[5];
        int param_addr = cpu.gpr[6];

        if (log.isDebugEnabled()) {
            log.debug("sceHeapAllocHeapMemoryWithOption heap_addr=0x" + Integer.toHexString(heap_addr)
                    + ", memSize=0x" + Integer.toHexString(memSize)
                    + ", param_addr=0x" + Integer.toHexString(param_addr));
        }

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_KERNEL_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        if (Memory.isAddressGood(param_addr)) {
            int paramSize = mem.read32(param_addr);
            if ((paramSize >= 4) && (paramSize <= 8)) {
                int memAlign = mem.read32(param_addr + 4);
                if (log.isDebugEnabled()) {
                	log.debug("sceHeapAllocHeapMemoryWithOption options: struct size=" + paramSize + ", alignment=0x" + Integer.toHexString(memAlign));
                }
            } else {
                log.warn("sceHeapAllocHeapMemoryWithOption option at 0x" + Integer.toHexString(param_addr) + " (size=" + paramSize + ")");
            }
        }
        // Try to allocate memory from the heap and return it's address.
        SysMemInfo heapInfo = null;
        SysMemInfo heapMemInfo = null;
        if (heapMap.containsKey(heap_addr)) {
            heapInfo = heapMap.get(heap_addr);
            heapMemInfo = Modules.SysMemUserForUserModule.malloc(heapInfo.partitionid, "ThreadMan-HeapMem", heapInfo.type, memSize, 0);
        }
        if (heapMemInfo != null) {
            heapMemMap.put(heapMemInfo.addr, heapMemInfo);
            cpu.gpr[2] = heapMemInfo.addr;
        } else {
            cpu.gpr[2] = 0;
        }
    }

    @HLEFunction(nid = 0x4929B40D, version = 500)
    public void sceHeapGetTotalFreeSize(Processor processor) {
        CpuState cpu = processor.cpu;

        int heap_addr = cpu.gpr[4];

        log.warn("UNIMPLEMENTED: sceHeapGetTotalFreeSize heap_addr=0x" + Integer.toHexString(heap_addr));

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_KERNEL_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }

        cpu.gpr[2] = 0xDEADC0DE;
    }

    @HLEFunction(nid = 0x7012BBDD, version = 500)
    public void sceHeapIsAllocatedHeapMemory(Processor processor) {
        CpuState cpu = processor.cpu;

        int heap_addr = cpu.gpr[4];
        int mem_addr = cpu.gpr[5];

        log.warn("UNIMPLEMENTED: sceHeapIsAllocatedHeapMemory heap_addr=0x" + Integer.toHexString(heap_addr)
                + ", mem_addr=0x" + Integer.toHexString(mem_addr));

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_KERNEL_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }

        cpu.gpr[2] = 0xDEADC0DE;
    }

    @HLEFunction(nid = 0x70210B73, version = 500)
    public void sceHeapDeleteHeap(Processor processor) {
        CpuState cpu = processor.cpu;

        int heap_addr = cpu.gpr[4];

        if (log.isDebugEnabled()) {
            log.debug("sceHeapDeleteHeap heap_addr=0x" + Integer.toHexString(heap_addr));
        }

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_KERNEL_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        if (heapMap.containsKey(heap_addr)) {
            Modules.SysMemUserForUserModule.free(heapMap.get(heap_addr));
            cpu.gpr[2] = 0;
        } else {
            cpu.gpr[2] = SceKernelErrors.ERROR_INVALID_ID;
        }
    }

    @HLEFunction(nid = 0x7DE281C2, version = 500)
    public void sceHeapCreateHeap(Processor processor) {
        CpuState cpu = processor.cpu;

        int name_addr = cpu.gpr[4];
        int heapSize = cpu.gpr[5];
        int attr = cpu.gpr[6];
        int param_addr = cpu.gpr[7];

        String name = Utilities.readStringZ(name_addr);
        if (log.isDebugEnabled()) {
            log.debug("sceHeapCreateHeap name='" + name
                    + "', heapSize=0x" + Integer.toHexString(heapSize)
                    + ", attr=0x" + Integer.toHexString(attr)
                    + ", param_addr=0x" + Integer.toHexString(param_addr));
        }

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_KERNEL_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        int memType = PSP_SMEM_Low;
        if ((attr & PSP_HEAP_ATTR_ADDR_HIGH) == PSP_HEAP_ATTR_ADDR_HIGH) {
            memType = PSP_SMEM_High;
        }
        if (param_addr != 0) {
            log.warn("sceHeapCreateHeap option at 0x" + Integer.toHexString(param_addr));
        }
        // Allocate a virtual heap memory space and return it's address.
        SysMemInfo info = null;
        int totalHeapSize = (heapSize + (4 - 1)) & (~(4 - 1));
        int maxFreeSize = Modules.SysMemUserForUserModule.maxFreeMemSize();
        if (totalHeapSize <= maxFreeSize) {
            info = Modules.SysMemUserForUserModule.malloc(SysMemUserForUser.USER_PARTITION_ID, "ThreadMan-Heap", memType, totalHeapSize, 0);
        } else {
            Modules.log.warn("sceHeapCreateHeap not enough free mem (want=" + totalHeapSize + ", free=" + maxFreeSize + ", diff=" + (totalHeapSize - maxFreeSize) + ")");
        }
        if (info != null) {
            heapMap.put(info.addr, info);
            cpu.gpr[2] = info.addr;
        } else {
            cpu.gpr[2] = 0; // Returns NULL on error.
        }
    }

    @HLEFunction(nid = 0xA8E102A0, version = 500)
    public void sceHeapAllocHeapMemory(Processor processor) {
        CpuState cpu = processor.cpu;

        int heap_addr = cpu.gpr[4];
        int memSize = cpu.gpr[5];

        if (log.isDebugEnabled()) {
            log.debug("sceHeapAllocHeapMemoryWithOption heap_addr=0x" + Integer.toHexString(heap_addr)
                    + ", memSize=0x" + Integer.toHexString(memSize));
        }

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_KERNEL_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        // Try to allocate memory from the heap and return it's address.
        SysMemInfo heapInfo = null;
        SysMemInfo heapMemInfo = null;
        if (heapMap.containsKey(heap_addr)) {
            heapInfo = heapMap.get(heap_addr);
            heapMemInfo = Modules.SysMemUserForUserModule.malloc(heapInfo.partitionid, "ThreadMan-HeapMem", heapInfo.type, memSize, 0);
        }
        if (heapMemInfo != null) {
            heapMemMap.put(heapMemInfo.addr, heapMemInfo);
            cpu.gpr[2] = heapMemInfo.addr;
        } else {
            cpu.gpr[2] = 0;
        }
    }

}