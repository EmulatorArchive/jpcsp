/* This autogenerated file is part of jpcsp. */
package jpcsp.HLE.modules;

import jpcsp.HLE.pspSysMem;
import jpcsp.Memory;
import jpcsp.Processor;

public class SysMemUserForUser implements HLEModule {

    @Override
    public final String getName() {
        return "SysMemUserForUser";
    }

    @Override
    public void installModule(HLEModuleManager mm, int version) {

        mm.add(sceKernelMaxFreeMemSize, 0xA291F107);

        mm.add(sceKernelTotalFreeMemSize, 0xF919F628);

        mm.add(sceKernelAllocPartitionMemory, 0x237DBD4F);

        mm.add(sceKernelFreePartitionMemory, 0xB6D61D02);

        mm.add(sceKernelGetBlockHeadAddr, 0x9D9A5BA1);

        mm.add(sceKernelPrintf, 0x13A5ABEF);

        mm.add(sceKernelDevkitVersion, 0x3FC9AE6A);

    }

    @Override
    public void uninstallModule(HLEModuleManager mm, int version) {

        mm.remove(sceKernelMaxFreeMemSize);

        mm.remove(sceKernelTotalFreeMemSize);

        mm.remove(sceKernelAllocPartitionMemory);

        mm.remove(sceKernelFreePartitionMemory);

        mm.remove(sceKernelGetBlockHeadAddr);

        mm.remove(sceKernelPrintf);

        mm.remove(sceKernelDevkitVersion);

    }
    public static final HLEModuleFunction sceKernelMaxFreeMemSize = new HLEModuleFunction("SysMemUserForUser", "sceKernelMaxFreeMemSize") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceKernelMaxFreeMemSize [0xA291F107]");
        }
    };
    public static final HLEModuleFunction sceKernelTotalFreeMemSize = new HLEModuleFunction("SysMemUserForUser", "sceKernelTotalFreeMemSize") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceKernelTotalFreeMemSize [0xF919F628]");
        }
    };
    public static final HLEModuleFunction sceKernelAllocPartitionMemory = new HLEModuleFunction("SysMemUserForUser", "sceKernelAllocPartitionMemory") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceKernelAllocPartitionMemory [0x237DBD4F]");
        }
    };
    public static final HLEModuleFunction sceKernelFreePartitionMemory = new HLEModuleFunction("SysMemUserForUser", "sceKernelFreePartitionMemory") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceKernelFreePartitionMemory [0xB6D61D02]");
        }
    };
    public static final HLEModuleFunction sceKernelGetBlockHeadAddr = new HLEModuleFunction("SysMemUserForUser", "sceKernelGetBlockHeadAddr") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceKernelGetBlockHeadAddr [0x9D9A5BA1]");
        }
    };
    public static final HLEModuleFunction sceKernelPrintf = new HLEModuleFunction("SysMemUserForUser", "sceKernelPrintf") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceKernelPrintf [0x13A5ABEF]");
        }
    };
    public static final HLEModuleFunction sceKernelDevkitVersion = new HLEModuleFunction("SysMemUserForUser", "sceKernelDevkitVersion") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceKernelDevkitVersion [0x3FC9AE6A]");
        }
    };
};
