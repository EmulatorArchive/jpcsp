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
package jpcsp.util;

import static java.lang.System.arraycopy;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

import jpcsp.Emulator;
import jpcsp.Memory;
import jpcsp.MemoryMap;
import jpcsp.Allegrex.CpuState;
import jpcsp.HLE.Modules;
import jpcsp.filesystems.SeekableDataInput;
import jpcsp.filesystems.SeekableRandomFile;
import jpcsp.memory.IMemoryReader;
import jpcsp.memory.IMemoryWriter;
import jpcsp.memory.MemoryReader;
import jpcsp.memory.MemoryWriter;

public class Utilities {
    public static final Charset charset = Charset.forName("UTF-8");

    public static String formatString(String type, String oldstring) {
        int counter = 0;
        if (type.equals("byte")) {
            counter = 2;
        }
        if (type.equals("short")) {
            counter = 4;
        }
        if (type.equals("long")) {
            counter = 8;
        }
        int len = oldstring.length();
        StringBuilder sb = new StringBuilder();
        while (len++ < counter) {
            sb.append('0');
        }
        oldstring = sb.append(oldstring).toString();
        return oldstring;

    }

    public static String integerToBin(int value) {
        return Long.toBinaryString(0x0000000100000000L|((value)&0x00000000FFFFFFFFL)).substring(1);
    }

    public static String integerToHex(int value) {
        return Integer.toHexString(0x100 | value).substring(1).toUpperCase();
    }

    public static String integerToHexShort(int value) {
        return Integer.toHexString(0x10000 | value).substring(1).toUpperCase();
    }

    public static long readUWord(SeekableDataInput f) throws IOException {
        long l = (f.readUnsignedByte() | (f.readUnsignedByte() << 8) | (f.readUnsignedByte() << 16) | (f.readUnsignedByte() << 24));
        return (l & 0xFFFFFFFFL);
    }

    public static int readUByte(SeekableDataInput f) throws IOException {
        return f.readUnsignedByte();
    }

    public static int readUHalf(SeekableDataInput f) throws IOException {
        return f.readUnsignedByte() | (f.readUnsignedByte() << 8);
    }

    public static int readWord(SeekableDataInput f) throws IOException {
        //readByte() isn't more correct? (already exists one readUWord() method to unsign values)
        return (f.readUnsignedByte() | (f.readUnsignedByte() << 8) | (f.readUnsignedByte() << 16) | (f.readUnsignedByte() << 24));
    }

    public static void skipUnknown(ByteBuffer buf, int length) throws IOException {
        buf.position(buf.position() + length);
    }

    public static String readStringZ(ByteBuffer buf) throws IOException {
        StringBuilder sb = new StringBuilder();
        byte b;
        for (; buf.position() < buf.limit();) {
            b = (byte)readUByte(buf);
            if (b == 0)
                break;
            sb.append((char)b);
        }
        return sb.toString();
    }

     public static String readStringNZ(ByteBuffer buf, int n) throws IOException {
         StringBuilder sb = new StringBuilder();
         byte b;
         for (; n > 0; n--) {
               b = (byte)readUByte(buf);
             if (b != 0)
                 sb.append((char)b);
         }
         return sb.toString();
     }

     /**
      * Read a string from memory.
      * The string ends when the maximal length is reached or a '\0' byte is found.
      * The memory bytes are interpreted as UTF-8 bytes to form the string.
      *
      * @param mem     the memory
      * @param address the address of the first byte of the string
      * @param n       the maximal string length
      * @return        the string converted to UTF-8
      */
     public static String readStringNZ(Memory mem, int address, int n) {
         address &= Memory.addressMask;
         if (address + n > MemoryMap.END_RAM) {
             n = MemoryMap.END_RAM - address + 1;
         }

         // Allocate a byte array to store the bytes of the string.
         // At first, allocate maximum 10000 bytes in case we don't know
         // the maximal string length. The array will be extended if required.
         byte[] bytes = new byte[Math.min(n, 10000)];

         int length = 0;
         IMemoryReader memoryReader = MemoryReader.getMemoryReader(address, n, 1);
         for (; n > 0; n--) {
             int b = memoryReader.readNext();
             if (b == 0) {
                 break;
             }

             if (length >= bytes.length) {
            	 // Extend the bytes array
            	 byte[] newBytes = new byte[bytes.length + 10000];
            	 System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
            	 bytes = newBytes;
             }

             bytes[length] = (byte) b;
             length++;
         }

         // Convert the bytes to UTF-8
         return new String(bytes, 0, length, charset);
     }

