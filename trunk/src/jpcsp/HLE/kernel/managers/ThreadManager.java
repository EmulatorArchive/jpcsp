/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jpcsp.HLE.kernel.managers;

import java.util.HashMap;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import jpcsp.Emulator;
import jpcsp.Memory;
import jpcsp.MemoryMap;
import jpcsp.Processor;
import jpcsp.Allegrex.CpuState;
import static jpcsp.util.Utilities.*;

import jpcsp.HLE.Modules;
import jpcsp.HLE.kernel.Managers;
import jpcsp.HLE.kernel.types.*;

import static jpcsp.HLE.kernel.types.SceKernelErrors.*;
import static jpcsp.HLE.kernel.types.SceKernelThreadInfo.*;

import jpcsp.HLE.pspSysMem;

/**
 *
 * @author hli
 */
public class ThreadManager {

    public static HashMap<Integer, SceKernelThreadInfo> threadMap;
    public static HashMap<Integer, Integer> waitThreadEndMap; // <thread to wait on, thread to wakeup>
    public ArrayList<Integer> waitingThreads;
    public SceKernelThreadInfo currentThread;
    public SceKernelThreadInfo idle0, idle1;
    public int continuousIdleCycles; // watch dog timer - number of continuous cycles in any idle thread
    public int syscallFreeCycles; // watch dog timer - number of cycles since last syscall

    // TODO figure out a decent number of cycles to wait
    private final int WDT_THREAD_IDLE_CYCLES = 1000000;
    private final int WDT_THREAD_HOG_CYCLES = 50000000;

    public final static int SCE_KERNEL_TMID_Thread = 1;
    public final static int SCE_KERNEL_TMID_Semaphore = 2;
    public final static int SCE_KERNEL_TMID_EventFlag = 3;
    public final static int SCE_KERNEL_TMID_Mbox = 4;
    public final static int SCE_KERNEL_TMID_Vpl = 5;
    public final static int SCE_KERNEL_TMID_Fpl = 6;
    public final static int SCE_KERNEL_TMID_Mpipe = 7;
    public final static int SCE_KERNEL_TMID_Callback = 8;
    public final static int SCE_KERNEL_TMID_ThreadEventHandler = 9;
    public final static int SCE_KERNEL_TMID_Alarm = 10;
    public final static int SCE_KERNEL_TMID_VTimer = 11;
    public final static int SCE_KERNEL_TMID_SleepThread = 64;
    public final static int SCE_KERNEL_TMID_DelayThread = 65;
    public final static int SCE_KERNEL_TMID_SuspendThread = 66;
    public final static int SCE_KERNEL_TMID_DormantThread = 67;

    public boolean isUidValid(int uid) {
        return threadMap.containsKey(uid);
    }

    /** call this when resetting the emulator
     * @param entry_addr entry from ELF header
     * @param attr from sceModuleInfo ELF section header */
    public void initialize(int entry_addr, int attr, String pspfilename) {

        threadMap = new HashMap<Integer, SceKernelThreadInfo>();
        Managers.callbacks.reset();
        Managers.sempahores.reset();
        Managers.eventsFlags.reset();
        waitThreadEndMap = new HashMap<Integer, Integer>();
        waitingThreads = new ArrayList<Integer>();

        installIdleThreads();

        // Create a thread the program will run inside
        currentThread = new SceKernelThreadInfo("root", entry_addr, 0x20, 0x4000, attr);

        // Set user mode bit if kernel mode bit is not present
        if ((currentThread.attr & THREAD_ATTR_KERNEL) != THREAD_ATTR_KERNEL) {
            currentThread.attr |= THREAD_ATTR_USER;
        }

        // Setup args by copying them onto the stack
        //Modules.log.debug("pspfilename - '" + pspfilename + "'");
        int len = pspfilename.length();
        int alignlen = (len + 1 + 3) & ~3; // string terminator + 4 byte align
        Memory mem = Memory.getInstance();
        for (int i = 0; i < len; i++) {
            mem.write8((currentThread.stack_addr - alignlen) + i, (byte) pspfilename.charAt(i));
        }
        for (int i = len; i < alignlen; i++) {
            mem.write8((currentThread.stack_addr - alignlen) + i, (byte) 0);
        }
        currentThread.cpuContext.gpr[29] -= alignlen; // Adjust sp for size of args
        currentThread.cpuContext.gpr[4] = len + 1; // a0 = len + string terminator
        currentThread.cpuContext.gpr[5] = currentThread.cpuContext.gpr[29]; // a1 = pointer to arg data in stack

        // HACK min stack size is set to 512, let's set sp to stack top - 512.
        // this allows plenty of padding between the sp and the eboot path
        // that we store at the top of the stack area. should help NesterJ and
        // Calender, both use sp+16 at the beginning (expected sp-16).
        currentThread.cpuContext.gpr[29] -= 512 - alignlen;

        currentThread.status = ThreadStatus.THREAD_READY;

        // Switch in the thread
        currentThread.status = ThreadStatus.THREAD_RUNNING;
        currentThread.restoreContext(Emulator.getProcessor());
        syscallFreeCycles = 0;
    }

