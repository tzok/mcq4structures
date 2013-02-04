package pl.poznan.put.cs.bioserver.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;

import javax.swing.JEditorPane;
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
    public static PdbManagerDialog managerDialog;

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
                managerDialog = new PdbManagerDialog();
                managerDialog.setVisible(true);

                MainWindow window = new MainWindow();
                window.setVisible(true);
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

        JEditorPane editorPane = new JEditorPane();
        editorPane.setBackground(new Color(0, 0, 0, 0));
        editorPane.setContentType("text/html");
        editorPane.setEditable(false);
        editorPane
                .setText("<h1>MCQ4Structures - an application and framework to "
                        + "compute 3D similarites of RNAs / proteins</h1>"
                        + "<h2>Links:</h2><ul"
                        + "<li><a href=\"http://bioserver.cs.put.poznan.pl\">BioServer @ cs.put.poznan.pl</a></li>"
                        + "<h2>Technologies used:</h2><ul>"
                        + "<li><a href=\"http://biojava.org\">BioJava</a></li>"
                        + "<li><a href=\"http://www.jfree.org/jfreechart\">JFreeChart</a></li>"
                        + "<li><a href=\"http://jmol.sourceforge.net\">Jmol</a></li>"
                        + "<li><a href=\"http://logging.apache.org/log4j\">Log4j</a></li>"
                        + "<li><a href=\"http://code.google.com/p/java-diff-utils\">DiffUtils</a></li>"
                        + "<li><a href=\"http://commons.apache.org/lang\">Apache Commons Lang</a></li>");

        JPanel panelAbout = new JPanel();
        panelAbout.setLayout(new GridLayout(1, 1));
        panelAbout.add(editorPane);

        /*
         * Tabbed pane
         */
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add("Global comparison", new GlobalComparisonPanel());
        tabbedPane.add("Local comparison", new TorsionLocalComparisonPanel());
        tabbedPane.add("Sequence alignment", new SequenceAlignmentPanel());
        tabbedPane.add("3D structure alignment", new StructureAlignmentPanel());
        tabbedPane.add("About the program", panelAbout);
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
