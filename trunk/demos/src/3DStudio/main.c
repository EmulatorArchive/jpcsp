#include <pspkernel.h>
#include <pspkernel.h>
#include <pspkernel.h>
#include <pspdebug.h>
#include <pspctrl.h>
#include <pspdisplay.h>
#include <pspgu.h>
#include <pspgum.h>

#include <sys/stat.h>
#include <stdio.h>
#include <string.h>
#include <assert.h>
#include <malloc.h>

PSP_MODULE_INFO("3D Studio", 0, 1, 0);
PSP_MAIN_THREAD_ATTR(THREAD_ATTR_USER);

#define BUF_WIDTH	512
#define SCR_WIDTH	480
#define SCR_HEIGHT	272
#define SCR_TEXTURE_WIDTH  512
#define SCR_TEXTURE_HEIGHT 512
#define FONT_HEIGHT	8
#define TEXTURE_WIDTH	128
#define TEXTURE_HEIGHT	128
#define NUMBER_MIPMAPS	8
#define NUMBER_CLUT_ENTRIES	8


//#define USE_VERTEX_8BIT	1
//#define USE_VERTEX_16BIT	1
#define USE_VERTEX_32BITF	1

//#define USE_TEXTURE_8BIT	1
//#define USE_TEXTURE_16BIT	1
#define USE_TEXTURE_32BITF	1

void sendCommandi(int cmd, int argument);
void sendCommandf(int cmd, float argument);

int done = 0;
int selectedAttribute = 0;
int firstRow = 0;
int numberRows = SCR_HEIGHT / FONT_HEIGHT;


static unsigned int __attribute__((aligned(16))) list[262144];
static unsigned int __attribute__((aligned(16))) debugPrintBuffer[SCR_HEIGHT * BUF_WIDTH];

struct DebugPrintVertex
{
	u16 u, v;
	s16 x, y, z;
};
struct DebugPrintVertex __attribute__((aligned(16))) debugPrintVertices[2];

static unsigned int staticOffset = 0;

static unsigned int getMemorySize(unsigned int width, unsigned int height, unsigned int psm)
{
	switch (psm)
	{
		case GU_PSM_T4:
			return (width * height) >> 1;

		case GU_PSM_T8:
			return width * height;

		case GU_PSM_5650:
		case GU_PSM_5551:
		case GU_PSM_4444:
		case GU_PSM_T16:
			return 2 * width * height;

		case GU_PSM_8888:
		case GU_PSM_T32:
			return 4 * width * height;

		default:
			return 0;
	}
}

void* getStaticVramBuffer(unsigned int width, unsigned int height, unsigned int psm)
{
	unsigned int memSize = getMemorySize(width,height,psm);
	void* result = (void*)staticOffset;
	staticOffset += memSize;

	return result;
}


struct attribute
{
	char *label;
	int x;
	int y;
	int *pvalue;
	float *pfvalue;
	float min;
	float max;
	float step;
	char *format;
	char **names;
};

struct attribute attributes[2000];
int nattributes = 0;
int textColor = 0xFFFFFFFF;
int selectedTextColor = 0xFF0000FF;
void* fbp0;
void* fbp1;
void* zbp;


void addAttribute(char *label, int *pvalue, float *pfvalue, int x, int y, float min, float max, float step, char *format)
{
	if (format == NULL)
	{
		if (pvalue != NULL)
		{
			format = "%d";
		}
		else if (pfvalue != NULL)
		{
			format = "%.1f";
		}
	}

	struct attribute *pattribute = &attributes[nattributes];
	pattribute->label  = label;
	pattribute->x	  = x;
	pattribute->y	  = y;
	pattribute->pvalue = pvalue;
	pattribute->pfvalue = pfvalue;
	pattribute->min	= min;
	pattribute->max	= max;
	pattribute->step   = step;
	pattribute->names  = NULL;
	pattribute->format = format;

	nattributes++;
}


void setAttributeValueNames(char **names)
{
	struct attribute *pattribute = &attributes[nattributes - 1];
	pattribute->names = names;
}


void changeAttributeValue(struct attribute *pattribute, float direction)
{
	if (pattribute->pvalue != NULL)
	{
		*(pattribute->pvalue) += direction * pattribute->step;
		if (*(pattribute->pvalue) > pattribute->max)
		{
			*(pattribute->pvalue) = pattribute->max;
		}
		if (*(pattribute->pvalue) < pattribute->min)
		{
			*(pattribute->pvalue) = pattribute->min;
		}
	}
	else if (pattribute->pfvalue != NULL)
	{
		*(pattribute->pfvalue) += direction * pattribute->step;
		if (*(pattribute->pfvalue) > pattribute->max)
		{
			*(pattribute->pfvalue) = pattribute->max;
		}
		if (*(pattribute->pfvalue) < pattribute->min)
		{
			*(pattribute->pfvalue) = pattribute->min;
		}
	}
}

void drawAttribute(struct attribute *pattribute)
{
	char buffer[100];
	char *value = "";

	if (pattribute-> y < firstRow || pattribute->y >= firstRow + numberRows)
	{
		// Outside screen display
		return;
	}

	if (pattribute->names != NULL)
	{
		value = pattribute->names[*(pattribute->pvalue)];
	}
	else if (pattribute->pvalue != NULL)
	{
		sprintf(buffer, pattribute->format, *(pattribute->pvalue));
		value = buffer;
	}
	else if (pattribute->pfvalue != NULL)
	{
		sprintf(buffer, pattribute->format, *(pattribute->pfvalue));
		value = buffer;
	}

	pspDebugScreenSetXY(pattribute->x, pattribute->y - firstRow);
	if (pattribute->label != NULL)
	{
		pspDebugScreenPrintf("%s: %s", pattribute->label, value);
	}
	else
	{
		pspDebugScreenPrintf("%s", value);
	}
}


void drawAttributes()
{
	int i;

	pspDebugScreenSetBase(debugPrintBuffer);
	pspDebugScreenSetOffset(0);
	pspDebugScreenSetBackColor(0x00000000);
	pspDebugScreenClear();
	for (i = 0; i < nattributes; i++)
	{
		pspDebugScreenSetTextColor(selectedAttribute == i ? selectedTextColor : textColor);
			
		drawAttribute(&attributes[i]);
	}
}


int states[] = {
	GU_TEXTURE_2D,
	GU_ALPHA_TEST,
	GU_DEPTH_TEST,
	GU_STENCIL_TEST,
	GU_BLEND,
	GU_LIGHTING,
	GU_LIGHT0,
	GU_LIGHT1,
	GU_LIGHT2,
	GU_LIGHT3,
	GU_CULL_FACE,
	GU_PATCH_CULL_FACE,
	GU_PATCH_FACE,
	GU_FACE_NORMAL_REVERSE,
	GU_FOG,
	GU_CLIP_PLANES,
	GU_SCISSOR_TEST,
	GU_DITHER,
	GU_LINE_SMOOTH,
	GU_COLOR_TEST,
	GU_COLOR_LOGIC_OP,
/*	GU_FRAGMENT_2X,
*/
	};

char *stateNames[] = {
	"GU_ALPHA_TEST  ",
	"GU_DEPTH_TEST  ",
	"GU_SCISSOR_TEST",
	"GU_STENCIL_TEST",
	"GU_BLEND       ",
	"GU_CULL_FACE   ",
	"GU_DITHER      ",
	"GU_FOG         ",
	"GU_CLIP_PLANES ",
	"GU_TEXTURE_2D  ",
	"GU_LIGHTING    ",
	"GU_LIGHT0      ",
	"GU_LIGHT1      ",
	"GU_LIGHT2      ",
	"GU_LIGHT3      ",
	"GU_LINE_SMOOTH ",
	"GU_PATCH_CULL_FACE",
	"GU_COLOR_TEST  ",
	"GU_COLOR_LOGIC_OP",
	"GU_FACE_NORMAL_REVERSE",
	"GU_PATCH_FACE  ",
	"GU_FRAGMENT_2X "
	};

int stateValues1[100];
int stateValues2[100];
char *stateValueNames[] = { "Off", "On", "Unchanged" };


struct Vertex
{
 #ifdef USE_TEXTURE_8BIT
   u8 u, v;
   u16 pad1;
 #endif
 #ifdef USE_TEXTURE_16BIT
   u16 u, v;
 #endif
 #ifdef USE_TEXTURE_32BITF
   float u, v;
 #endif

   u32 color;
   float nx, ny, nz;

 #ifdef USE_VERTEX_8BIT
   s8 x, y, z;
   s8 pad2;
 #endif
 #ifdef USE_VERTEX_16BIT
   s16 x, y, z;
   s16 pad2;
 #endif
 #ifdef USE_VERTEX_32BITF
   float x, y, z;
 #endif
};

struct VertexNoColor
{
 #ifdef USE_TEXTURE_8BIT
   u8 u, v;
 #endif
 #ifdef USE_TEXTURE_16BIT
   u16 u, v;
 #endif
 #ifdef USE_TEXTURE_32BITF
   float u, v;
 #endif

   float nx, ny, nz;

 #ifdef USE_VERTEX_8BIT
   s8 x, y, z;
   s8 pad;
 #endif
 #ifdef USE_VERTEX_16BIT
   s16 x, y, z;
   s16 pad;
 #endif
 #ifdef USE_VERTEX_32BITF
   float x, y, z;
 #endif
};

struct Vertex __attribute__((aligned(16))) vertices1[6];
struct Vertex __attribute__((aligned(16))) vertices2[6];

struct Color
{
	int r;
	int g;
	int b;
	int a;
};

struct Point
{
	float x;
	float y;
	float z;
};

struct Color rectangle1VertexColor;
struct Color rectangle2VertexColor;
struct Color rectangle1TextureColor;
struct Color rectangle2TextureColor;
struct Color mipmapLevelsColor[] = {{ 0x00, 0x00, 0xFF, 0xFF },
                                    { 0x00, 0xFF, 0xFF, 0xFF },
                                    { 0xFF, 0x00, 0xFF, 0xFF },
                                    { 0xFF, 0xFF, 0xFF, 0xFF },
                                    { 0x00, 0x00, 0xFF, 0xFF },
                                    { 0x00, 0xFF, 0xFF, 0xFF },
                                    { 0xFF, 0x00, 0xFF, 0xFF },
								   };
