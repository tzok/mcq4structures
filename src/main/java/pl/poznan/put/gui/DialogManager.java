package pl.poznan.put.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import org.biojava.bio.structure.Structure;

import pl.poznan.put.structure.tertiary.StructureManager;

final class DialogManager extends JDialog {
    private final DefaultListModel<File> model = new DefaultListModel<>();

    public DialogManager(Frame parent) {
        super(parent, "MCQ4Structures: structure manager");

        final JList<File> list = new JList<>(model);
        list.setBorder(BorderFactory.createTitledBorder("List of open structures"));

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

        fieldPdbId.setPreferredSize(new Dimension(128, fieldPdbId.getPreferredSize().height));
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
                List<Structure> models = StructureManager.loadStructure(pdbId);
                if (models.size() > 0) {
                    File path = StructureManager.getFile(models.get(0));
                    model.addElement(path);
                } else {
                    JOptionPane.showMessageDialog(DialogManager.this, "Failed to download " + pdbId + " from the Protein Data Bank", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    public Enumeration<File> getElements() {
        return model.elements();
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
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error: " + e.getClass(), JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
