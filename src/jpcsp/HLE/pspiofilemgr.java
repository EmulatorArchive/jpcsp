/*
Function:
- HLE everything in http://psp.jim.sh/pspsdk-doc/pspiofilemgr_8h.html
Notes:
- Just redirecting the xxxAsync calls to xxx

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

import jpcsp.filesystems.*;
import jpcsp.filesystems.umdiso.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import jpcsp.Emulator;
import jpcsp.Memory;
import jpcsp.MemoryMap;
import static jpcsp.util.Utilities.*;

/**
 *
 * @author George
 */
public class pspiofilemgr {
    private static pspiofilemgr  instance;
    private final boolean debug = true; //enable/disable debug

    public final static int PSP_O_RDONLY   = 0x0001;
    public final static int PSP_O_WRONLY   = 0x0002;
    public final static int PSP_O_RDWR     = (PSP_O_RDONLY | PSP_O_WRONLY);
    public final static int PSP_O_NBLOCK   = 0x0004;
    public final static int PSP_O_DIROPEN  = 0x0008; // Internal use for dopen
    public final static int PSP_O_APPEND   = 0x0100;
    public final static int PSP_O_CREAT    = 0x0200;
    public final static int PSP_O_TRUNC    = 0x0400;
    public final static int PSP_O_EXCL     = 0x0800;
    public final static int PSP_O_NOWAIT   = 0x8000;
    public final static int PSP_O_UNKNOWN1 = 0xf0000; // seen on Wipeout Pure

    public final static int PSP_SEEK_SET  = 0;
    public final static int PSP_SEEK_CUR  = 1;
    public final static int PSP_SEEK_END  = 2;

    public final static int PSP_ERROR_FILE_READ_ERROR     = 0x80020130;
    public final static int PSP_ERROR_TOO_MANY_OPEN_FILES = 0x80020320;
    public final static int PSP_ERROR_BAD_FILE_DESCRIPTOR = 0x80020323;
    public final static int PSP_ERROR_FILENAME_TOO_LONG   = 0x8002032d;
    public final static int PSP_ERROR_ASYNC_BUSY          = 0x80020329;


    private HashMap<Integer, IoInfo> filelist;
    private HashMap<Integer, IoDirInfo> dirlist;

    private String filepath; // current working directory on PC
    private UmdIsoReader iso;

    public static pspiofilemgr get_instance() {
        if (instance == null) {
            instance = new pspiofilemgr();
        }
        return instance;
    }

    private pspiofilemgr() {
    }

    public void Initialise() {
        if (filelist != null) {
            // Close open files
            for (Iterator<IoInfo> it = filelist.values().iterator(); it.hasNext();) {
                IoInfo info = it.next();
                try {
                    info.readOnlyFile.close();
                } catch(IOException e) {
                    Modules.log.error("pspiofilemgr - error closing file: " + e.getMessage());
                }
            }
        }

        filelist = new HashMap<Integer, IoInfo>();
        dirlist = new HashMap<Integer, IoDirInfo>();
    }

    private String getDeviceFilePath(String pspfilename) {
        //Modules.log.debug("getDeviceFilePath filepath='" + filepath + "' pspfilename='" + pspfilename + "'");
        String device = filepath;
        String path = pspfilename;
        String filename = null;

        // on PSP
        // /path - relative to cwd
        // dev:path
        // dev:/path

        // on PSP: device:path
        // on PC: device/path
        int findcolon = pspfilename.indexOf(":");
        if (findcolon != -1) {
            // Device absolute
            device = pspfilename.substring(0, findcolon);
            path = pspfilename.substring(findcolon + 1);
            //Modules.log.debug("getDeviceFilePath split device='" + device + "' path='" + path + "'");
        }

        if (path.startsWith("/")) {
            if (path.length() > 1) {
                filename = device + path;
            } else {
                filename = device;
            }
        } else {
            filename = device + "/" + path;
        }

        if (device.equals("host0")) {
            // If an iso is loaded, remap host0 to disc0
            // If an iso is not loaded, assume running an unpacked iso, remap to file system
            if (iso != null) {
                Modules.log.warn("pspiofilemgr - remapping host0 to disc0");
                filename = filename.replace("host0", "disc0");
            } else {
                Modules.log.warn("pspiofilemgr - remapping host0 to " + filepath);
                filename = filename.replace("host0", filepath);
            }
        }

        //if (filename != null)
        //    Modules.log.debug("getDeviceFilePath filename='" + filename + "'");

        return filename;
    }

    private boolean isUmdPath(String deviceFilePath) {
        return deviceFilePath.toLowerCase().startsWith("disc0");
    }

