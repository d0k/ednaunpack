import java.io.*;
import java.text.*;
import java.util.Date;

import SevenZip.Compression.LZMA.Decoder;

public class FileList {
	private static FileList instance = null;
	private byte[] data;
	private int offset;

	private FileList() {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			RandomAccessFile in = new RandomAccessFile("files.lzma", "r");

			Decoder decoder = new Decoder();
			decoder.SetDecoderProperties(Unpack.readProps(in));

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

	public static FileList getInstance() {
		return instance == null ? new FileList() : instance;
	}

	public FileLocation nextFile(){
		int start = offset;
		while (data[offset] != '\n') {
			if (offset+2 == data.length)
				return null;
			offset++;
		}
		String line[] = new String(data, start, offset-start).split(";");
		offset++;
		DateFormat df = new SimpleDateFormat("yyyy.MM.dd kk:mm");
		try {
			Date mtime = df.parse(line[1]);
			return new FileLocation(line[0], Integer.parseInt(line[2]), Integer.parseInt(line[3]), Integer.parseInt(line[4]), Integer.parseInt(line[6]), mtime);
		} catch (ParseException e) {
			return null;
		}
	}

	public class FileLocation {
		public String fileName;
		public int firstSlice, lastSlice;
		public int startOffset, originalSize;
		public Date mtime;
		private FileLocation(String fileName, int firstSlice, int lastSlice, int startOffset, int originalSize, Date mtime) {
			this.fileName = fileName;
			this.firstSlice = firstSlice;
			this.lastSlice = lastSlice;
			this.startOffset = startOffset;
			this.originalSize = originalSize;
			this.mtime = mtime;
		}
	}
}