struct Point rectangle1point;
struct Point rectangle2point;
struct Point rectangle1normal;
struct Point rectangle2normal;
ScePspFVector3 rectangle1translation;
ScePspFVector3 rectangle1rotation;
ScePspFVector3 rectangle2translation;
ScePspFVector3 rectangle2rotation;
float rectangle1width = 2;
float rectangle1height = 2;
float rectangle2width = 2;
float rectangle2height = 2;
#define RENDERING_3D	0
#define RENDERING_2D	1
#define RENDERING_OFF	2
int rectangle1rendering = RENDERING_3D;
int rectangle2rendering = RENDERING_3D;
char *renderingNames[] = { "3D", "2D", "Off" };

int textureScale = 2;

/* int zTestPixelX = 375; */
int zTestPixelX = 375 - 64;
int zTestPixelY = 125 + 16;
int geTestPixelX = 375;
int geTestPixelY = 198;

unsigned int __attribute__((aligned(16))) texture1level0[TEXTURE_WIDTH * TEXTURE_HEIGHT];
unsigned int __attribute__((aligned(16))) texture1level1[TEXTURE_WIDTH * TEXTURE_HEIGHT / 4];
unsigned int __attribute__((aligned(16))) texture1level2[TEXTURE_WIDTH * TEXTURE_HEIGHT / 16];
unsigned int __attribute__((aligned(16))) texture1level3[TEXTURE_WIDTH * TEXTURE_HEIGHT / 64];
unsigned int __attribute__((aligned(16))) texture1level4[TEXTURE_WIDTH * TEXTURE_HEIGHT / 256];
unsigned int __attribute__((aligned(16))) texture1level5[TEXTURE_WIDTH * TEXTURE_HEIGHT / 1024];
unsigned int __attribute__((aligned(16))) texture1level6[TEXTURE_WIDTH * TEXTURE_HEIGHT / 4096];
unsigned int __attribute__((aligned(16))) texture1level7[TEXTURE_WIDTH * TEXTURE_HEIGHT / 16384];
unsigned int* texture1[8] = { texture1level0, texture1level1, texture1level2, texture1level3, texture1level4, texture1level5, texture1level6, texture1level7 };
unsigned int __attribute__((aligned(16))) texture2level0[TEXTURE_WIDTH * TEXTURE_HEIGHT];
unsigned int __attribute__((aligned(16))) texture2level1[TEXTURE_WIDTH * TEXTURE_HEIGHT / 4];
unsigned int __attribute__((aligned(16))) texture2level2[TEXTURE_WIDTH * TEXTURE_HEIGHT / 16];
unsigned int __attribute__((aligned(16))) texture2level3[TEXTURE_WIDTH * TEXTURE_HEIGHT / 64];
unsigned int __attribute__((aligned(16))) texture2level4[TEXTURE_WIDTH * TEXTURE_HEIGHT / 256];
unsigned int __attribute__((aligned(16))) texture2level5[TEXTURE_WIDTH * TEXTURE_HEIGHT / 1024];
unsigned int __attribute__((aligned(16))) texture2level6[TEXTURE_WIDTH * TEXTURE_HEIGHT / 4096];
unsigned int __attribute__((aligned(16))) texture2level7[TEXTURE_WIDTH * TEXTURE_HEIGHT / 16384];
unsigned int* texture2[8] = { texture2level0, texture2level1, texture2level2, texture2level3, texture2level4, texture2level5, texture2level6, texture2level7 };

unsigned int __attribute__((aligned(16))) clut1[NUMBER_CLUT_ENTRIES * NUMBER_MIPMAPS];
unsigned int __attribute__((aligned(16))) clut2[NUMBER_CLUT_ENTRIES * NUMBER_MIPMAPS];

int texLevelMode1 = 0;
int texLevelMode2 = 0;
char *texModeNames[] = { "GU_TEXTURE_AUTO", "GU_TEXTURE_CONST", "GU_TEXTURE_SLOPE" };
int texBias1 = 0;
int texBias2 = 0;
float texSlope1 = 0;
float texSlope2 = 0;

int texFunc1 = 0;
int texFunc2 = 0;
char *texFuncNames[] = { "GU_TFX_MODULATE", "GU_TFX_DECAL", "GU_TFX_BLEND", "GU_TFX_REPLACE", "GU_TFX_ADD" };

int texMinFilter1 = 0;
int texMinFilter2 = 0;
int texMagFilter1 = 0;
int texMagFilter2 = 0;
char *texFilterNames[] = { "GU_NEAREST", "GU_LINEAR", "Unknown2", "Unknown3", "GU_NEAREST_MIPMAP_NEAREST", "GU_LINEAR_MIPMAP_NEAREST", "GU_NEAREST_MIPMAP_LINEAR", "GU_LINEAR_MIPMAP_LINEAR" };

int texFuncAlpha1 = 1;
int texFuncAlpha2 = 1;
char *texFuncAlphaNames[] = { "RGB", "ALPHA", "UNKNOWN 0x81" };

int texFuncDouble1 = 0;
int texFuncDouble2 = 0;
char *texFuncDoubleNames[] = { "RGBx1", "RGBx2" };

int depthMask = 0;
char *depthMaskNames[] = { "enableWrites", "disableWrites" };

struct Color pixelMask;

int alphaFunc = 7;
char *testFuncNames[] = { "GU_NEVER", "GU_ALWAYS", "GU_EQUAL", "GU_NOTEQUAL", "GU_LESS", "GU_LEQUAL", "GU_GREATER", "GU_GEQUAL" };
int alphaReference = 0;

int blendOp = 0;
char *blendOpNames[] = { "GU_ADD", "GU_SUBTRACT", "GU_REVERSE_SUBTRACT", "GU_MIN", "GU_MAX", "GU_ABS" };

int blendFuncSrc = 2;
int blendFuncDst = 3;
char *blendFuncNames[] = { "GU_SRC_COLOR", "GU_ONE_MINUS_SRC_COLOR", "GU_SRC_ALPHA", "GU_ONE_MINUS_SRC_ALPHA", "GU_DST_ALPHA", "GU_ONE_MINUS_DST_ALPHA", "GU_DOUBLE_SRC_ALPHA", "GU_ONE_MINUS_DOUBLE_SRC_ALPHA", "GU_DOUBLE_DST_ALPHA", "GU_ONE_MINUS_DOUBLE_DST_ALPHA", "GU_FIX" };
struct Color blendSFix;
struct Color blendDFix;

int textureType1 = 0;
int textureType2 = 0;
int mipmapLevel1Type = 1;
char *textureTypeNames[] = { "Checkboard 1x1", "Checkboard 3x3", "Unicolor", "Vertical", "Horizontal", "Center" };

struct Color backgroundColor;
int clearMode = 0;
char *clearModeNames[] = { "Normal", "Draw in CLEAR command" };

int clearFlagColor = 1;
int clearFlagStencil = 0;
int clearStencil = 0;
int clearFlagDepth = 1;
int clearDepth = 0;
char *onOffNames[] = { "Off", "On" };


int depthFunc = 7;
int nearZ = 10000;
int farZ = 50000;
int zscale = 20000;
int zpos = 30000;

struct Color materialAmbient;
int materialAmbientFlag = 0;
struct Color materialDiffuse;
int materialDiffuseFlag = 0;
struct Color materialEmissive;
int materialEmissiveFlag = 0;
struct Color materialSpecular;
int materialSpecularFlag = 0;
int vertexColorFlag = 1;
int tpsm1 = GU_PSM_8888;
int tpsm2 = GU_PSM_8888;
char *tpsmNames[] = { "GU_PSM_5650", "GU_PSM_5551", "GU_PSM_4444", "GU_PSM_8888", "GU_PSM_T4", "GU_PSM_T8", "GU_PSM_T16", "GU_PSM_T32" };

struct Color ambientColor;
struct Color texEnvColor;

int texture1_a2 = 0;
int texture2_a2 = 0;

float fogNear = 3;
float fogFar = 5;
struct Color fogColor;

struct Light
{
	ScePspFVector3 position;
	ScePspFVector3 direction;
	struct Color ambientColor;
	struct Color diffuseColor;
	struct Color specularColor;
	float constantAttenuation;
	float linearAttenuation;
	float quadraticAttenuation;
	int type;
	int kind;
	float spotExponent;
	float spotCutoff;
};
#define NUM_LIGHTS	4
struct Light lights[NUM_LIGHTS];
char *lightTypeNames[] = { "Directional", "Point", "Spot" };
char *lightKindNames[] = { "Ambient & Diffuse", "Diffuse & Specular", "Unknown1", "Unknown2" };
int lightMode = 0;
char *lightModeNames[] = { "Single Color", "Separate Specular Color" };

int viewportX = 2048;
int viewportY = 2048;
int viewportWidth = SCR_WIDTH;
int viewportHeight = SCR_HEIGHT;
int offsetX = 2048 - (SCR_WIDTH / 2);
int offsetY = 2048 - (SCR_HEIGHT / 2);
int regionX = 0;
int regionY = 0;
int regionWidth = SCR_WIDTH;
int regionHeight = SCR_HEIGHT;
int scissorX = 0;
int scissorY = 0;
int scissorWidth = SCR_WIDTH;
int scissorHeight = SCR_HEIGHT;
int displayMode = 0;
int displayWidth = SCR_WIDTH;
int displayHeight = SCR_HEIGHT;

int stencilOpFail = 0;
int stencilOpZFail = 0;
int stencilOpZPass = 0;
char *stencilOpNames[] = { "GU_KEEP", "GU_ZERO", "GU_REPLACE", "GU_INVERT", "GU_INCR", "GU_DECR" };
int stencilFunc = 1;
int stencilReference = 0;
int stencilMask = 0xFF;

int rectangle1PrimType = GU_TRIANGLE_STRIP;
int rectangle2PrimType = GU_TRIANGLE_STRIP;
char *primTypeNames[] = { "GU_POINTS", "GU_LINES", "GU_LINE_STRIP", "GU_TRIANGLES", "GU_TRIANGLE_STRIP", "GU_TRIANGLE_FAN", "GU_SPRITES" };

int frontFace1 = GU_CW + 1;
int frontFace2 = GU_CW + 1;
char *faceNames[] = { "Unchanged", "GU_CW", "GU_CCW" };
int patchPrim = 0;
char *patchPrimNames[] = { "GU_TRIANGLE_STRIP", "GU_LINE_STRIP", "GU_POINTS" };

