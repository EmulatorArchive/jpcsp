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

import static jpcsp.util.Utilities.readStringNZ;
import static jpcsp.util.Utilities.readStringZ;

import java.util.HashMap;
import java.util.Iterator;

import jpcsp.Emulator;
import jpcsp.Memory;
import jpcsp.MemoryMap;
import jpcsp.Processor;
import jpcsp.Allegrex.CpuState;
import jpcsp.HLE.Modules;
import jpcsp.HLE.kernel.managers.SceUidManager;
import jpcsp.HLE.kernel.types.SceKernelErrors;
import jpcsp.HLE.modules.HLEModule;
import jpcsp.HLE.modules.HLEModuleFunction;
import jpcsp.HLE.modules.HLEModuleManager;
import jpcsp.HLE.modules.HLEStartModule;

import org.apache.log4j.Logger;

/*
 * TODO list:
 * 1. Use the partitionid in functions that use it as a parameter.
 *  -> Info:
 *      1 = kernel, 2 = user, 3 = me, 4 = kernel mirror (from potemkin/dash)
 *      http://forums.ps2dev.org/viewtopic.php?p=75341#75341
 *      8 = slim, topaddr = 0x8A000000, size = 0x1C00000 (28 MB), attr = 0x0C
 *      8 = slim, topaddr = 0x8BC00000, size = 0x400000 (4 MB), attr = 0x0C
 *
 * 2. Implement format string parsing and reading variable number of parameters
 * in sceKernelPrintf.
 */
public class SysMemUserForUser implements HLEModule, HLEStartModule {
    protected static Logger log = Modules.getLogger("SysMemUserForUser");
    protected static Logger stdout = Logger.getLogger("stdout");
    protected static HashMap<Integer, SysMemInfo> blockList;
    protected static MemoryChunkList freeMemoryChunks;
    protected int firmwareVersion = 150;
    public static final int defaultSizeAlignment = 256;

    // PspSysMemBlockTypes
    public static final int PSP_SMEM_Low = 0;
    public static final int PSP_SMEM_High = 1;
    public static final int PSP_SMEM_Addr = 2;
    public static final int PSP_SMEM_LowAligned = 3;
    public static final int PSP_SMEM_HighAligned = 4;

	@Override
	public String getName() { return "SysMemUserForUser"; }

	@Override
	public void installModule(HLEModuleManager mm, int version) {
		if (version >= 150) {

			mm.addFunction(0xA291F107, sceKernelMaxFreeMemSizeFunction);
			mm.addFunction(0xF919F628, sceKernelTotalFreeMemSizeFunction);
			mm.addFunction(0x237DBD4F, sceKernelAllocPartitionMemoryFunction);
			mm.addFunction(0xB6D61D02, sceKernelFreePartitionMemoryFunction);
			mm.addFunction(0x9D9A5BA1, sceKernelGetBlockHeadAddrFunction);
			mm.addFunction(0x13A5ABEF, sceKernelPrintfFunction);
			mm.addFunction(0x3FC9AE6A, sceKernelDevkitVersionFunction);

		}
	}

	@Override
	public void uninstallModule(HLEModuleManager mm, int version) {
		if (version >= 150) {

			mm.removeFunction(sceKernelMaxFreeMemSizeFunction);
			mm.removeFunction(sceKernelTotalFreeMemSizeFunction);
			mm.removeFunction(sceKernelAllocPartitionMemoryFunction);
			mm.removeFunction(sceKernelFreePartitionMemoryFunction);
			mm.removeFunction(sceKernelGetBlockHeadAddrFunction);
			mm.removeFunction(sceKernelPrintfFunction);
			mm.removeFunction(sceKernelDevkitVersionFunction);

		}
	}

	protected boolean started = false;

