/* This autogenerated file is part of jpcsp. */
package jpcsp.HLE.modules;

import jpcsp.HLE.pspSysMem;
import jpcsp.Memory;
import jpcsp.Processor;

public class LflashFatfmt implements HLEModule {

    @Override
    public final String getName() {
        return "LflashFatfmt";
    }

    @Override
    public void installModule(HLEModuleManager mm, int version) {

        mm.add(sceLflashFatfmtStartFatfmt, 0xB7A424A4);

    }

    @Override
    public void uninstallModule(HLEModuleManager mm, int version) {

        mm.remove(sceLflashFatfmtStartFatfmt);

    }
    public static final HLEModuleFunction sceLflashFatfmtStartFatfmt = new HLEModuleFunction("LflashFatfmt", "sceLflashFatfmtStartFatfmt") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceLflashFatfmtStartFatfmt [0xB7A424A4]");
        }
    };
};
