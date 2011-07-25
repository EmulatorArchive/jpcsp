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

PSP_MODULE_INFO("Memory Test", 0, 1, 0);
PSP_MAIN_THREAD_ATTR(THREAD_ATTR_USER);

#define PSP_SMEM_LowAligned 3

int done = 0;

int testThread(SceSize args, void *argp)
{
	int stack;

	pspDebugScreenPrintf("Thread: stack 0x%08X\n", (int) &stack);
	sceKernelSleepThread();

	return 0;
}


int allocPartitionMemory(int size)
{
	SceUID uid = sceKernelAllocPartitionMemory(2, "Test", PSP_SMEM_Low, size, NULL);
	if (uid < 0)
	{
		if (size == 0)
		{
			pspDebugScreenPrintf("sceKernelAllocPartitionMemory(0x%08X) = 0x%08X - end Test\n", size, uid);
		}
		else
		{
			pspDebugScreenPrintf("sceKernelAllocPartitionMemory(0x%08X) = 0x%08X - error\n", size, uid);
		}
		return 0;
	}
	void *addr = sceKernelGetBlockHeadAddr(uid);
	pspDebugScreenPrintf("sceKernelAllocPartitionMemory(0x%08X) = 0x%08X\n", size, (int) addr);

	return 1;
}


int allocFpl(int size)
{
	SceUID uid = sceKernelCreateFpl("Test", 2, PSP_SMEM_Low, size, 1, NULL);
	if (uid < 0)
	{
		pspDebugScreenPrintf("sceKernelCreateFpl(0x%08X) = 0x%08X - error\n", size, uid);
		return 0;
	}
	void *addr;
	int result = sceKernelTryAllocateFpl(uid, &addr);
	if (result == 0)
	{
		pspDebugScreenPrintf("sceKernelTryAllocateFpl(0x%08X) = 0x%08X\n", size, (int) addr);
	}
	else
	{
		pspDebugScreenPrintf("sceKernelTryAllocateFpl(0x%08X) = 0x%08X - error\n", size, result);
	}

	return 1;
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
	pspDebugScreenPrintf("Press Cross to start the Memory Test\n");
	pspDebugScreenPrintf("Press Circle to Start a new thread\n");
	pspDebugScreenPrintf("Press Square to restart with sceKernelLoadExec (requires FW 1.5)\n");
	pspDebugScreenPrintf("Press Left to start Memory Test for alignment\n");
	pspDebugScreenPrintf("Press Triangle to Exit\n");

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
			pspDebugScreenPrintf("main  = 0x%08X\n", (int) main);
			pspDebugScreenPrintf("Stack = 0x%08X\n", (int) &pad);

			int maxFreeMemSize = sceKernelMaxFreeMemSize();
			pspDebugScreenPrintf("sceKernelMaxFreeMemSize   = 0x%08X\n", maxFreeMemSize);
			int totalFreeMemSize = sceKernelTotalFreeMemSize();
			pspDebugScreenPrintf("sceKernelTotalFreeMemSize = 0x%08X\n", totalFreeMemSize);

			int size;
			for (size = 16; size <= 1024; size *= 2)
			{
				if (!allocPartitionMemory(size))
				{
					break;
				}
			}

			allocPartitionMemory(129);

			for (size = 16; size <= 1024; size *= 2)
			{
				if (!allocFpl(size))
				{
					break;
				}
			}

			allocFpl(129);

			while (!done)
			{
				maxFreeMemSize = sceKernelMaxFreeMemSize();
				pspDebugScreenPrintf("sceKernelMaxFreeMemSize   = 0x%08X\n", maxFreeMemSize);
				totalFreeMemSize = sceKernelTotalFreeMemSize();
				pspDebugScreenPrintf("sceKernelTotalFreeMemSize = 0x%08X\n", totalFreeMemSize);

				size = maxFreeMemSize;
				if (!allocPartitionMemory(size))
				{
					break;
				}
			}
		}

		if (buttonDown & PSP_CTRL_CIRCLE)
		{
			SceUID thid = sceKernelCreateThread("TestThread", testThread, 0x11, 0x1000, 0, 0);
			if (thid >= 0)
			{
				sceKernelStartThread(thid, 0, 0);
			}
			else
			{
				pspDebugScreenPrintf("sceKernelCreateThread() = 0x%08X - error\n", thid);
			}
		}

		if (buttonDown & PSP_CTRL_SQUARE)
		{
			struct SceKernelLoadExecParam loadExecParam;
			memset(&loadExecParam, sizeof(loadExecParam), 0);
			loadExecParam.size = sizeof(loadExecParam);

			int result = sceKernelLoadExec("ms0:/PSP/GAME/memory/EBOOT.PBP", &loadExecParam);
			pspDebugScreenPrintf("sceKernelLoadExec() = 0x%08X\n", result);
		}

		if (buttonDown & PSP_CTRL_TRIANGLE)
		{
			done = 1;
		}

		if (buttonDown & PSP_CTRL_LEFT)
		{
			SceUID uid;
			int size;
			int alignment;
			void *addr;

			size = 0x100;
			alignment = 0x100;
			uid = sceKernelAllocPartitionMemory(2, "Test", PSP_SMEM_LowAligned, size, (void*) alignment);
			addr = sceKernelGetBlockHeadAddr(uid);
			pspDebugScreenPrintf("sceKernelAllocPartitionMemory(0x%X, align=0x%X) = 0x%08X\n", size, alignment, (int) addr);
			size = 0x1100;
			alignment = 0x1000;
			uid = sceKernelAllocPartitionMemory(2, "Test", PSP_SMEM_LowAligned, size, (void*) alignment);
			addr = sceKernelGetBlockHeadAddr(uid);
			pspDebugScreenPrintf("sceKernelAllocPartitionMemory(0x%X, align=0x%X) = 0x%08X\n", size, alignment, (int) addr);
			size = 0x100;
			alignment = 0x100;
			uid = sceKernelAllocPartitionMemory(2, "Test", PSP_SMEM_LowAligned, 0x100, (void*) alignment);
			addr = sceKernelGetBlockHeadAddr(uid);
			pspDebugScreenPrintf("sceKernelAllocPartitionMemory(0x%X, align=0x%X) = 0x%08X\n", size, alignment, (int) addr);
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