	@Override
	public void start() {
		if(started) return;

		blockList = new HashMap<Integer, SysMemInfo>();

        int startFreeMem = MemoryMap.START_USERSPACE;
        int endFreeMem = MemoryMap.END_USERSPACE;
        MemoryChunk initialMemory = new MemoryChunk(startFreeMem, endFreeMem - startFreeMem + 1);
        freeMemoryChunks = new MemoryChunkList(initialMemory);

        started = true;
	}

	@Override
	public void stop() {
		started = false;
	}

    public void reset() {
		blockList = new HashMap<Integer, SysMemInfo>();

        int startFreeMem = MemoryMap.START_USERSPACE;
        int endFreeMem = MemoryMap.END_USERSPACE;
        MemoryChunk initialMemory = new MemoryChunk(startFreeMem, endFreeMem - startFreeMem + 1);
        freeMemoryChunks = new MemoryChunkList(initialMemory);
	}

    public static class SysMemInfo implements Comparable<SysMemInfo> {

        public final int uid;
        public final int partitionid;
        public final String name;
        public final int type;
        public final int size;
        public final int allocatedSize;
        public final int addr;

        public SysMemInfo(int partitionid, String name, int type, int size, int allocatedSize, int addr) {
            this.partitionid = partitionid;
            this.name = name;
            this.type = type;
            this.size = size;
            this.allocatedSize = allocatedSize;
            this.addr = addr;

            uid = SceUidManager.getNewUid("SysMem");
            blockList.put(uid, this);
        }

        @Override
        public String toString() {
            return String.format("SysMemInfo[uid=%x, partition=%d, name='%s', type=%s, size=0x%X (allocated=0x%X), addr=0x%08X-0x%08X]", uid, partitionid, name, getTypeName(type), size, allocatedSize, addr, addr + allocatedSize);
        }

        @Override
        public int compareTo(SysMemInfo o) {
            if (addr == o.addr) {
                log.warn("Set invariant broken for SysMemInfo " + this);
                return 0;
            }
            return addr < o.addr ? -1 : 1;
        }
    }

    protected static class MemoryChunk {
    	// Start address of this MemoryChunk
    	public int addr;
    	// Size of this MemoryChunk: it extends from addr to (addr + size -1)
    	public int size;
    	// The MemoryChunk are kept sorted by addr and linked with next/previous
    	// The MemoryChunk with the lowest addr has previous == null
    	// The MemoryChunk with the highest addr has next == null
    	public MemoryChunk next;
    	public MemoryChunk previous;

    	public MemoryChunk(int addr, int size) {
    		this.addr = addr;
    		this.size = size;
    	}

    	/**
    	 * Check if the memoryChunk has enough space to allocate a block.
    	 *
    	 * @param availableSize size of the requested block
    	 * @param addrAlignment base address alignment of the requested block
    	 * @return              true if the chunk is large enough to allocate the block
    	 *                      false if the chunk is too small for the requested block
    	 */
    	public boolean isAvailable(int availableSize, int addrAlignment) {
    		if (alignUp(addr, addrAlignment) + availableSize <= addr + size) {
    			return true;
    		}

    		return false;
    	}

    	@Override
		public String toString() {
			return String.format("[addr=0x%08X-0x%08X, size=0x%X]", addr, addr + size, size);
		}
    }

    protected static class MemoryChunkList {
    	// The MemoryChunk objects are linked and kept sorted by address.
    	//
    	// low: MemoryChunk with the lowest address.
    	// Start point to scan list by increasing address
    	private MemoryChunk low;
    	// high: MemoryChunk with the highest address.
    	// Start point to scan the list by decreasing address
    	private MemoryChunk high;

    	public MemoryChunkList(MemoryChunk initialMemoryChunk) {
    		low = initialMemoryChunk;
    		high = initialMemoryChunk;
    	}

