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

import jpcsp.graphics.Uniforms;

/**
 * @author gid15
 * 
 * RenderingEngine Proxy class removing redundant calls.
 * E.g. calls setting multiple times the same value,
 * or calls with an invalid parameter (e.g. for unused shader uniforms).
 * This class implements no rendering logic, it just skips unnecessary calls.
 */
public class StateProxy extends BaseRenderingEngineProxy {
	protected boolean[] flags;
	protected float[][] matrix;
	protected static final int RE_BONES_MATRIX = 4;
	protected static final int matrix4Size = 4 * 4;
	protected int maxUniformId;
	protected int[] uniformInt;
	protected int[][] uniformIntArray;
	protected float[] uniformFloat;
	protected float[][] uniformFloatArray;
	protected boolean[] clientState;
	protected boolean[] vertexAttribArray;
	protected int textureMipmapMinFilter;
	protected int textureMipmapMagFilter;
	protected int textureMipmapMinLevel;
	protected int textureWrapModeS;
	protected int textureWrapModeT;
	protected boolean colorMaskRed;
	protected boolean colorMaskGreen;
	protected boolean colorMaskBlue;
	protected boolean colorMaskAlpha;
	protected int[] colorMask;
	protected boolean depthMask;
	protected int textureFunc;
	protected boolean textureFuncAlpha;
	protected boolean textureFuncColorDouble;
	protected boolean frontFace;
	protected int stencilFunc;
	protected int stencilFuncRef;
	protected int stencilFuncMask;
	protected int stencilOpFail;
	protected int stencilOpZFail;
	protected int stencilOpZPass;
	protected int depthFunc;
	protected int bindTexture = -1;

	public StateProxy(IRenderingEngine proxy) {
		super(proxy);
		init();
	}

	protected void init() {
		flags = new boolean[RE_NUMBER_FLAGS];

		maxUniformId = 0;
		for (Uniforms uniform : Uniforms.values()) {
			int id = uniform.getId();
			if (id > maxUniformId) {
				maxUniformId = id;
			}
		}
		int numberUniforms = maxUniformId + 1;

		uniformInt = new int[numberUniforms];
		uniformFloat = new float[numberUniforms];
		uniformIntArray = new int[numberUniforms][];
		uniformFloatArray = new float[numberUniforms][];

		matrix = new float[RE_BONES_MATRIX + 1][];
		matrix[GU_PROJECTION] = new float[matrix4Size];
		matrix[GU_VIEW] = new float[matrix4Size];
		matrix[GU_MODEL] = new float[matrix4Size];
		matrix[GU_TEXTURE] = new float[matrix4Size];
		matrix[RE_BONES_MATRIX] = new float[8 * matrix4Size];

		clientState = new boolean[4];
		vertexAttribArray = new boolean[numberUniforms];
		colorMask = new int[4];
	}

	@Override
	public void startDisplay() {
		// The following properties are lost when starting a new display
		for (int i = 0; i < clientState.length; i++) {
			clientState[i] = false;
		}

		for (int i = 0; i < flags.length; i++) {
			flags[i] = true;
		}

		System.arraycopy(identityMatrix, 0, matrix[GU_PROJECTION], 0, matrix4Size);
		System.arraycopy(identityMatrix, 0, matrix[GU_VIEW], 0, matrix4Size);
		System.arraycopy(identityMatrix, 0, matrix[GU_MODEL], 0, matrix4Size);
		System.arraycopy(identityMatrix, 0, matrix[GU_TEXTURE], 0, matrix4Size);
		textureMipmapMinFilter = -1;
		textureMipmapMagFilter = -1;
		textureMipmapMinLevel = 0;
		textureWrapModeS = -1;
		textureWrapModeT = -1;
		colorMaskRed = true;
		colorMaskGreen = true;
		colorMaskBlue = true;
		colorMaskAlpha = true;
		depthMask = true;
		textureFunc = -1;
		textureFuncAlpha = true;
		textureFuncColorDouble = false;
		frontFace = true;
		stencilFunc = -1;
		stencilFuncRef = -1;
		stencilFuncMask = -1;
		stencilOpFail = -1;
		stencilOpZFail = -1;
		stencilOpZPass = -1;
		depthFunc = -1;
		bindTexture = -1;

		super.startDisplay();
	}

	@Override
	public void disableFlag(int flag) {
		if (flags[flag]) {
			super.disableFlag(flag);
			flags[flag] = false;
		}
	}

	@Override
	public void enableFlag(int flag) {
		if (!flags[flag]) {
			super.enableFlag(flag);
			flags[flag] = true;
		}
	}

	@Override
	public void setUniform(int id, int value) {
		// An unused uniform as an id == -1
		if (id >= 0 && id <= maxUniformId) {
			if (uniformInt[id] != value) {
				super.setUniform(id, value);
				uniformInt[id] = value;
			}
		}
	}

	@Override
	public void setUniform(int id, float value) {
		if (id >= 0 && id <= maxUniformId) {
			if (uniformFloat[id] != value) {
				super.setUniform(id, value);
				uniformFloat[id] = value;
			}
		}
	}

