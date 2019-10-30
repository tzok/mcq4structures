package pl.poznan.put.gui.window;

import pl.poznan.put.gui.component.FilteredListModel;
import pl.poznan.put.matching.SelectionFactory;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbChain;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.structure.tertiary.StructureManager;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

final class DialogSelectChainsMultiple extends JDialog {
  private final FilteredListModel modelAll = new FilteredListModel();
  private final FilteredListModel modelSelected = new FilteredListModel();
  private final JList<PdbCompactFragment> listAll = new JList<>(modelAll);
  private final JList<PdbCompactFragment> listSelected = new JList<>(modelSelected);
  private final ListCellRenderer<? super PdbCompactFragment> renderer = listAll.getCellRenderer();
  private final JButton buttonOk = new JButton("OK");
  private final JCheckBox checkRNA = new JCheckBox("RNAs", true);
  private final JCheckBox checkProtein = new JCheckBox("proteins", true);
  private final JButton buttonSelect = new JButton("Select ->");
  private final JButton buttonSelectAll = new JButton("Select all ->");
  private final JButton buttonDeselect = new JButton("<- Deselect");

  private OkCancelOption chosenOption = OkCancelOption.CANCEL;
  private boolean isFragmentsSizeConstrained = false;
  private List<PdbCompactFragment> selectedChains = new ArrayList<>();

