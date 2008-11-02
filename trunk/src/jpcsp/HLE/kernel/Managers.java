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
package jpcsp.HLE.kernel;

import jpcsp.HLE.kernel.managers.*;

/**
 *
 * @author hli
 */
public class Managers {
    public static UidManager uids;
    public static CallbackManager callbacks;
    public static SemaphoreManager sempahores;
    public static EventFlagManager eventsFlags;
    public static ThreadManager threads;
    public static FplManager fpl;
    public static ModuleManager modules;

    /** call this when resetting the emulator */
    public static void reset() {
        // TODO add other reset calls here
        fpl.reset();
        modules.reset();
    }

    static {
        uids = UidManager.singleton;
        callbacks = CallbackManager.singleton;
        sempahores = SemaphoreManager.singleton;
        eventsFlags = EventFlagManager.singleton;
        threads = ThreadManager.singleton;
        fpl = FplManager.singleton;
        modules = ModuleManager.singleton;
    }
}
