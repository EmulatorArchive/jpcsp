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
import static jpcsp.HLE.kernel.types.SceKernelErrors.ERROR_KERNEL_NO_MEMORY;
import static jpcsp.HLE.kernel.types.SceKernelErrors.ERROR_KERNEL_WAIT_CANCELLED;
import static jpcsp.HLE.kernel.types.SceKernelErrors.ERROR_KERNEL_WAIT_DELETE;
import static jpcsp.HLE.kernel.types.SceKernelErrors.ERROR_KERNEL_WAIT_STATUS_RELEASED;
import static jpcsp.HLE.kernel.types.SceKernelErrors.ERROR_KERNEL_WAIT_TIMEOUT;
import static jpcsp.HLE.kernel.types.SceKernelThreadInfo.PSP_THREAD_READY;
import static jpcsp.HLE.kernel.types.SceKernelThreadInfo.PSP_WAIT_MBX;

import java.util.HashMap;
import java.util.Iterator;

import jpcsp.Emulator;
import jpcsp.Memory;
import jpcsp.Processor;
import jpcsp.Allegrex.CpuState;
import jpcsp.HLE.Modules;
import jpcsp.HLE.kernel.types.IWaitStateChecker;
import jpcsp.HLE.kernel.types.SceKernelMbxInfo;
import jpcsp.HLE.kernel.types.SceKernelThreadInfo;
import jpcsp.HLE.kernel.types.ThreadWaitInfo;
import jpcsp.HLE.modules.ThreadManForUser;
import jpcsp.util.Utilities;

import org.apache.log4j.Logger;

public class MbxManager {

    protected static Logger log = Modules.getLogger("ThreadManForUser");

    private HashMap<Integer, SceKernelMbxInfo> mbxMap;
    private MbxWaitStateChecker mbxWaitStateChecker;

    private final static int PSP_MBX_ATTR_FIFO = 0;
    private final static int PSP_MBX_ATTR_PRIORITY = 0x100;
    private final static int PSP_MBX_ATTR_MSG_FIFO = 0;           // Add new messages by FIFO.
    private final static int PSP_MBX_ATTR_MSG_PRIORITY = 0x400;   // Add new messages by MsgPacket priority.

    public void reset() {
        mbxMap = new HashMap<Integer, SceKernelMbxInfo>();
        mbxWaitStateChecker = new MbxWaitStateChecker();
    }

    private boolean removeWaitingThread(SceKernelThreadInfo thread) {
        SceKernelMbxInfo info = mbxMap.get(thread.wait.Mbx_id);
        if (info != null) {
            info.numWaitThreads--;
            if (info.numWaitThreads < 0) {
                log.warn("Removing waiting thread " + Integer.toHexString(thread.uid) + ", Mbx " + Integer.toHexString(info.uid) + " numWaitThreads underflowed");
                info.numWaitThreads = 0;
            }
            return true;
        }
        return false;
    }

    public void onThreadWaitTimeout(SceKernelThreadInfo thread) {
        if (removeWaitingThread(thread)) {
            thread.cpuContext.gpr[2] = ERROR_KERNEL_WAIT_TIMEOUT;
        } else {
            log.warn("Mbx deleted while we were waiting for it! (timeout expired)");
            thread.cpuContext.gpr[2] = ERROR_KERNEL_WAIT_DELETE;
        }
    }

