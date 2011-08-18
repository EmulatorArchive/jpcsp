package jpcsp.HLE.modules;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import jpcsp.Processor;
import jpcsp.HLE.CanBeNull;
import jpcsp.HLE.CheckArgument;
import jpcsp.HLE.HLEUidClass;
import jpcsp.HLE.HLEUidObjectMapping;
import jpcsp.HLE.Modules;
import jpcsp.HLE.SceKernelErrorException;
import jpcsp.HLE.TErrorPointer32;
import jpcsp.HLE.TPointer;
import jpcsp.HLE.TPointer32;
import jpcsp.HLE.TPointer64;
import jpcsp.HLE.TPointerBase;
import jpcsp.HLE.kernel.managers.IntrManager;
import jpcsp.HLE.kernel.types.SceKernelErrors;

public class HLEModuleFunctionReflection extends HLEModuleFunction {
	HLEModule  hleModule;
	Class<?>   hleModuleClass;
	String     hleModuleMethodName;
	Method     hleModuleMethod;
	Class<?>[] hleModuleMethodParametersTypes;
	Class<?>   hleModuleMethodReturnType;
	boolean    checkInsideInterrupt;
	boolean    fastOldInvoke;
	TErrorPointer32 errorHolder;
	
	public HLEModuleFunctionReflection(String moduleName, String functionName, HLEModule hleModule, String hleModuleMethodName, Method hleModuleMethod, boolean checkInsideInterrupt) {
		super(moduleName, functionName);
		
		this.hleModule = hleModule;
		this.hleModuleClass = hleModule.getClass();
		this.hleModuleMethodName = hleModuleMethodName;
		this.checkInsideInterrupt = checkInsideInterrupt;
		this.hleModuleMethod = hleModuleMethod; 
		this.hleModuleMethodParametersTypes = this.hleModuleMethod.getParameterTypes();
		this.hleModuleMethodReturnType = this.hleModuleMethod.getReturnType();
		
		if (
			this.hleModuleMethodReturnType == void.class &&
			this.hleModuleMethodParametersTypes.length == 1 &&
			this.hleModuleMethodParametersTypes[0] == Processor.class
		) {
			fastOldInvoke = true;
		} else {
			fastOldInvoke = false;
			prepareParameterDecodingRunList();
		}
	}
	
	protected void prepareParameterDecodingRunList() {
		
	}
	
	private void setReturnValue(Processor processor, Object returnObject) {
		try {
			if (hleModuleMethodReturnType == void.class) {
				// Do nothing
			} else if (hleModuleMethodReturnType == int.class) {
				processor.parameterReader.setReturnValueInt((Integer)returnObject);
			} else if (hleModuleMethodReturnType == boolean.class) {
				processor.parameterReader.setReturnValueInt(((Boolean)returnObject) ? 1 : 0);
			} else if (hleModuleMethodReturnType == long.class) {
				processor.parameterReader.setReturnValueLong((Integer)returnObject);
			} else if (hleModuleMethodReturnType == float.class) {
				processor.parameterReader.setReturnValueFloat((Float)returnObject);
			} else {
				
				HLEUidClass hleUidClass = hleModuleMethodReturnType.getAnnotation(HLEUidClass.class);
				
				if (hleUidClass != null) {
					if (hleUidClass.moduleMethodUidGenerator().length() == 0) {
						processor.parameterReader.setReturnValueInt(
							HLEUidObjectMapping.createUidForObject(hleModuleMethodReturnType, returnObject)
						);
					} else {
						Method moduleMethodUidGenerator = hleModule.getClass().getMethod(hleUidClass.moduleMethodUidGenerator(), new Class[] { });
						int uid = (Integer)moduleMethodUidGenerator.invoke(hleModule);
						processor.parameterReader.setReturnValueInt(
							HLEUidObjectMapping.addObjectMap(hleModuleMethodReturnType, uid, returnObject)
						);
					}
				} else {
					throw(new RuntimeException("Can't handle return type '" + hleModuleMethodReturnType + "'"));
				}
			}
		} catch (Throwable o) {
			throw(new RuntimeException(o.getCause()));
		}
	}

