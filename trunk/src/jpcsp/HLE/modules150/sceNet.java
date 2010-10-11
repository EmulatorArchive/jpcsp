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
import jpcsp.HLE.kernel.managers.IntrManager;
import jpcsp.HLE.kernel.types.SceKernelErrors;
import jpcsp.HLE.Modules;
import jpcsp.HLE.modules.HLEModule;
import jpcsp.HLE.modules.HLEModuleFunction;
import jpcsp.HLE.modules.HLEModuleManager;
import jpcsp.hardware.Wlan;
import jpcsp.memory.IMemoryReader;
import jpcsp.memory.IMemoryWriter;
import jpcsp.memory.MemoryReader;
import jpcsp.memory.MemoryWriter;
import jpcsp.util.Utilities;

import org.apache.log4j.Logger;

public class sceNet implements HLEModule {

    protected static Logger log = Modules.getLogger("sceNet");

    @Override
    public String getName() {
        return "sceNet";
    }

    @Override
    public void installModule(HLEModuleManager mm, int version) {
        if (version >= 150) {

            mm.addFunction(0x39AF39A6, sceNetInitFunction);
            mm.addFunction(0x281928A9, sceNetTermFunction);
            mm.addFunction(0x50647530, sceNetFreeThreadinfoFunction);
            mm.addFunction(0xAD6844c6, sceNetThreadAbortFunction);
            mm.addFunction(0x89360950, sceNetEtherNtostrFunction);
            mm.addFunction(0xD27961C9, sceNetEtherStrtonFunction);
            mm.addFunction(0xF5805EFE, sceNetHtonlFunction);
            mm.addFunction(0x39C1BF02, sceNetHtonsFunction);
            mm.addFunction(0x93C4AF7E, sceNetNtohlFunction);
            mm.addFunction(0x4CE03207, sceNetNtohsFunction);
            mm.addFunction(0x0BF0A3AE, sceNetGetLocalEtherAddrFunction);
            mm.addFunction(0xCC393E48, sceNetGetMallocStatFunction);

        }
    }

    @Override
    public void uninstallModule(HLEModuleManager mm, int version) {
        if (version >= 150) {

            mm.removeFunction(sceNetInitFunction);
            mm.removeFunction(sceNetTermFunction);
            mm.removeFunction(sceNetFreeThreadinfoFunction);
            mm.removeFunction(sceNetThreadAbortFunction);
            mm.removeFunction(sceNetEtherNtostrFunction);
            mm.removeFunction(sceNetEtherStrtonFunction);
            mm.removeFunction(sceNetHtonlFunction);
            mm.removeFunction(sceNetHtonsFunction);
            mm.removeFunction(sceNetNtohlFunction);
            mm.removeFunction(sceNetNtohsFunction);
            mm.removeFunction(sceNetGetLocalEtherAddrFunction);
            mm.removeFunction(sceNetGetMallocStatFunction);

        }
    }

    protected int netMemSize;

    public void sceNetInit(Processor processor) {
        CpuState cpu = processor.cpu;

        int poolSize = cpu.gpr[4];
        int calloutThreadPri = cpu.gpr[5];
        int calloutThreadStack = cpu.gpr[6];
        int netinitThreadPri = cpu.gpr[7];
        int netinitThreadStack = cpu.gpr[8];

        log.warn("IGNORING: sceNetInit (poolsize=0x" + Integer.toHexString(poolSize) + ", calloutThreadPri=0x" + Integer.toHexString(calloutThreadPri)
                + ", calloutThreadStack=0x" + Integer.toHexString(calloutThreadStack) + ", netinitThreadPri=0x" + Integer.toHexString(netinitThreadPri)
                + ", netinitThreadStack=0x" + Integer.toHexString(netinitThreadStack) + ")");

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        netMemSize = poolSize;
        cpu.gpr[2] = 0;
    }

    public void sceNetTerm(Processor processor) {
        CpuState cpu = processor.cpu;
        
        log.warn("IGNORING: sceNetTerm");
        
        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        cpu.gpr[2] = 0;
    }

    public void sceNetFreeThreadinfo(Processor processor) {
        CpuState cpu = processor.cpu;
        
        int thID = cpu.gpr[4];

        log.warn("IGNORING: sceNetFreeThreadinfo (thID=0x" + Integer.toHexString(thID) + ")");

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        cpu.gpr[2] = 0;
    }

    public void sceNetThreadAbort(Processor processor) {
        CpuState cpu = processor.cpu;

        int thID = cpu.gpr[4];

        log.warn("IGNORING: sceNetThreadAbort (thID=0x" + Integer.toHexString(thID) + ")");

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        cpu.gpr[2] = 0;
    }

