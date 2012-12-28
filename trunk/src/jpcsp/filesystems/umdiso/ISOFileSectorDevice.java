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
package jpcsp.filesystems.umdiso;

import static jpcsp.filesystems.umdiso.UmdIsoFile.sectorLength;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

import org.apache.log4j.Logger;

import jpcsp.Emulator;

public class ISOFileSectorDevice implements ISectorDevice {
	protected static Logger log = Emulator.log;
	protected RandomAccessFile fileReader;

	public ISOFileSectorDevice(RandomAccessFile fileReader) {
		this.fileReader = fileReader;
	}

	@Override
	public int getNumSectors() throws IOException {
		return (int) (fileReader.length() / sectorLength);
	}

	@Override
	public void readSector(int sectorNumber, byte[] buffer, int offset) throws IOException {
        fileReader.seek(((long) sectorLength) * sectorNumber);
        int length = fileReader.read(buffer, offset, sectorLength);
        if (length < sectorLength) {
        	Arrays.fill(buffer, length, sectorLength, (byte) 0);
        }
	}

	@Override
	public int readSectors(int sectorNumber, int numberSectors, byte[] buffer, int offset) throws IOException {
    	fileReader.seek(((long) sectorLength) * sectorNumber);
    	int length = fileReader.read(buffer, offset, numberSectors * sectorLength);

    	return length / sectorLength;
	}
}