    	/**
    	 * Remove a MemoryChunk from the list.
    	 *
    	 * @param memoryChunk the MemoryChunk to be removed
    	 */
    	public void remove(MemoryChunk memoryChunk) {
    		if (memoryChunk.previous != null) {
    			memoryChunk.previous.next = memoryChunk.next;
    		}
    		if (memoryChunk.next != null) {
    			memoryChunk.next.previous = memoryChunk.previous;
    		}

    		if (low == memoryChunk) {
    			low = memoryChunk.next;
    		}
    		if (high == memoryChunk) {
    			high = memoryChunk.previous;
    		}
    	}

    	/**
    	 * Allocate a memory from the MemoryChunk, at its lowest address.
    	 * The MemoryChunk is updated accordingly or is removed if it stays empty.
    	 *
    	 * @param memoryChunk   the MemoryChunk where the memory should be allocated
    	 * @param size          the size of the memory to be allocated
    	 * @param addrAlignment base address alignment of the requested block
    	 * @return              the base address of the allocated memory
    	 */
    	public int allocLow(MemoryChunk memoryChunk, int size, int addrAlignment) {
    		int addr = alignUp(memoryChunk.addr, addrAlignment);

    		return alloc(memoryChunk, addr, size);
    	}

    	/**
    	 * Allocate a memory from the MemoryChunk, at its highest address.
    	 * The MemoryChunk is updated accordingly or is removed if it stays empty.
    	 *
    	 * @param memoryChunk   the MemoryChunk where the memory should be allocated
    	 * @param size          the size of the memory to be allocated
    	 * @param addrAlignment base address alignment of the requested block
    	 * @return              the base address of the allocated memory
    	 */
    	public int allocHigh(MemoryChunk memoryChunk, int size, int addrAlignment) {
			int addr = alignDown(memoryChunk.addr + memoryChunk.size, addrAlignment) - size;

			return alloc(memoryChunk, addr, size);
    	}

    	/**
    	 * Allocate a memory from the MemoryChunk, given the base address.
    	 * The base address must be inside the MemoryChunk
    	 * The MemoryChunk is updated accordingly, is removed if it stays empty or
    	 * is split into 2 remaining free parts.
    	 *
    	 * @param memoryChunk the MemoryChunk where the memory should be allocated
    	 * @param addr        the base address of the memory to be allocated
    	 * @param size        the size of the memory to be allocated
    	 * @return            the base address of the allocated memory, or 0
    	 *                    if the MemoryChunk is too small to allocate the desired size.
    	 */
    	public int alloc(MemoryChunk memoryChunk, int addr, int size) {
    		if (addr < memoryChunk.addr || memoryChunk.addr + memoryChunk.size < addr + size) {
    			// The MemoryChunk is too small to allocate the desired size
    			// are the requested address is outside the MemoryChunk
    			return 0;
    		} else if (memoryChunk.size == size) {
    			// Allocate the complete MemoryChunk
    			remove(memoryChunk);
    		} else if (memoryChunk.addr == addr) {
    			// Allocate at the lowest address
				memoryChunk.size -= size;
				memoryChunk.addr += size;
    		} else if (memoryChunk.addr + memoryChunk.size == addr + size) {
    			// Allocate at the highest address
				memoryChunk.size -= size;
    		} else {
    			// Allocate in the middle of a MemoryChunk: it must be split
    			// in 2 parts: one for lowest part and one for the highest part.
    			// Update memoryChunk to contain the lowest part,
    			// and create a new MemoryChunk to contain to highest part.
    			int lowSize = addr - memoryChunk.addr;
    			int highSize = memoryChunk.size - lowSize - size;
    			MemoryChunk highMemoryChunk = new MemoryChunk(addr + size, highSize);
    			memoryChunk.size = lowSize;

    			addAfter(highMemoryChunk, memoryChunk);
    		}

    		return addr;
    	}

    	/**
    	 * Add a new MemoryChunk after another one.
    	 * This method does not check if the addresses are kept ordered.
    	 *
    	 * @param memoryChunk the MemoryChunk to be added
    	 * @param reference   memoryChunk has to be added after this reference
    	 */
    	private void addAfter(MemoryChunk memoryChunk, MemoryChunk reference) {
    		memoryChunk.previous = reference;
    		memoryChunk.next = reference.next;
    		reference.next = memoryChunk;
    		if (memoryChunk.next != null) {
    			memoryChunk.next.previous = memoryChunk;
    		}

    		if (high == reference) {
    			high = memoryChunk;
    		}
    	}

