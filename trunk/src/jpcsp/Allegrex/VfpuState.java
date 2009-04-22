/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jpcsp.Allegrex;

import jpcsp.Memory;

import java.util.Arrays;
import java.util.Random;

/**
 * Vectorial Floating Point Unit, handles scalar, vector and matrix operations.
 *
 * @author hli
 */
public class VfpuState extends FpuState {

    public float[][][] vpr; // mtx, fsl, idx
    private static final float floatConstants[] = {
        0.0f,
        Float.MAX_VALUE,
        (float) Math.sqrt(2.0f),
        (float) Math.sqrt(0.5f),
        2.0f / (float) Math.sqrt(Math.PI),
        2.0f / (float) Math.PI,
        1.0f / (float) Math.PI,
        (float) Math.PI / 4.0f,
        (float) Math.PI / 2.0f,
        (float) Math.PI,
        (float) Math.E,
        (float) (Math.log(Math.E) / Math.log(2.0)), // log2(E) = log(E) / log(2)
        (float) Math.log10(Math.E),
        (float) Math.log(2.0),
        (float) Math.log(10.0),
        (float) Math.PI * 2.0f,
        (float) Math.PI / 6.0f,
        (float) Math.log10(2.0),
        (float) (Math.log(10.0) / Math.log(2.0)), // log2(10) = log(10) / log(2)
        (float) Math.sqrt(3.0) / 2.0f
    };

    private static Random rnd;
    
    public class Vcr {

        public class PfxSrc /* $128, $129 */ {

            public int[] swz;
            public boolean[] abs;
            public boolean[] cst;
            public boolean[] neg;
            public boolean enabled;

            public void reset() {
                Arrays.fill(swz, 0);
                Arrays.fill(abs, false);
                Arrays.fill(cst, false);
                Arrays.fill(neg, false);
                enabled = false;
            }

            public PfxSrc() {
                swz = new int[4];
                abs = new boolean[4];
                cst = new boolean[4];
                neg = new boolean[4];
                enabled = false;
            }

            public void copy(PfxSrc that) {
                swz = that.swz.clone();
                abs = that.abs.clone();
                cst = that.cst.clone();
                neg = that.neg.clone();
                enabled = that.enabled;
            }

            public PfxSrc(PfxSrc that) {
                copy(that);
            }
        }
        public PfxSrc pfxs;
        public PfxSrc pfxt;

        public class PfxDst /* 130 */ {

            public int[] sat;
            public boolean[] msk;
            public boolean enabled;

            public void reset() {
                Arrays.fill(sat, 0);
                Arrays.fill(msk, false);
                enabled = false;
            }

            public PfxDst() {
                sat = new int[4];
                msk = new boolean[4];
                enabled = false;
            }

            public void copy(PfxDst that) {
                sat = that.sat.clone();
                msk = that.msk.clone();
                enabled = that.enabled;
            }

            public PfxDst(PfxDst that) {
                copy(that);
            }
        }
        public PfxDst pfxd;
        public boolean[] /* 131 */ cc;

        public void reset() {
            pfxs.reset();
            pfxt.reset();
            pfxd.reset();
            Arrays.fill(cc, false);
        }

        public Vcr() {
            pfxs = new PfxSrc();
            pfxt = new PfxSrc();
            pfxd = new PfxDst();
            cc = new boolean[6];
        }

        public void copy(Vcr that) {
            pfxs.copy(that.pfxs);
            pfxt.copy(that.pfxt);
            pfxd.copy(that.pfxd);
            cc = that.cc.clone();
        }

        public Vcr(Vcr that) {
            pfxs = new PfxSrc(that.pfxs);
            pfxt = new PfxSrc(that.pfxt);
            pfxd = new PfxDst(that.pfxd);
            cc = that.cc.clone();
        }
    }
    public Vcr vcr;

    private void resetFpr() {
        for (float[][] m : vpr) {
            for (float[] v : m) {
                Arrays.fill(v, 0.0f);
            }
        }
    }

    @Override
    public void reset() {
        resetFpr();
        vcr.reset();
    }

    @Override
    public void resetAll() {
        super.resetAll();
        resetFpr();
        vcr.reset();
    }

    public VfpuState() {
        vpr = new float[8][4][4]; // [matrix][column][row]
        vcr = new Vcr();
        rnd = new Random();
    }

    public void copy(VfpuState that) {
        super.copy(that);
        vpr = that.vpr.clone();
        vcr = new Vcr(that.vcr);
    }

    public VfpuState(VfpuState that) {
        super(that);
        vpr = that.vpr.clone();
        vcr = new Vcr(that.vcr);
    }
    private static float[] v1 = new float[4];
    private static float[] v2 = new float[4];
    private static float[] v3 = new float[4];
    // VFPU stuff
    private float transformVr(int swz, boolean abs, boolean cst, boolean neg, float[] x) {
        float value = 0.0f;
        if (cst) {
            switch (swz) {
                case 0:
                    value = abs ? 3.0f : 0.0f;
                    break;
                case 1:
                    value = abs ? (1.0f / 3.0f) : 1.0f;
                    break;
                case 2:
                    value = abs ? (1.0f / 4.0f) : 2.0f;
                    break;
                case 3:
                    value = abs ? (1.0f / 6.0f) : 0.5f;
                    break;
            }
        } else {
            value = x[swz];
        }

        if (abs) {
            value = Math.abs(value);
        }
        return neg ? (0.0f - value) : value;
    }

    private float applyPrefixVs(int i, float[] x) {
        return transformVr(vcr.pfxs.swz[i], vcr.pfxs.abs[i], vcr.pfxs.cst[i], vcr.pfxs.neg[i], x);
    }

    private float applyPrefixVt(int i, float[] x) {
        return transformVr(vcr.pfxt.swz[i], vcr.pfxt.abs[i], vcr.pfxt.cst[i], vcr.pfxt.neg[i], x);
    }

    private float applyPrefixVd(int i, float value) {
        switch (vcr.pfxd.sat[i]) {
            case 1:
                return Math.max(0.0f, Math.min(1.0f, value));
            case 3:
                return Math.max(-1.0f, Math.min(1.0f, value));
        }
        return value;
    }

    public void loadVs(int vsize, int vs) {
        int m, s, i;

        m = (vs >> 2) & 7;
        i = (vs >> 0) & 3;

        switch (vsize) {
            case 1:
                s = (vs >> 5) & 3;
                v1[0] = vpr[m][i][s];
                if (vcr.pfxs.enabled) {
                    v1[0] = applyPrefixVs(0, v1);
                    vcr.pfxs.enabled = false;
                }
                return;

            case 2:
                s = (vs & 64) >> 5;
                if ((vs & 32) != 0) {
                    v1[0] = vpr[m][s + 0][i];
                    v1[1] = vpr[m][s + 1][i];
                } else {
                    v1[0] = vpr[m][i][s + 0];
                    v1[1] = vpr[m][i][s + 1];
                }
                if (vcr.pfxs.enabled) {
                    v3[0] = applyPrefixVs(0, v1);
                    v3[1] = applyPrefixVs(1, v1);
                    v1[0] = v3[0];
                    v1[1] = v3[1];
                    vcr.pfxs.enabled = false;
                }
                return;

            case 3:
                s = (vs & 64) >> 6;
                if ((vs & 32) != 0) {
                    v1[0] = vpr[m][s + 0][i];
                    v1[1] = vpr[m][s + 1][i];
                    v1[2] = vpr[m][s + 2][i];
                } else {
                    v1[0] = vpr[m][i][s + 0];
                    v1[1] = vpr[m][i][s + 1];
                    v1[2] = vpr[m][i][s + 2];
                }
                if (vcr.pfxs.enabled) {
                    v3[0] = applyPrefixVs(0, v1);
                    v3[1] = applyPrefixVs(1, v1);
                    v3[2] = applyPrefixVs(2, v1);
                    v1[0] = v3[0];
                    v1[1] = v3[1];
                    v1[2] = v3[2];
                    vcr.pfxs.enabled = false;
                }
                return;

            case 4:
                if ((vs & 32) != 0) {
                    v1[0] = vpr[m][0][i];
                    v1[1] = vpr[m][1][i];
                    v1[2] = vpr[m][2][i];
                    v1[3] = vpr[m][3][i];
                } else {
                    v1[0] = vpr[m][i][0];
                    v1[1] = vpr[m][i][1];
                    v1[2] = vpr[m][i][2];
                    v1[3] = vpr[m][i][3];
                }
                if (vcr.pfxs.enabled) {
                    v3[0] = applyPrefixVs(0, v1);
                    v3[1] = applyPrefixVs(1, v1);
                    v3[2] = applyPrefixVs(2, v1);
                    v3[3] = applyPrefixVs(3, v1);
                    v1[0] = v3[0];
                    v1[1] = v3[1];
                    v1[2] = v3[2];
                    v1[3] = v3[3];
                    vcr.pfxs.enabled = false;
                }
            default:
        }
    }

