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
package jpcsp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import jpcsp.format.DeferredStub;
import jpcsp.format.Elf32;
import jpcsp.format.Elf32Header;
import jpcsp.format.Elf32ProgramHeader;
import jpcsp.format.Elf32SectionHeader;
import jpcsp.format.Elf32SectionHeader.ShFlags;
import jpcsp.format.Elf32SectionHeader.ShType;
import jpcsp.format.PBP;
import jpcsp.format.PSP;
import jpcsp.format.PSPModuleInfo;
import jpcsp.util.Utilities;

public class FileManager {
    public static String ElfInfo, ProgInfo, PbpInfo, SectInfo; // TODO : think a better way

    private PSPModuleInfo moduleInfo;
    private PBP pbp;
    private Elf32 elf;
    private PSP psp;
    private ByteBuffer actualFile;
    public final static int FORMAT_ELF = 0;
    public final static int FORMAT_PBP = 10;
    public final static int FORMAT_UMD = 20;
    public final static int FORMAT_ISO = 30;
    public final static int FORMAT_PSP = 40;
    private int type = -1;
    private long elfoffset = 0;
    private long baseoffset = 0;
    private int loadAddressLow, loadAddressHigh; // The space consumed by the program image
    private List<DeferredStub> deferredImports;


    public FileManager(ByteBuffer f) throws FileNotFoundException, IOException {
        loadAndDefine(f, 0x08800000);
    }

    public FileManager(ByteBuffer f, long baseoffset) throws FileNotFoundException, IOException {
        loadAndDefine(f, baseoffset);
    }

    public PSPModuleInfo getPSPModuleInfo() {
        return moduleInfo;
    }

    public PBP getPBP() {
        return pbp;
    }

    public Elf32 getElf32() {
        return elf;
    }

    public ByteBuffer getActualFile() {
        return actualFile;
    }

    private void setActualFile(ByteBuffer f) {
        actualFile = f;
    }

    private void loadAndDefine(ByteBuffer f, long relocationBaseoffset) throws FileNotFoundException, IOException {
        setActualFile(f);
        try {
            elfoffset = 0;
            baseoffset = 0;

            loadAddressLow = 0;
            loadAddressHigh = 0;

            moduleInfo = new PSPModuleInfo();
            deferredImports = new LinkedList<DeferredStub>();

            //makes sense put the more used first...

            /*try pbp format*/
            pbp = new PBP(f);
            if (pbp.getOffsetParam() > 0) {
                Emulator.log.info("PBP meta data :\n" + pbp.readPSF(f));
            }
            processPbp(relocationBaseoffset);
            if (getType() == FORMAT_PBP) {
                return;
            }
            /*end try pbp format*/

            /*try encrypted format*/
            f.position(0);
            PSP psp = new PSP(f);
            if (psp.isValid()) {
                Emulator.log.warn("Encrypted file not supported!");
                type = FORMAT_PSP;
                return;
            }

            /*try elf32 format*/
            f.position(0);
            elf = new Elf32(f);
            processElf(relocationBaseoffset);
            if (getType() == FORMAT_ELF) {
                return;
            }
            /*end try elf32 format*/

            /*try xxxx format*/
            /*try xxxx format*/

            //NONE FORMAT SELECTED OR DETECTED :(
            if (f.capacity() == 0) {
                Emulator.log.info("File is empty");
            } else {
                Emulator.log.info("Unrecognized file format");
                f.position(0);

                byte m0 = f.get();
                byte m1 = f.get();
                byte m2 = f.get();
                byte m3 = f.get();
                Emulator.log.info(String.format("File magic %02X %02X %02X %02X", m0, m1, m2, m3));
            }
        } finally {
            // f.close(); // close or let it open...
        }
    }

    public int getType() {
        return type;
    }

    private void processElf(long relocationBaseoffset) throws IOException {
        if (getElf32().getHeader().isValid()) {
            type = FORMAT_ELF;
            readElf32Header(relocationBaseoffset);
            readElfProgramHeaders();
            readElfSectionHeaders();
        } else {
            Emulator.log.debug("NOT AN ELF FILE");
        }
    }

