package pl.poznan.put.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.apache.commons.lang3.tuple.Pair;

import pl.poznan.put.common.MoleculeType;
import pl.poznan.put.nucleic.PseudophasePuckerAngle;
import pl.poznan.put.nucleic.RNATorsionAngle;
import pl.poznan.put.protein.ProteinTorsionAngle;
import pl.poznan.put.torsion.AverageAngle;
import pl.poznan.put.torsion.ChiTorsionAngleType;
import pl.poznan.put.torsion.TorsionAngle;

final class DialogAngles extends JDialog {
    private class AngleCheckBoxActionListener implements ActionListener {
        private final List<JCheckBox> checkBoxes;

        private AngleCheckBoxActionListener(List<JCheckBox> checkBoxes) {
            this.checkBoxes = checkBoxes;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();
            boolean isSelected = ((JCheckBox) source).isSelected();

            for (JCheckBox checkBox : checkBoxes) {
                handleCheckBox(isSelected, checkBox);

                if (checkBox.equals(checkBoxP)) {
                    for (JCheckBox tau : checkBoxTau) {
                        handleCheckBox(isSelected, tau);
                    }
                }
            }
        }

        private void handleCheckBox(boolean isSelected, JCheckBox checkBox) {
            if (isSelected) {
                checkBox.setSelected(true);
            }
            checkBox.setEnabled(!isSelected);
        }
    }

    private static DialogAngles instance;
    private static final long serialVersionUID = 1L;

    public static DialogAngles getInstance(Frame owner) {
        DialogAngles inst = DialogAngles.instance;
        if (inst == null) {
            inst = new DialogAngles(owner);
        }
        DialogAngles.instance = inst;
        return inst;
    }

    public static void selectAngles() {
        DialogAngles inst = DialogAngles.instance;
        if (inst != null) {
            inst.setVisible(true);
        }
    }

    private final List<Pair<TorsionAngle, JCheckBox>> anglesCheckBoxes = new ArrayList<>();
    private final List<TorsionAngle> selectedAngles = new ArrayList<>();
    private final Map<String, TorsionAngle> mapNameToAngleAmino = new HashMap<>();
    private final Map<String, TorsionAngle> mapNameToAngleNucleic = new HashMap<>();
    private final JButton buttonOk = new JButton("OK");

    private final AverageAngle mcqProtein = AverageAngle.getInstanceMainAngles(MoleculeType.PROTEIN);
    private final AverageAngle mcqRNA = AverageAngle.getInstanceMainAngles(MoleculeType.RNA);

    private JCheckBox[] checkBoxTau = new JCheckBox[5];
    private JCheckBox checkBoxP;
    private JCheckBox checkBoxMcqProtein;
    private JCheckBox checkBoxMcqRNA;

