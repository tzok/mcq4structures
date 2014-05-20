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

import pl.poznan.put.common.ChiTorsionAngleType;
import pl.poznan.put.common.MoleculeType;
import pl.poznan.put.common.TorsionAngle;
import pl.poznan.put.nucleic.PseudophasePuckerAngle;
import pl.poznan.put.nucleic.RNATorsionAngle;
import pl.poznan.put.protein.ProteinTorsionAngle;
import pl.poznan.put.utility.AverageAngle;

final class DialogAngles extends JDialog {
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

    private final List<TorsionAngle> selectedAngles = new ArrayList<>();
    private final Map<String, TorsionAngle> mapNameToAngleAmino = new HashMap<>();
    private final Map<String, TorsionAngle> mapNameToAngleNucleic = new HashMap<>();

    private DialogAngles(Frame owner) {
        super(owner, true);

        final JPanel panelAnglesAmino = new JPanel();
        panelAnglesAmino.setLayout(new BoxLayout(panelAnglesAmino,
                BoxLayout.Y_AXIS));

        List<TorsionAngle> angles = new ArrayList<>();
        angles.addAll(Arrays.asList(ProteinTorsionAngle.values()));
        angles.addAll(ChiTorsionAngleType.getChiTorsionAngles(MoleculeType.PROTEIN));
        angles.add(AverageAngle.getInstance(MoleculeType.PROTEIN));

        for (TorsionAngle angle : angles) {
            String displayName = angle.getDisplayName();
            JCheckBox checkBox = new JCheckBox(displayName);
            panelAnglesAmino.add(checkBox);
            mapNameToAngleAmino.put(displayName, angle);
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
        angles.addAll(ChiTorsionAngleType.getChiTorsionAngles(MoleculeType.RNA));
        angles.add(PseudophasePuckerAngle.getInstance());
        angles.add(AverageAngle.getInstance(MoleculeType.RNA));

        for (TorsionAngle angle : angles) {
            String displayName = angle.getDisplayName();
            JCheckBox checkBox = new JCheckBox(displayName);
            panelAnglesNucleic.add(checkBox);
            mapNameToAngleNucleic.put(displayName, angle);
        }

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

        JButton buttonOk = new JButton("OK");

        JPanel panelOkCancel = new JPanel();
        panelOkCancel.add(buttonOk);

        setLayout(new BorderLayout());
        add(panelOptions, BorderLayout.CENTER);
        add(panelOkCancel, BorderLayout.SOUTH);

        ActionListener actionListenerSelection = new ActionListener() {
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
                    }
                }
            }
        };
        buttonSelectAllAmino.addActionListener(actionListenerSelection);
        buttonClearAmino.addActionListener(actionListenerSelection);
        buttonSelectAllNucleic.addActionListener(actionListenerSelection);
        buttonClearNucleic.addActionListener(actionListenerSelection);

        buttonOk.addActionListener(new ActionListener() {
            @SuppressWarnings("synthetic-access")
            @Override
            public void actionPerformed(ActionEvent e) {
                List<TorsionAngle> selected = getAngles();
                selected.clear();

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
                            String displayName = ((JCheckBox) component).getText();
                            TorsionAngle torsionAngle = map.get(displayName);
                            selected.add(torsionAngle);
                        }
                    }
                }

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

    public List<TorsionAngle> getAngles() {
        return selectedAngles;
    }
}
