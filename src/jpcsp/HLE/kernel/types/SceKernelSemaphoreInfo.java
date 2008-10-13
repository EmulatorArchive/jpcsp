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

import jpcsp.HLE.Modules;
import jpcsp.HLE.kernel.Managers;

import jpcsp.Processor;
import jpcsp.Allegrex.CpuState;

/**
 *
 * @author hli
 */
public class SceKernelSemaphoreInfo extends SceKernelUid {

    public int initCount;
    public int currentCount;
    public int maxCount;

    public SceKernelSemaphoreInfo(String name, int attr, int initCount, int currentCount, int maxCount) {
        super(name, attr);
        if (-1 < this.uid) {
            this.initCount = initCount;
            this.currentCount = currentCount;
            this.maxCount = maxCount;
        }
    }
    
    public void release() {
        Managers.sempahores.releaseObject(this);
    }    

}
