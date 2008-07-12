import java.io.*;

import javax.swing.JProgressBar;

import SevenZip.Compression.LZMA.Decoder;


public class Unpack extends Thread {
	private final static byte[] zlbsignature = {'z', 'l', 'b', 26};
	private String input, output;
	private JProgressBar p;

	public Unpack(String inputpath, String outputpath, JProgressBar p) {
		input = inputpath;
		output = outputpath;
		this.p = p;
	}

	@Override
	public void run() {
		try {
			Slice slices[] = new Slice[8];
			for (int i = 0; i < slices.length; i++) {
				char letter = (char) (i+'a');
				slices[i] = new Slice("setup-1"+letter+".bin"); //TODO: fix path, this is for debugging
			}
			FileList fl = FileList.getInstance();
			FileList.FileLocation file = fl.nextFile();
			Decoder decoder = new Decoder();
			while (file != null) {
				RandomAccessFile in = slices[file.firstSlice].getFile();

				in.seek(file.startOffset);
				if (!Unpack.readBytes(in, zlbsignature.length).equals(zlbsignature))
					throw new InvalidFileException("Wrong sigature (2)");

				decoder.SetDecoderProperties(readProps(in));

				String fileName = output+"/"+file.fileName;
				File f = new File(fileName);
				f.getParentFile().mkdir();
				FileOutputStream out = new FileOutputStream(f);
				decoder.Code(in, out, file.originalSize);
				out.close();
				f.setLastModified(file.mtime.getTime());

				System.out.println(file.fileName);
				p.setValue(p.getValue()+1);
				file = fl.nextFile();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static byte[] readProps(DataInput in) throws IOException {
		byte[] props = new byte[5];
		in.readFully(props);
		return props;
	}

	public static ByteArray readBytes(DataInput in, int len) throws IOException {
		byte[] tmp = new byte[len];
		in.readFully(tmp);
		ByteArray ret = new ByteArray(tmp);
		return ret;
	}
}