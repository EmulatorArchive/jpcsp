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

import static jpcsp.HLE.kernel.types.SceKernelErrors.ERROR_KERNEL_MESSAGEBOX_NO_MESSAGE;
import static jpcsp.HLE.kernel.types.SceKernelErrors.ERROR_KERNEL_NOT_FOUND_MESSAGE_BOX;
import static jpcsp.HLE.kernel.types.SceKernelErrors.ERROR_KERNEL_WAIT_CANCELLED;
import static jpcsp.HLE.kernel.types.SceKernelErrors.ERROR_KERNEL_WAIT_DELETE;
import static jpcsp.HLE.kernel.types.SceKernelErrors.ERROR_KERNEL_WAIT_STATUS_RELEASED;
import static jpcsp.HLE.kernel.types.SceKernelErrors.ERROR_KERNEL_WAIT_TIMEOUT;
import static jpcsp.HLE.kernel.types.SceKernelThreadInfo.PSP_THREAD_READY;
import static jpcsp.HLE.kernel.types.SceKernelThreadInfo.PSP_WAIT_MBX;

import java.util.HashMap;
import java.util.Iterator;

import jpcsp.Memory;
import jpcsp.HLE.Modules;
import jpcsp.HLE.SceKernelErrorException;
import jpcsp.HLE.TPointer;
import jpcsp.HLE.TPointer32;
import jpcsp.HLE.kernel.types.IWaitStateChecker;
import jpcsp.HLE.kernel.types.SceKernelMbxInfo;
import jpcsp.HLE.kernel.types.SceKernelThreadInfo;
import jpcsp.HLE.kernel.types.ThreadWaitInfo;
import jpcsp.HLE.modules.ThreadManForUser;

import org.apache.log4j.Logger;

public class MbxManager {
    protected static Logger log = Modules.getLogger("ThreadManForUser");

    private HashMap<Integer, SceKernelMbxInfo> mbxMap;
    private MbxWaitStateChecker mbxWaitStateChecker;

    public final static int PSP_MBX_ATTR_FIFO = 0;
    public final static int PSP_MBX_ATTR_PRIORITY = 0x100;
    private final static int PSP_MBX_ATTR_MSG_FIFO = 0;           // Add new messages by FIFO.
    private final static int PSP_MBX_ATTR_MSG_PRIORITY = 0x400;   // Add new messages by MsgPacket priority.

    public void reset() {
        mbxMap = new HashMap<Integer, SceKernelMbxInfo>();
        mbxWaitStateChecker = new MbxWaitStateChecker();
    }

    private boolean removeWaitingThread(SceKernelThreadInfo thread) {
        SceKernelMbxInfo info = mbxMap.get(thread.wait.Mbx_id);
        if (info == null) {
        	return false;
        }

        info.threadWaitingList.removeWaitingThread(thread);

        return true;
    }

