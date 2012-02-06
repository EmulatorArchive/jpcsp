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
package jpcsp.graphics.RE.software;

import static jpcsp.graphics.RE.software.PixelColor.getColor;

import org.apache.log4j.Logger;

import jpcsp.graphics.VideoEngine;

/**
 * @author gid15
 *
 */
public class VertexColorFilter {
	protected static final Logger log = VideoEngine.log;

	public static IPixelFilter getVertexColorFilter(float[] c1, float c2[], float[] c3) {
		IPixelFilter filter;

		if (sameColor(c1, c2, c3)) {
        	if (log.isTraceEnabled()) {
        		log.trace(String.format("Using ColorTextureFilter color=0x%08X", getColor(c1)));
        	}
			filter = new ColorTextureFilter(c1);
		} else  {
        	if (log.isTraceEnabled()) {
        		log.trace(String.format("Using VertexTriangleTextureFilter color1=0x%08X, color2=0x%08X, color3=0x%08X", getColor(c1), getColor(c2), getColor(c3)));
        	}
			filter = new VertexTriangleTextureFilter(c1, c2, c3);
		}

		return filter;
	}

	private static boolean sameColor(float[] c1, float[] c2, float[] c3) {
		for (int i = 0; i < 4; i++) {
			if (c1[i] != c2[i] || c1[i] != c3[i]) {
				return false;
			}
		}

		return true;
	}

	private static final class VertexTriangleTextureFilter implements IPixelFilter {
		private final int[] color1 = new int[4];
		private final int[] color2 = new int[4];
		private final int[] color3 = new int[4];

		public VertexTriangleTextureFilter(float[] c1, float c2[], float[] c3) {
			for (int i = 0; i < 4; i++) {
				color1[i] = getColor(c1[i]);
				color2[i] = getColor(c2[i]);
				color3[i] = getColor(c3[i]);
			}
		}

		@Override
		public void filter(PixelState pixel) {
			int a = pixel.getTriangleWeightedValue(color1[3], color2[3], color3[3]);
			int b = pixel.getTriangleWeightedValue(color1[2], color2[2], color3[2]);
			int g = pixel.getTriangleWeightedValue(color1[1], color2[1], color3[1]);
			int r = pixel.getTriangleWeightedValue(color1[0], color2[0], color3[0]);

			pixel.primaryColor = getColor(a, b, g, r);
		}

		@Override
		public int getCompilationId() {
			return 493722550;
		}

		@Override
		public int getFlags() {
			return 0;
		}
	}

	private static final class ColorTextureFilter implements IPixelFilter {
		private int color;

		public ColorTextureFilter(float[] color) {
			this.color = getColor(color);
		}

		@Override
		public void filter(PixelState pixel) {
			pixel.primaryColor = color;
		}

		@Override
		public int getCompilationId() {
			return 903177108;
		}

		@Override
		public int getFlags() {
			return 0;
		}
	}
}
