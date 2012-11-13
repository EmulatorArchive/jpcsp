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
package jpcsp.HLE.modules271;

import jpcsp.HLE.HLEFunction;
import jpcsp.HLE.HLELogging;
import jpcsp.HLE.kernel.Managers;

@HLELogging
public class ThreadManForUser extends jpcsp.HLE.modules150.ThreadManForUser {
    @HLEFunction(nid = 0x0DDCD2C9, version = 271, checkInsideInterrupt = true)
    public void sceKernelTryLockMutex(int uid, int count) {
        Managers.mutex.sceKernelTryLockMutex(uid, count);
    }

    @HLEFunction(nid = 0x5BF4DD27, version = 271, checkInsideInterrupt = true, checkDispatchThreadEnabled = true)
    public void sceKernelLockMutexCB(int uid, int count, int timeout_addr) {
        Managers.mutex.sceKernelLockMutexCB(uid, count, timeout_addr);
    }

    @HLEFunction(nid = 0x6B30100F, version = 271, checkInsideInterrupt = true)
    public void sceKernelUnlockMutex(int uid, int count) {
        Managers.mutex.sceKernelUnlockMutex(uid, count);
    }

    @HLEFunction(nid = 0x87D9223C, version = 271)
    public void sceKernelCancelMutex(int uid, int newcount, int numWaitThreadAddr) {
        Managers.mutex.sceKernelCancelMutex(uid, newcount, numWaitThreadAddr);
    }

    @HLEFunction(nid = 0xA9C2CB9A, version = 271)
    public void sceKernelReferMutexStatus(int uid, int addr) {
        Managers.mutex.sceKernelReferMutexStatus(uid, addr);
    }

    @HLEFunction(nid = 0xB011B11F, version = 271, checkInsideInterrupt = true, checkDispatchThreadEnabled = true)
    public void sceKernelLockMutex(int uid, int count, int timeout_addr) {
        Managers.mutex.sceKernelLockMutex(uid, count, timeout_addr);
    }

    @HLEFunction(nid = 0xB7D098C6, version = 271, checkInsideInterrupt = true)
    public void sceKernelCreateMutex(int name_addr, int attr, int count, int option_addr) {
        Managers.mutex.sceKernelCreateMutex(name_addr, attr, count, option_addr);
    }

    @HLEFunction(nid = 0xF8170FBE, version = 271, checkInsideInterrupt = true)
    public void sceKernelDeleteMutex(int uid) {
        Managers.mutex.sceKernelDeleteMutex(uid);
    }
}