    private final ActionListener mainActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            setButtonOkState();
        }
    };

    private DialogAngles(Frame owner) {
        super(owner, true);

        final JPanel panelAnglesAmino = new JPanel();
        panelAnglesAmino.setLayout(new BoxLayout(panelAnglesAmino,
                BoxLayout.Y_AXIS));

        List<TorsionAngle> angles = new ArrayList<>();
        angles.addAll(Arrays.asList(ProteinTorsionAngle.values()));
        angles.addAll(Arrays.asList(ChiTorsionAngleType.getChiTorsionAngles(MoleculeType.PROTEIN)));
        angles.add(mcqProtein);
        angles.add(AverageAngle.getInstanceAllAngles(MoleculeType.PROTEIN));

        for (TorsionAngle angle : angles) {
            String displayName = angle.getLongDisplayName();
            JCheckBox checkBox = new JCheckBox(displayName);
            checkBox.addActionListener(mainActionListener);
            panelAnglesAmino.add(checkBox);
            mapNameToAngleAmino.put(displayName, angle);
            anglesCheckBoxes.add(Pair.of(angle, checkBox));

            if (angle.equals(mcqProtein)) {
                checkBoxMcqProtein = checkBox;
            }
        }

        final JButton buttonSelectAllAmino = new JButton("Select all");
        final JButton buttonClearAmino = new JButton("Clear");

        JPanel panelButtonsAmino = new JPanel();
        panelButtonsAmino.add(buttonSelectAllAmino);
        panelButtonsAmino.add(buttonClearAmino);

        JPanel panelAmino = new JPanel();
        panelAmino.setLayout(new BorderLayout());
        panelAmino.add(panelAnglesAmino, BorderLayout.CENTER);
        panelAmino.add(panelButtonsAmino, BorderLayout.SOUTH);
        panelAmino.setBorder(BorderFactory.createTitledBorder("Protein"));

        // PANEL NUCLEIC
        final JPanel panelAnglesNucleic = new JPanel();
        panelAnglesNucleic.setLayout(new BoxLayout(panelAnglesNucleic,
                BoxLayout.Y_AXIS));

        angles = new ArrayList<>();
        angles.addAll(Arrays.asList(RNATorsionAngle.values()));
        angles.addAll(Arrays.asList(ChiTorsionAngleType.getChiTorsionAngles(MoleculeType.RNA)));
        angles.add(PseudophasePuckerAngle.getInstance());
        angles.add(mcqRNA);
        angles.add(AverageAngle.getInstanceAllAngles(MoleculeType.RNA));

        for (TorsionAngle angle : angles) {
            String displayName = angle.getLongDisplayName();
            JCheckBox checkBox = new JCheckBox(displayName);
            checkBox.addActionListener(mainActionListener);
            panelAnglesNucleic.add(checkBox);
            mapNameToAngleNucleic.put(displayName, angle);
            anglesCheckBoxes.add(Pair.of(angle, checkBox));

            if (angle.equals(RNATorsionAngle.TAU0)) {
                checkBoxTau[0] = checkBox;
            } else if (angle.equals(RNATorsionAngle.TAU1)) {
                checkBoxTau[1] = checkBox;
            } else if (angle.equals(RNATorsionAngle.TAU2)) {
                checkBoxTau[2] = checkBox;
            } else if (angle.equals(RNATorsionAngle.TAU3)) {
                checkBoxTau[3] = checkBox;
            } else if (angle.equals(RNATorsionAngle.TAU4)) {
                checkBoxTau[4] = checkBox;
            } else if (angle.equals(PseudophasePuckerAngle.getInstance())) {
                checkBoxP = checkBox;
            } else if (angle.equals(mcqRNA)) {
                checkBoxMcqRNA = checkBox;
            }
        }

        checkBoxMcqProtein.addActionListener(new AngleCheckBoxActionListener(
                getCheckBoxes(mcqProtein)));
        checkBoxMcqRNA.addActionListener(new AngleCheckBoxActionListener(
                getCheckBoxes(mcqRNA)));
        checkBoxP.addActionListener(new AngleCheckBoxActionListener(
                Arrays.asList(checkBoxTau)));
        checkBoxMcqProtein.doClick();
        checkBoxMcqRNA.doClick();

        final JButton buttonSelectAllNucleic = new JButton("Select all");
        JButton buttonClearNucleic = new JButton("Clear");

        JPanel panelButtonsNucleic = new JPanel();
        panelButtonsNucleic.add(buttonSelectAllNucleic);
        panelButtonsNucleic.add(buttonClearNucleic);

        JPanel panelNucleic = new JPanel();
        panelNucleic.setLayout(new BorderLayout());
        panelNucleic.add(panelAnglesNucleic, BorderLayout.CENTER);
        panelNucleic.add(panelButtonsNucleic, BorderLayout.SOUTH);
        panelNucleic.setBorder(BorderFactory.createTitledBorder("RNA"));

        JPanel panelOptions = new JPanel();
        panelOptions.setLayout(new GridLayout(1, 2));
        panelOptions.add(panelNucleic);
        panelOptions.add(panelAmino);

        JPanel panelOkCancel = new JPanel();
        panelOkCancel.add(buttonOk);

        setLayout(new BorderLayout());
        add(panelOptions, BorderLayout.CENTER);
        add(panelOkCancel, BorderLayout.SOUTH);

        ActionListener selectClearActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                JPanel panel;
                boolean state;

                assert arg0 != null;
                Object source = arg0.getSource();
                if (source.equals(buttonSelectAllAmino)) {
                    panel = panelAnglesAmino;
                    state = true;
                } else if (source.equals(buttonClearAmino)) {
                    panel = panelAnglesAmino;
                    state = false;
                } else if (source.equals(buttonSelectAllNucleic)) {
                    panel = panelAnglesNucleic;
                    state = true;
                } else { // buttonClearNucleic
                    panel = panelAnglesNucleic;
                    state = false;
                }

                for (Component component : panel.getComponents()) {
                    if (component instanceof JCheckBox) {
                        ((JCheckBox) component).setSelected(state);
                        if (!state) {
                            component.setEnabled(true);
                        }
                    }
                }

                setButtonOkState();
            }
        };
        buttonSelectAllAmino.addActionListener(selectClearActionListener);
        buttonClearAmino.addActionListener(selectClearActionListener);
        buttonSelectAllNucleic.addActionListener(selectClearActionListener);
        buttonClearNucleic.addActionListener(selectClearActionListener);

        buttonOk.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<TorsionAngle> selected = new ArrayList<>();
                JPanel[] panels = new JPanel[] { panelAnglesAmino, panelAnglesNucleic };
                @SuppressWarnings("rawtypes")
                Map[] maps = new Map[] { mapNameToAngleAmino, mapNameToAngleNucleic };

                for (int i = 0; i < panels.length; i++) {
                    JPanel panel = panels[i];
                    @SuppressWarnings("unchecked")
                    Map<String, TorsionAngle> map = maps[i];
                    for (Component component : panel.getComponents()) {
                        if (component instanceof JCheckBox
                                && ((JCheckBox) component).isSelected()) {
                            String angleName = ((JCheckBox) component).getText();
                            TorsionAngle torsionAngle = map.get(angleName);
                            selected.add(torsionAngle);
                        }
                    }
                }

                selectedAngles.clear();
                selectedAngles.addAll(selected);
                dispose();
            }
        });

        pack();
        int width = getPreferredSize().width;
        int height = getPreferredSize().height;

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = screenSize.width - width;
        int y = screenSize.height - height;
        setSize(width, height);
        setLocation(x / 2, y / 2);

        setTitle("MCQ4Structures: torsion angle(s) selection");
    }

    public TorsionAngle[] getAngles() {
        return selectedAngles.toArray(new TorsionAngle[selectedAngles.size()]);
    }

    private List<JCheckBox> getCheckBoxes(AverageAngle averageAngle) {
        List<JCheckBox> checkBoxes = new ArrayList<>();

        for (TorsionAngle angle : averageAngle.getConsideredAngles()) {
            for (Pair<TorsionAngle, JCheckBox> pair : anglesCheckBoxes) {
                if (angle.equals(pair.getLeft())) {
                    checkBoxes.add(pair.getRight());
                    break;
                }
            }
        }

        return checkBoxes;
    }

    private void setButtonOkState() {
        for (Pair<TorsionAngle, JCheckBox> pair : anglesCheckBoxes) {
            if (pair.getKey() instanceof AverageAngle) {
                continue;
            }

            JCheckBox checkBox = pair.getValue();
            if (checkBox.isSelected()) {
                buttonOk.setEnabled(true);
                return;
            }
        }

        buttonOk.setEnabled(false);
    }
}
