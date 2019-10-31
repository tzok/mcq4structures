package pl.poznan.put.gui.panel;

import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbChain;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.structure.tertiary.StructureManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class ChainsPanel extends JPanel {
  private final DefaultComboBoxModel<PdbModel> structureComboBoxModel =
      new DefaultComboBoxModel<>();
  private final JComboBox<PdbModel> structureComboBox = new JComboBox<>(structureComboBoxModel);
  private final JPanel rnaPanel = new JPanel();
  private final JPanel proteinPanel = new JPanel();

  public ChainsPanel(final ActionListener actionListener) {
    super(new BorderLayout());

    rnaPanel.setLayout(new BoxLayout(rnaPanel, BoxLayout.PAGE_AXIS));
    proteinPanel.setLayout(new BoxLayout(proteinPanel, BoxLayout.PAGE_AXIS));

    final JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createTitledBorder("Select structure"));
    panel.add(structureComboBox, BorderLayout.CENTER);
    add(panel, BorderLayout.PAGE_START);

    final JPanel chainPanel = new JPanel(new GridLayout(1, 2));
    chainPanel.setBorder(BorderFactory.createTitledBorder("Select chain(s)"));
    chainPanel.add(rnaPanel);
    chainPanel.add(proteinPanel);
    add(new JScrollPane(chainPanel), BorderLayout.CENTER);

    final ListCellRenderer<? super PdbModel> renderer = structureComboBox.getRenderer();
    structureComboBox.setRenderer(
        (list, value, index, isSelected, cellHasFocus) -> {
          final JLabel label =
              (JLabel)
                  renderer.getListCellRendererComponent(
                      list, value, index, isSelected, cellHasFocus);
          if (value != null) {
            label.setText(StructureManager.getName(value));
          }
          return label;
        });

    structureComboBox.addActionListener(
        e -> {
          final PdbModel structure = (PdbModel) structureComboBox.getSelectedItem();
          if (structure == null) {
            return;
          }

          rnaPanel.removeAll();
          rnaPanel.add(new JLabel("RNAs:"));
          proteinPanel.removeAll();
          proteinPanel.add(new JLabel("Proteins:"));

          for (final PdbChain chain : structure.getChains()) {
            final JCheckBox checkBox = new JCheckBox(String.valueOf(chain.getIdentifier()));
            checkBox.addActionListener(actionListener);

            if (chain.getMoleculeType() == MoleculeType.RNA) {
              rnaPanel.add(checkBox);
            } else if (chain.getMoleculeType() == MoleculeType.PROTEIN) {
              proteinPanel.add(checkBox);
            }
          }

          rnaPanel.updateUI();
          proteinPanel.updateUI();
          actionListener.actionPerformed(e);
        });
  }

  public final JPanel getRnaPanel() {
    return rnaPanel;
  }

  public final JPanel getProteinPanel() {
    return proteinPanel;
  }

  public final PdbModel getSelectedStructure() {
    return (PdbModel) structureComboBox.getSelectedItem();
  }

  public final List<PdbChain> getSelectedChains() {
    final List<PdbChain> list = new ArrayList<>();
    final PdbModel structure = (PdbModel) structureComboBox.getSelectedItem();

    if (structure != null) {
      for (final JPanel panel : new JPanel[] {rnaPanel, proteinPanel}) {
        for (final Component component : panel.getComponents()) {
          if (component instanceof JCheckBox && ((AbstractButton) component).isSelected()) {
            final String chainId = ((AbstractButton) component).getText();

            structure.getChains().stream()
                .filter(chain -> chain.getIdentifier().equals(chainId))
                .findFirst()
                .ifPresent(list::add);
          }
        }
      }
    }
    return list;
  }

  public final void reloadStructures(final Iterable<? extends PdbModel> structures) {
    structureComboBoxModel.removeAllElements();

    for (final PdbModel structure : structures) {
      structureComboBoxModel.addElement(structure);
    }
  }
}
