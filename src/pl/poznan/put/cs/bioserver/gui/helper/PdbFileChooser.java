package pl.poznan.put.cs.bioserver.gui.helper;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public class PdbFileChooser {
    private static JFileChooser chooser;

    static {
        PdbFileChooser.chooser = new JFileChooser();
        PdbFileChooser.chooser
                .addChoosableFileFilter(new FileNameExtensionFilter(
                        "PDB file format", "pdb", "pdb1", "ent", "brk", "gz"));
        PdbFileChooser.chooser
                .addChoosableFileFilter(new FileNameExtensionFilter(
                        "mmCIF file format", "cif", "gz"));
        PdbFileChooser.chooser.setMultiSelectionEnabled(true);
    }

    private PdbFileChooser() {
    }

    public static File[] getSelectedFiles(Component parent) {
        int state = PdbFileChooser.chooser.showOpenDialog(parent);
        if (state == JFileChooser.APPROVE_OPTION) {
            return PdbFileChooser.chooser.getSelectedFiles();
        }
        return new File[0];
    }
}