	@Override
	public void execute(Processor processor) {
		try {

			if (checkInsideInterrupt) {
		        if (IntrManager.getInstance().isInsideInterrupt()) {
		        	throw(new SceKernelErrorException(SceKernelErrors.ERROR_KERNEL_CANNOT_BE_CALLED_FROM_INTERRUPT));
		        	//setReturnValue(processor, SceKernelErrors.ERROR_KERNEL_CANNOT_BE_CALLED_FROM_INTERRUPT); return;
		        }
			}
			
			if (getUnimplemented()) {
				Modules.getLogger(this.getModuleName()).warn(
					String.format(
						"Unimplemented NID function %s.%s [0x%08X]",
						this.getModuleName(),
						this.getFunctionName(),
						this.getNid()
					)
				);
			}
			
			try {
				processor.parameterReader.cpu = processor.cpu;
				processor.parameterReader.memory = Processor.memory;
				processor.parameterReader.resetReading();

				if (fastOldInvoke) {
					this.hleModuleMethod.invoke(hleModule, processor);
				} else {
					LinkedList<Object> params = new LinkedList<Object>();
					
					Annotation[][] paramsAnotations = this.hleModuleMethod.getParameterAnnotations();
					
					int paramIndex = 0;
					for (Class<?> paramClass : this.hleModuleMethod.getParameterTypes()) {
						Annotation[] paramAnnotations = paramsAnotations[paramIndex];
						boolean canBeNull = false;
						String methodToCheckName = null;

						for (Annotation currentAnnotation : paramAnnotations) {
							if (currentAnnotation instanceof CanBeNull) {
								canBeNull = true;
							}
							if (currentAnnotation instanceof CheckArgument) {
								methodToCheckName = ((CheckArgument)currentAnnotation).value();
							}
						}

						if (paramClass.isInstance(processor)) {
							params.add(processor);
						} else if (paramClass == int.class) {
							params.add(processor.parameterReader.getNextInt());
						} else if (paramClass == long.class) {
							params.add(processor.parameterReader.getNextLong());
						} else if (paramClass == boolean.class) {
							int value = processor.parameterReader.getNextInt();
							if (value < 0 || value > 1) {
								Logger.getRootLogger().warn(
									String.format("Parameter exepcted to be bool but had value 0x%08X", value)
								);
							}
							params.add(value != 0);
						} /*else if (paramClass.isEnum()) {
							params.add(paramClass.cast(processor.parameterReader.getNextInt()));
						}*/ else if (TPointer.class.isAssignableFrom(paramClass)) {
							TPointer pointer = new TPointer(Processor.memory, processor.parameterReader.getNextInt());
							
							if (!canBeNull && !pointer.isAddressGood()) {
								throw(new SceKernelErrorException(SceKernelErrors.ERROR_INVALID_POINTER));
							}
							params.add(pointer);
						} else if (TPointerBase.class.isAssignableFrom(paramClass)) {
							TPointerBase pointer;
							if (TPointer64.class.isAssignableFrom(paramClass)) {
								pointer = new TPointer64(Processor.memory, processor.parameterReader.getNextInt());
							} else if (TErrorPointer32.class.isAssignableFrom(paramClass)) {
								pointer = this.errorHolder = new TErrorPointer32(Processor.memory, processor.parameterReader.getNextInt());
							} else if (TPointer32.class.isAssignableFrom(paramClass)) {
								pointer = new TPointer32(Processor.memory, processor.parameterReader.getNextInt());
							} else {
								throw(new RuntimeException("Unknown TPointerBase parameter class '" + paramClass + "'"));
							}
							if (!canBeNull && !pointer.isAddressGood()) {
								throw(new SceKernelErrorException(SceKernelErrors.ERROR_INVALID_POINTER));
							}
							params.add(pointer);
						} else {
							HLEUidClass hleUidClass = paramClass.getAnnotation(HLEUidClass.class);
							if (hleUidClass != null) {
								int uid = processor.parameterReader.getNextInt();
								
								Object object = HLEUidObjectMapping.getObject(paramClass, uid);
								if (object == null) {
									throw(new SceKernelErrorException(hleUidClass.errorValueOnNotFound()));
								}
								params.add(object);
							} else {
								throw(new RuntimeException("Unknown parameter class '" + paramClass + "'"));
							}
						}
						
						if (methodToCheckName != null) {
							Method methodToCheck = null; //= hleModuleClass.getMethod(methodToCheckName, params.get(params.size() - 1).getClass());
							
							for (Method method : hleModuleClass.getMethods()) {
								if (method.getName().equals(methodToCheckName)) {
									methodToCheck = method;
									break;
								}
							}

							params.set(
								params.size() - 1,
								methodToCheck.invoke(hleModule, params.get(params.size() - 1))
							);
						}
						
						paramIndex++;
					}
		
					/*
					if (params.size() > 1) {
						System.err.println("----------- " + this.hleModuleMethodName);
						for (Object object : params) {
							System.err.println(object);
						}
					}
					*/
					
					//this.hleModuleMethod.invoke(hleModule, cpu);
					Object returnObject = this.hleModuleMethod.invoke(hleModule, params.toArray());

					if (errorHolder != null) {
						errorHolder.setValue(0);
					}

					setReturnValue(processor, returnObject);
				}
			} catch (InvocationTargetException e) {
				System.err.println(
					"Error calling "
					+ ":: hleModule='" + hleModule + "'"
					+ ":: hleModuleClass='" + hleModuleClass + "'"
					+ ":: hleModuleMethodName='" + hleModuleMethodName + "'"
					+ ":: hleModuleMethod='" + hleModuleMethod + "'"
				);
				throw(e.getCause());
				
				
			}
		} catch (SceKernelErrorException kernelError) {
			if (errorHolder != null) {
				errorHolder.setValue(kernelError.errorCode);
				setReturnValue(processor, 0);
			} else {
				setReturnValue(processor, kernelError.errorCode);
			}
		} catch (Throwable e1) {
			System.err.println("OnMethod: " + hleModuleMethod);
			e1.printStackTrace();
		}
	}

	@Override
	public String compiledString() {
		//return "processor.parameterReader.resetReading(); " + this.hleModuleClass.getName() + "." + this.hleModuleMethodName + "(processor);";
		return "";
	}

}
