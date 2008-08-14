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

import static jpcsp.AllegrexInstructions.*;
import jpcsp.HLE.SyscallHandler;

public class Processor implements AllegrexInstructions {

    public int[]     gpr;
    public float[]   fpr;
    public float[]   vpr;
    public long      hilo;
    public int       pc, npc;
    public int       cycles;

    Processor() {
        Memory.get_instance(); //intialize memory
        reset();
    }

    public void reset() {
        // intialize psp register
        pc = npc = 0x00000000;
        hilo = 0;
        gpr = new int[32];
        fpr = new float[32];
        vpr = new float[128];
    }

    public int hi() {
        return (int) (hilo >>> 32);
    }

    public int lo() {
        return (int) (hilo & 0xffffffff);
    }

    public static int signExtend(int value) {
        return (value << 16) >> 16;
    }

    public static int signExtend8(int value) {
        return (value << 24) >> 24;
    }

    public static int zeroExtend(int value) {
        return (value & 0xffff);
    }

    public static int zeroExtend8(int value) {
        return (value & 0xff);
    }

    public static int signedCompare(int i, int j) {
        return (i - j) >> 31;
    }

    public static int unsignedCompare(int i, int j) {
        return ((i - j) ^ i ^ j) >> 31;
    }

    public static int branchTarget(int npc, int simm16) {
        return npc + (simm16 << 2);
    }

    public static int jumpTarget(int npc, int uimm26) {
        return (npc & 0xf0000000) | (uimm26 << 2);
    }

    private static boolean addSubOverflow(long value) {
        long tmp = value << (62 - 31);
        return ((tmp >>> 1) == (tmp & 1));
    }

    private final Decoder interpreter = new Decoder();

    public void step() {
        npc = pc + 4;

        int insn = Memory.get_instance().read32(pc);

        // by default, any Allegrex instruction takes 1 cycle at least
        cycles += 1;

        // by default, the next instruction to emulate is at the next address
        pc = npc;

        // process the current instruction
        interpreter.process(this, insn);
    }

    public void stepDelayslot() {
        int insn = Memory.get_instance().read32(pc);

        // by default, any Allegrex instruction takes 1 cycle at least
        cycles += 1;

        // by default, the next instruction to emulate is at the next address
        pc += 4;

        // process the current instruction
        interpreter.process(this, insn);

        pc = npc;
        npc = pc + 4;
    }

    @Override
    public void doUNK(String reason) {
        System.out.println(reason);
    }

    @Override
    public void doNOP() {
    }

    @Override
    public void doSLL(int rd, int rt, int sa) {
        if (rd != 0) {
            gpr[rd] = (gpr[rt] << sa);
        }
    }

    @Override
    public void doSRL(int rd, int rt, int sa) {
        if (rd != 0) {
            gpr[rd] = (gpr[rt] >>> sa);
        }
    }

    @Override
    public void doSRA(int rd, int rt, int sa) {
        if (rd != 0) {
            gpr[rd] = (gpr[rt] >> sa);
        }
    }

    @Override
    public void doSLLV(int rd, int rt, int rs) {
        if (rd != 0) {
            gpr[rd] = (gpr[rt] << (gpr[rs] & 31));
        }
    }

    @Override
    public void doSRLV(int rd, int rt, int rs) {
        if (rd != 0) {
            gpr[rd] = (gpr[rt] >>> (gpr[rs] & 31));
        }
    }

    @Override
    public void doSRAV(int rd, int rt, int rs) {
        if (rd != 0) {
            gpr[rd] = (gpr[rt] >>> (gpr[rs] & 31));
        }
    }

    @Override
    public void doJR(int rs) {
        int previous_cycles = cycles;
        npc = gpr[rs];
        stepDelayslot();
        if (cycles - previous_cycles < 2) {
            cycles = previous_cycles + 2;
        }
    }

    @Override
    public void doJALR(int rd, int rs) {
        int previous_cycles = cycles;
        if (rd != 0) {
            gpr[rd] = pc + 4;
        }
        npc = gpr[rs];
        stepDelayslot();
        if (cycles - previous_cycles < 2) {
            cycles = previous_cycles + 2;
        }
    }

    @Override
    public void doMFHI(int rd) {
        if (rd != 0) {
            gpr[rd] = hi();
        }
    // cycles ?
    }

