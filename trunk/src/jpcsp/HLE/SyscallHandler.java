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
package jpcsp.HLE;

import jpcsp.Emulator;
import jpcsp.Allegrex.CpuState;
import jpcsp.HLE.modules.HLEModuleManager;
import jpcsp.util.DurationStatistics;

public class SyscallHandler {
	public static DurationStatistics durationStatistics = new DurationStatistics("Syscall");
    public static boolean ignoreUnmappedImports = false;

	public static void reset() {
		durationStatistics.reset();
	}

    public static boolean isEnableIgnoreUnmappedImports(){
        return ignoreUnmappedImports;
    }

    public static void setEnableIgnoreUnmappedImports(boolean enable){
        ignoreUnmappedImports = enable;
        if (enable) {
            Modules.log.info("Ignore Unmapped Imports enabled");
        }
    }

    public static void syscall(int code) {
        durationStatistics.start();

        // Try and handle as an HLE module export
        boolean handled = HLEModuleManager.getInstance().handleSyscall(code);
        if (!handled) {
            CpuState cpu = Emulator.getProcessor().cpu;
            if (code == 0xfffff) { // special code for unmapped imports
    	        if(isEnableIgnoreUnmappedImports()) {
    	            Modules.log.warn(String.format("IGNORING: Unmapped import @ 0x%08X - %08x %08x %08x", cpu.pc, cpu.gpr[4], cpu.gpr[5], cpu.gpr[6]));
    	        }
    	        else {
    		        Modules.log.error(String.format("Unmapped import @ 0x%08X - %08x %08x %08x", cpu.pc, cpu.gpr[4], cpu.gpr[5], cpu.gpr[6]));
    		        Emulator.PauseEmu();
    	        }
    	    } else {
	            String name = "";
	            for (SyscallIgnore c : SyscallIgnore.values()) {
	                if (c.getSyscall() == code) {
	                	name = c.toString();
	                	break;
	                }
	            }
	            Modules.log.warn(String.format("Unsupported syscall %X %s %08X %08X %08X", code, name, cpu.gpr[4], cpu.gpr[5], cpu.gpr[6]));
    	    }

            cpu.gpr[2] = 0;
        }

        durationStatistics.end();
    }
}