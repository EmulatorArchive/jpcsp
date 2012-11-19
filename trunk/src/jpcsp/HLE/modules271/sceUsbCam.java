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
package jpcsp.HLE.modules271;

import jpcsp.HLE.HLEFunction;
import jpcsp.HLE.HLELogging;
import jpcsp.HLE.HLEUnimplemented;
import jpcsp.HLE.TPointer;

import org.apache.log4j.Logger;

import jpcsp.Emulator;
import jpcsp.Memory;
import jpcsp.Processor;
import jpcsp.Allegrex.CpuState;
import jpcsp.HLE.Modules;
import jpcsp.HLE.kernel.types.pspUsbCamSetupMicParam;
import jpcsp.HLE.kernel.types.pspUsbCamSetupStillExParam;
import jpcsp.HLE.kernel.types.pspUsbCamSetupStillParam;
import jpcsp.HLE.kernel.types.pspUsbCamSetupVideoExParam;
import jpcsp.HLE.kernel.types.pspUsbCamSetupVideoParam;
import jpcsp.HLE.modules.HLEModule;

public class sceUsbCam extends HLEModule {
    public static Logger log = Modules.getLogger("sceUsbCam");

	@Override
	public String getName() {
		return "sceUsbCam";
	}

	public static final int PSP_USBCAM_PID = 0x282;
	public static final String PSP_USBCAM_DRIVERNAME = "USBCamDriver";
	public static final String PSP_USBCAMMIC_DRIVERNAME = "USBCamMicDriver";

	/** Resolutions for sceUsbCamSetupStill & sceUsbCamSetupVideo
     ** DO NOT use on sceUsbCamSetupStillEx & sceUsbCamSetupVideoEx */
	public static final int PSP_USBCAM_RESOLUTION_160_120  = 0;
	public static final int PSP_USBCAM_RESOLUTION_176_144  = 1;
	public static final int PSP_USBCAM_RESOLUTION_320_240  = 2;
	public static final int PSP_USBCAM_RESOLUTION_352_288  = 3;
	public static final int PSP_USBCAM_RESOLUTION_640_480  = 4;
	public static final int PSP_USBCAM_RESOLUTION_1024_768 = 5;
	public static final int PSP_USBCAM_RESOLUTION_1280_960 = 6;
	public static final int PSP_USBCAM_RESOLUTION_480_272  = 7;
	public static final int PSP_USBCAM_RESOLUTION_360_272  = 8;

	/** Resolutions for sceUsbCamSetupStillEx & sceUsbCamSetupVideoEx
     ** DO NOT use on sceUsbCamSetupStill & sceUsbCamSetupVideo */
	public static final int PSP_USBCAM_RESOLUTION_EX_160_120  = 0;
	public static final int PSP_USBCAM_RESOLUTION_EX_176_144  = 1;
	public static final int PSP_USBCAM_RESOLUTION_EX_320_240  = 2;
	public static final int PSP_USBCAM_RESOLUTION_EX_352_288  = 3;
	public static final int PSP_USBCAM_RESOLUTION_EX_360_272  = 4;
	public static final int PSP_USBCAM_RESOLUTION_EX_480_272  = 5;
	public static final int PSP_USBCAM_RESOLUTION_EX_640_480  = 6;
	public static final int PSP_USBCAM_RESOLUTION_EX_1024_768 = 7;
	public static final int PSP_USBCAM_RESOLUTION_EX_1280_960 = 8;

	/** Flags for reverse effects. */
	public static final int PSP_USBCAM_FLIP = 1;
	public static final int PSP_USBCAM_MIRROR = 0x100;

	/** Delay to take pictures */
	public static final int PSP_USBCAM_NODELAY = 0;
	public static final int PSP_USBCAM_DELAY_10SEC = 1;
	public static final int PSP_USBCAM_DELAY_20SEC = 2;
	public static final int PSP_USBCAM_DELAY_30SEC = 3;

	/** Usbcam framerates */
	public static final int PSP_USBCAM_FRAMERATE_3_75_FPS = 0; /* 3.75 fps */
	public static final int PSP_USBCAM_FRAMERATE_5_FPS = 1;
	public static final int PSP_USBCAM_FRAMERATE_7_5_FPS = 2; /* 7.5 fps */
	public static final int PSP_USBCAM_FRAMERATE_10_FPS = 3;
	public static final int PSP_USBCAM_FRAMERATE_15_FPS = 4;
	public static final int PSP_USBCAM_FRAMERATE_20_FPS = 5;
	public static final int PSP_USBCAM_FRAMERATE_30_FPS = 6;
	public static final int PSP_USBCAM_FRAMERATE_60_FPS = 7;

	/** White balance values */
	public static final int PSP_USBCAM_WB_AUTO = 0;
	public static final int PSP_USBCAM_WB_DAYLIGHT = 1;
	public static final int PSP_USBCAM_WB_FLUORESCENT = 2;
	public static final int PSP_USBCAM_WB_INCADESCENT = 3;

