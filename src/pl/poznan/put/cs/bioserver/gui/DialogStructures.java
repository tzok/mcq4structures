package pl.poznan.put.cs.bioserver.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.biojava.bio.structure.Structure;

import pl.poznan.put.cs.bioserver.helper.StructureManager;

final class DialogStructures extends JDialog {
    public static final int CANCEL = 0;
    public static final int OK = 1;
    private static final long serialVersionUID = 1L;

    private static DialogStructures instance;

    public static DialogStructures getInstance(Frame owner) {
        if (DialogStructures.instance == null) {
            DialogStructures.instance = new DialogStructures(owner);
        }
        return DialogStructures.instance;
    }

    private int chosenOption;
    private DefaultListModel<Structure> modelAll;
    private DefaultListModel<Structure> modelSelected;
    private Structure[] selectedStructures;

    private DialogStructures(Frame owner) {
        super(owner, true);

        modelAll = new DefaultListModel<>();
        final JList<Structure> listAll = new JList<>(modelAll);
        listAll.setBorder(BorderFactory
                .createTitledBorder("Available structures"));
        final ListCellRenderer<? super Structure> renderer = listAll
                .getCellRenderer();
        modelSelected = new DefaultListModel<>();
        final JList<Structure> listSelected = new JList<>(modelSelected);
        listSelected.setBorder(BorderFactory
                .createTitledBorder("Selected structures"));

        ListCellRenderer<Structure> pdbCellRenderer = new ListCellRenderer<Structure>() {
            @Override
            public Component getListCellRendererComponent(
                    JList<? extends Structure> list, Structure value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) renderer.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                label.setText(StructureManager.getName(value));
                return label;
            }
        };
        listAll.setCellRenderer(pdbCellRenderer);
        listSelected.setCellRenderer(pdbCellRenderer);

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

        setTitle("MCQ4Structures: structure selection");

        ListSelectionListener listSelectionListener = new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent arg0) {
                ListSelectionModel source = (ListSelectionModel) arg0
                        .getSource();
                if (source.equals(listAll.getSelectionModel())) {
                    buttonSelect.setEnabled(!listAll.isSelectionEmpty());
                } else { // source.equals(listSelected)
                    buttonDeselect.setEnabled(!listSelected.isSelectionEmpty());
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
                List<Structure> values;
                boolean isSelect;

                Object source = arg0.getSource();
                if (source.equals(buttonSelect)) {
                    values = listAll.getSelectedValuesList();
                    isSelect = true;
                } else if (source.equals(buttonSelectAll)) {
                    values = Collections.list(modelAll.elements());
                    isSelect = true;
                } else if (source.equals(buttonDeselect)) {
                    values = listSelected.getSelectedValuesList();
                    isSelect = false;
                } else { // source.equals(buttonDeselectAll)
                    values = Collections.list(modelSelected.elements());
                    isSelect = false;
                }

                for (Structure f : values) {
                    if (isSelect) {
                        modelAll.removeElement(f);
                        modelSelected.addElement(f);
                    } else {
                        modelAll.addElement(f);
                        modelSelected.removeElement(f);
                    }
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
                List<Structure> list = Collections.list(modelSelected
                        .elements());
                selectedStructures = list.toArray(new Structure[list.size()]);

                chosenOption = DialogStructures.OK;
                dispose();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                chosenOption = DialogStructures.CANCEL;
                dispose();
            }
        });
    }

    public String getSelectionDescription() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < selectedStructures.length; i++) {
            builder.append(StructureManager.getName(selectedStructures[i]));
            if (i != selectedStructures.length - 1) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }

    public Structure[] getStructures() {
        return selectedStructures;
    }

    public int showDialog() {
        SortedSet<Structure> setManager = StructureManager.getAllStructures();
        ArrayList<Structure> listLeft = Collections.list(modelAll.elements());
        ArrayList<Structure> listRight = Collections.list(modelSelected
                .elements());

        ArrayList<Structure> list = (ArrayList<Structure>) listLeft.clone();
        list.removeAll(setManager);
        for (Structure structure : list) {
            modelAll.removeElement(structure);
        }

        list = (ArrayList<Structure>) listRight.clone();
        list.removeAll(setManager);
        for (Structure structure : list) {
            modelSelected.removeElement(structure);
        }

        setManager.removeAll(listLeft);
        setManager.removeAll(listRight);
        for (Structure file : setManager) {
            modelAll.addElement(file);
        }

        chosenOption = DialogStructures.CANCEL;
        setVisible(true);
        return chosenOption;
    }
}
