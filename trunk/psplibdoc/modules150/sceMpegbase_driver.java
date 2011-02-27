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

import jpcsp.HLE.Modules;
import jpcsp.HLE.modules.HLEModule;
import jpcsp.HLE.modules.HLEModuleFunction;
import jpcsp.HLE.modules.HLEModuleManager;

import jpcsp.Memory;
import jpcsp.Processor;

import jpcsp.Allegrex.CpuState; // New-Style Processor

public class sceMpegbase_driver implements HLEModule {
	@Override
	public String getName() { return "sceMpegbase_driver"; }
	
	@Override
	public void installModule(HLEModuleManager mm, int version) {
		if (version >= 150) {
		
			mm.addFunction(sceMpegBaseInitFunction, 0x27A2982F);
			mm.addFunction(sceMpegbase_driver_BEA18F91Function, 0xBEA18F91);
			mm.addFunction(sceMpegBaseCscInitFunction, 0x492B5E4B);
			mm.addFunction(sceMpegbase_driver_AC9E717EFunction, 0xAC9E717E);
			mm.addFunction(sceMpegbase_driver_0530BE4EFunction, 0x0530BE4E);
			mm.addFunction(sceMpegBaseCscAvcFunction, 0x91929A21);
			mm.addFunction(sceMpegBaseCscVmeFunction, 0xCE8EB837);
			mm.addFunction(sceMpegBaseCscAvcRangeFunction, 0x304882E1);
			mm.addFunction(sceMpegBaseYCrCbCopyFunction, 0x7AC0321A);
			mm.addFunction(sceMpegBaseYCrCbCopyVmeFunction, 0xBE45C284);
			
		}
	}
	
	@Override
	public void uninstallModule(HLEModuleManager mm, int version) {
		if (version >= 150) {
		
			mm.removeFunction(sceMpegBaseInitFunction);
			mm.removeFunction(sceMpegbase_driver_BEA18F91Function);
			mm.removeFunction(sceMpegBaseCscInitFunction);
			mm.removeFunction(sceMpegbase_driver_AC9E717EFunction);
			mm.removeFunction(sceMpegbase_driver_0530BE4EFunction);
			mm.removeFunction(sceMpegBaseCscAvcFunction);
			mm.removeFunction(sceMpegBaseCscVmeFunction);
			mm.removeFunction(sceMpegBaseCscAvcRangeFunction);
			mm.removeFunction(sceMpegBaseYCrCbCopyFunction);
			mm.removeFunction(sceMpegBaseYCrCbCopyVmeFunction);
			
		}
	}
	
	
	public void sceMpegBaseInit(Processor processor) {
		CpuState cpu = processor.cpu;

		Modules.log.debug("Unimplemented NID function sceMpegBaseInit [0x27A2982F]");

		cpu.gpr[2] = 0xDEADC0DE;
	}
    
	public void sceMpegbase_driver_BEA18F91(Processor processor) {
		CpuState cpu = processor.cpu;

		Modules.log.debug("Unimplemented NID function sceMpegbase_driver_BEA18F91 [0xBEA18F91]");

		cpu.gpr[2] = 0xDEADC0DE;
	}
    
	public void sceMpegBaseCscInit(Processor processor) {
		CpuState cpu = processor.cpu;

		Modules.log.debug("Unimplemented NID function sceMpegBaseCscInit [0x492B5E4B]");

		cpu.gpr[2] = 0xDEADC0DE;
	}
    
	public void sceMpegbase_driver_AC9E717E(Processor processor) {
		CpuState cpu = processor.cpu;

		Modules.log.debug("Unimplemented NID function sceMpegbase_driver_AC9E717E [0xAC9E717E]");

		cpu.gpr[2] = 0xDEADC0DE;
	}
    
	public void sceMpegbase_driver_0530BE4E(Processor processor) {
		CpuState cpu = processor.cpu;

		Modules.log.debug("Unimplemented NID function sceMpegbase_driver_0530BE4E [0x0530BE4E]");

		cpu.gpr[2] = 0xDEADC0DE;
	}
    
	public void sceMpegBaseCscAvc(Processor processor) {
		CpuState cpu = processor.cpu;

		Modules.log.debug("Unimplemented NID function sceMpegBaseCscAvc [0x91929A21]");

		cpu.gpr[2] = 0xDEADC0DE;
	}
    
	public void sceMpegBaseCscVme(Processor processor) {
		CpuState cpu = processor.cpu;

		Modules.log.debug("Unimplemented NID function sceMpegBaseCscVme [0xCE8EB837]");

		cpu.gpr[2] = 0xDEADC0DE;
	}
    