     public static String readStringZ(Memory mem, int address) {
         address &= Memory.addressMask;
         return readStringNZ(mem, address, MemoryMap.END_RAM - address + 1);
     }
     public static String readStringZ(int address) {
         return readStringZ(Memory.getInstance(), address);
     }
     public static String readStringNZ(int address, int n) {
         return readStringNZ(Memory.getInstance(), address, n);
     }

     public static void writeStringNZ(Memory mem, int address, int n, String s) {
        int offset = 0;
        IMemoryWriter memoryWriter = MemoryWriter.getMemoryWriter(address, n, 1);
        byte[] bytes = s.getBytes(charset);
        while (offset < bytes.length && offset < n) {
            memoryWriter.writeNext(bytes[offset]);
            offset++;
        }
        while (offset < n) {
            memoryWriter.writeNext(0);
            offset++;
        }
    	memoryWriter.flush();
    }

     public static void writeStringZ(Memory mem, int address, String s) {
    	 // add 1 to the length to write the final '\0'
         writeStringNZ(mem, address, s.length() + 1, s);
     }
     public static void writeStringZ(ByteBuffer buf, String s) {
         buf.put(s.getBytes());
         buf.put((byte) 0);
     }

     public static short getUnsignedByte (ByteBuffer bb) throws IOException {
         return ((short)(bb.get() & 0xff));
     }
     public static void putUnsignedByte(ByteBuffer bb, int value) {
         bb.put((byte) (value & 0xFF));
     }
     public static short readUByte(ByteBuffer buf) throws IOException {
         return getUnsignedByte(buf);
     }
     public static int readUHalf(ByteBuffer buf) throws IOException {
         return getUnsignedByte(buf) | (getUnsignedByte(buf) << 8);
     }
     public static long readUWord(ByteBuffer buf) throws IOException {
         long l = (getUnsignedByte(buf) | (getUnsignedByte(buf) << 8 ) | (getUnsignedByte(buf) << 16 ) | (getUnsignedByte(buf) << 24));
         return (l & 0xFFFFFFFFL);
     }
     public static int readWord(ByteBuffer buf) throws IOException {
         return (getUnsignedByte(buf) | (getUnsignedByte(buf) << 8 ) | (getUnsignedByte(buf) << 16 ) | (getUnsignedByte(buf) << 24));
     }
     public static void writeWord(ByteBuffer buf, long value) {
         putUnsignedByte(buf, (int) (value >>  0));
         putUnsignedByte(buf, (int) (value >>  8));
         putUnsignedByte(buf, (int) (value >> 16));
         putUnsignedByte(buf, (int) (value >> 24));
     }
     public static void writeHalf(ByteBuffer buf, int value) {
         putUnsignedByte(buf, value >> 0);
         putUnsignedByte(buf, value >> 8);
     }
     public static void writeByte(ByteBuffer buf, int value) {
         putUnsignedByte(buf, value);
     }

     public static int parseAddress(String value) throws NumberFormatException {
         int address = 0;
         if (value == null) {
             return address;
         }

         value = value.trim();

         if (value.startsWith("0x")) {
             value = value.substring(2);
         }

         if (value.length() == 8 && value.charAt(0) >= '8') {
             address = (int) Long.parseLong(value, 16);
         } else {
             address = Integer.parseInt(value, 16);
         }

         return address;
     }


    /**
     * Parse the string as a number and returns its value.
     * If the string starts with "0x", the number is parsed
     * in base 16, otherwise base 10.
     *
     * @param s the string to be parsed
     * @return the numeric value represented by the string.
     */
     public static long parseLong(String s) {
         long value = 0;

         if (s == null) {
             return value;
         }

         if (s.startsWith("0x")) {
             value = Long.parseLong(s.substring(2), 16);
         } else {
             value = Long.parseLong(s);
         }
         return value;
     }

