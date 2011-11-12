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

package jpcsp.HLE.modules380;

import jpcsp.HLE.HLEFunction;
import jpcsp.Processor;
import jpcsp.HLE.kernel.Managers;

public class Kernel_Library extends jpcsp.HLE.modules150.Kernel_Library {

	@HLEFunction(nid = 0x15B6446B, version = 380, checkInsideInterrupt = true)
	public int sceKernelUnlockLwMutex(int workAreaAddr, int count) {
		return Managers.lwmutex.sceKernelUnlockLwMutex(workAreaAddr, count);
	}

	@HLEFunction(nid = 0x1FC64E09, version = 380, checkInsideInterrupt = true)
	public int sceKernelLockLwMutexCB(int workAreaAddr, int count, int timeout_addr) {
		return Managers.lwmutex.sceKernelLockLwMutexCB(workAreaAddr, count, timeout_addr);
	}

	@HLEFunction(nid = 0xBEA46419, version = 380, checkInsideInterrupt = true)
	public int sceKernelLockLwMutex(int workAreaAddr, int count, int timeout_addr) {
		return Managers.lwmutex.sceKernelLockLwMutex(workAreaAddr, count, timeout_addr);
	}

	@HLEFunction(nid = 0xC1734599, version = 380)
	public int sceKernelReferLwMutexStatus(int workAreaAddr, int addr) {
		return Managers.lwmutex.sceKernelReferLwMutexStatus(workAreaAddr, addr);
	}

	@HLEFunction(nid = 0xDC692EE3, version = 380, checkInsideInterrupt = true)
	public int sceKernelTryLockLwMutex(int workAreaAddr, int count) {
		return Managers.lwmutex.sceKernelTryLockLwMutex(workAreaAddr, count);
	}

    @HLEFunction(nid = 0x37431849, version = 380, checkInsideInterrupt = true)
    public void sceKernelTryLockLwMutex_600(int workAreaAddr, int count) {
        if (log.isDebugEnabled()) {
            log.debug("sceKernelTryLockLwMutex_600 redirecting to sceKernelTryLockLwMutex");
        }
		Managers.lwmutex.sceKernelTryLockLwMutex(workAreaAddr, count);
	}

    @HLEFunction(nid = 0x1839852A, version = 380)
    public int sceKernelMemcpy(int dst, int src, int length) {
		Processor.memory.memcpy(dst, src, length);
		return dst;
	}
}