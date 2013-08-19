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
package jpcsp.format;

import static jpcsp.util.Utilities.formatString;
import static jpcsp.util.Utilities.readUByte;
import static jpcsp.util.Utilities.readUHalf;
import static jpcsp.util.Utilities.readUWord;

import java.io.IOException;
import java.nio.ByteBuffer;

import jpcsp.Memory;

public class Elf32StubHeader {
    // Resolved version of s_modulename in a Java String
    private String s_modulenamez;

    private int s_modulename;
    private int s_version;
    private int s_flags;
    private int s_size;
    private int s_vstub_size;
    private int s_imports;
    private int s_nid;
    private int s_text;
    private int s_vstub;

    public static int sizeof() {
    	return 20;
    }

    public Elf32StubHeader(ByteBuffer f) throws IOException {
        s_modulenamez = "";

        s_modulename = readUWord(f);
        s_version = readUHalf(f);
        s_flags = readUHalf(f);
        s_size = readUByte(f);
        s_vstub_size = readUByte(f);
        s_imports = readUHalf(f);
        s_nid = readUWord(f);
        s_text = readUWord(f);
        if (hasVStub()) {
        	s_vstub = readUWord(f);
        }
    }

    public Elf32StubHeader(Memory mem, int address) {
        s_modulenamez = "";

        s_modulename = mem.read32(address);
        s_version = mem.read16(address + 4);
        s_flags = mem.read16(address + 6);
        s_size = mem.read8(address + 8);
        s_vstub_size = mem.read8(address + 9);
        s_imports = mem.read16(address + 10);
        s_nid = mem.read32(address + 12);
        s_text = mem.read32(address + 16);
        if (hasVStub()) {
        	s_vstub = mem.read32(address + 20);
        }
    }

    @Override
	public String toString() {
        StringBuilder str = new StringBuilder();
        if (s_modulenamez != null && s_modulenamez.length() > 0)
            str.append(s_modulenamez + "\n");
        str.append("s_modulename" + "\t" +  formatString("long", Long.toHexString(s_modulename & 0xFFFFFFFFL).toUpperCase()) + "\n");
        str.append("s_version" + "\t\t" +  formatString("short", Long.toHexString(s_version & 0xFFFF).toUpperCase()) + "\n");
        str.append("s_flags" + "\t\t\t" +  formatString("short", Long.toHexString(s_flags & 0xFFFF).toUpperCase()) + "\n");
        str.append("s_size" + "\t\t\t" +  formatString("short", Long.toHexString(s_size & 0xFFFF).toUpperCase()) + "\n");
        str.append("s_imports" + "\t\t" +  formatString("short", Long.toHexString(s_imports & 0xFFFF).toUpperCase()) + "\n");
        str.append("s_nid" + "\t\t\t" +  formatString("long", Long.toHexString(s_nid & 0xFFFFFFFFL).toUpperCase()) + "\n");
        str.append("s_text" + "\t\t\t" +  formatString("long", Long.toHexString(s_text & 0xFFFFFFFFL).toUpperCase()) + "\n");
        if (hasVStub()) {
            str.append("s_vstub" + "\t\t\t" +  formatString("long", Long.toHexString(s_vstub & 0xFFFFFFFFL).toUpperCase()) + "\n");
        }
        return str.toString();
    }

    public String getModuleNamez() {
        return s_modulenamez;
    }

    public void setModuleNamez(String moduleName) {
        s_modulenamez = moduleName;
    }

    public int getOffsetModuleName() {
        return s_modulename;
    }

    public int getVersion() {
        return s_version;
    }

    public int getFlags() {
        return s_flags;
    }

    public int getSize() {
        return s_size;
    }

    public int getVStubSize() {
    	return s_vstub_size;
    }

    /** The number of imports from this module */
    public int getImports() {
        return s_imports;
    }

    public int getOffsetNid() {
        return s_nid;
    }

    public int getOffsetText() {
        return s_text;
    }

    public int getVStub() {
    	return s_vstub;
    }

    public boolean hasVStub() {
    	return s_size >= 6;
    }
}
