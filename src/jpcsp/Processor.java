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

import static jpcsp.R4000OpCodes.*;

public class Processor {

    public int pc;
    public int hi,  lo;
    public int cpuregisters[] = new int[32];//32 base registers
    private byte[] cyclesPer;

    Processor() {
        Memory.get_instance(); //intialaze memory

        initCycleCost();
        reset();

    }

    private void initCycleCost() {
        cyclesPer = new byte[0xff];
        cyclesPer[MULT] = 12;
        cyclesPer[MULTU] = 12;
        cyclesPer[DIV] = 75;
        cyclesPer[DIVU] = 75;
    //cyclesPer[DMULT] = 20;
    //cyclesPer[DMULTU] = 20;
    //cyclesPer[DDIV] = 139;
    //cyclesPer[DDIVU] = 139;
    }

    public void reset() {
        //intialaze psp
        pc = 0x00000000;
        hi = lo = 0;
        for (int i = 0; i < 32; i++) {
            cpuregisters[i] = 0;//reset registers

        }

    }

    public int signExtend(int value) {
        /* Moves the sign bit
         * making a 16bit value a valid 32bit value */
        if ((value & 0x8000) != 0) {
            value |= 0xFFFF0000;
        }
        return value;
    }

    public long numberCyclesDelay() {
        return 0l;
    }

    private boolean couldRaiseOverflowOnAdd(int value0, int value1) {
        boolean v1 = (((long) value0 + (long) value1) > Integer.MAX_VALUE);
        boolean v2 = (((long) value0 + (long) value1) < Integer.MIN_VALUE); //or underflow????

        return v1 | v2;
    }

    private boolean couldRaiseOverflowOnSub(int value0, int value1) {
        boolean v1 = (((long) value0 - (long) value1) > Integer.MAX_VALUE);
        boolean v2 = (((long) value0 - (long) value1) < Integer.MIN_VALUE); //or underflow????

        return v1 | v2;
    }

