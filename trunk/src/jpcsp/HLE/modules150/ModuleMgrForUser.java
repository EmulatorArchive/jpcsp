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

import java.io.IOException;
import java.nio.ByteBuffer;

import jpcsp.HLE.Modules;
import jpcsp.HLE.pspSysMem;
import jpcsp.HLE.pspiofilemgr;
import jpcsp.HLE.kernel.Managers;
import jpcsp.HLE.kernel.types.SceKernelModuleInfo;
import jpcsp.HLE.kernel.types.SceModule;
import jpcsp.HLE.modules.HLEModule;
import jpcsp.HLE.modules.HLEModuleFunction;
import jpcsp.HLE.modules.HLEModuleManager;

import jpcsp.Emulator;
import jpcsp.Loader;
import jpcsp.Memory;
import jpcsp.Processor;
import jpcsp.filesystems.SeekableDataInput;
import jpcsp.filesystems.umdiso.UmdIsoFile;
import jpcsp.util.Utilities;
import jpcsp.HLE.kernel.types.SceKernelThreadInfo;
import static jpcsp.HLE.kernel.types.SceKernelErrors.*;

import jpcsp.Allegrex.CpuState;

public class ModuleMgrForUser implements HLEModule {
    enum bannedModulesList {
        LIBFONT,
        sc_sascore,
        audiocodec,
        libatrac3plus,
        videocodec,
        mpegbase,
        mpeg,
        psmf,
        pspnet,
        pspnet_adhoc,
        pspnet_adhocctl,
        pspnet_inet,
        pspnet_adhoc_matching,
        pspnet_adhoc_download,
        pspnet_apctl,
        pspnet_resolver,
        pspnet_ap_dialog_dummy,
        libparse_uri,
        libparse_http,
        libhttp_rfc,
        libssl
    }
	@Override
	public String getName() { return "ModuleMgrForUser"; }

	@Override
	public void installModule(HLEModuleManager mm, int version) {
		if (version >= 150) {

			mm.addFunction(sceKernelLoadModuleByIDFunction, 0xB7F46618);
			mm.addFunction(sceKernelLoadModuleFunction, 0x977DE386);
			mm.addFunction(sceKernelLoadModuleMsFunction, 0x710F61B5);
			mm.addFunction(sceKernelLoadModuleBufferUsbWlanFunction, 0xF9275D98);
			mm.addFunction(sceKernelStartModuleFunction, 0x50F0C1EC);
			mm.addFunction(sceKernelStopModuleFunction, 0xD1FF982A);
			mm.addFunction(sceKernelUnloadModuleFunction, 0x2E0911AA);
			mm.addFunction(sceKernelSelfStopUnloadModuleFunction, 0xD675EBB8);
			mm.addFunction(sceKernelStopUnloadSelfModuleFunction, 0xCC1D3699);
			mm.addFunction(sceKernelGetModuleIdListFunction, 0x644395E2);
			mm.addFunction(sceKernelQueryModuleInfoFunction, 0x748CBED9);
			mm.addFunction(sceKernelGetModuleIdFunction, 0xF0A26395);
			mm.addFunction(sceKernelGetModuleIdByAddressFunction, 0xD8B73127);
            mm.addFunction(sceKernelStopUnloadSelfModuleWithStatusFunction, 0x8f2df740);
		}
	}

	@Override
	public void uninstallModule(HLEModuleManager mm, int version) {
		if (version >= 150) {

			mm.removeFunction(sceKernelLoadModuleByIDFunction);
			mm.removeFunction(sceKernelLoadModuleFunction);
			mm.removeFunction(sceKernelLoadModuleMsFunction);
			mm.removeFunction(sceKernelLoadModuleBufferUsbWlanFunction);
			mm.removeFunction(sceKernelStartModuleFunction);
			mm.removeFunction(sceKernelStopModuleFunction);
			mm.removeFunction(sceKernelUnloadModuleFunction);
			mm.removeFunction(sceKernelSelfStopUnloadModuleFunction);
			mm.removeFunction(sceKernelStopUnloadSelfModuleFunction);
			mm.removeFunction(sceKernelGetModuleIdListFunction);
			mm.removeFunction(sceKernelQueryModuleInfoFunction);
			mm.removeFunction(sceKernelGetModuleIdFunction);
			mm.removeFunction(sceKernelGetModuleIdByAddressFunction);
            mm.removeFunction(sceKernelStopUnloadSelfModuleWithStatusFunction);
		}
	}

