package pl.poznan.put.gui.window;

import org.apache.commons.lang3.tuple.Pair;
import pl.poznan.put.gui.panel.ChainsPanel;
import pl.poznan.put.pdb.analysis.PdbChain;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.structure.tertiary.StructureManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

final class DialogSelectChains extends JDialog {
  private static final Dimension INITIAL_MAIN_PANEL_SIZE = new Dimension(640, 480);
  private final JButton buttonOk = new JButton("OK");
  private final ActionListener actionListener = arg0 -> updateButtonOkState();
  private final ChainsPanel panelsChainsLeft = new ChainsPanel(actionListener);
  private final ChainsPanel panelsChainsRight = new ChainsPanel(actionListener);
  private List<PdbChain> chainsLeft = new ArrayList<>();
  private List<PdbChain> chainsRight = new ArrayList<>();
  private PdbModel structureLeft = null;
  private PdbModel structureRight = null;
  private OkCancelOption chosenOption = OkCancelOption.CANCEL;

  DialogSelectChains(final Frame owner) {
    super(owner, "MCQ4Structures: structure & chain selection", true);
    setLayout(new BorderLayout());

    final JPanel panel = new JPanel(new GridLayout(1, 2));
    panel.setPreferredSize(DialogSelectChains.INITIAL_MAIN_PANEL_SIZE);
    panel.add(panelsChainsLeft);
    panel.add(panelsChainsRight);
    add(panel, BorderLayout.CENTER);

    final JPanel buttonPanel = new JPanel();
    buttonPanel.add(buttonOk);
    final JButton buttonCancel = new JButton("Cancel");
    buttonPanel.add(buttonCancel);
    add(buttonPanel, BorderLayout.SOUTH);
    pack();

    final Dimension size = getSize();
    final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    final int x = screenSize.width - size.width;
    final int y = screenSize.height - size.height;
    setLocation(x / 2, y / 2);

    buttonOk.addActionListener(
        arg0 -> {
          structureLeft = panelsChainsLeft.getSelectedStructure();
          structureRight = panelsChainsRight.getSelectedStructure();

          if (structureLeft == null || structureRight == null) {
            chosenOption = OkCancelOption.CANCEL;
            dispose();
            return;
          }

          chainsLeft = panelsChainsLeft.getSelectedChains();
          chainsRight = panelsChainsRight.getSelectedChains();

          if (chainsLeft.isEmpty() || chainsRight.isEmpty()) {
            chosenOption = OkCancelOption.CANCEL;
            dispose();
            return;
          }

          chosenOption = OkCancelOption.OK;
          dispose();
        });

    buttonCancel.addActionListener(
        e -> {
          chosenOption = OkCancelOption.CANCEL;
          dispose();
        });
  }

  private static boolean isAnyChainSelected(final ChainsPanel panelsChains) {
    return Stream.of(panelsChains.getRnaPanel(), panelsChains.getProteinPanel())
        .flatMap(panel -> Arrays.stream(panel.getComponents()))
        .anyMatch(
            component ->
                component instanceof JCheckBox && ((AbstractButton) component).isSelected());
  }

  Pair<List<PdbChain>, List<PdbChain>> getChains() {
    return Pair.of(chainsLeft, chainsRight);
  }

  public Pair<PdbModel, PdbModel> getStructures() {
    return Pair.of(structureLeft, structureRight);
  }

  OkCancelOption showDialog() {
    final List<PdbModel> structures = StructureManager.getAllStructures();
    panelsChainsLeft.reloadStructures(structures);
    panelsChainsRight.reloadStructures(structures);

    chosenOption = OkCancelOption.CANCEL;
    updateButtonOkState();
    setVisible(true);
    return chosenOption;
  }

  private void updateButtonOkState() {
    boolean flag = DialogSelectChains.isAnyChainSelected(panelsChainsLeft);
    flag &= DialogSelectChains.isAnyChainSelected(panelsChainsRight);
    buttonOk.setEnabled(flag);
  }
}
