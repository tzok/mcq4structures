package pl.poznan.put.gui.window;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.poznan.put.gui.component.PdbChooser;
import pl.poznan.put.pdb.PdbParsingException;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.structure.StructureManager;

public final class DialogManager extends JDialog {
  private static final long serialVersionUID = 8406487603428842396L;
  private static final Logger LOGGER = LoggerFactory.getLogger(DialogManager.class);

  private final DefaultListModel<File> model = new DefaultListModel<>();

  public DialogManager(final Frame parent) {
    super(parent, "MCQ4Structures: structure manager");

    final JList<File> list = new JList<>(model);
    list.setBorder(BorderFactory.createTitledBorder("List of open structures"));

    final JButton buttonOpen = new JButton("Open structure(s)");
    final JButton buttonRemove = new JButton("Close selected structure(s)");
    final JPanel panelButtons = new JPanel();
    panelButtons.add(buttonOpen);
    panelButtons.add(buttonRemove);

    final JPanel panelListButtons = new JPanel();
    panelListButtons.setLayout(new BorderLayout());
    panelListButtons.add(new JScrollPane(list), BorderLayout.CENTER);
    panelListButtons.add(panelButtons, BorderLayout.PAGE_END);

    final JTextField fieldPdbId = new JTextField();
    final JButton buttonFetch = new JButton("Download from PDB");
    final JPanel panelFetch = new JPanel();
    panelFetch.add(new JLabel("PDB id:"));
    panelFetch.add(fieldPdbId);
    panelFetch.add(buttonFetch);

    setLayout(new BorderLayout());
    add(panelListButtons, BorderLayout.CENTER);
    add(panelFetch, BorderLayout.PAGE_END);
    getRootPane().setDefaultButton(buttonFetch);

    fieldPdbId.setPreferredSize(new Dimension(128, fieldPdbId.getPreferredSize().height));
    pack();

    final Dimension size = getSize();
    final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    final int x = screenSize.width - size.width;
    final int y = screenSize.height - size.height;
    setLocation(x / 2, y / 2);
    setAlwaysOnTop(true);

    buttonOpen.addActionListener(arg0 -> selectAndLoadStructures());

    buttonRemove.addActionListener(
        e -> {
          final List<File> selected = list.getSelectedValuesList();
          for (final File file : selected) {
            StructureManager.remove(file);
            model.removeElement(file);
          }
        });

    buttonFetch.addActionListener(
        arg0 -> {
          final String pdbId = fieldPdbId.getText();

          try {
            final List<? extends PdbModel> models = StructureManager.loadStructure(pdbId);
            final File path = StructureManager.getFile(models.get(0));
            model.addElement(path);
          } catch (final IOException | PdbParsingException e) {
            final String message =
                String.format("Failed to download and/or parse PDB file: %s", pdbId);
            DialogManager.LOGGER.error(message, e);
            JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
          }
        });
  }

  public void selectAndLoadStructures() {
    final PdbChooser pdbChooser = PdbChooser.getInstance();
    final List<File> files = pdbChooser.selectFiles(this);
    loadStructures(files);
  }

  public void loadStructures(final Iterable<? extends File> files) {
    for (final File file : files) {
      try {
        if (!StructureManager.loadStructure(file).isEmpty()) {
          model.addElement(file);
        }
      } catch (final IOException | PdbParsingException e) {
        final String message = String.format("Failed to load and/or parse PDB file: %s", file);
        DialogManager.LOGGER.error(message, e);
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  public Enumeration<File> getElements() {
    return model.elements();
  }
}
