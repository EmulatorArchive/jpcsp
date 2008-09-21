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

public class sceHprm implements HLEModule {
	@Override
	public String getName() { return "sceHprm"; }
	
	@Override
	public void installModule(HLEModuleManager mm, int version) {
		if (version >= 150) {
			
			mm.addFunction(sceHprmRegisterCallbackFunction, 0xC7154136);
			
			mm.addFunction(sceHprmUnregisterCallbackFunction, 0x444ED0B7);
			
			mm.addFunction(sceHprm_71B5FB67Function, 0x71B5FB67);
			
			mm.addFunction(sceHprmIsRemoteExistFunction, 0x208DB1BD);
			
			mm.addFunction(sceHprmIsHeadphoneExistFunction, 0x7E69EDA4);
			
			mm.addFunction(sceHprmIsMicrophoneExistFunction, 0x219C58F1);
			
			mm.addFunction(sceHprmPeekCurrentKeyFunction, 0x1910B327);
			
			mm.addFunction(sceHprmPeekLatchFunction, 0x2BCEC83E);
			
			mm.addFunction(sceHprmReadLatchFunction, 0x40D2F9F0);
			
		}
	}
	
	@Override
	public void uninstallModule(HLEModuleManager mm, int version) {
		if (version >= 150) {
			
			mm.removeFunction(sceHprmRegisterCallbackFunction);
			
			mm.removeFunction(sceHprmUnregisterCallbackFunction);
			
			mm.removeFunction(sceHprm_71B5FB67Function);
			
			mm.removeFunction(sceHprmIsRemoteExistFunction);
			
			mm.removeFunction(sceHprmIsHeadphoneExistFunction);
			
			mm.removeFunction(sceHprmIsMicrophoneExistFunction);
			
			mm.removeFunction(sceHprmPeekCurrentKeyFunction);
			
			mm.removeFunction(sceHprmPeekLatchFunction);
			
			mm.removeFunction(sceHprmReadLatchFunction);
			
		}
	}
	
	
	public void sceHprmRegisterCallback(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceHprmRegisterCallback [0xC7154136]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceHprmUnregisterCallback(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceHprmUnregisterCallback [0x444ED0B7]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceHprm_71B5FB67(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceHprm_71B5FB67 [0x71B5FB67]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceHprmIsRemoteExist(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceHprmIsRemoteExist [0x208DB1BD]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceHprmIsHeadphoneExist(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceHprmIsHeadphoneExist [0x7E69EDA4]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceHprmIsMicrophoneExist(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceHprmIsMicrophoneExist [0x219C58F1]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceHprmPeekCurrentKey(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceHprmPeekCurrentKey [0x1910B327]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceHprmPeekLatch(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceHprmPeekLatch [0x2BCEC83E]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceHprmReadLatch(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceHprmReadLatch [0x40D2F9F0]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public final HLEModuleFunction sceHprmRegisterCallbackFunction = new HLEModuleFunction("sceHprm", "sceHprmRegisterCallback") {
		@Override
		public final void execute(Processor processor) {
			sceHprmRegisterCallback(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceHprm.sceHprmRegisterCallbackFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceHprmUnregisterCallbackFunction = new HLEModuleFunction("sceHprm", "sceHprmUnregisterCallback") {
		@Override
		public final void execute(Processor processor) {
			sceHprmUnregisterCallback(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceHprm.sceHprmUnregisterCallbackFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceHprm_71B5FB67Function = new HLEModuleFunction("sceHprm", "sceHprm_71B5FB67") {
		@Override
		public final void execute(Processor processor) {
			sceHprm_71B5FB67(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceHprm.sceHprm_71B5FB67Function.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceHprmIsRemoteExistFunction = new HLEModuleFunction("sceHprm", "sceHprmIsRemoteExist") {
		@Override
		public final void execute(Processor processor) {
			sceHprmIsRemoteExist(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceHprm.sceHprmIsRemoteExistFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceHprmIsHeadphoneExistFunction = new HLEModuleFunction("sceHprm", "sceHprmIsHeadphoneExist") {
		@Override
		public final void execute(Processor processor) {
			sceHprmIsHeadphoneExist(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceHprm.sceHprmIsHeadphoneExistFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceHprmIsMicrophoneExistFunction = new HLEModuleFunction("sceHprm", "sceHprmIsMicrophoneExist") {
		@Override
		public final void execute(Processor processor) {
			sceHprmIsMicrophoneExist(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceHprm.sceHprmIsMicrophoneExistFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceHprmPeekCurrentKeyFunction = new HLEModuleFunction("sceHprm", "sceHprmPeekCurrentKey") {
		@Override
		public final void execute(Processor processor) {
			sceHprmPeekCurrentKey(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceHprm.sceHprmPeekCurrentKeyFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceHprmPeekLatchFunction = new HLEModuleFunction("sceHprm", "sceHprmPeekLatch") {
		@Override
		public final void execute(Processor processor) {
			sceHprmPeekLatch(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceHprm.sceHprmPeekLatchFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceHprmReadLatchFunction = new HLEModuleFunction("sceHprm", "sceHprmReadLatch") {
		@Override
		public final void execute(Processor processor) {
			sceHprmReadLatch(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceHprm.sceHprmReadLatchFunction.execute(processor);";
		}
	};
    
};