	//
    // When an HLE module is loaded using sector syntax, with no file corresponding to the
	// referenced sector, try searching for the real module's name inside the file itself.
	// For encrypted modules, the real name can be found in the first sector of the file.
	// This name is not encrypted.
	//
	// For example:
	//   MONSTER HUNTER FREEDOM UNITE ULES01213
	//     hleKernelLoadModule(path='disc0:/sce_lbn0x11981_size0x59c0')
	//   and the sector 0x11981 is found inside a huge "DATA.BIN" file (a CD image):
	//     PSP_GAME/USRDIR/DATA.BIN: Starting at sector 0xD960, with size 737 MB
	//
    private String extractHLEModuleName(String path) {
        String result = "UNKNOWN";
        String sectorString = path.substring(path.indexOf("sce_lbn") + 7, path.indexOf("_size"));
        int PRXStartSector = (int) Utilities.parseHexLong(sectorString);

        try {
            byte[] buffer = pspiofilemgr.getInstance().getIsoReader().readSector(PRXStartSector);
            String libName = new String(buffer);
            if(libName.contains("sce")) {
                String module = libName.substring(libName.indexOf("sce"), libName.indexOf(" "));

                // Compare with known names and assign the real name for this module.
                if(module.contains("sceFont"))
                    result = "libfont";
                else if(module.contains("sceMpeg"))
                    result = "mpeg";
                else if(module.contains("sceSAScore"))
                    result = "sc_sascore";
                else if(module.contains("sceATRAC3plus"))
                    result = "libatrac3plus";
            }
        } catch (IOException ioe) {
            // Sector doesn't exist...
        }
        return result;
    }

	private boolean hleKernelLoadHLEModule(Processor processor, String name, StringBuilder prxname) {
        CpuState cpu = processor.cpu;

        if (prxname == null) {
        	prxname = new StringBuilder();
        }

        int findprx = name.lastIndexOf("/");
        int endprx = name.toLowerCase().indexOf(".prx");
        if (endprx >= 0) {
            prxname.append(name.substring(findprx+1, endprx));
        } else if(name.contains("sce_lbn")) {
            prxname.append(extractHLEModuleName(name));
        } else {
        	prxname.append("UNKNOWN");
        }

        // Load flash0 modules as Java HLE modules
        if (name.startsWith("flash0:")) {
            // Simulate a successful loading
        	HLEModuleManager moduleManager = HLEModuleManager.getInstance();
        	if (moduleManager.hasFlash0Module(prxname.toString())) {
        		Modules.log.info("hleKernelLoadModule(path='" + name + "') HLE module loaded");
        	} else {
                Modules.log.warn("IGNORED:hleKernelLoadModule(path='" + name + "'): module from flash0 not loaded");
        	}
            cpu.gpr[2] = HLEModuleManager.getInstance().LoadFlash0Module(prxname.toString());
            return true;
        }

        // Ban some modules
        for (bannedModulesList bannedModuleName : bannedModulesList.values())
        {
            if (bannedModuleName.name().equalsIgnoreCase(prxname.toString()))
            {
            	HLEModuleManager moduleManager = HLEModuleManager.getInstance();
            	if (moduleManager.hasFlash0Module(prxname.toString())) {
            		Modules.log.info("hleKernelLoadModule(path='" + name + "') HLE module loaded");
            	} else {
            		Modules.log.warn("IGNORED:hleKernelLoadModule(path='" + name + "'): module from banlist not loaded");
            	}
                cpu.gpr[2] = HLEModuleManager.getInstance().LoadFlash0Module(prxname.toString());
                return true;
            }
        }

        return false;
	}

