package jpcsp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import jpcsp.Debugger.ElfHeaderInfo;
import jpcsp.format.DeferredStub;
import jpcsp.format.Elf32;
import jpcsp.format.Elf32Header;
import jpcsp.format.Elf32ProgramHeader;
import jpcsp.format.Elf32Relocate;
import jpcsp.format.Elf32SectionHeader;
import jpcsp.format.Elf32StubHeader;
import jpcsp.format.PBP;
import jpcsp.format.PSP;
import jpcsp.format.PSPModuleInfo;
import jpcsp.HLE.pspSysMem;
import jpcsp.util.Utilities;

public class Loader {

    private static Loader instance;

    // TODO move to another class, and delete getInstance
    private List<ModuleContext> moduleList;

    private boolean loadedFirstModule;

    // Format bits
    public final static int FORMAT_UNKNOWN  = 0x00;
    public final static int FORMAT_ELF      = 0x01;
    public final static int FORMAT_PRX      = 0x02;
    public final static int FORMAT_PBP      = 0x04;
    public final static int FORMAT_PSP      = 0x08;

    public static Loader getInstance() {
        if (instance == null)
            instance = new Loader();
        return instance;
    }

    private Loader() {
    }


    public void Initialise() {
        moduleList = new LinkedList<ModuleContext>();
        loadedFirstModule = false;
    }

    /**
     * @param pspfilename   Example:
     *                      ms0:/PSP/GAME/xxx/EBOOT.PBP
     *                      disc0:/PSP_GAME/SYSDIR/BOOT.BIN
     *                      disc0:/PSP_GAME/SYSDIR/EBOOT.BIN
     *                      xxx:/yyy/zzz.prx
     * @param baseAddress   should be at least 64-byte aligned,
     *                      or how ever much is the default alignment in pspsysmem.
     * @return true         on success */
    public ModuleContext LoadModule(String pspfilename, ByteBuffer f, int baseAddress) throws IOException {
        ModuleContext module = new ModuleContext();

        // init context
        int currentOffset = f.position();
        module.fileFormat = FORMAT_UNKNOWN;
        module.pspfilename = pspfilename;

        // safety check
        if (f.capacity() - f.position() == 0) {
            Emulator.log.error("LoadModule: no data.");
            return module;
        }

        // chain loaders
        do {
            f.position(currentOffset);
            if (LoadPBP(f, module, baseAddress))
                currentOffset = f.position();

            f.position(currentOffset);
            if (LoadPSP(f, module, baseAddress))
                break;

            f.position(currentOffset);
            if (LoadELF(f, module, baseAddress))
                break;

            f.position(currentOffset);
            LoadUNK(f, module, baseAddress);
        } while(false);

        return module;
    }

    /** @return true on success */
    private boolean LoadPBP(ByteBuffer f, ModuleContext module, int baseAddress) throws IOException {
        PBP pbp = new PBP(f);
        if (pbp.isValid()) {
            module.fileFormat |= FORMAT_PBP;

            // Dump PSF info
            if (pbp.getOffsetParam() > 0) {
                Emulator.log.info("PBP meta data :\n" + pbp.readPSF(f));
            }

            // Dump unpacked PBP
            if (Settings.getInstance().readBool("emu.pbpunpack")) {
                PBP.unpackPBP(f);
            }

            // Save PBP info for debugger
            ElfHeaderInfo.PbpInfo = pbp.toString();

            // Setup position for chaining loaders
            f.position((int)pbp.getOffsetPspData());
            //Emulator.log.debug("Loader: PBP loaded");
            return true;
        } else {
            // Not a valid PBP
            //Emulator.log.debug("Loader: Not a PBP");
            return false;
        }
    }

    /** @return true on success */
    private boolean LoadPSP(ByteBuffer f, ModuleContext module, int baseAddress) throws IOException {
        PSP psp = new PSP(f);
        if (psp.isValid()) {
            module.fileFormat |= FORMAT_PSP;
            Emulator.log.warn("Encrypted file not supported!");
            return true;
        } else {
            // Not a valid PSP
            return false;
        }
    }