    private void installIdleThreads() {
        // Generate 2 idle threads which can toggle between each other when there are no ready threads
        int instruction_addiu = // addiu a0, zr, 0
                ((jpcsp.Allegrex.Opcodes.ADDIU & 0x3f) << 26) | ((0 & 0x1f) << 21) | ((4 & 0x1f) << 16);
        int instruction_lui = // lui ra, 0x08000000
                ((jpcsp.Allegrex.Opcodes.LUI & 0x3f) << 26) | ((31 & 0x1f) << 16) | (0x0800 & 0x0000ffff);
        int instruction_jr = // jr ra
                ((jpcsp.Allegrex.Opcodes.SPECIAL & 0x3f) << 26) | (jpcsp.Allegrex.Opcodes.JR & 0x3f) | ((31 & 0x1f) << 21);
        int instruction_syscall = // syscall <code>
                ((jpcsp.Allegrex.Opcodes.SPECIAL & 0x3f) << 26) | (jpcsp.Allegrex.Opcodes.SYSCALL & 0x3f) | ((0x201c & 0x000fffff) << 6);

        // TODO
        //pspSysMem.getInstance().malloc(1, pspSysMem.PSP_SMEM_Addr, 16, MemoryMap.START_RAM);

        Memory.getInstance().write32(MemoryMap.START_RAM + 0, instruction_addiu);
        Memory.getInstance().write32(MemoryMap.START_RAM + 4, instruction_lui);
        Memory.getInstance().write32(MemoryMap.START_RAM + 8, instruction_jr);
        Memory.getInstance().write32(MemoryMap.START_RAM + 12, instruction_syscall);

        idle0 = new SceKernelThreadInfo("idle0", MemoryMap.START_RAM, 0x7f, 0x0, 0x0);
        idle0.status = ThreadStatus.THREAD_READY;

        idle1 = new SceKernelThreadInfo("idle1", MemoryMap.START_RAM, 0x7f, 0x0, 0x0);
        idle1.status = ThreadStatus.THREAD_READY;

        continuousIdleCycles = 0;
    }

