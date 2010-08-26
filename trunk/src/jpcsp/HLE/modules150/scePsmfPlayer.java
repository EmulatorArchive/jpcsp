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
package jpcsp.HLE.modules150;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

import jpcsp.Memory;
import jpcsp.Processor;
import jpcsp.Allegrex.CpuState;
import jpcsp.HLE.kernel.managers.IntrManager;
import jpcsp.HLE.kernel.types.SceKernelErrors;
import jpcsp.HLE.Modules;
import jpcsp.HLE.modules.HLEModule;
import jpcsp.HLE.modules.HLEModuleFunction;
import jpcsp.HLE.modules.HLEModuleManager;
import jpcsp.filesystems.SeekableDataInput;
import jpcsp.media.MediaEngine;
import jpcsp.media.PacketChannel;
import jpcsp.memory.IMemoryWriter;
import jpcsp.memory.MemoryWriter;
import jpcsp.util.Debug;
import jpcsp.util.Utilities;

import org.apache.log4j.Logger;

public class scePsmfPlayer implements HLEModule {

    private static Logger log = Modules.getLogger("scePsmfPlayer");

    @Override
    public String getName() {
        return "scePsmfPlayer";
    }

    @Override
    public void installModule(HLEModuleManager mm, int version) {
        if (version >= 150) {

            mm.addFunction(0x235D8787, scePsmfPlayerCreateFunction);
            mm.addFunction(0x9B71A274, scePsmfPlayerDeleteFunction);
            mm.addFunction(0x3D6D25A9, scePsmfPlayerSetPsmfFunction);
            mm.addFunction(0x58B83577, scePsmfPlayerSetPsmfCBFunction);
            mm.addFunction(0xE792CD94, scePsmfPlayerReleasePsmfFunction);
            mm.addFunction(0x95A84EE5, scePsmfPlayerStartFunction);
            mm.addFunction(0x3EA82A4B, scePsmfPlayerGetAudioOutSizeFunction);
            mm.addFunction(0x1078C008, scePsmfPlayerStopFunction);
            mm.addFunction(0xA0B8CA55, scePsmfPlayerUpdateFunction);
            mm.addFunction(0x46F61F8B, scePsmfPlayerGetVideoDataFunction);
            mm.addFunction(0xB9848A74, scePsmfPlayerGetAudioDataFunction);
            mm.addFunction(0xF8EF08A6, scePsmfPlayerGetCurrentStatusFunction);
            mm.addFunction(0xDF089680, scePsmfPlayerGetPsmfInfoFunction);
            mm.addFunction(0x1E57A8E7, scePsmfPlayerConfigPlayerFunction);
            mm.addFunction(0xA3D81169, scePsmfPlayerChangePlayModeFunction);
            mm.addFunction(0x68F07175, scePsmfPlayerGetCurrentAudioStreamFunction);
            mm.addFunction(0xF3EFAA91, scePsmfPlayerGetCurrentPlayModeFunction);
            mm.addFunction(0x3ED62233, scePsmfPlayerGetCurrentPtsFunction);
            mm.addFunction(0x9FF2B2E7, scePsmfPlayerGetCurrentVideoStreamFunction);
            mm.addFunction(0x2BEB1569, scePsmfPlayerBreakFunction);
            mm.addFunction(0x76C0F4AE, scePsmfPlayerSetPsmfOffsetFunction);
            mm.addFunction(0xA72DB4F9, scePsmfPlayerSetPsmfOffsetCBFunction);
            mm.addFunction(0x2D0E4E0A, scePsmfPlayerSetTempBufFunction);
            mm.addFunction(0x75F03FA2, scePsmfPlayerSelectSpecificVideoFunction);
            mm.addFunction(0x85461EFF, scePsmfPlayerSelectSpecificAudioFunction);
            mm.addFunction(0x8A9EBDCD, scePsmfPlayerSelectVideoFunction);
            mm.addFunction(0xB8D10C56, scePsmfPlayerSelectAudioFunction);

        }
    }

    @Override
    public void uninstallModule(HLEModuleManager mm, int version) {
        if (version >= 150) {

            mm.removeFunction(scePsmfPlayerCreateFunction);
            mm.removeFunction(scePsmfPlayerDeleteFunction);
            mm.removeFunction(scePsmfPlayerSetPsmfFunction);
            mm.removeFunction(scePsmfPlayerSetPsmfCBFunction);
            mm.removeFunction(scePsmfPlayerReleasePsmfFunction);
            mm.removeFunction(scePsmfPlayerStartFunction);
            mm.removeFunction(scePsmfPlayerGetAudioOutSizeFunction);
            mm.removeFunction(scePsmfPlayerStopFunction);
            mm.removeFunction(scePsmfPlayerUpdateFunction);
            mm.removeFunction(scePsmfPlayerGetVideoDataFunction);
            mm.removeFunction(scePsmfPlayerGetAudioDataFunction);
            mm.removeFunction(scePsmfPlayerGetCurrentStatusFunction);
            mm.removeFunction(scePsmfPlayerGetPsmfInfoFunction);
            mm.removeFunction(scePsmfPlayerConfigPlayerFunction);
            mm.removeFunction(scePsmfPlayerChangePlayModeFunction);
            mm.removeFunction(scePsmfPlayerGetCurrentAudioStreamFunction);
            mm.removeFunction(scePsmfPlayerGetCurrentPlayModeFunction);
            mm.removeFunction(scePsmfPlayerGetCurrentPtsFunction);
            mm.removeFunction(scePsmfPlayerGetCurrentVideoStreamFunction);
            mm.removeFunction(scePsmfPlayerBreakFunction);
            mm.removeFunction(scePsmfPlayerSetPsmfOffsetFunction);
            mm.removeFunction(scePsmfPlayerSetPsmfOffsetCBFunction);
            mm.removeFunction(scePsmfPlayerSetTempBufFunction);
            mm.removeFunction(scePsmfPlayerSelectSpecificVideoFunction);
            mm.removeFunction(scePsmfPlayerSelectSpecificAudioFunction);
            mm.removeFunction(scePsmfPlayerSelectVideoFunction);
            mm.removeFunction(scePsmfPlayerSelectAudioFunction);

        }
    }
    // PSMF Player timing management.
    protected static final int psmfPlayerVideoTimestampStep = 3003;
    protected static final int psmfPlayerAudioTimestampStep = 4180;
    protected static final int psmfTimestampPerSecond = 90000;
    protected static final int psmfMaxAheadTimestamp = 100000;

