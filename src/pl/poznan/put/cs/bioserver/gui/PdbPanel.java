package pl.poznan.put.cs.bioserver.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Collections;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;

import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Structure;

import pl.poznan.put.cs.bioserver.helper.PdbManager;

class PdbPanel extends JPanel {
    private final class DeletePdb implements KeyListener {
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
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            // do nothing
        }

        @Override
        public void keyTyped(KeyEvent e) {
            // do nothing
        }
    }

    private static final long serialVersionUID = 1L;
    private DefaultListModel<String> listModel;
    private JList<String> list;
    private DefaultComboBoxModel<String> comboBoxModelFirst,
            comboBoxModelSecond;
    private JComboBox<String> comboBoxFirst, comboBoxSecond;

    public PdbPanel() {
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

        list.addKeyListener(new DeletePdb());
    }

    public JComboBox<String> getComboBoxFirst() {
        return comboBoxFirst;
    }

    public JComboBox<String> getComboBoxSecond() {
        return comboBoxSecond;
    }

    public DefaultListModel<String> getListModel() {
        return listModel;
    }

    void refreshComboBoxes() {
        comboBoxModelFirst.removeAllElements();
        comboBoxModelSecond.removeAllElements();

        Structure[] structures = PdbManager.getStructures(Collections
                .list(listModel.elements()));
        for (int i = 0; i < listModel.getSize(); ++i) {
            for (Chain c : structures[i].getChains()) {
                if (i == 0) {
                    comboBoxModelFirst.addElement(c.getChainID());
                } else {
                    comboBoxModelSecond.addElement(c.getChainID());
                }
            }
        }

    }
}
