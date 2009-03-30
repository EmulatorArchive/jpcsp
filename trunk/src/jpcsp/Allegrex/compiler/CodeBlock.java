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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

import jpcsp.Allegrex.Common.Instruction;

/**
 * @author gid15
 *
 */
public class CodeBlock {
	private int startAddress;
	private int lowestAddress;
	private LinkedList<CodeInstruction> codeInstructions = new LinkedList<CodeInstruction>();
	private LinkedList<SequenceCodeInstruction> sequenceCodeInstructions = new LinkedList<SequenceCodeInstruction>();
	private SequenceCodeInstruction currentSequence = null;
	private IExecutable executable = null;
	private final static String objectInternalName = Type.getInternalName(Object.class);
	private final static String[] interfacesForExecutable = new String[] { Type.getInternalName(IExecutable.class) };
	private final static String[] exceptions = new String[] { Type.getInternalName(Exception.class) };

	public CodeBlock(int startAddress) {
		this.startAddress = startAddress;
		this.lowestAddress = startAddress;

		RuntimeContext.addCodeBlock(startAddress, this);
	}

	public void addInstruction(int address, int opcode, Instruction insn, boolean isBranchTarget, boolean isBranching, int branchingTo) {
		if (Compiler.log.isDebugEnabled()) {
			Compiler.log.debug("CodeBlock.addInstruction 0x" + Integer.toHexString(address).toUpperCase() + " - " + insn.disasm(address, opcode));
		}

		CodeInstruction codeInstruction = new CodeInstruction(address, opcode, insn, isBranchTarget, isBranching, branchingTo);

		// Insert the codeInstruction in the codeInstructions list
		// and keep the list sorted by address.
		if (codeInstructions.isEmpty() || codeInstructions.getLast().getAddress() < address) {
			codeInstructions.add(codeInstruction);
		} else {
			for (ListIterator<CodeInstruction> lit = codeInstructions.listIterator(); lit.hasNext(); ) {
				CodeInstruction listItem = lit.next();
				if (listItem.getAddress() > address) {
					lit.previous();
					lit.add(codeInstruction);
					break;
				}
			}

			if (address < lowestAddress) {
				lowestAddress = address;
			}
		}
	}

	public void setIsBranchTarget(int address) {
		if (Compiler.log.isDebugEnabled()) {
			Compiler.log.debug("CodeBlock.setIsBranchTarget 0x" + Integer.toHexString(address).toUpperCase());
		}

		CodeInstruction codeInstruction = getCodeInstruction(address);
		if (codeInstruction != null) {
			codeInstruction.setBranchTarget(true);
		}
	}

	public int getStartAddress() {
		return startAddress;
	}

	public int getLowestAddress() {
		return lowestAddress;
	}

	public CodeInstruction getCodeInstruction(int address) {
	    if (currentSequence != null) {
            return currentSequence.getCodeSequence().getCodeInstruction(address);
	    }

	    for (CodeInstruction codeInstruction : codeInstructions) {
			if (codeInstruction.getAddress() == address) {
				return codeInstruction;
			}
		}

		return null;
	}

	public String getClassName() {
	    return CompilerContext.getClassName(getStartAddress());
	}

	public String getInternalClassName() {
		return getInternalName(getClassName());
	}

	private String getInternalName(String name) {
		return name.replace('.', '/');
	}

	@SuppressWarnings("unchecked")
	private Class<IExecutable> loadExecutable(CompilerContext context, String className, byte[] bytes) {
    	return (Class<IExecutable>) context.getClassLoader().defineClass(className, bytes);
	}