    // PSMF Player timestamp vars.
    protected Date psmfPlayerLastDate;
    protected long psmfPlayerLastTimestamp;
    protected int psmfPlayerAvcCurrentDecodingTimestamp;
    protected int psmfPlayerAtracCurrentDecodingTimestamp;
    protected int psmfPlayerAvcCurrentPresentationTimestamp;
    protected int psmfPlayerAtracCurrentPresentationTimestamp;

    // PSMF Player status.
    protected static final int PSMF_PLAYER_STATUS_NONE = 0x0;
    protected static final int PSMF_PLAYER_STATUS_INIT = 0x1;
    protected static final int PSMF_PLAYER_STATUS_STANDBY = 0x2;
    protected static final int PSMF_PLAYER_STATUS_PLAYING = 0x4;
    protected static final int PSMF_PLAYER_STATUS_ERROR = 0x100;
    protected static final int PSMF_PLAYER_STATUS_PLAYING_FINISHED = 0x200;

    // PSMF Player status vars.
    protected int psmfPlayerStatus;

    // PSMF Player mode.
    protected static final int PSMF_PLAYER_MODE_PLAY = 0;
    protected static final int PSMF_PLAYER_MODE_SLOWMOTION = 1;
    protected static final int PSMF_PLAYER_MODE_STEPFRAME = 2;
    protected static final int PSMF_PLAYER_MODE_PAUSE = 3;
    protected static final int PSMF_PLAYER_MODE_FORWARD = 4;
    protected static final int PSMF_PLAYER_MODE_REWIND = 5;

    // PSMF Player stream type.
    protected static final int PSMF_PLAYER_STREAM_AVC = 0;
    protected static final int PSMF_PLAYER_STREAM_ATRAC = 1;
    protected static final int PSMF_PLAYER_STREAM_PCM = 2;
    protected static final int PSMF_PLAYER_STREAM_VIDEO = 14;
    protected static final int PSMF_PLAYER_STREAM_AUDIO = 15;

    // PSMF Player playback speed.
    protected static final int PSMF_PLAYER_SPEED_SLOW = 1;
    protected static final int PSMF_PLAYER_SPEED_NORMAL = 2;
    protected static final int PSMF_PLAYER_SPEED_FAST = 3;

    // PSMF Player config mode.
    protected static final int PSMF_PLAYER_CONFIG_MODE_LOOP = 0;
    protected static final int PSMF_PLAYER_CONFIG_MODE_PIXEL_TYPE = 1;

    // PSMF Player config loop.
    protected static final int PSMF_PLAYER_CONFIG_LOOP = 0;
    protected static final int PSMF_PLAYER_CONFIG_NO_LOOP = 1;

    // PSMF Player config pixel type.
    protected static final int PSMF_PLAYER_PIXEL_TYPE_NONE = -1;
    protected static final int PSMF_PLAYER_PIXEL_TYPE_565 = 0;
    protected static final int PSMF_PLAYER_PIXEL_TYPE_5551 = 1;
    protected static final int PSMF_PLAYER_PIXEL_TYPE_4444 = 2;
    protected static final int PSMF_PLAYER_PIXEL_TYPE_8888 = 3;

    // PSMF Player version.
    protected static final int PSMF_PLAYER_VERSION_FULL = 0;
    protected static final int PSMF_PLAYER_VERSION_BASIC = 1;
    protected static final int PSMF_PLAYER_VERSION_NET = 2;

    // PMF file vars.
    protected String pmfFilePath;
    protected byte[] pmfFileData;

    // PMSF info.
    // TODO: Parse the right values from the PSMF file.
    protected int psmfCurrentPts = 0;
    protected int psmfAvcStreamNum = 1;
    protected int psmfAtracStreamNum = 1;
    protected int psmfPcmStreamNum = 0;
    protected int psmfPlayerVersion = PSMF_PLAYER_VERSION_FULL;

    // PSMF Player playback params.
    protected int displayBuffer;
    protected int displayBufferSize;
    protected int playbackThreadPriority;

    // PSMF Player playback info.
    protected int videoCodec;
    protected int videoStreamNum;
    protected int audioCodec;
    protected int audioStreamNum;
    protected int playMode;
    protected int playSpeed;

    // PSMF Player video data.
    protected int videoDataFrameWidth = 512;  // Default.
    protected int videoDataDisplayBuffer;
    protected int videoDataDisplayPts;

    // PSMF Player config.
    protected int videoPixelMode = PSMF_PLAYER_PIXEL_TYPE_8888;  // Default.
    protected int videoLoopStatus = PSMF_PLAYER_CONFIG_NO_LOOP;  // Default.

    // PSMF Player audio size
    protected int audioSize = 2048;  // Default.

    // Media Engine vars.
    protected PacketChannel pmfFileChannel;
    protected MediaEngine me;

    private boolean checkMediaEngineState() {
        return sceMpeg.isEnableMediaEngine();
    }

    protected Date convertPsmfTimestampToDate(long timestamp) {
        long millis = timestamp / (psmfTimestampPerSecond / 1000);
        return new Date(millis);
    }

