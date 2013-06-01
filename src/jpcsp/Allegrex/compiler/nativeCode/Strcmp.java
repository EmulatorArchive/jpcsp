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

import jpcsp.memory.IMemoryReader;
import jpcsp.memory.MemoryReader;

/**
 * @author gid15
 *
 */
public class Strcmp extends AbstractNativeCodeSequence {
	static public void call() {
		call(0, -1, 1);
	}

	static public void call(int valueEqual, int valueLower, int valueHigher) {
		int src1Addr = getGprA0();
		if (src1Addr == 0) {
			setGprV0(valueLower);
			return;
		}

		int src2Addr = getGprA1();
		if (src2Addr == 0) {
			setGprV0(valueHigher);
			return;
		}

		IMemoryReader memoryReader1 = MemoryReader.getMemoryReader(src1Addr, 1);
		IMemoryReader memoryReader2 = MemoryReader.getMemoryReader(src2Addr, 1);

		if (memoryReader1 != null && memoryReader2 != null) {
			while (true) {
				int c1 = memoryReader1.readNext();
				int c2 = memoryReader2.readNext();
				if (c1 != c2) {
					setGprV0(c1 > c2 ? valueHigher : valueLower);
					return;
				} else if (c1 == 0) {
					// c1 == 0 and c2 == 0
					break;
				}
			}
		}

		setGprV0(valueEqual);
	}
}
