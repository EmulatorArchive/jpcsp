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
package jpcsp.HLE.kernel.managers;

import static jpcsp.HLE.kernel.types.SceKernelErrors.ERROR_KERNEL_ILLEGAL_COUNT;
import static jpcsp.HLE.kernel.types.SceKernelErrors.ERROR_KERNEL_NOT_FOUND_SEMAPHORE;
import static jpcsp.HLE.kernel.types.SceKernelErrors.ERROR_KERNEL_SEMA_OVERFLOW;
import static jpcsp.HLE.kernel.types.SceKernelErrors.ERROR_KERNEL_SEMA_ZERO;
import static jpcsp.HLE.kernel.types.SceKernelErrors.ERROR_KERNEL_WAIT_CANCELLED;
import static jpcsp.HLE.kernel.types.SceKernelErrors.ERROR_KERNEL_WAIT_CAN_NOT_WAIT;
import static jpcsp.HLE.kernel.types.SceKernelErrors.ERROR_KERNEL_WAIT_DELETE;
import static jpcsp.HLE.kernel.types.SceKernelErrors.ERROR_KERNEL_WAIT_STATUS_RELEASED;
import static jpcsp.HLE.kernel.types.SceKernelErrors.ERROR_KERNEL_WAIT_TIMEOUT;
import static jpcsp.HLE.kernel.types.SceKernelThreadInfo.PSP_THREAD_READY;
import static jpcsp.HLE.kernel.types.SceKernelThreadInfo.PSP_WAIT_SEMA;

import java.util.HashMap;
import java.util.Iterator;

import jpcsp.HLE.Modules;
import jpcsp.HLE.SceKernelErrorException;
import jpcsp.HLE.TPointer;
import jpcsp.HLE.TPointer32;
import jpcsp.HLE.kernel.types.IWaitStateChecker;
import jpcsp.HLE.kernel.types.SceKernelSemaInfo;
import jpcsp.HLE.kernel.types.SceKernelThreadInfo;
import jpcsp.HLE.kernel.types.ThreadWaitInfo;
import jpcsp.HLE.modules.ThreadManForUser;

import org.apache.log4j.Logger;

public class SemaManager {
    protected static Logger log = Modules.getLogger("ThreadManForUser");

    private HashMap<Integer, SceKernelSemaInfo> semaMap;
    private SemaWaitStateChecker semaWaitStateChecker;

    public final static int PSP_SEMA_ATTR_FIFO = 0;           // Signal waiting threads with a FIFO iterator.
    public final static int PSP_SEMA_ATTR_PRIORITY = 0x100;   // Signal waiting threads with a priority based iterator.

    public void reset() {
        semaMap = new HashMap<Integer, SceKernelSemaInfo>();
        semaWaitStateChecker = new SemaWaitStateChecker();
    }

    /** Don't call this unless thread.wait.waitingOnSemaphore == true
     * @return true if the thread was waiting on a valid sema */
    private boolean removeWaitingThread(SceKernelThreadInfo thread) {
        SceKernelSemaInfo sema = semaMap.get(thread.wait.Semaphore_id);
        if (sema == null) {
        	return false;
        }

        sema.threadWaitingList.removeWaitingThread(thread);

    	return true;
    }

    /** Don't call this unless thread.wait.waitingOnSemaphore == true */
    public void onThreadWaitTimeout(SceKernelThreadInfo thread) {
        // Untrack
        if (removeWaitingThread(thread)) {
            // Return WAIT_TIMEOUT
            thread.cpuContext._v0 = ERROR_KERNEL_WAIT_TIMEOUT;
        } else {
            log.warn("Sema deleted while we were waiting for it! (timeout expired)");
            // Return WAIT_DELETE
            thread.cpuContext._v0 = ERROR_KERNEL_WAIT_DELETE;
        }
    }

