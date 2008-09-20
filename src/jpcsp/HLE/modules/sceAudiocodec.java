/* This autogenerated file is part of jpcsp. */
package jpcsp.HLE.modules;

import jpcsp.HLE.pspSysMem;
import jpcsp.Memory;
import jpcsp.Processor;

public class sceAudiocodec implements HLEModule {

    @Override
    public final String getName() {
        return "sceAudiocodec";
    }

    @Override
    public void installModule(HLEModuleManager mm, int version) {

        mm.add(sceAudiocodeCheckNeedMem, 0x9D3F790C);

        mm.add(sceAudiocodecInit, 0x5B37EB1D);

        mm.add(sceAudiocodecDecode, 0x70A703F8);

        mm.add(sceAudiocodecGetInfo, 0x8ACA11D5);

        mm.add(sceAudiocodec_6CD2A861, 0x6CD2A861);

        mm.add(sceAudiocodec_59176A0F, 0x59176A0F);

        mm.add(sceAudiocodecGetEDRAM, 0x3A20A200);

        mm.add(sceAudiocodecReleaseEDRAM, 0x29681260);

    }

    @Override
    public void uninstallModule(HLEModuleManager mm, int version) {

        mm.remove(sceAudiocodeCheckNeedMem);

        mm.remove(sceAudiocodecInit);

        mm.remove(sceAudiocodecDecode);

        mm.remove(sceAudiocodecGetInfo);

        mm.remove(sceAudiocodec_6CD2A861);

        mm.remove(sceAudiocodec_59176A0F);

        mm.remove(sceAudiocodecGetEDRAM);

        mm.remove(sceAudiocodecReleaseEDRAM);

    }
    public static final HLEModuleFunction sceAudiocodeCheckNeedMem = new HLEModuleFunction("sceAudiocodec", "sceAudiocodeCheckNeedMem") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceAudiocodeCheckNeedMem [0x9D3F790C]");
        }
    };
    public static final HLEModuleFunction sceAudiocodecInit = new HLEModuleFunction("sceAudiocodec", "sceAudiocodecInit") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceAudiocodecInit [0x5B37EB1D]");
        }
    };
    public static final HLEModuleFunction sceAudiocodecDecode = new HLEModuleFunction("sceAudiocodec", "sceAudiocodecDecode") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceAudiocodecDecode [0x70A703F8]");
        }
    };
    public static final HLEModuleFunction sceAudiocodecGetInfo = new HLEModuleFunction("sceAudiocodec", "sceAudiocodecGetInfo") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceAudiocodecGetInfo [0x8ACA11D5]");
        }
    };
    public static final HLEModuleFunction sceAudiocodec_6CD2A861 = new HLEModuleFunction("sceAudiocodec", "sceAudiocodec_6CD2A861") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceAudiocodec_6CD2A861 [0x6CD2A861]");
        }
    };
    public static final HLEModuleFunction sceAudiocodec_59176A0F = new HLEModuleFunction("sceAudiocodec", "sceAudiocodec_59176A0F") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceAudiocodec_59176A0F [0x59176A0F]");
        }
    };
    public static final HLEModuleFunction sceAudiocodecGetEDRAM = new HLEModuleFunction("sceAudiocodec", "sceAudiocodecGetEDRAM") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceAudiocodecGetEDRAM [0x3A20A200]");
        }
    };
    public static final HLEModuleFunction sceAudiocodecReleaseEDRAM = new HLEModuleFunction("sceAudiocodec", "sceAudiocodecReleaseEDRAM") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceAudiocodecReleaseEDRAM [0x29681260]");
        }
    };
};
