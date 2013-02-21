package pl.poznan.put.cs.bioserver.gui;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public final class PdbChooser {
    private static JFileChooser chooser;

    static {
        PdbChooser.chooser = new JFileChooser();
        PdbChooser.chooser.setFileFilter(new FileNameExtensionFilter(
                "Supported formats (PDB, mmCIF)", new String[] { "pdb", "pdb1",
                        "ent", "brk", "cif", "gz" }));
        PdbChooser.chooser.addChoosableFileFilter(new FileNameExtensionFilter(
                "PDB file format", "pdb", "pdb1", "ent", "brk", "gz"));
        PdbChooser.chooser.addChoosableFileFilter(new FileNameExtensionFilter(
                "mmCIF file format", "cif", "gz"));
        PdbChooser.chooser.setMultiSelectionEnabled(true);
    }

    public static File[] getSelectedFiles(Component parent) {
        int state = PdbChooser.chooser.showOpenDialog(parent);
        if (state == JFileChooser.APPROVE_OPTION) {
            return PdbChooser.chooser.getSelectedFiles();
        }
        return new File[0];
    }

    private PdbChooser() {
    }
}