    public void loadVt(int vsize, int vt) {
        int m, s, i;

        m = (vt >> 2) & 7;
        i = (vt >> 0) & 3;

        switch (vsize) {
            case 1:
                s = (vt >> 5) & 3;
                v2[0] = vpr[m][i][s];
                if (vcr.pfxt.enabled) {
                    v2[0] = applyPrefixVt(0, v2);
                    vcr.pfxt.enabled = false;
                }
                return;

            case 2:
                s = (vt & 64) >> 5;
                if ((vt & 32) != 0) {
                    v2[0] = vpr[m][s + 0][i];
                    v2[1] = vpr[m][s + 1][i];
                } else {
                    v2[0] = vpr[m][i][s + 0];
                    v2[1] = vpr[m][i][s + 1];
                }
                if (vcr.pfxt.enabled) {
                    v3[0] = applyPrefixVt(0, v2);
                    v3[1] = applyPrefixVt(1, v2);
                    v2[0] = v3[0];
                    v2[1] = v3[1];
                    vcr.pfxt.enabled = false;
                }
                return;

            case 3:
                s = (vt & 64) >> 6;
                if ((vt & 32) != 0) {
                    v2[0] = vpr[m][s + 0][i];
                    v2[1] = vpr[m][s + 1][i];
                    v2[2] = vpr[m][s + 2][i];
                } else {
                    v2[0] = vpr[m][i][s + 0];
                    v2[1] = vpr[m][i][s + 1];
                    v2[2] = vpr[m][i][s + 2];
                }
                if (vcr.pfxt.enabled) {
                    v3[0] = applyPrefixVt(0, v2);
                    v3[1] = applyPrefixVt(1, v2);
                    v3[2] = applyPrefixVt(2, v2);
                    v2[0] = v3[0];
                    v2[1] = v3[1];
                    v2[2] = v3[2];
                    vcr.pfxt.enabled = false;
                }
                return;

            case 4:
                if ((vt & 32) != 0) {
                    v2[0] = vpr[m][0][i];
                    v2[1] = vpr[m][1][i];
                    v2[2] = vpr[m][2][i];
                    v2[3] = vpr[m][3][i];
                } else {
                    v2[0] = vpr[m][i][0];
                    v2[1] = vpr[m][i][1];
                    v2[2] = vpr[m][i][2];
                    v2[3] = vpr[m][i][3];
                }
                if (vcr.pfxt.enabled) {
                    v3[0] = applyPrefixVt(0, v2);
                    v3[1] = applyPrefixVt(1, v2);
                    v3[2] = applyPrefixVt(2, v2);
                    v3[3] = applyPrefixVt(3, v2);
                    v2[0] = v3[0];
                    v2[1] = v3[1];
                    v2[2] = v3[2];
                    v2[3] = v3[3];
                    vcr.pfxt.enabled = false;
                }
            default:
        }
    }

    public void saveVd(int vsize, int vd, float[] vr) {
        int m, s, i;

        m = (vd >> 2) & 7;
        i = (vd >> 0) & 3;

        switch (vsize) {
            case 1:
                s = (vd >> 5) & 3;
                if (vcr.pfxd.enabled) {
                    if (!vcr.pfxd.msk[0]) {
                        vpr[m][i][s] = applyPrefixVd(0, vr[0]);
                    }
                    vcr.pfxd.enabled = false;
                } else {
                    vpr[m][i][s] = vr[0];
                }
                break;

            case 2:
                s = (vd & 64) >> 5;
                if (vcr.pfxd.enabled) {
                    if ((vd & 32) != 0) {
                        for (int j = 0; j < 2; ++j) {
                            if (!vcr.pfxd.msk[j]) {
                                vpr[m][s + j][i] = applyPrefixVd(j, vr[j]);
                            }
                        }
                    } else {
                        for (int j = 0; j < 2; ++j) {
                            if (!vcr.pfxd.msk[j]) {
                                vpr[m][i][s + j] = applyPrefixVd(j, vr[j]);
                            }
                        }
                    }
                    vcr.pfxd.enabled = false;
                } else {
                    if ((vd & 32) != 0) {
                        for (int j = 0; j < 2; ++j) {
                            vpr[m][s + j][i] = vr[j];
                        }
                    } else {
                        for (int j = 0; j < 2; ++j) {
                            vpr[m][i][s + j] = vr[j];
                        }
                    }
                }
                break;

            case 3:
                s = (vd & 64) >> 6;
                if (vcr.pfxd.enabled) {
                    if ((vd & 32) != 0) {
                        for (int j = 0; j < 3; ++j) {
                            if (!vcr.pfxd.msk[j]) {
                                vpr[m][s + j][i] = applyPrefixVd(j, vr[j]);
                            }
                        }
                    } else {
                        for (int j = 0; j < 3; ++j) {
                            if (!vcr.pfxd.msk[j]) {
                                vpr[m][i][s + j] = applyPrefixVd(j, vr[j]);
                            }
                        }
                    }
                    vcr.pfxd.enabled = false;
                } else {
                    if ((vd & 32) != 0) {
                        for (int j = 0; j < 3; ++j) {
                            vpr[m][s + j][i] = vr[j];
                        }
                    } else {
                        for (int j = 0; j < 3; ++j) {
                            vpr[m][i][s + j] = vr[j];
                        }
                    }
                }
                break;

            case 4:
                if (vcr.pfxd.enabled) {
                    if ((vd & 32) != 0) {
                        for (int j = 0; j < 4; ++j) {
                            if (!vcr.pfxd.msk[j]) {
                                vpr[m][j][i] = applyPrefixVd(j, vr[j]);
                            }
                        }
                    } else {
                        for (int j = 0; j < 4; ++j) {
                            if (!vcr.pfxd.msk[j]) {
                                vpr[m][i][j] = applyPrefixVd(j, vr[j]);
                            }
                        }
                    }
                    vcr.pfxd.enabled = false;
                } else {
                    if ((vd & 32) != 0) {
                        for (int j = 0; j < 4; ++j) {
                            vpr[m][j][i] = vr[j];
                        }
                    } else {
                        for (int j = 0; j < 4; ++j) {
                            vpr[m][i][j] = vr[j];
                        }
                    }
                }
                break;

            default:
                break;
        }
    }
    // group VFPU0
    // VFPU0:VADD
    public void doVADD(int vsize, int vd, int vs, int vt) {
        loadVs(vsize, vs);
        loadVt(vsize, vt);

        for (int i = 0; i < vsize; ++i) {
            v1[i] += v2[i];
        }

        saveVd(vsize, vd, v1);
    }

    // VFPU0:VSUB
    public void doVSUB(int vsize, int vd, int vs, int vt) {
        loadVs(vsize, vs);
        loadVt(vsize, vt);

        for (int i = 0; i < vsize; ++i) {
            v1[i] -= v2[i];
        }

        saveVd(vsize, vd, v1);
    }

    // VFPU0:VSBN
    public void doVSBN(int vsize, int vd, int vs, int vt) {
        if (vsize != 1) {
            doUNK("Only supported VSBN.S");
        }

        loadVs(1, vs);
        loadVt(1, vt);

        v1[0] = Math.scalb(v1[0], Float.floatToRawIntBits(v2[0]));

        saveVd(1, vd, v1);
    }

    // VFPU0:VDIV
    public void doVDIV(int vsize, int vd, int vs, int vt) {
        loadVs(vsize, vs);
        loadVt(vsize, vt);

        for (int i = 0; i < vsize; ++i) {
            v1[i] /= v2[i];
        }

        saveVd(vsize, vd, v1);
    }

    // group VFPU1
    // VFPU1:VMUL
    public void doVMUL(int vsize, int vd, int vs, int vt) {
        loadVs(vsize, vs);
        loadVt(vsize, vt);

        for (int i = 0; i < vsize; ++i) {
            v1[i] *= v2[i];
        }

        saveVd(vsize, vd, v1);
    }