    public void stepcpu() {
        long temp;
        long longA, longB;
        int value = Memory.get_instance().read32(pc);
        longA = longB = 0;
        int rs = (value >> 21) & 0x1f;
        int rt = (value >> 16) & 0x1f;
        int rd = (value >> 11) & 0x1f;
        int sa = (value >> 6) & 0x1f;
        int imm = value & 0xffff;
        byte opcode = (byte) ((value >> 26) & 0x3f);
        switch (opcode) {
            case SPECIAL:
                byte special = (byte) (value & 0x3f);
                switch (special) {
                    case SLL: //last update 31/07/2008 - should be okay (shadow)

                        cpuregisters[rd] = cpuregisters[rt] << sa;
                        break;
                    case SRL:
                        //last update 31/07/2008 - should be okay (shadow)
                        //last update 31/07/2008 - >>> does not sign extend (fiveofhearts)
                        cpuregisters[rd] = cpuregisters[rt] >>> sa;
                        break;
                    case SRA:
                        //last update 31/07/2008 - >> sign extension is automatic (fiveofhearts)
                        cpuregisters[rd] = cpuregisters[rt] >> sa;
                        break;
                    case SLLV:
                        cpuregisters[rd] = cpuregisters[rt] << (cpuregisters[rs] & 0x3F);
                        break;
                    case SRLV:
                        //last update 31/07/2008 - >>> does not sign extend (fiveofhearts)
                        cpuregisters[rd] = cpuregisters[rt] >>> (cpuregisters[rs] & 0x3F);
                        break;
                    case SRAV:
                        //last update 31/07/2008 - >> sign extension is automatic (fiveofhearts)
                        cpuregisters[rd] = cpuregisters[rt] >> (cpuregisters[rs] & 0x3F);
                        break;
                    case JR:
                        pc = cpuregisters[rs] - 4;
                        /* TODO: delay one cycle */
                        break;
                    case JALR:
                        cpuregisters[rd] = pc + 8; // second instruction after

                        pc = cpuregisters[rs] - 4;
                        /* TODO: delay one cycle */
                        break;
                    /*case 11://movn
                    break;
                    case 12://syscall
                    break;
                    case 13://break;
                    break;*/

                    case MFHI: //mfhi

                        cpuregisters[rd] = hi;
                        break;
                    case MTHI: //mthi

                        hi = cpuregisters[rs];
                        break;
                    case MFLO: //mflo

                        cpuregisters[rd] = lo;
                        break;
                    case MTLO://mtlo

                        lo = cpuregisters[rs];
                        break;
                    case MULT://mult

                        temp = (long) cpuregisters[rs] * (long) cpuregisters[rt];
                        hi = (int) ((temp >> 32) & 0xFFFFFFFF);
                        lo = (int) (temp & 0xFFFFFFFF);
                        break;
                    case MULTU://multu
                    /* We OR the bit so we are sure the sign bit isnt set */
                        longA |= cpuregisters[rs];
                        longB |= cpuregisters[rt];
                        temp = longA * longB;
                        hi = (int) ((temp >> 32) & 0xFFFFFFFF);
                        lo = (int) (temp & 0xFFFFFFFF);
                        break;
                    case DIV://div

                        lo = cpuregisters[rs] / cpuregisters[rt];
                        hi = cpuregisters[rs] % cpuregisters[rt];
                        break;
                    case DIVU://divu

                        longA |= cpuregisters[rs];
                        longB |= cpuregisters[rt];
                        lo = (int) (longA / longB);
                        hi = (int) (longA % longB);
                        break;
                    case ADD://add

                        if (couldRaiseOverflowOnAdd(cpuregisters[rs], cpuregisters[rt])) {
                            // TODO set exception overflow and break !!! (rd cannot be modify)
                        }
                        cpuregisters[rd] = cpuregisters[rs] + cpuregisters[rt];
                        /*TODO: integer overflow exception */
                        break;
                    case ADDU://addu

                        longA |= cpuregisters[rs];
                        longB |= cpuregisters[rt];
                        cpuregisters[rd] = (int) (longA + longB);
                        break;
                    case SUB://sub

                        if (couldRaiseOverflowOnSub(cpuregisters[rs], cpuregisters[rt])) {
                            // TODO set exception overflow and break !!! (rd cannot be modify)
                        }
                        cpuregisters[rd] = cpuregisters[rs] - cpuregisters[rt];
                        /* TODO: add integer overflow exception */
                        break;
                    case SUBU://subu

                        cpuregisters[rd] = cpuregisters[rs] - cpuregisters[rt];
                        break;
                    case AND://and

                        cpuregisters[rd] = cpuregisters[rs] & cpuregisters[rt];
                        break;
                    case OR://or

                        cpuregisters[rd] = cpuregisters[rs] | cpuregisters[rt];
                        break;
                    case XOR://xor

                        cpuregisters[rd] = cpuregisters[rs] ^ cpuregisters[rt];
                        break;
                    case NOR://nor

                        cpuregisters[rd] = ~cpuregisters[rs] | cpuregisters[rt];
                        break;
                    case SLT://slt

                        if (cpuregisters[rs] < cpuregisters[rt]) {
                            cpuregisters[rd] = 1;
                        } else {
                            cpuregisters[rd] = 0;
                        }
                        break;
                    case SLTU://sltu

                        longA |= cpuregisters[rs];
                        longB |= cpuregisters[rt];
                        if (longA < longB) {
                            cpuregisters[rd] = 1;
                        } else {
                            cpuregisters[rd] = 0;
                        }
                        break;
                    default:
                        System.out.println("Unsupported special instruction " + Integer.toHexString(special));
                        break;
                }
                break;
            case J:
                pc = ((pc & 0xF0000000) | ((value & 0x3FFFFFF) << 2)) - 4;
                /*TODO: delay one cycle */
                break;
            case JAL:
                cpuregisters[31] = pc + 8; // second instruction after

                pc = ((pc & 0xF0000000) | ((value & 0x3FFFFFF) << 2)) - 4;
                break;
            case BEQ:
                if (cpuregisters[rs] == cpuregisters[rt]) {
                    pc += (signExtend(imm) << 2) + 4 - 4; // relative to address of first instruction after

                }
                break;
            case BNE:
                if (cpuregisters[rs] != cpuregisters[rt]) {
                    pc += (signExtend(imm) << 2) + 4 - 4;
                }
                break;
            case BLEZ:
                if (cpuregisters[rs] <= 0) {
                    pc += (signExtend(imm) << 2) + 4 - 4;
                }
                break;
            case BGTZ:
                if (cpuregisters[rs] > 0) {
                    pc += (signExtend(imm) << 2) + 4 - 4;
                }
                break;

            case ADDI: //addi

                if (couldRaiseOverflowOnAdd(cpuregisters[rs], signExtend(imm))) {
                    // TODO set exception overflow and break !!! (rt cannot be modify)
                }
                //cpuregisters[rs] = cpuregisters[rs] + signExtend(imm); 
                cpuregisters[rt] = cpuregisters[rs] + signExtend(imm);
                /*TODO: integer overflow exception */
                break;
            case ADDIU: //addiu

                longA |= cpuregisters[rs];
                longB |= signExtend(imm);
                cpuregisters[rt] = (int) (longA + longB);
                break;
            case SLTI://slti

                if (cpuregisters[rs] < signExtend(imm)) {
                    cpuregisters[rd] = 1;
                } else {
                    cpuregisters[rd] = 0;
                }
                break;
            case SLTIU://sltiu

                longA |= cpuregisters[rs];
                longB |= signExtend(imm);
                if (longA < longB) {
                    cpuregisters[rd] = 1;
                } else {
                    cpuregisters[rd] = 0;
                }
                break;
            case ANDI://ANDI

                cpuregisters[rt] = cpuregisters[rs] & imm;
                break;
            case ORI: //ori

                cpuregisters[rt] = cpuregisters[rs] | imm;
                break;
            case XORI: //xori

                cpuregisters[rt] = cpuregisters[rs] ^ imm;
                break;
            case LUI://lui

                cpuregisters[rt] = imm << 16;
                break;
            case LH:
                int virtAddr;
                virtAddr = cpuregisters[rs] + signExtend(imm);
                cpuregisters[rt] = signExtend(Memory.get_instance().read16(virtAddr));
                break;
            case LHU:
                virtAddr = cpuregisters[rs] + signExtend(imm);
                cpuregisters[rt] = Memory.get_instance().read16(virtAddr);
                break;
            case LW:
                virtAddr = cpuregisters[rs] + signExtend(imm);
                cpuregisters[rt] = Memory.get_instance().read32(virtAddr);
                break;
            case SH:
                virtAddr = cpuregisters[rs] + signExtend(imm);
                Memory.get_instance().write16(virtAddr, (short) (cpuregisters[rt] & 0xFFFF));
                break;
            case SW:
                virtAddr = cpuregisters[rs] + signExtend(imm);
                Memory.get_instance().write32(virtAddr, cpuregisters[rt]);
                break;

            default:
                System.out.println("Unsupported instruction " + Integer.toHexString(opcode));
                break;
        }
        pc += 4;
    }
}
