package pl.poznan.put.gui.panel;

import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbChain;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.structure.tertiary.StructureManager;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
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

    rnaPanel.setLayout(new BoxLayout(rnaPanel, BoxLayout.Y_AXIS));
    proteinPanel.setLayout(new BoxLayout(proteinPanel, BoxLayout.Y_AXIS));

    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createTitledBorder("Select structure"));
    panel.add(structureComboBox, BorderLayout.CENTER);
    add(panel, BorderLayout.NORTH);

    panel = new JPanel(new GridLayout(1, 2));
    panel.setBorder(BorderFactory.createTitledBorder("Select chain(s)"));
    panel.add(rnaPanel);
    panel.add(proteinPanel);
    add(new JScrollPane(panel), BorderLayout.CENTER);

    final ListCellRenderer<? super PdbModel> renderer = structureComboBox.getRenderer();
    structureComboBox.setRenderer(
        new ListCellRenderer<PdbModel>() {
          @Override
          public Component getListCellRendererComponent(
              JList<? extends PdbModel> list,
              PdbModel value,
              int index,
              boolean isSelected,
              boolean cellHasFocus) {
            JLabel label =
                (JLabel)
                    renderer.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
            if (value != null) {
              label.setText(StructureManager.getName(value));
            }
            return label;
          }
        });

    structureComboBox.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            PdbModel structure = (PdbModel) structureComboBox.getSelectedItem();
            if (structure == null) {
              return;
            }

            rnaPanel.removeAll();
            rnaPanel.add(new JLabel("RNAs:"));
            proteinPanel.removeAll();
            proteinPanel.add(new JLabel("Proteins:"));

            for (PdbChain chain : structure.getChains()) {
              JCheckBox checkBox = new JCheckBox(String.valueOf(chain.getIdentifier()));
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
          }
        });
  }

  public JPanel getRnaPanel() {
    return rnaPanel;
  }

  public JPanel getProteinPanel() {
    return proteinPanel;
  }

  public PdbModel getSelectedStructure() {
    return (PdbModel) structureComboBox.getSelectedItem();
  }

  public List<PdbChain> getSelectedChains() {
    List<PdbChain> list = new ArrayList<>();
    PdbModel structure = (PdbModel) structureComboBox.getSelectedItem();

    if (structure != null) {
      for (JPanel panel : new JPanel[] {rnaPanel, proteinPanel}) {
        for (Component component : panel.getComponents()) {
          if (component instanceof JCheckBox && ((JCheckBox) component).isSelected()) {
            String chainId = ((JCheckBox) component).getText();

            for (PdbChain chain : structure.getChains()) {
              if (chain.getIdentifier().equals(chainId)) {
                list.add(chain);
                break;
              }
            }
          }
        }
      }
    }
    return list;
  }

  public void reloadStructures(List<PdbModel> structures) {
    structureComboBoxModel.removeAllElements();

    for (PdbModel structure : structures) {
      structureComboBoxModel.addElement(structure);
    }
  }
}
