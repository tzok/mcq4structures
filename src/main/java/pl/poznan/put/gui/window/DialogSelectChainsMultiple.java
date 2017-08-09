package pl.poznan.put.gui.window;

import pl.poznan.put.gui.component.FilteredListModel;
import pl.poznan.put.matching.SelectionFactory;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbChain;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.structure.tertiary.StructureManager;

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
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DialogSelectChainsMultiple extends JDialog {
    private static final long serialVersionUID = -5562038332587512308L;

    private static final int CANCEL = 0;
    public static final int OK = 1;

    private final FilteredListModel modelAll = new FilteredListModel();
    private final FilteredListModel modelSelected = new FilteredListModel();
    private final JList<PdbCompactFragment> listAll = new JList<>(modelAll);
    private final JList<PdbCompactFragment> listSelected =
            new JList<>(modelSelected);
    private final ListCellRenderer<? super PdbCompactFragment> renderer =
            listAll.getCellRenderer();
    private final JButton buttonOk = new JButton("OK");
    private final JCheckBox checkRNA = new JCheckBox("RNAs", true);
    private final JCheckBox checkProtein = new JCheckBox("proteins", true);
    private final JButton buttonSelect = new JButton("Select ->");
    private final JButton buttonSelectAll = new JButton("Select all ->");
    private final JButton buttonDeselect = new JButton("<- Deselect");

    private int chosenOption;
    private boolean isFragmentsSizeConstrained;
    private List<PdbCompactFragment> selectedChains = new ArrayList<>();

    public DialogSelectChainsMultiple(final Frame owner) {
        super(owner, true);
        setTitle("MCQ4Structures: multiple chain selection");
        setButtonOkState();

        listAll.setBorder(BorderFactory.createTitledBorder("Available chains"));
        listSelected
                .setBorder(BorderFactory.createTitledBorder("Selected chains"));
        buttonSelect.setEnabled(false);
        buttonDeselect.setEnabled(false);

        final ListCellRenderer<PdbCompactFragment> pdbCellRenderer =
                (list, value, index, isSelected, cellHasFocus) -> {
                    final JLabel label = (JLabel) renderer
                            .getListCellRendererComponent(list, value, index,
                                                          isSelected,
                                                          cellHasFocus);

                    if (value != null) {
                        final boolean isRNA =
                                value.getMoleculeType() == MoleculeType.RNA;
                        label.setText(value.getName());
                        label.setBackground(isRNA ? Color.CYAN : Color.YELLOW);
                    }

                    return label;
                };
        listAll.setCellRenderer(pdbCellRenderer);
        listSelected.setCellRenderer(pdbCellRenderer);

        final JPanel panelButtons = new JPanel();
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
        final JButton buttonDeselectAll = new JButton("<- Deselect all");
        panelButtons.add(buttonDeselectAll, constraints);

        final JPanel panelMain = new JPanel();
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

        final JPanel panelOkCancel = new JPanel();
        panelOkCancel.add(buttonOk);
        final JButton buttonCancel = new JButton("Cancel");
        panelOkCancel.add(buttonCancel);

        setLayout(new BorderLayout());
        add(panelMain, BorderLayout.CENTER);
        add(panelOkCancel, BorderLayout.PAGE_END);
        pack();

        final Dimension size = getSize();
        final Dimension screenSize =
                Toolkit.getDefaultToolkit().getScreenSize();
        final int x = screenSize.width - size.width;
        final int y = screenSize.height - size.height;
        setLocation(x / 2, y / 2);

        final ListSelectionListener listSelectionListener = arg0 -> {
            if (arg0 != null) {
                final ListSelectionModel source =
                        (ListSelectionModel) arg0.getSource();
                if (source.equals(listAll.getSelectionModel())) {
                    buttonSelect.setEnabled(!listAll.isSelectionEmpty());
                } else if (source.equals(listSelected)) {
                    buttonDeselect.setEnabled(!listSelected.isSelectionEmpty());
                }

                setButtonOkState();
            }
        };
        listAll.getSelectionModel()
               .addListSelectionListener(listSelectionListener);
        listSelected.getSelectionModel()
                    .addListSelectionListener(listSelectionListener);

        final ActionListener actionListenerSelectDeselect = evt -> {

            if (evt != null) {
                final List<PdbCompactFragment> values;
                final boolean isSelect;
                final Object source = evt.getSource();
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

                for (final PdbCompactFragment compactFragment : values) {
                    assert compactFragment != null;
                    if (isSelect) {
                        if (modelSelected.canAddElement(compactFragment,
                                                        isFragmentsSizeConstrained)) {
                            modelAll.removeElement(compactFragment);
                            modelSelected.addElement(compactFragment);
                        }
                    } else {
                        modelAll.addElement(compactFragment);
                        modelSelected.removeElement(compactFragment);
                    }
                }

                listAll.clearSelection();
                listSelected.clearSelection();

                listAll.updateUI();
                listSelected.updateUI();

                setButtonOkState();
            }
        };
        buttonSelect.addActionListener(actionListenerSelectDeselect);
        buttonSelectAll.addActionListener(actionListenerSelectDeselect);
        buttonDeselect.addActionListener(actionListenerSelectDeselect);
        buttonDeselectAll.addActionListener(actionListenerSelectDeselect);

        buttonOk.addActionListener(e -> {
            selectedChains = modelSelected.getSelectedElements();
            chosenOption = DialogSelectChainsMultiple.OK;
            dispose();
        });

        buttonCancel.addActionListener(arg0 -> {
            chosenOption = DialogSelectChainsMultiple.CANCEL;
            dispose();
        });

        final ActionListener checkBoxListener = e -> {
            final boolean isRNA = checkRNA.isSelected();
            final boolean isProtein = checkProtein.isSelected();
            modelAll.setRNA(isRNA);
            modelAll.setProtein(isProtein);
            modelSelected.setRNA(isRNA);
            modelSelected.setProtein(isProtein);

            listAll.updateUI();
            listSelected.updateUI();
        };
        checkRNA.addActionListener(checkBoxListener);
        checkProtein.addActionListener(checkBoxListener);
    }

    private void setButtonOkState() {
        buttonOk.setEnabled(modelSelected.getSize() >= 2);
    }

    public List<PdbCompactFragment> getChains() {
        return Collections.unmodifiableList(selectedChains);
    }

    public int showDialog(final boolean fragmentsSameSize) {
        final List<PdbCompactFragment> fragments = new ArrayList<>();

        for (final PdbModel structure : StructureManager.getAllStructures()) {
            for (final PdbChain chain : structure.getChains()) {
                final String name = String.format("%s.%s", StructureManager
                        .getName(structure), chain.getIdentifier());
                final StructureSelection selection = SelectionFactory
                        .create(name, Collections.singleton(chain));
                fragments.addAll(selection.getCompactFragments());
            }
        }

        final List<PdbCompactFragment> listL = modelAll.getElements();
        final List<PdbCompactFragment> listR = modelSelected.getElements();

        /*
         * Refresh data -> if some structure was removed from StructureManager,
         * removePair its chains as well
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
        isFragmentsSizeConstrained = fragmentsSameSize;

        deselectAll();
        setVisible(true);
        return chosenOption;
    }

    private void deselectAll() {
        for (final PdbCompactFragment compactFragment : modelSelected
                .getElements()) {
            modelAll.addElement(compactFragment);
            modelSelected.removeElement(compactFragment);
        }

        listAll.clearSelection();
        listSelected.clearSelection();
        listAll.updateUI();
        listSelected.updateUI();
    }
}