    /** @return true on success */
    private boolean LoadELF(ByteBuffer f, ModuleContext module, int baseAddress) throws IOException {
        int elfOffset = f.position();
        Elf32 elf = new Elf32(f);
        if (elf.getHeader().isValid()) {
            module.fileFormat |= FORMAT_ELF;

            if (!elf.getHeader().isMIPSExecutable()) {
                Emulator.log.error("Loader NOT a MIPS executable");
                return false;
            }

            if (elf.getHeader().isPRXDetected()) {
                Emulator.log.info("Loader: Relocation required (PRX)");
                module.fileFormat |= FORMAT_PRX;
            } else if (elf.getHeader().requiresRelocation()) {
                // seen in .elf's generated by pspsdk with BUILD_PRX=1 before conversion to .prx
                Emulator.log.info("Loader: Relocation required (ELF)");
            } else {
                //Emulator.log.debug("Relocation NOT required");

                // After the user chooses a game to run and we load it, then
                // we can't load another PBP at the same time. We can only load
                // relocatable modules (PRX's) after the user loaded app.
                if (baseAddress > 0x08900000)
                    Emulator.log.warn("Loader: Probably trying to load PBP ELF while another PBP ELF is already loaded");

                baseAddress = 0;
            }

            module.baseAddress = baseAddress;
            module.entryAddress = baseAddress + (int)elf.getHeader().getE_entry();

            // Load into mem
            LoadELFSections(f, module, baseAddress, elf, elfOffset);

            // Load .rodata.sceModuleInfo (TODO after relocation?)
            LoadELFModuleInfo(f, module, baseAddress, elf, elfOffset);

            // Relocate PRX
            if (elf.getHeader().requiresRelocation()) {
                relocatePRX(f, module, baseAddress, elf, elfOffset);
            }

            // Save imports
            LoadELFImports(module, baseAddress, elf);
            // Save exports
            // TODO LoadELFExports(module, baseAddress, elf);

            // Try to fixup imports for ALL modules
            moduleList.add(module);
            ProcessUnresolvedImports();

            // Save some debugger stuff
            LoadELFDebuggerInfo(f, module, baseAddress, elf, elfOffset);

            loadedFirstModule = true;
            //Emulator.log.debug("Loader: ELF loaded");
            return true;
        } else {
            // Not a valid ELF
            Emulator.log.debug("Loader: Not a ELF");
            return false;
        }
    }

    /** Dummy loader for unrecognized file formats, put at the end of a loader chain.
     * @return true on success */
    private boolean LoadUNK(ByteBuffer f, ModuleContext module, int baseAddress) throws IOException {
        Emulator.log.info("Unrecognized file format");

        // print some debug info
        byte m0 = f.get();
        byte m1 = f.get();
        byte m2 = f.get();
        byte m3 = f.get();
        Emulator.log.info(String.format("File magic %02X %02X %02X %02X", m0, m1, m2, m3));

        return false;
    }

    // ELF Loader