    	/**
    	 * Add a new MemoryChunk before another one.
    	 * This method does not check if the addresses are kept ordered.
    	 *
    	 * @param memoryChunk the MemoryChunk to be added
    	 * @param reference   memoryChunk has to be added before this reference
    	 */
    	private void addBefore(MemoryChunk memoryChunk, MemoryChunk reference) {
    		memoryChunk.previous = reference.previous;
    		memoryChunk.next = reference;
    		reference.previous = memoryChunk;
    		if (memoryChunk.previous != null) {
    			memoryChunk.previous.next = memoryChunk;
    		}

    		if (low == reference) {
    			low = memoryChunk;
    		}
    	}

    	/**
    	 * Add a new MemoryChunk to the list. It is added in the list so that
    	 * the addresses are kept in increasing order.
    	 * The MemoryChunk might be merged into another adjacent MemoryChunk.
    	 *
    	 * @param memoryChunk the MemoryChunk to be added
    	 */
    	public void add(MemoryChunk memoryChunk) {
    		// Scan the list to find the insertion point to keep the elements
    		// ordered by increasing address.
    		for (MemoryChunk scanChunk = low; scanChunk != null; scanChunk = scanChunk.next) {
    			// Merge the MemoryChunk if it is adjacent to other elements in the list
    			if (scanChunk.addr + scanChunk.size == memoryChunk.addr) {
    				// The MemoryChunk is adjacent at its lowest address,
    				// merge it into the previous one.
    				scanChunk.size += memoryChunk.size;

    				// Check if the gap to the next chunk has not been closed,
    				// in which case, we can also merge the next chunk.
    				MemoryChunk nextChunk = scanChunk.next;
    				if (nextChunk != null) {
    					if (scanChunk.addr + scanChunk.size == nextChunk.addr) {
    						// Merge with nextChunk
    						scanChunk.size += nextChunk.size;
    						remove(nextChunk);
    					}
    				}
    				return;
    			} else if (memoryChunk.addr + memoryChunk.size == scanChunk.addr) {
    				// The MemoryChunk is adjacent at its highest address,
    				// merge it into the next one.
    				scanChunk.addr = memoryChunk.addr;
    				scanChunk.size += memoryChunk.size;

    				// Check if the gap to the previous chunk has not been closed,
    				// in which case, we can also merge the previous chunk.
    				MemoryChunk previousChunk = scanChunk.previous;
    				if (previousChunk != null) {
    					if (previousChunk.addr + previousChunk.size == scanChunk.addr) {
    						// Merge with previousChunk
    						previousChunk.size += scanChunk.size;
    						remove(scanChunk);
    					}
    				}
    				return;
    			} else if (scanChunk.addr > memoryChunk.addr) {
    				// We have found the insertion point for the MemoryChunk,
    				// add it before this element to keep the addresses in
    				// increasing order.
    				addBefore(memoryChunk, scanChunk);
    				return;
    			}
    		}

    		// The MemoryChunk has not yet been added, add it at the very end
    		// of the list.
    		if (high == null && low == null) {
    			// The list is empty, add the element
    			high = memoryChunk;
    			low = memoryChunk;
    		} else {
    			addAfter(memoryChunk, high);
    		}
    	}

		@Override
		public String toString() {
			StringBuilder result = new StringBuilder();

			for (MemoryChunk memoryChunk = low; memoryChunk != null; memoryChunk = memoryChunk.next) {
				if (result.length() > 0) {
					result.append(", ");
				}
				result.append(memoryChunk.toString());
			}

			return result.toString();
		}
    }

