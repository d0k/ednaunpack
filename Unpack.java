import java.io.*;


public class Unpack extends Thread {
	private UI gui;

	public Unpack(UI g) {
		gui = g;
	}

	@Override
	public void run() {
		try {
			File f = new File(gui.getDVDPath());
			Slice slice;
			if (f.isFile()) {
				FileList list = new FileList(getClass().getClassLoader().getResourceAsStream("demo.lzma"));
				slice = new Slice(f.getAbsolutePath(), list, true);
			} else {
				FileList list = new FileList(getClass().getClassLoader().getResourceAsStream("files.lzma"));
				slice = new Slice(gui.getDVDPath()+"/install/instbin/software", list);
			}
			Worker[] w = new Worker[Runtime.getRuntime().availableProcessors()];

			for (int i = 0; i < w.length; i++) {
				w[i] = new Worker(gui, slice);
				w[i].start();
			}

			for (Worker worker: w)
				worker.join();

			// create some empty directories for savegames
			for (int i = 0; i < 10; i++) {
				String name = gui.getDestinationPath()+"/EbaSaveGame";
				if (i != 0)
					name += i;
				new File(name).mkdir();
			}

			gui.showSuccess();
		} catch (Exception e) {
			gui.showError(e.getMessage());
			e.printStackTrace();
		}
	}

}