	public void sceMpegBaseCscAvcRange(Processor processor) {
		CpuState cpu = processor.cpu;

		Modules.log.debug("Unimplemented NID function sceMpegBaseCscAvcRange [0x304882E1]");

		cpu.gpr[2] = 0xDEADC0DE;
	}
    
	public void sceMpegBaseYCrCbCopy(Processor processor) {
		CpuState cpu = processor.cpu;

		Modules.log.debug("Unimplemented NID function sceMpegBaseYCrCbCopy [0x7AC0321A]");

		cpu.gpr[2] = 0xDEADC0DE;
	}
    
	public void sceMpegBaseYCrCbCopyVme(Processor processor) {
		CpuState cpu = processor.cpu;

		Modules.log.debug("Unimplemented NID function sceMpegBaseYCrCbCopyVme [0xBE45C284]");

		cpu.gpr[2] = 0xDEADC0DE;
	}
    
	public final HLEModuleFunction sceMpegBaseInitFunction = new HLEModuleFunction("sceMpegbase_driver", "sceMpegBaseInit") {
		@Override
		public final void execute(Processor processor) {
			sceMpegBaseInit(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceMpegbase_driverModule.sceMpegBaseInit(processor);";
		}
	};
    
	public final HLEModuleFunction sceMpegbase_driver_BEA18F91Function = new HLEModuleFunction("sceMpegbase_driver", "sceMpegbase_driver_BEA18F91") {
		@Override
		public final void execute(Processor processor) {
			sceMpegbase_driver_BEA18F91(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceMpegbase_driverModule.sceMpegbase_driver_BEA18F91(processor);";
		}
	};
    
	public final HLEModuleFunction sceMpegBaseCscInitFunction = new HLEModuleFunction("sceMpegbase_driver", "sceMpegBaseCscInit") {
		@Override
		public final void execute(Processor processor) {
			sceMpegBaseCscInit(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceMpegbase_driverModule.sceMpegBaseCscInit(processor);";
		}
	};
    
	public final HLEModuleFunction sceMpegbase_driver_AC9E717EFunction = new HLEModuleFunction("sceMpegbase_driver", "sceMpegbase_driver_AC9E717E") {
		@Override
		public final void execute(Processor processor) {
			sceMpegbase_driver_AC9E717E(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceMpegbase_driverModule.sceMpegbase_driver_AC9E717E(processor);";
		}
	};
    
	public final HLEModuleFunction sceMpegbase_driver_0530BE4EFunction = new HLEModuleFunction("sceMpegbase_driver", "sceMpegbase_driver_0530BE4E") {
		@Override
		public final void execute(Processor processor) {
			sceMpegbase_driver_0530BE4E(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceMpegbase_driverModule.sceMpegbase_driver_0530BE4E(processor);";
		}
	};
    
	public final HLEModuleFunction sceMpegBaseCscAvcFunction = new HLEModuleFunction("sceMpegbase_driver", "sceMpegBaseCscAvc") {
		@Override
		public final void execute(Processor processor) {
			sceMpegBaseCscAvc(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceMpegbase_driverModule.sceMpegBaseCscAvc(processor);";
		}
	};
    
	public final HLEModuleFunction sceMpegBaseCscVmeFunction = new HLEModuleFunction("sceMpegbase_driver", "sceMpegBaseCscVme") {
		@Override
		public final void execute(Processor processor) {
			sceMpegBaseCscVme(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceMpegbase_driverModule.sceMpegBaseCscVme(processor);";
		}
	};
    
	public final HLEModuleFunction sceMpegBaseCscAvcRangeFunction = new HLEModuleFunction("sceMpegbase_driver", "sceMpegBaseCscAvcRange") {
		@Override
		public final void execute(Processor processor) {
			sceMpegBaseCscAvcRange(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceMpegbase_driverModule.sceMpegBaseCscAvcRange(processor);";
		}
	};
    
	public final HLEModuleFunction sceMpegBaseYCrCbCopyFunction = new HLEModuleFunction("sceMpegbase_driver", "sceMpegBaseYCrCbCopy") {
		@Override
		public final void execute(Processor processor) {
			sceMpegBaseYCrCbCopy(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceMpegbase_driverModule.sceMpegBaseYCrCbCopy(processor);";
		}
	};
    
	public final HLEModuleFunction sceMpegBaseYCrCbCopyVmeFunction = new HLEModuleFunction("sceMpegbase_driver", "sceMpegBaseYCrCbCopyVme") {
		@Override
		public final void execute(Processor processor) {
			sceMpegBaseYCrCbCopyVme(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceMpegbase_driverModule.sceMpegBaseYCrCbCopyVme(processor);";
		}
	};
    
};