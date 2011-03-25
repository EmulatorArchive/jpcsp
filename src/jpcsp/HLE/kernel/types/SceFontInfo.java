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
package jpcsp.HLE.kernel.types;

import jpcsp.format.PGF;
import jpcsp.util.Debug;
import jpcsp.HLE.modules150.sceFont;

/*
 * SceFontInfo struct based on BenHur's intraFont application.
 * This struct is used to give an easy and organized access to the PGF data.
 */

public class SceFontInfo {
    // Statics based on intraFont's findings.
    public static final int FONT_FILETYPE_PGF = 0x00;
    public static final int FONT_FILETYPE_BWFON = 0x01;
    public static final int FONT_PGF_BMP_H_ROWS = 0x01;
    public static final int FONT_PGF_BMP_V_ROWS = 0x02;
    public static final int FONT_PGF_BMP_OVERLAY = 0x03;
    public static final int FONT_PGF_METRIC_FLAG1 = 0x04;
    public static final int FONT_PGF_METRIC_FLAG2 = 0x08;
    public static final int FONT_PGF_METRIC_FLAG3 = 0x10;
    public static final int FONT_PGF_CHARGLYPH = 0x20;
    public static final int FONT_PGF_SHADOWGLYPH = 0x40;

    // PGF file.
    protected String fileName;  // The PGF file name.
    protected String fileType;  // The file type (only PGF support for now).
    protected int[] fontdata;   // Fontdata extracted from the PGF.
    protected long fontdataBits;

    // Characters properties and glyphs.
    protected int n_chars;
    protected int advancex;
    protected int advancey;
    protected int charmap_compr_len;
    protected int[] charmap_compr;
    protected int[] charmap;
    protected Glyph[] glyphs;
    protected int firstGlyph;

    // Shadow characters properties and glyphs.
    protected int n_shadows;
    protected int shadowscale;
    protected Glyph[] shadowGlyphs;

    // Tables from PGF.
    protected int[] shadowCharMap;
    protected int[] charPointerTable;
    protected int[][] advanceTable;

    // Font style from registry
    protected pspFontStyle fontStyle;

    // Glyph class.
    protected static class Glyph {
    	protected int x;
    	protected int y;
    	protected int w;
    	protected int h;
    	protected int left;
    	protected int top;
    	protected int flags;
    	protected int shadowID;
    	protected int advanceH;
    	protected int advanceV;
    	protected long ptr;

        public boolean hasFlag(int flag) {
        	return (flags & flag) == flag;
        }

        @Override
    	public String toString() {
    		return String.format("Glyph[x=%d, y=%d, w=%d, h=%d, left=%d, top=%d, flags=0x%X, shadowID=%d, advance=%d, ptr=%d]", x, y, w, h, left, top, flags, shadowID, advanceH, ptr);
    	}
    }

    public SceFontInfo(PGF fontFile) {
        // PGF.
        fileName = fontFile.getFileNamez();
        fileType = fontFile.getPGFMagic();

        // Characters/Shadow characters' variables.
        n_chars = fontFile.getCharPointerLength();
        n_shadows = fontFile.getShadowMapLength();
        charmap = new int[fontFile.getCharMapLength() * 2];
        charmap_compr_len = (fontFile.getRevision() == 3) ? 7 : 1;
        charmap_compr = new int[charmap_compr_len * 4];
        advancex = fontFile.getMaxAdvance()[0]/16;
        advancey = fontFile.getMaxAdvance()[1]/16;
        shadowscale = fontFile.getShadowScale()[0];
        glyphs = new Glyph[n_chars];
        shadowGlyphs = new Glyph[n_chars];
        firstGlyph = fontFile.getFirstGlyphInCharMap();

        // Get advance table.
        advanceTable = fontFile.getAdvanceTable();

        // Get shadow char map.
        int[] rawShadowCharMap = fontFile.getShadowCharMap();
        shadowCharMap = new int[fontFile.getShadowMapLength()];
        for (int i = 0; i < shadowCharMap.length; i++) {
        	shadowCharMap[i] = getBits(fontFile.getShadowMapBpe(), rawShadowCharMap, i * fontFile.getShadowMapBpe());
        }
        shadowCharMap = fontFile.getShadowCharMap();

        // Get char map.
        int[] rawCharMap = fontFile.getCharMap();
        for (int i = 0; i < fontFile.getCharMapLength(); i++) {
        	charmap[i] = getBits(fontFile.getCharMapBpe(), rawCharMap, i * fontFile.getCharMapBpe());
        	if (charmap[i] >= n_chars) {
        		charmap[i] = 65535;
        	}
        }

        // Get char pointer table.
        int[] rawCharPointerTable = fontFile.getCharPointerTable();
        charPointerTable = new int[n_chars];
        for (int i = 0; i < charPointerTable.length; i++) {
        	charPointerTable[i] = getBits(fontFile.getCharPointerBpe(), rawCharPointerTable, i * fontFile.getCharPointerBpe());
        }

        // Get raw fontdata.
        fontdata = fontFile.getFontdata();
        fontdataBits = fontdata.length * 8L;

        // Generate glyphs for all chars.
        for (int i = 0; i < n_chars; i++) {
            glyphs[i] = getGlyph(fontdata, (charPointerTable[i] * 4 * 8), FONT_PGF_CHARGLYPH, advanceTable[0], advanceTable[1]);
        }

        // Generate shadow glyphs for all chars.
        for (int i = 0; i < n_chars; i++) {
            shadowGlyphs[i] = getGlyph(fontdata, (charPointerTable[i] * 4 * 8), FONT_PGF_SHADOWGLYPH, null, null);
        }
    }

