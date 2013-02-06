package pl.poznan.put.cs.bioserver.gui;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

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
}