    /** Load some sections into memory */
    private void LoadELFSections(ByteBuffer f, ModuleContext module, int baseAddress,
        Elf32 elf, int elfOffset) throws IOException {

        List<Elf32SectionHeader> sectionHeaderList = elf.getListSectionHeader();
        ByteBuffer mainmemory = Memory.getInstance().mainmemory;

        // Note: baseAddress is 0 unless we are loading a PRX
        int loadAddressLow = ((int)baseAddress != 0) ? (int)baseAddress : 0x08900000;
        int loadAddressHigh = (int)baseAddress;

        for (Elf32SectionHeader shdr : sectionHeaderList) {
            if ((shdr.getSh_flags() & Elf32SectionHeader.SHF_ALLOCATE) == Elf32SectionHeader.SHF_ALLOCATE) {
                switch (shdr.getSh_type()) {
                case Elf32SectionHeader.SHT_PROGBITS: // 1
                    f.position((int)(elfOffset + shdr.getSh_offset()));
                    int offsettoread = (int)(baseAddress + shdr.getSh_addr() - MemoryMap.START_RAM);

                    // Load this section into memory
                    Utilities.copyByteBuffertoByteBuffer(f, mainmemory, offsettoread, (int)shdr.getSh_size());

                    // Update memory area consumed by the module
                    if ((int)(baseAddress + shdr.getSh_addr()) < loadAddressLow) {
                        loadAddressLow = (int)(baseAddress + shdr.getSh_addr());
                    }
                    if ((int)(baseAddress + shdr.getSh_addr() + shdr.getSh_size()) > loadAddressHigh) {
                        loadAddressHigh = (int)(baseAddress + shdr.getSh_addr() + shdr.getSh_size());
                    }
                    break;
                case Elf32SectionHeader.SHT_NOBITS: // 8
                    offsettoread = (int)(baseAddress + shdr.getSh_addr() - MemoryMap.START_RAM);
                    if (offsettoread >= 0 && offsettoread < MemoryMap.SIZE_RAM) {
                        byte[] all = mainmemory.array();
                        // Zero out this portion of memory
                        Arrays.fill(all,
                            offsettoread + mainmemory.arrayOffset(),
                            offsettoread + mainmemory.arrayOffset() + (int)shdr.getSh_size(),
                            (byte)0x0);

                        // Update memory area consumed by the module
                        if ((int)(baseAddress + shdr.getSh_addr()) < loadAddressLow) {
                            loadAddressLow = (int)(baseAddress + shdr.getSh_addr());
                        }
                        if ((int)(baseAddress + shdr.getSh_addr() + shdr.getSh_size()) > loadAddressHigh) {
                            loadAddressHigh = (int)(baseAddress + shdr.getSh_addr() + shdr.getSh_size());
                        }
                    } else {
                        Memory.log.warn("elf section type 8 attempting to allocate memory outside valid range 0x"
                            + Integer.toHexString((int)(baseAddress + shdr.getSh_addr())));
                    }
                    break;
                }
            }
        }

        // Mark the area of memory the module loaded into as used
        Memory.log.debug("Reserving " + (loadAddressHigh - loadAddressLow) + " bytes at "
            + String.format("%08x", loadAddressLow)
            + " for module '" + module.pspfilename + "'");

        pspSysMem SysMemUserForUserModule = pspSysMem.get_instance();
        int addr = SysMemUserForUserModule.malloc(2, pspSysMem.PSP_SMEM_Addr, loadAddressHigh - loadAddressLow, loadAddressLow);
        if (addr != loadAddressLow) {
            Memory.log.warn("Failed to properly reserve memory consumed by module " + module.moduleInfo.getM_namez()
                + " at address 0x" + Integer.toHexString(loadAddressLow) + " size " + (loadAddressHigh - loadAddressLow));
        }
        SysMemUserForUserModule.addSysMemInfo(2, module.moduleInfo.getM_namez(), pspSysMem.PSP_SMEM_Low, loadAddressHigh - loadAddressLow, loadAddressLow);
        module.loadAddressLow = loadAddressLow;
        module.loadAddressHigh = loadAddressHigh;
    }

    private void LoadELFModuleInfo(ByteBuffer f, ModuleContext module, int baseAddress,
        Elf32 elf, int elfOffset) throws IOException {

        Elf32SectionHeader shdr = elf.getSectionHeader(".rodata.sceModuleInfo");

        if (shdr != null) {
            f.position((int)(elfOffset + shdr.getSh_offset()));
            module.moduleInfo.read(f);
            //System.out.println(Long.toHexString(moduleinfo.m_gp));

            Emulator.log.info("Found ModuleInfo name:'" + module.moduleInfo.getM_namez()
                + "' version:" + String.format("%04x", module.moduleInfo.getM_version())
                + " attr:" + String.format("%08x", module.moduleInfo.getM_attr()));

            if ((module.moduleInfo.getM_attr() & 0x1000) != 0) {
                Emulator.log.warn("Kernel mode module detected");
            }
            if ((module.moduleInfo.getM_attr() & 0x0800) != 0) {
                Emulator.log.warn("VSH mode module detected");
            }
        } else {
            Emulator.log.error("ModuleInfo not found!");

            /* alternate method of locating .rodata.sceModuleInfo, only works on PRX's
            if (elf.getHeader().isPRXDetected()) {
                int diff = (int)((elf.getProgramHeader(0).getP_paddr() & 0x7fffffffL) - shdr.getSh_offset());
                Emulator.log.debug("SH#ModuleInfo sh_addr " + String.format("%08x", (int)shdr.getSh_addr()));
                Emulator.log.debug("SH#ModuleInfo sh_offset " + String.format("%08x", (int)shdr.getSh_offset()));
                Emulator.log.debug("PH#0 p_addr " + String.format("%08x", (int)elf.getProgramHeader(0).getP_paddr()));
                Emulator.log.debug("DIFF " + String.format("%08x", diff));
            }
            */
        }
    }

