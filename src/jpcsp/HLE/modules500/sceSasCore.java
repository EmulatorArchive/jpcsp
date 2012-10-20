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
package jpcsp.HLE.modules500;

import jpcsp.HLE.HLEFunction;
import jpcsp.HLE.SceKernelErrorException;
import jpcsp.HLE.kernel.types.SceKernelErrors;

public class sceSasCore extends jpcsp.HLE.modules150.sceSasCore {

    /** Identical to __sceSasSetVoice, but for raw PCM data (VAG/ADPCM is not allowed). */
    @HLEFunction(nid = 0xE1CD9561, version = 500, checkInsideInterrupt = true)
    public int __sceSasSetVoicePCM(int sasCore, int voice, int pcmAddr, int size, int loopmode) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("__sceSasSetVoicePCM sasCore=0x%08X, voice=%d, pcmAddr=0x%08X, size=0x%08X, loopmode=%d", sasCore, voice, pcmAddr, size, loopmode));
        }

        if (size <= 0 || (size & 0xF) != 0) {
        	log.warn(String.format("__sceSasSetVoicePCM invalid size 0x%08X", size));
        	throw(new SceKernelErrorException(SceKernelErrors.ERROR_SAS_INVALID_PARAMETER));
        }

        checkSasAndVoiceHandlesGood(sasCore, voice);

        voices[voice].setPCM(pcmAddr, size);
        voices[voice].setLoopMode(loopmode);

        return 0;
    }
}