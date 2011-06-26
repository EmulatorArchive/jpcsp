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

import org.apache.log4j.Logger;

import jpcsp.Processor;
import jpcsp.Allegrex.CpuState;
import jpcsp.HLE.Modules;
import jpcsp.HLE.modules.HLEModule;
import jpcsp.HLE.modules.HLEModuleFunction;
import jpcsp.HLE.modules.HLEModuleManager;
import jpcsp.util.Utilities;

public class sceUsb implements HLEModule {
    protected static Logger log = Modules.getLogger("sceUsb");

	@Override
	public String getName() {
		return "sceUsb";
	}

	@Override
	public void installModule(HLEModuleManager mm, int version) {
		if (version >= 150) {
			mm.addFunction(sceUsbStartFunction, 0xAE5DE6AF);
			mm.addFunction(sceUsbStopFunction, 0xC2464FA0);
			mm.addFunction(sceUsbGetStateFunction, 0xC21645A4);
			mm.addFunction(sceUsbGetDrvListFunction, 0x4E537366);
			mm.addFunction(sceUsbGetDrvStateFunction, 0x112CC951);
			mm.addFunction(sceUsbActivateFunction, 0x586DB82C);
			mm.addFunction(sceUsbDeactivateFunction, 0xC572A9C8);
			mm.addFunction(sceUsbWaitStateFunction, 0x5BE0E002);
			mm.addFunction(sceUsbWaitCancelFunction, 0x1C360735);

			usbActivated = false;
			usbStarted = false;
		}
	}

	@Override
	public void uninstallModule(HLEModuleManager mm, int version) {
		if (version >= 150) {
			mm.removeFunction(sceUsbStartFunction);
			mm.removeFunction(sceUsbStopFunction);
			mm.removeFunction(sceUsbGetStateFunction);
			mm.removeFunction(sceUsbGetDrvListFunction);
			mm.removeFunction(sceUsbGetDrvStateFunction);
			mm.removeFunction(sceUsbActivateFunction);
			mm.removeFunction(sceUsbDeactivateFunction);
			mm.removeFunction(sceUsbWaitStateFunction);
			mm.removeFunction(sceUsbWaitCancelFunction);
		}
	}

	public static final String PSP_USBBUS_DRIVERNAME = "USBBusDriver";

	public static final int PSP_USB_CONNECTION_ESTABLISHED = 0x002;
	public static final int PSP_USB_CABLE_CONNECTED = 0x020;
	public static final int PSP_USB_ACTIVATED = 0x200;

	protected boolean usbActivated;
	protected boolean usbStarted;

	protected int getUsbState() {
		// Simulate that a USB cacle is always connected
		int state = PSP_USB_CABLE_CONNECTED;

		// USB has been activated?
		if (usbActivated) {
			state |= PSP_USB_ACTIVATED;
		}

		// USB has been started?
		if (usbStarted) {
			state |= PSP_USB_CONNECTION_ESTABLISHED;
		}

		return state;
	}

	/**
	  * Start a USB driver.
	  *
	  * @param driverName - name of the USB driver to start
	  * @param size - Size of arguments to pass to USB driver start
	  * @param args - Arguments to pass to USB driver start
	  *
	  * @return 0 on success
	  */
	public void sceUsbStart(Processor processor) {
		CpuState cpu = processor.cpu;

		int driverNameAddr = cpu.gpr[4];
		int size = cpu.gpr[5];
		int args = cpu.gpr[6];

		String driverName = Utilities.readStringZ(driverNameAddr);

		log.warn(String.format("Unimplemented sceUsbStart driverName=0x%08X('%s'), size=%d, args=0x%08X", driverNameAddr, driverName, size, args));

		usbStarted = true;

		cpu.gpr[2] = 0;
	}

	/**
	  * Stop a USB driver.
	  *
	  * @param driverName - name of the USB driver to stop
	  * @param size - Size of arguments to pass to USB driver start
	  * @param args - Arguments to pass to USB driver start
	  *
	  * @return 0 on success
	  */
	public void sceUsbStop(Processor processor) {
		CpuState cpu = processor.cpu;

		int driverNameAddr = cpu.gpr[4];
		int size = cpu.gpr[5];
		int args = cpu.gpr[6];

		String driverName = Utilities.readStringZ(driverNameAddr);

		log.warn(String.format("Unimplemented sceUsbStop driverName=0x%08X('%s'), size=%d, args=0x%08X", driverNameAddr, driverName, size, args));

		usbStarted = false;

		cpu.gpr[2] = 0;
	}

	/**
	  * Get USB state
	  *
	  * @return OR'd PSP_USB_* constants
	  */
	public void sceUsbGetState(Processor processor) {
		CpuState cpu = processor.cpu;

		if (log.isDebugEnabled()) {
			log.debug(String.format("sceUsbGetState returning %d", getUsbState()));
		}

		cpu.gpr[2] = getUsbState();
	}