	/** Effect modes */
	public static final int PSP_USBCAM_EFFECTMODE_NORMAL = 0;
	public static final int PSP_USBCAM_EFFECTMODE_NEGATIVE = 1;
	public static final int PSP_USBCAM_EFFECTMODE_BLACKWHITE = 2;
	public static final int PSP_USBCAM_EFFECTMODE_SEPIA = 3;
	public static final int PSP_USBCAM_EFFECTMODE_BLUE = 4;
	public static final int PSP_USBCAM_EFFECTMODE_RED = 5;
	public static final int PSP_USBCAM_EFFECTMODE_GREEN = 6;

	/** Exposure levels */
	public static final int PSP_USBCAM_EVLEVEL_2_0_POSITIVE = 0; // +2.0
	public static final int PSP_USBCAM_EVLEVEL_1_7_POSITIVE = 1; // +1.7
	public static final int PSP_USBCAM_EVLEVEL_1_5_POSITIVE = 2; // +1.5
	public static final int PSP_USBCAM_EVLEVEL_1_3_POSITIVE = 3; // +1.3
	public static final int PSP_USBCAM_EVLEVEL_1_0_POSITIVE = 4; // +1.0
	public static final int PSP_USBCAM_EVLEVEL_0_7_POSITIVE = 5; // +0.7
	public static final int PSP_USBCAM_EVLEVEL_0_5_POSITIVE = 6; // +0.5
	public static final int PSP_USBCAM_EVLEVEL_0_3_POSITIVE = 7; // +0.3
	public static final int PSP_USBCAM_EVLEVEL_0_0 = 8; // 0.0
	public static final int PSP_USBCAM_EVLEVEL_0_3_NEGATIVE = 9; // -0.3
	public static final int PSP_USBCAM_EVLEVEL_0_5_NEGATIVE = 10; // -0.5
	public static final int PSP_USBCAM_EVLEVEL_0_7_NEGATIVE = 11; // -0.7
	public static final int PSP_USBCAM_EVLEVEL_1_0_NEGATIVE = 12; // -1.0
	public static final int PSP_USBCAM_EVLEVEL_1_3_NEGATIVE = 13; // -1.3
	public static final int PSP_USBCAM_EVLEVEL_1_5_NEGATIVE = 14; // -1.5
	public static final int PSP_USBCAM_EVLEVEL_1_7_NEGATIVE = 15; // -1.7
	public static final int PSP_USBCAM_EVLEVEL_2_0_NEGATIVE = 16; // -2.0

	protected int workArea;
	protected int workAreaSize;
	protected int jpegBuffer;
	protected int jpegBufferSize;

	// Camera settings
	protected int resolution; // One of PSP_USBCAM_RESOLUTION_* (not PSP_USBCAM_RESOLUTION_EX_*)
	protected int frameRate;
	protected int whiteBalance;
	protected int frameSize;
	protected int saturation;
	protected int brightness;
	protected int contrast;
	protected int sharpness;
	protected int imageEffectMode;
	protected int evLevel;
	protected boolean flip;
	protected boolean mirror;
	protected int zoom;
	protected boolean autoImageReverseSW;
	protected boolean lensDirectionAtYou;
	protected int micFrequency;
	protected int micGain;

	// Faked video reading
	protected long lastVideoFrameMillis;
	protected static final int[] framerateFrameDurationMillis = new int[] {
		267, // PSP_USBCAM_FRAMERATE_3_75_FPS
		200, // PSP_USBCAM_FRAMERATE_5_FPS
		133, // PSP_USBCAM_FRAMERATE_7_5_FPS
		100, // PSP_USBCAM_FRAMERATE_10_FPS
		67, // PSP_USBCAM_FRAMERATE_15_FPS
		50, // PSP_USBCAM_FRAMERATE_20_FPS
		33, // PSP_USBCAM_FRAMERATE_30_FPS
		17 // PSP_USBCAM_FRAMERATE_60_FPS
	};

	/**
	 * Convert a value PSP_USBCAM_RESOLUTION_EX_*
	 * to the corresponding PSP_USBCAM_RESOLUTION_*
	 * 
	 * @param resolutionEx One of PSP_USBCAM_RESOLUTION_EX_*
	 * @return             The corresponding value PSP_USBCAM_RESOLUTION_*
	 */
	protected int convertResolutionExToResolution(int resolutionEx) {
		switch(resolutionEx) {
			case PSP_USBCAM_RESOLUTION_EX_160_120: return PSP_USBCAM_RESOLUTION_160_120;
			case PSP_USBCAM_RESOLUTION_EX_176_144: return PSP_USBCAM_RESOLUTION_176_144;
			case PSP_USBCAM_RESOLUTION_EX_320_240: return PSP_USBCAM_RESOLUTION_320_240;
			case PSP_USBCAM_RESOLUTION_EX_352_288: return PSP_USBCAM_RESOLUTION_352_288;
			case PSP_USBCAM_RESOLUTION_EX_360_272: return PSP_USBCAM_RESOLUTION_360_272;
			case PSP_USBCAM_RESOLUTION_EX_480_272: return PSP_USBCAM_RESOLUTION_480_272;
			case PSP_USBCAM_RESOLUTION_EX_640_480: return PSP_USBCAM_RESOLUTION_640_480;
			case PSP_USBCAM_RESOLUTION_EX_1024_768: return PSP_USBCAM_RESOLUTION_1024_768;
			case PSP_USBCAM_RESOLUTION_EX_1280_960: return PSP_USBCAM_RESOLUTION_1280_960;
		}

		return resolutionEx;
	}

