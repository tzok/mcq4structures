package pl.poznan.put.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Structure;

import pl.poznan.put.common.MoleculeType;
import pl.poznan.put.utility.StructureManager;

final class DialogChainsMultiple extends JDialog {
    private static class FilteredListModel extends AbstractListModel<Chain> {
        private static final long serialVersionUID = 1L;

        boolean isProtein = true;
        boolean isRNA = true;
        List<Chain> listProteins = new ArrayList<>();
        List<Chain> listRNAs = new ArrayList<>();

        public FilteredListModel() {
        }

        public void addElement(Chain f) {
            if (MoleculeType.detect(f) == MoleculeType.RNA) {
                listRNAs.add(f);
            } else {
                listProteins.add(f);
            }
        }

        @Override
        public Chain getElementAt(int index) {
            if (isRNA) {
                if (index < listRNAs.size()) {
                    return listRNAs.get(index);
                }
                return listProteins.get(index - listRNAs.size());
            }
            return listProteins.get(index);
        }

        public ArrayList<Chain> getElements() {
            ArrayList<Chain> list = new ArrayList<>();
            list.addAll(listRNAs);
            list.addAll(listProteins);
            return list;
        }

        public List<Chain> getSelectedElements() {
            List<Chain> list = new ArrayList<>();
            if (isRNA) {
                list.addAll(listRNAs);
            }
            if (isProtein) {
                list.addAll(listProteins);
            }
            return list;
        }

        @Override
        public int getSize() {
            return (isRNA ? listRNAs.size() : 0)
                    + (isProtein ? listProteins.size() : 0);
        }

        public void removeElement(Chain f) {
            if (listRNAs.contains(f)) {
                listRNAs.remove(f);
            } else {
                listProteins.remove(f);
            }
        }
    }

    public static final int CANCEL = 0;
    public static final int OK = 1;
    private static DialogChainsMultiple instance;

    private static final long serialVersionUID = 1L;

    public static DialogChainsMultiple getInstance(Frame owner) {
        DialogChainsMultiple inst = DialogChainsMultiple.instance;
        if (inst == null) {
            inst = new DialogChainsMultiple(owner);
        }
        DialogChainsMultiple.instance = inst;
        return inst;
    }

    int chosenOption;
    FilteredListModel modelAll = new FilteredListModel();
    FilteredListModel modelSelected = new FilteredListModel();
    JList<Chain> listAll = new JList<>(modelAll);
    JList<Chain> listSelected = new JList<>(modelSelected);
    List<Chain> selectedChains = new ArrayList<>();

    private DialogChainsMultiple(Frame owner) {
        super(owner, true);

        listAll = new JList<>(modelAll);
        listAll.setBorder(BorderFactory.createTitledBorder("Available chains"));
        listSelected = new JList<>(modelSelected);
        listSelected.setBorder(BorderFactory.createTitledBorder("Selected chains"));

        final ListCellRenderer<? super Chain> renderer = listAll.getCellRenderer();
        ListCellRenderer<Chain> pdbCellRenderer = new ListCellRenderer<Chain>() {
            @Override
            public Component getListCellRendererComponent(
                    JList<? extends Chain> list, Chain value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) renderer.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                if (value != null) {
                    boolean isRNA = MoleculeType.detect(value) == MoleculeType.RNA;
                    int size = value.getAtomLength();

                    Structure parent = value.getParent();
                    assert parent != null;
                    String text = String.format("%s.%s (%s, %d %s)",
                            StructureManager.getName(parent),
                            value.getChainID(), isRNA ? "RNA" : "protein",
                            size, isRNA ? "nt" : "aa");
                    label.setText(text);
                    label.setBackground(isRNA ? Color.CYAN : Color.YELLOW);
                }
                assert label != null;
                return label;
            }
        };
        listAll.setCellRenderer(pdbCellRenderer);
        listSelected.setCellRenderer(pdbCellRenderer);

        final JCheckBox checkRNA = new JCheckBox("RNAs", true);
        final JCheckBox checkProtein = new JCheckBox("proteins", true);
        final JButton buttonSelect = new JButton("Select ->");
        buttonSelect.setEnabled(false);
        final JButton buttonSelectAll = new JButton("Select all ->");
        final JButton buttonDeselect = new JButton("<- Deselect");
        buttonDeselect.setEnabled(false);
        JButton buttonDeselectAll = new JButton("<- Deselect all");

        JPanel panelButtons = new JPanel();
        panelButtons.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        panelButtons.add(new JLabel("Limit to:"), constraints);
        constraints.gridy++;
        panelButtons.add(checkRNA, constraints);
        constraints.gridy++;
        panelButtons.add(checkProtein, constraints);
        constraints.gridy++;
        panelButtons.add(buttonSelect, constraints);
        constraints.gridy++;
        panelButtons.add(buttonSelectAll, constraints);
        constraints.gridy++;
        panelButtons.add(buttonDeselect, constraints);
        constraints.gridy++;
        panelButtons.add(buttonDeselectAll, constraints);