    static protected String getTypeName(int type) {
        String typeName;

        switch (type) {
            case PSP_SMEM_Low:
                typeName = "PSP_SMEM_Low";
                break;
            case PSP_SMEM_High:
                typeName = "PSP_SMEM_High";
                break;
            case PSP_SMEM_Addr:
                typeName = "PSP_SMEM_Addr";
                break;
            case PSP_SMEM_LowAligned:
                typeName = "PSP_SMEM_LowAligned";
                break;
            case PSP_SMEM_HighAligned:
                typeName = "PSP_SMEM_HighAligned";
                break;
            default:
                typeName = "UNHANDLED " + type;
                break;
        }

        return typeName;
    }

    private static int alignUp(int value, int alignment) {
    	return alignDown(value + alignment, alignment);
    }

    private static int alignDown(int value, int alignment) {
    	return value & ~alignment;
    }

    // Allocates to 256-byte alignment
    public SysMemInfo malloc(int partitionid, String name, int type, int size, int addr) {
        int allocatedAddress = 0;

        int alignment = defaultSizeAlignment - 1;
        if (type == PSP_SMEM_LowAligned || type == PSP_SMEM_HighAligned) {
            // Use the alignment provided in the addr parameter
            alignment = addr - 1;
        }
        int allocatedSize = alignUp(size, alignment);

        switch (type) {
        	case PSP_SMEM_Low:
        	case PSP_SMEM_LowAligned:
        		for (MemoryChunk memoryChunk = freeMemoryChunks.low; memoryChunk != null; memoryChunk = memoryChunk.next) {
        			if (memoryChunk.isAvailable(allocatedSize, alignment)) {
        				allocatedAddress = freeMemoryChunks.allocLow(memoryChunk, allocatedSize, alignment);
        				break;
        			}
        		}
        		break;
        	case PSP_SMEM_High:
        	case PSP_SMEM_HighAligned:
        		for (MemoryChunk memoryChunk = freeMemoryChunks.high; memoryChunk != null; memoryChunk = memoryChunk.previous) {
        			if (memoryChunk.isAvailable(allocatedSize, alignment)) {
        				allocatedAddress = freeMemoryChunks.allocHigh(memoryChunk, allocatedSize, alignment);
        				break;
        			}
        		}
        		break;
        	case PSP_SMEM_Addr:
        		for (MemoryChunk memoryChunk = freeMemoryChunks.low; memoryChunk != null; memoryChunk = memoryChunk.next) {
        			if (memoryChunk.addr <= addr && addr < memoryChunk.addr + memoryChunk.size) {
        				allocatedAddress = freeMemoryChunks.alloc(memoryChunk, addr, allocatedSize);
        			}
        		}
        		break;
    		default:
    			log.warn(String.format("malloc: unknown type %s", getTypeName(type)));
        }

        SysMemInfo sysMemInfo;
		if (allocatedAddress == 0) {
            log.warn(String.format("malloc cannot allocate partition=%d, type=%s, size=0x%X, addr=0x%08X", partitionid, getTypeName(type), size, addr));
			sysMemInfo = null;
		} else {
			sysMemInfo = new SysMemInfo(partitionid, name, type, size, allocatedSize, allocatedAddress);

			if (log.isDebugEnabled()) {
				log.debug(String.format("malloc partition=%d, type=%s, size=0x%X, addr=0x%08X: returns 0x%08X", partitionid, getTypeName(type), size, addr, allocatedAddress));
				if (log.isTraceEnabled()) {
					log.trace("Free list after malloc: " + freeMemoryChunks);
				}
			}
		}

		return sysMemInfo;
    }

    public String getDebugFreeMem() {
    	return freeMemoryChunks.toString();
    }

    public void free(SysMemInfo info) {
    	if (info != null) {
	    	MemoryChunk memoryChunk = new MemoryChunk(info.addr, info.allocatedSize);
	    	freeMemoryChunks.add(memoryChunk);

	    	if (log.isDebugEnabled()) {
	    		log.debug(String.format("free %s", info.toString()));
	    		if (log.isTraceEnabled()) {
	    			log.trace("Free list after free: " + freeMemoryChunks.toString());
	    		}
	    	}
    	}
    }