	@Override
	public void setUniform(int id, int value1, int value2) {
		if (id >= 0 && id <= maxUniformId) {
			int[] oldValues = uniformIntArray[id];
			if (oldValues == null || oldValues.length != 2) {
				super.setUniform(id, value1, value2);
				uniformIntArray[id] = new int[] { value1, value2 };
			} else {
				if (oldValues[0] != value1 || oldValues[1] != value2) {
					super.setUniform(id, value1, value2);
					oldValues[0] = value1;
					oldValues[1] = value2;
				}
			}
		}
	}

	@Override
	public void setUniform3(int id, int[] values) {
		if (id >= 0 && id <= maxUniformId) {
			int[] oldValues = uniformIntArray[id];
			if (oldValues == null || oldValues.length != 3) {
				super.setUniform3(id, values);
				oldValues = new int[3];
				oldValues[0] = values[0];
				oldValues[1] = values[1];
				oldValues[2] = values[2];
				uniformIntArray[id] = oldValues;
			} else if (oldValues[0] != values[0] || oldValues[1] != values[1] || oldValues[2] != values[2]) {
				super.setUniform3(id, values);
				oldValues[0] = values[0];
				oldValues[1] = values[1];
				oldValues[2] = values[2];
			}
		}
	}

	@Override
	public void setUniform4(int id, int[] values) {
		if (id >= 0 && id <= maxUniformId) {
			int[] oldValues = uniformIntArray[id];
			if (oldValues == null || oldValues.length != 4) {
				super.setUniform4(id, values);
				oldValues = new int[4];
				oldValues[0] = values[0];
				oldValues[1] = values[1];
				oldValues[2] = values[2];
				oldValues[3] = values[3];
				uniformIntArray[id] = oldValues;
			} else if (oldValues[0] != values[0] || oldValues[1] != values[1] || oldValues[2] != values[2] || oldValues[3] != values[3]) {
				super.setUniform4(id, values);
				oldValues[0] = values[0];
				oldValues[1] = values[1];
				oldValues[2] = values[2];
				oldValues[3] = values[3];
			}
		}
	}

	@Override
	public void setUniformMatrix4(int id, int count, float[] values) {
		if (id >= 0 && id <= maxUniformId && count > 0) {
			float[] oldValues = uniformFloatArray[id];
			int length = count * matrix4Size;
			if (oldValues == null || oldValues.length < length) {
				super.setUniformMatrix4(id, count, values);
				oldValues = new float[length];
				System.arraycopy(values, 0, oldValues, 0, length);
				uniformFloatArray[id] = oldValues;
			} else {
				boolean differ = false;
				for (int i = 0; i < length; i++) {
					if (oldValues[i] != values[i]) {
						differ = true;
						break;
					}
				}

				if (differ) {
					super.setUniformMatrix4(id, count, values);
					System.arraycopy(values, 0, oldValues, 0, length);
				}
			}
		}
	}

	protected int matrixFirstUpdated(int id, float[] values) {
		if (values == null) {
			values = identityMatrix;
		}

		float[] oldValues = matrix[id];
		for (int i = 0; i < values.length; i++) {
			if (values[i] != oldValues[i]) {
				// Update the remaining values
				System.arraycopy(values, i, oldValues, i, values.length - i);
				return i;
			}
		}

		return values.length;
	}

	protected int matrixLastUpdated(int id, float[] values, int length) {
		float[] oldValues = matrix[id];

		if (values == null) {
			values = identityMatrix;
		}

		for (int i = length - 1; i >= 0; i--) {
			if (oldValues[i] != values[i]) {
				// Update the remaining values
				System.arraycopy(values, 0, oldValues, 0, i + 1);
				return i;
			}
		}

		return 0;
	}

	protected boolean isIdentityMatrix(float[] values) {
		if (values == null) {
			return true;
		}

		if (values.length != identityMatrix.length) {
			return false;
		}

		for (int i = 0; i < identityMatrix.length; i++) {
			if (values[i] != identityMatrix[i]) {
				return false;
			}
		}

		return true;
	}

	@Override
	public void setMatrix(int type, float[] values) {
		if (matrixFirstUpdated(type, values) < matrix4Size) {
			if (isIdentityMatrix(values)) {
				// Identity Matrix is identified by the special value "null"
				super.setMatrix(type, null);
			} else {
				super.setMatrix(type, values);
			}
		}
	}

	@Override
	public void disableClientState(int type) {
		if (clientState[type]) {
			super.disableClientState(type);
			clientState[type] = false;
		}
	}

	@Override
	public void enableClientState(int type) {
		// enableClientState(RE_VERTEX) cannot be cached: it is required each time
		// by OpenGL and seems to trigger the correct Vertex generation.
		if (!clientState[type] || type == RE_VERTEX) {
			super.enableClientState(type);
			clientState[type] = true;
		}
	}

	@Override
	public void disableVertexAttribArray(int id) {
		if (id >= 0 && id <= maxUniformId) {
			if (vertexAttribArray[id]) {
				super.disableVertexAttribArray(id);
				vertexAttribArray[id] = false;
			}
		}
	}