    // Retrieve bits from a byte buffer based on bpe.
    private int getBits(int bpe, int[] buf, long pos) {
        int v = 0;
        for (int i = 0; i < bpe; i++) {
            v += (((buf[(int) (pos / 8)] >> ((pos) % 8) ) & 1) << i);
            pos++;
        }
        return v;
    }

    // Create and retrieve a glyph from the font data.
    private Glyph getGlyph(int[] fontdata, long charPtr, int glyphType, int[] advanceHmap, int[] advanceVmap) {
    	Glyph glyph = new Glyph();
        if (glyphType == FONT_PGF_SHADOWGLYPH) {
            if (charPtr + 96 > fontdataBits) {
        		return null;
        	}
            charPtr += getBits(14, fontdata, charPtr) * 8;
        }
        if (charPtr + 96 > fontdataBits) {
    		return null;
    	}

        charPtr += 14;

        glyph.w = getBits(7, fontdata, charPtr);
        charPtr += 7;

        glyph.h = getBits(7, fontdata, charPtr);
        charPtr += 7;

        glyph.left = getBits(7, fontdata, charPtr);
        charPtr += 7;
        if (glyph.left >= 64) {
            glyph.left -= 128;
        }

        glyph.top = getBits(7, fontdata, charPtr);
        charPtr += 7;
        if (glyph.top >= 64) {
            glyph.top -= 128;
        }

        glyph.flags = getBits(6, fontdata, charPtr);
        charPtr += 6;

        if (glyph.hasFlag(FONT_PGF_CHARGLYPH)) {
            charPtr += 7;

            glyph.shadowID = getBits(9, fontdata, charPtr);
            charPtr += 9;

            charPtr += 24 + (glyph.hasFlag(FONT_PGF_METRIC_FLAG1) ? 0 : 56)
                          + (glyph.hasFlag(FONT_PGF_METRIC_FLAG2) ? 0 : 56)
                          + (glyph.hasFlag(FONT_PGF_METRIC_FLAG3) ? 0 : 56);

        	int advanceIndex = getBits(8, fontdata, charPtr);
            charPtr += 8;
            if (advanceHmap != null && advanceIndex < advanceHmap.length) {
                glyph.advanceH = advanceHmap[advanceIndex];
            } else {
                glyph.advanceH = 0;
            }
            if (advanceVmap != null && advanceIndex < advanceVmap.length) {
                glyph.advanceV = advanceVmap[advanceIndex];
            } else {
                glyph.advanceV = 0;
            }
        } else {
            glyph.shadowID = 65535;
            glyph.advanceH = 0;
        }

        glyph.ptr = charPtr / 8;

        return glyph;
    }

    private Glyph getCharGlyph(int charCode, int glyphType) {
    	if (charCode < firstGlyph) {
    		return null;
    	}

    	charCode -= firstGlyph;
    	if (charCode < charmap.length) {
    		charCode = charmap[charCode];
    	}

    	Glyph glyph;
        if (glyphType == FONT_PGF_CHARGLYPH) {
            if (charCode >= glyphs.length) {
                return null;
            }
            glyph = glyphs[charCode];
        } else {
            if (charCode >= shadowGlyphs.length) {
                return null;
            }
            glyph = shadowGlyphs[charCode];
        }

        return glyph;
    }

