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
package jpcsp.HLE.kernel.types;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.List;

import jpcsp.Memory;
import jpcsp.HLE.Modules;
import jpcsp.filesystems.SeekableDataInput;
import jpcsp.filesystems.SeekableRandomFile;
import jpcsp.format.PSF;
import jpcsp.util.Utilities;

/*
 * TODO list:
 * 1. Check writePsf:
 *   -> sfoParam.parentalLevel & 0x3ff ? FF1 -> 1027 -> 3.
 *   -> Need to add SAVEDATA_FILE_LIST, SAVEDATA_PARAMS and SAVEDATA_DIRECTORY?
 *   -> Delete original file, size "map" cannot resize it smaller.
 */

public class SceUtilitySavedataParam extends pspAbstractMemoryMappedStructure {
	public final static String savedataPath     = "ms0:/PSP/SAVEDATA/";
	public final static String icon0FileName    = "ICON0.PNG";
	public final static String icon1FileName    = "ICON1.PNG";
	public final static String pic1FileName     = "PIC1.PNG";
	public final static String snd0FileName     = "SND0.AT3";
	public final static String paramSfoFileName = "PARAM.SFO";

	public int baseAddress;

	public pspUtilityDialogCommon base;
	public int mode;
		public final static int MODE_AUTOLOAD = 0;
		public final static int MODE_AUTOSAVE = 1;
		public final static int MODE_LOAD = 2;
		public final static int MODE_SAVE = 3;
		public final static int MODE_LISTLOAD = 4;
		public final static int MODE_LISTSAVE = 5;
		public final static int MODE_LISTDELETE = 6;
		public final static int MODE_DELETE = 7;
		public final static int MODE_SIZES = 8;
        public final static int MODE_AUTODELETE = 9;
        public final static int MODE_SINGLEDELETE = 10;
        public final static int MODE_LIST = 11;
        public final static int MODE_FILES = 12;
        public final static int MODE_MAKEDATASECURE = 13;
        public final static int MODE_MAKEDATA = 14;
		public final static int MODE_READSECURE = 15;
        public final static int MODE_READ = 16;
        public final static int MODE_WRITESECURE = 17;
        public final static int MODE_WRITE = 18;
        public final static int MODE_ERASESECURE = 19;
        public final static int MODE_ERASE = 20;
        public final static int MODE_DELETEDATA = 21;
        public final static int MODE_GETSIZE = 22;
    public int bind;   // Used by certain applications to detect if this save data was created on a different PSP.
        public final static int BIND_NOT_USED = 0;
        public final static int BIND_IS_OK = 1;
        public final static int BIND_IS_REJECTED = 2;
        public final static int BIND_IS_NOT_SUPPORTED = 3;
	public boolean overwrite;
	public String gameName; // name used from the game for saves, equal for all saves
	public String saveName; // name of the particular save, normally a number
	public String fileName; // name of the data file of the game for example DATA.BIN
	public String[] saveNameList; // used by multiple modes
	public int saveNameListAddr;
	int dataBuf;
	int dataBufSize;
	public int dataSize;
	public PspUtilitySavedataSFOParam sfoParam;
	public PspUtilitySavedataFileData icon0FileData;
	public PspUtilitySavedataFileData icon1FileData;
	public PspUtilitySavedataFileData pic1FileData;
	public PspUtilitySavedataFileData snd0FileData;
	int newDataAddr;
	public PspUtilitySavedataListSaveNewData newData;
    public int focus;
		public final static int FOCUS_UNKNOWN = 0;
		public final static int FOCUS_FIRSTLIST = 1;	// First in list
		public final static int FOCUS_LASTLIST = 2;		// Last in list
		public final static int FOCUS_LATEST = 3;		// Most recent one
		public final static int FOCUS_OLDEST = 4;		// Oldest one
		public final static int FOCUS_FIRSTEMPTY = 7;	// First empty slot
		public final static int FOCUS_LASTEMPTY = 8;	// Last empty slot
    public int abortStatus;     // Used by sceUtilityXXXAbort functions.
	public int msFreeAddr;      // Address of a buffer to hold MemoryStick free size data (used in MODE_SIZES only).
	public int msDataAddr;      // Address of a buffer to hold MemoryStick size data (used in MODE_SIZES only).
	public int utilityDataAddr; // Address of a buffer to hold utility size data (used in MODE_SIZES only).
    public String key;		    // Encrypt/decrypt key for save with firmware >= 2.00.
    public int secureVersion;   // 0 - Pre 2.00 (no encrypted files) / 1 - Post 2.00 (encrypted files are now used).
    public int multiStatus;     // After 2.00, several modes can be triggered at the same time using this for sync.
        public final static int MULTI_STATUS_SINGLE = 0;  // Save data is all generated in one call.
		public final static int MULTI_STATUS_INIT = 1;	  // Save data is generated in multiple calls and this is the first one.
		public final static int MULTI_STATUS_RELAY = 2;	  // Save data is generated in multiple calls and this is an intermediate call.
		public final static int MULTI_STATUS_FINISH = 3;  // Save data is generated in multiple calls and this is the last one.
	public int idListAddr;      // Address of a buffer to hold the file IDs generated by MODE_LIST.
    public int fileListAddr;    // Address of a buffer to hold the file names generated by MODE_FILES.
    public int sizeAddr;        // Address of a buffer to hold the sizes generated by MODE_GETSIZE.

