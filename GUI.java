import java.awt.*;
import java.awt.event.*;

import javax.swing.*;


public class GUI extends JFrame implements ActionListener, UI {
	private JTextField dvdpath, destinationpath;
	private JProgressBar progress;
	private JButton install, close;
	final String os = System.getProperty("os.name");

	public GUI() {
		super("Edna bricht aus");
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));

		JPanel paths = new JPanel(new GridLayout(2,2));

		JLabel dvdlabel = new JLabel("Pfad zur DVD:");
		paths.add(dvdlabel);

		dvdpath = new JTextField();
		if (os.equals("Mac OS X"))
			dvdpath.setText("/Volumes/EDNABRICHTAUS");
		else
			dvdpath.setText("/mnt/cdrom");
		paths.add(dvdpath);


		JLabel destinationlabel = new JLabel("Installationspfad:");
		paths.add(destinationlabel);

		destinationpath = new JTextField(System.getProperty("user.home")+"/Edna bricht aus");
		paths.add(destinationpath);

		getContentPane().add(paths);


		progress = new JProgressBar();
		progress.setMaximum(27578);
		getContentPane().add(progress);


		JPanel buttonpanel = new JPanel(new FlowLayout());
		install = new JButton("Installieren");
		install.addActionListener(this);
		buttonpanel.add(install);

		close = new JButton("Beenden");
		close.addActionListener(this);
		buttonpanel.add(close);

		getContentPane().add(buttonpanel);

		setSize(500, 150);
		setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == install) {
			Unpack unpack = new Unpack(this);
			unpack.start();
			enableFields(false);
		} else if (e.getSource() == close) {
			System.exit(0);
		}
	}

	private void enableFields(boolean enable) {
		install.setEnabled(enable);
		dvdpath.setEnabled(enable);
		destinationpath.setEnabled(enable);
	}

	public static void main(String[] args) {
		new GUI();
	}

	@Override
	public void increaseProgress() {
		progress.setValue(progress.getValue()+1);
	}

	@Override
	public void showError(String text) {
		JOptionPane.showMessageDialog(new JFrame(), text, "Error", JOptionPane.ERROR_MESSAGE);
		enableFields(true);
	}

	@Override
	public void showSuccess() {
		JOptionPane.showMessageDialog(new JFrame(), "Installation abgeschlossen!", "Fertig", JOptionPane.INFORMATION_MESSAGE);
		System.exit(0);
	}

	@Override
	public String getDVDPath() {
		return dvdpath.getText();
	}

	@Override
	public String getDestinationPath() {
		return destinationpath.getText();
	}

}