    // Generate a 4bpp texture for the given char id.
    private void generateFontTexture(int base, int bpl, int bufWidth, int bufHeight, int x, int y, int pixelformat, int charCode, int altCharCode, int glyphType) {
    	Glyph glyph = getCharGlyph(charCode, glyphType);
    	if (glyph == null) {
    		// No Glyph available for this charCode, try to use the alternate char.
            charCode = altCharCode;
            glyph = getCharGlyph(charCode, glyphType);
            if (glyph == null) {
            	return;
            }
    	}

        if (glyph.w <= 0 || glyph.h <= 0) {
        	return;
        }
        if (((glyph.flags & FONT_PGF_BMP_OVERLAY) != FONT_PGF_BMP_H_ROWS) &&
            ((glyph.flags & FONT_PGF_BMP_OVERLAY) != FONT_PGF_BMP_V_ROWS)) {
        	return;
        }

    	long bitPtr = glyph.ptr * 8;
        final int nibbleBits = 4;
        int nibble;
        int value = 0;
        int xx, yy, count;
        boolean bitmapHorizontalRows = (glyph.flags & FONT_PGF_BMP_OVERLAY) == FONT_PGF_BMP_H_ROWS;
        int numberPixels = glyph.w * glyph.h;
        int pixelIndex = 0;
        while (pixelIndex < numberPixels && bitPtr + 8 < fontdataBits) {
            nibble = getBits(nibbleBits, fontdata, bitPtr);
            bitPtr += nibbleBits;

            if (nibble < 8) {
                value = getBits(nibbleBits, fontdata, bitPtr);
                bitPtr += nibbleBits;
                count = nibble + 1;
            } else {
            	count = 16 - nibble;
            }

            for (int i = 0; i < count && pixelIndex < numberPixels; i++) {
                if (nibble >= 8) {
                    value = getBits(nibbleBits, fontdata, bitPtr);
                    bitPtr += nibbleBits;
                }

                if (bitmapHorizontalRows) {
                    xx = pixelIndex % glyph.w;
                    yy = pixelIndex / glyph.w;
                } else {
                    xx = pixelIndex / glyph.h;
                    yy = pixelIndex % glyph.h;
                }

                // 4-bit color value
                int pixelColor = value;
                switch (pixelformat) {
                	case sceFont.PSP_FONT_PIXELFORMAT_8:
                        // 8-bit color value
                		pixelColor |= pixelColor << 4;
                		break;
                	case sceFont.PSP_FONT_PIXELFORMAT_24:
                        // 24-bit color value
                		pixelColor |= pixelColor << 4;
                		pixelColor |= pixelColor << 8;
                		pixelColor |= pixelColor << 8;
                		break;
                	case sceFont.PSP_FONT_PIXELFORMAT_32:
                        // 32-bit color value
    					pixelColor |= pixelColor << 4;
    					pixelColor |= pixelColor << 8;
    					pixelColor |= pixelColor << 16;
    					break;
                }
                Debug.setFontPixel(base, bpl, bufWidth, bufHeight, x + xx, y + yy, pixelColor, pixelformat);
        		pixelIndex++;
            }
        }
    }

    public void printFont(int base, int bpl, int bufWidth, int bufHeight, int x, int y, int pixelformat, int charCode, int altCharCode) {
        generateFontTexture(base, bpl, bufWidth, bufHeight, x, y, pixelformat, charCode, altCharCode, FONT_PGF_CHARGLYPH);
    }

    public pspCharInfo getCharInfo(int charCode) {
    	pspCharInfo charInfo = new pspCharInfo();
    	Glyph glyph = getCharGlyph(charCode, FONT_PGF_CHARGLYPH);
    	if (glyph == null) {
    		return null;
    	}

    	charInfo.bitmapWidth = glyph.w;
    	charInfo.bitmapHeight = glyph.h;
    	charInfo.bitmapLeft = glyph.left;
    	charInfo.bitmapTop = glyph.top;
    	charInfo.sfp26Width = glyph.w << 6;
    	charInfo.sfp26Height = glyph.h << 6;
    	charInfo.sfp26Ascender = glyph.top << 6;
    	charInfo.sfp26Descender = (glyph.h - glyph.top) << 6;
    	charInfo.sfp26BearingHX = glyph.left << 6;
    	charInfo.sfp26BearingHY = glyph.top << 6;
    	charInfo.sfp26BearingVX = glyph.left << 6;
    	charInfo.sfp26BearingVY = glyph.top << 6;
    	charInfo.sfp26AdvanceH = glyph.advanceH;
    	charInfo.sfp26AdvanceV = glyph.advanceV;

    	return charInfo;
    }

	public pspFontStyle getFontStyle() {
		return fontStyle;
	}

	public void setFontStyle(pspFontStyle fontStyle) {
		this.fontStyle = fontStyle;
	}
}