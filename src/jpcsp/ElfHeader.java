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

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 *
 * @author George
 */
class ElfHeader {
    
  private static class PBP_Header
  {
     private long p_magic; 
     private long p_version;
     private long p_offset_param_sfo;
     private long p_icon0_png;
     private long offset_icon1_pmf;
     private long offset_pic0_png;
     private long offset_pic1_png;
     private long offset_snd0_at3;
     private long offset_psp_data;
     private long offset_psar_data;
    private void read (RandomAccessFile f) throws IOException
    {
     p_magic = readUWord(f); 
     p_version = readUWord(f);
     p_offset_param_sfo = readUWord(f);
     p_icon0_png = readUWord(f);
     offset_icon1_pmf = readUWord(f);
     offset_pic0_png = readUWord(f);
     offset_pic1_png = readUWord(f);
     offset_snd0_at3 = readUWord(f);
     offset_psp_data = readUWord(f);
     offset_psar_data = readUWord(f);
    }
  }
  private static class Elf32_Ehdr
  { 
    private long e_magic;
    private int e_class;
    private int e_data;
    private int e_idver;
    private byte[] e_pad=new byte[9];
    private int e_type;
    private int e_machine;
    private long e_version;
    private long e_entry;
    private long e_phoff;
    private long e_shoff;
    private long e_flags; 
    private int e_ehsize;
    private int e_phentsize;
    private int e_phnum;
    private int e_shentsize;
    private int e_shnum;
    private int e_shstrndx;
    
    private void read (RandomAccessFile f) throws IOException
    {
      e_magic = readUWord(f);
      e_class = readUByte(f);
      e_data = readUByte(f);
      e_idver = readUByte(f);
      f.readFully(e_pad);
      e_type = readUHalf(f);
      e_machine = readUHalf(f);
      e_version = readUWord (f);
      e_entry = readUWord (f);
      e_phoff = readUWord (f);
      e_shoff = readUWord (f);
      e_flags = readUWord (f);
      e_ehsize = readUHalf (f);
      e_phentsize = readUHalf (f);
      e_phnum = readUHalf (f);
      e_shentsize = readUHalf (f);
      e_shnum = readUHalf (f);
      e_shstrndx = readUHalf (f);
    }   
     public String toString() 
     {
       StringBuffer str = new StringBuffer();
       str.append("-----ELF HEADER---------" + "\n");
       str.append("e_magic " + "\t " +  Utilities.formatString("long", Long.toHexString(e_magic & 0xFFFFFFFFL).toUpperCase()) + "\n");
       str.append("e_class " + "\t " +  Utilities.formatString("byte", Integer.toHexString(e_class & 0xFF ).toUpperCase())+ "\n");
       str.append("e_data " + "\t " + Utilities.formatString("byte", Integer.toHexString(e_data & 0xFF ).toUpperCase())+ "\n");
       str.append("e_idver " + "\t " + Utilities.formatString("byte", Integer.toHexString(e_idver & 0xFF).toUpperCase())+ "\n");
       str.append("e_type " + "\t " + Utilities.formatString("short",Integer.toHexString(e_type & 0xFFFF).toUpperCase())+ "\n");
       str.append("e_machine " + "\t " + Utilities.formatString("short",Integer.toHexString(e_machine & 0xFFFF).toUpperCase())+ "\n");
       str.append("e_version " + "\t " + Utilities.formatString("long",Long.toHexString(e_version & 0xFFFFFFFFL).toUpperCase())+ "\n");
       str.append("e_entry " + "\t " + Utilities.formatString("long",Long.toHexString(e_entry & 0xFFFFFFFFL).toUpperCase())+ "\n");
       str.append("e_phoff "+ "\t " + Utilities.formatString("long",Long.toHexString(e_phoff & 0xFFFFFFFFL).toUpperCase())+ "\n");
       str.append("e_shoff "+ "\t " + Utilities.formatString("long",Long.toHexString(e_shoff  & 0xFFFFFFFFL).toUpperCase())+ "\n");
       str.append("e_flags "+ "\t " + Utilities.formatString("long",Long.toHexString(e_flags & 0xFFFFFFFFL).toUpperCase())+ "\n");
       str.append("e_ehsize "+ "\t " + Utilities.formatString("short",Integer.toHexString(e_ehsize& 0xFFFF).toUpperCase())+ "\n");
       str.append("e_phentsize " + "\t " + Utilities.formatString("short",Integer.toHexString(e_phentsize& 0xFFFF).toUpperCase())+ "\n");
       str.append("e_phnum " + "\t " + Utilities.formatString("short",Integer.toHexString(e_phnum& 0xFFFF).toUpperCase())+ "\n");
       str.append("e_shentsize " + "\t " + Utilities.formatString("short",Integer.toHexString(e_shentsize& 0xFFFF).toUpperCase())+ "\n");
       str.append("e_shnum " + "\t " + Utilities.formatString("short",Integer.toHexString(e_shnum& 0xFFFF).toUpperCase())+ "\n");
       str.append("e_shstrndx "+ "\t " + Utilities.formatString("short",Integer.toHexString(e_shstrndx& 0xFFFF).toUpperCase())+ "\n");
       return str.toString();
     }
  }
  
