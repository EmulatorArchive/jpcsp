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

import jpcsp.Processor;
import jpcsp.Allegrex.CpuState;
import jpcsp.HLE.kernel.managers.IntrManager;
import jpcsp.HLE.kernel.types.SceKernelErrors;
import jpcsp.HLE.Modules;
import jpcsp.HLE.modules.HLEModule;
import jpcsp.HLE.modules.HLEModuleFunction;
import jpcsp.HLE.modules.HLEModuleManager;

import org.apache.log4j.Logger;

public class sceHttp implements HLEModule {

    protected static Logger log = Modules.getLogger("sceHttp");

    @Override
    public String getName() {
        return "sceHttp";
    }

    @Override
    public void installModule(HLEModuleManager mm, int version) {
        if (version >= 150) {

            mm.addFunction(0xAB1ABE07, sceHttpInitFunction);
            mm.addFunction(0xD1C8945E, sceHttpEndFunction);
            mm.addFunction(0x0282A3BD, sceHttpGetContentLengthFunction);
            mm.addFunction(0x03D9526F, sceHttpSetResolveRetryFunction);
            mm.addFunction(0x06488A1C, sceHttpSetCookieSendCallbackFunction);
            mm.addFunction(0x0809C831, sceHttpEnableRedirectFunction);
            mm.addFunction(0x0B12ABFB, sceHttpDisableCookieFunction);
            mm.addFunction(0x0DAFA58F, sceHttpEnableCookieFunction);
            mm.addFunction(0x15540184, sceHttpDeleteHeaderFunction);
            mm.addFunction(0x1A0EBB69, sceHttpDisableRedirectFunction);
            mm.addFunction(0x1CEDB9D4, sceHttpFlushCacheFunction);
            mm.addFunction(0x1F0FC3E3, sceHttpSetRecvTimeOutFunction);
            mm.addFunction(0x2255551E, sceHttpGetNetworkPspErrorFunction);
            mm.addFunction(0x267618F4, sceHttpSetAuthInfoCallbackFunction);
            mm.addFunction(0x2A6C3296, sceHttpSetAuthInfoCBFunction);
            mm.addFunction(0x2C3C82CF, sceHttpFlushAuthListFunction);
            mm.addFunction(0x3A67F306, sceHttpSetCookieRecvCallbackFunction);
            mm.addFunction(0x3EABA285, sceHttpAddExtraHeaderFunction);
            mm.addFunction(0x47347B50, sceHttpCreateRequestFunction);
            mm.addFunction(0x47940436, sceHttpSetResolveTimeOutFunction);
            mm.addFunction(0x4CC7D78F, sceHttpGetStatusCodeFunction);
            mm.addFunction(0x5152773B, sceHttpDeleteConnectionFunction);
            mm.addFunction(0x54E7DF75, sceHttpIsRequestInCacheFunction);
            mm.addFunction(0x59E6D16F, sceHttpEnableCacheFunction);
            mm.addFunction(0x76D1363B, sceHttpSaveSystemCookieFunction);
            mm.addFunction(0x7774BF4C, sceHttpAddCookieFunction);
            mm.addFunction(0x77EE5319, sceHttpLoadAuthListFunction);
            mm.addFunction(0x78A0D3EC, sceHttpEnableKeepAliveFunction);
            mm.addFunction(0x78B54C09, sceHttpEndCacheFunction);
            mm.addFunction(0x8ACD1F73, sceHttpSetConnectTimeOutFunction);
            mm.addFunction(0x8EEFD953, sceHttpCreateConnectionFunction);
            mm.addFunction(0x951D310E, sceHttpDisableProxyAuthFunction);
            mm.addFunction(0x9668864C, sceHttpSetRecvBlockSizeFunction);
            mm.addFunction(0x96F16D3E, sceHttpGetCookieFunction);
            mm.addFunction(0x9988172D, sceHttpSetSendTimeOutFunction);
            mm.addFunction(0x9AFC98B2, sceHttpSendRequestInCacheFirstModeFunction);
            mm.addFunction(0x9B1F1F36, sceHttpCreateTemplateFunction);
            mm.addFunction(0x9FC5F10D, sceHttpEnableAuthFunction);
            mm.addFunction(0xA4496DE5, sceHttpSetRedirectCallbackFunction);
            mm.addFunction(0xA5512E01, sceHttpDeleteRequestFunction);
            mm.addFunction(0xA6800C34, sceHttpInitCacheFunction);
            mm.addFunction(0xAE948FEE, sceHttpDisableAuthFunction);
            mm.addFunction(0xB0C34B1D, sceHttpSetCacheContentLengthMaxSizeFunction);
            mm.addFunction(0xB509B09E, sceHttpCreateRequestWithURLFunction);
            mm.addFunction(0xBB70706F, sceHttpSendRequestFunction);
            mm.addFunction(0xC10B6BD9, sceHttpAbortRequestFunction);
            mm.addFunction(0xC6330B0D, sceHttpChangeHttpVersionFunction);
            mm.addFunction(0xC7EF2559, sceHttpDisableKeepAliveFunction);
            mm.addFunction(0xC98CBBA7, sceHttpSetResHeaderMaxSizeFunction);
            mm.addFunction(0xCCBD167A, sceHttpDisableCacheFunction);
            mm.addFunction(0xCDB0DC58, sceHttpEnableProxyAuthFunction);
            mm.addFunction(0xCDF8ECB9, sceHttpCreateConnectionWithURLFunction);
            mm.addFunction(0xD081EC8F, sceHttpGetNetworkErrnoFunction);
            mm.addFunction(0xD70D4847, sceHttpGetProxyFunction);
            mm.addFunction(0xDB266CCF, sceHttpGetAllHeaderFunction);
            mm.addFunction(0xDD6E7857, sceHttpSaveAuthListFunction);
            mm.addFunction(0xEDEEB999, sceHttpReadDataFunction);
            mm.addFunction(0xF0F46C62, sceHttpSetProxyFunction);
            mm.addFunction(0xF1657B22, sceHttpLoadSystemCookieFunction);
            mm.addFunction(0xF49934F6, sceHttpSetMallocFunctionFunction);
            mm.addFunction(0xFCF8C055, sceHttpDeleteTemplateFunction);

        }
    }

