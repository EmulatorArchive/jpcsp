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
package jpcsp.scheduler;

import jpcsp.Emulator;
import static jpcsp.HLE.modules150.ThreadManForUser.log;
import jpcsp.HLE.Modules;
import jpcsp.HLE.kernel.types.IAction;
import jpcsp.HLE.kernel.types.SceKernelVTimerInfo;
import jpcsp.HLE.modules.ThreadManForUser;

public class VTimerInterruptResultAction implements IAction {
	private SceKernelVTimerInfo sceKernelVTimerInfo;

	public VTimerInterruptResultAction(SceKernelVTimerInfo sceKernelVTimerInfo) {
		this.sceKernelVTimerInfo = sceKernelVTimerInfo;
	}

	@Override
	public void execute() {
		ThreadManForUser timerManager = Modules.ThreadManForUserModule;

		int vtimerInterruptResult = Emulator.getProcessor().cpu._v0;
		if (log.isDebugEnabled()) {
			log.debug("VTimer returned value " + vtimerInterruptResult);
		}

		if (vtimerInterruptResult == 0) {
			// VTimer is canceled
			timerManager.cancelVTimer(sceKernelVTimerInfo);
		} else {
			timerManager.rescheduleVTimer(sceKernelVTimerInfo, vtimerInterruptResult);
		}
	}

}