	public static class PspUtilitySavedataSFOParam extends pspAbstractMemoryMappedStructure {
        public String title;
		public String savedataTitle;
		public String detail;
		public int parentalLevel;

		@Override
		protected void read() {
			title = readStringNZ(0x80);
			savedataTitle = readStringNZ(0x80);
			detail = readStringNZ(0x400);
			parentalLevel = read32();
		}

		@Override
		protected void write() {
		    writeStringNZ(0x80, title);
            writeStringNZ(0x80, savedataTitle);
            writeStringNZ(0x400, detail);
            write32(parentalLevel);
		}

		@Override
		public int sizeof() {
			return 0x80 + 0x80 + 0x400 + 4;
		}
	};

	public static class PspUtilitySavedataFileData extends pspAbstractMemoryMappedStructure {
		public int buf;
		public int bufSize;
		public int size;

		@Override
		protected void read() {
			buf     = read32();
			bufSize = read32();
			size    = read32();
			readUnknown(4);
		}

		@Override
		protected void write() {
			write32(buf);
			write32(bufSize);
			write32(size);
			writeUnknown(4);
		}

		@Override
		public int sizeof() {
			return 4 * 4;
		}
	}

	public static class PspUtilitySavedataListSaveNewData extends pspAbstractMemoryMappedStructure {
		public PspUtilitySavedataFileData icon0;
		public int titleAddr;
		public String title;

		@Override
		protected void read() {
			icon0 = new PspUtilitySavedataFileData();
			read(icon0);
			titleAddr = read32();
			if (titleAddr != 0) {
				title = Utilities.readStringZ(mem, titleAddr);
			} else {
				title = null;
			}
		}

		@Override
		protected void write() {
			write(icon0);
			write32(titleAddr);
			if (titleAddr != 0) {
				Utilities.writeStringZ(mem, titleAddr, title);
			}
		}

		@Override
		public int sizeof() {
			return icon0.sizeof() + 4;
		}
	}

	@Override
	protected void read() {
		base = new pspUtilityDialogCommon();
		read(base);
		setMaxSize(base.size);

		mode        = read32();
		bind        = read32();
		overwrite   = read32() == 0 ? false : true;
		gameName    = readStringNZ(13);
		readUnknown(3);
		saveName    = readStringNZ(20);
		saveNameListAddr = read32();
		if (saveNameListAddr != 0) {
			List<String> newSaveNameList = new ArrayList<String>();
			boolean endOfList = false;
			for (int i = 0; !endOfList; i += 20) {
				String saveNameItem = Utilities.readStringNZ(mem, saveNameListAddr + i, 20);
				if (saveNameItem == null || saveNameItem.length() == 0) {
					endOfList = true;
				} else {
					newSaveNameList.add(saveNameItem);
				}
			}
			saveNameList = newSaveNameList.toArray(new String[newSaveNameList.size()]);
		}
		fileName    = readStringNZ(13);
		readUnknown(3);
		dataBuf     = read32();
		dataBufSize = read32();
		dataSize    = read32();

		sfoParam = new PspUtilitySavedataSFOParam();
		read(sfoParam);
		icon0FileData = new PspUtilitySavedataFileData();
		read(icon0FileData);
		icon1FileData = new PspUtilitySavedataFileData();
		read(icon1FileData);
		pic1FileData = new PspUtilitySavedataFileData();
		read(pic1FileData);
		snd0FileData = new PspUtilitySavedataFileData();
		read(snd0FileData);

		newDataAddr = read32();
		if (newDataAddr != 0) {
			newData = new PspUtilitySavedataListSaveNewData();
			newData.read(mem, newDataAddr);
		} else {
			newData = null;
		}
        focus = read32();
		abortStatus = read32();
		msFreeAddr = read32();
        msDataAddr = read32();
        utilityDataAddr = read32();
		key = readStringNZ(16);
		secureVersion = read32();
        multiStatus = read32();
		idListAddr = read32();
        fileListAddr = read32();
        sizeAddr = read32();
	}

