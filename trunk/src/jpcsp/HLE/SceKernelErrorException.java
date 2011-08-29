package jpcsp.HLE;

@SuppressWarnings("serial")
public class SceKernelErrorException extends RuntimeException {
	public int errorCode;

	public SceKernelErrorException(int errorCode) {
		this.errorCode = errorCode;
	}

	@Override
	public String toString() {
		return String.format("SceKernelErrorException(errorCode=0x%08X)", errorCode);
	}
}
