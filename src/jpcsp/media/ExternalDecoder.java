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
package jpcsp.media;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;

import org.apache.log4j.Logger;

import jpcsp.HLE.Modules;
import jpcsp.HLE.modules150.IoFileMgrForUser.IIoListener;
import jpcsp.connector.AtracCodec;
import jpcsp.filesystems.SeekableDataInput;

/**
 * @author gid15
 *
 */
public class ExternalDecoder {
	private static Logger log = Modules.log;
    private static File extAudioDecoder;
    private static IoListener ioListener;
    private static boolean enabled = true;

    public ExternalDecoder() {
    	init();
    }

    public static void setEnabled(boolean flag) {
    	enabled = flag;
    }

    private static void init() {
    	if (enabled && extAudioDecoder == null) {
    		String extAudioDecoderPath = System.getProperty("java.library.path");
    		if (extAudioDecoderPath == null) {
    			extAudioDecoderPath = "";
    		} else if (!extAudioDecoderPath.endsWith("/")) {
    			extAudioDecoderPath += "/";
    		}

    		String extAudioDecoders[] = { "DecodeAudio.bat", "DecodeAudio.exe", "DecodeAudio.sh" };
            for (int i = 0; i < extAudioDecoders.length; i++) {
                File f = new File(String.format("%s%s", extAudioDecoderPath, extAudioDecoders[i]));
                if (f.exists()) {
                    extAudioDecoder = f;
                    break;
                }
            }

            if (extAudioDecoder == null) {
            	enabled = false;
            }
    	}

    	if (enabled && ioListener == null) {
    		ioListener = new IoListener();
    		Modules.IoFileMgrForUserModule.registerIoListener(ioListener);
    	}
    }

    public static boolean isEnabled() {
    	init();

    	return enabled;
    }

    private boolean executeExternalDecoder(String inputFile, String outputFile) {
		String[] cmd;
		if (extAudioDecoder.toString().endsWith(".bat")) {
			cmd = new String[] {
					"cmd",
					"/C",
					extAudioDecoder.toString(),
					inputFile,
					outputFile };
		} else {
			cmd = new String[] {
					extAudioDecoder.toString(),
					inputFile,
					outputFile };
		}
		try {
			Process extAudioDecodeProcess = Runtime.getRuntime().exec(cmd);
			StreamReader stdoutReader = new StreamReader(extAudioDecodeProcess.getInputStream());
			StreamReader stderrReader = new StreamReader(extAudioDecodeProcess.getErrorStream());
			stdoutReader.start();
			stderrReader.start();
			int exitValue = extAudioDecodeProcess.waitFor();
			if (log.isDebugEnabled()) {
				log.debug(String.format("External AudioDecode Process '%s' returned %d", extAudioDecoder.toString(), exitValue));
				log.debug("stdout: " + stdoutReader.getInput());
				log.debug("stderr: " + stderrReader.getInput());
			}
		} catch (InterruptedException e) {
			log.error(e);
			return false;
		} catch (IOException e) {
			log.error(e);
			return false;
		}

		return true;
    }

    public void decodeExtAudio(int address, int mpegFileSize, int mpegOffset) {
    	if (!isEnabled()) {
    		return;
    	}

		byte[] mpegData = ioListener.readFileData(address, mpegFileSize);
    	if (mpegData == null) {
    		// MPEG data cannot be retrieved...
    		return;
    	}

    	MpegDemux mpegDemux = new MpegDemux(mpegData, mpegOffset);
		mpegDemux.demux(false, true);

		ByteBuffer audioStream = mpegDemux.getAudioStream();
		if (audioStream != null) {
			ByteBuffer omaBuffer = OMAFormat.convertStreamToOMA(audioStream); 
			try {
				new File(MediaEngine.getExtAudioBasePath(mpegFileSize)).mkdirs();
				String encodedFile = MediaEngine.getExtAudioPath(mpegFileSize, "oma");
				FileOutputStream os = new FileOutputStream(encodedFile);
				os.getChannel().write(omaBuffer);
				os.close();

				String decodedFile = MediaEngine.getExtAudioPath(mpegFileSize, "wav");
				if (!executeExternalDecoder(encodedFile, decodedFile)) {
					new File(encodedFile).delete();
				}
			} catch (IOException e) {
				// Ignore Exception
				log.error(e);
			}
		}
    }

    private static String getAtracAudioPath(int address, int atracFileSize, String suffix) {
    	return String.format("%sAtrac-%08X-%08X.%s", AtracCodec.getBaseDirectory(), atracFileSize, address, suffix);
    }

    private ReadableByteChannel getChannel(String fileName) {
    	try {
			FileInputStream fis = new FileInputStream(fileName);
			return fis.getChannel();
		} catch (FileNotFoundException e) {
			log.error(e);
		}

		return null;
    }