    private void writePSMFVideoImage(int dest_addr, int frameWidth) {
        final int bytesPerPixel = sceDisplay.getPixelFormatBytes(videoPixelMode);
        int width = Math.min(480, frameWidth);
        int height = 272;

        // Get the current generated image, convert it to pixels and write it
        // to memory.
        if (me != null && me.getCurrentImg() != null) {
            // Override the base dimensions with the image's real dimensions.
            width = me.getCurrentImg().getWidth();
            height = me.getCurrentImg().getHeight();

            for (int y = 0; y < height; y++) {
                int address = dest_addr + y * frameWidth * bytesPerPixel;
                IMemoryWriter memoryWriter = MemoryWriter.getMemoryWriter(address, bytesPerPixel);

                for (int x = 0; x < width; x++) {
                    int colorARGB = me.getCurrentImg().getRGB(x, y);
                    // Convert from ARGB to ABGR.
                    int a = (colorARGB >>> 24) & 0xFF;
                    int r = (colorARGB >>> 16) & 0xFF;
                    int g = (colorARGB >>> 8) & 0xFF;
                    int b = colorARGB & 0xFF;
                    int colorABGR = a << 24 | b << 16 | g << 8 | r;

                    int pixelColor = Debug.getPixelColor(colorABGR, videoPixelMode);
                    memoryWriter.writeNext(pixelColor);
                }
            }
        }
    }

    private void generateFakePSMFVideo(int dest_addr, int frameWidth) {
        Memory mem = Memory.getInstance();

        Random random = new Random();
        final int pixelSize = 3;
        final int bytesPerPixel = sceDisplay.getPixelFormatBytes(videoPixelMode);
        for (int y = 0; y < 272 - pixelSize + 1; y += pixelSize) {
            int address = dest_addr + y * frameWidth * bytesPerPixel;
            final int width = Math.min(480, frameWidth);
            for (int x = 0; x < width; x += pixelSize) {
                int n = random.nextInt(256);
                int color = 0xFF000000 | (n << 16) | (n << 8) | n;
                int pixelColor = Debug.getPixelColor(color, videoPixelMode);
                if (bytesPerPixel == 4) {
                    for (int i = 0; i < pixelSize; i++) {
                        for (int j = 0; j < pixelSize; j++) {
                            mem.write32(address + (i * frameWidth + j) * 4, pixelColor);
                        }
                    }
                } else if (bytesPerPixel == 2) {
                    for (int i = 0; i < pixelSize; i++) {
                        for (int j = 0; j < pixelSize; j++) {
                            mem.write16(address + (i * frameWidth + j) * 2, (short) pixelColor);
                        }
                    }
                }
                address += pixelSize * bytesPerPixel;
            }
        }

        Date currentDate = convertPsmfTimestampToDate(psmfPlayerAvcCurrentDecodingTimestamp);
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        Debug.printFramebuffer(dest_addr, frameWidth, 10, 250, 0xFFFFFFFF, 0xFF000000, videoPixelMode, 1, " This is a faked PSMF Player video. ");

        String displayedString;
        if (psmfPlayerLastDate != null) {
            displayedString = String.format(" %s / %s ", dateFormat.format(currentDate), dateFormat.format(psmfPlayerLastDate));
            Debug.printFramebuffer(dest_addr, frameWidth, 10, 10, 0xFFFFFFFF, 0xFF000000, videoPixelMode, 2, displayedString);
        }
    }

    protected void analyzePSMFLastTimestamp() {
        if (pmfFileData != null) {
            // Endian swapped inside the buffer.
            psmfPlayerLastTimestamp = ((pmfFileData[92] << 24) | (pmfFileData[93] << 16) | (pmfFileData[94] << 8) | (pmfFileData[95]));
            psmfPlayerLastDate = convertPsmfTimestampToDate(psmfPlayerLastTimestamp);
        }
    }

    public void scePsmfPlayerCreate(Processor processor) {
        CpuState cpu = processor.cpu;
        Memory mem = Processor.memory;

        int psmfPlayer = cpu.gpr[4];
        int psmfPlayerDataAddr = cpu.gpr[5];

        log.warn("PARTIAL: scePsmfPlayerCreate psmfPlayer=0x" + Integer.toHexString(psmfPlayer)
                + " psmfPlayerDataAddr=0x" + Integer.toHexString(psmfPlayerDataAddr));

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        // The psmfDataAddr contains three fields that are manually set before
        // scePsmfPlayerCreate is called.
        displayBuffer = mem.read32(psmfPlayerDataAddr);                   // The buffer allocated for scePsmf, which is ported into scePsmfPlayer.
        displayBufferSize = mem.read32(psmfPlayerDataAddr + 4);           // The buffer's size.
        playbackThreadPriority = mem.read32(psmfPlayerDataAddr + 8);      // Priority of the "START" thread.
        log.info("PSMF Player Data: displayBuffer=0x" + Integer.toHexString(displayBuffer)
                + ", displayBufferSize=0x" + Integer.toHexString(displayBufferSize)
                + ", playbackThreadPriority=0x" + Integer.toHexString(playbackThreadPriority));

        // Start with INIT.
        psmfPlayerStatus = PSMF_PLAYER_STATUS_INIT;

        cpu.gpr[2] = 0;
    }

    public void scePsmfPlayerDelete(Processor processor) {
        CpuState cpu = processor.cpu;

        int psmfPlayer = cpu.gpr[4];

        log.warn("PARTIAL: scePsmfPlayerDelete psmfPlayer=0x" + Integer.toHexString(psmfPlayer));

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        if (checkMediaEngineState()) {
            if (me != null) {
                me.finish();
            }
            if (pmfFileChannel != null) {
                pmfFileChannel.flush();
            }
        }

        // Set to NONE.
        psmfPlayerStatus = PSMF_PLAYER_STATUS_NONE;

        cpu.gpr[2] = 0;
    }

