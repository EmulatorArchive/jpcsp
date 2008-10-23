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

import jpcsp.Memory;

/** http://psp.jim.sh/pspsdk-doc/structSceIoStat.html */
public class SceIoStat {
    public int mode; // permissions?
    public int attr; // ?
    public long size;
    public ScePspDateTime ctime;
    public ScePspDateTime atime;
    public ScePspDateTime mtime;

    public SceIoStat(int mode, int attr, long size,
        ScePspDateTime ctime, ScePspDateTime atime, ScePspDateTime mtime) {
        this.mode = mode;
        this.attr = attr;
        this.size = size;
        this.ctime = ctime;
        this.atime = atime;
        this.mtime = mtime;
    }

    public void write(Memory mem, int address) {
        mem.write32(address, mode);
        mem.write32(address + 4, attr);
        mem.write64(address + 8, size);

        ctime.write(mem, address + 16);
        atime.write(mem, address + 32);
        mtime.write(mem, address + 48);

        // 6 ints reserved
        mem.write32(address + 64, 0x12121212); // TODO "LBA" "sce_lbn"
        mem.write32(address + 68, 0x34343434);
        mem.write32(address + 72, 0x56565656);
        mem.write32(address + 76, 0x78787878);
        mem.write32(address + 80, 0x9a9a9a9a);
        mem.write32(address + 84, 0xbcbcbcbc);
    }

    public static int sizeof() {
        return 16 + ScePspDateTime.sizeof() * 3 + 24;
    }
}