    // VFPU1:VDOT
    public void doVDOT(int vsize, int vd, int vs, int vt) {
        if (vsize == 1) {
            doUNK("Unsupported VDOT.S");
        }

        loadVs(vsize, vs);
        loadVt(vsize, vt);

        float dot = v1[0] * v2[0];

        for (int i = 1; i < vsize; ++i) {
            dot += v1[i] * v2[i];
        }

        v3[0] = dot;

        saveVd(1, vd, v3);
    }

    // VFPU1:VSCL
    public void doVSCL(int vsize, int vd, int vs, int vt) {
        if (vsize == 1) {
            doUNK("Unsupported VSCL.S");
        }

        loadVs(vsize, vs);
        loadVt(1, vt);

        float scale = v2[0];

        for (int i = 1; i < vsize; ++i) {
            v1[i] *= scale;
        }

        saveVd(vsize, vd, v1);
    }

    // VFPU1:VHDP
    public void doVHDP(int vsize, int vd, int vs, int vt) {
        if (vsize == 1) {
            doUNK("Unsupported VHDP.S");
        }

        loadVs(vsize, vs);
        loadVt(vsize, vt);

        float hdp = v1[0] * v2[0];

        int i;

        for (i = 1; i < vsize - 1; ++i) {
            hdp += v1[i] * v2[i];
        }

        v2[0] += hdp;

        saveVd(1, vd, v2);
    }

    // VFPU1:VCRS
    public void doVCRS(int vsize, int vd, int vs, int vt) {
        if (vsize != 3) {
            doUNK("Only supported VCRS.T");
        }

        loadVs(3, vs);
        loadVt(3, vt);

        v3[0] = v1[1] * v2[2];
        v3[1] = v1[2] * v2[0];
        v3[2] = v1[0] * v2[1];

        saveVd(3, vd, v3);
    }

    // VFPU1:VDET
    public void doVDET(int vsize, int vd, int vs, int vt) {
        if (vsize != 2) {
            doUNK("Only supported VDET.P");
            return;
        }

        loadVs(2, vs);
        loadVt(2, vt);

        v1[0] = v1[0] * v2[1] - v1[1] * v2[0];

        saveVd(1, vd, v1);
    }

    // VFPU2

    // VFPU2:MFV
    public void doMFV(int rt, int imm7) {
        int r = (imm7 >> 5) & 3;
        int m = (imm7 >> 2) & 7;
        int c = (imm7 >> 0) & 3;

        gpr[rt] = Float.floatToRawIntBits(vpr[m][c][r]);
    }
    // VFPU2:MFVC
    public void doMFVC(int rt, int imm7) {
        doUNK("Unimplemented MFVC");
    }
    // VFPU2:MTV
    public void doMTV(int rt, int imm7) {
        int r = (imm7 >> 5) & 3;
        int m = (imm7 >> 2) & 7;
        int c = (imm7 >> 0) & 3;

        vpr[m][c][r] = Float.intBitsToFloat(gpr[rt]);
    }

    // VFPU2:MTVC
    public void doMTVC(int rt, int imm7) {
        doUNK("Unimplemented MTVC");
    }

    // VFPU2:BVF
    public boolean doBVF(int imm3, int simm16) {
        npc = (!vcr.cc[imm3]) ? branchTarget(pc, simm16) : (pc + 4);
        return true;
    }
    // VFPU2:BVT
    public boolean doBVT(int imm3, int simm16) {
        npc = (vcr.cc[imm3]) ? branchTarget(pc, simm16) : (pc + 4);
        return true;
    }
    // VFPU2:BVFL
    public boolean doBVFL(int imm3, int simm16) {
    	if (!vcr.cc[imm3]) {
    		npc = branchTarget(pc, simm16);
    		return true;
    	} else {
    		pc = pc + 4;
    	}
        return false;
    }
    // VFPU2:BVTL
    public boolean doBVTL(int imm3, int simm16) {
    	if (vcr.cc[imm3]) {
    		npc = branchTarget(pc, simm16);
    		return true;
    	} else {
    		pc = pc + 4;
    	}
        return false;
    }
    // group VFPU3

    // VFPU3:VCMP
    public void doVCMP(int vsize, int vs, int vt, int cond) {
        boolean cc_or = false;
        boolean cc_and = true;

        if ((cond & 8) == 0) {
            boolean not = ((cond & 4) == 4);

            boolean cc = false;

            loadVs(vsize, vs);
            loadVt(vsize, vt);

            for (int i = 0; i < vsize; ++i) {
                switch (cond & 3) {
                    case 0:
                        cc = not;
                        break;

                    case 1:
                        cc = not ? (v1[i] != v2[i]) : (v1[i] == v2[i]);
                        break;

                    case 2:
                        cc = not ? (v1[i] >= v2[i]) : (v1[i] < v2[i]);
                        break;

                    case 3:
                        cc = not ? (v1[i] > v2[i]) : (v1[i] <= v2[i]);
                        break;

                }


                vcr.cc[i] = cc;
                cc_or = cc_or || cc;
                cc_and = cc_and && cc;
            }

        } else {
            loadVs(vsize, vs);

            for (int i = 0; i < vsize; ++i) {
                boolean cc;
                if ((cond & 3) == 0) {
                    cc = ((cond & 4) == 0) ? (v1[i] == 0.0f) : (v1[i] != 0.0f);
                } else {
                    cc = (((cond & 1) == 1) && Float.isNaN(v1[i])) ||
                            (((cond & 2) == 2) && Float.isInfinite(v1[i]));
                    if ((cond & 4) == 4) {
                        cc = !cc;
                    }

                }
                vcr.cc[i] = cc;
                cc_or = cc_or || cc;
                cc_and = cc_and && cc;
            }

        }
        vcr.cc[4] = cc_or;
        vcr.cc[5] = cc_and;
    }

    // VFPU3:VMIN
    public void doVMIN(int vsize, int vd, int vs, int vt) {
        loadVs(vsize, vs);
        loadVt(vsize, vt);

        for (int i = 0; i < vsize; ++i) {
            v3[i] = Math.min(v1[i], v2[i]);
        }

        saveVd(vsize, vd, v3);
    }

    // VFPU3:VMAX
    public void doVMAX(int vsize, int vd, int vs, int vt) {
        loadVs(vsize, vs);
        loadVt(vsize, vt);

        for (int i = 0; i < vsize; ++i) {
            v3[i] = Math.max(v1[i], v2[i]);
        }

        saveVd(vsize, vd, v3);
    }

    // VFPU3:VSCMP
    public void doVSCMP(int vsize, int vd, int vs, int vt) {
        loadVs(vsize, vs);
        loadVt(vsize, vt);

        for (int i = 0; i < vsize; ++i) {
            v3[i] = Math.signum(v1[i] - v2[i]);
        }

        saveVd(vsize, vd, v3);
    }

    // VFPU3:VSGE
    public void doVSGE(int vsize, int vd, int vs, int vt) {
        loadVs(vsize, vs);
        loadVt(vsize, vt);

        for (int i = 0; i < vsize; ++i) {
            v3[i] = (v1[i] >= v2[i]) ? 1.0f : 0.0f;
        }

        saveVd(vsize, vd, v3);
    }

    // VFPU3:VSLT
    public void doVSLT(int vsize, int vd, int vs, int vt) {
        loadVs(vsize, vs);
        loadVt(vsize, vt);

        for (int i = 0; i < vsize; ++i) {
            v3[i] = (v1[i] < v2[i]) ? 1.0f : 0.0f;
        }

        saveVd(vsize, vd, v3);
    }

    // group VFPU4
    // VFPU4:VMOV
    public void doVMOV(int vsize, int vd, int vs) {
        loadVs(vsize, vs);
        saveVd(vsize, vd, v1);
    }

    // VFPU4:VABS
    public void doVABS(int vsize, int vd, int vs) {
        loadVs(vsize, vs);
        for (int i = 0; i < vsize; ++i) {
            v3[i] = Math.abs(v1[i]);
        }
        saveVd(vsize, vd, v3);
    }

    // VFPU4:VNEG
    public void doVNEG(int vsize, int vd, int vs) {
        loadVs(vsize, vs);
        for (int i = 0; i < vsize; ++i) {
            v3[i] = 0.0f - v1[i];
        }
        saveVd(vsize, vd, v3);
    }

    // VFPU4:VIDT
    public void doVIDT(int vsize, int vd) {
        int id = vd & 3;
        for (int i = 0; i < vsize; ++i) {
            v3[i] = (id == i) ? 1.0f : 0.0f;
        }
        saveVd(vsize, vd, v3);
    }