	private void hleKernelLoadModule(Processor processor, String name, int flags, int uid, boolean byUid) {
        CpuState cpu = processor.cpu;
        Memory mem = Processor.memory;

        StringBuilder prxname = new StringBuilder();
        if (hleKernelLoadHLEModule(processor, name, prxname)) {
        	return;
        }

        // Load module as ELF
        try {
            SeekableDataInput moduleInput = pspiofilemgr.getInstance().getFile(name, flags);
            if (moduleInput != null) {
            	if (moduleInput instanceof UmdIsoFile) {
            		UmdIsoFile umdIsoFile = (UmdIsoFile) moduleInput;
            		String realFileName = umdIsoFile.getName();
            		if (realFileName != null && !name.endsWith(realFileName)) {
            			if (hleKernelLoadHLEModule(processor, realFileName, null)) {
            				moduleInput.close();
            				return;
            			}
            		}
            	}

            	byte[] moduleBytes = new byte[(int) moduleInput.length()];
                moduleInput.readFully(moduleBytes);
                ByteBuffer moduleBuffer = ByteBuffer.wrap(moduleBytes);

                // TODO
                // We need to get a load address, we can either add getHeapBottom to pspsysmem, or we can malloc something small
                // We're going to need to write a SceModule struct somewhere, so we could malloc that, and add the size of the struct to the address
                // For now we'll just malloc 64 bytes :P (the loadBase needs to be aligned anyway)
                int loadBase = pspSysMem.getInstance().malloc(2, pspSysMem.PSP_SMEM_Low, 256, 0) + 256;
                pspSysMem.getInstance().addSysMemInfo(2, "ModuleMgr", pspSysMem.PSP_SMEM_Low, 256, loadBase);
                SceModule module = Loader.getInstance().LoadModule(name, moduleBuffer, loadBase);

                if ((module.fileFormat & Loader.FORMAT_SCE) == Loader.FORMAT_SCE ||
                    (module.fileFormat & Loader.FORMAT_PSP) == Loader.FORMAT_PSP) {
                    // Simulate a successful loading
                    Modules.log.warn("IGNORED:hleKernelLoadModule(path='" + name + "') encrypted module not loaded");
                    SceModule fakeModule = new SceModule(true);
                    fakeModule.modname = prxname.toString();
                    fakeModule.write(mem, fakeModule.address);
                    Managers.modules.addModule(fakeModule);
                    cpu.gpr[2] = fakeModule.modid;
                } else if ((module.fileFormat & Loader.FORMAT_ELF) == Loader.FORMAT_ELF) {
                    cpu.gpr[2] = module.modid;
                } else {
                    // The Loader class now manages the module's memory footprint, it won't allocate if it failed to load
                    //pspSysMem.getInstance().free(loadBase);
                    cpu.gpr[2] = -1;
                }

                moduleInput.close();
            } else {
                Modules.log.warn("hleKernelLoadModule(path='" + name + "') can't find file");
                cpu.gpr[2] = ERROR_FILE_NOT_FOUND;
            }
        } catch (IOException e) {
            Modules.log.error("hleKernelLoadModule - Error while loading module " + name + ": " + e.getMessage());
            cpu.gpr[2] = -1;
        }
	}

	public void sceKernelLoadModuleByID(Processor processor) {
        CpuState cpu = processor.cpu;

        int uid = cpu.gpr[4];
        int option_addr = cpu.gpr[5];
        String name = pspiofilemgr.getInstance().getFileFilename(uid);

        Modules.log.debug("sceKernelLoadModuleByID(uid=0x" + Integer.toHexString(uid)
            + "('" + name + "')"
            + ",option=0x" + Integer.toHexString(option_addr) + ")");

        hleKernelLoadModule(processor, name, 0, uid, true);
    }

    public void sceKernelLoadModule(Processor processor) {
        CpuState cpu = processor.cpu;

        int path_addr = cpu.gpr[4];
        int flags = cpu.gpr[5];
        int option_addr = cpu.gpr[6];
        String name = Utilities.readStringZ(path_addr);
        Modules.log.debug("sceKernelLoadModule(path='" + name
            + "',flags=0x" + Integer.toHexString(flags)
            + ",option=0x" + Integer.toHexString(option_addr) + ")");

        hleKernelLoadModule(processor, name, flags, 0, false);
    }

	public void sceKernelLoadModuleMs(Processor processor) {
		CpuState cpu = processor.cpu;

		System.out.println("Unimplemented NID function sceKernelLoadModuleMs [0x710F61B5]");

		cpu.gpr[2] = 0xDEADC0DE;
	}

	public void sceKernelLoadModuleBufferUsbWlan(Processor processor) {
		CpuState cpu = processor.cpu;

		System.out.println("Unimplemented NID function sceKernelLoadModuleBufferUsbWlan [0xF9275D98]");

		cpu.gpr[2] = 0xDEADC0DE;
	}

