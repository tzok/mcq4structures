package pl.poznan.put.gui.component;

import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public final class PdbChooser extends JFileChooser {
  private static final PdbChooser INSTANCE = new PdbChooser();

  private PdbChooser() {
    super();
    setFileFilter(
        new FileNameExtensionFilter(
            "Supported formats (PDB, mmCIF)", "pdb", "pdb1", "ent", "brk", "cif", "gz"));
    addChoosableFileFilter(
        new FileNameExtensionFilter("PDB file format", "pdb", "pdb1", "ent", "brk", "gz"));
    addChoosableFileFilter(new FileNameExtensionFilter("mmCIF file format", "cif", "gz"));
    setMultiSelectionEnabled(true);
  }

  public static PdbChooser getInstance() {
    return PdbChooser.INSTANCE;
  }

  public List<File> selectFiles(final Component parent) {
    final int state = showOpenDialog(parent);
    if (state == JFileChooser.APPROVE_OPTION) {
      return Arrays.asList(getSelectedFiles());
    }
    return Collections.emptyList();
  }
}
