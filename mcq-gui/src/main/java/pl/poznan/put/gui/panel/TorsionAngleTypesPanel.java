package pl.poznan.put.gui.panel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.protein.torsion.ProteinTorsionAngleType;
import pl.poznan.put.rna.torsion.RNATorsionAngleType;
import pl.poznan.put.torsion.AverageTorsionAngleType;
import pl.poznan.put.torsion.MasterTorsionAngleType;
import pl.poznan.put.torsion.TorsionAngleType;

public class TorsionAngleTypesPanel extends JPanel {
  private final Map<JCheckBox, MasterTorsionAngleType> mapCheckBoxToMasterType =
      new LinkedHashMap<>();

  private final JPanel anglesPanel = new JPanel();
  private final JPanel buttonsPanel = new JPanel();
  private final JButton buttonSelectAll = new JButton("Select all");
  private final JButton buttonClear = new JButton("Clear");

  private final ActionListener checkBoxListener;

  public TorsionAngleTypesPanel(MoleculeType moleculeType, ActionListener checkBoxListener) {
    super(new BorderLayout());
    this.checkBoxListener = checkBoxListener;

    handleMoleculeType(moleculeType);

    anglesPanel.setLayout(new BoxLayout(anglesPanel, BoxLayout.Y_AXIS));
    buttonsPanel.add(buttonSelectAll);
    buttonsPanel.add(buttonClear);

    add(anglesPanel, BorderLayout.CENTER);
    add(buttonsPanel, BorderLayout.SOUTH);

    ActionListener selectClearActionListener =
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            boolean select = e.getSource().equals(buttonSelectAll) ? true : false;
            for (JCheckBox checkBox : mapCheckBoxToMasterType.keySet()) {
              checkBox.setSelected(select);
            }
          }
        };
    buttonSelectAll.addActionListener(selectClearActionListener);
    buttonClear.addActionListener(selectClearActionListener);
  }

  private void handleMoleculeType(final MoleculeType moleculeType) {
    MasterTorsionAngleType[] masterAngleTypes;
    AverageTorsionAngleType averageAngleType;

    switch (moleculeType) {
      case PROTEIN:
        setBorder(BorderFactory.createTitledBorder("Protein"));
        masterAngleTypes = ProteinTorsionAngleType.values();
        averageAngleType = ProteinTorsionAngleType.getAverageOverMainAngles();
        break;
      case RNA:
        setBorder(BorderFactory.createTitledBorder("RNA"));
        masterAngleTypes = RNATorsionAngleType.values();
        averageAngleType = RNATorsionAngleType.getAverageOverMainAngles();
        break;
      case UNKNOWN:
      default:
        return;
    }

    for (MasterTorsionAngleType masterType : masterAngleTypes) {
      handleMasterType(masterType, false);
    }
    handleMasterType(averageAngleType, true);
  }

  private void handleMasterType(MasterTorsionAngleType masterType, boolean selected) {
    SortedSet<String> angleNames = new TreeSet<>();
    for (TorsionAngleType angleType : masterType.getAngleTypes()) {
      angleNames.add(angleType.getLongDisplayName());
    }

    StringBuilder builder = new StringBuilder("<html>");
    for (String angleName : angleNames) {
      builder.append(angleName);
      builder.append("<br/>");
    }
    builder.delete(builder.length() - 5, builder.length());
    builder.append("</html>");

    String masterName = builder.toString();
    JCheckBox checkBox = new JCheckBox(masterName, selected);
    checkBox.addActionListener(checkBoxListener);

    anglesPanel.add(checkBox);
    anglesPanel.add(new JLabel("<html>&nbsp;</html>"));
    mapCheckBoxToMasterType.put(checkBox, masterType);
  }

  public boolean isAnySelected() {
    for (JCheckBox checkBox : mapCheckBoxToMasterType.keySet()) {
      if (checkBox.isSelected()) {
        return true;
      }
    }
    return false;
  }

  public List<MasterTorsionAngleType> getSelected() {
    List<MasterTorsionAngleType> selected = new ArrayList<>();

    for (Entry<JCheckBox, MasterTorsionAngleType> entry : mapCheckBoxToMasterType.entrySet()) {
      JCheckBox checkBox = entry.getKey();
      if (checkBox.isSelected()) {
        selected.add(entry.getValue());
      }
    }

    return selected;
  }
}
