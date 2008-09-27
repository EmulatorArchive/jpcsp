/*
Function:
- HLE everything in http://psp.jim.sh/pspsdk-doc/pspctrl_8h.html


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
import jpcsp.Memory;

public class pspctrl {
    private static pspctrl instance;

    private int cycle;
    private int mode;
    private int uiMake;
    private int uiBreak;
    private int uiPress;
    private int uiRelease;

    private int TimeStamp;
    private byte Lx;
    private byte Ly;
    private int Buttons;

    public final static int PSP_CTRL_SELECT = 0x000001;
    public final static int PSP_CTRL_START = 0x000008;
    public final static int PSP_CTRL_UP = 0x000010;
    public final static int PSP_CTRL_RIGHT = 0x000020;
    public final static int PSP_CTRL_DOWN = 0x000040;
    public final static int PSP_CTRL_LEFT = 0x000080;
    public final static int PSP_CTRL_LTRIGGER = 0x000100;
    public final static int PSP_CTRL_RTRIGGER = 0x000200;
    public final static int PSP_CTRL_TRIANGLE = 0x001000;
    public final static int PSP_CTRL_CIRCLE = 0x002000;
    public final static int PSP_CTRL_CROSS = 0x004000;
    public final static int PSP_CTRL_SQUARE = 0x008000;
    public final static int PSP_CTRL_HOME = 0x010000;
    public final static int PSP_CTRL_HOLD = 0x020000;
    public final static int PSP_CTRL_NOTE = 0x800000;
    public final static int PSP_CTRL_SCREEN = 0x400000;
    public final static int PSP_CTRL_VOLUP = 0x100000;
    public final static int PSP_CTRL_VOLDOWN = 0x200000;
    public final static int PSP_CTRL_WLAN_UP = 0x040000;
    public final static int PSP_CTRL_REMOTE = 0x080000;
    public final static int PSP_CTRL_DISC = 0x1000000;
    public final static int PSP_CTRL_MS = 0x2000000;

    // PspCtrlMode
    public final static int PSP_CTRL_MODE_DIGITAL = 0;
    public final static int PSP_CTRL_MODE_ANALOG = 1;

    public static pspctrl get_instance() {
        if (instance == null) {
            instance = new pspctrl();
        }
        return instance;
    }

    private pspctrl() {
    }

    /** Need to call setButtons even if the user didn't move any fingers, otherwise we can't track "press" properly */
    public void setButtons(byte Lx, byte Ly, int Buttons)
    {
        int oldButtons = this.Buttons;

        this.TimeStamp++;
        this.Lx = Lx;
        this.Ly = Ly;
        this.Buttons = Buttons;

        int changed = oldButtons ^ Buttons;
        int changed2 = oldButtons & Buttons;

        /* testing
        if ((changed2 & PSP_CTRL_CROSS) == PSP_CTRL_CROSS)
            System.out.println("PSP_CTRL_CROSS press");
        else
            System.out.println("PSP_CTRL_CROSS release");

        if ((changed & PSP_CTRL_CROSS) == PSP_CTRL_CROSS &&
            (oldButtons & PSP_CTRL_CROSS) == PSP_CTRL_CROSS)
            System.out.println("PSP_CTRL_CROSS break");

        if ((changed & PSP_CTRL_CROSS) == PSP_CTRL_CROSS &&
            (Buttons & PSP_CTRL_CROSS) == PSP_CTRL_CROSS)
            System.out.println("PSP_CTRL_CROSS make");
        /* */

        uiMake = changed & Buttons;
        uiBreak = changed & oldButtons;
        uiPress = changed2;
        uiRelease = ~changed2;
    }

    public boolean isModeDigital() {
        if (mode == 0)
            return true;
        return false;
    }

    public void sceCtrlSetSamplingCycle(int a0)
    {
        Emulator.getProcessor().cpu.gpr[2] = cycle;
        cycle = a0;
    }

    public void sceCtrlGetSamplingCycle(int a0)
    {
        Memory.getInstance().write32(a0, cycle);
        Emulator.getProcessor().cpu.gpr[2] = 0;
    }

    public void sceCtrlSetSamplingMode(int a0)
    {
        Emulator.getProcessor().cpu.gpr[2] = mode;
        mode = a0;
    }

    public void sceCtrlGetSamplingMode(int a0)
    {
        Memory.getInstance().write32(a0, mode);
        Emulator.getProcessor().cpu.gpr[2] = 0;
    }

    public void sceCtrlPeekBufferPositive(int a0, int a1)
    {
        Memory mem = Memory.getInstance();
        int i;

        for (i = 0; i < a1; i++) {
            mem.write32(a0, TimeStamp);
            mem.write32(a0 + 4, Buttons);
            mem.write8(a0 + 8, Lx);
            mem.write8(a0 + 9, Ly);
            a0 += 16;
        }

        Emulator.getProcessor().cpu.gpr[2] = i;
    }

    public void sceCtrlPeekBufferNegative(int a0, int a1)
    {
        Memory mem = Memory.getInstance();
        int i;

        for (i = 0; i < a1; i++) {
            mem.write32(a0, TimeStamp);
            mem.write32(a0 + 4, ~Buttons);
            mem.write8(a0 + 8, Lx);
            mem.write8(a0 + 9, Ly);
            a0 += 16;
        }

        Emulator.getProcessor().cpu.gpr[2] = i;
    }

    public void sceCtrlReadBufferPositive(int a0, int a1)
    {
        Memory mem = Memory.getInstance();
        int i;

        for (i = 0; i < a1; i++) {
            mem.write32(a0, TimeStamp);
            mem.write32(a0 + 4, Buttons);
            mem.write8(a0 + 8, Lx);
            mem.write8(a0 + 9, Ly);
            a0 += 16;
        }

        Emulator.getProcessor().cpu.gpr[2] = i;
        ThreadMan.get_instance().yieldCurrentThread();
    }

    public void sceCtrlReadBufferNegative(int a0, int a1)
    {
        Memory mem = Memory.getInstance();
        int i;

        for (i = 0; i < a1; i++) {
            mem.write32(a0, TimeStamp);
            mem.write32(a0 + 4, ~Buttons);
            mem.write8(a0 + 8, Lx);
            mem.write8(a0 + 9, Ly);
            a0 += 16;
        }

        Emulator.getProcessor().cpu.gpr[2] = i;
        ThreadMan.get_instance().yieldCurrentThread();
    }

    public void sceCtrlPeekLatch(int a0) {
        Memory mem = Memory.getInstance();

        mem.write32(a0, uiMake);
        mem.write32(a0 +4, uiBreak);
        mem.write32(a0 +8, uiPress);
        mem.write32(a0 +12, uiRelease);
        Emulator.getProcessor().cpu.gpr[2] = 0;
    }

    public void sceCtrlReadLatch(int a0) {
        Memory mem = Memory.getInstance();

        mem.write32(a0, uiMake);
        mem.write32(a0 +4, uiBreak);
        mem.write32(a0 +8, uiPress);
        mem.write32(a0 +12, uiRelease);
        Emulator.getProcessor().cpu.gpr[2] = 0;
        ThreadMan.get_instance().yieldCurrentThread();
    }
}
