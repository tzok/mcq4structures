package pl.poznan.put.gui.window;

import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.structure.StructureManager;

final class DialogSelectStructures extends JDialog {
  private static final Dimension INITIAL_STRUCTURE_LIST_SIZE = new Dimension(320, 420);

  private final DefaultListModel<PdbModel> modelAll = new DefaultListModel<>();
  private final DefaultListModel<PdbModel> modelSelected = new DefaultListModel<>();
  private final JList<PdbModel> listAll = new JList<>(modelAll);
  private final JList<PdbModel> listSelected = new JList<>(modelSelected);

  private final List<PdbModel> selectedStructures = new ArrayList<>();
  private final ListCellRenderer<? super PdbModel> renderer = listAll.getCellRenderer();
  private final JButton buttonSelect = new JButton("Select ->");
  private final JButton buttonSelectAll = new JButton("Select all ->");
  private final JButton buttonDeselect = new JButton("<- Deselect");
  private final JButton buttonDeselectAll = new JButton("<- Deselect all");
  private final JButton buttonOk = new JButton("OK");

  private OkCancelOption chosenOption = OkCancelOption.CANCEL;

  DialogSelectStructures(final Frame owner) {
    super(owner, "MCQ4Structures: structure selection", true);

    listAll.setBorder(BorderFactory.createTitledBorder("Available structures"));
    final ListCellRenderer<PdbModel> pdbCellRenderer =
        (list, value, index, isSelected, cellHasFocus) -> {
          final JLabel label =
              (JLabel)
                  renderer.getListCellRendererComponent(
                      list, value, index, isSelected, cellHasFocus);
          if (value != null) {
            label.setText(StructureManager.getName(value));
          }
          assert label != null;
          return label;
        };
    listAll.setCellRenderer(pdbCellRenderer);
    listSelected.setBorder(BorderFactory.createTitledBorder("Selected structures"));
    listSelected.setCellRenderer(pdbCellRenderer);
    final JScrollPane scrollPaneAll = new JScrollPane(listAll);
    scrollPaneAll.setPreferredSize(DialogSelectStructures.INITIAL_STRUCTURE_LIST_SIZE);
    final JScrollPane scrollPaneSelected = new JScrollPane(listSelected);
    scrollPaneSelected.setPreferredSize(DialogSelectStructures.INITIAL_STRUCTURE_LIST_SIZE);

    buttonSelect.setEnabled(false);
    buttonDeselect.setEnabled(false);

    final JPanel panelButtons = new JPanel();
    panelButtons.setLayout(new GridBagLayout());
    final GridBagConstraints constraintsButtons = new GridBagConstraints();
    constraintsButtons.gridx = 0;
    constraintsButtons.gridy = 0;
    constraintsButtons.gridwidth = 1;
    constraintsButtons.gridheight = 1;
    constraintsButtons.fill = GridBagConstraints.HORIZONTAL;
    panelButtons.add(buttonSelect, constraintsButtons);
    constraintsButtons.gridy++;
    panelButtons.add(buttonSelectAll, constraintsButtons);
    constraintsButtons.gridy++;
    panelButtons.add(buttonDeselect, constraintsButtons);
    constraintsButtons.gridy++;
    panelButtons.add(buttonDeselectAll, constraintsButtons);

    final JPanel panelMain = new JPanel();
    panelMain.setLayout(new GridBagLayout());
    final GridBagConstraints constraintsMain = new GridBagConstraints();
    constraintsMain.gridx = 0;
    constraintsMain.weighty = 1.0;
    constraintsMain.fill = GridBagConstraints.VERTICAL;
    panelMain.add(scrollPaneAll, constraintsMain);
    constraintsMain.gridx = 1;
    constraintsMain.fill = GridBagConstraints.VERTICAL;
    panelMain.add(panelButtons, constraintsMain);
    constraintsMain.gridx = 2;
    constraintsMain.fill = GridBagConstraints.VERTICAL;
    panelMain.add(scrollPaneSelected, constraintsMain);

    final JPanel panelOkCancel = new JPanel();
    panelOkCancel.add(buttonOk);
    final JButton buttonCancel = new JButton("Cancel");
    panelOkCancel.add(buttonCancel);

    setLayout(new BorderLayout());
    add(panelMain, BorderLayout.CENTER);
    add(panelOkCancel, BorderLayout.SOUTH);
    pack();

    final Dimension size = getSize();
    final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    final int x = screenSize.width - size.width;
    final int y = screenSize.height - size.height;
    setLocation(x / 2, y / 2);

    final ListSelectionListener listSelectionListener =
        arg0 -> {
          assert arg0 != null;
          final ListSelectionModel source = (ListSelectionModel) arg0.getSource();
          if (source.equals(listAll.getSelectionModel())) {
            buttonSelect.setEnabled(!listAll.isSelectionEmpty());
          } else if (source.equals(listSelected.getSelectionModel())) {
            buttonDeselect.setEnabled(!listSelected.isSelectionEmpty());
          }
        };
    listAll.getSelectionModel().addListSelectionListener(listSelectionListener);
    listSelected.getSelectionModel().addListSelectionListener(listSelectionListener);

    final ActionListener actionListenerSelectDeselect =
        arg0 -> {
          List<PdbModel> values = Collections.emptyList();
          boolean isSelect = false;

          assert arg0 != null;
          final Object source = arg0.getSource();
          if (source.equals(buttonSelect)) {
            values = listAll.getSelectedValuesList();
            isSelect = true;
          } else if (source.equals(buttonSelectAll)) {
            values = Collections.list(modelAll.elements());
            isSelect = true;
          } else if (source.equals(buttonDeselect)) {
            values = listSelected.getSelectedValuesList();
            isSelect = false;
          } else if (source.equals(buttonDeselectAll)) {
            values = Collections.list(modelSelected.elements());
            isSelect = false;
          }

          for (final PdbModel f : values) {
            if (isSelect) {
              modelAll.removeElement(f);
              modelSelected.addElement(f);
            } else {
              modelAll.addElement(f);
              modelSelected.removeElement(f);
            }
          }

          buttonOk.setEnabled(modelSelected.size() > 1);
        };
    buttonSelect.addActionListener(actionListenerSelectDeselect);
    buttonSelectAll.addActionListener(actionListenerSelectDeselect);
    buttonDeselect.addActionListener(actionListenerSelectDeselect);
    buttonDeselectAll.addActionListener(actionListenerSelectDeselect);

    buttonOk.addActionListener(
        e -> {
          selectedStructures.clear();
          selectedStructures.addAll(Collections.list(modelSelected.elements()));
          chosenOption = OkCancelOption.OK;
          dispose();
        });

    buttonCancel.addActionListener(
        arg0 -> {
          chosenOption = OkCancelOption.CANCEL;
          dispose();
        });
  }

  public List<PdbModel> getStructures() {
    return Collections.unmodifiableList(selectedStructures);
  }

  public OkCancelOption showDialog() {
    final List<PdbModel> setManager = StructureManager.getAllStructures();
    final ArrayList<PdbModel> listLeft = Collections.list(modelAll.elements());
    final ArrayList<PdbModel> listRight = Collections.list(modelSelected.elements());

    final Collection<PdbModel> listLeftCopy = new ArrayList<>(listLeft);
    listLeftCopy.removeAll(setManager);
    for (final PdbModel structure : listLeftCopy) {
      modelAll.removeElement(structure);
    }

    final Collection<PdbModel> listRightCopy = new ArrayList<>(listRight);
    listRightCopy.removeAll(setManager);
    for (final PdbModel structure : listRightCopy) {
      modelSelected.removeElement(structure);
    }

    setManager.removeAll(listLeft);
    setManager.removeAll(listRight);
    for (final PdbModel file : setManager) {
      modelAll.addElement(file);
    }

    buttonOk.setEnabled(modelSelected.size() > 1);
    chosenOption = OkCancelOption.CANCEL;
    setVisible(true);
    return chosenOption;
  }
}
