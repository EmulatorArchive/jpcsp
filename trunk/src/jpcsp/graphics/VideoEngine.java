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

import static jpcsp.HLE.modules150.sceGe_user.PSP_GE_LIST_CANCEL_DONE;
import static jpcsp.HLE.modules150.sceGe_user.PSP_GE_LIST_DONE;
import static jpcsp.HLE.modules150.sceGe_user.PSP_GE_LIST_DRAWING;
import static jpcsp.HLE.modules150.sceGe_user.PSP_GE_LIST_END_REACHED;
import static jpcsp.HLE.modules150.sceGe_user.PSP_GE_LIST_STALL_REACHED;
import static jpcsp.HLE.modules150.sceGe_user.PSP_GE_MATRIX_BONE0;
import static jpcsp.HLE.modules150.sceGe_user.PSP_GE_MATRIX_BONE1;
import static jpcsp.HLE.modules150.sceGe_user.PSP_GE_MATRIX_BONE2;
import static jpcsp.HLE.modules150.sceGe_user.PSP_GE_MATRIX_BONE3;
import static jpcsp.HLE.modules150.sceGe_user.PSP_GE_MATRIX_BONE4;
import static jpcsp.HLE.modules150.sceGe_user.PSP_GE_MATRIX_BONE5;
import static jpcsp.HLE.modules150.sceGe_user.PSP_GE_MATRIX_BONE6;
import static jpcsp.HLE.modules150.sceGe_user.PSP_GE_MATRIX_BONE7;
import static jpcsp.HLE.modules150.sceGe_user.PSP_GE_MATRIX_PROJECTION;
import static jpcsp.HLE.modules150.sceGe_user.PSP_GE_MATRIX_TEXGEN;
import static jpcsp.HLE.modules150.sceGe_user.PSP_GE_MATRIX_VIEW;
import static jpcsp.HLE.modules150.sceGe_user.PSP_GE_MATRIX_WORLD;
import static jpcsp.graphics.GeCommands.*;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

import javax.media.opengl.GL;
import javax.media.opengl.GLException;

import jpcsp.Emulator;
import jpcsp.Memory;
import jpcsp.MemoryMap;
import jpcsp.Settings;
import jpcsp.State;
import jpcsp.HLE.Modules;
import jpcsp.HLE.kernel.types.IAction;
import jpcsp.HLE.kernel.types.PspGeList;
import jpcsp.HLE.kernel.types.pspGeContext;
import jpcsp.HLE.modules.sceDisplay;
import jpcsp.graphics.RE.IRenderingEngine;
import jpcsp.graphics.RE.RenderingEngineFactory;
import jpcsp.graphics.capture.CaptureManager;
import jpcsp.graphics.textures.Texture;
import jpcsp.graphics.textures.TextureCache;
import jpcsp.memory.IMemoryReader;
import jpcsp.memory.MemoryReader;
import jpcsp.util.DurationStatistics;
import jpcsp.util.Utilities;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.sun.opengl.util.BufferUtil;

//
// Ideas for Optimization:
// - compile GE lists (or part of it) into OpenGL display list (glNewList/glCallList).
//   For example, immutable subroutines called using CALL could be compiled into a display list.
//   A first run of the game using a profiler option could be used to detect which parts
//   are immutable. This information could be stored in a file for subsequent runs and
//   used as hints for the next runs.
// - Unswizzle textures in shader (is this possible?)
// - interpret all the vertex type in a shader instead of transforming everything in
//   GL_FLOAT in VertexInfo.readVertex(). Mapping integer values to [0..2] or [-1..1]
//   could be performed in vertex shader.
// - implement PRIM_SPRITES in shader (instead of duplicating vertex in Java)
//
public class VideoEngine {

    public static final int NUM_LIGHTS = 4;
    private final int[] prim_mapping = new int[]{GL.GL_POINTS, GL.GL_LINES, GL.GL_LINE_STRIP, GL.GL_TRIANGLES, GL.GL_TRIANGLE_STRIP, GL.GL_TRIANGLE_FAN, GL.GL_QUADS};
    public final static String[] psm_names = new String[]{
        "PSM_5650",
        "PSM_5551",
        "PSM_4444",
        "PSM_8888",
        "PSM_4BIT_INDEXED",
        "PSM_8BIT_INDEXED",
        "PSM_16BIT_INDEXED",
        "PSM_32BIT_INDEXED",
        "PSM_DXT1",
        "PSM_DXT3",
        "PSM_DXT5"
    };
    public final static String[] logical_ops_names = new String[]{
        "LOP_CLEAR",
        "LOP_AND",
        "LOP_REVERSE_AND",
        "LOP_COPY",
        "LOP_INVERTED_AND",
        "LOP_NO_OPERATION",
        "LOP_EXLUSIVE_OR",
        "LOP_OR",
        "LOP_NEGATED_OR",
        "LOP_EQUIVALENCE",
        "LOP_INVERTED",
        "LOP_REVERSE_OR",
        "LOP_INVERTED_COPY",
        "LOP_INVERTED_OR",
        "LOP_NEGATED_AND",
        "LOP_SET"
    };
    private static final int[] texturetype_mapping = {
        GL.GL_UNSIGNED_SHORT_5_6_5_REV,
        GL.GL_UNSIGNED_SHORT_1_5_5_5_REV,
        GL.GL_UNSIGNED_SHORT_4_4_4_4_REV,
        GL.GL_UNSIGNED_BYTE,};
    private static final int[] textureByteAlignmentMapping = {2, 2, 2, 4};
    private static VideoEngine instance;
    private GL gl;
    private sceDisplay display;
    private IRenderingEngine re;
    public static Logger log = Logger.getLogger("ge");
    public static final boolean useTextureCache = true;
    private boolean useVertexCache = false;
    private static GeCommands helper;
    private VertexInfo vinfo = new VertexInfo();
    private VertexInfoReader vertexInfoReader = new VertexInfoReader();
    private static final char SPACE = ' ';
    private DurationStatistics statistics;
    private DurationStatistics vertexStatistics = new DurationStatistics("Vertex");
    private DurationStatistics waitSignalStatistics = new DurationStatistics("Wait for GE Signal completion");
    private DurationStatistics waitStallStatistics = new DurationStatistics("Wait on stall");
    private DurationStatistics textureCacheLookupStatistics = new DurationStatistics("Lookup in TextureCache");
    private DurationStatistics vertexCacheLookupStatistics = new DurationStatistics("Lookup in VertexCache");
    private DurationStatistics[] commandStatistics;
    private boolean openGL1_2;
    private boolean openGL1_5;
    private int errorCount;
    private static final int maxErrorCount = 5; // Abort list processing when detecting more errors
    private boolean isLogTraceEnabled;
    private boolean isLogDebugEnabled;
    private boolean isLogInfoEnabled;
    private boolean isLogWarnEnabled;
    private int primCount;
    private int base;
    // The value of baseOffset has to be added (not ORed) to the base value.
    // baseOffset is updated by the ORIGIN_ADDR and OFFSET_ADDR commands,
    // and both commands share the same value field.
    private int baseOffset;
    private int fbp, fbw; // frame buffer pointer and width
    private int zbp, zbw; // depth buffer pointer and width
    private int psm; // pixel format
    private int region_x1, region_y1, region_x2, region_y2;
    private int region_width, region_height; // derived
    private int scissor_x1, scissor_y1, scissor_x2, scissor_y2;
    private int scissor_width, scissor_height; // derived
    private int offset_x, offset_y;
    private int viewport_width, viewport_height; // derived from xyscale
    private int viewport_cx, viewport_cy;
    private boolean viewportChanged;
    private float[] proj_uploaded_matrix = new float[4 * 4];
    private MatrixUpload projectionMatrixUpload;
    private float[] texture_uploaded_matrix = new float[4 * 4];
    private MatrixUpload textureMatrixUpload;
    private float[] model_uploaded_matrix = new float[4 * 4];
    private MatrixUpload modelMatrixUpload;
    private float[] view_uploaded_matrix = new float[4 * 4];
    private MatrixUpload viewMatrixUpload;
    private int boneMatrixIndex;
    private float[][] bone_uploaded_matrix = new float[8][4 * 3];
    private float[] boneMatrixForShader = new float[8 * 4 * 4]; // Linearized version of bone_uploaded_matrix
    private int boneMatrixForShaderUpdatedMatrix; // number of updated matrix
    private float[] morph_weight = new float[8];
    private float[] tex_envmap_matrix = new float[4 * 4];
    private float[][] light_pos = new float[NUM_LIGHTS][4];
    private float[][] light_dir = new float[NUM_LIGHTS][3];
    private int[] light_enabled = new int[NUM_LIGHTS];
    private int[] light_type = new int[NUM_LIGHTS];
    private int[] light_kind = new int[NUM_LIGHTS];
    private float[][] lightAmbientColor = new float[NUM_LIGHTS][4];
    private float[][] lightDiffuseColor = new float[NUM_LIGHTS][4];
    private float[][] lightSpecularColor = new float[NUM_LIGHTS][4];
    private static final float[] blackColor = new float[]{0, 0, 0, 0};
    private float[] spotLightExponent = new float[NUM_LIGHTS];
    private float[] spotLightCutoff = new float[NUM_LIGHTS];
    private boolean lightingChanged;
    private float[] fog_color = new float[4];
    private float fog_far = 0.0f, fog_dist = 0.0f;
    private float nearZ = 0.0f, farZ = 0.0f, zscale, zpos;
    private int mat_flags = 0;
    private float[] mat_ambient = new float[4];
    private float[] mat_diffuse = new float[4];
    private float[] mat_specular = new float[4];
    private float[] mat_emissive = new float[4];
    private boolean materialChanged;
    private float[] ambient_light = new float[4];
    private int texture_storage, texture_num_mip_maps;
    private boolean texture_swizzle;
    private int[] texture_base_pointer = new int[8];
    private int[] texture_width = new int[8];
    private int[] texture_height = new int[8];
    private int[] texture_buffer_width = new int[8];
    private boolean textureChanged;
    private int tex_min_filter = GL.GL_NEAREST;
    private int tex_mag_filter = GL.GL_NEAREST;
    private int tex_mipmap_mode;
    private float tex_mipmap_bias;
    private int tex_mipmap_bias_int;
    private boolean mipmapShareClut;
    private float tex_translate_x = 0.f, tex_translate_y = 0.f;
    private float tex_scale_x = 1.f, tex_scale_y = 1.f;
    private float[] tex_env_color = new float[4];
    private int tex_clut_addr;
    private int tex_clut_num_blocks;
    private int tex_clut_mode, tex_clut_shift, tex_clut_mask, tex_clut_start;
    private int tex_wrap_s = TWRAP_WRAP_MODE_REPEAT, tex_wrap_t = TWRAP_WRAP_MODE_REPEAT;
    private int tex_shade_u = 0;
    private int tex_shade_v = 0;
    private int patch_div_s;
    private int patch_div_t;
    private int patch_prim;
    private int[] patch_prim_types = {GL.GL_TRIANGLE_STRIP, GL.GL_LINE_STRIP, GL.GL_POINTS};
    private boolean tsync_wait = false;
    private float tslope_level;
    private boolean clutIsDirty;
    private int transform_mode;
    private int textureTx_sourceAddress;
    private int textureTx_sourceLineWidth;
    private int textureTx_destinationAddress;
    private int textureTx_destinationLineWidth;
    private int textureTx_width;
    private int textureTx_height;
    private int textureTx_sx;
    private int textureTx_sy;
    private int textureTx_dx;
    private int textureTx_dy;
    private int textureTx_pixelSize;
    private boolean usingTRXKICK;
    private int maxSpriteHeight;
    private float[] dfix_color = new float[4];
    private float[] sfix_color = new float[4];
    private int blend_src;
    private int blend_dst;
    private boolean blendChanged;
    private boolean clearMode;
    private int clearModeDepthFunc;
    private float[] clearModeRgbScale = new float[1];
    private int[] clearModeTextureEnvMode = new int[1];
    private int depthFunc;
    private boolean depthChanged;
    private int[] dither_matrix = new int[16];
    private boolean takeConditionalJump;
    private int colorMask[];
    // opengl needed information/buffers
    private int[] gl_texture_id = new int[1];
    private int[] tmp_texture_buffer32 = new int[1024 * 1024];
    private short[] tmp_texture_buffer16 = new short[1024 * 1024];
    private int[] clut_buffer32 = new int[4096];
    private short[] clut_buffer16 = new short[4096];
    private int tex_map_mode = TMAP_TEXTURE_MAP_MODE_TEXTURE_COORDIATES_UV;
    private int tex_proj_map_mode = TMAP_TEXTURE_PROJECTION_MODE_POSITION;
    private boolean listHasEnded;
    private PspGeList currentList; // The currently executing list
    private boolean useVBO = true;
    private int[] vboBufferId = new int[1];
    private static final int vboBufferSize = 2 * 1024 * 1024 * BufferUtil.SIZEOF_FLOAT;
    private ByteBuffer vboBuffer = ByteBuffer.allocateDirect(vboBufferSize).order(ByteOrder.nativeOrder());
    private FloatBuffer vboFloatBuffer = vboBuffer.asFloatBuffer();
    private static final int nativeBufferSize = vboBufferSize;
    private ByteBuffer nativeBuffer = ByteBuffer.allocateDirect(nativeBufferSize).order(ByteOrder.LITTLE_ENDIAN);
    private boolean useShaders = true;
    private int shaderProgram;
    private boolean useSkinningShaders = true; // Use Shaders for Skinning?
    private int shaderAttribWeights1;
    private int shaderAttribWeights2;
    private int shaderCtestFunc;
    private int[] shaderCtestRef = { 0, 0, 0 };
    private int[] shaderCtestMsk = { 0, 0, 0 };
    private boolean glQueryAvailable;
    private int bboxQueryId;
    float[][] bboxVertices;
    private ConcurrentLinkedQueue<PspGeList> drawListQueue;
    private boolean somethingDisplayed;
    private boolean geBufChanged;
    private IAction hleAction;
    private HashMap<Integer, Integer> currentCMDValues;
    private static int contextBitCount = 0;
    private final List<EnableDisableFlag> flags = new LinkedList<EnableDisableFlag>();
    private final EnableDisableFlag alphaTestFlag = new EnableDisableFlag("GU_ALPHA_TEST", IRenderingEngine.GU_ALPHA_TEST, false);
    private final EnableDisableFlag depthTestFlag = new EnableDisableFlag("GU_DEPTH_TEST", IRenderingEngine.GU_DEPTH_TEST, false);
    private final EnableDisableFlag scissorTestFlag = new EnableDisableFlag("GU_SCISSOR_TEST", IRenderingEngine.GU_SCISSOR_TEST);
    private final EnableDisableFlag stencilTestFlag = new EnableDisableFlag("GU_STENCIL_TEST", IRenderingEngine.GU_STENCIL_TEST, false);
    private final EnableDisableFlag blendFlag = new EnableDisableFlag("GU_BLEND", IRenderingEngine.GU_BLEND, false);
    private final EnableDisableFlag cullFaceFlag = new EnableDisableFlag("GU_CULL_FACE", IRenderingEngine.GU_CULL_FACE, false);
    private final EnableDisableFlag ditherFlag = new EnableDisableFlag("GU_DITHER", IRenderingEngine.GU_DITHER);
    private final EnableDisableFlag fogFlag = new EnableDisableFlag("GU_FOG", IRenderingEngine.GU_FOG, false);
    private final EnableDisableFlag clipPlanesFlag = new EnableDisableFlag("GU_CLIP_PLANES", IRenderingEngine.GU_CLIP_PLANES);
    private final EnableDisableFlag textureFlag = new EnableDisableFlag("GU_TEXTURE_2D", IRenderingEngine.GU_TEXTURE_2D, false);
    private final EnableDisableFlag lightingFlag = new EnableDisableFlag("GU_LIGHTING", IRenderingEngine.GU_LIGHTING);
    private final EnableDisableFlag[] lightFlags = new EnableDisableFlag[]{
        new EnableDisableFlag("GU_LIGHT0", IRenderingEngine.GU_LIGHT0),
        new EnableDisableFlag("GU_LIGHT1", IRenderingEngine.GU_LIGHT1),
        new EnableDisableFlag("GU_LIGHT2", IRenderingEngine.GU_LIGHT2),
        new EnableDisableFlag("GU_LIGHT3", IRenderingEngine.GU_LIGHT3)
    };
    private final EnableDisableFlag lineSmoothFlag = new EnableDisableFlag("GU_LINE_SMOOTH", IRenderingEngine.GU_LINE_SMOOTH);
    private final EnableDisableFlag patchCullFaceFlag = new EnableDisableFlag("GU_PATCH_CULL_FACE", IRenderingEngine.GU_PATCH_CULL_FACE);
    private final EnableDisableFlag colorTestFlag = new EnableDisableFlag("GU_COLOR_TEST", IRenderingEngine.GU_COLOR_TEST, false);
    private final EnableDisableFlag colorLogicOpFlag = new EnableDisableFlag("GU_COLOR_LOGIC_OP", IRenderingEngine.GU_COLOR_LOGIC_OP, false);
    private final EnableDisableFlag faceNormalReverseFlag = new EnableDisableFlag("GU_FACE_NORMAL_REVERSE", IRenderingEngine.GU_FACE_NORMAL_REVERSE);
    private final EnableDisableFlag patchFaceFlag = new EnableDisableFlag("GU_PATCH_FACE", IRenderingEngine.GU_PATCH_FACE);

    private class EnableDisableFlag {
        private boolean enabled;
        private final int reFlag;
        private final String name;
        private final boolean validInClearMode;
        private int contextBit;

        public EnableDisableFlag(String name, int reFlag) {
            this.name = name;
            this.reFlag = reFlag;
            validInClearMode = true;
            init();
        }

        public EnableDisableFlag(String name, int reFlag, boolean validInClearMode) {
            this.name = name;
            this.reFlag = reFlag;
            this.validInClearMode = validInClearMode;
            init();
        }

        private void init() {
            enabled = false;
            contextBit = contextBitCount++;
            flags.add(this);
        }

        public boolean isEnabled() {
            return enabled;
        }

        public int isEnabledInt() {
            return enabled ? 1 : 0;
        }

        public boolean setEnabled(int enabledInt) {
            return setEnabled(enabledInt != 0);
        }

        /**
         * Enable/Disable the flag. Update the flag in RenderingEngine.
         * Check if the flag can be changed when in CLEAR mode.
         *
         * @param enabled        new flag value
         * @return               true if the flag value has really been changed
         *                       false if the flag value has not been changed
         */
        public boolean setEnabled(boolean enabled) {
            boolean changed = false;

            // Check if the flag can be changed when in CLEAR mode
            if (validInClearMode || !clearMode) {
                this.enabled = enabled;
                changed = true;

                // Update the flag in RenderingEngine
                if (enabled) {
                	re.enableFlag(reFlag);
                } else {
                	re.disableFlag(reFlag);
                }

                if (isLogDebugEnabled) {
                    log.debug(String.format("sceGu%s(%s)", enabled ? "Enable" : "Disable", name));
                }
            }

            return changed;
        }

        public int save(int bits) {
            return bits | (1 << contextBit);
        }

        public void restore(int bits) {
            setEnabled((bits & (1 << contextBit)) != 0);
        }
    }

    private static class MatrixUpload {

        private final float[] matrix;
        private final int matrixWidth;
        private final int matrixHeight;
        private int currentX;
        private int currentY;
        private boolean changed;

        public MatrixUpload(float[] matrix, int matrixWidth, int matrixHeight) {
            changed = true;
            this.matrix = matrix;
            this.matrixWidth = matrixWidth;
            this.matrixHeight = matrixHeight;

            for (int y = 0; y < 4; y++) {
                for (int x = 0; x < 4; x++) {
                    matrix[y * 4 + x] = (x == y ? 1 : 0);
                }
            }
        }

        public void startUpload(int startIndex) {
            currentX = startIndex % matrixWidth;
            currentY = startIndex / matrixWidth;
        }

        public boolean uploadValue(float value) {
            boolean done = false;

            if (currentY >= matrixHeight) {
                VideoEngine.getInstance().error(String.format("Ignored Matrix upload value (X=%d,Y=%d,idx=%08X)", currentX, currentY, currentY * matrixWidth + currentX));
                return true;
            }

            int index = currentY * 4 + currentX;
            if (matrix[index] != value) {
                matrix[index] = value;
                changed = true;
            }
            currentX++;
            if (currentX >= matrixWidth) {
                currentX = 0;
                currentY++;
                if (currentY >= matrixHeight) {
                    done = true;
                }
            }

            return done;
        }

        public boolean isChanged() {
            return changed;
        }

        public void setChanged(boolean changed) {
            this.changed = changed;
        }

        public boolean isIdentity() {
            return matrix[0] == 1 && matrix[1] == 0 && matrix[2] == 0 && matrix[3] == 0
                    && matrix[4] == 0 && matrix[5] == 1 && matrix[6] == 0 && matrix[7] == 0
                    && matrix[8] == 0 && matrix[9] == 0 && matrix[10] == 1 && matrix[11] == 0
                    && matrix[12] == 0 && matrix[13] == 0 && matrix[14] == 0 && matrix[15] == 1;
        }
    }

    private static void log(String msg) {
        log.debug(msg);
    }

    public static VideoEngine getInstance() {
        if (instance == null) {
            helper = new GeCommands();
            instance = new VideoEngine();
        }
        return instance;
    }

    private VideoEngine() {
        modelMatrixUpload = new MatrixUpload(model_uploaded_matrix, 3, 4);
        viewMatrixUpload = new MatrixUpload(view_uploaded_matrix, 3, 4);
        textureMatrixUpload = new MatrixUpload(texture_uploaded_matrix, 3, 4);
        projectionMatrixUpload = new MatrixUpload(proj_uploaded_matrix, 4, 4);
        tex_envmap_matrix[0] = tex_envmap_matrix[5] = tex_envmap_matrix[10] = tex_envmap_matrix[15] = 1.f;
        light_pos[0][3] = light_pos[1][3] = light_pos[2][3] = light_pos[3][3] = 1.f;
        morph_weight[0] = 1.f;
        tex_mipmap_mode = TBIAS_MODE_AUTO;
        tex_mipmap_bias = 0.f;
        tex_mipmap_bias_int = 0;
        takeConditionalJump = false;
        colorMask = new int[] { 0x00, 0x00, 0x00, 0x00 };
        mipmapShareClut = true;
        boneMatrixForShaderUpdatedMatrix = 8;
        base = 0;
        baseOffset = 0;

        // Force a first load of these values
        light_type[0] = light_type[1] = light_type[2] = light_type[3] = -1;
        light_kind[0] = light_kind[1] = light_kind[2] = light_kind[3] = -1;
        lightSpecularColor[0][0] = lightSpecularColor[1][0] = lightSpecularColor[2][0] = lightSpecularColor[3][0] = -1;
        light_enabled[0] = light_enabled[1] = light_enabled[2] = light_enabled[3] = -1;

        statistics = new DurationStatistics("VideoEngine Statistics");
        commandStatistics = new DurationStatistics[256];
        for (int i = 0; i < commandStatistics.length; i++) {
            commandStatistics[i] = new DurationStatistics(String.format("%-11s", helper.getCommandString(i)));
        }

        drawListQueue = new ConcurrentLinkedQueue<PspGeList>();

        bboxVertices = new float[8][3];
        for (int i = 0; i < 8; i++) {
            bboxVertices[i] = new float[3];
        }

        currentCMDValues = new HashMap<Integer, Integer>();
    }

    /** Called from pspge module */
    public void pushDrawList(PspGeList list) {
        drawListQueue.add(list);
    }

    /** Called from pspge module */
    public void pushDrawListHead(PspGeList list) {
        // The ConcurrentLinkedQueue type doesn't allow adding
        // objects directly at the head of the queue.

        // This function creates a new array using the given list as it's head
        // and constructs a new ConcurrentLinkedQueue based on it.
        // The actual drawListQueue is then replaced by this new one.
        int arraySize = drawListQueue.size();

        if (arraySize > 0) {
            PspGeList[] array = drawListQueue.toArray(new PspGeList[arraySize]);

            ConcurrentLinkedQueue<PspGeList> newQueue = new ConcurrentLinkedQueue<PspGeList>();
            PspGeList[] newArray = new PspGeList[arraySize + 1];

            newArray[0] = list;
            for (int i = 0; i < arraySize; i++) {
                newArray[i + 1] = array[i];
                newQueue.add(newArray[i]);
            }

            drawListQueue = newQueue;
        } else {    // If the queue is empty.
            drawListQueue.add(list);
        }
    }

    public boolean hasDrawLists() {
        return !drawListQueue.isEmpty();
    }

    public boolean hasDrawList(int listAddr) {
        if (currentList != null && currentList.list_addr == listAddr) {
            return true;
        }

        for (PspGeList list : drawListQueue) {
            if (list != null && list.list_addr == listAddr) {
                return true;
            }
        }

        return false;
    }

    public PspGeList getLastDrawList() {
        PspGeList lastList = null;
        for (PspGeList list : drawListQueue) {
            if (list != null) {
                lastList = list;
            }
        }

        if (lastList == null) {
            lastList = currentList;
        }

        return lastList;
    }

    public GL getGL() {
    	return gl;
    }

    public void setGL(GL gl) {
        this.gl = gl;
        display = Modules.sceDisplayModule;

        String openGLVersion = getOpenGLVersion(gl);
        openGL1_2 = openGLVersion.compareTo("1.2") >= 0;
        openGL1_5 = openGLVersion.compareTo("1.5") >= 0;

        useVBO = !Settings.getInstance().readBool("emu.disablevbo")
                && gl.isFunctionAvailable("glGenBuffersARB")
                && gl.isFunctionAvailable("glBindBufferARB")
                && gl.isFunctionAvailable("glBufferDataARB")
                && gl.isFunctionAvailable("glDeleteBuffersARB")
                && gl.isFunctionAvailable("glGenBuffers");

        useShaders = Settings.getInstance().readBool("emu.useshaders")
                && gl.isFunctionAvailable("glCreateShader")
                && gl.isFunctionAvailable("glShaderSource")
                && gl.isFunctionAvailable("glCompileShader")
                && gl.isFunctionAvailable("glCreateProgram")
                && gl.isFunctionAvailable("glAttachShader")
                && gl.isFunctionAvailable("glLinkProgram")
                && gl.isFunctionAvailable("glValidateProgram")
                && gl.isFunctionAvailable("glUseProgram");

        if (!useShaders) {
            useSkinningShaders = false;
        }

        VideoEngine.log.info("OpenGL version: " + openGLVersion);

        if (useShaders) {
            if (useSkinningShaders) {
                VideoEngine.log.info("Using shaders with Skinning");
            } else {
                VideoEngine.log.info("Using shaders");
            }
            loadShaders(gl);
        }

        if (useVBO) {
            VideoEngine.log.info("Using VBO");
            buildVBO(gl);
        } else {
            // VertexCache is relying on VBO
            useVertexCache = false;
        }

        glQueryAvailable = gl.isFunctionAvailable("glGenQueries")
                && gl.isFunctionAvailable("glBeginQuery")
                && gl.isFunctionAvailable("glEndQuery");
        if (glQueryAvailable) {
            int[] queryIds = new int[1];
            gl.glGenQueries(1, queryIds, 0);
            bboxQueryId = queryIds[0];
        }

        re = RenderingEngineFactory.getRenderingEngine(useShaders);
    }

    private void buildVBO(GL gl) {
        glGenBuffers(gl, 1, vboBufferId, 0);
        glBindBuffer();
        glBufferData(GL.GL_ARRAY_BUFFER, vboBufferSize, vboFloatBuffer, GL.GL_STREAM_DRAW);
    }

    public void glGenBuffers(GL gl, int length, int[] ids, int offset) {
        if (useVBO) {
            if (openGL1_5) {
                gl.glGenBuffers(length, ids, offset);
            } else {
                gl.glGenBuffersARB(length, ids, offset);
            }
        }
    }

    public void glDeleteBuffers(GL gl, int length, int[] ids, int offset) {
        if (useVBO) {
            if (openGL1_5) {
                gl.glDeleteBuffers(length, ids, offset);
            } else {
                gl.glDeleteBuffersARB(length, ids, offset);
            }
        }
    }

    public void glBufferData(int target, int size, Buffer buffer, int type) {
        if (useVBO) {
            if (openGL1_5) {
                gl.glBufferData(target, size, buffer, type);
            } else {
                gl.glBufferDataARB(target, size, buffer, type);
            }
        }
    }

