package pl.poznan.put.gui.panel;

import pl.poznan.put.interfaces.DisplayableExportable;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.torsion.AverageTorsionAngleType;
import pl.poznan.put.torsion.MasterTorsionAngleType;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class TorsionAngleTypesPanel extends JPanel {
  private final Map<JCheckBox, MasterTorsionAngleType> mapCheckBoxToMasterType =
      new LinkedHashMap<>();

  private final JPanel anglesPanel = new JPanel();
  private final JButton buttonSelectAll = new JButton("Select all");

  private final ActionListener checkBoxListener;

  public TorsionAngleTypesPanel(
      final MoleculeType moleculeType, final ActionListener checkBoxListener) {
    super(new BorderLayout());
    this.checkBoxListener = checkBoxListener;

    handleMoleculeType(moleculeType);

    anglesPanel.setLayout(new BoxLayout(anglesPanel, BoxLayout.Y_AXIS));
    final JPanel buttonsPanel = new JPanel();
    buttonsPanel.add(buttonSelectAll);
    final JButton buttonClear = new JButton("Clear");
    buttonsPanel.add(buttonClear);

    add(anglesPanel, BorderLayout.CENTER);
    add(buttonsPanel, BorderLayout.SOUTH);

    final ActionListener selectClearActionListener =
        e -> {
          final boolean select = e.getSource().equals(buttonSelectAll);
          for (final JCheckBox checkBox : mapCheckBoxToMasterType.keySet()) {
            checkBox.setSelected(select);
          }
        };
    buttonSelectAll.addActionListener(selectClearActionListener);
    buttonClear.addActionListener(selectClearActionListener);
  }

  public final boolean isAnySelected() {
    return mapCheckBoxToMasterType.keySet().stream().anyMatch(AbstractButton::isSelected);
  }

  public final List<MasterTorsionAngleType> getSelected() {
    final List<MasterTorsionAngleType> selected = new ArrayList<>();

    for (final Map.Entry<JCheckBox, MasterTorsionAngleType> entry :
        mapCheckBoxToMasterType.entrySet()) {
      final JCheckBox checkBox = entry.getKey();
      if (checkBox.isSelected()) {
        selected.add(entry.getValue());
      }
    }

    return selected;
  }

  private void handleMoleculeType(final MoleculeType moleculeType) {
    final MasterTorsionAngleType[] masterAngleTypes;
    final AverageTorsionAngleType averageAngleType;

    switch (moleculeType) {
      case PROTEIN:
        setBorder(BorderFactory.createTitledBorder("Protein"));
        masterAngleTypes =
            AverageTorsionAngleType.forProtein()
                .consideredAngles()
                .toArray(new MasterTorsionAngleType[0]);
        averageAngleType = AverageTorsionAngleType.forProtein();
        break;
      case RNA:
        setBorder(BorderFactory.createTitledBorder("RNA"));
        masterAngleTypes =
            AverageTorsionAngleType.forNucleicAcid()
                .consideredAngles()
                .toArray(new MasterTorsionAngleType[0]);
        averageAngleType = AverageTorsionAngleType.forNucleicAcid();
        break;
      case UNKNOWN:
      default:
        return;
    }

    for (final MasterTorsionAngleType masterType : masterAngleTypes) {
      handleMasterType(masterType, false);
    }
    handleMasterType(averageAngleType, true);
  }

  private void handleMasterType(final MasterTorsionAngleType masterType, final boolean selected) {
    final Collection<String> angleNames =
        masterType.angleTypes().stream()
            .map(DisplayableExportable::longDisplayName)
            .collect(Collectors.toCollection(TreeSet::new));

    final StringBuilder builder = new StringBuilder("<html>");
    for (final String angleName : angleNames) {
      builder.append(angleName);
      builder.append("<br/>");
    }
    builder.delete(builder.length() - 5, builder.length());
    builder.append("</html>");

    final String masterName = builder.toString();
    final JCheckBox checkBox = new JCheckBox(masterName, selected);
    checkBox.addActionListener(checkBoxListener);

    anglesPanel.add(checkBox);
    anglesPanel.add(new JLabel("<html>&nbsp;</html>"));
    mapCheckBoxToMasterType.put(checkBox, masterType);
  }
}