    private String trimUmdPrefix(String pcfilename) {
        if (pcfilename.toLowerCase().startsWith("disc0/"))
            return pcfilename.substring(6);
        if (pcfilename.toLowerCase().startsWith("disc0"))
            return pcfilename.substring(5);
        return pcfilename;
    }

    public void sceIoSync(int device_addr, int unknown) {
        String device = readStringZ(Memory.getInstance().mainmemory, (device_addr & 0x3fffffff) - MemoryMap.START_RAM);
        if (debug) Modules.log.debug("IGNORING:sceIoSync(device='" + device + "',unknown=0x" + Integer.toHexString(unknown) + ")");
        Emulator.getProcessor().cpu.gpr[2] = 0; // Fake success
    }

    public void sceIoWaitAsync(int uid, int res_addr) {
        if (debug) Modules.log.debug("sceIoWaitAsync - uid " + Integer.toHexString(uid) + " res:0x" + Integer.toHexString(res_addr));

        SceUIDMan.get_instance().checkUidPurpose(uid, "IOFileManager-File", true);
        IoInfo info = filelist.get(uid);
        if (info == null) {
            Modules.log.warn("sceIoClose - unknown uid " + Integer.toHexString(uid));
            Emulator.getProcessor().cpu.gpr[2] = -1;
        } else {
            Memory mem = Memory.getInstance();
            if (mem.isAddressGood(res_addr)) {
                mem.write32(res_addr, (int)(info.result & 0xffffffffL));
                mem.write32(res_addr + 4, (int)((info.result >> 32) & 0xffffffffL));
            }

            Emulator.getProcessor().cpu.gpr[2] = 0;
        }

        ThreadMan.get_instance().yieldCurrentThread();
    }

    public void sceIoWaitAsyncCB(int uid, int res_addr) {
        // TODO check callbacks
        if (debug) Modules.log.debug("sceIoWaitAsyncCB redirecting to sceIoWaitAsync");
        sceIoWaitAsync(uid, res_addr);
    }