    public void relocatePRX(ByteBuffer f, ModuleContext module, int baseAddress,
        Elf32 elf, int elfOffset) throws IOException {

        // Relocation
        final boolean logRelocations = false;
        //boolean logRelocations = true;

        for (Elf32SectionHeader shdr : elf.getListSectionHeader()) {
            if (shdr.getSh_type() == Elf32SectionHeader.SHT_REL) {
                Memory.log.warn(shdr.getSh_namez() + ": not relocating section");
            }

            if (shdr.getSh_type() == Elf32SectionHeader.SHT_PRXREL /*|| // 0x700000A0
                shdr.getSh_type() == Elf32SectionHeader.SHT_REL*/) // 0x00000009
            {
                Elf32Relocate rel = new Elf32Relocate();
                f.position((int)(elfOffset + shdr.getSh_offset()));

                int RelCount = (int) shdr.getSh_size() / Elf32Relocate.sizeof();
                Memory.log.debug(shdr.getSh_namez() + ": relocating " + RelCount + " entries");

                int AHL = 0; // (AHI << 16) | (ALO & 0xFFFF)

                //int HI_addr = 0; // We'll use this to relocate R_MIPS_HI16 when we get a R_MIPS_LO16
                List<Integer> deferredHi16 = new LinkedList<Integer>();

                for (int i = 0; i < RelCount; i++) {
                    rel.read(f);

                    int R_TYPE    = (int)( rel.getR_info()        & 0xFF);
                    int OFS_BASE  = (int)((rel.getR_info() >>  8) & 0xFF);
                    int ADDR_BASE = (int)((rel.getR_info() >> 16) & 0xFF);
                    //System.out.println("type=" + R_TYPE + ",base=" + OFS_BASE + ",addr=" + ADDR_BASE + "");

                    int phOffset     = (int)elf.getProgramHeader(OFS_BASE).getP_vaddr();
                    int phBaseOffset = (int)elf.getProgramHeader(ADDR_BASE).getP_vaddr();

                    // Address of data to relocate
                    int data_addr = (int)(baseAddress + rel.getR_offset() + phOffset);
                    // Value of data to relocate
                    int data = Memory.getInstance().read32(data_addr);
                    long result = 0; // Used to hold the result of relocation, OR this back into data

                    // these are the addends?
                    // SysV ABI MIPS quote: "Because MIPS uses only Elf32_Rel re-location entries, the relocated field holds the addend."
                    int half16 = data & 0x0000FFFF; // 31/07/08 unused (fiveofhearts)

                    int word32 = data & 0xFFFFFFFF; // <=> data;
                    int targ26 = data & 0x03FFFFFF;
                    int hi16 = data & 0x0000FFFF;
                    int lo16 = data & 0x0000FFFF;
                    int rel16 = data & 0x0000FFFF;

                    int A = 0; // addend
                    // moved outside the loop so context is saved
                    //int AHL = 0; // (AHI << 16) | (ALO & 0xFFFF)

                    int S = (int) baseAddress + phBaseOffset;
                    int GP = (int) baseAddress + (int) module.moduleInfo.getM_gp(); // final gp value, computed correctly? 31/07/08 only used in R_MIPS_GPREL16 which is untested (fiveofhearts)

                    switch (R_TYPE) {
                        case 0: //R_MIPS_NONE
                            // Don't do anything
                            if (logRelocations)
                                Memory.log.warn("R_MIPS_NONE addr=" + String.format("%08x", data_addr));
                            break;

                        case 5: //R_MIPS_HI16
                            A = hi16;
                            AHL = A << 16;
                            //HI_addr = data_addr;
                            deferredHi16.add(data_addr);
                            if (logRelocations) Memory.log.debug("R_MIPS_HI16 addr=" + String.format("%08x", data_addr));
                            break;

                        case 6: //R_MIPS_LO16
                            A = lo16;
                            AHL &= ~0x0000FFFF; // delete lower bits, since many R_MIPS_LO16 can follow one R_MIPS_HI16

                            AHL |= A & 0x0000FFFF;

                            result = AHL + S;
                            data &= ~0x0000FFFF;
                            data |= result & 0x0000FFFF; // truncate

                            // Process deferred R_MIPS_HI16
                            for (Iterator<Integer> it = deferredHi16.iterator(); it.hasNext();) {
                                int data_addr2 = it.next();
                                int data2 = Memory.getInstance().read32(data_addr2);

                                result = ((data2 & 0x0000FFFF) << 16) + A + S;
                                // The low order 16 bits are always treated as a signed
                                // value. Therefore, a negative value in the low order bits
                                // requires an adjustment in the high order bits. We need
                                // to make this adjustment in two ways: once for the bits we
                                // took from the data, and once for the bits we are putting
                                // back in to the data.
                                if ((A & 0x8000) != 0)
                                {
                                     result -= 0x10000;
                                }
                                if ((result & 0x8000) != 0)
                                {
                                     result += 0x10000;
                                }
                                data2 &= ~0x0000FFFF;
                                data2 |= (result >> 16) & 0x0000FFFF; // truncate


                                if (logRelocations)  {
                                    Memory.log.debug("R_MIPS_HILO16 addr=" + String.format("%08x", data_addr2)
                                        + " data2 before=" + Integer.toHexString(Memory.getInstance().read32(data_addr2))
                                        + " after=" + Integer.toHexString(data2));
                                }
                                Memory.getInstance().write32(data_addr2, data2);
                                it.remove();
                            }

                            if (logRelocations)  {
                                Memory.log.debug("R_MIPS_LO16 addr=" + String.format("%08x", data_addr) + " data before=" + Integer.toHexString(word32)
                                    + " after=" + Integer.toHexString(data));
                            }
                            break;

                        case 4: //R_MIPS_26
                            A = targ26;

                            // docs say "sign-extend(A < 2)", but is it meant to be A << 2? if so then there's no point sign extending
                            //result = (sign-extend(A < 2) + S) >> 2;
                            //result = (((A < 2) ? 0xFFFFFFFF : 0x00000000) + S) >> 2;
                            result = ((A << 2) + S) >> 2; // copied from soywiz/pspemulator

                            data &= ~0x03FFFFFF;
                            data |= (int) (result & 0x03FFFFFF); // truncate

                            if (logRelocations) {
                                Memory.log.debug("R_MIPS_26 addr=" + String.format("%08x", data_addr) + " before=" + Integer.toHexString(word32)
                                    + " after=" + Integer.toHexString(data));
                            }
                            break;

                        case 2: //R_MIPS_32
                            data += S;

                            if (logRelocations) {
                                Memory.log.debug("R_MIPS_32 addr=" + String.format("%08x", data_addr) + " before=" + Integer.toHexString(word32)
                                    + " after=" + Integer.toHexString(data));
                            }
                            break;

                        /* sample before relocation: 0x00015020: 0x8F828008 '....' - lw         $v0, -32760($gp)
                        case 7: //R_MIPS_GPREL16
                            // 31/07/08 untested (fiveofhearts)
                            Memory.log.warn("Untested relocation type " + R_TYPE + " at " + String.format("%08x", data_addr));

                            A = rel16;

                            //result = sign-extend(A) + S + GP;
                            result = (((A & 0x00008000) != 0) ? A | 0xFFFF0000 : A) + S + GP;

                            // verify
                            if ((result & ~0x0000FFFF) != 0) {
                                //throw new IOException("Relocation overflow (R_MIPS_GPREL16)");
                                Memory.log.warn("Relocation overflow (R_MIPS_GPREL16)");
                            }

                            data &= ~0x0000FFFF;
                            data |= (int)(result & 0x0000FFFF);

                            break;
                        /* */

                        default:
                            Memory.log.warn("Unhandled relocation type " + R_TYPE + " at " + String.format("%08x", data_addr));
                            break;
                    }

                    //System.out.println("Relocation type " + R_TYPE + " at " + String.format("%08x", (int)baseAddress + (int)rel.r_offset));
                    Memory.getInstance().write32(data_addr, data);
                }
            }
        }
    }

