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

import jpcsp.Processor;
import jpcsp.Allegrex.CpuState;
import jpcsp.HLE.Modules;
import jpcsp.HLE.kernel.Managers;
import jpcsp.HLE.modules.HLEModule;
import jpcsp.HLE.modules.HLEModuleFunction;
import jpcsp.HLE.modules.HLEModuleManager;

public class InterruptManager implements HLEModule {
	@Override
	public String getName() { return "InterruptManager"; }
	
	@Override
	public void installModule(HLEModuleManager mm, int version) {
		if (version >= 150) {
		
			mm.addFunction(sceKernelRegisterSubIntrHandlerFunction, 0xCA04A2B9);
			mm.addFunction(sceKernelReleaseSubIntrHandlerFunction, 0xD61E6961);
			mm.addFunction(sceKernelEnableSubIntrFunction, 0xFB8E22EC);
			mm.addFunction(sceKernelDisableSubIntrFunction, 0x8A389411);
			mm.addFunction(sceKernelSuspendSubIntrFunction, 0x5CB5A78B);
			mm.addFunction(sceKernelResumeSubIntrFunction, 0x7860E0DC);
			mm.addFunction(sceKernelIsSubInterruptOccurredFunction, 0xFC4374B8);
			mm.addFunction(QueryIntrHandlerInfoFunction, 0xD2E8363F);
			mm.addFunction(sceKernelRegisterUserSpaceIntrStackFunction, 0xEEE43F47);
			
		}
	}
	
	@Override
	public void uninstallModule(HLEModuleManager mm, int version) {
		if (version >= 150) {
		
			mm.removeFunction(sceKernelRegisterSubIntrHandlerFunction);
			mm.removeFunction(sceKernelReleaseSubIntrHandlerFunction);
			mm.removeFunction(sceKernelEnableSubIntrFunction);
			mm.removeFunction(sceKernelDisableSubIntrFunction);
			mm.removeFunction(sceKernelSuspendSubIntrFunction);
			mm.removeFunction(sceKernelResumeSubIntrFunction);
			mm.removeFunction(sceKernelIsSubInterruptOccurredFunction);
			mm.removeFunction(QueryIntrHandlerInfoFunction);
			mm.removeFunction(sceKernelRegisterUserSpaceIntrStackFunction);
			
		}
	}
	
	
	public void sceKernelRegisterSubIntrHandler(Processor processor) {
		int[] gpr = processor.cpu.gpr;
		gpr[2] = Managers.intr.sceKernelRegisterSubIntrHandler(gpr[4], gpr[5], gpr[6], gpr[7]);
	}
    
	public void sceKernelReleaseSubIntrHandler(Processor processor) {
		int[] gpr = processor.cpu.gpr;
		gpr[2] = Managers.intr.sceKernelReleaseSubIntrHandler(gpr[4], gpr[5]);
	}
    
	public void sceKernelEnableSubIntr(Processor processor) {
		int[] gpr = processor.cpu.gpr;
		gpr[2] = Managers.intr.sceKernelEnableSubIntr(gpr[4], gpr[5]);
	}
    
	public void sceKernelDisableSubIntr(Processor processor) {
		int[] gpr = processor.cpu.gpr;
		gpr[2] = Managers.intr.sceKernelDisableSubIntr(gpr[4], gpr[5]);
	}
    
	public void sceKernelSuspendSubIntr(Processor processor) {
		CpuState cpu = processor.cpu;

		Modules.log.debug("Unimplemented NID function sceKernelSuspendSubIntr [0x5CB5A78B]");

		cpu.gpr[2] = 0xDEADC0DE;
	}
    
	public void sceKernelResumeSubIntr(Processor processor) {
		CpuState cpu = processor.cpu;

		Modules.log.debug("Unimplemented NID function sceKernelResumeSubIntr [0x7860E0DC]");

		cpu.gpr[2] = 0xDEADC0DE;
	}
    
	public void sceKernelIsSubInterruptOccurred(Processor processor) {
		CpuState cpu = processor.cpu;

		Modules.log.debug("Unimplemented NID function sceKernelIsSubInterruptOccurred [0xFC4374B8]");

		cpu.gpr[2] = 0xDEADC0DE;
	}
    
