package pl.poznan.put.cs.bioserver.gui;

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

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.eclipse.jdt.annotation.Nullable;

import pl.poznan.put.cs.bioserver.torsion.AminoAcidDihedral;
import pl.poznan.put.cs.bioserver.torsion.AngleAverageAll;
import pl.poznan.put.cs.bioserver.torsion.AngleAverageSelected;
import pl.poznan.put.cs.bioserver.torsion.AnglePseudophasePucker;
import pl.poznan.put.cs.bioserver.torsion.AngleType;
import pl.poznan.put.cs.bioserver.torsion.NucleotideDihedral;

final class DialogAngles extends JDialog {
    private static DialogAngles instance;
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

    private HashMap<String, AngleType> mapNameToAngle;
    private List<AngleType> selectedNames = Arrays
            .asList(new AngleType[] { AngleAverageAll.getInstance() });

    private DialogAngles(Frame owner) {
        super(owner, true);
        mapNameToAngle = new HashMap<>();

        final JPanel panelAnglesAmino = new JPanel();
        panelAnglesAmino.setLayout(new BoxLayout(panelAnglesAmino,
                BoxLayout.Y_AXIS));

        List<AngleType> angles = new ArrayList<>(AminoAcidDihedral.getAngles());
        angles.add(AngleAverageSelected.getInstance());
        angles.add(AngleAverageAll.getInstance());
        for (AngleType type : angles) {
            String name = type.getAngleDisplayName();
            JCheckBox checkBox = new JCheckBox(name);
            panelAnglesAmino.add(checkBox);
            if (type.equals(AngleAverageAll.getInstance())) {
                checkBox.setSelected(true);
            }
            mapNameToAngle.put(name, type);
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

        // PANEL NUCLEIC
        final JPanel panelAnglesNucleic = new JPanel();
        panelAnglesNucleic.setLayout(new BoxLayout(panelAnglesNucleic,
                BoxLayout.Y_AXIS));

        angles = new ArrayList<>(NucleotideDihedral.getAngles());
        angles.add(AnglePseudophasePucker.getInstance());
        angles.add(AngleAverageSelected.getInstance());
        angles.add(AngleAverageAll.getInstance());
        for (AngleType type : angles) {
            String name = type.getAngleDisplayName();
            JCheckBox checkBox = new JCheckBox(name);
            panelAnglesNucleic.add(checkBox);
            if (type.equals(AngleAverageAll.getInstance())) {
                checkBox.setSelected(true);
            }
            mapNameToAngle.put(name, type);
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
            @Override
            public void actionPerformed(@Nullable ActionEvent e) {
                selectedNames = new ArrayList<>();
                for (JPanel panel : new JPanel[] { panelAnglesAmino,
                        panelAnglesNucleic }) {
                    for (Component component : panel.getComponents()) {
                        if (component instanceof JCheckBox
                                && ((JCheckBox) component).isSelected()) {
                            selectedNames.add(mapNameToAngle
                                    .get(((JCheckBox) component).getText()));
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

    public List<AngleType> getAngles() {
        return selectedNames;
    }
}
