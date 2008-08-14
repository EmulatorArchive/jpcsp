/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jpcsp.Debugger.DisassemblerModule;
import static jpcsp.Debugger.DisassemblerModule.DisHelper.*;
import jpcsp.AllegrexInstructions;
import jpcsp.Decoder;

/**
 *
 * @author shadow
 */
public class DisasmOpcodes implements AllegrexInstructions {
   private final Decoder disasm = new Decoder();
    int opcode_address;
    String returnString = "Unsupported Instruction"; // set default to unsupported
    public String disasm(int value,int opcode_address)
    {
      this.opcode_address = opcode_address;
      returnString = "Unsupported Instruction";
      disasm.process(this, value);
      return returnString;
    }
    @Override
    public void doUNK(String reason){ returnString = reason; }
    @Override
    public void doNOP(){ returnString = "nop"; }
    @Override
    public void doSLL(int rd, int rt, int sa){ returnString = Dis_RDRTSA("sll",rd,rt,sa); }
    @Override
    public void doSRL(int rd, int rt, int sa){ returnString = Dis_RDRTSA("srl",rd,rt,sa); }
    @Override
    public void doSRA(int rd, int rt, int sa){ returnString =Dis_RDRTSA("sra",rd,rt,sa);  }
    @Override
    public void doSLLV(int rd, int rt, int rs){returnString =Dis_RDRTRS("sllv",rd,rt,rs); }
    @Override
    public void doSRLV(int rd, int rt, int rs){returnString =Dis_RDRTRS("srlv",rd,rt,rs);  }
    @Override
    public void doSRAV(int rd, int rt, int rs){ returnString =Dis_RDRTRS("srav",rd,rt,rs); }
    @Override
    public void doJR(int rs){ returnString =Dis_RS("jr",rs); }
    @Override
    public void doJALR(int rd, int rs){ returnString = Dis_RDRS("jalr",rd,rs); } 
    @Override
    public void doMFHI(int rd){returnString = Dis_RD("mfhi",rd); }
    @Override
    public void doMTHI(int rs){returnString = Dis_RS("mthi",rs); }
    @Override
    public void doMFLO(int rd){returnString = Dis_RD("mflo",rd); }   
    @Override
    public void doMTLO(int rs){returnString = Dis_RS("mtlo",rs); }
    @Override
    public void doMULT(int rs, int rt){ returnString =Dis_RSRT("mult",rs,rt);  }
    @Override
    public void doMULTU(int rs, int rt){ returnString =Dis_RSRT("multu",rs,rt); }
    @Override
    public void doDIV(int rs, int rt){ returnString =Dis_RSRT("div",rs,rt); }  
    @Override
    public void doDIVU(int rs, int rt){returnString = Dis_RSRT("divu",rs,rt);  }  
    @Override
    public void doADD(int rd, int rs, int rt){ returnString =Dis_RDRSRT("add",rd,rs,rt); }
    @Override
    public void doADDU(int rd, int rs, int rt){ returnString =Dis_RDRSRT("addu",rd,rs,rt); }  
    @Override
    public void doSUB(int rd, int rs, int rt){ returnString =Dis_RDRSRT("sub",rd,rs,rt); }
    @Override
    public void doSUBU(int rd, int rs, int rt){ returnString =Dis_RDRSRT("subu",rd,rs,rt); }
    @Override
    public void doAND(int rd, int rs, int rt){ returnString =Dis_RDRSRT("and",rd,rs,rt); }
    @Override
    public void doOR(int rd, int rs, int rt){ returnString =Dis_RDRSRT("or",rd,rs,rt); }
    @Override
    public void doXOR(int rd, int rs, int rt){ returnString =Dis_RDRSRT("xor",rd,rs,rt); }
    @Override
    public void doNOR(int rd, int rs, int rt){ returnString =Dis_RDRSRT("nor",rd,rs,rt); }
    @Override
    public void doSLT(int rd, int rs, int rt){ returnString =Dis_RDRSRT("slt",rd,rs,rt); }
    @Override
    public void doSLTU(int rd, int rs, int rt){returnString =Dis_RDRSRT("sltu",rd,rs,rt); }
    @Override
    public void doBLTZ(int rs, int simm16){ returnString = Dis_RSOFFSET("bltz",rs,simm16,opcode_address); }  
    @Override
    public void doBGEZ(int rs, int simm16){ returnString = Dis_RSOFFSET("bgez",rs,simm16,opcode_address); }  
    @Override
    public void doBLTZL(int rs, int simm16){ returnString = Dis_RSOFFSET("bltzl",rs,simm16,opcode_address); } 
    @Override
    public void doBGEZL(int rs, int simm16){ returnString = Dis_RSOFFSET("bgezl",rs,simm16,opcode_address); }  
    @Override
    public void doBLTZAL(int rs, int simm16){ returnString = Dis_RSOFFSET("bltzal",rs,simm16,opcode_address); } 
    @Override
    public void doBGEZAL(int rs, int simm16){ returnString = Dis_RSOFFSET("bgezal",rs,simm16,opcode_address); }   
    @Override
    public void doBLTZALL(int rs, int simm16){ returnString = Dis_RSOFFSET("bltzall",rs,simm16,opcode_address); } 
    @Override
    public void doBGEZALL(int rs, int simm16){ returnString = Dis_RSOFFSET("bgezall",rs,simm16,opcode_address); }  
    @Override
    public void doJ(int uimm26){ returnString = Dis_JUMP("j",uimm26,opcode_address); }  
    @Override
    public void doJAL(int uimm26){ returnString = Dis_JUMP("jal",uimm26,opcode_address); }
    @Override
    public void doBEQ(int rs, int rt, int simm16){ returnString = Dis_RSRTOFFSET("beq",rs,rt,simm16,opcode_address); } 
    @Override
    public void doBNE(int rs, int rt, int simm16){ returnString = Dis_RSRTOFFSET("bne",rs,rt,simm16,opcode_address);  }    
    @Override
    public void doBLEZ(int rs, int simm16){ returnString = Dis_RSOFFSET("blez",rs,simm16,opcode_address); }    
    @Override
    public void doBGTZ(int rs, int simm16){ returnString = Dis_RSOFFSET("bgtz",rs,simm16,opcode_address); }    
    @Override
    public void doBEQL(int rs, int rt, int simm16){ returnString = Dis_RSRTOFFSET("beql",rs,rt,simm16,opcode_address);  }   
    @Override
    public void doBNEL(int rs, int rt, int simm16){ returnString = Dis_RSRTOFFSET("bnel",rs,rt,simm16,opcode_address);  }   
    @Override
    public void doBLEZL(int rs, int simm16){ returnString = Dis_RSOFFSET("blezl",rs,simm16,opcode_address); }
    @Override
    public void doBGTZL(int rs, int simm16){ returnString = Dis_RSOFFSET("bgtzl",rs,simm16,opcode_address); }
    @Override
    public void doADDI(int rt, int rs, int simm16){ returnString = Dis_RTRSIMM("addi",rt,rs,simm16); }
    @Override
    public void doADDIU(int rt, int rs, int simm16){ returnString = Dis_RTRSIMM("addiu",rt,rs,simm16); }
    @Override
    public void doSLTI(int rt, int rs, int simm16){ returnString = Dis_RTRSIMM("slti",rt,rs,simm16); } 
    @Override
    public void doSLTIU(int rt, int rs, int simm16){ returnString = Dis_RTRSIMM("sltiu",rt,rs,simm16); }  
    @Override
    public void doANDI(int rt, int rs, int uimm16){ returnString = Dis_RTRSIMM("andi",rt,rs,uimm16); }  
    @Override
    public void doORI(int rt, int rs, int uimm16){ returnString = Dis_RTRSIMM("ori",rt,rs,uimm16); }    
    @Override
    public void doXORI(int rt, int rs, int uimm16){ returnString = Dis_RTRSIMM("xori",rt,rs,uimm16); }  
    @Override
    public void doLUI(int rt, int uimm16){ returnString = Dis_RTIMM("lui",rt,uimm16);  }
    @Override
    public void doHALT(){ returnString =  "halt"; }    
    @Override
    public void doMFIC(int rt){ returnString = Dis_RT("mfic",rt);}
    @Override
    public void doMTIC(int rt){ returnString = Dis_RT("mtic",rt); }
    @Override
    public void doMFC0(int rt, int c0dr){ returnString = "mfc0 " + gprNames[rt] + ", " + cop0Names[c0dr]; }  
    @Override
    public void doCFC0(int rt, int c0cr){ returnString = "cfc0 ???"; /*+ gprNames[rt] + ", " + cop0Names[c0cr]; */}  //does that actually exists??
    @Override
    public void doMTC0(int rt, int c0dr){ returnString = "mtc0 " + gprNames[rt] + ", " + cop0Names[c0dr]; }   
    @Override
    public void doCTC0(int rt, int c0cr){ returnString = "ctc0 ???"; /* + gprNames[rt] + ", " + cop0Names[c0cr]; */} //does that actually exists??
    @Override
    public void doERET(){ returnString= "eret"; } 
    @Override
    public void doLB(int rt, int rs, int simm16){ returnString = Dis_RTIMMRS("lb",rt,rs,simm16); }
    @Override
    public void doLBU(int rt, int rs, int simm16){ returnString = Dis_RTIMMRS("lbu",rt,rs,simm16);}
    @Override
    public void doLH(int rt, int rs, int simm16){returnString = Dis_RTIMMRS("lh",rt,rs,simm16);  }
    @Override
    public void doLHU(int rt, int rs, int simm16){ returnString = Dis_RTIMMRS("lhu",rt,rs,simm16); }
    @Override
    public void doLWL(int rt, int rs, int simm16){returnString = Dis_RTIMMRS("lwl",rt,rs,simm16);  }
    @Override
    public void doLW(int rt, int rs, int simm16){ returnString = Dis_RTIMMRS("lw",rt,rs,simm16); }
    @Override
    public void doLWR(int rt, int rs, int simm16){ returnString = Dis_RTIMMRS("lwr",rt,rs,simm16); }
    @Override
    public void doSB(int rt, int rs, int simm16){returnString = Dis_RTIMMRS("sb",rt,rs,simm16);  }
    @Override
    public void doSH(int rt, int rs, int simm16){ returnString = Dis_RTIMMRS("sh",rt,rs,simm16); }
    @Override
    public void doSWL(int rt, int rs, int simm16){ returnString = Dis_RTIMMRS("swl",rt,rs,simm16); }
    @Override
    public void doSW(int rt, int rs, int simm16){ returnString = Dis_RTIMMRS("sw",rt,rs,simm16); }
    @Override
    public void doSWR(int rt, int rs, int simm16){ returnString = Dis_RTIMMRS("swr",rt,rs,simm16); }
    @Override
    public void doCACHE(int rt, int rs, int simm16)
    {        
        returnString = "cache " + Integer.toHexString(rt) + ", " + simm16 + "(" + gprNames[rs] + ")"; 
    } 
    @Override
    public void doLL(int rt, int rs, int simm16){ returnString = Dis_RTIMMRS("ll",rt,rs,simm16); }
    @Override
    public void doSC(int rt, int rs, int simm16){ returnString = Dis_RTIMMRS("sc",rt,rs,simm16); }
    @Override
    public void doROTR(int rd, int rt, int sa){ returnString = Dis_RDRTSA("rotr",rd,rt,sa);  } 
    @Override
    public void doROTRV(int rd, int rt, int rs){ returnString = Dis_RDRTRS("rotrv",rd,rt,rs); } 
    @Override
    public void doMOVZ(int rd, int rs, int rt){ returnString = Dis_RDRSRT("movz",rd,rs,rt); } 
    @Override
    public void doMOVN(int rd, int rs, int rt){ returnString = Dis_RDRSRT("movn",rd,rs,rt); } 
    @Override
    public void doSYSCALL(int code){ returnString = Dis_Syscall(code); }    
    @Override
    public void doBREAK(int code){ returnString = Dis_Break(code); }  
    @Override
    public void doSYNC(){ returnString = "sync"; }
    @Override
    public void doCLZ(int rd, int rs){ returnString = Dis_RDRS("clz",rd,rs); }   
    @Override
    public void doCLO(int rd, int rs){ returnString = Dis_RDRS("clo",rd,rs); }   
    @Override
    public void doMADD(int rs, int rt){ returnString = Dis_RSRT("madd",rs,rt); }  
    @Override
    public void doMADDU(int rs, int rt){ returnString = Dis_RSRT("maddu",rs,rt); } 
    @Override
    public void doMAX(int rd, int rs, int rt){ returnString = Dis_RDRSRT("max",rd,rs,rt); }
    @Override
    public void doMIN(int rd, int rs, int rt){ returnString = Dis_RDRSRT("min",rd,rs,rt); }  
    @Override
    public void doMSUB(int rs, int rt){ returnString = Dis_RSRT("msub",rs,rt); }  
    @Override
    public void doMSUBU(int rs, int rt){ returnString = Dis_RSRT("msubu",rs,rt); }  
    @Override
    public void doEXT(int rt, int rs, int rd, int sa)
    {  
     returnString = "ext " + gprNames[rt] + ", " + gprNames[rs] + ", " + sa + ", " + (rd + 1);
    }
    @Override
    public void doINS(int rt, int rs, int rd, int sa)
    {  
      returnString = "ins " + gprNames[rt] + ", " + gprNames[rs] + ", " + sa + ", " + (rd - sa + 1);           
    }
    @Override
    public void doWSBH(int rd, int rt){ returnString = Dis_RDRT("wsbh",rd,rt); } 
    @Override
    public void doWSBW(int rd, int rt){ returnString = Dis_RDRT("wsbw",rd,rt); }
    @Override
    public void doSEB(int rd, int rt){ returnString = Dis_RDRT("seb",rd,rt); }   
    @Override
    public void doBITREV(int rd, int rt){ returnString = Dis_RDRT("bitrev",rd,rt); }  
    @Override
    public void doSEH(int rd, int rt){ returnString = Dis_RDRT("seh",rd,rt); }
        //COP1 instructions
    @Override
    public void doMFC1(int rt, int c1dr){returnString = "mtc1 " + gprNames[rt] + ", " + fprNames[c1dr];}
    @Override
   