	public void QueryIntrHandlerInfo(Processor processor) {
		CpuState cpu = processor.cpu;

		Modules.log.debug("Unimplemented NID function QueryIntrHandlerInfo [0xD2E8363F]");

		cpu.gpr[2] = 0xDEADC0DE;
	}
    
	public void sceKernelRegisterUserSpaceIntrStack(Processor processor) {
		CpuState cpu = processor.cpu;

		Modules.log.debug("Unimplemented NID function sceKernelRegisterUserSpaceIntrStack [0xEEE43F47]");

		cpu.gpr[2] = 0xDEADC0DE;
	}
    
	public final HLEModuleFunction sceKernelRegisterSubIntrHandlerFunction = new HLEModuleFunction("InterruptManager", "sceKernelRegisterSubIntrHandler") {
		@Override
		public final void execute(Processor processor) {
			sceKernelRegisterSubIntrHandler(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.InterruptManagerModule.sceKernelRegisterSubIntrHandler(processor);";
		}
	};
    
	public final HLEModuleFunction sceKernelReleaseSubIntrHandlerFunction = new HLEModuleFunction("InterruptManager", "sceKernelReleaseSubIntrHandler") {
		@Override
		public final void execute(Processor processor) {
			sceKernelReleaseSubIntrHandler(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.InterruptManagerModule.sceKernelReleaseSubIntrHandler(processor);";
		}
	};
    
	public final HLEModuleFunction sceKernelEnableSubIntrFunction = new HLEModuleFunction("InterruptManager", "sceKernelEnableSubIntr") {
		@Override
		public final void execute(Processor processor) {
			sceKernelEnableSubIntr(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.InterruptManagerModule.sceKernelEnableSubIntr(processor);";
		}
	};
    
	public final HLEModuleFunction sceKernelDisableSubIntrFunction = new HLEModuleFunction("InterruptManager", "sceKernelDisableSubIntr") {
		@Override
		public final void execute(Processor processor) {
			sceKernelDisableSubIntr(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.InterruptManagerModule.sceKernelDisableSubIntr(processor);";
		}
	};
    
	public final HLEModuleFunction sceKernelSuspendSubIntrFunction = new HLEModuleFunction("InterruptManager", "sceKernelSuspendSubIntr") {
		@Override
		public final void execute(Processor processor) {
			sceKernelSuspendSubIntr(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.InterruptManagerModule.sceKernelSuspendSubIntr(processor);";
		}
	};
    
	public final HLEModuleFunction sceKernelResumeSubIntrFunction = new HLEModuleFunction("InterruptManager", "sceKernelResumeSubIntr") {
		@Override
		public final void execute(Processor processor) {
			sceKernelResumeSubIntr(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.InterruptManagerModule.sceKernelResumeSubIntr(processor);";
		}
	};
    
	public final HLEModuleFunction sceKernelIsSubInterruptOccurredFunction = new HLEModuleFunction("InterruptManager", "sceKernelIsSubInterruptOccurred") {
		@Override
		public final void execute(Processor processor) {
			sceKernelIsSubInterruptOccurred(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.InterruptManagerModule.sceKernelIsSubInterruptOccurred(processor);";
		}
	};
    
	public final HLEModuleFunction QueryIntrHandlerInfoFunction = new HLEModuleFunction("InterruptManager", "QueryIntrHandlerInfo") {
		@Override
		public final void execute(Processor processor) {
			QueryIntrHandlerInfo(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.InterruptManagerModule.QueryIntrHandlerInfo(processor);";
		}
	};
    
	public final HLEModuleFunction sceKernelRegisterUserSpaceIntrStackFunction = new HLEModuleFunction("InterruptManager", "sceKernelRegisterUserSpaceIntrStack") {
		@Override
		public final void execute(Processor processor) {
			sceKernelRegisterUserSpaceIntrStack(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.InterruptManagerModule.sceKernelRegisterUserSpaceIntrStack(processor);";
		}
	};
    
};