    public void scePsmfPlayerSetPsmf(Processor processor) {
        CpuState cpu = processor.cpu;

        int psmfPlayer = cpu.gpr[4];
        int fileAddr = cpu.gpr[5];  // PMF file path.

        log.warn("PARTIAL: scePsmfPlayerSetPsmf psmfPlayer=0x" + Integer.toHexString(psmfPlayer) + " fileAddr=0x" + Integer.toHexString(fileAddr));

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        pmfFilePath = Utilities.readStringZ(fileAddr);

        //Get the file and read it to a buffer.
        try {
            SeekableDataInput psmfFile = Modules.IoFileMgrForUserModule.getFile(pmfFilePath, 0);
            pmfFileData = new byte[(int) psmfFile.length()];
            psmfFile.readFully(pmfFileData);

            log.info("'" + pmfFilePath + "' PSMF file loaded.");

            if (checkMediaEngineState()) {
                pmfFileChannel = new PacketChannel();
                pmfFileChannel.writeFile(pmfFileData);
            }
        } catch (Exception e) {
            //TODO
        }

        // Switch to STANDBY.
        psmfPlayerStatus = PSMF_PLAYER_STATUS_STANDBY;

        cpu.gpr[2] = 0;
    }

    public void scePsmfPlayerSetPsmfCB(Processor processor) {
        log.warn("scePsmfPlayerSetPsmfCB redirecting to scePsmfPlayerSetPsmf");
        scePsmfPlayerSetPsmf(processor);
        Modules.ThreadManForUserModule.hleRescheduleCurrentThread(true);
    }

    public void scePsmfPlayerReleasePsmf(Processor processor) {
        CpuState cpu = processor.cpu;

        int psmfPlayer = cpu.gpr[4];

        log.warn("PARTIAL: scePsmfPlayerReleasePsmf psmfPlayer=0x" + Integer.toHexString(psmfPlayer));

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        if (checkMediaEngineState()) {
            if (me != null) {
                me.finish();
            }
            if (pmfFileChannel != null) {
                pmfFileChannel.flush();
            }
        }

        // Go back to INIT, because some applications recognize that another file can be
        // loaded after scePsmfPlayerReleasePsmf has been called.
        psmfPlayerStatus = PSMF_PLAYER_STATUS_INIT;

        cpu.gpr[2] = 0;
    }

    public void scePsmfPlayerStart(Processor processor) {
        CpuState cpu = processor.cpu;
        Memory mem = Processor.memory;

        int psmfPlayer = cpu.gpr[4];
        int initPlayInfoAddr = cpu.gpr[5];
        int initPts = cpu.gpr[6];

        log.warn("PARTIAL: scePsmfPlayerStart psmfPlayer=0x" + Integer.toHexString(psmfPlayer)
                + " initPlayInfoAddr=0x" + Integer.toHexString(initPlayInfoAddr)
                + " initPts=" + initPts);

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        // Read the playback parameters.
        if(mem.isAddressGood(initPlayInfoAddr)) {
            videoCodec = mem.read32(initPlayInfoAddr);
            videoStreamNum = mem.read32(initPlayInfoAddr + 4);
            audioCodec = mem.read32(initPlayInfoAddr + 8);
            audioStreamNum = mem.read32(initPlayInfoAddr + 12);
            playMode = mem.read32(initPlayInfoAddr + 16);
            playSpeed = mem.read32(initPlayInfoAddr + 20);

             log.info("Found play info data: videoCodec=0x" + Integer.toHexString(videoCodec)
                + ", videoStreamNum=" + videoStreamNum
                + ", audioCodec=0x" + Integer.toHexString(audioCodec)
                + ", audioStreamNum=" + audioStreamNum
                + ", playMode=" + playMode
                + ", playSpeed=" + playSpeed);
        }

        // Initialize the current PTS and DTS with the given timestamp (mostly set to 0).
        psmfPlayerAvcCurrentDecodingTimestamp = initPts;
        psmfPlayerAtracCurrentDecodingTimestamp = initPts;
        psmfPlayerAvcCurrentPresentationTimestamp = initPts;
        psmfPlayerAtracCurrentPresentationTimestamp = initPts;

        analyzePSMFLastTimestamp();

        if (checkMediaEngineState()) {
            if (pmfFileChannel != null) {
                me = new MediaEngine();
                me.init(pmfFileChannel.getFilePath());
            }
        }

        // Switch to PLAYING.
        psmfPlayerStatus = PSMF_PLAYER_STATUS_PLAYING;

        cpu.gpr[2] = 0;
    }

    public void scePsmfPlayerGetAudioOutSize(Processor processor) {
        CpuState cpu = processor.cpu;

        int psmfPlayer = cpu.gpr[4];

        log.warn("PARTIAL: scePsmfPlayerGetAudioOutSize psmfPlayer=0x" + Integer.toHexString(psmfPlayer));

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        cpu.gpr[2] = audioSize;
    }

    public void scePsmfPlayerStop(Processor processor) {
        CpuState cpu = processor.cpu;

        int psmfPlayer = cpu.gpr[4];

        log.warn("PARTIAL: scePsmfPlayerStop psmfPlayer=0x" + Integer.toHexString(psmfPlayer));

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        if (checkMediaEngineState()) {
            if (me != null) {
                me.finish();
            }
            if (pmfFileChannel != null) {
                pmfFileChannel.flush();
            }
        }

        // Always switch to STANDBY, because this PSMF can still be resumed.
        psmfPlayerStatus = PSMF_PLAYER_STATUS_STANDBY;

        cpu.gpr[2] = 0;
    }

    public void scePsmfPlayerUpdate(Processor processor) {
        CpuState cpu = processor.cpu;

        int psmfPlayer = cpu.gpr[4];

        // Can be called from interrupt.
        log.warn("PARTIAL: scePsmfPlayerUpdate psmfPlayer=0x" + Integer.toHexString(psmfPlayer));

        // Update video presentation timestamp.
        psmfPlayerAvcCurrentPresentationTimestamp = psmfPlayerAvcCurrentDecodingTimestamp;

        // Check playback status.
        if (psmfPlayerAvcCurrentDecodingTimestamp > 0) {
            if (psmfPlayerAvcCurrentDecodingTimestamp > psmfPlayerLastTimestamp) {
                // If we've reached the last timestamp, change the status to PLAYING_FINISHED.
                psmfPlayerStatus = PSMF_PLAYER_STATUS_PLAYING_FINISHED;
            }
        }

        cpu.gpr[2] = 0;
    }