int psm = GU_PSM_8888;
int fbw = BUF_WIDTH;
int zbw = BUF_WIDTH;
int fbpOffset = 0;
int zbpOffset = 0;

u16 zTestPixelDepth;
u32 geTestPixelValue;

void addColorAttribute(char *label, struct Color *pcolor, int x, int y, int hasAlpha, int step)
{
	addAttribute(label, &pcolor->r, NULL, x +  0, y, 0, 0xFF, step, "%02X");
	x += strlen(label);
	addAttribute(", G", &pcolor->g, NULL, x +  5, y, 0, 0xFF, step, "%02X");
	addAttribute(", B", &pcolor->b, NULL, x + 13, y, 0, 0xFF, step, "%02X");
	if (hasAlpha)
	{
		addAttribute(", A", &pcolor->a, NULL, x + 21, y, 0, 0xFF, step, "%02X");
	}
}


void addPositionAttribute(char *label, ScePspFVector3 *pposition, int x, int y)
{
	addAttribute(label, NULL, &pposition->x,  x, y, -10, 10, 0.1, NULL);
	x += strlen(label);
	addAttribute(", Y", NULL, &pposition->y, x + 6, y, -10, 10, 0.1, NULL);
	addAttribute(", Z", NULL, &pposition->z, x + 15, y, -10, 10, 0.1, NULL);
}


int addLightAttribute(int index, struct Light *plight, int x, int y)
{
	char *label = malloc(100);
	sprintf(label, "Light %d", index + 1);

	addAttribute(label, &plight->type, NULL, x, y, 0, 2, 1, NULL);
	setAttributeValueNames(&lightTypeNames[0]);
	addAttribute(", Kind", &plight->kind, NULL, x + 20, y, 0, 3, 1, NULL);
	setAttributeValueNames(&lightKindNames[0]);
	y++;
	addPositionAttribute("    Position  X", &plight->position, x, y);
	y++;
	addPositionAttribute("    Direction X", &plight->direction, x, y);
	y++;
	addColorAttribute("    Ambient Color  R", &plight->ambientColor, x, y, 0, 0x10);
	y++;
	addColorAttribute("    Diffuse Color  R", &plight->diffuseColor, x, y, 0, 0x10);
	y++;
	addColorAttribute("    Specular Color R", &plight->specularColor, x, y, 0, 0x10);
	y++;
	addAttribute("    Attenuation Cst", NULL, &plight->constantAttenuation, x, y, 0, 1000, 1, NULL);
	addAttribute(", Lin", NULL, &plight->linearAttenuation, x + 24, y, 0, 1000, 1, NULL);
	addAttribute(", Quad", NULL, &plight->quadraticAttenuation, x + 24 + 10, y, 0, 1000, 1, NULL);
	y++;

	addAttribute("    Spot Exponent", NULL, &plight->spotExponent, x, y, 0, 128, 1, NULL);
	addAttribute(", Cutoff", NULL, &plight->spotCutoff, x + 22, y, -1, 10, 0.1, NULL);
	y++;

	return y;
}


void drawStates(int stateValues[])
{
	int i;

	for (i = 0; i < sizeof(states) / sizeof(int); i++)
	{
		switch (stateValues[i])
		{
			case 0: sceGuDisable(states[i]); break;
			case 1: sceGuEnable(states[i]);  break;
			case 2: /* Unchanged */		  break;
		}
	}
}


unsigned int getColor(struct Color *pcolor)
{
	unsigned int a = pcolor->a & 0xFF;
	unsigned int r = pcolor->r & 0xFF;
	unsigned int g = pcolor->g & 0xFF;
	unsigned int b = pcolor->b & 0xFF;

	return (a << 24) | (b << 16) | (g << 8) | (r << 0);
}


void setVertexColor(struct Color *pcolor, struct Vertex *pvertex)
{
	pvertex->color = getColor(pcolor);
}


void setVerticesColor(struct Color *pcolor, struct Vertex pvertices[], int sizeofVertices)
{
	int i;
	for (i = 0; i < sizeofVertices / sizeof(struct Vertex); i++)
	{
		setVertexColor(pcolor, &pvertices[i]);
	}
}


void setVertexPoint(struct Point *ppoint, struct Point *pnormal, struct Vertex *pvertex, float x, float y, float z, int u, int v)
{
	pvertex->u = u;
	pvertex->v = v;
	pvertex->nx = pnormal->x;
	pvertex->ny = pnormal->y;
	pvertex->nz = pnormal->z;
	pvertex->x = x + ppoint->x;
	pvertex->y = y + ppoint->y;
	pvertex->z = z + ppoint->z;
}


void setVertexNoColorPoint(struct Point *ppoint, struct Point *pnormal, struct VertexNoColor *pvertex, float x, float y, float z, int u, int v)
{
	pvertex->u = u;
	pvertex->v = v;
	pvertex->nx = pnormal->x;
	pvertex->ny = pnormal->y;
	pvertex->nz = pnormal->z;
	pvertex->x = x + ppoint->x;
	pvertex->y = y + ppoint->y;
	pvertex->z = z + ppoint->z;
}


void setRectanglePoint(struct Point *ppoint, struct Point *pnormal, struct Vertex pvertices[], float width, float height, int textureWidth, int textureHeight)
{
	width  /= 2;
	height /= 2;
	setVertexPoint(ppoint, pnormal, &pvertices[0], -width,  height, 0, 0,	            0);
	setVertexPoint(ppoint, pnormal, &pvertices[1], -width, -height, 0, 0,                textureHeight);
	setVertexPoint(ppoint, pnormal, &pvertices[2],      0,  height, 0, textureWidth / 2, 0);
	setVertexPoint(ppoint, pnormal, &pvertices[3],      0, -height, 0, textureWidth / 2, textureHeight);
	setVertexPoint(ppoint, pnormal, &pvertices[4],  width,  height, 0, textureWidth,     0);
	setVertexPoint(ppoint, pnormal, &pvertices[5],  width, -height, 0, textureWidth,     textureHeight);
}


void setNoColorRectanglePoint(struct Point *ppoint, struct Point *pnormal, struct VertexNoColor pvertices[], float width, float height, int textureWidth, int textureHeight)
{
	width  /= 2;
	height /= 2;
	setVertexNoColorPoint(ppoint, pnormal, &pvertices[0], -width,  height, 0, 0,	            0);
	setVertexNoColorPoint(ppoint, pnormal, &pvertices[1], -width, -height, 0, 0,                textureHeight);
	setVertexNoColorPoint(ppoint, pnormal, &pvertices[2],      0,  height, 0, textureWidth / 2, 0);
	setVertexNoColorPoint(ppoint, pnormal, &pvertices[3],      0, -height, 0, textureWidth / 2, textureHeight);
	setVertexNoColorPoint(ppoint, pnormal, &pvertices[4],  width,  height, 0, textureWidth,     0);
	setVertexNoColorPoint(ppoint, pnormal, &pvertices[5],  width, -height, 0, textureWidth,     textureHeight);
}


unsigned int getTextureColor(struct Color *pcolor, int textureType, int x, int y)
{
	unsigned int color1 = getColor(pcolor);
	unsigned int color2 = 0xFF000000;
	unsigned int color = color1;
	int factor = -1;
	int a, b;

	switch (textureType)
	{
		case 0: color = (((x + y / 1 * 1) / 1) & 1) == 0 ? color1 : color2; break;	/* Checkboard 1x1 */
		case 1: color = (((x + y / 3 * 3) / 3) & 1) == 0 ? color1 : color2; break;	/* Checkboard 3x3 */
		case 2: color = color1; break;									/* Unicolor */
		case 3: factor = x * 256 / TEXTURE_WIDTH; break;				/* Vertical */
		case 4: factor = y * 256 / TEXTURE_HEIGHT; break;				/* Horizontal */
		case 5: a = x - (TEXTURE_WIDTH  / 2);
			b = y - (TEXTURE_HEIGHT / 2);
			int a2 = TEXTURE_WIDTH * TEXTURE_WIDTH / 2;
			factor = (256 * (a2 - ((a * a) + (b * b)))) / a2; break;	/* Center */
	}

	if (factor != -1)
	{
		color = ((color1 * factor) + (color2 * (256 - factor))) / 256;
	}

	return color;
}


void createTexture32(struct Color *pcolor, int textureType, unsigned int *texture, int width, int height)
{
	int x, y;

	for (y = 0; y < height; y++)
	{
		for (x = 0; x < width; x++)
		{
			int color = getTextureColor(pcolor, textureType, x, y);
			texture[y * width + x] = color;
		}
	}
}


void createTexture16(struct Color *pcolor, int textureType, unsigned short *texture, int width, int height, int tpsm)
{
	int x, y;

	for (y = 0; y < height; y++)
	{
		for (x = 0; x < width; x++)
		{
			int color = getTextureColor(pcolor, textureType, x, y);
			switch (tpsm)
			{
				case GU_PSM_5650:
					color = ((color >> 3) & 0x0000001F) |
					        ((color >> 5) & 0x000007E0) |
					        ((color >> 8) & 0x0000F800);
					break;
				case GU_PSM_5551:
					color = ((color >>  3) & 0x0000001F) |
					        ((color >>  6) & 0x000003E0) |
					        ((color >>  9) & 0x00007C00) |
					        ((color >> 16) & 0x00008000);
					break;
				case GU_PSM_4444:
					color = ((color >>  4) & 0x0000000F) |
					        ((color >>  8) & 0x000000F0) |
					        ((color >> 12) & 0x00000F00) |
					        ((color >> 16) & 0x0000F000);
					break;
			}
			texture[y * width + x] = (unsigned short) color;
		}
	}
}


int getClutIndex(unsigned int *clut, int color)
{
	int i;

	for (i = 0; i < NUMBER_CLUT_ENTRIES; i++)
	{
		if ((clut[i] & 0x00FFFFFF) == (color & 0x00FFFFFF))
		{
			return i;
		}
	}

	return 0;
}