    public int maxFreeMemSize() {
    	int maxFreeMemSize = 0;
    	for (MemoryChunk memoryChunk = freeMemoryChunks.low; memoryChunk != null; memoryChunk = memoryChunk.next) {
    		if (memoryChunk.size > maxFreeMemSize) {
    			maxFreeMemSize = memoryChunk.size;
    		}
    	}
		return maxFreeMemSize;
    }

    public int totalFreeMemSize() {
        int totalFreeMemSize = 0;
    	for (MemoryChunk memoryChunk = freeMemoryChunks.low; memoryChunk != null; memoryChunk = memoryChunk.next) {
    		totalFreeMemSize += memoryChunk.size;
    	}

    	return totalFreeMemSize;
    }

    /** @param firmwareVersion : in this format: ABB, where A = major and B = minor, for example 271 */
    public void setFirmwareVersion(int firmwareVersion) {
    	this.firmwareVersion = firmwareVersion;
    }

    // note: we're only looking at user memory, so 0x08800000 - 0x0A000000
    // this is mainly to make it fit on one console line
    public void dumpSysMemInfo() {
        final int MEMORY_SIZE = 0x1800000;
        final int SLOT_COUNT = 64; // 0x60000
        final int SLOT_SIZE = MEMORY_SIZE / SLOT_COUNT; // 0x60000
        boolean[] allocated = new boolean[SLOT_COUNT];
        boolean[] fragmented = new boolean[SLOT_COUNT];
        int allocatedSize = 0;
        int fragmentedSize = 0;

        for (Iterator<SysMemInfo> it = blockList.values().iterator(); it.hasNext();) {
            SysMemInfo info = it.next();
            for (int i = info.addr; i < info.addr + info.size; i += SLOT_SIZE) {
                if (i >= 0x08800000 && i < 0x0A000000) {
                    allocated[(i - 0x08800000) / SLOT_SIZE] = true;
                }
            }
            allocatedSize += info.size;
        }

        for (MemoryChunk memoryChunk = freeMemoryChunks.low; memoryChunk != null; memoryChunk = memoryChunk.next) {
            for (int i = memoryChunk.addr; i < memoryChunk.addr + memoryChunk.size; i += SLOT_SIZE) {
                if (i >= 0x08800000 && i < 0x0A000000) {
                    fragmented[(i - 0x08800000) / SLOT_SIZE] = true;
                }
            }
            fragmentedSize += memoryChunk.size;
        }

        StringBuilder allocatedDiagram = new StringBuilder();
        allocatedDiagram.append("[");
        for (int i = 0; i < SLOT_COUNT; i++) {
            allocatedDiagram.append(allocated[i] ? "X" : " ");
        }
        allocatedDiagram.append("]");

        StringBuilder fragmentedDiagram = new StringBuilder();
        fragmentedDiagram.append("[");
        for (int i = 0; i < SLOT_COUNT; i++) {
            fragmentedDiagram.append(fragmented[i] ? "X" : " ");
        }
        fragmentedDiagram.append("]");

        System.err.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        System.err.println(String.format("Allocated memory:  %08X %d bytes", allocatedSize, allocatedSize));
        System.err.println(allocatedDiagram);
        System.err.println(String.format("Fragmented memory: %08X %d bytes", fragmentedSize, fragmentedSize));
        System.err.println(fragmentedDiagram);
    }

	public void sceKernelMaxFreeMemSize(Processor processor) {
		CpuState cpu = processor.cpu;

		int maxFreeMemSize = maxFreeMemSize();

        // Some games expect size to be rounded down in 16 bytes block
        maxFreeMemSize &= ~15;

    	if (log.isDebugEnabled()) {
    		log.debug(String.format("sceKernelMaxFreeMemSize %d(hex=0x%1$X)", maxFreeMemSize));
    	}
        cpu.gpr[2] = maxFreeMemSize;
	}