    @Override
    public void doMTHI(int rs) {
        int hi = gpr[rs];
        hilo = (((long) hi) << 32) | (hilo & 0xffffffff);
    // cycles ?
    }

    @Override
    public void doMFLO(int rd) {
        if (rd != 0) {
            gpr[rd] = lo();
        }
    // cycles ?
    }

    @Override
    public void doMTLO(int rs) {
        int lo = gpr[rs];
        hilo = ((hilo >>> 32) << 32) | (((long) lo) & 0xffffffff);
    // cycles ?
    }

    @Override
    public void doMULT(int rs, int rt) {
        hilo = ((long) gpr[rs]) * ((long) gpr[rs]);
        cycles += 4;
    }

    @Override
    public void doMULTU(int rs, int rt) {
        hilo = (((long) gpr[rs]) & 0xffffffff) * (((long) gpr[rs]) & 0xffffffff);
        cycles += 4;
    }

    @Override
    public void doDIV(int rs, int rt) {
        int lo = gpr[rs] / gpr[rt];
        int hi = gpr[rs] % gpr[rt];
        hilo = ((long) hi) << 32 | (((long) lo) & 0xffffffff);
        cycles += 35;
    }

    @Override
    public void doDIVU(int rs, int rt) {
        long x = ((long) gpr[rs]) & 0xffffffff;
        long y = ((long) gpr[rt]) & 0xffffffff;
        int lo = (int) (x / y);
        int hi = (int) (x % y);
        hilo = ((long) hi) << 32 | (((long) lo) & 0xffffffff);
        cycles += 35;
    }

    @Override
    public void doADD(int rd, int rs, int rt) {
        if (rd != 0) {
            long result = (long) gpr[rs] + (long) gpr[rt];

            if (!addSubOverflow(result)) {
                gpr[rd] = (int) result;
            } else {
                // TODO set exception overflow and break !!! (rd cannot be modify)
            }
        }
    }

    @Override
    public void doADDU(int rd, int rs, int rt) {
        if (rd != 0) {
            gpr[rd] = gpr[rs] + gpr[rt];
        }
    }

    @Override
    public void doSUB(int rd, int rs, int rt) {
        if (rd != 0) {
            long result = (long) gpr[rs] - (long) gpr[rt];

            if (!addSubOverflow(result)) {
                gpr[rd] = (int) result;
            } else {
                // TODO set exception overflow and break !!! (rd cannot be modify)
            }
        }
    }

    @Override
    public void doSUBU(int rd, int rs, int rt) {
        if (rd != 0) {
            gpr[rd] = (int) ((((long) gpr[rs]) & 0xffffffff) - (((long) gpr[rt]) & 0xffffffff));
        }
    }

    @Override
    public void doAND(int rd, int rs, int rt) {
        if (rd != 0) {
            gpr[rd] = gpr[rs] & gpr[rt];
        }
    }

    @Override
    public void doOR(int rd, int rs, int rt) {
        if (rd != 0) {
            gpr[rd] = gpr[rs] | gpr[rt];
        }
    }

    @Override
    public void doXOR(int rd, int rs, int rt) {
        if (rd != 0) {
            gpr[rd] = gpr[rs] ^ gpr[rt];
        }
    }

    @Override
    public void doNOR(int rd, int rs, int rt) {
        if (rd != 0) {
            gpr[rd] = ~(gpr[rs] | gpr[rt]);
        }
    }

    @Override
    public void doSLT(int rd, int rs, int rt) {
        if (rd != 0) {
            gpr[rd] = signedCompare(gpr[rs], gpr[rt]);
        }
    }

    @Override
    public void doSLTU(int rd, int rs, int rt) {
        if (rd != 0) {
            gpr[rd] = unsignedCompare(gpr[rs], gpr[rt]);
        }
    }

    @Override
    public void doBLTZ(int rs, int simm16) {
        int previous_cycles = cycles;
        npc = (gpr[rs] < 0) ? branchTarget(pc, simm16) : (pc + 4);
        stepDelayslot();
        if (cycles - previous_cycles < 3) {
            cycles = previous_cycles + 3;
        }
    }

    @Override
    public void doBGEZ(int rs, int simm16) {
        int previous_cycles = cycles;
        npc = (gpr[rs] >= 0) ? branchTarget(pc, simm16) : (pc + 4);
        stepDelayslot();
        if (cycles - previous_cycles < 3) {
            cycles = previous_cycles + 3;
        }
    }

