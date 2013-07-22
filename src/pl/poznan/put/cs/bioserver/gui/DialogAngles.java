package pl.poznan.put.cs.bioserver.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.eclipse.jdt.annotation.Nullable;

import pl.poznan.put.cs.bioserver.helper.Constants;

final class DialogAngles extends JDialog {
    private static final List<String> AMINO_CODES = Arrays.asList(new String[] {
            "PHI", "PSI", "OMEGA", "CALPHA", "CHI1", "CHI2", "CHI3", "CHI4",
            "CHI5", "SELECTED", "AVERAGE" });
    private static final List<String> AMINO_NAMES = Arrays.asList(new String[] {
            Constants.UNICODE_PHI + " (phi)", Constants.UNICODE_PSI + " (psi)",
            Constants.UNICODE_OMEGA + " (omega)",
            "C-" + Constants.UNICODE_ALPHA + " (C-alpha)", Constants.UNICODE_CHI + "1 (chi1)",
            Constants.UNICODE_CHI + "2 (chi2)", Constants.UNICODE_CHI + "3 (chi3)",
            Constants.UNICODE_CHI + "4 (chi4)", Constants.UNICODE_CHI + "5 (chi5)",
            "Average of selected angles", "Average of all angles" });
    private static DialogAngles instance;
    private static final List<String> NUCLEIC_CODES = Arrays
            .asList(new String[] { "ALPHA", "BETA", "GAMMA", "DELTA",
                    "EPSILON", "ZETA", "CHI", "TAU0", "TAU1", "TAU2", "TAU3",
                    "TAU4", "P", "ETA", "THETA", "ETA_PRIM", "THETA_PRIM",
                    "SELECTED", "AVERAGE" });
    private static final List<String> NUCLEIC_NAMES = Arrays
            .asList(new String[] { Constants.UNICODE_ALPHA + " (alpha)",
                    Constants.UNICODE_BETA + " (beta)", Constants.UNICODE_GAMMA + " (gamma)",
                    Constants.UNICODE_DELTA + " (delta)",
                    Constants.UNICODE_EPSILON + " (epsilon)",
                    Constants.UNICODE_ZETA + " (zeta)", Constants.UNICODE_CHI + " (chi)",
                    Constants.UNICODE_TAU + "0 (tau0)", Constants.UNICODE_TAU + "1 (tau1)",
                    Constants.UNICODE_TAU + "2 (tau2)", Constants.UNICODE_TAU + "3 (tau3)",
                    Constants.UNICODE_TAU + "4 (tau4)", "P (sugar pucker)",
                    Constants.UNICODE_ETA + " (eta)", Constants.UNICODE_THETA + " (theta)",
                    Constants.UNICODE_ETA + "' (eta')", Constants.UNICODE_THETA + "' (theta')",
                    "Average of selected angles", "Average of all angles" });

    private static final long serialVersionUID = 1L;

    public static DialogAngles getInstance(Frame owner) {
        if (DialogAngles.instance == null) {
            DialogAngles.instance = new DialogAngles(owner);
        }
        return DialogAngles.instance;
    }

    public static void selectAngles() {
        DialogAngles.instance.setVisible(true);
    }

    private List<String> selectedNames = Arrays
            .asList(new String[] { "AVERAGE" });