    public void onThreadWaitReleased(SceKernelThreadInfo thread) {
        // Untrack
        if (removeWaitingThread(thread)) {
            // Return ERROR_WAIT_STATUS_RELEASED
            thread.cpuContext._v0 = ERROR_KERNEL_WAIT_STATUS_RELEASED;
        } else {
            log.warn("EventFlag deleted while we were waiting for it!");
            // Return WAIT_DELETE
            thread.cpuContext._v0 = ERROR_KERNEL_WAIT_DELETE;
        }
    }

    public void onThreadDeleted(SceKernelThreadInfo thread) {
        if (thread.isWaitingForType(PSP_WAIT_SEMA)) {
            // decrement numWaitThreads
            removeWaitingThread(thread);
        }
    }

    private void onSemaphoreDeletedCancelled(int semaid, int result) {
        ThreadManForUser threadMan = Modules.ThreadManForUserModule;
        boolean reschedule = false;

        for (Iterator<SceKernelThreadInfo> it = threadMan.iterator(); it.hasNext();) {
            SceKernelThreadInfo thread = it.next();
            if (thread.isWaitingForType(PSP_WAIT_SEMA) &&
                    thread.wait.Semaphore_id == semaid) {
                thread.cpuContext._v0 = result;
                threadMan.hleChangeThreadState(thread, PSP_THREAD_READY);
                reschedule = true;
            }
        }
        // Reschedule only if threads waked up.
        if (reschedule) {
            threadMan.hleRescheduleCurrentThread();
        }
    }

    private void onSemaphoreDeleted(int semaid) {
        onSemaphoreDeletedCancelled(semaid, ERROR_KERNEL_WAIT_DELETE);
    }

    private void onSemaphoreCancelled(int semaid) {
        onSemaphoreDeletedCancelled(semaid, ERROR_KERNEL_WAIT_CANCELLED);
    }

