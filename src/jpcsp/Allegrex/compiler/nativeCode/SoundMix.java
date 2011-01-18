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
package jpcsp.Allegrex.compiler.nativeCode;

import jpcsp.sound.Utils;

/**
 * @author gid15
 *
 */
public class SoundMix extends AbstractNativeCodeSequence {
	static public void mixStereoInMemory(int inAddrReg, int inOutAddrReg, int countReg, int leftVolumeFReg, int rightVolumeFReg) {
		int inAddr = getRegisterValue(inAddrReg);
		int inOutAddr = getRegisterValue(inOutAddrReg);
		int count = getRegisterValue(countReg);
		float inLeftVolume = getFRegisterValue(leftVolumeFReg);
		float inRightVolume = getFRegisterValue(rightVolumeFReg);

		Utils.mixStereoInMemory(inAddr, inOutAddr, count, inLeftVolume, inRightVolume);
	}

	static public void mixStereoInMemory(int inAddrReg, int inOutAddrReg, int countReg, int maxCountAddrReg, int leftVolumeFReg, int rightVolumeFReg) {
		int inAddr = getRegisterValue(inAddrReg);
		int inOutAddr = getRegisterValue(inOutAddrReg);
		int count = getRegisterValue(countReg);
		int maxCount = getMemory().read32(getRegisterValue(maxCountAddrReg));
		float inLeftVolume = getFRegisterValue(leftVolumeFReg);
		float inRightVolume = getFRegisterValue(rightVolumeFReg);

		Utils.mixStereoInMemory(inAddr, inOutAddr, maxCount - count, inLeftVolume, inRightVolume);
	}
}