    /** to be called from the main emulation loop */
    public void step() {
        CpuState cpu = Emulator.getProcessor().cpu;
        if (currentThread != null) {
            currentThread.runClocks++;

            //Modules.log.debug("pc=" + Emulator.getProcessor().pc + " ra=" + Emulator.getProcessor().gpr[31]);

            // Hook jr ra to 0 (thread function returned)
            if (cpu.pc == 0 && cpu.gpr[31] == 0) {
                // Thread has exited
                Modules.log.debug("Thread exit detected SceUid:" + Integer.toHexString(currentThread.uid)
                    + " name:'" + currentThread.name + "' return:" + Integer.toHexString(cpu.gpr[2]));
                currentThread.exitStatus = cpu.gpr[2]; // v0
                currentThread.status = ThreadStatus.THREAD_STOPPED;
                onThreadStopped(currentThread);
                contextSwitch(nextThread());
            }

            // Watch dog timer
            if (currentThread == idle0 || currentThread == idle1) {
                continuousIdleCycles++;
                if (continuousIdleCycles > WDT_THREAD_IDLE_CYCLES) {
                    Modules.log.info("Watch dog timer - pausing emulator (idle)");
                    Emulator.PauseEmuWithStatus(Emulator.EMU_STATUS_WDT_IDLE);
                }
            } else {
                continuousIdleCycles = 0;
                syscallFreeCycles++;
                if (syscallFreeCycles > WDT_THREAD_HOG_CYCLES) {
                    Modules.log.info("Watch dog timer - pausing emulator (thread hogging)");
                    Emulator.PauseEmuWithStatus(Emulator.EMU_STATUS_WDT_HOG);
                }
            }
        } else {
            // We always need to be in a thread! we shouldn't get here.
            Modules.log.error("No ready threads!");
        }

        Iterator<SceKernelThreadInfo> it = threadMap.values().iterator();
        while (it.hasNext()) {
            SceKernelThreadInfo thread = it.next();

            // Decrement delaysteps on sleeping threads
            if (thread.status == ThreadStatus.THREAD_WAITING) {
                if (thread.delaysteps > 0) {
                    thread.delaysteps--;
                }
                if (thread.delaysteps == 0) {
                    thread.status = ThreadStatus.THREAD_READY;

                    // If this thread was doing sceKernelWaitThreadEnd then remove the wakeup callback
                    if (thread.do_waitThreadEnd) {
                        thread.do_waitThreadEnd = false;
                        thread.cpuContext.gpr[2] = -1; // timeout occured before the thread ended, so fail
                        waitThreadEndMap.remove(thread.waitThreadEndUid);
                    }
                }
            }

            // Cleanup stopped threads marked for deletion
            if (thread.status == ThreadStatus.THREAD_STOPPED) {
                if (thread.do_delete) {
                    // cleanup thread - free the stack
                    if (thread.stack_addr != 0) {
                        pspSysMem.getInstance().free(thread.stack_addr);
                    }
                    // TODO remove from any internal lists? such as sema waiting lists

                    it.remove();

                    thread.release();
                }
            }
        }
    }

    public void clearSyscallFreeCycles() {
        syscallFreeCycles = 0;
    }

    private void contextSwitch(SceKernelThreadInfo newThread) {
        Processor processor = Emulator.getProcessor();

        if (currentThread != null) {
            // Switch out old thread
            if (currentThread.status == ThreadStatus.THREAD_RUNNING) {
                currentThread.status = ThreadStatus.THREAD_READY;
            // save registers
            }
            currentThread.saveContext(processor);

        /*
        Modules.log.debug("saveContext SceUid:" + Integer.toHexString(current_thread.uid)
        + " name:" + current_thread.name
        + " PC:" + Integer.toHexString(current_thread.pcreg)
        + " NPC:" + Integer.toHexString(current_thread.npcreg));
         */
        }

        if (newThread != null) {
            // Switch in new thread
            newThread.status = ThreadStatus.THREAD_RUNNING;
            newThread.wakeupCount++; // check
            // restore registers
            newThread.restoreContext(processor);

        //Modules.log.debug("ThreadMan: switched to thread SceUid:" + Integer.toHexString(newthread.uid) + " name:'" + newthread.name + "'");
            /*
        Modules.log.debug("restoreContext SceUid:" + Integer.toHexString(newthread.uid)
        + " name:" + newthread.name
        + " PC:" + Integer.toHexString(newthread.pcreg)
        + " NPC:" + Integer.toHexString(newthread.npcreg));
         */

        } else {
            // Shouldn't get here now we are using idle threads
            Modules.log.info("No ready threads - pausing emulator");
            Emulator.PauseEmuWithStatus(Emulator.EMU_STATUS_UNKNOWN);
        }

        currentThread = newThread;
        syscallFreeCycles = 0;
    }

    // This function must have the property of never returning current_thread, unless current_thread is already null
    private SceKernelThreadInfo nextThread() {
        Collection<SceKernelThreadInfo> c;
        List<SceKernelThreadInfo> list;
        Iterator<SceKernelThreadInfo> it;
        SceKernelThreadInfo found = null;

        // Find the thread with status PSP_THREAD_READY and the highest priority
        // In this implementation low priority threads can get starved
        c = threadMap.values();
        list = new LinkedList<SceKernelThreadInfo>(c);
        Collections.sort(list, idle0); // We need an instance of ThreadInfo for the comparator, so we use idle0
        it = list.iterator();
        while (it.hasNext()) {
            SceKernelThreadInfo thread = it.next();
            //Modules.log.debug("nextThread pri=" + Integer.toHexString(thread.currentPriority) + " name:" + thread.name + " status:" + thread.status);

            if (thread != currentThread &&
                    thread.status == ThreadStatus.THREAD_READY) {
                found = thread;
                break;
            }
        }

        return found;
    }