    public ReadableByteChannel decodeAtrac(int address, int atracFileSize) {
    	if (!isEnabled()) {
    		return null;
    	}

		String decodedFile = getAtracAudioPath(address, atracFileSize, "wav");
		if (new File(decodedFile).canRead()) {
			// Already decoded
			return getChannel(decodedFile);
		}

		byte[] atracData = ioListener.readFileData(address, atracFileSize);
    	if (atracData == null) {
    		// Atrac data cannot be retrieved...
    		return null;
    	}

    	try {
	    	ByteBuffer riffBuffer = ByteBuffer.wrap(atracData);
	    	ByteBuffer omaBuffer = OMAFormat.convertRIFFtoOMA(riffBuffer);
	    	if (omaBuffer == null) {
	    		return null;
	    	}

			new File(AtracCodec.getBaseDirectory()).mkdirs();
			String encodedFile = getAtracAudioPath(address, atracFileSize, "oma");
			FileOutputStream os = new FileOutputStream(encodedFile);
			os.getChannel().write(omaBuffer);
			os.close();

			if (!executeExternalDecoder(encodedFile, decodedFile)) {
				new File(encodedFile).delete();
				return null;
			}
		} catch (IOException e) {
			// Ignore Exception
			log.error(e);
		}

		return getChannel(decodedFile);
    }

    private static class IoListener implements IIoListener {
    	private static class ReadInfo {
    		public int address;
    		public SeekableDataInput dataInput;
    		public long position;

    		public ReadInfo(int address, SeekableDataInput dataInput, long position) {
    			this.address = address;
    			this.dataInput = dataInput;
    			this.position = position;
    		}
    	}
    	private HashMap<Integer, ReadInfo> readInfos;

    	public IoListener() {
    		readInfos = new HashMap<Integer, ReadInfo>();
    	}

    	public byte[] readFileData(int address, int size) {
    		ReadInfo readInfo = readInfos.get(address);
    		if (readInfo == null) {
    			return null;
    		}

    		byte[] fileData = new byte[size];
    		try {
				long currentPosition = readInfo.dataInput.getFilePointer();
				readInfo.dataInput.seek(readInfo.position);
	    		readInfo.dataInput.readFully(fileData);
				readInfo.dataInput.seek(currentPosition);
			} catch (IOException e) {
				return null;
			}

			return fileData;
    	}

    	@Override
		public void sceIoRead(int result, int uid, int data_addr, int size,	int bytesRead, long position, SeekableDataInput dataInput) {
			if (result >= 0) {
				ReadInfo readInfo = new ReadInfo(data_addr, dataInput, position);
				readInfos.put(data_addr, readInfo);
			}
		}

		@Override
		public void sceIoAssign(int result, int dev1_addr, String dev1, int dev2_addr, String dev2, int dev3_addr, String dev3, int mode, int unk1, int unk2) {
		}

		@Override
		public void sceIoCancel(int result, int uid) {
		}

		@Override
		public void sceIoChdir(int result, int path_addr, String path) {
		}

		@Override
		public void sceIoChstat(int result, int path_addr, String path,	int stat_addr, int bits) {
		}

		@Override
		public void sceIoClose(int result, int uid) {
		}

		@Override
		public void sceIoDclose(int result, int uid) {
		}

		@Override
		public void sceIoDevctl(int result, int device_addr, String device, int cmd, int indata_addr, int inlen, int outdata_addr, int outlen) {
		}

		@Override
		public void sceIoDopen(int result, int path_addr, String path) {
		}

		@Override
		public void sceIoDread(int result, int uid, int dirent_addr) {
		}

		@Override
		public void sceIoGetStat(int result, int path_addr, String path, int stat_addr) {
		}

		@Override
		public void sceIoIoctl(int result, int uid, int cmd, int indata_addr, int inlen, int outdata_addr, int outlen) {
		}

		@Override
		public void sceIoMkdir(int result, int path_addr, String path, int permissions) {
		}

		@Override
		public void sceIoOpen(int result, int filename_addr, String filename, int flags, int permissions, String mode) {
		}

		@Override
		public void sceIoPollAsync(int result, int uid, int res_addr) {
		}

		@Override
		public void sceIoRemove(int result, int path_addr, String path) {
		}

		@Override
		public void sceIoRename(int result, int path_addr, String path, int new_path_addr, String newpath) {
		}

		@Override
		public void sceIoRmdir(int result, int path_addr, String path) {
		}

		@Override
		public void sceIoSeek32(int result, int uid, int offset, int whence) {
		}

		@Override
		public void sceIoSeek64(long result, int uid, long offset, int whence) {
		}

		@Override
		public void sceIoSync(int result, int device_addr, String device, int unknown) {
		}

		@Override
		public void sceIoWaitAsync(int result, int uid, int res_addr) {
		}

		@Override
		public void sceIoWrite(int result, int uid, int data_addr, int size, int bytesWritten) {
		}
    }
}
