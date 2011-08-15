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
import jpcsp.Memory;
import jpcsp.Processor;
import jpcsp.Settings;
import jpcsp.Allegrex.CpuState;
import jpcsp.HLE.Modules;
import jpcsp.HLE.modules.HLEModule;
import jpcsp.HLE.modules.HLEModuleFunction;
import jpcsp.HLE.modules.HLEModuleManager;
import jpcsp.HLE.modules.HLEStartModule;
import jpcsp.hardware.Battery;

import org.apache.log4j.Logger;

public class sceImpose implements HLEModule, HLEStartModule {
    private static Logger log = Modules.getLogger("sceImpose");

	@Override
	public String getName() { return "sceImpose"; }

	@Override
    public void start() {
        languageMode_language = Settings.getInstance().readInt("emu.impose.language", PSP_LANGUAGE_ENGLISH);
        languageMode_button = Settings.getInstance().readInt("emu.impose.button", PSP_CONFIRM_BUTTON_CROSS);
    }

    @Override
    public void stop() {
    }

    public final static int PSP_LANGUAGE_JAPANESE = 0;
    public final static int PSP_LANGUAGE_ENGLISH = 1;
    public final static int PSP_LANGUAGE_FRENCH = 2;
    public final static int PSP_LANGUAGE_SPANISH = 3;
    public final static int PSP_LANGUAGE_GERMAN = 4;
    public final static int PSP_LANGUAGE_ITALIAN = 5;
    public final static int PSP_LANGUAGE_DUTCH = 6;
    public final static int PSP_LANGUAGE_PORTUGUESE = 7;
    public final static int PSP_LANGUAGE_RUSSIAN = 8;
    public final static int PSP_LANGUAGE_KOREAN = 9;
    public final static int PSP_LANGUAGE_TRADITIONAL_CHINESE = 10;
    public final static int PSP_LANGUAGE_SIMPLIFIED_CHINESE = 11;
    private int languageMode_language;

    public final static int PSP_CONFIRM_BUTTON_CIRCLE = 0;
    public final static int PSP_CONFIRM_BUTTON_CROSS = 1;
    private int languageMode_button;

    public final static int PSP_UMD_POPUP_DISABLE = 0;
    public final static int PSP_UMD_POPUP_ENABLE = 1;
    private int umdPopupStatus;

    private int backlightOffTime;

	@HLEFunction(nid = 0x381BD9E7, version = 150)
	public void sceImposeHomeButton(Processor processor) {
	    CpuState cpu = processor.cpu;

		log.warn("Unimplemented NID function sceImposeHomeButton [0x381BD9E7]");

		cpu.gpr[2] = 0xDEADC0DE;
	}

	@HLEFunction(nid = 0x5595A71A, version = 150)
	public void sceImposeSetHomePopup(Processor processor) {
		CpuState cpu = processor.cpu;

		log.warn("Unimplemented NID function sceImposeSetHomePopup [0x5595A71A]");

		cpu.gpr[2] = 0xDEADC0DE;
	}

	@HLEFunction(nid = 0x0F341BE4, version = 150)
	public void sceImposeGetHomePopup(Processor processor) {
		CpuState cpu = processor.cpu;

		log.warn("Unimplemented NID function sceImposeGetHomePopup [0x0F341BE4]");

		cpu.gpr[2] = 0xDEADC0DE;
	}

	@HLEFunction(nid = 0x72189C48, version = 150)
	public void sceImposeSetUMDPopup(Processor processor) {
		CpuState cpu = processor.cpu;

        int mode = cpu.gpr[4];

		log.debug("sceImposeSetUMDPopup(mode=" + mode + ")");

        umdPopupStatus = mode;

		cpu.gpr[2] = 0;
	}

	@HLEFunction(nid = 0xE0887BC8, version = 150)
	public void sceImposeGetUMDPopup(Processor processor) {
		CpuState cpu = processor.cpu;

		log.debug("sceImposeGetUMDPopup)");

		cpu.gpr[2] = umdPopupStatus;
	}

	@HLEFunction(nid = 0x36AA6E91, version = 150)
	public void sceImposeSetLanguageMode(Processor processor) {
		CpuState cpu = processor.cpu;

        int lang = cpu.gpr[4];
        int button = cpu.gpr[5];

        String langStr;
        switch(lang) {
            case PSP_LANGUAGE_JAPANESE: langStr = "JAP"; break;
            case PSP_LANGUAGE_ENGLISH: langStr = "ENG"; break;
            case PSP_LANGUAGE_FRENCH: langStr = "FR"; break;
            case PSP_LANGUAGE_KOREAN: langStr = "KOR"; break;
            default: langStr = "PSP_LANGUAGE_UNKNOWN" + lang; break;
        }

		log.debug("sceImposeSetLanguageMode(lang=" + lang + "(" + langStr + "),button=" + button + ")");

        languageMode_language = lang;
        languageMode_button = button;

		cpu.gpr[2] = 0;
	}

    @HLEFunction(nid = 0x24FD7BCF, version = 150)
    public void sceImposeGetLanguageMode(Processor processor) {
        CpuState cpu = processor.cpu;
        Memory mem = Processor.memory;

        int lang_addr = cpu.gpr[4];
        int button_addr = cpu.gpr[5];

        log.debug("sceImposeGetLanguageMode(lang=0x" + Integer.toHexString(lang_addr)
            + ",button=0x" + Integer.toHexString(button_addr) + ")"
            + " returning lang=" + languageMode_language + " button=" + languageMode_button);

        if (Memory.isAddressGood(lang_addr)) {
            mem.write32(lang_addr, languageMode_language);
        }

        if (Memory.isAddressGood(button_addr)) {
            mem.write32(button_addr, languageMode_button);
        }

        cpu.gpr[2] = 0;
    }

	@HLEFunction(nid = 0x8C943191, version = 150)
	public void sceImposeGetBatteryIconStatus(Processor processor) {
		CpuState cpu = processor.cpu;
		Memory mem = Processor.memory;

        int addrCharging = cpu.gpr[4];
        int addrIconStatus = cpu.gpr[5];
        int batteryPowerPercent = Battery.getCurrentPowerPercent();
        // Possible values for iconStatus: 0..3
        int iconStatus = Math.min(batteryPowerPercent / 25, 3);
        boolean charging = Battery.isCharging();

        if (Memory.isAddressGood(addrCharging)) {
            mem.write32(addrCharging, charging ? 1 : 0); // Values: 0..1
        }
        if (Memory.isAddressGood(addrIconStatus)) {
            mem.write32(addrIconStatus, iconStatus); // Values: 0..3
        }

		cpu.gpr[2] = 0;
	}

    @HLEFunction(nid = 0x8F6E3518, version = 150)
    public void sceImposeGetBacklightOffTime(Processor processor) {
		CpuState cpu = processor.cpu;

		log.debug("sceImposeGetBacklightOffTime");

		cpu.gpr[2] = backlightOffTime;
	}

    @HLEFunction(nid = 0x967F6D4A, version = 150)
    public void sceImposeSetBacklightOffTime(Processor processor) {
		CpuState cpu = processor.cpu;

        int time = cpu.gpr[4];

		log.debug("sceImposeSetBacklightOffTime (time=" + time + ")");

        backlightOffTime = time;

		cpu.gpr[2] = 0;
	}

}