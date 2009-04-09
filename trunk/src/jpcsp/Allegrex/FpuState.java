/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jpcsp.Allegrex;

import java.util.Arrays;

/**
 * Floating Point Unit, handles floating point operations, including BCU and LSU
 *
 * @author hli
 */
public class FpuState extends BcuState {

    public static final class Fcr0 {

        public static final int imp = 0; /* FPU design number */

        public static final int rev = 0; /* FPU revision bumber */

    }

    public class Fcr31 {

        public int rm;
        public boolean c;
        public boolean fs;

        public void reset() {
            rm = 0;
            c = false;
            fs = false;
        }

        public Fcr31() {
            reset();
        }

        public Fcr31(Fcr31 that) {
            rm = that.rm;
            c = that.c;
            fs = that.fs;
        }
    }
    public float[] fpr;
    public Fcr31 fcr31;

    @Override
    public void reset() {
        Arrays.fill(fpr, 0.0f);
        fcr31.reset();
    }

    @Override
    public void resetAll() {
        super.resetAll();
        Arrays.fill(fpr, 0.0f);
        fcr31.reset();
    }          
    
    public FpuState() {
        fpr = new float[32];
        fcr31 = new Fcr31();
    }

    public void copy(FpuState that) {
        super.copy(that);
        fpr = that.fpr.clone();
        fcr31 = new Fcr31(that.fcr31);
    }

    public FpuState(FpuState that) {
        super(that);
        fpr = that.fpr.clone();
        fcr31 = new Fcr31(that.fcr31);
    }
    
    public void doMFC1(int rt, int c1dr) {
        gpr[rt] = Float.floatToRawIntBits(fpr[c1dr]);
    }
    
    public void doCFC1(int rt, int c1cr) {
        if (rt != 0) {
            switch (c1cr) {
                case 0:
                    gpr[rt] = (Fcr0.imp << 8) | (Fcr0.rev);
                    break;

                case 31:
                    gpr[rt] = (fcr31.fs ? (1 << 24) : 0) | (fcr31.c ? (1 << 23) : 0) | (fcr31.rm & 3);
                    break;

                default:
                    doUNK("Unsupported cfc1 instruction for fcr" + Integer.toString(c1cr));
            }
        }
    }

    public void doMTC1(int rt, int c1dr) {
        fpr[c1dr] = Float.intBitsToFloat(gpr[rt]);
    }

    public void doCTC1(int rt, int c1cr) {
        switch (c1cr) {
            case 31:
                int bits = gpr[rt] & 0x01800003;
                fcr31.rm = bits & 3;
                bits >>= 23;
                fcr31.fs = (bits > 1);
                fcr31.c = (bits >> 1) == 1;
                break;

            default:
                doUNK("Unsupported ctc1 instruction for fcr" + Integer.toString(c1cr));
        }
    }

    public boolean doBC1F(int simm16) {
        npc = !fcr31.c ? branchTarget(pc, simm16) : (pc + 4);
        return true;
    }

    public boolean doBC1T(int simm16) {
        npc = fcr31.c ? branchTarget(pc, simm16) : (pc + 4);
        return true;
    }

    public boolean doBC1FL(int simm16) {
        if (!fcr31.c) {
            npc = branchTarget(pc, simm16);
            return true;
        } else {
            pc += 4;
        }
        return false;
    }

    public boolean doBC1TL(int simm16) {
        if (fcr31.c) {
            npc = branchTarget(pc, simm16);
            return true;
        } else {
            pc += 4;
        }
        return false;
    }

    public void doADDS(int fd, int fs, int ft) {
        fpr[fd] = fpr[fs] + fpr[ft];
    }

    public void doSUBS(int fd, int fs, int ft) {
        fpr[fd] = fpr[fs] - fpr[ft];
    }

    public void doMULS(int fd, int fs, int ft) {
        fpr[fd] = fpr[fs] * fpr[ft];
    }

    public void doDIVS(int fd, int fs, int ft) {
        fpr[fd] = fpr[fs] / fpr[ft];
    }

    public void doSQRTS(int fd, int fs) {
        fpr[fd] = (float) Math.sqrt(fpr[fs]);
    }

    public void doABSS(int fd, int fs) {
        fpr[fd] = Math.abs(fpr[fs]);
    }

    public void doMOVS(int fd, int fs) {
        fpr[fd] = fpr[fs];
    }

    public void doNEGS(int fd, int fs) {
        fpr[fd] = 0.0f - fpr[fs];
    }

    public void doROUNDWS(int fd, int fs) {
        fpr[fd] = Float.intBitsToFloat(Math.round(fpr[fs]));
    }

    public void doTRUNCWS(int fd, int fs) {
        fpr[fd] = Float.intBitsToFloat((int) (fpr[fs]));
    }

    public void doCEILWS(int fd, int fs) {
        fpr[fd] = Float.intBitsToFloat((int) Math.ceil(fpr[fs]));
    }

    public void doFLOORWS(int fd, int fs) {
        fpr[fd] = Float.intBitsToFloat((int) Math.floor(fpr[fs]));
    }

    public void doCVTSW(int fd, int fs) {
        fpr[fd] = (float) Float.floatToRawIntBits(fpr[fs]);
    }

    public void doCVTWS(int fd, int fs) {
        switch (fcr31.rm) {
            case 1:
                fpr[fd] = Float.intBitsToFloat((int) (fpr[fs]));
                break;
            case 2:
                fpr[fd] = Float.intBitsToFloat((int) Math.ceil(fpr[fs]));
                break;
            case 3:
                fpr[fd] = Float.intBitsToFloat((int) Math.floor(fpr[fs]));
                break;
            default:
                fpr[fd] = Float.intBitsToFloat((int) Math.rint(fpr[fs]));
                break;
        }
    }

    public void doCCONDS(int fs, int ft, int cond) {
        float x = fpr[fs];
        float y = fpr[ft];

        if (Float.isNaN(x) || Float.isNaN(y)) {
        	fcr31.c = (cond & 1) != 0;
        } else {
            boolean equal = ((cond & 2) != 0) && (x == y);
            boolean less = ((cond & 4) != 0) && (x < y);

            fcr31.c = less || equal;
        }
    }
    
    public void doLWC1(int ft, int rs, int simm16) {
        fpr[ft] = Float.intBitsToFloat(memory.read32(gpr[rs] + simm16));
    }

    public void doSWC1(int ft, int rs, int simm16) {
        memory.write32(gpr[rs] + simm16, Float.floatToRawIntBits(fpr[ft]));
    }
}