    public int getCurrentThreadID() {
        return currentThread.uid;
    }

    public String getThreadName(int uid) {
        SceKernelThreadInfo thread = threadMap.get(uid);
        if (thread == null) {
            return "NOT A THREAD";
        } else {
            return thread.name;
        }
    }

    public void setCurrentThreadWaiting() {
        this.waitingThreads.add(getCurrentThreadID());
        blockCurrentThread();
    }

    public void yieldCurrentThread() {
        contextSwitch(nextThread());
    }

    public void blockCurrentThread() {
        currentThread.status = ThreadStatus.THREAD_SUSPEND;
        contextSwitch(nextThread());
    }

    public void unblockThread(int uid) {
        if (isUidValid(uid)) {
            SceKernelThreadInfo thread = threadMap.get(uid);
            thread.status = ThreadStatus.THREAD_READY;
        }
    }

    private void onThreadStopped(SceKernelThreadInfo stoppedThread) {
        // Wakeup threads that are in sceKernelWaitThreadEnd
        Integer uid = waitThreadEndMap.remove(stoppedThread.uid);
        if (uid != null) {
            // This should be consistent/no error checking required because waitthreadendlist can only be changed privately
            SceKernelThreadInfo waitingThread = threadMap.get(uid);
            waitingThread.status = ThreadStatus.THREAD_READY;
            waitingThread.do_waitThreadEnd = false;
        }
    }

    public int createThread(String name, int entry_addr, int initPriority, int stackSize, int attr, int option_addr, boolean startImmediately, int userDataLength, int userDataAddr, int gp) {
        SceKernelThreadInfo thread = new SceKernelThreadInfo(name, entry_addr, initPriority, stackSize, attr);

        if (thread.uid >= 0) {
            // Copy user data to the new thread's stack, since we are not
            // starting the thread immediately, only marking it as ready,
            // the data needs to be saved somewhere safe.
            int alignlen = (userDataLength + 3) & ~3; // 4 byte align
            Memory mem = Memory.getInstance();
            for (int i = 0; i < userDataLength; i++) {
                mem.write8((thread.stack_addr - alignlen) + i, (byte) mem.read8(userDataAddr + i));
            }
            for (int i = userDataLength; i < alignlen; i++) {
                mem.write8((thread.stack_addr - alignlen) + i, (byte) 0);
            }
            thread.cpuContext.gpr[29] -= alignlen; // Adjust sp for size of user data
            thread.cpuContext.gpr[4] = userDataLength; // a0 = userDataLength
            thread.cpuContext.gpr[5] = thread.cpuContext.gpr[29]; // a1 = pointer to copy of data at data_addr
            thread.cpuContext.gpr[28] = thread.gpReg_addr = gp;

            if (startImmediately) {
                thread.status = ThreadStatus.THREAD_READY;
                contextSwitch(thread);
            }
        }
        return thread.uid;
    }

