#include <pspkernel.h>
#include <pspdebug.h>
#include <pspctrl.h>
#include <pspdisplay.h>
#include <pspgu.h>
#include <pspgum.h>
#include <psppower.h>

#include <sys/stat.h>
#include <stdio.h>
#include <string.h>
#include <assert.h>

PSP_MODULE_INFO("IO Test", 0, 1, 0);
PSP_MAIN_THREAD_ATTR(THREAD_ATTR_USER | PSP_THREAD_ATTR_VFPU);

int done = 0;

struct msInfo
{
	int maxClusters;
	int freeClusters;
	int maxSectors;
	int sectorSize;
	int sectorCount;
};

void testIoDevctl()
{
	struct msInfo info;
	struct msInfo *pinfo;
	int res;

	pinfo = &info;
	res = sceIoDevctl("ms0:", 0x02425818, &pinfo, sizeof(pinfo), NULL, 0);
	pspDebugScreenPrintf("sceIoDevctl cmd=0x%08X, res=0x%0X, maxClusters=%d, freeClusters=%d, maxSectors=%d, sectorSize=%d, sectorCount=%d\n", 0x02425818, res, info.maxClusters, info.freeClusters, info.maxSectors, info.sectorSize, info.sectorCount);
}


char getPrintableChar(char c)
{
	if (c < ' ' || c > 0x7e)
	{
		c = '.';
	}

	return c;
}


void displayBuffer(char *buffer, int length)
{
	int i, j;

	for (i = 0; i < length; i++)
	{
		pspDebugScreenPrintf(" %02X", buffer[i] & 0xFF);
		if ((i % 16) == 15)
		{
			pspDebugScreenPrintf("  ");
			for (j = i - 15; j <= i; j++)
			{
				pspDebugScreenPrintf("%c", getPrintableChar(buffer[j]));
			}
			pspDebugScreenPrintf("\n");
			if ((i % 480) == 479)
			{
				pspDebugScreenPrintf("Press Cross to continue");
				SceCtrlData pad;
				// Wait for Cross press
				while (1)
				{
					sceCtrlReadBufferPositive(&pad, 1);
					if (pad.Buttons & PSP_CTRL_CROSS)
					{
						break;
					}
				}
				// Wait for Cross release
				while (1)
				{
					sceCtrlReadBufferPositive(&pad, 1);
					if (!(pad.Buttons & PSP_CTRL_CROSS))
					{
						break;
					}
				}
				pspDebugScreenClear();
			}
		}
	}

	int lengthLastLine = length % 16;
	if (lengthLastLine > 0)
	{
		for (i = 0; i < lengthLastLine; i++)
		{
			pspDebugScreenPrintf("   ");
		}
		pspDebugScreenPrintf("  ");
		for (i = length - lengthLastLine; i < length; i++)
		{
			pspDebugScreenPrintf("%c", getPrintableChar(buffer[i]));
		}
	}

	pspDebugScreenPrintf("\n");
}


void testIoIoctl()
{
	char buffer[0x800];
	SceUID uid;
	int result;
	int cmd;

	memset(buffer, 0x11, sizeof(buffer));
	uid = sceIoOpen("disc0:/PSP_GAME/SYSDIR/BOOT.BIN", PSP_O_RDONLY, 0);
	if (uid < 0)
	{
		pspDebugScreenPrintf("Cannot open UMD file: result=0x%08X\n", uid);
		return;
	}

	cmd = 0x01020001;
	result = sceIoIoctl(uid, cmd, NULL, 0, buffer, sizeof(buffer));
	pspDebugScreenPrintf("sceIoIoctl: 0x%08X result=0x%08X\n", cmd, result);
	displayBuffer(buffer, sizeof(buffer));

	int pathTableSize = *((int *) (&buffer[132]));
	if (pathTableSize > sizeof(buffer))
	{
		pathTableSize = sizeof(buffer);
	}

	cmd = 0x01020002;
	result = sceIoIoctl(uid, cmd, NULL, 0, buffer, pathTableSize);
	pspDebugScreenPrintf("sceIoIoctl: 0x%08X result=0x%08X\n", cmd, result);
	displayBuffer(buffer, pathTableSize);

	sceIoClose(uid);
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

	pspDebugScreenInit();
	pspDebugScreenPrintf("Press Cross to start the sceIoDevctl Test\n");
	pspDebugScreenPrintf("Press Circle to start the sceIoIoctl Test\n");

	while(!done)
	{
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
			testIoDevctl();
		}

		if (buttonDown & PSP_CTRL_CIRCLE)
		{
			testIoIoctl();
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