    public void sceNetEtherNtostr(Processor processor) {
        CpuState cpu = processor.cpu;
        Memory mem = Processor.memory;
        
        int etherAddr = cpu.gpr[4];
        int strAddr = cpu.gpr[5];

        if (log.isDebugEnabled()) {
            log.debug("sceNetEtherNtostr (etherAddr=0x" + Integer.toHexString(etherAddr)
                    + ", strAddr=0x" + Integer.toHexString(strAddr) + ")");
        }
   
        if (mem.isAddressGood(etherAddr) && mem.isAddressGood(strAddr)) {
            // Convert 6-byte Mac address into string representation (XX:XX:XX:XX:XX:XX).
            StringBuilder str = new StringBuilder();
            IMemoryReader memoryReader = MemoryReader.getMemoryReader(etherAddr, Wlan.MAC_ADDRESS_LENGTH, 1);
            for (int i = 0; i < Wlan.MAC_ADDRESS_LENGTH; i++) {
                if (i > 0) {
                	str.append(":");
                }
                str.append(String.format("%02x", memoryReader.readNext()));
            }
            Utilities.writeStringZ(mem, strAddr, str.toString());
            cpu.gpr[2] = 0;
        } else {
        	cpu.gpr[2] = -1;
        }
    }

    protected int parseHexDigit(char c) {
    	if (c >= '0' && c <= '9') {
    		return c - '0';
    	} else if (c >= 'A' && c <= 'F') {
    		return c - 'A' + 10;
    	} else if (c >= 'a' && c <= 'f') {
    		return c - 'a' + 10;
    	} else {
    		log.error(String.format("Cannot parse hex digit '%c'", c));
    		return 0;
    	}
    }

    public void sceNetEtherStrton(Processor processor) {
        CpuState cpu = processor.cpu;
        Memory mem = Processor.memory;
        
        int strAddr = cpu.gpr[4];
        int etherAddr = cpu.gpr[5];

        if (log.isDebugEnabled()) {
            log.debug("sceNetEtherStrton (strAddr=0x" + Integer.toHexString(strAddr)
                    + ", etherAddr=0x" + Integer.toHexString(etherAddr) + ")");
        }

        if (mem.isAddressGood(strAddr) && mem.isAddressGood(etherAddr)) {
            // Convert string Mac address string representation (XX:XX:XX:XX:XX:XX)
        	// into 6-byte representation.
        	IMemoryReader memoryReader = MemoryReader.getMemoryReader(strAddr, 17, 1);
        	IMemoryWriter memoryWriter = MemoryWriter.getMemoryWriter(etherAddr, Wlan.MAC_ADDRESS_LENGTH, 1);
            for (int i = 0; i < Wlan.MAC_ADDRESS_LENGTH; i++) {
            	int n1 = parseHexDigit((char) memoryReader.readNext());
            	int n2 = parseHexDigit((char) memoryReader.readNext());
            	memoryReader.skip(1); // skip ':'
            	memoryWriter.writeNext((n1 << 4) + n2);
            }
            memoryWriter.flush();
            cpu.gpr[2] = 0;
        } else {
        	cpu.gpr[2] = -1;
        }
    }

    protected int networkSwap32(int value) {
    	return Integer.reverseBytes(value);
    }

    protected int networkSwap16(int value) {
    	return Integer.reverseBytes(value) >>> 16;
    }

    public void sceNetHtonl(Processor processor) {
        CpuState cpu = processor.cpu;

        int host32 = cpu.gpr[4];

        if (log.isDebugEnabled()) {
        	log.debug(String.format("sceNetHtonl(%08X)", host32));
        }

        // Convert host 32-bits to network 32-bits
        cpu.gpr[2] = networkSwap32(host32);
    }

    public void sceNetHtons(Processor processor) {
        CpuState cpu = processor.cpu;

        int host16 = cpu.gpr[4];

        if (log.isDebugEnabled()) {
        	log.debug(String.format("sceNetHtons(%04X)", host16));
        }

        // Convert host 16-bits to network 16-bits
        cpu.gpr[2] = networkSwap16(host16);
    }

    public void sceNetNtohl(Processor processor) {
        CpuState cpu = processor.cpu;

        int net32 = cpu.gpr[4];

        if (log.isDebugEnabled()) {
        	log.debug(String.format("sceNetNtohl(%08X)", net32));
        }

        // Convert network 32-bits to host 32-bits
        cpu.gpr[2] = networkSwap32(net32);
    }

    public void sceNetNtohs(Processor processor) {
        CpuState cpu = processor.cpu;

        int net16 = cpu.gpr[4];

        if (log.isDebugEnabled()) {
        	log.debug(String.format("sceNetNtohs(%04X)", net16));
        }

        // Convert network 16-bits to host 16-bits
        cpu.gpr[2] = networkSwap16(net16);
    }