    public void sceKernelCreateThread(Processor processor) {
        int[] gpr = processor.cpu.gpr;
        Memory mem = Processor.memory;

        int name_addr = gpr[4];
        int entry_addr = gpr[5];
        int initPriority = gpr[6];
        int stackSize = gpr[7];
        int attr = gpr[8];
        int option_addr = gpr[9];

        String name = readStringZ(mem.mainmemory,
                (name_addr & 0x3fffffff) - MemoryMap.START_RAM);

        Modules.log.debug(
                String.format("sceKernelCreateThread name='%s' entry_addr=0x%08x initPriority=%d stackSize=%d attr=0x%08x option_addr=0x%08x",
                name, entry_addr, initPriority, stackSize, attr, option_addr));

        if (option_addr != 0) {
            Modules.log.warn("sceKernelCreateThread: UNSUPPORTED Option Value");
        }

        SceKernelThreadInfo thread = new SceKernelThreadInfo(name, entry_addr, initPriority, stackSize, attr);

        // Inherit kernel mode if user mode bit is not set
        if ((currentThread.attr & THREAD_ATTR_KERNEL) == THREAD_ATTR_KERNEL &&
            (attr & THREAD_ATTR_USER) != THREAD_ATTR_USER) {
            Modules.log.debug("sceKernelCreateThread inheriting kernel mode");
            thread.attr |= THREAD_ATTR_KERNEL;
        }
        // Inherit user mode
        if ((currentThread.attr & THREAD_ATTR_USER) == THREAD_ATTR_USER) {
            if ((thread.attr & THREAD_ATTR_USER) != THREAD_ATTR_USER)
                Modules.log.debug("sceKernelCreateThread inheriting user mode");
            thread.attr |= THREAD_ATTR_USER;
            // Always remove kernel mode bit
            thread.attr &= ~THREAD_ATTR_KERNEL;
        }

        int uid = thread.getUid();

        gpr[2] = uid;

        if (0 < uid) {
            threadMap.put(uid, thread);
        }
    }

    /** terminate thread id */
    public void sceKernelTerminateThread(Processor processor) {
        int[] gpr = processor.cpu.gpr;

        int uid = gpr[4];

        SceKernelThreadInfo thread = threadMap.get(uid);

        if (thread == null) {
            gpr[2] = ERROR_NOT_FOUND_THREAD;
        } else {
            Modules.log.debug("sceKernelTerminateThread id:" + thread.uid + " name:'" + thread.name + "'");

            thread.status = ThreadStatus.THREAD_STOPPED; // PSP_THREAD_STOPPED or PSP_THREAD_KILLED ?

            gpr[2] = 0;

            onThreadStopped(thread);
        }
    }

    /** delete thread id */
    public void sceKernelDeleteThread(Processor processor) {
        int[] gpr = processor.cpu.gpr;

        int uid = gpr[4];

        SceKernelThreadInfo thread = threadMap.get(uid);

        if (thread == null) {
            gpr[2] = ERROR_NOT_FOUND_THREAD;
        } else {
            Modules.log.debug("sceKernelDeleteThread id:" + uid + " name:'" + thread.name + "'");

            // Mark thread for deletion
            thread.do_delete = true;

            gpr[2] = 0;
        }
    }

    public void sceKernelStartThread(Processor processor) {
        int[] gpr = processor.cpu.gpr;
        Memory mem = Processor.memory;

        int uid = gpr[4];
        int len = gpr[5];
        int data_addr = gpr[6];

        SceKernelThreadInfo thread = threadMap.get(uid);

        if (thread == null) {
            gpr[2] = ERROR_NOT_FOUND_THREAD;
        } else {
            Modules.log.debug("sceKernelStartThread id:" + thread.uid + " name:'" + thread.name + "'");

            // Copy user data to the new thread's stack, since we are not
            // starting the thread immediately, only marking it as ready,
            // the data needs to be saved somewhere safe.
            int alignlen = (len + 3) & ~3; // 4 byte align
            for (int i = 0; i < len; i++)
                mem.write8((thread.stack_addr - alignlen) + i, (byte)mem.read8(data_addr + i));
            for (int i = len; i < alignlen; i++)
                mem.write8((thread.stack_addr - alignlen) + i, (byte)0);
            thread.cpuContext.gpr[29] -= alignlen; // Adjust sp for size of user data
            thread.cpuContext.gpr[4] = len; // a0 = len
            thread.cpuContext.gpr[5] = thread.cpuContext.gpr[29]; // a1 = pointer to copy of data at data_addr
            thread.status = ThreadStatus.THREAD_READY;

            gpr[2] = 0;

            // TODO does start thread defer start or really start?
            // threadstatus.pbp on real PSP shows the callback thread prints
            // before the main thread, so it starts immediately.
            contextSwitch(thread);
        }
    }

    /** exit the current thread */
    public void sceKernelExitThread(Processor processor) {
        int[] gpr = processor.cpu.gpr;
        Memory mem = Processor.memory;

        int exitStatus = gpr[4];

        SceKernelThreadInfo thread = currentThread;

        Modules.log.debug("sceKernelExitThread id:" + thread.uid
            + " name:'" + thread.name + "' exitStatus:" + exitStatus);

        thread.status = ThreadStatus.THREAD_STOPPED;
        thread.exitStatus = exitStatus;

        gpr[2] = 0;

        onThreadStopped(thread);

        contextSwitch(nextThread());
    }

