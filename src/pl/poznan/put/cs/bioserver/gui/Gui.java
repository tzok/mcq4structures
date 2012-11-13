package pl.poznan.put.cs.bioserver.gui;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

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
            @SuppressWarnings("unused")
            @Override
            public void run() {
                new Gui();
            }
        });
    }

    public Gui() {
        super();
        /*
         * Tabbed pane
         */
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add("Sequence alignment", new SequenceAlignmentPanel());
        tabbedPane.add("Structure alignment", new StructureAlignmentPanel());
        tabbedPane.add("Global comparison", new GlobalComparisonPanel());
        tabbedPane.add("Torsion local comparison",
                new TorsionLocalComparisonPanel());
        setContentPane(tabbedPane);
        /*
         * Show window
         */
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("mcq4structures");
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension size = toolkit.getScreenSize();
        setSize(size.width * 3 / 4, size.height * 3 / 4);
        setLocation(size.width / 8, size.height / 8);
        setVisible(true);
    }
}