	protected int getFramerateFrameDurationMillis() {
		if (frameRate < 0 || frameRate > PSP_USBCAM_FRAMERATE_60_FPS) {
			return framerateFrameDurationMillis[PSP_USBCAM_FRAMERATE_60_FPS];
		}
		return framerateFrameDurationMillis[frameRate];
	}

	protected int readFakeVideoFrame() {
		Memory mem = Processor.memory;

		// Image has to be stored in Jpeg format in buffer
		mem.memset(jpegBuffer, (byte) 0x00, jpegBufferSize);

		return jpegBufferSize;
	}

	/**
	 * Set ups the parameters for video capture.
	 *
	 * @param param - Pointer to a pspUsbCamSetupVideoParam structure.
	 * @param workarea - Pointer to a buffer used as work area by the driver.
	 * @param wasize - Size of the work area.
	 *
	 * @return 0 on success, < 0 on error
	 */
	@HLEFunction(nid = 0x17F7B2FB, version = 271)
	public void sceUsbCamSetupVideo(Processor processor) {
		CpuState cpu = processor.cpu;
		Memory mem = Processor.memory;

		int param = cpu._a0;
		int workArea = cpu._a1;
		int workAreaSize = cpu._a2;

		pspUsbCamSetupVideoParam usbCamSetupVideoParam = new pspUsbCamSetupVideoParam();
		usbCamSetupVideoParam.read(mem, param);

		log.warn(String.format("Partial sceUsbCamSetupVideo param=0x%08X, workArea=0x%08X, workAreaSize=%d, param=%s", param, workArea, workAreaSize, usbCamSetupVideoParam.toString()));

		this.workArea = workArea;
		this.workAreaSize = workAreaSize;
		resolution = usbCamSetupVideoParam.resolution;
		frameRate = usbCamSetupVideoParam.framerate;
		whiteBalance = usbCamSetupVideoParam.wb;
		saturation = usbCamSetupVideoParam.saturation;
		brightness = usbCamSetupVideoParam.brightness;
		contrast = usbCamSetupVideoParam.contrast;
		sharpness = usbCamSetupVideoParam.sharpness;
		imageEffectMode = usbCamSetupVideoParam.effectmode;
		frameSize = usbCamSetupVideoParam.framesize;
		evLevel = usbCamSetupVideoParam.evlevel;

		cpu._v0 = 0;
	}

	/**
	 * Sets if the image should be automatically reversed, depending of the position
	 * of the camera.
	 *
	 * @param on - 1 to set the automatical reversal of the image, 0 to set it off
	 *
	 * @return 0 on success, < 0 on error
	 */
	@HLEFunction(nid = 0xF93C4669, version = 271)
	public void sceUsbCamAutoImageReverseSW(Processor processor) {
		CpuState cpu = processor.cpu;

		int on = cpu._a0;

		autoImageReverseSW = (on != 1);

		log.warn(String.format("Partial sceUsbCamAutoImageReverseSW on=%d", on));

		cpu._v0 = 0;
	}

	/**
	 * Starts video input from the camera.
	 *
	 * @return 0 on success, < 0 on error
	 */
	@HLEFunction(nid = 0x574A8C3F, version = 271)
	public void sceUsbCamStartVideo(Processor processor) {
		CpuState cpu = processor.cpu;

		// No parameters

		log.warn(String.format("Ignoring sceUsbCamStartVideo"));

		cpu._v0 = 0;
	}

	/**
	 * Stops video input from the camera.
	 *
	 * @return 0 on success, < 0 on error
	*/
	@HLEFunction(nid = 0x6CF32CB9, version = 271)
	public void sceUsbCamStopVideo(Processor processor) {
		CpuState cpu = processor.cpu;

		// No parameters

		log.warn(String.format("Ignoring sceUsbCamStopVideo"));

		cpu._v0 = 0;
	}

	@HLEFunction(nid = 0x03ED7A82, version = 271)
	public int sceUsbCamSetupMic(pspUsbCamSetupMicParam camSetupMicParam, TPointer workArea, int workAreaSize) {
		micFrequency = camSetupMicParam.frequency;
		micGain = camSetupMicParam.gain;

		return 0;
	}

	@HLELogging(level="info")
	@HLEFunction(nid = 0x82A64030, version = 271)
	public int sceUsbCamStartMic() {
		return 0;
	}