	public void sceKernelTotalFreeMemSize(Processor processor) {
		CpuState cpu = processor.cpu;

		int totalFreeMemSize = totalFreeMemSize();

    	if (log.isDebugEnabled()) {
    		log.debug(String.format("sceKernelTotalFreeMemSize %d(hex=0x%1$X)", totalFreeMemSize));
    	}
        cpu.gpr[2] = totalFreeMemSize;
	}

	public void sceKernelAllocPartitionMemory(Processor processor) {
		CpuState cpu = processor.cpu;

		int partitionid = cpu.gpr[4];
		int pname = cpu.gpr[5];
		int type = cpu.gpr[6];
		int size = cpu.gpr[7];
		int addr = cpu.gpr[8];

        addr &= Memory.addressMask;
        String name = readStringZ(pname);

        if (log.isDebugEnabled()) {
	        log.debug(String.format("sceKernelAllocPartitionMemory(partition=%d, name='%s', type=%s, size=0x%X, addr=0x%08X", partitionid, name, getTypeName(type), size, addr));
        }

        if (type < PSP_SMEM_Low || type > PSP_SMEM_HighAligned) {
            cpu.gpr[2] = SceKernelErrors.ERROR_ILLEGAL_MEMBLOCK_ALLOC_TYPE;
        } else {
            SysMemInfo info = malloc(partitionid, name, type, size, addr);
            if (info != null) {
                cpu.gpr[2] = info.uid;
            } else {
                cpu.gpr[2] = SceKernelErrors.ERROR_FAILED_ALLOC_MEMBLOCK;
            }
        }
	}

	public void sceKernelFreePartitionMemory(Processor processor) {
		CpuState cpu = processor.cpu;

		int uid = cpu.gpr[4];

		SceUidManager.checkUidPurpose(uid, "SysMem", true);
        SysMemInfo info = blockList.remove(uid);
        if (info == null) {
            log.warn("sceKernelFreePartitionMemory unknown SceUID=" + Integer.toHexString(uid));
            Emulator.getProcessor().cpu.gpr[2] = SceKernelErrors.ERROR_ILLEGAL_CHUNK_ID;
        } else {
        	if (log.isDebugEnabled()) {
        		log.debug("sceKernelFreePartitionMemory SceUID=" + Integer.toHexString(info.uid) + " name:'" + info.name + "'");
        	}
            free(info);
            cpu.gpr[2] = 0;
        }
	}

	public void sceKernelGetBlockHeadAddr(Processor processor) {
		CpuState cpu = processor.cpu;

		int uid = cpu.gpr[4];

		SceUidManager.checkUidPurpose(uid, "SysMem", true);
        SysMemInfo info = blockList.get(uid);
        if (info == null) {
            log.warn("sceKernelGetBlockHeadAddr unknown SceUID=" + Integer.toHexString(uid));
            cpu.gpr[2] = SceKernelErrors.ERROR_ILLEGAL_CHUNK_ID;
        } else {
        	if (log.isDebugEnabled()) {
        		log.debug("sceKernelGetBlockHeadAddr SceUID=" + Integer.toHexString(info.uid) + " name:'" + info.name + "' headAddr:" + Integer.toHexString(info.addr));
        	}
            cpu.gpr[2] = info.addr;
        }
	}

