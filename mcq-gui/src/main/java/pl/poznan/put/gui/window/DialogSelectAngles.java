package pl.poznan.put.gui.window;

import pl.poznan.put.gui.panel.TorsionAngleTypesPanel;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.torsion.MasterTorsionAngleType;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class DialogSelectAngles extends JDialog {
  private final List<MasterTorsionAngleType> selectedAngles = new ArrayList<>();
  private final JButton buttonOk = new JButton("OK");
  private final TorsionAngleTypesPanel panelAnglesProtein =
      new TorsionAngleTypesPanel(MoleculeType.PROTEIN, e -> setButtonOkState());
  private final TorsionAngleTypesPanel panelAnglesRNA =
      new TorsionAngleTypesPanel(MoleculeType.RNA, e -> setButtonOkState());
  private OkCancelOption chosenOption = OkCancelOption.CANCEL;

  DialogSelectAngles(final Frame owner) {
    super(owner, true);

    final JPanel panelOptions = new JPanel();
    panelOptions.setLayout(new GridLayout(1, 2));
    panelOptions.add(panelAnglesRNA);
    panelOptions.add(panelAnglesProtein);

    final JPanel panelOkCancel = new JPanel();
    panelOkCancel.add(buttonOk);
    final JButton buttonCancel = new JButton("Cancel");
    panelOkCancel.add(buttonCancel);

    setLayout(new BorderLayout());
    add(panelOptions, BorderLayout.CENTER);
    add(panelOkCancel, BorderLayout.SOUTH);
    pack();

    final Dimension size = getSize();
    final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    final int x = screenSize.width - size.width;
    final int y = screenSize.height - size.height;
    setLocation(x / 2, y / 2);
    setTitle("MCQ4Structures: torsion angle(s) selection");

    buttonOk.addActionListener(
        e -> {
          chosenOption = OkCancelOption.OK;
          selectedAngles.clear();
          selectedAngles.addAll(panelAnglesRNA.getSelected());
          selectedAngles.addAll(panelAnglesProtein.getSelected());
          dispose();
        });

    buttonCancel.addActionListener(
        e -> {
          chosenOption = OkCancelOption.CANCEL;
          selectedAngles.clear();
          dispose();
        });
  }

  public List<MasterTorsionAngleType> getAngles() {
    return Collections.unmodifiableList(selectedAngles);
  }

  private void setButtonOkState() {
    final boolean enabled = panelAnglesRNA.isAnySelected() || panelAnglesProtein.isAnySelected();
    buttonOk.setEnabled(enabled);
  }

  public OkCancelOption showDialog() {
    setVisible(true);
    return chosenOption;
  }
}
