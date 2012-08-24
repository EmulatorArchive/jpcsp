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
package jpcsp.HLE.modules150;

import jpcsp.HLE.HLEFunction;
import jpcsp.HLE.HLEUnimplemented;
import jpcsp.HLE.Modules;
import jpcsp.HLE.modules.HLEModule;

import org.apache.log4j.Logger;

public class sceGameUpdate extends HLEModule {
    protected static Logger log = Modules.getLogger("sceGameUpdate");

    @Override
    public String getName() {
        return "sceGameUpdate";
    }

    @HLEUnimplemented
    @HLEFunction(nid = 0xCBE69FB3, version = 150)
    public int sceGameUpdateInit() {
    	return 0;
    }

    @HLEUnimplemented
    @HLEFunction(nid = 0xBB4B68DE, version = 150)
    public int sceGameUpdateTerm() {
    	return 0;
    }

    @HLEUnimplemented
    @HLEFunction(nid = 0x596AD78C, version = 150)
    public int sceGameUpdateRun() {
    	return 0;
    }

    @HLEUnimplemented
    @HLEFunction(nid = 0x5F5D98A6, version = 150)
    public int sceGameUpdateAbort() {
    	return 0;
    }
}