	/**
	 * Reads a video frame. The function doesn't return until the frame
	 * has been acquired.
	 *
	 * @param buf - The buffer that receives the frame jpeg data
	 * @param size - The size of the buffer.
	 *
	 * @return size of acquired frame on success, < 0 on error
	 */
	@HLEUnimplemented
	@HLEFunction(nid = 0x7DAC0C71, version = 271)
	public int sceUsbCamReadVideoFrameBlocking(TPointer jpegBuffer, int jpegBufferSize) {
		this.jpegBuffer = jpegBuffer.getAddress();
		this.jpegBufferSize = jpegBufferSize;

		long now = Emulator.getClock().currentTimeMillis();
		int millisSinceLastFrame = (int) (now - lastVideoFrameMillis);
		int frameDurationMillis = getFramerateFrameDurationMillis();
		if (millisSinceLastFrame >= 0 && millisSinceLastFrame < frameDurationMillis) {
			int delayMillis = frameDurationMillis - millisSinceLastFrame;
			Modules.ThreadManForUserModule.hleKernelDelayThread(delayMillis * 1000, false);
			lastVideoFrameMillis = now + delayMillis;
		} else {
			lastVideoFrameMillis = now;
		}

		return readFakeVideoFrame();
	}

	/**
	 * Reads a video frame. The function returns inmediately, and
	 * the completion has to be handled by calling sceUsbCamWaitReadVideoFrameEnd
	 * or sceUsbCamPollReadVideoFrameEnd.
	 *
	 * @param buf - The buffer that receives the frame jpeg data
	 * @param size - The size of the buffer.
	 *
	 * @return 0 on success, < 0 on error
	 */
	@HLEFunction(nid = 0x99D86281, version = 271)
	public void sceUsbCamReadVideoFrame(Processor processor) {
		CpuState cpu = processor.cpu;

		int jpegBuffer = cpu._a0;
		int jpegBufferSize = cpu._a1;

		log.warn(String.format("Unimplemented sceUsbCamReadVideoFrame jpegBuffer=0x%08X, jpegBufferSize=%d", jpegBuffer, jpegBufferSize));

		this.jpegBuffer = jpegBuffer;
		this.jpegBufferSize = jpegBufferSize;

		cpu._v0 = 0;
	}

	/**
	 * Polls the status of video frame read completion.
	 *
	 * @return the size of the acquired frame if it has been read,
	 * 0 if the frame has not yet been read, < 0 on error.
	 */
	@HLEFunction(nid = 0x41E73E95, version = 271)
	public void sceUsbCamPollReadVideoFrameEnd(Processor processor) {
		CpuState cpu = processor.cpu;

		log.warn(String.format("Unimplemented sceUsbCamPollReadVideoFrameEnd"));

		cpu._v0 = readFakeVideoFrame();
	}

	/**
	 * Waits untils the current frame has been read.
	 *
	 * @return the size of the acquired frame on sucess, < 0 on error
	 */
	@HLEFunction(nid = 0xF90B2293, version = 271)
	public void sceUsbCamWaitReadVideoFrameEnd(Processor processor) {
		CpuState cpu = processor.cpu;

		log.warn(String.format("Unimplemented sceUsbCamWaitReadVideoFrameEnd"));

		cpu._v0 = readFakeVideoFrame();
	}

	/**
	 * Gets the direction of the camera lens
	 *
	 * @return 1 if the camera is "looking to you", 0 if the camera
	 * is "looking to the other side".
	 */
	@HLEFunction(nid = 0x4C34F553, version = 271)
	public void sceUsbCamGetLensDirection(Processor processor) {
		CpuState cpu = processor.cpu;

		log.warn(String.format("Unimplemented sceUsbCamGetLensDirection"));

		cpu._v0 = lensDirectionAtYou ? 1 : 0;
	}

	/**
	 * Setups the parameters to take a still image.
	 *
	 * @param param - pointer to a pspUsbCamSetupStillParam
	 *
	 * @return 0 on success, < 0 on error
	 */
	@HLEFunction(nid = 0x3F0CF289, version = 271)
	public void sceUsbCamSetupStill(Processor processor) {
		CpuState cpu = processor.cpu;

		int paramAddr = cpu._a0;

		pspUsbCamSetupStillParam usbCamSetupStillParam = new pspUsbCamSetupStillParam();
		usbCamSetupStillParam.read(Processor.memory, paramAddr);

		log.warn(String.format("Unimplemented sceUsbCamSetupStill param=0x%08X, %s", paramAddr, usbCamSetupStillParam.toString()));

		cpu._v0 = 0;
	}

