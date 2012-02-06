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

/**
 * @author gid15
 *
 * Interface for a pixel filter.
 */
public interface IPixelFilter {
	public static final int REQUIRES_SOURCE_DEPTH = (1 << 0);
	public static final int DISCARDS_SOURCE_DEPTH = (1 << 1);
	public static final int REQUIRES_TEXTURE_U_V = (1 << 2);
	public static final int DISCARDS_TEXTURE_U_V = (1 << 3);

	/**
	 * Filter the value of the current pixel according to the filter function.
	 * Updates the values in the PixelState.
	 *
	 * @param pixel    the state of the current pixel
	 */
	public void filter(PixelState pixel);

	public int getCompilationId();

	public int getFlags();
}
