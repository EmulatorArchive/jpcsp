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

public class sceNetAdhocctl_lib implements HLEModule {
	@Override
	public String getName() { return "sceNetAdhocctl_lib"; }
	
	@Override
	public void installModule(HLEModuleManager mm, int version) {
		if (version >= 150) {
		
			mm.addFunction(sceNetAdhocctl_lib_EFA6AEAFFunction, 0xEFA6AEAF);
			mm.addFunction(sceNetThreadExitDeleteEventFlagFunction, 0x1593C05D);
			mm.addFunction(sceNetAdhocctl_lib_F8BABD85Function, 0xF8BABD85);
			mm.addFunction(LinkDiscoverSkipFunction, 0x1C679240);
			mm.addFunction(sceNetAdhocctl_lib_CDEA7ACBFunction, 0xCDEA7ACB);
			
		}
	}
	
	@Override
	public void uninstallModule(HLEModuleManager mm, int version) {
		if (version >= 150) {
		
			mm.removeFunction(sceNetAdhocctl_lib_EFA6AEAFFunction);
			mm.removeFunction(sceNetThreadExitDeleteEventFlagFunction);
			mm.removeFunction(sceNetAdhocctl_lib_F8BABD85Function);
			mm.removeFunction(LinkDiscoverSkipFunction);
			mm.removeFunction(sceNetAdhocctl_lib_CDEA7ACBFunction);
			
		}
	}
	
	
	public void sceNetAdhocctl_lib_EFA6AEAF(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;
		
		/* put your own code here instead */

		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

		System.out.println("Unimplemented NID function sceNetAdhocctl_lib_EFA6AEAF [0xEFA6AEAF]");

		cpu.gpr[2] = 0xDEADC0DE;

		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32); cpu.fpr[0] = result;
	}
    
	public void sceNetThreadExitDeleteEventFlag(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;
		
		/* put your own code here instead */

		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

		System.out.println("Unimplemented NID function sceNetThreadExitDeleteEventFlag [0x1593C05D]");

		cpu.gpr[2] = 0xDEADC0DE;

		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32); cpu.fpr[0] = result;
	}
    
	public void sceNetAdhocctl_lib_F8BABD85(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;
		
		/* put your own code here instead */

		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

		System.out.println("Unimplemented NID function sceNetAdhocctl_lib_F8BABD85 [0xF8BABD85]");

		cpu.gpr[2] = 0xDEADC0DE;

		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32); cpu.fpr[0] = result;
	}
    
	public void LinkDiscoverSkip(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;
		
		/* put your own code here instead */

		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

		System.out.println("Unimplemented NID function LinkDiscoverSkip [0x1C679240]");

		cpu.gpr[2] = 0xDEADC0DE;

		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32); cpu.fpr[0] = result;
	}
    
	public void sceNetAdhocctl_lib_CDEA7ACB(Processor processor) {
		// CpuState cpu = processor.cpu; // New-Style Processor
		Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;
		
		/* put your own code here instead */

		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

		System.out.println("Unimplemented NID function sceNetAdhocctl_lib_CDEA7ACB [0xCDEA7ACB]");

		cpu.gpr[2] = 0xDEADC0DE;

		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32); cpu.fpr[0] = result;
	}
    
	public final HLEModuleFunction sceNetAdhocctl_lib_EFA6AEAFFunction = new HLEModuleFunction("sceNetAdhocctl_lib", "sceNetAdhocctl_lib_EFA6AEAF") {
		@Override
		public final void execute(Processor processor) {
			sceNetAdhocctl_lib_EFA6AEAF(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceNetAdhocctl_libModule.sceNetAdhocctl_lib_EFA6AEAF(processor);";
		}
	};
    
	public final HLEModuleFunction sceNetThreadExitDeleteEventFlagFunction = new HLEModuleFunction("sceNetAdhocctl_lib", "sceNetThreadExitDeleteEventFlag") {
		@Override
		public final void execute(Processor processor) {
			sceNetThreadExitDeleteEventFlag(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceNetAdhocctl_libModule.sceNetThreadExitDeleteEventFlag(processor);";
		}
	};
    
	public final HLEModuleFunction sceNetAdhocctl_lib_F8BABD85Function = new HLEModuleFunction("sceNetAdhocctl_lib", "sceNetAdhocctl_lib_F8BABD85") {
		@Override
		public final void execute(Processor processor) {
			sceNetAdhocctl_lib_F8BABD85(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceNetAdhocctl_libModule.sceNetAdhocctl_lib_F8BABD85(processor);";
		}
	};
    
	public final HLEModuleFunction LinkDiscoverSkipFunction = new HLEModuleFunction("sceNetAdhocctl_lib", "LinkDiscoverSkip") {
		@Override
		public final void execute(Processor processor) {
			LinkDiscoverSkip(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceNetAdhocctl_libModule.LinkDiscoverSkip(processor);";
		}
	};
    
	public final HLEModuleFunction sceNetAdhocctl_lib_CDEA7ACBFunction = new HLEModuleFunction("sceNetAdhocctl_lib", "sceNetAdhocctl_lib_CDEA7ACB") {
		@Override
		public final void execute(Processor processor) {
			sceNetAdhocctl_lib_CDEA7ACB(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceNetAdhocctl_libModule.sceNetAdhocctl_lib_CDEA7ACB(processor);";
		}
	};
    
};