	/**
	 * Setups the parameters to take a still image (with more options)
	 *
	 * @param param - pointer to a pspUsbCamSetupStillParamEx
	 *
	 * @return 0 on success, < 0 on error
	 */
	@HLEFunction(nid = 0x0A41A298, version = 271)
	public void sceUsbCamSetupStillEx(Processor processor) {
		CpuState cpu = processor.cpu;

		int paramAddr = cpu._a0;

		pspUsbCamSetupStillExParam usbCamSetupStillExParam = new pspUsbCamSetupStillExParam();
		usbCamSetupStillExParam.read(Processor.memory, paramAddr);

		log.warn(String.format("Unimplemented sceUsbCamSetupStillEx param=0x%08X, %s", paramAddr, usbCamSetupStillExParam.toString()));

		cpu._v0 = 0;
	}

	/**
	 * Gets a still image. The function doesn't return until the image
	 * has been acquired.
	 *
	 * @param buf - The buffer that receives the image jpeg data
	 * @param size - The size of the buffer.
	 *
	 * @return size of acquired image on success, < 0 on error
	 */
	@HLEFunction(nid = 0x61BE5CAC, version = 271)
	public void sceUsbCamStillInputBlocking(Processor processor) {
		CpuState cpu = processor.cpu;

		int buffer = cpu._a0;
		int size = cpu._a1;

		log.warn(String.format("Unimplemented sceUsbCamStillInputBlocking buffer=0x%08X, size=%d", buffer, size));

		cpu._v0 = 0;
	}

	/**
	 * Gets a still image. The function returns immediately, and
	 * the completion has to be handled by calling ::sceUsbCamStillWaitInputEnd
	 * or ::sceUsbCamStillPollInputEnd.
	 *
	 * @param buf - The buffer that receives the image jpeg data
	 * @param size - The size of the buffer.
	 *
	 * @return 0 on success, < 0 on error
	 */
	@HLEFunction(nid = 0xFB0A6C5D, version = 271)
	public void sceUsbCamStillInput(Processor processor) {
		CpuState cpu = processor.cpu;

		int buffer = cpu._a0;
		int size = cpu._a1;

		log.warn(String.format("Unimplemented sceUsbCamStillInput buffer=0x%08X, size=%d", buffer, size));

		cpu._v0 = 0;
	}

	/**
	 * Waits until still input has been finished.
	 *
	 * @return the size of the acquired image on success, < 0 on error
	 */
	@HLEFunction(nid = 0x7563AFA1, version = 271)
	public void sceUsbCamStillWaitInputEnd(Processor processor) {
		CpuState cpu = processor.cpu;

		log.warn(String.format("Unimplemented sceUsbCamStillWaitInputEnd"));

		cpu._v0 = 0;
	}

	/**
	 * Polls the status of still input completion.
	 *
	 * @return the size of the acquired image if still input has ended,
	 * 0 if the input has not ended, < 0 on error.
	 */
	@HLEFunction(nid = 0x1A46CFE7, version = 271)
	public void sceUsbCamStillPollInputEnd(Processor processor) {
		CpuState cpu = processor.cpu;

		log.warn(String.format("Unimplemented sceUsbCamStillPollInputEnd"));

		cpu._v0 = 0;
	}

	/**
	 * Cancels the still input.
	 *
	 * @return 0 on success, < 0 on error
	 */
	@HLEFunction(nid = 0xA720937C, version = 271)
	public void sceUsbCamStillCancelInput(Processor processor) {
		CpuState cpu = processor.cpu;

		log.warn(String.format("Unimplemented sceUsbCamStillCancelInput"));

		cpu._v0 = 0;
	}

	/**
	 * Gets the size of the acquired still image.
	 *
	 * @return the size of the acquired image on success, < 0 on error
	 */
	@HLEFunction(nid = 0xE5959C36, version = 271)
	public void sceUsbCamStillGetInputLength(Processor processor) {
		CpuState cpu = processor.cpu;

		log.warn(String.format("Unimplemented sceUsbCamStillGetInputLength"));

		cpu._v0 = 0;
	}

	/**
	 * Set ups the parameters for video capture (with more options)
	 *
	 * @param param - Pointer to a pspUsbCamSetupVideoExParam structure.
	 * @param workarea - Pointer to a buffer used as work area by the driver.
	 * @param wasize - Size of the work area.
	 *
	 * @return 0 on success, < 0 on error
	 */
	@HLEFunction(nid = 0xCFE9E999, version = 271)
	public void sceUsbCamSetupVideoEx(Processor processor) {
		CpuState cpu = processor.cpu;
		Memory mem = Processor.memory;

		int param = cpu._a0;
		int workArea = cpu._a1;
		int workAreaSize = cpu._a2;

		pspUsbCamSetupVideoExParam usbCamSetupVideoExParam = new pspUsbCamSetupVideoExParam();
		usbCamSetupVideoExParam.read(mem, param);

		log.warn(String.format("Partial sceUsbCamSetupVideoEx param=0x%08X, workArea=0x%08X, workAreaSize=%d, param=%s", param, workArea, workAreaSize, usbCamSetupVideoExParam.toString()));

		this.workArea = workArea;
		this.workAreaSize = workAreaSize;
		resolution = convertResolutionExToResolution(usbCamSetupVideoExParam.resolution);
		frameRate = usbCamSetupVideoExParam.framerate;
		whiteBalance = usbCamSetupVideoExParam.wb;
		saturation = usbCamSetupVideoExParam.saturation;
		brightness = usbCamSetupVideoExParam.brightness;
		contrast = usbCamSetupVideoExParam.contrast;
		sharpness = usbCamSetupVideoExParam.sharpness;
		imageEffectMode = usbCamSetupVideoExParam.effectmode;
		frameSize = usbCamSetupVideoExParam.framesize;
		evLevel = usbCamSetupVideoExParam.evlevel;

		cpu._v0 = 0;
	}

