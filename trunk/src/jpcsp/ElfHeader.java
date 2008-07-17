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

  
    public void printHeader()
    {
         
         System.out.println("e_magic 0x" + Long.toHexString(e_magic & 0xFFFFFFFFL).toUpperCase());
         System.out.println("e_class " +  Integer.toHexString(e_class & 0xFF ));
         System.out.println("e_data " + Integer.toHexString(e_data & 0xFF ));
         System.out.println("e_idver " + Integer.toHexString(e_idver & 0xFF));
         System.out.println("e_type " + Integer.toHexString(e_type & 0xFFFF));
         System.out.println("e_machine " + Integer.toHexString(e_machine & 0xFFFF));
         System.out.println("e_version " + Long.toHexString(e_version & 0xFFFFFFFFL));
         System.out.println("e_entry " + Long.toHexString(e_entry & 0xFFFFFFFFL));
         System.out.println("e_phoff "+ Long.toHexString(e_phoff & 0xFFFFFFFFL));
         System.out.println("e_shoff "+ Long.toHexString(e_shoff  & 0xFFFFFFFFL));
         System.out.println("e_flags "+ Long.toHexString(e_flags & 0xFFFFFFFFL));
         System.out.println("e_ehsize "+ Integer.toHexString(e_ehsize& 0xFFFF));
         System.out.println("e_phentsize " + Integer.toHexString(e_phentsize& 0xFFFF));
         System.out.println("e_phnum " + Integer.toHexString(e_phnum& 0xFFFF));
         System.out.println("e_shentsize " + Integer.toHexString(e_shentsize& 0xFFFF));
         System.out.println("e_shnum " + Integer.toHexString(e_shnum& 0xFFFF));
         System.out.println("e_shstrndx "+ Integer.toHexString(e_shstrndx& 0xFFFF));
	
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
  static void readHeader(String file) throws IOException
  {
    RandomAccessFile f = new RandomAccessFile (file, "r");
    /** Read the ELF header. */
    Elf32_Ehdr ehdr = new Elf32_Ehdr ();
    ehdr.read (f);
    ehdr.printHeader();
    Elf32_Shdr shdr = new Elf32_Shdr();
    shdr.read(f);
    shdr.printSectionHeader();
    
    for (int i = 0; i < ehdr.e_shnum; i++)
    {
       	// Read information about this section.
	f.seek (ehdr.e_shoff + (i * ehdr.e_shentsize));
	shdr.read (f);
        
        		//i.position = header._shoff + (header._shentsize * n);
			//Elf32_Shdr shdr = void; i.read(TA(shdr)); shdrs ~= shdr;
    }
    
  }
}