	public void sceKernelPrintf(Processor processor) {
		CpuState cpu = processor.cpu;

		int string_addr = cpu.gpr[4];

		String msg = readStringNZ(string_addr, 256);
        if (log.isDebugEnabled()) {
        	log.debug(String.format("sceKernelPrintf(string_addr=0x%08X) '%s'", string_addr, msg));
        }

        // Format and print the message to stdout
        if (stdout.isInfoEnabled()) {
        	String formattedMsg = msg;
        	try {
        		// Translate the C-like format string to a Java format string:
        		// - %u or %i -> %d
        		// - %p -> %08X
        		String javaMsg = msg;
        		javaMsg = javaMsg.replaceAll("\\%[ui]", "%d");
        		javaMsg = javaMsg.replaceAll("\\%p", "%08X");

        		int[] gpr = cpu.gpr;
            	// For now, use only the 7 register parameters: $a1-$a3, $t0-$t3
            	// Further parameters should be retrieved from the stack.
            	// String.format: If there are more arguments than format specifiers, the extra arguments are ignored.
        		formattedMsg = String.format(javaMsg, gpr[5], gpr[6], gpr[7], gpr[8], gpr[9], gpr[10], gpr[11]);
        	} catch (Exception e) {
        		// Ignore formatting exception
        	}
        	stdout.info(formattedMsg);
        }

        cpu.gpr[2] = 0;
	}

	public void sceKernelDevkitVersion(Processor processor) {
		CpuState cpu = processor.cpu;

		int major = firmwareVersion / 100;
        int minor = (firmwareVersion / 10) % 10;
        int revision = firmwareVersion % 10;
        int devkitVersion = (major << 24) | (minor << 16) | (revision << 8) | 0x10;
        if (log.isDebugEnabled()) {
        	log.debug(String.format("sceKernelDevkitVersion return:0x%08X", devkitVersion));
        }

        cpu.gpr[2] = devkitVersion;
	}

	public final HLEModuleFunction sceKernelMaxFreeMemSizeFunction = new HLEModuleFunction("SysMemUserForUser", "sceKernelMaxFreeMemSize") {
		@Override
		public final void execute(Processor processor) {
			sceKernelMaxFreeMemSize(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.SysMemUserForUserModule.sceKernelMaxFreeMemSize(processor);";
		}
	};

	public final HLEModuleFunction sceKernelTotalFreeMemSizeFunction = new HLEModuleFunction("SysMemUserForUser", "sceKernelTotalFreeMemSize") {
		@Override
		public final void execute(Processor processor) {
			sceKernelTotalFreeMemSize(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.SysMemUserForUserModule.sceKernelTotalFreeMemSize(processor);";
		}
	};

	public final HLEModuleFunction sceKernelAllocPartitionMemoryFunction = new HLEModuleFunction("SysMemUserForUser", "sceKernelAllocPartitionMemory") {
		@Override
		public final void execute(Processor processor) {
			sceKernelAllocPartitionMemory(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.SysMemUserForUserModule.sceKernelAllocPartitionMemory(processor);";
		}
	};

	public final HLEModuleFunction sceKernelFreePartitionMemoryFunction = new HLEModuleFunction("SysMemUserForUser", "sceKernelFreePartitionMemory") {
		@Override
		public final void execute(Processor processor) {
			sceKernelFreePartitionMemory(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.SysMemUserForUserModule.sceKernelFreePartitionMemory(processor);";
		}
	};

	public final HLEModuleFunction sceKernelGetBlockHeadAddrFunction = new HLEModuleFunction("SysMemUserForUser", "sceKernelGetBlockHeadAddr") {
		@Override
		public final void execute(Processor processor) {
			sceKernelGetBlockHeadAddr(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.SysMemUserForUserModule.sceKernelGetBlockHeadAddr(processor);";
		}
	};

	public final HLEModuleFunction sceKernelPrintfFunction = new HLEModuleFunction("SysMemUserForUser", "sceKernelPrintf") {
		@Override
		public final void execute(Processor processor) {
			sceKernelPrintf(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.SysMemUserForUserModule.sceKernelPrintf(processor);";
		}
	};

	public final HLEModuleFunction sceKernelDevkitVersionFunction = new HLEModuleFunction("SysMemUserForUser", "sceKernelDevkitVersion") {
		@Override
		public final void execute(Processor processor) {
			sceKernelDevkitVersion(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.SysMemUserForUserModule.sceKernelDevkitVersion(processor);";
		}
	};
}