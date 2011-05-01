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

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import jpcsp.HLE.Modules;
import jpcsp.HLE.kernel.types.IAction;
import jpcsp.HLE.kernel.types.SceKernelErrors;
import jpcsp.HLE.kernel.types.SceKernelThreadInfo;
import jpcsp.HLE.kernel.types.pspNetSockAddrInternet;
import jpcsp.HLE.modules.HLEModule;
import jpcsp.HLE.modules.HLEModuleFunction;
import jpcsp.HLE.modules.HLEModuleManager;
import jpcsp.HLE.modules.HLEStartModule;
import jpcsp.HLE.modules.sceNetApctl;
import jpcsp.memory.IMemoryReader;
import jpcsp.memory.IMemoryWriter;
import jpcsp.memory.MemoryReader;
import jpcsp.memory.MemoryWriter;
import jpcsp.util.Utilities;

import jpcsp.Emulator;
import jpcsp.Memory;
import jpcsp.Processor;

import jpcsp.Allegrex.CpuState;

public class sceNetInet implements HLEModule, HLEStartModule {
    protected static Logger log = Modules.getLogger("sceNetInet");

    public static final int AF_INET = 2; // Address familiy internet
    public static final int SOCK_STREAM = 1; // Stream socket
    public static final int SOCK_DGRAM = 2; // Datagram socket
    public static final int SOCK_RAW = 3; // Raw socket
    public static final int SOL_SOCKET = 0xFFFF; // Socket level
    public static final int INADDR_ANY = 0x00000000; // wildcard/any IP address
    public static final int INADDR_BROADCAST = 0xFFFFFFFF; // Broadcast address
    public static final int EAGAIN = SceKernelErrors.ERROR_ERRNO_RESOURCE_UNAVAILABLE & 0x0000FFFF;
    public static final int EINPROGRESS = SceKernelErrors.ERROR_ERRNO_IN_PROGRESS & 0x0000FFFF;
    public static final int ENOTCONN = SceKernelErrors.ERROR_ERRNO_NOT_CONNECTED & 0x0000FFFF;
    public static final int ECLOSED = SceKernelErrors.ERROR_ERRNO_CLOSED & 0x0000FFFF;
    public static final int EIO = SceKernelErrors.ERROR_ERRNO_IO_ERROR & 0x0000FFFF;

    // Socket options
    public static final int SO_DEBUG        = 0x0001; // turn on debugging info recording
    public static final int SO_ACCEPTCONN   = 0x0002; // socket has had listen()
    public static final int SO_REUSEADDR    = 0x0004; // allow local address reuse
    public static final int SO_KEEPALIVE    = 0x0008; // keep connections alive
    public static final int SO_DONTROUTE    = 0x0010; // just use interface addresses
    public static final int SO_BROADCAST    = 0x0020; // permit sending of broadcast msgs
    public static final int SO_USELOOPBACK  = 0x0040; // bypass hardware when possible
    public static final int SO_LINGER       = 0x0080; // linger on close if data present
    public static final int SO_OOBINLINE    = 0x0100; // leave received OOB data in line
    public static final int SO_REUSEPORT    = 0x0200; // allow local address & port reuse
    public static final int SO_TIMESTAMP    = 0x0400; // timestamp received dgram traffic
    public static final int SO_ONESBCAST    = 0x0800; // allow broadcast to 255.255.255.255
    public static final int SO_SNDBUF       = 0x1001; // send buffer size
    public static final int SO_RCVBUF       = 0x1002; // receive buffer size
    public static final int SO_SNDLOWAT     = 0x1003; // send low-water mark
    public static final int SO_RCVLOWAT     = 0x1004; // receive low-water mark
    public static final int SO_SNDTIMEO     = 0x1005; // send timeout
    public static final int SO_RCVTIMEO     = 0x1006; // receive timeout
    public static final int SO_ERROR        = 0x1007; // get error status and clear
    public static final int SO_TYPE         = 0x1008; // get socket type
    public static final int SO_OVERFLOWED   = 0x1009; // datagrams: return packets dropped
    public static final int SO_NONBLOCK     = 0x1009; // non-blocking I/O

    // Polling period (micro seconds) for blocking operations
    protected static final int BLOCKED_OPERATION_POLLING_MICROS = 10000;

    @Override
	public String getName() { return "sceNetInet"; }
	
	@Override
	public void installModule(HLEModuleManager mm, int version) {
		if (version >= 150) {
			mm.addFunction(sceNetInetInitFunction, 0x17943399);
			mm.addFunction(sceNetInetTermFunction, 0xA9ED66B9);
			mm.addFunction(sceNetInetAcceptFunction, 0xDB094E1B);
			mm.addFunction(sceNetInetBindFunction, 0x1A33F9AE);
			mm.addFunction(sceNetInetCloseFunction, 0x8D7284EA);
			mm.addFunction(sceNetInetCloseWithRSTFunction, 0x805502DD);
			mm.addFunction(sceNetInetConnectFunction, 0x410B34AA);
			mm.addFunction(sceNetInetGetpeernameFunction, 0xE247B6D6);
			mm.addFunction(sceNetInetGetsocknameFunction, 0x162E6FD5);
			mm.addFunction(sceNetInetGetsockoptFunction, 0x4A114C7C);
			mm.addFunction(sceNetInetListenFunction, 0xD10A1A7A);
			mm.addFunction(sceNetInetPollFunction, 0xFAABB1DD);
			mm.addFunction(sceNetInetRecvFunction, 0xCDA85C99);
			mm.addFunction(sceNetInetRecvfromFunction, 0xC91142E4);
			mm.addFunction(sceNetInetRecvmsgFunction, 0xEECE61D2);
			mm.addFunction(sceNetInetSelectFunction, 0x5BE8D595);
			mm.addFunction(sceNetInetSendFunction, 0x7AA671BC);
			mm.addFunction(sceNetInetSendtoFunction, 0x05038FC7);
			mm.addFunction(sceNetInetSendmsgFunction, 0x774E36F4);
			mm.addFunction(sceNetInetSetsockoptFunction, 0x2FE71FE7);
			mm.addFunction(sceNetInetShutdownFunction, 0x4CFE4E56);
			mm.addFunction(sceNetInetSocketFunction, 0x8B7B220F);
			mm.addFunction(sceNetInetSocketAbortFunction, 0x80A21ABD);
			mm.addFunction(sceNetInetGetErrnoFunction, 0xFBABE411);
			mm.addFunction(sceNetInetGetTcpcbstatFunction, 0xB3888AD4);
			mm.addFunction(sceNetInetGetUdpcbstatFunction, 0x39B0C7D3);
			mm.addFunction(sceNetInetInetAddrFunction, 0xB75D5B0A);
			mm.addFunction(sceNetInetInetAtonFunction, 0x1BDF5D13);
			mm.addFunction(sceNetInetInetNtopFunction, 0xD0792666);
			mm.addFunction(sceNetInetInetPtonFunction, 0xE30B8C19);
		}
	}
	
	@Override
	public void uninstallModule(HLEModuleManager mm, int version) {
		if (version >= 150) {
			mm.removeFunction(sceNetInetInitFunction);
			mm.removeFunction(sceNetInetTermFunction);
			mm.removeFunction(sceNetInetAcceptFunction);
			mm.removeFunction(sceNetInetBindFunction);
			mm.removeFunction(sceNetInetCloseFunction);
			mm.removeFunction(sceNetInetCloseWithRSTFunction);
			mm.removeFunction(sceNetInetConnectFunction);
			mm.removeFunction(sceNetInetGetpeernameFunction);
			mm.removeFunction(sceNetInetGetsocknameFunction);
			mm.removeFunction(sceNetInetGetsockoptFunction);
			mm.removeFunction(sceNetInetListenFunction);
			mm.removeFunction(sceNetInetPollFunction);
			mm.removeFunction(sceNetInetRecvFunction);
			mm.removeFunction(sceNetInetRecvfromFunction);
			mm.removeFunction(sceNetInetRecvmsgFunction);
			mm.removeFunction(sceNetInetSelectFunction);
			mm.removeFunction(sceNetInetSendFunction);
			mm.removeFunction(sceNetInetSendtoFunction);
			mm.removeFunction(sceNetInetSendmsgFunction);
			mm.removeFunction(sceNetInetSetsockoptFunction);
			mm.removeFunction(sceNetInetShutdownFunction);
			mm.removeFunction(sceNetInetSocketFunction);
			mm.removeFunction(sceNetInetSocketAbortFunction);
			mm.removeFunction(sceNetInetGetErrnoFunction);
			mm.removeFunction(sceNetInetGetTcpcbstatFunction);
			mm.removeFunction(sceNetInetGetUdpcbstatFunction);
			mm.removeFunction(sceNetInetInetAddrFunction);
			mm.removeFunction(sceNetInetInetAtonFunction);
			mm.removeFunction(sceNetInetInetNtopFunction);
			mm.removeFunction(sceNetInetInetPtonFunction);
		}
	}

	protected static abstract class BlockingState implements IAction {
		public pspInetSocket inetSocket;
		public int threadId;
		public boolean threadBlocked;

		public BlockingState(pspInetSocket inetSocket) {
			this.inetSocket = inetSocket;
			threadId = Modules.ThreadManForUserModule.getCurrentThreadID();
			threadBlocked = false;
		}
	}

	protected  class BlockingSelectState extends BlockingState {
		public Selector selector;
		public int numberSockets;
		public int readSocketsAddr;
		public int writeSocketsAddr;
		public int outOfBandSocketsAddr;
		public long timeout;
		public long start;
		public int count;

		public BlockingSelectState(Selector selector, int numberSockets, int readSocketsAddr, int writeSocketsAddr, int outOfBandSocketsAddr, long timeout, int count) {
			super(null);
			this.selector = selector;
			this.numberSockets = numberSockets;
			this.readSocketsAddr = readSocketsAddr;
			this.writeSocketsAddr = writeSocketsAddr;
			this.outOfBandSocketsAddr = outOfBandSocketsAddr;
			this.timeout = timeout;
			this.count = count;
			this.start = Emulator.getClock().microTime();
		}