    public void doCFC1(int rt, int c1cr){returnString = "cfc1 " + gprNames[rt] + ", " + fcrNames[c1cr];}
    @Override
    public void doMTC1(int rt, int c1dr){returnString = "mtc1 " + gprNames[rt] + ", " + fprNames[c1dr];}  
    @Override
    public void doCTC1(int rt, int c1cr){returnString = "ctc1 " + gprNames[rt] + ", " + fcrNames[c1cr];}
       @Override
    public void doBC1F(int simm16){returnString = Dis_OFFSET("bc1f",simm16,opcode_address);}
    @Override
    public void doBC1T (int simm16){returnString = Dis_OFFSET("bc1t",simm16,opcode_address);}
    @Override
    public void doBC1FL(int simm16){returnString = Dis_OFFSET("bc1fl",simm16,opcode_address);}
    @Override
    public void doBC1TL(int simm16){returnString = Dis_OFFSET("bc1tl",simm16,opcode_address);}
    @Override
    public void doADDS(int fd , int fs ,int ft){returnString = Dis_FDFSFT("add.s",fd,fs,ft);}
    @Override
    public void doSUBS(int fd , int fs ,int ft){returnString = Dis_FDFSFT("sub.s",fd,fs,ft);} 
    @Override
    public void doMULS(int fd , int fs ,int ft){returnString = Dis_FDFSFT("mul.s",fd,fs,ft);} 
    @Override
    public void doDIVS(int fd , int fs ,int ft){returnString = Dis_FDFSFT("div.s",fd,fs,ft);} 
    @Override
    public void doSQRTS(int fd,int fs){returnString = Dis_FDFS("sqrt.s",fd,fs);}
    @Override
    public void doABSS(int fd,int fs){returnString = Dis_FDFS("abs.s",fd,fs);}
    @Override
    public void doMOVS(int fd,int fs){returnString = Dis_FDFS("mov.s",fd,fs);}
    @Override
    public void doNEGS(int fd,int fs){returnString = Dis_FDFS("neg.s",fd,fs);}
    @Override
    public void doROUNDWS(int fd,int fs){returnString = Dis_FDFS("round.w.s",fd,fs);}
    @Override
    public void doTRUNCWS(int fd,int fs){returnString = Dis_FDFS("trunc.w.s",fd,fs);}
    @Override
    public void doCEILWS(int fd,int fs){returnString = Dis_FDFS("ceil.w.s",fd,fs);}
    @Override
    public void doFLOORWS(int fd,int fs){returnString = Dis_FDFS("floor.w.s",fd,fs);}
    @Override
    public void doCVTSW(int fd,int fs){returnString = Dis_FDFS("cvt.s.w",fd,fs);}
    @Override
    public void doCVTWS(int fd,int fs){returnString = Dis_FDFS("cvt.w.s",fd,fs);}
    @Override
    public void doCF(int fs,int ft){returnString = Dis_FSFT("c.f",fs,ft);}
    @Override
    public void doCUN(int fs,int ft){returnString = Dis_FSFT("c.un",fs,ft);}
    @Override
    public void doCEQ(int fs,int ft){returnString = Dis_FSFT("c.eq",fs,ft);}
    @Override
    public void doCUEQ(int fs,int ft){returnString = Dis_FSFT("c.ueq",fs,ft);}
    @Override
    public void doCOLT(int fs,int ft){returnString = Dis_FSFT("c.olt",fs,ft);}
    @Override
    public void doCULT(int fs,int ft){returnString = Dis_FSFT("c.ult",fs,ft);}
    @Override
    public void doCOLE(int fs,int ft){returnString = Dis_FSFT("c.ole",fs,ft);}
    @Override
    public void doCULE(int fs,int ft){returnString = Dis_FSFT("c.ule",fs,ft);}
    @Override
    public void doCSF(int fs,int ft){returnString = Dis_FSFT("c.sf",fs,ft);}
    @Override
    public void doCNGLE(int fs,int ft){returnString = Dis_FSFT("c.ngle",fs,ft);}
    @Override
    public void doCSEQ(int fs,int ft){returnString = Dis_FSFT("c.seq",fs,ft);}
    @Override
    public void doCNGL(int fs,int ft){returnString = Dis_FSFT("c.ngl",fs,ft);}
    @Override
    public void doCLT(int fs,int ft){returnString = Dis_FSFT("c.lt",fs,ft);}
    @Override
    public void doCNGE(int fs,int ft){returnString = Dis_FSFT("c.nge",fs,ft);}
    @Override
    public void doCLE(int fs,int ft){returnString = Dis_FSFT("c.cle",fs,ft);}
    @Override
    public void doCNGT(int fs,int ft){returnString = Dis_FSFT("c.cngt",fs,ft);}
}
