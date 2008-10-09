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
package jpcsp.graphics;

import jpcsp.Memory;

// Based on soywiz/pspemulator
public class VertexInfo {
    // vtype
    private int transform2D; // for logging purposes (got moved into VideoEngine.java)
    public int skinningWeightCount;
    public int morphingVertexCount;
    public int texture;
    public int color;
    public int normal;
    public int position;
    public int weight;
    public int index;

    // vaddr, iaddr
    public int ptr_vertex;
    public int ptr_index;

    // other data
    public int vertexSize;

    private static int[] size_mapping = new int[] { 0, 1, 2, 4 };

    private static String[] texture_info = new String[] {
        null, "GU_TEXTURE_8BIT", "GU_TEXTURE_16BIT", "GU_TEXTURE_32BITF"
    };
    private static String[] color_info = new String[] {
        null, "GU_COLOR_UNK2", "GU_COLOR_UNK3", "GU_COLOR_UNK4",
        "GU_COLOR_5650", "GU_COLOR_5551", "GU_COLOR_4444", "GU_COLOR_8888"
    };
    private static String[] normal_info = new String[] {
        null, "GU_NORMAL_8BIT", "GU_NORMAL_16BIT", "GU_NORMAL_32BITF"
    };
    private static String[] vertex_info = new String[] {
        null, "GU_VERTEX_8BIT", "GU_VERTEX_16BIT", "GU_VERTEX_32BITF"
    };
    private static String[] weight_info = new String[] {
        null, "GU_WEIGHT_8BIT", "GU_WEIGHT_16BIT", "GU_WEIGHT_32BITF"
    };
    private static String[] index_info = new String[] {
        null, "GU_INDEX_8BIT",  "GU_INDEX_16BIT", "GU_INDEX_UNK3"
    };
    private static String[] transform_info = new String[] {
        "GU_TRANSFORM_3D", "GU_TRANSFORM_2D"
    };

    public void processType(int param) {
        texture             = (param >>  0) & 0x3;
        color               = (param >>  2) & 0x7;
        normal              = (param >>  5) & 0x3;
        position            = (param >>  7) & 0x3;
        weight              = (param >>  9) & 0x3;
        index               = (param >> 11) & 0x3;
        skinningWeightCount = ((param >> 14) & 0x7) + 1;
        morphingVertexCount = ((param >> 18) & 0x7) + 1;
        transform2D         = (param >> 23) & 0x1;

        vertexSize = 0;
        vertexSize += size_mapping[weight] * skinningWeightCount;
        vertexSize += (color != 0) ? ((color == 7) ? 4 : 2) : 0;
        vertexSize += size_mapping[texture] * 2;
        vertexSize += size_mapping[position] * 3;
        vertexSize += size_mapping[normal] * 3;

        // 32-bit align
        // messes up lines.pbp demo
        //vertexSize = (vertexSize + 3) & ~3;
    }

    public int getAddress(Memory mem, int i) {
        if (ptr_index != 0) {
            int addr = ptr_index + i * index;
            switch(index) {
                case 1: i = mem.read8(addr); break;
                case 2: i = mem.read16(addr); break;
                case 4: i = mem.read32(addr); break;
            }
        }

        return ptr_vertex + i * vertexSize;
    }

