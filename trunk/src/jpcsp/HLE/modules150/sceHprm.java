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

import jpcsp.Memory;
import jpcsp.Processor;
import jpcsp.Allegrex.CpuState;
import jpcsp.HLE.Modules;
import jpcsp.HLE.modules.HLEModule;
import jpcsp.HLE.modules.HLEModuleFunction;
import jpcsp.HLE.modules.HLEModuleManager;
import jpcsp.HLE.modules.HLEStartModule;

import org.apache.log4j.Logger;

public class sceHprm implements HLEModule, HLEStartModule {
    private static Logger log = Modules.getLogger("sceHprm");

    @Override
    public String getName() { return "sceHprm"; }

    @Override
    public void installModule(HLEModuleManager mm, int version) {
        if (version >= 150) {

            mm.addFunction(0xC7154136, sceHprmRegisterCallbackFunction);
            mm.addFunction(0x444ED0B7, sceHprmUnregisterCallbackFunction);
            mm.addFunction(0x71B5FB67, sceHprmGetHpDetectFunction);
            mm.addFunction(0x208DB1BD, sceHprmIsRemoteExistFunction);
            mm.addFunction(0x7E69EDA4, sceHprmIsHeadphoneExistFunction);
            mm.addFunction(0x219C58F1, sceHprmIsMicrophoneExistFunction);
            mm.addFunction(0x1910B327, sceHprmPeekCurrentKeyFunction);
            mm.addFunction(0x2BCEC83E, sceHprmPeekLatchFunction);
            mm.addFunction(0x40D2F9F0, sceHprmReadLatchFunction);

        }
    }

    @Override
    public void uninstallModule(HLEModuleManager mm, int version) {
        if (version >= 150) {

            mm.removeFunction(sceHprmRegisterCallbackFunction);
            mm.removeFunction(sceHprmUnregisterCallbackFunction);
            mm.removeFunction(sceHprmGetHpDetectFunction);
            mm.removeFunction(sceHprmIsRemoteExistFunction);
            mm.removeFunction(sceHprmIsHeadphoneExistFunction);
            mm.removeFunction(sceHprmIsMicrophoneExistFunction);
            mm.removeFunction(sceHprmPeekCurrentKeyFunction);
            mm.removeFunction(sceHprmPeekLatchFunction);
            mm.removeFunction(sceHprmReadLatchFunction);

        }
    }
    
    @Override
    public void start() {
    	peekCurrentKeyWarningLogged = false;
    }

    @Override
    public void stop() {
    }

    private boolean enableRemote = false;
    private boolean enableHeadphone = false;
    private boolean enableMicrophone = false;

    private boolean peekCurrentKeyWarningLogged;

