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
package jpcsp.HLE.kernel.types;

import jpcsp.HLE.kernel.managers.SceUidManager;

public class SceKernelEventFlagInfo extends pspAbstractMemoryMappedStructureVariableLength {
    public final String name;
    public final int attr;
    public final int initPattern;
    public int currentPattern;
    public int numWaitThreads;

    public final int uid;

    public SceKernelEventFlagInfo(String name, int attr, int initPattern, int currentPattern) {
        this.name = name;
        this.attr = attr;
        this.initPattern = initPattern;
        this.currentPattern = currentPattern;
        numWaitThreads = 0;

        uid = SceUidManager.getNewUid("ThreadMan-eventflag");
    }

	@Override
	protected void write() {
		super.write();
		writeStringNZ(32, name);
		write32(attr);
		write32(initPattern);
		write32(currentPattern);
		write32(numWaitThreads);
	}
}