    private void loadShaders(GL gl) {
        int v = gl.glCreateShader(GL.GL_VERTEX_SHADER);
        int f = gl.glCreateShader(GL.GL_FRAGMENT_SHADER);

        String[] srcArray = new String[1];
        final String shaderVert = "/jpcsp/graphics/shader.vert";
        try {
            srcArray[0] = Utilities.toString(getClass().getResourceAsStream(shaderVert), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        gl.glShaderSource(v, 1, srcArray, null, 0);
        gl.glCompileShader(v);
        printShaderInfoLog(gl, v);
        final String shaderFrag = "/jpcsp/graphics/shader.frag";
        try {
            srcArray[0] = Utilities.toString(getClass().getResourceAsStream(shaderFrag), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        gl.glShaderSource(f, 1, srcArray, null, 0);
        gl.glCompileShader(f);
        printShaderInfoLog(gl, f);

        shaderProgram = gl.glCreateProgram();
        gl.glAttachShader(shaderProgram, v);
        gl.glAttachShader(shaderProgram, f);
        gl.glLinkProgram(shaderProgram);
        printProgramInfoLog(gl, shaderProgram);
        gl.glValidateProgram(shaderProgram);
        printProgramInfoLog(gl, shaderProgram);

        for (Uniforms uniform : Uniforms.values()) {
            uniform.allocateId(gl, shaderProgram);
        }

        if (useSkinningShaders) {
            shaderAttribWeights1 = gl.glGetAttribLocation(shaderProgram, "psp_weights1");
            shaderAttribWeights2 = gl.glGetAttribLocation(shaderProgram, "psp_weights2");
        }
    }

    void printShaderInfoLog(GL gl, int obj) {
        int[] infologLength = new int[1];
        int[] charsWritten = new int[1];
        byte[] infoLog;

        gl.glGetShaderiv(obj, GL.GL_INFO_LOG_LENGTH, infologLength, 0);

        if (infologLength[0] > 1) {
            infoLog = new byte[infologLength[0]];
            gl.glGetShaderInfoLog(obj, infologLength[0], charsWritten, 0, infoLog, 0);
            log.error("Shader info log : " + new String(infoLog));
        }
    }

    void printProgramInfoLog(GL gl, int obj) {
        int[] infologLength = new int[1];
        int[] charsWritten = new int[1];
        byte[] infoLog;

        gl.glGetProgramiv(obj, GL.GL_INFO_LOG_LENGTH, infologLength, 0);

        if (infologLength[0] > 1) {
            infoLog = new byte[infologLength[0]];
            gl.glGetProgramInfoLog(obj, infologLength[0], charsWritten, 0, infoLog, 0);
            log.error("Program info log : " + new String(infoLog));
        }
    }

    public static void exit() {
        if (instance != null) {
            log.info(instance.statistics.toString());
            Arrays.sort(instance.commandStatistics);
            int numberCommands = 20;
            log.info(numberCommands + " most time intensive Video commands:");
            for (int i = 0; i < numberCommands; i++) {
                VideoEngine.log.info("    " + instance.commandStatistics[i].toString());
            }
            log.info(instance.vertexStatistics);
            log.info(instance.waitSignalStatistics);
            log.info(instance.waitStallStatistics);
            log.info(instance.textureCacheLookupStatistics);
            log.info(instance.vertexCacheLookupStatistics);
        }
    }

    public static DurationStatistics getStatistics() {
        if (instance == null) {
            return null;
        }

        return instance.statistics;
    }

    /** call from GL thread
     * @return true if an update was made
     */
    public boolean update() {
        int listCount = drawListQueue.size();
        PspGeList list = drawListQueue.poll();
        if (list == null) {
            return false;
        }

        if (useShaders) {
            gl.glUseProgram(shaderProgram);
        }

        startUpdate();

        if (State.captureGeNextFrame) {
            CaptureManager.startCapture("capture.bin", list);
        }

        if (State.replayGeNextFrame) {
            // Load the replay list into drawListQueue
            CaptureManager.startReplay("capture.bin");

            // Hijack the current list with the replay list
            // TODO this is assuming there is only 1 list in drawListQueue at this point, only the last list is the replay list
            PspGeList replayList = drawListQueue.poll();
            replayList.id = list.id;
            replayList.blockedThreadIds.clear();
            replayList.blockedThreadIds.addAll(list.blockedThreadIds);
            list = replayList;
        }

        // Draw only as many lists as currently available in the drawListQueue.
        // Some game add automatically a new list to the queue when the current
        // list is finishing.
        do {
            executeList(list);
            listCount--;
            if (listCount <= 0) {
                break;
            }
            list = drawListQueue.poll();
        } while (list != null);

        if (useShaders) {
            gl.glUseProgram(0);
        }

        if (State.captureGeNextFrame) {
            // Can't end capture until we get a sceDisplaySetFrameBuf after the list has executed
            CaptureManager.markListExecuted();
        }

        if (State.replayGeNextFrame) {
            CaptureManager.endReplay();
            State.replayGeNextFrame = false;
        }

        endUpdate();

        return true;
    }

    private void logLevelUpdated() {
        isLogTraceEnabled = log.isTraceEnabled();
        isLogDebugEnabled = log.isDebugEnabled();
        isLogInfoEnabled = log.isInfoEnabled();
        isLogWarnEnabled = log.isEnabledFor(Level.WARN);
    }

    public void setLogLevel(Level level) {
        log.setLevel(level);
        logLevelUpdated();
    }

    /**
     * The memory used by GE has been updated or changed.
     * Update the caches so that they see these changes.
     */
    private void memoryForGEUpdated() {
        if (useTextureCache) {
            TextureCache.getInstance().resetTextureAlreadyHashed();
        }
        if (useVertexCache) {
            VertexCache.getInstance().resetVertexAlreadyHashed();
        }
    }

    private void startUpdate() {
        statistics.start();

        logLevelUpdated();
        memoryForGEUpdated();
        somethingDisplayed = false;
        textureChanged = true;
        projectionMatrixUpload.setChanged(true);
        modelMatrixUpload.setChanged(true);
        viewMatrixUpload.setChanged(true);
        textureMatrixUpload.setChanged(true);
        clutIsDirty = true;
        lightingChanged = true;
        blendChanged = true;
        viewportChanged = true;
        depthChanged = true;
        materialChanged = true;
        errorCount = 0;
        usingTRXKICK = false;
        maxSpriteHeight = 0;
        primCount = 0;

        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glMatrixMode(GL.GL_TEXTURE);
        gl.glPushMatrix();
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glPushMatrix();
    }

    private void endUpdate() {
        gl.glMatrixMode(GL.GL_TEXTURE);
        gl.glPopMatrix();
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPopMatrix();
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glPopMatrix();

        if (useVertexCache) {
            if (primCount > VertexCache.cacheMaxSize) {
                log.warn(String.format("VertexCache size (%d) too small to execute %d PRIM commands", VertexCache.cacheMaxSize, primCount));
            }
        }

        statistics.end();
    }

    public void error(String message) {
        errorCount++;
        log.error(message);
        if (errorCount >= maxErrorCount) {
            if (tryToFallback()) {
                log.error("Aborting current list processing due to too many errors");
            }
        }
    }

    private boolean tryToFallback() {
        boolean abort = false;

        if (!currentList.isStackEmpty()) {
            // When have some CALLs on the stack, try to return from the last CALL
            int oldPc = currentList.pc;
            currentList.ret();
            int newPc = currentList.pc;
            if (isLogDebugEnabled) {
                log(String.format("tryToFallback old PC: 0x%08X, new PC: 0x%08X", oldPc, newPc));
            }
        } else {
            // Finish this list
            currentList.finishList();
            listHasEnded = true;
            abort = true;
        }

        return abort;
    }

    private void checkCurrentListPc() {
        Memory mem = Memory.getInstance();
        while (!mem.isAddressGood(currentList.pc)) {
            if (!mem.isIgnoreInvalidMemoryAccess()) {
                error("Reading GE list from invalid address 0x" + Integer.toHexString(currentList.pc));
                break;
            }
			// Ignoring memory read errors.
			// Try to fall back and continue the list processing.
			log.warn("Reading GE list from invalid address 0x" + Integer.toHexString(currentList.pc));
			if (tryToFallback()) {
			    break;
			}
        }
    }

    private void executeHleAction() {
        if (hleAction != null) {
            hleAction.execute();
            hleAction = null;
        }
    }

    // call from GL thread
    // There is an issue here with Emulator.pause
    // - We want to stop on errors
    // - But user may also press pause button
    //   - Either continue drawing to the end of the list (bad if the list contains an infinite loop)
    //   - Or we want to be able to restart drawing when the user presses the run button
    private void executeList(PspGeList list) {
        currentList = list;
        listHasEnded = false;
        currentList.status = PSP_GE_LIST_DRAWING;

        if (isLogDebugEnabled) {
            log("executeList " + list);
        }

        executeHleAction();

        IMemoryReader memoryReader = MemoryReader.getMemoryReader(currentList.pc, 4);
        int memoryReaderPc = currentList.pc;
        int waitForSyncCount = 0;
        while (!listHasEnded && (!Emulator.pause || State.captureGeNextFrame)) {
            if (currentList.isPaused()) {
                if (currentList.isFinished()) {
                    listHasEnded = true;
                    break;
                }
				waitSignalStatistics.start();
				if (isLogDebugEnabled) {
				    log.debug(String.format("SIGNAL / END reached, waiting for Sync"));
				}
				currentList.status = PSP_GE_LIST_END_REACHED;
				if (!currentList.waitForSync(10)) {
				    if (isLogDebugEnabled) {
				        log.debug("Wait for sync while END reached");
				    }
				    waitForSyncCount++;

				    // Waiting maximum 100 * 10ms (= 1 second) on an END command.
				    // After this timeout, abort the list.
				    if (waitForSyncCount > 100) {
				        error(String.format("Waiting too long on an END command, aborting the list %s", currentList));
				    }
				} else {
				    waitForSyncCount = 0;
				}

				executeHleAction();
				if (!currentList.isPaused()) {
				    currentList.status = PSP_GE_LIST_DRAWING;
				}
				waitSignalStatistics.end();
            } else if (currentList.isStallReached()) {
                waitStallStatistics.start();
                if (isLogDebugEnabled) {
                    log.debug(String.format("Stall address 0x%08X reached, waiting for Sync", currentList.pc));
                }
                currentList.status = PSP_GE_LIST_STALL_REACHED;
                if (!currentList.waitForSync(10)) {
                    if (isLogDebugEnabled) {
                        log.debug("Wait for sync while stall reached");
                    }
                    waitForSyncCount++;

                    // Waiting maximum 100 * 10ms (= 1 second) on a stall address.
                    // After this timeout, abort the list.
                    //
                    // When the stall address is at the very beginning of the list
                    // (i.e. the list has just been enqueued, but the stall has not yet been updated),
                    // allow waiting for a longer time (the CPU might be busy
                    // compiling a huge CodeBlock on the first call).
                    // This avoids aborting the first list enqueued.
                    int maxStallCount = (currentList.pc != currentList.list_addr ? 100 : 400);

                    if (waitForSyncCount > maxStallCount) {
                        error(String.format("Waiting too long on stall address 0x%08X, aborting the list %s", currentList.pc, currentList));
                    }
                } else {
                    waitForSyncCount = 0;
                }
                executeHleAction();
                if (!currentList.isStallReached()) {
                    currentList.status = PSP_GE_LIST_DRAWING;
                }
                waitStallStatistics.end();
            } else {
                if (currentList.pc != memoryReaderPc) {
                    // The currentList.pc is no longer reading in sequence
                    // and has jumped to a next location, get a new memory reader.
                    checkCurrentListPc();
                    if (listHasEnded || Emulator.pause) {
                        break;
                    }
                    memoryReader = MemoryReader.getMemoryReader(currentList.pc, 4);
                }
                int ins = memoryReader.readNext();
                currentList.pc += 4;
                memoryReaderPc = currentList.pc;

                executeCommand(ins);
            }
        }

        if (Emulator.pause && !listHasEnded) {
            VideoEngine.log.info("Emulator paused - cancelling current list id=" + currentList.id);
            currentList.status = PSP_GE_LIST_CANCEL_DONE;
        }

        // let DONE take priority over STALL_REACHED
        if (listHasEnded) {
            setTsync(false);
            currentList.status = PSP_GE_LIST_END_REACHED;

            // Tested on PSP:
            // A list is only DONE after a combination of FINISH + END.
            if (currentList.isEnded()) {
                currentList.status = PSP_GE_LIST_DONE;
            }
        }

        if (list.isDone()) {
        	Modules.sceGe_userModule.hleGeListSyncDone(list);
        }

        executeHleAction();

        currentList = null;
    }

    public PspGeList getCurrentList() {
        return currentList;
    }

    public float[] getMatrix(int mtxtype) {
        float resmtx[] = new float[4 * 4];
        switch (mtxtype) {
            case PSP_GE_MATRIX_BONE0:
                resmtx = bone_uploaded_matrix[0];
                break;
            case PSP_GE_MATRIX_BONE1:
                resmtx = bone_uploaded_matrix[1];
                break;
            case PSP_GE_MATRIX_BONE2:
                resmtx = bone_uploaded_matrix[2];
                break;
            case PSP_GE_MATRIX_BONE3:
                resmtx = bone_uploaded_matrix[3];
                break;
            case PSP_GE_MATRIX_BONE4:
                resmtx = bone_uploaded_matrix[4];
                break;
            case PSP_GE_MATRIX_BONE5:
                resmtx = bone_uploaded_matrix[5];
                break;
            case PSP_GE_MATRIX_BONE6:
                resmtx = bone_uploaded_matrix[6];
                break;
            case PSP_GE_MATRIX_BONE7:
                resmtx = bone_uploaded_matrix[7];
                break;
            case PSP_GE_MATRIX_WORLD:
                resmtx = model_uploaded_matrix;
                break;
            case PSP_GE_MATRIX_VIEW:
                resmtx = view_uploaded_matrix;
                break;
            case PSP_GE_MATRIX_PROJECTION:
                resmtx = proj_uploaded_matrix;
                break;
            case PSP_GE_MATRIX_TEXGEN:
                resmtx = texture_uploaded_matrix;
                break;
        }

        return resmtx;
    }

    public int getCommandValue(int cmd) {
        return currentCMDValues.get(cmd);
    }

    public String commandToString(int cmd) {
        return GeCommands.getInstance().getCommandString(cmd);
    }

    public static int command(int instruction) {
        return (instruction >>> 24);
    }

    private static int intArgument(int instruction) {
        return (instruction & 0x00FFFFFF);
    }

    private static float floatArgument(int normalArgument) {
        return Float.intBitsToFloat(normalArgument << 8);
    }

    private int getStencilOp(int pspOP) {
        switch (pspOP) {
            case SOP_KEEP_STENCIL_VALUE:
                return GL.GL_KEEP;

            case SOP_ZERO_STENCIL_VALUE:
                return GL.GL_ZERO;

            case SOP_REPLACE_STENCIL_VALUE:
                return GL.GL_REPLACE;

            case SOP_INVERT_STENCIL_VALUE:
                return GL.GL_INVERT;

            case SOP_INCREMENT_STENCIL_VALUE:
                return GL.GL_INCR;

            case SOP_DECREMENT_STENCIL_VALUE:
                return GL.GL_DECR;
        }

        log("UNKNOWN stencil op " + pspOP);
        return GL.GL_KEEP;
    }

    private int getBlendFix(float[] fix_color) {
        if (fix_color[0] == 0 && fix_color[1] == 0 && fix_color[2] == 0) {
            return IRenderingEngine.GU_FIX_BLACK;
        } else if (fix_color[0] == 1 && fix_color[1] == 1 && fix_color[2] == 1) {
            return IRenderingEngine.GU_FIX_WHITE;
        } else {
            return IRenderingEngine.GU_FIX_BLEND_COLOR;
        }
    }

    private float[] getBlendColor(int gl_blend_src, int gl_blend_dst) {
        float[] blend_color = null;
        if (gl_blend_src == IRenderingEngine.GU_FIX_BLEND_COLOR) {
            blend_color = sfix_color;
            if (gl_blend_dst == IRenderingEngine.GU_FIX_BLEND_COLOR) {
                if (sfix_color[0] != dfix_color[0]
                        || sfix_color[1] != dfix_color[1]
                        || sfix_color[2] != dfix_color[2]
                        || sfix_color[3] != dfix_color[3]) {
                    log.warn("UNSUPPORTED: Both different SFIX and DFIX are not supported");
                }
            }
        } else if (gl_blend_dst == IRenderingEngine.GU_FIX_BLEND_COLOR) {
            blend_color = dfix_color;
        }

        return blend_color;
    }

    // hack partially based on pspplayer
    private void setBlendFunc() {
    	int reBlendSrc = blend_src;
    	if (blend_src < 0 || blend_src > 10) {
            error("Unhandled alpha blend src used " + blend_src);
            reBlendSrc = 0;
    	} else if (blend_src == 10) { // GU_FIX
    		reBlendSrc = getBlendFix(sfix_color);
    	}

    	int reBlendDst = blend_dst;
    	if (blend_dst < 0 || blend_dst > 10) {
            error("Unhandled alpha blend dst used " + blend_dst);
            reBlendDst = 0;
    	} else if (blend_dst == 10) { // GU_FIX
    		reBlendDst = getBlendFix(dfix_color);
    	}

        float[] blend_color = getBlendColor(blend_src, blend_dst);
        if (blend_color != null) {
        	re.setBlendColor(blend_color);
        }

        re.setBlendFunc(reBlendSrc, reBlendDst);
    }

    private int getClutAddr(int level, int clutNumEntries, int clutEntrySize) {
        return tex_clut_addr + tex_clut_start * clutEntrySize;
    }

    private void readClut() {
        if (!clutIsDirty) {
            return;
        }

        // Texture using clut?
        if (texture_storage >= TPSM_PIXEL_STORAGE_MODE_4BIT_INDEXED && texture_storage <= TPSM_PIXEL_STORAGE_MODE_32BIT_INDEXED) {
            if (tex_clut_mode == CMODE_FORMAT_32BIT_ABGR8888) {
                readClut32(0);
            } else {
                readClut16(0);
            }
        }
    }

    private short[] readClut16(int level) {
        int clutNumEntries = tex_clut_num_blocks * 16;

        // Update the clut_buffer only if some clut parameters have been changed
        // since last update.
        if (clutIsDirty) {
            IMemoryReader memoryReader = MemoryReader.getMemoryReader(getClutAddr(level, clutNumEntries, 2), (clutNumEntries - tex_clut_start) * 2, 2);
            for (int i = tex_clut_start; i < clutNumEntries; i++) {
                clut_buffer16[i] = (short) memoryReader.readNext();
            }
            clutIsDirty = false;
        }

        if (State.captureGeNextFrame) {
            log.info("Capture readClut16");
            CaptureManager.captureRAM(tex_clut_addr, clutNumEntries * 2);
        }

        return clut_buffer16;
    }

    private int[] readClut32(int level) {
        int clutNumEntries = tex_clut_num_blocks * 8;

        // Update the clut_buffer only if some clut parameters have been changed
        // since last update.
        if (clutIsDirty) {
            IMemoryReader memoryReader = MemoryReader.getMemoryReader(getClutAddr(level, clutNumEntries, 4), (clutNumEntries - tex_clut_start) * 4, 4);
            for (int i = tex_clut_start; i < clutNumEntries; i++) {
                clut_buffer32[i] = memoryReader.readNext();
            }
            clutIsDirty = false;
        }

        if (State.captureGeNextFrame) {
            log.info("Capture readClut32");
            CaptureManager.captureRAM(tex_clut_addr, clutNumEntries * 4);
        }

        return clut_buffer32;
    }

    private int getClutIndex(int index) {
        return ((tex_clut_start + index) >> tex_clut_shift) & tex_clut_mask;
    }

    // UnSwizzling based on pspplayer
    private Buffer unswizzleTextureFromMemory(int texaddr, int bytesPerPixel, int level) {
        int rowWidth = (bytesPerPixel > 0) ? (texture_buffer_width[level] * bytesPerPixel) : (texture_buffer_width[level] / 2);
        int pitch = (rowWidth - 16) / 4;
        int bxc = rowWidth / 16;
        int byc = (texture_height[level] + 7) / 8;

        int ydest = 0;

        IMemoryReader memoryReader = MemoryReader.getMemoryReader(texaddr, 4);
        for (int by = 0; by < byc; by++) {
            int xdest = ydest;
            for (int bx = 0; bx < bxc; bx++) {
                int dest = xdest;
                for (int n = 0; n < 8; n++) {
                    tmp_texture_buffer32[dest] = memoryReader.readNext();
                    tmp_texture_buffer32[dest + 1] = memoryReader.readNext();
                    tmp_texture_buffer32[dest + 2] = memoryReader.readNext();
                    tmp_texture_buffer32[dest + 3] = memoryReader.readNext();

                    dest += pitch + 4;
                }
                xdest += (16 / 4);
            }
            ydest += (rowWidth * 8) / 4;
        }

        if (State.captureGeNextFrame) {
            log.info("Capture unswizzleTextureFromMemory");
            CaptureManager.captureRAM(texaddr, rowWidth * texture_height[level]);
        }

        return IntBuffer.wrap(tmp_texture_buffer32);
    }

    private String getArgumentLog(int normalArgument) {
        if (normalArgument == 0) {
            return "(0)"; // a very common case...
        }

        return String.format("(hex=%08X,int=%d,float=%f)", normalArgument, normalArgument, floatArgument(normalArgument));
    }

    public void setTsync(boolean status) {
        tsync_wait = status;
    }

    public void executeCommand(int instruction) {
        int normalArgument = intArgument(instruction);
        // Compute floatArgument only on demand, most commands do not use it.
        //float floatArgument = floatArgument(instruction);

        int command = command(instruction);
        currentCMDValues.put(command, normalArgument);
        if (isLogInfoEnabled) {
            commandStatistics[command].start();
        }
        switch (command) {
            case NOP:
                if (isLogDebugEnabled) {
                    log(helper.getCommandString(NOP));
                }

                // Check if we are not reading from an invalid memory region.
                // Abort the list if this is the case.
                // This is only done in the NOP command to not impact performance.
                checkCurrentListPc();
                break;

            case VADDR:
                vinfo.ptr_vertex = currentList.getAddress(normalArgument);
                if (isLogDebugEnabled) {
                    log(helper.getCommandString(VADDR) + " " + String.format("%08x", vinfo.ptr_vertex));
                }
                break;

            case IADDR:
                vinfo.ptr_index = currentList.getAddress(normalArgument);
                if (isLogDebugEnabled) {
                    log(helper.getCommandString(IADDR) + " " + String.format("%08x", vinfo.ptr_index));
                }
                break;

            case PRIM:
                executeCommandPRIM(normalArgument);
                break;

            case BEZIER:
                int ucount = normalArgument & 0xFF;
                int vcount = (normalArgument >> 8) & 0xFF;
                if (isLogDebugEnabled) {
                    log(helper.getCommandString(BEZIER) + " ucount=" + ucount + ", vcount=" + vcount);
                }

                updateGeBuf();
                loadTexture();

                drawBezier(ucount, vcount);
                break;

            case SPLINE: {
                // Number of control points.
                int sp_ucount = normalArgument & 0xFF;
                int sp_vcount = (normalArgument >> 8) & 0xFF;
                // Knot types.
                int sp_utype = (normalArgument >> 16) & 0x3;
                int sp_vtype = (normalArgument >> 18) & 0x3;

                if (isLogDebugEnabled) {
                    log(helper.getCommandString(SPLINE) + " sp_ucount=" + sp_ucount + ", sp_vcount=" + sp_vcount +
                            " sp_utype=" + sp_utype + ", sp_vtype=" + sp_vtype);
                }

                updateGeBuf();
                loadTexture();

                drawSpline(sp_ucount, sp_vcount, sp_utype, sp_vtype);
                break;
            }

            case BBOX:
                executeCommandBBOX(normalArgument);
                break;

            case JUMP: {
                int oldPc = currentList.pc;
                currentList.jump(normalArgument);
                int newPc = currentList.pc;
                if (isLogDebugEnabled) {
                    log(String.format("%s old PC: 0x%08X, new PC: 0x%08X", helper.getCommandString(JUMP), oldPc, newPc));
                }
                break;
            }

            case BJUMP:
                executeCommandBJUMP(normalArgument);
                break;

            case CALL: {
                int oldPc = currentList.pc;
                currentList.call(normalArgument);
                int newPc = currentList.pc;
                if (isLogDebugEnabled) {
                    log(String.format("%s old PC: 0x%08X, new PC: 0x%08X", helper.getCommandString(CALL), oldPc, newPc));
                }
                break;
            }

            case RET: {
                int oldPc = currentList.pc;
                currentList.ret();
                int newPc = currentList.pc;
                if (isLogDebugEnabled) {
                    log(String.format("%s old PC: 0x%08X, new PC: 0x%08X", helper.getCommandString(RET), oldPc, newPc));
                }
                break;
            }

            case END:
                // Try to end the current list.
                // The list only ends (isEnded() == true) if FINISH was called previously.
                // In SIGNAL + END cases, isEnded() still remains false.
                currentList.endList();
                currentList.pauseList();
                if (isLogDebugEnabled) {
                    log(helper.getCommandString(END) + " pc=0x" + Integer.toHexString(currentList.pc));
                }
                updateGeBuf();
                break;

            case SIGNAL:
                int behavior = (normalArgument >> 16) & 0xFF;
                int signal = normalArgument & 0xFFFF;
                if (behavior < 1 || behavior > 3) {
                    if (isLogWarnEnabled) {
                        log(helper.getCommandString(SIGNAL) + " (behavior=" + behavior + ",signal=0x" + Integer.toHexString(signal) + ") unknown behavior");
                    }
                } else if (isLogDebugEnabled) {
                    log(helper.getCommandString(SIGNAL) + " (behavior=" + behavior + ",signal=0x" + Integer.toHexString(signal) + ")");
                }
                currentList.pushSignalCallback(currentList.id, behavior, signal);
                break;

            case FINISH:
                if (isLogDebugEnabled) {
                    log(helper.getCommandString(FINISH) + " " + getArgumentLog(normalArgument));
                }
                currentList.finishList();
                currentList.pushFinishCallback(normalArgument);
                break;

            case BASE:
                base = (normalArgument << 8) & 0xff000000;
                // Bits of (normalArgument & 0x0000FFFF) are ignored
                // (tested: "Ape Escape On the Loose")
                if (isLogDebugEnabled) {
                    log(helper.getCommandString(BASE) + " " + String.format("%08x", base));
                }
                break;

            case VTYPE: {
                int old_transform_mode = transform_mode;
                boolean old_vertex_hasColor = vinfo.color != 0;
                vinfo.processType(normalArgument);
                transform_mode = (normalArgument >> 23) & 0x1;
                boolean vertex_hasColor = vinfo.color != 0;

                //Switching from 2D to 3D or 3D to 2D?
                if (old_transform_mode != transform_mode) {
                    projectionMatrixUpload.setChanged(true);
                    modelMatrixUpload.setChanged(true);
                    viewMatrixUpload.setChanged(true);
                    textureMatrixUpload.setChanged(true);
                    viewportChanged = true;
                    depthChanged = true;
                    materialChanged = true;
                    // Switching from 2D to 3D?
                    if (transform_mode == VTYPE_TRANSFORM_PIPELINE_TRANS_COORD) {
                        lightingChanged = true;
                    }
                } else if (old_vertex_hasColor != vertex_hasColor) {
                    // Materials have to be reloaded when the vertex color presence is changing
                    materialChanged = true;
                }

                if (isLogDebugEnabled) {
                    log(helper.getCommandString(VTYPE) + " " + vinfo.toString());
                }
                break;
            }

            case OFFSET_ADDR:
                baseOffset = normalArgument << 8;
                if (isLogDebugEnabled) {
                    log(String.format("%s 0x%08X", helper.getCommandString(OFFSET_ADDR), baseOffset));
                }
                break;

            case ORIGIN_ADDR:
                baseOffset = currentList.pc - 4;
                if (normalArgument != 0) {
                    log.warn(String.format("%s unknown argument 0x%08X", helper.getCommandString(ORIGIN_ADDR), normalArgument));
                } else if (isLogDebugEnabled) {
                    log(String.format("%s 0x%08X originAddr=0x%08X", helper.getCommandString(ORIGIN_ADDR), normalArgument, baseOffset));
                }
                break;

            case REGION1:
                region_x1 = normalArgument & 0x3ff;
                region_y1 = (normalArgument >> 10) & 0x3ff;
                break;

            case REGION2:
                region_x2 = normalArgument & 0x3ff;
                region_y2 = (normalArgument >> 10) & 0x3ff;
                region_width = (region_x2 + 1) - region_x1;
                region_height = (region_y2 + 1) - region_y1;
                if (isLogDebugEnabled) {
                    log("drawRegion(" + region_x1 + "," + region_y1 + "," + region_width + "," + region_height + ")");
                }
                break;

            /*
             * Lighting enable/disable
             */
            case LTE: {
                if (lightingFlag.setEnabled(normalArgument)) {
                    if (lightingFlag.isEnabled()) {
                        lightingChanged = true;
                    }
                }
                break;
            }

            /*
             * Individual lights enable/disable
             */
            case LTE0:
            case LTE1:
            case LTE2:
            case LTE3: {
                int lnum = command - LTE0;
                EnableDisableFlag lightFlag = lightFlags[lnum];
                if (lightFlag.setEnabled(normalArgument)) {
                    if (lightFlag.isEnabled()) {
                        lightingChanged = true;
                    }
                }
                break;
            }

            case CPE:
                if (normalArgument != 0) {
                    gl.glEnable(GL.GL_CLIP_PLANE0);
                    gl.glEnable(GL.GL_CLIP_PLANE1);
                    gl.glEnable(GL.GL_CLIP_PLANE2);
                    gl.glEnable(GL.GL_CLIP_PLANE3);
                    gl.glEnable(GL.GL_CLIP_PLANE4);
                    gl.glEnable(GL.GL_CLIP_PLANE5);
                    if (isLogDebugEnabled) {
                        log("Clip Plane Enable " + getArgumentLog(normalArgument));
                    }
                } else {
                    gl.glDisable(GL.GL_CLIP_PLANE0);
                    gl.glDisable(GL.GL_CLIP_PLANE1);
                    gl.glDisable(GL.GL_CLIP_PLANE2);
                    gl.glDisable(GL.GL_CLIP_PLANE3);
                    gl.glDisable(GL.GL_CLIP_PLANE4);
                    gl.glDisable(GL.GL_CLIP_PLANE5);
                    if (isLogDebugEnabled) {
                        log("Clip Plane Disable " + getArgumentLog(normalArgument));
                    }
                }
                clipPlanesFlag.setEnabled(normalArgument);
                break;

            case BCE:
                cullFaceFlag.setEnabled(normalArgument);
                break;

            case TME:
                textureFlag.setEnabled(normalArgument);
                break;

            case FGE:
                if (fogFlag.setEnabled(normalArgument)) {
                    if (fogFlag.isEnabled()) {
                        gl.glFogi(GL.GL_FOG_MODE, GL.GL_LINEAR);
                        gl.glHint(GL.GL_FOG_HINT, GL.GL_DONT_CARE);
                    }
                }
                break;

            case DTE:
                ditherFlag.setEnabled(normalArgument);
                break;

            case ABE:
                blendFlag.setEnabled(normalArgument);
                break;

            case ATE:
                alphaTestFlag.setEnabled(normalArgument);
                break;

            case ZTE:
                if (depthTestFlag.setEnabled(normalArgument)) {
                    if (depthTestFlag.isEnabled()) {
                        // OpenGL requires the Depth parameters to be reloaded
                        depthChanged = true;
                    }
                }
                break;

            case STE:
                stencilTestFlag.setEnabled(normalArgument);
                break;

            case AAE:
                if (lineSmoothFlag.setEnabled(normalArgument)) {
                    if (lineSmoothFlag.isEnabled()) {
                        gl.glHint(GL.GL_LINE_SMOOTH_HINT, GL.GL_NICEST);
                    }
                }
                break;

            case PCE: {
                patchCullFaceFlag.setEnabled(normalArgument);
                break;
            }

            case CTE: {
                colorTestFlag.setEnabled(normalArgument);
                break;
            }

            case LOE:
                colorLogicOpFlag.setEnabled(normalArgument);
                break;

            /*
             * Skinning
             */
            case BOFS: {
                boneMatrixIndex = normalArgument;
                if (isLogDebugEnabled) {
                    log("bone matrix offset", normalArgument);
                }
                break;
            }

            case BONE: {
                // Multiple BONE matrix can be loaded in sequence
                // without having to issue a BOFS for each matrix.
                int matrixIndex = boneMatrixIndex / 12;
                int elementIndex = boneMatrixIndex % 12;
                if (matrixIndex >= 8) {
                    error("Ignoring BONE matrix element: boneMatrixIndex=" + boneMatrixIndex);
                } else {
                    float floatArgument = floatArgument(normalArgument);
                    bone_uploaded_matrix[matrixIndex][elementIndex] = floatArgument;
                    if (useSkinningShaders) {
                        boneMatrixForShader[(boneMatrixIndex / 3) * 4 + (boneMatrixIndex % 3)] = floatArgument;
                        if (matrixIndex >= boneMatrixForShaderUpdatedMatrix) {
                            boneMatrixForShaderUpdatedMatrix = matrixIndex + 1;
                        }
                    }

                    boneMatrixIndex++;

                    if (isLogDebugEnabled && (boneMatrixIndex % 12) == 0) {
                        for (int x = 0; x < 3; x++) {
                            log.debug(String.format("bone matrix %d %.2f %.2f %.2f %.2f",
                                    matrixIndex,
                                    bone_uploaded_matrix[matrixIndex][x + 0],
                                    bone_uploaded_matrix[matrixIndex][x + 3],
                                    bone_uploaded_matrix[matrixIndex][x + 6],
                                    bone_uploaded_matrix[matrixIndex][x + 9]));
                        }
                    }
                }
                break;
            }

            /*
             * Morphing
             */
            case MW0:
            case MW1:
            case MW2:
            case MW3:
            case MW4:
            case MW5:
            case MW6:
            case MW7: {
                int index = command - MW0;
                float floatArgument = floatArgument(normalArgument);
                morph_weight[index] = floatArgument;
                re.setMorphWeight(index, floatArgument);
                if (isLogDebugEnabled) {
                    log("morph weight " + index, floatArgument);
                }
                break;
            }

            case PSUB:
                patch_div_s = normalArgument & 0xFF;
                patch_div_t = (normalArgument >> 8) & 0xFF;
                re.setPatchDiv(patch_div_s, patch_div_t);
                if (isLogDebugEnabled) {
                    log(helper.getCommandString(PSUB) + " patch_div_s=" + patch_div_s + ", patch_div_t=" + patch_div_t);
                }
                break;

            case PPRIM: {
                patch_prim = (normalArgument & 0x3);
                // Primitive type to use in patch division:
                // 0 - Triangle.
                // 1 - Line.
                // 2 - Point.
                re.setPatchPrim(patch_prim);
                if (isLogDebugEnabled) {
                    log(helper.getCommandString(PPRIM) + " patch_prim=" + patch_prim);
                }
                break;
            }

            case PFACE: {
                // 0 - Clockwise oriented patch / 1 - Counter clockwise oriented patch.
                patchFaceFlag.setEnabled(normalArgument);
                break;
            }

            case MMS:
                modelMatrixUpload.startUpload(normalArgument);
                if (isLogDebugEnabled) {
                    log("sceGumMatrixMode GU_MODEL " + normalArgument);
                }
                break;

            case MODEL:
                if (modelMatrixUpload.uploadValue(floatArgument(normalArgument))) {
                    log("glLoadMatrixf", model_uploaded_matrix);
                }
                break;

            case VMS:
                viewMatrixUpload.startUpload(normalArgument);
                if (isLogDebugEnabled) {
                    log("sceGumMatrixMode GU_VIEW " + normalArgument);
                }
                break;

            case VIEW:
                if (viewMatrixUpload.uploadValue(floatArgument(normalArgument))) {
                    log("glLoadMatrixf", view_uploaded_matrix);
                }
                break;

            case PMS:
                projectionMatrixUpload.startUpload(normalArgument);
                if (isLogDebugEnabled) {
                    log("sceGumMatrixMode GU_PROJECTION " + normalArgument);
                }
                break;

            case PROJ:
                if (projectionMatrixUpload.uploadValue(floatArgument(normalArgument))) {
                    log("glLoadMatrixf", proj_uploaded_matrix);
                }
                break;

            case TMS:
                textureMatrixUpload.startUpload(normalArgument);
                if (isLogDebugEnabled) {
                    log("sceGumMatrixMode GU_TEXTURE " + normalArgument);
                }
                break;

            case TMATRIX:
                if (textureMatrixUpload.uploadValue(floatArgument(normalArgument))) {
                    log("glLoadMatrixf", texture_uploaded_matrix);
                }
                break;

            case XSCALE: {
                int old_viewport_width = viewport_width;
                viewport_width = (int) floatArgument(normalArgument);
                if (old_viewport_width != viewport_width) {
                    viewportChanged = true;
                }
                break;
            }

            case YSCALE: {
                int old_viewport_height = viewport_height;
                viewport_height = (int) floatArgument(normalArgument);
                if (old_viewport_height != viewport_height) {
                    viewportChanged = true;
                }
                break;
            }

            case ZSCALE: {
                float old_zscale = zscale;
                float floatArgument = floatArgument(normalArgument);
                zscale = floatArgument / 65535.f;
                if (old_zscale != zscale) {
                    depthChanged = true;
                }

                if (isLogDebugEnabled) {
                    log(helper.getCommandString(ZSCALE) + " " + floatArgument);
                }
                break;
            }

            case XPOS: {
                int old_viewport_cx = viewport_cx;
                viewport_cx = (int) floatArgument(normalArgument);
                if (old_viewport_cx != viewport_cx) {
                    viewportChanged = true;
                }
                break;
            }

            case YPOS: {
                int old_viewport_cy = viewport_cy;
                viewport_cy = (int) floatArgument(normalArgument);
                if (old_viewport_cy != viewport_cy) {
                    viewportChanged = true;
                }

                // Log only on the last called command (always XSCALE -> YSCALE -> XPOS -> YPOS).
                if (isLogDebugEnabled) {
                    log.debug("sceGuViewport(cx=" + viewport_cx + ", cy=" + viewport_cy + ", w=" + viewport_width + " h=" + viewport_height + ")");
                }
                break;
            }

            case ZPOS: {
                float old_zpos = zpos;
                float floatArgument = floatArgument(normalArgument);
                zpos = floatArgument / 65535.f;
                if (old_zpos != zpos) {
                    depthChanged = true;
                }

                if (isLogDebugEnabled) {
                    log(helper.getCommandString(ZPOS), floatArgument);
                }
                break;
            }

            /*
             * Texture transformations
             */
            case USCALE: {
                float old_tex_scale_x = tex_scale_x;
                tex_scale_x = floatArgument(normalArgument);

                if (old_tex_scale_x != tex_scale_x) {
                    textureMatrixUpload.setChanged(true);
                }
                break;
            }

            case VSCALE: {
                float old_tex_scale_y = tex_scale_y;
                tex_scale_y = floatArgument(normalArgument);

                if (old_tex_scale_y != tex_scale_y) {
                    textureMatrixUpload.setChanged(true);
                }

                if (isLogDebugEnabled) {
                    log("sceGuTexScale(u=" + tex_scale_x + ", v=" + tex_scale_y + ")");
                }
                break;
            }

            case UOFFSET: {
                float old_tex_translate_x = tex_translate_x;
                tex_translate_x = floatArgument(normalArgument);

                if (old_tex_translate_x != tex_translate_x) {
                    textureMatrixUpload.setChanged(true);
                }
                break;
            }

            case VOFFSET: {
                float old_tex_translate_y = tex_translate_y;
                tex_translate_y = floatArgument(normalArgument);

                if (old_tex_translate_y != tex_translate_y) {
                    textureMatrixUpload.setChanged(true);
                }

                if (isLogDebugEnabled) {
                    log("sceGuTexOffset(u=" + tex_translate_x + ", v=" + tex_translate_y + ")");
                }
                break;
            }

            case OFFSETX: {
                int old_offset_x = offset_x;
                offset_x = normalArgument >> 4;
                if (old_offset_x != offset_x) {
                    viewportChanged = true;
                }
                break;
            }

            case OFFSETY: {
                int old_offset_y = offset_y;
                offset_y = normalArgument >> 4;
                if (old_offset_y != offset_y) {
                    viewportChanged = true;
                }

                if(isLogDebugEnabled) {
                    log.debug("sceGuOffset(x=" + offset_x + ",y=" + offset_y + ")");
                }

                break;
            }

            case SHADE: {
                re.setShadeModel(normalArgument);
                if (isLogDebugEnabled) {
                    log("sceGuShadeModel(" + ((normalArgument != 0) ? "smooth" : "flat") + ")");
                }
                break;
            }

            case RNORM: {
                // This seems to be taked into account when calculating the lighting
                // for the current normal.
                faceNormalReverseFlag.setEnabled(normalArgument);
                break;
            }

            /*
             * Material setup
             */
            case CMAT: {
                int old_mat_flags = mat_flags;
                mat_flags = normalArgument & 7;
                if (old_mat_flags != mat_flags) {
                    materialChanged = true;
                }

                if (isLogDebugEnabled) {
                    log("sceGuColorMaterial " + mat_flags);
                }
                break;
            }

            case EMC:
                mat_emissive[0] = ((normalArgument) & 255) / 255.f;
                mat_emissive[1] = ((normalArgument >> 8) & 255) / 255.f;
                mat_emissive[2] = ((normalArgument >> 16) & 255) / 255.f;
                mat_emissive[3] = 1.f;
                materialChanged = true;
                re.setMaterialEmissiveColor(mat_emissive);
                if (isLogDebugEnabled) {
                    log("material emission " + String.format("r=%.1f g=%.1f b=%.1f (%08X)",
                            mat_emissive[0], mat_emissive[1], mat_emissive[2], normalArgument));
                }
                break;

            case AMC:
                mat_ambient[0] = ((normalArgument) & 255) / 255.f;
                mat_ambient[1] = ((normalArgument >> 8) & 255) / 255.f;
                mat_ambient[2] = ((normalArgument >> 16) & 255) / 255.f;
                materialChanged = true;
                if (isLogDebugEnabled) {
                    log(String.format("material ambient r=%.1f g=%.1f b=%.1f (%08X)",
                            mat_ambient[0], mat_ambient[1], mat_ambient[2], normalArgument));
                }
                break;

            case DMC:
                mat_diffuse[0] = ((normalArgument) & 255) / 255.f;
                mat_diffuse[1] = ((normalArgument >> 8) & 255) / 255.f;
                mat_diffuse[2] = ((normalArgument >> 16) & 255) / 255.f;
                mat_diffuse[3] = 1.f;
                materialChanged = true;
                if (isLogDebugEnabled) {
                    log("material diffuse " + String.format("r=%.1f g=%.1f b=%.1f (%08X)",
                            mat_diffuse[0], mat_diffuse[1], mat_diffuse[2], normalArgument));
                }
                break;

            case SMC:
                mat_specular[0] = ((normalArgument) & 255) / 255.f;
                mat_specular[1] = ((normalArgument >> 8) & 255) / 255.f;
                mat_specular[2] = ((normalArgument >> 16) & 255) / 255.f;
                mat_specular[3] = 1.f;
                materialChanged = true;
                if (isLogDebugEnabled) {
                    log("material specular " + String.format("r=%.1f g=%.1f b=%.1f (%08X)",
                            mat_specular[0], mat_specular[1], mat_specular[2], normalArgument));
                }
                break;

            case AMA: {
                mat_ambient[3] = ((normalArgument) & 255) / 255.f;
                materialChanged = true;
                if (isLogDebugEnabled) {
                    log(String.format("material ambient a=%.1f (%02X)",
                            mat_ambient[3], normalArgument & 255));
                }
                break;
            }

            case SPOW: {
                float floatArgument = floatArgument(normalArgument);
                gl.glMaterialf(GL.GL_FRONT, GL.GL_SHININESS, floatArgument);
                if (isLogDebugEnabled) {
                    log("material shininess " + floatArgument);
                }
                break;
            }

            case ALC:
                ambient_light[0] = ((normalArgument) & 255) / 255.f;
                ambient_light[1] = ((normalArgument >> 8) & 255) / 255.f;
                ambient_light[2] = ((normalArgument >> 16) & 255) / 255.f;
                re.setLightModelAmbientColor(ambient_light);
                if (isLogDebugEnabled) {
                    log("ambient light " + String.format("r=%.1f g=%.1f b=%.1f (%08X)",
                            ambient_light[0], ambient_light[1], ambient_light[2], normalArgument));
                }
                break;

            case ALA:
                ambient_light[3] = ((normalArgument) & 255) / 255.f;
                re.setLightModelAmbientColor(ambient_light);
                break;

            case LMODE: {
                re.setLightMode(normalArgument);
                if (isLogDebugEnabled) {
                    VideoEngine.log.info("sceGuLightMode(" + ((normalArgument != 0) ? "GU_SEPARATE_SPECULAR_COLOR" : "GU_SINGLE_COLOR") + ")");
                }
                // Check if other values than 0 and 1 are set
                if ((normalArgument & ~1) != 0) {
                    VideoEngine.log.warn(String.format("Unknown light mode sceGuLightMode(%06X)", normalArgument));
                }
                break;
            }

            /*
             * Light types
             */
            case LT0:
            case LT1:
            case LT2:
            case LT3: {
                int lnum = command - LT0;
                int old_light_type = light_type[lnum];
                int old_light_kind = light_kind[lnum];
                light_type[lnum] = (normalArgument >> 8) & 3;
                light_kind[lnum] = normalArgument & 3;

                if (old_light_type != light_type[lnum] || old_light_kind != light_kind[lnum]) {
                    lightingChanged = true;
                }

                switch (light_type[lnum]) {
                    case LIGHT_DIRECTIONAL:
                        light_pos[lnum][3] = 0.f;
                        break;
                    case LIGHT_POINT:
                    	re.setLightSpotCutoff(lnum, 180);
                        light_pos[lnum][3] = 1.f;
                        break;
                    case LIGHT_SPOT:
                        light_pos[lnum][3] = 1.f;
                        break;
                    default:
                        error("Unknown light type : " + normalArgument);
                }
                re.setLightType(lnum, light_type[lnum], light_kind[lnum]);

                if (isLogDebugEnabled) {
                    log.debug("Light " + lnum + " type " + (normalArgument >> 8) + " kind " + (normalArgument & 3));
                }
                break;
            }

            /*
             *  Light attributes
             */

            // Position
            case LXP0:
            case LXP1:
            case LXP2:
            case LXP3:
            case LYP0:
            case LYP1:
            case LYP2:
            case LYP3:
            case LZP0:
            case LZP1:
            case LZP2:
            case LZP3: {
                int lnum = (command - LXP0) / 3;
                int component = (command - LXP0) % 3;
                float old_light_pos = light_pos[lnum][component];
                light_pos[lnum][component] = floatArgument(normalArgument);

                if (old_light_pos != light_pos[lnum][component]) {
                    lightingChanged = true;
                }
                break;
            }

            case LXD0:
            case LXD1:
            case LXD2:
            case LXD3:
            case LYD0:
            case LYD1:
            case LYD2:
            case LYD3:
            case LZD0:
            case LZD1:
            case LZD2:
            case LZD3: {
                int lnum = (command - LXD0) / 3;
                int component = (command - LXD0) % 3;
                float old_light_dir = light_dir[lnum][component];

                // OpenGL requires a normal in the opposite direction as the PSP
                light_dir[lnum][component] = -floatArgument(normalArgument);

                if (old_light_dir != light_dir[lnum][component]) {
                    lightingChanged = true;
                }
                // OpenGL parameter for light direction is set in initRendering
                // because it depends on the model/view matrix
                break;
            }

            // Light Attenuation

            // Constant
            case LCA0:
            case LCA1:
            case LCA2:
            case LCA3: {
                int lnum = (command - LCA0) / 3;
                re.setLightConstantAttenuation(lnum, floatArgument(normalArgument));
                break;
            }

            // Linear
            case LLA0:
            case LLA1:
            case LLA2:
            case LLA3: {
                int lnum = (command - LLA0) / 3;
                re.setLightLinearAttenuation(lnum, floatArgument(normalArgument));
                break;
            }

            // Quadratic
            case LQA0:
            case LQA1:
            case LQA2:
            case LQA3: {
                int lnum = (command - LQA0) / 3;
                re.setLightQuadraticAttenuation(lnum, floatArgument(normalArgument));
                break;
            }

            /*
             * Spot light exponent
             */
            case SLE0:
            case SLE1:
            case SLE2:
            case SLE3: {
                int lnum = command - SLE0;
                float old_spotLightExponent = spotLightExponent[lnum];
                spotLightExponent[lnum] = floatArgument(normalArgument);

                if (old_spotLightExponent != spotLightExponent[lnum]) {
                    lightingChanged = true;
                }

                if (isLogDebugEnabled) {
                    VideoEngine.log.debug("sceGuLightSpot(" + lnum + ",X," + spotLightExponent[lnum] + ",X)");
                }
                break;
            }

            /*
             * Spot light cutoff angle
             */
            case SLF0:
            case SLF1:
            case SLF2:
            case SLF3: {
                int lnum = command - SLF0;
                float old_spotLightCutoff = spotLightCutoff[lnum];

                // PSP Cutoff is cosine of angle, OpenGL expects degrees
                float floatArgument = floatArgument(normalArgument);
                float degreeCutoff = (float) Math.toDegrees(Math.acos(floatArgument));
                if ((degreeCutoff >= 0 && degreeCutoff <= 90) || degreeCutoff == 180) {
                    spotLightCutoff[lnum] = degreeCutoff;

                    if (old_spotLightCutoff != spotLightCutoff[lnum]) {
                        lightingChanged = true;
                    }

                    if (isLogDebugEnabled) {
                        log.debug("sceGuLightSpot(" + lnum + ",X,X," + floatArgument + "=" + degreeCutoff + ")");
                    }
                } else {
                    log.warn("sceGuLightSpot(" + lnum + ",X,X," + floatArgument + ") invalid argument value");
                }
                break;
            }

            // Color

            // Ambient
            case ALC0:
            case ALC1:
            case ALC2:
            case ALC3: {
                int lnum = (command - ALC0) / 3;
                lightAmbientColor[lnum][0] = ((normalArgument) & 255) / 255.f;
                lightAmbientColor[lnum][1] = ((normalArgument >> 8) & 255) / 255.f;
                lightAmbientColor[lnum][2] = ((normalArgument >> 16) & 255) / 255.f;
                lightAmbientColor[lnum][3] = 1.f;
                re.setLightAmbientColor(lnum, lightAmbientColor[lnum]);
                log("sceGuLightColor (GU_LIGHT0, GU_AMBIENT)");
                break;
            }

            // Diffuse
            case DLC0:
            case DLC1:
            case DLC2:
            case DLC3: {
                int lnum = (command - DLC0) / 3;
                lightDiffuseColor[lnum][0] = ((normalArgument) & 255) / 255.f;
                lightDiffuseColor[lnum][1] = ((normalArgument >> 8) & 255) / 255.f;
                lightDiffuseColor[lnum][2] = ((normalArgument >> 16) & 255) / 255.f;
                lightDiffuseColor[lnum][3] = 1.f;
                re.setLightDiffuseColor(lnum, lightDiffuseColor[lnum]);
                log("sceGuLightColor (GU_LIGHT0, GU_DIFFUSE)");
                break;
            }

            // Specular
            case SLC0:
            case SLC1:
            case SLC2:
            case SLC3: {
                int lnum = (command - SLC0) / 3;
                float old_lightSpecularColor0 = lightSpecularColor[lnum][0];
                float old_lightSpecularColor1 = lightSpecularColor[lnum][1];
                float old_lightSpecularColor2 = lightSpecularColor[lnum][2];
                lightSpecularColor[lnum][0] = ((normalArgument) & 255) / 255.f;
                lightSpecularColor[lnum][1] = ((normalArgument >> 8) & 255) / 255.f;
                lightSpecularColor[lnum][2] = ((normalArgument >> 16) & 255) / 255.f;
                lightSpecularColor[lnum][3] = 1.f;

                if (old_lightSpecularColor0 != lightSpecularColor[lnum][0] || old_lightSpecularColor1 != lightSpecularColor[lnum][1] || old_lightSpecularColor2 != lightSpecularColor[lnum][2]) {
                    lightingChanged = true;
                }
                re.setLightSpecularColor(lnum, lightDiffuseColor[lnum]);
                log("sceGuLightColor (GU_LIGHT0, GU_SPECULAR)");
                break;
            }

            case FFACE: {
                int frontFace = (normalArgument != 0) ? GL.GL_CW : GL.GL_CCW;
                gl.glFrontFace(frontFace);
                if (isLogDebugEnabled) {
                    log(helper.getCommandString(FFACE) + " " + ((normalArgument != 0) ? "clockwise" : "counter-clockwise"));
                }
                break;
            }

            case FBP:
                // FBP can be called before or after FBW
                fbp = (fbp & 0xff000000) | normalArgument;
                if (isLogDebugEnabled) {
                    log(helper.getCommandString(FBP) + " fbp=" + Integer.toHexString(fbp) + ", fbw=" + fbw);
                }
                geBufChanged = true;
                break;

            case FBW:
                fbp = (fbp & 0x00ffffff) | ((normalArgument << 8) & 0xff000000);
                fbw = normalArgument & 0xffff;
                if (isLogDebugEnabled) {
                    log(helper.getCommandString(FBW) + " fbp=" + Integer.toHexString(fbp) + ", fbw=" + fbw);
                }
                geBufChanged = true;
                break;

            case ZBP:
                zbp = (zbp & 0xff000000) | normalArgument;
                if (isLogDebugEnabled) {
                    log("zbp=" + Integer.toHexString(zbp) + ", zbw=" + zbw);
                }
                break;

            case ZBW:
                zbp = (zbp & 0x00ffffff) | ((normalArgument << 8) & 0xff000000);
                zbw = normalArgument & 0xffff;
                if (isLogDebugEnabled) {
                    log("zbp=" + Integer.toHexString(zbp) + ", zbw=" + zbw);
                }
                break;

            case TBP0:
            case TBP1:
            case TBP2:
            case TBP3:
            case TBP4:
            case TBP5:
            case TBP6:
            case TBP7: {
                int level = command - TBP0;
                int old_texture_base_pointer = texture_base_pointer[level];
                texture_base_pointer[level] = (texture_base_pointer[level] & 0xff000000) | normalArgument;

                if (old_texture_base_pointer != texture_base_pointer[level]) {
                    textureChanged = true;
                }

                if (isLogDebugEnabled) {
                    log("sceGuTexImage(level=" + level + ", X, X, X, lo(pointer=0x" + Integer.toHexString(texture_base_pointer[level]) + "))");
                }
                break;
            }

            case TBW0:
            case TBW1:
            case TBW2:
            case TBW3:
            case TBW4:
            case TBW5:
            case TBW6:
            case TBW7: {
                int level = command - TBW0;
                int old_texture_base_pointer = texture_base_pointer[level];
                int old_texture_buffer_width = texture_buffer_width[level];
                texture_base_pointer[level] = (texture_base_pointer[level] & 0x00ffffff) | ((normalArgument << 8) & 0xff000000);
                texture_buffer_width[level] = normalArgument & 0xffff;

                if (old_texture_base_pointer != texture_base_pointer[level] || old_texture_buffer_width != texture_buffer_width[level]) {
                    textureChanged = true;
                }

                if (isLogDebugEnabled) {
                    log("sceGuTexImage(level=" + level + ", X, X, texBufferWidth=" + texture_buffer_width[level] + ", hi(pointer=0x" + Integer.toHexString(texture_base_pointer[level]) + "))");
                }
                break;
            }

            case CBP: {
                int old_tex_clut_addr = tex_clut_addr;
                tex_clut_addr = (tex_clut_addr & 0xff000000) | normalArgument;

                clutIsDirty = true;
                if (old_tex_clut_addr != tex_clut_addr) {
                    textureChanged = true;
                }

                if (isLogDebugEnabled) {
                    log("sceGuClutLoad(X, lo(cbp=0x" + Integer.toHexString(tex_clut_addr) + "))");
                }
                break;
            }

            case CBPH: {
                int old_tex_clut_addr = tex_clut_addr;
                tex_clut_addr = (tex_clut_addr & 0x00ffffff) | ((normalArgument << 8) & 0x0f000000);

                clutIsDirty = true;
                if (old_tex_clut_addr != tex_clut_addr) {
                    textureChanged = true;
                }

                if (isLogDebugEnabled) {
                    log("sceGuClutLoad(X, hi(cbp=0x" + Integer.toHexString(tex_clut_addr) + "))");
                }
                break;
            }

            case TRXSBP:
                textureTx_sourceAddress = (textureTx_sourceAddress & 0xFF000000) | normalArgument;
                break;

            case TRXSBW:
                textureTx_sourceAddress = (textureTx_sourceAddress & 0x00FFFFFF) | ((normalArgument << 8) & 0xFF000000);
                textureTx_sourceLineWidth = normalArgument & 0x0000FFFF;

                // TODO Check when sx and sy are reset to 0. Here or after TRXKICK?
                textureTx_sx = 0;
                textureTx_sy = 0;
                break;

            case TRXDBP:
                textureTx_destinationAddress = (textureTx_destinationAddress & 0xFF000000) | normalArgument;
                break;

            case TRXDBW:
                textureTx_destinationAddress = (textureTx_destinationAddress & 0x00FFFFFF) | ((normalArgument << 8) & 0xFF000000);
                textureTx_destinationLineWidth = normalArgument & 0x0000FFFF;

                // TODO Check when dx and dy are reset to 0. Here or after TRXKICK?
                textureTx_dx = 0;
                textureTx_dy = 0;
                break;

            case TSIZE0:
            case TSIZE1:
            case TSIZE2:
            case TSIZE3:
            case TSIZE4:
            case TSIZE5:
            case TSIZE6:
            case TSIZE7: {
                int level = command - TSIZE0;
                int old_texture_height = texture_height[level];
                int old_texture_width = texture_width[level];
                // Astonishia Story is using normalArgument = 0x1804
                // -> use texture_height = 1 << 0x08 (and not 1 << 0x18)
                //        texture_width  = 1 << 0x04
                // The maximum texture size is 512x512: the exponent value must be [0..9]
                int height_exp2 = Math.min((normalArgument >> 8) & 0x0F, 9);
                int width_exp2 = Math.min((normalArgument) & 0x0F, 9);
                texture_height[level] = 1 << height_exp2;
                texture_width[level] = 1 << width_exp2;

                if (old_texture_height != texture_height[level] || old_texture_width != texture_width[level]) {
                    if (transform_mode == VTYPE_TRANSFORM_PIPELINE_RAW_COORD && level == 0) {
                        textureMatrixUpload.setChanged(true);
                    }
                    textureChanged = true;
                }

                if (isLogDebugEnabled) {
                    log("sceGuTexImage(level=" + level + ", width=" + texture_width[level] + ", height=" + texture_height[level] + ", X, X)");
                }
                break;
            }

            case TMAP:
                int old_tex_map_mode = tex_map_mode;
                tex_map_mode = normalArgument & 3;
                tex_proj_map_mode = (normalArgument >> 8) & 3;

                if (old_tex_map_mode != tex_map_mode) {
                    textureMatrixUpload.setChanged(true);
                }

                if (isLogDebugEnabled) {
                    log("sceGuTexMapMode(mode=" + tex_map_mode + ", X, X)");
                    log("sceGuTexProjMapMode(mode=" + tex_proj_map_mode + ")");
                }
                break;

            case TEXTURE_ENV_MAP_MATRIX: {
                tex_shade_u = (normalArgument >> 0) & 0x3;
                tex_shade_v = (normalArgument >> 8) & 0x3;

                for (int i = 0; i < 3; i++) {
                    tex_envmap_matrix[i + 0] = light_pos[tex_shade_u][i];
                    tex_envmap_matrix[i + 4] = light_pos[tex_shade_v][i];
                }

                textureMatrixUpload.setChanged(true);
                if (isLogDebugEnabled) {
                    log("sceGuTexMapMode(X, " + tex_shade_u + ", " + tex_shade_v + ")");
                }
                break;
            }

            case TMODE: {
                int old_texture_num_mip_maps = texture_num_mip_maps;
                boolean old_mipmapShareClut = mipmapShareClut;
                boolean old_texture_swizzle = texture_swizzle;
                texture_num_mip_maps = (normalArgument >> 16) & 0x7;
                // This parameter has only a meaning when
                //  texture_storage == GU_PSM_T4 and texture_num_mip_maps > 0
                // when parameter==0: all the mipmaps share the same clut entries (normal behavior)
                // when parameter==1: each mipmap has its own clut table, 16 entries each, stored sequentially
                mipmapShareClut = ((normalArgument >> 8) & 0x1) == 0;
                texture_swizzle = ((normalArgument) & 0x1) != 0;

                if (old_texture_num_mip_maps != texture_num_mip_maps || old_mipmapShareClut != mipmapShareClut || old_texture_swizzle != texture_swizzle) {
                    textureChanged = true;
                }

                if (isLogDebugEnabled) {
                    log("sceGuTexMode(X, mipmaps=" + texture_num_mip_maps + ", mipmapShareClut=" + mipmapShareClut + ", swizzle=" + texture_swizzle + ")");
                }
                break;
            }

            case TPSM: {
                int old_texture_storage = texture_storage;
                texture_storage = normalArgument & 0xF; // Lower four bits.

                if (old_texture_storage != texture_storage) {
                    textureChanged = true;
                }

                if (isLogDebugEnabled) {
                    log("sceGuTexMode(tpsm=" + texture_storage + "(" + getPsmName(texture_storage) + "), X, X, X)");
                }
                break;
            }

            case CLOAD: {
                int old_tex_clut_num_blocks = tex_clut_num_blocks;
                tex_clut_num_blocks = normalArgument & 0x3F;

                clutIsDirty = true;
                if (old_tex_clut_num_blocks != tex_clut_num_blocks) {
                    textureChanged = true;
                }

                // Some games use the following sequence:
                // - sceGuClutLoad(num_blocks=32, X)
                // - sceGuClutLoad(num_blocks=1, X)
                // - tflush
                // - prim ... (texture data is referencing the clut entries from 32 blocks)
                //
                readClut();

                if (isLogDebugEnabled) {
                    log("sceGuClutLoad(num_blocks=" + tex_clut_num_blocks + ", X)");
                }
                break;
            }

            case CMODE: {
                int old_tex_clut_mode = tex_clut_mode;
                int old_tex_clut_shift = tex_clut_shift;
                int old_tex_clut_mask = tex_clut_mask;
                int old_tex_clut_start = tex_clut_start;
                tex_clut_mode = normalArgument & 0x03;
                tex_clut_shift = (normalArgument >> 2) & 0x1F;
                tex_clut_mask = (normalArgument >> 8) & 0xFF;
                tex_clut_start = (normalArgument >> 16) & 0x1F;

                clutIsDirty = true;
                if (old_tex_clut_mode != tex_clut_mode || old_tex_clut_shift != tex_clut_shift || old_tex_clut_mask != tex_clut_mask || old_tex_clut_start != tex_clut_start) {
                    textureChanged = true;
                }

                if (isLogDebugEnabled) {
                    log("sceGuClutMode(cpsm=" + tex_clut_mode + "(" + getPsmName(tex_clut_mode) + "), shift=" + tex_clut_shift + ", mask=0x" + Integer.toHexString(tex_clut_mask) + ", start=" + tex_clut_start + ")");
                }
                break;
            }

            case TFLT: {
                int old_tex_mag_filter = tex_mag_filter;
                int old_tex_min_filter = tex_min_filter;

                if (isLogDebugEnabled) {
                    log("sceGuTexFilter(min=" + (normalArgument & 0x7) + ", mag=" + ((normalArgument >> 8) & 0x1) + ") (mm#" + texture_num_mip_maps + ")");
                }

                switch ((normalArgument >> 8) & 0x1) {
                    case TFLT_NEAREST: {
                        tex_mag_filter = GL.GL_NEAREST;
                        break;
                    }
                    case TFLT_LINEAR: {
                        tex_mag_filter = GL.GL_LINEAR;
                        break;
                    }
                }

                switch (normalArgument & 0x7) {
                    case TFLT_NEAREST: {
                        tex_min_filter = GL.GL_NEAREST;
                        break;
                    }
                    case TFLT_LINEAR: {
                        tex_min_filter = GL.GL_LINEAR;
                        break;
                    }
                    case TFLT_NEAREST_MIPMAP_NEAREST: {
                        tex_min_filter = GL.GL_NEAREST_MIPMAP_NEAREST;
                        break;
                    }
                    case TFLT_NEAREST_MIPMAP_LINEAR: {
                        tex_min_filter = GL.GL_NEAREST_MIPMAP_LINEAR;
                        break;
                    }
                    case TFLT_LINEAR_MIPMAP_NEAREST: {
                        tex_min_filter = GL.GL_LINEAR_MIPMAP_NEAREST;
                        break;
                    }
                    case TFLT_LINEAR_MIPMAP_LINEAR: {
                        tex_min_filter = GL.GL_LINEAR_MIPMAP_LINEAR;
                        break;
                    }

                    default: {
                        log.warn("Unknown minimizing filter " + (normalArgument & 0xFF));
                        break;
                    }
                }

                if (old_tex_mag_filter != tex_mag_filter || old_tex_min_filter != tex_min_filter) {
                    textureChanged = true;
                }
                break;
            }

            case TWRAP: {
                tex_wrap_s = normalArgument & 0xFF;
                tex_wrap_t = (normalArgument >> 8) & 0xFF;

                if (tex_wrap_s > TWRAP_WRAP_MODE_CLAMP) {
                    log.warn(helper.getCommandString(TWRAP) + " unknown wrap mode " + tex_wrap_s);
                    tex_wrap_s = TWRAP_WRAP_MODE_REPEAT;
                }
                if (tex_wrap_t > TWRAP_WRAP_MODE_CLAMP) {
                    log.warn(helper.getCommandString(TWRAP) + " unknown wrap mode " + tex_wrap_t);
                    tex_wrap_t = TWRAP_WRAP_MODE_REPEAT;
                }
                break;
            }

            case TBIAS: {
                tex_mipmap_mode = normalArgument & 0x3;
                tex_mipmap_bias_int = (int) (byte) (normalArgument >> 16); // Signed 8-bit integer
                tex_mipmap_bias = tex_mipmap_bias_int / 16.0f;
                if (isLogDebugEnabled) {
                    log.debug("sceGuTexLevelMode(mode=" + tex_mipmap_mode + ", bias=" + tex_mipmap_bias + ")");
                }
                break;
            }

            case TFUNC:
                executeCommandTFUNC(normalArgument);
                break;

            case TEC:
                tex_env_color[0] = ((normalArgument) & 255) / 255.f;
                tex_env_color[1] = ((normalArgument >> 8) & 255) / 255.f;
                tex_env_color[2] = ((normalArgument >> 16) & 255) / 255.f;
                tex_env_color[3] = 1.f;
                gl.glTexEnvfv(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_COLOR, tex_env_color, 0);

                if (isLogDebugEnabled) {
                    log(String.format("sceGuTexEnvColor %08X (no alpha)", normalArgument));
                }
                break;

            case TFLUSH: {
                // Do not load the texture right now, clut parameters can still be
                // defined after the TFLUSH and before the PRIM command.
                // Delay the texture loading until the PRIM command.
                if (isLogDebugEnabled) {
                    log("tflush (deferring to prim)");
                }
                break;
            }

            case TSYNC: {
                // Block texture reading until the current list is drawn.
                // TODO: Currently just faking. Needs to be tested and compared
                // in terms of speed.
                setTsync(true);
                if (isLogDebugEnabled) {
                    log(helper.getCommandString(TSYNC) + " waiting for drawing.");
                }
                break;
            }

            case FFAR:
                fog_far = floatArgument(normalArgument);
                break;

            case FDIST:
                fog_dist = floatArgument(normalArgument);
                if ((fog_far != 0.0f) && (fog_dist != 0.0f)) {
                    float end = fog_far;
                    float start = end - (1 / fog_dist);
                    gl.glFogf(GL.GL_FOG_START, start);
                    gl.glFogf(GL.GL_FOG_END, end);
                }
                break;

            case FCOL:
                fog_color[0] = ((normalArgument) & 255) / 255.f;
                fog_color[1] = ((normalArgument >> 8) & 255) / 255.f;
                fog_color[2] = ((normalArgument >> 16) & 255) / 255.f;
                fog_color[3] = 1.f;
                gl.glFogfv(GL.GL_FOG_COLOR, fog_color, 0);

                if (isLogDebugEnabled) {
                    log(String.format("sceGuFog(X, X, color=%08X) (no alpha)", normalArgument));
                }
                break;

            case TSLOPE: {
                tslope_level = floatArgument(normalArgument);
                if (isLogDebugEnabled) {
                    log(helper.getCommandString(TSLOPE) + " tslope_level=" + tslope_level);
                }
                break;
            }

            case PSM:
                psm = normalArgument;
                if (isLogDebugEnabled) {
                    log("psm=" + normalArgument + "(" + getPsmName(normalArgument) + ")");
                }
                geBufChanged = true;
                break;

            case CLEAR:
                executeCommandCLEAR(normalArgument);
                break;

            case SCISSOR1:
                scissor_x1 = normalArgument & 0x3ff;
                scissor_y1 = (normalArgument >> 10) & 0x3ff;
                break;

            case SCISSOR2:
                scissor_x2 = normalArgument & 0x3ff;
                scissor_y2 = (normalArgument >> 10) & 0x3ff;
                scissor_width = 1 + scissor_x2 - scissor_x1;
                scissor_height = 1 + scissor_y2 - scissor_y1;

                if (isLogDebugEnabled) {
                    log("sceGuScissor(" + scissor_x1 + "," + scissor_y1 + "," + scissor_width + "," + scissor_height + ")");
                }
                if (scissor_x1 >= 0 && scissor_y1 >= 0 && scissor_width <= region_width && scissor_height <= region_height) {
                    scissorTestFlag.setEnabled(true);
                    gl.glScissor(scissor_x1, scissor_y1, scissor_width, scissor_height);
                    if (isLogDebugEnabled) {
                        log("sceGuEnable(GU_SCISSOR_TEST)");
                    }
                } else {
                    scissorTestFlag.setEnabled(false);
                }
                break;

            case NEARZ: {
                float old_nearZ = nearZ;
                nearZ = (normalArgument & 0xFFFF) / (float) 0xFFFF;
                if (old_nearZ != nearZ) {
                    depthChanged = true;
                }
                break;
            }

            case FARZ: {
                float old_farZ = farZ;
                farZ = (normalArgument & 0xFFFF) / (float) 0xFFFF;
                if (old_farZ != farZ) {
                    // OpenGL requires the Depth parameters to be reloaded
                    depthChanged = true;
                }

                if (depthChanged) {
                    re.setDepthRange(zpos, zscale, nearZ, farZ);
                }

                if (isLogDebugEnabled) {
                    log.debug("sceGuDepthRange(" + nearZ + ", " + farZ + ")");
                }
                break;
            }

            case CTST: {
                shaderCtestFunc = normalArgument & 3;
                re.setColorTestFunc(shaderCtestFunc);
                break;
            }

            case CREF: {
                shaderCtestRef[0] = (normalArgument) & 0xFF;
                shaderCtestRef[1] = (normalArgument >> 8) & 0xFF;
                shaderCtestRef[2] = (normalArgument >> 16) & 0xFF;
                re.setColorTestReference(shaderCtestRef);
                break;
            }

            case CMSK: {
                shaderCtestMsk[0] = (normalArgument) & 0xFF;
                shaderCtestMsk[1] = (normalArgument >> 8) & 0xFF;
                shaderCtestMsk[2] = (normalArgument >> 16) & 0xFF;
                re.setColorTestMask(shaderCtestMsk);
                break;
            }

            case ATST: {

                int guFunc = normalArgument & 0xFF;
                int guReferenceAlphaValue = (normalArgument >> 8) & 0xFF;
                int glFunc = GL.GL_ALWAYS;
                float glReferenceAlphaValue = guReferenceAlphaValue / 255.0f;

                if (isLogDebugEnabled) {
                	log("sceGuAlphaFunc(" + guFunc + "," + guReferenceAlphaValue + ")");
                }

                switch (guFunc) {
                    case ATST_NEVER_PASS_PIXEL:
                        glFunc = GL.GL_NEVER;
                        break;

                    case ATST_ALWAYS_PASS_PIXEL:
                        glFunc = GL.GL_ALWAYS;
                        break;

                    case ATST_PASS_PIXEL_IF_MATCHES:
                        glFunc = GL.GL_EQUAL;
                        break;

                    case ATST_PASS_PIXEL_IF_DIFFERS:
                        glFunc = GL.GL_NOTEQUAL;
                        break;

                    case ATST_PASS_PIXEL_IF_LESS:
                        glFunc = GL.GL_LESS;
                        break;

                    case ATST_PASS_PIXEL_IF_LESS_OR_EQUAL:
                        glFunc = GL.GL_LEQUAL;
                        break;

                    case ATST_PASS_PIXEL_IF_GREATER:
                        glFunc = GL.GL_GREATER;
                        break;

                    case ATST_PASS_PIXEL_IF_GREATER_OR_EQUAL:
                        glFunc = GL.GL_GEQUAL;
                        break;

                    default:
                        log.warn("sceGuAlphaFunc unhandled func " + guFunc);
                        break;
                }

                gl.glAlphaFunc(glFunc, glReferenceAlphaValue);

                break;
            }

            case STST: {

                int func = GL.GL_ALWAYS;

                switch (normalArgument & 0xFF) {
                    case STST_FUNCTION_NEVER_PASS_STENCIL_TEST:
                        func = GL.GL_NEVER;
                        break;

                    case STST_FUNCTION_ALWAYS_PASS_STENCIL_TEST:
                        func = GL.GL_ALWAYS;
                        break;

                    case STST_FUNCTION_PASS_TEST_IF_MATCHES:
                        func = GL.GL_EQUAL;
                        break;

                    case STST_FUNCTION_PASS_TEST_IF_DIFFERS:
                        func = GL.GL_NOTEQUAL;
                        break;

                    case STST_FUNCTION_PASS_TEST_IF_LESS:
                        func = GL.GL_LESS;
                        break;

                    case STST_FUNCTION_PASS_TEST_IF_LESS_OR_EQUAL:
                        func = GL.GL_LEQUAL;
                        break;

                    case STST_FUNCTION_PASS_TEST_IF_GREATER:
                        func = GL.GL_GREATER;
                        break;

                    case STST_FUNCTION_PASS_TEST_IF_GREATER_OR_EQUAL:
                        func = GL.GL_GEQUAL;
                        break;
                }

                int ref = (normalArgument >> 8) & 0xff;
                int mask = (normalArgument >> 16) & 0xff;
                gl.glStencilFunc(func, ref, mask);

                log("sceGuStencilFunc(func=" + (normalArgument & 0xFF) + ", ref=" + ref + ", mask=" + mask + ")");
                break;
            }

            case SOP: {
                int fail = getStencilOp(normalArgument & 0xFF);
                int zfail = getStencilOp((normalArgument >> 8) & 0xFF);
                int zpass = getStencilOp((normalArgument >> 16) & 0xFF);

                gl.glStencilOp(fail, zfail, zpass);
                break;
            }

            case ZTST: {
                int oldDepthFunc = depthFunc;

                depthFunc = normalArgument & 0xFF;
                if (depthFunc > ZTST_FUNCTION_PASS_PX_WHEN_DEPTH_IS_GREATER_OR_EQUAL) {
                	error(String.format("%s unknown depth function %d", commandToString(ZTST), depthFunc));
                	depthFunc = ZTST_FUNCTION_PASS_PX_WHEN_DEPTH_IS_LESS;
                }

                if (oldDepthFunc != depthFunc) {
                    depthChanged = true;
                }

                if (isLogDebugEnabled) {
                    log("sceGuDepthFunc(" + normalArgument + ")");
                }
                break;
            }

            case ALPHA: {
                int blend_mode = GL.GL_FUNC_ADD;
                int old_blend_src = blend_src;
                int old_blend_dst = blend_dst;
                blend_src = normalArgument & 0xF;
                blend_dst = (normalArgument >> 4) & 0xF;
                int op = (normalArgument >> 8) & 0xF;

                switch (op) {
                    case ALPHA_SOURCE_BLEND_OPERATION_ADD:
                        blend_mode = GL.GL_FUNC_ADD;
                        break;

                    case ALPHA_SOURCE_BLEND_OPERATION_SUBTRACT:
                        blend_mode = GL.GL_FUNC_SUBTRACT;
                        break;

                    case ALPHA_SOURCE_BLEND_OPERATION_REVERSE_SUBTRACT:
                        blend_mode = GL.GL_FUNC_REVERSE_SUBTRACT;
                        break;

                    case ALPHA_SOURCE_BLEND_OPERATION_MINIMUM_VALUE:
                        blend_mode = GL.GL_MIN;
                        break;

                    case ALPHA_SOURCE_BLEND_OPERATION_MAXIMUM_VALUE:
                        blend_mode = GL.GL_MAX;
                        break;

                    case ALPHA_SOURCE_BLEND_OPERATION_ABSOLUTE_VALUE:
                        blend_mode = GL.GL_FUNC_ADD;
                        break;

                    default:
                        error("Unhandled blend mode " + op);
                        break;
                }

                try {
                    gl.glBlendEquation(blend_mode);
                } catch (GLException e) {
                    log.warn("VideoEngine: " + e.getMessage());
                }

                if (old_blend_src != blend_src || old_blend_dst != blend_dst) {
                    blendChanged = true;
                }

                if (isLogDebugEnabled) {
                    log("sceGuBlendFunc(op=" + op + ", src=" + blend_src + ", dst=" + blend_dst + ")");
                }
                break;
            }

            case SFIX: {
                float old_sfix_color0 = sfix_color[0];
                float old_sfix_color1 = sfix_color[1];
                float old_sfix_color2 = sfix_color[2];
                sfix_color[0] = ((normalArgument) & 255) / 255.f;
                sfix_color[1] = ((normalArgument >> 8) & 255) / 255.f;
                sfix_color[2] = ((normalArgument >> 16) & 255) / 255.f;
                sfix_color[3] = 1.f;

                if (old_sfix_color0 != sfix_color[0] || old_sfix_color1 != sfix_color[1] || old_sfix_color2 != sfix_color[2]) {
                    blendChanged = true;
                }

                if (isLogDebugEnabled) {
                    log(String.format("%s : 0x%08X", helper.getCommandString(command), normalArgument));
                }
                break;
            }

            case DFIX: {
                float old_dfix_color0 = dfix_color[0];
                float old_dfix_color1 = dfix_color[1];
                float old_dfix_color2 = dfix_color[2];
                dfix_color[0] = ((normalArgument) & 255) / 255.f;
                dfix_color[1] = ((normalArgument >> 8) & 255) / 255.f;
                dfix_color[2] = ((normalArgument >> 16) & 255) / 255.f;
                dfix_color[3] = 1.f;

                if (old_dfix_color0 != dfix_color[0] || old_dfix_color1 != dfix_color[1] || old_dfix_color2 != dfix_color[2]) {
                    blendChanged = true;
                }

                if (isLogDebugEnabled) {
                    log(String.format("%s : 0x%08X", helper.getCommandString(command), normalArgument));
                }
                break;
            }

            case DTH0:
                dither_matrix[0] = (normalArgument) & 0xF;
                dither_matrix[1] = (normalArgument >> 4) & 0xF;
                dither_matrix[2] = (normalArgument >> 8) & 0xF;
                dither_matrix[3] = (normalArgument >> 12) & 0xF;
                break;

            case DTH1:
                dither_matrix[4] = (normalArgument) & 0xF;
                dither_matrix[5] = (normalArgument >> 4) & 0xF;
                dither_matrix[6] = (normalArgument >> 8) & 0xF;
                dither_matrix[7] = (normalArgument >> 12) & 0xF;
                break;

            case DTH2:
                dither_matrix[8] = (normalArgument) & 0xF;
                dither_matrix[9] = (normalArgument >> 4) & 0xF;
                dither_matrix[10] = (normalArgument >> 8) & 0xF;
                dither_matrix[11] = (normalArgument >> 12) & 0xF;
                break;

            case DTH3:
                dither_matrix[12] = (normalArgument) & 0xF;
                dither_matrix[13] = (normalArgument >> 4) & 0xF;
                dither_matrix[14] = (normalArgument >> 8) & 0xF;
                dither_matrix[15] = (normalArgument >> 12) & 0xF;

                // The dither matrix's values can vary between -8 and 7.
                // The most significant bit acts as sign bit.
                // Translate and log only at the last command.

                for (int i = 0; i < 16; i++) {
                    if (dither_matrix[i] > 7) {
                        dither_matrix[i] |= 0xFFFFFFF0;
                    }
                }

                if (isLogDebugEnabled) {
                    log.debug("DTH0:" + "  " + dither_matrix[0] + "  " + dither_matrix[1] + "  " + dither_matrix[2] + "  " + dither_matrix[3]);
                    log.debug("DTH1:" + "  " + dither_matrix[4] + "  " + dither_matrix[5] + "  " + dither_matrix[6] + "  " + dither_matrix[7]);
                    log.debug("DTH2:" + "  " + dither_matrix[8] + "  " + dither_matrix[9] + "  " + dither_matrix[10] + "  " + dither_matrix[11]);
                    log.debug("DTH3:" + "  " + dither_matrix[12] + "  " + dither_matrix[13] + "  " + dither_matrix[14] + "  " + dither_matrix[15]);
                }
                break;

            case LOP: {
            	re.setLogicOp(normalArgument & 0xF);
            	if (isLogDebugEnabled) {
            		log.debug("sceGuLogicalOp( LogicOp = " + normalArgument + "(" + getLOpName(normalArgument) + ")");
            	}
                break;
            }

            case ZMSK: {
                if (!clearMode) {
                    // NOTE: PSP depth mask as 1 is meant to avoid depth writes,
                    //		on pc it's the opposite
                    if (normalArgument != 0) {
                        gl.glDepthMask(false);
                    } else {
                        gl.glDepthMask(true);
                        // OpenGL requires the Depth parameters to be reloaded
                        depthChanged = true;
                    }

                    if (isLogDebugEnabled) {
                        log("sceGuDepthMask(" + (normalArgument != 0 ? "disableWrites" : "enableWrites") + ")");
                    }
                }
                break;
            }

            case PMSKC: {
                if (isLogDebugEnabled) {
                    log(String.format("%s color mask=0x%06X", helper.getCommandString(PMSKC), normalArgument));
                }
                colorMask[0] = normalArgument & 0xFF;
                colorMask[1] = (normalArgument >> 8) & 0xFF;
                colorMask[2] = (normalArgument >> 16) & 0xFF;
                if (!clearMode) {
                	re.setColorMask(colorMask[0], colorMask[1], colorMask[2], colorMask[3]);
                }
                break;
            }

            case PMSKA: {
                if (isLogDebugEnabled) {
                    log(String.format("%s alpha mask=0x%02X", helper.getCommandString(PMSKA), normalArgument));
                }
                colorMask[3] = normalArgument & 0xFF;
                if (!clearMode) {
                	re.setColorMask(colorMask[0], colorMask[1], colorMask[2], colorMask[3]);
                }
                break;
            }

            case TRXKICK:
                executeCommandTRXKICK(normalArgument);
                break;

            case TRXPOS:
                textureTx_sx = normalArgument & 0x1FF;
                textureTx_sy = (normalArgument >> 10) & 0x1FF;
                break;

            case TRXDPOS:
                textureTx_dx = normalArgument & 0x1FF;
                textureTx_dy = (normalArgument >> 10) & 0x1FF;
                break;

            case TRXSIZE:
                textureTx_width = (normalArgument & 0x3FF) + 1;
                textureTx_height = ((normalArgument >> 10) & 0x1FF) + 1;
                break;

            case UNKNOWNCOMMAND_0xFF: {
                // This command always appears before a BOFS command and seems to have
                // no special meaning.
                // The command also appears sometimes after a PRIM command.
                // Ignore the command in these cases.
                if (isLogInfoEnabled) {
                    Memory mem = Memory.getInstance();
                    int nextCommand = mem.read8(currentList.pc + 3);
                    int previousCommand = mem.read8(currentList.pc - 5);
                    if (normalArgument != 0) {
                        // normalArgument != 0 means that we are executing some random
                        // command list. Display this as an error, which will abort
                        // the list processing when too many errors are displayed.
                        error("Unknown/unimplemented video command [" + helper.getCommandString(command(instruction)) + "]" + getArgumentLog(normalArgument));
                    } else if (nextCommand != BOFS && previousCommand != PRIM && previousCommand != UNKNOWNCOMMAND_0xFF) {
                        if (isLogWarnEnabled) {
                            log.warn("Unknown/unimplemented video command [" + helper.getCommandString(command(instruction)) + "]" + getArgumentLog(normalArgument));
                        }
                    } else if (isLogDebugEnabled) {
                        log.debug("Ignored video command [" + helper.getCommandString(command(instruction)) + "]" + getArgumentLog(normalArgument));
                    }
                }
                break;
            }

            default:
                if (isLogWarnEnabled) {
                    log.warn("Unknown/unimplemented video command [" + helper.getCommandString(command(instruction)) + "]" + getArgumentLog(normalArgument));
                }
        }
        if (isLogInfoEnabled) {
            commandStatistics[command].end();
        }
    }

    private void executeCommandCLEAR(int normalArgument) {
        if (clearMode && (normalArgument & 1) == 0) {
            clearMode = false;
            depthFunc = clearModeDepthFunc;
            gl.glPopAttrib();
            // These attributes were not restored by glPopAttrib,
            // restore saved copy.
            gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_RGB_SCALE, clearModeRgbScale[0]);
            gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, clearModeTextureEnvMode[0]);

            enableShaders();
            log("clear mode end");
        } else if ((normalArgument & 1) != 0) {
            clearMode = true;
            // Save these attributes manually, they are not saved by glPushAttrib
            gl.glGetTexEnvfv(GL.GL_TEXTURE_ENV, GL.GL_RGB_SCALE, clearModeRgbScale, 0);
            gl.glGetTexEnviv(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, clearModeTextureEnvMode, 0);
            gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_RGB_SCALE, 1.0f);
            gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_REPLACE);

            gl.glPushAttrib(GL.GL_ENABLE_BIT | GL.GL_COLOR_BUFFER_BIT | GL.GL_STENCIL_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
//            gl.glPushAttrib(GL.GL_ALL_ATTRIB_BITS);
            gl.glDisable(GL.GL_BLEND);
            gl.glDisable(GL.GL_STENCIL_TEST);
            gl.glDisable(GL.GL_LIGHTING);
            gl.glDisable(GL.GL_TEXTURE_2D);
            gl.glDisable(GL.GL_ALPHA_TEST);
            gl.glDisable(GL.GL_FOG);
            gl.glDisable(GL.GL_DEPTH_TEST);
            gl.glDisable(GL.GL_LOGIC_OP);
            gl.glDisable(GL.GL_CULL_FACE);
            // TODO disable: scissor?

            disableShaders();

            // TODO Add more disabling in clear mode, we also need to reflect the change to the internal GE registers
            boolean color = false;
            boolean alpha = false;
            if ((normalArgument & 0x100) != 0) {
                color = true;
            }
            if ((normalArgument & 0x200) != 0) {
                alpha = true;
                // TODO Stencil not perfect, pspsdk clear code is doing more things
                gl.glEnable(GL.GL_STENCIL_TEST);
                gl.glStencilFunc(GL.GL_ALWAYS, 0, 0);
                gl.glStencilOp(GL.GL_KEEP, GL.GL_KEEP, GL.GL_ZERO);
            }
            if ((normalArgument & 0x400) != 0) {
                gl.glEnable(GL.GL_DEPTH_TEST);
                gl.glDepthMask(true);
            } else {
                gl.glDepthMask(false);
            }
            clearModeDepthFunc = depthFunc;
            depthFunc = ZTST_FUNCTION_ALWAYS_PASS_PIXEL;
            gl.glColorMask(color, color, color, alpha);
            if (isLogDebugEnabled) {
                log("clear mode : " + (normalArgument >> 8));
            }
        }

        blendChanged = true;
        lightingChanged = true;
        projectionMatrixUpload.setChanged(true);
        modelMatrixUpload.setChanged(true);
        viewMatrixUpload.setChanged(true);
        textureMatrixUpload.setChanged(true);
        viewportChanged = true;
        depthChanged = true;
        materialChanged = true;
    }

    private void executeCommandTFUNC(int normalArgument) {
    	int func = normalArgument & 0x7;
    	if (func >= TFUNC_FRAGMENT_DOUBLE_TEXTURE_EFECT_UNKNOW1) {
            VideoEngine.log.warn("Unimplemented tfunc mode " + func);
            func = TFUNC_FRAGMENT_DOUBLE_TEXTURE_EFECT_MODULATE;
    	}

        boolean alphaUsed = false;
        int alphaParam = (normalArgument >> 8) & 0x1;
        if (alphaParam == TFUNC_FRAGMENT_DOUBLE_TEXTURE_COLOR_ALPHA_IS_READ) {
            alphaUsed = true;
        } else if (alphaParam != TFUNC_FRAGMENT_DOUBLE_TEXTURE_COLOR_ALPHA_IS_IGNORED) {
            log.warn(String.format("sceGuTexFunc unknown alpha parameter %06X", normalArgument));
        }

        boolean colorDoubled = false;
        int rgbScaleParam = (normalArgument >> 16) & 0x1;
        if (rgbScaleParam == TFUNC_FRAGMENT_DOUBLE_ENABLE_COLOR_DOUBLED) {
        	colorDoubled = true;
        } else if (rgbScaleParam != TFUNC_FRAGMENT_DOUBLE_ENABLE_COLOR_UNTOUCHED) {
            log.warn(String.format("sceGuTexFunc unknown RGB scale parameter %06X", normalArgument));
        }

        re.setTextureFunc(func, alphaUsed, colorDoubled);

        if (isLogDebugEnabled) {
            log(String.format("sceGuTexFunc mode %06X", normalArgument)
                    + (((normalArgument & 0x10000) != 0) ? " SCALE" : "")
                    + (((normalArgument & 0x100) != 0) ? " ALPHA" : ""));
        }
    }

    private void executeCommandPRIM(int normalArgument) {
        int numberOfVertex = normalArgument & 0xFFFF;
        int type = ((normalArgument >> 16) & 0x7);

        Memory mem = Memory.getInstance();
        if (!mem.isAddressGood(vinfo.ptr_vertex)) {
            // Abort here to avoid a lot of useless memory read errors...
            error(helper.getCommandString(PRIM) + " Invalid vertex address 0x" + Integer.toHexString(vinfo.ptr_vertex));
            return;
        }

        if (type >= prim_mapping.length) {
            error(helper.getCommandString(PRIM) + " Type unhandled " + type);
            return;
        }

        updateGeBuf();
        somethingDisplayed = true;
        primCount++;

        loadTexture();

        // Logging
        if (isLogDebugEnabled) {
            switch (type) {
                case PRIM_POINT:
                    log("prim point " + numberOfVertex + "x");
                    break;
                case PRIM_LINE:
                    log("prim line " + (numberOfVertex / 2) + "x");
                    break;
                case PRIM_LINES_STRIPS:
                    log("prim lines_strips " + (numberOfVertex - 1) + "x");
                    break;
                case PRIM_TRIANGLE:
                    log("prim triangle " + (numberOfVertex / 3) + "x");
                    break;
                case PRIM_TRIANGLE_STRIPS:
                    log("prim triangle_strips " + (numberOfVertex - 2) + "x");
                    break;
                case PRIM_TRIANGLE_FANS:
                    log("prim triangle_fans " + (numberOfVertex - 2) + "x");
                    break;
                case PRIM_SPRITES:
                    log("prim sprites " + (numberOfVertex / 2) + "x");
                    break;
                default:
                    VideoEngine.log.warn("prim unhandled " + type);
                    break;
            }
        }

        boolean useVertexColor = initRendering();

        boolean useTexture = false;
        boolean useTextureFromNormal = false;
        boolean useTextureFromPosition = false;
        if (vinfo.texture != 0) {
            useTexture = true;
        } else if (textureFlag.isEnabled() && transform_mode == VTYPE_TRANSFORM_PIPELINE_TRANS_COORD) {
            switch (tex_proj_map_mode) {
                // What is the difference between MODE_NORMAL and MODE_NORMALIZED_NORMAL?
                case TMAP_TEXTURE_PROJECTION_MODE_NORMAL:
                case TMAP_TEXTURE_PROJECTION_MODE_NORMALIZED_NORMAL:
                    if (tex_proj_map_mode == TMAP_TEXTURE_PROJECTION_MODE_NORMALIZED_NORMAL) {
                        log.warn("Texture mode TMAP_TEXTURE_PROJECTION_MODE_NORMALIZED_NORMAL not tested");
                    }
                    if (vinfo.normal != 0) {
                        useTexture = true;
                        useTextureFromNormal = true;
                    }
                    break;
                case TMAP_TEXTURE_PROJECTION_MODE_POSITION:
                    if (vinfo.position != 0) {
                        useTexture = true;
                        useTextureFromPosition = true;
                    }
                    break;
            }
        }

        vertexStatistics.start();

        vinfo.setMorphWeights(morph_weight);
        vinfo.setDirty();

        int numberOfWeightsForShader = 0;
        if (useSkinningShaders) {
            if (vinfo.weight != 0) {
            	re.setBones(vinfo.skinningWeightCount, boneMatrixForShader);
                numberOfWeightsForShader = (vinfo.skinningWeightCount <= 4 ? 4 : 8);
            } else {
            	re.setBones(0, null);
            }
        }

        // Do not use optimized VertexInfo reading when tracing is enabled,
        // it doesn't produce any trace information
        if (!useVertexCache && vinfo.index == 0 && type != PRIM_SPRITES && mem.isAddressGood(vinfo.ptr_vertex) && !isLogTraceEnabled) {
            // Optimized VertexInfo reading:
            // - do not copy the info already available in the OpenGL format
            //   (native format), load it into nativeBuffer (a direct buffer
            //   is required by OpenGL).
            // - try to keep the info in "int" format when possible, convert
            //   to "float" only when necessary
            // The best case is no reading and no conversion at all when all the
            // vertex info are available in a format usable by OpenGL.
            //
            // The optimized reading cannot currently handle
            // indexed vertex info (vinfo.index != 0) and PRIM_SPRITES.
            //
            Buffer buffer = vertexInfoReader.read(vinfo, vinfo.ptr_vertex, numberOfVertex);

            enableClientState(useVertexColor, useTexture, numberOfWeightsForShader);

            int stride = vertexInfoReader.getStride();
            glBindBuffer();
            if (buffer != null) {
                if (useVBO) {
                    if (openGL1_5) {
                        gl.glBufferData(GL.GL_ARRAY_BUFFER, stride * numberOfVertex, buffer, GL.GL_STREAM_DRAW);
                    } else {
                        gl.glBufferDataARB(GL.GL_ARRAY_BUFFER, stride * numberOfVertex, buffer, GL.GL_STREAM_DRAW);
                    }
                } else {
                    vboBuffer.clear();
                    Utilities.putBuffer(vboBuffer, buffer, ByteOrder.nativeOrder());
                }
            }

            if (vertexInfoReader.hasNative()) {
                // Copy the VertexInfo from Memory to the nativeBuffer
                // (a direct buffer is required by glXXXPointer())
                nativeBuffer.clear();
                Buffer memBuffer = mem.getBuffer(vinfo.ptr_vertex, vinfo.vertexSize * numberOfVertex);
                Utilities.putBuffer(nativeBuffer, memBuffer, ByteOrder.LITTLE_ENDIAN);
            }

            if (vinfo.texture != 0 || useTexture) {
                boolean textureNative;
                int textureOffset;
                int textureType;
                if (useTextureFromNormal) {
                    textureNative = vertexInfoReader.isNormalNative();
                    textureOffset = vertexInfoReader.getNormalOffset();
                    textureType = vertexInfoReader.getNormalType();
                } else if (useTextureFromPosition) {
                    textureNative = vertexInfoReader.isPositionNative();
                    textureOffset = vertexInfoReader.getPositionOffset();
                    textureType = vertexInfoReader.getPositionType();
                } else {
                    textureNative = vertexInfoReader.isTextureNative();
                    textureOffset = vertexInfoReader.getTextureOffset();
                    textureType = vertexInfoReader.getTextureType();
                }
                glTexCoordPointer(useTexture, textureType, stride, textureOffset, textureNative, false, true);
            }

            glColorPointer(useVertexColor, vertexInfoReader.getColorType(), stride, vertexInfoReader.getColorOffset(), vertexInfoReader.isColorNative(), false, true);
            glNormalPointer(vertexInfoReader.getNormalType(), stride, vertexInfoReader.getNormalOffset(), vertexInfoReader.isNormalNative(), false, true);
            glVertexPointer(vertexInfoReader.getPositionType(), stride, vertexInfoReader.getPositionOffset(), vertexInfoReader.isPositionNative(), false, true);

            gl.glDrawArrays(prim_mapping[type], 0, numberOfVertex);

        } else {
            // Non-optimized VertexInfo reading

            VertexInfo cachedVertexInfo = null;
            if (useVertexCache) {
                vertexCacheLookupStatistics.start();
                cachedVertexInfo = VertexCache.getInstance().getVertex(vinfo, numberOfVertex, bone_uploaded_matrix, numberOfWeightsForShader);
                vertexCacheLookupStatistics.end();
            }
            vboFloatBuffer.clear();

            switch (type) {
                case PRIM_POINT:
                case PRIM_LINE:
                case PRIM_LINES_STRIPS:
                case PRIM_TRIANGLE:
                case PRIM_TRIANGLE_STRIPS:
                case PRIM_TRIANGLE_FANS:
                    if (cachedVertexInfo == null) {
                        for (int i = 0; i < numberOfVertex; i++) {
                            int addr = vinfo.getAddress(mem, i);

                            VertexState v = vinfo.readVertex(mem, addr);

                            // Do skinning first as it modifies v.p and v.n
                            if (vinfo.weight != 0 && vinfo.position != 0 && !useSkinningShaders) {
                                doSkinning(vinfo, v);
                            }

                            if (vinfo.texture != 0) {
                                vboFloatBuffer.put(v.t);
                            } else if (useTextureFromNormal) {
                                vboFloatBuffer.put(v.n, 0, 2);
                            } else if (useTextureFromPosition) {
                                vboFloatBuffer.put(v.p, 0, 2);
                            }
                            if (useVertexColor) {
                                vboFloatBuffer.put(v.c);
                            }
                            if (vinfo.normal != 0) {
                                vboFloatBuffer.put(v.n);
                            }
                            if (vinfo.position != 0) {
                                vboFloatBuffer.put(v.p);
                            }
                            if (numberOfWeightsForShader > 0) {
                                vboFloatBuffer.put(v.boneWeights, 0, numberOfWeightsForShader);
                            }

                            if (isLogTraceEnabled) {
                                if (vinfo.texture != 0 && vinfo.position != 0) {
                                    log.trace("  vertex#" + i + " (" + ((int) v.t[0]) + "," + ((int) v.t[1]) + ") at (" + ((int) v.p[0]) + "," + ((int) v.p[1]) + "," + ((int) v.p[2]) + ")");
                                }
                            }
                        }

                        if (useVBO) {
                            if (useVertexCache) {
                                cachedVertexInfo = new VertexInfo(vinfo);
                                VertexCache.getInstance().addVertex(gl, cachedVertexInfo, numberOfVertex, bone_uploaded_matrix, numberOfWeightsForShader);
                                int size = vboFloatBuffer.position();
                                vboFloatBuffer.rewind();
                                cachedVertexInfo.loadVertex(gl, vboFloatBuffer, size);
                            } else {
                                glBindBuffer();
                                if (openGL1_5) {
                                    gl.glBufferData(GL.GL_ARRAY_BUFFER, vboFloatBuffer.position() * BufferUtil.SIZEOF_FLOAT, vboFloatBuffer.rewind(), GL.GL_STREAM_DRAW);
                                } else {
                                    gl.glBufferDataARB(GL.GL_ARRAY_BUFFER, vboFloatBuffer.position() * BufferUtil.SIZEOF_FLOAT, vboFloatBuffer.rewind(), GL.GL_STREAM_DRAW);
                                }
                            }
                        }
                    } else {
                        if (isLogDebugEnabled) {
                            log.debug("Reusing cached Vertex Data");
                        }
                        cachedVertexInfo.bindVertex(gl);
                    }
                    bindBuffers(useVertexColor, useTexture, vinfo.normal != 0, false, numberOfWeightsForShader);
                    gl.glDrawArrays(prim_mapping[type], 0, numberOfVertex);
                    maxSpriteHeight = Integer.MAX_VALUE;
                    break;

                case PRIM_SPRITES:
                    gl.glPushAttrib(GL.GL_ENABLE_BIT);
                    gl.glDisable(GL.GL_CULL_FACE);
                    if (cachedVertexInfo == null) {
                        for (int i = 0; i < numberOfVertex; i += 2) {
                            int addr1 = vinfo.getAddress(mem, i);
                            int addr2 = vinfo.getAddress(mem, i + 1);
                            VertexState v1 = vinfo.readVertex(mem, addr1);
                            VertexState v2 = vinfo.readVertex(mem, addr2);

                            v1.p[2] = v2.p[2];

                            if (v2.p[1] > maxSpriteHeight) {
                                maxSpriteHeight = (int) v2.p[1];
                            }

                            // Flipped:
                            //  sprite (16,0)-(0,56) at (0,16,65535)-(56,0,65535)
                            // Not flipped:
                            //	sprite (24,0)-(0,48) at (226,120,0)-(254,178,0)
                            boolean flippedTexture = v1.t[0] > v2.t[0] && v1.p[1] > v2.p[1];

                            if (flippedTexture && isLogInfoEnabled) {
                                log.info("  sprite (" + ((int) v1.t[0]) + "," + ((int) v1.t[1]) + ")-(" + ((int) v2.t[0]) + "," + ((int) v2.t[1]) + ") at (" + ((int) v1.p[0]) + "," + ((int) v1.p[1]) + "," + ((int) v1.p[2]) + ")-(" + +((int) v2.p[0]) + "," + ((int) v2.p[1]) + "," + ((int) v2.p[2]) + ") flipped");
                            } else if (isLogDebugEnabled && transform_mode == VTYPE_TRANSFORM_PIPELINE_RAW_COORD) {
                                log("  sprite (" + ((int) v1.t[0]) + "," + ((int) v1.t[1]) + ")-(" + ((int) v2.t[0]) + "," + ((int) v2.t[1]) + ") at (" + ((int) v1.p[0]) + "," + ((int) v1.p[1]) + "," + ((int) v1.p[2]) + ")-(" + +((int) v2.p[0]) + "," + ((int) v2.p[1]) + "," + ((int) v2.p[2]) + ")");
                            }

                            // V1
                            if (vinfo.texture != 0) {
                                vboFloatBuffer.put(v1.t);
                            }
                            if (useVertexColor) {
                                vboFloatBuffer.put(v2.c);
                            }
                            if (vinfo.normal != 0) {
                                vboFloatBuffer.put(v2.n);
                            }
                            if (vinfo.position != 0) {
                                vboFloatBuffer.put(v1.p);
                            }

                            if (vinfo.texture != 0) {
                                if (flippedTexture) {
                                    vboFloatBuffer.put(v2.t[0]).put(v1.t[1]);
                                } else {
                                    vboFloatBuffer.put(v1.t[0]).put(v2.t[1]);
                                }
                            }
                            if (useVertexColor) {
                                vboFloatBuffer.put(v2.c);
                            }
                            if (vinfo.normal != 0) {
                                vboFloatBuffer.put(v2.n);
                            }
                            if (vinfo.position != 0) {
                                vboFloatBuffer.put(v1.p[0]).put(v2.p[1]).put(v2.p[2]);
                            }

                            // V2
                            if (vinfo.texture != 0) {
                                vboFloatBuffer.put(v2.t);
                            }
                            if (useVertexColor) {
                                vboFloatBuffer.put(v2.c);
                            }
                            if (vinfo.normal != 0) {
                                vboFloatBuffer.put(v2.n);
                            }
                            if (vinfo.position != 0) {
                                vboFloatBuffer.put(v2.p);
                            }

                            if (vinfo.texture != 0) {
                                if (flippedTexture) {
                                    vboFloatBuffer.put(v1.t[0]).put(v2.t[1]);
                                } else {
                                    vboFloatBuffer.put(v2.t[0]).put(v1.t[1]);
                                }
                            }
                            if (useVertexColor) {
                                vboFloatBuffer.put(v2.c);
                            }
                            if (vinfo.normal != 0) {
                                vboFloatBuffer.put(v2.n);
                            }
                            if (vinfo.position != 0) {
                                vboFloatBuffer.put(v2.p[0]).put(v1.p[1]).put(v2.p[2]);
                            }
                        }
                        if (useVBO) {
                            if (useVertexCache) {
                                cachedVertexInfo = new VertexInfo(vinfo);
                                VertexCache.getInstance().addVertex(gl, cachedVertexInfo, numberOfVertex, bone_uploaded_matrix, numberOfWeightsForShader);
                                int size = vboFloatBuffer.position();
                                vboFloatBuffer.rewind();
                                cachedVertexInfo.loadVertex(gl, vboFloatBuffer, size);
                            } else {
                                glBindBuffer();
                                if (openGL1_5) {
                                    gl.glBufferData(GL.GL_ARRAY_BUFFER, vboFloatBuffer.position() * BufferUtil.SIZEOF_FLOAT, vboFloatBuffer.rewind(), GL.GL_STREAM_DRAW);
                                } else {
                                    gl.glBufferDataARB(GL.GL_ARRAY_BUFFER, vboFloatBuffer.position() * BufferUtil.SIZEOF_FLOAT, vboFloatBuffer.rewind(), GL.GL_STREAM_DRAW);
                                }
                            }
                        }
                    } else {
                        if (isLogDebugEnabled) {
                            log.debug("Reusing cached Vertex Data");
                        }
                        cachedVertexInfo.bindVertex(gl);
                    }
                    bindBuffers(useVertexColor, useTexture, vinfo.normal != 0, false, 0);
                    gl.glDrawArrays(GL.GL_QUADS, 0, numberOfVertex * 2);
                    gl.glPopAttrib();
                    break;
            }
        }

        vertexStatistics.end();

        // Don't capture the ram if the vertex list is embedded in the display list. TODO handle stall_addr == 0 better
        // TODO may need to move inside the loop if indices are used, or find the largest index so we can calculate the size of the vertex list
        if (State.captureGeNextFrame) {
    		if (!isVertexBufferEmbedded()) {
	            log.info("Capture PRIM");
	            CaptureManager.captureRAM(vinfo.ptr_vertex, vinfo.vertexSize * numberOfVertex);
    		}
            display.captureGeImage(gl);
        }

        endRendering(useVertexColor, useTexture, numberOfVertex);
    }

    private void executeCommandTRXKICK(int normalArgument) {
        textureTx_pixelSize = normalArgument & 0x1;

        textureTx_sourceAddress &= Memory.addressMask;
        textureTx_destinationAddress &= Memory.addressMask;

        if (isLogDebugEnabled) {
            log(helper.getCommandString(TRXKICK) + " from 0x" + Integer.toHexString(textureTx_sourceAddress) + "(" + textureTx_sx + "," + textureTx_sy + ") to 0x" + Integer.toHexString(textureTx_destinationAddress) + "(" + textureTx_dx + "," + textureTx_dy + "), width=" + textureTx_width + ", height=" + textureTx_height + ", pixelSize=" + textureTx_pixelSize);
        }

        usingTRXKICK = true;
        updateGeBuf();

        int pixelFormatGe = psm;
        int bpp = (textureTx_pixelSize == TRXKICK_16BIT_TEXEL_SIZE) ? 2 : 4;
        int bppGe = sceDisplay.getPixelFormatBytes(pixelFormatGe);

        memoryForGEUpdated();

        if (!display.isGeAddress(textureTx_destinationAddress) || bpp != bppGe) {
            if (isLogDebugEnabled) {
                if (bpp != bppGe) {
                    log(helper.getCommandString(TRXKICK) + " BPP not compatible with GE");
                } else {
                    log(helper.getCommandString(TRXKICK) + " not in Ge Address space");
                }
            }
            int width = textureTx_width;
            int height = textureTx_height;

            int srcAddress = textureTx_sourceAddress + (textureTx_sy * textureTx_sourceLineWidth + textureTx_sx) * bpp;
            int dstAddress = textureTx_destinationAddress + (textureTx_dy * textureTx_destinationLineWidth + textureTx_dx) * bpp;
            Memory memory = Memory.getInstance();
            if (textureTx_sourceLineWidth == width && textureTx_destinationLineWidth == width) {
                // All the lines are adjacent in memory,
                // copy them all in a single memcpy operation.
                int copyLength = height * width * bpp;
                if (isLogDebugEnabled) {
                    log(String.format("%s memcpy(0x%08X-0x%08X, 0x%08X, 0x%X)", helper.getCommandString(TRXKICK), dstAddress, dstAddress + copyLength, srcAddress, copyLength));
                }
                memory.memcpy(dstAddress, srcAddress, copyLength);
            } else {
                // The lines are not adjacent in memory: copy line by line.
                int copyLength = width * bpp;
                int srcLineLength = textureTx_sourceLineWidth * bpp;
                int dstLineLength = textureTx_destinationLineWidth * bpp;
                for (int y = 0; y < height; y++) {
                    if (isLogDebugEnabled) {
                        log(String.format("%s memcpy(0x%08X-0x%08X, 0x%08X, 0x%X)", helper.getCommandString(TRXKICK), dstAddress, dstAddress + copyLength, srcAddress, copyLength));
                    }
                    memory.memcpy(dstAddress, srcAddress, copyLength);
                    srcAddress += srcLineLength;
                    dstAddress += dstLineLength;
                }
            }

            if (State.captureGeNextFrame) {
                log.warn("TRXKICK outside of Ge Address space not supported in capture yet");
            }
        } else {
            int width = textureTx_width;
            int height = textureTx_height;
            int dx = textureTx_dx;
            int dy = textureTx_dy;
            int lineWidth = textureTx_sourceLineWidth;

            int geAddr = display.getTopAddrGe();
            dy += (textureTx_destinationAddress - geAddr) / (display.getBufferWidthGe() * bpp);
            dx += ((textureTx_destinationAddress - geAddr) % (display.getBufferWidthGe() * bpp)) / bpp;

            if (isLogDebugEnabled) {
                log(helper.getCommandString(TRXKICK) + " in Ge Address space: dx=" + dx + ", dy=" + dy + ", width=" + width + ", height=" + height + ", lineWidth=" + lineWidth + ", bpp=" + bpp);
            }

            int[] textures = new int[1];
            gl.glGenTextures(1, textures, 0);
            int texture = textures[0];
            gl.glBindTexture(GL.GL_TEXTURE_2D, texture);

            gl.glPushAttrib(GL.GL_ENABLE_BIT);
            gl.glDisable(GL.GL_DEPTH_TEST);
            gl.glDisable(GL.GL_BLEND);
            gl.glDisable(GL.GL_ALPHA_TEST);
            gl.glDisable(GL.GL_FOG);
            gl.glDisable(GL.GL_LIGHTING);
            gl.glDisable(GL.GL_LOGIC_OP);
            gl.glDisable(GL.GL_STENCIL_TEST);
            gl.glDisable(GL.GL_SCISSOR_TEST);

            gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, bpp);
            gl.glPixelStorei(GL.GL_UNPACK_ROW_LENGTH, lineWidth);

            gl.glMatrixMode(GL.GL_PROJECTION);
            gl.glPushMatrix();
            gl.glLoadIdentity();
            gl.glOrtho(0, 480, 272, 0, -1, 1);
            gl.glMatrixMode(GL.GL_MODELVIEW);
            gl.glPushMatrix();
            gl.glLoadIdentity();

            Buffer buffer = Memory.getInstance().getBuffer(textureTx_sourceAddress, lineWidth * height * bpp);

            if (State.captureGeNextFrame) {
                log.info("Capture TRXKICK");
                CaptureManager.captureRAM(textureTx_sourceAddress, lineWidth * height * bpp);
            }

            //
            // glTexImage2D only supports
            //		width = (1 << n)	for some integer n
            //		height = (1 << m)	for some integer m
            //
            // This the reason why we are also using glTexSubImage2D.
            //
            int bufferHeight = Utilities.makePow2(height);
            int pixelFormatGL = sceDisplay.getPixelFormatGL(pixelFormatGe);
            int formatGL = sceDisplay.getFormatGL(pixelFormatGe);
            gl.glTexImage2D(
                    GL.GL_TEXTURE_2D, 0,
                    GL.GL_RGBA,
                    lineWidth, bufferHeight, 0,
                    formatGL, pixelFormatGL, null);

            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP);

            gl.glTexSubImage2D(
                    GL.GL_TEXTURE_2D, 0,
                    textureTx_sx, textureTx_sy, width, height,
                    formatGL, pixelFormatGL, buffer);

            gl.glEnable(GL.GL_TEXTURE_2D);

            gl.glBegin(GL.GL_QUADS);
            gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

            float texCoordX = width / (float) lineWidth;
            float texCoordY = height / (float) bufferHeight;

            gl.glTexCoord2f(0.0f, 0.0f);
            gl.glVertex2i(dx, dy);

            gl.glTexCoord2f(texCoordX, 0.0f);
            gl.glVertex2i(dx + width, dy);

            gl.glTexCoord2f(texCoordX, texCoordY);
            gl.glVertex2i(dx + width, dy + height);

            gl.glTexCoord2f(0.0f, texCoordY);
            gl.glVertex2i(dx, dy + height);

            gl.glEnd();

            gl.glMatrixMode(GL.GL_MODELVIEW);
            gl.glPopMatrix();
            gl.glMatrixMode(GL.GL_PROJECTION);
            gl.glPopMatrix();

            gl.glPopAttrib();

            gl.glDeleteTextures(1, textures, 0);
        }
    }

    public void disableShaders() {
        if (useShaders) {
            gl.glUniform1f(Uniforms.zPos.getId(), 0);
            gl.glUniform1f(Uniforms.zScale.getId(), 0);
            gl.glUniform1i(Uniforms.texEnable.getId(), 0);
            gl.glUniform1i(Uniforms.lightingEnable.getId(), 0);
            gl.glUniform1i(Uniforms.numberBones.getId(), 0);
            gl.glUniform1i(Uniforms.ctestEnable.getId(), 0);
        }
    }

    public void enableShaders() {
        if (useShaders) {
            gl.glUniform1f(Uniforms.zPos.getId(), zpos);
            gl.glUniform1f(Uniforms.zScale.getId(), zscale);
            gl.glUniform1i(Uniforms.texEnable.getId(), textureFlag.isEnabledInt());
            gl.glUniform1i(Uniforms.lightingEnable.getId(), lightingFlag.isEnabledInt());
            gl.glUniform1i(Uniforms.ctestEnable.getId(), colorTestFlag.isEnabledInt());
        }
    }

    private void executeCommandBBOX(int normalArgument) {
        int numberOfVertexBoundingBox = normalArgument;

        if (vinfo.position == 0) {
            log.warn(helper.getCommandString(BBOX) + " no positions for vertex!");
            return;
        } else if (!glQueryAvailable) {
            log.info("Not supported by your OpenGL version (but can be ignored): " + helper.getCommandString(BBOX) + " numberOfVertex=" + numberOfVertexBoundingBox);
            return;
        } else if ((numberOfVertexBoundingBox % 8) != 0) {
            // How to interpret non-multiple of 8?
            log.warn(helper.getCommandString(BBOX) + " unsupported numberOfVertex=" + numberOfVertexBoundingBox);
        } else if (isLogDebugEnabled) {
            log.debug(helper.getCommandString(BBOX) + " numberOfVertex=" + numberOfVertexBoundingBox);
        }

        Memory mem = Memory.getInstance();
        boolean useVertexColor = initRendering();

        // Bounding box should not be displayed, disable all drawings
        gl.glPushAttrib(GL.GL_ENABLE_BIT | GL.GL_COLOR_BUFFER_BIT | GL.GL_STENCIL_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glColorMask(false, false, false, false);
        gl.glDepthMask(false);
        gl.glDisable(GL.GL_BLEND);
        gl.glDisable(GL.GL_STENCIL_TEST);
        gl.glDisable(GL.GL_LIGHTING);
        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glDisable(GL.GL_ALPHA_TEST);
        gl.glDisable(GL.GL_FOG);
        gl.glDisable(GL.GL_DEPTH_TEST);
        gl.glDisable(GL.GL_LOGIC_OP);
        gl.glDisable(GL.GL_CULL_FACE);
        gl.glDisable(GL.GL_SCISSOR_TEST);

        disableShaders();

        gl.glBeginQuery(GL.GL_SAMPLES_PASSED, bboxQueryId);
        //
        // The bounding box is a cube defined by 8 vertices.
        // It is not clear if the vertices have to be listed in a pre-defined order.
        // Which primitive should be used?
        // - GL_TRIANGLE_STRIP: we only draw 3 faces of the cube
        // - GL_QUADS: how are organized the 8 vertices to draw all the cube faces?
        //
        gl.glBegin(GL.GL_QUADS);
        for (int i = 0; i < numberOfVertexBoundingBox; i++) {
            int addr = vinfo.getAddress(mem, i);

            VertexState v = vinfo.readVertex(mem, addr);
            if (isLogDebugEnabled) {
                log.debug(String.format("%s (%f,%f,%f)", helper.getCommandString(BBOX), v.p[0], v.p[1], v.p[2]));
            }

            int vertexIndex = i % 8;
            bboxVertices[vertexIndex][0] = v.p[0];
            bboxVertices[vertexIndex][1] = v.p[1];
            bboxVertices[vertexIndex][2] = v.p[2];

            if (vertexIndex == 7) {
                //
                // Cube from BBOX:
                //
                // BBOX Front face:
                //  2---3
                //  |   |
                //  |   |
                //  0---1
                //
                // BBOX Back face:
                //  6---7
                //  |   |
                //  |   |
                //  4---5
                //
                // OpenGL QUAD:
                //  3---2
                //  |   |
                //  |   |
                //  0---1
                //

                // Front face
                gl.glVertex3fv(bboxVertices[0], 0);
                gl.glVertex3fv(bboxVertices[1], 0);
                gl.glVertex3fv(bboxVertices[3], 0);
                gl.glVertex3fv(bboxVertices[2], 0);

                // Back face
                gl.glVertex3fv(bboxVertices[4], 0);
                gl.glVertex3fv(bboxVertices[5], 0);
                gl.glVertex3fv(bboxVertices[7], 0);
                gl.glVertex3fv(bboxVertices[6], 0);

                // Right face
                gl.glVertex3fv(bboxVertices[1], 0);
                gl.glVertex3fv(bboxVertices[5], 0);
                gl.glVertex3fv(bboxVertices[7], 0);
                gl.glVertex3fv(bboxVertices[3], 0);

                // Left face
                gl.glVertex3fv(bboxVertices[0], 0);
                gl.glVertex3fv(bboxVertices[4], 0);
                gl.glVertex3fv(bboxVertices[6], 0);
                gl.glVertex3fv(bboxVertices[2], 0);

                // Top face
                gl.glVertex3fv(bboxVertices[2], 0);
                gl.glVertex3fv(bboxVertices[3], 0);
                gl.glVertex3fv(bboxVertices[7], 0);
                gl.glVertex3fv(bboxVertices[6], 0);

                // Bottom face
                gl.glVertex3fv(bboxVertices[0], 0);
                gl.glVertex3fv(bboxVertices[1], 0);
                gl.glVertex3fv(bboxVertices[5], 0);
                gl.glVertex3fv(bboxVertices[4], 0);
            }
        }
        gl.glEnd();
        gl.glEndQuery(GL.GL_SAMPLES_PASSED);
        gl.glPopAttrib();

        enableShaders();

        endRendering(useVertexColor, false, numberOfVertexBoundingBox);
    }

    private void executeCommandBJUMP(int normalArgument) {
        takeConditionalJump = false;

        if (glQueryAvailable) {
            int[] result = new int[1];
            boolean resultAvailable = false;

            // Wait for query result available
            for (int i = 0; i < 10000; i++) {
                gl.glGetQueryObjectiv(bboxQueryId, GL.GL_QUERY_RESULT_AVAILABLE, result, 0);
                if (isLogTraceEnabled) {
                    log.trace("glGetQueryObjectiv result available " + result[0]);
                }

                // 0 means result not yet available, 1 means result available
                if (result[0] != 0) {
                    resultAvailable = true;

                    // Retrieve query result (number of visible samples)
                    gl.glGetQueryObjectiv(bboxQueryId, GL.GL_QUERY_RESULT, result, 0);
                    if (isLogTraceEnabled) {
                        log.trace("glGetQueryObjectiv result " + result[0]);
                    }

                    // 0 samples visible means the bounding box was occluded (not visible)
                    if (result[0] == 0) {
                        takeConditionalJump = true;
                    }
                    break;
                }
            }

            if (!resultAvailable) {
                if (isLogWarnEnabled) {
                    log.warn(helper.getCommandString(BJUMP) + " glQuery result not available in due time");
                }
            }
        }

        if (takeConditionalJump) {
            int oldPc = currentList.pc;
            currentList.jump(normalArgument);
            int newPc = currentList.pc;
            if (isLogDebugEnabled) {
                log(String.format("%s old PC: 0x%08X, new PC: 0x%08X", helper.getCommandString(BJUMP), oldPc, newPc));
            }
        } else {
            if (isLogDebugEnabled) {
                log(helper.getCommandString(BJUMP) + " not taking Conditional Jump");
            }
        }
    }

    private void enableClientState(boolean useVertexColor, boolean useTexture, int numberOfWeightsForShader) {
        if (vinfo.texture != 0 || useTexture) {
            gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);
        }
        if (useVertexColor) {
            gl.glEnableClientState(GL.GL_COLOR_ARRAY);
        }
        if (vinfo.normal != 0) {
            gl.glEnableClientState(GL.GL_NORMAL_ARRAY);
        }
        if (numberOfWeightsForShader > 0) {
            gl.glEnableVertexAttribArray(shaderAttribWeights1);
            if (numberOfWeightsForShader > 4) {
                gl.glEnableVertexAttribArray(shaderAttribWeights2);
            }
        }
        gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
    }

    private void glTexCoordPointer(boolean useTexture, int type, int stride, int offset, boolean isNative, boolean useVboFloatBuffer, boolean doBindBuffer) {
        if (vinfo.texture != 0 || useTexture) {
            if (isNative) {
                if (doBindBuffer) {
                    glBindBuffer(0);
                }
                gl.glTexCoordPointer(2, type, vinfo.vertexSize, nativeBuffer.position(offset));
            } else {
                if (doBindBuffer) {
                    glBindBuffer();
                }
                if (useVBO) {
                    gl.glTexCoordPointer(2, type, stride, offset);
                } else if (useVboFloatBuffer) {
                    gl.glTexCoordPointer(2, type, stride, vboFloatBuffer.position(offset / BufferUtil.SIZEOF_FLOAT));
                } else {
                    gl.glTexCoordPointer(2, type, stride, vboBuffer.position(offset));
                }
            }
        }
    }

    private void glColorPointer(boolean useVertexColor, int type, int stride, int offset, boolean isNative, boolean useVboFloatBuffer, boolean doBindBuffer) {
        if (useVertexColor) {
            if (isNative) {
                if (doBindBuffer) {
                    glBindBuffer(0);
                }
                gl.glColorPointer(4, type, vinfo.vertexSize, nativeBuffer.position(offset));
            } else {
                if (doBindBuffer) {
                    glBindBuffer();
                }
                if (useVBO) {
                    gl.glColorPointer(4, type, stride, offset);
                } else if (useVboFloatBuffer) {
                    gl.glColorPointer(4, type, stride, vboFloatBuffer.position(offset / BufferUtil.SIZEOF_FLOAT));
                } else {
                    gl.glColorPointer(4, type, stride, vboBuffer.position(offset));
                }
            }
        }
    }

    private void glVertexPointer(int type, int stride, int offset, boolean isNative, boolean useVboFloatBuffer, boolean doBindBuffer) {
        if (isNative) {
            if (doBindBuffer) {
                glBindBuffer(0);
            }
            gl.glVertexPointer(3, type, vinfo.vertexSize, nativeBuffer.position(offset));
        } else {
            if (doBindBuffer) {
                glBindBuffer();
            }
            if (useVBO) {
                gl.glVertexPointer(3, type, stride, offset);
            } else if (useVboFloatBuffer) {
                gl.glVertexPointer(3, type, stride, vboFloatBuffer.position(offset / BufferUtil.SIZEOF_FLOAT));
            } else {
                gl.glVertexPointer(3, type, stride, vboBuffer.position(offset));
            }
        }

    }

    private void glNormalPointer(int type, int stride, int offset, boolean isNative, boolean useVboFloatBuffer, boolean doBindBuffer) {
        if (vinfo.normal != 0) {
            if (isNative) {
                if (doBindBuffer) {
                    glBindBuffer(0);
                }
                gl.glNormalPointer(type, vinfo.vertexSize, nativeBuffer.position(offset));
            } else {
                if (doBindBuffer) {
                    glBindBuffer();
                }
                if (useVBO) {
                    gl.glNormalPointer(type, stride, offset);
                } else if (useVboFloatBuffer) {
                    gl.glNormalPointer(type, stride, vboFloatBuffer.position(offset / BufferUtil.SIZEOF_FLOAT));
                } else {
                    gl.glNormalPointer(type, stride, vboBuffer.position(offset));
                }
            }
        }
    }

    private void bindBuffers(boolean useVertexColor, boolean useTexture, boolean useNormal, boolean doBindBuffer, int numberOfWeightsForShader) {
        int stride = 0, cpos = 0, npos = 0, vpos = 0, wpos1 = 0, wpos2 = 0;

        if (vinfo.texture != 0 || useTexture) {
            stride += BufferUtil.SIZEOF_FLOAT * 2;
            cpos = npos = vpos = stride;
        }
        if (useVertexColor) {
            stride += BufferUtil.SIZEOF_FLOAT * 4;
            npos = vpos = stride;
        }
        if (useNormal) {
            stride += BufferUtil.SIZEOF_FLOAT * 3;
            vpos = stride;
        }
        stride += BufferUtil.SIZEOF_FLOAT * 3;
        if (numberOfWeightsForShader > 0) {
            wpos1 = stride;
            stride += BufferUtil.SIZEOF_FLOAT * 4;
            if (numberOfWeightsForShader > 4) {
                wpos2 = stride;
                stride += BufferUtil.SIZEOF_FLOAT * 4;
            }
        }

        enableClientState(useVertexColor, useTexture, numberOfWeightsForShader);
        if (doBindBuffer) {
            glBindBuffer();
        }
        glTexCoordPointer(useTexture, GL.GL_FLOAT, stride, 0, false, true, false);
        glColorPointer(useVertexColor, GL.GL_FLOAT, stride, cpos, false, true, false);
        glNormalPointer(GL.GL_FLOAT, stride, npos, false, true, false);
        if (numberOfWeightsForShader > 0) {
            gl.glVertexAttribPointer(shaderAttribWeights1, 4, GL.GL_FLOAT, false, stride, wpos1);
            if (numberOfWeightsForShader > 4) {
                gl.glVertexAttribPointer(shaderAttribWeights2, 4, GL.GL_FLOAT, false, stride, wpos2);
            }
        }
        glVertexPointer(GL.GL_FLOAT, stride, vpos, false, true, false);
    }

    public void doPositionSkinning(VertexInfo vinfo, float[] boneWeights, float[] position) {
        float x = 0, y = 0, z = 0;
        for (int i = 0; i < vinfo.skinningWeightCount; i++) {
            if (boneWeights[i] != 0) {
                x += (position[0] * bone_uploaded_matrix[i][0]
                        + position[1] * bone_uploaded_matrix[i][3]
                        + position[2] * bone_uploaded_matrix[i][6]
                        + bone_uploaded_matrix[i][9]) * boneWeights[i];

                y += (position[0] * bone_uploaded_matrix[i][1]
                        + position[1] * bone_uploaded_matrix[i][4]
                        + position[2] * bone_uploaded_matrix[i][7]
                        + bone_uploaded_matrix[i][10]) * boneWeights[i];

                z += (position[0] * bone_uploaded_matrix[i][2]
                        + position[1] * bone_uploaded_matrix[i][5]
                        + position[2] * bone_uploaded_matrix[i][8]
                        + bone_uploaded_matrix[i][11]) * boneWeights[i];
            }
        }

        position[0] = x;
        position[1] = y;
        position[2] = z;
    }

    public void doNormalSkinning(VertexInfo vinfo, float[] boneWeights, float[] normal) {
        float nx = 0, ny = 0, nz = 0;
        for (int i = 0; i < vinfo.skinningWeightCount; i++) {
            if (boneWeights[i] != 0) {
                // Normals shouldn't be translated :)
                nx += (normal[0] * bone_uploaded_matrix[i][0]
                        + normal[1] * bone_uploaded_matrix[i][3]
                        + normal[2] * bone_uploaded_matrix[i][6]) * boneWeights[i];

                ny += (normal[0] * bone_uploaded_matrix[i][1]
                        + normal[1] * bone_uploaded_matrix[i][4]
                        + normal[2] * bone_uploaded_matrix[i][7]) * boneWeights[i];

                nz += (normal[0] * bone_uploaded_matrix[i][2]
                        + normal[1] * bone_uploaded_matrix[i][5]
                        + normal[2] * bone_uploaded_matrix[i][8]) * boneWeights[i];
            }
        }

        /*
        // TODO: I doubt psp hardware normalizes normals after skinning,
        // but if it does, this should be uncommented :)
        float length = nx*nx + ny*ny + nz*nz;

        if (length > 0.f) {
        length = 1.f / (float)Math.sqrt(length);

        nx *= length;
        ny *= length;
        nz *= length;
        }
         */

        normal[0] = nx;
        normal[1] = ny;
        normal[2] = nz;
    }

    private void doSkinning(VertexInfo vinfo, VertexState v) {
        float x = 0, y = 0, z = 0;
        float nx = 0, ny = 0, nz = 0;
        for (int i = 0; i < vinfo.skinningWeightCount; ++i) {
            if (v.boneWeights[i] != 0.f) {

                x += (v.p[0] * bone_uploaded_matrix[i][0]
                        + v.p[1] * bone_uploaded_matrix[i][3]
                        + v.p[2] * bone_uploaded_matrix[i][6]
                        + bone_uploaded_matrix[i][9]) * v.boneWeights[i];

                y += (v.p[0] * bone_uploaded_matrix[i][1]
                        + v.p[1] * bone_uploaded_matrix[i][4]
                        + v.p[2] * bone_uploaded_matrix[i][7]
                        + bone_uploaded_matrix[i][10]) * v.boneWeights[i];

                z += (v.p[0] * bone_uploaded_matrix[i][2]
                        + v.p[1] * bone_uploaded_matrix[i][5]
                        + v.p[2] * bone_uploaded_matrix[i][8]
                        + bone_uploaded_matrix[i][11]) * v.boneWeights[i];

                // Normals shouldn't be translated :)
                nx += (v.n[0] * bone_uploaded_matrix[i][0]
                        + v.n[1] * bone_uploaded_matrix[i][3]
                        + v.n[2] * bone_uploaded_matrix[i][6]) * v.boneWeights[i];

                ny += (v.n[0] * bone_uploaded_matrix[i][1]
                        + v.n[1] * bone_uploaded_matrix[i][4]
                        + v.n[2] * bone_uploaded_matrix[i][7]) * v.boneWeights[i];

                nz += (v.n[0] * bone_uploaded_matrix[i][2]
                        + v.n[1] * bone_uploaded_matrix[i][5]
                        + v.n[2] * bone_uploaded_matrix[i][8]) * v.boneWeights[i];
            }
        }

        v.p[0] = x;
        v.p[1] = y;
        v.p[2] = z;

        /*
        // TODO: I doubt psp hardware normalizes normals after skinning,
        // but if it does, this should be uncommented :)
        float length = nx*nx + ny*ny + nz*nz;

        if (length > 0.f) {
        length = 1.f / (float)Math.sqrt(length);

        nx *= length;
        ny *= length;
        nz *= length;
        }
         */

        v.n[0] = nx;
        v.n[1] = ny;
        v.n[2] = nz;
    }

    private void log(String commandString, float floatArgument) {
        if (isLogDebugEnabled) {
            log(commandString + SPACE + floatArgument);
        }
    }

    private void log(String commandString, int value) {
        if (isLogDebugEnabled) {
            log(commandString + SPACE + value);
        }
    }

    private void log(String commandString, float[] matrix) {
        if (isLogDebugEnabled) {
            for (int y = 0; y < 4; y++) {
                log(commandString + SPACE + String.format("%.1f %.1f %.1f %.1f", matrix[0 + y * 4], matrix[1 + y * 4], matrix[2 + y * 4], matrix[3 + y * 4]));
            }
        }
    }

    private String getOpenGLVersion(GL gl) {
        return gl.glGetString(GL.GL_VERSION);
    }

    private void convertPixelType(short[] source, int[] destination,
            int aMask, int aShift,
            int rMask, int rShift,
            int gMask, int gShift,
            int bMask, int bShift,
            int level) {
        for (int i = 0; i < texture_buffer_width[level] * texture_height[level]; i++) {
            int pixel = source[i];
            int color = ((pixel & aMask) << aShift)
                    | ((pixel & rMask) << rShift)
                    | ((pixel & gMask) << gShift)
                    | ((pixel & bMask) << bShift);
            destination[i] = color;
        }
    }

    private void loadTexture() {
        // No need to reload or check the texture cache if no texture parameter
        // has been changed since last call loadTexture()
        if (!textureChanged) {
            return;
        }

        // HACK: avoid texture uploads of null pointers
        // This can come from Sony's GE init code (pspsdk GE init is ok)
        if (texture_base_pointer[0] == 0) {
            return;
        }

        // Texture not used in clear mode or when disabled.
        if (clearMode || !textureFlag.isEnabled()) {
            return;
        }

        Texture texture;
        int tex_addr = texture_base_pointer[0] & Memory.addressMask;
        // Some games are storing compressed textures in VRAM (e.g. Skate Park City).
        // Force only a reload of textures that can be generated by the GE buffer,
        // i.e. when texture_storage is one of
        // BGR5650=0, ABGR5551=1, ABGR4444=2 or ABGR8888=3.
        if (!useTextureCache || (isVRAM(tex_addr) && texture_storage <= TPSM_PIXEL_STORAGE_MODE_32BIT_ABGR8888)) {
            texture = null;

            // Generate a texture id if we don't have one
            if (gl_texture_id[0] == 0) {
                gl.glGenTextures(1, gl_texture_id, 0);
            }

            gl.glBindTexture(GL.GL_TEXTURE_2D, gl_texture_id[0]);
        } else {
            textureCacheLookupStatistics.start();
            // Check if the texture is in the cache
            texture = TextureCache.getInstance().getTexture(texture_base_pointer[0], texture_buffer_width[0], texture_width[0], texture_height[0], texture_storage, tex_clut_addr, tex_clut_mode, tex_clut_start, tex_clut_shift, tex_clut_mask, tex_clut_num_blocks, texture_num_mip_maps, mipmapShareClut);
            textureCacheLookupStatistics.end();

            // Create the texture if not yet in the cache
            if (texture == null) {
                TextureCache textureCache = TextureCache.getInstance();
                texture = new Texture(textureCache, texture_base_pointer[0], texture_buffer_width[0], texture_width[0], texture_height[0], texture_storage, tex_clut_addr, tex_clut_mode, tex_clut_start, tex_clut_shift, tex_clut_mask, tex_clut_num_blocks, texture_num_mip_maps, mipmapShareClut);
                textureCache.addTexture(gl, texture);
            }

            texture.bindTexture(gl);
        }

        // Load the texture if not yet loaded
        if (texture == null || !texture.isLoaded() || State.captureGeNextFrame || State.replayGeNextFrame) {
            if (isLogDebugEnabled) {
                log(helper.getCommandString(TFLUSH)
                        + " " + String.format("0x%08X", texture_base_pointer[0])
                        + ", buffer_width=" + texture_buffer_width[0]
                        + " (" + texture_width[0] + "," + texture_height[0] + ")");

                log(helper.getCommandString(TFLUSH)
                        + " texture_storage=0x" + Integer.toHexString(texture_storage)
                        + "(" + getPsmName(texture_storage)
                        + "), tex_clut_mode=0x" + Integer.toHexString(tex_clut_mode)
                        + ", tex_clut_addr=" + String.format("0x%08X", tex_clut_addr)
                        + ", texture_swizzle=" + texture_swizzle);
            }

            Buffer final_buffer = null;
            int texture_type = 0;
            int texclut = tex_clut_addr;
            int texaddr;

            int textureByteAlignment = 4;   // 32 bits
            int texture_format = GL.GL_RGBA;
            boolean compressedTexture = false;

            int numberMipmaps = texture_num_mip_maps;

            for (int level = 0; level <= numberMipmaps; level++) {
                // Extract texture information with the minor conversion possible
                // TODO: Get rid of information copying, and implement all the available formats
                texaddr = texture_base_pointer[level];
                texaddr &= Memory.addressMask;
                texture_format = GL.GL_RGBA;
                compressedTexture = false;
                int compressedTextureSize = 0;

                switch (texture_storage) {
                    case TPSM_PIXEL_STORAGE_MODE_4BIT_INDEXED: {
                        switch (tex_clut_mode) {
                            case CMODE_FORMAT_16BIT_BGR5650:
                            case CMODE_FORMAT_16BIT_ABGR5551:
                            case CMODE_FORMAT_16BIT_ABGR4444: {
                                if (texclut == 0) {
                                    return;
                                }

                                texture_type = texturetype_mapping[tex_clut_mode];
                                textureByteAlignment = 2;  // 16 bits
                                short[] clut = readClut16(level);
                                int clutSharingOffset = mipmapShareClut ? 0 : level * 16;

                                if (!texture_swizzle) {
                                    int length = texture_buffer_width[level] * texture_height[level];
                                    IMemoryReader memoryReader = MemoryReader.getMemoryReader(texaddr, length / 2, 1);
                                    for (int i = 0; i < length; i += 2) {

                                        int index = memoryReader.readNext();

                                        tmp_texture_buffer16[i] = clut[getClutIndex(index & 0xF) + clutSharingOffset];
                                        tmp_texture_buffer16[i + 1] = clut[getClutIndex((index >> 4) & 0xF) + clutSharingOffset];
                                    }
                                    final_buffer = ShortBuffer.wrap(tmp_texture_buffer16);

                                    if (State.captureGeNextFrame) {
                                        log.info("Capture loadTexture clut 4/16 unswizzled");
                                        CaptureManager.captureRAM(texaddr, length / 2);
                                    }
                                } else {
                                    unswizzleTextureFromMemory(texaddr, 0, level);
                                    int pixels = texture_buffer_width[level] * texture_height[level];
                                    for (int i = 0, j = 0; i < pixels; i += 8, j++) {
                                        int n = tmp_texture_buffer32[j];
                                        int index = n & 0xF;
                                        tmp_texture_buffer16[i + 0] = clut[getClutIndex(index) + clutSharingOffset];
                                        index = (n >> 4) & 0xF;
                                        tmp_texture_buffer16[i + 1] = clut[getClutIndex(index) + clutSharingOffset];
                                        index = (n >> 8) & 0xF;
                                        tmp_texture_buffer16[i + 2] = clut[getClutIndex(index) + clutSharingOffset];
                                        index = (n >> 12) & 0xF;
                                        tmp_texture_buffer16[i + 3] = clut[getClutIndex(index) + clutSharingOffset];
                                        index = (n >> 16) & 0xF;
                                        tmp_texture_buffer16[i + 4] = clut[getClutIndex(index) + clutSharingOffset];
                                        index = (n >> 20) & 0xF;
                                        tmp_texture_buffer16[i + 5] = clut[getClutIndex(index) + clutSharingOffset];
                                        index = (n >> 24) & 0xF;
                                        tmp_texture_buffer16[i + 6] = clut[getClutIndex(index) + clutSharingOffset];
                                        index = (n >> 28) & 0xF;
                                        tmp_texture_buffer16[i + 7] = clut[getClutIndex(index) + clutSharingOffset];
                                    }
                                    final_buffer = ShortBuffer.wrap(tmp_texture_buffer16);
                                    break;
                                }

                                break;
                            }

                            case CMODE_FORMAT_32BIT_ABGR8888: {
                                if (texclut == 0) {
                                    return;
                                }

                                texture_type = GL.GL_UNSIGNED_BYTE;
                                int[] clut = readClut32(level);
                                int clutSharingOffset = mipmapShareClut ? 0 : level * 16;

                                if (!texture_swizzle) {
                                    int length = texture_buffer_width[level] * texture_height[level];
                                    IMemoryReader memoryReader = MemoryReader.getMemoryReader(texaddr, length / 2, 1);
                                    for (int i = 0; i < length; i += 2) {

                                        int index = memoryReader.readNext();

                                        tmp_texture_buffer32[i + 1] = clut[getClutIndex((index >> 4) & 0xF) + clutSharingOffset];
                                        tmp_texture_buffer32[i] = clut[getClutIndex(index & 0xF) + clutSharingOffset];
                                    }
                                    final_buffer = IntBuffer.wrap(tmp_texture_buffer32);

                                    if (State.captureGeNextFrame) {
                                        log.info("Capture loadTexture clut 4/32 unswizzled");
                                        CaptureManager.captureRAM(texaddr, length / 2);
                                    }
                                } else {
                                    unswizzleTextureFromMemory(texaddr, 0, level);
                                    int pixels = texture_buffer_width[level] * texture_height[level];
                                    for (int i = pixels - 8, j = (pixels / 8) - 1; i >= 0; i -= 8, j--) {
                                        int n = tmp_texture_buffer32[j];
                                        int index = n & 0xF;
                                        tmp_texture_buffer32[i + 0] = clut[getClutIndex(index) + clutSharingOffset];
                                        index = (n >> 4) & 0xF;
                                        tmp_texture_buffer32[i + 1] = clut[getClutIndex(index) + clutSharingOffset];
                                        index = (n >> 8) & 0xF;
                                        tmp_texture_buffer32[i + 2] = clut[getClutIndex(index) + clutSharingOffset];
                                        index = (n >> 12) & 0xF;
                                        tmp_texture_buffer32[i + 3] = clut[getClutIndex(index) + clutSharingOffset];
                                        index = (n >> 16) & 0xF;
                                        tmp_texture_buffer32[i + 4] = clut[getClutIndex(index) + clutSharingOffset];
                                        index = (n >> 20) & 0xF;
                                        tmp_texture_buffer32[i + 5] = clut[getClutIndex(index) + clutSharingOffset];
                                        index = (n >> 24) & 0xF;
                                        tmp_texture_buffer32[i + 6] = clut[getClutIndex(index) + clutSharingOffset];
                                        index = (n >> 28) & 0xF;
                                        tmp_texture_buffer32[i + 7] = clut[getClutIndex(index) + clutSharingOffset];
                                    }
                                    final_buffer = IntBuffer.wrap(tmp_texture_buffer32);
                                }

                                break;
                            }

                            default: {
                                error("Unhandled clut4 texture mode " + tex_clut_mode);
                                return;
                            }
                        }

                        break;
                    }
                    case TPSM_PIXEL_STORAGE_MODE_8BIT_INDEXED: {
                        final_buffer = readIndexedTexture(level, texaddr, texclut, 1);
                        texture_type = texturetype_mapping[tex_clut_mode];
                        textureByteAlignment = textureByteAlignmentMapping[tex_clut_mode];
                        break;
                    }
                    case TPSM_PIXEL_STORAGE_MODE_16BIT_INDEXED: {
                        final_buffer = readIndexedTexture(level, texaddr, texclut, 2);
                        texture_type = texturetype_mapping[tex_clut_mode];
                        textureByteAlignment = textureByteAlignmentMapping[tex_clut_mode];
                        break;
                    }
                    case TPSM_PIXEL_STORAGE_MODE_32BIT_INDEXED: {
                        final_buffer = readIndexedTexture(level, texaddr, texclut, 4);
                        texture_type = texturetype_mapping[tex_clut_mode];
                        textureByteAlignment = textureByteAlignmentMapping[tex_clut_mode];
                        break;
                    }
                    case TPSM_PIXEL_STORAGE_MODE_16BIT_BGR5650:
                    case TPSM_PIXEL_STORAGE_MODE_16BIT_ABGR5551:
                    case TPSM_PIXEL_STORAGE_MODE_16BIT_ABGR4444: {
                        texture_type = texturetype_mapping[texture_storage];
                        textureByteAlignment = 2;  // 16 bits

                        if (!texture_swizzle) {
                            int length = Math.max(texture_buffer_width[level], texture_width[level]) * texture_height[level];
                            final_buffer = Memory.getInstance().getBuffer(texaddr, length * 2);
                            if (final_buffer == null) {
                                IMemoryReader memoryReader = MemoryReader.getMemoryReader(texaddr, length * 2, 2);
                                for (int i = 0; i < length; i++) {
                                    int pixel = memoryReader.readNext();
                                    tmp_texture_buffer16[i] = (short) pixel;
                                }

                                final_buffer = ShortBuffer.wrap(tmp_texture_buffer16);
                            }

                            if (State.captureGeNextFrame) {
                                log.info("Capture loadTexture 16 unswizzled");
                                CaptureManager.captureRAM(texaddr, length * 2);
                            }
                        } else {
                            final_buffer = unswizzleTextureFromMemory(texaddr, 2, level);
                        }

                        break;
                    }

                    case TPSM_PIXEL_STORAGE_MODE_32BIT_ABGR8888: {
                        if (openGL1_2) {
                            texture_type = GL.GL_UNSIGNED_INT_8_8_8_8_REV;  // Only available from V1.2
                        } else {
                            texture_type = GL.GL_UNSIGNED_BYTE;
                        }

                        final_buffer = getTexture32BitBuffer(texaddr, level);
                        break;
                    }

                    case TPSM_PIXEL_STORAGE_MODE_DXT1: {
                        if (isLogDebugEnabled) {
                            log.debug("Loading texture TPSM_PIXEL_STORAGE_MODE_DXT1 " + Integer.toHexString(texaddr));
                        }
                        texture_type = GL.GL_COMPRESSED_RGB_S3TC_DXT1_EXT;
                        compressedTexture = true;
                        compressedTextureSize = getCompressedTextureSize(level, 8);
                        IMemoryReader memoryReader = MemoryReader.getMemoryReader(texaddr, compressedTextureSize, 4);
                        // PSP DXT1 hardware format reverses the colors and the per-pixel
                        // bits, and encodes the color in RGB 565 format
                        int i = 0;
                        for (int y = 0; y < texture_height[level]; y += 4) {
                            for (int x = 0; x < texture_buffer_width[level]; x += 4, i += 2) {
                                tmp_texture_buffer32[i + 1] = memoryReader.readNext();
                                tmp_texture_buffer32[i + 0] = memoryReader.readNext();
                            }
                            for (int x = texture_buffer_width[level]; x < texture_width[level]; x += 4, i += 2) {
                                tmp_texture_buffer32[i + 0] = 0;
                                tmp_texture_buffer32[i + 1] = 0;
                            }
                        }
                        final_buffer = IntBuffer.wrap(tmp_texture_buffer32);
                        break;
                    }

                    case TPSM_PIXEL_STORAGE_MODE_DXT3: {
                        if (isLogDebugEnabled) {
                            log.debug("Loading texture TPSM_PIXEL_STORAGE_MODE_DXT3 " + Integer.toHexString(texaddr));
                        }
                        texture_type = GL.GL_COMPRESSED_RGBA_S3TC_DXT3_EXT;
                        compressedTexture = true;
                        compressedTextureSize = getCompressedTextureSize(level, 4);
                        IMemoryReader memoryReader = MemoryReader.getMemoryReader(texaddr, compressedTextureSize, 4);
                        // PSP DXT3 format reverses the alpha and color parts of each block,
                        // and reverses the color and per-pixel terms in the color part.
                        int i = 0;
                        for (int y = 0; y < texture_height[level]; y += 4) {
                            for (int x = 0; x < texture_buffer_width[level]; x += 4, i += 4) {
                                // Color
                                tmp_texture_buffer32[i + 3] = memoryReader.readNext();
                                tmp_texture_buffer32[i + 2] = memoryReader.readNext();
                                // Alpha
                                tmp_texture_buffer32[i + 0] = memoryReader.readNext();
                                tmp_texture_buffer32[i + 1] = memoryReader.readNext();
                            }
                            for (int x = texture_buffer_width[level]; x < texture_width[level]; x += 4, i += 4) {
                                tmp_texture_buffer32[i + 0] = 0;
                                tmp_texture_buffer32[i + 1] = 0;
                                tmp_texture_buffer32[i + 2] = 0;
                                tmp_texture_buffer32[i + 3] = 0;
                            }
                        }
                        final_buffer = IntBuffer.wrap(tmp_texture_buffer32);
                        break;
                    }

                    case TPSM_PIXEL_STORAGE_MODE_DXT5: {
                        if (isLogDebugEnabled) {
                            log.debug("Loading texture TPSM_PIXEL_STORAGE_MODE_DXT5 " + Integer.toHexString(texaddr));
                        }
                        texture_type = GL.GL_COMPRESSED_RGBA_S3TC_DXT5_EXT;
                        compressedTexture = true;
                        compressedTextureSize = getCompressedTextureSize(level, 4);
                        IMemoryReader memoryReader = MemoryReader.getMemoryReader(texaddr, compressedTextureSize, 2);
                        // PSP DXT5 format reverses the alpha and color parts of each block,
                        // and reverses the color and per-pixel terms in the color part. In
                        // the alpha part, the 2 reference alpha values are swapped with the
                        // alpha interpolation values.
                        int i = 0;
                        for (int y = 0; y < texture_height[level]; y += 4) {
                            for (int x = 0; x < texture_buffer_width[level]; x += 4, i += 8) {
                                // Color
                                tmp_texture_buffer16[i + 6] = (short) memoryReader.readNext();
                                tmp_texture_buffer16[i + 7] = (short) memoryReader.readNext();
                                tmp_texture_buffer16[i + 4] = (short) memoryReader.readNext();
                                tmp_texture_buffer16[i + 5] = (short) memoryReader.readNext();
                                // Alpha
                                tmp_texture_buffer16[i + 1] = (short) memoryReader.readNext();
                                tmp_texture_buffer16[i + 2] = (short) memoryReader.readNext();
                                tmp_texture_buffer16[i + 3] = (short) memoryReader.readNext();
                                tmp_texture_buffer16[i + 0] = (short) memoryReader.readNext();
                            }
                            for (int x = texture_buffer_width[level]; x < texture_width[level]; x += 4, i += 8) {
                                tmp_texture_buffer16[i + 0] = 0;
                                tmp_texture_buffer16[i + 1] = 0;
                                tmp_texture_buffer16[i + 2] = 0;
                                tmp_texture_buffer16[i + 3] = 0;
                                tmp_texture_buffer16[i + 4] = 0;
                                tmp_texture_buffer16[i + 5] = 0;
                                tmp_texture_buffer16[i + 6] = 0;
                                tmp_texture_buffer16[i + 7] = 0;
                            }
                        }
                        final_buffer = ShortBuffer.wrap(tmp_texture_buffer16);
                        break;
                    }

                    default: {
                        error("Unhandled texture storage " + texture_storage);
                        return;
                    }
                }

                // Some textureTypes are only supported from OpenGL v1.2.
                // Try to convert to type supported in v1.
                if (!openGL1_2) {
                    if (texture_type == GL.GL_UNSIGNED_SHORT_4_4_4_4_REV) {
                        convertPixelType(tmp_texture_buffer16, tmp_texture_buffer32, 0xF000, 16, 0x0F00, 12, 0x00F0, 8, 0x000F, 4, level);
                        final_buffer = IntBuffer.wrap(tmp_texture_buffer32);
                        texture_type = GL.GL_UNSIGNED_BYTE;
                        textureByteAlignment = 4;
                    } else if (texture_type == GL.GL_UNSIGNED_SHORT_1_5_5_5_REV) {
                        convertPixelType(tmp_texture_buffer16, tmp_texture_buffer32, 0x8000, 16, 0x7C00, 9, 0x03E0, 6, 0x001F, 3, level);
                        final_buffer = IntBuffer.wrap(tmp_texture_buffer32);
                        texture_type = GL.GL_UNSIGNED_BYTE;
                        textureByteAlignment = 4;
                    } else if (texture_type == GL.GL_UNSIGNED_SHORT_5_6_5_REV) {
                        convertPixelType(tmp_texture_buffer16, tmp_texture_buffer32, 0x0000, 0, 0xF800, 8, 0x07E0, 5, 0x001F, 3, level);
                        final_buffer = IntBuffer.wrap(tmp_texture_buffer32);
                        texture_type = GL.GL_UNSIGNED_BYTE;
                        textureByteAlignment = 4;
                        texture_format = GL.GL_RGB;
                    }
                }

                if (texture_type == GL.GL_UNSIGNED_SHORT_5_6_5_REV) {
                    texture_format = GL.GL_RGB;
                }

                // Upload texture to openGL.
                gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, tex_mag_filter);
                gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, tex_min_filter);

                gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, textureByteAlignment);
                gl.glPixelStorei(GL.GL_UNPACK_ROW_LENGTH, texture_buffer_width[level]);

                if (compressedTexture) {
                    gl.glCompressedTexImage2D(GL.GL_TEXTURE_2D,
                            level,
                            texture_type,
                            texture_width[level], texture_height[level],
                            0,
                            compressedTextureSize,
                            final_buffer);
                } else {
                    gl.glTexImage2D(GL.GL_TEXTURE_2D,
                            level,
                            texture_format,
                            texture_width[level], texture_height[level],
                            0,
                            texture_format,
                            texture_type,
                            final_buffer);
                }

                if (State.captureGeNextFrame) {
                    if (isVRAM(tex_addr)) {
                        CaptureManager.captureImage(texaddr, level, final_buffer, texture_width[level], texture_height[level], texture_buffer_width[level], texture_type, compressedTexture, compressedTextureSize, false);
                    } else if (!CaptureManager.isImageCaptured(texaddr)) {
                        CaptureManager.captureImage(texaddr, level, final_buffer, texture_width[level], texture_height[level], texture_buffer_width[level], texture_type, compressedTexture, compressedTextureSize, true);
                    }
                }

                if (texture != null) {
                    texture.setIsLoaded();
                    if (isLogDebugEnabled) {
                        log(helper.getCommandString(TFLUSH) + " Loaded texture " + texture.getGlId());
                    }
                }
            }

            checkTextureMinFilter(compressedTexture, numberMipmaps);
        } else {
            boolean compressedTexture = (texture_storage >= TPSM_PIXEL_STORAGE_MODE_DXT1 && texture_storage <= TPSM_PIXEL_STORAGE_MODE_DXT5);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, tex_mag_filter);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, tex_min_filter);
            checkTextureMinFilter(compressedTexture, texture_num_mip_maps);

            if (isLogDebugEnabled) {
                log(helper.getCommandString(TFLUSH) + " Reusing cached texture " + texture.getGlId());
            }
        }

        textureChanged = false;
    }

    private void checkTextureMinFilter(boolean compressedTexture, int numberMipmaps) {
        // OpenGL/Hardware cannot interpolate between compressed textures;
        // this restriction has been checked on NVIDIA GeForce 8500 GT and 9800 GT
        if (compressedTexture) {
            int new_tex_min_filter;
            if (tex_min_filter == GL.GL_NEAREST_MIPMAP_LINEAR || tex_min_filter == GL.GL_NEAREST_MIPMAP_NEAREST) {
                new_tex_min_filter = GL.GL_NEAREST;
            } else {
                new_tex_min_filter = GL.GL_LINEAR;
            }
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, new_tex_min_filter);
            if (isLogDebugEnabled) {
                log("Overwriting texture min filter, no mipmap was generated but filter was set to use mipmap");
            }
        }
    }

    private Buffer readIndexedTexture(int level, int texaddr, int texclut, int bytesPerIndex) {
        Buffer buffer = null;

        int length = texture_buffer_width[level] * texture_height[level];
        switch (tex_clut_mode) {
            case CMODE_FORMAT_16BIT_BGR5650:
            case CMODE_FORMAT_16BIT_ABGR5551:
            case CMODE_FORMAT_16BIT_ABGR4444: {
                if (texclut == 0) {
                    return null;
                }

                short[] clut = readClut16(level);

                if (!texture_swizzle) {
                    IMemoryReader memoryReader = MemoryReader.getMemoryReader(texaddr, length * bytesPerIndex, bytesPerIndex);
                    for (int i = 0; i < length; i++) {
                        int index = memoryReader.readNext();
                        tmp_texture_buffer16[i] = clut[getClutIndex(index)];
                    }
                    buffer = ShortBuffer.wrap(tmp_texture_buffer16);

                    if (State.captureGeNextFrame) {
                        log.info("Capture loadTexture clut 8/16 unswizzled");
                        CaptureManager.captureRAM(texaddr, length * bytesPerIndex);
                    }
                } else {
                    unswizzleTextureFromMemory(texaddr, bytesPerIndex, level);
                    switch (bytesPerIndex) {
                        case 1: {
                            for (int i = 0, j = 0; i < length; i += 4, j++) {
                                int n = tmp_texture_buffer32[j];
                                int index = n & 0xFF;
                                tmp_texture_buffer16[i + 0] = clut[getClutIndex(index)];
                                index = (n >> 8) & 0xFF;
                                tmp_texture_buffer16[i + 1] = clut[getClutIndex(index)];
                                index = (n >> 16) & 0xFF;
                                tmp_texture_buffer16[i + 2] = clut[getClutIndex(index)];
                                index = (n >> 24) & 0xFF;
                                tmp_texture_buffer16[i + 3] = clut[getClutIndex(index)];
                            }
                            break;
                        }
                        case 2: {
                            for (int i = 0, j = 0; i < length; i += 2, j++) {
                                int n = tmp_texture_buffer32[j];
                                tmp_texture_buffer16[i + 0] = clut[getClutIndex(n & 0xFFFF)];
                                tmp_texture_buffer16[i + 1] = clut[getClutIndex(n >>> 16)];
                            }
                            break;
                        }
                        case 4: {
                            for (int i = 0; i < length; i++) {
                                int n = tmp_texture_buffer32[i];
                                tmp_texture_buffer16[i] = clut[getClutIndex(n)];
                            }
                            break;
                        }
                    }
                    buffer = ShortBuffer.wrap(tmp_texture_buffer16);
                }

                break;
            }

            case CMODE_FORMAT_32BIT_ABGR8888: {
                if (texclut == 0) {
                    return null;
                }

                int[] clut = readClut32(level);

                if (!texture_swizzle) {
                    IMemoryReader memoryReader = MemoryReader.getMemoryReader(texaddr, length * bytesPerIndex, bytesPerIndex);
                    for (int i = 0; i < length; i++) {
                        int index = memoryReader.readNext();
                        tmp_texture_buffer32[i] = clut[getClutIndex(index)];
                    }
                    buffer = IntBuffer.wrap(tmp_texture_buffer32);

                    if (State.captureGeNextFrame) {
                        log.info("Capture loadTexture clut 8/32 unswizzled");
                        CaptureManager.captureRAM(texaddr, length * bytesPerIndex);
                    }
                } else {
                    unswizzleTextureFromMemory(texaddr, bytesPerIndex, level);
                    switch (bytesPerIndex) {
                        case 1: {
                            for (int i = length - 4, j = (length / 4) - 1; i >= 0; i -= 4, j--) {
                                int n = tmp_texture_buffer32[j];
                                int index = n & 0xFF;
                                tmp_texture_buffer32[i + 0] = clut[getClutIndex(index)];
                                index = (n >> 8) & 0xFF;
                                tmp_texture_buffer32[i + 1] = clut[getClutIndex(index)];
                                index = (n >> 16) & 0xFF;
                                tmp_texture_buffer32[i + 2] = clut[getClutIndex(index)];
                                index = (n >> 24) & 0xFF;
                                tmp_texture_buffer32[i + 3] = clut[getClutIndex(index)];
                            }
                            break;
                        }
                        case 2: {
                            for (int i = length - 2, j = (length / 2) - 1; i >= 0; i -= 2, j--) {
                                int n = tmp_texture_buffer32[j];
                                tmp_texture_buffer32[i + 0] = clut[getClutIndex(n & 0xFFFF)];
                                tmp_texture_buffer32[i + 1] = clut[getClutIndex(n >>> 16)];
                            }
                            break;
                        }
                        case 4: {
                            for (int i = 0; i < length; i++) {
                                int n = tmp_texture_buffer32[i];
                                tmp_texture_buffer32[i] = clut[getClutIndex(n)];
                            }
                            break;
                        }
                    }
                    buffer = IntBuffer.wrap(tmp_texture_buffer32);
                }

                break;
            }

            default: {
                error("Unhandled clut8 texture mode " + tex_clut_mode);
                break;
            }
        }

        return buffer;
    }

    private boolean initRendering() {
        /*
         * Defer transformations until primitive rendering
         */

        /*
         * Apply Blending
         */
        if (blendChanged) {
            setBlendFunc();
            blendChanged = false;
        }

        /*
         * Apply projection matrix
         */
        if (projectionMatrixUpload.isChanged()) {
            if (transform_mode == VTYPE_TRANSFORM_PIPELINE_TRANS_COORD) {
            	re.setProjectionMatrixElements(proj_uploaded_matrix);
            } else {
            	re.setProjectionMatrixElements(null);
            }
            projectionMatrixUpload.setChanged(false);

            // The viewport has to be reloaded when the projection matrix has changed
            viewportChanged = true;
        }

        /*
         * Apply viewport
         */
        boolean loadOrtho2D = false;
        if (viewportChanged) {
            if (transform_mode == VTYPE_TRANSFORM_PIPELINE_RAW_COORD) {
                re.setViewport(0, 0, 480, 272);
                // Load the ortho for 2D after the depth settings
                loadOrtho2D = true;
            } else {
                if (viewport_cx == 0 && viewport_cy == 0 && viewport_height == 0 && viewport_width == 0) {
                    viewport_cx = 2048;
                    viewport_cy = 2048;
                    viewport_width = 480;
                    viewport_height = 272;
                }
                re.setViewport(viewport_cx - offset_x, viewport_cy - offset_y, viewport_width, viewport_height);
            }
            viewportChanged = false;
        }

        /*
         * Apply depth handling
         */
        if (depthChanged) {
            if (transform_mode == VTYPE_TRANSFORM_PIPELINE_TRANS_COORD) {
            	re.setDepthFunc(depthFunc);
                re.setDepthRange(zpos, zscale, zpos - zscale, zpos + zscale);
            } else {
            	re.setDepthFunc(depthFunc);
                re.setDepthRange(0.5f, 0.5f, 0, 1);
            }
            depthChanged = false;
        }

        /*
         * Load the 2D ortho (only after the depth settings
         */
        if (loadOrtho2D) {
        	float left = 0;
        	float right = 480;
        	float bottom = 272;
        	float top = 0;
        	float near = 0;
        	float far = -0xFFFF;

        	float dx = right - left;
        	float dy = top - bottom;
        	float dz = far - near;
        	float[] projectionMatrix = {
            		2.f / dx, 0, 0, 0,
            		0, 2.f / dy, 0, 0,
            		0, 0, -2.f / dz, 0,
            		-(right + left) / dx, -(top + bottom) / dy, -(far + near) / dz, 1
            };
            re.setProjectionMatrixElements(projectionMatrix);
        }

        /*
         * 2D mode handling
         */
        if (transform_mode == VTYPE_TRANSFORM_PIPELINE_RAW_COORD) {
            // 2D mode shouldn't be affected by the lighting and fog
            gl.glPushAttrib(GL.GL_LIGHTING_BIT | GL.GL_FOG_BIT);
            gl.glDisable(GL.GL_LIGHTING);
            gl.glDisable(GL.GL_FOG);
            if (useShaders) {
                gl.glUniform1i(Uniforms.lightingEnable.getId(), 0);
                gl.glUniform1f(Uniforms.zPos.getId(), zpos);
                gl.glUniform1f(Uniforms.zScale.getId(), zscale);
            }

            // TODO I don't know why, but the GL_MODELVIEW matrix has to be reloaded
            // each time in 2D mode... Otherwise textures are not displayed.
            modelMatrixUpload.setChanged(true);
        }

        /*
         * Model-View matrix has to reloaded when
         * - model matrix changed
         * - view matrix changed
         * - lighting has to be reloaded
         */
        boolean loadLightingSettings = (viewMatrixUpload.isChanged() || lightingChanged) && lightingFlag.isEnabled() && transform_mode == VTYPE_TRANSFORM_PIPELINE_TRANS_COORD;
        boolean modelViewMatrixChanged = modelMatrixUpload.isChanged() || viewMatrixUpload.isChanged() || loadLightingSettings;

        /*
         * Apply view matrix
         */
        if (modelViewMatrixChanged) {
            if (transform_mode == VTYPE_TRANSFORM_PIPELINE_TRANS_COORD) {
            	re.setViewMatrixElements(view_uploaded_matrix);
            } else {
            	re.setViewMatrixElements(null);
            }
            viewMatrixUpload.setChanged(false);
        }

        /*
         *  Setup lights on when view transformation is set up
         */
        if (loadLightingSettings || tex_map_mode == TMAP_TEXTURE_MAP_MODE_ENVIRONMENT_MAP) {
            for (int i = 0; i < NUM_LIGHTS; i++) {
                if (lightFlags[i].isEnabled() || (tex_map_mode == TMAP_TEXTURE_MAP_MODE_ENVIRONMENT_MAP && (tex_shade_u == i || tex_shade_v == i))) {
                	re.setLightPosition(i, light_pos[i]);
                	re.setLightDirection(i, light_dir[i]);

                    if (light_type[i] == LIGHT_SPOT) {
                    	re.setLightSpotExponent(i, spotLightExponent[i]);
                    	re.setLightSpotCutoff(i, spotLightCutoff[i]);
                    } else {
                        // uniform light distribution
                    	re.setLightSpotExponent(i, 0);
                    	re.setLightSpotCutoff(i, 180);
                    }

                    // Light kind:
                    //  LIGHT_DIFFUSE_SPECULAR: use ambient, diffuse and specular colors
                    //  all other light kinds: use ambient and diffuse colors (not specular)
                    if (light_kind[i] != LIGHT_AMBIENT_DIFFUSE) {
                    	re.setLightSpecularColor(i, lightSpecularColor[i]);
                    } else {
                    	re.setLightSpecularColor(i, blackColor);
                    }
                }
            }

            lightingChanged = false;
        }

        if (modelViewMatrixChanged) {
            // Apply model matrix
            if (transform_mode == VTYPE_TRANSFORM_PIPELINE_TRANS_COORD) {
                if (!modelMatrixUpload.isIdentity()) {
                	re.setModelMatrixElements(model_uploaded_matrix);
                }
            }
            modelMatrixUpload.setChanged(false);
        }

        /*
         * Apply texture transforms
         */
        if (textureMatrixUpload.isChanged()) {
        	float[] textureMatrix = new float[] {
        		1, 0, 0, 0,
        		0, 1, 0, 0,
        		0, 0, 1, 0,
        		0, 0, 0, 1
        	};

            if (transform_mode != VTYPE_TRANSFORM_PIPELINE_TRANS_COORD) {
            	re.setTextureMapMode(TMAP_TEXTURE_MAP_MODE_TEXTURE_COORDIATES_UV, TMAP_TEXTURE_PROJECTION_MODE_POSITION);;
            	textureMatrix[0] = 1.f / texture_width[0];
            	textureMatrix[5] = 1.f / texture_height[0];
                //gl.glScalef(1.f / texture_width[0], 1.f / texture_height[0], 1.f);
            	re.setTextureMatrixElements(textureMatrix);
            } else {
            	re.setTextureEnvironmentMapping(tex_shade_u, tex_shade_v);
            	re.setTextureMapMode(tex_map_mode, tex_proj_map_mode);
                switch (tex_map_mode) {
                    case TMAP_TEXTURE_MAP_MODE_TEXTURE_COORDIATES_UV:
                    	textureMatrix[0] = tex_scale_x;
                    	textureMatrix[5] = tex_scale_y;
                    	textureMatrix[3] = tex_translate_x;
                    	textureMatrix[7] = tex_translate_y;
                        //gl.glTranslatef(tex_translate_x, tex_translate_y, 0.f);
                        //gl.glScalef(tex_scale_x, tex_scale_y, 1.f);
                    	re.setTextureMatrixElements(textureMatrix);
                        break;

                    case TMAP_TEXTURE_MAP_MODE_TEXTURE_MATRIX:
                    	re.setTextureMatrixElements(texture_uploaded_matrix);
                        //gl.glMultMatrixf(texture_uploaded_matrix, 0);
                        break;

                    case TMAP_TEXTURE_MAP_MODE_ENVIRONMENT_MAP: {
                    	re.setTextureEnvironmentMapping(tex_shade_u, tex_shade_v);
                        re.enableFlag(IRenderingEngine.RE_TEXTURE_GEN_S);
                        re.enableFlag(IRenderingEngine.RE_TEXTURE_GEN_T);
                    	re.setTextureMatrixElements(tex_envmap_matrix);
                        break;
                    }

                    default:
                        log("Unhandled texture matrix mode " + tex_map_mode);
                }
            }

            textureMatrixUpload.setChanged(false);
        }

        boolean useVertexColor = false;
        if (!lightingFlag.isEnabled() || transform_mode == VTYPE_TRANSFORM_PIPELINE_RAW_COORD) {
        	re.disableFlag(IRenderingEngine.RE_COLOR_MATERIAL);
            if (vinfo.color != 0) {
                useVertexColor = true;
            } else {
                if (materialChanged) {
                	re.setVertexColor(mat_ambient);
                    materialChanged = false;
                }
            }
        } else if (vinfo.color != 0 && mat_flags != 0) {
            useVertexColor = true;
            if (materialChanged) {
            	boolean ambient = (mat_flags & 1) != 0;
            	boolean diffuse = (mat_flags & 2) != 0;
            	boolean specular = (mat_flags & 4) != 0;
                if (!ambient) {
                	re.setMaterialAmbientColor(mat_ambient);
                }
                if (!diffuse) {
                	re.setMaterialDiffuseColor(mat_diffuse);
                }
                if (!specular) {
                	re.setMaterialSpecularColor(mat_specular);
                }
                re.setColorMaterial(ambient, diffuse, specular);
            	re.enableFlag(IRenderingEngine.RE_COLOR_MATERIAL);
                materialChanged = false;
            }
        } else {
        	re.disableFlag(IRenderingEngine.RE_COLOR_MATERIAL);
            if (materialChanged) {
            	re.setMaterialAmbientColor(mat_ambient);
            	re.setMaterialDiffuseColor(mat_diffuse);
            	re.setMaterialSpecularColor(mat_specular);
            	re.setColorMaterial(false, false, false);
                materialChanged = false;
            }
        }

        re.setTextureWrapMode(tex_wrap_s, tex_wrap_t);

        int mipmapBaseLevel = 0;
        int mipmapMaxLevel = texture_num_mip_maps;
        if (tex_mipmap_mode == TBIAS_MODE_CONST) {
            // TBIAS_MODE_CONST uses the tex_mipmap_bias_int level supplied by TBIAS.
            mipmapBaseLevel = tex_mipmap_bias_int;
            mipmapMaxLevel = tex_mipmap_bias_int;
            if (isLogDebugEnabled) {
                log.debug("TBIAS_MODE_CONST " + tex_mipmap_bias_int);
            }
        } else if (tex_mipmap_mode == TBIAS_MODE_AUTO) {
            // TBIAS_MODE_AUTO performs a comparison between the texture's weight and height at level 0.
            int maxValue = Math.max(texture_width[0], texture_height[0]);

            if(maxValue <= 1) {
                mipmapBaseLevel = 0;
            } else {
                mipmapBaseLevel = (int) ((Math.log((Math.abs(maxValue) / Math.abs(zpos))) / Math.log(2)) + tex_mipmap_bias);
            }
            mipmapMaxLevel = mipmapBaseLevel;
            if (isLogDebugEnabled) {
                log.debug("TBIAS_MODE_AUTO " + tex_mipmap_bias + ", param=" + maxValue);
            }
        } else if (tex_mipmap_mode == TBIAS_MODE_SLOPE) {
            // TBIAS_MODE_SLOPE uses the tslope_level level supplied by TSLOPE.
            mipmapBaseLevel = (int) ((Math.log(Math.abs(tslope_level) / Math.abs(zpos)) / Math.log(2)) + tex_mipmap_bias);
            mipmapMaxLevel = mipmapBaseLevel;
            if (isLogDebugEnabled) {
                log.debug("TBIAS_MODE_SLOPE " + tex_mipmap_bias + ", slope=" + tslope_level);
            }
        }

        // Clamp to [0..texture_num_mip_maps]
        mipmapBaseLevel = Math.max(0, Math.min(mipmapBaseLevel, texture_num_mip_maps));
        // Clamp to [mipmapBaseLevel..texture_num_mip_maps]
        mipmapMaxLevel = Math.max(mipmapBaseLevel, Math.min(mipmapMaxLevel, texture_num_mip_maps));
        if (isLogDebugEnabled) {
            log.debug("Texture Mipmap base=" + mipmapBaseLevel + ", max=" + mipmapMaxLevel + ", textureNumMipmaps=" + texture_num_mip_maps);
        }
        re.setTextureMipmapMinLevel(mipmapBaseLevel);
        re.setTextureMipmapMaxLevel(mipmapMaxLevel);

        return useVertexColor;
    }

    private void endRendering(boolean useVertexColor, boolean useTexture, int numberOfVertex) {
        Memory mem = Memory.getInstance();

        // VADDR/IADDR are updated after vertex rendering
        // (IADDR when indexed and VADDR when not).
        // Some games rely on this and don't reload VADDR/IADDR between 2 PRIM/BBOX calls.
        if (vinfo.index == 0) {
            vinfo.ptr_vertex = vinfo.getAddress(mem, numberOfVertex);
        } else {
            vinfo.ptr_index += numberOfVertex * vinfo.index;
        }

        switch (tex_map_mode) {
            case TMAP_TEXTURE_MAP_MODE_ENVIRONMENT_MAP: {
                re.disableFlag(IRenderingEngine.RE_TEXTURE_GEN_S);
                re.disableFlag(IRenderingEngine.RE_TEXTURE_GEN_T);
                break;
            }
        }

        gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
        if (vinfo.texture != 0 || useTexture) {
            gl.glDisableClientState(GL.GL_TEXTURE_COORD_ARRAY);
        }
        if (useVertexColor) {
            gl.glDisableClientState(GL.GL_COLOR_ARRAY);
        }
        if (vinfo.normal != 0) {
            gl.glDisableClientState(GL.GL_NORMAL_ARRAY);
        }

        if (transform_mode == VTYPE_TRANSFORM_PIPELINE_RAW_COORD) {
            gl.glPopAttrib();
            if (useShaders) {
                gl.glUniform1i(Uniforms.lightingEnable.getId(), lightingFlag.isEnabledInt());
            }
        }
    }

    float spline_n(int i, int j, float u, int[] knot) {
    	if(j == 0) {
    		if(knot[i] <= u && u < knot[i + 1])
    			return 1;
    		return 0;
    	}
    	float res = 0;
    	if(knot[i + j] - knot[i] != 0)
    	    res += (u - knot[i]) / (knot[i + j] - knot[i]) * spline_n(i, j - 1, u, knot);
    	if(knot[i + j + 1] - knot[i + 1] != 0)
    		res += (knot[i + j + 1] - u) / (knot[i + j + 1] - knot[i + 1]) * spline_n(i + 1, j - 1, u, knot);
    	return res;
    }

    int[] spline_knot(int n, int type) {
    	int[] knot = new int[n + 5];
    	for(int i = 0; i < n - 1; i++) {
    		knot[i + 3] = i;
        }

    	if((type & 1) == 0) {
    		knot[0] = -3;
    		knot[1] = -2;
    		knot[2] = -1;
    	}
    	if((type & 2) == 0) {
    		knot[n + 2] = n - 1;
    		knot[n + 3] = n;
    		knot[n + 4] = n + 1;
    	} else {
    		knot[n + 2] = n - 2;
    		knot[n + 3] = n - 2;
    		knot[n + 4] = n - 2;
    	}

    	return knot;
    }

    private void drawSpline(int ucount, int vcount, int utype, int vtype) {
        if (ucount < 4 || vcount < 4) {
            log.warn("Unsupported spline parameters uc=" + ucount + " vc=" + vcount);
            return;
        }

        boolean useVertexColor = initRendering();
        boolean useTexture = vinfo.texture != 0 || textureFlag.isEnabled();
        boolean useNormal = lightingFlag.isEnabled();

        // Generate control points.
        VertexState[][] ctrlpoints = getControlPoints(ucount, vcount);

        // GE capture.
        if (State.captureGeNextFrame && !isVertexBufferEmbedded()) {
            log.info("Capture drawSpline");
            CaptureManager.captureRAM(vinfo.ptr_vertex, vinfo.vertexSize * ucount * vcount);
        }

        // Generate patch VertexState.
        VertexState[][] patch = new VertexState[patch_div_s + 1][patch_div_t + 1];

        // Calculate knot arrays.
        int n = ucount - 1;
        int m = vcount - 1;
        int[] knot_u = spline_knot(n, utype);
        int[] knot_v = spline_knot(m, vtype);

        // The spline grows to a limit defined by n - 2 for u and m - 2 for v.
        // This limit is open, so we need to get a very close approximation of it.
        float limit = 2.000001f;

        // Process spline vertexes with Cox-deBoor's algorithm.
        for(int j = 0; j <= patch_div_t; j++) {
        	float v = (float)j * (float)(m - limit) / (float)patch_div_t;

        	for(int i = 0; i <= patch_div_s; i++) {
        		float u = (float)i * (float)(n - limit) / (float)patch_div_s;

        		patch[i][j] = new VertexState();
        		VertexState p = patch[i][j];

        		for(int ii = 0; ii <= n; ii++) {
        			for(int jj = 0; jj <= m; jj++) {
        				float f = spline_n(ii, 3, u, knot_u) * spline_n(jj, 3, v, knot_v);
        				if(f != 0) {
        					pointMultAdd(p, ctrlpoints[ii][jj], f, useVertexColor, useTexture, useNormal);
        				}
        			}
        		}
        		if(useTexture && vinfo.texture == 0) {
        			p.t[0] = u;
        			p.t[1] = v;
        		}
        	}
        }

        drawCurvedSurface(patch, ucount, vcount, useVertexColor, useTexture, useNormal);
    }

	private void pointMultAdd(VertexState dest, VertexState src, float f, boolean useVertexColor, boolean useTexture, boolean useNormal) {
		dest.p[0] += f * src.p[0];
		dest.p[1] += f * src.p[1];
		dest.p[2] += f * src.p[2];
		if(useTexture) {
			dest.t[0] += f * src.t[0];
			dest.t[1] += f * src.t[1];
		}
		if(useVertexColor) {
			dest.c[0] += f * src.c[0];
			dest.c[1] += f * src.c[1];
			dest.c[2] += f * src.c[2];
		}
		if(useNormal) {
			dest.n[0] += f * src.n[0];
			dest.n[1] += f * src.n[1];
			dest.n[2] += f * src.n[2];
		}
	}

    private void drawBezier(int ucount, int vcount) {
        if ((ucount - 1) % 3 != 0 && (vcount - 1) % 3 != 0) {
            log.warn("Unsupported bezier parameters ucount=" + ucount + " vcount=" + vcount);
            return;
        }

        boolean useVertexColor = initRendering();
        boolean useTexture = vinfo.texture != 0 || textureFlag.isEnabled();
        boolean useNormal = lightingFlag.isEnabled();

        VertexState[][] anchors = getControlPoints(ucount, vcount);

        // Don't capture the ram if the vertex list is embedded in the display list. TODO handle stall_addr == 0 better
        // TODO may need to move inside the loop if indices are used, or find the largest index so we can calculate the size of the vertex list
        if (State.captureGeNextFrame && !isVertexBufferEmbedded()) {
            log.info("Capture drawBezier");
            CaptureManager.captureRAM(vinfo.ptr_vertex, vinfo.vertexSize * ucount * vcount);
        }

        // Generate patch VertexState.
        VertexState[][] patch = new VertexState[patch_div_s + 1][patch_div_t + 1];

        // Number of patches in the U and V directions
        int upcount = ucount / 3;
        int vpcount = vcount / 3;

        float[][] ucoeff = new float[patch_div_s + 1][];

        for(int j = 0; j <= patch_div_t; j++) {
        	float vglobal = (float)j * vpcount / (float)patch_div_t;

        	int vpatch = (int)vglobal; // Patch number
        	float v = vglobal - vpatch;
        	if(j == patch_div_t) {
    			vpatch--;
    			v = 1.f;
    		}
        	float[] vcoeff = BernsteinCoeff(v);

        	for(int i = 0; i <= patch_div_s; i++) {
        		float uglobal = (float)i * upcount / (float)patch_div_s;
        		int upatch = (int)uglobal;
        		float u = uglobal - upatch;
        		if(i == patch_div_s) {
        			upatch--;
        			u = 1.f;
        		}
        		ucoeff[i] = BernsteinCoeff(u);

        		patch[i][j] = new VertexState();
        		VertexState p = patch[i][j];

        		for(int ii = 0; ii < 4; ++ii) {
        			for(int jj = 0; jj < 4; ++jj) {
        				pointMultAdd(p,
        						anchors[3 * upatch + ii][3 * vpatch + jj],
        						ucoeff[i][ii] * vcoeff[jj],
        						useVertexColor, useTexture, useNormal);
        			}
        		}

        		if(useTexture && vinfo.texture == 0) {
        			p.t[0] = uglobal;
        			p.t[1] = vglobal;
        		}
        	}
        }

        drawCurvedSurface(patch, ucount, vcount, useVertexColor, useTexture, useNormal);
    }

	private void drawCurvedSurface(VertexState[][] patch, int ucount, int vcount,
			boolean useVertexColor, boolean useTexture, boolean useNormal) {
		// TODO: Compute the normals
		bindBuffers(useVertexColor, useTexture, useNormal, true, 0);

        for(int j = 0; j <= patch_div_t - 1; j++) {
        	vboFloatBuffer.clear();

        	for(int i = 0; i <= patch_div_s; i++) {
        		VertexState v1 = patch[i][j];
                VertexState v2 = patch[i][j + 1];

        		if(useTexture)     vboFloatBuffer.put(v1.t);
        		if(useVertexColor) vboFloatBuffer.put(v1.c);
        		if(useNormal)      vboFloatBuffer.put(v1.n);
        		vboFloatBuffer.put(v1.p);

        		if(useTexture)     vboFloatBuffer.put(v2.t);
        		if(useVertexColor) vboFloatBuffer.put(v2.c);
        		if(useNormal)      vboFloatBuffer.put(v2.n);
        		vboFloatBuffer.put(v2.p);
        	}

        	glBufferData(GL.GL_ARRAY_BUFFER, vboFloatBuffer.position() * BufferUtil.SIZEOF_FLOAT, vboFloatBuffer.rewind(), GL.GL_STREAM_DRAW);
            gl.glDrawArrays(patch_prim_types[patch_prim], 0, (patch_div_s + 1) * 2);
        }

        endRendering(useVertexColor, useTexture, ucount * vcount);
	}

	private VertexState[][] getControlPoints(int ucount, int vcount) {
		VertexState[][] controlPoints = new VertexState[ucount][vcount];

		Memory mem = Memory.getInstance();
        for (int u = 0; u < ucount; u++) {
            for (int v = 0; v < vcount; v++) {
                int addr = vinfo.getAddress(mem, v * ucount + u);
                VertexState vs = vinfo.readVertex(mem, addr);
                if (isLogDebugEnabled) {
                	log(String.format("control point #%d,%d p(%f,%f,%f) t(%f,%f), c(%f,%f,%f)",
                			u, v,
                			vs.p[0], vs.p[1], vs.p[2],
                			vs.t[0], vs.t[1],
                			vs.c[0], vs.c[1], vs.c[2]));
                }
                controlPoints[u][v] = vs;
            }
        }
        return controlPoints;
	}

    private float[] BernsteinCoeff(float u) {
        float uPow2 = u * u;
        float uPow3 = uPow2 * u;
        float u1 = 1 - u;
        float u1Pow2 = u1 * u1;
        float u1Pow3 = u1Pow2 * u1;
        return new float[] {u1Pow3, 3 * u * u1Pow2, 3 * uPow2 * u1, uPow3 };
    }

    private Buffer getTexture32BitBuffer(int texaddr, int level) {
        Buffer final_buffer = null;

        if (!texture_swizzle) {
            // texture_width might be larger than texture_buffer_width
            int bufferlen = Math.max(texture_buffer_width[level], texture_width[level]) * texture_height[level] * 4;
            final_buffer = Memory.getInstance().getBuffer(texaddr, bufferlen);
            if (final_buffer == null) {
                int length = texture_buffer_width[level] * texture_height[level];
                IMemoryReader memoryReader = MemoryReader.getMemoryReader(texaddr, length * 4, 4);
                for (int i = 0; i < length; i++) {
                    tmp_texture_buffer32[i] = memoryReader.readNext();
                }
                final_buffer = IntBuffer.wrap(tmp_texture_buffer32);
            }

            if (State.captureGeNextFrame) {
                log.info("Capture getTexture32BitBuffer unswizzled");
                CaptureManager.captureRAM(texaddr, bufferlen);
            }
        } else {
            final_buffer = unswizzleTextureFromMemory(texaddr, 4, level);
        }

        return final_buffer;
    }

    public final static String getPsmName(final int psm) {
        return (psm >= 0 && psm < psm_names.length)
                ? psm_names[psm % psm_names.length]
                : "PSM_UNKNOWN" + psm;
    }

    public final static String getLOpName(final int ops) {
        return (ops >= 0 && ops < logical_ops_names.length)
                ? logical_ops_names[ops % logical_ops_names.length]
                : "UNKNOWN_LOP" + ops;
    }

    private int getCompressedTextureSize(int level, int compressionRatio) {
        return getCompressedTextureSize(texture_width[level], texture_height[level], compressionRatio);
    }

    public static int getCompressedTextureSize(int width, int height, int compressionRatio) {
        int compressedTextureWidth = ((width + 3) / 4) * 4;
        int compressedTextureHeight = ((height + 3) / 4) * 4;
        int compressedTextureSize = compressedTextureWidth * compressedTextureHeight * 4 / compressionRatio;

        return compressedTextureSize;
    }

    private void glBindBuffer() {
        glBindBuffer(vboBufferId[0]);
    }

    public void glBindBuffer(int bufferId) {
        if (useVBO) {
            if (openGL1_5) {
                gl.glBindBuffer(GL.GL_ARRAY_BUFFER, bufferId);
            } else {
                gl.glBindBufferARB(GL.GL_ARRAY_BUFFER, bufferId);
            }
        }
    }

    private void updateGeBuf() {
        if (geBufChanged) {
            display.hleDisplaySetGeBuf(gl, fbp, fbw, psm, somethingDisplayed);
            geBufChanged = false;

            textureChanged = true;
            maxSpriteHeight = 0;
            projectionMatrixUpload.setChanged(true);
            modelMatrixUpload.setChanged(true);
            viewMatrixUpload.setChanged(true);
            textureMatrixUpload.setChanged(true);
            viewportChanged = true;
            depthChanged = true;
            materialChanged = true;
        }
    }
    // For capture/replay

    public int getFBP() {
        return fbp;
    }

    public int getFBW() {
        return fbw;
    }

    public int getZBP() {
        return zbp;
    }

    public int getZBW() {
        return zbw;
    }

    public int getPSM() {
        return psm;
    }

    private boolean isVertexBufferEmbedded() {
        // stall_addr may be 0
        return (vinfo.ptr_vertex >= currentList.list_addr && vinfo.ptr_vertex < currentList.getStallAddr());
    }

    private boolean isVRAM(int addr) {
        addr &= Memory.addressMask;

        return addr >= MemoryMap.START_VRAM && addr <= MemoryMap.END_VRAM;
    }

    private void hlePerformAction(IAction action, Semaphore sync) {
        hleAction = action;

        while (true) {
            try {
                sync.acquire();
                break;
            } catch (InterruptedException e) {
                // Retry again..
            }
        }
    }

    public void hleSaveContext(pspGeContext context) {
        // If we are rendering, we have to wait for a consistent state
        // before saving the context: let the display thread perform
        // the save when appropriate.
        if (hasDrawLists() || currentList != null) {
            Semaphore sync = new Semaphore(0);
            hlePerformAction(new SaveContextAction(context, sync), sync);
        } else {
            saveContext(context);
        }
    }

    public void hleRestoreContext(pspGeContext context) {
        // If we are rendering, we have to wait for a consistent state
        // before restoring the context: let the display thread perform
        // the restore when appropriate.
        if (hasDrawLists() || currentList != null) {
            Semaphore sync = new Semaphore(0);
            hlePerformAction(new RestoreContextAction(context, sync), sync);
        } else {
            restoreContext(context);
        }
    }

    private void saveContext(pspGeContext context) {
        context.base = base;
        context.baseOffset = baseOffset;

        context.fbp = fbp;
        context.fbw = fbw;
        context.zbp = zbp;
        context.zbw = zbw;
        context.psm = psm;

        context.flags = 0;
        for (EnableDisableFlag flag : flags) {
            context.flags = flag.save(context.flags);
        }

        context.region_x1 = region_x1;
        context.region_y1 = region_y1;
        context.region_x2 = region_x2;
        context.region_y2 = region_y2;
        context.region_width = region_width;
        context.region_height = region_height;
        context.scissor_x1 = scissor_x1;
        context.scissor_y1 = scissor_y1;
        context.scissor_x2 = scissor_x2;
        context.scissor_y2 = scissor_y2;
        context.scissor_width = scissor_width;
        context.scissor_height = scissor_height;
        context.offset_x = offset_x;
        context.offset_y = offset_y;
        context.viewport_width = viewport_width;
        context.viewport_height = viewport_height;
        context.viewport_cx = viewport_cx;
        context.viewport_cy = viewport_cy;

        System.arraycopy(proj_uploaded_matrix, 0, context.proj_uploaded_matrix, 0, proj_uploaded_matrix.length);
        System.arraycopy(texture_uploaded_matrix, 0, context.texture_uploaded_matrix, 0, texture_uploaded_matrix.length);
        System.arraycopy(model_uploaded_matrix, 0, context.model_uploaded_matrix, 0, model_uploaded_matrix.length);
        System.arraycopy(view_uploaded_matrix, 0, context.view_uploaded_matrix, 0, view_uploaded_matrix.length);
        System.arraycopy(morph_weight, 0, context.morph_weight, 0, morph_weight.length);
        System.arraycopy(tex_envmap_matrix, 0, context.tex_envmap_matrix, 0, tex_envmap_matrix.length);
        if (pspGeContext.fullVersion) {
            for (int i = 0; i < bone_uploaded_matrix.length; i++) {
                System.arraycopy(bone_uploaded_matrix[i], 0, context.bone_uploaded_matrix[i], 0, bone_uploaded_matrix[i].length);
            }
        }
        for (int i = 0; i < light_pos.length; i++) {
            System.arraycopy(light_pos[i], 0, context.light_pos[i], 0, light_pos[i].length);
            System.arraycopy(light_dir[i], 0, context.light_dir[i], 0, light_dir[i].length);
        }

        System.arraycopy(light_enabled, 0, context.light_enabled, 0, light_enabled.length);
        System.arraycopy(light_type, 0, context.light_type, 0, light_type.length);
        System.arraycopy(light_kind, 0, context.light_kind, 0, light_kind.length);
        System.arraycopy(spotLightExponent, 0, context.spotLightExponent, 0, spotLightExponent.length);
        System.arraycopy(spotLightCutoff, 0, context.spotLightCutoff, 0, spotLightCutoff.length);

        System.arraycopy(fog_color, 0, context.fog_color, 0, fog_color.length);
        context.fog_far = fog_far;
        context.fog_dist = fog_dist;

        context.nearZ = nearZ;
        context.farZ = farZ;
        context.zscale = zscale;
        context.zpos = zpos;

        context.mat_flags = mat_flags;
        System.arraycopy(mat_ambient, 0, context.mat_ambient, 0, mat_ambient.length);
        System.arraycopy(mat_diffuse, 0, context.mat_diffuse, 0, mat_diffuse.length);
        System.arraycopy(mat_specular, 0, context.mat_specular, 0, mat_specular.length);
        System.arraycopy(mat_emissive, 0, context.mat_emissive, 0, mat_emissive.length);

        System.arraycopy(ambient_light, 0, context.ambient_light, 0, ambient_light.length);

        context.texture_storage = texture_storage;
        context.texture_num_mip_maps = texture_num_mip_maps;
        context.texture_swizzle = texture_swizzle;

        System.arraycopy(texture_base_pointer, 0, context.texture_base_pointer, 0, texture_base_pointer.length);
        System.arraycopy(texture_width, 0, context.texture_width, 0, texture_width.length);
        System.arraycopy(texture_height, 0, context.texture_height, 0, texture_height.length);
        System.arraycopy(texture_buffer_width, 0, context.texture_buffer_width, 0, texture_buffer_width.length);
        context.tex_min_filter = tex_min_filter;
        context.tex_mag_filter = tex_mag_filter;

        context.tex_translate_x = tex_translate_x;
        context.tex_translate_y = tex_translate_y;
        context.tex_scale_x = tex_scale_x;
        context.tex_scale_y = tex_scale_y;
        System.arraycopy(tex_env_color, 0, context.tex_env_color, 0, tex_env_color.length);
        context.tex_enable = textureFlag.isEnabledInt();

        context.tex_clut_addr = tex_clut_addr;
        context.tex_clut_num_blocks = tex_clut_num_blocks;
        context.tex_clut_mode = tex_clut_mode;
        context.tex_clut_shift = tex_clut_shift;
        context.tex_clut_mask = tex_clut_mask;
        context.tex_clut_start = tex_clut_start;
        context.tex_wrap_s = tex_wrap_s;
        context.tex_wrap_t = tex_wrap_t;
        context.patch_div_s = patch_div_s;
        context.patch_div_t = patch_div_t;

        context.transform_mode = transform_mode;

        context.textureTx_sourceAddress = textureTx_sourceAddress;
        context.textureTx_sourceLineWidth = textureTx_sourceLineWidth;
        context.textureTx_destinationAddress = textureTx_destinationAddress;
        context.textureTx_destinationLineWidth = textureTx_destinationLineWidth;
        context.textureTx_width = textureTx_width;
        context.textureTx_height = textureTx_height;
        context.textureTx_sx = textureTx_sx;
        context.textureTx_sy = textureTx_sy;
        context.textureTx_dx = textureTx_dx;
        context.textureTx_dy = textureTx_dy;
        context.textureTx_pixelSize = textureTx_pixelSize;

        System.arraycopy(dfix_color, 0, context.dfix_color, 0, dfix_color.length);
        System.arraycopy(sfix_color, 0, context.sfix_color, 0, sfix_color.length);
        context.blend_src = blend_src;
        context.blend_dst = blend_dst;

        context.clearMode = clearMode;
        context.depthFuncClearMode = clearModeDepthFunc;

        context.depthFunc = depthFunc;

        context.tex_map_mode = tex_map_mode;
        context.tex_proj_map_mode = tex_proj_map_mode;

        System.arraycopy(colorMask, 0, context.glColorMask, 0, colorMask.length);

        context.copyGLToContext(gl);
    }

    private void restoreContext(pspGeContext context) {
        base = context.base;
        baseOffset = context.baseOffset;

        fbp = context.fbp;
        fbw = context.fbw;
        zbp = context.zbp;
        zbw = context.zbw;
        psm = context.psm;

        for (EnableDisableFlag flag : flags) {
            flag.restore(context.flags);
        }

        region_x1 = context.region_x1;
        region_y1 = context.region_y1;
        region_x2 = context.region_x2;
        region_y2 = context.region_y2;
        region_width = context.region_width;
        region_height = context.region_height;
        scissor_x1 = context.scissor_x1;
        scissor_y1 = context.scissor_y1;
        scissor_x2 = context.scissor_x2;
        scissor_y2 = context.scissor_y2;
        scissor_width = context.scissor_width;
        scissor_height = context.scissor_height;
        offset_x = context.offset_x;
        offset_y = context.offset_y;
        viewport_width = context.viewport_width;
        viewport_height = context.viewport_height;
        viewport_cx = context.viewport_cx;
        viewport_cy = context.viewport_cy;

        System.arraycopy(context.proj_uploaded_matrix, 0, proj_uploaded_matrix, 0, proj_uploaded_matrix.length);
        System.arraycopy(context.texture_uploaded_matrix, 0, texture_uploaded_matrix, 0, texture_uploaded_matrix.length);
        System.arraycopy(context.model_uploaded_matrix, 0, model_uploaded_matrix, 0, model_uploaded_matrix.length);
        System.arraycopy(context.view_uploaded_matrix, 0, view_uploaded_matrix, 0, view_uploaded_matrix.length);
        System.arraycopy(context.morph_weight, 0, morph_weight, 0, morph_weight.length);
        System.arraycopy(context.tex_envmap_matrix, 0, tex_envmap_matrix, 0, tex_envmap_matrix.length);
        if (pspGeContext.fullVersion) {
            for (int i = 0; i < bone_uploaded_matrix.length; i++) {
                System.arraycopy(context.bone_uploaded_matrix[i], 0, bone_uploaded_matrix[i], 0, bone_uploaded_matrix[i].length);
            }
        }
        for (int i = 0; i < light_pos.length; i++) {
            System.arraycopy(context.light_pos[i], 0, light_pos[i], 0, light_pos[i].length);
            System.arraycopy(context.light_dir[i], 0, light_dir[i], 0, light_dir[i].length);
        }

        System.arraycopy(context.light_enabled, 0, light_enabled, 0, light_enabled.length);
        System.arraycopy(context.light_type, 0, light_type, 0, light_type.length);
        System.arraycopy(context.light_kind, 0, light_kind, 0, light_kind.length);
        System.arraycopy(context.spotLightExponent, 0, spotLightExponent, 0, spotLightExponent.length);
        System.arraycopy(context.spotLightCutoff, 0, spotLightCutoff, 0, spotLightCutoff.length);

        System.arraycopy(context.fog_color, 0, fog_color, 0, fog_color.length);
        fog_far = context.fog_far;
        fog_dist = context.fog_dist;

        nearZ = context.nearZ;
        farZ = context.farZ;
        zscale = context.zscale;
        zpos = context.zpos;

        mat_flags = context.mat_flags;
        System.arraycopy(context.mat_ambient, 0, mat_ambient, 0, mat_ambient.length);
        System.arraycopy(context.mat_diffuse, 0, mat_diffuse, 0, mat_diffuse.length);
        System.arraycopy(context.mat_specular, 0, mat_specular, 0, mat_specular.length);
        System.arraycopy(context.mat_emissive, 0, mat_emissive, 0, mat_emissive.length);

        System.arraycopy(context.ambient_light, 0, ambient_light, 0, ambient_light.length);

        texture_storage = context.texture_storage;
        texture_num_mip_maps = context.texture_num_mip_maps;
        texture_swizzle = context.texture_swizzle;

        System.arraycopy(context.texture_base_pointer, 0, texture_base_pointer, 0, texture_base_pointer.length);
        System.arraycopy(context.texture_width, 0, texture_width, 0, texture_width.length);
        System.arraycopy(context.texture_height, 0, texture_height, 0, texture_height.length);
        System.arraycopy(context.texture_buffer_width, 0, texture_buffer_width, 0, texture_buffer_width.length);
        tex_min_filter = context.tex_min_filter;
        tex_mag_filter = context.tex_mag_filter;

        tex_translate_x = context.tex_translate_x;
        tex_translate_y = context.tex_translate_y;
        tex_scale_x = context.tex_scale_x;
        tex_scale_y = context.tex_scale_y;
        System.arraycopy(context.tex_env_color, 0, tex_env_color, 0, tex_env_color.length);
        textureFlag.setEnabled(context.tex_enable);

        tex_clut_addr = context.tex_clut_addr;
        tex_clut_num_blocks = context.tex_clut_num_blocks;
        tex_clut_mode = context.tex_clut_mode;
        tex_clut_shift = context.tex_clut_shift;
        tex_clut_mask = context.tex_clut_mask;
        tex_clut_start = context.tex_clut_start;
        tex_wrap_s = context.tex_wrap_s;
        tex_wrap_t = context.tex_wrap_t;
        patch_div_s = context.patch_div_s;
        patch_div_t = context.patch_div_t;

        transform_mode = context.transform_mode;

        textureTx_sourceAddress = context.textureTx_sourceAddress;
        textureTx_sourceLineWidth = context.textureTx_sourceLineWidth;
        textureTx_destinationAddress = context.textureTx_destinationAddress;
        textureTx_destinationLineWidth = context.textureTx_destinationLineWidth;
        textureTx_width = context.textureTx_width;
        textureTx_height = context.textureTx_height;
        textureTx_sx = context.textureTx_sx;
        textureTx_sy = context.textureTx_sy;
        textureTx_dx = context.textureTx_dx;
        textureTx_dy = context.textureTx_dy;
        textureTx_pixelSize = context.textureTx_pixelSize;

        System.arraycopy(context.dfix_color, 0, dfix_color, 0, dfix_color.length);
        System.arraycopy(context.sfix_color, 0, sfix_color, 0, sfix_color.length);
        blend_src = context.blend_src;
        blend_dst = context.blend_dst;

        clearMode = context.clearMode;
        clearModeDepthFunc = context.depthFuncClearMode;

        depthFunc = context.depthFunc;

        tex_map_mode = context.tex_map_mode;
        tex_proj_map_mode = context.tex_proj_map_mode;

        System.arraycopy(context.glColorMask, 0, colorMask, 0, colorMask.length);

        context.copyContextToGL(gl);

        projectionMatrixUpload.setChanged(true);
        modelMatrixUpload.setChanged(true);
        viewMatrixUpload.setChanged(true);
        textureMatrixUpload.setChanged(true);
        lightingChanged = true;
        blendChanged = true;
        textureChanged = true;
        geBufChanged = true;
        viewportChanged = true;
        depthChanged = true;
        materialChanged = true;
    }

    public boolean isUsingTRXKICK() {
        return usingTRXKICK;
    }

    public int getMaxSpriteHeight() {
        return maxSpriteHeight;
    }

    public void setUseVertexCache(boolean useVertexCache) {
        // VertexCache is relying on VBO
        if (useVBO) {
            this.useVertexCache = useVertexCache;
            if (useVertexCache) {
                VideoEngine.log.info("Using Vertex Cache");
            }
        }
    }

    public int getBase() {
        return base;
    }

    public void setBase(int base) {
        this.base = base;
    }

    public int getBaseOffset() {
        return baseOffset;
    }

    public void setBaseOffset(int baseOffset) {
        this.baseOffset = baseOffset;
    }

    private class SaveContextAction implements IAction {

        private pspGeContext context;
        private Semaphore sync;

        public SaveContextAction(pspGeContext context, Semaphore sync) {
            this.context = context;
            this.sync = sync;
        }

        @Override
        public void execute() {
            saveContext(context);
            sync.release();
        }
    }

    private class RestoreContextAction implements IAction {

        private pspGeContext context;
        private Semaphore sync;

        public RestoreContextAction(pspGeContext context, Semaphore sync) {
            this.context = context;
            this.sync = sync;
        }

        @Override
        public void execute() {
            restoreContext(context);
            sync.release();
        }
    }
}