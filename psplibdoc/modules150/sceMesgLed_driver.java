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

public class sceMesgLed_driver implements HLEModule {
	@Override
	public String getName() { return "sceMesgLed_driver"; }
	
	@Override
	public void installModule(HLEModuleManager mm, int version) {
		if (version >= 150) {
			
			mm.addFunction(sceMesgLed_driver_84A04017Function, 0x84A04017);
			
			mm.addFunction(sceMesgLed_driver_A86D5005Function, 0xA86D5005);
			
			mm.addFunction(sceMesgLed_driver_A4547DF1Function, 0xA4547DF1);
			
			mm.addFunction(sceMesgLed_driver_94EB1072Function, 0x94EB1072);
			
			mm.addFunction(sceMesgLed_driver_198FD3BEFunction, 0x198FD3BE);
			
			mm.addFunction(sceMesgLed_driver_FBC694C7Function, 0xFBC694C7);
			
			mm.addFunction(sceMesgLed_driver_07E152BEFunction, 0x07E152BE);
			
			mm.addFunction(sceMesgLed_driver_9906F33AFunction, 0x9906F33A);
			
			mm.addFunction(sceMesgLed_driver_46AC0E78Function, 0x46AC0E78);
			
			mm.addFunction(sceMesgLed_driver_55C8785EFunction, 0x55C8785E);
			
			mm.addFunction(sceMesgLed_driver_67A5ECDFFunction, 0x67A5ECDF);
			
			mm.addFunction(sceMesgLed_driver_85B9D9F3Function, 0x85B9D9F3);
			
			mm.addFunction(sceMesgLed_driver_951F4A5BFunction, 0x951F4A5B);
			
			mm.addFunction(sceMesgLed_driver_58999D8EFunction, 0x58999D8E);
			
			mm.addFunction(sceMesgLed_driver_9FC926A0Function, 0x9FC926A0);
			
			mm.addFunction(sceMesgLed_driver_7A922276Function, 0x7A922276);
			
		}
	}
	