    @Override
    public void doBLTZL(int rs, int simm16) {
        int previous_cycles = cycles;
        if (gpr[rs] < 0) {
            npc = branchTarget(pc, simm16);
            stepDelayslot();
            if (cycles - previous_cycles < 3) {
                cycles = previous_cycles + 3;
            }
        } else {
            pc += 4;
            cycles += 3;
        }
    }

    @Override
    public void doBGEZL(int rs, int simm16) {
        int previous_cycles = cycles;
        if (gpr[rs] >= 0) {
            npc = branchTarget(pc, simm16);
            stepDelayslot();
            if (cycles - previous_cycles < 3) {
                cycles = previous_cycles + 3;
            }
        } else {
            pc += 4;
            cycles += 3;
        }
    }

    @Override
    public void doBLTZAL(int rs, int simm16) {
        int previous_cycles = cycles;
        int target = pc + 4;
        boolean t = (gpr[rs] < 0);
        gpr[31] = target;
        npc = t ? branchTarget(pc, simm16) : target;
        stepDelayslot();
        if (cycles - previous_cycles < 3) {
            cycles = previous_cycles + 3;
        }
    }

    @Override
    public void doBGEZAL(int rs, int simm16) {
        int previous_cycles = cycles;
        int target = pc + 4;
        boolean t = (gpr[rs] >= 0);
        gpr[31] = target;
        npc = t ? branchTarget(pc, simm16) : target;
        stepDelayslot();
        if (cycles - previous_cycles < 3) {
            cycles = previous_cycles + 3;
        }
    }

    @Override
    public void doBLTZALL(int rs, int simm16) {
        int previous_cycles = cycles;
        boolean t = (gpr[rs] < 0);
        gpr[31] = pc + 4;
        if (t) {
            npc = branchTarget(pc, simm16);
            stepDelayslot();
            if (cycles - previous_cycles < 3) {
                cycles = previous_cycles + 3;
            }
        } else {
            pc += 4;
            cycles += 3;
        }
    }

    @Override
    public void doBGEZALL(int rs, int simm16) {
        int previous_cycles = cycles;
        boolean t = (gpr[rs] >= 0);
        gpr[31] = pc + 4;
        if (t) {
            npc = branchTarget(pc, simm16);
            stepDelayslot();
            if (cycles - previous_cycles < 3) {
                cycles = previous_cycles + 3;
            }
        } else {
            pc += 4;
            cycles += 3;
        }
    }

    @Override
    public void doJ(int uimm26) {
        int previous_cycles = cycles;
        npc = jumpTarget(pc, uimm26);
        stepDelayslot();
        if (cycles - previous_cycles < 2) {
            cycles = previous_cycles + 2;
        }
    }

    @Override
    public void doJAL(int uimm26) {
        int previous_cycles = cycles;
        gpr[31] = pc + 4;
        npc = jumpTarget(pc, uimm26);
        stepDelayslot();
        if (cycles - previous_cycles < 2) {
            cycles = previous_cycles + 2;
        }
    }

    @Override
    public void doBEQ(int rs, int rt, int simm16) {
        int previous_cycles = cycles;
        npc = (gpr[rs] == gpr[rt]) ? branchTarget(pc, simm16) : (pc + 4);
        stepDelayslot();
        if (cycles - previous_cycles < 3) {
            cycles = previous_cycles + 3;
        }
    }

    @Override
    public void doBNE(int rs, int rt, int simm16) {
        int previous_cycles = cycles;
        npc = (gpr[rs] != gpr[rt]) ? branchTarget(pc, simm16) : (pc + 4);
        stepDelayslot();
        if (cycles - previous_cycles < 3) {
            cycles = previous_cycles + 3;
        }
    }

    @Override
    public void doBLEZ(int rs, int simm16) {
        int previous_cycles = cycles;
        npc = (gpr[rs] <= 0) ? branchTarget(pc, simm16) : (pc + 4);
        stepDelayslot();
        if (cycles - previous_cycles < 3) {
            cycles = previous_cycles + 3;
        }
    }

    @Override
    public void doBGTZ(int rs, int simm16) {
        int previous_cycles = cycles;
        npc = (gpr[rs] > 0) ? branchTarget(pc, simm16) : (pc + 4);
        stepDelayslot();
        if (cycles - previous_cycles < 3) {
            cycles = previous_cycles + 3;
        }
    }