    public void sceIoOpen(int filename_addr, int flags, int permissions) {
        String filename = readStringZ(Memory.getInstance().mainmemory, (filename_addr & 0x3fffffff) - MemoryMap.START_RAM);
        if (debug) Modules.log.debug("sceIoOpen filename = " + filename + " flags = " + Integer.toHexString(flags) + " permissions = " + Integer.toOctalString(permissions));

        if (debug) {
            if ((flags & PSP_O_RDONLY) == PSP_O_RDONLY) Modules.log.debug("PSP_O_RDONLY");
            if ((flags & PSP_O_WRONLY) == PSP_O_WRONLY) Modules.log.debug("PSP_O_WRONLY");
            if ((flags & PSP_O_NBLOCK) == PSP_O_NBLOCK) Modules.log.debug("PSP_O_NBLOCK");
            if ((flags & PSP_O_DIROPEN) == PSP_O_DIROPEN) Modules.log.debug("PSP_O_DIROPEN");
            if ((flags & PSP_O_APPEND) == PSP_O_APPEND) Modules.log.debug("PSP_O_APPEND");
            if ((flags & PSP_O_CREAT) == PSP_O_CREAT) Modules.log.debug("PSP_O_CREAT");
            if ((flags & PSP_O_TRUNC) == PSP_O_TRUNC) Modules.log.debug("PSP_O_TRUNC");
            if ((flags & PSP_O_EXCL) == PSP_O_EXCL) Modules.log.debug("PSP_O_EXCL");
            if ((flags & PSP_O_NOWAIT) == PSP_O_NOWAIT) Modules.log.debug("PSP_O_NOWAIT");
        }

        String mode;

        // PSP_O_RDWR check must come before the individual PSP_O_RDONLY and PSP_O_WRONLY checks
        if ((flags & PSP_O_RDWR) == PSP_O_RDWR) {
            mode = "rw";
        } else if ((flags & PSP_O_RDONLY) == PSP_O_RDONLY) {
            mode = "r";
        } else if ((flags & PSP_O_WRONLY) == PSP_O_WRONLY) {
            // SeekableRandomFile doesn't support write only
            mode = "rw";
        } else {
            Modules.log.error("sceIoOpen - unhandled flags " + Integer.toHexString(flags));
            Emulator.getProcessor().cpu.gpr[2] = -1;
            return;
        }

        // TODO we may want to do something with PSP_O_CREAT and permissions
        // using java File and its setReadable/Writable/Executable.
        // Does PSP filesystem even support permissions?

        // TODO PSP_O_TRUNC flag. delete the file and recreate it?

        // This could get messy, is it even allowed?
        if ((flags & PSP_O_RDONLY) == PSP_O_RDONLY &&
            (flags & PSP_O_APPEND) == PSP_O_APPEND) {
            Modules.log.warn("sceIoOpen - read and append flags both set!");
        }

        try {
            String pcfilename = getDeviceFilePath(filename);
            if (pcfilename != null) {
                if (debug) Modules.log.debug("sceIoOpen - opening file " + pcfilename);
                //if (debug) Modules.log.debug("sceIoOpen - isUmdPath " + isUmdPath(pcfilename));

                if (isUmdPath(pcfilename)) {
                    // check umd is mounted
                    if (iso == null) {
                        Modules.log.error("sceIoOpen - no umd mounted");
                        Emulator.getProcessor().cpu.gpr[2] = -1;

                    // check flags are valid
                    } else if ((flags & PSP_O_WRONLY) == PSP_O_WRONLY ||
                        (flags & PSP_O_CREAT) == PSP_O_CREAT ||
                        (flags & PSP_O_TRUNC) == PSP_O_TRUNC) {
                        // should we refuse (return -1) or just ignore?
                        Modules.log.error("sceIoOpen - refusing to open umd media for write");
                        Emulator.getProcessor().cpu.gpr[2] = -1;
                    } else {
                        // open file
                        try {
                            UmdIsoFile file = iso.getFile(trimUmdPrefix(pcfilename));
                            IoInfo info = new IoInfo(file, mode, flags, permissions);
                            info.result = info.uid;
                            Emulator.getProcessor().cpu.gpr[2] = info.uid;
                        } catch(FileNotFoundException e) {
                            if (debug) Modules.log.debug("sceIoOpen - umd file not found (ok to ignore this message, debug purpose only)");
                            Emulator.getProcessor().cpu.gpr[2] = -1;
                        } catch(IOException e) {
                            Modules.log.error("sceIoOpen - error opening umd media: " + e.getMessage());
                            Emulator.getProcessor().cpu.gpr[2] = -1;
                        }
                    }
                } else {
                    // First check if the file already exists
                    File file = new File(pcfilename);
                    if (file.exists() &&
                        (flags & PSP_O_CREAT) == PSP_O_CREAT &&
                        (flags & PSP_O_EXCL) == PSP_O_EXCL) {
                        // PSP_O_CREAT + PSP_O_EXCL + file already exists = error
                        if (debug) Modules.log.debug("sceIoOpen - file already exists (PSP_O_CREAT + PSP_O_EXCL)");
                        Emulator.getProcessor().cpu.gpr[2] = -1;
                    } else {
                        if (file.exists() &&
                            (flags & PSP_O_TRUNC) == PSP_O_TRUNC) {
                            if (debug) Modules.log.warn("sceIoOpen - file already exists, deleting UNIMPLEMENT (PSP_O_TRUNC)");
                            //file.delete();
                        }

                        SeekableRandomFile raf = new SeekableRandomFile(pcfilename, mode);
                        IoInfo info = new IoInfo(raf, mode, flags, permissions);
                        info.result = info.uid;
                        Emulator.getProcessor().cpu.gpr[2] = info.uid;
                    }
                }
            } else {
                Emulator.getProcessor().cpu.gpr[2] = -1;
            }
        } catch(FileNotFoundException e) {
            // To be expected under mode="r" and file doesn't exist
            if (debug) Modules.log.debug("sceIoOpen - file not found (ok to ignore this message, debug purpose only)");
            Emulator.getProcessor().cpu.gpr[2] = -1;
        }
    }

    public void sceIoOpenAsync(int filename_addr, int flags, int permissions) {
        if (debug) Modules.log.debug("sceIoOpenAsync redirecting to sceIoOpen");
        sceIoOpen(filename_addr, flags, permissions);
    }

    public void sceIoClose(int uid) {
        if (debug) Modules.log.debug("sceIoClose - uid " + Integer.toHexString(uid));

        try {
            SceUIDMan.get_instance().checkUidPurpose(uid, "IOFileManager-File", true);
            IoInfo info = filelist.remove(uid);
            if (info == null) {
                if (uid != 1 && uid != 2) // stdin and stderr
                    Modules.log.warn("sceIoClose - unknown uid " + Integer.toHexString(uid));
                Emulator.getProcessor().cpu.gpr[2] = -1;
            } else {
                info.readOnlyFile.close();
                SceUIDMan.get_instance().releaseUid(info.uid, "IOFileManager-File");
                info.result = 0;
                Emulator.getProcessor().cpu.gpr[2] = 0;
            }
        } catch(IOException e) {
            Modules.log.error("pspiofilemgr - error closing file: " + e.getMessage());
            e.printStackTrace();
            Emulator.getProcessor().cpu.gpr[2] = -1;
        }
    }