    private void ProcessUnresolvedImports() {
        Memory mem = Memory.getInstance();
        NIDMapper nidMapper = NIDMapper.get_instance();
        int numberoffailedNIDS = 0;
        int numberofmappedNIDS = 0;

        for (ModuleContext module : moduleList) {
            module.importFixupAttempts++;
            for (Iterator<DeferredStub> it = module.unresolvedImports.iterator(); it.hasNext(); ) {
                DeferredStub deferredStub = it.next();
                String moduleName = deferredStub.getModuleName();
                int nid           = deferredStub.getNid();
                int importAddress = deferredStub.getImportAddress();
                int exportAddress;

                // Attempt to fixup stub to point to an already loaded module export
                exportAddress = nidMapper.moduleNidToAddress(moduleName, nid);
                if (exportAddress != -1)
                {
                    int instruction = // j <jumpAddress>
                        ((jpcsp.AllegrexOpcodes.J & 0x3f) << 26)
                        | ((exportAddress >>> 2) & 0x03ffffff);

                    mem.write32(importAddress, instruction);
                    it.remove();
                    numberofmappedNIDS++;

                    Emulator.log.debug(String.format("Mapped import at 0x%08X to export at 0x%08X [0x%08X] (attempt %d)",
                        importAddress, exportAddress, nid, module.importFixupAttempts));
                }

                else
                {
                    // Attempt to fixup stub to known syscalls
                    int code = nidMapper.nidToSyscall(nid);
                    if (code != -1)
                    {
                        // Fixup stub, replacing nop with syscall
                        int instruction = // syscall <code>
                            ((jpcsp.AllegrexOpcodes.SPECIAL & 0x3f) << 26)
                            | (jpcsp.AllegrexOpcodes.SYSCALL & 0x3f)
                            | ((code & 0x000fffff) << 6);

                        mem.write32(importAddress + 4, instruction);
                        it.remove();
                        numberofmappedNIDS++;

                        // Don't spam mappings on the first module (the one the user loads)
                        if (loadedFirstModule) {
                            Emulator.log.debug(String.format("Mapped import at 0x%08X to syscall 0x%05X [0x%08X] (attempt %d)",
                                importAddress, code, nid, module.importFixupAttempts));
                        }
                    }

                    // Save for later
                    else
                    {
                        Emulator.log.warn(String.format("Failed to map import at 0x%08X [0x%08X] (attempt %d)",
                            importAddress, nid, module.importFixupAttempts));
                        numberoffailedNIDS++;
                    }
                }
            }
        }

        Emulator.log.info(numberofmappedNIDS + " NIDS mapped");
        if (numberoffailedNIDS > 0)
            Emulator.log.info(numberoffailedNIDS + " remaining unmapped NIDS");
    }

