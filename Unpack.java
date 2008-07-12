import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import javax.swing.*;

import SevenZip.Compression.LZMA.Decoder;


public class Unpack extends Thread {
	private final static byte[] zlbsignature = {'z', 'l', 'b', 26};
	private String input, output;
	private GUI gui;

	public Unpack(GUI g) {
		input = g.dvdpath.getText();
		output = g.destinationpath.getText();
		gui = g;
	}

	@Override
	public void run() {
		try {
			MappedByteBuffer slices[] = new MappedByteBuffer[8];
			for (int i = 0; i < slices.length; i++) {
				char letter = (char) (i+'a');
				Slice slice = new Slice(input+"/install/instbin/software/setup-1"+letter+".bin");
				slices[i] = slice.getFile().getChannel().map(FileChannel.MapMode.READ_ONLY, 0, slice.getFile().length());
				slice.getFile().close();
			}
			FileList fl = FileList.getInstance();
			FileList.FileLocation file = fl.nextFile();
			Decoder decoder = new Decoder();
			while (file != null) {
				MappedByteBuffer in = slices[file.firstSlice];
				in.position(file.startOffset);

				if (!Unpack.readBytes(in, zlbsignature.length).equals(zlbsignature))
					throw new InvalidFileException("Wrong sigature (2)");

				decoder.SetDecoderProperties(readProps(in));

				String fileName = output+"/"+file.fileName;
				File f = new File(fileName);
				f.getParentFile().mkdirs();

				if (fileName.contains(".exe") || fileName.contains(".dll")) { //FIXME: there is an property for this in the installer
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					decoder.Code(in, out, file.originalSize);
					byte[] data = out.toByteArray();
					transformCallInstructions(data);
					FileOutputStream write = new FileOutputStream(f);
					write.write(data);
					write.close();
				} else {
					FileOutputStream out = new FileOutputStream(f);

					// this part is just quick'n'dirty. I need to redo it sometime ;)
					if (file.firstSlice != file.lastSlice) {
						int firstlen = (int)in.capacity()-file.startOffset-zlbsignature.length-5;
						int len = file.compressedSize-firstlen;
						byte[] firstdata = new byte[firstlen];
						in.get(firstdata);

						slices[file.firstSlice] = null;
						System.gc();

						in = slices[file.lastSlice];
						byte[] lastdata = new byte[len];
						in.position(12);
						in.get(lastdata);

						File tmpfile = new File(output+"/cross.tmp");
						FileOutputStream tmp = new FileOutputStream(tmpfile); //TODO: quick'n'dirty
						tmp.write(firstdata);
						tmp.write(lastdata);
						tmp.close();

						RandomAccessFile rtmp = new RandomAccessFile(tmpfile, "r");
						MappedByteBuffer btmp = rtmp.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, rtmp.length());
						rtmp.close();
						decoder.Code(btmp, out, file.originalSize);
						tmpfile.delete();
					} else {
						decoder.Code(in, out, file.originalSize);
					}
					out.close();
				}
				f.setLastModified(file.mtime.getTime());

				//System.out.println(file.fileName);
				gui.progress.setValue(gui.progress.getValue()+1);
				file = fl.nextFile();
			}

			// create some empty directories for savegames
			for (int i = 0; i < 10; i++) {
				String name = output+"/EbaSaveGame";
				if (i != 0)
					name += i;
				new File(name).mkdir();
			}

			JOptionPane.showMessageDialog(new JFrame(), "Installation abgeschlossen!", "Fertig", JOptionPane.INFORMATION_MESSAGE);
			System.exit(0);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(new JFrame(), e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			gui.install.setEnabled(true);
		}
	}

	public static byte[] readProps(ByteBuffer in) throws IOException {
		byte[] props = new byte[5];
		in.get(props);
		return props;
	}

	public static ByteArray readBytes(ByteBuffer in, int len) throws IOException {
		byte[] tmp = new byte[len];
		in.get(tmp);
		ByteArray ret = new ByteArray(tmp);
		return ret;
	}

	/** Transforms addresses (x86) in absolute CALL or JMP instructions to relative ones */
	private static void transformCallInstructions(byte[] p) {
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
