import java.io.*;


public final class Util {
	private Util() {} // don't allow instances

	/** converts 4 bytes of little endian data into a 32 bit integer. */
	public static int littleEndianToInt(byte[] bytes) {
		return (int) (((bytes[0] & 0xff) << 0) +
				((bytes[1] & 0xff) << 8) +
				((bytes[2] & 0xff) << 16) +
				((bytes[3] & 0xff) << 24));
	}

	/** returns true if a equals b. */
	public static boolean isByteArrayEqual(byte[] a, byte[] b) {
		if (a.length != b.length)
			return false;
		for (int i = 0; i < a.length; i++) {
			if (a[i] != b[i])
				return false;
		}
		return true;
	}

	/** reads len bytes from file. */
	public static byte[] readBytes(RandomAccessFile file, int len) throws IOException {
		byte[] tmp = new byte[len];
		file.read(tmp);
		return tmp;
	}

	/** Transforms addresses (x86) in absolute CALL or JMP instructions to relative ones */
	public static void transformCallInstructions(byte[] p) {
		int addr;
		int i = 0;
		while (i < p.length-4) {
			if ((p[i] & 0xFF) == (0xE8) || (p[i] & 0xFF) == 0xE9) {
				i++;
				if ((p[i+3] & 0xFF) == 0x00 || (p[i+3] & 0xFF) == 0xFF) {
					addr = i + 4;
					addr = -addr;
					for (int x = 0; x <= 2; x++) {
						addr += p[i+x] & 0xFF;
						p[i+x] = (byte) addr;
						addr >>= 8;
					}
				}
				i += 4;
			} else {
				i++;
			}
		}
	}

}
