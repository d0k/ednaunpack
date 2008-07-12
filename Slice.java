import java.io.*;


class Slice {
	private final static byte[] signature = {'i', 'd', 's', 'k', 'a', '3', '2', 26};
	private RandomAccessFile file;

	public Slice(String fileName) throws InvalidFileException, FileNotFoundException {
		try {
			file = new RandomAccessFile(fileName, "r");

			if (!Unpack.readBytes(file, signature.length).equals(signature))
				throw new InvalidFileException("Wrong signature");

			if (Unpack.readBytes(file, 4).toInt() != new File(fileName).length()) //TODO do we really need an extra file handle?
				throw new InvalidFileException("Wrong filesize");

		} catch (IOException e) {
			throw new InvalidFileException(e.getMessage());
		}
	}



	public RandomAccessFile getFile() throws IOException {
		return file;
	}
}
