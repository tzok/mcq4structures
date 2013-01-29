package pl.poznan.put.cs.bioserver.gui;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * A main window of the application.
 * 
 * @author tzok
 */
public class Gui extends JFrame {
	private static final long serialVersionUID = 1L;

	/**
	 * Run the main graphical application.
	 * 
	 * @param args
	 *            Unused.
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new Gui();
			}
		});
	}

	public Gui() {
		super();
		/*
		 * Set L&F
		 */
		for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
			if ("Nimbus".equals(info.getName())) {
				try {
					UIManager.setLookAndFeel(info.getClassName());
				} catch (ClassNotFoundException | InstantiationException
						| IllegalAccessException
						| UnsupportedLookAndFeelException e) {
					// do nothing
				}
				break;
			}
		}
		/*
		 * Tabbed pane
		 */
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.add("Global comparison", new GlobalComparisonPanel());
		tabbedPane.add("Local comparison", new TorsionLocalComparisonPanel());
		tabbedPane.add("Sequence alignment", new SequenceAlignmentPanel());
		tabbedPane.add("3D structure alignment", new StructureAlignmentPanel());
		tabbedPane.add("About the program", new JPanel());
		setContentPane(tabbedPane);
		/*
		 * Show window
		 */
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("MCQ4Structures: computing similarity of 3D RNA / protein structures");
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension size = toolkit.getScreenSize();
		setSize(size.width * 3 / 4, size.height * 3 / 4);
		setLocation(size.width / 8, size.height / 8);
		setVisible(true);
	}
}