    /** exit the current thread, then delete it */
    public void sceKernelExitDeleteThread(Processor processor) {
        int[] gpr = processor.cpu.gpr;
        Memory mem = Processor.memory;

        int exitStatus = gpr[4];

        SceKernelThreadInfo thread = currentThread; // save a reference for post context switch operations

        Modules.log.debug("sceKernelExitDeleteThread id:" + thread.uid
            + " name:'" + thread.name + "' exitStatus:" + exitStatus);

        // Exit
        thread.status = ThreadStatus.THREAD_STOPPED;
        thread.exitStatus = exitStatus;
        gpr[2] = 0;
        onThreadStopped(thread); // TODO maybe not here in Exit and Delete thread function

        // Mark thread for deletion
        thread.do_delete = true;

        contextSwitch(nextThread());
    }

    /** sleep the current thread until a registered callback is triggered */
    public void sceKernelSleepThreadCB(Processor processor) {
        SceKernelThreadInfo thread = currentThread; // save a reference for post context switch operations

        Modules.log.debug("sceKernelSleepThreadCB id:" + thread.uid + " name:'" + thread.name + "'");

        thread.status = ThreadStatus.THREAD_SUSPEND;
        thread.do_callbacks = true;
        processor.cpu.gpr[2] = 0;

        contextSwitch(nextThread());
    }

    /** sleep the current thread */
    public void sceKernelSleepThread(Processor processor) {
        SceKernelThreadInfo thread = currentThread; // save a reference for post context switch operations

        Modules.log.debug("sceKernelSleepThread id:" + thread.uid + " name:'" + thread.name + "'");

        thread.status = ThreadStatus.THREAD_SUSPEND;
        thread.do_callbacks = false;
        processor.cpu.gpr[2] = 0;

        contextSwitch(nextThread());
    }

    /** sleep the current thread for a certain number of microseconds */
    public void sceKernelDelayThread(Processor processor) {
        int[] gpr = processor.cpu.gpr;

        int micros = gpr[4];

        SceKernelThreadInfo thread = currentThread;

        thread.status = ThreadStatus.THREAD_WAITING;
        //thread.delaysteps = micros * 200000000 / 1000000; // TODO delaysteps = micros * steprate
        thread.delaysteps = (micros < WDT_THREAD_IDLE_CYCLES) ? micros : WDT_THREAD_IDLE_CYCLES - 1; // test version
        thread.do_callbacks = false;

        gpr[2] = 0;

        contextSwitch(nextThread());
    }

    /** sleep the current thread for a certain number of microseconds */
    public void sceKernelDelayThreadCB(Processor processor) {
        int[] gpr = processor.cpu.gpr;

        int micros = gpr[4];

        SceKernelThreadInfo thread = currentThread;

        thread.status = ThreadStatus.THREAD_WAITING;
        //thread.delaysteps = micros * 200000000 / 1000000; // TODO delaysteps = micros * steprate
        thread.delaysteps = (micros < WDT_THREAD_IDLE_CYCLES) ? micros : WDT_THREAD_IDLE_CYCLES - 1; // test version
        thread.do_callbacks = true;

        gpr[2] = 0;

        contextSwitch(nextThread());
    }

    public void sceKernelGetThreadId(Processor processor) {
        //Get the current thread Id
        processor.cpu.gpr[2] = currentThread.uid;
    }

