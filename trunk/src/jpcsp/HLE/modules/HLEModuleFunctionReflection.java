package jpcsp.HLE.modules;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;

import jpcsp.Processor;
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
	
	public HLEModuleFunctionReflection(String moduleName, String functionName, HLEModule hleModule, String hleModuleMethodName, boolean checkInsideInterrupt) {
		super(moduleName, functionName);
		
		this.hleModule = hleModule;
		this.hleModuleClass = hleModule.getClass();
		this.hleModuleMethodName = hleModuleMethodName;
		this.checkInsideInterrupt = checkInsideInterrupt;

		try {
			//this.hleModuleMethod = hleModuleClass.getMethod(this.hleModuleMethodName, new Class[] { Processor.class });
			Boolean found = false;
			for (Method method : hleModuleClass.getMethods()) {
				if (method.getName().equals(this.hleModuleMethodName)) {
					this.hleModuleMethod = method;
					found = true;
					break;
				}
			}

			if (!found) {
				throw(new RuntimeException("Can't find method ' + this.hleModuleMethodName + '"));
			}
			
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
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void execute(Processor processor) {
		if (checkInsideInterrupt) {
	        if (IntrManager.getInstance().isInsideInterrupt()) {
	        	processor.cpu.gpr[2] = SceKernelErrors.ERROR_KERNEL_CANNOT_BE_CALLED_FROM_INTERRUPT;
	            return;
	        }
		}
		
		try {
			processor.parameterReader.cpu = processor.cpu;
			processor.parameterReader.memory = Processor.memory;
			processor.parameterReader.resetReading();
			if (fastOldInvoke) {
				this.hleModuleMethod.invoke(hleModule, processor);
			} else {
				LinkedList<Object> params = new LinkedList<Object>();
				
				for (Class<?> paramClass : this.hleModuleMethod.getParameterTypes()) {
					if (paramClass.isInstance(processor)) {
						params.add(processor);
					} else if (paramClass == int.class) {
						params.add(processor.parameterReader.getNextInt());
					} else {
						throw(new RuntimeException("Unknown parameter class '" + paramClass + "'"));
					}
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
				
				if (hleModuleMethodReturnType == void.class) {
					// Do nothing
				} else if (hleModuleMethodReturnType == int.class) {
					processor.parameterReader.setReturnValueInt((Integer)returnObject);
				} else if (hleModuleMethodReturnType == long.class) {
					processor.parameterReader.setReturnValueLong((Integer)returnObject);
				} else {
					throw(new RuntimeException("Can't handle return type '" + hleModuleMethodReturnType + "'"));
				}
			}
		} catch (InvocationTargetException e) {
			System.err.println(
				"Error calling "
				+ ":: hleModule='" + hleModule + "'"
				+ ":: hleModuleClass='" + hleModuleClass + "'"
				+ ":: hleModuleMethodName='" + hleModuleMethodName + "'"
				+ ":: hleModuleMethod='" + hleModuleMethod + "'"
			);
			try {
				throw(e.getCause());
			} catch (Throwable e1) {
				e1.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String compiledString() {
		//return "processor.parameterReader.resetReading(); " + this.hleModuleClass.getName() + "." + this.hleModuleMethodName + "(processor);";
		return "";
	}

}