    public void scePsmfPlayerGetVideoData(Processor processor) {
        CpuState cpu = processor.cpu;
        Memory mem = Memory.getInstance();

        int psmfPlayer = cpu.gpr[4];
        int videoDataAddr = cpu.gpr[5];

        log.warn("PARTIAL: scePsmfPlayerGetVideoData psmfPlayer=0x" + Integer.toHexString(psmfPlayer) + " videoDataAddr=0x" + Integer.toHexString(videoDataAddr));

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        if (mem.isAddressGood(videoDataAddr)) {
            videoDataFrameWidth = mem.read32(videoDataAddr);
            videoDataDisplayBuffer = mem.read32(videoDataAddr + 4);
            videoDataDisplayPts = mem.read32(videoDataAddr + 8);
        }
        // Check if there's already a valid pointer at videoDataAddr.
        // If not, use the displayBuffer from scePsmfPlayerCreate.
        if (mem.isAddressGood(videoDataDisplayBuffer)) {
            displayBuffer = videoDataDisplayBuffer;
        } else {
            mem.write32(videoDataAddr + 4, displayBuffer);
        }

        // Write video data.
        if (checkMediaEngineState()) {
            if (me != null) {
                if (me.getContainer() != null) {
                    me.step();
                    writePSMFVideoImage(displayBuffer, videoDataFrameWidth);
                    psmfPlayerLastTimestamp = me.getPacketTimestamp("Video", "DTS");
                }
            }
        } else {
            generateFakePSMFVideo(displayBuffer, videoDataFrameWidth);
        }

        // Update video decoding timestamp.
        psmfPlayerAvcCurrentDecodingTimestamp += psmfPlayerVideoTimestampStep;

        if (psmfPlayerAvcCurrentDecodingTimestamp > psmfPlayerAtracCurrentDecodingTimestamp + psmfMaxAheadTimestamp) {
            // If we're ahead of audio, return an error.
            cpu.gpr[2] = 0x8061600c;  // No more data (actual name unknown).
        } else {
            cpu.gpr[2] = 0;
        }
    }

    public void scePsmfPlayerGetAudioData(Processor processor) {
        CpuState cpu = processor.cpu;
        Memory mem = Processor.memory;

        int psmfPlayer = cpu.gpr[4];
        int audioDataAddr = cpu.gpr[5];

        log.warn("PARTIAL: scePsmfPlayerGetAudioData psmfPlayer=0x" + Integer.toHexString(psmfPlayer) + " audioDataAddr=0x" + Integer.toHexString(audioDataAddr));

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        // Set empty data.
        if(mem.isAddressGood(audioDataAddr)) {
            mem.memset(audioDataAddr, (byte) 0, audioSize);
        }
        // Update audio timestamp.
        psmfPlayerAtracCurrentDecodingTimestamp += psmfPlayerAudioTimestampStep;

        if (psmfPlayerAtracCurrentDecodingTimestamp > psmfPlayerAvcCurrentDecodingTimestamp + psmfMaxAheadTimestamp) {
            // If we're ahead of video, return an error.
            cpu.gpr[2] = 0x8061600c;  // No more data (actual name unknown).
        } else {
            cpu.gpr[2] = 0;
        }
    }

    public void scePsmfPlayerGetCurrentStatus(Processor processor) {
        CpuState cpu = processor.cpu;

        int psmfPlayer = cpu.gpr[4];

        log.warn("PARTIAL: scePsmfPlayerGetCurrentStatus psmfPlayer=0x" + Integer.toHexString(psmfPlayer));

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        cpu.gpr[2] = psmfPlayerStatus;
    }

    public void scePsmfPlayerGetPsmfInfo(Processor processor) {
        CpuState cpu = processor.cpu;
        Memory mem = Processor.memory;

        int psmfPlayer = cpu.gpr[4];
        int psmfInfoAddr = cpu.gpr[5];

        log.warn("PARTIAL: scePsmfPlayerGetPsmfInfo psmfPlayer=0x" + Integer.toHexString(psmfPlayer) + " psmfInfoAddr=0x" + Integer.toHexString(psmfInfoAddr));

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        if(mem.isAddressGood(psmfInfoAddr)) {
            mem.write32(psmfInfoAddr, psmfCurrentPts);
            mem.write32(psmfInfoAddr + 4, psmfAvcStreamNum);
            mem.write32(psmfInfoAddr + 8, psmfAtracStreamNum);
            mem.write32(psmfInfoAddr + 12, psmfPcmStreamNum);
            mem.write32(psmfInfoAddr + 16, psmfPlayerVersion);
        }
        cpu.gpr[2] = 0;
    }

    public void scePsmfPlayerConfigPlayer(Processor processor) {
        CpuState cpu = processor.cpu;

        int psmfPlayer = cpu.gpr[4];
        int configMode = cpu.gpr[5];
        int configAttr = cpu.gpr[6];

        log.warn("PARTIAL: scePsmfPlayerConfigPlayer psmfPlayer=0x" + Integer.toHexString(psmfPlayer)
                + " configMode=" + configMode + " configAttr=" + configAttr);

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        if (configMode == PSMF_PLAYER_CONFIG_MODE_LOOP) {                 // Sets if the video is looped or not.
            videoLoopStatus = configAttr;
        } else if (configMode == PSMF_PLAYER_CONFIG_MODE_PIXEL_TYPE) {    // Sets the display's pixel type.
            videoPixelMode = configAttr;
        } else {
            log.warn("scePsmfPlayerConfigPlayer unknown config mode.");
        }
        cpu.gpr[2] = 0;
    }

