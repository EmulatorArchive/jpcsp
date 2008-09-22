/* This autogenerated file is part of jpcsp. */
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

import jpcsp.HLE.modules.HLEModuleManager;
import jpcsp.HLE.modules.HLEModule;
import jpcsp.HLE.modules.HLEModuleFunction;
import jpcsp.Memory;
import jpcsp.Processor;

public class Sample implements HLEModule {

    // Regular code
    protected int someContext;

    // This could be some common code used by more than one exported function,
    // or it could just be to make code tidier.
    // Example: native implementation of stat used by both sceIoGetstat and sceIoDread.
    // Example: thread scheduling functions in ThreadMan like nextThread and contextSwitch.
    protected void someHelperFunction() {
        System.out.println("abc");
    }

    // Example: yield/block/unblock in ThreadMan.
    // Example: setDirty in pspdisplay.
    public void someModuleHelperFunction() {
        System.out.println("def");
    }
    // This is an export function, it can optionally use context saved between calls.
    // Example: sceKernelCreateThread adds a ThreadInfo object to a List,
    // then sceKernelStartThread gets that object out of the list and modifies it.
    // Example: sceKernelStdin, this just returns a value and doesn't do any processing.
    public void pspSampleFoo(Processor processor) {
        // CpuState cpu = processor.cpu; // New-Style Processor
        Processor cpu = processor; // Old-Style Processor
        int param = cpu.gpr[4];

        System.out.println("pspSampleFoo 150 context = " + someContext);
        someContext++;

        cpu.gpr[2] = 0;
    }

    // Not sure what style to use, this version with parameters and return value
    // or above version where the registers are manipulated directly. I prefer
    // the above version since it is more flexible (such as 64-bit params/return,
    // or adding/ignoring parameters).
    public int pspSampleFoo(int param) {
        System.out.println("pspSampleFoo 150 context = " + someContext);
        someContext++;

        return 0;
    }
    // Dynamic module loading/firmware handling junk

    // Only root class can have this, or we get rid of "final" keyword
    @Override
    public final String getName() {
        return "Sample";
    }

    @Override
    public void installModule(HLEModuleManager mm, int version) {

        // The NID may not change between firmware versions, but here we can do it.
        mm.addFunction(pspSampleFooFunction, 0x15151515);
    }

    @Override
    public void uninstallModule(HLEModuleManager mm, int version) {

        mm.removeFunction(pspSampleFooFunction);
    }
    public final HLEModuleFunction pspSampleFooFunction = new HLEModuleFunction("Sample", "pspSampleFoo") {

        @Override
        public final void execute(Processor processor) {
            // We need to decide on the call style
            // I prefer the first version (fiveofhearts)
            pspSampleFoo(processor);
        //processor.gpr[2] = pspSampleFoo(processor.gpr[4]);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.modules150.Samplemodule.pspSampleFoo(processor);";
        }
    };
}