	@Override
	public void uninstallModule(HLEModuleManager mm, int version) {
		if (version >= 150) {
			
			mm.removeFunction(sceMesgLed_driver_84A04017Function);
			
			mm.removeFunction(sceMesgLed_driver_A86D5005Function);
			
			mm.removeFunction(sceMesgLed_driver_A4547DF1Function);
			
			mm.removeFunction(sceMesgLed_driver_94EB1072Function);
			
			mm.removeFunction(sceMesgLed_driver_198FD3BEFunction);
			
			mm.removeFunction(sceMesgLed_driver_FBC694C7Function);
			
			mm.removeFunction(sceMesgLed_driver_07E152BEFunction);
			
			mm.removeFunction(sceMesgLed_driver_9906F33AFunction);
			
			mm.removeFunction(sceMesgLed_driver_46AC0E78Function);
			
			mm.removeFunction(sceMesgLed_driver_55C8785EFunction);
			
			mm.removeFunction(sceMesgLed_driver_67A5ECDFFunction);
			
			mm.removeFunction(sceMesgLed_driver_85B9D9F3Function);
			
			mm.removeFunction(sceMesgLed_driver_951F4A5BFunction);
			
			mm.removeFunction(sceMesgLed_driver_58999D8EFunction);
			
			mm.removeFunction(sceMesgLed_driver_9FC926A0Function);
			
			mm.removeFunction(sceMesgLed_driver_7A922276Function);
			
		}
	}
	
	
	public void sceMesgLed_driver_84A04017(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceMesgLed_driver_84A04017 [0x84A04017]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceMesgLed_driver_A86D5005(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceMesgLed_driver_A86D5005 [0xA86D5005]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceMesgLed_driver_A4547DF1(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceMesgLed_driver_A4547DF1 [0xA4547DF1]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceMesgLed_driver_94EB1072(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceMesgLed_driver_94EB1072 [0x94EB1072]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceMesgLed_driver_198FD3BE(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceMesgLed_driver_198FD3BE [0x198FD3BE]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceMesgLed_driver_FBC694C7(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceMesgLed_driver_FBC694C7 [0xFBC694C7]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceMesgLed_driver_07E152BE(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceMesgLed_driver_07E152BE [0x07E152BE]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceMesgLed_driver_9906F33A(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceMesgLed_driver_9906F33A [0x9906F33A]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceMesgLed_driver_46AC0E78(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceMesgLed_driver_46AC0E78 [0x46AC0E78]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceMesgLed_driver_55C8785E(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceMesgLed_driver_55C8785E [0x55C8785E]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceMesgLed_driver_67A5ECDF(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceMesgLed_driver_67A5ECDF [0x67A5ECDF]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceMesgLed_driver_85B9D9F3(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceMesgLed_driver_85B9D9F3 [0x85B9D9F3]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceMesgLed_driver_951F4A5B(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceMesgLed_driver_951F4A5B [0x951F4A5B]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceMesgLed_driver_58999D8E(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceMesgLed_driver_58999D8E [0x58999D8E]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceMesgLed_driver_9FC926A0(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceMesgLed_driver_9FC926A0 [0x9FC926A0]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceMesgLed_driver_7A922276(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceMesgLed_driver_7A922276 [0x7A922276]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public final HLEModuleFunction sceMesgLed_driver_84A04017Function = new HLEModuleFunction("sceMesgLed_driver", "sceMesgLed_driver_84A04017") {
		@Override
		public final void execute(Processor processor) {
			sceMesgLed_driver_84A04017(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceMesgLed_driver.sceMesgLed_driver_84A04017Function.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceMesgLed_driver_A86D5005Function = new HLEModuleFunction("sceMesgLed_driver", "sceMesgLed_driver_A86D5005") {
		@Override
		public final void execute(Processor processor) {
			sceMesgLed_driver_A86D5005(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceMesgLed_driver.sceMesgLed_driver_A86D5005Function.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceMesgLed_driver_A4547DF1Function = new HLEModuleFunction("sceMesgLed_driver", "sceMesgLed_driver_A4547DF1") {
		@Override
		public final void execute(Processor processor) {
			sceMesgLed_driver_A4547DF1(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceMesgLed_driver.sceMesgLed_driver_A4547DF1Function.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceMesgLed_driver_94EB1072Function = new HLEModuleFunction("sceMesgLed_driver", "sceMesgLed_driver_94EB1072") {
		@Override
		public final void execute(Processor processor) {
			sceMesgLed_driver_94EB1072(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceMesgLed_driver.sceMesgLed_driver_94EB1072Function.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceMesgLed_driver_198FD3BEFunction = new HLEModuleFunction("sceMesgLed_driver", "sceMesgLed_driver_198FD3BE") {
		@Override
		public final void execute(Processor processor) {
			sceMesgLed_driver_198FD3BE(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceMesgLed_driver.sceMesgLed_driver_198FD3BEFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceMesgLed_driver_FBC694C7Function = new HLEModuleFunction("sceMesgLed_driver", "sceMesgLed_driver_FBC694C7") {
		@Override
		public final void execute(Processor processor) {
			sceMesgLed_driver_FBC694C7(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceMesgLed_driver.sceMesgLed_driver_FBC694C7Function.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceMesgLed_driver_07E152BEFunction = new HLEModuleFunction("sceMesgLed_driver", "sceMesgLed_driver_07E152BE") {
		@Override
		public final void execute(Processor processor) {
			sceMesgLed_driver_07E152BE(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceMesgLed_driver.sceMesgLed_driver_07E152BEFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceMesgLed_driver_9906F33AFunction = new HLEModuleFunction("sceMesgLed_driver", "sceMesgLed_driver_9906F33A") {
		@Override
		public final void execute(Processor processor) {
			sceMesgLed_driver_9906F33A(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceMesgLed_driver.sceMesgLed_driver_9906F33AFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceMesgLed_driver_46AC0E78Function = new HLEModuleFunction("sceMesgLed_driver", "sceMesgLed_driver_46AC0E78") {
		@Override
		public final void execute(Processor processor) {
			sceMesgLed_driver_46AC0E78(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceMesgLed_driver.sceMesgLed_driver_46AC0E78Function.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceMesgLed_driver_55C8785EFunction = new HLEModuleFunction("sceMesgLed_driver", "sceMesgLed_driver_55C8785E") {
		@Override
		public final void execute(Processor processor) {
			sceMesgLed_driver_55C8785E(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceMesgLed_driver.sceMesgLed_driver_55C8785EFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceMesgLed_driver_67A5ECDFFunction = new HLEModuleFunction("sceMesgLed_driver", "sceMesgLed_driver_67A5ECDF") {
		@Override
		public final void execute(Processor processor) {
			sceMesgLed_driver_67A5ECDF(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceMesgLed_driver.sceMesgLed_driver_67A5ECDFFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceMesgLed_driver_85B9D9F3Function = new HLEModuleFunction("sceMesgLed_driver", "sceMesgLed_driver_85B9D9F3") {
		@Override
		public final void execute(Processor processor) {
			sceMesgLed_driver_85B9D9F3(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceMesgLed_driver.sceMesgLed_driver_85B9D9F3Function.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceMesgLed_driver_951F4A5BFunction = new HLEModuleFunction("sceMesgLed_driver", "sceMesgLed_driver_951F4A5B") {
		@Override
		public final void execute(Processor processor) {
			sceMesgLed_driver_951F4A5B(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceMesgLed_driver.sceMesgLed_driver_951F4A5BFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceMesgLed_driver_58999D8EFunction = new HLEModuleFunction("sceMesgLed_driver", "sceMesgLed_driver_58999D8E") {
		@Override
		public final void execute(Processor processor) {
			sceMesgLed_driver_58999D8E(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceMesgLed_driver.sceMesgLed_driver_58999D8EFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceMesgLed_driver_9FC926A0Function = new HLEModuleFunction("sceMesgLed_driver", "sceMesgLed_driver_9FC926A0") {
		@Override
		public final void execute(Processor processor) {
			sceMesgLed_driver_9FC926A0(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceMesgLed_driver.sceMesgLed_driver_9FC926A0Function.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceMesgLed_driver_7A922276Function = new HLEModuleFunction("sceMesgLed_driver", "sceMesgLed_driver_7A922276") {
		@Override
		public final void execute(Processor processor) {
			sceMesgLed_driver_7A922276(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.sceMesgLed_driver.sceMesgLed_driver_7A922276Function.execute(processor);";
		}
	};
    
};
