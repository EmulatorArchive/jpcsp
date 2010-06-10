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

public class SceUtilityGameSharingParams extends pspAbstractMemoryMappedStructure {
    public pspUtilityDialogCommon base;
    public String gameSharingName;
    public int result;
    public String gameSharingFilepath;   // File path to the game's EBOOT.BIN.
    public int gameSharingMode;          // GameSharing mode: 1 - Single send; 2 - Multiple sends (up to 4).
    public int gameSharingDataType;      // GameSharing data type: 1 - game's EBOOT is a file; 2 - game's EBOOT is in memory.
    public int gameSharingDataAddr;      // Pointer to EBOOT.BIN data.
    public int gameSharingDataSize;      // EBOOT.BIN data's size.

    @Override
    protected void read() {
        base = new pspUtilityDialogCommon();
        read(base);
        setMaxSize(base.size);

        readUnknown(8);
        gameSharingName = readStringNZ(8);
        readUnknown(12);
        result = read32();
        gameSharingFilepath = readStringNZ(32);
        gameSharingMode = read32();
        gameSharingDataType = read32();
        gameSharingDataAddr = read32();
        gameSharingDataSize = read32();
    }

    @Override
    protected void write() {
        setMaxSize(base.size);
        write(base);

        writeUnknown(8);
        writeStringNZ(8, gameSharingName);
        writeUnknown(12);
        write32(result);
        writeStringNZ(32, gameSharingFilepath);
        write32(gameSharingMode);
        write32(gameSharingDataType);
        write32(gameSharingDataAddr);
        write32(gameSharingDataSize);
    }

    @Override
    public int sizeof() {
        return base.size;
    }

    @Override
    public String toString() {
        return String.format("title=%s, EBOOTAddr=0x%08X, EBOOTSize=%i", gameSharingName, gameSharingDataAddr, gameSharingDataSize);
    }
}