    public void onThreadWaitTimeout(SceKernelThreadInfo thread) {
        if (removeWaitingThread(thread)) {
            thread.cpuContext._v0 = ERROR_KERNEL_WAIT_TIMEOUT;
        } else {
            log.warn("Mbx deleted while we were waiting for it! (timeout expired)");
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
    	if (thread.isWaitingForType(PSP_WAIT_MBX)) {
    		removeWaitingThread(thread);
    	}
    }

    private void onMbxDeletedCancelled(int mbxid, int result) {
        ThreadManForUser threadMan = Modules.ThreadManForUserModule;
        boolean reschedule = false;

        for (Iterator<SceKernelThreadInfo> it = threadMan.iterator(); it.hasNext();) {
            SceKernelThreadInfo thread = it.next();
            if (thread.isWaitingFor(PSP_WAIT_MBX, mbxid)) {
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

    private void onMbxDeleted(int mbxid) {
        onMbxDeletedCancelled(mbxid, ERROR_KERNEL_WAIT_DELETE);
    }

    private void onMbxCancelled(int mbxid) {
        onMbxDeletedCancelled(mbxid, ERROR_KERNEL_WAIT_CANCELLED);
    }

    public int checkMbxID(int uid) {
        if (!mbxMap.containsKey(uid)) {
        	log.warn(String.format("checkMbxID unknown uid=0x%X", uid));
        	throw new SceKernelErrorException(ERROR_KERNEL_NOT_FOUND_MESSAGE_BOX);
        }

        return uid;
    }

    public int sceKernelCreateMbx(String name, int attr, TPointer option) {
        if (option.isNotNull()) {
            int optionSize = option.getValue32();
            log.warn(String.format("sceKernelCreateMbx option at %s: size=%d", option, optionSize));
        }

        SceKernelMbxInfo info = new SceKernelMbxInfo(name, attr);
        if (log.isDebugEnabled()) {
            log.debug(String.format("sceKernelCreateMbx returning %s", info));
        }
        mbxMap.put(info.uid, info);

        return info.uid;
    }

    public int sceKernelDeleteMbx(int uid) {
        mbxMap.remove(uid);
        onMbxDeleted(uid);

        return 0;
    }

    public int sceKernelSendMbx(int uid, TPointer msgAddr) {
        SceKernelMbxInfo info = mbxMap.get(uid);

        boolean msgConsumed = false;

        // If the Mbx is empty, check if some thread is already waiting.
        // If a thread is already waiting, do not update the msg "nextMsgPacketAddr" field.
        if (!info.hasMessage()) {
            SceKernelThreadInfo thread = info.threadWaitingList.getFirstWaitingThread();
            if (thread != null) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("sceKernelSendMbx waking thread %s", thread));
                }
                thread.wait.Mbx_resultAddr.setValue(msgAddr.getAddress());
                info.threadWaitingList.removeWaitingThread(thread);
                thread.cpuContext._v0 = 0;

                ThreadManForUser threadMan = Modules.ThreadManForUserModule;
                threadMan.hleChangeThreadState(thread, PSP_THREAD_READY);
                threadMan.hleRescheduleCurrentThread();

                msgConsumed = true;
            }
        }

        // Add the message if it has not yet been consumed by a waiting thread
        if (!msgConsumed) {
	        if ((info.attr & PSP_MBX_ATTR_MSG_PRIORITY) == PSP_MBX_ATTR_MSG_FIFO) {
	            info.addMsg(msgAddr.getMemory(), msgAddr.getAddress());
	        } else if ((info.attr & PSP_MBX_ATTR_MSG_PRIORITY) == PSP_MBX_ATTR_MSG_PRIORITY) {
	            info.addMsgByPriority(msgAddr.getMemory(), msgAddr.getAddress());
	        }
        }

        return 0;
    }

    private int hleKernelReceiveMbx(int uid, TPointer32 addrMsgAddr, TPointer32 timeoutAddr, boolean doCallbacks, boolean poll) {
        SceKernelMbxInfo info = mbxMap.get(uid);
        ThreadManForUser threadMan = Modules.ThreadManForUserModule;
        if (!info.hasMessage()) {
            if (!poll) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("hleKernelReceiveMbx - %s (waiting)", info));
                }
                SceKernelThreadInfo currentThread = threadMan.getCurrentThread();
                info.threadWaitingList.addWaitingThread(currentThread);
                currentThread.wait.Mbx_id = uid;
                currentThread.wait.Mbx_resultAddr = addrMsgAddr;
                threadMan.hleKernelThreadEnterWaitState(PSP_WAIT_MBX, uid, mbxWaitStateChecker, timeoutAddr.getAddress(), doCallbacks);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("hleKernelReceiveMbx has no messages.");
                }
                return ERROR_KERNEL_MESSAGEBOX_NO_MESSAGE;
            }
        } else {
            // Success, do not reschedule the current thread.
            if (log.isDebugEnabled()) {
                log.debug(String.format("hleKernelReceiveMbx - %s fast check succeeded", info));
            }
            int msgAddr = info.removeMsg(Memory.getInstance());
            addrMsgAddr.setValue(msgAddr);
        }

        return 0;
    }

    public int sceKernelReceiveMbx(int uid, TPointer32 addrMsgAddr, TPointer32 timeoutAddr) {
        return hleKernelReceiveMbx(uid, addrMsgAddr, timeoutAddr, false, false);
    }

    public int sceKernelReceiveMbxCB(int uid, TPointer32 addrMsgAddr, TPointer32 timeoutAddr) {
        return hleKernelReceiveMbx(uid, addrMsgAddr, timeoutAddr, true, false);
    }

    public int sceKernelPollMbx(int uid, TPointer32 addrMsgAddr) {
        return hleKernelReceiveMbx(uid, addrMsgAddr, TPointer32.NULL, false, true);
    }

    public int sceKernelCancelReceiveMbx(int uid, TPointer32 pnumAddr) {
        SceKernelMbxInfo info = mbxMap.get(uid);
        pnumAddr.setValue(info.getNumWaitThreads());
        info.threadWaitingList.removeAllWaitingThreads();
        onMbxCancelled(uid);

        return 0;
    }

    public int sceKernelReferMbxStatus(int uid, TPointer infoAddr) {
        SceKernelMbxInfo info = mbxMap.get(uid);
        info.write(infoAddr);

        return 0;
    }

    private class MbxWaitStateChecker implements IWaitStateChecker {
        @Override
        public boolean continueWaitState(SceKernelThreadInfo thread, ThreadWaitInfo wait) {
            // Check if the thread has to continue its wait state or if the mbx
            // has received a new message during the callback execution.
            SceKernelMbxInfo info = mbxMap.get(wait.Mbx_id);
            if (info == null) {
                thread.cpuContext._v0 = ERROR_KERNEL_NOT_FOUND_MESSAGE_BOX;
                return false;
            }

            // Check the mbx for a new message.
            if (info.hasMessage()) {
                Memory mem = Memory.getInstance();
                int msgAddr = info.removeMsg(mem);
                wait.Mbx_resultAddr.setValue(msgAddr);
                info.threadWaitingList.removeWaitingThread(thread);
                thread.cpuContext._v0 = 0;
                return false;
            }

            return true;
        }
    }
    public static final MbxManager singleton = new MbxManager();

    private MbxManager() {
    }
}