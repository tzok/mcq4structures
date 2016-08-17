package pl.poznan.put.gui.window;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.poznan.put.gui.component.PdbChooser;
import pl.poznan.put.pdb.PdbParsingException;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.structure.tertiary.StructureManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;

public final class DialogManager extends JDialog {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(DialogManager.class);

    private final DefaultListModel<File> model = new DefaultListModel<>();

    public DialogManager(Frame parent) {
        super(parent, "MCQ4Structures: structure manager");

        final JList<File> list = new JList<>(model);
        list.setBorder(
                BorderFactory.createTitledBorder("List of open structures"));

        JButton buttonOpen = new JButton("Open structure(s)");
        JButton buttonRemove = new JButton("Close selected structure(s)");
        JPanel panelButtons = new JPanel();
        panelButtons.add(buttonOpen);
        panelButtons.add(buttonRemove);

        JPanel panelListButtons = new JPanel();
        panelListButtons.setLayout(new BorderLayout());
        panelListButtons.add(new JScrollPane(list), BorderLayout.CENTER);
        panelListButtons.add(panelButtons, BorderLayout.SOUTH);

        final JTextField fieldPdbId = new JTextField();
        JButton buttonFetch = new JButton("Download from PDB");
        JPanel panelFetch = new JPanel();
        panelFetch.add(new JLabel("PDB id:"));
        panelFetch.add(fieldPdbId);
        panelFetch.add(buttonFetch);

        setLayout(new BorderLayout());
        add(panelListButtons, BorderLayout.CENTER);
        add(panelFetch, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(buttonFetch);

        fieldPdbId.setPreferredSize(
                new Dimension(128, fieldPdbId.getPreferredSize().height));
        pack();

        Dimension size = getSize();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = screenSize.width - size.width;
        int y = screenSize.height - size.height;
        setLocation(x / 2, y / 2);
        setAlwaysOnTop(true);

        buttonOpen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                selectAndLoadStructures();
            }
        });

        buttonRemove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<File> selected = list.getSelectedValuesList();
                for (File f : selected) {
                    StructureManager.remove(f);
                    model.removeElement(f);
                }
            }
        });

        buttonFetch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                String pdbId = fieldPdbId.getText();

                try {
                    List<PdbModel> models =
                            StructureManager.loadStructure(pdbId);
                    File path = StructureManager.getFile(models.get(0));
                    model.addElement(path);
                } catch (IOException | PdbParsingException e) {
                    String message =
                            "Failed to download and/or parse PDB file: "
                            + pdbId;
                    DialogManager.LOGGER.error(message, e);
                    JOptionPane.showMessageDialog(DialogManager.this, message,
                                                  "Error",
                                                  JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    public void selectAndLoadStructures() {
        PdbChooser pdbChooser = PdbChooser.getInstance();
        List<File> files = pdbChooser.selectFiles(DialogManager.this);
        loadStructures(files);
    }

    public void loadStructures(List<File> files) {
        for (File file : files) {
            try {
                if (StructureManager.loadStructure(file).size() > 0) {
                    model.addElement(file);
                }
            } catch (IOException | PdbParsingException e) {
                String message =
                        "Failed to load and/or parse PDB file: " + file;
                DialogManager.LOGGER.error(message, e);
                JOptionPane.showMessageDialog(this, message, "Error",
                                              JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public Enumeration<File> getElements() {
        return model.elements();
    }
}
