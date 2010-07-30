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
package jpcsp.graphics.capture;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.Buffer;
import java.nio.BufferUnderflowException;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import jpcsp.graphics.GeCommands;
import jpcsp.graphics.VideoEngine;
import jpcsp.memory.IMemoryReader;
import jpcsp.memory.MemoryReader;

/**
 * @author gid15
 *
 */
public class CaptureImage {
	private int imageaddr;
	private int level;
	private Buffer buffer;
	private int width;
	private int height;
	private int bufferWidth;
	private int bufferStorage;
	private boolean compressedImage;
	private int compressedImageSize;
	private boolean overwriteFile;

	public CaptureImage(int imageaddr, int level, Buffer buffer, int width, int height, int bufferWidth, int bufferStorage, boolean compressedImage, int compressedImageSize) {
		this.imageaddr = imageaddr;
		this.level = level;
		this.buffer = buffer;
		this.width = width;
		this.height = height;
		this.bufferWidth = bufferWidth;
		this.bufferStorage = bufferStorage;
		this.compressedImage = compressedImage;
		this.compressedImageSize = compressedImageSize;
		this.overwriteFile = false;
	}

	public CaptureImage(int imageaddr, int level, Buffer buffer, int width, int height, int bufferWidth, int bufferStorage, boolean compressedImage, int compressedImageSize, boolean overwriteFile) {
		this.imageaddr = imageaddr;
		this.level = level;
		this.buffer = buffer;
		this.width = width;
		this.height = height;
		this.bufferWidth = bufferWidth;
		this.bufferStorage = bufferStorage;
		this.compressedImage = compressedImage;
		this.compressedImageSize = compressedImageSize;
		this.overwriteFile = overwriteFile;
	}