    /**
     * Parse the string as a number and returns its value.
     * The number is always parsed in base 16.
     * The string can start as an option with "0x".
     *
     * @param s the string to be parsed in base 16
     * @return the numeric value represented by the string.
     */
     public static long parseHexLong(String s) {
         long value = 0;

         if (s == null) {
             return value;
         }

         if (s.startsWith("0x")) {
             s = s.substring(2);
         }
         value = Long.parseLong(s, 16);
         return value;
     }

     public static int makePow2(int n) {
         --n;
         n = (n >>  1) | n;
         n = (n >>  2) | n;
         n = (n >>  4) | n;
         n = (n >>  8) | n;
         n = (n >> 16) | n;
         return ++n;
     }

     public static void readFully(SeekableDataInput input, int address, int length) throws IOException {
         final int blockSize = 1024 * 1024;  // 1Mb
         while (length > 0) {
             int size = Math.min(length, blockSize);
             byte[] buffer = new byte[size];
             input.readFully(buffer);
             Memory.getInstance().copyToMemory(address, ByteBuffer.wrap(buffer), size);
             address += size;
             length -= size;
         }
     }

     public static void write(SeekableRandomFile output, int address, int length) throws IOException {
         Buffer buffer = Memory.getInstance().getBuffer(address, length);
         if (buffer instanceof ByteBuffer) {
             output.getChannel().write((ByteBuffer) buffer);
         } else if (length > 0) {
             byte[] bytes = new byte[length];
             IMemoryReader memoryReader = MemoryReader.getMemoryReader(address, length, 1);
             for (int i = 0; i < length; i++) {
                 bytes[i] = (byte) memoryReader.readNext();
             }
             output.write(bytes);
         }
     }

     public static void bytePositionBuffer(Buffer buffer, int bytePosition) {
         buffer.position(bytePosition / bufferElementSize(buffer));
     }

     public static int bufferElementSize(Buffer buffer) {
         if (buffer instanceof IntBuffer) {
             return 4;
    	}

    	return 1;
    }

    public static String stripNL(String s) {
    	if (s != null && s.endsWith("\n")) {
    		s = s.substring(0, s.length() - 1);
    	}

    	return s;
    }

    public static void putBuffer(ByteBuffer destination, Buffer source, ByteOrder sourceByteOrder) {
        // Set the destination to the desired ByteOrder
		ByteOrder order = destination.order();
		destination.order(sourceByteOrder);

		if (source instanceof IntBuffer) {
    		destination.asIntBuffer().put((IntBuffer) source);
    	} else if (source instanceof ShortBuffer) {
    		destination.asShortBuffer().put((ShortBuffer) source);
    	} else if (source instanceof ByteBuffer) {
    		destination.put((ByteBuffer) source);
    	} else if (source instanceof FloatBuffer) {
    		destination.asFloatBuffer().put((FloatBuffer) source);
    	} else {
    		Modules.log.error("Utilities.putBuffer: Unsupported Buffer type " + source.getClass().getName());
    		Emulator.PauseEmuWithStatus(Emulator.EMU_STATUS_UNIMPLEMENTED);
    	}

		// Reset the original ByteOrder of the destination
		destination.order(order);
    }

    public static void putBuffer(ByteBuffer destination, Buffer source, ByteOrder sourceByteOrder, int lengthInBytes) {
        // Set the destination to the desired ByteOrder
		ByteOrder order = destination.order();
		destination.order(sourceByteOrder);

		int srcLimit = source.limit();
		if (source instanceof IntBuffer) {
    		destination.asIntBuffer().put((IntBuffer) source.limit(source.position() + (lengthInBytes >> 2)));
    	} else if (source instanceof ShortBuffer) {
    		destination.asShortBuffer().put((ShortBuffer) source.limit(source.position() + (lengthInBytes >> 1)));
    	} else if (source instanceof ByteBuffer) {
    		destination.put((ByteBuffer) source.limit(source.position() + lengthInBytes));
    	} else if (source instanceof FloatBuffer) {
    		destination.asFloatBuffer().put((FloatBuffer) source.limit(source.position() + (lengthInBytes >> 2)));
    	} else {
    		Modules.log.error("Utilities.putBuffer: Unsupported Buffer type " + source.getClass().getName());
    		Emulator.PauseEmuWithStatus(Emulator.EMU_STATUS_UNIMPLEMENTED);
    	}

		// Reset the original ByteOrder of the destination
		destination.order(order);
		// Reset the original limit of the source
		source.limit(srcLimit);
    }