    public void onThreadWaitReleased(SceKernelThreadInfo thread) {
        // Untrack
        if (removeWaitingThread(thread)) {
            // Return ERROR_WAIT_STATUS_RELEASED
            thread.cpuContext.gpr[2] = ERROR_KERNEL_WAIT_STATUS_RELEASED;
        } else {
            log.warn("EventFlag deleted while we were waiting for it!");
            // Return WAIT_DELETE
            thread.cpuContext.gpr[2] = ERROR_KERNEL_WAIT_DELETE;
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
            if (thread.isWaitingForType(PSP_WAIT_MBX) &&
                    thread.wait.Mbx_id == mbxid) {
                thread.cpuContext.gpr[2] = result;
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

    private void onMbxModified(SceKernelMbxInfo info) {
        ThreadManForUser threadMan = Modules.ThreadManForUserModule;
        boolean reschedule = false;

        if ((info.attr & PSP_MBX_ATTR_PRIORITY) == PSP_MBX_ATTR_FIFO) {
            for (Iterator<SceKernelThreadInfo> it = threadMan.iterator(); it.hasNext();) {
                SceKernelThreadInfo thread = it.next();
                if (thread.isWaitingForType(PSP_WAIT_MBX) &&
                        thread.wait.Mbx_id == info.uid &&
                        info.hasMessage()) {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("onMbxModified waking thread %s", thread.toString()));
                    }
                    Memory mem = Memory.getInstance();
                    int msgAddr = info.removeMsg(mem);
                    mem.write32(thread.wait.Mbx_resultAddr, msgAddr);
                    info.numWaitThreads--;
                    thread.cpuContext.gpr[2] = 0;
                    threadMan.hleChangeThreadState(thread, PSP_THREAD_READY);
                    reschedule = true;
                }
            }
        } else if ((info.attr & PSP_MBX_ATTR_PRIORITY) == PSP_MBX_ATTR_PRIORITY) {
            for (Iterator<SceKernelThreadInfo> it = threadMan.iteratorByPriority(); it.hasNext();) {
                SceKernelThreadInfo thread = it.next();
                if (thread.isWaitingForType(PSP_WAIT_MBX) &&
                        thread.wait.Mbx_id == info.uid &&
                        info.hasMessage()) {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("onMbxModified waking thread %s", thread.toString()));
                    }
                    Memory mem = Memory.getInstance();
                    int msgAddr = info.removeMsg(mem);
                    mem.write32(thread.wait.Mbx_resultAddr, msgAddr);
                    info.numWaitThreads--;
                    thread.cpuContext.gpr[2] = 0;
                    threadMan.hleChangeThreadState(thread, PSP_THREAD_READY);
                    reschedule = true;
                }
            }
        }
        // Reschedule only if threads waked up.
        if (reschedule) {
            threadMan.hleRescheduleCurrentThread();
        }
    }

    public void sceKernelCreateMbx(int name_addr, int attr, int opt_addr) {
        CpuState cpu = Emulator.getProcessor().cpu;
        Memory mem = Memory.getInstance();

        String name = Utilities.readStringZ(name_addr);
        if (log.isDebugEnabled()) {
            log.debug("sceKernelCreateMbx(name=" + name + ",attr=0x" + Integer.toHexString(attr) + ",opt=0x" + Integer.toHexString(opt_addr) + ")");
        }

        if (Memory.isAddressGood(opt_addr)) {
            int optsize = mem.read32(opt_addr);
            log.warn("sceKernelCreateMbx option at 0x" + Integer.toHexString(opt_addr) + " (size=" + optsize + ")");
        }

        SceKernelMbxInfo info = new SceKernelMbxInfo(name, attr);
        if (info != null) {
            if (log.isDebugEnabled()) {
                log.debug("sceKernelCreateMbx '" + name + "' assigned uid " + Integer.toHexString(info.uid));
            }
            mbxMap.put(info.uid, info);
            cpu.gpr[2] = info.uid;
        } else {
            cpu.gpr[2] = ERROR_KERNEL_NO_MEMORY;
        }
    }

    public void sceKernelDeleteMbx(int uid) {
        CpuState cpu = Emulator.getProcessor().cpu;

        if (log.isDebugEnabled()) {
            log.debug("sceKernelDeleteMbx(uid=0x" + Integer.toHexString(uid) + ")");
        }

        SceKernelMbxInfo info = mbxMap.remove(uid);
        if (info == null) {
            log.warn("sceKernelDeleteMbx unknown uid=0x" + Integer.toHexString(uid));
            cpu.gpr[2] = ERROR_KERNEL_NOT_FOUND_MESSAGE_BOX;
        } else {
            cpu.gpr[2] = 0;
            onMbxDeleted(uid);
        }
    }

    public void sceKernelSendMbx(int uid, int msg_addr) {
        CpuState cpu = Emulator.getProcessor().cpu;
        Memory mem = Memory.getInstance();

        if (log.isDebugEnabled()) {
            log.debug("sceKernelSendMbx(uid=0x" + Integer.toHexString(uid) + ",msg=0x" + Integer.toHexString(msg_addr) + ")");
        }

        SceKernelMbxInfo info = mbxMap.get(uid);
        if (info == null) {
            log.warn("sceKernelSendMbx unknown uid=0x" + Integer.toHexString(uid));
            cpu.gpr[2] = ERROR_KERNEL_NOT_FOUND_MESSAGE_BOX;
        } else {
            if ((info.attr & PSP_MBX_ATTR_MSG_PRIORITY) == PSP_MBX_ATTR_MSG_FIFO) {
                info.addMsg(mem, msg_addr);
            } else if ((info.attr & PSP_MBX_ATTR_MSG_PRIORITY) == PSP_MBX_ATTR_MSG_PRIORITY) {
                info.addMsgByPriority(mem, msg_addr);
            }
            cpu.gpr[2] = 0;
            onMbxModified(info);
        }
    }

    private void hleKernelReceiveMbx(int uid, int addr_msg_addr, int timeout_addr,
            boolean doCallbacks, boolean poll) {
        CpuState cpu = Emulator.getProcessor().cpu;
        Memory mem = Processor.memory;

        if (log.isDebugEnabled()) {
            String waitType = "";
            if (poll) {
                waitType = "poll";
            } else if (timeout_addr == 0) {
                waitType = "forever";
            } else {
                waitType = mem.read32(timeout_addr) + " ms";
            }
            if (doCallbacks) {
                waitType += " + CB";
            }
            log.debug("hleKernelReceiveMbx(uid=0x" + Integer.toHexString(uid) + ", msg_pointer=0x" + Integer.toHexString(addr_msg_addr) + ", timeout=0x" + Integer.toHexString(timeout_addr) + ")" + " " + waitType);
        }

        SceKernelMbxInfo info = mbxMap.get(uid);
        if (info == null) {
            log.warn("hleKernelReceiveMbx unknown uid=0x" + Integer.toHexString(uid));
            cpu.gpr[2] = ERROR_KERNEL_NOT_FOUND_MESSAGE_BOX;
        } else {
            ThreadManForUser threadMan = Modules.ThreadManForUserModule;
            if (!info.hasMessage()) {
                if (!poll) {
                    if (log.isDebugEnabled()) {
                        log.debug("hleKernelReceiveMbx - '" + info.name + "' (waiting)");
                    }
                    info.numWaitThreads++;
                    SceKernelThreadInfo currentThread = threadMan.getCurrentThread();
                    currentThread.wait.Mbx_id = uid;
                    currentThread.wait.Mbx_resultAddr = addr_msg_addr;
                    threadMan.hleKernelThreadEnterWaitState(PSP_WAIT_MBX, uid, mbxWaitStateChecker, timeout_addr, doCallbacks);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("hleKernelReceiveMbx has no messages.");
                    }
                    cpu.gpr[2] = ERROR_KERNEL_MESSAGEBOX_NO_MESSAGE;
                }
            } else {
                // Success, do not reschedule the current thread.
                if (log.isDebugEnabled()) {
                    log.debug("hleKernelReceiveMbx - '" + info.name + "' fast check succeeded");
                }
                int msgAddr = info.removeMsg(mem);
                mem.write32(addr_msg_addr, msgAddr);
                cpu.gpr[2] = 0;
            }
        }
    }

    public void sceKernelReceiveMbx(int uid, int addr_msg_addr, int timeout_addr) {
        hleKernelReceiveMbx(uid, addr_msg_addr, timeout_addr, false, false);
    }

    public void sceKernelReceiveMbxCB(int uid, int addr_msg_addr, int timeout_addr) {
        hleKernelReceiveMbx(uid, addr_msg_addr, timeout_addr, true, false);
    }

    public void sceKernelPollMbx(int uid, int addr_msg_addr) {
        hleKernelReceiveMbx(uid, addr_msg_addr, 0, false, true);
    }

    public void sceKernelCancelReceiveMbx(int uid, int pnum_addr) {
        CpuState cpu = Emulator.getProcessor().cpu;
        Memory mem = Memory.getInstance();

        if (log.isDebugEnabled()) {
            log.debug("sceKernelCancelReceiveMbx(uid=0x" + Integer.toHexString(uid) + ")");
        }

        SceKernelMbxInfo info = mbxMap.get(uid);
        if (info == null) {
            log.warn("sceKernelCancelReceiveMbx unknown uid=0x" + Integer.toHexString(uid));
            cpu.gpr[2] = ERROR_KERNEL_NOT_FOUND_MESSAGE_BOX;
        } else {
            if (Memory.isAddressGood(pnum_addr)) {
                mem.write32(pnum_addr, info.numWaitThreads);
            }
            cpu.gpr[2] = 0;
            onMbxCancelled(uid);
        }
    }

    public void sceKernelReferMbxStatus(int uid, int info_addr) {
        CpuState cpu = Emulator.getProcessor().cpu;
        Memory mem = Memory.getInstance();

        if (log.isDebugEnabled()) {
            log.debug("sceKernelReferMbxStatus(uid=0x" + Integer.toHexString(uid) + ",info=0x" + Integer.toHexString(info_addr) + ")");
        }

        SceKernelMbxInfo info = mbxMap.get(uid);
        if (info == null) {
            log.warn("sceKernelReferMbxStatus unknown uid=0x" + Integer.toHexString(uid));
            cpu.gpr[2] = ERROR_KERNEL_NOT_FOUND_MESSAGE_BOX;
        } else {
            info.write(mem, info_addr);
            cpu.gpr[2] = 0;
        }
    }

    private class MbxWaitStateChecker implements IWaitStateChecker {

        @Override
        public boolean continueWaitState(SceKernelThreadInfo thread, ThreadWaitInfo wait) {
            // Check if the thread has to continue its wait state or if the mbx
            // has received a new message during the callback execution.
            SceKernelMbxInfo info = mbxMap.get(wait.Mbx_id);
            if (info == null) {
                thread.cpuContext.gpr[2] = ERROR_KERNEL_NOT_FOUND_MESSAGE_BOX;
                return false;
            }

            // Check the mbx for a new message.
            if (info.hasMessage()) {
                Memory mem = Memory.getInstance();
                int msgAddr = info.removeMsg(mem);
                mem.write32(wait.Mbx_resultAddr, msgAddr);
                info.numWaitThreads--;
                thread.cpuContext.gpr[2] = 0;
                return false;
            }

            return true;
        }
    }
    public static final MbxManager singleton = new MbxManager();

    private MbxManager() {
    }

}