void createIndexedTexture(struct Color *pcolor, int textureType, void *texture, int width, int height, unsigned int *clut, int level, int tpsm)
{
	int x, y;

	for (y = 0; y < height; y++)
	{
		for (x = 0; x < width; x++)
		{
			int color = getTextureColor(pcolor, textureType, x, y);
			int index = getClutIndex(clut + NUMBER_CLUT_ENTRIES * level, color);
			if (tpsm == GU_PSM_T4)
			{
				int offset = (y * width + x) / 2;
				unsigned char *ptexture = (unsigned char *) texture;
				if (x & 1)
				{
					ptexture[offset] = (ptexture[offset] & 0x0F) | (index << 4);
				}
				else
				{
					ptexture[offset] = (ptexture[offset] & 0xF0) | (index << 0);
				}
			}
			else if (tpsm == GU_PSM_T8)
			{
				((unsigned char *) texture)[y * width + x] = index;
			}
			else if (tpsm == GU_PSM_T16)
			{
				((unsigned short *) texture)[y * width + x] = index;
			}
			else if (tpsm == GU_PSM_T32)
			{
				((unsigned int *) texture)[y * width + x] = index;
			}
		}
	}
}


void drawRectangles()
{
	int level;
	int width;
	int height;
	int numberMipmaps = 0;
	int i;

	int useClut1 = tpsm1 >= GU_PSM_T4 && tpsm1 <= GU_PSM_T32;
	int useClut2 = tpsm2 >= GU_PSM_T4 && tpsm2 <= GU_PSM_T32;

	if (useClut1 || useClut2)
	{
		for (i = 0; i < NUMBER_CLUT_ENTRIES; i++)
		{
			struct Color color;
			color.r = (rectangle1TextureColor.r * i / (NUMBER_CLUT_ENTRIES - 1)) & 0xFF;
			color.g = (rectangle1TextureColor.g * i / (NUMBER_CLUT_ENTRIES - 1)) & 0xFF;
			color.b = (rectangle1TextureColor.b * i / (NUMBER_CLUT_ENTRIES - 1)) & 0xFF;
			color.a = (rectangle1TextureColor.a * i / (NUMBER_CLUT_ENTRIES - 1)) & 0xFF;
			clut1[i] = getColor(&color);

			color.r = (rectangle2TextureColor.r * i / (NUMBER_CLUT_ENTRIES - 1)) & 0xFF;
			color.g = (rectangle2TextureColor.g * i / (NUMBER_CLUT_ENTRIES - 1)) & 0xFF;
			color.b = (rectangle2TextureColor.b * i / (NUMBER_CLUT_ENTRIES - 1)) & 0xFF;
			color.a = (rectangle2TextureColor.a * i / (NUMBER_CLUT_ENTRIES - 1)) & 0xFF;
			clut2[i] = getColor(&color);

			for (level = 1; level < NUMBER_MIPMAPS; level++)
			{
				color.r = (mipmapLevelsColor[level - 1].r * i / (NUMBER_CLUT_ENTRIES - 1)) & 0xFF;
				color.g = (mipmapLevelsColor[level - 1].g * i / (NUMBER_CLUT_ENTRIES - 1)) & 0xFF;
				color.b = (mipmapLevelsColor[level - 1].b * i / (NUMBER_CLUT_ENTRIES - 1)) & 0xFF;
				color.a = (mipmapLevelsColor[level - 1].a * i / (NUMBER_CLUT_ENTRIES - 1)) & 0xFF;
				clut1[i + level * NUMBER_CLUT_ENTRIES] = getColor(&color);
				clut2[i + level * NUMBER_CLUT_ENTRIES] = getColor(&color);
			}
		}
	}

	for (level = 0, width = TEXTURE_WIDTH, height = TEXTURE_HEIGHT; level < NUMBER_MIPMAPS && width > 0 && height > 0; level++, width /= 2, height /= 2)
	{
		struct Color *pcolor;
		pcolor = (level <= 0 ? &rectangle1TextureColor : &mipmapLevelsColor[level - 1]);
		if (tpsm1 >= GU_PSM_T4 && tpsm1 <= GU_PSM_T32)
		{
			createIndexedTexture(pcolor, textureType1, texture1[level], width, height, clut1, level, tpsm1);
		}
		else if (tpsm1 == GU_PSM_5650 || tpsm1 == GU_PSM_5551 || tpsm1 == GU_PSM_4444)
		{
			createTexture16(pcolor, textureType1, (unsigned short *) texture1[level], width, height, tpsm1);
		}
		else
		{
			createTexture32(pcolor, textureType1, texture1[level], width, height);
		}

		pcolor = (level <= 0 ? &rectangle2TextureColor : &mipmapLevelsColor[level - 1]);
		if (tpsm2 >= GU_PSM_T4 && tpsm2 <= GU_PSM_T32)
		{
			createIndexedTexture(pcolor, textureType2, texture2[level], width, height, clut2, level, tpsm2);
		}
		else if (tpsm2 == GU_PSM_5650 || tpsm2 == GU_PSM_5551 || tpsm2 == GU_PSM_4444)
		{
			createTexture16(pcolor, textureType2, (unsigned short *) texture2[level], width, height, tpsm2);
		}
		else
		{
			createTexture32(pcolor, textureType2, texture2[level], width, height);
		}

		numberMipmaps = level;
	}

	if (vertexColorFlag)
	{
		setVerticesColor(&rectangle1VertexColor, vertices1, sizeof(vertices1));
		setVerticesColor(&rectangle2VertexColor, vertices2, sizeof(vertices2));
	}

//	sceGuDispBuffer(displayWidth, displayHeight, fbp1 + fbpOffset, fbw);
	sceGuDrawBuffer(psm, fbp0 + fbpOffset, fbw);
	sceGuDepthBuffer(zbp + zbpOffset, zbw);

	int clearFlags = 0;
	if (clearFlagColor   != 0) clearFlags |= GU_COLOR_BUFFER_BIT;
	if (clearFlagDepth   != 0) clearFlags |= GU_DEPTH_BUFFER_BIT;
	if (clearFlagStencil != 0) clearFlags |= GU_STENCIL_BUFFER_BIT;

	sceGuPixelMask(getColor(&pixelMask));

	if (clearMode == 0)
	{
		sceGuDisable(GU_TEXTURE_2D);
		sceGuClearColor(getColor(&backgroundColor));
		sceGuClearDepth(clearDepth);
		sceGuClearStencil(clearStencil);
		sceGuClear(clearFlags);
	}
	else
	{
		sendCommandi(211, (clearFlags << 8) | 0x01);
	}

	sceGumMatrixMode(GU_PROJECTION);
	sceGumLoadIdentity();
	sceGumPerspective(75.0f,16.0f/9.0f,0.9f,1000.0f);

	sceGumMatrixMode(GU_VIEW);
	sceGumLoadIdentity();

	sceGuDepthMask(depthMask);
	sceGuDepthFunc(depthFunc);
	sceGuAlphaFunc(alphaFunc, alphaReference, 0xFF);
	sceGuBlendFunc(blendOp, blendFuncSrc, blendFuncDst, getColor(&blendSFix), getColor(&blendDFix));
	sendCommandf(68, (float) zscale);
	sendCommandf(71, (float) zpos);
	sendCommandi(214, nearZ);
	sendCommandi(215, farZ);
	//sceGuDepthRange(nearZ, farZ);
	//sceGuDepthOffset(depthOffset);
	sceGuOffset(offsetX, offsetY);
	sceGuViewport(viewportX, viewportY, viewportWidth, viewportHeight);
	sendCommandi(21, (regionY << 10) | regionX); // REGION1 command
	sendCommandi(22, (((regionY + regionHeight - 1) << 10) | (regionX + regionWidth - 1))); // REGION2 command
	sceGuScissor(scissorX, scissorY, scissorWidth, scissorHeight);
	int materialFlags = 0;
	if (materialAmbientFlag ) materialFlags |= GU_AMBIENT;
	if (materialDiffuseFlag ) materialFlags |= GU_DIFFUSE;
	if (materialSpecularFlag) materialFlags |= GU_SPECULAR;
	sceGuColorMaterial(materialFlags);
	sceGuMaterial(GU_AMBIENT , getColor(&materialAmbient ));
	sendCommandi(88, (getColor(&materialAmbient) >> 24) & 0xFF);
	sceGuMaterial(GU_DIFFUSE , getColor(&materialDiffuse ));
	sceGuMaterial(GU_SPECULAR, getColor(&materialSpecular));
	if (materialEmissiveFlag)
	{
		sendCommandi(84, getColor(&materialEmissive));
	}
	sceGuStencilOp(stencilOpFail, stencilOpZFail, stencilOpZPass);
	sceGuStencilFunc(stencilFunc, stencilReference, stencilMask);
	sendCommandi(55, patchPrim);	// sceGuPatchPrim
	sceGuFog(fogNear, fogFar, getColor(&fogColor));

	for (i = 0; i < NUM_LIGHTS; i++)
	{
		struct Light *plight = &lights[i];
		int components = 0;
		if (plight->kind == 0)
		{
			components = GU_AMBIENT_AND_DIFFUSE;
		}
		else if (plight->kind == 1)
		{
			components = GU_DIFFUSE_AND_SPECULAR;
		}
		else if (plight-> kind == 2)
		{
			components = GU_UNKNOWN_LIGHT_COMPONENT;
		}
		sceGuLight(i, plight->type, components, &plight->position);
		sceGuLightAtt(i, plight->constantAttenuation, plight->linearAttenuation, plight->quadraticAttenuation);
		sceGuLightColor(i, GU_AMBIENT, getColor(&plight->ambientColor));
		sceGuLightColor(i, GU_DIFFUSE, getColor(&plight->diffuseColor));
		sceGuLightColor(i, GU_SPECULAR, getColor(&plight->specularColor));
		sceGuLightSpot(i, &plight->direction, plight->spotExponent, plight->spotCutoff);
	}
	sceGuAmbient(getColor(&ambientColor));
	sceGuTexEnvColor(getColor(&texEnvColor));

	drawStates(stateValues1);
	if (frontFace1 != 0)	// 0 means unchanged
	{
		sceGuFrontFace(frontFace1 - 1);
	}
	sceKernelDcacheWritebackAll();
	if (useClut1)
	{
		sceGuClutMode(GU_PSM_8888, 0, 0xFF, 0);
		sceGuClutLoad(NUMBER_CLUT_ENTRIES / 8 * (texture1_a2 ? 16 : 1), clut1);
	}
	sceGuTexMode(tpsm1, numberMipmaps, texture1_a2, 0);
	sceGuTexLevelMode(texLevelMode1, texBias1 / 16.0);
	sceGuTexSlope(texSlope1);
	for (level = 0, width = TEXTURE_WIDTH, height = TEXTURE_HEIGHT; level <= numberMipmaps; level++, width /= 2, height /= 2)
	{
		sceGuTexImage(level, width, height, width, texture1[level]); 
	}
	if (texFuncDouble1)
	{
		sceGuEnable(GU_FRAGMENT_2X);
	}
	else
	{
		sceGuDisable(GU_FRAGMENT_2X);
	}
	sceGuTexFunc(texFunc1, texFuncAlpha1 == 2 ? 0x81 : texFuncAlpha1);
	sceGuTexFilter(texMinFilter1, texMagFilter1);
	sceGuTexWrap(GU_CLAMP, GU_CLAMP);
	sceGuTexScale(1.0 / textureScale, 1.0 / textureScale);
	sceGuTexOffset(0, 0);

	sceGumMatrixMode(GU_VIEW);
	sceGumLoadIdentity();
	sceGumTranslate(&rectangle1translation);

	sceGumMatrixMode(GU_MODEL);
	sceGumLoadIdentity();
	sceGumRotateXYZ(&rectangle1rotation);

	int numberVertex1 = sizeof(vertices1) / sizeof(struct Vertex);
	if (rectangle1rendering == RENDERING_3D)
	{
		rectangle1point.x = 0;
		rectangle1point.y = 0;
		rectangle1point.z = rectangle1PrimType != GU_SPRITES ? 0 : (int) (  0 + rectangle1translation.z * 10 + 0.5);
		int width = rectangle1PrimType != GU_SPRITES ? rectangle1width : rectangle1width * 10;
		int height = rectangle1PrimType != GU_SPRITES ? rectangle1height : rectangle1height * 10;
		if (vertexColorFlag)
		{
			setRectanglePoint(&rectangle1point, &rectangle1normal, vertices1, width, height, textureScale, textureScale);
			if (rectangle1PrimType == GU_SPRITES)
			{
				numberVertex1 /= 2;
				int i;
				for (i = 0; i < numberVertex1; i += 2)
				{
					vertices1[i] = vertices1[2 * i];
					vertices1[i+1] = vertices1[2 * i + 3];
				}
			}
		}
		else
		{
			setNoColorRectanglePoint(&rectangle1point, &rectangle1normal, (struct VertexNoColor *) vertices1, width, height, textureScale, textureScale);
			if (rectangle1PrimType == GU_SPRITES)
			{
				numberVertex1 /= 2;
				int i;
				struct VertexNoColor *vertices = (struct VertexNoColor *) vertices1;
				for (i = 0; i < numberVertex1; i += 2)
				{
					vertices[i] = vertices[2 * i];
					vertices[i+1] = vertices[2 * i + 3];
				}
			}
		}
	}
	else if (rectangle1rendering == RENDERING_2D)
	{
		rectangle1point.x = (int) (350 + rectangle1translation.x * 10 + 0.5);
		rectangle1point.y = (int) (100 + rectangle1translation.y * 10 + 0.5);
		rectangle1point.z = (int) (  0 + rectangle1translation.z * 10 + 0.5);
		if (vertexColorFlag)
		{
			setRectanglePoint(&rectangle1point, &rectangle1normal, vertices1, rectangle1width * 50, rectangle1height * 50, TEXTURE_WIDTH, TEXTURE_HEIGHT);
			if (rectangle1PrimType == GU_SPRITES)
			{
				numberVertex1 /= 2;
				int i;
				for (i = 0; i < numberVertex1; i += 2)
				{
					vertices1[i] = vertices1[2 * i];
					vertices1[i+1] = vertices1[2 * i + 3];
				}
			}
		}
		else
		{
			setNoColorRectanglePoint(&rectangle1point, &rectangle1normal, (struct VertexNoColor *) vertices1, rectangle1width * 50, rectangle1height * 50, TEXTURE_WIDTH, TEXTURE_HEIGHT);
		}
	}

	int vertexFlags = GU_NORMAL_32BITF;
#ifdef USE_TEXTURE_8BIT
	vertexFlags |= GU_TEXTURE_8BIT;
#endif
#ifdef USE_TEXTURE_16BIT
	vertexFlags |= GU_TEXTURE_16BIT;
#endif
#ifdef USE_TEXTURE_32BITF
	vertexFlags |= GU_TEXTURE_32BITF;
#endif

#ifdef USE_VERTEX_8BIT
	vertexFlags |= GU_VERTEX_8BIT;
#endif
#ifdef USE_VERTEX_16BIT
	vertexFlags |= GU_VERTEX_16BIT;
#endif
#ifdef USE_VERTEX_32BITF
	vertexFlags |= GU_VERTEX_32BITF;
#endif
	if (vertexColorFlag) vertexFlags |= GU_COLOR_8888;

	if (rectangle1rendering != RENDERING_OFF)
	{
		vertexFlags |= (rectangle1rendering == RENDERING_3D) ? GU_TRANSFORM_3D : GU_TRANSFORM_2D;
		sceGumDrawArray(rectangle1PrimType, vertexFlags, numberVertex1, 0, vertices1);
	}

	drawStates(stateValues2);
	if (frontFace2 != 0)	// 0 means unchanged
	{
		sceGuFrontFace(frontFace2 - 1);
	}
	if (useClut2)
	{
		sceGuClutMode(GU_PSM_8888, 0, 0xFF, 0);
		sceGuClutLoad(NUMBER_CLUT_ENTRIES / 8 * (texture2_a2 ? 16 : 1), clut2);
	}
	sceGuTexMode(tpsm2, numberMipmaps, texture2_a2, 0);
	sceGuTexLevelMode(texLevelMode2, texBias2 / 16.0);
	sceGuTexSlope(texSlope2);
	for (level = 0, width = TEXTURE_WIDTH, height = TEXTURE_HEIGHT; level <= numberMipmaps; level++, width /= 2, height /= 2)
	{
		sceGuTexImage(level, width, height, width, texture2[level]); 
	}
	if (texFuncDouble2)
	{
		sceGuEnable(GU_FRAGMENT_2X);
	}
	else
	{
		sceGuDisable(GU_FRAGMENT_2X);
	}
	sceGuTexFunc(texFunc2, texFuncAlpha2 == 2 ? 0x81 : texFuncAlpha2);
	sceGuTexFilter(texMinFilter2, texMagFilter2);
	sceGuTexWrap(GU_CLAMP, GU_CLAMP);
	sceGuTexScale(1.0 / textureScale, 1.0 / textureScale);
	sceGuTexOffset(0, 0);

	sceGumMatrixMode(GU_VIEW);
	sceGumLoadIdentity();
	sceGumTranslate(&rectangle2translation);

	sceGumMatrixMode(GU_MODEL);
	sceGumLoadIdentity();
	sceGumRotateXYZ(&rectangle2rotation);

	int numberVertex2 = sizeof(vertices2) / sizeof(struct Vertex);
	if (rectangle2rendering == RENDERING_3D)
	{
		rectangle2point.x = 0;
		rectangle2point.y = 0;
		rectangle2point.z = rectangle2PrimType != GU_SPRITES ? 0 : (int) (  0 + rectangle2translation.z * 10 + 0.5);
		int width = rectangle2PrimType != GU_SPRITES ? rectangle2width : rectangle2width * 10;
		int height = rectangle2PrimType != GU_SPRITES ? rectangle2height : rectangle2height * 10;
		if (vertexColorFlag)
		{
			setRectanglePoint(&rectangle2point, &rectangle2normal, vertices2, width, height, textureScale, textureScale);
			if (rectangle2PrimType == GU_SPRITES)
			{
				numberVertex2 /= 2;
				int i;
				for (i = 0; i < numberVertex2; i += 2)
				{
					vertices2[i] = vertices2[2 * i];
					vertices2[i+1] = vertices2[2 * i + 3];
				}
			}
		}
		else
		{
			setNoColorRectanglePoint(&rectangle2point, &rectangle2normal, (struct VertexNoColor *) vertices2, width, height, textureScale, textureScale);
			if (rectangle2PrimType == GU_SPRITES)
			{
				numberVertex2 /= 2;
				int i;
				struct VertexNoColor *vertices = (struct VertexNoColor *) vertices2;
				for (i = 0; i < numberVertex2; i += 2)
				{
					vertices[i] = vertices[2 * i];
					vertices[i+1] = vertices[2 * i + 3];
				}
			}
		}
	}
	else if (rectangle2rendering == RENDERING_2D)
	{
		rectangle2point.x = (int) (400 + rectangle2translation.x * 10 + 0.5);
		rectangle2point.y = (int) (150 + rectangle2translation.y * 10 + 0.5);
		rectangle2point.z = (int) (  0 + rectangle2translation.z * 10 + 0.5);
		if (vertexColorFlag)
		{
			setRectanglePoint(&rectangle2point, &rectangle2normal, vertices2, rectangle2width * 50, rectangle2height * 50, TEXTURE_WIDTH, TEXTURE_HEIGHT);
		}
		else
		{
			setNoColorRectanglePoint(&rectangle2point, &rectangle2normal, (struct VertexNoColor *) vertices2, rectangle2width * 50, rectangle2height * 50, TEXTURE_WIDTH, TEXTURE_HEIGHT);
		}
	}

	if (rectangle2rendering != RENDERING_OFF)
	{
		vertexFlags &= ~GU_TRANSFORM_BITS;
		vertexFlags |= (rectangle2rendering == RENDERING_3D) ? GU_TRANSFORM_3D : GU_TRANSFORM_2D;

		sceGumDrawArray(rectangle2PrimType, vertexFlags, numberVertex2, 0, vertices2);
	}

	if (clearMode != 0)
	{
		sendCommandi(211,0);
	}
}