    public void sceKernelReferThreadStatus(Processor processor) {
        int[] gpr = processor.cpu.gpr;
        Memory mem = Processor.memory;

        int uid = gpr[4];
        int info_addr = gpr[5];

        //Get the status information for the specified thread
        SceKernelThreadInfo thread = threadMap.get(uid);

        if (thread == null) {
            gpr[2] = ERROR_NOT_FOUND_THREAD;
            return;
        } else {
            Modules.log.debug(String.format("sceKernelReferThreadStatus id:%d info_addr=0x%08x", uid, info_addr));

            int i, len;
            mem.write32(info_addr, 106); //struct size

        //thread name max 32bytes
        len = thread.name.length();
        if (len > 31) len = 31;
        for (i=0; i < len; i++)
            mem.write8(info_addr +4 +i, (byte)thread.name.charAt(i));
        mem.write8(info_addr +4 +i, (byte)0);

        mem.write32(info_addr +36, thread.attr);
        mem.write32(info_addr +40, thread.status.getValue());
        mem.write32(info_addr +44, thread.entry_addr);
        mem.write32(info_addr +48, thread.stack_addr);
        mem.write32(info_addr +52, thread.stackSize);
        mem.write32(info_addr +56, thread.gpReg_addr);
        mem.write32(info_addr +60, thread.initPriority);
        mem.write32(info_addr +64, thread.currentPriority);
        mem.write32(info_addr +68, thread.waitType);
        mem.write32(info_addr +72, thread.waitId);
        mem.write32(info_addr +78, thread.wakeupCount);
        mem.write32(info_addr +82, thread.exitStatus);
        mem.write64(info_addr +86, thread.runClocks);
        mem.write32(info_addr +94, thread.intrPreemptCount);
        mem.write32(info_addr +98, thread.threadPreemptCount);
        mem.write32(info_addr +102, thread.releaseCount);

        gpr[2] = 0;
        }
    }

    public void sceKernelGetThreadmanIdList(Processor processor) {
        int gpr[] = processor.cpu.gpr;
        Memory mem = Processor.memory;

        int type = gpr[4];
        int readbuf_addr = gpr[5];
        int readbufsize = gpr[6];
        int idcount_addr = gpr[7];

        Modules.log.warn("UNIMPLEMENTED:sceKernelGetThreadmanIdList type:" + type
            + " readbuf:0x" + Integer.toHexString(readbuf_addr)
            + " readbufsize:" + readbufsize
            + " idcount:0x" + Integer.toHexString(idcount_addr));

        // TODO type=SCE_KERNEL_TMID_Thread, don't show the idle threads!

        // Fake success - 0 entries written
        if (mem.isAddressGood(idcount_addr)) {
            idcount_addr = 0;
        }
        Emulator.getProcessor().cpu.gpr[2] = 0;
    }

    public void sceKernelChangeThreadPriority(Processor processor) {
        int gpr[] = processor.cpu.gpr;

        int uid = gpr[4];
        int priority = gpr[5];

        if (uid == 0) uid = getCurrentThreadID();

        SceKernelThreadInfo thread = threadMap.get(uid);

        if (thread == null) {
            Modules.log.warn("sceKernelChangeThreadPriority id:" + Integer.toHexString(uid)
                    + " newPriority:0x" + Integer.toHexString(priority) + " unknown thread");
            gpr[2] = ERROR_NOT_FOUND_THREAD;
        } else {
            Modules.log.debug("sceKernelChangeThreadPriority id:" + Integer.toHexString(thread.uid)
                    + " newPriority:0x" + Integer.toHexString(priority) + " oldPriority:0x" + Integer.toHexString(thread.currentPriority));

            thread.currentPriority = priority;

            gpr[2] = 0;
        }
    }

    public void sceKernelChangeCurrentThreadAttr(Processor processor) {
        int gpr[] = processor.cpu.gpr;

        int clearAttr = gpr[4];
        int setAttr = gpr[5];

        Modules.log.debug("sceKernelChangeCurrentThreadAttr"
                + " clearAttr:0x" + Integer.toHexString(clearAttr)
                + " setAttr:0x" + Integer.toHexString(setAttr)
                + " oldAttr:0x" + Integer.toHexString(currentThread.attr));

        // Don't allow switching into kernel mode!
        if ((currentThread.attr & THREAD_ATTR_USER) == THREAD_ATTR_USER &&
            (clearAttr & THREAD_ATTR_USER) == THREAD_ATTR_USER) {
            Modules.log.debug("sceKernelChangeCurrentThreadAttr forcing user mode");
            clearAttr &= ~THREAD_ATTR_USER;
        }

        currentThread.attr = (currentThread.attr & ~clearAttr) | setAttr;

        gpr[2] = 0;
    }

