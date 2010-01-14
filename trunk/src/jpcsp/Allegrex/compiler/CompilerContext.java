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

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import jpcsp.Memory;
import jpcsp.Processor;
import jpcsp.Allegrex.Common;
import jpcsp.Allegrex.CpuState;
import jpcsp.Allegrex.Instructions;
import jpcsp.Allegrex.Common.Instruction;
import jpcsp.Allegrex.FpuState.Fcr31;
import jpcsp.Allegrex.VfpuState.Vcr;
import jpcsp.Allegrex.compiler.nativeCode.NativeCodeManager;
import jpcsp.Allegrex.compiler.nativeCode.NativeCodeSequence;
import jpcsp.Allegrex.compiler.nativeCode.Nop;
import jpcsp.HLE.kernel.types.SceKernelThreadInfo;
import jpcsp.memory.SafeFastMemory;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author gid15
 *
 */
public class CompilerContext implements ICompilerContext {
	private CompilerClassLoader classLoader;
	private CodeBlock codeBlock;
	private int numberInstructionsToBeSkipped;
	private boolean skipDelaySlot;
	private MethodVisitor mv;
	private CodeInstruction codeInstruction;
	private static final boolean storeGprLocal = true;
	private static final boolean storeProcessorLocal = true;
	private static final boolean storeCpuLocal = false;
	private static final int LOCAL_RETURN_ADDRESS = 0;
	private static final int LOCAL_ALTERVATIVE_RETURN_ADDRESS = 1;
    private static final int LOCAL_IS_JUMP = 2;
    private static final int LOCAL_PROCESSOR = 3;
    private static final int LOCAL_GPR = 4;
    private static final int LOCAL_INSTRUCTION_COUNT = 5;
    private static final int LOCAL_CPU = 6;
    private static final int LOCAL_TMP = 7;
    private static final int LOCAL_MAX = 8;
    private static final int STACK_MAX = 10;
    private static final int spRegisterIndex = 29;
    public Set<Integer> analysedAddresses = new HashSet<Integer>();
    public Stack<Integer> blocksToBeAnalysed = new Stack<Integer>();
    private int currentInstructionCount;
    private int preparedRegisterForStore = -1;
    private boolean memWrite32prepared = false;
    private boolean hiloPrepared = false;
    private int methodMaxInstructions = 3000;
    private NativeCodeManager nativeCodeManager;
    private NativeCodeSequence nativeCodeSequence = null;
    private static final String runtimeContextInternalName = Type.getInternalName(RuntimeContext.class);
    private static final String processorDescriptor = Type.getDescriptor(Processor.class);
    private static final String cpuDescriptor = Type.getDescriptor(CpuState.class);
    private static final String cpuInternalName = Type.getInternalName(CpuState.class);
    private static final String instructionsInternalName = Type.getInternalName(Instructions.class);
    private static final String instructionInternalName = Type.getInternalName(Instruction.class);
    private static final String instructionDescriptor = Type.getDescriptor(Instruction.class);
    private static final String sceKernalThreadInfoInternalName = Type.getInternalName(SceKernelThreadInfo.class);
    private static final String sceKernalThreadInfoDescriptor = Type.getDescriptor(SceKernelThreadInfo.class);
    private static final String stringDescriptor = Type.getDescriptor(String.class);
    private static final String memoryDescriptor = Type.getDescriptor(Memory.class);
    private static final String memoryInternalName = Type.getInternalName(Memory.class);
    private static final String profilerInternalName = Type.getInternalName(Profiler.class);

    public CompilerContext(CompilerClassLoader classLoader) {
        this.classLoader = classLoader;
        nativeCodeManager = Compiler.getInstance().getNativeCodeManager();
    }

    public CompilerClassLoader getClassLoader() {
		return classLoader;
	}

	public void setClassLoader(CompilerClassLoader classLoader) {
		this.classLoader = classLoader;
	}

    public CodeBlock getCodeBlock() {
        return codeBlock;
    }

    public void setCodeBlock(CodeBlock codeBlock) {
        this.codeBlock = codeBlock;
    }

    private void loadGpr() {
    	if (storeGprLocal) {
    		mv.visitVarInsn(Opcodes.ALOAD, LOCAL_GPR);
    	} else {
    		mv.visitFieldInsn(Opcodes.GETSTATIC, runtimeContextInternalName, "gpr", "[I");
    	}
    }

    private void loadCpu() {
    	if (storeCpuLocal) {
    		mv.visitVarInsn(Opcodes.ALOAD, LOCAL_CPU);
    	} else {
    		mv.visitFieldInsn(Opcodes.GETSTATIC, runtimeContextInternalName, "cpu", cpuDescriptor);
    	}
	}

    private void loadProcessor() {
    	if (storeProcessorLocal) {
    		mv.visitVarInsn(Opcodes.ALOAD, LOCAL_PROCESSOR);
    	} else {
            mv.visitFieldInsn(Opcodes.GETSTATIC, runtimeContextInternalName, "processor", processorDescriptor);
    	}
    }

    public void loadRegister(int reg) {
    	loadGpr();
        mv.visitLdcInsn(reg);
        mv.visitInsn(Opcodes.IALOAD);
    }

    public void prepareRegisterForStore(int reg) {
    	if (preparedRegisterForStore < 0) {
        	loadGpr();
            mv.visitLdcInsn(reg);
    		preparedRegisterForStore = reg;
    	}
    }