    // VFPU4:VSAT0
    public void doVSAT0(int vsize, int vd, int vs) {
        loadVs(vsize, vs);
        for (int i = 0; i < vsize; ++i) {
            v3[i] = Math.min(Math.max(0.0f, v1[i]), 1.0f);
        }
        saveVd(vsize, vd, v3);
    }

    // VFPU4:VSAT1
    public void doVSAT1(int vsize, int vd, int vs) {
        loadVs(vsize, vs);
        for (int i = 0; i < vsize; ++i) {
            v3[i] = Math.min(Math.max(-1.0f, v1[i]), 1.0f);
        }
        saveVd(vsize, vd, v3);
    }

    // VFPU4:VZERO
    public void doVZERO(int vsize, int vd) {
        for (int i = 0; i < vsize; ++i) {
            v3[i] = 0.0f;
        }
        saveVd(vsize, vd, v3);
    }
    // VFPU4:VONE
    public void doVONE(int vsize, int vd) {
        for (int i = 0; i < vsize; ++i) {
            v3[i] = 1.0f;
        }
        saveVd(vsize, vd, v3);
    }
    // VFPU4:VRCP
    public void doVRCP(int vsize, int vd, int vs) {
        loadVs(vsize, vs);
        for (int i = 0; i < vsize; ++i) {
            v3[i] = 1.0f / v1[i];
        }
        saveVd(vsize, vd, v3);
    }
    // VFPU4:VRSQ
    public void doVRSQ(int vsize, int vd, int vs) {
        loadVs(vsize, vs);
        for (int i = 0; i < vsize; ++i) {
            v3[i] = (float) (1.0 / Math.sqrt(v1[i]));
        }
        saveVd(vsize, vd, v3);
    }
    // VFPU4:VSIN
    public void doVSIN(int vsize, int vd, int vs) {
        loadVs(vsize, vs);
        for (int i = 0; i < vsize; ++i) {
            v3[i] = (float) Math.sin(0.5 * Math.PI * v1[i]);
        }
        saveVd(vsize, vd, v3);
    }
    // VFPU4:VCOS
    public void doVCOS(int vsize, int vd, int vs) {
        loadVs(vsize, vs);
        for (int i = 0; i < vsize; ++i) {
            v3[i] = (float) Math.cos(0.5 * Math.PI * v1[i]);
        }
        saveVd(vsize, vd, v3);
    }
    // VFPU4:VEXP2
    public void doVEXP2(int vsize, int vd, int vs) {
        loadVs(vsize, vs);
        for (int i = 0; i < vsize; ++i) {
            v3[i] = (float) Math.pow(2.0, v1[i]);
        }
        saveVd(vsize, vd, v3);
    }
    // VFPU4:VLOG2
    public void doVLOG2(int vsize, int vd, int vs) {
        loadVs(vsize, vs);
        for (int i = 0; i < vsize; ++i) {
            v3[i] = (float) (Math.log(v1[i]) / Math.log(2.0));
        }
        saveVd(vsize, vd, v3);
    }

    // VFPU4:VSQRT
    public void doVSQRT(int vsize, int vd, int vs) {
        loadVs(vsize, vs);
        for (int i = 0; i < vsize; ++i) {
            v3[i] = (float) (Math.sqrt(v1[i]));
        }
        saveVd(vsize, vd, v3);
    }

    // VFPU4:VASIN
    public void doVASIN(int vsize, int vd, int vs) {
        loadVs(vsize, vs);
        for (int i = 0; i < vsize; ++i) {
            v3[i] = (float) (Math.asin(v1[i]) * 2.0 / Math.PI);
        }
        saveVd(vsize, vd, v3);
    }

    // VFPU4:VNRCP
    public void doVNRCP(int vsize, int vd, int vs) {
        loadVs(vsize, vs);
        for (int i = 0; i < vsize; ++i) {
            v3[i] = 0.0f - (1.0f / v1[i]);
        }
        saveVd(vsize, vd, v3);
    }

    // VFPU4:VNSIN
    public void doVNSIN(int vsize, int vd, int vs) {
        loadVs(vsize, vs);
        for (int i = 0; i < vsize; ++i) {
            v3[i] = 0.0f - (float) Math.sin(0.5 * Math.PI * v1[i]);
        }
        saveVd(vsize, vd, v3);
    }
    // VFPU4:VREXP2
    public void doVREXP2(int vsize, int vd, int vs) {
        loadVs(vsize, vs);
        for (int i = 0; i < vsize; ++i) {
            v3[i] = (float) (1.0 / Math.pow(2.0, v1[i]));
        }
        saveVd(vsize, vd, v3);
    }
    // VFPU4:VRNDS
    public void doVRNDS(int vsize, int vs) {
        // temporary solution
        if (vsize != 1) {
            doUNK("Only supported VRNDS.S");
            return;
        }
        
        loadVs(1, vs);
        rnd.setSeed(Float.floatToRawIntBits(v1[0]));
    }
    // VFPU4:VRNDI
    public void doVRNDI(int vsize, int vd) {
        // temporary solution
        for (int i = 0; i < vsize; ++i) {
            v3[i] = Float.intBitsToFloat(rnd.nextInt());
        }
        saveVd(vsize, vd, v3);
    }
    // VFPU4:VRNDF1
    public void doVRNDF1(int vsize, int vd) {
        // temporary solution
        for (int i = 0; i < vsize; ++i) {
            v3[i] = 1.0f + rnd.nextFloat();
        }
        saveVd(vsize, vd, v3);
    }
    // VFPU4:VRNDF2
    public void doVRNDF2(int vsize, int vd) {
        // temporary solution
        for (int i = 0; i < vsize; ++i) {
            v3[i] = (1.0f + rnd.nextFloat())*2.0f;
        }
        saveVd(vsize, vd, v3);
    }
    // VFPU4:VF2H
    public void doVF2H(int vsize, int vd, int vs) {
        doUNK("Unimplemented VF2H");
    }
    // VFPU4:VH2F
    public void doVH2F(int vsize, int vd, int vs) {
        doUNK("Unimplemented VH2F");
    }
    // VFPU4:VSBZ
    public void doVSBZ(int vsize, int vd, int vs) {
        doUNK("Unimplemented VSBZ");
    }
    // VFPU4:VLGB
    public void doVLGB(int vsize, int vd, int vs) {
        doUNK("Unimplemented VLGB");
    }
    // VFPU4:VUC2I
    public void doVUC2I(int vsize, int vd, int vs) {
        doUNK("Unimplemented VUC2I");
    }
    // VFPU4:VC2I
    public void doVC2I(int vsize, int vd, int vs) {
        doUNK("Unimplemented VC2I");
    }
    // VFPU4:VUS2I
    public void doVUS2I(int vsize, int vd, int vs) {
        doUNK("Unimplemented VUS2I");
    }
    // VFPU4:VS2I
    public void doVS2I(int vsize, int vd, int vs) {
        doUNK("Unimplemented VS2I");
    }

    // VFPU4:VI2UC
    public void doVI2UC(int vsize, int vd, int vs) {
        if (vsize != 4) {
            doUNK("Only supported VI2UC.Q");
            return;
        }

        loadVs(4, vs);

        int x = Float.floatToRawIntBits(v1[0]);
        int y = Float.floatToRawIntBits(v1[1]);
        int z = Float.floatToRawIntBits(v1[2]);
        int w = Float.floatToRawIntBits(v1[3]);

        v3[0] = Float.intBitsToFloat(
                ((x > 0) ? ((x >>> 23) << 0) : 0) |
                ((y > 0) ? ((y >>> 23) << 8) : 0) |
                ((z > 0) ? ((z >>> 23) << 16) : 0) |
                ((w > 0) ? ((w >>> 23) << 24) : 0));

        saveVd(1, vd, v3);
    }