    /* Loads from memory */
    private void LoadELFImports(ModuleContext module, int baseAddress, Elf32 elf) throws IOException {

        Elf32SectionHeader shdr = elf.getSectionHeader(".lib.stub");
        if (shdr == null) {
            Emulator.log.warn("Failed to find .lib.stub section");
            return;
        }

        Memory mem = Memory.getInstance();
        int stubHeadersAddress = (int)(baseAddress + shdr.getSh_addr());
        int stubHeadersCount = (int)(shdr.getSh_size() / Elf32StubHeader.sizeof());

        //System.out.println(shdr.getSh_namez() + ":" + stubsCount + " module entries");

        // n modules to import, 1 stub header per module to import
        for (int i = 0; i < stubHeadersCount; i++)
        {
            Elf32StubHeader stubHeader = new Elf32StubHeader(mem, stubHeadersAddress);
            stubHeader.setModuleNamez(Utilities.readStringNZ(mem.mainmemory, (int)(stubHeader.getOffsetModuleName() - MemoryMap.START_RAM), 64));
            stubHeadersAddress += Elf32StubHeader.sizeof(); //stubHeader.s_size * 4;
            //System.out.println(stubHeader.toString());

            // n stubs per module to import
            for (int j = 0; j < stubHeader.getImports(); j++)
            {
                int nid = mem.read32((int)(stubHeader.getOffsetNid() + j * 4));
                int importAddress = (int)(stubHeader.getOffsetText() + j * 8);
                DeferredStub deferredStub = new DeferredStub(stubHeader.getModuleNamez(), importAddress, nid);
                module.unresolvedImports.add(deferredStub);

                // Add a 0xfffff syscall so we can detect if an unresolved import is called
                int instruction = // syscall <code>
                    ((jpcsp.AllegrexOpcodes.SPECIAL & 0x3f) << 26)
                    | (jpcsp.AllegrexOpcodes.SYSCALL & 0x3f)
                    | ((0xfffff & 0x000fffff) << 6);

                mem.write32(importAddress + 4, instruction);
            }
        }

        Emulator.log.info("Found " + module.unresolvedImports.size() + " imports from " + stubHeadersCount + " modules");
    }