	/**
	 * Gets the size of the acquired frame.
	 *
	 * @return the size of the acquired frame on success, < 0 on error
	 */
	@HLEFunction(nid = 0xDF9D0C92, version = 271)
	public void sceUsbCamGetReadVideoFrameSize(Processor processor) {
		CpuState cpu = processor.cpu;

		log.warn(String.format("Unimplemented sceUsbCamGetReadVideoFrameSize"));

		cpu._v0 = jpegBufferSize;
	}

	/**
	 * Sets the saturation
	 *
	 * @param saturation - The saturation (0-255)
	 *
	 * @return 0 on success, < 0 on error
	 */
	@HLEFunction(nid = 0x6E205974, version = 271)
	public void sceUsbCamSetSaturation(Processor processor) {
		CpuState cpu = processor.cpu;

		int saturation = cpu._a0;

		log.warn(String.format("Unimplemented sceUsbCamSetSaturation %d", saturation));

		this.saturation = saturation;

		cpu._v0 = 0;
	}

	/**
	 * Sets the brightness
	 *
	 * @param brightness - The brightness (0-255)
	 *
	 * @return 0 on success, < 0 on error
	 */
	@HLEFunction(nid = 0x4F3D84D5, version = 271)
	public void sceUsbCamSetBrightness(Processor processor) {
		CpuState cpu = processor.cpu;

		int brightness = cpu._a0;

		log.warn(String.format("Unimplemented sceUsbCamSetBrightness %d", brightness));

		this.brightness = brightness;

		cpu._v0 = 0;
	}

	/**
	 * Sets the contrast
	 *
	 * @param contrast - The contrast (0-255)
	 *
	 * @return 0 on success, < 0 on error
	 */
	@HLEFunction(nid = 0x09C26C7E, version = 271)
	public void sceUsbCamSetContrast(Processor processor) {
		CpuState cpu = processor.cpu;

		int contrast = cpu._a0;

		log.warn(String.format("Unimplemented sceUsbCamSetContrast %d", contrast));

		this.contrast = contrast;

		cpu._v0 = 0;
	}

	/**
	 * Sets the sharpness
	 *
	 * @param sharpness - The sharpness (0-255)
	 *
	 * @return 0 on success, < 0 on error
	 */
	@HLEFunction(nid = 0x622F83CC, version = 271)
	public void sceUsbCamSetSharpness(Processor processor) {
		CpuState cpu = processor.cpu;

		int sharpness = cpu._a0;

		log.warn(String.format("Unimplemented sceUsbCamSetSharpness %d", sharpness));

		this.sharpness = sharpness;

		cpu._v0 = 0;
	}

	/**
	 * Sets the image effect mode
	 *
	 * @param effectmode - The effect mode, one of ::PspUsbCamEffectMode
	 *
	 * @return 0 on success, < 0 on error
	 */
	@HLEFunction(nid = 0xD4876173, version = 271)
	public void sceUsbCamSetImageEffectMode(Processor processor) {
		CpuState cpu = processor.cpu;

		int effectMode = cpu._a0;

		log.warn(String.format("Unimplemented sceUsbCamSetImageEffectMode %d", effectMode));

		this.imageEffectMode = effectMode;

		cpu._v0 = 0;
	}

	/**
	 * Sets the exposure level
	 *
	 * @param ev - The exposure level, one of ::PspUsbCamEVLevel
	 *
	 * @return 0 on success, < 0 on error
	 */
	@HLEFunction(nid = 0x1D686870, version = 271)
	public void sceUsbCamSetEvLevel(Processor processor) {
		CpuState cpu = processor.cpu;

		int evLevel = cpu._a0;

		log.warn(String.format("Unimplemented sceUsbCamSetEvLevel %d", evLevel));

		this.evLevel = evLevel;

		cpu._v0 = 0;
	}

	/**
	 * Sets the reverse mode
	 *
	 * @param reverseflags - The reverse flags, zero or more of ::PspUsbCamReverseFlags
	 *
	 * @return 0 on success, < 0 on error
	 */
	@HLEFunction(nid = 0x951BEDF5, version = 271)
	public void sceUsbCamSetReverseMode(Processor processor) {
		CpuState cpu = processor.cpu;

		int reverseMode = cpu._a0;

		log.warn(String.format("Unimplemented sceUsbCamSetReverseMode %d", reverseMode));

		this.flip = (reverseMode & PSP_USBCAM_FLIP) != 0;
		this.mirror = (reverseMode & PSP_USBCAM_MIRROR) != 0;

		cpu._v0 = 0;
	}

