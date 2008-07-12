
public class ByteArray {
	private byte[] bytes;

	public ByteArray(byte[] values) {
		bytes = values;
	}

	public int toInt() {
		return (int) (((bytes[0] & 0xff) << 0) +
				((bytes[1] & 0xff) << 8) +
				((bytes[2] & 0xff) << 16) +
				((bytes[3] & 0xff) << 24));
	}

	public boolean equals(byte[] x) {
		if (bytes.length != x.length)
			return false;
		for (int i = 0; i < bytes.length; i++) {
			if (bytes[i] != x[i])
				return false;
		}
		return true;
	}
}
