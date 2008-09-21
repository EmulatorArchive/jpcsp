/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jpcsp.Allegrex;

/**
 * Load Store Unit, handles memory operations.
 *
 * @author hli
 */
public class LsuState extends MduState {

    public static final jpcsp.Memory memory = jpcsp.Memory.getInstance();
    
    @Override
    public void reset() {
    }

    public LsuState() {
        reset();
    }

    public void copy(LsuState that) {
        super.copy(that);
    }

    public LsuState(LsuState that) {
        this.copy(that);
    }
    
    public void doLB(int rt, int rs, int simm16) {
        int word = (memory.read8(gpr[rs] + simm16) << 24) >> 24;
        if (rt != 0) {
            gpr[rt] = word;
        }
    }

    public void doLBU(int rt, int rs, int simm16) {
        int word = memory.read8(gpr[rs] + simm16) & 0xff;
        if (rt != 0) {
            gpr[rt] = word;
        }
    }

    public void doLH(int rt, int rs, int simm16) {
        int word = (memory.read16(gpr[rs] + simm16) << 16) >> 16;
        if (rt != 0) {
            gpr[rt] = word;
        }
    }

    public void doLHU(int rt, int rs, int simm16) {
        int word = memory.read16(gpr[rs] + simm16) & 0xffff;
        if (rt != 0) {
            gpr[rt] = word;
        }
    }

    public void doLWL(int rt, int rs, int simm16) {
        int address = gpr[rs] + simm16;
        int offset = address & 0x3;
        int reg = gpr[rt];

        int word = memory.read32(address & 0xfffffffc);

        switch (offset) {
            case 0:
                word = ((word & 0xff) << 24) | (reg & 0xffffff);
                break;

            case 1:
                word = ((word & 0xffff) << 16) | (reg & 0xffff);
                break;

            case 2:
                word = ((word & 0xffffff) << 8) | (reg & 0xff);
                break;

            case 3:
                break;
        }

        if (rt != 0) {
            gpr[rt] = word;
        }
    }

    public void doLW(int rt, int rs, int simm16) {
        int word = memory.read32(gpr[rs] + simm16);
        if (rt != 0) {
            gpr[rt] = word;
        }
    }

    public void doLWR(int rt, int rs, int simm16) {
        int address = gpr[rs] + simm16;
        int offset = address & 0x3;
        int reg = gpr[rt];

        int word = memory.read32(address & 0xfffffffc);

        switch (offset) {
            case 0:
                break;

            case 1:
                word = (reg & 0xff000000) | ((word & 0xffffff00) >> 8);
                break;

            case 2:
                word = (reg & 0xffff0000) | ((word & 0xffff0000) >> 16);
                break;

            case 3:
                word = (reg & 0xffffff00) | ((word & 0xff000000) >> 24);
                break;
        }

        if (rt != 0) {
            gpr[rt] = word;
        }
    }

    public void doSB(int rt, int rs, int simm16) {
        memory.write8(gpr[rs] + simm16, (byte) (gpr[rt] & 0xFF));
    }

    public void doSH(int rt, int rs, int simm16) {
        memory.write16(gpr[rs] + simm16, (short) (gpr[rt] & 0xFFFF));
    }

    public void doSWL(int rt, int rs, int simm16) {
        int address = gpr[rs] + simm16;
        int offset = address & 0x3;
        int reg = gpr[rt];
        int data = memory.read32(address & 0xfffffffc);

        switch (offset) {
            case 0:
                data = (data & 0xffffff00) | (reg >> 24 & 0xff);
                break;

            case 1:
                data = (data & 0xffff0000) | (reg >> 16 & 0xffff);
                break;

            case 2:
                data = (data & 0xff000000) | (reg >> 8 & 0xffffff);
                break;

            case 3:
                data = reg;
                break;
        }

        memory.write32(address & 0xfffffffc, data);
    }

    public void doSW(int rt, int rs, int simm16) {
        memory.write32(gpr[rs] + simm16, gpr[rt]);
    }

    public void doSWR(int rt, int rs, int simm16) {
        int address = gpr[rs] + simm16;
        int offset = address & 0x3;
        int reg = gpr[rt];
        int data = memory.read32(address & 0xfffffffc);

        switch (offset) {
            case 0:
                data = reg;
                break;

            case 1:
                data = ((reg << 8) & 0xffffff00) | (data & 0xff);
                break;

            case 2:
                data = ((reg << 16) & 0xffff0000) | (data & 0xffff);
                break;

            case 3:
                data = ((reg << 24) & 0xff000000) | (data & 0xffffff);
                break;
        }

        memory.write32(address & 0xfffffffc, data);
    }

    public void doLL(int rt, int rs, int simm16) {
        int word = memory.read32(gpr[rs] + simm16);
        if (rt != 0) {
            gpr[rt] = word;
        }
        //ll_bit = 1;
    }

    public void doSC(int rt, int rs, int simm16) {
        memory.write32(gpr[rs] + simm16, gpr[rt]);
        if (rt != 0) {
            gpr[rt] = 1; // = ll_bit;
        }
    }    
}