void drawDebugPrintBuffer()
{
	sceKernelDcacheWritebackAll();

	sceGuDisable(GU_ALPHA_TEST);
	sceGuDisable(GU_DEPTH_TEST);
	sceGuDisable(GU_SCISSOR_TEST);
	sceGuDisable(GU_STENCIL_TEST);
	sceGuDisable(GU_BLEND);
	sceGuDisable(GU_CULL_FACE);
	sceGuDisable(GU_DITHER);
	sceGuDisable(GU_FOG);
	sceGuDisable(GU_CLIP_PLANES);
	sceGuDisable(GU_LIGHTING);
	sceGuDisable(GU_LINE_SMOOTH);
	sceGuDisable(GU_PATCH_CULL_FACE);
	sceGuDisable(GU_COLOR_TEST);
	sceGuDisable(GU_COLOR_LOGIC_OP);
	sceGuDisable(GU_FACE_NORMAL_REVERSE);
	sceGuDisable(GU_PATCH_FACE);
	sceGuDisable(GU_FRAGMENT_2X);

	sceGuEnable(GU_TEXTURE_2D);
	sceGuTexFunc(GU_TFX_REPLACE, GU_TCC_RGBA);
	sceGuEnable(GU_ALPHA_TEST);
	sceGuAlphaFunc(GU_GREATER, 0x00, 0xFF);
	sceGuDepthMask(0);
	sceGuPixelMask(0x00000000);
	sceGuTexMode(GU_PSM_8888, 0, 0, 0);
	sceGuTexImage(0, SCR_TEXTURE_WIDTH, SCR_TEXTURE_HEIGHT, BUF_WIDTH, debugPrintBuffer);
	debugPrintVertices[0].u = 0;
	debugPrintVertices[0].v = 0;
	debugPrintVertices[0].x = 0;
	debugPrintVertices[0].y = 0;
	debugPrintVertices[0].z = 0;
	debugPrintVertices[1].u = SCR_WIDTH;
	debugPrintVertices[1].v = SCR_HEIGHT;
	debugPrintVertices[1].x = SCR_WIDTH;
	debugPrintVertices[1].y = SCR_HEIGHT;
	debugPrintVertices[1].z = 0;
	sceGuDrawArray(GU_SPRITES, GU_TEXTURE_16BIT | GU_VERTEX_16BIT | GU_TRANSFORM_2D, 2, NULL, debugPrintVertices);
}