    public void scePsmfPlayerChangePlayMode(Processor processor) {
        CpuState cpu = processor.cpu;

        int psmfPlayer = cpu.gpr[4];
        int newPlayMode = cpu.gpr[5];
        int newPlaySpeed = cpu.gpr[6];

        log.warn("PARTIAL: scePsmfPlayerChangePlayMode psmfPlayer=0x" + Integer.toHexString(psmfPlayer)
                + ", newPlayMode=" + newPlayMode + ", newPlaySpeed=" + newPlaySpeed);

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        playMode = newPlayMode;
        playSpeed = newPlaySpeed;
        cpu.gpr[2] = 0;
    }

    public void scePsmfPlayerGetCurrentAudioStream(Processor processor) {
        CpuState cpu = processor.cpu;
        Memory mem = Memory.getInstance();

        int psmfPlayer = cpu.gpr[4];
        int audioCodecAddr = cpu.gpr[5];
        int audioStreamNumAddr = cpu.gpr[6];

        log.warn("PARTIAL: scePsmfPlayerGetCurrentAudioStream psmfPlayer=0x" + Integer.toHexString(psmfPlayer)
                + ", audioCodecAddr=0x" + Integer.toHexString(audioCodecAddr)
                + ", audioStreamNumAddr=0x" + Integer.toHexString(audioStreamNumAddr));

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        if(mem.isAddressGood(audioCodecAddr)) {
            mem.write32(audioCodecAddr, audioCodec);
        }
        if(mem.isAddressGood(audioStreamNumAddr)) {
            mem.write32(audioStreamNumAddr, audioStreamNum);
        }
        cpu.gpr[2] = 0;
    }

    public void scePsmfPlayerGetCurrentPlayMode(Processor processor) {
        CpuState cpu = processor.cpu;
        Memory mem = Memory.getInstance();

        int psmfPlayer = cpu.gpr[4];
        int playModeAddr = cpu.gpr[5];
        int playSpeedAddr = cpu.gpr[6];

        log.warn("PARTIAL: scePsmfPlayerGetCurrentPlayMode psmfplayer=0x" + Integer.toHexString(psmfPlayer)
                + ", playModeAddr=0x" + Integer.toHexString(playModeAddr)
                + ", playSpeedAddr=0x" + Integer.toHexString(playSpeedAddr));

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        if(mem.isAddressGood(playModeAddr)) {
            mem.write32(playModeAddr, playMode);
        }
        if(mem.isAddressGood(playSpeedAddr)) {
            mem.write32(playSpeedAddr, playSpeed);
        }
        cpu.gpr[2] = 0;
    }

    public void scePsmfPlayerGetCurrentPts(Processor processor) {
        CpuState cpu = processor.cpu;
        Memory mem = Memory.getInstance();

        int psmfPlayer = cpu.gpr[4];
        int currentPtsAddr = cpu.gpr[5];

        log.warn("PARTIAL: scePsmfPlayerGetCurrentPts psmfPlayer=0x" + Integer.toHexString(psmfPlayer)
                + ", currentPtsAddr=0x" + Integer.toHexString(currentPtsAddr));

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        // Write our current video presentation timestamp.
        if(mem.isAddressGood(currentPtsAddr)) {
            mem.write32(currentPtsAddr, psmfPlayerAvcCurrentPresentationTimestamp);
        }
        cpu.gpr[2] = 0;
    }

    public void scePsmfPlayerGetCurrentVideoStream(Processor processor) {
        CpuState cpu = processor.cpu;
        Memory mem = Memory.getInstance();

        int psmfPlayer = cpu.gpr[4];
        int videoCodecAddr = cpu.gpr[5];
        int videoStreamNumAddr = cpu.gpr[6];

        log.warn("PARTIAL: scePsmfPlayerGetCurrentVideoStream psmfPlayer=0x" + Integer.toHexString(psmfPlayer)
                + ", videoCodecAddr=0x" + Integer.toHexString(videoCodecAddr)
                + ", videoStreamNumAddr=0x" + Integer.toHexString(videoStreamNumAddr));

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        if(mem.isAddressGood(videoCodecAddr)) {
            mem.write32(videoCodecAddr, videoCodec);
        }
        if(mem.isAddressGood(videoStreamNumAddr)) {
            mem.write32(videoStreamNumAddr, videoStreamNum);
        }
        cpu.gpr[2] = 0;
    }

    public void scePsmfPlayerBreak(Processor processor) {
        CpuState cpu = processor.cpu;

        int psmfPlayer = cpu.gpr[4];

        // Can be called from interrupt.
        log.warn("IGNORING: scePsmfPlayerBreak psmfPlayer=0x" + Integer.toHexString(psmfPlayer));

        cpu.gpr[2] = 0;
    }

    public void scePsmfPlayerSetPsmfOffset(Processor processor) {
        log.warn("scePsmfPlayerSetPsmfOffset redirecting to scePsmfPlayerSetPsmf");
        scePsmfPlayerSetPsmf(processor);
    }

    public void scePsmfPlayerSetPsmfOffsetCB(Processor processor) {
        log.warn("scePsmfPlayerSetPsmfOffsetCB redirecting to scePsmfPlayerSetPsmfCB");
        scePsmfPlayerSetPsmfCB(processor);
    }

    public void scePsmfPlayerSetTempBuf(Processor processor) {
        CpuState cpu = processor.cpu;

        int psmfPlayer = cpu.gpr[4];

        log.warn("IGNORING: scePsmfPlayerSetTempBuf psmfPlayer=0x" + Integer.toHexString(psmfPlayer));

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        cpu.gpr[2] = 0;
    }

    public void scePsmfPlayerSelectSpecificVideo(Processor processor) {
        CpuState cpu = processor.cpu;

        int psmfPlayer = cpu.gpr[4];
        int newVideoCodec = cpu.gpr[5];
        int newVideoStreamNum = cpu.gpr[6];

        log.warn("PARTIAL: scePsmfPlayerSelectSpecificVideo psmfPlayer=0x" + Integer.toHexString(psmfPlayer)
                + ", newVideoCodec=0x" + Integer.toHexString(newVideoCodec)
                + ", newVideoStreamNum=" + newVideoStreamNum);

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        videoCodec = newVideoCodec;
        videoStreamNum = newVideoStreamNum;
        cpu.gpr[2] = 0;
    }