    // VFPU4:VI2C
    public void doVI2C(int vsize, int vd, int vs) {
        if (vsize != 4) {
            doUNK("Only supported VI2C.Q");
            return;
        }

        loadVs(4, vs);

        int x = Float.floatToRawIntBits(v1[0]);
        int y = Float.floatToRawIntBits(v1[1]);
        int z = Float.floatToRawIntBits(v1[2]);
        int w = Float.floatToRawIntBits(v1[3]);

        v3[0] = Float.intBitsToFloat(
                ((x >>> 24) << 0) |
                ((y >>> 24) << 8) |
                ((z >>> 24) << 16) |
                ((w >>> 24) << 24));

        saveVd(1, vd, v3);
    }
    // VFPU4:VI2US
    public void doVI2US(int vsize, int vd, int vs) {
        if ((vsize & 1) != 0) {
            doUNK("Only supported VI2US.P and VI2US.Q");
            return;
        }

        loadVs(vsize, vs);

        int x = Float.floatToRawIntBits(v1[0]);
        int y = Float.floatToRawIntBits(v1[1]);

        v3[0] = Float.intBitsToFloat(
                ((x > 0) ? ((x >>> 15) << 0) : 0) |
                ((y > 0) ? ((y >>> 15) << 16) : 0));

        if (vsize == 4) {
            int z = Float.floatToRawIntBits(v1[2]);
            int w = Float.floatToRawIntBits(v1[3]);

            v3[1] = Float.intBitsToFloat(
                    ((z > 0) ? ((z >>> 15) << 0) : 0) |
                    ((w > 0) ? ((w >>> 15) << 16) : 0));
            saveVd(2, vd, v3);
        } else {
            saveVd(1, vd, v3);
        }
    }
    // VFPU4:VI2S
    public void doVI2S(int vsize, int vd, int vs) {
        if ((vsize & 1) != 0) {
            doUNK("Only supported VI2S.P and VI2S.Q");
            return;
        }

        loadVs(vsize, vs);

        int x = Float.floatToRawIntBits(v1[0]);
        int y = Float.floatToRawIntBits(v1[1]);

        v3[0] = Float.intBitsToFloat(
                ((x >>> 16) << 0) |
                ((y >>> 16) << 16));

        if (vsize == 4) {
            int z = Float.floatToRawIntBits(v1[2]);
            int w = Float.floatToRawIntBits(v1[3]);

            v3[1] = Float.intBitsToFloat(
                    ((z >>> 16) << 0) |
                    ((w >>> 16) << 16));
            saveVd(2, vd, v3);
        } else {
            saveVd(1, vd, v3);
        }
    }
    // VFPU4:VSRT1
    public void doVSRT1(int vsize, int vd, int vs) {
        if (vsize != 4) {
            doUNK("Only supported VSRT1.Q");
            return;
        }

        loadVs(4, vs);
        float x = v1[0];
        float y = v1[0];
        float z = v1[0];
        float w = v1[0];
        v3[0] = Math.min(x, y);
        v3[1] = Math.max(x, y);
        v3[2] = Math.min(z, w);
        v3[3] = Math.max(z, w);
        saveVd(4, vd, v3);
    }
    // VFPU4:VSRT2
    public void doVSRT2(int vsize, int vd, int vs) {
        if (vsize != 4) {
            doUNK("Only supported VSRT2.Q");
            return;
        }

        loadVs(4, vs);
        float x = v1[0];
        float y = v1[0];
        float z = v1[0];
        float w = v1[0];
        v3[0] = Math.min(x, w);
        v3[1] = Math.min(y, z);
        v3[2] = Math.max(y, z);
        v3[3] = Math.max(x, w);
        saveVd(4, vd, v3);
    }
    // VFPU4:VBFY1
    public void doVBFY1(int vsize, int vd, int vs) {
        if ((vsize & 1) == 1) {
            doUNK("Only supported VBFY1.P or VBFY1.Q");
            return;
        }

        loadVs(vsize, vs);
        float x = v1[0];
        float y = v1[1];
        v3[0] = x + y;
        v3[1] = x - y;
        if (vsize > 2) {
            float z = v1[2];
            float w = v1[3];
            v3[2] = z + w;
            v3[3] = z - w;
            saveVd(4, vd, v3);
        } else {
            saveVd(2, vd, v3);
        }
    }
    // VFPU4:VBFY2
    public void doVBFY2(int vsize, int vd, int vs) {
        if (vsize != 4) {
            doUNK("Only supported VBFY2.Q");
            return;
        }

        loadVs(vsize, vs);
        float x = v1[0];
        float y = v1[1];
        float z = v1[2];
        float w = v1[3];
        v3[0] = x + z;
        v3[1] = y + w;
        v3[2] = x - z;
        v3[3] = y - w;
        saveVd(4, vd, v3);
    }
    // VFPU4:VOCP
    public void doVOCP(int vsize, int vd, int vs) {
        loadVs(vsize, vs);

        for (int i = 1; i < vsize; ++i) {
            v1[i] = 0.0f - v1[i];
        }

        saveVd(vsize, vd, v1);
    }
    // VFPU4:VSOCP
    public void doVSOCP(int vsize, int vd, int vs) {
        if (vsize > 2) {
            doUNK("Only supported VSOCP.S or VSOCP.P");
            return;
        }

        loadVs(vsize, vs);
        float x = v1[0];
        v3[0] = Math.min(Math.max(0.0f, 0.0f - x), 1.0f);
        v3[1] = Math.min(Math.max(0.0f, 0.0f + x), 1.0f);
        if (vsize > 1) {
            float y = v1[1];
            v3[2] = Math.min(Math.max(0.0f, 0.0f - y), 1.0f);
            v3[3] = Math.min(Math.max(0.0f, 0.0f + y), 1.0f);
            saveVd(4, vd, v3);
        } else {
            saveVd(2, vd, v3);
        }
    }
    // VFPU4:VFAD
    public void doVFAD(int vsize, int vd, int vs) {
        if (vsize == 1) {
            doUNK("Unsupported VFAD.S");
            return;
        }

        loadVs(vsize, vs);

        for (int i = 1; i < vsize; ++i) {
            v1[0] += v1[i];
        }

        saveVd(1, vd, v1);
    }
    // VFPU4:VAVG
    public void doVAVG(int vsize, int vd, int vs) {
        if (vsize == 1) {
            doUNK("Unsupported VAVG.S");
            return;
        }

        loadVs(vsize, vs);

        for (int i = 1; i < vsize; ++i) {
            v1[0] += v1[i];
        }

        v1[0] /= vsize;

        saveVd(1, vd, v1);
    }
    // VFPU4:VSRT3
    public void doVSRT3(int vsize, int vd, int vs) {
        if (vsize != 4) {
            doUNK("Only supported VSRT3.Q (vsize=" + vsize + ")");
            return;
        }

        loadVs(4, vs);
        float x = v1[0];
        float y = v1[1];
        float z = v1[2];
        float w = v1[3];
        v3[0] = Math.max(x, y);
        v3[1] = Math.min(x, y);
        v3[2] = Math.max(z, w);
        v3[3] = Math.min(z, w);
        saveVd(4, vd, v3);
    }
    // VFPU4:VSRT4
    public void doVSRT4(int vsize, int vd, int vs) {
        if (vsize != 4) {
            doUNK("Only supported VSRT4.Q");
            return;
        }

        loadVs(4, vs);
        float x = v1[0];
        float y = v1[1];
        float z = v1[2];
        float w = v1[3];
        v3[0] = Math.max(x, w);
        v3[1] = Math.max(y, z);
        v3[2] = Math.min(y, z);
        v3[3] = Math.min(x, w);
        saveVd(4, vd, v3);
    }
    // VFPU4:VMFVC
    public void doVMFVC(int vd, int imm7) {
        doUNK("Unimplemented VMFVC");
    }
    // VFPU4:VMTVC
    public void doVMTVC(int vd, int imm7) {
        doUNK("Unimplemented VMTVC");
    }
    // VFPU4:VT4444
    public void doVT4444(int vsize, int vd, int vs) {
        loadVs(4, vs);
        int i0 = Float.floatToRawIntBits(v1[0]);
        int i1 = Float.floatToRawIntBits(v1[1]);
        int i2 = Float.floatToRawIntBits(v1[2]);
        int i3 = Float.floatToRawIntBits(v1[3]);
        int o0 = 0, o1 = 0;
        o0 |= ((i0>> 4)&15) << 0;
        o0 |= ((i0>>12)&15) << 4;
        o0 |= ((i0>>20)&15) << 8;
        o0 |= ((i0>>28)&15) <<12;
        o0 |= ((i1>> 4)&15) <<16;
        o0 |= ((i1>>12)&15) <<20;
        o0 |= ((i1>>20)&15) <<24;
        o0 |= ((i1>>28)&15) <<28;
        o1 |= ((i2>> 4)&15) << 0;
        o1 |= ((i2>>12)&15) << 4;
        o1 |= ((i2>>20)&15) << 8;
        o1 |= ((i2>>28)&15) <<12;
        o1 |= ((i3>> 4)&15) <<16;
        o1 |= ((i3>>12)&15) <<20;
        o1 |= ((i3>>20)&15) <<24;
        o1 |= ((i3>>28)&15) <<28;
        v3[0] = Float.intBitsToFloat(o0);
        v3[1] = Float.intBitsToFloat(o1);
        saveVd(2, vd, v3);
    }
    // VFPU4:VT5551
    public void doVT5551(int vsize, int vd, int vs) {
        loadVs(4, vs);
        int i0 = Float.floatToRawIntBits(v1[0]);
        int i1 = Float.floatToRawIntBits(v1[1]);
        int i2 = Float.floatToRawIntBits(v1[2]);
        int i3 = Float.floatToRawIntBits(v1[3]);
        int o0 = 0, o1 = 0;
        o0 |= ((i0>> 3)&31) << 0;
        o0 |= ((i0>>11)&31) << 5;
        o0 |= ((i0>>19)&31) <<10;
        o0 |= ((i0>>31)& 1) <<15;
        o0 |= ((i1>> 3)&31) <<16;
        o0 |= ((i1>>11)&31) <<21;
        o0 |= ((i1>>19)&31) <<26;
        o0 |= ((i1>>31)& 1) <<31;
        o1 |= ((i2>> 3)&31) << 0;
        o1 |= ((i2>>11)&31) << 5;
        o1 |= ((i2>>19)&31) <<10;
        o1 |= ((i2>>31)& 1) <<15;
        o1 |= ((i3>> 3)&31) <<16;
        o1 |= ((i3>>11)&31) <<21;
        o1 |= ((i3>>19)&31) <<26;
        o1 |= ((i3>>31)& 1) <<31;
        v3[0] = Float.intBitsToFloat(o0);
        v3[1] = Float.intBitsToFloat(o1);
        saveVd(2, vd, v3);
    }
    // VFPU4:VT5650
    public void doVT5650(int vsize, int vd, int vs) {
        loadVs(4, vs);
        int i0 = Float.floatToRawIntBits(v1[0]);
        int i1 = Float.floatToRawIntBits(v1[1]);
        int i2 = Float.floatToRawIntBits(v1[2]);
        int i3 = Float.floatToRawIntBits(v1[3]);
        int o0 = 0, o1 = 0;
        o0 |= ((i0>> 3)&31) << 0;
        o0 |= ((i0>>10)&63) << 5;
        o0 |= ((i0>>19)&31) <<11;
        o0 |= ((i1>> 3)&31) <<16;
        o0 |= ((i1>>10)&63) <<21;
        o0 |= ((i1>>19)&31) <<27;
        o1 |= ((i2>> 3)&31) << 0;
        o1 |= ((i2>>10)&63) << 5;
        o1 |= ((i2>>19)&31) <<11;
        o1 |= ((i3>> 3)&31) <<16;
        o1 |= ((i3>>10)&63) <<21;
        o1 |= ((i3>>19)&31) <<27;
        v3[0] = Float.intBitsToFloat(o0);
        v3[1] = Float.intBitsToFloat(o1);
        saveVd(2, vd, v3);
    }
    // VFPU4:VCST
    public void doVCST(int vsize, int vd, int imm5) {
        float constant = 0.0f;

        if (imm5 >= 0 && imm5 < floatConstants.length) {
            constant = floatConstants[imm5];
        }

        for (int i = 0; i < vsize; ++i) {
            v3[i] = constant;
        }

        saveVd(vsize, vd, v3);
    }