	/**
	 * Sets the zoom.
	 *
	 * @param zoom - The zoom level starting by 10. (10 = 1X, 11 = 1.1X, etc)
	 *
	 * @returns 0 on success, < 0 on error
	 */
	@HLEFunction(nid = 0xC484901F, version = 271)
	public void sceUsbCamSetZoom(Processor processor) {
		CpuState cpu = processor.cpu;

		int zoom = cpu._a0;

		log.warn(String.format("Unimplemented sceUsbCamSetZoom %d", zoom));

		this.zoom = zoom;

		cpu._v0 = 0;
	}

	/**
	 * Gets the current saturation
	 *
	 * @param saturation - pointer to a variable that receives the current saturation
	 *
	 * @return 0 on success, < 0 on error
	 */
	@HLEFunction(nid = 0x383E9FA8, version = 271)
	public void sceUsbCamGetSaturation(Processor processor) {
		CpuState cpu = processor.cpu;

		int saturationAddr = cpu._a0;

		log.warn(String.format("Unimplemented sceUsbCamGetSaturation 0x%08X", saturationAddr));

		Processor.memory.write32(saturationAddr, saturation);

		cpu._v0 = 0;
	}

	/**
	 * Gets the current brightness
	 *
	 * @param brightness - pointer to a variable that receives the current brightness
	 *
	 * @return 0 on success, < 0 on error
	 */
	@HLEFunction(nid = 0x70F522C5, version = 271)
	public void sceUsbCamGetBrightness(Processor processor) {
		CpuState cpu = processor.cpu;

		int brightnessAddr = cpu._a0;

		log.warn(String.format("Unimplemented sceUsbCamGetBrightness 0x%08X", brightnessAddr));

		Processor.memory.write32(brightnessAddr, brightness);

		cpu._v0 = 0;
	}

	/**
	 * Gets the current contrast
	 *
	 * @param contrast - pointer to a variable that receives the current contrast
	 *
	 * @return 0 on success, < 0 on error
	 */
	@HLEFunction(nid = 0xA063A957, version = 271)
	public void sceUsbCamGetContrast(Processor processor) {
		CpuState cpu = processor.cpu;

		int contrastAddr = cpu._a0;

		log.warn(String.format("Unimplemented sceUsbCamGetContrast 0x%08X", contrastAddr));

		Processor.memory.write32(contrastAddr, contrast);

		cpu._v0 = 0;
	}

	/**
	 * Gets the current sharpness
	 *
	 * @param brightness - pointer to a variable that receives the current sharpness
	 *
	 * @return 0 on success, < 0 on error
	 */
	@HLEFunction(nid = 0xFDB68C23, version = 271)
	public void sceUsbCamGetSharpness(Processor processor) {
		CpuState cpu = processor.cpu;

		int sharpnessAddr = cpu._a0;

		log.warn(String.format("Unimplemented sceUsbCamGetSharpness 0x%08X", sharpnessAddr));

		Processor.memory.write32(sharpnessAddr, sharpness);

		cpu._v0 = 0;
	}

	/**
	 * Gets the current image efect mode
	 *
	 * @param effectmode - pointer to a variable that receives the current effect mode
	 *
	 * @return 0 on success, < 0 on error
	 */
	@HLEFunction(nid = 0x994471E0, version = 271)
	public void sceUsbCamGetImageEffectMode(Processor processor) {
		CpuState cpu = processor.cpu;

		int effectModeAddr = cpu._a0;

		log.warn(String.format("Unimplemented sceUsbCamGetImageEffectMode 0x%08X", effectModeAddr));

		Processor.memory.write32(effectModeAddr, imageEffectMode);

		cpu._v0 = 0;
	}

	/**
	 * Gets the current exposure level.
	 *
	 * @param ev - pointer to a variable that receives the current exposure level
	 *
	 * @return 0 on success, < 0 on error
	 */
	@HLEFunction(nid = 0x2BCD50C0, version = 271)
	public void sceUsbCamGetEvLevel(Processor processor) {
		CpuState cpu = processor.cpu;

		int evLevelAddr = cpu._a0;

		log.warn(String.format("Unimplemented sceUsbCamGetEvLevel 0x%08X", evLevelAddr));

		Processor.memory.write32(evLevelAddr, evLevel);

		cpu._v0 = 0;
	}

	/**
	 * Gets the current reverse mode.
	 *
	 * @param reverseflags - pointer to a variable that receives the current reverse mode flags
	 *
	 * @return 0 on success, < 0 on error
	 */
	@HLEFunction(nid = 0xD5279339, version = 271)
	public void sceUsbCamGetReverseMode(Processor processor) {
		CpuState cpu = processor.cpu;

		int reverseModeAddr = cpu._a0;

		log.warn(String.format("Unimplemented sceUsbCamGetReverseMode 0x%08X", reverseModeAddr));

		int reverseMode = 0;
		if (mirror) {
			reverseMode |= PSP_USBCAM_MIRROR;
		}
		if (flip) {
			reverseMode |= PSP_USBCAM_FLIP;
		}

		Processor.memory.write32(reverseModeAddr, reverseMode);

		cpu._v0 = 0;
	}