  private static class Elf32_Shdr
  {
    private long sh_name;
    private int sh_type;
    private int sh_flags;
    private long sh_addr;
    private long sh_offset;
    private long sh_size;
    private int sh_link;
    private int sh_info;
    private int sh_addralign;
    private long sh_entsize;
    

    private static int sizeof () { return 40; }
    private void read (RandomAccessFile f) throws IOException
    {
      sh_name = readUWord (f);
      sh_type = readWord (f);
      sh_flags = readWord (f);
      sh_addr = readUWord (f);
      sh_offset = readUWord (f);
      sh_size = readUWord (f);
      sh_link = readWord (f);
      sh_info = readWord (f);
      sh_addralign = readWord (f);
      sh_entsize = readWord (f);
    }
    public void printSectionHeader()
    {
             System.out.println("sh_name "  + sh_name);
             System.out.println("sh_type " + sh_type);
             System.out.println("sh_flags "  + Long.toHexString(sh_flags));
             System.out.println("sh_addr "  + Long.toHexString(sh_addr));
             System.out.println("sh_offset " + sh_offset);
             System.out.println("sh_size " + sh_size);
             System.out.println("sh_link " + sh_link);
             System.out.println("sh_info " + sh_info);
             System.out.println("sh_addralign " + sh_addralign);
             System.out.println("sh_entsize "  + sh_entsize);
    }
  }

  private static int readUByte (RandomAccessFile f) throws IOException
  {
    return f.readUnsignedByte();   
  }
  private static int readUHalf (RandomAccessFile f) throws IOException
  {
      return f.readUnsignedByte () | (f.readUnsignedByte () << 8);
      
  }

  private static int readWord (RandomAccessFile f) throws IOException
  {


      return (f.readUnsignedByte () | (f.readUnsignedByte () << 8)
	      | (f.readUnsignedByte () << 16) | (f.readUnsignedByte () << 24));
  }

  private static long readUWord (RandomAccessFile f) throws IOException
  {

	long l = (f.readUnsignedByte () | (f.readUnsignedByte () << 8)
		  | (f.readUnsignedByte () << 16) | (f.readUnsignedByte () << 24));
	return (l & 0xFFFFFFFFL);
      
   
   }
   enum ShFlags { None(0) , Write(1) , Allocate(2) , Execute(4); 
            private int value;
            ShFlags(int val)
            {
                value=val;
            }
            int getValue()
            {
                return value;
            }
    
    }
   enum ShType { NULL(0), PROGBITS(1) ,SYMTAB(2) ,STRTAB(3),
		 RELA(4),HASH(5),DYNAMIC(6),NOTE(7),NOBITS(8)
		 ,REL(9),SHLIB(10),DYNSYM(11);
             private int value;
            ShType(int val)
            {
                value=val;
            }
            int getValue()
            {
                return value;
            }
   }
  static String ElfInfo;
  static void readHeader(String file) throws IOException
  {
    RandomAccessFile f = new RandomAccessFile (file, "r");
    /** Read pbp **/
    PBP_Header pbp = new PBP_Header();
    pbp.read(f);
    if(Long.toHexString(pbp.p_magic & 0xFFFFFFFFL).equals("50425000"))//file is pbp
    {
        f.seek(pbp.offset_psp_data); //seek the new offset!
    }
    else
    {
        f.seek(0); // start read from start file is not pbp check if it an elf;
    }
    /** Read the ELF header. */
    Elf32_Ehdr ehdr = new Elf32_Ehdr ();
    ehdr.read (f);
    if(!Long.toHexString(ehdr.e_magic & 0xFFFFFFFFL).toUpperCase().equals("464C457F"))
    {
        System.out.println("NOT AN ELF FILE");
        
    }
    if(!Integer.toHexString(ehdr.e_machine & 0xFFFF).equals("8"))
    {
        System.out.println("NOT A MIPS executable");
    }
    ElfInfo = ehdr.toString();
    Elf32_Shdr shdr = new Elf32_Shdr();
    //shdr.read(f);
    
    
    for (int i = 0; i < ehdr.e_shnum; i++)
    {
       	// Read information about this section.
	f.seek (ehdr.e_shoff + (i * ehdr.e_shentsize));
	shdr.read (f);
        shdr.printSectionHeader();
        if((shdr.sh_flags & ShFlags.Allocate.getValue())== ShFlags.Allocate.getValue())
        {
             
             switch(shdr.sh_type)
             {
                 case 1: //ShType.PROGBITS
                     System.out.println("FEED MEMORY WITH IT!");
                     break;
                 case 8: // ShType.NOBITS
                     System.out.println("NO BITS");
                     break;
                 
                 
                 
             }
        }
    }
    
  }
}