    public void sceIoCloseAsync(int uid) {
        if (debug) Modules.log.debug("sceIoCloseAsync redirecting to sceIoClose");
        sceIoClose(uid);
        Emulator.getProcessor().cpu.gpr[2] = 0;
    }

    public void sceIoWrite(int uid, int data_addr, int size) {
        //if (debug) Modules.log.debug("sceIoWrite - uid " + Integer.toHexString(uid) + " data " + Integer.toHexString(data_addr) + " size " + size);
        data_addr &= 0x3fffffff; // remove kernel/cache bits

        if (uid == 1) { // stdout
            String stdout = readStringNZ(Memory.getInstance().mainmemory, data_addr - MemoryMap.START_RAM, size);
            System.out.print(stdout);
            Emulator.getProcessor().cpu.gpr[2] = size;
        } else if (uid == 2) { // stderr
            String stderr = readStringNZ(Memory.getInstance().mainmemory, data_addr - MemoryMap.START_RAM, size);
            System.out.print(stderr);
            Emulator.getProcessor().cpu.gpr[2] = size;
        } else {
            if (debug) Modules.log.debug("sceIoWrite - uid " + Integer.toHexString(uid) + " data " + Integer.toHexString(data_addr) + " size " + size);

            try {
                SceUIDMan.get_instance().checkUidPurpose(uid, "IOFileManager-File", true);
                IoInfo info = filelist.get(uid);
                if (info == null) {
                    Modules.log.warn("sceIoWrite - unknown uid " + Integer.toHexString(uid));
                    Emulator.getProcessor().cpu.gpr[2] = -1;
                } else if ((data_addr >= MemoryMap.START_RAM ) && (data_addr + size <= MemoryMap.END_RAM)) {
                    if ((info.flags & PSP_O_APPEND) == PSP_O_APPEND) {
                        Modules.log.warn("sceIoWrite - untested append operation");
                        info.msFile.seek(info.msFile.length());
                    }

                    info.msFile.write(
                        Memory.getInstance().mainmemory.array(),
                        Memory.getInstance().mainmemory.arrayOffset() + data_addr - MemoryMap.START_RAM,
                        size);

                    info.result = size;
                    Emulator.getProcessor().cpu.gpr[2] = size;
                } else {
                    Modules.log.warn("sceIoWrite - data is outside of ram " + Integer.toHexString(data_addr));
                    info.result = -1;
                    Emulator.getProcessor().cpu.gpr[2] = -1;
                }
            } catch(IOException e) {
                e.printStackTrace();
                Emulator.getProcessor().cpu.gpr[2] = -1;
            }
        }
    }

    public void sceIoWriteAsync(int uid, int data_addr, int size) {
        if (debug) Modules.log.debug("sceIoWriteAsync redirecting to sceIoWrite");
        sceIoWrite(uid, data_addr, size);
        Emulator.getProcessor().cpu.gpr[2] = 0;
    }

    public void sceIoRead(int uid, int data_addr, int size) {
        if (debug) Modules.log.debug("sceIoRead - uid " + Integer.toHexString(uid) + " data " + Integer.toHexString(data_addr) + " size " + size);
        data_addr &= 0x3fffffff; // remove kernel/cache bits

        if (uid == 3) { // stdin
            // TODO
            Emulator.getProcessor().cpu.gpr[2] = 0;
        } else {
            try {
                SceUIDMan.get_instance().checkUidPurpose(uid, "IOFileManager-File", true);
                IoInfo info = filelist.get(uid);
                if (info == null) {
                    Modules.log.warn("sceIoRead - unknown uid " + Integer.toHexString(uid));
                    Emulator.getProcessor().cpu.gpr[2] = PSP_ERROR_BAD_FILE_DESCRIPTOR;
                } else if ((data_addr >= MemoryMap.START_RAM ) && (data_addr + size <= MemoryMap.END_RAM)) {
                    // Using readFully for ms/umd compatibility, but now we must
                    // manually make sure it doesn't read off the end of the file.
                    if (info.readOnlyFile.getFilePointer() + size > info.readOnlyFile.length()) {
                        int oldSize = size;
                        size = (int)(info.readOnlyFile.length() - info.readOnlyFile.getFilePointer());
                        Modules.log.warn("sceIoRead - clamping size old=" + oldSize + " new=" + size
                            + " fp=" + info.readOnlyFile.getFilePointer() + " len=" + info.readOnlyFile.length());
                    }

                    info.readOnlyFile.readFully(
                        Memory.getInstance().mainmemory.array(),
                        Memory.getInstance().mainmemory.arrayOffset() + data_addr - MemoryMap.START_RAM,
                        size);

                    info.result = size;
                    Emulator.getProcessor().cpu.gpr[2] = size;
                } else {
                    Modules.log.warn("sceIoRead - data is outside of ram " + Integer.toHexString(data_addr));
                    info.result = PSP_ERROR_FILE_READ_ERROR;
                    Emulator.getProcessor().cpu.gpr[2] = PSP_ERROR_FILE_READ_ERROR;
                }
            } catch(IOException e) {
                e.printStackTrace();
                Emulator.getProcessor().cpu.gpr[2] = PSP_ERROR_FILE_READ_ERROR;
            }
        }
    }

