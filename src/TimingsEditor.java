import java.io.IOException;
import java.lang.IllegalArgumentException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TimingsEditor
{
    /*
    public static void main(String[] args)
    {
        final String bios_name = "Hawaii.rom";

        TimingsEditor te = new TimingsEditor(bios_name);
        te.fix_checksum();
    }
    */

    private static void print_timings(ATOM_VRAM_TIMING_ENTRY e)
    {
        System.out.println(String.format("%d\t%d", e.ulClkRange, e.ucIndex));
        for(byte b : e.ucLatency)
            System.out.print(String.format("0x%02X ", b));
    }

    public TimingsEditor(String bios_file)
    {
        Path path = Paths.get(bios_file);
        try
        {
            bios_bytes = Files.readAllBytes(path);
        }
        catch(Exception e)
        {
            System.err.println("exception caught: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public ArrayList<ATOM_VRAM_TIMING_ENTRY> get_timings()
    {
        /*
        byte[] vram_info_bytes = new byte[ATOM_VRAM_INFO.size];
        System.arraycopy(bios_bytes, VRAM_Info_offset, vram_info_bytes, 0, vram_info_bytes.length);
        */

        // [AB6E, AB81]
        //ATOM_VRAM_INFO vram_info = new ATOM_VRAM_INFO(vram_info_bytes);

        /* 
         * this doesn't work with pre-polaris bioses
         * [AB82, ABC1], [ABC2, AC01], [AC02, AC41]
         */
        /*
        ArrayList<ATOM_VRAM_ENTRY> vram_entries = new ArrayList<>();
        int offset = VRAM_Info_offset + ATOM_VRAM_INFO.size;
        for(int i = 0; i < vram_info.ucNumOfVRAMModule; i++)
        {
            byte[] vram_entry_bytes = new byte[ATOM_VRAM_ENTRY.size];
            System.arraycopy(bios_bytes, offset, vram_entry_bytes, 0, ATOM_VRAM_ENTRY.size);

            ATOM_VRAM_ENTRY vram_entry = new ATOM_VRAM_ENTRY(vram_entry_bytes);
            vram_entries.add(vram_entry);

            offset += vram_entry.usModuleSize;
        }
        */

        // find the 400MHz strap
        byte[] needle = { (byte)0x40, (byte)0x9C, (byte)0x00, (byte)0x01 };
        VRAM_Timings_offset = find_bytes(bios_bytes, needle);
        if(VRAM_Timings_offset == -1)
        {
            System.err.println("failed to find 400MHz strap in BIOS");
            return null;
        }

        int offset = VRAM_Timings_offset;
        ArrayList<ATOM_VRAM_TIMING_ENTRY> vram_timing_entries = new ArrayList<>();
        // unknown length, 32 should be more than enough
        for(int i = 0; i < 32; i++)
        {
            byte[] vram_timing_entry_bytes = new byte[ATOM_VRAM_TIMING_ENTRY.size];
            System.arraycopy(bios_bytes, offset, vram_timing_entry_bytes, 0, ATOM_VRAM_TIMING_ENTRY.size);

            ATOM_VRAM_TIMING_ENTRY vram_timing_entry = new ATOM_VRAM_TIMING_ENTRY(vram_timing_entry_bytes);

            if(vram_timing_entry.ulClkRange == 0) break;

            vram_timing_entries.add(vram_timing_entry);

            offset += ATOM_VRAM_TIMING_ENTRY.size;
        }

        return vram_timing_entries;
    }

    /*
     * finds timings.ulClkRange and timings.ucIndex in bios_bytes and
     * overwrites ucLatency in bios_bytes with timings.ucLatency
     * returns false, if it isn't found
     * otherwise, returns true
     */
    public boolean set_timings(ATOM_VRAM_TIMING_ENTRY timings)
    {
        ArrayList<ATOM_VRAM_TIMING_ENTRY> curr_timings = get_timings();
        if(curr_timings == null) return false;

        boolean found = false;
        for(ATOM_VRAM_TIMING_ENTRY e : curr_timings)
        {
            if(e.ulClkRange == timings.ulClkRange && e.ucIndex == timings.ucIndex)
                found = true;
        }
        if(!found)
        {
            System.err.println(String.format("failed to find timings for index %d %dkHz", 
                timings.ucIndex, timings.ulClkRange));
            return false;
        }

        // find the strap
        byte[] needle = {
            (byte)(timings.ulClkRange & 0xFF),
            (byte)((timings.ulClkRange >> 8) & 0xFF),
            (byte)((timings.ulClkRange >> 16) & 0xFF),
            timings.ucIndex
        };
        int offset = find_bytes(bios_bytes, needle);
        if(offset == -1)
        {
            System.err.println(String.format("failed to find the needle for index %d %dkHz", 
                timings.ucIndex, timings.ulClkRange));
            return false;
        }
        else
        {
            // overwrite timings
            System.arraycopy(timings.ucLatency, 0, bios_bytes, offset + needle.length, timings.ucLatency.length);
            return true;
        }
    }

    /*
     * writes bios_bytes to new_bios_file
     * returns true if succesful, false otherwise
     */
    public boolean save_bios(String new_bios_file)
    {
        Path path = Paths.get(new_bios_file);
        try
        {
            fix_checksum();

            Files.write(path, bios_bytes);
        }
        catch(IOException e)
        {
            System.err.println("failed to write to " + new_bios_file);
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private void fix_checksum()
    {
        final int atom_rom_checksum_offset = 0x21;

        int size = Byte.toUnsignedInt(bios_bytes[2]) * 512;
        byte new_checksum = 0;
        for(int i = 0; i < size; i++)
            new_checksum += bios_bytes[i];

        if(new_checksum != 0)
            bios_bytes[atom_rom_checksum_offset] -= (byte)new_checksum;
    }

    /*
     * converts the 2 bytes at offset to an unsigned 16 bit value
     * bytes are in little endian
     * returns an int as java doesn't have an unsigned 16 bit value
     */
    private int bytes_to_uint16(byte[] bytes, int offset)
    {
        return Byte.toUnsignedInt(bytes[offset + 1]) << 8 | Byte.toUnsignedInt(bytes[offset]);
    }

    private long bytes_to_uint32(byte[] bytes, int offset)
    {
        return Byte.toUnsignedLong(bytes[offset + 3]) << 24 |
               Byte.toUnsignedLong(bytes[offset + 2]) << 16 |
               Byte.toUnsignedLong(bytes[offset + 1]) << 8 |
               Byte.toUnsignedLong(bytes[offset]);
    }

    /*
     * finds the first occurrence of needle in haystack
     * returns the index to the start of needle in haystack
     * returns -1 if not found
     * maybe replace with KMP algorithm?
     */
    private int find_bytes(byte[] haystack, byte[] needle)
    {
        if(haystack == null || needle == null)
            return -1;

        if(haystack.length == 0 || needle.length == 0)
            return -1;

        if(haystack.length < needle.length)
            return -1;

        for(int i = 0; i < haystack.length - needle.length; i++)
        {
            boolean found = true;
            for(int j = 0; j < needle.length; j++)
            {
                if(bios_bytes[i + j] != needle[j])
                {
                    found = false; break;
                }
            }

            if(found) return i;
        }

        return -1;
    }

    class ATOM_COMMON_TABLE_HEADER
    {
        public static final int size = 4;

        public int usStructureSize;     // 2 bytes
        public byte ucTableFormatRevision;
        public byte ucTableContentRevision;

        public ATOM_COMMON_TABLE_HEADER(byte[] bytes) throws IllegalArgumentException
        {
            if(bytes.length != size) 
                throw new IllegalArgumentException(String.format("ATOM_COMMON_TABLE_HEADER: expected %d bytes, got %d bytes", size, bytes.length));
            
            usStructureSize = bytes_to_uint16(bytes, 0);
            ucTableFormatRevision = bytes[2];
            ucTableContentRevision = bytes[3];
        }
    }

    class ATOM_VRAM_INFO
    {
        public static final int size = ATOM_COMMON_TABLE_HEADER.size + 16;

        public ATOM_COMMON_TABLE_HEADER sHeader;
        // vvv 2 bytes vvv -> java doesn't have unsigned :/
        public int usMemAdjustTblOffset;
        public int usMemClkPatchTblOffset;
        public int usMcAdjustPerTileTblOffset;
        public int usMcPhyInitTableOffset;
        public int usDramDataRemapTblOffset;
        public int usReserved1;
        // ^^^ 2 bytes ^^^
        public byte ucNumOfVRAMModule;
        public byte ucMemoryClkPatchTblVer;
        public byte ucVramModuleVer;
        public byte ucMcPhyTileNum;

        public ATOM_VRAM_INFO(byte[] bytes) throws IllegalArgumentException
        {
            if(bytes.length != size)
                throw new IllegalArgumentException(String.format("ATOM_VRAM_INFO: expected %d bytes, got %d bytes", size, bytes.length));

            sHeader = new ATOM_COMMON_TABLE_HEADER(Arrays.copyOf(bytes, ATOM_COMMON_TABLE_HEADER.size));
            int i = ATOM_COMMON_TABLE_HEADER.size;
            usMemAdjustTblOffset = bytes_to_uint16(bytes, i); i += 2;
            usMemClkPatchTblOffset = bytes_to_uint16(bytes, i); i += 2;
            usMcAdjustPerTileTblOffset = bytes_to_uint16(bytes, i); i += 2;
            usMcPhyInitTableOffset = bytes_to_uint16(bytes, i); i += 2;
            usDramDataRemapTblOffset = bytes_to_uint16(bytes, i); i += 2;
            usReserved1 = bytes_to_uint16(bytes, i); i += 2;
            ucNumOfVRAMModule = bytes[i++];
            ucMemoryClkPatchTblVer = bytes[i++];
            ucVramModuleVer = bytes[i++];
            ucMcPhyTileNum = bytes[i++];
        }
    }

    class ATOM_VRAM_ENTRY
    {
        public static final int size = 64;

        public long ulChannelMapCfg;    // uint32
        public int usModuleSize;        // uint16
        public int usMcRamCfg;          // uint16
        public int usEnableChannels;    // uint16
        public byte ucExtMemoryID;
        public byte ucMemoryType;
        public byte ucChannelNum;
        public byte ucChannelWidth;
        public byte ucDensity;
        public byte ucBankCol;
        public byte ucMisc;
        public byte ucVREFI;
        public int usReserved;          // uint16
        public int usMemorySize;        // uint16
        public byte ucMcTunningSetId;
        public byte ucRowNum;
        public int usEMRS2Value;        // uint16
        public int usEMRS3Value;        // uint16
        public byte ucMemoryVenderID;
        public byte ucRefreshRateFactor;
        public byte ucFIFODepth;
        public byte ucCDR_Bandwidth;
        public long ulChannelMapCfg1;   // uint32
        public long ulBankMapCfg;       // uint32
        public long ulReserved;         // uint32
        public final byte[] strMemPNString = new byte[20];   // 20 bytes

        public ATOM_VRAM_ENTRY(byte[] bytes) throws IllegalArgumentException
        {
            if(bytes.length != size)
                throw new IllegalArgumentException(String.format("ATOM_VRAM_ENTRY: expected %d bytes, got %d bytes", size, bytes.length));

            int i = 0;
            ulChannelMapCfg = bytes_to_uint32(bytes, i); i += 4;
            usModuleSize = bytes_to_uint16(bytes, i); i += 2;
            usMcRamCfg = bytes_to_uint16(bytes, i); i += 2;
            usEnableChannels = bytes_to_uint16(bytes, i); i += 2;
            ucExtMemoryID = bytes[i++];
            ucMemoryType = bytes[i++];
            ucChannelNum = bytes[i++];
            ucChannelWidth = bytes[i++];
            ucDensity = bytes[i++];
            ucBankCol = bytes[i++];
            ucMisc = bytes[i++];
            ucVREFI = bytes[i++];
            usReserved = bytes_to_uint16(bytes, i); i += 2;
            usMemorySize = bytes_to_uint16(bytes, i); i += 2;
            ucMcTunningSetId = bytes[i++];
            ucRowNum = bytes[i++];
            usEMRS2Value = bytes_to_uint16(bytes, i); i += 2;
            usEMRS3Value = bytes_to_uint16(bytes, i); i += 2;
            ucMemoryVenderID = bytes[i++];
            ucRefreshRateFactor = bytes[i++];
            ucFIFODepth = bytes[i++];
            ucCDR_Bandwidth = bytes[i++];
            ulChannelMapCfg1 = bytes_to_uint32(bytes, i); i += 4;
            ulBankMapCfg = bytes_to_uint32(bytes, i); i += 4;
            ulReserved = bytes_to_uint32(bytes, i); i += 4;
            System.arraycopy(bytes, i, strMemPNString, 0, 20);
        }
    }

    class ATOM_VRAM_TIMING_ENTRY
    {
        public static final int size = 0x34;

        public int ulClkRange;  // unsigned int, 3 bytes, units are kHz
        public byte ucIndex;
        public final byte[] ucLatency = new byte[0x30];

        public ATOM_VRAM_TIMING_ENTRY(byte[] bytes) throws IllegalArgumentException
        {
            if(bytes.length != size)
                throw new IllegalArgumentException(String.format("ATOM_VRAM_TIMING_ENTRY: expected %d bytes, got %d bytes", size, bytes.length));

            ulClkRange = Byte.toUnsignedInt(bytes[2]) << 16 | 
                         Byte.toUnsignedInt(bytes[1]) << 8 | 
                         Byte.toUnsignedInt(bytes[0]);
            ucIndex = bytes[3];
            System.arraycopy(bytes, 4, ucLatency, 0, 0x30);
        }
    }

    private byte[] bios_bytes;
    private int VRAM_Timings_offset;
}