void draw()
{
	sceDisplaySetMode(displayMode, displayWidth, displayHeight);
	sceGuStart(GU_DIRECT, list);

	drawRectangles();

	drawAttributes();

	u16 *zTestPixelAddress = (u16 *) (zbp + 0x44000000 + (zTestPixelY * BUF_WIDTH + zTestPixelX) * 2);
	pspDebugScreenSetXY(35, 0);
	pspDebugScreenPrintf("Depth (%d,%d)=0x%04X", zTestPixelX, zTestPixelY, zTestPixelDepth);

	u32 *geTestPixelAddress = (u32 *) (fbp0 + 0x44000000 + (geTestPixelY * BUF_WIDTH + geTestPixelX) * 4);
	pspDebugScreenSetXY(35, 1);
	pspDebugScreenPrintf("GE (%d,%d)=0x%08X", geTestPixelX, geTestPixelY, geTestPixelValue);

	drawDebugPrintBuffer();

	sceGuFinish();
	sceGuSync(0, 0);

	zTestPixelDepth = *zTestPixelAddress;
	geTestPixelValue = *geTestPixelAddress;

	sceDisplayWaitVblank();
	fbp0 = sceGuSwapBuffers();
}


void init()
{
	pspDebugScreenInit();

	fbp0 = getStaticVramBuffer((BUF_WIDTH + 64), SCR_HEIGHT, GU_PSM_8888);
	fbp1 = getStaticVramBuffer((BUF_WIDTH + 64), SCR_HEIGHT, GU_PSM_8888);
	zbp  = getStaticVramBuffer((BUF_WIDTH + 64), SCR_HEIGHT, GU_PSM_4444);
 
	sceGuInit();
	sceGuStart(GU_DIRECT,list);
	sceGuDrawBuffer(GU_PSM_8888,fbp0,BUF_WIDTH);
	sceGuDispBuffer(SCR_WIDTH,SCR_HEIGHT,fbp1,BUF_WIDTH);
	sceGuDepthBuffer(zbp,BUF_WIDTH);
	sceGuOffset(2048 - (SCR_WIDTH/2),2048 - (SCR_HEIGHT/2));
	sceGuViewport(2048,2048,SCR_WIDTH,SCR_HEIGHT);
	sceGuDepthRange(65535,0);
	sceGuScissor(0,0,SCR_WIDTH,SCR_HEIGHT);
	sceGuEnable(GU_SCISSOR_TEST);
	sceGuFrontFace(GU_CW);
	sceGuShadeModel(GU_SMOOTH);
	sceGuDisable(GU_TEXTURE_2D);
	sceGuFinish();
	sceGuSync(0,0);
 
	sceDisplayWaitVblankStart();
	sceGuDisplay(1);

	sceCtrlSetSamplingCycle(0);
	sceCtrlSetSamplingMode(PSP_CTRL_MODE_ANALOG);

	memset(clut1, 0, sizeof(clut1));
	memset(clut2, 0, sizeof(clut2));

	int x = 0;
	int y = 0;

	addAttribute("Rendering      ", &rectangle1rendering, NULL, x, y, 0, 2, 1, NULL);
	setAttributeValueNames(&renderingNames[0]);
	addAttribute(NULL, &rectangle2rendering, NULL, x + 27, y, 0, 2, 1, NULL);
	setAttributeValueNames(&renderingNames[0]);
	y++;

	int i;
	for (i = 0; i < sizeof(states) / sizeof(int); i++)
	{
		stateValues1[i] = 0;
		stateValues2[i] = 0;
		addAttribute(stateNames[states[i]], &stateValues1[i], NULL, x, y, 0, 2, 1, NULL);
		setAttributeValueNames(&stateValueNames[0]);
		addAttribute(NULL, &stateValues2[i], NULL, x + 27, y, 0, 2, 1, NULL);
		setAttributeValueNames(&stateValueNames[0]);
		y++;
	}

	addAttribute("Front Face     ", &frontFace1, NULL, x, y, 0, 2, 1, NULL);
	setAttributeValueNames(&faceNames[0]);
	addAttribute(NULL, &frontFace2, NULL, x + 27, y, 0, 2, 1, NULL);
	setAttributeValueNames(&faceNames[0]);
	y++;

	addAttribute("Patch Primitive", &patchPrim, NULL, x, y, 0, 2, 1, NULL);
	setAttributeValueNames(&patchPrimNames[0]);
	y++;

	addAttribute("sceGuDepthMask", &depthMask, NULL, x, y, 0, 1, 1, NULL);
	setAttributeValueNames(&depthMaskNames[0]);
	y++;

	pixelMask.r = 0x00;
	pixelMask.g = 0x00;
	pixelMask.b = 0x00;
	pixelMask.a = 0x00;

	addColorAttribute("Pixel Mask R", &pixelMask, x, y, 1, 1);
	y++;

	addAttribute("sceGuDepthFunc", &depthFunc, NULL, x, y, 0, 7, 1, NULL);
	setAttributeValueNames(&testFuncNames[0]);
	y++;

	addAttribute("NearZ", &nearZ, NULL, x, y, 0, 0xFFFF, 1000, NULL);
	addAttribute(", FarZ", &farZ, NULL, x + 12, y, 0, 0xFFFF, 1000, NULL);
	y++;

	addAttribute("Z Scale", &zscale, NULL, x, y, 0, 0xFFFF, 1000, NULL);
	addAttribute(", Pos", &zpos, NULL, x + 14, y, 0, 0xFFFF, 1000, NULL);
	y++;

	addAttribute("Viewport X", &viewportX, NULL, x, y, 0, 4095, 10, NULL);
	addAttribute(", Y", &viewportY, NULL, x + 16, y, 0, 4095, 10, NULL);
	y++;
	addAttribute("         Width", &viewportWidth, NULL, x, y, -4096, 4095, 10, NULL);
	addAttribute(", Height", &viewportHeight, NULL, x + 19, y, -4096, 4095, 10, NULL);
	y++;
	addAttribute("Offset X", &offsetX, NULL, x, y, 0, 4096, 1, NULL);
	addAttribute(", Y", &offsetY, NULL, x + 14, y, 0, 4096, 1, NULL);
	y++;
	addAttribute("Region   X", &regionX, NULL, x, y, 0, 4095, 10, NULL);
	addAttribute(", Y", &regionY, NULL, x + 16, y, 0, 4095, 10, NULL);
	y++;
	addAttribute("         Width", &regionWidth, NULL, x, y, -4096, 4095, 10, NULL);
	addAttribute(", Height", &regionHeight, NULL, x + 19, y, -4096, 4095, 10, NULL);
	y++;
	addAttribute("Scissor  X", &scissorX, NULL, x, y, 0, 4095, 10, NULL);
	addAttribute(", Y", &scissorY, NULL, x + 16, y, 0, 4095, 10, NULL);
	y++;
	addAttribute("         Width", &scissorWidth, NULL, x, y, -4096, 4095, 10, NULL);
	addAttribute(", Height", &scissorHeight, NULL, x + 19, y, -4096, 4095, 10, NULL);
	y++;
	addAttribute("Display Width", &displayWidth, NULL, x, y, 0, 512, 1, NULL);
	addAttribute(", Height", &displayHeight, NULL, x + 18, y, 0, 512, 1, NULL);
	addAttribute(", Mode", &displayMode, NULL, x + 31, y, 0, 100, 1, NULL);	// Which values can take displayMode?
	y++;

	addAttribute("FrameBuffer Width", &fbw, NULL, x, y, 0, 1024, 1, NULL);
	addAttribute(", Offset", &fbpOffset, NULL, x + 22, y, -1024, 1024, 4, NULL);
	addAttribute(", Format", &psm, NULL, x + 35, y, GU_PSM_5650, GU_PSM_8888, 1, NULL);
	setAttributeValueNames(&tpsmNames[0]);
	y++;
	addAttribute("DepthBuffer Width", &zbw, NULL, x, y, 0, 1024, 1, NULL);
	addAttribute(", Offset", &zbpOffset, NULL, x + 22, y, -1024, 1024, 4, NULL);
	y++;

	addAttribute("sceGuAlphaFunc", &alphaFunc, NULL, x, y, 0, 7, 1, NULL);
	setAttributeValueNames(&testFuncNames[0]);
	addAttribute(", Reference", &alphaReference, NULL, x + 27, y, 0, 255, 0x10, "%02X");
	y++;

	addAttribute("sceGuBlendFunc op", &blendOp, NULL, x, y, 0, 5, 1, NULL);
	setAttributeValueNames(&blendOpNames[0]);
	y++;
	addAttribute("src", &blendFuncSrc, NULL, x + 15, y, 0, 10, 1, NULL);
	setAttributeValueNames(&blendFuncNames[0]);
	y++;
	addAttribute("dst", &blendFuncDst, NULL, x + 15, y, 0, 10, 1, NULL);
	setAttributeValueNames(&blendFuncNames[0]);
	y++;
	addColorAttribute("SFix R", &blendSFix, x + 15, y, 0, 0x10);
	y++;
	addColorAttribute("DFix R", &blendDFix, x + 15, y, 0, 0x10);
	y++;

	addAttribute("sceGuStencilOp fail", &stencilOpFail, NULL, x, y, 0, 5, 1, NULL);
	setAttributeValueNames(&stencilOpNames[0]);
	y++;
	addAttribute("               zfail", &stencilOpZFail, NULL, x, y, 0, 5, 1, NULL);
	setAttributeValueNames(&stencilOpNames[0]);
	y++;
	addAttribute("               zpass", &stencilOpZPass, NULL, x, y, 0, 5, 1, NULL);
	setAttributeValueNames(&stencilOpNames[0]);
	y++;

	addAttribute("sceGuStencilFunc", &stencilFunc, NULL, x, y, 0, 7, 1, NULL);
	setAttributeValueNames(&testFuncNames[0]);
	addAttribute(", Ref", &stencilReference, NULL, x + 27, y, 0, 0xFF, 0x10, "%02X");
	addAttribute(", Mask", &stencilMask, NULL, x + 38, y, 0, 0xFF, 0x10, "%02X");
	y++;

	rectangle1VertexColor.r = 0x00;
	rectangle1VertexColor.g = 0xFF;
	rectangle1VertexColor.b = 0x00;
	rectangle1VertexColor.a = 0xFF;
	rectangle1TextureColor.r = 0x00;
	rectangle1TextureColor.g = 0xFF;
	rectangle1TextureColor.b = 0x00;
	rectangle1TextureColor.a = 0xFF;
	rectangle1point.x = 0;
	rectangle1point.y = 0;
	rectangle1point.z = 0;
	rectangle1translation.x = 3;
	rectangle1translation.y = 0.5;
	rectangle1translation.z = -4;
	rectangle1rotation.x = 0;
	rectangle1rotation.y = 0;
	rectangle1rotation.z = 0;
	rectangle1normal.x = 0;
	rectangle1normal.y = 0;
	rectangle1normal.z = 1;

	addAttribute("Rect1 pos X", NULL, &rectangle1translation.x, x, y, -10, 10, 0.1, NULL);
	addAttribute(", Y", NULL, &rectangle1translation.y, x + 17, y, -10, 10, 0.1, NULL);
	addAttribute(", Z", NULL, &rectangle1translation.z, x + 26, y, -20, 10, 0.1, NULL);
	y++;
	addAttribute("width", NULL, &rectangle1width, x + 6, y, 0, 20, 0.1, NULL);
	addAttribute(", height", NULL, &rectangle1height, x + 17, y, 0, 20, 0.1, NULL);
	y++;
	addAttribute("rot X", NULL, &rectangle1rotation.x, x + 6, y, -GU_PI, GU_PI, 0.1, NULL);
	addAttribute(", Y", NULL, &rectangle1rotation.y, x + 17, y, -GU_PI, GU_PI, 0.1, NULL);
	addAttribute(", Z", NULL, &rectangle1rotation.z, x + 26, y, -GU_PI, GU_PI, 0.1, NULL);
	y++;
	addAttribute("normal X", NULL, &rectangle1normal.x, x + 6, y, -10, 10, 0.1, NULL);
	addAttribute(", Y", NULL, &rectangle1normal.y, x + 20, y, -10, 10, 0.1, NULL);
	addAttribute(", Z", NULL, &rectangle1normal.z, x + 29, y, -10, 10, 0.1, NULL);
	y++;
	addColorAttribute("Vertex R", &rectangle1VertexColor, x + 6, y, 1, 0x10);
	y++;
	addColorAttribute("Texture R", &rectangle1TextureColor, x + 6, y, 1, 0x10);
	y++;
	addAttribute("Type", &rectangle1PrimType, NULL, x + 6, y, 0, 6, 1, NULL);
	setAttributeValueNames(&primTypeNames[0]);
	y++;

	addAttribute("sceGuTexFunc", &texFunc1, NULL, x + 6, y, 0, 4, 1, NULL);
	setAttributeValueNames(&texFuncNames[0]);
	addAttribute(NULL, &texFuncAlpha1, NULL, x + 36, y, 0, 2, 1, NULL);
	setAttributeValueNames(&texFuncAlphaNames[0]);
	addAttribute(NULL, &texFuncDouble1, NULL, x + 42, y, 0, 1, 1, NULL);
	setAttributeValueNames(&texFuncDoubleNames[0]);
	y++;

	addAttribute("Texture Type", &textureType1, NULL, x + 6, y, 0, 5, 1, NULL);
	setAttributeValueNames(&textureTypeNames[0]);
	addAttribute(", a2", &texture1_a2, NULL, x + 34, y, 0, 1, 1, NULL);
	y++;

	addAttribute("Tex Level Mode", &texLevelMode1, NULL, x + 6, y, 0, 2, 1, NULL);
	setAttributeValueNames(&texModeNames[0]);
	addAttribute(", Bias", &texBias1, NULL, x + 38, y, -127, 128, 1, NULL);
	y++;

	addAttribute("Slope", NULL, &texSlope1, x + 16, y, 0, 10, 0.1, NULL);
	y++;

	addAttribute("TextureFilter Min", &texMinFilter1, NULL, x + 6, y, 0, 7, 1, NULL);
	setAttributeValueNames(&texFilterNames[0]);
	y++;
	addAttribute("Mag", &texMagFilter1, NULL, x + 20, y, 0, 7, 1, NULL);
	setAttributeValueNames(&texFilterNames[0]);
	y++;

	addAttribute("Texture Format", &tpsm1, NULL, x + 6, y, GU_PSM_5650, GU_PSM_T32, 1, NULL);
	setAttributeValueNames(&tpsmNames[0]);
	y++;

	rectangle2VertexColor.r = 0xFF;
	rectangle2VertexColor.g = 0x00;
	rectangle2VertexColor.b = 0x00;
	rectangle2VertexColor.a = 0xFF;
	rectangle2TextureColor.r = 0xFF;
	rectangle2TextureColor.g = 0x00;
	rectangle2TextureColor.b = 0x00;
	rectangle2TextureColor.a = 0xFF;
	rectangle2point.x = rectangle1point.x;
	rectangle2point.y = rectangle1point.y;
	rectangle2point.z = rectangle1point.z;
	rectangle2translation.x = rectangle1translation.x + 1;
	rectangle2translation.y = rectangle1translation.y - 1;
	rectangle2translation.z = rectangle1translation.z;
	rectangle2rotation.x = 0;
	rectangle2rotation.y = 0;
	rectangle2rotation.z = 0;
	rectangle2normal.x = 0;
	rectangle2normal.y = 0;
	rectangle2normal.z = 1;

	addAttribute("Rect2 pos X", NULL, &rectangle2translation.x,  x, y, -10, 10, 0.1, NULL);
	addAttribute(", Y", NULL, &rectangle2translation.y, x + 17, y, -10, 10, 0.1, NULL);
	addAttribute(", Z", NULL, &rectangle2translation.z, x + 26, y, -20, 10, 0.1, NULL);
	y++;
	addAttribute("width", NULL, &rectangle2width, x + 6, y, 0, 20, 0.1, NULL);
	addAttribute(", height", NULL, &rectangle2height, x + 17, y, 0, 20, 0.1, NULL);
	y++;
	addAttribute("rot X", NULL, &rectangle2rotation.x, x + 6, y, -GU_PI, GU_PI, 0.1, NULL);
	addAttribute(", Y", NULL, &rectangle2rotation.y,  x + 17, y, -GU_PI, GU_PI, 0.1, NULL);
	addAttribute(", Z", NULL, &rectangle2rotation.z,  x + 26, y, -GU_PI, GU_PI, 0.1, NULL);
	y++;
	addAttribute("normal X", NULL, &rectangle2normal.x, x + 6, y, -10, 10, 0.1, NULL);
	addAttribute(", Y", NULL, &rectangle2normal.y, x + 20, y, -10, 10, 0.1, NULL);
	addAttribute(", Z", NULL, &rectangle2normal.z, x + 29, y, -10, 10, 0.1, NULL);
	y++;
	addColorAttribute("Vertex R", &rectangle2VertexColor, x + 6, y, 1, 0x10);
	y++;
	addColorAttribute("Texture R", &rectangle2TextureColor, x + 6, y, 1, 0x10);
	y++;
	addAttribute("Type", &rectangle2PrimType, NULL, x + 6, y, 0, 6, 1, NULL);
	setAttributeValueNames(&primTypeNames[0]);
	y++;

	addAttribute("sceGuTexFunc", &texFunc2, NULL, x + 6, y, 0, 4, 1, NULL);
	setAttributeValueNames(&texFuncNames[0]);
	addAttribute(NULL, &texFuncAlpha2, NULL, x + 36, y, 0, 1, 1, NULL);
	setAttributeValueNames(&texFuncAlphaNames[0]);
	addAttribute(NULL, &texFuncDouble2, NULL, x + 42, y, 0, 1, 1, NULL);
	setAttributeValueNames(&texFuncDoubleNames[0]);
	y++;

	addAttribute("Texture Type", &textureType2, NULL, x + 6, y, 0, 5, 1, NULL);
	setAttributeValueNames(&textureTypeNames[0]);
	addAttribute(", a2", &texture2_a2, NULL, x + 34, y, 0, 1, 1, NULL);
	y++;

	addAttribute("Tex Level Mode", &texLevelMode2, NULL, x + 6, y, 0, 2, 1, NULL);
	setAttributeValueNames(&texModeNames[0]);
	addAttribute(", Bias", &texBias2, NULL, x + 38, y, -127, 128, 1, NULL);
	y++;

	addAttribute("Slope", NULL, &texSlope2, x + 16, y, 0, 10, 0.1, NULL);
	y++;

	addAttribute("TextureFilter Min", &texMinFilter2, NULL, x + 6, y, 0, 7, 1, NULL);
	setAttributeValueNames(&texFilterNames[0]);
	y++;
	addAttribute("Mag", &texMagFilter2, NULL, x + 20, y, 0, 7, 1, NULL);
	setAttributeValueNames(&texFilterNames[0]);
	y++;

	addAttribute("Texture Format", &tpsm2, NULL, x + 6, y, GU_PSM_5650, GU_PSM_T32, 1, NULL);
	setAttributeValueNames(&tpsmNames[0]);
	y++;

	addAttribute("Use Vertex Color", &vertexColorFlag, NULL, x, y, 0, 1, 1, NULL);
	setAttributeValueNames(&onOffNames[0]);
	y++;

	backgroundColor.r = 70;
	backgroundColor.g = 70;
	backgroundColor.b = 70;
	backgroundColor.a = 0xFF;

	addColorAttribute("Background R", &backgroundColor, x, y, 1, 0x10);
	y++;

	addAttribute("Clear Mode", &clearMode, NULL, x, y, 0, 1, 1, NULL);
	setAttributeValueNames(&clearModeNames[0]);
	y++;

	addAttribute("Clear Color", &clearFlagColor, NULL, x, y, 0, 1, 1, NULL);
	setAttributeValueNames(&onOffNames[0]);
	addAttribute(", Depth", &clearFlagDepth, NULL, x + 16, y, 0, 1, 1, NULL);
	setAttributeValueNames(&onOffNames[0]);
	addAttribute(", Stencil", &clearFlagStencil, NULL, x + 16 + 13, y, 0, 1, 1, NULL);
	setAttributeValueNames(&onOffNames[0]);
	y++;
	addAttribute("Clear Depth", &clearDepth, NULL, x, y, 0, 0xFFFF, 2000, NULL);
	addAttribute(", Stencil ", &clearStencil, NULL, x + 18, y, 0, 0xFF, 0x10, "0x%02X");
	y++;

	ambientColor.r = 0x00;
	ambientColor.g = 0x00;
	ambientColor.b = 0x00;
	ambientColor.a = 0xFF;
	texEnvColor.r = 0xE0;
	texEnvColor.g = 0xE0;
	texEnvColor.b = 0xE0;
	materialAmbient.r  = 0xE0;
	materialAmbient.g  = 0xE0;
	materialAmbient.b  = 0xE0;
	materialAmbient.a  = 0xFF;
	materialSpecular.r = 0xE0;
	materialSpecular.g = 0xE0;
	materialSpecular.b = 0xE0;
	materialDiffuse.r  = 0xE0;
	materialDiffuse.g  = 0xE0;
	materialDiffuse.b  = 0xE0;
	materialEmissive.r = 0xE0;
	materialEmissive.g = 0xE0;
	materialEmissive.b = 0xE0;

	addColorAttribute("Ambient Color R", &ambientColor, x, y, 1, 0x10);
	y++;
	addColorAttribute("TexEnv Color R", &texEnvColor, x, y, 0, 0x10);
	y++;
	addAttribute("Material Emissive", &materialEmissiveFlag, NULL, x, y, 0, 1, 1, NULL);
	setAttributeValueNames(&onOffNames[0]);
	addColorAttribute(", R", &materialEmissive, x + 22, y, 0, 0x10);
	y++;
	addAttribute("Material Diffuse ", &materialDiffuseFlag, NULL, x, y, 0, 1, 1, NULL);
	setAttributeValueNames(&onOffNames[0]);
	addColorAttribute(", R", &materialDiffuse , x + 22, y, 0, 0x10);
	y++;
	addAttribute("Material Specular", &materialSpecularFlag, NULL, x, y, 0, 1, 1, NULL);
	setAttributeValueNames(&onOffNames[0]);
	addColorAttribute(", R", &materialSpecular, x + 22, y, 0, 0x10);
	y++;
	addAttribute("Material Ambient ", &materialAmbientFlag, NULL, x, y, 0, 1, 1, NULL);
	setAttributeValueNames(&onOffNames[0]);
	addColorAttribute(", R", &materialAmbient , x + 22, y, 1, 0x10);
	y++;

	fogColor.r = 0xFF;
	fogColor.g = 0xFF;
	fogColor.b = 0xFF;

	addAttribute("Fog Near", NULL, &fogNear, x, y, -10, 10, 0.1, NULL);
	addAttribute(", Far", NULL, &fogFar, x + 15, y, -10, 10, 0.1, NULL);
	y++;
	addColorAttribute("Fog Color R", &fogColor, x, y, 0, 0x10);
	y++;

	addAttribute("Light Mode", &lightMode, NULL, x, y, 0, 1, 1, NULL);
	setAttributeValueNames(&lightModeNames[0]);
	y++;

	for (i = 0; i < NUM_LIGHTS; i++)
	{
		struct Light *plight = &lights[i];

		plight->position.x = i * 5;
		plight->position.y = 0;
		plight->position.z = 5;
		plight->direction.x = 0;
		plight->direction.y = 0;
		plight->direction.z = 1;
		plight->ambientColor.r = 0x00;
		plight->ambientColor.g = 0x00;
		plight->ambientColor.b = 0x00;
		plight->diffuseColor.r = 0x00;
		plight->diffuseColor.g = 0x00;
		plight->diffuseColor.b = 0x00;
		plight->specularColor.r = 0x00;
		plight->specularColor.g = 0x00;
		plight->specularColor.b = 0x00;
		plight->constantAttenuation = 1;
		plight->linearAttenuation = 0;
		plight->quadraticAttenuation = 0;
		plight->type = GU_DIRECTIONAL;
		plight->kind = 0;
		plight->spotExponent = 10;
		plight->spotCutoff = 0.5;

		y = addLightAttribute(i, plight, 0, y);
	}
}

