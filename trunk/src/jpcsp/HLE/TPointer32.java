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
package jpcsp.HLE;

import jpcsp.Memory;

public class TPointer32 extends TPointerBase {
	public TPointer32(Memory memory, int address) {
		super(memory, address, false);
	}

	public TPointer32(Memory memory, int address, boolean canBeNull) {
		super(memory, address, canBeNull);
	}

	public int getValue() {
		return pointer.getValue32();
	}

	public void setValue(int value) {
		if (canSetValue()) {
			pointer.setValue32(value);
		}
	}

	public int getValue(int offset) {
		return pointer.getValue32(offset);
	}

	public void setValue(int offset, int value) {
		if (canSetValue()) {
			pointer.setValue32(offset, value);
		}
	}
}