	private void addConstructor(ClassVisitor cv) {
	    MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
	    mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, objectInternalName, "<init>", "()V");
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
	    mv.visitEnd();
	}

    private void addNonStaticMethods(CompilerContext context, ClassVisitor cv) {
        MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC, context.getExecMethodName(), context.getExecMethodDesc(), null, exceptions);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ILOAD, 1);
        mv.visitVarInsn(Opcodes.ILOAD, 2);
        mv.visitVarInsn(Opcodes.ILOAD, 3);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, getClassName(), context.getStaticExecMethodName(), context.getStaticExecMethodDesc());
        mv.visitInsn(Opcodes.IRETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        mv = cv.visitMethod(Opcodes.ACC_PUBLIC, "getCallCount", "()I", null, null);
        mv.visitCode();
        mv.visitFieldInsn(Opcodes.GETSTATIC, getClassName(), "callCount", "I");
        mv.visitInsn(Opcodes.IRETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        cv.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, "callCount", "I", null, 0);
    }

    private void generateCodeSequences(List<CodeSequence> codeSequences, int sequenceMaxInstructions) {
        CodeSequence currentCodeSequence = null;

        int nextAddress = 0;
        for (CodeInstruction codeInstruction : codeInstructions) {
            int address = codeInstruction.getAddress();
            if (address < nextAddress) {
                // Skip it
            } else {
                if (codeInstruction.hasFlags(Instruction.FLAG_CANNOT_BE_SPLIT)) {
                    if (currentCodeSequence != null) {
                        codeSequences.add(currentCodeSequence);
                    }
                    currentCodeSequence = null;
                    if (codeInstruction.hasFlags(Instruction.FLAG_HAS_DELAY_SLOT)) {
                        nextAddress = address + 8;
                    }
                } else if (codeInstruction.isBranchTarget()) {
                    if (currentCodeSequence != null) {
                        codeSequences.add(currentCodeSequence);
                    }
                    currentCodeSequence = new CodeSequence(address);
                } else {
                    if (currentCodeSequence == null) {
                        currentCodeSequence = new CodeSequence(address);
                    } else if (currentCodeSequence.getLength() >= sequenceMaxInstructions) {
                        codeSequences.add(currentCodeSequence);
                        currentCodeSequence = new CodeSequence(address);
                    }
                    currentCodeSequence.setEndAddress(address);
                }
            }
        }

        if (currentCodeSequence != null) {
            codeSequences.add(currentCodeSequence);
        }
    }

    private CodeSequence findCodeSequence(CodeInstruction codeInstruction, List<CodeSequence> codeSequences, CodeSequence currentCodeSequence) {
        int address = codeInstruction.getAddress();

        if (currentCodeSequence != null) {
            if (currentCodeSequence.isInside(address)) {
                return currentCodeSequence;
            }
        }

        for (CodeSequence codeSequence : codeSequences) {
            if (codeSequence.isInside(address)) {
                return codeSequence;
            }
        }

        return null;
    }

    private void splitCodeSequences(CompilerContext context, int methodMaxInstructions) {
        Vector<CodeSequence> codeSequences = new Vector<CodeSequence>();

        generateCodeSequences(codeSequences, methodMaxInstructions);
        Collections.sort(codeSequences);

        int currentMethodInstructions = codeInstructions.size();
        Vector<CodeSequence> sequencesToBeSplit = new Vector<CodeSequence>();
        for (CodeSequence codeSequence : codeSequences) {
            sequencesToBeSplit.add(codeSequence);
            if (Compiler.log.isDebugEnabled()) {
                Compiler.log.debug("Sequence to be split: " + codeSequence.toString());
            }
            currentMethodInstructions -= codeSequence.getLength();
            if (currentMethodInstructions <= methodMaxInstructions) {
                break;
            }
        }

        CodeSequence currentCodeSequence = null;
        for (ListIterator<CodeInstruction> lit = codeInstructions.listIterator(); lit.hasNext(); ) {
            CodeInstruction codeInstruction = lit.next();
            CodeSequence codeSequence = findCodeSequence(codeInstruction, sequencesToBeSplit, currentCodeSequence);
            if (codeSequence != null) {
                lit.remove();
                if (codeSequence.getInstructions().isEmpty()) {
                    codeSequence.addInstruction(codeInstruction);
                    SequenceCodeInstruction sequenceCodeInstruction = new SequenceCodeInstruction(codeSequence);
                    lit.add(sequenceCodeInstruction);
                    sequenceCodeInstructions.add(sequenceCodeInstruction);
                } else {
                    codeSequence.addInstruction(codeInstruction);
                }
                currentCodeSequence = codeSequence;
            }
        }
    }

    private void prepare(CompilerContext context, int methodMaxInstructions) {
        if (codeInstructions.size() > methodMaxInstructions) {
            if (Compiler.log.isInfoEnabled()) {
                Compiler.log.info("Splitting " + getClassName() + " (" + codeInstructions.size() + "/" + methodMaxInstructions + ")");
            }
            splitCodeSequences(context, methodMaxInstructions);
        }
    }

    private void compile(CompilerContext context, MethodVisitor mv, List<CodeInstruction> codeInstructions) {
        boolean skipNextInstruction = false;
        for (CodeInstruction codeInstruction : codeInstructions) {
            if (skipNextInstruction) {
            	if (codeInstruction.isBranchTarget()) {
            		context.compileDelaySlotAsBranchTarget(codeInstruction);
            	}
                skipNextInstruction = false;
            } else {
                codeInstruction.compile(context, mv);
                skipNextInstruction = context.isSkipNextIntruction();
                context.setSkipNextIntruction(false);
            }
        }
    }

    private Class<IExecutable> compile(CompilerContext context) {
		Class<IExecutable> compiledClass = null;

		context.setCodeBlock(this);
		String className = getInternalClassName();
		if (Compiler.log.isDebugEnabled()) {
		    Compiler.log.debug("Compiling " + className);
		}

        prepare(context, context.getMethodMaxInstructions());

        currentSequence = null;
        int computeFlag = ClassWriter.COMPUTE_FRAMES;
		if (context.isAutomaticMaxLocals() || context.isAutomaticMaxStack()) {
		    computeFlag |= ClassWriter.COMPUTE_MAXS;
		}
    	ClassWriter cw = new ClassWriter(computeFlag);
    	ClassVisitor cv = cw;
    	if (Compiler.log.isDebugEnabled()) {
    		cv = new CheckClassAdapter(cv);
    	}
        StringWriter debugOutput = null;
    	if (Compiler.log.isDebugEnabled()) {
    	    debugOutput = new StringWriter();
    	    PrintWriter debugPrintWriter = new PrintWriter(debugOutput);
    	    cv = new TraceClassVisitor(cv, debugPrintWriter);
    	}
    	cv.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, className, null, objectInternalName, interfacesForExecutable);
    	context.startClass(cv);

    	addConstructor(cv);
    	addNonStaticMethods(context, cv);

    	MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, context.getStaticExecMethodName(), context.getStaticExecMethodDesc(), null, exceptions);
        mv.visitCode();
        context.setMethodVisitor(mv);
        context.startMethod();

        // Jump to the block start if other instructions have been inserted in front
        if (!codeInstructions.isEmpty() && codeInstructions.getFirst().getAddress() != getStartAddress()) {
            mv.visitJumpInsn(Opcodes.GOTO, getCodeInstruction(getStartAddress()).getLabel());
        }

        compile(context, mv, codeInstructions);
        mv.visitMaxs(context.getMaxLocals(), context.getMaxStack());
        mv.visitEnd();

        for (SequenceCodeInstruction sequenceCodeInstruction : sequenceCodeInstructions) {
            if (Compiler.log.isDebugEnabled()) {
                Compiler.log.debug("Compiling Sequence " + sequenceCodeInstruction.getMethodName(context));
            }
            currentSequence = sequenceCodeInstruction;
            mv = cv.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, sequenceCodeInstruction.getMethodName(context), "()V", null, exceptions);
            mv.visitCode();
            context.setMethodVisitor(mv);
            context.startSequenceMethod();

            compile(context, mv, sequenceCodeInstruction.getCodeSequence().getInstructions());

            context.endSequenceMethod();
            mv.visitMaxs(context.getMaxLocals(), context.getMaxStack());
            mv.visitEnd();
        }
        currentSequence = null;

        cv.visitEnd();

    	if (debugOutput != null) {
    	    Compiler.log.debug(debugOutput.toString());
    	}

	    compiledClass = loadExecutable(context, className, cw.toByteArray());

    	return compiledClass;
	}

    public IExecutable getExecutable() {
        return executable;
    }

    public synchronized IExecutable getExecutable(CompilerContext context) {
	    if (executable == null) {
	        Class<IExecutable> classExecutable = compile(context);
	        if (classExecutable != null) {
	            try {
                    executable = classExecutable.newInstance();
                } catch (InstantiationException e) {
                    Compiler.log.error(e);
                } catch (IllegalAccessException e) {
                    Compiler.log.error(e);
                }
	        }
	    }

	    return executable;
	}
}
