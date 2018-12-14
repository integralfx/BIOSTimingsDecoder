import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class TimingsDecoder
{
	public static VBIOS_STRAP_R9 decode_r9_timings(String timings)
	{
		return new VBIOS_STRAP_R9(hex_string_to_bytes(timings));
	}

	public static VBIOS_STRAP_RX decode_rx_timings(String timings)
	{
		return new VBIOS_STRAP_RX(hex_string_to_bytes(timings));
	}

	public static String encode_r9_timings(VBIOS_STRAP_R9 timings)
	{
		StringBuilder str = new StringBuilder();

		str.append(timings.SEQ_WR_CTL_D1.to_hex_string());
		str.append(timings.SEQ_WR_CTL_2.to_hex_string());
		str.append(timings.SEQ_RAS_TIMING.to_hex_string());
		str.append(timings.SEQ_CAS_TIMING.to_hex_string());
		str.append(timings.SEQ_MISC_TIMING.to_hex_string());
		str.append(timings.SEQ_MISC_TIMING2.to_hex_string());
		str.append(timings.SEQ_PMG_TIMING.to_hex_string());
		str.append(String.format("%08X", (int)timings.SEQ_MISC1));
		str.append(String.format("%08X", (int)timings.SEQ_MISC3));
		str.append(String.format("%08X", (int)timings.SEQ_MISC8));
		str.append(timings.ARB_DRAM_TIMING.to_hex_string());
		str.append(timings.ARB_DRAM_TIMING2.to_hex_string());

		return str.toString();
	}

	public static String encode_rx_timings(VBIOS_STRAP_RX timings)
	{
		StringBuilder str = new StringBuilder();

		str.append(timings.SEQ_WR_CTL_D1.to_hex_string());
		str.append(timings.SEQ_WR_CTL_2.to_hex_string());
		str.append(timings.SEQ_PMG_TIMING.to_hex_string());
		str.append(timings.SEQ_RAS_TIMING.to_hex_string());
		str.append(timings.SEQ_CAS_TIMING.to_hex_string());
		str.append(timings.SEQ_MISC_TIMING.to_hex_string());
		str.append(timings.SEQ_MISC_TIMING2.to_hex_string());
		str.append(String.format("%08X", (int)timings.SEQ_MISC1));
		str.append(String.format("%08X", (int)timings.SEQ_MISC3));
		str.append(String.format("%08X", (int)timings.SEQ_MISC8));
		str.append(timings.ARB_DRAM_TIMING.to_hex_string());
		str.append(timings.ARB_DRAM_TIMING2.to_hex_string());

		return str.toString();
	}

	private static byte[] hex_string_to_bytes(String s)
	{
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) 
		{
			int h = Character.digit(s.charAt(i), 16) << 4,
				l = Character.digit(s.charAt(i + 1), 16);
			data[i / 2] = (byte)(h + l);
		}

		return data;
	}

	// treats 4 bytes as an unsigned int in little endian
	private static int bytes_to_int(byte[] bytes, int offset)
	{
		return Byte.toUnsignedInt(bytes[offset + 3]) << 24 |
			   Byte.toUnsignedInt(bytes[offset + 2]) << 16 |
			   Byte.toUnsignedInt(bytes[offset + 1]) << 8 |
			   Byte.toUnsignedInt(bytes[offset]);
	}

	static class SEQ_WR_CTL_D1_FORMAT
	{
		public byte DAT_DLY;		// 4
		public byte DQS_DLY;		// 4
		public byte DQS_XTR;		// 1
		public byte DAT_2Y_DLY;		// 1
		public byte ADR_2Y_DLY;		// 1
		public byte CMD_2Y_DLY;		// 1
		public byte OEN_DLY;		// 4
		public byte OEN_EXT;		// 4
		public byte OEN_SEL;		// 2
		public byte Pad0;		// 2
		public byte ODT_DLY;		// 4
		public byte ODT_EXT;		// 1
		public byte ADR_DLY;		// 1
		public byte CMD_DLY;		// 1
		public byte Pad1;		// 1
	
		public SEQ_WR_CTL_D1_FORMAT(int n)
		{
			DAT_DLY = (byte)(n & 0xf); n >>= 4;
			DQS_DLY = (byte)(n & 0xf); n >>= 4;
			DQS_XTR = (byte)(n & 0x1); n >>= 1;
			DAT_2Y_DLY = (byte)(n & 0x1); n >>= 1;
			ADR_2Y_DLY = (byte)(n & 0x1); n >>= 1;
			CMD_2Y_DLY = (byte)(n & 0x1); n >>= 1;
			OEN_DLY = (byte)(n & 0xf); n >>= 4;
			OEN_EXT = (byte)(n & 0xf); n >>= 4;
			OEN_SEL = (byte)(n & 0x3); n >>= 2;
			Pad0 = (byte)(n & 0x3); n >>= 2;
			ODT_DLY = (byte)(n & 0xf); n >>= 4;
			ODT_EXT = (byte)(n & 0x1); n >>= 1;
			ADR_DLY = (byte)(n & 0x1); n >>= 1;
			CMD_DLY = (byte)(n & 0x1); n >>= 1;
			Pad1 = (byte)(n & 0x1); n >>= 1;
		}
	
		public LinkedHashMap<String, Byte> get_timings()
		{
			LinkedHashMap<String, Byte> timings = new LinkedHashMap<>();
	
			timings.put("DAT_DLY", DAT_DLY);
			timings.put("DQS_DLY", DQS_DLY);
			timings.put("DQS_XTR", DQS_XTR);
			timings.put("DAT_2Y_DLY", DAT_2Y_DLY);
			timings.put("ADR_2Y_DLY", ADR_2Y_DLY);
			timings.put("CMD_2Y_DLY", CMD_2Y_DLY);
			timings.put("OEN_DLY", OEN_DLY);
			timings.put("OEN_EXT", OEN_EXT);
			timings.put("OEN_SEL", OEN_SEL);
			timings.put("Pad0", Pad0);
			timings.put("ODT_DLY", ODT_DLY);
			timings.put("ODT_EXT", ODT_EXT);
			timings.put("ADR_DLY", ADR_DLY);
			timings.put("CMD_DLY", CMD_DLY);
			timings.put("Pad1", Pad1);
	
			return timings;
		}
	
		public String to_hex_string()
		{
			int i = 0, n = 0;
	
			n |= (Byte.toUnsignedInt(DAT_DLY) & 0xf) << i; i += 4;
			n |= (Byte.toUnsignedInt(DQS_DLY) & 0xf) << i; i += 4;
			n |= (Byte.toUnsignedInt(DQS_XTR) & 0x1) << i; i += 1;
			n |= (Byte.toUnsignedInt(DAT_2Y_DLY) & 0x1) << i; i += 1;
			n |= (Byte.toUnsignedInt(ADR_2Y_DLY) & 0x1) << i; i += 1;
			n |= (Byte.toUnsignedInt(CMD_2Y_DLY) & 0x1) << i; i += 1;
			n |= (Byte.toUnsignedInt(OEN_DLY) & 0xf) << i; i += 4;
			n |= (Byte.toUnsignedInt(OEN_EXT) & 0xf) << i; i += 4;
			n |= (Byte.toUnsignedInt(OEN_SEL) & 0x3) << i; i += 2;
			n |= (Byte.toUnsignedInt(Pad0) & 0x3) << i; i += 2;
			n |= (Byte.toUnsignedInt(ODT_DLY) & 0xf) << i; i += 4;
			n |= (Byte.toUnsignedInt(ODT_EXT) & 0x1) << i; i += 1;
			n |= (Byte.toUnsignedInt(ADR_DLY) & 0x1) << i; i += 1;
			n |= (Byte.toUnsignedInt(CMD_DLY) & 0x1) << i; i += 1;
			n |= (Byte.toUnsignedInt(Pad1) & 0x1) << i; i += 1;
	
			String hex = String.format("%08X", n);
			return hex.substring(6, 8) + hex.substring(4, 6) + hex.substring(2, 4) + hex.substring(0, 2);
		}
	
		public LinkedHashMap<String, Integer> get_sizes()
		{
			LinkedHashMap<String, Integer> sizes = new LinkedHashMap<>();
	
			sizes.put("DAT_DLY", 4);
			sizes.put("DQS_DLY", 4);
			sizes.put("DQS_XTR", 1);
			sizes.put("DAT_2Y_DLY", 1);
			sizes.put("ADR_2Y_DLY", 1);
			sizes.put("CMD_2Y_DLY", 1);
			sizes.put("OEN_DLY", 4);
			sizes.put("OEN_EXT", 4);
			sizes.put("OEN_SEL", 2);
			sizes.put("Pad0", 2);
			sizes.put("ODT_DLY", 4);
			sizes.put("ODT_EXT", 1);
			sizes.put("ADR_DLY", 1);
			sizes.put("CMD_DLY", 1);
			sizes.put("Pad1", 1);
	
			return sizes;
		}
	
		public static final String[] names = {
			"DAT_DLY", "DQS_DLY", "DQS_XTR", "DAT_2Y_DLY", 
			"ADR_2Y_DLY", "CMD_2Y_DLY", "OEN_DLY", "OEN_EXT", 
			"OEN_SEL", "Pad0", "ODT_DLY", "ODT_EXT", 
			"ADR_DLY", "CMD_DLY", "Pad1"
		};
	}
	
	static class SEQ_WR_CTL_2_FORMAT
	{
		public byte DAT_DLY_H_D0;		// 1
		public byte DQS_DLY_H_D0;		// 1
		public byte OEN_DLY_H_D0;		// 1
		public byte DAT_DLY_H_D1;		// 1
		public byte DQS_DLY_H_D1;		// 1
		public byte OEN_DLY_H_D1;		// 1
		public byte WCDR_EN;		// 1
		public byte Pad0;		// 25
	
		public SEQ_WR_CTL_2_FORMAT(int n)
		{
			DAT_DLY_H_D0 = (byte)(n & 0x1); n >>= 1;
			DQS_DLY_H_D0 = (byte)(n & 0x1); n >>= 1;
			OEN_DLY_H_D0 = (byte)(n & 0x1); n >>= 1;
			DAT_DLY_H_D1 = (byte)(n & 0x1); n >>= 1;
			DQS_DLY_H_D1 = (byte)(n & 0x1); n >>= 1;
			OEN_DLY_H_D1 = (byte)(n & 0x1); n >>= 1;
			WCDR_EN = (byte)(n & 0x1); n >>= 1;
			Pad0 = (byte)(n & 0x1ffffff); n >>= 25;
		}
	
		public LinkedHashMap<String, Byte> get_timings()
		{
			LinkedHashMap<String, Byte> timings = new LinkedHashMap<>();
	
			timings.put("DAT_DLY_H_D0", DAT_DLY_H_D0);
			timings.put("DQS_DLY_H_D0", DQS_DLY_H_D0);
			timings.put("OEN_DLY_H_D0", OEN_DLY_H_D0);
			timings.put("DAT_DLY_H_D1", DAT_DLY_H_D1);
			timings.put("DQS_DLY_H_D1", DQS_DLY_H_D1);
			timings.put("OEN_DLY_H_D1", OEN_DLY_H_D1);
			timings.put("WCDR_EN", WCDR_EN);
			timings.put("Pad0", Pad0);
	
			return timings;
		}
	
		public String to_hex_string()
		{
			int i = 0, n = 0;
	
			n |= (Byte.toUnsignedInt(DAT_DLY_H_D0) & 0x1) << i; i += 1;
			n |= (Byte.toUnsignedInt(DQS_DLY_H_D0) & 0x1) << i; i += 1;
			n |= (Byte.toUnsignedInt(OEN_DLY_H_D0) & 0x1) << i; i += 1;
			n |= (Byte.toUnsignedInt(DAT_DLY_H_D1) & 0x1) << i; i += 1;
			n |= (Byte.toUnsignedInt(DQS_DLY_H_D1) & 0x1) << i; i += 1;
			n |= (Byte.toUnsignedInt(OEN_DLY_H_D1) & 0x1) << i; i += 1;
			n |= (Byte.toUnsignedInt(WCDR_EN) & 0x1) << i; i += 1;
			n |= (Byte.toUnsignedInt(Pad0) & 0x1ffffff) << i; i += 25;
	
			String hex = String.format("%08X", n);
			return hex.substring(6, 8) + hex.substring(4, 6) + hex.substring(2, 4) + hex.substring(0, 2);
		}
	
		public LinkedHashMap<String, Integer> get_sizes()
		{
			LinkedHashMap<String, Integer> sizes = new LinkedHashMap<>();
	
			sizes.put("DAT_DLY_H_D0", 1);
			sizes.put("DQS_DLY_H_D0", 1);
			sizes.put("OEN_DLY_H_D0", 1);
			sizes.put("DAT_DLY_H_D1", 1);
			sizes.put("DQS_DLY_H_D1", 1);
			sizes.put("OEN_DLY_H_D1", 1);
			sizes.put("WCDR_EN", 1);
			sizes.put("Pad0", 25);
	
			return sizes;
		}
	
		public static final String[] names = {
			"DAT_DLY_H_D0", "DQS_DLY_H_D0", "OEN_DLY_H_D0", "DAT_DLY_H_D1", 
			"DQS_DLY_H_D1", "OEN_DLY_H_D1", "WCDR_EN", "Pad0"
	
		};
	}
	
	static class SEQ_PMG_TIMING_FORMAT
	{
		public byte TCKSRE;		// 3
		public byte Pad0;		// 1
		public byte TCKSRX;		// 3
		public byte Pad1;		// 1
		public byte TCKE_PULSE;		// 4
		public byte TCKE;		// 6
		public byte SEQ_IDLE;		// 3
		public byte Pad2;		// 2
		public byte TCKE_PULSE_MSB;		// 1
		public byte SEQ_IDLE_SS;		// 8
	
		public SEQ_PMG_TIMING_FORMAT(int n)
		{
			TCKSRE = (byte)(n & 0x7); n >>= 3;
			Pad0 = (byte)(n & 0x1); n >>= 1;
			TCKSRX = (byte)(n & 0x7); n >>= 3;
			Pad1 = (byte)(n & 0x1); n >>= 1;
			TCKE_PULSE = (byte)(n & 0xf); n >>= 4;
			TCKE = (byte)(n & 0x3f); n >>= 6;
			SEQ_IDLE = (byte)(n & 0x7); n >>= 3;
			Pad2 = (byte)(n & 0x3); n >>= 2;
			TCKE_PULSE_MSB = (byte)(n & 0x1); n >>= 1;
			SEQ_IDLE_SS = (byte)(n & 0xff); n >>= 8;
		}
	
		public LinkedHashMap<String, Byte> get_timings()
		{
			LinkedHashMap<String, Byte> timings = new LinkedHashMap<>();
	
			timings.put("TCKSRE", TCKSRE);
			timings.put("Pad0", Pad0);
			timings.put("TCKSRX", TCKSRX);
			timings.put("Pad1", Pad1);
			timings.put("TCKE_PULSE", TCKE_PULSE);
			timings.put("TCKE", TCKE);
			timings.put("SEQ_IDLE", SEQ_IDLE);
			timings.put("Pad2", Pad2);
			timings.put("TCKE_PULSE_MSB", TCKE_PULSE_MSB);
			timings.put("SEQ_IDLE_SS", SEQ_IDLE_SS);
	
			return timings;
		}
	
		public String to_hex_string()
		{
			int i = 0, n = 0;
	
			n |= (Byte.toUnsignedInt(TCKSRE) & 0x7) << i; i += 3;
			n |= (Byte.toUnsignedInt(Pad0) & 0x1) << i; i += 1;
			n |= (Byte.toUnsignedInt(TCKSRX) & 0x7) << i; i += 3;
			n |= (Byte.toUnsignedInt(Pad1) & 0x1) << i; i += 1;
			n |= (Byte.toUnsignedInt(TCKE_PULSE) & 0xf) << i; i += 4;
			n |= (Byte.toUnsignedInt(TCKE) & 0x3f) << i; i += 6;
			n |= (Byte.toUnsignedInt(SEQ_IDLE) & 0x7) << i; i += 3;
			n |= (Byte.toUnsignedInt(Pad2) & 0x3) << i; i += 2;
			n |= (Byte.toUnsignedInt(TCKE_PULSE_MSB) & 0x1) << i; i += 1;
			n |= (Byte.toUnsignedInt(SEQ_IDLE_SS) & 0xff) << i; i += 8;
	
			String hex = String.format("%08X", n);
			return hex.substring(6, 8) + hex.substring(4, 6) + hex.substring(2, 4) + hex.substring(0, 2);
		}
	
		public LinkedHashMap<String, Integer> get_sizes()
		{
			LinkedHashMap<String, Integer> sizes = new LinkedHashMap<>();
	
			sizes.put("TCKSRE", 3);
			sizes.put("Pad0", 1);
			sizes.put("TCKSRX", 3);
			sizes.put("Pad1", 1);
			sizes.put("TCKE_PULSE", 4);
			sizes.put("TCKE", 6);
			sizes.put("SEQ_IDLE", 3);
			sizes.put("Pad2", 2);
			sizes.put("TCKE_PULSE_MSB", 1);
			sizes.put("SEQ_IDLE_SS", 8);
	
			return sizes;
		}
	
		public static final String[] names = {
			"TCKSRE", "Pad0", "TCKSRX", "Pad1", 
			"TCKE_PULSE", "TCKE", "SEQ_IDLE", "Pad2", 
			"TCKE_PULSE_MSB", "SEQ_IDLE_SS"
		};
	}
	
	static class SEQ_RAS_TIMING_FORMAT
	{
		public byte TRCDW;		// 5
		public byte TRCDWA;		// 5
		public byte TRCDR;		// 5
		public byte TRCDRA;		// 5
		public byte TRRD;		// 4
		public byte TRC;		// 7
		public byte Pad0;		// 1
	
		public SEQ_RAS_TIMING_FORMAT(int n)
		{
			TRCDW = (byte)(n & 0x1f); n >>= 5;
			TRCDWA = (byte)(n & 0x1f); n >>= 5;
			TRCDR = (byte)(n & 0x1f); n >>= 5;
			TRCDRA = (byte)(n & 0x1f); n >>= 5;
			TRRD = (byte)(n & 0xf); n >>= 4;
			TRC = (byte)(n & 0x7f); n >>= 7;
			Pad0 = (byte)(n & 0x1); n >>= 1;
		}
	
		public LinkedHashMap<String, Byte> get_timings()
		{
			LinkedHashMap<String, Byte> timings = new LinkedHashMap<>();
	
			timings.put("TRCDW", TRCDW);
			timings.put("TRCDWA", TRCDWA);
			timings.put("TRCDR", TRCDR);
			timings.put("TRCDRA", TRCDRA);
			timings.put("TRRD", TRRD);
			timings.put("TRC", TRC);
			timings.put("Pad0", Pad0);
	
			return timings;
		}
	
		public String to_hex_string()
		{
			int i = 0, n = 0;
	
			n |= (Byte.toUnsignedInt(TRCDW) & 0x1f) << i; i += 5;
			n |= (Byte.toUnsignedInt(TRCDWA) & 0x1f) << i; i += 5;
			n |= (Byte.toUnsignedInt(TRCDR) & 0x1f) << i; i += 5;
			n |= (Byte.toUnsignedInt(TRCDRA) & 0x1f) << i; i += 5;
			n |= (Byte.toUnsignedInt(TRRD) & 0xf) << i; i += 4;
			n |= (Byte.toUnsignedInt(TRC) & 0x7f) << i; i += 7;
			n |= (Byte.toUnsignedInt(Pad0) & 0x1) << i; i += 1;
	
			String hex = String.format("%08X", n);
			return hex.substring(6, 8) + hex.substring(4, 6) + hex.substring(2, 4) + hex.substring(0, 2);
		}
	
		public LinkedHashMap<String, Integer> get_sizes()
		{
			LinkedHashMap<String, Integer> sizes = new LinkedHashMap<>();
	
			sizes.put("TRCDW", 5);
			sizes.put("TRCDWA", 5);
			sizes.put("TRCDR", 5);
			sizes.put("TRCDRA", 5);
			sizes.put("TRRD", 4);
			sizes.put("TRC", 7);
			sizes.put("Pad0", 1);
	
			return sizes;
		}
	
		public static final String[] names = {
			"TRCDW", "TRCDWA", "TRCDR", "TRCDRA", 
			"TRRD", "TRC", "Pad0"
		};
	}
	
	static class SEQ_CAS_TIMING_FORMAT
	{
		public byte TNOPW;		// 2
		public byte TNOPR;		// 2
		public byte TR2W;		// 5
		public byte TCCDL;		// 3
		public byte TCCDS;		// 4
		public byte TW2R;		// 5
		public byte Pad0;		// 3
		public byte TCL;		// 5
		public byte Pad1;		// 3
	
		public SEQ_CAS_TIMING_FORMAT(int n)
		{
			TNOPW = (byte)(n & 0x3); n >>= 2;
			TNOPR = (byte)(n & 0x3); n >>= 2;
			TR2W = (byte)(n & 0x1f); n >>= 5;
			TCCDL = (byte)(n & 0x7); n >>= 3;
			TCCDS = (byte)(n & 0xf); n >>= 4;
			TW2R = (byte)(n & 0x1f); n >>= 5;
			Pad0 = (byte)(n & 0x7); n >>= 3;
			TCL = (byte)(n & 0x1f); n >>= 5;
			Pad1 = (byte)(n & 0x7); n >>= 3;
		}
	
		public LinkedHashMap<String, Byte> get_timings()
		{
			LinkedHashMap<String, Byte> timings = new LinkedHashMap<>();
	
			timings.put("TNOPW", TNOPW);
			timings.put("TNOPR", TNOPR);
			timings.put("TR2W", TR2W);
			timings.put("TCCDL", TCCDL);
			timings.put("TCCDS", TCCDS);
			timings.put("TW2R", TW2R);
			timings.put("Pad0", Pad0);
			timings.put("TCL", TCL);
			timings.put("Pad1", Pad1);
	
			return timings;
		}
	
		public String to_hex_string()
		{
			int i = 0, n = 0;
	
			n |= (Byte.toUnsignedInt(TNOPW) & 0x3) << i; i += 2;
			n |= (Byte.toUnsignedInt(TNOPR) & 0x3) << i; i += 2;
			n |= (Byte.toUnsignedInt(TR2W) & 0x1f) << i; i += 5;
			n |= (Byte.toUnsignedInt(TCCDL) & 0x7) << i; i += 3;
			n |= (Byte.toUnsignedInt(TCCDS) & 0xf) << i; i += 4;
			n |= (Byte.toUnsignedInt(TW2R) & 0x1f) << i; i += 5;
			n |= (Byte.toUnsignedInt(Pad0) & 0x7) << i; i += 3;
			n |= (Byte.toUnsignedInt(TCL) & 0x1f) << i; i += 5;
			n |= (Byte.toUnsignedInt(Pad1) & 0x7) << i; i += 3;
	
			String hex = String.format("%08X", n);
			return hex.substring(6, 8) + hex.substring(4, 6) + hex.substring(2, 4) + hex.substring(0, 2);
		}
	
		public LinkedHashMap<String, Integer> get_sizes()
		{
			LinkedHashMap<String, Integer> sizes = new LinkedHashMap<>();
	
			sizes.put("TNOPW", 2);
			sizes.put("TNOPR", 2);
			sizes.put("TR2W", 5);
			sizes.put("TCCDL", 3);
			sizes.put("TCCDS", 4);
			sizes.put("TW2R", 5);
			sizes.put("Pad0", 3);
			sizes.put("TCL", 5);
			sizes.put("Pad1", 3);
	
			return sizes;
		}
	
		public static final String[] names = {
			"TNOPW", "TNOPR", "TR2W", "TCCDL", 
			"TCCDS", "TW2R", "Pad0", "TCL", 
			"Pad1"
		};
	}
	
	static class SEQ_MISC_TIMING_FORMAT_RX
	{
		public byte TRP_WRA;		// 7
		public byte TRP_RDA;		// 7
		public byte TRP;		// 6
		public byte TRFC;		// 9
		public byte Pad0;		// 3
	
		public SEQ_MISC_TIMING_FORMAT_RX(int n)
		{
			TRP_WRA = (byte)(n & 0x7f); n >>= 7;
			TRP_RDA = (byte)(n & 0x7f); n >>= 7;
			TRP = (byte)(n & 0x3f); n >>= 6;
			TRFC = (byte)(n & 0x1ff); n >>= 9;
			Pad0 = (byte)(n & 0x7); n >>= 3;
		}
	
		public LinkedHashMap<String, Byte> get_timings()
		{
			LinkedHashMap<String, Byte> timings = new LinkedHashMap<>();
	
			timings.put("TRP_WRA", TRP_WRA);
			timings.put("TRP_RDA", TRP_RDA);
			timings.put("TRP", TRP);
			timings.put("TRFC", TRFC);
			timings.put("Pad0", Pad0);
	
			return timings;
		}
	
		public String to_hex_string()
		{
			int i = 0, n = 0;
	
			n |= (Byte.toUnsignedInt(TRP_WRA) & 0x7f) << i; i += 7;
			n |= (Byte.toUnsignedInt(TRP_RDA) & 0x7f) << i; i += 7;
			n |= (Byte.toUnsignedInt(TRP) & 0x3f) << i; i += 6;
			n |= (Byte.toUnsignedInt(TRFC) & 0x1ff) << i; i += 9;
			n |= (Byte.toUnsignedInt(Pad0) & 0x7) << i; i += 3;
	
			String hex = String.format("%08X", n);
			return hex.substring(6, 8) + hex.substring(4, 6) + hex.substring(2, 4) + hex.substring(0, 2);
		}
	
		public LinkedHashMap<String, Integer> get_sizes()
		{
			LinkedHashMap<String, Integer> sizes = new LinkedHashMap<>();
	
			sizes.put("TRP_WRA", 7);
			sizes.put("TRP_RDA", 7);
			sizes.put("TRP", 6);
			sizes.put("TRFC", 9);
			sizes.put("Pad0", 3);
	
			return sizes;
		}
	
		public static final String[] names = {
			"TRP_WRA", "TRP_RDA", "TRP", "TRFC", 
			"Pad0"
		};
	}
	
	static class SEQ_MISC_TIMING_FORMAT_R9
	{
		public byte TRP_WRA;		// 8
		public byte TRP_RDA;		// 7
		public byte TRP;		// 5
		public byte TRFC;		// 9
		public byte Pad0;		// 3
	
		public SEQ_MISC_TIMING_FORMAT_R9(int n)
		{
			TRP_WRA = (byte)(n & 0xff); n >>= 8;
			TRP_RDA = (byte)(n & 0x7f); n >>= 7;
			TRP = (byte)(n & 0x1f); n >>= 5;
			TRFC = (byte)(n & 0x1ff); n >>= 9;
			Pad0 = (byte)(n & 0x7); n >>= 3;
		}
	
		public LinkedHashMap<String, Byte> get_timings()
		{
			LinkedHashMap<String, Byte> timings = new LinkedHashMap<>();
	
			timings.put("TRP_WRA", TRP_WRA);
			timings.put("TRP_RDA", TRP_RDA);
			timings.put("TRP", TRP);
			timings.put("TRFC", TRFC);
			timings.put("Pad0", Pad0);
	
			return timings;
		}
	
		public String to_hex_string()
		{
			int i = 0, n = 0;
	
			n |= (Byte.toUnsignedInt(TRP_WRA) & 0xff) << i; i += 8;
			n |= (Byte.toUnsignedInt(TRP_RDA) & 0x7f) << i; i += 7;
			n |= (Byte.toUnsignedInt(TRP) & 0x1f) << i; i += 5;
			n |= (Byte.toUnsignedInt(TRFC) & 0x1ff) << i; i += 9;
			n |= (Byte.toUnsignedInt(Pad0) & 0x7) << i; i += 3;
	
			String hex = String.format("%08X", n);
			return hex.substring(6, 8) + hex.substring(4, 6) + hex.substring(2, 4) + hex.substring(0, 2);
		}
	
		public LinkedHashMap<String, Integer> get_sizes()
		{
			LinkedHashMap<String, Integer> sizes = new LinkedHashMap<>();
	
			sizes.put("TRP_WRA", 8);
			sizes.put("TRP_RDA", 7);
			sizes.put("TRP", 5);
			sizes.put("TRFC", 9);
			sizes.put("Pad0", 3);
	
			return sizes;
		}
	
		public static final String[] names = {
			"TRP_WRA", "TRP_RDA", "TRP", "TRFC", 
			"Pad0"
		};
	}
	
	static class SEQ_MISC_TIMING2_FORMAT
	{
		public byte PA2RDATA;		// 3
		public byte Pad0;		// 1
		public byte PA2WDATA;		// 3
		public byte Pad1;		// 1
		public byte TFAW;		// 5
		public byte TCRCRL;		// 3
		public byte TCRCWL;		// 5
		public byte T32AW;		// 4
		public byte Pad2;		// 3
		public byte TWDATATR;		// 4
	
		public SEQ_MISC_TIMING2_FORMAT(int n)
		{
			PA2RDATA = (byte)(n & 0x7); n >>= 3;
			Pad0 = (byte)(n & 0x1); n >>= 1;
			PA2WDATA = (byte)(n & 0x7); n >>= 3;
			Pad1 = (byte)(n & 0x1); n >>= 1;
			TFAW = (byte)(n & 0x1f); n >>= 5;
			TCRCRL = (byte)(n & 0x7); n >>= 3;
			TCRCWL = (byte)(n & 0x1f); n >>= 5;
			T32AW = (byte)(n & 0xf); n >>= 4;
			Pad2 = (byte)(n & 0x7); n >>= 3;
			TWDATATR = (byte)(n & 0xf); n >>= 4;
		}
	
		public LinkedHashMap<String, Byte> get_timings()
		{
			LinkedHashMap<String, Byte> timings = new LinkedHashMap<>();
	
			timings.put("PA2RDATA", PA2RDATA);
			timings.put("Pad0", Pad0);
			timings.put("PA2WDATA", PA2WDATA);
			timings.put("Pad1", Pad1);
			timings.put("TFAW", TFAW);
			timings.put("TCRCRL", TCRCRL);
			timings.put("TCRCWL", TCRCWL);
			timings.put("T32AW", T32AW);
			timings.put("Pad2", Pad2);
			timings.put("TWDATATR", TWDATATR);
	
			return timings;
		}
	
		public String to_hex_string()
		{
			int i = 0, n = 0;
	
			n |= (Byte.toUnsignedInt(PA2RDATA) & 0x7) << i; i += 3;
			n |= (Byte.toUnsignedInt(Pad0) & 0x1) << i; i += 1;
			n |= (Byte.toUnsignedInt(PA2WDATA) & 0x7) << i; i += 3;
			n |= (Byte.toUnsignedInt(Pad1) & 0x1) << i; i += 1;
			n |= (Byte.toUnsignedInt(TFAW) & 0x1f) << i; i += 5;
			n |= (Byte.toUnsignedInt(TCRCRL) & 0x7) << i; i += 3;
			n |= (Byte.toUnsignedInt(TCRCWL) & 0x1f) << i; i += 5;
			n |= (Byte.toUnsignedInt(T32AW) & 0xf) << i; i += 4;
			n |= (Byte.toUnsignedInt(Pad2) & 0x7) << i; i += 3;
			n |= (Byte.toUnsignedInt(TWDATATR) & 0xf) << i; i += 4;
	
			String hex = String.format("%08X", n);
			return hex.substring(6, 8) + hex.substring(4, 6) + hex.substring(2, 4) + hex.substring(0, 2);
		}
	
		public LinkedHashMap<String, Integer> get_sizes()
		{
			LinkedHashMap<String, Integer> sizes = new LinkedHashMap<>();
	
			sizes.put("PA2RDATA", 3);
			sizes.put("Pad0", 1);
			sizes.put("PA2WDATA", 3);
			sizes.put("Pad1", 1);
			sizes.put("TFAW", 5);
			sizes.put("TCRCRL", 3);
			sizes.put("TCRCWL", 5);
			sizes.put("T32AW", 4);
			sizes.put("Pad2", 3);
			sizes.put("TWDATATR", 4);
	
			return sizes;
		}
	
		public static final String[] names = {
			"PA2RDATA", "Pad0", "PA2WDATA", "Pad1", 
			"TFAW", "TCRCRL", "TCRCWL", "T32AW", 
			"Pad2", "TWDATATR"
		};
	}
	
	static class ARB_DRAM_TIMING_FORMAT
	{
		public byte ACTRD;		// 8
		public byte ACTWR;		// 8
		public byte RASMACTRD;		// 8
		public byte RASMACTWR;		// 8
	
		public ARB_DRAM_TIMING_FORMAT(int n)
		{
			ACTRD = (byte)(n & 0xff); n >>= 8;
			ACTWR = (byte)(n & 0xff); n >>= 8;
			RASMACTRD = (byte)(n & 0xff); n >>= 8;
			RASMACTWR = (byte)(n & 0xff); n >>= 8;
		}
	
		public LinkedHashMap<String, Byte> get_timings()
		{
			LinkedHashMap<String, Byte> timings = new LinkedHashMap<>();
	
			timings.put("ACTRD", ACTRD);
			timings.put("ACTWR", ACTWR);
			timings.put("RASMACTRD", RASMACTRD);
			timings.put("RASMACTWR", RASMACTWR);
	
			return timings;
		}
	
		public String to_hex_string()
		{
			int i = 0, n = 0;
	
			n |= (Byte.toUnsignedInt(ACTRD) & 0xff) << i; i += 8;
			n |= (Byte.toUnsignedInt(ACTWR) & 0xff) << i; i += 8;
			n |= (Byte.toUnsignedInt(RASMACTRD) & 0xff) << i; i += 8;
			n |= (Byte.toUnsignedInt(RASMACTWR) & 0xff) << i; i += 8;
	
			String hex = String.format("%08X", n);
			return hex.substring(6, 8) + hex.substring(4, 6) + hex.substring(2, 4) + hex.substring(0, 2);
		}
	
		public LinkedHashMap<String, Integer> get_sizes()
		{
			LinkedHashMap<String, Integer> sizes = new LinkedHashMap<>();
	
			sizes.put("ACTRD", 8);
			sizes.put("ACTWR", 8);
			sizes.put("RASMACTRD", 8);
			sizes.put("RASMACTWR", 8);
	
			return sizes;
		}
	
		public static final String[] names = {
			"ACTRD", "ACTWR", "RASMACTRD", "RASMACTWR"
	
		};
	}
	
	static class ARB_DRAM_TIMING2_FORMAT
	{
		public byte RAS2RAS;		// 8
		public byte RP;		// 8
		public byte WRPLUSRP;		// 8
		public byte BUS_TURN;		// 8
	
		public ARB_DRAM_TIMING2_FORMAT(int n)
		{
			RAS2RAS = (byte)(n & 0xff); n >>= 8;
			RP = (byte)(n & 0xff); n >>= 8;
			WRPLUSRP = (byte)(n & 0xff); n >>= 8;
			BUS_TURN = (byte)(n & 0xff); n >>= 8;
		}
	
		public LinkedHashMap<String, Byte> get_timings()
		{
			LinkedHashMap<String, Byte> timings = new LinkedHashMap<>();
	
			timings.put("RAS2RAS", RAS2RAS);
			timings.put("RP", RP);
			timings.put("WRPLUSRP", WRPLUSRP);
			timings.put("BUS_TURN", BUS_TURN);
	
			return timings;
		}
	
		public String to_hex_string()
		{
			int i = 0, n = 0;
	
			n |= (Byte.toUnsignedInt(RAS2RAS) & 0xff) << i; i += 8;
			n |= (Byte.toUnsignedInt(RP) & 0xff) << i; i += 8;
			n |= (Byte.toUnsignedInt(WRPLUSRP) & 0xff) << i; i += 8;
			n |= (Byte.toUnsignedInt(BUS_TURN) & 0xff) << i; i += 8;
	
			String hex = String.format("%08X", n);
			return hex.substring(6, 8) + hex.substring(4, 6) + hex.substring(2, 4) + hex.substring(0, 2);
		}
	
		public LinkedHashMap<String, Integer> get_sizes()
		{
			LinkedHashMap<String, Integer> sizes = new LinkedHashMap<>();
	
			sizes.put("RAS2RAS", 8);
			sizes.put("RP", 8);
			sizes.put("WRPLUSRP", 8);
			sizes.put("BUS_TURN", 8);
	
			return sizes;
		}
	
		public static final String[] names = {
			"RAS2RAS", "RP", "WRPLUSRP", "BUS_TURN"
	
		};
	}
	
	static class VBIOS_STRAP_R9
	{
		public static final int size = 48;

		public SEQ_WR_CTL_D1_FORMAT SEQ_WR_CTL_D1;
		public SEQ_WR_CTL_2_FORMAT SEQ_WR_CTL_2;
		public SEQ_RAS_TIMING_FORMAT SEQ_RAS_TIMING;
		public SEQ_CAS_TIMING_FORMAT SEQ_CAS_TIMING;
		public SEQ_MISC_TIMING_FORMAT_R9 SEQ_MISC_TIMING;
		public SEQ_MISC_TIMING2_FORMAT SEQ_MISC_TIMING2;
		public SEQ_PMG_TIMING_FORMAT SEQ_PMG_TIMING;
		public long SEQ_MISC1;		// uint32
		public long SEQ_MISC3;		// uint32
		public long SEQ_MISC8;		// uint32
		public ARB_DRAM_TIMING_FORMAT ARB_DRAM_TIMING;
		public ARB_DRAM_TIMING2_FORMAT ARB_DRAM_TIMING2;

		public VBIOS_STRAP_R9()
		{
			SEQ_WR_CTL_D1 = new SEQ_WR_CTL_D1_FORMAT(0);
			SEQ_WR_CTL_2 = new SEQ_WR_CTL_2_FORMAT(0);
			SEQ_RAS_TIMING = new SEQ_RAS_TIMING_FORMAT(0);
			SEQ_CAS_TIMING = new SEQ_CAS_TIMING_FORMAT(0);
			SEQ_MISC_TIMING = new SEQ_MISC_TIMING_FORMAT_R9(0);
			SEQ_MISC_TIMING2 = new SEQ_MISC_TIMING2_FORMAT(0);
			SEQ_PMG_TIMING = new SEQ_PMG_TIMING_FORMAT(0);
			ARB_DRAM_TIMING = new ARB_DRAM_TIMING_FORMAT(0);
			ARB_DRAM_TIMING2 = new ARB_DRAM_TIMING2_FORMAT(0);
		}

		public VBIOS_STRAP_R9(byte[] bytes)
		{
			if(bytes.length != size) 
				throw new IllegalArgumentException(String.format("VBIOS_STRAP_R9: expected %d bytes, got %d bytes", size, bytes.length));
				
			int i = 0;
			SEQ_WR_CTL_D1 = new SEQ_WR_CTL_D1_FORMAT(bytes_to_int(bytes, i)); i += 4;
			SEQ_WR_CTL_2 = new SEQ_WR_CTL_2_FORMAT(bytes_to_int(bytes, i)); i += 4;
			SEQ_RAS_TIMING = new SEQ_RAS_TIMING_FORMAT(bytes_to_int(bytes, i)); i += 4;
			SEQ_CAS_TIMING = new SEQ_CAS_TIMING_FORMAT(bytes_to_int(bytes, i)); i += 4;
			SEQ_MISC_TIMING = new SEQ_MISC_TIMING_FORMAT_R9(bytes_to_int(bytes, i)); i += 4;
			SEQ_MISC_TIMING2 = new SEQ_MISC_TIMING2_FORMAT(bytes_to_int(bytes, i)); i += 4;
			SEQ_PMG_TIMING = new SEQ_PMG_TIMING_FORMAT(bytes_to_int(bytes, i)); i += 4;
			SEQ_MISC1 = bytes_to_int(bytes, i); i += 4;
			SEQ_MISC3 = bytes_to_int(bytes, i); i += 4;
			SEQ_MISC8 = bytes_to_int(bytes, i); i += 4;
			ARB_DRAM_TIMING = new ARB_DRAM_TIMING_FORMAT(bytes_to_int(bytes, i)); i += 4;
			ARB_DRAM_TIMING2 = new ARB_DRAM_TIMING2_FORMAT(bytes_to_int(bytes, i)); i += 4;
		}
	}

	static class VBIOS_STRAP_RX
	{
		public static final int size = 48;

		public SEQ_WR_CTL_D1_FORMAT SEQ_WR_CTL_D1;
		public SEQ_WR_CTL_2_FORMAT SEQ_WR_CTL_2;
		public SEQ_PMG_TIMING_FORMAT SEQ_PMG_TIMING;
		public SEQ_RAS_TIMING_FORMAT SEQ_RAS_TIMING;
		public SEQ_CAS_TIMING_FORMAT SEQ_CAS_TIMING;
		public SEQ_MISC_TIMING_FORMAT_RX SEQ_MISC_TIMING;
		public SEQ_MISC_TIMING2_FORMAT SEQ_MISC_TIMING2;
		public long SEQ_MISC1;		// uint32
		public long SEQ_MISC3;		// uint32
		public long SEQ_MISC8;		// uint32
		public ARB_DRAM_TIMING_FORMAT ARB_DRAM_TIMING;
		public ARB_DRAM_TIMING2_FORMAT ARB_DRAM_TIMING2;

		public VBIOS_STRAP_RX()
		{
			SEQ_WR_CTL_D1 = new SEQ_WR_CTL_D1_FORMAT(0);
			SEQ_WR_CTL_2 = new SEQ_WR_CTL_2_FORMAT(0);
			SEQ_PMG_TIMING = new SEQ_PMG_TIMING_FORMAT(0);
			SEQ_RAS_TIMING = new SEQ_RAS_TIMING_FORMAT(0);
			SEQ_CAS_TIMING = new SEQ_CAS_TIMING_FORMAT(0);
			SEQ_MISC_TIMING = new SEQ_MISC_TIMING_FORMAT_RX(0);
			SEQ_MISC_TIMING2 = new SEQ_MISC_TIMING2_FORMAT(0);
			ARB_DRAM_TIMING = new ARB_DRAM_TIMING_FORMAT(0);
			ARB_DRAM_TIMING2 = new ARB_DRAM_TIMING2_FORMAT(0);
		}

		public VBIOS_STRAP_RX(byte[] bytes)
		{
			if(bytes.length != size) 
				throw new IllegalArgumentException(String.format("VBIOS_STRAP_RX: expected %d bytes, got %d bytes", size, bytes.length));
				
			int i = 0;
			SEQ_WR_CTL_D1 = new SEQ_WR_CTL_D1_FORMAT(bytes_to_int(bytes, i)); i += 4;
			SEQ_WR_CTL_2 = new SEQ_WR_CTL_2_FORMAT(bytes_to_int(bytes, i)); i += 4;
			SEQ_PMG_TIMING = new SEQ_PMG_TIMING_FORMAT(bytes_to_int(bytes, i)); i += 4;
			SEQ_RAS_TIMING = new SEQ_RAS_TIMING_FORMAT(bytes_to_int(bytes, i)); i += 4;
			SEQ_CAS_TIMING = new SEQ_CAS_TIMING_FORMAT(bytes_to_int(bytes, i)); i += 4;
			SEQ_MISC_TIMING = new SEQ_MISC_TIMING_FORMAT_RX(bytes_to_int(bytes, i)); i += 4;
			SEQ_MISC_TIMING2 = new SEQ_MISC_TIMING2_FORMAT(bytes_to_int(bytes, i)); i += 4;
			SEQ_MISC1 = bytes_to_int(bytes, i); i += 4;
			SEQ_MISC3 = bytes_to_int(bytes, i); i += 4;
			SEQ_MISC8 = bytes_to_int(bytes, i); i += 4;
			ARB_DRAM_TIMING = new ARB_DRAM_TIMING_FORMAT(bytes_to_int(bytes, i)); i += 4;
			ARB_DRAM_TIMING2 = new ARB_DRAM_TIMING2_FORMAT(bytes_to_int(bytes, i)); i += 4;
		}
	}
}