    @Override
    public void uninstallModule(HLEModuleManager mm, int version) {
        if (version >= 150) {

            mm.removeFunction(sceHttpInitFunction);
            mm.removeFunction(sceHttpEndFunction);
            mm.removeFunction(sceHttpGetContentLengthFunction);
            mm.removeFunction(sceHttpSetResolveRetryFunction);
            mm.removeFunction(sceHttpSetCookieSendCallbackFunction);
            mm.removeFunction(sceHttpEnableRedirectFunction);
            mm.removeFunction(sceHttpDisableCookieFunction);
            mm.removeFunction(sceHttpEnableCookieFunction);
            mm.removeFunction(sceHttpDeleteHeaderFunction);
            mm.removeFunction(sceHttpDisableRedirectFunction);
            mm.removeFunction(sceHttpFlushCacheFunction);
            mm.removeFunction(sceHttpSetRecvTimeOutFunction);
            mm.removeFunction(sceHttpGetNetworkPspErrorFunction);
            mm.removeFunction(sceHttpSetAuthInfoCallbackFunction);
            mm.removeFunction(sceHttpSetAuthInfoCBFunction);
            mm.removeFunction(sceHttpFlushAuthListFunction);
            mm.removeFunction(sceHttpSetCookieRecvCallbackFunction);
            mm.removeFunction(sceHttpAddExtraHeaderFunction);
            mm.removeFunction(sceHttpCreateRequestFunction);
            mm.removeFunction(sceHttpSetResolveTimeOutFunction);
            mm.removeFunction(sceHttpGetStatusCodeFunction);
            mm.removeFunction(sceHttpDeleteConnectionFunction);
            mm.removeFunction(sceHttpIsRequestInCacheFunction);
            mm.removeFunction(sceHttpEnableCacheFunction);
            mm.removeFunction(sceHttpSaveSystemCookieFunction);
            mm.removeFunction(sceHttpAddCookieFunction);
            mm.removeFunction(sceHttpLoadAuthListFunction);
            mm.removeFunction(sceHttpEnableKeepAliveFunction);
            mm.removeFunction(sceHttpEndCacheFunction);
            mm.removeFunction(sceHttpSetConnectTimeOutFunction);
            mm.removeFunction(sceHttpCreateConnectionFunction);
            mm.removeFunction(sceHttpDisableProxyAuthFunction);
            mm.removeFunction(sceHttpSetRecvBlockSizeFunction);
            mm.removeFunction(sceHttpGetCookieFunction);
            mm.removeFunction(sceHttpSetSendTimeOutFunction);
            mm.removeFunction(sceHttpSendRequestInCacheFirstModeFunction);
            mm.removeFunction(sceHttpCreateTemplateFunction);
            mm.removeFunction(sceHttpEnableAuthFunction);
            mm.removeFunction(sceHttpSetRedirectCallbackFunction);
            mm.removeFunction(sceHttpDeleteRequestFunction);
            mm.removeFunction(sceHttpInitCacheFunction);
            mm.removeFunction(sceHttpDisableAuthFunction);
            mm.removeFunction(sceHttpSetCacheContentLengthMaxSizeFunction);
            mm.removeFunction(sceHttpCreateRequestWithURLFunction);
            mm.removeFunction(sceHttpSendRequestFunction);
            mm.removeFunction(sceHttpAbortRequestFunction);
            mm.removeFunction(sceHttpChangeHttpVersionFunction);
            mm.removeFunction(sceHttpDisableKeepAliveFunction);
            mm.removeFunction(sceHttpSetResHeaderMaxSizeFunction);
            mm.removeFunction(sceHttpDisableCacheFunction);
            mm.removeFunction(sceHttpEnableProxyAuthFunction);
            mm.removeFunction(sceHttpCreateConnectionWithURLFunction);
            mm.removeFunction(sceHttpGetNetworkErrnoFunction);
            mm.removeFunction(sceHttpGetProxyFunction);
            mm.removeFunction(sceHttpGetAllHeaderFunction);
            mm.removeFunction(sceHttpSaveAuthListFunction);
            mm.removeFunction(sceHttpReadDataFunction);
            mm.removeFunction(sceHttpSetProxyFunction);
            mm.removeFunction(sceHttpLoadSystemCookieFunction);
            mm.removeFunction(sceHttpSetMallocFunctionFunction);
            mm.removeFunction(sceHttpDeleteTemplateFunction);

        }
    }
    public static final int PSP_HTTP_SYSTEM_COOKIE_HEAP_SIZE = 130 * 1024;
    private boolean isHttpInit;
    private boolean isSystemCookieLoaded;
    private int maxMemSize;