    public void sceKernelStartModule(Processor processor) {
        CpuState cpu = processor.cpu;
        Memory mem = Processor.memory;

        int uid = cpu.gpr[4];
        int argsize = cpu.gpr[5];
        int argp_addr = cpu.gpr[6];
        int status_addr = cpu.gpr[7]; // TODO
        int option_addr = cpu.gpr[8]; // SceKernelSMOption

        Modules.log.debug("sceKernelStartModule(uid=0x" + Integer.toHexString(uid)
            + ",argsize=" + argsize
            + ",argp=0x" + Integer.toHexString(argp_addr)
            + ",status=0x" + Integer.toHexString(status_addr)
            + ",option=0x" + Integer.toHexString(option_addr) + ")");

        SceModule sceModule = Managers.modules.getModuleByUID(uid);

        if (sceModule == null) {
            Modules.log.warn("sceKernelStartModule - unknown module UID 0x" + Integer.toHexString(uid));
            cpu.gpr[2] = ERROR_UNKNOWN_MODULE;
        } else  if (sceModule.isFlashModule) {
            // Trying to start a module loaded from flash0:
            // Do nothing...
        	if (HLEModuleManager.getInstance().hasFlash0Module(sceModule.modname)) {
        		Modules.log.info("IGNORING:sceKernelStartModule HLE module '" + sceModule.modname + "'");
        	} else {
        		Modules.log.warn("IGNORING:sceKernelStartModule flash module '" + sceModule.modname + "'");
        	}
            cpu.gpr[2] = sceModule.modid; // return the module id
        } else {
        	ThreadManForUser threadMan = Modules.ThreadManForUserModule;
            if (mem.isAddressGood(sceModule.entry_addr)) {
                if (mem.isAddressGood(status_addr)) {
                    mem.write32(status_addr, 0); // TODO set to return value of the thread (when it exits, of course)
                }

                int priority = 0x20;
                if (sceModule.module_start_thread_priority > 0) {
                	priority = sceModule.module_start_thread_priority;
                }
                int stackSize = 0x40000;
                if (sceModule.module_start_thread_stacksize > 0) {
                	stackSize = sceModule.module_start_thread_stacksize;
                }
                SceKernelThreadInfo thread = threadMan.hleKernelCreateThread("SceModmgrStart",
                        sceModule.entry_addr, priority, stackSize, sceModule.attribute, option_addr);
                // override inherited module id with the new module we are starting
                thread.moduleid = sceModule.modid;
                cpu.gpr[2] = sceModule.modid; // return the module id
                threadMan.hleKernelStartThread(thread, argsize, argp_addr, sceModule.gp_value);
            } else if (sceModule.entry_addr == -1) {
                Modules.log.info("sceKernelStartModule - module has no entry point");
                // Try using the start_func parameters.
                if (mem.isAddressGood(sceModule.module_start_func)) {
                    Modules.log.info("sceKernelStartModule - using start_func parameters");
                    if (mem.isAddressGood(status_addr)) {
                        mem.write32(status_addr, 0); // TODO set to return value of the thread (when it exits, of course)
                    }

                    SceKernelThreadInfo thread = threadMan.hleKernelCreateThread("SceModmgrStart",
                            sceModule.module_start_func, sceModule.module_start_thread_priority,
                            sceModule.module_start_thread_stacksize, sceModule.module_start_thread_attr
                            , option_addr);

                    thread.moduleid = sceModule.modid;
                    cpu.gpr[2] = sceModule.modid; // return the module id
                    threadMan.hleKernelStartThread(thread, argsize, argp_addr, sceModule.gp_value);
                } else {
                    cpu.gpr[2] = -1;
                }
            } else {
                Modules.log.warn("sceKernelStartModule - invalid entry address 0x" + Integer.toHexString(sceModule.entry_addr));
                cpu.gpr[2] = -1;
            }
        }
    }

