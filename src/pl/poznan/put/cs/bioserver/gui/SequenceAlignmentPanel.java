package pl.poznan.put.cs.bioserver.gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collections;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Structure;
import org.biojava3.alignment.Alignments.PairwiseSequenceAlignerType;
import org.biojava3.core.sequence.compound.AminoAcidCompound;
import org.biojava3.core.sequence.compound.NucleotideCompound;
import org.jmol.util.Logger;

import pl.poznan.put.cs.bioserver.alignment.SequenceAligner;
import pl.poznan.put.cs.bioserver.helper.Helper;
import pl.poznan.put.cs.bioserver.helper.PdbManager;

/**
 * A panel in the main window that has all options related to sequence
 * alignment.
 * 
 * @author tzok
 */
public class SequenceAlignmentPanel extends JPanel {
    private final class AlignSequences implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (settingsPanel.pdbPanel.getListModel().size() != 2) {
                warning();
                return;
            }

            Structure[] structures = PdbManager.getStructures(Collections
                    .list(settingsPanel.pdbPanel.getListModel().elements()));
            Chain chains[] = new Chain[2];
            chains[0] = structures[0].getChain(settingsPanel.pdbPanel
                    .getComboBoxFirst().getSelectedIndex());
            chains[1] = structures[1].getChain(settingsPanel.pdbPanel
                    .getComboBoxSecond().getSelectedIndex());

            PairwiseSequenceAlignerType type;
            if (settingsPanel.buttonPanel.radioGlobal.isSelected()) {
                type = PairwiseSequenceAlignerType.GLOBAL;
            } else {
                type = PairwiseSequenceAlignerType.LOCAL;
            }

            boolean isRNA = Helper.isNucleicAcid(chains[0]);
            if (isRNA != Helper.isNucleicAcid(chains[1])) {
                String message = "Structures meant to be aligned "
                        + "represent different molecule types!";
                Logger.error(message);
                JOptionPane.showMessageDialog(null, message, "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (isRNA) {
                SequenceAligner<NucleotideCompound> aligner = new SequenceAligner<>(
                        NucleotideCompound.class);
                textArea.setText(aligner.alignSequences(chains[0], chains[1],
                        type).toString());
            } else {
                SequenceAligner<AminoAcidCompound> aligner = new SequenceAligner<>(
                        AminoAcidCompound.class);
                textArea.setText(aligner.alignSequences(chains[0], chains[1],
                        type).toString());
            }
        }
    }

    private static class ButtonPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private JButton buttonAddFile;
        private JButton buttonAlign;
        private JRadioButton radioGlobal;
        private JRadioButton radioLocal;

        public ButtonPanel() {
            super();
            buttonAddFile = new JButton("Add file");
            buttonAlign = new JButton("Align");
            radioGlobal = new JRadioButton("Global", true);
            radioLocal = new JRadioButton("Local");

            ButtonGroup group = new ButtonGroup();
            group.add(radioGlobal);
            group.add(radioLocal);

            add(buttonAddFile);
            add(buttonAlign);
            add(new JLabel("Alignment type:"));
            add(radioGlobal);
            add(radioLocal);
        }
    }

    private static class SettingsPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private ButtonPanel buttonPanel;
        private PdbPanel pdbPanel;

        public SettingsPanel() {
            super(new BorderLayout());
            buttonPanel = new ButtonPanel();
            pdbPanel = new PdbPanel();

            add(buttonPanel, BorderLayout.NORTH);
            add(pdbPanel, BorderLayout.SOUTH);
        }
    }

    private static final int FONT_SIZE = 20;

    private static final long serialVersionUID = 1L;
    private final JFileChooser chooser = new JFileChooser();
    private JTextArea textArea;
    private SettingsPanel settingsPanel;

    public SequenceAlignmentPanel() {
        super(new BorderLayout());

        settingsPanel = new SettingsPanel();
        textArea = new JTextArea();

        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN,
                SequenceAlignmentPanel.FONT_SIZE));
        textArea.setEditable(false);

        add(settingsPanel, BorderLayout.NORTH);
        add(textArea, BorderLayout.CENTER);

        chooser.addChoosableFileFilter(new FileNameExtensionFilter(
                "PDB file format", "pdb", "pdb1", "ent", "brk", "gz"));
        chooser.setMultiSelectionEnabled(true);

        settingsPanel.buttonPanel.buttonAddFile
                .addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
                            return;
                        }
                        for (File f : chooser.getSelectedFiles()) {
                            if (!addFile(f)) {
                                break;
                            }
                        }
                    }
                });

        settingsPanel.buttonPanel.buttonAlign
                .addActionListener(new AlignSequences());
    }

    boolean addFile(File path) {
        if (settingsPanel.pdbPanel.getListModel().size() >= 2) {
            warning();
            return false;
        }
        String absolutePath = path.getAbsolutePath();
        PdbManager.loadStructure(absolutePath);
        settingsPanel.pdbPanel.getListModel().addElement(absolutePath);
        settingsPanel.pdbPanel.refreshComboBoxes();
        return true;
    }

    void warning() {
        JOptionPane.showMessageDialog(this,
                "You must have exactly two molecules", "Warning",
                JOptionPane.WARNING_MESSAGE);
    }
}