    public void sceHttpInit(Processor processor) {
        CpuState cpu = processor.cpu;

        int heapSize = cpu.gpr[4];

        log.info("sceHttpInit: heapSize=" + Integer.toHexString(heapSize));

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_KERNEL_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        if (isHttpInit) {
            cpu.gpr[2] = SceKernelErrors.ERROR_HTTP_ALREADY_INIT;
        } else {
            maxMemSize = heapSize;
            isHttpInit = true;
            cpu.gpr[2] = 0;
        }
    }

    public void sceHttpEnd(Processor processor) {
        CpuState cpu = processor.cpu;

        log.info("sceHttpEnd");

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_KERNEL_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        if (!isHttpInit) {
            cpu.gpr[2] = SceKernelErrors.ERROR_HTTP_NOT_INIT;
        } else {
            isSystemCookieLoaded = false;
            isHttpInit = false;
            cpu.gpr[2] = 0;
        }
    }

    public void sceHttpGetContentLength(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpGetContentLength");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpSetResolveRetry(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpSetResolveRetry");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpSetCookieSendCallback(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpSetCookieSendCallback");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpEnableRedirect(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpEnableRedirect");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpDisableCookie(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpDisableCookie");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpEnableCookie(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpEnableCookie");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpDeleteHeader(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpDeleteHeader");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpDisableRedirect(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpDisableRedirect");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpFlushCache(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpFlushCache");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpSetRecvTimeOut(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpSetRecvTimeOut");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpGetNetworkPspError(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpGetNetworkPspError");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpSetAuthInfoCallback(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpSetAuthInfoCallback");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpSetAuthInfoCB(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpSetAuthInfoCB");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpFlushAuthList(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpFlushAuthList");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpSetCookieRecvCallback(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpSetCookieRecvCallback");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpAddExtraHeader(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpAddExtraHeader");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpCreateRequest(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpCreateRequest");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpSetResolveTimeOut(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpSetResolveTimeOut");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpGetStatusCode(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpGetStatusCode");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpDeleteConnection(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpDeleteConnection");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpIsRequestInCache(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpIsRequestInCache");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpEnableCache(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpEnableCache");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpSaveSystemCookie(Processor processor) {
        CpuState cpu = processor.cpu;

        log.info("sceHttpSaveSystemCookie");

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_KERNEL_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        if (!isHttpInit) {
            cpu.gpr[2] = SceKernelErrors.ERROR_HTTP_NOT_INIT;
        } else if (!isSystemCookieLoaded){
            cpu.gpr[2] = SceKernelErrors.ERROR_HTTP_SYSTEM_COOKIE_NOT_LOADED;
        } else {
            cpu.gpr[2] = 0;
        }
    }

