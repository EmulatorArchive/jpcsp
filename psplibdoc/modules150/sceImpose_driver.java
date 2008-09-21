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

public class sceImpose_driver implements HLEModule {
	@Override
	public String getName() { return "sceImpose_driver"; }
	
	@Override
	public void installModule(HLEModuleManager mm, int version) {
		if (version >= 150) {
			
			mm.addFunction(sceImposeInitFunction, 0xBDBC42A6);
			
			mm.addFunction(sceImposeEndFunction, 0xC7E36CC7);
			
			mm.addFunction(sceImposeSuspendFunction, 0x1AEED8FE);
			
			mm.addFunction(sceImposeResumeFunction, 0x86924032);
			
			mm.addFunction(sceImposeGetStatusFunction, 0x1B6E3400);
			
			mm.addFunction(sceImposeSetStatusFunction, 0x9C8C6C81);
			
			mm.addFunction(sceImposeGetParamFunction, 0x531C9778);
			
			mm.addFunction(sceImposeSetParamFunction, 0x810FB7FB);
			
			mm.addFunction(sceImposeChangesFunction, 0xB415FC59);
			
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
			
			mm.removeFunction(sceImposeInitFunction);
			
			mm.removeFunction(sceImposeEndFunction);
			
			mm.removeFunction(sceImposeSuspendFunction);
			
			mm.removeFunction(sceImposeResumeFunction);
			
			mm.removeFunction(sceImposeGetStatusFunction);
			
			mm.removeFunction(sceImposeSetStatusFunction);
			
			mm.removeFunction(sceImposeGetParamFunction);
			
			mm.removeFunction(sceImposeSetParamFunction);
			
			mm.removeFunction(sceImposeChangesFunction);
			
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
	
	
	public void sceImposeInit(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceImposeInit [0xBDBC42A6]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceImposeEnd(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceImposeEnd [0xC7E36CC7]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceImposeSuspend(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceImposeSuspend [0x1AEED8FE]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceImposeResume(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceImposeResume [0x86924032]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceImposeGetStatus(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceImposeGetStatus [0x1B6E3400]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceImposeSetStatus(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceImposeSetStatus [0x9C8C6C81]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceImposeGetParam(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceImposeGetParam [0x531C9778]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceImposeSetParam(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceImposeSetParam [0x810FB7FB]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceImposeChanges(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceImposeChanges [0xB415FC59]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceImposeHomeButton(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceImposeHomeButton [0x381BD9E7]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceImposeSetHomePopup(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceImposeSetHomePopup [0x5595A71A]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceImposeGetHomePopup(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceImposeGetHomePopup [0x0F341BE4]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceImposeSetUMDPopup(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceImposeSetUMDPopup [0x72189C48]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceImposeGetUMDPopup(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceImposeGetUMDPopup [0xE0887BC8]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceImposeSetLanguageMode(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceImposeSetLanguageMode [0x36AA6E91]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceImposeGetLanguageMode(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceImposeGetLanguageMode [0x24FD7BCF]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceImposeGetBatteryIconStatus(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceImposeGetBatteryIconStatus [0x8C943191]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public final HLEModuleFunction sceImposeInitFunction = new HLEModuleFunction("sceImpose_driver", "sceImposeInit") {
		@Override
		public final void execute(Processor processor) {
			sceImposeInit(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceImpose_driver.sceImposeInitFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceImposeEndFunction = new HLEModuleFunction("sceImpose_driver", "sceImposeEnd") {
		@Override
		public final void execute(Processor processor) {
			sceImposeEnd(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceImpose_driver.sceImposeEndFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceImposeSuspendFunction = new HLEModuleFunction("sceImpose_driver", "sceImposeSuspend") {
		@Override
		public final void execute(Processor processor) {
			sceImposeSuspend(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceImpose_driver.sceImposeSuspendFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceImposeResumeFunction = new HLEModuleFunction("sceImpose_driver", "sceImposeResume") {
		@Override
		public final void execute(Processor processor) {
			sceImposeResume(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceImpose_driver.sceImposeResumeFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceImposeGetStatusFunction = new HLEModuleFunction("sceImpose_driver", "sceImposeGetStatus") {
		@Override
		public final void execute(Processor processor) {
			sceImposeGetStatus(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceImpose_driver.sceImposeGetStatusFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceImposeSetStatusFunction = new HLEModuleFunction("sceImpose_driver", "sceImposeSetStatus") {
		@Override
		public final void execute(Processor processor) {
			sceImposeSetStatus(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceImpose_driver.sceImposeSetStatusFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceImposeGetParamFunction = new HLEModuleFunction("sceImpose_driver", "sceImposeGetParam") {
		@Override
		public final void execute(Processor processor) {
			sceImposeGetParam(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceImpose_driver.sceImposeGetParamFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceImposeSetParamFunction = new HLEModuleFunction("sceImpose_driver", "sceImposeSetParam") {
		@Override
		public final void execute(Processor processor) {
			sceImposeSetParam(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceImpose_driver.sceImposeSetParamFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceImposeChangesFunction = new HLEModuleFunction("sceImpose_driver", "sceImposeChanges") {
		@Override
		public final void execute(Processor processor) {
			sceImposeChanges(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceImpose_driver.sceImposeChangesFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceImposeHomeButtonFunction = new HLEModuleFunction("sceImpose_driver", "sceImposeHomeButton") {
		@Override
		public final void execute(Processor processor) {
			sceImposeHomeButton(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceImpose_driver.sceImposeHomeButtonFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceImposeSetHomePopupFunction = new HLEModuleFunction("sceImpose_driver", "sceImposeSetHomePopup") {
		@Override
		public final void execute(Processor processor) {
			sceImposeSetHomePopup(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceImpose_driver.sceImposeSetHomePopupFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceImposeGetHomePopupFunction = new HLEModuleFunction("sceImpose_driver", "sceImposeGetHomePopup") {
		@Override
		public final void execute(Processor processor) {
			sceImposeGetHomePopup(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceImpose_driver.sceImposeGetHomePopupFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceImposeSetUMDPopupFunction = new HLEModuleFunction("sceImpose_driver", "sceImposeSetUMDPopup") {
		@Override
		public final void execute(Processor processor) {
			sceImposeSetUMDPopup(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceImpose_driver.sceImposeSetUMDPopupFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceImposeGetUMDPopupFunction = new HLEModuleFunction("sceImpose_driver", "sceImposeGetUMDPopup") {
		@Override
		public final void execute(Processor processor) {
			sceImposeGetUMDPopup(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceImpose_driver.sceImposeGetUMDPopupFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceImposeSetLanguageModeFunction = new HLEModuleFunction("sceImpose_driver", "sceImposeSetLanguageMode") {
		@Override
		public final void execute(Processor processor) {
			sceImposeSetLanguageMode(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceImpose_driver.sceImposeSetLanguageModeFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceImposeGetLanguageModeFunction = new HLEModuleFunction("sceImpose_driver", "sceImposeGetLanguageMode") {
		@Override
		public final void execute(Processor processor) {
			sceImposeGetLanguageMode(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceImpose_driver.sceImposeGetLanguageModeFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceImposeGetBatteryIconStatusFunction = new HLEModuleFunction("sceImpose_driver", "sceImposeGetBatteryIconStatus") {
		@Override
		public final void execute(Processor processor) {
			sceImposeGetBatteryIconStatus(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceImpose_driver.sceImposeGetBatteryIconStatusFunction.execute(processor);";
		}
	};
    
};
