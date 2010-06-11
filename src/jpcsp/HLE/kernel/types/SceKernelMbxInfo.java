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

import jpcsp.HLE.kernel.managers.SceUidManager;
import jpcsp.Memory;
import jpcsp.util.Utilities;

public class SceKernelMbxInfo {
    //Mbx info
    public int size;
    public String name;
    public int attr;
    public int numWaitThreads;
    public int numMessages;
    public int firstMessage_addr;

    //Mbx message info
    //Header (SceKernelMsgPacket in pspsdk seems to be wrong)
    public int unk;
    public int msgTextAddr;

    //Message
    public String msgText;

    // Internal info
    public final int uid;
    public boolean hasNewMessage = false;

    public SceKernelMbxInfo(String name, int attr) {
        this.name = name;
        this.attr = attr;

        this.numWaitThreads = 0;
        this.numMessages = 0;
        this.firstMessage_addr = 0;

        this.uid = SceUidManager.getNewUid("ThreadMan-Mbx");
    }

    public void read(Memory mem, int address) {
        size                    = mem.read32(address);
        name                    = Utilities.readStringNZ(mem, address + 4, 32);
        attr                    = mem.read32(address + 36);
        numWaitThreads          = mem.read32(address + 40);
        numMessages             = mem.read32(address + 44);
        firstMessage_addr       = mem.read32(address + 48);
    }

    public void write(Memory mem, int address) {
        mem.write32(address, size);
        Utilities.writeStringNZ(mem, address + 4, 32, name);
        mem.write32(address + 36, attr);
        mem.write32(address + 40, numWaitThreads);
        mem.write32(address + 44, numMessages);
        mem.write32(address + 48, firstMessage_addr);
    }

    public void storeMsg(Memory mem, int address) {
        unk             = mem.read32(address);
        msgTextAddr     = mem.read32(address + 4);

        if(msgTextAddr != 0)
            msgText         = Utilities.readStringNZ(msgTextAddr, 32);
        else
            msgText = "";

        hasNewMessage = true;
    }

    public boolean checkMbxNewMessage() {
        if(hasNewMessage) {
            hasNewMessage = false;
            return true;
        }
        return false;
    }
}