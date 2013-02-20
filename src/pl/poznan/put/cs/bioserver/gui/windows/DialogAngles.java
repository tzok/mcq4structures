package pl.poznan.put.cs.bioserver.gui.windows;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

class DialogAngles extends JDialog {
    private static final long serialVersionUID = 1L;
    private static DialogAngles INSTANCE;
    private static final String[] AMINO_NAMES = new String[] { "Φ (phi)",
            "Ψ (psi)", "Ω (omega)", "Average" };
    private static final String[] AMINO_CODES = new String[] { "PHI", "PSI",
            "OMEGA", "AVERAGE" };
    private static final String[] NUCLEIC_NAMES = new String[] { "α (alpha)",
            "β (beta)", "γ (gamma)", "δ (delta)", "ε (epsilon)", "ζ (zeta)",
            "χ (chi)", "τ0 (tau0)", "τ1 (tau1)", "τ2 (tau2)", "τ3 (tau3)",
            "τ4 (tau4)", "P (sugar pucker)", "Average" };
    private static final String[] NUCLEIC_CODES = new String[] { "ALPHA",
            "BETA", "GAMMA", "DELTA", "EPSILON", "ZETA", "CHI", "TAU0", "TAU1",
            "TAU2", "TAU3", "TAU4", "P", "AVERAGE" };

    public static DialogAngles getInstance(Frame owner) {
        if (DialogAngles.INSTANCE == null) {
            DialogAngles.INSTANCE = new DialogAngles(owner);
        }
        return DialogAngles.INSTANCE;
    }

    private static String[] selectedNames = new String[] { "AVERAGE" };

    private DialogAngles(Frame owner) {
        super(owner, true);

        JPanel panelAnglesAmino = new JPanel();
        panelAnglesAmino.setLayout(new BoxLayout(panelAnglesAmino,
                BoxLayout.Y_AXIS));

        final JCheckBox[] checksAmino = new JCheckBox[DialogAngles.AMINO_NAMES.length];
        for (int i = 0; i < DialogAngles.AMINO_NAMES.length; i++) {
            JCheckBox checkBox = new JCheckBox(DialogAngles.AMINO_NAMES[i]);
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

        final JCheckBox[] checksNucleic = new JCheckBox[DialogAngles.NUCLEIC_NAMES.length];
        for (int i = 0; i < DialogAngles.NUCLEIC_NAMES.length; i++) {
            JCheckBox checkBox = new JCheckBox(DialogAngles.NUCLEIC_NAMES[i]);
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
        // FIXME: Cancel button necessary?
        // JButton buttonCancel = new JButton("Cancel");

        JPanel panelOkCancel = new JPanel();
        panelOkCancel.add(buttonOk);
        // panelOkCancel.add(buttonCancel);

        setLayout(new BorderLayout());
        add(panelOptions, BorderLayout.CENTER);
        add(panelOkCancel, BorderLayout.SOUTH);

        ActionListener actionListenerSelection = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                JCheckBox[] checkBoxes;
                boolean state;

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
            public void actionPerformed(ActionEvent e) {
                JCheckBox[][] checkBoxes = new JCheckBox[][] { checksAmino,
                        checksNucleic };
                String[][] codes = new String[][] { DialogAngles.AMINO_CODES,
                        DialogAngles.NUCLEIC_CODES };

                LinkedHashSet<String> set = new LinkedHashSet<>();
                for (int i = 0; i < 2; i++) {
                    for (int j = 0; j < checkBoxes[i].length; j++) {
                        if (checkBoxes[i][j].isSelected()) {
                            set.add(codes[i][j]);
                        }
                    }
                }
                List<String> list = new ArrayList<>(set);
                DialogAngles.selectedNames = list.toArray(new String[list
                        .size()]);

                dispose();
            }
        });

        // buttonCancel.addActionListener(new ActionListener() {
        // @Override
        // public void actionPerformed(ActionEvent e) {
        // selectedNames = null;
        // dispose();
        // }
        // });

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

    public static String[] getAngles() {
        return DialogAngles.selectedNames;
    }
}
