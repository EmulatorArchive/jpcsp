/*
 *	PMF Player Module
 *	Copyright (c) 2006 by Sorin P. C. <magik@hypermagik.com>
 */
#include <pspkernel.h>

#include <stdio.h>
#include <malloc.h>
#include <string.h>

#include "JpcspConnector.h"

PSP_MODULE_INFO(pmfPlayer, 0, 1, 0);
PSP_MAIN_THREAD_ATTR(THREAD_ATTR_USER);


int main_thread(SceSize _argc, void* _argp)
{
	JpcspConnector *Connector = new JpcspConnector();

	Connector->run(_argc, _argp);

	return 0;
}

extern "C" int module_start(SceSize _argc, char* _argp)
{
	char* arg = _argp + strlen(_argp) + 1;

	SceUID T = sceKernelCreateThread("pmfplayer_thread", main_thread, 0x20, 0xFA0, THREAD_ATTR_USER, NULL);

	sceKernelStartThread(T, strlen(arg)+1, arg);

	sceKernelWaitThreadEnd(T, 0);

	return 0;
}