    public void storeRegister(int reg) {
    	if (preparedRegisterForStore == reg) {
	        mv.visitInsn(Opcodes.IASTORE);
	        preparedRegisterForStore = -1;
    	} else {
	    	loadGpr();
	        mv.visitInsn(Opcodes.SWAP);
	        mv.visitLdcInsn(reg);
	        mv.visitInsn(Opcodes.SWAP);
	        mv.visitInsn(Opcodes.IASTORE);
    	}
    }

    public void storeRegister(int reg, int constantValue) {
    	loadGpr();
        mv.visitLdcInsn(reg);
        mv.visitLdcInsn(constantValue);
        mv.visitInsn(Opcodes.IASTORE);
    }

    public void loadFcr31() {
    	loadCpu();
        mv.visitFieldInsn(Opcodes.GETFIELD, Type.getInternalName(CpuState.class), "fcr31", Type.getDescriptor(Fcr31.class));
    }

    public void loadVcr() {
    	loadCpu();
        mv.visitFieldInsn(Opcodes.GETFIELD, Type.getInternalName(CpuState.class), "vcr", Type.getDescriptor(Vcr.class));
    }

	@Override
	public void loadHilo() {
		loadCpu();
        mv.visitFieldInsn(Opcodes.GETFIELD, Type.getInternalName(CpuState.class), "hilo", Type.getDescriptor(long.class));
	}

	@Override
	public void prepareHiloForStore() {
		loadCpu();
		hiloPrepared = true;
	}

	@Override
	public void storeHilo() {
		if (!hiloPrepared) {
			loadCpu();
			mv.visitInsn(Opcodes.DUP_X2);
        	mv.visitInsn(Opcodes.POP);
		}
        mv.visitFieldInsn(Opcodes.PUTFIELD, Type.getInternalName(CpuState.class), "hilo", Type.getDescriptor(long.class));

        hiloPrepared = false;
	}

	public void loadFcr31c() {
    	loadFcr31();
        mv.visitFieldInsn(Opcodes.GETFIELD, Type.getInternalName(Fcr31.class), "c", "Z");
    }

	public void loadVcrCc() {
		loadVcrCc((codeInstruction.getOpcode() >> 18) & 7);
	}

	private void loadVcrCc(int cc) {
    	loadVcr();
        mv.visitFieldInsn(Opcodes.GETFIELD, Type.getInternalName(Vcr.class), "cc", "[Z");
    	loadImm(cc);
    	mv.visitInsn(Opcodes.BALOAD);
    }

    private void loadLocalVar(int localVar) {
        mv.visitVarInsn(Opcodes.ILOAD, localVar);
    }

    private void loadInstruction(Instruction insn) {
    	String classInternalName = instructionsInternalName;

    	if (insn == Common.UNK) {
    		// UNK instruction is in Common class, not Instructions
    		classInternalName = Type.getInternalName(Common.class);
    	}

    	mv.visitFieldInsn(Opcodes.GETSTATIC, classInternalName, insn.name().replace('.', '_').replace(' ', '_'), instructionDescriptor);
    }