	@Override
	public void enableVertexAttribArray(int id) {
		if (id >= 0 && id <= maxUniformId) {
			if (!vertexAttribArray[id]) {
				super.enableVertexAttribArray(id);
				vertexAttribArray[id] = true;
			}
		}
	}

	@Override
	public void setColorMask(boolean redWriteEnabled, boolean greenWriteEnabled, boolean blueWriteEnabled, boolean alphaWriteEnabled) {
		if (redWriteEnabled != colorMaskRed || greenWriteEnabled != colorMaskGreen || blueWriteEnabled != colorMaskBlue || alphaWriteEnabled != colorMaskAlpha) {
			super.setColorMask(redWriteEnabled, greenWriteEnabled, blueWriteEnabled, alphaWriteEnabled);
			colorMaskRed = redWriteEnabled;
			colorMaskGreen = greenWriteEnabled;
			colorMaskBlue = blueWriteEnabled;
			colorMaskAlpha = alphaWriteEnabled;
			colorMask[0] = redWriteEnabled ? 0x00 : 0xFF;
			colorMask[1] = greenWriteEnabled ? 0x00 : 0xFF;
			colorMask[2] = blueWriteEnabled ? 0x00 : 0xFF;
			colorMask[3] = alphaWriteEnabled ? 0x00 : 0xFF;
		}
	}

	@Override
	public void setColorMask(int redMask, int greenMask, int blueMask, int alphaMask) {
		if (redMask != colorMask[0] || greenMask != colorMask[1] || blueMask != colorMask[2] || alphaMask != colorMask[3]) {
			super.setColorMask(redMask, greenMask, blueMask, alphaMask);
			colorMask[0] = redMask;
			colorMask[1] = greenMask;
			colorMask[2] = blueMask;
			colorMask[3] = alphaMask;
		}
	}

	@Override
	public void setDepthMask(boolean depthWriteEnabled) {
		if (depthWriteEnabled != depthMask) {
			super.setDepthMask(depthWriteEnabled);
			depthMask = depthWriteEnabled;
		}
	}

	@Override
	public void setFrontFace(boolean cw) {
		if (cw != frontFace) {
			super.setFrontFace(cw);
			frontFace = cw;
		}
	}

	@Override
	public void setTextureFunc(int func, boolean alphaUsed, boolean colorDoubled) {
		if (func != textureFunc || alphaUsed != textureFuncAlpha || colorDoubled != textureFuncColorDouble) {
			super.setTextureFunc(func, alphaUsed, colorDoubled);
			textureFunc = func;
			textureFuncAlpha = alphaUsed;
			textureFuncColorDouble = colorDoubled;
		}
	}

	@Override
	public void setTextureMipmapMinFilter(int filter) {
		if (filter != textureMipmapMinFilter) {
			super.setTextureMipmapMinFilter(filter);
			textureMipmapMinFilter = filter;
		}
	}

	@Override
	public void setTextureMipmapMagFilter(int filter) {
		if (filter != textureMipmapMagFilter) {
			super.setTextureMipmapMagFilter(filter);
			textureMipmapMagFilter = filter;
		}
	}

	@Override
	public void setTextureMipmapMinLevel(int level) {
		if (level != textureMipmapMinLevel) {
			super.setTextureMipmapMinLevel(level);
			textureMipmapMinLevel = level;
		}
	}

	@Override
	public void setTextureWrapMode(int s, int t) {
		if (s != textureWrapModeS || t != textureWrapModeT) {
			super.setTextureWrapMode(s, t);
			textureWrapModeS = s;
			textureWrapModeT = t;
		}
	}

	@Override
	public void bindTexture(int texture) {
		if (texture != bindTexture) {
			super.bindTexture(texture);
			bindTexture = texture;
			// Binding a new texture can change the OpenGL texture wrap mode
			textureWrapModeS = -1;
			textureWrapModeT = -1;
		}
	}

	@Override
	public void setDepthFunc(int func) {
		if (func != depthFunc) {
			super.setDepthFunc(func);
			depthFunc = func;
		}
	}

	@Override
	public void setStencilFunc(int func, int ref, int mask) {
		if (func != stencilFunc || ref != stencilFuncRef || mask != stencilFuncMask) {
			super.setStencilFunc(func, ref, mask);
			stencilFunc = func;
			stencilFuncRef = ref;
			stencilFuncMask = mask;
		}
	}

	@Override
	public void setStencilOp(int fail, int zfail, int zpass) {
		if (fail != stencilOpFail || zfail != stencilOpZFail || zpass != stencilOpZPass) {
			super.setStencilOp(fail, zfail, zpass);
			stencilOpFail = fail;
			stencilOpZFail = zfail;
			stencilOpZPass = zpass;
		}
	}

	@Override
	public void deleteTexture(int texture) {
		// When deleting the current texture binding, it is reset to 0
		if (texture == bindTexture) {
			bindTexture = 0;
		}
		super.deleteTexture(texture);
	}
}