void moveLine(int step)
{
	int currentY = attributes[selectedAttribute].y;

	while (attributes[selectedAttribute].y != currentY + step)
	{
		if (step > 0)
		{
			selectedAttribute++;
			if (selectedAttribute >= nattributes)
			{
				selectedAttribute = 0;
				break;
			}
		}
		else
		{
			selectedAttribute--;
			if (selectedAttribute < 0)
			{
				selectedAttribute = nattributes - 1;
				break;
			}
		}
	}

	// Move to the beginning of the line
	while (selectedAttribute > 0 && attributes[selectedAttribute].y == attributes[selectedAttribute - 1].y)
	{
		selectedAttribute--;
	}

	return;
}


int main(int argc, char *argv[])
{
	SceCtrlData pad;
	int oldButtons = 0;
#define SECOND	   1000000
#define REPEAT_START (1 * SECOND)
#define REPEAT_DELAY (SECOND / 5)
	struct timeval repeatStart;
	struct timeval repeatDelay;

	repeatStart.tv_sec = 0;
	repeatStart.tv_usec = 0;
	repeatDelay.tv_sec = 0;
	repeatDelay.tv_usec = 0;

	init();

	while(!done)
	{
		struct attribute *pattribute = &attributes[selectedAttribute];

		if (pattribute->y < firstRow)
		{
			firstRow = pattribute->y;
		}
		else if (pattribute->y >= firstRow + numberRows)
		{
			firstRow = pattribute->y - (numberRows - 1);
		}

		draw();

		sceCtrlReadBufferPositive(&pad, 1);
		int buttonDown = (oldButtons ^ pad.Buttons) & pad.Buttons;

		if (pad.Buttons == oldButtons)
		{
			struct timeval now;
			gettimeofday(&now, NULL);
			if (repeatStart.tv_sec == 0)
			{
				repeatStart.tv_sec = now.tv_sec;
				repeatStart.tv_usec = now.tv_usec;
				repeatDelay.tv_sec = 0;
				repeatDelay.tv_usec = 0;
			}
			else
			{
				long usec = (now.tv_sec - repeatStart.tv_sec) * SECOND;
				usec += (now.tv_usec - repeatStart.tv_usec);
				if (usec >= REPEAT_START)
				{
					if (repeatDelay.tv_sec != 0)
					{
						usec = (now.tv_sec - repeatDelay.tv_sec) * SECOND;
						usec += (now.tv_usec - repeatDelay.tv_usec);
						if (usec >= REPEAT_DELAY)
						{
							repeatDelay.tv_sec = 0;
						}
					}

					if (repeatDelay.tv_sec == 0)
					{
						buttonDown = pad.Buttons;
						repeatDelay.tv_sec = now.tv_sec;
						repeatDelay.tv_usec = now.tv_usec;
					}
				}
			}
		}
		else
		{
			repeatStart.tv_sec = 0;
		}

		if (buttonDown & PSP_CTRL_CROSS)
		{
		}

		if (buttonDown & PSP_CTRL_LEFT)
		{
			changeAttributeValue(pattribute, -1);
		}

		if (buttonDown & PSP_CTRL_RIGHT)
		{
			changeAttributeValue(pattribute, 1);
		}

		if (buttonDown & PSP_CTRL_UP)
		{
			selectedAttribute--;
			if (selectedAttribute < 0)
			{
				selectedAttribute = nattributes - 1;
			}
		}

		if (buttonDown & PSP_CTRL_DOWN)
		{
			selectedAttribute++;
			if (selectedAttribute >= nattributes)
			{
				selectedAttribute = 0;
			}
		}

		if (buttonDown & PSP_CTRL_LTRIGGER)
		{
			moveLine(-2);
		}

		if (buttonDown & PSP_CTRL_RTRIGGER)
		{
			moveLine(2);
		}

		if (buttonDown & PSP_CTRL_TRIANGLE)
		{
			done = 1;
		}

		oldButtons = pad.Buttons;
	}

	sceGuTerm();

	sceKernelExitGame();
	return 0;
}

/* Exit callback */
int exit_callback(int arg1, int arg2, void *common)
{
	done = 1;
	return 0;
}

/* Callback thread */
int CallbackThread(SceSize args, void *argp)
{
	int cbid;

	cbid = sceKernelCreateCallback("Exit Callback", exit_callback, (void*)0);
	sceKernelRegisterExitCallback(cbid);

	sceKernelSleepThreadCB();

	return 0;
}

/* Sets up the callback thread and returns its thread id */
int SetupCallbacks(void)
{
	int thid = 0;

	thid = sceKernelCreateThread("CallbackThread", CallbackThread, 0x11, 0xFA0, 0, 0);
	if(thid >= 0)
	{
		sceKernelStartThread(thid, 0, 0);
	}

	return thid;
}

