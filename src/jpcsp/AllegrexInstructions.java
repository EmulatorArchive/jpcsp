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
package jpcsp;

/**
 *
 * @author hli
 */
public interface AllegrexInstructions {

    public void doUNK(String reason);

    public void doNOP();

    public void doSLL(int rd, int rt, int sa);

    public void doSRL(int rd, int rt, int sa);

    public void doSRA(int rd, int rt, int sa);

    public void doSLLV(int rd, int rt, int rs);

    public void doSRLV(int rd, int rt, int rs);

    public void doSRAV(int rd, int rt, int rs);

    public void doJR(int rs);

    public void doJALR(int rd, int rs);

    public void doMFHI(int rd);

    public void doMTHI(int rs);

    public void doMFLO(int rd);

    public void doMTLO(int rs);

    public void doMULT(int rs, int rt);

    public void doMULTU(int rs, int rt);

    public void doDIV(int rs, int rt);

    public void doDIVU(int rs, int rt);

    public void doADD(int rd, int rs, int rt);

    public void doADDU(int rd, int rs, int rt);

    public void doSUB(int rd, int rs, int rt);

    public void doSUBU(int rd, int rs, int rt);

    public void doAND(int rd, int rs, int rt);

    public void doOR(int rd, int rs, int rt);

    public void doXOR(int rd, int rs, int rt);

    public void doNOR(int rd, int rs, int rt);

    public void doSLT(int rd, int rs, int rt);

    public void doSLTU(int rd, int rs, int rt);

    public void doBLTZ(int rs, int simm16);

    public void doBGEZ(int rs, int simm16);

    public void doBLTZL(int rs, int simm16);

    public void doBGEZL(int rs, int simm16);

    public void doBLTZAL(int rs, int simm16);

    public void doBGEZAL(int rs, int simm16);

    public void doBLTZALL(int rs, int simm16);

    public void doBGEZALL(int rs, int simm16);

    public void doJ(int uimm26);

    public void doJAL(int uimm26);

    public void doBEQ(int rs, int rt, int simm16);

    public void doBNE(int rs, int rt, int simm16);

    public void doBLEZ(int rs, int simm16);

    public void doBGTZ(int rs, int simm16);

    public void doBEQL(int rs, int rt, int simm16);

    public void doBNEL(int rs, int rt, int simm16);

    public void doBLEZL(int rs, int simm16);

    public void doBGTZL(int rs, int simm16);

    public void doADDI(int rt, int rs, int simm16);

    public void doADDIU(int rt, int rs, int simm16);

    public void doSLTI(int rt, int rs, int simm16);

    public void doSLTIU(int rt, int rs, int simm16);

    public void doANDI(int rt, int rs, int uimm16);

    public void doORI(int rt, int rs, int uimm16);

    public void doXORI(int rt, int rs, int uimm16);

    public void doLUI(int rt, int uimm16);

    public void doHALT();

    public void doMFIC(int rt);

    public void doMTIC(int rt);

    public void doMFC0(int rt, int c0dr);

    public void doCFC0(int rt, int c0cr);

    public void doMTC0(int rt, int c0dr);

    public void doCTC0(int rt, int c0cr);

    public void doERET();

    public void doLB(int rt, int rs, int simm16);

    public void doLBU(int rt, int rs, int simm16);

    public void doLH(int rt, int rs, int simm16);

    public void doLHU(int rt, int rs, int simm16);

    public void doLWL(int rt, int rs, int simm16);

    public void doLW(int rt, int rs, int simm16);

    public void doLWR(int rt, int rs, int simm16);

    public void doSB(int rt, int rs, int simm16);

    public void doSH(int rt, int rs, int simm16);

    public void doSWL(int rt, int rs, int simm16);

    public void doSW(int rt, int rs, int simm16);

    public void doSWR(int rt, int rs, int simm16);

    public void doCACHE(int rt, int rs, int simm16);

