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
package jpcsp.Allegrex.compiler;

import java.util.Stack;

import org.apache.log4j.Logger;

import jpcsp.Memory;
import jpcsp.Allegrex.Common;
import jpcsp.Allegrex.Decoder;
import jpcsp.Allegrex.Common.Instruction;
import jpcsp.util.DurationStatistics;

/*
 * TODO to cleanup the code:
 * - add flags to Common.Instruction:
 *     - isBranching (see branchBlockInstructions list below) [DONE]
 *     - is end of CodeBlock (see endBlockInstructions list below) [DONE]
 *     - is starting a new CodeBlock (see newBlockInstructions list below) [DONE]
 *     - isBranching unconditional [DONE]
 *     - isBranching with 16 bits target [DONE]
 *     - isBranching with 26 bits target (see jumpBlockInstructions list below) [DONE]
 *     - is jump or call [DONE]
 *     - is compiled or interpreted [DONE]
 *     - is referencing $pc register
 *     - can switch thread context
 *     - is loading $sp address to another register
 *     - is reading Rs
 *     - is reading Rt
 *     - is writing Rt
 *     - is writing Rd
 *
 * TODO Ideas to further enhance performance:
 * - store stack variables "nn(sp)" into local variables
 *   if CodeBlock is following standard MIPS call conventions and
 *   if sp is not loaded into another register
 * - store registers in local variables and flush registers back to gpr[] when calling
 *   subroutine (if CodeBlock is following standard MIPS call conventions).
 *     subroutine parameters: $a0-$a3
 *     callee-saved registers: $fp, $s0-$s7
 *     caller-saved registers: $ra, $t0-$t9, $a0-$a3
 *     Register description:
 *       $zr (0): 0
 *       $at (1): reserved for assembler
 *       $v0-$v1 (2-3): expression evaluation & function results
 *       $a0-$a3 (4-7): arguments, not preserved across subroutine calls
 *       $t0-$t7 (8-15): temporary: caller saves, not preserved across subroutine calls
 *       $s0-$s7 (16-23): callee saves, must be preserved across subroutine calls
 *       $t8-$t9 (24-25): temporary (cont'd)
 *       $k0-$k1 (26-27): reserved for OS kernel
 *       $gp (28): Pointer to global area (Global Pointer)
 *       $sp (29): Stack Pointer
 *       $fp (30): Frame Pointer, must be preserved across subroutine calls
 *       $ra (31): Return Address
 *  - provide bytecode compilation for mostly used instructions
 *    e.g.: typical program is using the following instructions based on frequency usage:
 *            ADDIU 8979678 (14,1%)
 *             ADDU 6340735 ( 9,9%)
 *               LW 5263292 ( 8,3%)
 *               SW 5223074 ( 8,2%)
 *              LUI 3930978 ( 6,2%)
 *             ANDI 2833815 ( 4,4%)
 *               SB 2812802 ( 4,4%)
 *              BNE 2176061 ( 3,4%)
 *              SRL 1919480 ( 3,0%)
 *               OR 1888281 ( 3,0%)
 *              AND 1411970 ( 2,2%)
 *              SLL 1406954 ( 2,2%)
 *              JAL 1297959 ( 2,0%)
 *              NOP 1272929 ( 2,0%)
 *             SRAV 1132740
 *               JR 1124095
 *              LBU 1068533
 *              BEQ 1032768
 *              LHU 1011791
 *            BGTZL 943264
 *             MFLO 680659
 *             MULT 679357
 *              ORI 503067
 *                J 453007
 *             SLTI 432961
 *              SRA 293213
 *             BEQL 228478
 *          SYSCALL 215713
 *             BGEZ 168494
 *             BNEL 159020
 *             SLTU 124580
 *  - implement subroutine arguments as Java arguments instead of $a0-$a3.
 *    Automatically detect how many arguments are expected by a subroutine.
 *    Generate different Java classes if number of expected arguments is refined
 *    over time (include number in Class name).
 */
/**
 * @author gid15
 *
 */
public class Compiler implements ICompiler {
    public static Logger log = Logger.getLogger("compiler");
	private static Compiler instance;
	private static int resetCount = 0;
	private Memory mem;
	private CompilerClassLoader classLoader;
	private DurationStatistics compileDuration = new DurationStatistics("Compilation Time");

	public static Compiler getInstance() {
		if (instance == null) {
			instance = new Compiler();
		}

		return instance;
	}

	private Compiler() {
		Initialise();
	}

	public static void exit() {
	    if (instance != null) {
	        log.info(instance.compileDuration.toString());
	    }
	}

	public void reset() {
		resetCount++;
		classLoader = new CompilerClassLoader(this);
		compileDuration.reset();
	}

	private void Initialise() {
		mem = Memory.getInstance();

		reset();
	}

	private int jumpTarget(int pc, int opcode) {
		return (pc & 0xF0000000) | ((opcode & 0x03FFFFFF) << 2);
	}

	private int branchTarget(int pc, int opcode) {
		return pc + (((int)(short) (opcode & 0x0000FFFF)) << 2);
	}