    public void sceIoReadAsync(int uid, int data_addr, int size) {
        if (debug) Modules.log.debug("sceIoReadAsync redirecting to sceIoRead");
        sceIoRead(uid, data_addr, size);
        Emulator.getProcessor().cpu.gpr[2] = 0;
    }

    // TODO sceIoLseek with 64-bit return value
    public void sceIoLseek(int uid, long offset, int whence) {
        if (debug) Modules.log.debug("sceIoLseek - uid " + Integer.toHexString(uid) + " offset " + offset + " (hex=0x" + Long.toHexString(offset) + ") whence " + getWhenceName(whence));
        seek(uid, offset, whence, true);
    }

    public void sceIoLseekAsync(int uid, long offset, int whence) {
        if (debug) Modules.log.debug("sceIoLseekAsync - uid " + Integer.toHexString(uid) + " offset " + offset + " (hex=0x" + Long.toHexString(offset) + ") whence " + getWhenceName(whence));
        seek(uid, offset, whence, true);
        Emulator.getProcessor().cpu.gpr[2] = 0;
    }

    public void sceIoLseek32(int uid, int offset, int whence) {
        if (debug) System.out.println("sceIoLseek32 - uid " + Integer.toHexString(uid) + " offset " + offset + " (hex=0x" + Integer.toHexString(offset) + ") whence " + getWhenceName(whence));
        seek(uid, ((long)offset & 0xFFFFFFFFL), whence, false);
    }

    public void sceIoLseek32Async(int uid, int offset, int whence) {
        if (debug) System.out.println("sceIoLseek32Async - uid " + Integer.toHexString(uid) + " offset " + offset + " (hex=0x" + Integer.toHexString(offset) + ") whence " + getWhenceName(whence));
        seek(uid, ((long)offset & 0xFFFFFFFFL), whence, false);
        Emulator.getProcessor().cpu.gpr[2] = 0;
    }

    private String getWhenceName(int whence) {
        switch(whence) {
            case PSP_SEEK_SET: return "PSP_SEEK_SET";
            case PSP_SEEK_CUR: return "PSP_SEEK_CUR";
            case PSP_SEEK_END: return "PSP_SEEK_END";
            default: return "UNHANDLED " + whence;
        }
    }

    private void seek(int uid, long offset, int whence, boolean resultIs64bit) {
        //if (debug) Modules.log.debug("seek - uid " + Integer.toHexString(uid) + " offset " + offset + " whence " + whence);

        if (uid == 1 || uid == 2 || uid == 3) { // stdio
            Modules.log.error("seek - can't seek on stdio uid " + Integer.toHexString(uid));
            Emulator.getProcessor().cpu.gpr[2] = -1;
            if (resultIs64bit)
                Emulator.getProcessor().cpu.gpr[3] = -1;
        } else {
            try {
                SceUIDMan.get_instance().checkUidPurpose(uid, "IOFileManager-File", true);
                IoInfo info = filelist.get(uid);
                if (info == null) {
                    Modules.log.warn("seek - unknown uid " + Integer.toHexString(uid));
                    Emulator.getProcessor().cpu.gpr[2] = -1;
                    if (resultIs64bit)
                        Emulator.getProcessor().cpu.gpr[3] = -1;
                } else {
                    switch(whence) {
                        case PSP_SEEK_SET:
                            if (offset > info.readOnlyFile.length()) {
                                Modules.log.warn("seek - offset longer than file length!");
                            }
                            info.readOnlyFile.seek(offset);
                            break;
                        case PSP_SEEK_CUR:
                            info.readOnlyFile.seek(info.readOnlyFile.getFilePointer() + offset);
                            break;
                        case PSP_SEEK_END:
                            info.readOnlyFile.seek(info.readOnlyFile.length() - offset);
                            break;
                        default:
                            Modules.log.error("seek - unhandled whence " + whence);
                            break;
                    }
                    long result = info.readOnlyFile.getFilePointer();

                    info.result = result;
                    Emulator.getProcessor().cpu.gpr[2] = (int)(result & 0xFFFFFFFFL);
                    if (resultIs64bit)
                        Emulator.getProcessor().cpu.gpr[3] = (int)(result >> 32);
                }
            } catch(IOException e) {
                e.printStackTrace();
                Emulator.getProcessor().cpu.gpr[2] = -1;
                if (resultIs64bit)
                    Emulator.getProcessor().cpu.gpr[3] = -1;
            }
        }
    }