    public void write() throws IOException {
    	String levelName = "";
    	if (level > 0) {
    		levelName = "_" + level;
    	}

    	String fileName = null;
    	for (int i = 0; ; i++) {
    		String id = (i == 0 ? "" : "-" + i);
    		fileName = String.format("tmp/Image%08X%s%s.bmp", imageaddr, levelName, id);
    		if (overwriteFile) {
    			break;
    		}

    		File file = new File(fileName);
    		if (!file.exists()) {
    			break;
    		}
    	}

    	if (compressedImage) {
    		decompressImage();
    	}

		if (width > bufferWidth) {
			width = bufferWidth;
		}

		// Unfortunately, I was not able to generate the image file
		// using the ImageIO API :-(
		// This is why I'm generating a BMP file manually...
		byte[] fileHeader = new byte[14];
		byte[] dibHeader = new byte[40];
		int rowPad = (4 - ((width * 3) & 3)) & 3;
		int imageSize = height * ((width * 3) + rowPad);
		int fileSize = fileHeader.length + dibHeader.length + imageSize;
		OutputStream outBmp = new BufferedOutputStream(new FileOutputStream(fileName), fileSize);

		fileHeader[0] = 'B';
		fileHeader[1] = 'M';
		storeLittleEndianInt(fileHeader, 2, fileSize);
		storeLittleEndianInt(fileHeader, 10, fileHeader.length + dibHeader.length);

		storeLittleEndianInt(dibHeader, 0, dibHeader.length);
		storeLittleEndianInt(dibHeader, 4, width);
		storeLittleEndianInt(dibHeader, 8, -height);
		storeLittleEndianShort(dibHeader, 12, 1);
		storeLittleEndianShort(dibHeader, 14, 24);
		storeLittleEndianInt(dibHeader, 16, 0);
		storeLittleEndianInt(dibHeader, 20, imageSize);
		storeLittleEndianInt(dibHeader, 24, 2835);
		storeLittleEndianInt(dibHeader, 28, 2835);
		storeLittleEndianInt(dibHeader, 32, 0);
		storeLittleEndianInt(dibHeader, 36, 0);

		outBmp.write(fileHeader);
		outBmp.write(dibHeader);
		byte[] rowPadBytes = new byte[rowPad];
		byte[] pixelBytes = new byte[3];
		boolean imageType32Bit = bufferStorage == GeCommands.TPSM_PIXEL_STORAGE_MODE_32BIT_ABGR8888;
    	if (buffer instanceof IntBuffer && imageType32Bit) {
    		IntBuffer intBuffer = (IntBuffer) buffer;
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					try {
						int pixel = intBuffer.get();
						pixelBytes[0] = (byte) (pixel >> 16);
						pixelBytes[1] = (byte) (pixel >>  8);
						pixelBytes[2] = (byte) (pixel      );
						outBmp.write(pixelBytes);
					} catch (BufferUnderflowException e) {
						pixelBytes[0] = pixelBytes[1] = pixelBytes[2] = 0;
						outBmp.write(pixelBytes);
					}
				}
				outBmp.write(rowPadBytes);
				for (int x = width; x < bufferWidth; x++) {
					try {
						intBuffer.get();
					} catch (BufferUnderflowException e) {
						// Ignore exception
					}
				}
			}
    	} else if (buffer instanceof IntBuffer && !imageType32Bit) {
    		IntBuffer intBuffer = (IntBuffer) buffer;
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x += 2) {
					try {
						int twoPixels = intBuffer.get();
						getPixelBytes((short) twoPixels, bufferStorage, pixelBytes);
						outBmp.write(pixelBytes);
						getPixelBytes((short) (twoPixels >>> 16), bufferStorage, pixelBytes);
						outBmp.write(pixelBytes);
					} catch (BufferUnderflowException e) {
						pixelBytes[0] = pixelBytes[1] = pixelBytes[2] = 0;
						outBmp.write(pixelBytes);
						outBmp.write(pixelBytes);
					}
				}
				outBmp.write(rowPadBytes);
				for (int x = width; x < bufferWidth; x += 2) {
					try {
						intBuffer.get();
					} catch (BufferUnderflowException e) {
						// Ignore exception
					}
				}
			}
    	} else if (buffer instanceof ShortBuffer && !imageType32Bit) {
    		ShortBuffer shortBuffer = (ShortBuffer) buffer;
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					short pixel = shortBuffer.get();
					getPixelBytes(pixel, bufferStorage, pixelBytes);
					outBmp.write(pixelBytes);
				}
				outBmp.write(rowPadBytes);
				for (int x = width; x < bufferWidth; x++) {
					shortBuffer.get();
				}
			}
    	} else if (imageType32Bit) {
    		IMemoryReader memoryReader = MemoryReader.getMemoryReader(imageaddr, bufferWidth * height * 4, 4);
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					int pixel = memoryReader.readNext();
					pixelBytes[0] = (byte) (pixel >> 16);
					pixelBytes[1] = (byte) (pixel >>  8);
					pixelBytes[2] = (byte) (pixel      );
					outBmp.write(pixelBytes);
				}
				outBmp.write(rowPadBytes);
				for (int x = width; x < bufferWidth; x++) {
					memoryReader.readNext();
				}
			}
    	} else {
    		IMemoryReader memoryReader = MemoryReader.getMemoryReader(imageaddr, bufferWidth * height * 2, 2);
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					short pixel = (short) memoryReader.readNext();
					getPixelBytes(pixel, bufferStorage, pixelBytes);
					outBmp.write(pixelBytes);
				}
				outBmp.write(rowPadBytes);
				for (int x = width; x < bufferWidth; x++) {
					memoryReader.readNext();
				}
			}
    	}
    	buffer.rewind();
		outBmp.close();

        VideoEngine.log.debug(String.format("Saved image to %s", fileName));
    }

    private void storeLittleEndianInt(byte[] buffer, int offset, int value) {
    	buffer[offset    ] = (byte) (value      );
    	buffer[offset + 1] = (byte) (value >>  8);
    	buffer[offset + 2] = (byte) (value >> 16);
    	buffer[offset + 3] = (byte) (value >> 24);
    }

    private void storeLittleEndianShort(byte[] buffer, int offset, int value) {
    	buffer[offset    ] = (byte) (value      );
    	buffer[offset + 1] = (byte) (value >>  8);
    }

    private void getPixelBytes(short pixel, int imageType, byte[] pixelBytes) {
    	switch (imageType) {
    	case GeCommands.TPSM_PIXEL_STORAGE_MODE_16BIT_BGR5650:
    		pixelBytes[0] = (byte) ((pixel >> 8) & 0xF8);
    		pixelBytes[1] = (byte) ((pixel >> 3) & 0xFC);
    		pixelBytes[2] = (byte) ((pixel << 3) & 0xF8);
    		break;
    	case GeCommands.TPSM_PIXEL_STORAGE_MODE_16BIT_ABGR5551:
    		pixelBytes[0] = (byte) ((pixel >> 7) & 0xF8);
    		pixelBytes[1] = (byte) ((pixel >> 2) & 0xF8);
    		pixelBytes[2] = (byte) ((pixel << 3) & 0xF8);
    		break;
    	case GeCommands.TPSM_PIXEL_STORAGE_MODE_16BIT_ABGR4444:
    		pixelBytes[0] = (byte) ((pixel >> 4) & 0xF0);
    		pixelBytes[1] = (byte) ((pixel     ) & 0xF0);
    		pixelBytes[2] = (byte) ((pixel << 4) & 0xF0);
    		int alpha = (pixel >> 12) & 0x0F;
    		pixelBytes[0] = (byte) ((pixelBytes[0] * alpha) / 0x0F);
    		pixelBytes[1] = (byte) ((pixelBytes[1] * alpha) / 0x0F);
    		pixelBytes[2] = (byte) ((pixelBytes[2] * alpha) / 0x0F);
    		break;
		default:
			pixelBytes[0] = 0;
			pixelBytes[1] = 0;
			pixelBytes[2] = 0;
			break;
    	}
    }

    private void storePixel(IntBuffer buffer, int x, int y, int color) {
    	buffer.put(y * width + x, color);
    }

    private int round4(int n) {
    	return (n + 3) & ~3;
    }

    private void decompressImageDXT(int dxtLevel) {
		IntBuffer decompressedBuffer = IntBuffer.allocate(round4(width) * round4(height));
		IntBuffer compressedBuffer = (IntBuffer) buffer;

		int strideX = 0;
		int strideY = 0;
		int[] colors = new int[4];
		int strideSize = (dxtLevel == 1 ? 8 : 16);
		for (int i = 0; i < compressedImageSize; i += strideSize) {
			if (dxtLevel > 1) {
				// Skip Alpha values
				compressedBuffer.get();
				compressedBuffer.get();
			}
			int color = compressedBuffer.get();
			int color0 = (color >>  0) & 0xFFFF;
			int color1 = (color >> 16) & 0xFFFF;

			int r0 = (color0 >> 8) & 0xF8;
			int g0 = (color0 >> 3) & 0xFC;
			int b0 = (color0 << 3) & 0xF8;

			int r1 = (color1 >> 8) & 0xF8;
			int g1 = (color1 >> 3) & 0xFC;
			int b1 = (color1 << 3) & 0xF8;

			int r2, g2, b2;
			if (color0 > color1) {
				r2 = (r0 * 2 + r1) / 3;
				g2 = (g0 * 2 + g1) / 3;
				b2 = (b0 * 2 + b1) / 3;
			} else {
				r2 = (r0 + r1) / 2;
				g2 = (g0 + g1) / 2;
				b2 = (b0 + b1) / 2;
			}

			int r3, g3, b3;
			if (color0 > color1 || dxtLevel > 1) {
				r3 = (r0 + r1 * 2) / 3;
				g3 = (g0 + g1 * 2) / 3;
				b3 = (b0 + b1 * 2) / 3;
			} else {
				r3 = 0x00;
				g3 = 0x00;
				b3 = 0x00;
			}

			colors[0] = ((b0 & 0xFF) << 16) | ((g0 & 0xFF) << 8) | (r0 & 0xFF);
			colors[1] = ((b1 & 0xFF) << 16) | ((g1 & 0xFF) << 8) | (r1 & 0xFF);
			colors[2] = ((b2 & 0xFF) << 16) | ((g2 & 0xFF) << 8) | (r2 & 0xFF);
			colors[3] = ((b3 & 0xFF) << 16) | ((g3 & 0xFF) << 8) | (r3 & 0xFF);

			int bits = compressedBuffer.get();
			for (int y = 0; y < 4; y++) {
				for (int x = 0; x < 4; x++, bits >>>= 2) {
					storePixel(decompressedBuffer, strideX + x, strideY + y, colors[bits & 3]);
				}
			}

			strideX += 4;
			if (strideX >= width) {
				strideX = 0;
				strideY += 4;
			}
		}

		compressedBuffer.rewind();
		compressedImage = false;
		buffer = decompressedBuffer;
		bufferWidth = width;
		bufferStorage = GeCommands.TPSM_PIXEL_STORAGE_MODE_32BIT_ABGR8888;
    }

    private void decompressImage() {
    	switch (bufferStorage) {
		case GeCommands.TPSM_PIXEL_STORAGE_MODE_DXT1: {
			decompressImageDXT(1);
			break;
		}

		case GeCommands.TPSM_PIXEL_STORAGE_MODE_DXT3: {
			decompressImageDXT(3);
			break;
		}

		case GeCommands.TPSM_PIXEL_STORAGE_MODE_DXT5: {
			decompressImageDXT(5);
			break;
		}

		default:
			VideoEngine.log.warn("Unsupported compressed buffer storage " + bufferStorage);
			break;
		}
    }
}