    public void scePsmfPlayerSelectSpecificAudio(Processor processor) {
        CpuState cpu = processor.cpu;

        int psmfPlayer = cpu.gpr[4];
        int newAudioCodec = cpu.gpr[5];
        int newAudioStreamNum = cpu.gpr[6];

        log.warn("PARTIAL: scePsmfPlayerSelectSpecificVideo psmfPlayer=0x" + Integer.toHexString(psmfPlayer)
                + ", newAudioCodec=0x" + Integer.toHexString(newAudioCodec)
                + ", newAudioStreamNum=" + newAudioStreamNum);

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        audioCodec = newAudioCodec;
        audioStreamNum = newAudioStreamNum;
        cpu.gpr[2] = 0;
    }

    public void scePsmfPlayerSelectVideo(Processor processor) {
        CpuState cpu = processor.cpu;

        int psmfPlayer = cpu.gpr[4];

        log.warn("PARTIAL: scePsmfPlayerSelectVideo psmfPlayer=0x" + Integer.toHexString(psmfPlayer));

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        // Advances to the next video stream number.
        videoStreamNum++;
        cpu.gpr[2] = 0;
    }

    public void scePsmfPlayerSelectAudio(Processor processor) {
        CpuState cpu = processor.cpu;

        int psmfPlayer = cpu.gpr[4];

        log.warn("PARTIAL: scePsmfPlayerSelectAudio psmfPlayer=0x" + Integer.toHexString(psmfPlayer));

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        // Advances to the next audio stream number.
        audioStreamNum++;
        cpu.gpr[2] = 0;
    }