    private void readElf32Header(long relocationBaseoffset) {
        if (!getElf32().getHeader().isMIPSExecutable()) {
            Emulator.log.error("NOT A MIPS executable");
        }

        if (getElf32().getHeader().isPRXDetected()) {
            Emulator.log.info("PRX detected, requires relocation");
            baseoffset = relocationBaseoffset;
        } else if (getElf32().getHeader().requiresRelocation()) {
            // seen in .elf's generated by pspsdk with BUILD_PRX=1 before conversion to .prx
            Emulator.log.info("ELF requires relocation");
            baseoffset = relocationBaseoffset;
        }

        ElfInfo = getElf32().getElfInfo();
    }

    private void processPbp(long relocationBaseoffset) throws IOException {
        if (getPBP().isValid()) {

            if (Settings.getInstance().readBool("emu.pbpunpack")) {
                PBP.unpackPBP(getActualFile());
            }
            elfoffset = getPBP().getOffsetPspData();
            getActualFile().position((int)elfoffset); //seek the new offset

            PbpInfo = getPBP().toString(); //inteast this use PBP.getInfo()

            elf = new Elf32(getActualFile()); //the elf of pbp
            if(!getElf32().getHeader().isValid())//probably not an elf
            {
                getActualFile().position((int)elfoffset); //seek again to elfoffset
                psp = new PSP(getActualFile());
                if(psp.isValid())//check if it is an encrypted file
                {
                    Emulator.log.error("Encrypted psp format. Not Supported!");
                    type = FORMAT_PSP;
                    return;
                }
            }
            getPBP().setElf32(elf); //composite the pbp...

            processElf(relocationBaseoffset);

            type = FORMAT_PBP;
        } else {
            elfoffset = 0;
            getActualFile().position(0);
            getPBP().setInfo("-----NOT A PBP FILE---------\n");
        }
    }

    private void readElfProgramHeaders() throws IOException {
        List<Elf32ProgramHeader> programheaders = new LinkedList<Elf32ProgramHeader>();
        StringBuffer phsb = new StringBuffer();

        for (int i = 0; i < getElf32().getHeader().getE_phnum(); i++) {
            getActualFile().position((int)(elfoffset + getElf32().getHeader().getE_phoff() + (i * getElf32().getHeader().getE_phentsize())));
            Elf32ProgramHeader phdr = new Elf32ProgramHeader(getActualFile());
            programheaders.add(phdr);

            phsb.append("-----PROGRAM HEADER #" + i + "-----" + "\n");
            phsb.append(phdr.toString());

            // yapspd: if the PRX file is a kernel module then the most significant
            // bit must be set in the phsyical address of the first program header.
            if (i == 0 && (phdr.getP_paddr() & 0x80000000L) == 0x80000000L) {
                // kernel mode prx
                Emulator.log.debug("Kernel mode PRX detected");
            }
            ProgInfo = phsb.toString();

            getElf32().setProgInfo(ProgInfo);
            getElf32().setListProgramHeader(programheaders);
        }
    }

    private void readElfSectionHeaders() throws IOException {
        List<Elf32SectionHeader> sectionheaders = new LinkedList<Elf32SectionHeader>(); //use in more than one step

        Elf32SectionHeader shstrtab = null; //use in more than one step

        shstrtab = firstStep(getElf32().getHeader(), getActualFile(), sectionheaders);
        secondStep(sectionheaders, shstrtab, getActualFile(), getPSPModuleInfo());
    }