	private IExecutable interpret(CompilerContext context, int startAddress) {
        if (log.isDebugEnabled()) {
            log.debug("Compiler.interpret Block 0x" + Integer.toHexString(startAddress));
        }
        startAddress = startAddress & Memory.addressMask;
        CodeBlock codeBlock = new CodeBlock(startAddress);

        IExecutable executable = codeBlock.getInterpretedExecutable(context);
        if (log.isDebugEnabled()) {
            log.debug("Executable: " + executable);
        }

        return executable;
	}

	private IExecutable analyse(CompilerContext context, int startAddress, boolean recursive) {
        if (log.isDebugEnabled()) {
            log.debug("Compiler.analyse Block 0x" + Integer.toHexString(startAddress));
        }
        startAddress = startAddress & Memory.addressMask;
        CodeBlock codeBlock = new CodeBlock(startAddress);
        Stack<Integer> pendingBlockAddresses = new Stack<Integer>();
        pendingBlockAddresses.clear();
        pendingBlockAddresses.push(startAddress);
        while (!pendingBlockAddresses.isEmpty()) {
            int pc = pendingBlockAddresses.pop();
            boolean isBranchTarget = true;
            int endPc = Integer.MAX_VALUE;
            if (context.analysedAddresses.contains(pc) && isBranchTarget) {
                codeBlock.setIsBranchTarget(pc);
            } else {
                while (!context.analysedAddresses.contains(pc) && pc <= endPc) {
                    int opcode = mem.read32(pc);

                    Common.Instruction insn = Decoder.instruction(opcode);

                    context.analysedAddresses.add(pc);
                    int npc = pc + 4;

                    int branchingTo = 0;
                    boolean isBranching = false;
                    if (insn.hasFlags(Instruction.FLAG_IS_BRANCHING)) {
                        branchingTo = branchTarget(npc, opcode);
                        isBranching = true;
                    } else if (insn.hasFlags(Instruction.FLAG_IS_JUMPING)) {
                        branchingTo = jumpTarget(npc, opcode);
                        isBranching = true;
                    }
                    if (insn.hasFlags(Instruction.FLAG_ENDS_BLOCK)) {
                        endPc = npc;
                    }
                    if (insn.hasFlags(Instruction.FLAG_STARTS_NEW_BLOCK)) {
                        if (recursive) {
                            context.blocksToBeAnalysed.push(branchingTo);
                        }
                    } else if (isBranching) {
                        if (branchingTo != 0) {  // Ignore "J 0x00000000" instruction
                            pendingBlockAddresses.push(branchingTo);
                        }
                    }

                    codeBlock.addInstruction(pc, opcode, insn, isBranchTarget, isBranching, branchingTo);
                    pc = npc;

                    isBranchTarget = false;
                }
            }
        }

        IExecutable executable = codeBlock.getExecutable(context);
        if (log.isDebugEnabled()) {
            log.debug("Executable: " + executable);
        }

        return executable;
	}

	public void analyseRecursive(int startAddress) {
	    if (RuntimeContext.hasCodeBlock(startAddress)) {
	        if (log.isDebugEnabled()) {
	            log.debug("Compiler.analyse 0x" + Integer.toHexString(startAddress).toUpperCase() + " - already analysed");
	        }
	        return;
	    }

        if (log.isDebugEnabled()) {
            log.debug("Compiler.analyse 0x" + Integer.toHexString(startAddress).toUpperCase());
        }

        CompilerContext context = new CompilerContext(classLoader);
		context.blocksToBeAnalysed.push(startAddress);
		while (!context.blocksToBeAnalysed.isEmpty()) {
			int blockStartAddress = context.blocksToBeAnalysed.pop();
			analyse(context, blockStartAddress, true);
		}
	}

    @Override
    public IExecutable compile(String name) {
        return compile(CompilerContext.getClassAddress(name));
    }

    @Override
    public IExecutable compile(int address) {
        compileDuration.start();
        CompilerContext context = new CompilerContext(classLoader);
        IExecutable executable = null;
        ClassFormatError error = null;
        for (int retries = 2; retries > 0; retries--) {
            try {
                executable = analyse(context, address, false);
                break;
            } catch (ClassFormatError e) {
                // Catch exception
                //     java.lang.ClassFormatError: Invalid method Code length nnnnnn in class file XXXX
                //
                error = e;

                // Try again with stricter methodMaxInstructions (75% of current value)
                int methodMaxInstructions = context.getMethodMaxInstructions() * 3 / 4;
                Compiler.log.debug("Catched exception '" + e.toString() + "' (can be ignored)");
                Compiler.log.debug("Retrying compilation again with maxInstruction=" + methodMaxInstructions + ", retries left=" + (retries - 1) + "...");
                context = new CompilerContext(classLoader);
                context.setMethodMaxInstructions(methodMaxInstructions);
            }
        }
        compileDuration.end();

        if (error != null) {
            if (executable == null) {
                Compiler.log.debug("Compilation failed with maxInstruction=" + context.getMethodMaxInstructions());
                context = new CompilerContext(classLoader);
                executable = interpret(context, address);
                if (executable == null) {
                	throw error;
                }
            } else {
                Compiler.log.debug("Compilation was now correct with maxInstruction=" + context.getMethodMaxInstructions());
            }
        }

        return executable;
    }

    public CompilerClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(CompilerClassLoader classLoader) {
        this.classLoader = classLoader;
    }

	public static int getResetCount() {
		return resetCount;
	}

	public static void setResetCount(int resetCount) {
		Compiler.resetCount = resetCount;
	}
}