		@Override
		public void execute() {
			Modules.sceNetInet.blockedSelect(this);
		}
	}

	protected static class BlockingReceiveState extends BlockingState {
		public int buffer;
		public int bufferLength;
		public int flags;
		public int receivedLength;

		public BlockingReceiveState(pspInetSocket inetSocket, int buffer, int bufferLength, int flags, int receivedLength) {
			super(inetSocket);
			this.buffer = buffer;
			this.bufferLength = bufferLength;
			this.flags = flags;
			this.receivedLength = receivedLength;
		}

		@Override
		public void execute() {
			inetSocket.blockedRecv(this);
		}
	}

	protected static class BlockingReceiveFromState extends BlockingState {
		public int buffer;
		public int bufferLength;
		public int flags;
		public pspNetSockAddrInternet fromAddr;
		public int receivedLength;

		public BlockingReceiveFromState(pspInetSocket inetSocket, int buffer, int bufferLength, int flags, pspNetSockAddrInternet fromAddr, int receivedLength) {
			super(inetSocket);
			this.buffer = buffer;
			this.bufferLength = bufferLength;
			this.flags = flags;
			this.fromAddr = fromAddr;
			this.receivedLength = receivedLength;
		}

		@Override
		public void execute() {
			inetSocket.blockedRecvfrom(this);
		}
	}

	protected static class BlockingSendState extends BlockingState {
		public int buffer;
		public int bufferLength;
		public int flags;
		public int sentLength;

		public BlockingSendState(pspInetSocket inetSocket, int buffer, int bufferLength, int flags, int sentLength) {
			super(inetSocket);
			this.buffer = buffer;
			this.bufferLength = bufferLength;
			this.flags = flags;
			this.sentLength = sentLength;
		}

		@Override
		public void execute() {
			inetSocket.blockedSend(this);
		}
	}

	protected static class BlockingSendToState extends BlockingState {
		public int buffer;
		public int bufferLength;
		public int flags;
		public pspNetSockAddrInternet toAddr;
		public int sentLength;

		public BlockingSendToState(pspInetSocket inetSocket, int buffer, int bufferLength, int flags, pspNetSockAddrInternet toAddr, int sentLength) {
			super(inetSocket);
			this.buffer = buffer;
			this.bufferLength = bufferLength;
			this.flags = flags;
			this.toAddr = toAddr;
			this.sentLength = sentLength;
		}

		@Override
		public void execute() {
			inetSocket.blockedSendto(this);
		}
	}

	protected abstract class pspInetSocket {
		private int uid;
		protected boolean blocking = true;
		protected boolean broadcast;
		protected boolean onesBroadcast;
		protected int receiveLowWaterMark = 1;
		protected int sendLowWaterMark = 2048;
		protected int receiveTimeout = 0;
		protected int sendTimetout = 0;
		protected int error;

		public pspInetSocket(int uid) {
			this.uid = uid;
		}

		public int getUid() {
			return uid;
		}

		public abstract int connect(pspNetSockAddrInternet addr);
		public abstract int bind(pspNetSockAddrInternet addr);
		public abstract int recv(int buffer, int bufferLength, int flags);
		public abstract int send(int buffer, int bufferLength, int flags);
		public abstract int recvfrom(int buffer, int bufferLength, int flags, pspNetSockAddrInternet fromAddr);
		public abstract int sendto(int buffer, int bufferLength, int flags, pspNetSockAddrInternet toAddr);
		public abstract int close();
		public abstract void blockedRecv(BlockingReceiveState blockingState);
		public abstract void blockedRecvfrom(BlockingReceiveFromState blockingState);
		public abstract void blockedSend(BlockingSendState blockingState);
		public abstract void blockedSendto(BlockingSendToState blockingState);
		public abstract SelectableChannel getSelectableChannel();
		public abstract boolean isValid();

		public int setBlocking(boolean blocking) {
			this.blocking = blocking;

			return 0;
		}

		public boolean isBlocking() {
			return blocking;
		}

		protected SocketAddress getSocketAddress(int address, int port) throws UnknownHostException {
			SocketAddress socketAddress;
			if (address == INADDR_ANY) {
				socketAddress = new InetSocketAddress(port);
			} else if (address == INADDR_BROADCAST && !isOnesBroadcast()) {
				// WHen SO_ONESBCAST is not enabled, map the broadcast address
				// to the broadcast address from the network of the local IP address.
				// E.g.
				//  - localHostIP: A.B.C.D
				//  - subnetMask: 255.255.255.0
				// -> localBroadcastIP: A.B.C.255
				InetAddress localInetAddress = InetAddress.getByName(sceNetApctl.getLocalHostIP());
				int localAddress = bytesToInternetAddress(localInetAddress.getAddress());
				int subnetMask = Integer.reverseBytes(sceNetApctl.getSubnetMaskInt());
				int localBroadcastAddress = localAddress & subnetMask;
				localBroadcastAddress |= address & ~subnetMask;

				socketAddress = new InetSocketAddress(InetAddress.getByAddress(internetAddressToBytes(localBroadcastAddress)), port);
			} else {
				socketAddress = new InetSocketAddress(InetAddress.getByAddress(internetAddressToBytes(address)), port);
			}

			return socketAddress;
		}

		protected SocketAddress getSocketAddress(pspNetSockAddrInternet addr) throws UnknownHostException {
			return getSocketAddress(addr.sin_addr, addr.sin_port);
		}

		public boolean isBroadcast() {
			return broadcast;
		}

		public int setBroadcast(boolean broadcast) {
			this.broadcast = broadcast;

			return 0;
		}

		public int getReceiveLowWaterMark() {
			return receiveLowWaterMark;
		}

		public void setReceiveLowWaterMark(int receiveLowWaterMark) {
			this.receiveLowWaterMark = receiveLowWaterMark;
		}

		public int getSendLowWaterMark() {
			return sendLowWaterMark;
		}

		public void setSendLowWaterMark(int sendLowWaterMark) {
			this.sendLowWaterMark = sendLowWaterMark;
		}

		protected ByteBuffer getByteBuffer(int address, int length) {
			byte[] bytes = new byte[length];
			IMemoryReader memoryReader = MemoryReader.getMemoryReader(address, length, 1);
			for (int i = 0; i < length; i++) {
				bytes[i] = (byte) memoryReader.readNext();
			}

			return ByteBuffer.wrap(bytes);
		}

		@Override
		public String toString() {
			return String.format("pspInetSocket[uid=%d]", uid);
		}

		public boolean isOnesBroadcast() {
			return onesBroadcast;
		}

		public void setOnesBroadcast(boolean onesBroadcast) {
			this.onesBroadcast = onesBroadcast;
		}

		public int getReceiveTimeout() {
			return receiveTimeout;
		}

		public void setReceiveTimeout(int receiveTimeout) {
			this.receiveTimeout = receiveTimeout;
		}

		public int getSendTimetout() {
			return sendTimetout;
		}

		public void setSendTimetout(int sendTimetout) {
			this.sendTimetout = sendTimetout;
		}

		public int getErrorAndClear() {
			int value = error;
			error = 0;

			return value;
		}

		protected void setSocketError(Exception e) {
			if (e instanceof NotYetConnectedException) {
				error = ENOTCONN;
			} else if (e instanceof ClosedChannelException) {
				error = ECLOSED;
			} else if (e instanceof AsynchronousCloseException) {
				error = ECLOSED;
			} else if (e instanceof ClosedByInterruptException) {
				error = ECLOSED;
			} else if (e instanceof IOException) {
				error = EIO;
			} else {
				error = -1; // Unknown error
			}
		}

		protected void setError(IOException e) {
			setSocketError(e);
			errno = error;
		}

		protected void clearError() {
			error = 0;
			errno = 0;
		}
	}

	protected class pspInetStreamSocket extends pspInetSocket {
		private SocketChannel socketChannel;

		public pspInetStreamSocket(int uid) {
			super(uid);
		}

		private void openChannel() throws IOException {
			if (socketChannel == null) {
				socketChannel = SocketChannel.open();

				// We have to use non-blocking sockets at Java level
				// to allow further PSP thread scheduling while the PSP is
				// waiting for a blocking operation.
				socketChannel.configureBlocking(false);

				// Connect has no timeout
				socketChannel.socket().setSoTimeout(0);
			}
		}

		@Override
		public int connect(pspNetSockAddrInternet addr) {
			try {
				openChannel();
				boolean connected = socketChannel.connect(getSocketAddress(addr));

				if (isBlocking()) {
					// blocking mode: wait for the connection to complete
					while (!socketChannel.finishConnect()) {
						try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
							// Ignore exception
						}
					}
				} else if (!connected) {
					// non-blocking mode: return EINPROGRESS
					errno = EINPROGRESS;
					return -1;
				}
			} catch (IOException e) {
				log.error(e);
				setError(e);
				return -1;
			}

			clearError();
			return 0;
		}

		@Override
		public int bind(pspNetSockAddrInternet addr) {
			try {
				openChannel();
				socketChannel.socket().bind(getSocketAddress(addr));
			} catch (IOException e) {
				log.error(e);
				setError(e);
				return -1;
			}

			clearError();
			return 0;
		}

		@Override
		public int recv(int buffer, int bufferLength, int flags) {
			if (flags != 0) {
				log.warn(String.format("sceNetInetRecv unsupported flag 0x%X on stream socket", flags));
			}
			return recv(buffer, bufferLength, flags, null);
		}

