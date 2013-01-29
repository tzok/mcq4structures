package pl.poznan.put.cs.bioserver.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Structure;

import pl.poznan.put.cs.bioserver.gui.helper.PdbChangeListener;
import pl.poznan.put.cs.bioserver.gui.helper.PdbFileChooser;
import pl.poznan.put.cs.bioserver.helper.PdbManager;

class PdbPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private DefaultListModel<File> listModel;
    private JList<File> list;
    private DefaultComboBoxModel<String> comboBoxModelFirst,
            comboBoxModelSecond;
    private JComboBox<String> comboBoxFirst, comboBoxSecond;

    public PdbPanel(final PdbChangeListener listener) {
        super();

        listModel = new DefaultListModel<>();
        list = new JList<>(listModel);
        comboBoxModelFirst = new DefaultComboBoxModel<>();
        comboBoxModelSecond = new DefaultComboBoxModel<>();
        comboBoxFirst = new JComboBox<>(comboBoxModelFirst);
        comboBoxSecond = new JComboBox<>(comboBoxModelSecond);

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 2;
        add(list, c);
        c.gridx++;
        c.gridheight--;
        add(comboBoxFirst, c);
        c.gridy++;
        add(comboBoxSecond, c);

        list.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    int index = list.getSelectedIndex();
                    if (index == 0) {
                        comboBoxModelFirst.removeAllElements();
                    } else {
                        comboBoxModelSecond.removeAllElements();
                    }
                    listModel.remove(index);

                    refreshComboBoxes();
                    listener.pdbListChanged();
                }
            }
        });
    }

    public JComboBox<String> getComboBoxFirst() {
        return comboBoxFirst;
    }

    public JComboBox<String> getComboBoxSecond() {
        return comboBoxSecond;
    }

    public DefaultListModel<File> getListModel() {
        return listModel;
    }

    void refreshComboBoxes() {
        comboBoxModelFirst.removeAllElements();
        comboBoxModelSecond.removeAllElements();

        int size = listModel.getSize();
        for (int i = 0; i < size; ++i) {
            File file = listModel.getElementAt(i);
            Structure structure = PdbManager.getStructure(file);
            for (Chain c : structure.getChains()) {
                if (i == 0) {
                    comboBoxModelFirst.addElement(c.getChainID());
                } else {
                    comboBoxModelSecond.addElement(c.getChainID());
                }
            }
        }
    }

    public void loadStructuresWithOpenDialog() {
        if (listModel.size() >= 2) {
            JOptionPane.showMessageDialog(null,
                    "You already have two structures loaded", "Information",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        File[] files = PdbFileChooser.getSelectedFiles(this);
        for (File f : files) {
            if (listModel.size() >= 2) {
                JOptionPane.showMessageDialog(null,
                        "You cannot load more than two structures", "Warning",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            PdbManager.loadStructure(f);
            listModel.addElement(f);
            refreshComboBoxes();
        }
    }
}