    public void sceIoMkdir(int dir_addr, int permissions) {
        String dir = readStringZ(Memory.getInstance().mainmemory, (dir_addr & 0x3fffffff) - MemoryMap.START_RAM);
        if (debug) Modules.log.debug("sceIoMkdir dir = " + dir);
        //should work okay..
        String pcfilename = getDeviceFilePath(dir);
        if (pcfilename != null) {
            File f = new File(pcfilename);
            f.mkdir();
            Emulator.getProcessor().cpu.gpr[2] = 0;
        } else {
            Emulator.getProcessor().cpu.gpr[2] = -1;
        }
    }

    public void sceIoChdir(int path_addr) {
        String path = readStringZ(Memory.getInstance().mainmemory, (path_addr & 0x3fffffff) - MemoryMap.START_RAM);
        if (debug) Modules.log.debug("sceIoChdir path = " + path);

        if (path.equals("..")) {
            int index = filepath.lastIndexOf("/");
            if (index != -1)
                filepath = filepath.substring(0, index);

            Modules.log.info("pspiofilemgr - filepath " + filepath);
            Emulator.getProcessor().cpu.gpr[2] = 0;
        } else {
            String pcfilename = getDeviceFilePath(path);
            if (pcfilename != null) {
                filepath = pcfilename;

                Modules.log.info("pspiofilemgr - filepath " + filepath);
                Emulator.getProcessor().cpu.gpr[2] = 0;
            } else {
                Emulator.getProcessor().cpu.gpr[2] = -1;
            }
        }
    }

    public void sceIoDopen(int dirname_addr) {
        String dirname = readStringZ(Memory.getInstance().mainmemory, (dirname_addr & 0x3fffffff) - MemoryMap.START_RAM);
        if (debug) Modules.log.debug("sceIoDopen dirname = " + dirname);

        String pcfilename = getDeviceFilePath(dirname);
        if (pcfilename != null) {

            if (isUmdPath(pcfilename)) {
                // Files in our iso virtual file system
                String isofilename = trimUmdPrefix(pcfilename);
                if (debug) Modules.log.debug("sceIoDopen - isofilename = " + isofilename);
                // check umd is mounted
                if (iso == null) {
                    Modules.log.error("sceIoDopen - no umd mounted");
                    Emulator.getProcessor().cpu.gpr[2] = -1;
                } else {
                    if (debug) Modules.log.debug("sceIoDopen - isofilename = " + isofilename);
                    try {
                        if (iso.isDirectory(isofilename)) {
                            String[] filenames = iso.listDirectory(isofilename);
                            //if (debug) Modules.log.debug("sceIoDopen on umd, " + filenames.length + " files");
                            IoDirInfo info = new IoDirInfo(pcfilename, filenames);
                            Emulator.getProcessor().cpu.gpr[2] = info.uid;
                        } else {
                            if (debug) Modules.log.warn("sceIoDopen not a umd directory!");
                            Emulator.getProcessor().cpu.gpr[2] = -1;
                        }
                    } catch(FileNotFoundException e) {
                        Modules.log.warn("sceIoDopen - umd file not found");
                        Emulator.getProcessor().cpu.gpr[2] = -1;
                    } catch(IOException e) {
                        Modules.log.warn("sceIoDopen - umd io error: " + e.getMessage());
                        Emulator.getProcessor().cpu.gpr[2] = -1;
                    }
                }
            } else if (dirname.startsWith("/") && dirname.indexOf(":") != -1) {
                // Detect paths outside of our emulated mstick dir and show a helpful message
                // It is unsafe to try and support this, as an app could access any part of your computer instead of being limited to the ms0 dir
                Modules.log.warn("sceIoDopen apps running outside of ms0 dir are not fully supported, relative child paths should still work");
                Emulator.getProcessor().cpu.gpr[2] = -1;
            } else {
                // Regular apps inside mstick dir
                if (debug) Modules.log.debug("sceIoDopen - pcfilename = " + pcfilename);
                File f = new File(pcfilename);
                if (f.isDirectory()) {
                    IoDirInfo info = new IoDirInfo(pcfilename, f.list());
                    Emulator.getProcessor().cpu.gpr[2] = info.uid;
                } else {
                    if (debug) Modules.log.warn("sceIoDopen not a directory!");
                    Emulator.getProcessor().cpu.gpr[2] = -1;
                }
            }
        } else {
            Emulator.getProcessor().cpu.gpr[2] = -1;
        }
    }

