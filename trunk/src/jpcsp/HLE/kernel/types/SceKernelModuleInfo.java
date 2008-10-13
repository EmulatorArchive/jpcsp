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

import jpcsp.HLE.kernel.Managers;

/**
 *
 * @author hli
 */
public class SceKernelModuleInfo extends SceKernelUid {
    
    public int start_addr;
    
    public static final int flashModuleUid = 0;

    public SceKernelModuleInfo(String name, int start_addr, int attr) {
        super(name, attr);
        if (-1 < this.uid) {
            this.start_addr = start_addr;
        }
    }

    public int getStartAddr() {
        return start_addr;
    }

    public void release() {
    }        
}