  DialogSelectChainsMultiple(final Frame owner) {
    super(owner, true);
    setTitle("MCQ4Structures: multiple chain selection");
    setButtonOkState();

    listAll.setBorder(BorderFactory.createTitledBorder("Available chains"));
    listSelected.setBorder(BorderFactory.createTitledBorder("Selected chains"));
    buttonSelect.setEnabled(false);
    buttonDeselect.setEnabled(false);

    final ListCellRenderer<PdbCompactFragment> pdbCellRenderer =
        (list, value, index, isSelected, cellHasFocus) -> {
          final JLabel label =
              (JLabel)
                  renderer.getListCellRendererComponent(
                      list, value, index, isSelected, cellHasFocus);

          if (value != null) {
            final boolean isRNA = value.getMoleculeType() == MoleculeType.RNA;
            label.setText(value.getName());
            label.setBackground(isRNA ? Color.CYAN : Color.YELLOW);
          }

          return label;
        };
    listAll.setCellRenderer(pdbCellRenderer);
    listSelected.setCellRenderer(pdbCellRenderer);

    final JPanel panelButtons = new JPanel();
    panelButtons.setLayout(new GridBagLayout());
    final GridBagConstraints constraintsButtons = new GridBagConstraints();
    constraintsButtons.gridx = 0;
    constraintsButtons.gridy = 0;
    constraintsButtons.gridwidth = 1;
    constraintsButtons.gridheight = 1;
    constraintsButtons.fill = GridBagConstraints.HORIZONTAL;
    panelButtons.add(new JLabel("Limit to:"), constraintsButtons);
    constraintsButtons.gridy++;
    panelButtons.add(checkRNA, constraintsButtons);
    constraintsButtons.gridy++;
    panelButtons.add(checkProtein, constraintsButtons);
    constraintsButtons.gridy++;
    panelButtons.add(buttonSelect, constraintsButtons);
    constraintsButtons.gridy++;
    panelButtons.add(buttonSelectAll, constraintsButtons);
    constraintsButtons.gridy++;
    panelButtons.add(buttonDeselect, constraintsButtons);
    constraintsButtons.gridy++;
    final JButton buttonDeselectAll = new JButton("<- Deselect all");
    panelButtons.add(buttonDeselectAll, constraintsButtons);

    final JPanel panelMain = new JPanel();
    panelMain.setLayout(new GridBagLayout());
    final GridBagConstraints constraintsMain = new GridBagConstraints();
    constraintsMain.gridx = 0;
    constraintsMain.gridy = 0;
    constraintsMain.weightx = 0.5;
    constraintsMain.weighty = 0.5;
    constraintsMain.fill = GridBagConstraints.BOTH;
    panelMain.add(new JScrollPane(listAll), constraintsMain);
    constraintsMain.gridx++;
    constraintsMain.weightx = 0;
    constraintsMain.fill = GridBagConstraints.VERTICAL;
    panelMain.add(panelButtons, constraintsMain);
    constraintsMain.gridx++;
    constraintsMain.weightx = 0.5;
    constraintsMain.fill = GridBagConstraints.BOTH;
    panelMain.add(new JScrollPane(listSelected), constraintsMain);

    final JPanel panelOkCancel = new JPanel();
    panelOkCancel.add(buttonOk);
    final JButton buttonCancel = new JButton("Cancel");
    panelOkCancel.add(buttonCancel);

    setLayout(new BorderLayout());
    add(panelMain, BorderLayout.CENTER);
    add(panelOkCancel, BorderLayout.PAGE_END);
    pack();

    final Dimension size = getSize();
    final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    final int x = screenSize.width - size.width;
    final int y = screenSize.height - size.height;
    setLocation(x / 2, y / 2);

    final ListSelectionListener listSelectionListener =
        arg0 -> {
          if (arg0 != null) {
            final ListSelectionModel source = (ListSelectionModel) arg0.getSource();
            if (source.equals(listAll.getSelectionModel())) {
              buttonSelect.setEnabled(!listAll.isSelectionEmpty());
            } else if (source.equals(listSelected.getSelectionModel())) {
              buttonDeselect.setEnabled(!listSelected.isSelectionEmpty());
            }

            setButtonOkState();
          }
        };
    listAll.getSelectionModel().addListSelectionListener(listSelectionListener);
    listSelected.getSelectionModel().addListSelectionListener(listSelectionListener);

    final ActionListener actionListenerSelectDeselect =
        evt -> {
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
                if (modelSelected.canAddElement(compactFragment, isFragmentsSizeConstrained)) {
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

    buttonOk.addActionListener(
        e -> {
          selectedChains = modelSelected.getSelectedElements();
          chosenOption = OkCancelOption.OK;
          dispose();
        });

    buttonCancel.addActionListener(
        arg0 -> {
          chosenOption = OkCancelOption.CANCEL;
          dispose();
        });

    final ActionListener checkBoxListener =
        e -> {
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

  public OkCancelOption showDialog(final boolean fragmentsSameSize) {
    final Collection<PdbCompactFragment> fragments = new ArrayList<>();

    for (final PdbModel structure : StructureManager.getAllStructures()) {
      for (final PdbChain chain : structure.getChains()) {
        final String name =
            String.format("%s.%s", StructureManager.getName(structure), chain.getIdentifier());
        final StructureSelection selection =
            SelectionFactory.create(name, Collections.singleton(chain));
        fragments.addAll(selection.getCompactFragments());
      }
    }

    final List<PdbCompactFragment> listL = modelAll.getElements();
    final List<PdbCompactFragment> listR = modelSelected.getElements();

    /*
     * Refresh data -> if some structure was removed from StructureManager,
     * removePair its chains as well
     */
    final Collection<PdbCompactFragment> listLCopy = new ArrayList<>(listL);
    listLCopy.removeAll(fragments);
    modelAll.removeElements(listLCopy);
    final Collection<PdbCompactFragment> listRCopy = new ArrayList<>(listR);
    listRCopy.removeAll(fragments);
    modelSelected.removeElements(listRCopy);

    /*
     * Add all chains from structure that are new in the StructureManager
     */
    fragments.removeAll(listL);
    fragments.removeAll(listR);
    modelAll.addElements(fragments);

    listAll.updateUI();
    listSelected.updateUI();
    chosenOption = OkCancelOption.CANCEL;
    isFragmentsSizeConstrained = fragmentsSameSize;

    deselectAll();
    setVisible(true);
    return chosenOption;
  }

  private void deselectAll() {
    for (final PdbCompactFragment compactFragment : modelSelected.getElements()) {
      modelAll.addElement(compactFragment);
      modelSelected.removeElement(compactFragment);
    }

    listAll.clearSelection();
    listSelected.clearSelection();
    listAll.updateUI();
    listSelected.updateUI();
  }
}