    public void sceIoDread(int uid, int dirent_addr) {
        if (debug) Modules.log.debug("sceIoDread - uid = " + Integer.toHexString(uid) + " dirent = " + Integer.toHexString(dirent_addr));

        SceUIDMan.get_instance().checkUidPurpose(uid, "IOFileManager-Directory", true);
        IoDirInfo info = dirlist.get(uid);
        if (info == null) {
            Modules.log.warn("sceIoDread - unknown uid " + Integer.toHexString(uid));
            Emulator.getProcessor().cpu.gpr[2] = PSP_ERROR_BAD_FILE_DESCRIPTOR;
        } else if (info.hasNext()) {
            String filename = info.next(); // TODO is the separator needed?
            Modules.log.debug("sceIoDread - path = " + info.path + " filename = " + filename);

            SceIoStat stat = stat(info.path + "/" + filename);
            if (stat != null) {
                SceIoDirent dirent = new SceIoDirent(stat, filename);
                dirent.write(Memory.getInstance(), dirent_addr);
                Emulator.getProcessor().cpu.gpr[2] = 1; // TODO "> 0", so number of files remaining or 1 is ok?
            } else {
                Modules.log.warn("sceIoDread - stat failed (" + info.path + "/" + filename + ")");
                Emulator.getProcessor().cpu.gpr[2] = -1;
            }
        } else {
            Modules.log.debug("sceIoDread - no more files");
            Emulator.getProcessor().cpu.gpr[2] = 0;
        }
    }

    public void sceIoDclose(int uid) {
        if (debug) Modules.log.debug("sceIoDclose - uid = " + Integer.toHexString(uid));

        SceUIDMan.get_instance().checkUidPurpose(uid, "IOFileManager-Directory", true);
        IoDirInfo info = dirlist.remove(uid);
        if (info == null) {
            Modules.log.warn("sceIoDclose - unknown uid " + Integer.toHexString(uid));
            Emulator.getProcessor().cpu.gpr[2] = PSP_ERROR_BAD_FILE_DESCRIPTOR;
        } else {
            SceUIDMan.get_instance().releaseUid(info.uid, "IOFileManager-Directory");
            Emulator.getProcessor().cpu.gpr[2] = 0;
        }
    }

    public void sceIoDevctl(int device_addr, int cmd, int indata_addr, int inlen, int outdata_addr, int outlen) {
        String device = readStringZ(Memory.getInstance().mainmemory, (device_addr & 0x3fffffff) - MemoryMap.START_RAM);
        if (debug) {
            Modules.log.debug("sceIoDevctl(device='" + device
                + "',cmd=0x" + Integer.toHexString(cmd)
                + ",indata=0x" + Integer.toHexString(indata_addr)
                + ",inlen=" + inlen
                + ",outdata=0x" + Integer.toHexString(outdata_addr)
                + ",outlen=" + outlen + ")");
        }

        switch(cmd) {
            case 0x2415821:
                Modules.log.warn("UNIMPLEMENTED: sceIoDevctl register ms eject callback");
                Emulator.getProcessor().cpu.gpr[2] = 0; // Fake success
                break;

            default:
                Modules.log.warn("sceIoDevctl unknown command " + String.format("0x%08X", cmd));
                Emulator.getProcessor().cpu.gpr[2] = -1; // Just fail for now
                break;
        }
    }

    /** @param pcfilename can be null for convenience
     * @returns null on error */
    private SceIoStat stat(String pcfilename) {
        SceIoStat stat = null;
        if (pcfilename != null) {
            //if (debug) Modules.log.debug("stat - pcfilename = " + pcfilename);
            if (isUmdPath(pcfilename)) {
                // check umd is mounted
                if (iso == null) {
                    Modules.log.error("stat - no umd mounted");
                    Emulator.getProcessor().cpu.gpr[2] = -1;
                } else {
                    String isofilename = trimUmdPrefix(pcfilename);
                    try {
                        int mode = 4; // 4=readable
                        int attr = 0;
                        long size = 0;

                        // Set attr (dir/file)
                        if (iso.isDirectory(isofilename)) {
                            attr |= 0x10;
                            mode |= 1; // 1=executable
                        } else { // isFile
                            attr |= 0x20;
                            UmdIsoFile file = iso.getFile(isofilename);
                            size = file.length();
                        }

                        // Octal extend into user and group
                        mode = mode + mode * 8 + mode * 64;
                        // Copy attr into mode
                        mode |= attr << 8;

                        stat = new SceIoStat(mode, attr, size,
                            new ScePspDateTime(0), new ScePspDateTime(0),
                            new ScePspDateTime(0));
                    } catch(FileNotFoundException e) {
                        Modules.log.warn("stat - umd file not found");
                    } catch(IOException e) {
                        Modules.log.warn("stat - umd io error: " + e.getMessage());
                    }
                }
            } else {
                File file = new File(pcfilename);
                if (file.exists()) {
                    int mode = (file.canRead() ? 4 : 0) + (file.canWrite() ? 2 : 0) + (file.canExecute() ? 1 : 0);
                    int attr = 0;
                    long size = file.length();
                    long mtime = file.lastModified();

                    // Octal extend into user and group
                    mode = mode + mode * 8 + mode * 64;
                    //if (debug) Modules.log.debug("stat - permissions = " + Integer.toOctalString(mode));

                    // Set attr (dir/file) and copy into mode
                    if (file.isDirectory())
                        attr |= 0x10;
                    if (file.isFile())
                        attr |= 0x20;
                    mode |= attr << 8;

                    // Java can't see file create/access time
                    stat = new SceIoStat(mode, attr, size,
                        new ScePspDateTime(0), new ScePspDateTime(0),
                        new ScePspDateTime(mtime));
                }
            }
        }
        return stat;
    }

