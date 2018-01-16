package pl.poznan.put.gui.window;

import pl.poznan.put.gui.panel.TorsionAngleTypesPanel;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.torsion.MasterTorsionAngleType;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DialogSelectAngles extends JDialog {
  public static final int CANCEL = 0;
  public static final int OK = 1;
  private final List<MasterTorsionAngleType> selectedAngles = new ArrayList<>();
  private final JButton buttonOk = new JButton("OK");
  private final JButton buttonCancel = new JButton("Cancel");
  private final ActionListener checkBoxListener =
      new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          setButtonOkState();
        }
      };
  private final TorsionAngleTypesPanel panelAnglesRNA =
      new TorsionAngleTypesPanel(MoleculeType.RNA, checkBoxListener);
  private final TorsionAngleTypesPanel panelAnglesProtein =
      new TorsionAngleTypesPanel(MoleculeType.PROTEIN, checkBoxListener);
  private int chosenOption;

  public DialogSelectAngles(Frame owner) {
    super(owner, true);

    JPanel panelOptions = new JPanel();
    panelOptions.setLayout(new GridLayout(1, 2));
    panelOptions.add(panelAnglesRNA);
    panelOptions.add(panelAnglesProtein);

    JPanel panelOkCancel = new JPanel();
    panelOkCancel.add(buttonOk);
    panelOkCancel.add(buttonCancel);

    setLayout(new BorderLayout());
    add(panelOptions, BorderLayout.CENTER);
    add(panelOkCancel, BorderLayout.SOUTH);
    pack();

    Dimension size = getSize();
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int x = screenSize.width - size.width;
    int y = screenSize.height - size.height;
    setLocation(x / 2, y / 2);
    setTitle("MCQ4Structures: torsion angle(s) selection");

    buttonOk.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            chosenOption = DialogSelectAngles.OK;
            selectedAngles.clear();
            selectedAngles.addAll(panelAnglesRNA.getSelected());
            selectedAngles.addAll(panelAnglesProtein.getSelected());
            dispose();
          }
        });

    buttonCancel.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            chosenOption = DialogSelectAngles.CANCEL;
            selectedAngles.clear();
            dispose();
          }
        });
  }

  public List<MasterTorsionAngleType> getAngles() {
    return Collections.unmodifiableList(selectedAngles);
  }

  private void setButtonOkState() {
    boolean enabled = panelAnglesRNA.isAnySelected() || panelAnglesProtein.isAnySelected();
    buttonOk.setEnabled(enabled);
  }

  public int showDialog() {
    setVisible(true);
    return chosenOption;
  }
}
