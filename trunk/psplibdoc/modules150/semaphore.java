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

public class semaphore implements HLEModule {
	@Override
	public String getName() { return "semaphore"; }
	
	@Override
	public void installModule(HLEModuleManager mm, int version) {
		if (version >= 150) {
			
			mm.addFunction(sceUtilsBufferCopyFunction, 0x00EEC06A);
			
			mm.addFunction(sceUtilsBufferCopyByPollingFunction, 0x8EEB7BF2);
			
			mm.addFunction(sceUtilsBufferCopyWithRangeFunction, 0x4C537C72);
			
			mm.addFunction(sceUtilsBufferCopyByPollingWithRangeFunction, 0x77E97079);
			
		}
	}
	
	@Override
	public void uninstallModule(HLEModuleManager mm, int version) {
		if (version >= 150) {
			
			mm.removeFunction(sceUtilsBufferCopyFunction);
			
			mm.removeFunction(sceUtilsBufferCopyByPollingFunction);
			
			mm.removeFunction(sceUtilsBufferCopyWithRangeFunction);
			
			mm.removeFunction(sceUtilsBufferCopyByPollingWithRangeFunction);
			
		}
	}
	
	
	public void sceUtilsBufferCopy(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceUtilsBufferCopy [0x00EEC06A]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceUtilsBufferCopyByPolling(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceUtilsBufferCopyByPolling [0x8EEB7BF2]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceUtilsBufferCopyWithRange(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceUtilsBufferCopyWithRange [0x4C537C72]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public void sceUtilsBufferCopyByPollingWithRange(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;		
		/* put your own code here instead */
		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  int a2 = cpu.gpr[6];  int a3 = cpu.gpr[7];  int t0 = cpu.gpr[8];  int t1 = cpu.gpr[9];  int t2 = cpu.gpr[10];  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  float f14 = cpu.fpr[14];  float f15 = cpu.fpr[15];  float f16 = cpu.fpr[16];  float f17 = cpu.fpr[17];  float f18 = cpu.fpr[18]; float f19 = cpu.fpr[19];
		System.out.println("Unimplemented NID function sceUtilsBufferCopyByPollingWithRange [0x77E97079]");
		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32);
		// cpu.fpr[0] = result;
	}
    
	public final HLEModuleFunction sceUtilsBufferCopyFunction = new HLEModuleFunction("semaphore", "sceUtilsBufferCopy") {
		@Override
		public final void execute(Processor processor) {
			sceUtilsBufferCopy(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.semaphore.sceUtilsBufferCopyFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceUtilsBufferCopyByPollingFunction = new HLEModuleFunction("semaphore", "sceUtilsBufferCopyByPolling") {
		@Override
		public final void execute(Processor processor) {
			sceUtilsBufferCopyByPolling(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.semaphore.sceUtilsBufferCopyByPollingFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceUtilsBufferCopyWithRangeFunction = new HLEModuleFunction("semaphore", "sceUtilsBufferCopyWithRange") {
		@Override
		public final void execute(Processor processor) {
			sceUtilsBufferCopyWithRange(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.semaphore.sceUtilsBufferCopyWithRangeFunction.execute(processor);";
		}
	};
    
	public final HLEModuleFunction sceUtilsBufferCopyByPollingWithRangeFunction = new HLEModuleFunction("semaphore", "sceUtilsBufferCopyByPollingWithRange") {
		@Override
		public final void execute(Processor processor) {
			sceUtilsBufferCopyByPollingWithRange(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.modules150.semaphore.sceUtilsBufferCopyByPollingWithRangeFunction.execute(processor);";
		}
	};
    
};
