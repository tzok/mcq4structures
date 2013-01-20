package pl.poznan.put.cs.bioserver.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListModel;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Structure;
import org.biojava3.alignment.Alignments.PairwiseSequenceAlignerType;
import org.biojava3.alignment.template.AlignedSequence;
import org.biojava3.alignment.template.PairwiseSequenceAligner;
import org.biojava3.alignment.template.SequencePair;
import org.biojava3.core.sequence.compound.AminoAcidCompound;
import org.biojava3.core.sequence.compound.NucleotideCompound;
import org.biojava3.core.sequence.template.Sequence;

import pl.poznan.put.cs.bioserver.alignment.SequenceAligner;
import pl.poznan.put.cs.bioserver.gui.helper.PdbChangeListener;
import pl.poznan.put.cs.bioserver.helper.Helper;
import pl.poznan.put.cs.bioserver.helper.PdbManager;

/**
 * A panel in the main window that has all options related to sequence
 * alignment.
 * 
 * @author tzok
 */
public class SequenceAlignmentPanel extends JPanel {
    private static final int FONT_SIZE = 20;
    private static final Logger LOGGER = Logger
            .getLogger(SequenceAlignmentPanel.class);
    private static final long serialVersionUID = 1L;

    public SequenceAlignmentPanel() {
        super();

        JButton buttonLoad = new JButton("Load structure(s)");
        final JButton buttonAlign = new JButton("Compute alignment");
        buttonAlign.setEnabled(false);

        final JRadioButton radioGlobal = new JRadioButton("Global", true);
        JRadioButton radioLocal = new JRadioButton("Local", false);
        ButtonGroup group = new ButtonGroup();
        group.add(radioGlobal);
        group.add(radioLocal);

        JPanel panelButtons = new JPanel();
        panelButtons.add(buttonLoad);
        panelButtons.add(buttonAlign);
        panelButtons.add(new JLabel("Alignment type:"));
        panelButtons.add(radioGlobal);
        panelButtons.add(radioLocal);

        final PdbPanel panelPdb = new PdbPanel(new PdbChangeListener() {
            @Override
            public void pdbListChanged() {
                buttonAlign.setEnabled(false);
            }
        });

        JPanel panelButtonsPdb = new JPanel();
        panelButtonsPdb.setLayout(new BorderLayout());
        panelButtonsPdb.add(panelButtons, BorderLayout.NORTH);
        panelButtonsPdb.add(panelPdb, BorderLayout.CENTER);

        JEditorPane editorPane = new JEditorPane();
        editorPane.setBackground(new Color(0, 0, 0, 0));
        editorPane.setContentType("text/html");
        editorPane.setEditable(false);
        editorPane
                .setText("Instructions:<ol>"
                        + "<li>Load structure(s) from files (PDB or mmCif)</li>"
                        + "<li>Select global (Smith-Waterman) or local (Needleman-Wunsch) alignment method</li>"
                        + "<li>Compute alignment</li></ol>"
                        + "Additional information:<ul>"
                        + "<li>Open penalty: 1</li>"
                        + "<li>Extension penalty: 10</li>"
                        + "<li>Substitution matrix for nucleic acids: NUC 4.4</li>"
                        + "<li>Substitution matrix for proteins: BLOSUM62</li></ul>");

        JPanel panelOptions = new JPanel();
        panelOptions.setLayout(new GridLayout(1, 2));
        panelOptions.add(panelButtonsPdb);
        panelOptions.add(editorPane);

        final JLabel labelScore = new JLabel();
        final JLabel labelSimilarity = new JLabel();
        final JLabel labelGaps = new JLabel();

        JPanel panelScore = new JPanel();
        panelScore.add(new JLabel("Score:"));
        panelScore.add(labelScore);
        panelScore.add(new JLabel("Similarity:"));
        panelScore.add(labelSimilarity);
        panelScore.add(new JLabel("Gaps:"));
        panelScore.add(labelGaps);

        JPanel panelOptionsScore = new JPanel();
        panelOptionsScore.setLayout(new BorderLayout());
        panelOptionsScore.add(panelOptions, BorderLayout.NORTH);
        panelOptionsScore.add(panelScore, BorderLayout.SOUTH);

        final JTextArea textArea = new JTextArea();
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN,
                SequenceAlignmentPanel.FONT_SIZE));
        textArea.setEditable(false);

        setLayout(new BorderLayout());
        add(panelOptionsScore, BorderLayout.NORTH);
        add(new JScrollPane(textArea), BorderLayout.CENTER);

        buttonLoad.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                panelPdb.loadStructuresWithOpenDialog();
                if (panelPdb.getListModel().size() >= 2) {
                    buttonAlign.setEnabled(true);
                }
            }
        });

        buttonAlign.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ListModel<File> model = panelPdb.getListModel();
                Structure[] structures = new Structure[] {
                        PdbManager.getStructure(model.getElementAt(0)),
                        PdbManager.getStructure(model.getElementAt(1)) };

                int chainIndexFirst = panelPdb.getComboBoxFirst()
                        .getSelectedIndex();
                int chainIndexSecond = panelPdb.getComboBoxSecond()
                        .getSelectedIndex();
                Chain chains[] = new Chain[] {
                        structures[0].getChain(chainIndexFirst),
                        structures[1].getChain(chainIndexSecond) };

                PairwiseSequenceAlignerType type;
                if (radioGlobal.isSelected()) {
                    type = PairwiseSequenceAlignerType.GLOBAL;
                } else {
                    type = PairwiseSequenceAlignerType.LOCAL;
                }

                boolean isRNA = Helper.isNucleicAcid(chains[0]);
                if (isRNA != Helper.isNucleicAcid(chains[1])) {
                    String message = "Structures meant to be aligned "
                            + "represent different molecule types!";
                    SequenceAlignmentPanel.LOGGER.error(message);
                    JOptionPane.showMessageDialog(null, message, "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int gaps, length, minScore, maxScore, score;
                double similarity;
                if (isRNA) {
                    SequenceAligner<NucleotideCompound> aligner = new SequenceAligner<>(
                            NucleotideCompound.class);
                    PairwiseSequenceAligner<Sequence<NucleotideCompound>, NucleotideCompound> sequenceAligner = aligner
                            .alignSequences(chains[0], chains[1], type);
                    SequencePair<Sequence<NucleotideCompound>, NucleotideCompound> pair = sequenceAligner
                            .getPair();

                    gaps = 0;
                    for (AlignedSequence<Sequence<NucleotideCompound>, NucleotideCompound> as : pair
                            .getAlignedSequences()) {
                        gaps += StringUtils.countMatches(
                                as.getSequenceAsString(), "-");
                    }
                    length = pair.getLength();
                    score = sequenceAligner.getScore();
                    minScore = sequenceAligner.getMinScore();
                    maxScore = sequenceAligner.getMaxScore();
                    similarity = sequenceAligner.getSimilarity();

                    textArea.setText(pair.toString());
                } else {
                    SequenceAligner<AminoAcidCompound> aligner = new SequenceAligner<>(
                            AminoAcidCompound.class);
                    PairwiseSequenceAligner<Sequence<AminoAcidCompound>, AminoAcidCompound> sequenceAligner = aligner
                            .alignSequences(chains[0], chains[1], type);
                    SequencePair<Sequence<AminoAcidCompound>, AminoAcidCompound> pair = sequenceAligner
                            .getPair();

                    gaps = 0;
                    for (AlignedSequence<Sequence<AminoAcidCompound>, AminoAcidCompound> as : pair
                            .getAlignedSequences()) {
                        gaps += StringUtils.countMatches(
                                as.getSequenceAsString(), "-");
                    }
                    length = pair.getLength();
                    score = sequenceAligner.getScore();
                    minScore = sequenceAligner.getMinScore();
                    maxScore = sequenceAligner.getMaxScore();
                    similarity = sequenceAligner.getSimilarity();

                    textArea.setText(pair.toString());
                }

                labelScore.setText(String.format("%d (min: %d, max: %d)",
                        score, minScore, maxScore));
                labelSimilarity.setText(String.format("%.0f%%",
                        100.0 * similarity));
                labelGaps.setText(String.format("%d/%d (%.0f%%)", gaps, length,
                        100.0 * gaps / length));
            }
        });
    }

    void warning() {
        JOptionPane.showMessageDialog(this, "You need exactly two structures",
                "Warning", JOptionPane.WARNING_MESSAGE);
    }
}