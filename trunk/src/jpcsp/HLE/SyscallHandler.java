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
package jpcsp.HLE;

import jpcsp.Emulator;
import jpcsp.GeneralJpcspException;

public class SyscallHandler {

    // Change this to return the number of cycles used?
    public static void syscall(int code) {
        int gpr[] = Emulator.getProcessor().gpr;

        // Some syscalls implementation throw GeneralJpcspException,
        // and Processor isn't setup to catch exceptions so we'll do it
        // here for now, or we could just stop throwing exceptions.
        // Also we need to decide whether to pass arguments to the functions,
        // or let them read the registers they want themselves.
        try {
            // Currently using FW1.50 codes
            switch(code) {
                case 0x201c:
                    ThreadMan.get_instance().ThreadMan_sceKernelDelayThread(gpr[4]);
                    break;
                case 0x206d:
                    ThreadMan.get_instance().ThreadMan_sceKernelCreateThread(gpr[4], gpr[5], gpr[6], gpr[7], gpr[8], gpr[9]);
                    break;
                case 0x206e:
                    ThreadMan.get_instance().ThreadMan_sceKernelDeleteThread(gpr[4]);
                    break;
                case 0x206f:
                    ThreadMan.get_instance().ThreadMan_sceKernelStartThread(gpr[4], gpr[5], gpr[6]);
                    break;
                case 0x2070:
                case 0x2071:
                    ThreadMan.get_instance().ThreadMan_sceKernelExitThread(gpr[4]);
                    break;
                case 0x2073:
                    ThreadMan.get_instance().ThreadMan_sceKernelTerminateThread(gpr[4]);
                    break;

                case 0x20bf:
                    Utils.get_instance().Utils_sceKernelUtilsMt19937Init(gpr[4], gpr[5]);
                    break;
                case 0x20c0:
                    Utils.get_instance().Utils_sceKernelUtilsMt19937UInt(gpr[4]);
                    break;

                /* TODO (for minifire)
                case 0x20eb:
                    LoadExec.get_instance().LoadExec_sceKernelExitGame();
                    break;

                case 0x213a:
                    pspdisplay.get_instance().pspdisplay_sceDisplaySetMode(gpr[4], gpr[5], gpr[6]);
                    break;
                case 0x213f:
                    pspdisplay.get_instance().pspdisplay_sceDisplaySetFrameBuf(gpr[4], gpr[5], gpr[6], gpr[7]);
                    break;
                case 0x2147:
                    pspdisplay.get_instance().pspdisplay_sceDisplayWaitVblankStart();
                    break;
                */

                default:
                    System.out.println("Unsupported syscall " + Integer.toHexString(code));
                    break;
            }
        } catch(GeneralJpcspException e) {
            System.out.println(e.getMessage());
        }
    }
}