	@Override
	protected void write() {
        setMaxSize(base.size);
	    write(base);

		write32(mode);
        write32(bind);
		write32(overwrite ? 1 : 0);
		writeStringNZ(13, gameName);
		writeUnknown(3);
		writeStringNZ(20, saveName);
		write32(saveNameListAddr);
		writeStringNZ(13, fileName);
		writeUnknown(3);
		write32(dataBuf);
		write32(dataBufSize);
		write32(dataSize);

		write(sfoParam);
		write(icon0FileData);
        write(icon1FileData);
        write(pic1FileData);
        write(snd0FileData);

		write32(newDataAddr);
		if (newDataAddr != 0) {
			newData.write(mem, newDataAddr);
		}
		write32(focus);
		write32(abortStatus);
		write32(msFreeAddr);
        write32(msDataAddr);
        write32(utilityDataAddr);
		writeStringNZ(16, key);
		write32(secureVersion);
        write32(multiStatus);
		write32(idListAddr);
        write32(fileListAddr);
        write32(sizeAddr);
	}

    private String getBasePath() {
        return getBasePath(saveName);
    }

    public String getBasePath(String saveName) {
        String path = savedataPath + gameName;
        if (!saveName.equals("<>")) {
            path += saveName;
        }
        path += "/";
        return path;
    }

	public String getFileName(String saveName, String fileName) {
		return getBasePath(saveName) + fileName;
	}

    private int getFileSize(String fileName) {
		int size = 0;

		if (fileName != null && fileName.length() > 0) {
		    SceIoStat fileStat = Modules.IoFileMgrForUserModule.statFile(getFileName(saveName, fileName));
		    if (fileStat != null) {
		    	size = (int) fileStat.size;
		    }
		}

        return size;
	}

    public int getSizeKb(String gameName, String saveName) {
		int sizeKb;

		sizeKb  = Utilities.getSizeKb(getFileSize(fileName));
		sizeKb += Utilities.getSizeKb(getFileSize(icon0FileName));
		sizeKb += Utilities.getSizeKb(getFileSize(icon1FileName));
		sizeKb += Utilities.getSizeKb(getFileSize(pic1FileName));
		sizeKb += Utilities.getSizeKb(getFileSize(snd0FileName));
		sizeKb += Utilities.getSizeKb(getFileSize(paramSfoFileName));

        return sizeKb;
	}

	private SeekableDataInput getDataInput(String path, String name) {
		SeekableDataInput fileInput = Modules.IoFileMgrForUserModule.getFile(path + name, jpcsp.HLE.modules150.IoFileMgrForUser.PSP_O_RDONLY);

		return fileInput;
	}

	private SeekableRandomFile getDataOutput(String path, String name) {
		SeekableDataInput fileInput = Modules.IoFileMgrForUserModule.getFile(path + name, jpcsp.HLE.modules150.IoFileMgrForUser.PSP_O_RDWR | jpcsp.HLE.modules150.IoFileMgrForUser.PSP_O_CREAT);

		if (fileInput instanceof SeekableRandomFile) {
			return (SeekableRandomFile) fileInput;
		}

		return null;
	}

    public void singleRead(Memory mem) throws IOException {
        String path = getBasePath();
        dataSize = loadFile(mem, path, fileName, dataBuf, dataBufSize);
    }

    public void singleWrite(Memory mem) throws IOException {
        String path = getBasePath();
        Modules.IoFileMgrForUserModule.mkdirs(path);
        writeFile(mem, path, fileName, dataBuf, dataSize);
    }

    public void load(Memory mem) throws IOException {
		String path = getBasePath();

		dataSize = loadFile(mem, path, fileName, dataBuf, dataBufSize);
        safeLoad(mem, icon0FileName, icon0FileData);
        safeLoad(mem, icon1FileName, icon1FileData);
        safeLoad(mem, pic1FileName, pic1FileData);
        safeLoad(mem, snd0FileName, snd0FileData);
		loadPsf(mem, path, paramSfoFileName, sfoParam);
        bind = BIND_IS_OK;
	}

    private void safeLoad(Memory mem, String filename, PspUtilitySavedataFileData fileData) throws IOException {
		String path = getBasePath();

        try {
            fileData.size = loadFile(mem, path, filename, fileData.buf, fileData.bufSize);
        } catch(FileNotFoundException e) {
            // ignore
        }
    }

    public void save(Memory mem) throws IOException {
		String path = getBasePath();

		Modules.IoFileMgrForUserModule.mkdirs(path);
		writeFile(mem, path, fileName,      dataBuf,           dataSize);
		writeFile(mem, path, icon0FileName, icon0FileData.buf, icon0FileData.size);
		writeFile(mem, path, icon1FileName, icon1FileData.buf, icon1FileData.size);
		writeFile(mem, path, pic1FileName,  pic1FileData.buf,  pic1FileData.size);
		writeFile(mem, path, snd0FileName,  snd0FileData.buf,  snd0FileData.size);
		writePsf(mem, path, paramSfoFileName, sfoParam);
	}

