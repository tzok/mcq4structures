package pl.poznan.put.cs.bioserver.gui;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

public class Gui extends JFrame {
    private static final long serialVersionUID = 1L;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @SuppressWarnings("unused")
            @Override
            public void run() {
                new Gui(new PdbManager());
            }
        });
    }

    public Gui(PdbManager manager) {
        super();
        /*
         * Tabbed pane
         */
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add("Sequence alignment",
                new SequenceAlignmentPanel(manager));
        tabbedPane.add("Structure alignment", new StructureAlignmentPanel(
                manager));
        tabbedPane.add("Global comparison", new GlobalComparisonPanel(manager));
        tabbedPane.add("Torsion local comparison",
                new TorsionLocalComparisonPanel(manager));
        // BiojavaJmol jmol = new BiojavaJmol();
        // tabbedPane.add("Jmol", jmol.getFrame());
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
