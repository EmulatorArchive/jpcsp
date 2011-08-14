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

import jpcsp.HLE.HLEFunction;
import jpcsp.Processor;
import jpcsp.Allegrex.CpuState;
import jpcsp.HLE.kernel.managers.IntrManager;
import jpcsp.HLE.kernel.types.SceKernelErrors;
import jpcsp.HLE.Modules;
import jpcsp.HLE.modules.HLEModule;
import jpcsp.HLE.modules.HLEModuleFunction;
import jpcsp.HLE.modules.HLEModuleManager;

import org.apache.log4j.Logger;

public class sceNetAdhoc implements HLEModule {

    protected static Logger log = Modules.getLogger("sceNetAdhoc");

    @Override
    public String getName() {
        return "sceNetAdhoc";
    }

    @Override
    public void installModule(HLEModuleManager mm, int version) { mm.installModuleWithAnnotations(this, version); }

    @Override
    public void uninstallModule(HLEModuleManager mm, int version) { mm.uninstallModuleWithAnnotations(this, version); }

    /**
     * Initialise the adhoc library.
     *
     * @return 0 on success, < 0 on error
     */
    public void sceNetAdhocInit(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("IGNORING: sceNetAdhocInit");

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_KERNEL_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        cpu.gpr[2] = 0;
    }

    /**
     * Terminate the adhoc library
     *
     * @return 0 on success, < 0 on error
     */
    public void sceNetAdhocTerm(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("IGNORING: sceNetAdhocTerm");

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_KERNEL_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        cpu.gpr[2] = 0;
    }

