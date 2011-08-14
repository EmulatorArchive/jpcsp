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
package jpcsp.HLE.modules600;

import jpcsp.HLE.HLEFunction;
import jpcsp.Processor;
import jpcsp.Allegrex.CpuState;
import jpcsp.HLE.modules.HLEModuleFunction;
import jpcsp.HLE.modules.HLEModuleManager;

public class sceMpeg extends jpcsp.HLE.modules352.sceMpeg {

    @Override
    public void installModule(HLEModuleManager mm, int version) { mm.installModuleWithAnnotations(this, version); }

    @Override
    public void uninstallModule(HLEModuleManager mm, int version) { mm.uninstallModuleWithAnnotations(this, version); }

    public void sceMpegAvcResourceGetAvcDecTopAddr(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceMpegAvcResourceGetAvcDecTopAddr");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceMpegAvcResourceFinish(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("IGNORING: sceMpegAvcResourceFinish");

        cpu.gpr[2] = 0;
    }

    public void sceMpegAvcResourceGetAvcEsBuf(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceMpegAvcResourceGetAvcEsBuf");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceMpegAvcResourceInit(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("IGNORING: sceMpegAvcResourceInit");

        cpu.gpr[2] = 0;
    }
    @HLEFunction(nid = 0x63B9536A, version = 600)
    public final HLEModuleFunction sceMpegAvcResourceGetAvcDecTopAddrFunction = new HLEModuleFunction("sceMpeg", "sceMpegAvcResourceGetAvcDecTopAddr") {

        @Override
        public final void execute(Processor processor) {
            sceMpegAvcResourceGetAvcDecTopAddr(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceMpegModule.sceMpegAvcResourceGetAvcDecTopAddr(processor);";
        }
    };
    @HLEFunction(nid = 0x8160A2FE, version = 600)
    public final HLEModuleFunction sceMpegAvcResourceFinishFunction = new HLEModuleFunction("sceMpeg", "sceMpegAvcResourceFinish") {

        @Override
        public final void execute(Processor processor) {
            sceMpegAvcResourceFinish(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceMpegModule.sceMpegAvcResourceFinish(processor);";
        }
    };
    @HLEFunction(nid = 0xAF26BB01, version = 600)
    public final HLEModuleFunction sceMpegAvcResourceGetAvcEsBufFunction = new HLEModuleFunction("sceMpeg", "sceMpegAvcResourceGetAvcEsBuf") {

        @Override
        public final void execute(Processor processor) {
            sceMpegAvcResourceGetAvcEsBuf(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceMpegModule.sceMpegAvcResourceGetAvcEsBuf(processor);";
        }
    };
    @HLEFunction(nid = 0xFCBDB5AD, version = 600)
    public final HLEModuleFunction sceMpegAvcResourceInitFunction = new HLEModuleFunction("sceMpeg", "sceMpegAvcResourceInit") {

        @Override
        public final void execute(Processor processor) {
            sceMpegAvcResourceInit(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceMpegModule.sceMpegAvcResourceInit(processor);";
        }
    };
}