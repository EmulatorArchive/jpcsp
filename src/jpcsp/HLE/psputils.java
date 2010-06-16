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

import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;

import jpcsp.Allegrex.compiler.RuntimeContext;
import jpcsp.Clock;
import jpcsp.Emulator;
import jpcsp.Memory;
import jpcsp.State;
import jpcsp.HLE.kernel.managers.SystemTimeManager;

/*
 * TODO list:
 * 1. Improve sceKernelLibcGettimeofday() result.
 *  -> Info:
 *       struct timeval {
 *           time_t tv_sec; // seconds since Jan. 1, 1970
 *           suseconds_t tv_usec; // and microseconds
 *       };
 *
 *     struct timezone {
 *          int tz_minuteswest; // of Greenwich
 *           int tz_dsttime; // type of dst correction to apply
 *      };
 */

public class psputils {
    private static psputils instance;
    private HashMap<Integer, SceKernelUtilsMt19937Context> Mt19937List;

    public static psputils getInstance() {
        if (instance == null) {
            instance = new psputils();
        }
        return instance;
    }

    private psputils() {
    }

    /** call this when resetting the emulator */
    public void Initialise() {
        Mt19937List = new HashMap<Integer, SceKernelUtilsMt19937Context>();
    }

    /** Get the time in seconds since the epoc (1st Jan 1970).  */
    public void sceKernelLibcTime(int time_t_addr) {
        Memory mem = Memory.getInstance();
        int seconds = (int)(Calendar.getInstance().getTimeInMillis() / 1000);
        if (mem.isAddressGood(time_t_addr)) {
            mem.write32(time_t_addr, seconds);
        }
        Emulator.getProcessor().cpu.gpr[2] = seconds;
    }

    /** returns the number of clocks since the "process" started.
     *  This is equivalent to the "System Time".
     */
    public void sceKernelLibcClock() {
        Emulator.getProcessor().cpu.gpr[2] = (int) SystemTimeManager.getSystemTime();
    }

    public void sceKernelLibcGettimeofday(int tp, int tzp) {
        Memory mem = Memory.getInstance();

        if (mem.isAddressGood(tp)) {
        	Clock.TimeNanos currentTimeNano = Emulator.getClock().currentTimeNanos();
            int tv_sec = currentTimeNano.seconds;
            int tv_usec = currentTimeNano.millis * 1000 + currentTimeNano.micros;
            mem.write32(tp, tv_sec);
            mem.write32(tp + 4, tv_usec);
        }

        if (mem.isAddressGood(tzp)) {
            int tz_minuteswest = 0;
            int tz_dsttime = 0;
            mem.write32(tzp, tz_minuteswest);
            mem.write32(tzp + 4, tz_dsttime);
        }

        Emulator.getProcessor().cpu.gpr[2] = 0;
    }

    public void sceKernelDcacheWritebackAll() {
        Modules.log.trace("UNIMPLEMENTED:sceKernelDcacheWritebackAll");
    }

    public void sceKernelDcacheWritebackInvalidateAll() {
        Modules.log.trace("UNIMPLEMENTED:sceKernelDcacheWritebackInvalidateAll");
    }

    public void sceKernelDcacheWritebackRange(int p_addr, int size) {
        Modules.log.trace("UNIMPLEMENTED:sceKernelDcacheWritebackRange");
    }

    public void sceKernelDcacheWritebackInvalidateRange(int p_addr, int size) {
        Modules.log.trace("UNIMPLEMENTED:sceKernelDcacheWritebackInvalidateRange");
    }

    public void sceKernelDcacheInvalidateRange(int p_addr, int size) {
        Modules.log.trace("UNIMPLEMENTED:sceKernelDcacheInvalidateRange");
    }

    public void sceKernelIcacheInvalidateRange(int p_addr, int size) {
        Modules.log.trace("UNIMPLEMENTED:sceKernelIcacheInvalidateRange");
    }

    public void sceKernelIcacheInvalidateAll() {
        // Some games attempt to change compiled code at runtime
        // by calling this function.
        // Use the RuntimeContext to regenerate a compiling context
        // and restart from there.
    	// This method only works for compiled code being called by
    	//    JR   $rs
    	// or
    	//    JALR $rs, $rd
    	// but not for compiled code being called by
    	//    JAL xxxx
        RuntimeContext.invalidateAll();
        Modules.log.info("sceKernelIcacheInvalidateAll");
    }

    public void sceKernelUtilsMt19937Init(int ctx_addr, int seed) {
        // We'll use the address of the ctx as a key
        Mt19937List.remove(ctx_addr); // Remove records of any already existing context at a0
        Mt19937List.put(ctx_addr, new SceKernelUtilsMt19937Context(seed));

        // We'll overwrite all the context memory, 628 bytes
        Memory.getInstance().memset(ctx_addr, (byte) 0xCD, 628);

        Emulator.getProcessor().cpu.gpr[2] = 0;
    }

    public void sceKernelUtilsMt19937UInt(int ctx_addr) {
        SceKernelUtilsMt19937Context ctx = Mt19937List.get(ctx_addr);
        if (ctx != null) {
            Emulator.getProcessor().cpu.gpr[2] = ctx.r.nextInt();
        } else {
            Modules.log.warn("sceKernelUtilsMt19937UInt uninitialised context " + Integer.toHexString(ctx_addr));
            Emulator.getProcessor().cpu.gpr[2] = 0;
        }
    }

    private static class SceKernelUtilsMt19937Context {
        private Random r;

        public SceKernelUtilsMt19937Context(int seed) {
            r = new Random(seed);
        }
    }

    public void sceKernelGetGPI() {
        if (State.debugger != null) {
            int gpi = State.debugger.GetGPI();
            if (Modules.log.isDebugEnabled()) {
            	Modules.log.debug("sceKernelGetGPI 0x" + String.format("%02X", gpi));
            }
            Emulator.getProcessor().cpu.gpr[2] = gpi;
        } else {
        	if (Modules.log.isDebugEnabled()) {
        		Modules.log.debug("sceKernelGetGPI debugger not enabled");
        	}
            Emulator.getProcessor().cpu.gpr[2] = 0;
        }
    }

    public void sceKernelSetGPO(int value) {
        if (State.debugger != null) {
            State.debugger.SetGPO(value);
            if (Modules.log.isDebugEnabled()) {
            	Modules.log.debug("sceKernelSetGPO 0x" + String.format("%02X", value));
            }
        } else {
        	if (Modules.log.isDebugEnabled()) {
        		Modules.log.debug("sceKernelSetGPO debugger not enabled");
        	}
        }
        Emulator.getProcessor().cpu.gpr[2] = 0;
    }
}