    public void sceKernelWakeupThread(Processor processor) {
        int gpr[] = processor.cpu.gpr;

        int uid = gpr[4];

        SceKernelThreadInfo thread = threadMap.get(uid);

        if (thread == null) {
            Modules.log.warn("sceKernelWakeupThread id:" + uid + " unknown thread");
            gpr[2] = ERROR_NOT_FOUND_THREAD;
        } else if (thread.status != ThreadStatus.THREAD_SUSPEND) {
            Modules.log.warn("sceKernelWakeupThread id:" + uid + " not suspended (status=" + thread.status + ")");
            gpr[2] = ERROR_THREAD_IS_NOT_SUSPEND;
        } else {
            Modules.log.debug("sceKernelWakeupThread id:" + uid + " name:'" + thread.name + "'");
            thread.status = ThreadStatus.THREAD_READY;
            gpr[2] = 0;
        }
    }

    public void sceKernelWaitThreadEnd(Processor processor) {
        int gpr[] = processor.cpu.gpr;

        int uid = gpr[4];
        int micros = gpr[5];

        Modules.log.debug("sceKernelWaitThreadEnd id:" + uid + " timeout:" + micros);

        SceKernelThreadInfo thread = threadMap.get(uid);

        if (thread == null) {
            Modules.log.warn("sceKernelWaitThreadEnd unknown thread");
            gpr[2] = ERROR_NOT_FOUND_THREAD;
        } else if (waitThreadEndMap.get(uid) != null) {
            // TODO out current implementation only allows 1 thread to wait on another thread to end
            Modules.log.warn("UNIMPLEMENTED:sceKernelWaitThreadEnd another thread already waiting for the target thread to end");
            gpr[2] = -1;
        } else {

            waitThreadEndMap.put(uid, currentThread.uid);

            if (micros > 0) {
                currentThread.status = ThreadStatus.THREAD_WAITING;
                //currentThread.delaysteps = micros * 200000000 / 1000000; // TODO delaysteps = micros * steprate
                currentThread.delaysteps = micros; // test version
            } else {
                currentThread.status = ThreadStatus.THREAD_SUSPEND;
            }

            currentThread.do_callbacks = false;
            currentThread.do_waitThreadEnd = true;
            currentThread.waitThreadEndUid = uid;
            gpr[2] = 0;

            contextSwitch(nextThread());
        }
    }

    /** SceKernelSysClock time_addr http://psp.jim.sh/pspsdk-doc/structSceKernelSysClock.html
     * +1mil every second
     * high 32-bits never set on real psp? */
    public void sceKernelGetSystemTime(Processor processor) {
        int gpr[] = processor.cpu.gpr;
        Memory mem = Processor.memory;

        int time_addr = gpr[4];

        Modules.log.debug("sceKernelGetSystemTime time_addr=0x" + Integer.toHexString(time_addr));

        if (mem.isAddressGood(time_addr)) {
            long systemTime = System.nanoTime();
            int low = (int)(systemTime & 0xffffffffL);
            int hi = (int)((systemTime >> 32) & 0xffffffffL);

            mem.write32(time_addr, low);
            mem.write32(time_addr + 4, hi);
            gpr[2] = 0;
        } else {
            gpr[2] = -1;
        }
    }

    public void sceKernelGetSystemTimeWide(Processor processor) {
        int gpr[] = processor.cpu.gpr;

        Modules.log.debug("sceKernelGetSystemTimeWide");

        long systemTime = System.nanoTime();

        gpr[2] = (int)(systemTime & 0xffffffffL);
        gpr[3] = (int)((systemTime >> 32) & 0xffffffffL);
    }

    public void ThreadMan_sceKernelGetSystemTimeLow(Processor processor) {
        Modules.log.debug("sceKernelGetSystemTimeLow");
        long systemTime = System.nanoTime();
        processor.cpu.gpr[2] = (int)(systemTime & 0xffffffffL);
    }

    public boolean releaseObject(SceKernelUid object) {
        if (Managers.uids.removeObject(object)) {
            int uid = object.uid;
            threadMap.remove(uid);
            //waitThreadEndMap.remove(uid); ?
            return true;
        }
        return false;
    }


    public static final ThreadManager singleton;

    private ThreadManager() {
    }

    static {
        singleton = new ThreadManager();
    }
}