        /**
     * Reads inputstream i into a String with the UTF-8 charset
     * until the inputstream is finished (don't use with infinite streams).
     * @param inputStream to read into a string
     * @param close if true, close the inputstream
     * @return a string
     * @throws java.io.IOException if thrown on reading the stream
     * @throws java.lang.NullPointerException if the given inputstream is null
     */
    public static String toString(InputStream inputStream, boolean close) throws IOException {
        if (inputStream == null) {
            throw new NullPointerException("null inputstream");
        }
        String string;
        StringBuilder outputBuilder = new StringBuilder();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            while (null != (string = reader.readLine())) {
                outputBuilder.append(string).append('\n');
            }
        } finally {
            if (close) {
                close(inputStream);
            }
        }
        return outputBuilder.toString();
    }

    /**
     * Close closeables. Use this in a finally clause.
     */
    public static void close(Closeable... closeables) {
        for (Closeable c : closeables) {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception ex) {
                    Logger.getLogger(Utilities.class.getName()).log(Level.WARNING, "Couldn't close Closeable", ex);
                }
            }
        }
    }

    public static long makeValue64(int low32, int high32) {
    	return (((long) high32) << 32) | ((low32) & 0xFFFFFFFFL);
    }

    public static void storeRegister64(CpuState cpu, int register, long value) {
    	cpu.gpr[register    ] = (int) (value      );
    	cpu.gpr[register + 1] = (int) (value >> 32);
    }

    public static void returnRegister64(CpuState cpu, long value) {
    	storeRegister64(cpu, 2, value);
    }

    public static long getRegister64(CpuState cpu, int register) {
    	return makeValue64(cpu.gpr[register], cpu.gpr[register + 1]);
    }

    public static int getSizeKb(long sizeByte) {
    	return (int) ((sizeByte + 1023) / 1024);
    }

    private static void addAsciiDump(StringBuilder dump, IMemoryReader charReader, int bytesPerLine) {
		dump.append("  >");
		for (int i = 0; i < bytesPerLine; i++) {
			char c = (char) charReader.readNext();
			if (c < ' ' || c > '~') {
				c = '.';
			}
			dump.append(c);
		}
		dump.append("<");
    }

    public static String getMemoryDump(int address, int length, int step, int bytesPerLine) {
    	if (!Memory.isAddressGood(address) || length <= 0 || bytesPerLine <= 0 || step <= 0) {
    		return "";
    	}

    	StringBuilder dump = new StringBuilder();

    	if (length < bytesPerLine) {
    		bytesPerLine = length;
    	}

    	IMemoryReader memoryReader = MemoryReader.getMemoryReader(address, length, step);
    	IMemoryReader charReader = MemoryReader.getMemoryReader(address, length, 1);
    	String format = String.format(" %%0%dX", step * 2);
    	boolean startOfLine = true;
    	for (int i = 0; i < length; i += step) {
    		if ((i % bytesPerLine) < step) {
    			if (i > 0) {
    				// Add an ASCII representation at the end of the line
    				addAsciiDump(dump, charReader, bytesPerLine);
    			}
    			dump.append("\n");
    			startOfLine = true;
    		}
    		if (startOfLine) {
    			dump.append(String.format("0x%08X", address + i));
    			startOfLine = false;
    		}

    		int value = memoryReader.readNext();
    		if (length - i >= step) {
    			dump.append(String.format(format, value));
    		} else {
    			switch (length - i) {
    				case 3:
    	    			dump.append(String.format(" %06X", value & 0x00FFFFFF));
    	    			break;
    				case 2:
    	    			dump.append(String.format(" %04X", value & 0x0000FFFF));
    	    			break;
    				case 1:
    	    			dump.append(String.format(" %02X", value & 0x000000FF));
    	    			break;
    			}
    		}
    	}

		int lengthLastLine = length % bytesPerLine;
    	if (lengthLastLine > 0) {
    		for (int i = lengthLastLine; i < bytesPerLine; i++) {
    			dump.append("  ");
    			if ((i % step) == 0) {
    				dump.append(" ");
    			}
    		}
    		addAsciiDump(dump, charReader, bytesPerLine - lengthLastLine);
    	} else {
    		addAsciiDump(dump, charReader, bytesPerLine);
    	}

    	return dump.toString();
    }

    public static String getMemoryDump(int[] values, int offset, int length, int entriesPerLine) {
    	StringBuilder dump = new StringBuilder();

    	boolean startOfLine = true;
    	for (int i = 0; i < length; i++) {
    		if ((i % entriesPerLine) == 0) {
    			dump.append("\n");
    			startOfLine = true;
    		}
    		if (startOfLine) {
    			dump.append(String.format("0x%08X", (offset + i) << 2));
    			startOfLine = false;
    		}

    		int value = values[offset + i];
    		dump.append(String.format(" %08X", value));
    	}

    	return dump.toString();
    }

    public static int alignUp(int value, int alignment) {
    	return alignDown(value + alignment, alignment);
    }

    public static int alignDown(int value, int alignment) {
    	return value & ~alignment;
    }

    public static int endianSwap32(int x) {
    	return Integer.reverseBytes(x);
    }

    public static int readUnaligned32(Memory mem, int address) {
        switch (address & 3) {
            case 0: return mem.read32(address);
            case 2: return mem.read16(address) | (mem.read16(address + 2) << 16);
            default:
                return (mem.read8(address + 3) << 24) |
                       (mem.read8(address + 2) << 16) |
                       (mem.read8(address + 1) <<  8) |
                       (mem.read8(address));
        }
    }

    public static void writeUnaligned32(Memory mem, int address, int data) {
        switch (address & 3) {
            case 0:
            	mem.write32(address, data);
            	break;
            case 2:
            	mem.write16(address, (short) data);
            	mem.write16(address + 2, (short) (data >> 16));
            	break;
            default:
            	mem.write8(address, (byte) data);
            	mem.write8(address + 1, (byte) (data >> 8));
            	mem.write8(address + 2, (byte) (data >> 16));
            	mem.write8(address + 3, (byte) (data >> 24));
        }
    }

	public static int min(int a, int b) {
		return Math.min(a, b);
	}

	public static int max(int a, int b) {
		return Math.max(a, b);
	}

	public static int min(int a, int b, int c) {
		return Math.min(a, Math.min(b, c));
	}

	public static int max(int a, int b, int c) {
		return Math.max(a, Math.max(b, c));
	}

	public static void sleep(int micros) {
    	sleep(micros / 1000, micros % 1000);
    }

    public static void sleep(int millis, int micros) {
        try {
        	if (micros <= 0) {
        		Thread.sleep(millis);
        	} else {
        		Thread.sleep(millis, micros * 1000);
        	}
        } catch (InterruptedException e) {
        	// Ignore exception
        }
    }

    public static void matrixMult(float[] result, final float[] m1, final float[] m2) {
    	// If the result has to be stored into one of the input matrix,
    	// store the result in a temp array first.
    	float[] origResult = null;
    	if (result == m1 || result == m2) {
    		origResult = result;
    		result = new float[16];
    	}

    	int i = 0;
    	int j = 0;
    	for (int y = 0; y < 4; y++) {
    		for (int x = 0; x < 4; x++) {
    			result[i] = m1[x] * m2[j]
    			          + m1[x + 4] * m2[j + 1]
    			          + m1[x + 8] * m2[j + 2]
    			          + m1[x + 12] * m2[j + 3];
    			i++;
    		}
    		j += 4;
    	}

    	if (origResult != null) {
    		arraycopy(result, 0, origResult, 0, result.length);
    	}
    }

    public static void vectorMult(final float[] result, final float[] m, final float[] v) {
    	for (int i = 0; i < result.length; i++) {
    		float s = v[0] * m[i];
    		int k = i + 4;
    		for (int j = 1; j < v.length; j++) {
    			s += v[j] * m[k];
    			k += 4;
    		}
    		result[i] = s;
    	}
    }
}