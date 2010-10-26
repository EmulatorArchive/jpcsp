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
package jpcsp.graphics.RE;

import static jpcsp.graphics.RE.DirectBufferUtilities.getDirectBuffer;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.opengl.GL15;

/**
 * @author gid15
 *
 * A RenderingEngine implementing calls to OpenGL using jogl
 * for OpenGL Version >= 1.5.
 * The class contains no rendering logic, it just implements the interface to jogl.
 */
public class RenderingEngineLwjgl15 extends RenderingEngineLwjgl12 {
	public RenderingEngineLwjgl15() {
	}

	@Override
	public void deleteBuffer(int buffer) {
		GL15.glDeleteBuffers(buffer);
	}

	@Override
	public int genBuffer() {
		return GL15.glGenBuffers();
	}

	@Override
	public void setBufferData(int size, Buffer buffer, int usage) {
		if (buffer instanceof ByteBuffer) {
			GL15.glBufferData(bufferTarget, getDirectBuffer(size, (ByteBuffer) buffer), bufferUsageToGL[usage]);
		} else if (buffer instanceof IntBuffer) {
			GL15.glBufferData(bufferTarget, getDirectBuffer(size, (IntBuffer) buffer), bufferUsageToGL[usage]);
		} else if (buffer instanceof ShortBuffer) {
			GL15.glBufferData(bufferTarget, getDirectBuffer(size, (ShortBuffer) buffer), bufferUsageToGL[usage]);
		} else if (buffer instanceof FloatBuffer) {
			GL15.glBufferData(bufferTarget, getDirectBuffer(size, (FloatBuffer) buffer), bufferUsageToGL[usage]);
		} else {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public void bindBuffer(int buffer) {
		GL15.glBindBuffer(bufferTarget, buffer);
	}
}