    // VFPU4:VF2IN
    public void doVF2IN(int vsize, int vd, int vs, int imm5) {
        loadVs(vsize, vs);

        for (int i = 0; i < vsize; ++i) {
            float value = Math.scalb(v1[i], imm5);
            v3[i] = Float.intBitsToFloat((int) Math.round(value));
        }

        saveVd(vsize, vd, v3);
    }
    // VFPU4:VF2IZ
    public void doVF2IZ(int vsize, int vd, int vs, int imm5) {
        loadVs(vsize, vs);

        for (int i = 0; i < vsize; ++i) {
            float value = Math.scalb(v1[i], imm5);
            v3[i] = Float.intBitsToFloat(v1[i] >= 0 ? (int) Math.floor(value) : (int) Math.ceil(value));
        }

        saveVd(vsize, vd, v3);
    }
    // VFPU4:VF2IU
    public void doVF2IU(int vsize, int vd, int vs, int imm5) {
        loadVs(vsize, vs);

        for (int i = 0; i < vsize; ++i) {
            float value = Math.scalb(v1[i], imm5);
            v3[i] = Float.intBitsToFloat((int) Math.ceil(value));
        }

        saveVd(vsize, vd, v3);
    }
    // VFPU4:VF2ID
    public void doVF2ID(int vsize, int vd, int vs, int imm5) {
        loadVs(vsize, vs);

        for (int i = 0; i < vsize; ++i) {
            float value = Math.scalb(v1[i], imm5);
            v3[i] = Float.intBitsToFloat((int) Math.floor(value));
        }

        saveVd(vsize, vd, v3);
    }
    // VFPU4:VI2F
    public void doVI2F(int vsize, int vd, int vs, int imm5) {
        loadVs(vsize, vs);

        for (int i = 0; i < vsize; ++i) {
            float value = (float) Float.floatToRawIntBits(v1[i]);
            v3[i] = Math.scalb(value, -imm5);
        }

        saveVd(vsize, vd, v3);
    }
    // VFPU4:VCMOVT
    public void doVCMOVT(int vsize, int imm3, int vd, int vs) {
        if (imm3 < 6) {
            if (vcr.cc[imm3]) {
                loadVs(vsize, vs);
                saveVd(vsize, vd, v1);
            }
        } else {
            loadVs(vsize, vs);
            loadVt(vsize, vd);
            for (int i = 0; i < vsize; ++i) {
                if (vcr.cc[i]) {
                    v2[i] = v1[i];
                }
            }
            saveVd(vsize, vd, v2);
        }
    }
    // VFPU4:VCMOVF
    public void doVCMOVF(int vsize, int imm3, int vd, int vs) {
        if (imm3 < 6) {
            if (!vcr.cc[imm3]) {
                loadVs(vsize, vs);
                saveVd(vsize, vd, v1);
            }
        } else {
            loadVs(vsize, vs);
            loadVt(vsize, vd);
            for (int i = 0; i < vsize; ++i) {
                if (!vcr.cc[i]) {
                    v2[i] = v1[i];
                }
            }
            saveVd(vsize, vd, v2);
        }
    }
    // VFPU4:VWBN
    public void doVWBN(int vsize, int vd, int vs, int imm8) {
        doUNK("Unimplemented VWBN");
    }
    // group VFPU5
    // VFPU5:VPFXS
    public void doVPFXS(
            int negw, int negz, int negy, int negx,
            int cstw, int cstz, int csty, int cstx,
            int absw, int absz, int absy, int absx,
            int swzw, int swzz, int swzy, int swzx) {
        vcr.pfxs.swz[0] = swzx;
        vcr.pfxs.swz[1] = swzy;
        vcr.pfxs.swz[2] = swzz;
        vcr.pfxs.swz[3] = swzw;
        vcr.pfxs.abs[0] = absx != 0;
        vcr.pfxs.abs[1] = absy != 0;
        vcr.pfxs.abs[2] = absz != 0;
        vcr.pfxs.abs[3] = absw != 0;
        vcr.pfxs.cst[0] = cstx != 0;
        vcr.pfxs.cst[1] = csty != 0;
        vcr.pfxs.cst[2] = cstz != 0;
        vcr.pfxs.cst[3] = cstw != 0;
        vcr.pfxs.neg[0] = negx != 0;
        vcr.pfxs.neg[1] = negy != 0;
        vcr.pfxs.neg[2] = negz != 0;
        vcr.pfxs.neg[3] = negw != 0;
        vcr.pfxs.enabled = true;
    }

    // VFPU5:VPFXT
    public void doVPFXT(
            int negw, int negz, int negy, int negx,
            int cstw, int cstz, int csty, int cstx,
            int absw, int absz, int absy, int absx,
            int swzw, int swzz, int swzy, int swzx) {
        vcr.pfxt.swz[0] = swzx;
        vcr.pfxt.swz[1] = swzy;
        vcr.pfxt.swz[2] = swzz;
        vcr.pfxt.swz[3] = swzw;
        vcr.pfxt.abs[0] = absx != 0;
        vcr.pfxt.abs[1] = absy != 0;
        vcr.pfxt.abs[2] = absz != 0;
        vcr.pfxt.abs[3] = absw != 0;
        vcr.pfxt.cst[0] = cstx != 0;
        vcr.pfxt.cst[1] = csty != 0;
        vcr.pfxt.cst[2] = cstz != 0;
        vcr.pfxt.cst[3] = cstw != 0;
        vcr.pfxt.neg[0] = negx != 0;
        vcr.pfxt.neg[1] = negy != 0;
        vcr.pfxt.neg[2] = negz != 0;
        vcr.pfxt.neg[3] = negw != 0;
        vcr.pfxt.enabled = true;
    }

