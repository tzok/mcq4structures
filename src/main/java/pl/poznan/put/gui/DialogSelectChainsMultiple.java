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

import pl.poznan.put.matching.SelectionFactory;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbChain;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.structure.tertiary.StructureManager;

final class DialogSelectChainsMultiple extends JDialog {
    public static final int CANCEL = 0;
    public static final int OK = 1;

    private final FilteredListModel modelAll = new FilteredListModel();
    private final FilteredListModel modelSelected = new FilteredListModel();
    private final JList<PdbCompactFragment> listAll = new JList<>(modelAll);
    private final JList<PdbCompactFragment> listSelected = new JList<>(modelSelected);
    private final ListCellRenderer<? super PdbCompactFragment> renderer = listAll.getCellRenderer();
    private final JButton buttonOk = new JButton("OK");
    private final JButton buttonCancel = new JButton("Cancel");
    private final JCheckBox checkRNA = new JCheckBox("RNAs", true);
    private final JCheckBox checkProtein = new JCheckBox("proteins", true);
    private final JButton buttonSelect = new JButton("Select ->");
    private final JButton buttonSelectAll = new JButton("Select all ->");
    private final JButton buttonDeselect = new JButton("<- Deselect");
    private final JButton buttonDeselectAll = new JButton("<- Deselect all");

    private int chosenOption;
    private List<PdbCompactFragment> selectedChains = new ArrayList<>();

    public DialogSelectChainsMultiple(Frame owner) {
        super(owner, true);
        setTitle("MCQ4Structures: multiple chain selection");
        setButtonOkState();

        listAll.setBorder(BorderFactory.createTitledBorder("Available chains"));
        listSelected.setBorder(BorderFactory.createTitledBorder("Selected chains"));
        buttonSelect.setEnabled(false);
        buttonDeselect.setEnabled(false);

        ListCellRenderer<PdbCompactFragment> pdbCellRenderer = new ListCellRenderer<PdbCompactFragment>() {
            @Override
            public Component getListCellRendererComponent(
                    JList<? extends PdbCompactFragment> list,
                    PdbCompactFragment value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                JLabel label = (JLabel) renderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (value != null) {
                    boolean isRNA = value.getMoleculeType() == MoleculeType.RNA;
                    label.setText(value.getName());
                    label.setBackground(isRNA ? Color.CYAN : Color.YELLOW);
                }

                return label;
            }
        };
        listAll.setCellRenderer(pdbCellRenderer);
        listSelected.setCellRenderer(pdbCellRenderer);

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

        JPanel panelOkCancel = new JPanel();
        panelOkCancel.add(buttonOk);
        panelOkCancel.add(buttonCancel);

        setLayout(new BorderLayout());
        add(panelMain, BorderLayout.CENTER);
        add(panelOkCancel, BorderLayout.SOUTH);
        pack();

        Dimension size = getSize();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = screenSize.width - size.width;
        int y = screenSize.height - size.height;
        setLocation(x / 2, y / 2);

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

                    setButtonOkState();
                }
            }
        };
        listAll.getSelectionModel().addListSelectionListener(listSelectionListener);
        listSelected.getSelectionModel().addListSelectionListener(listSelectionListener);

        ActionListener actionListenerSelectDeselect = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                List<PdbCompactFragment> values;
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

                    for (PdbCompactFragment f : values) {
                        assert f != null;
                        if (isSelect) {
                            if (modelSelected.canAddElement(f)) {
                                modelAll.removeElement(f);
                                modelSelected.addElement(f);
                            }
                        } else {
                            modelAll.addElement(f);
                            modelSelected.removeElement(f);
                        }
                    }

                    listAll.clearSelection();
                    listSelected.clearSelection();

                    listAll.updateUI();
                    listSelected.updateUI();

                    setButtonOkState();
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
                chosenOption = DialogSelectChainsMultiple.OK;
                dispose();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                chosenOption = DialogSelectChainsMultiple.CANCEL;
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

    public List<PdbCompactFragment> getChains() {
        return selectedChains;
    }

    public void setButtonOkState() {
        buttonOk.setEnabled(modelSelected.getSize() >= 2);
    }

    public int showDialog() {
        List<PdbCompactFragment> fragments = new ArrayList<>();

        for (PdbModel structure : StructureManager.getAllStructures()) {
            for (PdbChain chain : structure.getChains()) {
                String name = StructureManager.getName(structure) + "." + chain.getIdentifier();
                StructureSelection selection = SelectionFactory.create(name, chain);
                fragments.addAll(selection.getCompactFragments());
            }
        }

        List<PdbCompactFragment> listL = modelAll.getElements();
        List<PdbCompactFragment> listR = modelSelected.getElements();

        /*
         * Refresh data -> if some structure was removed from StructureManager,
         * remove its chains as well
         */
        List<PdbCompactFragment> list = new ArrayList<>(listL);
        list.removeAll(fragments);
        modelAll.removeElements(list);
        list = new ArrayList<>(listR);
        list.removeAll(fragments);
        modelSelected.removeElements(list);

        /*
         * Add all chains from structure that are new in the StructureManager
         */
        fragments.removeAll(listL);
        fragments.removeAll(listR);
        modelAll.addElements(fragments);

        listAll.updateUI();
        listSelected.updateUI();
        chosenOption = DialogSelectChainsMultiple.CANCEL;

        setVisible(true);
        return chosenOption;
    }
}
