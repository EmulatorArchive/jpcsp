/* This autogenerated file is part of jpcsp. */
package jpcsp.HLE.modules;

import jpcsp.HLE.pspSysMem;
import jpcsp.Memory;
import jpcsp.Processor;

public class sceChkreg_driver implements HLEModule {

    @Override
    public final String getName() {
        return "sceChkreg_driver";
    }

    @Override
    public void installModule(HLEModuleManager mm, int version) {

        mm.add(sceChkregCheckRegion, 0x54495B19);

        mm.add(sceChkregGetPsCode, 0x59F8491D);

    }

    @Override
    public void uninstallModule(HLEModuleManager mm, int version) {

        mm.remove(sceChkregCheckRegion);

        mm.remove(sceChkregGetPsCode);

    }
    public static final HLEModuleFunction sceChkregCheckRegion = new HLEModuleFunction("sceChkreg_driver", "sceChkregCheckRegion") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceChkregCheckRegion [0x54495B19]");
        }
    };
    public static final HLEModuleFunction sceChkregGetPsCode = new HLEModuleFunction("sceChkreg_driver", "sceChkregGetPsCode") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceChkregGetPsCode [0x59F8491D]");
        }
    };
};
