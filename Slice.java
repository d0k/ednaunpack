import java.io.*;


class Slice {
	private final static byte[] signature = {'i', 'd', 's', 'k', 'a', '3', '2', 26};
	private final static byte[] zlbsignature = {'z', 'l', 'b', 26};
	private String path;
	private char slicenum = 'a';
	private RandomAccessFile file;

	public Slice(String path) throws InvalidFileException, FileNotFoundException {
		this.path = path;
		next();
	}

	private void next() throws InvalidFileException, FileNotFoundException {
		try {
			String fileName = path+"/setup-1"+slicenum+".bin";
			slicenum++;
			if (file != null)
				file.close();
			file = new RandomAccessFile(fileName, "r");

			if (!Util.isByteArrayEqual(Util.readBytes(file, signature.length), signature))
				throw new InvalidFileException("Wrong signature");

			if (Util.littleEndianToInt(Util.readBytes(file, 4)) != new File(fileName).length()) //TODO do we really need an extra file handle?
				throw new InvalidFileException("Wrong filesize");

		} catch (IOException e) {
			throw new InvalidFileException(e.getMessage());
		}
	}

	/** reads data for <b>f</b> from Slice. */
	public synchronized FileData readFile(FileList.FileLocation f) throws InvalidFileException, FileNotFoundException, IOException {
		byte[] tmp = new byte[f.compressedSize];
		byte[] props = new byte[5];

		file.seek(f.startOffset);
		if (!Util.isByteArrayEqual(Util.readBytes(file, zlbsignature.length), zlbsignature)) {
			System.out.println();
			throw new InvalidFileException("Wrong zlbsignature");
		}

		file.read(props);

		if (f.firstSlice != f.lastSlice) {
			int firstlen = (int)file.length()-f.startOffset-zlbsignature.length-5;
			int lastlen = f.compressedSize-firstlen;

			byte[] first = new byte[firstlen];
			file.read(first);
			next();
			byte[] last = new byte[lastlen];
			file.read(last);

			for (int i = 0; i < tmp.length; i++) {
				if (i < firstlen)
					tmp[i] = first[i];
				else
					tmp[i] = last[i-firstlen];
			}
		} else {
			file.read(tmp);
		}

		FileData data = new FileData();
		data.data = tmp;
		data.props = props;
		return data;
	}

	public static class FileData {
		public byte[] props;
		public byte[] data;
	}
}
