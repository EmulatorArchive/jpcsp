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

public class sceImpose implements HLEModule {
	@Override
	public String getName() { return "sceImpose"; }
	
	@Override
	public void installModule(HLEModuleManager mm, int version) {
		if (version >= 150) {
		
			mm.addFunction(sceImposeHomeButtonFunction, 0x381BD9E7);
			mm.addFunction(sceImposeSetHomePopupFunction, 0x5595A71A);
			mm.addFunction(sceImposeGetHomePopupFunction, 0x0F341BE4);
			mm.addFunction(sceImposeSetUMDPopupFunction, 0x72189C48);
			mm.addFunction(sceImposeGetUMDPopupFunction, 0xE0887BC8);
			mm.addFunction(sceImposeSetLanguageModeFunction, 0x36AA6E91);
			mm.addFunction(sceImposeGetLanguageModeFunction, 0x24FD7BCF);
			mm.addFunction(sceImposeGetBatteryIconStatusFunction, 0x8C943191);
			
		}
	}
	
	@Override
	public void uninstallModule(HLEModuleManager mm, int version) {
		if (version >= 150) {
		
			mm.removeFunction(sceImposeHomeButtonFunction);
			mm.removeFunction(sceImposeSetHomePopupFunction);
			mm.removeFunction(sceImposeGetHomePopupFunction);
			mm.removeFunction(sceImposeSetUMDPopupFunction);
			mm.removeFunction(sceImposeGetUMDPopupFunction);
			mm.removeFunction(sceImposeSetLanguageModeFunction);
			mm.removeFunction(sceImposeGetLanguageModeFunction);
			mm.removeFunction(sceImposeGetBatteryIconStatusFunction);
			
		}
	}
	
	
	public void sceImposeHomeButton(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;
		
		/* put your own code here instead */

		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

		System.out.println("Unimplemented NID function sceImposeHomeButton [0x381BD9E7]");

		cpu.gpr[2] = 0xDEADC0DE;

		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32); cpu.fpr[0] = result;
	}
    
	public void sceImposeSetHomePopup(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;
		
		/* put your own code here instead */

		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

		System.out.println("Unimplemented NID function sceImposeSetHomePopup [0x5595A71A]");

		cpu.gpr[2] = 0xDEADC0DE;

		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32); cpu.fpr[0] = result;
	}
    
	public void sceImposeGetHomePopup(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;
		
		/* put your own code here instead */

		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

		System.out.println("Unimplemented NID function sceImposeGetHomePopup [0x0F341BE4]");

		cpu.gpr[2] = 0xDEADC0DE;

		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32); cpu.fpr[0] = result;
	}
    
	public void sceImposeSetUMDPopup(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;
		
		/* put your own code here instead */

		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

		System.out.println("Unimplemented NID function sceImposeSetUMDPopup [0x72189C48]");

		cpu.gpr[2] = 0xDEADC0DE;

		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32); cpu.fpr[0] = result;
	}
    
	public void sceImposeGetUMDPopup(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;
		
		/* put your own code here instead */

		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

		System.out.println("Unimplemented NID function sceImposeGetUMDPopup [0xE0887BC8]");

		cpu.gpr[2] = 0xDEADC0DE;

		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32); cpu.fpr[0] = result;
	}
    
	public void sceImposeSetLanguageMode(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;
		
		/* put your own code here instead */

		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

		System.out.println("Unimplemented NID function sceImposeSetLanguageMode [0x36AA6E91]");

		cpu.gpr[2] = 0xDEADC0DE;

		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32); cpu.fpr[0] = result;
	}
    
	public void sceImposeGetLanguageMode(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;
		
		/* put your own code here instead */

		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

		System.out.println("Unimplemented NID function sceImposeGetLanguageMode [0x24FD7BCF]");

		cpu.gpr[2] = 0xDEADC0DE;

		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32); cpu.fpr[0] = result;
	}
    
	public void sceImposeGetBatteryIconStatus(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;
		
		/* put your own code here instead */

		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

		System.out.println("Unimplemented NID function sceImposeGetBatteryIconStatus [0x8C943191]");

		cpu.gpr[2] = 0xDEADC0DE;

		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32); cpu.fpr[0] = result;
	}
    
	public final HLEModuleFunction sceImposeHomeButtonFunction = new HLEModuleFunction("sceImpose", "sceImposeHomeButton") {
		@Override
		public final void execute(Processor processor) {
			sceImposeHomeButton(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceImposeModule.sceImposeHomeButton(processor);";
		}
	};
    
	public final HLEModuleFunction sceImposeSetHomePopupFunction = new HLEModuleFunction("sceImpose", "sceImposeSetHomePopup") {
		@Override
		public final void execute(Processor processor) {
			sceImposeSetHomePopup(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceImposeModule.sceImposeSetHomePopup(processor);";
		}
	};
    
	public final HLEModuleFunction sceImposeGetHomePopupFunction = new HLEModuleFunction("sceImpose", "sceImposeGetHomePopup") {
		@Override
		public final void execute(Processor processor) {
			sceImposeGetHomePopup(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceImposeModule.sceImposeGetHomePopup(processor);";
		}
	};
    
	public final HLEModuleFunction sceImposeSetUMDPopupFunction = new HLEModuleFunction("sceImpose", "sceImposeSetUMDPopup") {
		@Override
		public final void execute(Processor processor) {
			sceImposeSetUMDPopup(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceImposeModule.sceImposeSetUMDPopup(processor);";
		}
	};
    
	public final HLEModuleFunction sceImposeGetUMDPopupFunction = new HLEModuleFunction("sceImpose", "sceImposeGetUMDPopup") {
		@Override
		public final void execute(Processor processor) {
			sceImposeGetUMDPopup(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceImposeModule.sceImposeGetUMDPopup(processor);";
		}
	};
    
	public final HLEModuleFunction sceImposeSetLanguageModeFunction = new HLEModuleFunction("sceImpose", "sceImposeSetLanguageMode") {
		@Override
		public final void execute(Processor processor) {
			sceImposeSetLanguageMode(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceImposeModule.sceImposeSetLanguageMode(processor);";
		}
	};
    
	public final HLEModuleFunction sceImposeGetLanguageModeFunction = new HLEModuleFunction("sceImpose", "sceImposeGetLanguageMode") {
		@Override
		public final void execute(Processor processor) {
			sceImposeGetLanguageMode(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceImposeModule.sceImposeGetLanguageMode(processor);";
		}
	};
    
	public final HLEModuleFunction sceImposeGetBatteryIconStatusFunction = new HLEModuleFunction("sceImpose", "sceImposeGetBatteryIconStatus") {
		@Override
		public final void execute(Processor processor) {
			sceImposeGetBatteryIconStatus(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceImposeModule.sceImposeGetBatteryIconStatus(processor);";
		}
	};
    
};
