package pl.poznan.put.cs.bioserver.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

class TorsionAnglesSelectionDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    private final String[] namesAmino = new String[] { "Phi Φ", "Psi Ψ",
            "Omega Ω", "Average" };
    private final String[] namesNucleic = new String[] { "Alpha α", "Beta β",
            "Gamma γ", "Delta δ", "Epsilon ε", "Zeta ζ", "Chi χ", "Tau0 τ0",
            "Tau1 τ1", "Tau2 τ2", "Tau3 τ3", "Tau4 τ4", "P (sugar pucker)",
            "Average" };
    List<String> selectedNames;

    TorsionAnglesSelectionDialog(Frame owner) {
        super(owner, true);

        JPanel panelAnglesAmino = new JPanel();
        panelAnglesAmino.setLayout(new BoxLayout(panelAnglesAmino,
                BoxLayout.Y_AXIS));

        final JCheckBox[] checksAmino = new JCheckBox[namesAmino.length];
        for (int i = 0; i < namesAmino.length; i++) {
            JCheckBox checkBox = new JCheckBox(namesAmino[i]);
            checksAmino[i] = checkBox;
            panelAnglesAmino.add(checkBox);
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
        panelAmino.setBorder(BorderFactory.createTitledBorder("Amino acids"));

        JPanel panelAnglesNucleic = new JPanel();
        panelAnglesNucleic.setLayout(new BoxLayout(panelAnglesNucleic,
                BoxLayout.Y_AXIS));

        final JCheckBox[] checksNucleic = new JCheckBox[namesNucleic.length];
        for (int i = 0; i < namesNucleic.length; i++) {
            JCheckBox checkBox = new JCheckBox(namesNucleic[i]);
            checksNucleic[i] = checkBox;
            panelAnglesNucleic.add(checkBox);
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
        panelNucleic.setBorder(BorderFactory.createTitledBorder("Nucleotides"));

        JPanel panelOptions = new JPanel();
        panelOptions.setLayout(new GridLayout(1, 2));
        panelOptions.add(panelAmino);
        panelOptions.add(panelNucleic);

        JButton buttonOk = new JButton("OK");
        JButton buttonCancel = new JButton("Cancel");

        JPanel panelOkCancel = new JPanel();
        panelOkCancel.add(buttonOk);
        panelOkCancel.add(buttonCancel);

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
                selectedNames = new ArrayList<>();
                for (JCheckBox[] array : new JCheckBox[][] { checksAmino,
                        checksNucleic }) {
                    for (JCheckBox checkBox : array) {
                        if (checkBox.isSelected()) {
                            String text = checkBox.getText();
                            text = text.split(" ")[0];
                            text = text.toUpperCase();
                            selectedNames.add(text);
                        }
                    }
                }
                dispose();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedNames = null;
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

        setTitle("Torsion angles selection dialog");
    }
}