		private int recv(int buffer, int bufferLength, int flags, BlockingReceiveState blockingState) {
			try {
				// On non-blocking, the connect might still be in progress
				if (!isBlocking() && !socketChannel.finishConnect()) {
					errno = EAGAIN;
					return -1;
				}

				byte[] bytes = new byte[bufferLength];
				int length = socketChannel.read(ByteBuffer.wrap(bytes));
				if (length > 0) {
					IMemoryWriter memoryWriter = MemoryWriter.getMemoryWriter(buffer, length, 1);
					for (int i = 0; i < length; i++) {
						memoryWriter.writeNext(bytes[i]);
					}
					memoryWriter.flush();
				}

				if (log.isDebugEnabled()) {
					log.debug(String.format("sceNetInetRecv received %d bytes: %s", length, Utilities.getMemoryDump(buffer, length, 4, 16)));
				}

				// end of stream
				if (length < 0) {
					clearError();
					return 0;
				}

				// Nothing received on a non-blocking stream, return EAGAIN in errno
				if (length == 0 && !isBlocking()) {
					errno = EAGAIN;
					return -1;
				}

				if (blockingState != null) {
					blockingState.receivedLength += length;
				}

				// With a blocking stream, at least the low water mark has to be read
				if (isBlocking()) {
					if (blockingState == null) {
						blockingState = new BlockingReceiveState(this, buffer, bufferLength, flags, length);
					}

					// If we have not yet read as much as the low water mark,
					// block the thread and retry later.
					if (blockingState.receivedLength < getReceiveLowWaterMark()) {
						blockThread(blockingState);
						return -1;
					}
				}

				clearError();
				return length;
			} catch (IOException e) {
				log.error(e);
				setError(e);
				return -1;
			}
		}

		@Override
		public int send(int buffer, int bufferLength, int flags) {
			if (flags != 0) {
				log.warn(String.format("sceNetInetSend unsupported flag 0x%X on stream socket", flags));
			}
			return send(buffer, bufferLength, flags, null);
		}

		private int send(int buffer, int bufferLength, int flags, BlockingSendState blockingState) {
			try {
				// On non-blocking, the connect might still be in progress
				if (!isBlocking() && socketChannel.isConnectionPending()) {
					// Try to finish the connection
					if (!socketChannel.finishConnect()) {
						errno = ENOTCONN;
						return -1;
					}
				}

				ByteBuffer byteBuffer = getByteBuffer(buffer, bufferLength);
				int length = socketChannel.write(byteBuffer);
				if (log.isDebugEnabled()) {
					log.debug(String.format("sceNetInetSend successfully sent %d bytes", length));
				}

				// Nothing sent on a non-blocking stream, return EAGAIN in errno
				if (length == 0 && !isBlocking()) {
					errno = EAGAIN;
					return -1;
				}

				if (blockingState != null) {
					blockingState.sentLength += length;
				}

				// With a blocking stream, we have to send all the bytes
				if (isBlocking()) {
					if (blockingState == null) {
						blockingState = new BlockingSendState(this, buffer, bufferLength, flags, length);
					}

					// If we have not yet sent all the bytes, block the thread
					// and retry later
					if (length < bufferLength) {
						blockThread(blockingState);
						return -1;
					}
				}

				clearError();
				return length;
			} catch (IOException e) {
				log.error(e);
				setError(e);
				return -1;
			}
		}

		@Override
		public int close() {
			if (socketChannel != null) {
				try {
					socketChannel.close();
					socketChannel = null;
				} catch (IOException e) {
					log.error(e);
					setError(e);
					return -1;
				}
			}

			clearError();
			return 0;
		}

		@Override
		public int recvfrom(int buffer, int bufferLength, int flags, pspNetSockAddrInternet fromAddr) {
			log.warn("sceNetInetRecvfrom not supported on stream socket");
			errno = -1;
			return -1;
		}

		@Override
		public int sendto(int buffer, int bufferLength, int flags, pspNetSockAddrInternet toAddr) {
			log.warn("sceNetInetSendto not supported on stream socket");
			errno = -1;
			return -1;
		}

		@Override
		public void blockedRecv(BlockingReceiveState blockingState) {
			int length = recv(blockingState.buffer + blockingState.receivedLength, blockingState.bufferLength - blockingState.receivedLength, blockingState.flags, blockingState);
			if (length > 0) {
				unblockThread(blockingState, blockingState.receivedLength);
			}
		}

		@Override
		public void blockedRecvfrom(BlockingReceiveFromState blockingState) {
			log.error("blockedRecvfrom not supported on stream socket");
		}

		@Override
		public void blockedSend(BlockingSendState blockingState) {
			int length = send(blockingState.buffer + blockingState.sentLength, blockingState.bufferLength - blockingState.sentLength, blockingState.flags, blockingState);
			if (length > 0) {
				unblockThread(blockingState, blockingState.sentLength);
			}
		}

		@Override
		public void blockedSendto(BlockingSendToState blockingState) {
			log.error("blockedRecvfrom not supported on stream socket");
		}

		@Override
		public SelectableChannel getSelectableChannel() {
			return socketChannel;
		}