    // VFPU5:VPFXD
    public void doVPFXD(
            int mskw, int mskz, int msky, int mskx,
            int satw, int satz, int saty, int satx) {
        vcr.pfxd.sat[0] = satx;
        vcr.pfxd.sat[1] = saty;
        vcr.pfxd.sat[2] = satz;
        vcr.pfxd.sat[3] = satw;
        vcr.pfxd.msk[0] = mskx != 0;
        vcr.pfxd.msk[1] = msky != 0;
        vcr.pfxd.msk[2] = mskz != 0;
        vcr.pfxd.msk[3] = mskw != 0;
        vcr.pfxd.enabled = true;
    }

    // VFPU5:VIIM
    public void doVIIM(int vd, int imm16) {
        v3[0] = (float) imm16;

        saveVd(1, vd, v3);
    }

    // VFPU5:VFIM
    public void doVFIM(int vd, int imm16) {
        float s = ((imm16 >> 15) == 0) ? 1.0f : -1.0f;
        int e = ((imm16 >> 10) & 0x1f);
        int m = (e == 0) ? ((imm16 & 0x3ff) << 1) : ((imm16 & 0x3ff) | 0x400);

        v3[0] = s * ((float) m) / ((float) (1 << e)) / ((float) (1 << 41));

        saveVd(1, vd, v3);
    }

    // group VFPU6   
    // VFPU6:VMMUL
    public void doVMMUL(int vsize, int vd, int vs, int vt) {
        // you must do it for disasm, not for emulation !
        //vs = vs ^ 32;

        // not sure :(
        for (int i = 0; i < vsize; ++i) {
            loadVt(vsize, vt + i);
            for (int j = 0; j < vsize; ++j) {
                loadVs(vsize, vs + j);
                float dot = v1[0] * v2[0];
                for (int k = 1; k < vsize; ++k) {
                    dot += v1[k] * v2[k];
                }
                v3[j] = dot;
            }
            saveVd(vsize, vd + i, v3);
        }
    }

    // VFPU6:VHTFM2
    public void doVHTFM2(int vd, int vs, int vt) {
        // not sure :(
        float dot0;
        float dot1;
        loadVt(1, vt);
        loadVs(2, vs + 0);
        dot0 = v1[0] * v2[0];
        dot1 = v1[1] * v2[0];
        loadVs(2, vs + 1);
        v3[0] = dot0 + v1[0];
        v3[1] = dot1 + v1[1];
        saveVd(2, vd, v3);
    }

    // VFPU6:VTFM2
    public void doVTFM2(int vd, int vs, int vt) {
        // not sure :(
        float dot0;
        float dot1;
        loadVt(2, vt);
        loadVs(2, vs + 0);
        dot0 = v1[0] * v2[0];
        dot1 = v1[1] * v2[0];
        loadVs(2, vs + 1);
        v3[0] = dot0 + v1[0] * v2[1];
        v3[1] = dot1 + v1[1] * v2[1];
        saveVd(2, vd, v3);
    }

    // VFPU6:VHTFM3
    public void doVHTFM3(int vd, int vs, int vt) {
        // not sure :(
        float dot0;
        float dot1;
        float dot2;
        loadVt(2, vt);
        loadVs(3, vs + 0);
        dot0 = v1[0] * v2[0];
        dot1 = v1[1] * v2[0];
        dot2 = v1[2] * v2[0];
        loadVs(3, vs + 1);
        dot0 += v1[0] * v2[1];
        dot1 += v1[1] * v2[1];
        dot2 += v1[2] * v2[1];
        loadVs(3, vs + 2);
        v3[0] = dot0 + v1[0];
        v3[1] = dot1 + v1[1];
        v3[2] = dot2 + v1[2];
        saveVd(3, vd, v3);
    }

    // VFPU6:VTFM3
    public void doVTFM3(int vd, int vs, int vt) {
        // not sure :(
        float dot0;
        float dot1;
        float dot2;
        loadVt(3, vt);
        loadVs(3, vs + 0);
        dot0 = v1[0] * v2[0];
        dot1 = v1[1] * v2[0];
        dot2 = v1[2] * v2[0];
        loadVs(3, vs + 1);
        dot0 += v1[0] * v2[1];
        dot1 += v1[1] * v2[1];
        dot2 += v1[2] * v2[1];
        loadVs(3, vs + 2);
        v3[0] = dot0 + v1[0] * v2[2];
        v3[1] = dot1 + v1[1] * v2[2];
        v3[2] = dot2 + v1[2] * v2[2];
        saveVd(3, vd, v3);
    }

    // VFPU6:VHTFM4
    public void doVHTFM4(int vd, int vs, int vt) {
        // not sure :(
        float dot0;
        float dot1;
        float dot2;
        float dot3;
        loadVt(3, vt);
        loadVs(4, vs + 0);
        dot0 = v1[0] * v2[0];
        dot1 = v1[1] * v2[0];
        dot2 = v1[2] * v2[0];
        dot3 = v1[3] * v2[0];
        loadVs(4, vs + 1);
        dot0 += v1[0] * v2[1];
        dot1 += v1[1] * v2[1];
        dot2 += v1[2] * v2[1];
        dot3 += v1[3] * v2[1];
        loadVs(4, vs + 2);
        dot0 += v1[0] * v2[2];
        dot1 += v1[1] * v2[2];
        dot2 += v1[2] * v2[2];
        dot3 += v1[3] * v2[2];
        loadVs(4, vs + 3);
        v3[0] = dot0 + v1[0];
        v3[1] = dot1 + v1[1];
        v3[2] = dot2 + v1[2];
        v3[3] = dot3 + v1[3];
        saveVd(4, vd, v3);
    }

    // VFPU6:VTFM4
    public void doVTFM4(int vd, int vs, int vt) {
        // not sure :(
        float dot0;
        float dot1;
        float dot2;
        float dot3;
        loadVt(4, vt);
        loadVs(4, vs + 0);
        dot0 = v1[0] * v2[0];
        dot1 = v1[1] * v2[0];
        dot2 = v1[2] * v2[0];
        dot3 = v1[3] * v2[0];
        loadVs(4, vs + 1);
        dot0 += v1[0] * v2[1];
        dot1 += v1[1] * v2[1];
        dot2 += v1[2] * v2[1];
        dot3 += v1[3] * v2[1];
        loadVs(4, vs + 2);
        dot0 += v1[0] * v2[2];
        dot1 += v1[1] * v2[2];
        dot2 += v1[2] * v2[2];
        dot3 += v1[3] * v2[2];
        loadVs(4, vs + 3);
        v3[0] = dot0 + v1[0] * v2[3];
        v3[1] = dot1 + v1[1] * v2[3];
        v3[2] = dot2 + v1[2] * v2[3];
        v3[3] = dot3 + v1[3] * v2[3];
        saveVd(4, vd, v3);
    }

    // VFPU6:VMSCL
    public void doVMSCL(int vsize, int vd, int vs, int vt) {
        for (int i = 0; i < vsize; ++i) {
            this.doVSCL(vsize, vd + i, vs + i, vt);
        }
    }

    // VFPU6:VCRSP
    public void doVCRSP(int vd, int vs, int vt) {
        loadVs(3, vs);
        loadVt(3, vt);

        v3[0] = +v1[1] * v2[2] - v1[2] * v2[1];
        v3[1] = +v1[2] * v2[0] - v1[0] * v2[2];
        v3[2] = +v1[0] * v2[1] - v1[1] * v2[0];

        saveVd(3, vd, v3);
    }

    // VFPU6:VQMUL
    public void doVQMUL(int vd, int vs, int vt) {
        loadVs(4, vs);
        loadVt(4, vt);

        v3[0] = +v1[0] * v2[3] + v1[1] * v2[2] - v1[2] * v2[1] + v1[3] * v2[0];
        v3[1] = -v1[0] * v2[2] + v1[1] * v2[3] + v1[2] * v2[0] + v1[3] * v2[1];
        v3[2] = +v1[0] * v2[1] - v1[1] * v2[0] + v1[2] * v2[3] + v1[3] * v2[2];
        v3[3] = -v1[0] * v2[0] - v1[1] * v2[1] - v1[2] * v2[2] + v1[3] * v2[3];

        saveVd(4, vd, v3);
    }