        JPanel panelMain = new JPanel();
        panelMain.setLayout(new GridBagLayout());
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 0.5;
        constraints.weighty = 0.5;
        constraints.fill = GridBagConstraints.BOTH;
        panelMain.add(new JScrollPane(listAll), constraints);
        constraints.gridx++;
        constraints.weightx = 0;
        constraints.fill = GridBagConstraints.VERTICAL;
        panelMain.add(panelButtons, constraints);
        constraints.gridx++;
        constraints.weightx = 0.5;
        constraints.fill = GridBagConstraints.BOTH;
        panelMain.add(new JScrollPane(listSelected), constraints);

        JButton buttonOk = new JButton("OK");
        JButton buttonCancel = new JButton("Cancel");
        JPanel panelOkCancel = new JPanel();
        panelOkCancel.add(buttonOk);
        panelOkCancel.add(buttonCancel);

        setLayout(new BorderLayout());
        add(panelMain, BorderLayout.CENTER);
        add(panelOkCancel, BorderLayout.SOUTH);

        int width = 640;
        int height = 480;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = screenSize.width - width;
        int y = screenSize.height - height;
        setSize(width, height);
        setLocation(x / 2, y / 2);

        setTitle("MCQ4Structures: multiple chain selection");

        ListSelectionListener listSelectionListener = new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent arg0) {
                if (arg0 != null) {
                    ListSelectionModel source = (ListSelectionModel) arg0.getSource();
                    if (source.equals(listAll.getSelectionModel())) {
                        buttonSelect.setEnabled(!listAll.isSelectionEmpty());
                    } else { // source.equals(listSelected)
                        buttonDeselect.setEnabled(!listSelected.isSelectionEmpty());
                    }
                }
            }
        };
        listAll.getSelectionModel().addListSelectionListener(
                listSelectionListener);
        listSelected.getSelectionModel().addListSelectionListener(
                listSelectionListener);

        ActionListener actionListenerSelectDeselect = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                List<Chain> values;
                boolean isSelect;

                if (arg0 != null) {
                    Object source = arg0.getSource();
                    if (source.equals(buttonSelect)) {
                        values = listAll.getSelectedValuesList();
                        isSelect = true;
                    } else if (source.equals(buttonSelectAll)) {
                        values = modelAll.getElements();
                        isSelect = true;
                    } else if (source.equals(buttonDeselect)) {
                        values = listSelected.getSelectedValuesList();
                        isSelect = false;
                    } else { // source.equals(buttonDeselectAll)
                        values = modelSelected.getElements();
                        isSelect = false;
                    }

                    for (Chain f : values) {
                        assert f != null;
                        if (isSelect) {
                            modelAll.removeElement(f);
                            modelSelected.addElement(f);
                        } else {
                            modelAll.addElement(f);
                            modelSelected.removeElement(f);
                        }
                    }

                    listAll.clearSelection();
                    listSelected.clearSelection();

                    listAll.updateUI();
                    listSelected.updateUI();
                }
            }
        };
        buttonSelect.addActionListener(actionListenerSelectDeselect);
        buttonSelectAll.addActionListener(actionListenerSelectDeselect);
        buttonDeselect.addActionListener(actionListenerSelectDeselect);
        buttonDeselectAll.addActionListener(actionListenerSelectDeselect);

        buttonOk.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedChains = modelSelected.getSelectedElements();
                chosenOption = DialogChainsMultiple.OK;
                dispose();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                chosenOption = DialogChainsMultiple.CANCEL;
                dispose();
            }
        });

        ActionListener checkBoxListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean isRNA = checkRNA.isSelected();
                boolean isProtein = checkProtein.isSelected();
                modelAll.isRNA = isRNA;
                modelAll.isProtein = isProtein;
                modelSelected.isRNA = isRNA;
                modelSelected.isProtein = isProtein;

                listAll.updateUI();
                listSelected.updateUI();
            }
        };
        checkRNA.addActionListener(checkBoxListener);
        checkProtein.addActionListener(checkBoxListener);
    }

    public List<Chain> getChains() {
        return selectedChains;
    }

    public String getSelectionDescription() {
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (Chain c : selectedChains) {
            builder.append("<span style=\"color: "
                    + (i % 2 == 0 ? "blue" : "green") + "\">");
            builder.append(StructureManager.getName(c.getParent()));
            builder.append('.');
            builder.append(c.getChainID());
            builder.append("</span>, ");
            i++;
        }
        builder.delete(builder.length() - 2, builder.length());
        return builder.toString();
    }

    public int showDialog() {
        List<Chain> setManager = new ArrayList<>();
        for (Structure structure : StructureManager.getAllStructures()) {
            for (Chain chain : structure.getChains()) {
                setManager.add(chain);
            }
        }

        ArrayList<Chain> listLeft = modelAll.getElements();
        ArrayList<Chain> listRight = modelSelected.getElements();

        ArrayList<Chain> list = (ArrayList<Chain>) listLeft.clone();
        list.removeAll(setManager);
        for (Chain file : list) {
            modelAll.removeElement(file);
        }

        list = (ArrayList<Chain>) listRight.clone();
        list.removeAll(setManager);
        for (Chain file : list) {
            modelSelected.removeElement(file);
        }

        setManager.removeAll(listLeft);
        setManager.removeAll(listRight);
        for (Chain file : setManager) {
            modelAll.addElement(file);
        }

        listAll.updateUI();
        listSelected.updateUI();

        chosenOption = DialogChainsMultiple.CANCEL;
        setVisible(true);
        return chosenOption;
    }
}