    public VertexState readVertex(Memory mem, int addr) {
        VertexState v = new VertexState();

		for (int i = 0; i < skinningWeightCount; ++i) {
			switch (weight) {
			case 1:
				v.boneWeights[i] = mem.read8(addr); addr += 1;
				break;
			case 2:
				v.boneWeights[i] = mem.read16(addr); addr += 2;
				break;
			case 3:
				v.boneWeights[i] = Float.intBitsToFloat(mem.read32(addr)); addr += 4;
				break;
			}
			//System.err.println(String.format("Weight %.1f %.1f %.1f %.1f %.1f %.1f %.1f %.1f", v.boneWeights[0], v.boneWeights[1], v.boneWeights[2], v.boneWeights[3], v.boneWeights[4], v.boneWeights[5], v.boneWeights[6], v.boneWeights[7]));
		}

        switch(texture) {
            case 1:
                v.u = mem.read8(addr); addr += 1;
                v.v = mem.read8(addr); addr += 1;
                VideoEngine.log.warn("texture type 1 " + v.u + ", " + v.v + "");
                break;
            case 2:
                v.u = mem.read16(addr); addr += 2;
                v.v = mem.read16(addr); addr += 2;
                VideoEngine.log.warn("texture type 2 " + v.u + ", " + v.v + "");
                break;
            case 3:
                v.u = Float.intBitsToFloat(mem.read32(addr)); addr += 4;
                v.v = Float.intBitsToFloat(mem.read32(addr)); addr += 4;
                break;
        }

        switch(color) {
            case 1: case 2: case 3: VideoEngine.log.warn("unimplemented color type " + color); addr += 1; break;
            case 4: case 5: VideoEngine.log.warn("unimplemented color type " + color); addr += 2; break;

            case 6: { // GU_COLOR_4444
                int packed = mem.read16(addr); addr += 2;
                v.r = (float)((packed      ) & 0xf) / 15;
                v.g = (float)((packed >>  4) & 0xf) / 15;
                v.b = (float)((packed >>  8) & 0xf) / 15;
                v.a = (float)((packed >> 12) & 0xf) / 15;
                VideoEngine.log.warn("color type " + color);
                break;
            }

            case 7: { // GU_COLOR_8888
                int packed = mem.read32(addr); addr += 4;
                v.r = (float)((packed      ) & 0xff) / 255;
                v.g = (float)((packed >>  8) & 0xff) / 255;
                v.b = (float)((packed >> 16) & 0xff) / 255;
                v.a = (float)((packed >> 24) & 0xff) / 255;
                break;
            }
        }

        switch(normal) {
            case 1:
                v.nx = mem.read8(addr); addr += 1;
                v.ny = mem.read8(addr); addr += 1;
                v.nz = mem.read8(addr); addr += 1;
                VideoEngine.log.warn("normal type 1 " + v.nx + ", " + v.ny + ", " + v.nz + "");
                break;
            case 2:
                v.nx = mem.read16(addr); addr += 2;
                v.ny = mem.read16(addr); addr += 2;
                v.nz = mem.read16(addr); addr += 2;
                VideoEngine.log.warn("normal type 2 " + v.nx + ", " + v.ny + ", " + v.nz + "");
                break;
            case 3:
                v.nx = Float.intBitsToFloat(mem.read32(addr)); addr += 4;
                v.ny = Float.intBitsToFloat(mem.read32(addr)); addr += 4;
                v.nz = Float.intBitsToFloat(mem.read32(addr)); addr += 4;
                break;
		}

		switch (position) {
			case 1:
				v.px = mem.read8(addr); addr += 1;
				v.py = mem.read8(addr); addr += 1;
				v.pz = mem.read8(addr); addr += 1;
                VideoEngine.log.trace("vertex type 1 " + v.px + ", " + v.py + ", " + v.pz + "");
				break;
			case 2:
				v.px = mem.read16(addr); addr += 2;
				v.py = mem.read16(addr); addr += 2;
				v.pz = mem.read16(addr); addr += 2;
                VideoEngine.log.trace("vertex type 2 " + v.px + ", " + v.py + ", " + v.pz + "");
				break;
			case 3: // GU_VERTEX_32BITF
				v.px = Float.intBitsToFloat(mem.read32(addr)); addr += 4;
				v.py = Float.intBitsToFloat(mem.read32(addr)); addr += 4;
				v.pz = Float.intBitsToFloat(mem.read32(addr)); addr += 4;
				break;
		}

        return v;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();

        if (texture_info[texture] != null)
            sb.append(texture_info[texture] + "|");
        if (color_info[color] != null)
            sb.append(color_info[color] + "|");
        if (normal_info[normal] != null)
            sb.append(normal_info[normal] + "|");
        if (vertex_info[position] != null)
            sb.append(vertex_info[position] + "|");
        if (weight_info[weight] != null)
            sb.append(weight_info[weight] + "|");
        if (index_info[index] != null)
            sb.append(index_info[index] + "|");
        if (transform_info[transform2D] != null)
            sb.append(transform_info[transform2D]);

        sb.append(" size=" + vertexSize);
        return sb.toString();
    }
}