    private Elf32SectionHeader firstStep(Elf32Header ehdr, ByteBuffer f, List<Elf32SectionHeader> sectionheaders) throws IOException {
        /** Read the ELF section headers (1st pass) */
        getElf32().setListSectionHeader(sectionheaders); //make the connection

        // Note: baseoffset is 0 unless we are loading a PRX
        loadAddressLow = ((int)baseoffset != 0) ? (int)baseoffset : 0x08900000;
        loadAddressHigh = (int)baseoffset;
        //Memory.log.debug("init low " + Integer.toHexString(loadAddressLow));
        //Memory.log.debug("init high " + Integer.toHexString(loadAddressHigh));

        Elf32SectionHeader shstrtab = null;
        for (int i = 0; i < ehdr.getE_shnum(); i++) {
            f.position((int)(elfoffset + ehdr.getE_shoff() + (i * ehdr.getE_shentsize())));
            Elf32SectionHeader shdr = new Elf32SectionHeader(f);
            sectionheaders.add(shdr);

            // Find the shstrtab
            if (shdr.getSh_type() == ShType.STRTAB.getValue() && shstrtab == null) // 0x00000003
            {
                 // Some programs have 2 STRTAB headers,
                // the header with size 1 has to be ignored.
                if (shdr.getSh_size() > 1)
                {
                   shstrtab = shdr;
                }
            }

            // Load some sections into memory
            if ((shdr.getSh_flags() & ShFlags.Allocate.getValue()) == ShFlags.Allocate.getValue()) {
                switch (shdr.getSh_type()) {
                    case 1: //ShType.PROGBITS
                        ///System.out.println("FEED MEMORY WITH IT!");

                        f.position((int)(elfoffset + shdr.getSh_offset()));
                        int offsettoread = (int) getBaseoffset() + (int) shdr.getSh_addr() - MemoryMap.START_RAM;

                        /***************************************
                         * Load a block on main memory ....
                         ***************************************/
                        Utilities.copyByteBuffertoByteBuffer(f, Memory.getInstance().mainmemory, offsettoread, (int) shdr.getSh_size());

                        if ((int)(baseoffset + shdr.getSh_addr()) < loadAddressLow) {
                            loadAddressLow = (int)(baseoffset + shdr.getSh_addr());
                            //Memory.log.debug("sh1 low " + Integer.toHexString(loadAddressLow));
                        }
                        if ((int)(baseoffset + shdr.getSh_addr() + shdr.getSh_size()) > loadAddressHigh) {
                            loadAddressHigh = (int)(baseoffset + shdr.getSh_addr() + shdr.getSh_size());
                            //Memory.log.debug("sh1 high " + Integer.toHexString(loadAddressHigh));
                        }
                        break;
                    case 8: // ShType.NOBITS
                        //System.out.println("NO BITS");
                        // zero out this memory
                        offsettoread = (int)(getBaseoffset() + shdr.getSh_addr() - MemoryMap.START_RAM);
                        if (offsettoread >= 0 && offsettoread < MemoryMap.SIZE_RAM) {
                            Memory mem = Memory.getInstance();
                            byte[] all = mem.mainmemory.array();
                            Arrays.fill(all,
                                offsettoread + mem.mainmemory.arrayOffset(),
                                offsettoread + mem.mainmemory.arrayOffset() + (int)shdr.getSh_size(),
                                (byte)0x0);

                            if ((int)(baseoffset + shdr.getSh_addr()) < loadAddressLow) {
                                loadAddressLow = (int)(baseoffset + shdr.getSh_addr());
                                //Memory.log.debug("sh8 low " + Integer.toHexString(loadAddressLow));
                            }
                            if ((int)(baseoffset + shdr.getSh_addr() + shdr.getSh_size()) > loadAddressHigh) {
                                loadAddressHigh = (int)(baseoffset + shdr.getSh_addr() + shdr.getSh_size());
                                //Memory.log.debug("sh8 high " + Integer.toHexString(loadAddressHigh));
                            }
                        } else {
                            Memory.log.warn("elf section type 8 attempting to allocate memory outside valid range 0x"
                                + Integer.toHexString((int)(getBaseoffset() + shdr.getSh_addr())));
                        }
                        break;
                }
            }
        }

        //System.out.println("load image low=" + Integer.toHexString(loadAddressLow)
        //            + " high=" + Integer.toHexString(loadAddressHigh) + "");

        return shstrtab;
    }

