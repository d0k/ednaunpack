import java.io.*;


class Slice {
	private final static byte[] signature = {'i', 'd', 's', 'k', 'a', '3', '2', 26};
	private RandomAccessFile file;

	public Slice(String fileName) throws InvalidFileException, FileNotFoundException {
		try {
			file = new RandomAccessFile(fileName, "r");

			if (!readBytes(signature.length).equals(signature))
				throw new InvalidFileException("Wrong signature");

			if (readBytes(4).toInt() != new File(fileName).length()) //TODO do we really need an extra file handle?
				throw new InvalidFileException("Wrong filesize");

		} catch (IOException e) {
			throw new InvalidFileException(e.getMessage());
		}
	}

	private ByteArray readBytes(int len) throws IOException {
		byte[] tmp = new byte[len];
		file.read(tmp);
		ByteArray ret = new ByteArray(tmp);
		return ret;
	}

	public RandomAccessFile getFile() throws IOException {
		return file;
	}
}
