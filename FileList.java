import java.io.*;
import java.text.*;
import java.util.Date;

import SevenZip.Compression.LZMA.Decoder;

public class FileList {
	private byte[] data;
	private int offset;

	/** Creates a new FileList and reads a file list from an lzma compressed stream (lzma file format by lzma-utils)
	 * @param in input stream the list is read from
	 */
	public FileList(InputStream in) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();

			Decoder decoder = new Decoder();
			byte[] props = new byte[5];
			in.read(props);
			decoder.SetDecoderProperties(props);

			//from LzmaAlone.java
			int len = 0;
			for (int i = 0; i < 8; i++)
			{
				int v = in.read();
				len |= ((long)v) << (8 * i);
			}
			decoder.Code(in, out, len);
			data = out.toByteArray();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** Returns the next file information from the read list.
	 * @return a FileLoction with the read information or null if an error occurs (or the list is exhausted)
	 */
	public FileLocation nextFile(){
		String read;
		synchronized (this) {
			int start = offset;
			while (data[offset] != '\n') {
				if (offset+1 == data.length)
					return null;
				offset++;
			}

			read = new String(data, start, offset-start);
			offset++;
		}
		String[] line = read.split(";");
		if (line.length != 8)
			return null;
		final DateFormat df = new SimpleDateFormat("yyyy.MM.dd kk:mm");
		try {
			FileLocation fl = new FileLocation();
			fl.fileName = line[0];
			fl.firstSlice = Integer.parseInt(line[2]);
			fl.lastSlice = Integer.parseInt(line[3]);
			fl.startOffset = Integer.parseInt(line[4]);
			fl.originalSize = Integer.parseInt(line[6]);
			fl.compressedSize = Integer.parseInt(line[7]);
			fl.mtime = df.parse(line[1]);
			return fl;
		} catch (ParseException e) {
			return null;
		}
	}

	public static class FileLocation {
		/** path and name of the file. */
		public String fileName;
		/** first slice which contains data of this file */
		public int firstSlice;
		/** last slice which contains data of this file */
		public int lastSlice;
		/** the data starts in the first Slice at this offset */
		public int startOffset;
		/** the decompressed size of the file */
		public int originalSize;
		/** the compressed size of the file */
		public int compressedSize;
		/** the modification time of the file */
		public Date mtime;
	}
}