    public void sceNetGetLocalEtherAddr(Processor processor) {
        CpuState cpu = processor.cpu;
        Memory mem = Processor.memory;

        int etherAddr = cpu.gpr[4];

        if (log.isDebugEnabled()) {
            log.debug("sceNetGetLocalEtherAddr (etherAddr=0x" + Integer.toHexString(etherAddr) + ")");
        }

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }

        if (mem.isAddressGood(etherAddr)) {
            // Return WLAN MAC address
        	byte[] netAddr = Wlan.getMacAddress();
            for (int i = 0; i < netAddr.length; i++) {
                mem.write8(etherAddr + i, netAddr[i]);
            }
            cpu.gpr[2] = 0;
        } else {
        	cpu.gpr[2] = -1;
        }
    }

    public void sceNetGetMallocStat(Processor processor) {
        CpuState cpu = processor.cpu;
        Memory mem = Processor.memory;

        int statAddr = cpu.gpr[4];
        
        log.warn("PARTIAL: sceNetGetMallocStat (statAddr=0x" + Integer.toHexString(statAddr) + ")");

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }     
        if(mem.isAddressGood(statAddr)) {
            // Faking. Assume no free size.
            mem.write32(statAddr, netMemSize);      // Poolsize from sceNetInit.
            mem.write32(statAddr + 4, netMemSize);  // Currently in use size.
            mem.write32(statAddr + 8, 0);           // Free size.
        }
        cpu.gpr[2] = 0;
    }

    public final HLEModuleFunction sceNetInitFunction = new HLEModuleFunction("sceNet", "sceNetInit") {

        @Override
        public final void execute(Processor processor) {
            sceNetInit(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetModule.sceNetInit(processor);";
        }
    };

    public final HLEModuleFunction sceNetTermFunction = new HLEModuleFunction("sceNet", "sceNetTerm") {

        @Override
        public final void execute(Processor processor) {
            sceNetTerm(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetModule.sceNetTerm(processor);";
        }
    };

    public final HLEModuleFunction sceNetFreeThreadinfoFunction = new HLEModuleFunction("sceNet", "sceNetFreeThreadinfo") {

        @Override
        public final void execute(Processor processor) {
            sceNetFreeThreadinfo(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetModule.sceNetFreeThreadinfo(processor);";
        }
    };

    public final HLEModuleFunction sceNetThreadAbortFunction = new HLEModuleFunction("sceNet", "sceNetThreadAbort") {

        @Override
        public final void execute(Processor processor) {
            sceNetThreadAbort(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetModule.sceNetThreadAbort(processor);";
        }
    };

    public final HLEModuleFunction sceNetEtherNtostrFunction = new HLEModuleFunction("sceNet", "sceNetEtherNtostr") {

        @Override
        public final void execute(Processor processor) {
            sceNetEtherNtostr(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetModule.sceNetEtherNtostr(processor);";
        }
    };

    public final HLEModuleFunction sceNetEtherStrtonFunction = new HLEModuleFunction("sceNet", "sceNetEtherStrton") {

        @Override
        public final void execute(Processor processor) {
            sceNetEtherStrton(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetModule.sceNetEtherStrton(processor);";
        }
    };

    public final HLEModuleFunction sceNetHtonlFunction = new HLEModuleFunction("sceNet", "sceNetHtonl") {

        @Override
        public final void execute(Processor processor) {
            sceNetHtonl(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetModule.sceNetHtonl(processor);";
        }
    };

    public final HLEModuleFunction sceNetHtonsFunction = new HLEModuleFunction("sceNet", "sceNetHtons") {

        @Override
        public final void execute(Processor processor) {
            sceNetHtons(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetModule.sceNetHtons(processor);";
        }
    };

    public final HLEModuleFunction sceNetNtohlFunction = new HLEModuleFunction("sceNet", "sceNetNtohl") {

        @Override
        public final void execute(Processor processor) {
            sceNetNtohl(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetModule.sceNetNtohl(processor);";
        }
    };

    public final HLEModuleFunction sceNetNtohsFunction = new HLEModuleFunction("sceNet", "sceNetNtohs") {

        @Override
        public final void execute(Processor processor) {
            sceNetNtohs(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetModule.sceNetNtohs(processor);";
        }
    };

    public final HLEModuleFunction sceNetGetLocalEtherAddrFunction = new HLEModuleFunction("sceNet", "sceNetGetLocalEtherAddr") {

        @Override
        public final void execute(Processor processor) {
            sceNetGetLocalEtherAddr(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetModule.sceNetGetLocalEtherAddr(processor);";
        }
    };

    public final HLEModuleFunction sceNetGetMallocStatFunction = new HLEModuleFunction("sceNet", "sceNetGetMallocStat") {

        @Override
        public final void execute(Processor processor) {
            sceNetGetMallocStat(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetModule.sceNetGetMallocStat(processor);";
        }
    };
}