    private void LoadELFDebuggerInfo(ByteBuffer f, ModuleContext module, int baseAddress,
        Elf32 elf, int elfOffset) throws IOException {

        // Save executable section address/size for the debugger/instruction counter
        Elf32SectionHeader shdr;

        shdr = elf.getSectionHeader(".text");
        if (shdr != null)
        {
            module.textsection[0] = (int)(baseAddress + shdr.getSh_addr());
            module.textsection[1] = (int)shdr.getSh_size();
        }

        shdr = elf.getSectionHeader(".init");
        if (shdr != null)
        {
            module.initsection[0] = (int)(baseAddress + shdr.getSh_addr());
            module.initsection[1] = (int)shdr.getSh_size();
        }

        shdr = elf.getSectionHeader(".fini");
        if (shdr != null)
        {
            module.finisection[0] = (int)(baseAddress + shdr.getSh_addr());
            module.finisection[1] = (int)shdr.getSh_size();
        }

        shdr = elf.getSectionHeader(".sceStub.text");
        if (shdr != null)
        {
            module.stubtextsection[0] = (int)(baseAddress + shdr.getSh_addr());
            module.stubtextsection[1] = (int)shdr.getSh_size();
        }

        // test the instruction counter
        //if (/*shdr.getSh_namez().equals(".text") || */shdr.getSh_namez().equals(".init") /*|| shdr.getSh_namez().equals(".fini")*/) {
        /*
           int sectionAddress = (int)(baseAddress + shdr.getSh_addr());
           System.out.println(Integer.toHexString(sectionAddress) + " size = " + shdr.getSh_size());
           for(int i =0; i< shdr.getSh_size(); i+=4)
           {
             int memread32 = Memory.get_instance().read32(sectionAddress+i);
             //System.out.println(memread32);
             jpcsp.Allegrex.Decoder.instruction(memread32).increaseCount();
           }


        }
        System.out.println(jpcsp.Allegrex.Instructions.ADDIU.getCount());
        */

        // Set ELF info in the debugger
        ElfHeaderInfo.ElfInfo = elf.getElfInfo();
        ElfHeaderInfo.ProgInfo = elf.getProgInfo();
        ElfHeaderInfo.SectInfo = elf.getSectInfo();
    }
}