	public void sceUsbGetDrvList(Processor processor) {
		CpuState cpu = processor.cpu;

		log.warn(String.format("Unimplemented sceUsbGetDrvList"));

		cpu.gpr[2] = 0;
	}

	/**
	  * Get state of a specific USB driver
	  *
	  * @param driverName - name of USB driver to get status from
	  *
	  * @return 1 if the driver has been started, 2 if it is stopped
	  */
	public void sceUsbGetDrvState(Processor processor) {
		CpuState cpu = processor.cpu;

		int driverNameAddr = cpu.gpr[4];

		String driverName = Utilities.readStringZ(driverNameAddr);

		log.warn(String.format("Unimplemented sceUsbGetDrvState driverName=0x%08X('%s')", driverNameAddr, driverName));

		cpu.gpr[2] = 0;
	}

	/**
	  * Activate a USB driver.
	  *
	  * @param pid - Product ID for the default USB Driver
	  *
	  * @return 0 on success
	  */
	public void sceUsbActivate(Processor processor) {
		CpuState cpu = processor.cpu;

		int pid = cpu.gpr[4];

		log.warn(String.format("Unimplemented sceUsbActivate pid=0x%X", pid));

		usbActivated = true;

		cpu.gpr[2] = 0;
	}

	/**
	  * Deactivate USB driver.
	  *
	  * @param pid - Product ID for the default USB driver
	  *
	  * @return 0 on success
	  */
	public void sceUsbDeactivate(Processor processor) {
		CpuState cpu = processor.cpu;

		int pid = cpu.gpr[4];

		log.warn(String.format("Unimplemented sceUsbDeactivate pid=0x%08X", pid));

		usbActivated = false;

		cpu.gpr[2] = 0;
	}

	public void sceUsbWaitState(Processor processor) {
		CpuState cpu = processor.cpu;

		log.warn(String.format("Unimplemented sceUsbWaitState"));

		cpu.gpr[2] = 0;
	}

	public void sceUsbWaitCancel(Processor processor) {
		CpuState cpu = processor.cpu;

		log.warn(String.format("Unimplemented sceUsbWaitCancel"));

		cpu.gpr[2] = 0;
	}

	public final HLEModuleFunction sceUsbStartFunction = new HLEModuleFunction("sceUsb", "sceUsbStart") {
		@Override
		public final void execute(Processor processor) {
			sceUsbStart(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceUsbModule.sceUsbStart(processor);";
		}
	};

	public final HLEModuleFunction sceUsbStopFunction = new HLEModuleFunction("sceUsb", "sceUsbStop") {
		@Override
		public final void execute(Processor processor) {
			sceUsbStop(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceUsbModule.sceUsbStop(processor);";
		}
	};

	public final HLEModuleFunction sceUsbGetStateFunction = new HLEModuleFunction("sceUsb", "sceUsbGetState") {
		@Override
		public final void execute(Processor processor) {
			sceUsbGetState(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceUsbModule.sceUsbGetState(processor);";
		}
	};

	public final HLEModuleFunction sceUsbGetDrvListFunction = new HLEModuleFunction("sceUsb", "sceUsbGetDrvList") {
		@Override
		public final void execute(Processor processor) {
			sceUsbGetDrvList(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceUsbModule.sceUsbGetDrvList(processor);";
		}
	};

	public final HLEModuleFunction sceUsbGetDrvStateFunction = new HLEModuleFunction("sceUsb", "sceUsbGetDrvState") {
		@Override
		public final void execute(Processor processor) {
			sceUsbGetDrvState(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceUsbModule.sceUsbGetDrvState(processor);";
		}
	};

	public final HLEModuleFunction sceUsbActivateFunction = new HLEModuleFunction("sceUsb", "sceUsbActivate") {
		@Override
		public final void execute(Processor processor) {
			sceUsbActivate(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceUsbModule.sceUsbActivate(processor);";
		}
	};

	public final HLEModuleFunction sceUsbDeactivateFunction = new HLEModuleFunction("sceUsb", "sceUsbDeactivate") {
		@Override
		public final void execute(Processor processor) {
			sceUsbDeactivate(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceUsbModule.sceUsbDeactivate(processor);";
		}
	};

	public final HLEModuleFunction sceUsbWaitStateFunction = new HLEModuleFunction("sceUsb", "sceUsbWaitState") {
		@Override
		public final void execute(Processor processor) {
			sceUsbWaitState(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceUsbModule.sceUsbWaitState(processor);";
		}
	};

	public final HLEModuleFunction sceUsbWaitCancelFunction = new HLEModuleFunction("sceUsb", "sceUsbWaitCancel") {
		@Override
		public final void execute(Processor processor) {
			sceUsbWaitCancel(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceUsbModule.sceUsbWaitCancel(processor);";
		}
	};
}