    public void visitJump() {
    	flushInstructionCount(true, false);

    	//
        //      jr x
        //
        // translates to:
        //
        //      while (true) {
        //          if (x == returnAddress || isJump) {
        //              return x;
        //          }
        //          x = RuntimeContext.jump(x, returnAddress)
        //      }
        //
        Label returnLabel = new Label();
        Label jumpLabel = new Label();
        Label jumpLoop = new Label();

        mv.visitLabel(jumpLoop);
        mv.visitInsn(Opcodes.DUP);
        loadLocalVar(LOCAL_RETURN_ADDRESS);
        visitJump(Opcodes.IF_ICMPEQ, returnLabel);
        mv.visitInsn(Opcodes.DUP);
        loadLocalVar(LOCAL_ALTERVATIVE_RETURN_ADDRESS);
        visitJump(Opcodes.IF_ICMPEQ, returnLabel);
        loadLocalVar(LOCAL_IS_JUMP);
        visitJump(Opcodes.IFEQ, jumpLabel);

        mv.visitLabel(returnLabel);
        endMethod();
        mv.visitInsn(Opcodes.IRETURN);

        mv.visitLabel(jumpLabel);
        loadLocalVar(LOCAL_RETURN_ADDRESS);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeContextInternalName, "jump", "(II)I");
        visitJump(Opcodes.GOTO, jumpLoop);
    }

    public void visitCall(int address, int returnAddress, int returnRegister, boolean useAltervativeReturnAddress) {
    	NativeCodeSequence calledNativeCodeBlock = null;

    	// Do not call native block directly if we are profiling,
        // this would loose profiler information
        if (!Profiler.enableProfiler) {
        	// Is a native equivalent for this CodeBlock available?
        	calledNativeCodeBlock = nativeCodeManager.getCompiledNativeCodeBlock(address);
        }

        if (calledNativeCodeBlock != null) {
    		if (calledNativeCodeBlock.getNativeCodeSequenceClass().equals(Nop.class)) {
        		// NativeCodeSequence Nop means nothing to do!
    		} else {
    			// Call NativeCodeSequence
    			if (Compiler.log.isDebugEnabled()) {
    				Compiler.log.debug(String.format("Inlining call at 0x%08X to %s", getCodeInstruction().getAddress(), calledNativeCodeBlock));
    			}

    			visitNativeCodeSequence(calledNativeCodeBlock);
    		}
    	} else {
	    	if (useAltervativeReturnAddress) {
	    		loadLocalVar(LOCAL_RETURN_ADDRESS);
	    		loadImm(returnAddress);
		        if (returnRegister != 0) {
		        	prepareRegisterForStore(returnRegister);
		    		loadImm(returnAddress);
		            storeRegister(returnRegister);
		        }
	    	} else {
		        mv.visitLdcInsn(returnAddress);
		        if (returnRegister != 0) {
		        	prepareRegisterForStore(returnRegister);
		    		loadImm(returnAddress);
		            storeRegister(returnRegister);
		        }
		    	mv.visitInsn(Opcodes.DUP);			// alternativeReturnAddress = returnAddress
	    	}
	        mv.visitInsn(Opcodes.ICONST_0);		// isJump = false
	        mv.visitMethodInsn(Opcodes.INVOKESTATIC, getClassName(address), getStaticExecMethodName(), getStaticExecMethodDesc());
	        if (useAltervativeReturnAddress) {
	        	Label doNotReturnImmediately = new Label();
	        	mv.visitInsn(Opcodes.DUP);
	        	loadLocalVar(LOCAL_RETURN_ADDRESS);
	    		mv.visitJumpInsn(Opcodes.IF_ICMPNE, doNotReturnImmediately);
	    		endMethod();
	    		mv.visitInsn(Opcodes.IRETURN);
	        	mv.visitLabel(doNotReturnImmediately);
	        	mv.visitInsn(Opcodes.POP);
	        } else {
	        	mv.visitInsn(Opcodes.POP);
	        }
    	}
    }

    public void visitCall(int returnAddress, int returnRegister) {
        if (returnRegister != 0) {
            storeRegister(returnRegister, returnAddress);
        }
        mv.visitLdcInsn(returnAddress);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeContextInternalName, "call", "(II)V");
    }

    public void visitCall(int address, String methodName) {
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, getClassName(address), methodName, "()V");
    }

    public void visitIntepreterCall(int opcode, Instruction insn) {
    	loadInstruction(insn);
        loadProcessor();
        mv.visitLdcInsn(opcode);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, instructionInternalName, "interpret", "(" + processorDescriptor + "I)V");
    }

    public void visitSyscall(int opcode) {
    	flushInstructionCount(false, false);

    	int code = (opcode >> 6) & 0x000FFFFF;
    	mv.visitLdcInsn(code);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeContextInternalName, "syscall", "(I)V");

        if (storeGprLocal) {
        	// Reload "gpr", it could have been changed due to a thread switch
        	mv.visitFieldInsn(Opcodes.GETSTATIC, runtimeContextInternalName, "gpr", "[I");
            mv.visitVarInsn(Opcodes.ASTORE, LOCAL_GPR);
        }

        if (storeCpuLocal) {
        	// Reload "cpu", it could have been changed due to a thread switch
        	mv.visitFieldInsn(Opcodes.GETSTATIC, runtimeContextInternalName, "cpu", cpuDescriptor);
            mv.visitVarInsn(Opcodes.ASTORE, LOCAL_CPU);
        }
    }

    public void startClass(ClassVisitor cv) {
    	if (RuntimeContext.enableLineNumbers) {
    		cv.visitSource(getCodeBlock().getClassName() + ".java", null);
    	}
    }

    public void startSequenceMethod() {
        if (storeProcessorLocal) {
            mv.visitFieldInsn(Opcodes.GETSTATIC, runtimeContextInternalName, "processor", processorDescriptor);
            mv.visitVarInsn(Opcodes.ASTORE, LOCAL_PROCESSOR);
        }

        if (storeGprLocal) {
            mv.visitFieldInsn(Opcodes.GETSTATIC, runtimeContextInternalName, "gpr", "[I");
            mv.visitVarInsn(Opcodes.ASTORE, LOCAL_GPR);
        }

        if (storeCpuLocal) {
            mv.visitFieldInsn(Opcodes.GETSTATIC, runtimeContextInternalName, "cpu", cpuDescriptor);
            mv.visitVarInsn(Opcodes.ASTORE, LOCAL_CPU);
        }

        if (RuntimeContext.enableIntructionCounting) {
            currentInstructionCount = 0;
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitVarInsn(Opcodes.ISTORE, LOCAL_INSTRUCTION_COUNT);
        }
    }

    public void endSequenceMethod() {
        mv.visitInsn(Opcodes.RETURN);
    }

    public void checkSync() {
    	if (RuntimeContext.enableDaemonThreadSync) {
    		Label doNotWantSync = new Label();
            mv.visitFieldInsn(Opcodes.GETSTATIC, runtimeContextInternalName, "wantSync", "Z");
            mv.visitJumpInsn(Opcodes.IFEQ, doNotWantSync);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeContextInternalName, RuntimeContext.syncName, "()V");
            mv.visitLabel(doNotWantSync);
    	}
    }

    private void startInternalMethod() {
    	checkSync();

    	if (Profiler.enableProfiler) {
    		mv.visitLdcInsn(getCodeBlock().getStartAddress());
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, profilerInternalName, "addCall", "(I)V");
    	}

    	if (RuntimeContext.debugCodeBlockCalls) {
        	mv.visitLdcInsn(getCodeBlock().getStartAddress());
        	loadLocalVar(LOCAL_RETURN_ADDRESS);
        	loadLocalVar(LOCAL_ALTERVATIVE_RETURN_ADDRESS);
        	loadLocalVar(LOCAL_IS_JUMP);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeContextInternalName, RuntimeContext.debugCodeBlockStart, "(IIIZ)V");
        }
    }

    public void startMethod() {
    	startInternalMethod();
    	startSequenceMethod();
    }

    private void flushInstructionCount(boolean local, boolean last) {
        if (RuntimeContext.enableIntructionCounting) {
        	if (local) {
        		if (currentInstructionCount > 0) {
        			mv.visitIincInsn(LOCAL_INSTRUCTION_COUNT, currentInstructionCount);
        		}
        	} else {
		        mv.visitFieldInsn(Opcodes.GETSTATIC, runtimeContextInternalName, "currentThread", sceKernalThreadInfoDescriptor);
		        mv.visitInsn(Opcodes.DUP);
		        mv.visitFieldInsn(Opcodes.GETFIELD, sceKernalThreadInfoInternalName, "runClocks", "J");
		        mv.visitVarInsn(Opcodes.ILOAD, LOCAL_INSTRUCTION_COUNT);
		        if (currentInstructionCount > 0) {
		        	mv.visitLdcInsn(currentInstructionCount);
			        mv.visitInsn(Opcodes.IADD);
		        }
		        if (Profiler.enableProfiler) {
			        mv.visitInsn(Opcodes.DUP);
		    		mv.visitLdcInsn(getCodeBlock().getStartAddress());
		            mv.visitMethodInsn(Opcodes.INVOKESTATIC, profilerInternalName, "addInstructionCount", "(II)V");
		        }
		        mv.visitInsn(Opcodes.I2L);
		        mv.visitInsn(Opcodes.LADD);
		        mv.visitFieldInsn(Opcodes.PUTFIELD, sceKernalThreadInfoInternalName, "runClocks", "J");
		        if (!last) {
		        	mv.visitInsn(Opcodes.ICONST_0);
		        	mv.visitVarInsn(Opcodes.ISTORE, LOCAL_INSTRUCTION_COUNT);
		        }
        	}
	        currentInstructionCount = 0;
        }
    }

    private void endInternalMethod() {
        if (RuntimeContext.debugCodeBlockCalls) {
            mv.visitInsn(Opcodes.DUP);
        	mv.visitLdcInsn(getCodeBlock().getStartAddress());
            mv.visitInsn(Opcodes.SWAP);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeContextInternalName, RuntimeContext.debugCodeBlockEnd, "(II)V");
        }
    }

    public void endMethod() {
    	endInternalMethod();
    	flushInstructionCount(false, true);
    }

    public void beforeInstruction(CodeInstruction codeInstruction) {
	    if (RuntimeContext.enableIntructionCounting) {
	    	if (codeInstruction.isBranchTarget()) {
	    		flushInstructionCount(true, false);
	    	}
	    	currentInstructionCount++;
	    }

	    if (RuntimeContext.enableLineNumbers) {
	    	// Force the instruction to emit a label
    		codeInstruction.getLabel(false);
    	}
    }

    public void startInstruction(CodeInstruction codeInstruction) {
    	if (RuntimeContext.enableLineNumbers) {
    		int lineNumber = codeInstruction.getAddress() - getCodeBlock().getLowestAddress();
    		// Java line number is unsigned 16bits
    		if (lineNumber >= 0 && lineNumber <= 0xFFFF) {
    			mv.visitLineNumber(lineNumber, codeInstruction.getLabel());
    		}
    	}

    	if (RuntimeContext.debugCodeInstruction) {
        	mv.visitLdcInsn(codeInstruction.getAddress());
        	mv.visitLdcInsn(codeInstruction.getOpcode());
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeContextInternalName, RuntimeContext.debugCodeInstructionName, "(II)V");
	    }

	    if (RuntimeContext.enableInstructionTypeCounting) {
	    	if (codeInstruction.getInsn() != null) {
		    	loadInstruction(codeInstruction.getInsn());
	            mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeContextInternalName, RuntimeContext.instructionTypeCount, "(" + instructionDescriptor + ")V");
	    	}
	    }

	    if (RuntimeContext.enableDebugger) {
	    	mv.visitLdcInsn(codeInstruction.getAddress());
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeContextInternalName, RuntimeContext.debuggerName, "(I)V");
	    }

	    nativeCodeSequence = nativeCodeManager.getNativeCodeSequence(getCodeInstruction(), getCodeBlock());
    }

    public void visitJump(int opcode, CodeInstruction target) {
    	// Back branch? i.e probably a loop
        if (target.getAddress() <= getCodeInstruction().getAddress()) {
        	checkSync();

        	if (Profiler.enableProfiler) {
        		mv.visitLdcInsn(getCodeInstruction().getAddress());
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, profilerInternalName, "addBackBranch", "(I)V");
        	}
        }
        visitJump(opcode, target.getLabel());
    }

    public void visitJump(int opcode, Label label) {
    	flushInstructionCount(true, false);
        mv.visitJumpInsn(opcode, label);
    }

    public void visitJump(int opcode, int address) {
        flushInstructionCount(true, false);
        if (opcode == Opcodes.GOTO) {
            mv.visitLdcInsn(address);
            visitJump();
        } else {
            Compiler.log.error("Not implemented: branching to an unknown address");
            if (opcode == Opcodes.IF_ACMPEQ ||
                opcode == Opcodes.IF_ACMPNE ||
                opcode == Opcodes.IF_ICMPEQ ||
                opcode == Opcodes.IF_ICMPNE ||
                opcode == Opcodes.IF_ICMPGE ||
                opcode == Opcodes.IF_ICMPGT ||
                opcode == Opcodes.IF_ICMPLE ||
                opcode == Opcodes.IF_ICMPLT) {
                // 2 Arguments to POP
                mv.visitInsn(Opcodes.POP);
            }
            mv.visitInsn(Opcodes.POP);
            mv.visitLdcInsn(address);
            visitJump();
        }
    }

    public static String getClassName(int address) {
    	return "_S1_" + Compiler.getResetCount() + "_" + Integer.toHexString(address).toUpperCase();
    }

    public static int getClassAddress(String name) {
    	String hexAddress = name.substring(name.lastIndexOf("_") + 1);

        return Integer.parseInt(hexAddress, 16);
    }

    public String getExecMethodName() {
        return "exec";
    }

    public String getExecMethodDesc() {
        return "(IIZ)I";
    }

    public String getStaticExecMethodName() {
        return "s";
    }

    public String getStaticExecMethodDesc() {
        return "(IIZ)I";
    }

    public boolean isAutomaticMaxLocals() {
        return false;
    }

    public int getMaxLocals() {
        return LOCAL_MAX;
    }

    public boolean isAutomaticMaxStack() {
        return false;
    }

    public int getMaxStack() {
        return STACK_MAX;
    }

    public void visitPauseEmuWithStatus(MethodVisitor mv, int status) {
    	mv.visitLdcInsn(status);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeContextInternalName, RuntimeContext.pauseEmuWithStatus, "(I)V");
    }

    public void visitLogInfo(MethodVisitor mv, String message) {
    	mv.visitLdcInsn(message);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeContextInternalName, RuntimeContext.logInfo, "(" + stringDescriptor + ")V");
    }

	@Override
	public MethodVisitor getMethodVisitor() {
		return mv;
	}

	public void setMethodVisitor(MethodVisitor mv) {
		this.mv = mv;
	}

	public CodeInstruction getCodeInstruction() {
		return codeInstruction;
	}

	public void setCodeInstruction(CodeInstruction codeInstruction) {
		this.codeInstruction = codeInstruction;
	}

	@Override
	public int getSaValue() {
        return (codeInstruction.getOpcode() >> 6) & 31;
    }

	@Override
	public int getRsRegisterIndex() {
        return (codeInstruction.getOpcode() >> 21) & 31;
    }

	@Override
    public int getRtRegisterIndex() {
        return (codeInstruction.getOpcode() >> 16) & 31;
    }

	@Override
    public int getRdRegisterIndex() {
        return (codeInstruction.getOpcode() >> 11) & 31;
    }

	@Override
    public void loadRs() {
        loadRegister(getRsRegisterIndex());
    }

	@Override
    public void loadRt() {
        loadRegister(getRtRegisterIndex());
    }

	@Override
    public void loadRd() {
        loadRegister(getRdRegisterIndex());
    }

	@Override
    public void loadSaValue() {
        loadImm(getSaValue());
    }

    public void loadRegisterIndex(int registerIndex) {
    	mv.visitLdcInsn(registerIndex);
    }

    public void loadRsIndex() {
        loadRegisterIndex(getRsRegisterIndex());
    }

    public void loadRtIndex() {
        loadRegisterIndex(getRtRegisterIndex());
    }

    public void loadRdIndex() {
        loadRegisterIndex(getRdRegisterIndex());
    }

	@Override
    public int getImm16(boolean signedImm) {
    	int imm16 = codeInstruction.getOpcode()& 0xFFFF;
    	if (signedImm) {
    		imm16 = (int)(short) imm16;
    	}

    	return imm16;
    }

	@Override
    public void loadImm16(boolean signedImm) {
    	loadImm(getImm16(signedImm));
    }

	@Override
    public void loadImm(int imm) {
		switch (imm) {
			case -1: mv.visitInsn(Opcodes.ICONST_M1); break;
			case  0: mv.visitInsn(Opcodes.ICONST_0);  break;
			case  1: mv.visitInsn(Opcodes.ICONST_1);  break;
			case  2: mv.visitInsn(Opcodes.ICONST_2);  break;
			case  3: mv.visitInsn(Opcodes.ICONST_3);  break;
			case  4: mv.visitInsn(Opcodes.ICONST_4);  break;
			case  5: mv.visitInsn(Opcodes.ICONST_5);  break;
			default: mv.visitLdcInsn(imm);            break;
		}
    }

	@Override
	public void compileInterpreterInstruction() {
		visitIntepreterCall(codeInstruction.getOpcode(), codeInstruction.getInsn());
	}

	@Override
	public void compileRTRSIMM(String method, boolean signedImm) {
		loadCpu();
		loadRtIndex();
		loadRsIndex();
		loadImm16(signedImm);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, cpuInternalName, method, "(III)V");
	}

	@Override
	public void compileRDRSRT(String method) {
		loadCpu();
		loadRdIndex();
		loadRsIndex();
		loadRtIndex();
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, cpuInternalName, method, "(III)V");
	}

	@Override
	public void compileRDRTRS(String method) {
		loadCpu();
		loadRdIndex();
		loadRtIndex();
		loadRsIndex();
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, cpuInternalName, method, "(III)V");
	}

	@Override
	public void compileRDRT(String method) {
		loadCpu();
		loadRdIndex();
		loadRtIndex();
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, cpuInternalName, method, "(II)V");
	}

	@Override
	public void storeRd() {
		storeRegister(getRdRegisterIndex());
	}

	@Override
	public void storeRt() {
		storeRegister(getRtRegisterIndex());
	}

    @Override
	public boolean isRdRegister0() {
		return getRdRegisterIndex() == 0;
	}

	@Override
	public boolean isRtRegister0() {
		return getRtRegisterIndex() == 0;
	}

	@Override
	public boolean isRsRegister0() {
		return getRsRegisterIndex() == 0;
	}

	@Override
	public void prepareRdForStore() {
		prepareRegisterForStore(getRdRegisterIndex());
	}

	@Override
	public void prepareRtForStore() {
		prepareRegisterForStore(getRtRegisterIndex());
	}

	@Override
	public void memRead32(int registerIndex, int offset) {
		if (RuntimeContext.memoryInt == null) {
    		mv.visitFieldInsn(Opcodes.GETSTATIC, runtimeContextInternalName, "memory", memoryDescriptor);
		} else {
    		mv.visitFieldInsn(Opcodes.GETSTATIC, runtimeContextInternalName, "memoryInt", "[I");
		}

		loadRegister(registerIndex);
		if (offset != 0) {
			loadImm(offset);
			mv.visitInsn(Opcodes.IADD);
		}

		if (RuntimeContext.debugMemoryRead && (!RuntimeContext.debugMemoryReadWriteNoSP || registerIndex != spRegisterIndex)) {
			mv.visitInsn(Opcodes.DUP);
			loadImm(0);
            mv.visitLdcInsn(codeInstruction.getAddress());
			loadImm(1);
			loadImm(32);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeContextInternalName, "debugMemoryReadWrite", "(IIIZI)V");
		}

		if (RuntimeContext.memoryInt == null) {
	        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, memoryInternalName, "read32", "(I)I");
		} else {
			if (registerIndex == spRegisterIndex) {
				// No need to check for a valid memory access when referencing the $sp register
				loadImm(2);
    			mv.visitInsn(Opcodes.IUSHR);
			} else if (checkMemoryAccess()) {
                mv.visitLdcInsn(codeInstruction.getAddress());
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeContextInternalName, "checkMemoryRead32", "(II)I");
                loadImm(2);
                mv.visitInsn(Opcodes.IUSHR);
            } else {
    			// memoryInt[(address & 0x3FFFFFFF) / 4] == memoryInt[(address << 2) >>> 4]
    			loadImm(2);
    			mv.visitInsn(Opcodes.ISHL);
    			loadImm(4);
    			mv.visitInsn(Opcodes.IUSHR);
            }
			mv.visitInsn(Opcodes.IALOAD);
		}
	}

	@Override
	public void memRead16(int registerIndex, int offset) {
		if (RuntimeContext.memoryInt == null) {
    		mv.visitFieldInsn(Opcodes.GETSTATIC, runtimeContextInternalName, "memory", memoryDescriptor);
		} else {
    		mv.visitFieldInsn(Opcodes.GETSTATIC, runtimeContextInternalName, "memoryInt", "[I");
		}

		loadRegister(registerIndex);
		if (offset != 0) {
			loadImm(offset);
			mv.visitInsn(Opcodes.IADD);
		}

		if (RuntimeContext.debugMemoryRead) {
			mv.visitInsn(Opcodes.DUP);
			loadImm(0);
            mv.visitLdcInsn(codeInstruction.getAddress());
			loadImm(1);
			loadImm(16);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeContextInternalName, "debugMemoryReadWrite", "(IIIZI)V");
		}

		if (RuntimeContext.memoryInt == null) {
	        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, memoryInternalName, "read16", "(I)I");
		} else {
            if (checkMemoryAccess()) {
                mv.visitLdcInsn(codeInstruction.getAddress());
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeContextInternalName, "checkMemoryRead16", "(II)I");
                loadImm(1);
                mv.visitInsn(Opcodes.IUSHR);
            } else {
    			// memoryInt[(address & 0x3FFFFFFF) / 4] == memoryInt[(address << 2) >>> 4]
    			loadImm(2);
    			mv.visitInsn(Opcodes.ISHL);
    			loadImm(3);
    			mv.visitInsn(Opcodes.IUSHR);
            }
			mv.visitInsn(Opcodes.DUP);
			loadImm(1);
			mv.visitInsn(Opcodes.IAND);
			loadImm(4);
			mv.visitInsn(Opcodes.ISHL);
			mv.visitVarInsn(Opcodes.ISTORE, LOCAL_TMP);
			loadImm(1);
			mv.visitInsn(Opcodes.IUSHR);
			mv.visitInsn(Opcodes.IALOAD);
			mv.visitVarInsn(Opcodes.ILOAD, LOCAL_TMP);
			mv.visitInsn(Opcodes.IUSHR);
			loadImm(0xFFFF);
			mv.visitInsn(Opcodes.IAND);
		}
	}

	@Override
	public void memRead8(int registerIndex, int offset) {
		if (RuntimeContext.memoryInt == null) {
    		mv.visitFieldInsn(Opcodes.GETSTATIC, runtimeContextInternalName, "memory", memoryDescriptor);
		} else {
    		mv.visitFieldInsn(Opcodes.GETSTATIC, runtimeContextInternalName, "memoryInt", "[I");
		}

		loadRegister(registerIndex);
		if (offset != 0) {
			loadImm(offset);
			mv.visitInsn(Opcodes.IADD);
		}

		if (RuntimeContext.debugMemoryRead) {
			mv.visitInsn(Opcodes.DUP);
			loadImm(0);
            mv.visitLdcInsn(codeInstruction.getAddress());
			loadImm(1);
			loadImm(8);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeContextInternalName, "debugMemoryReadWrite", "(IIIZI)V");
		}

		if (RuntimeContext.memoryInt == null) {
	        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, memoryInternalName, "read8", "(I)I");
		} else {
            if (checkMemoryAccess()) {
                mv.visitLdcInsn(codeInstruction.getAddress());
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeContextInternalName, "checkMemoryRead8", "(II)I");
            } else {
    			// memoryInt[(address & 0x3FFFFFFF) / 4] == memoryInt[(address << 2) >>> 4]
    			loadImm(2);
    			mv.visitInsn(Opcodes.ISHL);
    			loadImm(2);
    			mv.visitInsn(Opcodes.IUSHR);
            }
			mv.visitInsn(Opcodes.DUP);
			loadImm(3);
			mv.visitInsn(Opcodes.IAND);
			loadImm(3);
			mv.visitInsn(Opcodes.ISHL);
			mv.visitVarInsn(Opcodes.ISTORE, LOCAL_TMP);
			loadImm(2);
			mv.visitInsn(Opcodes.IUSHR);
			mv.visitInsn(Opcodes.IALOAD);
			mv.visitVarInsn(Opcodes.ILOAD, LOCAL_TMP);
			mv.visitInsn(Opcodes.IUSHR);
			loadImm(0xFF);
			mv.visitInsn(Opcodes.IAND);
		}
	}

	@Override
	public void prepareMemWrite32(int registerIndex, int offset) {
		if (RuntimeContext.memoryInt == null) {
    		mv.visitFieldInsn(Opcodes.GETSTATIC, runtimeContextInternalName, "memory", memoryDescriptor);
		} else {
    		mv.visitFieldInsn(Opcodes.GETSTATIC, runtimeContextInternalName, "memoryInt", "[I");
		}

		loadRegister(registerIndex);
		if (offset != 0) {
			loadImm(offset);
			mv.visitInsn(Opcodes.IADD);
		}

		if (RuntimeContext.memoryInt != null) {
			if (registerIndex == spRegisterIndex) {
				// No need to check for a valid memory access when referencing the $sp register
				loadImm(2);
    			mv.visitInsn(Opcodes.IUSHR);
			} else if (checkMemoryAccess()) {
	            mv.visitLdcInsn(codeInstruction.getAddress());
	            mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeContextInternalName, "checkMemoryWrite32", "(II)I");
                loadImm(2);
                mv.visitInsn(Opcodes.IUSHR);
	        } else {
    			// memoryInt[(address & 0x3FFFFFFF) / 4] == memoryInt[(address << 2) >>> 4]
    			loadImm(2);
    			mv.visitInsn(Opcodes.ISHL);
    			loadImm(4);
    			mv.visitInsn(Opcodes.IUSHR);
	        }
		}

		memWrite32prepared = true;
	}

	@Override
	public void memWrite32(int registerIndex, int offset) {
		if (!memWrite32prepared) {
			if (RuntimeContext.memoryInt == null) {
	    		mv.visitFieldInsn(Opcodes.GETSTATIC, runtimeContextInternalName, "memory", memoryDescriptor);
			} else {
	    		mv.visitFieldInsn(Opcodes.GETSTATIC, runtimeContextInternalName, "memoryInt", "[I");
			}
			mv.visitInsn(Opcodes.SWAP);

			loadRegister(registerIndex);
			if (offset != 0) {
				loadImm(offset);
				mv.visitInsn(Opcodes.IADD);
			}
            if (checkMemoryAccess()) {
                mv.visitLdcInsn(codeInstruction.getAddress());
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeContextInternalName, "checkMemoryWrite32", "(II)I");
            }
			mv.visitInsn(Opcodes.SWAP);
		}

		if (RuntimeContext.debugMemoryWrite && (!RuntimeContext.debugMemoryReadWriteNoSP || registerIndex != spRegisterIndex)) {
			mv.visitInsn(Opcodes.DUP2);
			mv.visitInsn(Opcodes.SWAP);
			loadImm(2);
			mv.visitInsn(Opcodes.ISHL);
			mv.visitInsn(Opcodes.SWAP);
            mv.visitLdcInsn(codeInstruction.getAddress());
			loadImm(0);
			loadImm(32);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeContextInternalName, "debugMemoryReadWrite", "(IIIZI)V");
		}

		if (RuntimeContext.memoryInt == null) {
	        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, memoryInternalName, "write32", "(II)V");
		} else {
			mv.visitInsn(Opcodes.IASTORE);
		}

		memWrite32prepared = false;
	}

	@Override
	public void compileSyscall() {
		visitSyscall(codeInstruction.getOpcode());
	}

	@Override
	public void compileRDRTSA(String method) {
		loadCpu();
		loadRdIndex();
		loadRtIndex();
		loadSaValue();
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, cpuInternalName, method, "(III)V");
	}

	@Override
	public void convertUnsignedIntToLong() {
		mv.visitInsn(Opcodes.I2L);
		mv.visitLdcInsn(0xFFFFFFFFL);
		mv.visitInsn(Opcodes.LAND);
	}

    public int getMethodMaxInstructions() {
        return methodMaxInstructions;
    }

    public void setMethodMaxInstructions(int methodMaxInstructions) {
        this.methodMaxInstructions = methodMaxInstructions;
    }

    private boolean checkMemoryAccess() {
        if (RuntimeContext.memoryInt == null) {
            return false;
        }

        if (RuntimeContext.memory instanceof SafeFastMemory) {
            return true;
        }

        return false;
    }

    public void compileDelaySlotAsBranchTarget(CodeInstruction codeInstruction) {
    	Label afterDelaySlot = new Label();
    	mv.visitJumpInsn(Opcodes.GOTO, afterDelaySlot);
    	codeInstruction.compile(this, mv);
    	mv.visitLabel(afterDelaySlot);
    }

    public void compileExecuteInterpreter(int startAddress) {
    	loadImm(startAddress);
        loadLocalVar(LOCAL_RETURN_ADDRESS);
        loadLocalVar(LOCAL_ALTERVATIVE_RETURN_ADDRESS);
        loadLocalVar(LOCAL_IS_JUMP);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeContextInternalName, "executeInterpreter", "(IIIZ)I");
        endMethod();
        mv.visitInsn(Opcodes.IRETURN);
    }

	public boolean isNativeCodeSequence() {
		return nativeCodeSequence != null;
	}

	private void visitNativeCodeSequence(NativeCodeSequence nativeCodeSequence) {
    	StringBuilder methodSignature = new StringBuilder("(");
    	int numberParameters = nativeCodeSequence.getNumberParameters();
    	for (int i = 0; i < numberParameters; i++) {
    		loadImm(nativeCodeSequence.getParameter(i));
    		methodSignature.append("I");
    	}
    	methodSignature.append(")V");
	    mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(nativeCodeSequence.getNativeCodeSequenceClass()), "call", methodSignature.toString());

	    if (nativeCodeSequence.hasBranchInstruction()) {
	    	int branchInstructionAddress = getCodeInstruction().getAddress() + nativeCodeSequence.getBranchInstructionAddressOffset();

	    	CodeInstruction branchInstruction = getCodeBlock().getCodeInstruction(branchInstructionAddress);
	    	if (branchInstruction != null) {
	    		int branchingTo = branchInstruction.getBranchingTo();
	    		CodeInstruction targetInstruction = getCodeBlock().getCodeInstruction(branchingTo);
	    		if (targetInstruction != null) {
	    			visitJump(Opcodes.GOTO, targetInstruction);
	    		} else {
	    			visitJump(Opcodes.GOTO, branchingTo);
	    		}
	    	}
	    }
	}

	public void compileNativeCodeSequence() {
	    if (nativeCodeSequence == null) {
	    	return;
	    }

    	int numOpcodes = nativeCodeSequence.getNumOpcodes();
	    skipInstructions(numOpcodes - 1, true);

	    visitNativeCodeSequence(nativeCodeSequence);

	    if (nativeCodeSequence.isReturning()) {
	    	loadLocalVar(LOCAL_RETURN_ADDRESS);
	        endInternalMethod();
	        mv.visitInsn(Opcodes.IRETURN);
	    }

	    // Replacing the whole CodeBlock?
	    if (numOpcodes == getCodeBlock().getLength()) {
        	nativeCodeManager.setCompiledNativeCodeBlock(getCodeBlock().getStartAddress(), nativeCodeSequence);

        	// Be more verbose when Debug enabled
	        if (Compiler.log.isDebugEnabled()) {
	        	Compiler.log.info(String.format("Replacing CodeBlock at 0x%08X (%08X-0x%08X, length %d) by %s", getCodeBlock().getStartAddress(), getCodeBlock().getLowestAddress(), codeBlock.getHighestAddress(), codeBlock.getLength(), nativeCodeSequence));
	        } else if (Compiler.log.isInfoEnabled()) {
	        	Compiler.log.info(String.format("Replacing CodeBlock at 0x%08X by Native Code '%s'", getCodeBlock().getStartAddress(), nativeCodeSequence.getName()));
	        }
	    } else {
        	// Be more verbose when Debug enabled
	    	if (Compiler.log.isDebugEnabled()) {
		    	Compiler.log.debug(String.format("Replacing CodeSequence at 0x%08X-0x%08X by Native Code %s", getCodeInstruction().getAddress(), getCodeInstruction().getAddress() + (numOpcodes - 1) * 4, nativeCodeSequence));
	        } else if (Compiler.log.isInfoEnabled()) {
		    	Compiler.log.info(String.format("Replacing CodeSequence at 0x%08X-0x%08X by Native Code '%s'", getCodeInstruction().getAddress(), getCodeInstruction().getAddress() + (numOpcodes - 1) * 4, nativeCodeSequence.getName()));
	    	}
	    }

	    nativeCodeSequence = null;
	}

	public int getNumberInstructionsToBeSkipped() {
		return numberInstructionsToBeSkipped;
	}

	public boolean isSkipDelaySlot() {
		return skipDelaySlot;
	}

	public void skipInstructions(int numberInstructionsToBeSkipped, boolean skipDelaySlot) {
		this.numberInstructionsToBeSkipped = numberInstructionsToBeSkipped;
		this.skipDelaySlot = skipDelaySlot;
	}
}
