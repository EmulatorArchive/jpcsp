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

public class scePEQ_driver implements HLEModule {
	@Override
	public String getName() { return "scePEQ_driver"; }
	
	@Override
	public void installModule(HLEModuleManager mm, int version) {
		if (version >= 150) {
			
			mm.addFunction(scePEQ_driver_213DE849Function, 0x213DE849);
			
			mm.addFunction(scePEQ_driver_FC45514BFunction, 0xFC45514B);
			
			mm.addFunction(scePEQ_driver_F7EA0632Function, 0xF7EA0632);
			
			mm.addFunction(scePEQ_driver_ED13C3B5Function, 0xED13C3B5);
			
		}
	}
	
	@Override
	public void uninstallModule(HLEModuleManager mm, int version) {
		if (version >= 150) {
			
			mm.removeFunction(scePEQ_driver_213DE849Function);
			
			mm.removeFunction(scePEQ_driver_FC45514BFunction);
			
			mm.removeFunction(scePEQ_driver_F7EA0632Function);
			
			mm.removeFunction(scePEQ_driver_ED13C3B5Function);
			
		}
	}
	
	
	public void scePEQ_driver_213DE849(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function scePEQ_driver_213DE849 [0x213DE849]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void scePEQ_driver_FC45514B(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function scePEQ_driver_FC45514B [0xFC45514B]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void scePEQ_driver_F7EA0632(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function scePEQ_driver_F7EA0632 [0xF7EA0632]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void scePEQ_driver_ED13C3B5(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function scePEQ_driver_ED13C3B5 [0xED13C3B5]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public final HLEModuleFunction scePEQ_driver_213DE849Function = new HLEModuleFunction("scePEQ_driver", "scePEQ_driver_213DE849") {
		@Override
		public final void execute(Processor processor) {
			scePEQ_driver_213DE849(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.scePEQ_driver.scePEQ_driver_213DE849Function.execute(processor);";
		}
	};
    
	public final HLEModuleFunction scePEQ_driver_FC45514BFunction = new HLEModuleFunction("scePEQ_driver", "scePEQ_driver_FC45514B") {
		@Override
		public final void execute(Processor processor) {
			scePEQ_driver_FC45514B(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.scePEQ_driver.scePEQ_driver_FC45514BFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction scePEQ_driver_F7EA0632Function = new HLEModuleFunction("scePEQ_driver", "scePEQ_driver_F7EA0632") {
		@Override
		public final void execute(Processor processor) {
			scePEQ_driver_F7EA0632(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.scePEQ_driver.scePEQ_driver_F7EA0632Function.execute(processor);";
		}
	};
    
	public final HLEModuleFunction scePEQ_driver_ED13C3B5Function = new HLEModuleFunction("scePEQ_driver", "scePEQ_driver_ED13C3B5") {
		@Override
		public final void execute(Processor processor) {
			scePEQ_driver_ED13C3B5(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.scePEQ_driver.scePEQ_driver_ED13C3B5Function.execute(processor);";
		}
	};
    
};
