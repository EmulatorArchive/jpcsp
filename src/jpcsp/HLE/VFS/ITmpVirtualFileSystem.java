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
package jpcsp.HLE.VFS;

import static java.io.File.separatorChar;
import static java.lang.Math.abs;

import jpcsp.State;
import jpcsp.HLE.Modules;
import jpcsp.HLE.kernel.types.SceIoStat;

public interface ITmpVirtualFileSystem extends IVirtualFileSystem {
	public static final IPurpose tmpPurposePGD = new PurposePGD();
	public static final IPurpose tmpPurposeAtrac = new PurposeAtrac();
	public IVirtualFile ioOpen(String fileName, int flags, int mode, IPurpose purpose);

	public static interface IPurpose {
		public String getFileName(String fileName);
	}

	public static class PurposePGD implements IPurpose {
		@Override
		public String getFileName(String fileName) {
			int fileId = 0;

			SceIoStat stat = Modules.IoFileMgrForUserModule.statFile(fileName);
			if (stat != null) {
				// Use the UMD start sector as file ID.
				fileId = stat.getStartSector();
			}

			if (fileId == 0 && fileName != null) {
				// If the file is not stored on the UMD (e.g. stored on ms0:),
				// use a unique ID based on the file name as file ID.
				fileId = abs(VirtualFileSystemManager.getFileNameLastPart(fileName).hashCode());
			}

			return String.format("%s%cPGD%cFile-%d%cPGDfile.raw.decrypted", State.discId, separatorChar, separatorChar, fileId, separatorChar);
		}
	}

	public static class PurposeAtrac implements IPurpose {
		@Override
		public String getFileName(String fileName) {
			return String.format("%s%cAtrac%c%s", State.discId, separatorChar, separatorChar, fileName);
		}
	}
}