    public void sceKernelStopModule(Processor processor) {
        CpuState cpu = processor.cpu;
        Memory mem = Processor.memory;

        int uid = cpu.gpr[4];
        int argsize = cpu.gpr[5];
        int argp_addr = cpu.gpr[6];
        int status_addr = cpu.gpr[7]; // TODO
        int option_addr = cpu.gpr[8]; // SceKernelSMOption

        Modules.log.warn("sceKernelStopModule(uid=0x" + Integer.toHexString(uid)
            + ",argsize=" + argsize
            + ",argp=0x" + Integer.toHexString(argp_addr)
            + ",status=0x" + Integer.toHexString(status_addr)
            + ",option=0x" + Integer.toHexString(option_addr) + ")");

        SceModule sceModule = Managers.modules.getModuleByUID(uid);

        if (sceModule == null) {
            Modules.log.warn("sceKernelStopModule - unknown module UID 0x" + Integer.toHexString(uid));
            cpu.gpr[2] = ERROR_UNKNOWN_MODULE;
        } else  if (sceModule.isFlashModule) {
            // Trying to stop a module loaded from flash0:
            // Shouldn't get here...
        	if (HLEModuleManager.getInstance().hasFlash0Module(sceModule.modname)) {
        		Modules.log.info("IGNORING:sceKernelStopModule HLE module '" + sceModule.modname + "'");
        	} else {
        		Modules.log.warn("IGNORING:sceKernelStopModule flash module '" + sceModule.modname + "'");
        	}
            cpu.gpr[2] = 0; // Fake success.
        } else {
        	ThreadManForUser threadMan = Modules.ThreadManForUserModule;
            if (mem.isAddressGood(sceModule.module_stop_func)) {
                if (mem.isAddressGood(status_addr)) {
                    mem.write32(status_addr, 0); // TODO set to return value of the thread (when it exits, of course)
                }

                SceKernelThreadInfo thread = threadMan.hleKernelCreateThread("SceModmgrStop",
                        sceModule.module_stop_func, sceModule.module_stop_thread_priority,
                        sceModule.module_stop_thread_stacksize, sceModule.module_stop_thread_attr
                        , option_addr);

                thread.moduleid = sceModule.modid;
                cpu.gpr[2] = 0;
                threadMan.hleKernelStartThread(thread, argsize, argp_addr, sceModule.gp_value);
            } else {
                // TODO: 0x80020135 module already stopped.
                // May be related to the SceModule status or with the thread exit status.
                Modules.log.warn("sceKernelStopModule - no stop function found");
                cpu.gpr[2] = -1;
            }
        }
    }

    public void sceKernelUnloadModule(Processor processor) {
        CpuState cpu = processor.cpu;

        int uid = cpu.gpr[4];

        SceModule sceModule = Managers.modules.getModuleByUID(uid);
        if (sceModule == null) {
            Modules.log.warn("sceKernelUnloadModule unknown module UID 0x" + Integer.toHexString(uid));
            cpu.gpr[2] = -1;
        } else {
            Modules.log.warn("PARTIAL:sceKernelUnloadModule(uid=" + Integer.toHexString(uid) + ") modname:'" + sceModule.modname + "'");

            HLEModuleManager.getInstance().UnloadFlash0Module(sceModule);
            cpu.gpr[2] = 0;
        }
    }

    public void sceKernelSelfStopUnloadModule(Processor processor) {
        CpuState cpu = processor.cpu;

        int argsize = cpu.gpr[4];
        int argp_addr = cpu.gpr[5];
        int status_addr = cpu.gpr[6];
        int options_addr = cpu.gpr[7];  // SceKernelSMOption

        Modules.log.debug("sceKernelSelfStopUnloadModule(argsize=" + argsize
            + ",argp_addr=0x" + Integer.toHexString(argp_addr)
            + ",status_addr=0x" + Integer.toHexString(status_addr)
            + ",options_addr=0x" + Integer.toHexString(options_addr) +
            ") current thread:'" + Modules.ThreadManForUserModule.getCurrentThread().name + "'");

        Modules.log.info("Program exit detected (sceKernelSelfStopUnloadModule)");
        Emulator.PauseEmuWithStatus(Emulator.EMU_STATUS_OK);

        cpu.gpr[2] = 0;
    }