    public void sceHttpAddCookie(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpAddCookie");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpLoadAuthList(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpLoadAuthList");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpEnableKeepAlive(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpEnableKeepAlive");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpEndCache(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpEndCache");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpSetConnectTimeOut(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpSetConnectTimeOut");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpCreateConnection(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpCreateConnection");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpDisableProxyAuth(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpDisableProxyAuth");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpSetRecvBlockSize(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpSetRecvBlockSize");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpGetCookie(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpGetCookie");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpSetSendTimeOut(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpSetSendTimeOut");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpSendRequestInCacheFirstMode(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpSendRequestInCacheFirstMode");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpCreateTemplate(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpCreateTemplate");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpEnableAuth(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpEnableAuth");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpSetRedirectCallback(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpSetRedirectCallback");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpDeleteRequest(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpDeleteRequest");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpInitCache(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpInitCache");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpDisableAuth(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpDisableAuth");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpSetCacheContentLengthMaxSize(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpSetCacheContentLengthMaxSize");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpCreateRequestWithURL(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpCreateRequestWithURL");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpSendRequest(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpSendRequest");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpAbortRequest(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpAbortRequest");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpChangeHttpVersion(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpChangeHttpVersion");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpDisableKeepAlive(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpDisableKeepAlive");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpSetResHeaderMaxSize(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpSetResHeaderMaxSize");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpDisableCache(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpDisableCache");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpEnableProxyAuth(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpEnableProxyAuth");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpCreateConnectionWithURL(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpCreateConnectionWithURL");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpGetNetworkErrno(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpGetNetworkErrno");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpGetProxy(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpGetProxy");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpGetAllHeader(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpGetAllHeader");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpSaveAuthList(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpSaveAuthList");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpReadData(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpReadData");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpSetProxy(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpSetProxy");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpLoadSystemCookie(Processor processor) {
        CpuState cpu = processor.cpu;

        log.info("sceHttpLoadSystemCookie");

        if (IntrManager.getInstance().isInsideInterrupt()) {
            cpu.gpr[2] = SceKernelErrors.ERROR_KERNEL_CANNOT_BE_CALLED_FROM_INTERRUPT;
            return;
        }
        if (isSystemCookieLoaded) { // The system's cookie list can only be loaded once per session.
            cpu.gpr[2] = SceKernelErrors.ERROR_HTTP_ALREADY_INIT;
        } else if (maxMemSize <  PSP_HTTP_SYSTEM_COOKIE_HEAP_SIZE){
            cpu.gpr[2] = SceKernelErrors.ERROR_HTTP_NO_MEMORY;
        } else {
            isSystemCookieLoaded = true;
            cpu.gpr[2] = 0;
        }
    }

    public void sceHttpSetMallocFunction(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpSetMallocFunction");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public void sceHttpDeleteTemplate(Processor processor) {
        CpuState cpu = processor.cpu;

        log.warn("UNIMPLEMENTED: sceHttpDeleteTemplate");

        cpu.gpr[2] = 0xDEADC0DE;
    }