		@Override
		public boolean isValid() {
			if (socketChannel == null) {
				return false;
			}

			if (socketChannel.isConnectionPending()) {
				// Finish the connection otherwise, the channel will never
				// be readable/writable
				try {
					socketChannel.finishConnect();
				} catch (IOException e) {
					if (log.isDebugEnabled()) {
						log.debug(String.format("%s: %s", toString(), e.toString()));
					}
					return false;
				}
			} else if (!socketChannel.isConnected()) {
				return false;
			}

			return !socketChannel.socket().isClosed();
		}
	}

	protected class pspInetDatagramSocket extends pspInetSocket {
		private DatagramChannel datagramChannel;

		public pspInetDatagramSocket(int uid) {
			super(uid);
		}

		private void openChannel() throws IOException {
			if (datagramChannel == null) {
				datagramChannel = DatagramChannel.open();
				// We have to use non-blocking sockets at Java level
				// to allow further PSP thread scheduling while the PSP is
				// waiting for a blocking operation.
				datagramChannel.configureBlocking(false);
			}
		}

		@Override
		public int connect(pspNetSockAddrInternet addr) {
			try {
				openChannel();
				datagramChannel.connect(getSocketAddress(addr));
			} catch (IOException e) {
				log.error(e);
				setError(e);
				return -1;
			}

			clearError();
			return 0;
		}

		@Override
		public int bind(pspNetSockAddrInternet addr) {
			try {
				openChannel();
				datagramChannel.socket().bind(getSocketAddress(addr));
			} catch (IOException e) {
				log.error(e);
				setError(e);
				return -1;
			}

			clearError();
			return 0;
		}

		@Override
		public int close() {
			if (datagramChannel != null) {
				try {
					datagramChannel.close();
					datagramChannel = null;
				} catch (IOException e) {
					log.error(e);
					setError(e);
					return -1;
				}
			}

			clearError();
			return 0;
		}

		@Override
		public int recv(int buffer, int bufferLength, int flags) {
			if (flags != 0) {
				log.warn(String.format("sceNetInetRecv unsupported flag 0x%X on datagram socket", flags));
			}
			return recv(buffer, bufferLength, flags, null);
		}

		private int recv(int buffer, int bufferLength, int flags, BlockingReceiveState blockingState) {
			try {
				byte[] bytes = new byte[bufferLength];
				ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
				SocketAddress socketAddress = datagramChannel.receive(byteBuffer);
				int length = byteBuffer.position();
				if (length > 0) {
					IMemoryWriter memoryWriter = MemoryWriter.getMemoryWriter(buffer, length, 1);
					for (int i = 0; i < length; i++) {
						memoryWriter.writeNext(bytes[i]);
					}
					memoryWriter.flush();
				}

				if (log.isDebugEnabled()) {
					log.debug(String.format("sceNetInetRecv received %d bytes from %s: %s", length, socketAddress, Utilities.getMemoryDump(buffer, length, 4, 16)));
				}

				if (length < 0) {
					// end of stream
					clearError();
					return 0;
				}

				// Nothing received on a non-blocking stream, return EAGAIN in errno
				if (length == 0 && !isBlocking()) {
					errno = EAGAIN;
					return -1;
				}

				if (blockingState != null) {
					blockingState.receivedLength += length;
				}

				// With a blocking stream, at least the low water mark has to be read
				if (isBlocking()) {
					if (blockingState == null) {
						blockingState = new BlockingReceiveState(this, buffer, bufferLength, flags, length);
					}

					// If we have not yet read as much as the low water mark,
					// block the thread and retry later.
					if (blockingState.receivedLength < getReceiveLowWaterMark()) {
						blockThread(blockingState);
						return -1;
					}
				}

				clearError();
				return length;
			} catch (IOException e) {
				log.error(e);
				setError(e);
				return -1;
			}
		}

		@Override
		public int send(int buffer, int bufferLength, int flags) {
			log.warn("sceNetInetSend not supported on datagram socket");
			errno = -1;
			return -1;
		}

		@Override
		public int recvfrom(int buffer, int bufferLength, int flags, pspNetSockAddrInternet fromAddr) {
			if (flags != 0) {
				log.warn(String.format("sceNetInetRecvfrom unsupported flag 0x%X on datagram socket", flags));
			}
			return recvfrom(buffer, bufferLength, flags, fromAddr, null);
		}

		private int recvfrom(int buffer, int bufferLength, int flags, pspNetSockAddrInternet fromAddr, BlockingReceiveFromState blockingState) {
			try {
				byte[] bytes = new byte[bufferLength];
				ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
				SocketAddress socketAddress = datagramChannel.receive(byteBuffer);
				int length = byteBuffer.position();
				if (length > 0) {
					IMemoryWriter memoryWriter = MemoryWriter.getMemoryWriter(buffer, length, 1);
					for (int i = 0; i < length; i++) {
						memoryWriter.writeNext(bytes[i]);
					}
					memoryWriter.flush();
				}

				if (log.isDebugEnabled()) {
					log.debug(String.format("sceNetInetRecvfrom received %d bytes from %s: %s", length, socketAddress, Utilities.getMemoryDump(buffer, length, 4, 16)));
				}

				if (socketAddress == null) {
					// Nothing received on a non-blocking datagram, return EAGAIN in errno
					if (!isBlocking()) {
						errno = EAGAIN;
						return -1;
					}

					// Nothing received on a blocking datagram, block the thread
					if (blockingState == null) {
						blockingState = new BlockingReceiveFromState(this, buffer, bufferLength, flags, fromAddr, length);
					}
					blockThread(blockingState);
					return -1;
				}

				if (socketAddress instanceof InetSocketAddress) {
					InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
					fromAddr.sin_family = AF_INET;
					fromAddr.sin_port = inetSocketAddress.getPort();
					fromAddr.sin_addr = bytesToInternetAddress(inetSocketAddress.getAddress().getAddress());
				}

				clearError();
				return length;
			} catch (IOException e) {
				log.error(e);
				setError(e);
				return -1;
			}
		}

		@Override
		public int sendto(int buffer, int bufferLength, int flags, pspNetSockAddrInternet toAddr) {
			if (flags != 0) {
				log.warn(String.format("sceNetInetSendto unsupported flag 0x%X on datagram socket", flags));
			}
			return sendto(buffer, bufferLength, flags, toAddr, null);
		}

		private int sendto(int buffer, int bufferLength, int flags, pspNetSockAddrInternet toAddr, BlockingSendToState blockingState) {
			try {
				ByteBuffer byteBuffer = getByteBuffer(buffer, bufferLength);
				SocketAddress socketAddress = getSocketAddress(toAddr);
				int length = datagramChannel.send(byteBuffer, socketAddress);
				if (log.isDebugEnabled()) {
					log.debug(String.format("sceNetInetSendto successfully sent %d bytes", length));
				}

				// Nothing sent on a non-blocking stream, return EAGAIN in errno
				if (length == 0 && !isBlocking()) {
					errno = EAGAIN;
					return -1;
				}

				if (blockingState != null) {
					blockingState.sentLength += length;
				}

				// With a blocking stream, we have to send all the bytes
				if (isBlocking()) {
					if (blockingState == null) {
						blockingState = new BlockingSendToState(this, buffer, bufferLength, flags, toAddr, length);
					}

					// If we have not yet sent all the bytes, block the thread
					// and retry later
					if (length < bufferLength) {
						blockThread(blockingState);
						return -1;
					}
				}

				clearError();
				return length;
			} catch (IOException e) {
				log.error(e);
				setError(e);
				return -1;
			}
		}

		@Override
		public int setBroadcast(boolean broadcast) {
			super.setBroadcast(broadcast);
			try {
				openChannel();
				datagramChannel.socket().setBroadcast(broadcast);
			} catch (IOException e) {
				log.error(e);
				setError(e);
				return -1;
			}

			clearError();
			return 0;
		}

		@Override
		public void blockedRecv(BlockingReceiveState blockingState) {
			int length = recv(blockingState.buffer + blockingState.receivedLength, blockingState.bufferLength - blockingState.receivedLength, blockingState.flags, blockingState);
			if (length > 0) {
				unblockThread(blockingState, blockingState.receivedLength);
			}
		}

		@Override
		public void blockedRecvfrom(BlockingReceiveFromState blockingState) {
			int length = recvfrom(blockingState.buffer + blockingState.receivedLength, blockingState.bufferLength - blockingState.receivedLength, blockingState.flags, blockingState.fromAddr, blockingState);
			if (length > 0) {
				unblockThread(blockingState, blockingState.receivedLength);
			}
		}

		@Override
		public void blockedSend(BlockingSendState blockingState) {
			log.error("blockedSend not supported on datagram socket");
		}

		@Override
		public void blockedSendto(BlockingSendToState blockingState) {
			int length = sendto(blockingState.buffer + blockingState.sentLength, blockingState.bufferLength - blockingState.sentLength, blockingState.flags, blockingState.toAddr, blockingState);
			if (length > 0) {
				unblockThread(blockingState, blockingState.sentLength);
			}
		}

		@Override
		public SelectableChannel getSelectableChannel() {
			return datagramChannel;
		}

		@Override
		public boolean isValid() {
			return datagramChannel != null && !datagramChannel.socket().isClosed();
		}
	}

	protected HashMap<Integer, pspInetSocket> sockets;
	protected int errno;
	protected LinkedList<Integer> freeSocketIds;

	@Override
	public void start() {
		errno = 0;
		sockets = new HashMap<Integer, pspInetSocket>();

		// A socket ID has to be a number [0..255],
		// because sceNetInetSelect can handle only 256 bits
		freeSocketIds = new LinkedList<Integer>();
		for (int i = 0; i < 256; i++) {
			freeSocketIds.add(i);
		}
	}

	@Override
	public void stop() {
		sockets = null;
	}

	protected int createSocketId() {
		return freeSocketIds.pop();
	}

	protected void releaseSocketId(int id) {
		freeSocketIds.add(id);
	}

	protected int readSocketList(Selector selector, int address, int n, int selectorOperation, String comment) {
		int closedSocketsCount = 0;

		if (address != 0) {
			LinkedList<Integer> closedChannels = new LinkedList<Integer>();
			int length = (n + 7) / 8;
			IMemoryReader memoryReader = MemoryReader.getMemoryReader(address, length, 4);
			int value = 0;
			for (int socket = 0; socket < n; socket++) {
				if ((socket % 32) == 0) {
					value = memoryReader.readNext();
				}
				int bit = (value & 1);
				value = value >>> 1;
				if (bit != 0) {
					pspInetSocket inetSocket = sockets.get(socket);
					if (inetSocket != null) {
						SelectableChannel selectableChannel = inetSocket.getSelectableChannel();
						if (selectableChannel != null) {
							try {
								selectableChannel.register(selector, selectorOperation, new Integer(socket));
							} catch (ClosedChannelException e) {
								closedChannels.add(socket);
								if (log.isDebugEnabled()) {
									log.debug(String.format("%s: %s", inetSocket.toString(), e.toString()));
								}
							}
						}
					}
				}
			}

			// Clear the socket list so that we just have to set the bits for
			// the sockets that are ready.
			Processor.memory.memset(address, (byte) 0, length);

			// and set the bit for all the closed channels
			for (Integer socket : closedChannels) {
				setSelectBit(address, socket);
				closedSocketsCount++;
			}
		}

		return closedSocketsCount;
	}

	protected String dumpSelectBits(int addr, int n) {
		if (addr == 0 || n <= 0) {
			return "";
		}

		StringBuilder dump = new StringBuilder();
		Memory mem = Processor.memory;
		for (int i = 0; i < n; i++) {
			int bit = 1 << (i % 8);
			int value = mem.read8(addr + (i / 8));
			dump.append((value & bit) != 0 ? '1' : '0');
		}

		return dump.toString();
	}

	protected void setSelectBit(int addr, int socket) {
		if (addr != 0) {
			Memory mem = Processor.memory;

			addr += socket / 8;
			int value = 1 << (socket % 8);
			mem.write8(addr, (byte) (mem.read8(addr) | value));
		}
	}

	protected void blockThread(BlockingState blockingState) {
		if (!blockingState.threadBlocked) {
			Modules.ThreadManForUserModule.hleBlockCurrentThread(blockingState);
			blockingState.threadBlocked = true;
		}
		long schedule = Emulator.getClock().microTime() + BLOCKED_OPERATION_POLLING_MICROS;
		Emulator.getScheduler().addAction(schedule, blockingState);
	}

	protected void unblockThread(BlockingState blockingState, int returnValue) {
		SceKernelThreadInfo thread = Modules.ThreadManForUserModule.getThreadById(blockingState.threadId);
		if (thread != null) {
			thread.cpuContext.gpr[2] = returnValue;
		}
		if (blockingState.threadBlocked) {
			Modules.ThreadManForUserModule.hleUnblockThread(blockingState.threadId);
			blockingState.threadBlocked = false;
		}
	}

	protected int checkInvalidSelectedSockets(BlockingSelectState blockingState) {
		int countInvalidSocket = 0;

		// Check for valid sockets.
		// When a socket is no longer valid (e.g. connect failed),
		// return the select bit for this socket so that the application
		// has a chance to see the failed connection.
		for (SelectionKey selectionKey : blockingState.selector.keys()) {
			if (selectionKey.isValid()) {
				int socket = (Integer) selectionKey.attachment();
				pspInetSocket inetSocket = sockets.get(socket);
				if (inetSocket == null || !inetSocket.isValid()) {
					countInvalidSocket++;

					int interestOps;
					try {
						interestOps = selectionKey.interestOps();
					} catch (CancelledKeyException e) {
						// The key has been cancelled, set the selection bit for all operations
						interestOps = SelectionKey.OP_READ | SelectionKey.OP_WRITE | SelectionKey.OP_CONNECT | SelectionKey.OP_ACCEPT;
					}

					if ((interestOps & SelectionKey.OP_READ) != 0) {
						setSelectBit(blockingState.readSocketsAddr, socket);
					}
					if ((interestOps & SelectionKey.OP_WRITE) != 0) {
						setSelectBit(blockingState.writeSocketsAddr, socket);
					}
					if ((interestOps & (SelectionKey.OP_CONNECT | SelectionKey.OP_ACCEPT)) != 0) {
						setSelectBit(blockingState.outOfBandSocketsAddr, socket);
					}
				}
			}
		}

		if (log.isDebugEnabled()) {
			log.debug(String.format("checkInvalidSelectedSockets returns %d", countInvalidSocket));
		}

		return countInvalidSocket;
	}

	protected void blockedSelect(BlockingSelectState blockingState) {
		try {
			// Start with the count of closed channels
			// (detected when registering the selector)
			int count = blockingState.count;
			// We do not want to block here on the selector, call selectNow
			count += blockingState.selector.selectNow();
			// add any socket becoming invalid (e.g. connect failed)
			count += checkInvalidSelectedSockets(blockingState);

			boolean threadBlocked;
			if (count <= 0) {
				// Check for timeout
				long now = Emulator.getClock().microTime();
				if (now >= blockingState.start + blockingState.timeout) {
					// Timeout
					if (log.isDebugEnabled()) {
						log.debug(String.format("sceNetInetSelect returns %d sockets (timeout)", count));
					}
					threadBlocked = false;
				} else {
					// No timeout
					threadBlocked = true;
				}
			} else {
				if (log.isDebugEnabled()) {
					log.debug(String.format("sceNetInetSelect returns %d sockets", count));
				}

				for (Iterator<SelectionKey> it = blockingState.selector.selectedKeys().iterator(); it.hasNext(); ) {
					// Retrieve the next key and remove it from the set
					SelectionKey selectionKey = it.next();
					it.remove();

					if (selectionKey.isReadable()) {
						int socket = (Integer) selectionKey.attachment();
						setSelectBit(blockingState.readSocketsAddr, socket);
					}
					if (selectionKey.isWritable()) {
						int socket = (Integer) selectionKey.attachment();
						setSelectBit(blockingState.writeSocketsAddr, socket);
					}
					if (selectionKey.isAcceptable() || selectionKey.isConnectable()) {
						int socket = (Integer) selectionKey.attachment();
						setSelectBit(blockingState.outOfBandSocketsAddr, socket);
					}
				}

				threadBlocked = false;
			}

			if (threadBlocked) {
				blockThread(blockingState);
			} else {
				// We do no longer need the selector, close it
				blockingState.selector.close();

				if (log.isDebugEnabled()) {
					log.debug(String.format("sceNetInetSelect returning Read Sockets       : %s", dumpSelectBits(blockingState.readSocketsAddr, blockingState.numberSockets)));
					log.debug(String.format("sceNetInetSelect returning Write Sockets      : %s", dumpSelectBits(blockingState.writeSocketsAddr, blockingState.numberSockets)));
					log.debug(String.format("sceNetInetSelect returning Out-of-band Sockets: %s", dumpSelectBits(blockingState.outOfBandSocketsAddr, blockingState.numberSockets)));
				}

				// sceNetInetSelect can now return the count, unblock the thread
				unblockThread(blockingState, count);
			}
		} catch (IOException e) {
			log.error(e);
		}
	}

	public static String internetAddressToString(int address) {
		int n4 = (address >> 24) & 0xFF;
		int n3 = (address >> 16) & 0xFF;
		int n2 = (address >>  8) & 0xFF;
		int n1 = (address >>  0) & 0xFF;

		return String.format("%d.%d.%d.%d", n1, n2, n3, n4);
	}

	public static int bytesToInternetAddress(byte[] bytes) {
		if (bytes == null) {
			return 0;
		}

		int inetAddress = 0;
		for (int i = bytes.length - 1; i >= 0; i--) {
			inetAddress = (inetAddress << 8) | (bytes[i] & 0xFF);
		}

		return inetAddress;
	}

	public static byte[] internetAddressToBytes(int address) {
		byte[] bytes = new byte[4];

		int n4 = (address >> 24) & 0xFF;
		int n3 = (address >> 16) & 0xFF;
		int n2 = (address >>  8) & 0xFF;
		int n1 = (address >>  0) & 0xFF;
		bytes[3] = (byte) n4;
		bytes[2] = (byte) n3;
		bytes[1] = (byte) n2;
		bytes[0] = (byte) n1;

		return bytes;
	}

	protected static String getOptionNameString(int optionName) {
		switch (optionName) {
			case SO_DEBUG: return "SO_DEBUG";
			case SO_ACCEPTCONN: return "SO_ACCEPTCONN";
			case SO_REUSEADDR: return "SO_REUSEADDR";
			case SO_KEEPALIVE: return "SO_KEEPALIVE";
			case SO_DONTROUTE: return "SO_DONTROUTE";
			case SO_BROADCAST: return "SO_BROADCAST";
			case SO_USELOOPBACK: return "SO_USELOOPBACK";
			case SO_LINGER: return "SO_LINGER";
			case SO_OOBINLINE: return "SO_OOBINLINE";
			case SO_REUSEPORT: return "SO_REUSEPORT";
			case SO_TIMESTAMP: return "SO_TIMESTAMP";
			case SO_SNDBUF: return "SO_SNDBUF";
			case SO_RCVBUF: return "SO_RCVBUF";
			case SO_SNDLOWAT: return "SO_SNDLOWAT";
			case SO_RCVLOWAT: return "SO_RCVLOWAT";
			case SO_SNDTIMEO: return "SO_SNDTIMEO";
			case SO_RCVTIMEO: return "SO_RCVTIMEO";
			case SO_ERROR: return "SO_ERROR";
			case SO_TYPE: return "SO_TYPE";
			case SO_NONBLOCK: return "SO_NONBLOCK";
			default: return String.format("Unknown option 0x%X", optionName);
		}
	}

	public void sceNetInetInit(Processor processor) {
		CpuState cpu = processor.cpu;

		// This function has no parameter

		cpu.gpr[2] = 0;
	}
    
	public void sceNetInetTerm(Processor processor) {
		CpuState cpu = processor.cpu;

		log.warn("Unimplemented NID function sceNetInetTerm [0xA9ED66B9]");

		cpu.gpr[2] = 0;
	}

	// int     sceNetInetAccept(int s, struct sockaddr *addr, socklen_t *addrlen);
	public void sceNetInetAccept(Processor processor) {
		CpuState cpu = processor.cpu;
		Memory mem = Processor.memory;

		int socket = cpu.gpr[4];
		int address = cpu.gpr[5];
		int addressLength = cpu.gpr[6];

		pspNetSockAddrInternet sockAddrInternet = new pspNetSockAddrInternet();
		sockAddrInternet.read(mem, address);

		log.warn(String.format("Unimplemented sceNetInetAccept socket=%d, address=0x%08X(%s), addressLength=%d", socket, address, sockAddrInternet.toString(), addressLength));

		cpu.gpr[2] = 0;
	}

	// int     sceNetInetBind(int socket, const struct sockaddr *address, socklen_t address_len);
	public void sceNetInetBind(Processor processor) {
		CpuState cpu = processor.cpu;
		Memory mem = Processor.memory;

		int socket = cpu.gpr[4];
		int address = cpu.gpr[5];
		int addressLength = cpu.gpr[6];

		if (log.isDebugEnabled()) {
			log.debug(String.format("sceNetInetBind socket=%d, address=0x%08X, addressLength=%d", socket, address, addressLength));
		}

		if (!Memory.isAddressGood(address)) {
			log.warn(String.format("sceNetInetBind invalid address=0x%08X", address));
			cpu.gpr[2] = -1;
		} else if (addressLength < 16) {
			log.warn(String.format("sceNetInetBind invalid addressLength=%d", addressLength));
			cpu.gpr[2] = -1;
		} else {
			pspInetSocket inetSocket = sockets.get(socket);
			pspNetSockAddrInternet sockAddrInternet = new pspNetSockAddrInternet();
			sockAddrInternet.read(mem, address);
			if (sockAddrInternet.sin_family != AF_INET) {
				log.warn(String.format("sceNetInetBind invalid socket address family=%d", sockAddrInternet.sin_family));
				cpu.gpr[2] = -1;
			} else {
				cpu.gpr[2] = inetSocket.bind(sockAddrInternet);
				if (cpu.gpr[2] == 0) {
					log.info(String.format("sceNetInetBind binding to %s", sockAddrInternet.toString()));
				} else {
					log.info(String.format("sceNetInetBind failed binding to %s", sockAddrInternet.toString()));
				}
			}
		}
	}

	// int sceNetInetClose(int s);
	public void sceNetInetClose(Processor processor) {
		CpuState cpu = processor.cpu;

		int socket = cpu.gpr[4];

		if (log.isDebugEnabled()) {
			log.debug(String.format("sceNetInetClose socket=%d", socket));
		}

		if (!sockets.containsKey(socket)) {
			log.warn(String.format("sceNetInetClose invalid socket=%d", socket));
			cpu.gpr[2] = -1;
		} else {
			pspInetSocket inetSocket = sockets.get(socket);
			cpu.gpr[2] = inetSocket.close();
			releaseSocketId(socket);
	        sockets.remove(socket);
		}
	}
    
	public void sceNetInetCloseWithRST(Processor processor) {
		CpuState cpu = processor.cpu;

		log.warn("Unimplemented NID function sceNetInetCloseWithRST [0x805502DD]");

		cpu.gpr[2] = 0;
	}

	// int     sceNetInetConnect(int socket, const struct sockaddr *serv_addr, socklen_t addrlen);
	public void sceNetInetConnect(Processor processor) {
		CpuState cpu = processor.cpu;
		Memory mem = Processor.memory;

		int socket = cpu.gpr[4];
		int address = cpu.gpr[5];
		int addressLength = cpu.gpr[6];

		if (log.isDebugEnabled()) {
			log.debug(String.format("sceNetInetConnect socket=%d, address=0x%08X, addressLength=%d", socket, address, addressLength));
		}

		if (!sockets.containsKey(socket)) {
			log.warn(String.format("sceNetInetConnect invalid socket=%d", socket));
			cpu.gpr[2] = -1;
		} else if (!Memory.isAddressGood(address)) {
			log.warn(String.format("sceNetInetConnect invalid address=0x%08X", address));
			cpu.gpr[2] = -1;
		} else if (addressLength < 16) {
			log.warn(String.format("sceNetInetConnect invalid addressLength=%d", addressLength));
			cpu.gpr[2] = -1;
		} else {
			pspInetSocket inetSocket = sockets.get(socket);
			pspNetSockAddrInternet sockAddrInternet = new pspNetSockAddrInternet();
			sockAddrInternet.read(mem, address);
			if (sockAddrInternet.sin_family != AF_INET) {
				log.warn(String.format("sceNetInetConnect invalid socket address family=%d", sockAddrInternet.sin_family));
				cpu.gpr[2] = -1;
			} else {
				cpu.gpr[2] = inetSocket.connect(sockAddrInternet);

				if (log.isInfoEnabled()) {
					if (cpu.gpr[2] == 0) {
						log.info(String.format("sceNetInetConnect connected to %s", sockAddrInternet.toString()));
					} else {
						if (errno == EINPROGRESS) {
							log.info(String.format("sceNetInetConnect connecting to %s", sockAddrInternet.toString()));
						} else {
							log.info(String.format("sceNetInetConnect failed connecting to %s (errno=%d)", sockAddrInternet.toString(), errno));
						}
					}
				}
			}
		}
	}
    
	public void sceNetInetGetpeername(Processor processor) {
		CpuState cpu = processor.cpu;

		log.warn("Unimplemented NID function sceNetInetGetpeername [0xE247B6D6]");

		cpu.gpr[2] = 0;
	}
    
	public void sceNetInetGetsockname(Processor processor) {
		CpuState cpu = processor.cpu;

		log.warn("Unimplemented NID function sceNetInetGetsockname [0x162E6FD5]");

		cpu.gpr[2] = 0;
	}
    
	public void sceNetInetGetsockopt(Processor processor) {
		CpuState cpu = processor.cpu;
		Memory mem = Processor.memory;

		int socket = cpu.gpr[4];
		int level = cpu.gpr[5];
		int optionName = cpu.gpr[6];
		int optionValue = cpu.gpr[7];
		int optionLengthAddr = cpu.gpr[8];
		int optionLength = Memory.isAddressGood(optionLengthAddr) ? mem.read32(optionLengthAddr) : 0;

		if (log.isDebugEnabled()) {
			log.debug(String.format("sceNetInetGetsockopt socket=%d, level=%d, optionName=0x%X, optionValue=0x%08X, optionLength=0x%08X(%d)", socket, level, optionName, optionValue, optionLengthAddr, optionLength));
		}

		if (!sockets.containsKey(socket)) {
			log.warn(String.format("sceNetInetGetsockopt unknown socket=%d", socket));
			cpu.gpr[2] = -1;
		} else {
			pspInetSocket inetSocket = sockets.get(socket);

			cpu.gpr[2] = 0;
			if (optionName == SO_ERROR && optionLength >= 4) {
				mem.write32(optionValue, inetSocket.getErrorAndClear());
				mem.write32(optionLengthAddr, 4);
			} else {
				log.warn(String.format("Unimplemented sceNetInetGetsockopt socket=%d, level=%d, optionName=0x%X, optionValue=0x%08X, optionLength=0x%08X(%d)", socket, level, optionName, optionValue, optionLengthAddr, optionLength));
			}
		}
	}

	// int     sceNetInetListen(int s, int backlog);
	public void sceNetInetListen(Processor processor) {
		CpuState cpu = processor.cpu;

		int socket = cpu.gpr[4];
		int backlog = cpu.gpr[5];

		log.warn(String.format("Unimplemented sceNetInetListen socket=%d, backlog=%d", socket, backlog));

		cpu.gpr[2] = 0;
	}
    
	public void sceNetInetPoll(Processor processor) {
		CpuState cpu = processor.cpu;

		log.warn("Unimplemented NID function sceNetInetPoll [0xFAABB1DD]");

		cpu.gpr[2] = 0;
	}

	// size_t  sceNetInetRecv(int s, void *buf, size_t len, int flags);
	public void sceNetInetRecv(Processor processor) {
		CpuState cpu = processor.cpu;

		int socket = cpu.gpr[4];
		int buffer = cpu.gpr[5];
		int bufferLength = cpu.gpr[6];
		int flags = cpu.gpr[7];

		if (log.isDebugEnabled()) {
			log.debug(String.format("sceNetInetRecv socket=%d, buffer=0x%08X, bufferLength=%d, flags=0x%X", socket, buffer, bufferLength, flags));
		}

		if (!sockets.containsKey(socket)) {
			log.warn(String.format("sceNetInetRecv invalid socket=%d", socket));
			cpu.gpr[2] = -1;
		} else if (!Memory.isAddressGood(buffer)) {
			log.warn(String.format("sceNetInetRecv invalid buffer address 0x%08X", buffer));
			cpu.gpr[2] = -1;
		} else {
			pspInetSocket inetSocket = sockets.get(socket);
			cpu.gpr[2] = inetSocket.recv(buffer, bufferLength, flags);
		}
	}

	// size_t  sceNetInetRecvfrom(int socket, void *buffer, size_t bufferLength, int flags, struct sockaddr *from, socklen_t *fromlen);
	public void sceNetInetRecvfrom(Processor processor) {
		CpuState cpu = processor.cpu;
		Memory mem = Processor.memory;

		int socket = cpu.gpr[4];
		int buffer = cpu.gpr[5];
		int bufferLength = cpu.gpr[6];
		int flags = cpu.gpr[7];
		int from = cpu.gpr[8];
		int fromLengthAddr = cpu.gpr[9];
		int fromLength = mem.read32(fromLengthAddr);

		if (log.isDebugEnabled()) {
			log.debug(String.format("sceNetInetRecvfrom socket=%d, buffer=0x%08X, bufferLength=%d, flags=0x%X, from=0x%08X, fromLengthAddr=0x%08X(fromLength=%d)", socket, buffer, bufferLength, flags, from, fromLengthAddr, fromLength));
		}

		if (!sockets.containsKey(socket)) {
			log.warn(String.format("sceNetInetRecvfrom invalid socket=%d", socket));
			cpu.gpr[2] = -1;
		} else if (!Memory.isAddressGood(buffer)) {
			log.warn(String.format("sceNetInetRecvfrom invalid buffer address 0x%08X", buffer));
			cpu.gpr[2] = -1;
		} else {
			pspInetSocket inetSocket = sockets.get(socket);
			pspNetSockAddrInternet fromAddrInternet = new pspNetSockAddrInternet();
			cpu.gpr[2] = inetSocket.recvfrom(buffer, bufferLength, flags, fromAddrInternet);

			fromAddrInternet.setMaxSize(fromLength);
			fromAddrInternet.write(mem, from);
			if (fromAddrInternet.sizeof() < fromLength) {
				mem.write32(fromLengthAddr, fromAddrInternet.sizeof());
			}
		}
	}
    
	public void sceNetInetRecvmsg(Processor processor) {
		CpuState cpu = processor.cpu;

		log.warn("Unimplemented NID function sceNetInetRecvmsg [0xEECE61D2]");

		cpu.gpr[2] = 0;
	}

	public void sceNetInetSelect(Processor processor) {
		CpuState cpu = processor.cpu;
		Memory mem = Processor.memory;

		int numberSockets = cpu.gpr[4];
		int readSocketsAddr = cpu.gpr[5];
		int writeSocketsAddr = cpu.gpr[6];
		int outOfBandSocketsAddr = cpu.gpr[7];
		int timeoutAddr = cpu.gpr[8];

		numberSockets = Math.min(numberSockets, 256);

		long timeoutUsec;
		if (Memory.isAddressGood(timeoutAddr)) {
			timeoutUsec = mem.read32(timeoutAddr) * 1000000L;
			timeoutUsec += mem.read32(timeoutAddr + 4);
		} else {
			// Take a very large value
			timeoutUsec = Integer.MAX_VALUE * 1000000L;
		}

		if (log.isDebugEnabled()) {
			log.debug(String.format("sceNetInetSelect numberSockets=%d, readSocketsAddr=0x%08X, writeSocketsAddr=0x%08X, outOfBandSocketsAddr=0x%08X, timeoutAddr=0x%08X(%d us)", numberSockets, readSocketsAddr, writeSocketsAddr, outOfBandSocketsAddr, timeoutAddr, timeoutUsec));
			log.debug(String.format("sceNetInetSelect Read Sockets       : %s", dumpSelectBits(readSocketsAddr, numberSockets)));
			log.debug(String.format("sceNetInetSelect Write Sockets      : %s", dumpSelectBits(writeSocketsAddr, numberSockets)));
			log.debug(String.format("sceNetInetSelect Out-of-band Sockets: %s", dumpSelectBits(outOfBandSocketsAddr, numberSockets)));
		}

		try {
			Selector selector = Selector.open();

			int count = 0;

			// Read the socket list for the read operation and register them with the selector
			count += readSocketList(selector, readSocketsAddr, numberSockets, SelectionKey.OP_READ, "readSockets");

			// Read the socket list for the write operation and register them with the selector
			count += readSocketList(selector, writeSocketsAddr, numberSockets, SelectionKey.OP_WRITE, "writeSockets");

			// Read the socket list for the accept/connect operation and register them with the selector
			count += readSocketList(selector, outOfBandSocketsAddr, numberSockets, SelectionKey.OP_ACCEPT | SelectionKey.OP_CONNECT, "outOfBandSockets");

			BlockingSelectState blockingState = new BlockingSelectState(selector, numberSockets, readSocketsAddr, writeSocketsAddr, outOfBandSocketsAddr, timeoutUsec, count);

			errno = 0;
			cpu.gpr[2] = 0; // This will be overwritten by the execution of the blockingState

			// Check if there are ready operations, otherwise, block the thread
			blockingState.execute();
		} catch (IOException e) {
			log.error(e);
			errno = -1;
			cpu.gpr[2] = -1;
		}
	}

	// size_t  sceNetInetSend(int socket, const void *buffer, size_t bufferLength, int flags);
	public void sceNetInetSend(Processor processor) {
		CpuState cpu = processor.cpu;

		int socket = cpu.gpr[4];
		int buffer = cpu.gpr[5];
		int bufferLength = cpu.gpr[6];
		int flags = cpu.gpr[7];

		if (log.isDebugEnabled()) {
			log.debug(String.format("sceNetInetSend socket=%d, buffer=0x%08X, bufferLength=%d, flags=0x%X, buffer: %s", socket, buffer, bufferLength, flags, Utilities.getMemoryDump(buffer, bufferLength, 4, 16)));
		}

		if (!sockets.containsKey(socket)) {
			log.warn(String.format("sceNetInetSend invalid socket=%d", socket));
			cpu.gpr[2] = -1;
		} else if (!Memory.isAddressGood(buffer)) {
			log.warn(String.format("sceNetInetSend invalid buffer address 0x%08X", buffer));
			cpu.gpr[2] = -1;
		} else {
			pspInetSocket inetSocket = sockets.get(socket);
			cpu.gpr[2] = inetSocket.send(buffer, bufferLength, flags);
		}
	}

	// size_t  sceNetInetSendto(int socket, const void *buffer, size_t bufferLength, int flags, const struct sockaddr *to, socklen_t tolen);
	public void sceNetInetSendto(Processor processor) {
		CpuState cpu = processor.cpu;
		Memory mem = Processor.memory;

		int socket = cpu.gpr[4];
		int buffer = cpu.gpr[5];
		int bufferLength = cpu.gpr[6];
		int flags = cpu.gpr[7];
		int to = cpu.gpr[8];
		int toLength = cpu.gpr[9];

		if (log.isDebugEnabled()) {
			log.debug(String.format("sceNetInetSendto socket=%d, buffer=0x%08X, bufferLength=%d, flags=0x%X, to=0x%08X, toLength=%d, buffer: %s", socket, buffer, bufferLength, flags, to, toLength, Utilities.getMemoryDump(buffer, bufferLength, 4, 16)));
		}

		if (!sockets.containsKey(socket)) {
			log.warn(String.format("sceNetInetSend invalid socket=%d", socket));
			cpu.gpr[2] = -1;
		} else if (!Memory.isAddressGood(to)) {
			log.warn(String.format("sceNetInetSendto invalid address to=0x%08X", to));
			cpu.gpr[2] = -1;
		} else if (toLength < 16) {
			log.warn(String.format("sceNetInetSendto invalid length toLength=%d", toLength));
			cpu.gpr[2] = -1;
		} else {
			pspNetSockAddrInternet toSockAddress = new pspNetSockAddrInternet();
			toSockAddress.read(mem, to);
			if (toSockAddress.sin_family != AF_INET) {
				log.warn(String.format("sceNetInetSendto invalid socket address familiy sin_family=%d", toSockAddress.sin_family));
				cpu.gpr[2] = -1;
			} else {
				if (log.isDebugEnabled()) {
					log.debug(String.format("sceNetInetSendto sending to %s", toSockAddress.toString()));
				}
				pspInetSocket inetSocket = sockets.get(socket);
				cpu.gpr[2] = inetSocket.sendto(buffer, bufferLength, flags, toSockAddress);
			}
		}
	}
    
	public void sceNetInetSendmsg(Processor processor) {
		CpuState cpu = processor.cpu;

		log.warn("Unimplemented NID function sceNetInetSendmsg [0x774E36F4]");

		cpu.gpr[2] = 0;
	}

	// int     sceNetInetSetsockopt(int socket, int level, int optname, const void *optval, socklen_t optlen);
	public void sceNetInetSetsockopt(Processor processor) {
		CpuState cpu = processor.cpu;
		Memory mem = Processor.memory;

		int socket = cpu.gpr[4];
		int level = cpu.gpr[5];
		int optionName = cpu.gpr[6];
		int optionValueAddr = cpu.gpr[7];
		int optionLength = cpu.gpr[8];

		int optionValue = 0;
		if (Memory.isAddressGood(optionValueAddr) && optionLength >= 4) {
			optionValue = mem.read32(optionValueAddr);
		}

		if (log.isDebugEnabled()) {
			log.debug(String.format("sceNetInetSetsockopt socket=%d, level=%d, optionName=%d(%s), optionValueAddr=0x%08X, optionLength=%d, optionValue: %s", socket, level, optionName, getOptionNameString(optionName), optionValueAddr, optionLength, Utilities.getMemoryDump(optionValueAddr, optionLength, 4, 16)));
		}

		if (!sockets.containsKey(socket)) {
			log.warn(String.format("sceNetInetSetsockopt invalid socket=%d", socket));
			cpu.gpr[2] = -1;
		} else {
			pspInetSocket inetSocket = sockets.get(socket);

			if (optionName == SO_NONBLOCK && optionLength == 4) {
				cpu.gpr[2] = inetSocket.setBlocking(optionValue == 0);
			} else if (optionName == SO_BROADCAST && optionLength == 4) {
				cpu.gpr[2] = inetSocket.setBroadcast(optionValue != 0);
			} else {
				log.warn(String.format("Unimplemented sceNetInetSetsockopt socket=%d, level=%d, optionName=%d(%s), optionValue=0x%08X, optionLength=%d, optionValue: %s", socket, level, optionName, getOptionNameString(optionName), optionValueAddr, optionLength, Utilities.getMemoryDump(optionValueAddr, optionLength, 4, 16)));
				cpu.gpr[2] = 0;
			}
		}
	}
    
	public void sceNetInetShutdown(Processor processor) {
		CpuState cpu = processor.cpu;

		log.warn("Unimplemented NID function sceNetInetShutdown [0x4CFE4E56]");

		cpu.gpr[2] = 0;
	}

	// int     sceNetInetSocket(int domain, int type, int protocol);
	public void sceNetInetSocket(Processor processor) {
		CpuState cpu = processor.cpu;

		int domain = cpu.gpr[4];
		int type = cpu.gpr[5];
		int protocol = cpu.gpr[6];

		if (log.isDebugEnabled()) {
			log.debug(String.format("sceNetInetSocket domain=%d, type=%d, protocol=%d", domain, type, protocol));
		}

		if (domain != AF_INET) {
			log.warn(String.format("sceNetInetSocket unsupported domain=%d, type=%d, protocol=%d", domain, type, protocol));
			cpu.gpr[2] = -1;
		} else if (type != SOCK_DGRAM && type != SOCK_STREAM) {
			log.warn(String.format("sceNetInetSocket unsupported type=%d, domain=%d, protocol=%d", type, domain, protocol));
			cpu.gpr[2] = -1;
		} else {
			int uid = createSocketId();
	    	pspInetSocket inetSocket = null;
	    	if (type == SOCK_STREAM) {
	    		inetSocket = new pspInetStreamSocket(uid);
	    	} else if (type == SOCK_DGRAM) {
	    		inetSocket = new pspInetDatagramSocket(uid);
	    	}
	    	sockets.put(uid, inetSocket);

	    	cpu.gpr[2] = uid;
		}
	}
    
	public void sceNetInetSocketAbort(Processor processor) {
		CpuState cpu = processor.cpu;

		log.warn("Unimplemented NID function sceNetInetSocketAbort [0x80A21ABD]");

		cpu.gpr[2] = 0;
	}
    
	public void sceNetInetGetErrno(Processor processor) {
		CpuState cpu = processor.cpu;

		if (log.isDebugEnabled()) {
			log.debug(String.format("sceNetInetGetErrno returning 0x%08X(%1$d)", errno));
		}

		cpu.gpr[2] = errno;
	}
    
	public void sceNetInetGetTcpcbstat(Processor processor) {
		CpuState cpu = processor.cpu;

		log.warn("Unimplemented NID function sceNetInetGetTcpcbstat [0xB3888AD4]");

		cpu.gpr[2] = 0;
	}
    
	public void sceNetInetGetUdpcbstat(Processor processor) {
		CpuState cpu = processor.cpu;

		log.warn("Unimplemented NID function sceNetInetGetUdpcbstat [0x39B0C7D3]");

		cpu.gpr[2] = 0;
	}

	public void sceNetInetInetAddr(Processor processor) {
		CpuState cpu = processor.cpu;

		int nameAddr = cpu.gpr[4];
		String name = Utilities.readStringZ(nameAddr);

		if (log.isDebugEnabled()) {
			log.debug(String.format("sceNetInetInetAddr 0x%08X('%s')", nameAddr, name));
		}

		try {
			byte[] inetAddressBytes = InetAddress.getByName(name).getAddress();
			int inetAddress = bytesToInternetAddress(inetAddressBytes);
			if (log.isDebugEnabled()) {
				log.debug(String.format("sceNetInetInetAddr 0x%08X('%s') returning 0x%08X", nameAddr, name, inetAddress));
			}
			cpu.gpr[2] = inetAddress;
		} catch (UnknownHostException e) {
			log.error(e);
			cpu.gpr[2] = -1;
		}
	}
    
	public void sceNetInetInetAton(Processor processor) {
		CpuState cpu = processor.cpu;
		Memory mem = Processor.memory;

		int hostnameAddr = cpu.gpr[4];
		int addr = cpu.gpr[5];
		String hostname = Utilities.readStringZ(hostnameAddr);

		if (log.isDebugEnabled()) {
			log.debug(String.format("sceNetInetInetAton hostnameAddr=0x%08X('%s'), addr=0x%08X", hostnameAddr, hostname, addr));
		}

		try {
			InetAddress inetAddress = InetAddress.getByName(hostname);
			int resolvedAddress = bytesToInternetAddress(inetAddress.getAddress());
			mem.write32(addr, resolvedAddress);
			if (log.isDebugEnabled()) {
				log.debug(String.format("sceNetInetInetAton returning address 0x%08X('%s')", resolvedAddress, sceNetInet.internetAddressToString(resolvedAddress)));
			} else if (log.isInfoEnabled()) {
				log.info(String.format("sceNetInetInetAton resolved '%s' into '%s'", hostname, sceNetInet.internetAddressToString(resolvedAddress)));
			}
			cpu.gpr[2] = 1;
		} catch (UnknownHostException e) {
			log.error(e);
			cpu.gpr[2] = 0;
		}
	}
    
	public void sceNetInetInetNtop(Processor processor) {
		CpuState cpu = processor.cpu;
		Memory mem = Processor.memory;

		int familiy = cpu.gpr[4];
		int srcAddr = cpu.gpr[5];
		int buffer = cpu.gpr[6];
		int bufferLength = cpu.gpr[7];

		if (log.isDebugEnabled()) {
			log.debug(String.format("sceNetInetInetNtop family=%d, srcAddr=0x%08X, buffer=0x%08X, bufferLength=%d", familiy, srcAddr, buffer, bufferLength));
		}

		if (familiy != AF_INET) {
			log.warn(String.format("sceNetInetInetNtop unsupported family %d", familiy));
			cpu.gpr[2] = 0;
		} else {
			pspNetSockAddrInternet addr = new pspNetSockAddrInternet();
			addr.read(mem, srcAddr);
			String ip = internetAddressToString(addr.sin_addr);
			if (log.isDebugEnabled()) {
				log.debug(String.format("sceNetInetInetNtop returning %s for %s", ip, addr.toString()));
			}
			Utilities.writeStringNZ(mem, buffer, bufferLength, ip);
			cpu.gpr[2] = buffer;
		}
	}
    
	public void sceNetInetInetPton(Processor processor) {
		CpuState cpu = processor.cpu;

		log.warn("Unimplemented NID function sceNetInetInetPton [0xE30B8C19]");

		cpu.gpr[2] = 0;
	}
    
	public final HLEModuleFunction sceNetInetInitFunction = new HLEModuleFunction("sceNetInet", "sceNetInetInit") {
		@Override
		public final void execute(Processor processor) {
			sceNetInetInit(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceNetInetModule.sceNetInetInit(processor);";
		}
	};
    
	public final HLEModuleFunction sceNetInetTermFunction = new HLEModuleFunction("sceNetInet", "sceNetInetTerm") {
		@Override
		public final void execute(Processor processor) {
			sceNetInetTerm(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceNetInetModule.sceNetInetTerm(processor);";
		}
	};
    
	public final HLEModuleFunction sceNetInetAcceptFunction = new HLEModuleFunction("sceNetInet", "sceNetInetAccept") {
		@Override
		public final void execute(Processor processor) {
			sceNetInetAccept(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceNetInetModule.sceNetInetAccept(processor);";
		}
	};
    
	public final HLEModuleFunction sceNetInetBindFunction = new HLEModuleFunction("sceNetInet", "sceNetInetBind") {
		@Override
		public final void execute(Processor processor) {
			sceNetInetBind(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceNetInetModule.sceNetInetBind(processor);";
		}
	};
    
	public final HLEModuleFunction sceNetInetCloseFunction = new HLEModuleFunction("sceNetInet", "sceNetInetClose") {
		@Override
		public final void execute(Processor processor) {
			sceNetInetClose(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceNetInetModule.sceNetInetClose(processor);";
		}
	};
    
	public final HLEModuleFunction sceNetInetCloseWithRSTFunction = new HLEModuleFunction("sceNetInet", "sceNetInetCloseWithRST") {
		@Override
		public final void execute(Processor processor) {
			sceNetInetCloseWithRST(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceNetInetModule.sceNetInetCloseWithRST(processor);";
		}
	};
    
	public final HLEModuleFunction sceNetInetConnectFunction = new HLEModuleFunction("sceNetInet", "sceNetInetConnect") {
		@Override
		public final void execute(Processor processor) {
			sceNetInetConnect(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceNetInetModule.sceNetInetConnect(processor);";
		}
	};
    
	public final HLEModuleFunction sceNetInetGetpeernameFunction = new HLEModuleFunction("sceNetInet", "sceNetInetGetpeername") {
		@Override
		public final void execute(Processor processor) {
			sceNetInetGetpeername(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceNetInetModule.sceNetInetGetpeername(processor);";
		}
	};
    
	public final HLEModuleFunction sceNetInetGetsocknameFunction = new HLEModuleFunction("sceNetInet", "sceNetInetGetsockname") {
		@Override
		public final void execute(Processor processor) {
			sceNetInetGetsockname(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceNetInetModule.sceNetInetGetsockname(processor);";
		}
	};
    
	public final HLEModuleFunction sceNetInetGetsockoptFunction = new HLEModuleFunction("sceNetInet", "sceNetInetGetsockopt") {
		@Override
		public final void execute(Processor processor) {
			sceNetInetGetsockopt(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceNetInetModule.sceNetInetGetsockopt(processor);";
		}
	};
    
	public final HLEModuleFunction sceNetInetListenFunction = new HLEModuleFunction("sceNetInet", "sceNetInetListen") {
		@Override
		public final void execute(Processor processor) {
			sceNetInetListen(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceNetInetModule.sceNetInetListen(processor);";
		}
	};
    
	public final HLEModuleFunction sceNetInetPollFunction = new HLEModuleFunction("sceNetInet", "sceNetInetPoll") {
		@Override
		public final void execute(Processor processor) {
			sceNetInetPoll(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceNetInetModule.sceNetInetPoll(processor);";
		}
	};
    
	public final HLEModuleFunction sceNetInetRecvFunction = new HLEModuleFunction("sceNetInet", "sceNetInetRecv") {
		@Override
		public final void execute(Processor processor) {
			sceNetInetRecv(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceNetInetModule.sceNetInetRecv(processor);";
		}
	};
    
	public final HLEModuleFunction sceNetInetRecvfromFunction = new HLEModuleFunction("sceNetInet", "sceNetInetRecvfrom") {
		@Override
		public final void execute(Processor processor) {
			sceNetInetRecvfrom(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceNetInetModule.sceNetInetRecvfrom(processor);";
		}
	};
    
	public final HLEModuleFunction sceNetInetRecvmsgFunction = new HLEModuleFunction("sceNetInet", "sceNetInetRecvmsg") {
		@Override
		public final void execute(Processor processor) {
			sceNetInetRecvmsg(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceNetInetModule.sceNetInetRecvmsg(processor);";
		}
	};
    
	public final HLEModuleFunction sceNetInetSelectFunction = new HLEModuleFunction("sceNetInet", "sceNetInetSelect") {
		@Override
		public final void execute(Processor processor) {
			sceNetInetSelect(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceNetInetModule.sceNetInetSelect(processor);";
		}
	};
    
	public final HLEModuleFunction sceNetInetSendFunction = new HLEModuleFunction("sceNetInet", "sceNetInetSend") {
		@Override
		public final void execute(Processor processor) {
			sceNetInetSend(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceNetInetModule.sceNetInetSend(processor);";
		}
	};
    
	public final HLEModuleFunction sceNetInetSendtoFunction = new HLEModuleFunction("sceNetInet", "sceNetInetSendto") {
		@Override
		public final void execute(Processor processor) {
			sceNetInetSendto(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceNetInetModule.sceNetInetSendto(processor);";
		}
	};
    
	public final HLEModuleFunction sceNetInetSendmsgFunction = new HLEModuleFunction("sceNetInet", "sceNetInetSendmsg") {
		@Override
		public final void execute(Processor processor) {
			sceNetInetSendmsg(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceNetInetModule.sceNetInetSendmsg(processor);";
		}
	};
    
	public final HLEModuleFunction sceNetInetSetsockoptFunction = new HLEModuleFunction("sceNetInet", "sceNetInetSetsockopt") {
		@Override
		public final void execute(Processor processor) {
			sceNetInetSetsockopt(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceNetInetModule.sceNetInetSetsockopt(processor);";
		}
	};
    
	public final HLEModuleFunction sceNetInetShutdownFunction = new HLEModuleFunction("sceNetInet", "sceNetInetShutdown") {
		@Override
		public final void execute(Processor processor) {
			sceNetInetShutdown(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceNetInetModule.sceNetInetShutdown(processor);";
		}
	};
    
	public final HLEModuleFunction sceNetInetSocketFunction = new HLEModuleFunction("sceNetInet", "sceNetInetSocket") {
		@Override
		public final void execute(Processor processor) {
			sceNetInetSocket(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceNetInetModule.sceNetInetSocket(processor);";
		}
	};
    
	public final HLEModuleFunction sceNetInetSocketAbortFunction = new HLEModuleFunction("sceNetInet", "sceNetInetSocketAbort") {
		@Override
		public final void execute(Processor processor) {
			sceNetInetSocketAbort(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceNetInetModule.sceNetInetSocketAbort(processor);";
		}
	};
    
	public final HLEModuleFunction sceNetInetGetErrnoFunction = new HLEModuleFunction("sceNetInet", "sceNetInetGetErrno") {
		@Override
		public final void execute(Processor processor) {
			sceNetInetGetErrno(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceNetInetModule.sceNetInetGetErrno(processor);";
		}
	};
    
	public final HLEModuleFunction sceNetInetGetTcpcbstatFunction = new HLEModuleFunction("sceNetInet", "sceNetInetGetTcpcbstat") {
		@Override
		public final void execute(Processor processor) {
			sceNetInetGetTcpcbstat(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceNetInetModule.sceNetInetGetTcpcbstat(processor);";
		}
	};
    
	public final HLEModuleFunction sceNetInetGetUdpcbstatFunction = new HLEModuleFunction("sceNetInet", "sceNetInetGetUdpcbstat") {
		@Override
		public final void execute(Processor processor) {
			sceNetInetGetUdpcbstat(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceNetInetModule.sceNetInetGetUdpcbstat(processor);";
		}
	};
    
	public final HLEModuleFunction sceNetInetInetAddrFunction = new HLEModuleFunction("sceNetInet", "sceNetInetInetAddr") {
		@Override
		public final void execute(Processor processor) {
			sceNetInetInetAddr(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceNetInetModule.sceNetInetInetAddr(processor);";
		}
	};
    
	public final HLEModuleFunction sceNetInetInetAtonFunction = new HLEModuleFunction("sceNetInet", "sceNetInetInetAton") {
		@Override
		public final void execute(Processor processor) {
			sceNetInetInetAton(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceNetInetModule.sceNetInetInetAton(processor);";
		}
	};
    
	public final HLEModuleFunction sceNetInetInetNtopFunction = new HLEModuleFunction("sceNetInet", "sceNetInetInetNtop") {
		@Override
		public final void execute(Processor processor) {
			sceNetInetInetNtop(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceNetInetModule.sceNetInetInetNtop(processor);";
		}
	};
    
	public final HLEModuleFunction sceNetInetInetPtonFunction = new HLEModuleFunction("sceNetInet", "sceNetInetInetPton") {
		@Override
		public final void execute(Processor processor) {
			sceNetInetInetPton(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.sceNetInetModule.sceNetInetInetPton(processor);";
		}
	};
};