    private void onSemaphoreModified(SceKernelSemaInfo sema) {
        ThreadManForUser threadMan = Modules.ThreadManForUserModule;
        boolean reschedule = false;

        SceKernelThreadInfo checkedThread = null;
        while (sema.currentCount > 0) {
            SceKernelThreadInfo thread = sema.threadWaitingList.getNextWaitingThread(checkedThread);
            if (thread == null) {
            	break;
            }
            if (tryWaitSemaphore(sema, thread.wait.Semaphore_signal)) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("onSemaphoreModified waking thread %s", thread));
                }
                sema.threadWaitingList.removeWaitingThread(thread);
                thread.cpuContext._v0 = 0;
                threadMan.hleChangeThreadState(thread, PSP_THREAD_READY);
                reschedule = true;
            } else {
            	checkedThread = thread;
            }
        }

        // Reschedule only if threads waked up.
        if (reschedule) {
            threadMan.hleRescheduleCurrentThread();
        }
    }

    private boolean tryWaitSemaphore(SceKernelSemaInfo sema, int signal) {
        boolean success = false;
        if (sema.currentCount >= signal) {
            sema.currentCount -= signal;
            success = true;
        }
        return success;
    }

    public int checkSemaID(int semaid) {
        SceUidManager.checkUidPurpose(semaid, "ThreadMan-sema", true);
        if (!semaMap.containsKey(semaid)) {
        	if (semaid == 0) {
            	// Some applications systematically try to signal a semaid=0.
            	// Do not spam WARNings for this case.
        		log.debug(String.format("checkSemaID - unknown uid 0x%X", semaid));
        	} else {
        		log.warn(String.format("checkSemaID - unknown uid 0x%X", semaid));
        	}
            throw new SceKernelErrorException(ERROR_KERNEL_NOT_FOUND_SEMAPHORE);
        }

        return semaid;
    }

    public SceKernelSemaInfo hleKernelCreateSema(String name, int attr, int initVal, int maxVal, TPointer option) {
        if (option.isNotNull()) {
            // The first int does not seem to be the size of the struct, found values:
            // SSX On Tour: 0, 0x08B0F9E4, 0x0892E664, 0x08AF7257 (some values are used in more than one semaphore)
            int optionSize = option.getValue32();
            log.warn(String.format("sceKernelCreateSema option at %s, size=%d", option, optionSize));
        }

        SceKernelSemaInfo sema = new SceKernelSemaInfo(name, attr, initVal, maxVal);
        semaMap.put(sema.uid, sema);

        return sema;
    }

    public int hleKernelWaitSema(SceKernelSemaInfo sema, int signal, TPointer32 timeoutAddr, boolean doCallbacks) {
        if (!tryWaitSemaphore(sema, signal)) {
            // Failed, but it's ok, just wait a little
            if (log.isDebugEnabled()) {
                log.debug(String.format("hleKernelWaitSema %s fast check failed", sema));
            }
            ThreadManForUser threadMan = Modules.ThreadManForUserModule;
            SceKernelThreadInfo currentThread = threadMan.getCurrentThread();
            sema.threadWaitingList.addWaitingThread(currentThread);
            // Wait on a specific semaphore
            currentThread.wait.Semaphore_id = sema.uid;
            currentThread.wait.Semaphore_signal = signal;
            threadMan.hleKernelThreadEnterWaitState(PSP_WAIT_SEMA, sema.uid, semaWaitStateChecker, timeoutAddr.getAddress(), doCallbacks);
        } else {
            // Success, do not reschedule the current thread.
            if (log.isDebugEnabled()) {
                log.debug(String.format("hleKernelWaitSema %s fast check succeeded", sema));
            }
        }

        return 0;
    }

    private int hleKernelWaitSema(int semaid, int signal, TPointer32 timeoutAddr, boolean doCallbacks) {
        if (signal <= 0) {
            log.warn("hleKernelWaitSema - bad signal " + signal);
            return ERROR_KERNEL_ILLEGAL_COUNT;
        }
        if (!Modules.ThreadManForUserModule.isDispatchThreadEnabled()) {
            if (log.isDebugEnabled()) {
                log.debug("hleKernelWaitSema called when dispatch thread disabled");
            }
        	return ERROR_KERNEL_WAIT_CAN_NOT_WAIT;
        }
        SceKernelSemaInfo sema = semaMap.get(semaid);
        if (signal > sema.maxCount) {
        	return ERROR_KERNEL_ILLEGAL_COUNT;
        }

        return hleKernelWaitSema(sema, signal, timeoutAddr, doCallbacks);
    }

    public int hleKernelPollSema(SceKernelSemaInfo sema, int signal) {
        if (sema.currentCount - signal < 0) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("sceKernelPollSema id=0x%X('%s'), signal=%d", sema.uid, sema.name, signal));
            }
            return ERROR_KERNEL_SEMA_ZERO;
        }

        if (log.isDebugEnabled()) {
            log.debug(String.format("sceKernelPollSema id=0x%X('%s'), signal=%d", sema.uid, sema.name, signal));
        }
        sema.currentCount -= signal;

        return 0;
    }

    public int hleKernelSignalSema(SceKernelSemaInfo sema, int signal) {
    	// Check that currentCount will not exceed the maxCount
    	// after releasing all the threads waiting on this sema.
    	int newCount = sema.currentCount + signal;
    	if (newCount > sema.maxCount) {
	        for (Iterator<SceKernelThreadInfo> it = Modules.ThreadManForUserModule.iterator(); it.hasNext();) {
	            SceKernelThreadInfo thread = it.next();
	            if (thread.isWaitingForType(PSP_WAIT_SEMA) && thread.wait.Semaphore_id == sema.uid) {
	            	newCount -= thread.wait.Semaphore_signal;
	            }
	        }
	        if (newCount > sema.maxCount) {
	        	return ERROR_KERNEL_SEMA_OVERFLOW;
	        }
    	}

    	sema.currentCount += signal;

        onSemaphoreModified(sema);

        // Sanity check...
        if (sema.currentCount > sema.maxCount) {
        	// This situation should never happen, otherwise something went wrong
        	// in the overflow check above.
        	log.error(String.format("hleKernelSignalSema currentCount %d exceeding maxCount %d", sema.currentCount, sema.maxCount));
        }

        return 0;
    }

    public int sceKernelCreateSema(String name, int attr, int initVal, int maxVal, TPointer option) {
        SceKernelSemaInfo sema = hleKernelCreateSema(name, attr, initVal, maxVal, option);

        if (log.isDebugEnabled()) {
            log.debug(String.format("sceKernelCreateSema %s", sema));
        }

        return sema.uid;
    }

    public int sceKernelDeleteSema(int semaid) {
        semaMap.remove(semaid);
        onSemaphoreDeleted(semaid);

        return 0;
    }

    public int sceKernelWaitSema(int semaid, int signal, TPointer32 timeoutAddr) {
        return hleKernelWaitSema(semaid, signal, timeoutAddr, false);
    }

    public int sceKernelWaitSemaCB(int semaid, int signal, TPointer32 timeoutAddr) {
        return hleKernelWaitSema(semaid, signal, timeoutAddr, true);
    }

    public int sceKernelSignalSema(int semaid, int signal) {
        SceKernelSemaInfo sema = semaMap.get(semaid);
        return hleKernelSignalSema(sema, signal);
    }

    /** This is attempt to signal the sema and always return immediately */
    public int sceKernelPollSema(int semaid, int signal) {
        if (signal <= 0) {
            log.warn(String.format("sceKernelPollSema id=0x%X, signal=%d: bad signal", semaid, signal));
            return ERROR_KERNEL_ILLEGAL_COUNT;
        }

        SceKernelSemaInfo sema = semaMap.get(semaid);
        return hleKernelPollSema(sema, signal);
    }

    public int sceKernelCancelSema(int semaid, int newcount, TPointer32 numWaitThreadAddr) {
        SceKernelSemaInfo sema = semaMap.get(semaid);

        if (newcount > sema.maxCount) {
            return ERROR_KERNEL_ILLEGAL_COUNT;
        }

        // Write previous numWaitThreads count.
        numWaitThreadAddr.setValue(sema.getNumWaitThreads());
        sema.threadWaitingList.removeAllWaitingThreads();
        // Reset this semaphore's count based on newcount.
        // Note: If newcount is negative, the count becomes this semaphore's initCount.
        if (newcount < 0) {
            sema.currentCount = sema.initCount;
        } else {
            sema.currentCount = newcount;
        }
        onSemaphoreCancelled(semaid);

        return 0;
    }

    public int sceKernelReferSemaStatus(int semaid, TPointer addr) {
        SceKernelSemaInfo sema = semaMap.get(semaid);
        sema.write(addr);
        if (log.isDebugEnabled()) {
        	log.debug(String.format("sceKernelReferSemaStatus returning %s", sema));
        }

        return 0;
    }

    private class SemaWaitStateChecker implements IWaitStateChecker {
        @Override
        public boolean continueWaitState(SceKernelThreadInfo thread, ThreadWaitInfo wait) {
            // Check if the thread has to continue its wait state or if the sema
            // has been signaled during the callback execution.
            SceKernelSemaInfo sema = semaMap.get(wait.Semaphore_id);
            if (sema == null) {
                thread.cpuContext._v0 = ERROR_KERNEL_NOT_FOUND_SEMAPHORE;
                return false;
            }

            // Check the sema.
            if (tryWaitSemaphore(sema, wait.Semaphore_signal)) {
            	sema.threadWaitingList.removeWaitingThread(thread);
                thread.cpuContext._v0 = 0;
                return false;
            }

            return true;
        }
    }
    public static final SemaManager singleton = new SemaManager();

    private SemaManager() {
    }
}