    public void sceHprmRegisterCallback(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("Unimplemented NID function sceHprmRegisterCallback [0xC7154136]");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHprmUnregisterCallback(Processor processor) {
        CpuState cpu = processor.cpu; 

        log.warn("Unimplemented NID function sceHprmUnregisterCallback [0x444ED0B7]");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHprmGetHpDetect(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("Unimplemented NID function sceHprmGetHpDetect [0x71B5FB67]");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHprmIsRemoteExist(Processor processor) {
        CpuState cpu = processor.cpu;

        int result = enableRemote ? 1 : 0;
        log.debug("sceHprmIsRemoteExist ret:" + result);

        cpu.gpr[2] = result;
    }

    public void sceHprmIsHeadphoneExist(Processor processor) {
        CpuState cpu = processor.cpu;

        int result = enableHeadphone ? 1 : 0;
        log.debug("sceHprmIsHeadphoneExist ret:" + result);

        cpu.gpr[2] = result;
    }

    public void sceHprmIsMicrophoneExist(Processor processor) {
        CpuState cpu = processor.cpu;

        int result = enableMicrophone ? 1 : 0;
        log.debug("sceHprmIsMicrophoneExist ret:" + result);

        cpu.gpr[2] = result;
    }

    public void sceHprmPeekCurrentKey(Processor processor) {
        CpuState cpu = processor.cpu;
        Memory mem = Processor.memory;

        int key_addr = cpu.gpr[4];

        if (Memory.isAddressGood(key_addr)) {
            if (peekCurrentKeyWarningLogged) {
                if (log.isTraceEnabled()) {
                    log.trace("IGNORING:sceHprmPeekCurrentKey(key_addr=0x" + Integer.toHexString(key_addr) + ")");
                }
            } else {
                log.warn("IGNORING:sceHprmPeekCurrentKey(key_addr=0x" + Integer.toHexString(key_addr) + ") future calls will only appear in TRACE log");
                peekCurrentKeyWarningLogged = true;
            }
            mem.write32(key_addr, 0); // fake
            cpu.gpr[2] = 0; // check
        } else {
            log.warn("sceHprmPeekCurrentKey(key_addr=0x" + Integer.toHexString(key_addr) + ") invalid address");
            cpu.gpr[2] = -1; // check
        }
    }

    public void sceHprmPeekLatch(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("Unimplemented NID function sceHprmPeekLatch [0x2BCEC83E]");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHprmReadLatch(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("Unimplemented NID function sceHprmReadLatch [0x40D2F9F0]");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public final HLEModuleFunction sceHprmRegisterCallbackFunction = new HLEModuleFunction("sceHprm", "sceHprmRegisterCallback") {
        @Override
        public final void execute(Processor processor) {
            sceHprmRegisterCallback(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHprmModule.sceHprmRegisterCallback(processor);";
        }
    };

    public final HLEModuleFunction sceHprmUnregisterCallbackFunction = new HLEModuleFunction("sceHprm", "sceHprmUnregisterCallback") {
        @Override
        public final void execute(Processor processor) {
            sceHprmUnregisterCallback(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHprmModule.sceHprmUnregisterCallback(processor);";
        }
    };

    public final HLEModuleFunction sceHprmGetHpDetectFunction = new HLEModuleFunction("sceHprm", "sceHprmGetHpDetect") {
        @Override
        public final void execute(Processor processor) {
            sceHprmGetHpDetect(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHprmModule.sceHprmGetHpDetect(processor);";
        }
    };

    public final HLEModuleFunction sceHprmIsRemoteExistFunction = new HLEModuleFunction("sceHprm", "sceHprmIsRemoteExist") {
        @Override
        public final void execute(Processor processor) {
            sceHprmIsRemoteExist(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHprmModule.sceHprmIsRemoteExist(processor);";
        }
    };

    public final HLEModuleFunction sceHprmIsHeadphoneExistFunction = new HLEModuleFunction("sceHprm", "sceHprmIsHeadphoneExist") {
        @Override
        public final void execute(Processor processor) {
            sceHprmIsHeadphoneExist(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHprmModule.sceHprmIsHeadphoneExist(processor);";
        }
    };

    public final HLEModuleFunction sceHprmIsMicrophoneExistFunction = new HLEModuleFunction("sceHprm", "sceHprmIsMicrophoneExist") {
        @Override
        public final void execute(Processor processor) {
            sceHprmIsMicrophoneExist(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHprmModule.sceHprmIsMicrophoneExist(processor);";
        }
    };

    public final HLEModuleFunction sceHprmPeekCurrentKeyFunction = new HLEModuleFunction("sceHprm", "sceHprmPeekCurrentKey") {
        @Override
        public final void execute(Processor processor) {
            sceHprmPeekCurrentKey(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHprmModule.sceHprmPeekCurrentKey(processor);";
        }
    };

    public final HLEModuleFunction sceHprmPeekLatchFunction = new HLEModuleFunction("sceHprm", "sceHprmPeekLatch") {
        @Override
        public final void execute(Processor processor) {
            sceHprmPeekLatch(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHprmModule.sceHprmPeekLatch(processor);";
        }
    };

    public final HLEModuleFunction sceHprmReadLatchFunction = new HLEModuleFunction("sceHprm", "sceHprmReadLatch") {
        @Override
        public final void execute(Processor processor) {
            sceHprmReadLatch(processor);
        }
        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHprmModule.sceHprmReadLatch(processor);";
        }
    };
}