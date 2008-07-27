import java.io.*;


class Slice {
	private final static byte[] signature = {'i', 'd', 's', 'k', 'a', '3', '2', 26};
	private final static byte[] zlbsignature = {'z', 'l', 'b', 26};
	private String path;
	private char slicenum = 'a';
	private RandomAccessFile file;
	private final FileList list = new FileList(getClass().getClassLoader().getResourceAsStream("files.lzma"));

	/** creates a new Slice object and reads the first Slice */
	public Slice(String path) throws InvalidFileException, FileNotFoundException {
		this.path = path;
		next();
	}

	private void next() throws InvalidFileException, FileNotFoundException {
		try {
			String fileName = path+"/setup-1"+slicenum+".bin";
			if (!new File(fileName).exists()) // setup- does not exist, trying setup_ (fixes a bug reported by Ignaz Forster)
				fileName = path+"/setup_1"+slicenum+".bin";

			slicenum++;
			if (file != null)
				file.close();
			file = new RandomAccessFile(fileName, "r");

			/* Slice file format:
			 * first 8 bytes contain the signature "idska32"+26
			 * then a 4 byte 32 bit integer follows which contains the length of the Slice file
			 */

			if (!Util.isByteArrayEqual(Util.readBytes(file, signature.length), signature))
				throw new InvalidFileException("Wrong signature");

			if (Util.littleEndianToInt(Util.readBytes(file, 4)) != new File(fileName).length()) //TODO do we really need an extra file handle?
				throw new InvalidFileException("Wrong filesize");

		} catch (IOException e) {
			throw new InvalidFileException(e.getMessage());
		}
	}

	/** reads data for <b>f</b> from Slice.
	 * If the file is continued in the next Slice, the next Slice is opened and the old Slice is closed.<br/>
	 * <b>Warning</b>: The file must not span across more than 2 slices and must begin in the open Slice!
	 */
	private FileData readFile(FileList.FileLocation f) throws InvalidFileException, FileNotFoundException, IOException {
		byte[] tmp = new byte[f.compressedSize];
		byte[] props = new byte[5];

		synchronized (this) {
			file.seek(f.startOffset);
			/* the begin of a new file is marked with the signature "zlb"+26 */
			if (!Util.isByteArrayEqual(Util.readBytes(file, zlbsignature.length), zlbsignature)) {
				throw new InvalidFileException("Wrong zlbsignature");
			}

			file.read(props);

			if (f.firstSlice != f.lastSlice) { // TODO files with more than 2 slices?
				int firstlen = (int)file.length()-f.startOffset-zlbsignature.length-5; // TODO examine these 5 bytes
				int lastlen = f.compressedSize-firstlen;

				byte[] first = new byte[firstlen];
				file.read(first);
				next();
				byte[] last = new byte[lastlen];
				file.read(last);

				System.arraycopy(first, 0, tmp, 0 ,firstlen);
				System.arraycopy(last, 0, tmp, firstlen, lastlen);
			} else {
				file.read(tmp);
			}
		}

		FileData data = new FileData();
		data.data = tmp;
		data.props = props;
		data.file = f;
		return data;
	}

	/** reads data for next file from list. */
	public FileData readNextFile() throws FileNotFoundException, InvalidFileException, IOException {
		FileList.FileLocation loc = list.nextFile();
		if (loc != null)
			return readFile(loc);
		return null;
	}

	public static class FileData {
		/** byte properties for the lzma-decoder */
		public byte[] props;
		/** actual compresed data */
		public byte[] data;
		/** information about the file */
		public FileList.FileLocation file;
	}
}
