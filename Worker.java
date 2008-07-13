import java.io.*;

import SevenZip.Compression.LZMA.Decoder;


public class Worker extends Thread {
	private Slice slice;
	private Decoder decoder = new Decoder();
	private UI ui;

	public Worker(UI ui, Slice slice) {
		this.slice = slice;
		this.ui = ui;
	}

	public void run() {
		try {
			for (Slice.FileData data = slice.readNextFile(); data != null; data = slice.readNextFile()) {
				FileList.FileLocation file = data.file;

				ByteArrayInputStream in = new ByteArrayInputStream(data.data);
				decoder.SetDecoderProperties(data.props);

				String fileName = ui.getDestinationPath()+"/"+file.fileName;
				File f = new File(fileName);
				f.getParentFile().mkdirs();

				boolean needsTransform = file.fileName.contains(".exe") || file.fileName.contains(".dll");
				if (needsTransform) {
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					decoder.Code(in, out, file.originalSize);
					byte[] decompressedData = out.toByteArray();
					Util.transformCallInstructions(decompressedData);
					BufferedOutputStream outfile = new BufferedOutputStream(new FileOutputStream(f));
					outfile.write(decompressedData);
					outfile.close();
				} else {
					BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(f));
					decoder.Code(in, out, file.originalSize);
					out.close();
				}
				ui.increaseProgress();
			}
		} catch (Exception e) {
			ui.showError(e.getLocalizedMessage());
			e.printStackTrace();
		}
	}
}