    public final HLEModuleFunction sceHttpInitFunction = new HLEModuleFunction("sceHttp", "sceHttpInit") {

        @Override
        public final void execute(Processor processor) {
            sceHttpInit(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpInit(processor);";
        }
    };

    public final HLEModuleFunction sceHttpEndFunction = new HLEModuleFunction("sceHttp", "sceHttpEnd") {

        @Override
        public final void execute(Processor processor) {
            sceHttpEnd(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpEnd(processor);";
        }
    };

    public final HLEModuleFunction sceHttpGetContentLengthFunction = new HLEModuleFunction("sceHttp", "sceHttpGetContentLength") {

        @Override
        public final void execute(Processor processor) {
            sceHttpGetContentLength(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpGetContentLength(processor);";
        }
    };

    public final HLEModuleFunction sceHttpSetResolveRetryFunction = new HLEModuleFunction("sceHttp", "sceHttpSetResolveRetry") {

        @Override
        public final void execute(Processor processor) {
            sceHttpSetResolveRetry(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpSetResolveRetry(processor);";
        }
    };

    public final HLEModuleFunction sceHttpSetCookieSendCallbackFunction = new HLEModuleFunction("sceHttp", "sceHttpSetCookieSendCallback") {

        @Override
        public final void execute(Processor processor) {
            sceHttpSetCookieSendCallback(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpSetCookieSendCallback(processor);";
        }
    };

    public final HLEModuleFunction sceHttpEnableRedirectFunction = new HLEModuleFunction("sceHttp", "sceHttpEnableRedirect") {

        @Override
        public final void execute(Processor processor) {
            sceHttpEnableRedirect(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpEnableRedirect(processor);";
        }
    };

    public final HLEModuleFunction sceHttpDisableCookieFunction = new HLEModuleFunction("sceHttp", "sceHttpDisableCookie") {

        @Override
        public final void execute(Processor processor) {
            sceHttpDisableCookie(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpDisableCookie(processor);";
        }
    };

    public final HLEModuleFunction sceHttpEnableCookieFunction = new HLEModuleFunction("sceHttp", "sceHttpEnableCookie") {

        @Override
        public final void execute(Processor processor) {
            sceHttpEnableCookie(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpEnableCookie(processor);";
        }
    };

    public final HLEModuleFunction sceHttpDeleteHeaderFunction = new HLEModuleFunction("sceHttp", "sceHttpDeleteHeader") {

        @Override
        public final void execute(Processor processor) {
            sceHttpDeleteHeader(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpDeleteHeader(processor);";
        }
    };

    public final HLEModuleFunction sceHttpDisableRedirectFunction = new HLEModuleFunction("sceHttp", "sceHttpDisableRedirect") {

        @Override
        public final void execute(Processor processor) {
            sceHttpDisableRedirect(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpDisableRedirect(processor);";
        }
    };

    public final HLEModuleFunction sceHttpFlushCacheFunction = new HLEModuleFunction("sceHttp", "sceHttpFlushCache") {

        @Override
        public final void execute(Processor processor) {
            sceHttpFlushCache(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpFlushCache(processor);";
        }
    };

    public final HLEModuleFunction sceHttpSetRecvTimeOutFunction = new HLEModuleFunction("sceHttp", "sceHttpSetRecvTimeOut") {

        @Override
        public final void execute(Processor processor) {
            sceHttpSetRecvTimeOut(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpSetRecvTimeOut(processor);";
        }
    };

    public final HLEModuleFunction sceHttpGetNetworkPspErrorFunction = new HLEModuleFunction("sceHttp", "sceHttpGetNetworkPspError") {

        @Override
        public final void execute(Processor processor) {
            sceHttpGetNetworkPspError(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpGetNetworkPspError(processor);";
        }
    };

    public final HLEModuleFunction sceHttpSetAuthInfoCallbackFunction = new HLEModuleFunction("sceHttp", "sceHttpSetAuthInfoCallback") {

        @Override
        public final void execute(Processor processor) {
            sceHttpSetAuthInfoCallback(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpSetAuthInfoCallback(processor);";
        }
    };

    public final HLEModuleFunction sceHttpSetAuthInfoCBFunction = new HLEModuleFunction("sceHttp", "sceHttpSetAuthInfoCB") {

        @Override
        public final void execute(Processor processor) {
            sceHttpSetAuthInfoCB(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpSetAuthInfoCB(processor);";
        }
    };

    public final HLEModuleFunction sceHttpFlushAuthListFunction = new HLEModuleFunction("sceHttp", "sceHttpFlushAuthList") {

        @Override
        public final void execute(Processor processor) {
            sceHttpFlushAuthList(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpFlushAuthList(processor);";
        }
    };

    public final HLEModuleFunction sceHttpSetCookieRecvCallbackFunction = new HLEModuleFunction("sceHttp", "sceHttpSetCookieRecvCallback") {

        @Override
        public final void execute(Processor processor) {
            sceHttpSetCookieRecvCallback(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpSetCookieRecvCallback(processor);";
        }
    };

    public final HLEModuleFunction sceHttpAddExtraHeaderFunction = new HLEModuleFunction("sceHttp", "sceHttpAddExtraHeader") {

        @Override
        public final void execute(Processor processor) {
            sceHttpAddExtraHeader(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpAddExtraHeader(processor);";
        }
    };

    public final HLEModuleFunction sceHttpCreateRequestFunction = new HLEModuleFunction("sceHttp", "sceHttpCreateRequest") {

        @Override
        public final void execute(Processor processor) {
            sceHttpCreateRequest(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpCreateRequest(processor);";
        }
    };

    public final HLEModuleFunction sceHttpSetResolveTimeOutFunction = new HLEModuleFunction("sceHttp", "sceHttpSetResolveTimeOut") {

        @Override
        public final void execute(Processor processor) {
            sceHttpSetResolveTimeOut(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpSetResolveTimeOut(processor);";
        }
    };

    public final HLEModuleFunction sceHttpGetStatusCodeFunction = new HLEModuleFunction("sceHttp", "sceHttpGetStatusCode") {

        @Override
        public final void execute(Processor processor) {
            sceHttpGetStatusCode(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpGetStatusCode(processor);";
        }
    };

    public final HLEModuleFunction sceHttpDeleteConnectionFunction = new HLEModuleFunction("sceHttp", "sceHttpDeleteConnection") {

        @Override
        public final void execute(Processor processor) {
            sceHttpDeleteConnection(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpDeleteConnection(processor);";
        }
    };

    public final HLEModuleFunction sceHttpIsRequestInCacheFunction = new HLEModuleFunction("sceHttp", "sceHttpIsRequestInCache") {

        @Override
        public final void execute(Processor processor) {
            sceHttpIsRequestInCache(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpIsRequestInCache(processor);";
        }
    };

    public final HLEModuleFunction sceHttpEnableCacheFunction = new HLEModuleFunction("sceHttp", "sceHttpEnableCache") {

        @Override
        public final void execute(Processor processor) {
            sceHttpEnableCache(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpEnableCache(processor);";
        }
    };

    public final HLEModuleFunction sceHttpSaveSystemCookieFunction = new HLEModuleFunction("sceHttp", "sceHttpSaveSystemCookie") {

        @Override
        public final void execute(Processor processor) {
            sceHttpSaveSystemCookie(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpSaveSystemCookie(processor);";
        }
    };

    public final HLEModuleFunction sceHttpAddCookieFunction = new HLEModuleFunction("sceHttp", "sceHttpAddCookie") {

        @Override
        public final void execute(Processor processor) {
            sceHttpAddCookie(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpAddCookie(processor);";
        }
    };

    public final HLEModuleFunction sceHttpLoadAuthListFunction = new HLEModuleFunction("sceHttp", "sceHttpLoadAuthList") {

        @Override
        public final void execute(Processor processor) {
            sceHttpLoadAuthList(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpLoadAuthList(processor);";
        }
    };

    public final HLEModuleFunction sceHttpEnableKeepAliveFunction = new HLEModuleFunction("sceHttp", "sceHttpEnableKeepAlive") {

        @Override
        public final void execute(Processor processor) {
            sceHttpEnableKeepAlive(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpEnableKeepAlive(processor);";
        }
    };

    public final HLEModuleFunction sceHttpEndCacheFunction = new HLEModuleFunction("sceHttp", "sceHttpEndCache") {

        @Override
        public final void execute(Processor processor) {
            sceHttpEndCache(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpEndCache(processor);";
        }
    };

    public final HLEModuleFunction sceHttpSetConnectTimeOutFunction = new HLEModuleFunction("sceHttp", "sceHttpSetConnectTimeOut") {

        @Override
        public final void execute(Processor processor) {
            sceHttpSetConnectTimeOut(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpSetConnectTimeOut(processor);";
        }
    };

    public final HLEModuleFunction sceHttpCreateConnectionFunction = new HLEModuleFunction("sceHttp", "sceHttpCreateConnection") {

        @Override
        public final void execute(Processor processor) {
            sceHttpCreateConnection(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpCreateConnection(processor);";
        }
    };

    public final HLEModuleFunction sceHttpDisableProxyAuthFunction = new HLEModuleFunction("sceHttp", "sceHttpDisableProxyAuth") {

        @Override
        public final void execute(Processor processor) {
            sceHttpDisableProxyAuth(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpDisableProxyAuth(processor);";
        }
    };

    public final HLEModuleFunction sceHttpSetRecvBlockSizeFunction = new HLEModuleFunction("sceHttp", "sceHttpSetRecvBlockSize") {

        @Override
        public final void execute(Processor processor) {
            sceHttpSetRecvBlockSize(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpSetRecvBlockSize(processor);";
        }
    };

    public final HLEModuleFunction sceHttpGetCookieFunction = new HLEModuleFunction("sceHttp", "sceHttpGetCookie") {

        @Override
        public final void execute(Processor processor) {
            sceHttpGetCookie(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpGetCookie(processor);";
        }
    };

    public final HLEModuleFunction sceHttpSetSendTimeOutFunction = new HLEModuleFunction("sceHttp", "sceHttpSetSendTimeOut") {

        @Override
        public final void execute(Processor processor) {
            sceHttpSetSendTimeOut(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpSetSendTimeOut(processor);";
        }
    };

    public final HLEModuleFunction sceHttpSendRequestInCacheFirstModeFunction = new HLEModuleFunction("sceHttp", "sceHttpSendRequestInCacheFirstMode") {

        @Override
        public final void execute(Processor processor) {
            sceHttpSendRequestInCacheFirstMode(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpSendRequestInCacheFirstMode(processor);";
        }
    };

    public final HLEModuleFunction sceHttpCreateTemplateFunction = new HLEModuleFunction("sceHttp", "sceHttpCreateTemplate") {

        @Override
        public final void execute(Processor processor) {
            sceHttpCreateTemplate(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpCreateTemplate(processor);";
        }
    };

    public final HLEModuleFunction sceHttpEnableAuthFunction = new HLEModuleFunction("sceHttp", "sceHttpEnableAuth") {

        @Override
        public final void execute(Processor processor) {
            sceHttpEnableAuth(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpEnableAuth(processor);";
        }
    };

    public final HLEModuleFunction sceHttpSetRedirectCallbackFunction = new HLEModuleFunction("sceHttp", "sceHttpSetRedirectCallback") {

        @Override
        public final void execute(Processor processor) {
            sceHttpSetRedirectCallback(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpSetRedirectCallback(processor);";
        }
    };

    public final HLEModuleFunction sceHttpDeleteRequestFunction = new HLEModuleFunction("sceHttp", "sceHttpDeleteRequest") {

        @Override
        public final void execute(Processor processor) {
            sceHttpDeleteRequest(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpDeleteRequest(processor);";
        }
    };

    public final HLEModuleFunction sceHttpInitCacheFunction = new HLEModuleFunction("sceHttp", "sceHttpInitCache") {

        @Override
        public final void execute(Processor processor) {
            sceHttpInitCache(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpInitCache(processor);";
        }
    };

    public final HLEModuleFunction sceHttpDisableAuthFunction = new HLEModuleFunction("sceHttp", "sceHttpDisableAuth") {

        @Override
        public final void execute(Processor processor) {
            sceHttpDisableAuth(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpDisableAuth(processor);";
        }
    };

    public final HLEModuleFunction sceHttpSetCacheContentLengthMaxSizeFunction = new HLEModuleFunction("sceHttp", "sceHttpSetCacheContentLengthMaxSize") {

        @Override
        public final void execute(Processor processor) {
            sceHttpSetCacheContentLengthMaxSize(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpSetCacheContentLengthMaxSize(processor);";
        }
    };

    public final HLEModuleFunction sceHttpCreateRequestWithURLFunction = new HLEModuleFunction("sceHttp", "sceHttpCreateRequestWithURL") {

        @Override
        public final void execute(Processor processor) {
            sceHttpCreateRequestWithURL(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpCreateRequestWithURL(processor);";
        }
    };

    public final HLEModuleFunction sceHttpSendRequestFunction = new HLEModuleFunction("sceHttp", "sceHttpSendRequest") {

        @Override
        public final void execute(Processor processor) {
            sceHttpSendRequest(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpSendRequest(processor);";
        }
    };

    public final HLEModuleFunction sceHttpAbortRequestFunction = new HLEModuleFunction("sceHttp", "sceHttpAbortRequest") {

        @Override
        public final void execute(Processor processor) {
            sceHttpAbortRequest(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpAbortRequest(processor);";
        }
    };

    public final HLEModuleFunction sceHttpChangeHttpVersionFunction = new HLEModuleFunction("sceHttp", "sceHttpChangeHttpVersion") {

        @Override
        public final void execute(Processor processor) {
            sceHttpChangeHttpVersion(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpChangeHttpVersion(processor);";
        }
    };

    public final HLEModuleFunction sceHttpDisableKeepAliveFunction = new HLEModuleFunction("sceHttp", "sceHttpDisableKeepAlive") {

        @Override
        public final void execute(Processor processor) {
            sceHttpDisableKeepAlive(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpDisableKeepAlive(processor);";
        }
    };

    public final HLEModuleFunction sceHttpSetResHeaderMaxSizeFunction = new HLEModuleFunction("sceHttp", "sceHttpSetResHeaderMaxSize") {

        @Override
        public final void execute(Processor processor) {
            sceHttpSetResHeaderMaxSize(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpSetResHeaderMaxSize(processor);";
        }
    };

    public final HLEModuleFunction sceHttpDisableCacheFunction = new HLEModuleFunction("sceHttp", "sceHttpDisableCache") {

        @Override
        public final void execute(Processor processor) {
            sceHttpDisableCache(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpDisableCache(processor);";
        }
    };

    public final HLEModuleFunction sceHttpEnableProxyAuthFunction = new HLEModuleFunction("sceHttp", "sceHttpEnableProxyAuth") {

        @Override
        public final void execute(Processor processor) {
            sceHttpEnableProxyAuth(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpEnableProxyAuth(processor);";
        }
    };

    public final HLEModuleFunction sceHttpCreateConnectionWithURLFunction = new HLEModuleFunction("sceHttp", "sceHttpCreateConnectionWithURL") {

        @Override
        public final void execute(Processor processor) {
            sceHttpCreateConnectionWithURL(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpCreateConnectionWithURL(processor);";
        }
    };

    public final HLEModuleFunction sceHttpGetNetworkErrnoFunction = new HLEModuleFunction("sceHttp", "sceHttpGetNetworkErrno") {

        @Override
        public final void execute(Processor processor) {
            sceHttpGetNetworkErrno(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpGetNetworkErrno(processor);";
        }
    };

    public final HLEModuleFunction sceHttpGetProxyFunction = new HLEModuleFunction("sceHttp", "sceHttpGetProxy") {

        @Override
        public final void execute(Processor processor) {
            sceHttpGetProxy(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpGetProxy(processor);";
        }
    };

    public final HLEModuleFunction sceHttpGetAllHeaderFunction = new HLEModuleFunction("sceHttp", "sceHttpGetAllHeader") {

        @Override
        public final void execute(Processor processor) {
            sceHttpGetAllHeader(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpGetAllHeader(processor);";
        }
    };

    public final HLEModuleFunction sceHttpSaveAuthListFunction = new HLEModuleFunction("sceHttp", "sceHttpSaveAuthList") {

        @Override
        public final void execute(Processor processor) {
            sceHttpSaveAuthList(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpSaveAuthList(processor);";
        }
    };

    public final HLEModuleFunction sceHttpReadDataFunction = new HLEModuleFunction("sceHttp", "sceHttpReadData") {

        @Override
        public final void execute(Processor processor) {
            sceHttpReadData(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpReadData(processor);";
        }
    };

    public final HLEModuleFunction sceHttpSetProxyFunction = new HLEModuleFunction("sceHttp", "sceHttpSetProxy") {

        @Override
        public final void execute(Processor processor) {
            sceHttpSetProxy(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpSetProxy(processor);";
        }
    };

    public final HLEModuleFunction sceHttpLoadSystemCookieFunction = new HLEModuleFunction("sceHttp", "sceHttpLoadSystemCookie") {

        @Override
        public final void execute(Processor processor) {
            sceHttpLoadSystemCookie(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpLoadSystemCookie(processor);";
        }
    };

    public final HLEModuleFunction sceHttpSetMallocFunctionFunction = new HLEModuleFunction("sceHttp", "sceHttpSetMallocFunction") {

        @Override
        public final void execute(Processor processor) {
            sceHttpSetMallocFunction(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpSetMallocFunction(processor);";
        }
    };

    public final HLEModuleFunction sceHttpDeleteTemplateFunction = new HLEModuleFunction("sceHttp", "sceHttpDeleteTemplate") {

        @Override
        public final void execute(Processor processor) {
            sceHttpDeleteTemplate(processor);
        }

        @Override
        public final String compiledString() {
            return "jpcsp.HLE.Modules.sceHttpModule.sceHttpDeleteTemplate(processor);";
        }
    };
}