    private DialogAngles(Frame owner) {
        super(owner, true);

        JPanel panelAnglesAmino = new JPanel();
        panelAnglesAmino.setLayout(new BoxLayout(panelAnglesAmino,
                BoxLayout.Y_AXIS));

        final JCheckBox[] checksAmino =
                new JCheckBox[DialogAngles.AMINO_NAMES.size()];
        for (int i = 0; i < DialogAngles.AMINO_NAMES.size(); i++) {
            JCheckBox checkBox = new JCheckBox(DialogAngles.AMINO_NAMES.get(i));
            checksAmino[i] = checkBox;
            panelAnglesAmino.add(checkBox);
        }
        checksAmino[checksAmino.length - 1].setSelected(true);

        final JButton buttonSelectAllAmino = new JButton("Select all");
        final JButton buttonClearAmino = new JButton("Clear");

        JPanel panelButtonsAmino = new JPanel();
        panelButtonsAmino.add(buttonSelectAllAmino);
        panelButtonsAmino.add(buttonClearAmino);

        JPanel panelAmino = new JPanel();
        panelAmino.setLayout(new BorderLayout());
        panelAmino.add(panelAnglesAmino, BorderLayout.CENTER);
        panelAmino.add(panelButtonsAmino, BorderLayout.SOUTH);
        panelAmino.setBorder(BorderFactory.createTitledBorder("Amino acids"));

        JPanel panelAnglesNucleic = new JPanel();
        panelAnglesNucleic.setLayout(new BoxLayout(panelAnglesNucleic,
                BoxLayout.Y_AXIS));

        final JCheckBox[] checksNucleic =
                new JCheckBox[DialogAngles.NUCLEIC_NAMES.size()];
        for (int i = 0; i < DialogAngles.NUCLEIC_NAMES.size(); i++) {
            JCheckBox checkBox =
                    new JCheckBox(DialogAngles.NUCLEIC_NAMES.get(i));
            checksNucleic[i] = checkBox;
            panelAnglesNucleic.add(checkBox);
        }
        checksNucleic[checksNucleic.length - 1].setSelected(true);

        final JButton buttonSelectAllNucleic = new JButton("Select all");
        JButton buttonClearNucleic = new JButton("Clear");

        JPanel panelButtonsNucleic = new JPanel();
        panelButtonsNucleic.add(buttonSelectAllNucleic);
        panelButtonsNucleic.add(buttonClearNucleic);

        JPanel panelNucleic = new JPanel();
        panelNucleic.setLayout(new BorderLayout());
        panelNucleic.add(panelAnglesNucleic, BorderLayout.CENTER);
        panelNucleic.add(panelButtonsNucleic, BorderLayout.SOUTH);
        panelNucleic.setBorder(BorderFactory.createTitledBorder("Nucleotides"));

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
            public void actionPerformed(@Nullable ActionEvent arg0) {
                JCheckBox[] checkBoxes;
                boolean state;

                assert arg0 != null;
                Object source = arg0.getSource();
                if (source.equals(buttonSelectAllAmino)) {
                    checkBoxes = checksAmino;
                    state = true;
                } else if (source.equals(buttonClearAmino)) {
                    checkBoxes = checksAmino;
                    state = false;
                } else if (source.equals(buttonSelectAllNucleic)) {
                    checkBoxes = checksNucleic;
                    state = true;
                } else { // buttonClearNucleic
                    checkBoxes = checksNucleic;
                    state = false;
                }

                for (JCheckBox checkBox : checkBoxes) {
                    checkBox.setSelected(state);
                }
            }
        };
        buttonSelectAllAmino.addActionListener(actionListenerSelection);
        buttonClearAmino.addActionListener(actionListenerSelection);
        buttonSelectAllNucleic.addActionListener(actionListenerSelection);
        buttonClearNucleic.addActionListener(actionListenerSelection);

        buttonOk.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(@Nullable ActionEvent e) {
                JCheckBox[][] checkBoxes =
                        new JCheckBox[][] { checksAmino, checksNucleic };
                String[][] codes =
                        new String[][] {
                                DialogAngles.AMINO_CODES
                                        .toArray(new String[DialogAngles.AMINO_CODES
                                                .size()]),
                                DialogAngles.NUCLEIC_CODES
                                        .toArray(new String[DialogAngles.NUCLEIC_CODES
                                                .size()]) };

                LinkedHashSet<String> set = new LinkedHashSet<>();
                for (int i = 0; i < 2; i++) {
                    for (int j = 0; j < checkBoxes[i].length; j++) {
                        if (checkBoxes[i][j].isSelected()) {
                            set.add(codes[i][j]);
                        }
                    }
                }
                selectedNames = new ArrayList<>(set);
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

    public List<String> getAngles() {
        return selectedNames;
    }
}