    @Override
    public void doBEQL(int rs, int rt, int simm16) {
        int previous_cycles = cycles;
        if (gpr[rs] == gpr[rt]) {
            npc = branchTarget(pc, simm16);
            stepDelayslot();
            if (cycles - previous_cycles < 3) {
                cycles = previous_cycles + 3;
            }
        } else {
            pc += 4;
            cycles += 3;
        }
    }

    @Override
    public void doBNEL(int rs, int rt, int simm16) {
        int previous_cycles = cycles;
        if (gpr[rs] != gpr[rt]) {
            npc = branchTarget(pc, simm16);
            stepDelayslot();
            if (cycles - previous_cycles < 3) {
                cycles = previous_cycles + 3;
            }
        } else {
            pc += 4;
            cycles += 3;
        }
    }

    @Override
    public void doBLEZL(int rs, int simm16) {
        int previous_cycles = cycles;
        if (gpr[rs] <= 0) {
            npc = branchTarget(pc, simm16);
            stepDelayslot();
            if (cycles - previous_cycles < 3) {
                cycles = previous_cycles + 3;
            }
        } else {
            pc += 4;
            cycles += 3;
        }
    }

    @Override
    public void doBGTZL(int rs, int simm16) {
        int previous_cycles = cycles;
        if (gpr[rs] > 0) {
            npc = branchTarget(pc, simm16);
            stepDelayslot();
            if (cycles - previous_cycles < 3) {
                cycles = previous_cycles + 3;
            }
        } else {
            pc += 4;
            cycles += 3;
        }
    }

    @Override
    public void doADDI(int rt, int rs, int simm16) {
        if (rt != 0) {
            long result = (long) gpr[rs] + (long) simm16;

            if (!addSubOverflow(result)) {
                gpr[rt] = (int) result;
            } else {
                // TODO set exception overflow and break !!! (rd cannot be modify)
            }
        }
    }

    @Override
    public void doADDIU(int rt, int rs, int simm16) {
        if (rt != 0) {
            gpr[rt] = gpr[rs] + simm16;
        }
    }

    @Override
    public void doSLTI(int rt, int rs, int simm16) {
        if (rt != 0) {
            gpr[rt] = signedCompare(gpr[rs], simm16);
        }
    }

    @Override
    public void doSLTIU(int rt, int rs, int simm16) {
        if (rt != 0) {
            gpr[rt] = unsignedCompare(gpr[rs], simm16);
        }
    }

    @Override
    public void doANDI(int rt, int rs, int uimm16) {
        if (rt != 0) {
            gpr[rt] = gpr[rs] & uimm16;
        }
    }

    @Override
    public void doORI(int rt, int rs, int uimm16) {
        if (rt != 0) {
            gpr[rt] = gpr[rs] | uimm16;
        }
    }

    @Override
    public void doXORI(int rt, int rs, int uimm16) {
        if (rt != 0) {
            gpr[rt] = gpr[rs] ^ uimm16;
        }
    }

    @Override
    public void doLUI(int rt, int uimm16) {
        if (rt != 0) {
            gpr[rt] = uimm16 << 16;
        }
    }

    @Override
    public void doHALT() {
        // TODO
        System.out.println("Interpreter: Unsupported HALT instruction");
    }

    @Override
    public void doMFIC(int rt) {
        // TODO
        System.out.println("Interpreter: Unsupported mfic instruction");
    }

    @Override
    public void doMTIC(int rt) {
        // TODO
        System.out.println("Interpreter: Unsupported mtic instruction");
    }

    @Override
    public void doMFC0(int rt, int c0dr) {
        // TODO
        System.out.println("Interpreter: Unsupported mfc0 instruction");
    }

    @Override
    public void doCFC0(int rt, int c0cr) {
        // TODO
        System.out.println("Interpreter: Unsupported cfc0 instruction");
    }

    @Override
    public void doMTC0(int rt, int c0dr) {
        // TODO
        System.out.println("Interpreter: Unsupported mtc0 instruction");
    }

    @Override
    public void doCTC0(int rt, int c0cr) {
        // TODO
        System.out.println("Interpreter: Unsupported ctc0 instruction");
    }

    @Override
    public void doERET() {
        // TODO
        System.out.println("Interpreter: Unsupported eret instruction");
    }

    @Override
    public void doLB(int rt, int rs, int simm16) {
        int virtAddr = gpr[rs] + simm16;
        gpr[rt] = (Memory.get_instance().read8(virtAddr) << 24) >> 24;
    }