    public void sceIoGetstat(int file_addr, int stat_addr) {
        String filename = readStringZ(Memory.getInstance().mainmemory, (file_addr & 0x3fffffff) - MemoryMap.START_RAM);
        if (debug) Modules.log.debug("sceIoGetstat - file = " + filename + " stat = " + Integer.toHexString(stat_addr));

        String pcfilename = getDeviceFilePath(filename);
        SceIoStat stat = stat(pcfilename);
        if (stat != null) {
            stat.write(Memory.getInstance(), stat_addr);
            Emulator.getProcessor().cpu.gpr[2] = 0;
        } else {
            Emulator.getProcessor().cpu.gpr[2] = -1;
        }
    }

    //the following sets the filepath from memstick manager.
    public void setfilepath(String filepath)
    {
        // This could mess up... I don't think it's really needed anyway, just makes logging slightly nicer
        filepath = filepath.replaceAll("\\\\", "/");

        Modules.log.info("pspiofilemgr - filepath " + filepath);
        this.filepath = filepath;
    }

    public void setIsoReader(UmdIsoReader iso)
    {
        /* debug
        if (iso != null)
        {
            Modules.log.debug("pspiofilemgr - umd mounted " + iso.getFilename());
        }
        else
        {
            Modules.log.debug("pspiofilemgr - umd unmounted");
        }
        */
        this.iso = iso;

        // testing remapping of host0
        //getDeviceFilePath("host0:modules/module.cnf");
    }

    class IoInfo {
        // Internal settings
        public final SeekableRandomFile msFile; // on memory stick, should either be identical to readOnlyFile or null
        public final SeekableDataInput readOnlyFile; // on memory stick or umd
        public final String mode;

        // PSP settings
        public final int flags;
        public final int permissions;

        public final int uid;
        public long result; // The return value from the last operation on this file, used by sceIoWaitAsync

        public IoInfo(SeekableRandomFile f, String mode, int flags, int permissions) {
            this.msFile = f;
            this.readOnlyFile = f;
            this.mode = mode;
            this.flags = flags;
            this.permissions = permissions;
            uid = SceUIDMan.get_instance().getNewUid("IOFileManager-File");
            filelist.put(uid, this);
        }

        public IoInfo(SeekableDataInput f, String mode, int flags, int permissions) {
            this.msFile = null;
            this.readOnlyFile = f;
            this.mode = mode;
            this.flags = flags;
            this.permissions = permissions;
            uid = SceUIDMan.get_instance().getNewUid("IOFileManager-File");
            filelist.put(uid, this);
        }
    }

    class IoDirInfo {
        final String path;
        final String[] filenames;
        int position;
        final int uid;

        public IoDirInfo(String path, String[] filenames) {
            // iso reader doesn't like path//filename, so trim trailing /
            // (it's like doing cd somedir/ instead of cd somedir, makes little difference)
            if (path.endsWith("/"))
                path = path.substring(0, path.length() - 1);

            this.path = path;

            this.filenames = filenames;
            position = 0;

            // Hide iso special files
            if (filenames.length > position && filenames[position].equals("."))
                position++;
            if (filenames.length > position && filenames[position].equals("\01"))
                position++;

            uid = SceUIDMan.get_instance().getNewUid("IOFileManager-Directory");
            dirlist.put(uid, this);
        }

        public boolean hasNext() {
            return (position < filenames.length);
        }

        public String next() {
            String filename = null;
            if (position < filenames.length) {
                filename = filenames[position];
                position++;
            }
            return filename;
        }
    }

}