	/**
	 * Gets the current zoom.
	 *
	 * @param zoom - pointer to a variable that receives the current zoom
	 *
	 * @return 0 on success, < 0 on error
	 */
	@HLEFunction(nid = 0x9E8AAF8D, version = 271)
	public void sceUsbCamGetZoom(Processor processor) {
		CpuState cpu = processor.cpu;

		int zoomAddr = cpu._a0;

		log.warn(String.format("Unimplemented sceUsbCamGetZoom 0x%08X", zoomAddr));

		Processor.memory.write32(zoomAddr, zoom);

		cpu._v0 = 0;
	}

	/**
	 * Gets the state of the autoreversal of the image.
	 *
	 * @return 1 if it is set to automatic, 0 otherwise
	 */
	@HLEFunction(nid = 0x11A1F128, version = 271)
	public void sceUsbCamGetAutoImageReverseState(Processor processor) {
		CpuState cpu = processor.cpu;

		log.warn(String.format("Unimplemented sceUsbCamGetAutoImageReverseState"));

		cpu._v0 = autoImageReverseSW ? 1 : 0;
	}

	@HLEFunction(nid = 0x08AEE98A, version = 271)
	public void sceUsbCamSetMicGain(Processor processor) {
		CpuState cpu = processor.cpu;

		log.warn(String.format("Unimplemented sceUsbCamSetMicGain"));

		cpu._v0 = 0;
	}

	@HLEFunction(nid = 0x2E930264, version = 271)
	public void sceUsbCamSetupMicEx(Processor processor) {
		CpuState cpu = processor.cpu;

		log.warn(String.format("Unimplemented sceUsbCamSetupMicEx"));

		cpu._v0 = 0;
	}

	@HLEFunction(nid = 0x36636925, version = 271)
	public int sceUsbCamReadMicBlocking(TPointer buffer, int bufferSize) {
		return Modules.sceAudioModule.hleAudioInputBlocking(bufferSize >> 1, micFrequency, buffer);
	}

	@HLEFunction(nid = 0x3DC0088E, version = 271)
	public void sceUsbCamReadMic(Processor processor) {
		CpuState cpu = processor.cpu;

		log.warn(String.format("Unimplemented sceUsbCamReadMic"));

		cpu._v0 = 0;
	}

	@HLEFunction(nid = 0x41EE8797, version = 271)
	public void sceUsbCamUnregisterLensRotationCallback(Processor processor) {
		CpuState cpu = processor.cpu;

		log.warn(String.format("Unimplemented sceUsbCamUnregisterLensRotationCallback"));

		cpu._v0 = 0;
	}

	@HLEFunction(nid = 0x5145868A, version = 271)
	public int sceUsbCamStopMic() {
		return 0;
	}

	@HLEFunction(nid = 0x5778B452, version = 271)
	public void sceUsbCamGetMicDataLength(Processor processor) {
		CpuState cpu = processor.cpu;

		log.warn(String.format("Unimplemented sceUsbCamGetMicDataLength"));

		cpu._v0 = 0;
	}

	@HLEFunction(nid = 0x6784E6A8, version = 271)
	public void sceUsbCamSetAntiFlicker(Processor processor) {
		CpuState cpu = processor.cpu;

		log.warn(String.format("Unimplemented sceUsbCamSetAntiFlicker"));

		cpu._v0 = 0;
	}

	@HLEFunction(nid = 0xAA7D94BA, version = 271)
	public void sceUsbCamGetAntiFlicker(Processor processor) {
		CpuState cpu = processor.cpu;

		log.warn(String.format("Unimplemented sceUsbCamGetAntiFlicker"));

		cpu._v0 = 0;
	}

	@HLEFunction(nid = 0xB048A67D, version = 271)
	public void sceUsbCamWaitReadMicEnd(Processor processor) {
		CpuState cpu = processor.cpu;

		log.warn(String.format("Unimplemented sceUsbCamWaitReadMicEnd"));

		cpu._v0 = 0;
	}

	@HLEFunction(nid = 0xD293A100, version = 271)
	public void sceUsbCamRegisterLensRotationCallback(Processor processor) {
		CpuState cpu = processor.cpu;

		log.warn(String.format("Unimplemented sceUsbCamRegisterLensRotationCallback"));

		cpu._v0 = 0;
	}

	@HLEFunction(nid = 0xF8847F60, version = 271)
	public void sceUsbCamPollReadMicEnd(Processor processor) {
		CpuState cpu = processor.cpu;

		log.warn(String.format("Unimplemented sceUsbCamPollReadMicEnd"));

		cpu._v0 = 0;
	}

}