    public final HLEModuleFunction scePsmfPlayerCreateFunction = new HLEModuleFunction("scePsmfPlayer", "scePsmfPlayerCreate") {

        @Override
        public final void execute(Processor processor) {
            scePsmfPlayerCreate(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.scePsmfPlayer.scePsmfPlayerCreate(processor);";
        }
    };
    public final HLEModuleFunction scePsmfPlayerDeleteFunction = new HLEModuleFunction("scePsmfPlayer", "scePsmfPlayerDelete") {

        @Override
        public final void execute(Processor processor) {
            scePsmfPlayerDelete(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.scePsmfPlayer.scePsmfPlayerDelete(processor);";
        }
    };
    public final HLEModuleFunction scePsmfPlayerSetPsmfFunction = new HLEModuleFunction("scePsmfPlayer", "scePsmfPlayerSetPsmf") {

        @Override
        public final void execute(Processor processor) {
            scePsmfPlayerSetPsmf(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.scePsmfPlayer.scePsmfPlayerSetPsmf(processor);";
        }
    };
    public final HLEModuleFunction scePsmfPlayerSetPsmfCBFunction = new HLEModuleFunction("scePsmfPlayer", "scePsmfPlayerSetPsmfCB") {

        @Override
        public final void execute(Processor processor) {
            scePsmfPlayerSetPsmfCB(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.scePsmfPlayer.scePsmfPlayerSetPsmfCB(processor);";
        }
    };
    public final HLEModuleFunction scePsmfPlayerReleasePsmfFunction = new HLEModuleFunction("scePsmfPlayer", "scePsmfPlayerReleasePsmf") {

        @Override
        public final void execute(Processor processor) {
            scePsmfPlayerReleasePsmf(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.scePsmfPlayer.scePsmfPlayerReleasePsmf(processor);";
        }
    };
    public final HLEModuleFunction scePsmfPlayerStartFunction = new HLEModuleFunction("scePsmfPlayer", "scePsmfPlayerStart") {

        @Override
        public final void execute(Processor processor) {
            scePsmfPlayerStart(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.scePsmfPlayer.scePsmfPlayerStart(processor);";
        }
    };
    public final HLEModuleFunction scePsmfPlayerGetAudioOutSizeFunction = new HLEModuleFunction("scePsmfPlayer", "scePsmfPlayerGetAudioOutSize") {

        @Override
        public final void execute(Processor processor) {
            scePsmfPlayerGetAudioOutSize(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.scePsmfPlayer.scePsmfPlayerGetAudioOutSize(processor);";
        }
    };
    public final HLEModuleFunction scePsmfPlayerStopFunction = new HLEModuleFunction("scePsmfPlayer", "scePsmfPlayerStop") {

        @Override
        public final void execute(Processor processor) {
            scePsmfPlayerStop(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.scePsmfPlayer.scePsmfPlayerStop(processor);";
        }
    };
    public final HLEModuleFunction scePsmfPlayerUpdateFunction = new HLEModuleFunction("scePsmfPlayer", "scePsmfPlayerUpdate") {

        @Override
        public final void execute(Processor processor) {
            scePsmfPlayerUpdate(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.scePsmfPlayer.scePsmfPlayerUpdate(processor);";
        }
    };
    public final HLEModuleFunction scePsmfPlayerGetVideoDataFunction = new HLEModuleFunction("scePsmfPlayer", "scePsmfPlayerGetVideoData") {

        @Override
        public final void execute(Processor processor) {
            scePsmfPlayerGetVideoData(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.scePsmfPlayer.scePsmfPlayerGetVideoData(processor);";
        }
    };
    public final HLEModuleFunction scePsmfPlayerGetAudioDataFunction = new HLEModuleFunction("scePsmfPlayer", "scePsmfPlayerGetAudioData") {

        @Override
        public final void execute(Processor processor) {
            scePsmfPlayerGetAudioData(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.scePsmfPlayer.scePsmfPlayerGetAudioData(processor);";
        }
    };
    public final HLEModuleFunction scePsmfPlayerGetCurrentStatusFunction = new HLEModuleFunction("scePsmfPlayer", "scePsmfPlayerGetCurrentStatus") {

        @Override
        public final void execute(Processor processor) {
            scePsmfPlayerGetCurrentStatus(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.scePsmfPlayer.scePsmfPlayerGetCurrentStatus(processor);";
        }
    };
    public final HLEModuleFunction scePsmfPlayerGetPsmfInfoFunction = new HLEModuleFunction("scePsmfPlayer", "scePsmfPlayerGetPsmfInfo") {

        @Override
        public final void execute(Processor processor) {
            scePsmfPlayerGetPsmfInfo(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.scePsmfPlayer.scePsmfPlayerGetPsmfInfo(processor);";
        }
    };
    public final HLEModuleFunction scePsmfPlayerConfigPlayerFunction = new HLEModuleFunction("scePsmfPlayer", "scePsmfPlayerConfigPlayer") {

        @Override
        public final void execute(Processor processor) {
            scePsmfPlayerConfigPlayer(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.scePsmfPlayerConfigPlayer(processor);";
        }
    };
    public final HLEModuleFunction scePsmfPlayerChangePlayModeFunction = new HLEModuleFunction("scePsmfPlayer", "scePsmfPlayerChangePlayMode") {

        @Override
        public final void execute(Processor processor) {
            scePsmfPlayerChangePlayMode(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.scePsmfPlayerChangePlayMode(processor);";
        }
    };
    public final HLEModuleFunction scePsmfPlayerGetCurrentAudioStreamFunction = new HLEModuleFunction("scePsmfPlayer", "scePsmfPlayerGetCurrentAudioStream") {

        @Override
        public final void execute(Processor processor) {
            scePsmfPlayerGetCurrentAudioStream(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.scePsmfPlayerGetCurrentAudioStream(processor);";
        }
    };
    public final HLEModuleFunction scePsmfPlayerGetCurrentPlayModeFunction = new HLEModuleFunction("scePsmfPlayer", "scePsmfPlayerGetCurrentPlayMode") {

        @Override
        public final void execute(Processor processor) {
            scePsmfPlayerGetCurrentPlayMode(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.scePsmfPlayerGetCurrentPlayMode(processor);";
        }
    };
    public final HLEModuleFunction scePsmfPlayerGetCurrentPtsFunction = new HLEModuleFunction("scePsmfPlayer", "scePsmfPlayerGetCurrentPts") {

        @Override
        public final void execute(Processor processor) {
            scePsmfPlayerGetCurrentPts(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.scePsmfPlayerGetCurrentPts(processor);";
        }
    };
    public final HLEModuleFunction scePsmfPlayerGetCurrentVideoStreamFunction = new HLEModuleFunction("scePsmfPlayer", "scePsmfPlayerGetCurrentVideoStream") {

        @Override
        public final void execute(Processor processor) {
            scePsmfPlayerGetCurrentVideoStream(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.scePsmfPlayerGetCurrentVideoStream(processor);";
        }
    };
    public final HLEModuleFunction scePsmfPlayerBreakFunction = new HLEModuleFunction("scePsmfPlayer", "scePsmfPlayerBreak") {

        @Override
        public final void execute(Processor processor) {
            scePsmfPlayerBreak(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.scePsmfPlayer.scePsmfPlayerBreak(processor);";
        }
    };
    public final HLEModuleFunction scePsmfPlayerSetPsmfOffsetFunction = new HLEModuleFunction("scePsmfPlayer", "scePsmfPlayerSetPsmfOffset") {

        @Override
        public final void execute(Processor processor) {
            scePsmfPlayerSetPsmfOffset(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.scePsmfPlayer.scePsmfPlayerSetPsmfOffset(processor);";
        }
    };
    public final HLEModuleFunction scePsmfPlayerSetPsmfOffsetCBFunction = new HLEModuleFunction("scePsmfPlayer", "scePsmfPlayerSetPsmfOffsetCB") {

        @Override
        public final void execute(Processor processor) {
            scePsmfPlayerSetPsmfOffsetCB(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.scePsmfPlayer.scePsmfPlayerSetPsmfOffsetCB(processor);";
        }
    };
    public final HLEModuleFunction scePsmfPlayerSetTempBufFunction = new HLEModuleFunction("scePsmfPlayer", "scePsmfPlayerSetTempBuf") {

        @Override
        public final void execute(Processor processor) {
            scePsmfPlayerSetTempBuf(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.scePsmfPlayer.scePsmfPlayerSetTempBuf(processor);";
        }
    };
    public final HLEModuleFunction scePsmfPlayerSelectSpecificVideoFunction = new HLEModuleFunction("scePsmfPlayer", "scePsmfPlayerSelectSpecificVideo") {

        @Override
        public final void execute(Processor processor) {
            scePsmfPlayerSelectSpecificVideo(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.scePsmfPlayer.scePsmfPlayerSelectSpecificVideo(processor);";
        }
    };
    public final HLEModuleFunction scePsmfPlayerSelectSpecificAudioFunction = new HLEModuleFunction("scePsmfPlayer", "scePsmfPlayerSelectSpecificAudio") {

        @Override
        public final void execute(Processor processor) {
            scePsmfPlayerSelectSpecificAudio(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.scePsmfPlayer.scePsmfPlayerSelectSpecificAudio(processor);";
        }
    };
    public final HLEModuleFunction scePsmfPlayerSelectVideoFunction = new HLEModuleFunction("scePsmfPlayer", "scePsmfPlayerSelectVideo") {

        @Override
        public final void execute(Processor processor) {
            scePsmfPlayerSelectVideo(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.scePsmfPlayer.scePsmfPlayerSelectVideo(processor);";
        }
    };
    public final HLEModuleFunction scePsmfPlayerSelectAudioFunction = new HLEModuleFunction("scePsmfPlayer", "scePsmfPlayerSelectAudio") {

        @Override
        public final void execute(Processor processor) {
            scePsmfPlayerSelectAudio(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.scePsmfPlayer.scePsmfPlayerSelectAudio(processor);";
        }
    };
}