    public void sceKernelStopUnloadSelfModuleWithStatus(Processor processor) {
        CpuState cpu = processor.cpu;

        int exitcode = cpu.gpr[4];
        int argsize = cpu.gpr[5];
        int argp_addr = cpu.gpr[6];
        int status_addr = cpu.gpr[7];
        int options_addr = cpu.gpr[8];  // SceKernelSMOption

        Modules.log.debug("sceKernelSelfStopUnloadModule(exitcode=" + exitcode
            + ",argsize=" + argsize
            + ",argp_addr=0x" + Integer.toHexString(argp_addr)
            + ",status_addr=0x" + Integer.toHexString(status_addr)
            + ",options_addr=0x" + Integer.toHexString(options_addr) +
            ") current thread:'" + Modules.ThreadManForUserModule.getCurrentThread().name + "'");


        Modules.log.info("Program exit detected (sceKernelStopUnloadSelfModuleWithStatus)");

        // Pause the emulator with the given status.
        // TODO: Check for mismatches.
        Emulator.PauseEmuWithStatus(exitcode);

        cpu.gpr[2] = 0;
    }

	public void sceKernelStopUnloadSelfModule(Processor processor) {
		CpuState cpu = processor.cpu;

		System.out.println("Unimplemented NID function sceKernelStopUnloadSelfModule [0xCC1D3699]");

		cpu.gpr[2] = 0xDEADC0DE;
	}

	public void sceKernelGetModuleIdList(Processor processor) {
		CpuState cpu = processor.cpu;

		System.out.println("Unimplemented NID function sceKernelGetModuleIdList [0x644395E2]");

		cpu.gpr[2] = 0xDEADC0DE;
	}

    public void sceKernelQueryModuleInfo(Processor processor) {
        CpuState cpu = processor.cpu;
        Memory mem = Processor.memory;

        int uid = cpu.gpr[4];
        int info_addr = cpu.gpr[5];

        SceModule sceModule = Managers.modules.getModuleByUID(uid);
        if (sceModule == null) {
            Modules.log.warn("sceKernelQueryModuleInfo unknown module UID 0x" + Integer.toHexString(uid));
            cpu.gpr[2] = -1;
        } else if (!mem.isAddressGood(info_addr)) {
            Modules.log.warn("sceKernelQueryModuleInfo bad info pointer " + String.format("0x%08X", info_addr));
            cpu.gpr[2] = -1;
        } else {
            Modules.log.debug("sceKernelQueryModuleInfo UID 0x" + Integer.toHexString(uid)
                + " info " + String.format("0x%08X", info_addr)
                + " modname '" + sceModule.modname + "'");
            SceKernelModuleInfo moduleInfo = new SceKernelModuleInfo();
            moduleInfo.copy(sceModule);
            moduleInfo.write(mem, info_addr);
            cpu.gpr[2] = 0;
        }
    }

    public void sceKernelGetModuleId(Processor processor) {
        CpuState cpu = processor.cpu;

        int moduleid = Modules.ThreadManForUserModule.getCurrentThread().moduleid;

        if (Modules.log.isDebugEnabled()) {
        	Modules.log.debug("sceKernelGetModuleId returning 0x" + Integer.toHexString(moduleid));
        }

        cpu.gpr[2] = moduleid;
    }

	public void sceKernelGetModuleIdByAddress(Processor processor) {
		CpuState cpu = processor.cpu;

        int addr = cpu.gpr[4];

        SceModule module = Managers.modules.getModuleByAddress(addr);
        if (module != null) {
            Modules.log.debug("sceKernelGetModuleIdByAddress(addr=0x" + Integer.toHexString(addr) + ") returning 0x" + Integer.toHexString(module.modid));
            cpu.gpr[2] = module.modid;
        } else {
            Modules.log.warn("sceKernelGetModuleIdByAddress(addr=0x" + Integer.toHexString(addr) + ") module not found");
            cpu.gpr[2] = -1;
        }
	}