    @Override
    public void doLBU(int rt, int rs, int simm16) {
        int virtAddr = gpr[rs] + simm16;
        gpr[rt] = Memory.get_instance().read8(virtAddr) & 0xff;
    }

    @Override
    public void doLH(int rt, int rs, int simm16) {
        int virtAddr = gpr[rs] + simm16;
        gpr[rt] = (Memory.get_instance().read16(virtAddr) << 16) >> 16;
    }

    @Override
    public void doLHU(int rt, int rs, int simm16) {
        int virtAddr = gpr[rs] + simm16;
        gpr[rt] = Memory.get_instance().read16(virtAddr) & 0xffff;
    }

    @Override
    public void doLWL(int rt, int rs, int simm16) {
      int virtAddr = gpr[rs] + simm16;
      int offset = virtAddr & 0x3;
      int reg = gpr[rt];

      int word = Memory.get_instance().read32(virtAddr & 0xfffffffc);

      switch (offset)
      {
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
      
      gpr[rt] = word;
    }

    @Override
    public void doLW(int rt, int rs, int simm16) {
        int virtAddr = gpr[rs] + simm16;
        gpr[rt] = Memory.get_instance().read32(virtAddr);
    }

    @Override
    public void doLWR(int rt, int rs, int simm16) {
      int virtAddr = gpr[rs] + simm16;
      int offset = virtAddr & 0x3;
      int reg = gpr[rt];

      int word = Memory.get_instance().read32(virtAddr & 0xfffffffc);

      switch (offset)
      {
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
      
      gpr[rt] = word;
    }

    @Override
    public void doSB(int rt, int rs, int simm16) {
        int virtAddr = gpr[rs] + simm16;
        Memory.get_instance().write8(virtAddr, (byte) (gpr[rt] & 0xFF));
    }

    @Override
    public void doSH(int rt, int rs, int simm16) {
        int virtAddr = gpr[rs] + simm16;
        Memory.get_instance().write16(virtAddr, (short) (gpr[rt] & 0xFFFF));
    }

    @Override
    public void doSWL(int rt, int rs, int simm16) {
       int virtAddr = gpr[rs] + simm16;
      int offset = virtAddr & 0x3;
      int reg = gpr[rt];
      int data = Memory.get_instance().read32(virtAddr & 0xfffffffc);

      switch (offset)
        {
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

      Memory.get_instance().write32(virtAddr & 0xfffffffc, data);
    }

    @Override
    public void doSW(int rt, int rs, int simm16) {
        int virtAddr = gpr[rs] + simm16;
        Memory.get_instance().write32(virtAddr, gpr[rt]);
    }

    @Override
    public void doSWR(int rt, int rs, int simm16) {
      int virtAddr = gpr[rs] + simm16;
      int offset = virtAddr & 0x3;
      int reg = gpr[rt];
      int data = Memory.get_instance().read32(virtAddr & 0xfffffffc);

      switch (offset)
        {
        case 0:
          data = reg;
          break;

        case 1:
          data = ((reg << 8) & 0xffffff00) | (data & 0xff) ;
          break;

        case 2:
          data = ((reg << 16) & 0xffff0000) | (data & 0xffff);
          break;

        case 3:
          data = ((reg << 24) & 0xff000000) | (data & 0xffffff);
          break;
        }

      Memory.get_instance().write32(virtAddr & 0xfffffffc, data);
    }

    @Override
    public void doCACHE(int code, int rs, int simm16) {
        // TODO
        System.out.println("Interpreter: Unsupported cache instruction");
    }

    @Override
    public void doLL(int rt, int rs, int simm16) {
        // TODO
        System.out.println("Interpreter: Unsupported ll instruction");
    }

    @Override
    public void doSC(int rt, int rs, int simm16) {
        // TODO
        System.out.println("Interpreter: Unsupported sc instruction");
    }

    @Override
    public void doROTR(int rd, int rt, int sa) {
        if (rd != 0) {
            gpr[rd] = Integer.rotateRight(gpr[rt], sa);
        }
    }

    @Override
    public void doROTRV(int rd, int rt, int rs) {
        if (rd != 0) {
            // no need of "gpr[rs] & 31", rotateRight does it for us
            gpr[rd] = Integer.rotateRight(gpr[rt], gpr[rs]);
        }
    }

    @Override
    public void doMOVZ(int rd, int rs, int rt) {
        if ((rd != 0) && (gpr[rt] == 0)) {
            gpr[rd] = gpr[rs];
        }
    }

    @Override
    public void doMOVN(int rd, int rs, int rt) {
        if ((rd != 0) && (gpr[rt] != 0)) {
            gpr[rd] = gpr[rs];
        }
    }

    @Override
    public void doSYSCALL(int code) {
        // TODO
        // cop0.epc = pc - 4;
        // cop0.cause.exc |= SYSCALL_EXC;
        // npc = cop0.exception_handler;
        SyscallHandler.syscall(code);
    }

    @Override
    public void doBREAK(int code) {
        // TODO
        // cop0.debug_epc = pc - 4;
        // cop0.cause.exc |= BREAK_EXC;
        // npc = cop0.exception_handler;
        System.out.println("Interpreter: Unsupported break instruction");
    }

    @Override
    public void doSYNC() {
        cycles += 6;
    }

    @Override
    public void doCLZ(int rd, int rs) {
        if (rd != 0) {
            gpr[rd] = Integer.numberOfLeadingZeros(gpr[rs]);
        }
    }

    @Override
    public void doCLO(int rd, int rs) {
        if (rd != 0) {
            gpr[rd] = Integer.numberOfLeadingZeros(~gpr[rs]);
        }
    }

    @Override
    public void doMADD(int rs, int rt) {
        hilo += ((long) gpr[rs]) * ((long) gpr[rs]);
        cycles += 4;
    }

    @Override
    public void doMADDU(int rs, int rt) {
        hilo += (((long) gpr[rs]) & 0xffffffff) * (((long) gpr[rs]) & 0xffffffff);
        cycles += 4;
    }

    @Override
    public void doMAX(int rd, int rs, int rt) {
        if (rd != 0) {
            int x = gpr[rs];
            int y = gpr[rt];
            gpr[rd] = (x > y) ? x : y;
        }
    }

    @Override
    public void doMIN(int rd, int rs, int rt) {
        if (rd != 0) {
            int x = gpr[rs];
            int y = gpr[rt];
            gpr[rd] = (x < y) ? x : y;
        }
    }

    @Override
    public void doMSUB(int rs, int rt) {
        hilo -= ((long) gpr[rs]) * ((long) gpr[rs]);
        cycles += 4;
    }

    @Override
    public void doMSUBU(int rs, int rt) {
        hilo -= (((long) gpr[rs]) & 0xffffffff) * (((long) gpr[rs]) & 0xffffffff);
        cycles += 4;
    }

    @Override
    public void doEXT(int rt, int rs, int rd, int sa) {
        int mask = ~(~1 << rd);
        gpr[rt] = (gpr[rs] >> sa) & mask;
    }

    @Override
    public void doINS(int rt, int rs, int rd, int sa) {
        int mask1 = ~(~0 << sa);
        int mask2 = (~0 << rd);
        int mask3 = mask1 | mask2;
        gpr[rt] = (gpr[rt] & mask3) | ((gpr[rs] >> sa) & mask2);
    }

    @Override
    public void doWSBH(int rd, int rt) {
        if (rd != 0) {
            gpr[rd] = Integer.rotateRight(Integer.reverseBytes(gpr[rt]), 16);
        }
    }

    @Override
    public void doWSBW(int rd, int rt) {
        if (rd != 0) {
            gpr[rd] = Integer.reverseBytes(gpr[rt]);
        }
    }

    @Override
    public void doSEB(int rd, int rt) {
        if (rd != 0) {
            gpr[rd] = (gpr[rt] << 24) >> 24;
        }
    }

    @Override
    public void doBITREV(int rd, int rt) {
        if (rd != 0) {
            gpr[rd] = Integer.reverse(gpr[rt]);
        }
    }

    @Override
    public void doSEH(int rd, int rt) {
        gpr[rd] = (gpr[rt] << 16) >> 16;
    }
    @Override
    public void doMFC1(int rt, int c1dr){System.out.println("Interpreter: Unsupported mfc1 instruction");}
    @Override
    public void doCFC1(int rt, int c1cr){System.out.println("Interpreter: Unsupported cfc1 instruction");}
    @Override
    public void doMTC1(int rt, int c1dr){System.out.println("Interpreter: Unsupported mtc1 instruction");}  
    @Override
    public void doCTC1(int rt, int c1cr){System.out.println("Interpreter: Unsupported ctc1 instruction");}
    @Override
    public void doBC1F(int simm16){System.out.println("Interpreter: Unsupported bc1f instruction");}
    @Override
    public void doBC1T (int simm16){System.out.println("Interpreter: Unsupported bc1t instruction");}
    @Override
    public void doBC1FL(int simm16){System.out.println("Interpreter: Unsupported bc1fl instruction");}
    @Override
    public void doBC1TL(int simm16){System.out.println("Interpreter: Unsupported bc1tl instruction");}
    @Override
    public void doADDS(int fd , int fs ,int ft){System.out.println("Interpreter: Unsupported add.s instruction");}
    @Override
    public void doSUBS(int fd , int fs ,int ft){System.out.println("Interpreter: Unsupported sub.s instruction");} 
    @Override
    public void doMULS(int fd , int fs ,int ft){System.out.println("Interpreter: Unsupported mul.s instruction");} 
    @Override
    public void doDIVS(int fd , int fs ,int ft){System.out.println("Interpreter: Unsupported div.s instruction");} 
    @Override
    public void doSQRTS(int fd,int fs){System.out.println("Interpreter: Unsupported sqrt.s instruction");}
    @Override
    public void doABSS(int fd,int fs){System.out.println("Interpreter: Unsupported abs.s instruction");}
    @Override
    public void doMOVS(int fd,int fs){System.out.println("Interpreter: Unsupported mov.s instruction");}
    @Override
    public void doNEGS(int fd,int fs){System.out.println("Interpreter: Unsupported neg.s instruction");}
    @Override
    public void doROUNDWS(int fd,int fs){System.out.println("Interpreter: Unsupported round.w.s instruction");}
    @Override
    public void doTRUNCWS(int fd,int fs){System.out.println("Interpreter: Unsupported trunc.w.s instruction");}
    @Override
    public void doCEILWS(int fd,int fs){System.out.println("Interpreter: Unsupported ceil.w.s instruction");}
    @Override
    public void doFLOORWS(int fd,int fs){System.out.println("Interpreter: Unsupported floor.w.s instruction");}
    @Override
    public void doCVTSW(int fd,int fs){System.out.println("Interpreter: Unsupported cvt.s.w instruction");}
    @Override
    public void doCVTWS(int fd,int fs){System.out.println("Interpreter: Unsupported cvt.w.s instruction");}
    @Override
    public void doCF(int fs,int ft){System.out.println("Interpreter: Unsupported c.f instruction");}
    @Override
    public void doCUN(int fs,int ft){System.out.println("Interpreter: Unsupported c.un instruction");}
    @Override
    public void doCEQ(int fs,int ft){System.out.println("Interpreter: Unsupported c.eq instruction");}
    @Override
    public void doCUEQ(int fs,int ft){System.out.println("Interpreter: Unsupported c.ueq instruction");}
    @Override
    public void doCOLT(int fs,int ft){System.out.println("Interpreter: Unsupported c.olt instruction");}
    @Override
    public void doCULT(int fs,int ft){System.out.println("Interpreter: Unsupported c.ult instruction");}
    @Override
    public void doCOLE(int fs,int ft){System.out.println("Interpreter: Unsupported c.ole instruction");}
    @Override
    public void doCULE(int fs,int ft){System.out.println("Interpreter: Unsupported c.ule instruction");}
    @Override
    public void doCSF(int fs,int ft){System.out.println("Interpreter: Unsupported c.sf instruction");}
    @Override
    public void doCNGLE(int fs,int ft){System.out.println("Interpreter: Unsupported c.ngle instruction");}
    @Override
    public void doCSEQ(int fs,int ft){System.out.println("Interpreter: Unsupported c.seq instruction");}
    @Override
    public void doCNGL(int fs,int ft){System.out.println("Interpreter: Unsupported c.ngl instruction");}
    @Override
    public void doCLT(int fs,int ft){System.out.println("Interpreter: Unsupported c.lt instruction");}
    @Override
    public void doCNGE(int fs,int ft){System.out.println("Interpreter: Unsupported c.nge instruction");}
    @Override
    public void doCLE(int fs,int ft){System.out.println("Interpreter: Unsupported c.le instruction");}
    @Override
    public void doCNGT(int fs,int ft){System.out.println("Interpreter: Unsupported c.ngt instruction");}
}