    public void doLL(int rt, int rs, int simm16);

    public void doLWC1(int rt, int rs, int simm16);

    public void doLVS(int vt, int rs, int simm14);

    public void doSC(int rt, int rs, int simm16);

    public void doSWC1(int rt, int rs, int simm16);

    public void doSVS(int vt, int rs, int simm14);

    public void doROTR(int rd, int rt, int sa);

    public void doROTRV(int rd, int rt, int rs);

    public void doMOVZ(int rd, int rs, int rt);

    public void doMOVN(int rd, int rs, int rt);

    public void doSYSCALL(int code);

    public void doBREAK(int code);

    public void doSYNC();

    public void doCLZ(int rd, int rs);

    public void doCLO(int rd, int rs);

    public void doMADD(int rs, int rt);

    public void doMADDU(int rs, int rt);

    public void doMAX(int rd, int rs, int rt);

    public void doMIN(int rd, int rs, int rt);

    public void doMSUB(int rs, int rt);

    public void doMSUBU(int rs, int rt);

    public void doEXT(int rt, int rs, int rd, int sa);

    public void doINS(int rt, int rs, int rd, int sa);

    public void doWSBH(int rd, int rt);

    public void doWSBW(int rd, int rt);

    public void doSEB(int rd, int rt);

    public void doBITREV(int rd, int rt);

    public void doSEH(int rd, int rt);
    //COP1 instructions
    public void doMFC1(int rt, int c1dr);

    public void doCFC1(int rt, int c1cr);

    public void doMTC1(int rt, int c1dr);

    public void doCTC1(int rt, int c1cr);

    public void doBC1F(int simm16);

    public void doBC1T(int simm16);

    public void doBC1FL(int simm16);

    public void doBC1TL(int simm16);

    public void doADDS(int fd, int fs, int ft);

    public void doSUBS(int fd, int fs, int ft);

    public void doMULS(int fd, int fs, int ft);

    public void doDIVS(int fd, int fs, int ft);

    public void doSQRTS(int fd, int fs);

    public void doABSS(int fd, int fs);

    public void doMOVS(int fd, int fs);

    public void doNEGS(int fd, int fs);

    public void doROUNDWS(int fd, int fs);

    public void doTRUNCWS(int fd, int fs);

    public void doCEILWS(int fd, int fs);

    public void doFLOORWS(int fd, int fs);

    public void doCVTSW(int fd, int fs);

    public void doCVTWS(int fd, int fs);

    public void doCCONDS(int fs, int ft, int cond);

    // VFPU0
    public void doVADD(int vsize, int vd, int vs, int vt);

    public void doVSUB(int vsize, int vd, int vs, int vt);

    public void doVSBN(int vsize, int vd, int vs, int vt);

    public void doVDIV(int vsize, int vd, int vs, int vt);

    // VFPU1
    public void doVMUL(int vsize, int vd, int vs, int vt);

    public void doVDOT(int vsize, int vd, int vs, int vt);

    public void doVSCL(int vsize, int vd, int vs, int vt);

    public void doVHDP(int vsize, int vd, int vs, int vt);

    public void doVCRS(int vsize, int vd, int vs, int vt);

    public void doVDET(int vsize, int vd, int vs, int vt);

    // VFPU3
    public void doVCMP(int vsize, int vs, int vt, int cond);

    public void doVMIN(int vsize, int vd, int vs, int vt);

    public void doVMAX(int vsize, int vd, int vs, int vt);

    public void doVSCMP(int vsize, int vd, int vs, int vt);

    public void doVSGE(int vsize, int vd, int vs, int vt);

    public void doVSLT(int vsize, int vd, int vs, int vt);

    // VFPU5
    public void doVPFXS(int imm24);

    public void doVPFXT(int imm24);

    public void doVPFXD(int imm24);

    public void doVIIM(int vs, int imm16);

    public void doVFIM(int vs, int imm16);
}
