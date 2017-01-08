package pl.poznan.put.gui.window;

import org.apache.commons.lang3.tuple.Pair;
import pl.poznan.put.gui.panel.ChainsPanel;
import pl.poznan.put.pdb.analysis.PdbChain;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.structure.tertiary.StructureManager;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public final class DialogSelectChains extends JDialog {
    public static final int CANCEL = 0;
    public static final int OK = 1;
    private static final Dimension INITIAL_MAIN_PANEL_SIZE =
            new Dimension(640, 480);
    private final JButton buttonOk = new JButton("OK");
    private final ActionListener actionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent arg0) {
            updateButtonOkState();
        }
    };
    private final ChainsPanel panelsChainsLeft =
            new ChainsPanel(actionListener);
    private final ChainsPanel panelsChainsRight =
            new ChainsPanel(actionListener);
    private final JButton buttonCancel = new JButton("Cancel");

    private List<PdbChain> chainsLeft = new ArrayList<>();
    private List<PdbChain> chainsRight = new ArrayList<>();
    private PdbModel structureLeft;
    private PdbModel structureRight;
    private int chosenOption;

    public DialogSelectChains(Frame owner) {
        super(owner, "MCQ4Structures: structure & chain selection", true);
        setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(1, 2));
        panel.setPreferredSize(DialogSelectChains.INITIAL_MAIN_PANEL_SIZE);
        panel.add(panelsChainsLeft);
        panel.add(panelsChainsRight);
        add(panel, BorderLayout.CENTER);

        panel = new JPanel();
        panel.add(buttonOk);
        panel.add(buttonCancel);
        add(panel, BorderLayout.SOUTH);
        pack();

        Dimension size = getSize();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = screenSize.width - size.width;
        int y = screenSize.height - size.height;
        setLocation(x / 2, y / 2);

        buttonOk.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                structureLeft = panelsChainsLeft.getSelectedStructure();
                structureRight = panelsChainsRight.getSelectedStructure();

                if (structureLeft == null || structureRight == null) {
                    chosenOption = DialogSelectChains.CANCEL;
                    dispose();
                    return;
                }

                chainsLeft = panelsChainsLeft.getSelectedChains();
                chainsRight = panelsChainsRight.getSelectedChains();

                if (chainsLeft.size() == 0 || chainsRight.size() == 0) {
                    chosenOption = DialogSelectChains.CANCEL;
                    dispose();
                    return;
                }

                chosenOption = DialogSelectChains.OK;
                dispose();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chosenOption = DialogSelectChains.CANCEL;
                dispose();
            }
        });
    }

    public Pair<List<PdbChain>, List<PdbChain>> getChains() {
        return Pair.of(chainsLeft, chainsRight);
    }

    public Pair<PdbModel, PdbModel> getStructures() {
        return Pair.of(structureLeft, structureRight);
    }

    public int showDialog() {
        List<PdbModel> structures = StructureManager.getAllStructures();
        panelsChainsLeft.reloadStructures(structures);
        panelsChainsRight.reloadStructures(structures);

        chosenOption = DialogSelectChains.CANCEL;
        updateButtonOkState();
        setVisible(true);
        return chosenOption;
    }

    private void updateButtonOkState() {
        boolean flag = DialogSelectChains.isAnyChainSelected(panelsChainsLeft);
        flag &= DialogSelectChains.isAnyChainSelected(panelsChainsRight);
        buttonOk.setEnabled(flag);
    }

    private static boolean isAnyChainSelected(ChainsPanel panelsChains) {
        for (JPanel panel : new JPanel[]{panelsChains.getRnaPanel(),
                                         panelsChains.getProteinPanel()}) {
            for (Component component : panel.getComponents()) {
                if (component instanceof JCheckBox && ((JCheckBox) component)
                        .isSelected()) {
                    return true;
                }
            }
        }

        return false;
    }
}