    private void secondStep(List<Elf32SectionHeader> sectionheaders, Elf32SectionHeader shstrtab, ByteBuffer f, PSPModuleInfo moduleinfo) throws IOException {
        // 2nd pass generate info string for the GUI and get module infos
        //moduleinfo = new PSPModuleInfo(); moved to loadAndDefine()
        StringBuffer shsb = new StringBuffer();
        int SectionCounter = 0;
        for (Elf32SectionHeader shdr : sectionheaders) {
            // Number the section
            shsb.append("-----SECTION HEADER #" + SectionCounter + "-----" + "\n");

            // Resolve section name (if possible)
            if (shstrtab != null) {
                int position = (int)(elfoffset + shstrtab.getSh_offset() + shdr.getSh_name());
                if (position < f.limit()) {
                    f.position(position);
                } else {
                    Emulator.log.error("Section " + (SectionCounter + 1) + "/" + sectionheaders.size()
                        + " beyond end of file " + position + " >= " + f.limit());
                    break;
                }

                String SectionName = "";//Utilities.readStringZ(f);
                try {
                    SectionName = Utilities.readStringZ(f);
                }
                catch(IOException e){
                    Emulator.log.error("ERROR:SectionNames can't be found. NIDs can't be load");
                }

                if (SectionName.length() > 0) {

                    shdr.setSh_namez(SectionName);
                    shsb.append(SectionName + "\n");
                    //System.out.println(SectionName);
                    // Get module infos
                    if (SectionName.matches(".rodata.sceModuleInfo")) {
                        f.position((int)((elfoffset + shdr.getSh_offset())));
                        moduleinfo.read(f);
                        //System.out.println(Long.toHexString(moduleinfo.m_gp));

                        Emulator.log.debug("Found ModuleInfo name:'" + moduleinfo.getM_namez()
                            + "' version:" + String.format("%04x", moduleinfo.getM_version())
                            + " attr:" + String.format("%08x", moduleinfo.getM_attr()));

                        /* alternate method of locating .rodata.sceModuleInfo, only works on PRX's
                        if (elf.getHeader().isPRXDetected()) {
                            int diff = (int)((elf.getProgramHeader(0).getP_paddr() & 0x7fffffffL) - shdr.getSh_offset());
                            Emulator.log.debug("SH#ModuleInfo sh_addr " + String.format("%08x", (int)shdr.getSh_addr()));
                            Emulator.log.debug("SH#ModuleInfo sh_offset " + String.format("%08x", (int)shdr.getSh_offset()));
                            Emulator.log.debug("PH#0 p_addr " + String.format("%08x", (int)elf.getProgramHeader(0).getP_paddr()));
                            Emulator.log.debug("DIFF " + String.format("%08x", diff));
                        }
                        */

                        if ((moduleinfo.getM_attr() & 0x1000) != 0) {
                            Emulator.log.debug("Kernel mode module detected");
                        }
                        if ((moduleinfo.getM_attr() & 0x0800) != 0) {
                            Emulator.log.debug("VSH mode module detected");
                        }
                    } else if (SectionName.matches(".rodata.sceResident")) {
                        // We may need this later, for now just print whether this file has this section or not
                        Emulator.log.debug("Found Resident section");
                    }
                }
            }
            // Add this section header's info
            shsb.append(shdr.toString());
            SectionCounter++;
        }
        SectInfo = shsb.toString();

        getElf32().setSectInfo(SectInfo);
    }

    public long getBaseoffset() {
        return baseoffset;
    }

    public long getElfoffset() {
        return elfoffset;
    }

    public int getLoadAddressLow() {
        return loadAddressLow;
    }

    public int getLoadAddressHigh() {
        return loadAddressHigh;
    }

    // TODO process deferred imports each time a new module is loaded
    public void addDeferredImports(List<DeferredStub> list) {
        deferredImports.addAll(list);
    }
}