    private int loadFile(Memory mem, String path, String name, int address, int maxLength) throws IOException {
		if (name == null || name.length() <= 0 || address == 0 || maxLength <= 0) {
			return 0;
		}

		SeekableDataInput fileInput = getDataInput(path, name);
		if (fileInput == null) {
			throw new FileNotFoundException("File not found '" + path + "' '" + name + "'");
		}

		int fileSize = (int) fileInput.length();
		if (fileSize > maxLength) {
			fileSize = maxLength;
		}

		Utilities.readFully(fileInput, address, fileSize);
		fileInput.close();

		return fileSize;
	}

	private void writeFile(Memory mem, String path, String name, int address, int length) throws IOException {
		if (name == null || name.length() <= 0 || address == 0) {
			return;
		}

		SeekableRandomFile fileOutput = getDataOutput(path, name);
		if (fileOutput == null) {
			return;
		}

        Utilities.write(fileOutput, address, length);
		fileOutput.close();
	}

    private void loadPsf(Memory mem, String path, String name, PspUtilitySavedataSFOParam sfoParam) throws IOException {
        SeekableDataInput fileInput = getDataInput(path, name);
        if (fileInput != null) {
            byte[] buffer = new byte[(int) fileInput.length()];
            fileInput.readFully(buffer);
            fileInput.close();

            PSF psf = new PSF();
            psf.read(ByteBuffer.wrap(buffer));

            sfoParam.parentalLevel = psf.getNumeric("PARENTAL_LEVEL");
            sfoParam.title = psf.getString("TITLE");
            sfoParam.detail = psf.getString("SAVEDATA_DETAIL");
            sfoParam.savedataTitle = psf.getString("SAVEDATA_TITLE");
        }
    }

	private void writePsf(Memory mem, String path, String name, PspUtilitySavedataSFOParam sfoParam) throws IOException {
        SeekableRandomFile fileOutput = getDataOutput(path, name);
		if (fileOutput == null) {
			return;
		}

		PSF psf = new PSF();
        psf.put("PARENTAL_LEVEL", sfoParam.parentalLevel);
        psf.put("TITLE", sfoParam.title, 128);
        psf.put("SAVEDATA_DETAIL", sfoParam.detail, 1024);
        psf.put("SAVEDATA_TITLE", sfoParam.savedataTitle, 128);

        psf.write(fileOutput.getChannel().map(MapMode.READ_WRITE, 0, psf.size()));
	}

	public boolean test(Memory mem) throws IOException {
		String path = getBasePath();

		boolean result = testFile(mem, path, fileName);

		return result;
	}

    private boolean testFile(Memory mem, String path, String name) throws IOException {
		if (name == null || name.length() <= 0) {
			return false;
		}

		SeekableDataInput fileInput = getDataInput(path, name);
		if (fileInput == null) {
			throw new FileNotFoundException("File not found '" + path + "' '" + name + "'");
		}

		fileInput.close();

		return true;
	}

	public boolean isPresent(String gameName, String saveName) {
	    String path = getBasePath();
        // NULL can also be sent in saveName (seen in MODE_SIZES).
        // It means any save from the current game, since all saves share a common
        // save data file.
        if(saveName == null || saveName.length() <= 0) {
            File f = new File("ms0/PSP/SAVEDATA/");
            if(f.list() == null) {
                return false;
            }
            for(int i = 0; i < f.list().length; i++) {
                if(!f.list()[i].equals(gameName) && f.list()[i].contains(gameName)) {
                    saveName = f.list()[i].replace(gameName, "");
                    break;
                }
            }
            path = getBasePath(saveName);
        }
        // When NULL is sent in fileName, it means any file inside the savedata folder.
        if (fileName == null || fileName.length() <= 0) {
            File f = new File("ms0/PSP/SAVEDATA/" + gameName + saveName);
            if(f.list() == null) {
                return false;
            }
            return true;
	    }
	    try {
            SeekableDataInput fileInput = getDataInput(path, fileName);
            if (fileInput != null) {
                fileInput.close();
                return true;
            }
	    } catch (IOException e) {
	    }

        return false;
	}

	public boolean isPresent() {
		return isPresent(gameName, saveName);
	}

    @Override
	public int sizeof() {
        return base.size;
    }

	@Override
	public String toString() {
		return String.format("Address 0x%08X, mode=%d, gameName=%s, saveName=%s, fileName=%s",
				getBaseAddress(), mode, gameName, saveName, fileName);
	}
}