    // VFPU6:VMMOV
    public void doVMMOV(int vsize, int vd, int vs) {
        for (int i = 0; i < vsize; ++i) {
            this.doVMOV(vsize, vd + i, vs + i);
        }
    }

    // VFPU6:VMIDT
    public void doVMIDT(int vsize, int vd) {
        for (int i = 0; i < vsize; ++i) {
            this.doVIDT(vsize, vd + i);
        }
    }

    // VFPU6:VMZERO
    public void doVMZERO(int vsize, int vd) {
        for (int i = 0; i < vsize; ++i) {
            this.doVZERO(vsize, vd + i);
        }
    }

    // VFPU7:VMONE
    public void doVMONE(int vsize, int vd) {
        for (int i = 0; i < vsize; ++i) {
            this.doVONE(vsize, vd + i);
        }
    }

    // VFPU6:VROT
    public void doVROT(int vsize, int vd, int vs, int imm5) {
        loadVs(1, vs);

        double a = 0.5 * Math.PI * v1[0];
        double ca = Math.cos(a);
        double sa = Math.sin(a);

        int i;
        int si = (imm5 >>> 2) & 3;
        int ci = (imm5 >>> 0) & 3;

        if (((imm5 & 16) != 0)) {
            sa = 0.0 - sa;
        }

        if (si == ci) {
            for (i = 0; i < vsize; ++i) {
                v3[i] = (float) sa;
            }
        } else {
            for (i = 0; i < vsize; ++i) {
                v3[i] = (float) 0.0;
            }
            v3[si] = (float) sa;
        }
        v3[ci] = (float) ca;

        saveVd(vsize, vd, v3);
    }

    // group VLSU     
    // LSU:LVS
    public void doLVS(int vt, int rs, int simm14_a16) {
        int s = (vt >> 5) & 3;
        int m = (vt >> 2) & 7;
        int i = (vt >> 0) & 3;

        vpr[m][i][s] = Float.intBitsToFloat(memory.read32(gpr[rs] + simm14_a16));
    }

    // LSU:SVS
    public void doSVS(int vt, int rs, int simm14_a16) {
        int s = (vt >> 5) & 3;
        int m = (vt >> 2) & 7;
        int i = (vt >> 0) & 3;

        if (CHECK_ALIGNMENT) {
            int address = gpr[rs] + simm14_a16;
            if ((address & 3) != 0) {
                Memory.log.error(String.format("SV.S unaligned addr:0x%08x pc:0x%08x", address, pc));
            }
        }

        memory.write32(gpr[rs] + simm14_a16, Float.floatToRawIntBits(vpr[m][i][s]));
    }

    // LSU:LVQ
    public void doLVQ(int vt, int rs, int simm14_a16) {
        int m = (vt >> 2) & 7;
        int i = (vt >> 0) & 3;

        int address = gpr[rs] + simm14_a16;

        if (CHECK_ALIGNMENT) {
            if ((address & 15) != 0) {
                Memory.log.error(String.format("LV.Q unaligned addr:0x%08x pc:0x%08x", address, pc));
            }
        }

        if ((vt & 32) != 0) {
            for (int j = 0; j < 4; ++j) {
                vpr[m][j][i] = Float.intBitsToFloat(memory.read32(address + j * 4));
            }
        } else {
            for (int j = 0; j < 4; ++j) {
                vpr[m][i][j] = Float.intBitsToFloat(memory.read32(address + j * 4));
            }
        }
    }

    // LSU:LVLQ
    public void doLVLQ(int vt, int rs, int simm14_a16) {
        int m = (vt >> 2) & 7;
        int i = (vt >> 0) & 3;

        int address = gpr[rs] + simm14_a16 - 12;
        //Memory.log.error("Forbidden LVL.Q");

        if (CHECK_ALIGNMENT) {
            if ((address & 3) != 0) {
                Memory.log.error(String.format("LVL.Q unaligned addr:0x%08x pc:0x%08x", address, pc));
            }
        }

        /* I assume it should be something like that :
        Mem = 4321
        Reg = wzyx
        
        0   1 z y x 
        1   2 1 y x
        2   3 2 1 x
        3   4 3 2 1
         */

        int k = 4 - ((address >> 2) & 3);

        if ((vt & 32) != 0) {
            for (int j = 0; j < k; ++j) {
                vpr[m][j][i] = Float.intBitsToFloat(memory.read32(address));
                address += 4;
            }
        } else {
            for (int j = 0; j < k; ++j) {
                vpr[m][i][j] = Float.intBitsToFloat(memory.read32(address));
                address += 4;
            }
        }
    }

    // LSU:LVRQ
    public void doLVRQ(int vt, int rs, int simm14_a16) {
        int m = (vt >> 2) & 7;
        int i = (vt >> 0) & 3;

        int address = gpr[rs] + simm14_a16;
        //Memory.log.error("Forbidden LVR.Q");

        if (CHECK_ALIGNMENT) {
            if ((address & 3) != 0) {
                Memory.log.error(String.format("LVR.Q unaligned addr:0x%08x pc:0x%08x", address, pc));
            }
        }

        /* I assume it should be something like that :
        Mem = 4321
        Reg = wzyx
        
        0   4 3 2 1 
        1   w 4 3 2
        2   w z 4 3
        3   w z y 4
         */

        int k = (address >> 2) & 3;
        address += (4 - k) << 2;
        if ((vt & 32) != 0) {
            for (int j = 4 - k; j < 4; ++j) {
                vpr[m][j][i] = Float.intBitsToFloat(memory.read32(address));
                address += 4;
            }
        } else {
            for (int j = 4 - k; j < 4; ++j) {
                vpr[m][i][j] = Float.intBitsToFloat(memory.read32(address));
                address += 4;
            }
        }
    }
    // LSU:SVQ
    public void doSVQ(int vt, int rs, int simm14_a16) {
        int m = (vt >> 2) & 7;
        int i = (vt >> 0) & 3;

        int address = gpr[rs] + simm14_a16;

        if (CHECK_ALIGNMENT) {
            if ((address & 15) != 0) {
                Memory.log.error(String.format("SV.Q unaligned addr:0x%08x pc:0x%08x", address, pc));
            }
        }

        if ((vt & 32) != 0) {
            for (int j = 0; j < 4; ++j) {
                memory.write32((address + j * 4), Float.floatToRawIntBits(vpr[m][j][i]));
            }
        } else {
            for (int j = 0; j < 4; ++j) {
                memory.write32((address + j * 4), Float.floatToRawIntBits(vpr[m][i][j]));
            }
        }
    }

    // LSU:SVLQ
    public void doSVLQ(int vt, int rs, int simm14_a16) {
        int m = (vt >> 2) & 7;
        int i = (vt >> 0) & 3;

        int address = gpr[rs] + simm14_a16 - 12;

        if (CHECK_ALIGNMENT) {
            if ((address & 3) != 0) {
                Memory.log.error(String.format("SVL.Q unaligned addr:0x%08x pc:0x%08x", address, pc));
            }
        }

        int k = 4 - ((address >> 2) & 3);

        if ((vt & 32) != 0) {
            for (int j = 0; j < k; ++j) {
                memory.write32((address), Float.floatToRawIntBits(vpr[m][j][i]));
                address += 4;
            }
        } else {
            for (int j = 0; j < k; ++j) {
                memory.write32((address), Float.floatToRawIntBits(vpr[m][i][j]));
                address += 4;
            }
        }
    }

    // LSU:SVRQ
    public void doSVRQ(int vt, int rs, int simm14_a16) {
        int m = (vt >> 2) & 7;
        int i = (vt >> 0) & 3;

        int address = gpr[rs] + simm14_a16;

        if (CHECK_ALIGNMENT) {
            if ((address & 3) != 0) {
                Memory.log.error(String.format("SVR.Q unaligned addr:0x%08x pc:0x%08x", address, pc));
            }
        }

        int k = (address >> 2) & 3;
        address += (4 - k) << 2;
        if ((vt & 32) != 0) {
            for (int j = 4 - k; j < 4; ++j) {
                memory.write32((address), Float.floatToRawIntBits(vpr[m][j][i]));
                address += 4;
            }
        } else {
            for (int j = 4 - k; j < 4; ++j) {
                memory.write32((address), Float.floatToRawIntBits(vpr[m][i][j]));
                address += 4;
            }
        }
    }
}