    public void sceNetAdhocPollSocket(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceNetAdhocPollSocket");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceNetAdhocSetSocketAlert(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceNetAdhocSetSocketAlert");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceNetAdhocGetSocketAlert(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceNetAdhocGetSocketAlert");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    /**
     * Create a PDP object.
     *
     * @param mac - Your MAC address (from sceWlanGetEtherAddr)
     * @param port - Port to use, lumines uses 0x309
     * @param unk2 - Unknown, lumines sets to 0x400
     * @param unk3 - Unknown, lumines sets to 0
     *
     * @return The ID of the PDP object (< 0 on error)
     */
    public void sceNetAdhocPdpCreate(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceNetAdhocPdpCreate");

        cpu.gpr[2] = 1;
    }

    /**
     * Send a PDP packet to a destination
     *
     * @param id - The ID as returned by ::sceNetAdhocPdpCreate
     * @param destMacAddr - The destination MAC address, can be set to all 0xFF for broadcast
     * @param port - The port to send to
     * @param data - The data to send
     * @param len - The length of the data.
     * @param timeout - Timeout in microseconds.
     * @param nonblock - Set to 0 to block, 1 for non-blocking.
     *
     * @return Bytes sent, < 0 on error
     */
    public void sceNetAdhocPdpSend(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceNetAdhocPdpSend");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    /**
     * Receive a PDP packet
     *
     * @param id - The ID of the PDP object, as returned by ::sceNetAdhocPdpCreate
     * @param srcMacAddr - Buffer to hold the source mac address of the sender
     * @param port - Buffer to hold the port number of the received data
     * @param data - Data buffer
     * @param dataLength - The length of the data buffer
     * @param timeout - Timeout in microseconds.
     * @param nonblock - Set to 0 to block, 1 for non-blocking.
     *
     * @return Number of bytes received, < 0 on error.
     */
    public void sceNetAdhocPdpRecv(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceNetAdhocPdpRecv");

        cpu.gpr[2] = 0;
    }

    /**
     * Delete a PDP object.
     *
     * @param id - The ID returned from ::sceNetAdhocPdpCreate
     * @param unk1 - Unknown, set to 0
     *
     * @return 0 on success, < 0 on error
     */
    public void sceNetAdhocPdpDelete(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceNetAdhocPdpDelete");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    /**
     * Get the status of all PDP objects
     *
     * @param size - Pointer to the size of the stat array (e.g 20 for one structure)
     * @param stat - Pointer to a list of ::pspStatStruct structures.
     *
     * typedef struct pdpStatStruct
     * {
     *    struct pdpStatStruct *next; // Pointer to next PDP structure in list
     *    int pdpId;                  // pdp ID
     *    unsigned char mac[6];       // MAC address
     *    unsigned short port;        // Port
     *    unsigned int rcvdData;      // Bytes received
     * } pdpStatStruct
     *
     * @return 0 on success, < 0 on error
     */
    public void sceNetAdhocGetPdpStat(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceNetAdhocGetPdpStat");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    /**
     * Open a PTP connection
     *
     * @param srcmac - Local mac address.
     * @param srcport - Local port.
     * @param destmac - Destination mac.
     * @param destport - Destination port
     * @param bufsize - Socket buffer size
     * @param delay - Interval between retrying (microseconds).
     * @param count - Number of retries.
     * @param unk1 - Pass 0.
     *
     * @return A socket ID on success, < 0 on error.
     */
    public void sceNetAdhocPtpOpen(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceNetAdhocPtpOpen");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    /**
     * Wait for connection created by sceNetAdhocPtpOpen()
     *
     * @param id - A socket ID.
     * @param timeout - Timeout in microseconds.
     * @param nonblock - Set to 0 to block, 1 for non-blocking.
     *
     * @return 0 on success, < 0 on error.
     */
    public void sceNetAdhocPtpConnect(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceNetAdhocPtpConnect");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    /**
     * Wait for an incoming PTP connection
     *
     * @param srcmac - Local mac address.
     * @param srcport - Local port.
     * @param bufsize - Socket buffer size
     * @param delay - Interval between retrying (microseconds).
     * @param count - Number of retries.
     * @param queue - Connection queue length.
     * @param unk1 - Pass 0.
     *
     * @return A socket ID on success, < 0 on error.
     */
    public void sceNetAdhocPtpListen(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceNetAdhocPtpListen");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    /**
     * Accept an incoming PTP connection
     *
     * @param id - A socket ID.
     * @param mac - Connecting peers mac.
     * @param port - Connecting peers port.
     * @param timeout - Timeout in microseconds.
     * @param nonblock - Set to 0 to block, 1 for non-blocking.
     *
     * @return 0 on success, < 0 on error.
     */
    public void sceNetAdhocPtpAccept(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceNetAdhocPtpAccept");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    /**
     * Send data
     *
     * @param id - A socket ID.
     * @param data - Data to send.
     * @param datasize - Size of the data.
     * @param timeout - Timeout in microseconds.
     * @param nonblock - Set to 0 to block, 1 for non-blocking.
     *
     * @return 0 success, < 0 on error.
     */
    public void sceNetAdhocPtpSend(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceNetAdhocPtpSend");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    /**
     * Receive data
     *
     * @param id - A socket ID.
     * @param data - Buffer for the received data.
     * @param datasize - Size of the data received.
     * @param timeout - Timeout in microseconds.
     * @param nonblock - Set to 0 to block, 1 for non-blocking.
     *
     * @return 0 on success, < 0 on error.
     */
    public void sceNetAdhocPtpRecv(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceNetAdhocPtpRecv");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    /**
     * Wait for data in the buffer to be sent
     *
     * @param id - A socket ID.
     * @param timeout - Timeout in microseconds.
     * @param nonblock - Set to 0 to block, 1 for non-blocking.
     *
     * @return A socket ID on success, < 0 on error.
     */
    public void sceNetAdhocPtpFlush(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceNetAdhocPtpFlush");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    /**
     * Close a socket
     *
     * @param id - A socket ID.
     * @param unk1 - Pass 0.
     *
     * @return A socket ID on success, < 0 on error.
     */
    public void sceNetAdhocPtpClose(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceNetAdhocPtpClose");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    /**
     * Get the status of all PTP objects
     *
     * @param size - Pointer to the size of the stat array (e.g 20 for one structure)
     * @param stat - Pointer to a list of ::ptpStatStruct structures.
     *
     * typedef struct ptpStatStruct
     * {
     *    struct ptpStatStruct *next; // Pointer to next PTP structure in list
     *    int ptpId;                  // ptp ID
     *    unsigned char mac[6];       // MAC address
     *    unsigned char peermac[6];   // Peer MAC address
     *    unsigned short port;        // Port
     *    unsigned short peerport;    // Peer Port
     *    unsigned int sentData;      // Bytes sent
     *    unsigned int rcvdData;      // Bytes received
     *    int unk1;                   // Unknown
     * } ptpStatStruct;
     *
     * @return 0 on success, < 0 on error
     */
    public void sceNetAdhocGetPtpStat(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceNetAdhocGetPtpStat");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    /**
     * Create own game object type data.
     *
     * @param data - A pointer to the game object data.
     * @param size - Size of the game data.
     *
     * @return 0 on success, < 0 on error.
     */
    public void sceNetAdhocGameModeCreateMaster(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceNetAdhocGameModeCreateMaster");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    /**
     * Create peer game object type data.
     *
     * @param mac - The mac address of the peer.
     * @param data - A pointer to the game object data.
     * @param size - Size of the game data.
     *
     * @return The id of the replica on success, < 0 on error.
     */
    public void sceNetAdhocGameModeCreateReplica(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceNetAdhocGameModeCreateReplica");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    /**
     * Update own game object type data.
     *
     * @return 0 on success, < 0 on error.
     */
    public void sceNetAdhocGameModeUpdateMaster(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceNetAdhocGameModeUpdateMaster");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    /**
     * Update peer game object type data.
     *
     * @param id - The id of the replica returned by sceNetAdhocGameModeCreateReplica.
     * @param unk1 - Pass 0.
     *
     * @return 0 on success, < 0 on error.
     */
    public void sceNetAdhocGameModeUpdateReplica(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceNetAdhocGameModeUpdateReplica");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    /**
     * Delete own game object type data.
     *
     * @return 0 on success, < 0 on error.
     */
    public void sceNetAdhocGameModeDeleteMaster(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceNetAdhocGameModeDeleteMaster");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    /**
     * Delete peer game object type data.
     *
     * @param id - The id of the replica.
     *
     * @return 0 on success, < 0 on error.
     */
    public void sceNetAdhocGameModeDeleteReplica(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceNetAdhocGameModeDeleteReplica");

        cpu.gpr[2] = 0xDEADC0DE;
    }
    @HLEFunction(nid = 0xE1D621D7, version = 150)
    public final HLEModuleFunction sceNetAdhocInitFunction = new HLEModuleFunction("sceNetAdhoc", "sceNetAdhocInit") {

        @Override
        public final void execute(Processor processor) {
            sceNetAdhocInit(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetAdhocModule.sceNetAdhocInit(processor);";
        }
    };
    @HLEFunction(nid = 0xA62C6F57, version = 150)
    public final HLEModuleFunction sceNetAdhocTermFunction = new HLEModuleFunction("sceNetAdhoc", "sceNetAdhocTerm") {

        @Override
        public final void execute(Processor processor) {
            sceNetAdhocTerm(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetAdhocModule.sceNetAdhocTerm(processor);";
        }
    };
    @HLEFunction(nid = 0x7A662D6B, version = 150)
    public final HLEModuleFunction sceNetAdhocPollSocketFunction = new HLEModuleFunction("sceNetAdhoc", "sceNetAdhocPollSocket") {

        @Override
        public final void execute(Processor processor) {
            sceNetAdhocPollSocket(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetAdhocModule.sceNetAdhocPollSocket(processor);";
        }
    };
    @HLEFunction(nid = 0x73BFD52D, version = 150)
    public final HLEModuleFunction sceNetAdhocSetSocketAlertFunction = new HLEModuleFunction("sceNetAdhoc", "sceNetAdhocSetSocketAlert") {

        @Override
        public final void execute(Processor processor) {
            sceNetAdhocSetSocketAlert(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetAdhocModule.sceNetAdhocSetSocketAlert(processor);";
        }
    };
    @HLEFunction(nid = 0x4D2CE199, version = 150)
    public final HLEModuleFunction sceNetAdhocGetSocketAlertFunction = new HLEModuleFunction("sceNetAdhoc", "sceNetAdhocGetSocketAlert") {

        @Override
        public final void execute(Processor processor) {
            sceNetAdhocGetSocketAlert(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetAdhocModule.sceNetAdhocGetSocketAlert(processor);";
        }
    };
    @HLEFunction(nid = 0x6F92741B, version = 150)
    public final HLEModuleFunction sceNetAdhocPdpCreateFunction = new HLEModuleFunction("sceNetAdhoc", "sceNetAdhocPdpCreate") {

        @Override
        public final void execute(Processor processor) {
            sceNetAdhocPdpCreate(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetAdhocModule.sceNetAdhocPdpCreate(processor);";
        }
    };
    @HLEFunction(nid = 0xABED3790, version = 150)
    public final HLEModuleFunction sceNetAdhocPdpSendFunction = new HLEModuleFunction("sceNetAdhoc", "sceNetAdhocPdpSend") {

        @Override
        public final void execute(Processor processor) {
            sceNetAdhocPdpSend(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetAdhocModule.sceNetAdhocPdpSend(processor);";
        }
    };
    @HLEFunction(nid = 0xDFE53E03, version = 150)
    public final HLEModuleFunction sceNetAdhocPdpRecvFunction = new HLEModuleFunction("sceNetAdhoc", "sceNetAdhocPdpRecv") {

        @Override
        public final void execute(Processor processor) {
            sceNetAdhocPdpRecv(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetAdhocModule.sceNetAdhocPdpRecv(processor);";
        }
    };
    @HLEFunction(nid = 0x7F27BB5E, version = 150)
    public final HLEModuleFunction sceNetAdhocPdpDeleteFunction = new HLEModuleFunction("sceNetAdhoc", "sceNetAdhocPdpDelete") {

        @Override
        public final void execute(Processor processor) {
            sceNetAdhocPdpDelete(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetAdhocModule.sceNetAdhocPdpDelete(processor);";
        }
    };
    @HLEFunction(nid = 0xC7C1FC57, version = 150)
    public final HLEModuleFunction sceNetAdhocGetPdpStatFunction = new HLEModuleFunction("sceNetAdhoc", "sceNetAdhocGetPdpStat") {

        @Override
        public final void execute(Processor processor) {
            sceNetAdhocGetPdpStat(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetAdhocModule.sceNetAdhocGetPdpStat(processor);";
        }
    };
    @HLEFunction(nid = 0x877F6D66, version = 150)
    public final HLEModuleFunction sceNetAdhocPtpOpenFunction = new HLEModuleFunction("sceNetAdhoc", "sceNetAdhocPtpOpen") {

        @Override
        public final void execute(Processor processor) {
            sceNetAdhocPtpOpen(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetAdhocModule.sceNetAdhocPtpOpen(processor);";
        }
    };
    @HLEFunction(nid = 0xFC6FC07B, version = 150)
    public final HLEModuleFunction sceNetAdhocPtpConnectFunction = new HLEModuleFunction("sceNetAdhoc", "sceNetAdhocPtpConnect") {

        @Override
        public final void execute(Processor processor) {
            sceNetAdhocPtpConnect(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetAdhocModule.sceNetAdhocPtpConnect(processor);";
        }
    };
    @HLEFunction(nid = 0xE08BDAC1, version = 150)
    public final HLEModuleFunction sceNetAdhocPtpListenFunction = new HLEModuleFunction("sceNetAdhoc", "sceNetAdhocPtpListen") {

        @Override
        public final void execute(Processor processor) {
            sceNetAdhocPtpListen(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetAdhocModule.sceNetAdhocPtpListen(processor);";
        }
    };
    @HLEFunction(nid = 0x9DF81198, version = 150)
    public final HLEModuleFunction sceNetAdhocPtpAcceptFunction = new HLEModuleFunction("sceNetAdhoc", "sceNetAdhocPtpAccept") {

        @Override
        public final void execute(Processor processor) {
            sceNetAdhocPtpAccept(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetAdhocModule.sceNetAdhocPtpAccept(processor);";
        }
    };
    @HLEFunction(nid = 0x4DA4C788, version = 150)
    public final HLEModuleFunction sceNetAdhocPtpSendFunction = new HLEModuleFunction("sceNetAdhoc", "sceNetAdhocPtpSend") {

        @Override
        public final void execute(Processor processor) {
            sceNetAdhocPtpSend(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetAdhocModule.sceNetAdhocPtpSend(processor);";
        }
    };
    @HLEFunction(nid = 0x8BEA2B3E, version = 150)
    public final HLEModuleFunction sceNetAdhocPtpRecvFunction = new HLEModuleFunction("sceNetAdhoc", "sceNetAdhocPtpRecv") {

        @Override
        public final void execute(Processor processor) {
            sceNetAdhocPtpRecv(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetAdhocModule.sceNetAdhocPtpRecv(processor);";
        }
    };
    @HLEFunction(nid = 0x9AC2EEAC, version = 150)
    public final HLEModuleFunction sceNetAdhocPtpFlushFunction = new HLEModuleFunction("sceNetAdhoc", "sceNetAdhocPtpFlush") {

        @Override
        public final void execute(Processor processor) {
            sceNetAdhocPtpFlush(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetAdhocModule.sceNetAdhocPtpFlush(processor);";
        }
    };
    @HLEFunction(nid = 0x157E6225, version = 150)
    public final HLEModuleFunction sceNetAdhocPtpCloseFunction = new HLEModuleFunction("sceNetAdhoc", "sceNetAdhocPtpClose") {

        @Override
        public final void execute(Processor processor) {
            sceNetAdhocPtpClose(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetAdhocModule.sceNetAdhocPtpClose(processor);";
        }
    };
    @HLEFunction(nid = 0xB9685118, version = 150)
    public final HLEModuleFunction sceNetAdhocGetPtpStatFunction = new HLEModuleFunction("sceNetAdhoc", "sceNetAdhocGetPtpStat") {

        @Override
        public final void execute(Processor processor) {
            sceNetAdhocGetPtpStat(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetAdhocModule.sceNetAdhocGetPtpStat(processor);";
        }
    };
    @HLEFunction(nid = 0x7F75C338, version = 150)
    public final HLEModuleFunction sceNetAdhocGameModeCreateMasterFunction = new HLEModuleFunction("sceNetAdhoc", "sceNetAdhocGameModeCreateMaster") {

        @Override
        public final void execute(Processor processor) {
            sceNetAdhocGameModeCreateMaster(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetAdhocModule.sceNetAdhocGameModeCreateMaster(processor);";
        }
    };
    @HLEFunction(nid = 0x3278AB0C, version = 150)
    public final HLEModuleFunction sceNetAdhocGameModeCreateReplicaFunction = new HLEModuleFunction("sceNetAdhoc", "sceNetAdhocGameModeCreateReplica") {

        @Override
        public final void execute(Processor processor) {
            sceNetAdhocGameModeCreateReplica(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetAdhocModule.sceNetAdhocGameModeCreateReplica(processor);";
        }
    };
    @HLEFunction(nid = 0x98C204C8, version = 150)
    public final HLEModuleFunction sceNetAdhocGameModeUpdateMasterFunction = new HLEModuleFunction("sceNetAdhoc", "sceNetAdhocGameModeUpdateMaster") {

        @Override
        public final void execute(Processor processor) {
            sceNetAdhocGameModeUpdateMaster(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetAdhocModule.sceNetAdhocGameModeUpdateMaster(processor);";
        }
    };
    @HLEFunction(nid = 0xFA324B4E, version = 150)
    public final HLEModuleFunction sceNetAdhocGameModeUpdateReplicaFunction = new HLEModuleFunction("sceNetAdhoc", "sceNetAdhocGameModeUpdateReplica") {

        @Override
        public final void execute(Processor processor) {
            sceNetAdhocGameModeUpdateReplica(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetAdhocModule.sceNetAdhocGameModeUpdateReplica(processor);";
        }
    };
    @HLEFunction(nid = 0xA0229362, version = 150)
    public final HLEModuleFunction sceNetAdhocGameModeDeleteMasterFunction = new HLEModuleFunction("sceNetAdhoc", "sceNetAdhocGameModeDeleteMaster") {

        @Override
        public final void execute(Processor processor) {
            sceNetAdhocGameModeDeleteMaster(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetAdhocModule.sceNetAdhocGameModeDeleteMaster(processor);";
        }
    };
    @HLEFunction(nid = 0x0B2228E9, version = 150)
    public final HLEModuleFunction sceNetAdhocGameModeDeleteReplicaFunction = new HLEModuleFunction("sceNetAdhoc", "sceNetAdhocGameModeDeleteReplica") {

        @Override
        public final void execute(Processor processor) {
            sceNetAdhocGameModeDeleteReplica(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceNetAdhocModule.sceNetAdhocGameModeDeleteReplica(processor);";
        }
    };
}