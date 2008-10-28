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

package jpcsp.HLE.modules;

import jpcsp.HLE.kernel.types.SceModule;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import jpcsp.Emulator;
import jpcsp.HLE.Modules;
import jpcsp.HLE.pspSysMem;
import jpcsp.NIDMapper;

/**
 * For backwards compatibility with the current jpcsp code, the old
 * SyscallHandler can still be used. When an unhandled syscall is found
 * HLEModuleManager.getInstance().handleSyscall(code) should be called.
 * This function will then return true if the syscall is handled, in which case
 * no error message should be printed by SyscallHandler.java.
 *
 * Modules that require stepping should implement HLEThread and call
 * mm.addThread inside installModule with a matching mm.removeThread in
 * uninstall module.
 * Example: ThreadMan, pspctrl, pspAudio, pspdisplay
 *
 * @author fiveofhearts
 */
public class HLEModuleManager {
    private static HLEModuleManager instance;

    private HashMap<Integer, HLEModuleFunction> syscallCodeToFunction;
    private int syscallCodeAllocator;

    private List<HLEThread> hleThreadList;
    private List<SceModule> sceModuleList;

    private HashMap<String, List<HLEModule>> flash0prxMap;

    /** The current firmware version we are using */
    private int firmwareVersion;

    // TODO add more modules here
    private HLEModule[] defaultModules = new HLEModule[] {
        new Sample(), // For testing purposes
        Modules.StdioForUserModule,
        Modules.sceUmdUserModule,
        Modules.scePowerModule,
        Modules.sceUtilityModule,
        Modules.sceRtcModule,
        Modules.Kernel_LibraryModule,
        Modules.ModuleMgrForUserModule,
        Modules.sceMpegModule,
        Modules.LoadCoreForKernelModule,
        Modules.sceAttrac3plusModule,
        Modules.sceCtrlModule,
    };

    public static HLEModuleManager getInstance() {
        if (instance == null) {
            instance = new HLEModuleManager();
        }
        return instance;
    }

    public void Initialise() {
        syscallCodeToFunction = new HashMap<Integer, HLEModuleFunction>();

        // Official syscalls start at 0x2000,
        // so we'll put the HLE syscalls far away at 0x4000.
        syscallCodeAllocator = 0x4000;

        hleThreadList = new LinkedList<HLEThread>();
        sceModuleList = new LinkedList<SceModule>();

        // TODO use fw version from PSF, unless it's a banned PSF (such as used by pspsdk)
        firmwareVersion = pspSysMem.PSP_FIRMWARE_150;
        installDefaultModules();
        initialiseFlash0PRXMap();
    }

    private void installDefaultModules() {
        for (HLEModule module : defaultModules) {
            module.installModule(this, firmwareVersion);
        }
    }

    // TODO add here modules in flash that aren't loaded by default
    // We could add all modules but I think we just need the unloaded ones (fiveofhearts)
    private void initialiseFlash0PRXMap() {
        flash0prxMap = new HashMap<String, List<HLEModule>>();
        /* TODO
        List<HLEModule> prx;

        prx = new LinkedList<HLEModule>();
        prx.add(Modules.sceNetIfhandleModule);
        prx.add(Modules.sceNetIfhandle_libModule);
        prx.add(Modules.sceNetIfhandle_driverModule);
        flash0prxMap.put("ifhandle.prx", prx);

        prx = new LinkedList<HLEModule>();
        prx.add(Modules.sceNetModule);
        prx.add(Modules.sceNet_libModule);
        flash0prxMap.put("pspnet.prx", prx);
        */
    }

    /** @return the UID assigned to the module or negative on error */
    public int LoadFlash0Module(String prxname) {
        int uid = -1;
        List<HLEModule> prx = flash0prxMap.get(prxname);
        if (prx != null) {
            for (HLEModule module : prx) {
                module.installModule(this, firmwareVersion);
            }
            // TODO assign a proper uid and SceModule struct
            uid = SceModule.flashModuleUid;
        }
        return uid;
    }

    /** Instead use addFunction. */
    @Deprecated
    public void add(HLEModuleFunction func, int nid) {
        addFunction(func, nid);
    }

    /** Instead use removeFunction. */
    @Deprecated
    public void remove(HLEModuleFunction func) {
        removeFunction(func);
    }

    public void addFunction(HLEModuleFunction func, int nid) {
        func.setNid(nid);

        // See if a known syscall code exists for this NID
        int code = NIDMapper.getInstance().nidToSyscall(func.getNid());
        if (code == -1) {
            // Allocate an arbitrary syscall code to the function
            code = syscallCodeAllocator;
            // Add the new code to the NIDMapper
            NIDMapper.getInstance().addSyscallNid(func.getNid(), syscallCodeAllocator);
            syscallCodeAllocator++;
        }

        /*
        System.out.println("HLEModuleManager - registering "
                + func.getModuleName() + "_"
                + String.format("%08x", func.getNid()) + "_"
                + func.getFunctionName()
                + " to " + Integer.toHexString(code));
        */
        func.setSyscallCode(code);
        syscallCodeToFunction.put(code, func);
    }

    public void removeFunction(HLEModuleFunction func) {
        /*
        System.out.println("HLEModuleManager - unregistering "
                + func.getModuleName() + "_"
                + String.format("%08x", func.getNid()) + "_"
                + func.getFunctionName());
        */
        int syscallCode = func.getSyscallCode();
        syscallCodeToFunction.remove(syscallCode);
    }

    public void addThread(HLEThread thread) {
        hleThreadList.add(thread);
    }

    public void removeThread(HLEThread thread) {
        hleThreadList.remove(thread);
    }

    public void step() {
        for (Iterator<HLEThread> it = hleThreadList.iterator(); it.hasNext();) {
            HLEThread thread = it.next();
            thread.step();
        }
    }

    /**
     * @param code The syscall code to try and execute.
     * @return true if handled, false if not handled.
     */
    public boolean handleSyscall(int code) {
        HLEModuleFunction func = syscallCodeToFunction.get(code);
        if (func != null) {
            func.execute(Emulator.getProcessor());
            return true;
        } else {
            return false;
        }
    }

    public void addSceModule(SceModule sceModule) {
        sceModuleList.add(sceModule);
    }

    public SceModule getSceModuleByUid(int uid) {
        for (Iterator<SceModule> it = sceModuleList.iterator(); it.hasNext();) {
            SceModule sceModule = it.next();
            if (sceModule.getUid() == uid) {
                return sceModule;
            }
        }

        return null;
    }
}
