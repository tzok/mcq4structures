package pl.poznan.put.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * A main window of the application.
 *
 * @author tzok
 */
class Gui extends JFrame {
    /**
     * Run the main graphical application.
     */
    public static void main(final String[] args) {
        final List<File> pdbs = new ArrayList<>();

        for (String argument : args) {
            File file = new File(argument);
            if (file.canRead()) {
                pdbs.add(file);
            }
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                /*
                 * Set L&F
                 */
                for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        try {
                            UIManager.setLookAndFeel(info.getClassName());
                        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
                            // do nothing
                        }
                        break;
                    }
                }

                MainWindow window = new MainWindow(pdbs);
                window.setVisible(true);
            }
        });
    }
}
