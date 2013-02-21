package pl.poznan.put.cs.bioserver.gui.windows;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
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

import pl.poznan.put.cs.bioserver.gui.PdbChooser;
import pl.poznan.put.cs.bioserver.helper.StructureManager;

public class DialogPdbs extends JDialog {
    private static final long serialVersionUID = 1L;
    private static final DefaultListModel<File> MODEL = new DefaultListModel<>();
    private static DialogPdbs INSTANCE;

    public static Enumeration<File> getElements() {
        return DialogPdbs.MODEL.elements();
    }

    public static DialogPdbs getInstance(Frame owner) {
        if (DialogPdbs.INSTANCE == null) {
            DialogPdbs.INSTANCE = new DialogPdbs(owner);
        }
        return DialogPdbs.INSTANCE;
    }

    public static void loadStructure(File file) {
        if (StructureManager.loadStructure(file) != null) {
            DialogPdbs.MODEL.addElement(file);
        }
    }

    private DialogPdbs(Frame parent) {
        super(parent);

        final JList<File> list = new JList<>(DialogPdbs.MODEL);
        list.setBorder(BorderFactory
                .createTitledBorder("List of open structures"));

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

        fieldPdbId.setPreferredSize(new Dimension(128, fieldPdbId
                .getPreferredSize().height));

        int width = 480;
        int height = 480;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = screenSize.width - width;
        int y = screenSize.height - height;
        setSize(width, height);
        setLocation(x / 2, y / 2);

        setAlwaysOnTop(true);
        setTitle("MCQ4Structures: structure manager");

        buttonOpen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                File[] files = PdbChooser.getSelectedFiles(DialogPdbs.this);
                for (File f : files) {
                    if (StructureManager.loadStructure(f) != null) {
                        DialogPdbs.MODEL.addElement(f);
                    }
                }
            }
        });

        buttonRemove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<File> selected = list.getSelectedValuesList();
                for (File f : selected) {
                    StructureManager.remove(f);
                    DialogPdbs.MODEL.removeElement(f);
                }
            }
        });

        buttonFetch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                String pdbId = fieldPdbId.getText();
                File path = StructureManager.loadStructure(pdbId);
                if (path != null) {
                    DialogPdbs.MODEL.addElement(path);
                } else {
                    JOptionPane.showMessageDialog(DialogPdbs.this,
                            "Failed to download " + pdbId
                                    + " from the Protein Data Bank", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
}