	public final HLEModuleFunction sceKernelLoadModuleByIDFunction = new HLEModuleFunction("ModuleMgrForUser", "sceKernelLoadModuleByID") {
		@Override
		public final void execute(Processor processor) {
			sceKernelLoadModuleByID(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.ModuleMgrForUserModule.sceKernelLoadModuleByID(processor);";
		}
	};

	public final HLEModuleFunction sceKernelLoadModuleFunction = new HLEModuleFunction("ModuleMgrForUser", "sceKernelLoadModule") {
		@Override
		public final void execute(Processor processor) {
			sceKernelLoadModule(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.ModuleMgrForUserModule.sceKernelLoadModule(processor);";
		}
	};

	public final HLEModuleFunction sceKernelLoadModuleMsFunction = new HLEModuleFunction("ModuleMgrForUser", "sceKernelLoadModuleMs") {
		@Override
		public final void execute(Processor processor) {
			sceKernelLoadModuleMs(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.ModuleMgrForUserModule.sceKernelLoadModuleMs(processor);";
		}
	};

	public final HLEModuleFunction sceKernelLoadModuleBufferUsbWlanFunction = new HLEModuleFunction("ModuleMgrForUser", "sceKernelLoadModuleBufferUsbWlan") {
		@Override
		public final void execute(Processor processor) {
			sceKernelLoadModuleBufferUsbWlan(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.ModuleMgrForUserModule.sceKernelLoadModuleBufferUsbWlan(processor);";
		}
	};

	public final HLEModuleFunction sceKernelStartModuleFunction = new HLEModuleFunction("ModuleMgrForUser", "sceKernelStartModule") {
		@Override
		public final void execute(Processor processor) {
			sceKernelStartModule(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.ModuleMgrForUserModule.sceKernelStartModule(processor);";
		}
	};

	public final HLEModuleFunction sceKernelStopModuleFunction = new HLEModuleFunction("ModuleMgrForUser", "sceKernelStopModule") {
		@Override
		public final void execute(Processor processor) {
			sceKernelStopModule(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.ModuleMgrForUserModule.sceKernelStopModule(processor);";
		}
	};

	public final HLEModuleFunction sceKernelUnloadModuleFunction = new HLEModuleFunction("ModuleMgrForUser", "sceKernelUnloadModule") {
		@Override
		public final void execute(Processor processor) {
			sceKernelUnloadModule(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.ModuleMgrForUserModule.sceKernelUnloadModule(processor);";
		}
	};

	public final HLEModuleFunction sceKernelSelfStopUnloadModuleFunction = new HLEModuleFunction("ModuleMgrForUser", "sceKernelSelfStopUnloadModule") {
		@Override
		public final void execute(Processor processor) {
			sceKernelSelfStopUnloadModule(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.ModuleMgrForUserModule.sceKernelSelfStopUnloadModule(processor);";
		}
	};

	public final HLEModuleFunction sceKernelStopUnloadSelfModuleFunction = new HLEModuleFunction("ModuleMgrForUser", "sceKernelStopUnloadSelfModule") {
		@Override
		public final void execute(Processor processor) {
			sceKernelStopUnloadSelfModule(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.ModuleMgrForUserModule.sceKernelStopUnloadSelfModule(processor);";
		}
	};

	public final HLEModuleFunction sceKernelGetModuleIdListFunction = new HLEModuleFunction("ModuleMgrForUser", "sceKernelGetModuleIdList") {
		@Override
		public final void execute(Processor processor) {
			sceKernelGetModuleIdList(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.ModuleMgrForUserModule.sceKernelGetModuleIdList(processor);";
		}
	};

	public final HLEModuleFunction sceKernelQueryModuleInfoFunction = new HLEModuleFunction("ModuleMgrForUser", "sceKernelQueryModuleInfo") {
		@Override
		public final void execute(Processor processor) {
			sceKernelQueryModuleInfo(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.ModuleMgrForUserModule.sceKernelQueryModuleInfo(processor);";
		}
	};

	public final HLEModuleFunction sceKernelGetModuleIdFunction = new HLEModuleFunction("ModuleMgrForUser", "sceKernelGetModuleId") {
		@Override
		public final void execute(Processor processor) {
			sceKernelGetModuleId(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.ModuleMgrForUserModule.sceKernelGetModuleId(processor);";
		}
	};

	public final HLEModuleFunction sceKernelGetModuleIdByAddressFunction = new HLEModuleFunction("ModuleMgrForUser", "sceKernelGetModuleIdByAddress") {
		@Override
		public final void execute(Processor processor) {
			sceKernelGetModuleIdByAddress(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.ModuleMgrForUserModule.sceKernelGetModuleIdByAddress(processor);";
		}
	};

    public final HLEModuleFunction sceKernelStopUnloadSelfModuleWithStatusFunction = new HLEModuleFunction("ModuleMgrForUser", "sceKernelStopUnloadSelfModuleWithStatus") {
		@Override
		public final void execute(Processor processor) {
			sceKernelStopUnloadSelfModuleWithStatus(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.ModuleMgrForUserModule.sceKernelStopUnloadSelfModuleWithStatus(processor);";
		}
	};
}