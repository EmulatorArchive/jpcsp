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
package jpcsp.graphics.RE.buffer;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import jpcsp.util.Utilities;

/**
 * @author gid15
 *
 */
public class BufferManagerDefault extends BaseBufferManager {
	protected int currentBufferId;

	@Override
	protected void init() {
		super.init();
		currentBufferId = 12345678;
	}

	@Override
	public boolean useVBO() {
		return false;
	}

	@Override
	public int genBuffer(int target, int type, int size, int usage) {
		int totalSize = size * sizeOfType[type];
		ByteBuffer byteBuffer = createByteBuffer(totalSize);

		int buffer = currentBufferId++;

		buffers.put(buffer, new BufferInfo(buffer, byteBuffer, type, size));

		return buffer;
	}

	@Override
	public void bindBuffer(int target, int buffer) {
		// Not supported
	}

	@Override
	public void setColorPointer(int buffer, int size, int type, int stride, int offset) {
		BufferInfo bufferInfo = buffers.get(buffer);
		re.setColorPointer(size, type, stride, bufferInfo.getBufferSize() - offset, bufferInfo.getBufferPosition(offset));
	}

	@Override
	public void setNormalPointer(int buffer, int type, int stride, int offset) {
		BufferInfo bufferInfo = buffers.get(buffer);
		re.setNormalPointer(type, stride, bufferInfo.getBufferSize() - offset, bufferInfo.getBufferPosition(offset));
	}

	@Override
	public void setTexCoordPointer(int buffer, int size, int type, int stride, int offset) {
		BufferInfo bufferInfo = buffers.get(buffer);
		re.setTexCoordPointer(size, type, stride, bufferInfo.getBufferSize() - offset, bufferInfo.getBufferPosition(offset));
	}

	@Override
	public void setVertexAttribPointer(int buffer, int id, int size, int type, boolean normalized, int stride, int offset) {
		BufferInfo bufferInfo = buffers.get(buffer);
		re.setVertexAttribPointer(id, size, type, normalized, stride, bufferInfo.getBufferSize() - offset, bufferInfo.getBufferPosition(offset));
	}

	@Override
	public void setVertexPointer(int buffer, int size, int type, int stride, int offset) {
		BufferInfo bufferInfo = buffers.get(buffer);
		re.setVertexPointer(size, type, stride, bufferInfo.getBufferSize() - offset, bufferInfo.getBufferPosition(offset));
	}

	@Override
	public void setWeightPointer(int buffer, int size, int type, int stride, int offset) {
		BufferInfo bufferInfo = buffers.get(buffer);
		re.setWeightPointer(size, type, stride, bufferInfo.getBufferSize() - offset, bufferInfo.getBufferPosition(offset));
	}

	@Override
	public void setBufferData(int target, int buffer, int size, Buffer data, int usage) {
		BufferInfo bufferInfo = buffers.get(buffer);
		if (bufferInfo.byteBuffer != data) {
			bufferInfo.byteBuffer.clear();
			Utilities.putBuffer(bufferInfo.byteBuffer, data, ByteOrder.nativeOrder());
		} else {
			bufferInfo.byteBuffer.position(0);
		}
	}

	@Override
	public void setBufferSubData(int target, int buffer, int offset, int size, Buffer data, int usage) {
		BufferInfo bufferInfo = buffers.get(buffer);
		if (bufferInfo.byteBuffer != data) {
			bufferInfo.byteBuffer.clear();
			Utilities.putBuffer(bufferInfo.byteBuffer, data, ByteOrder.nativeOrder());
		} else {
			bufferInfo.byteBuffer.position(0);
		}
	}
}
