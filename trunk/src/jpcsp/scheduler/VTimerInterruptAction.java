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

import jpcsp.HLE.Modules;
import jpcsp.HLE.kernel.managers.IntrManager;
import jpcsp.HLE.kernel.types.IAction;
import jpcsp.HLE.kernel.types.SceKernelVTimerInfo;

public class VTimerInterruptAction implements IAction {
	private SceKernelVTimerInfo sceKernelVTimerInfo;

	public VTimerInterruptAction(SceKernelVTimerInfo sceKernelVTimerInfo) {
		this.sceKernelVTimerInfo = sceKernelVTimerInfo;
	}

	@Override
	public void execute() {
		long now = Scheduler.getInstance().getNow();

		// Trigger interrupt
		if (Modules.log.isDebugEnabled()) {
			Modules.log.debug(String.format("Calling VTimer uid=%x, now=%d", sceKernelVTimerInfo.uid, now));
		}

		IntrManager.getInstance().triggerInterrupt(IntrManager.PSP_SYSTIMER0_INTR, null, sceKernelVTimerInfo.vtimerInterruptResultAction, sceKernelVTimerInfo.vtimerInterruptHandler);
	}
}
