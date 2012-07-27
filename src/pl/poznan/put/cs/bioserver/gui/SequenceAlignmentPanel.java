
package pl.poznan.put.cs.bioserver.gui;

import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Structure;
import org.biojava3.alignment.Alignments.PairwiseSequenceAlignerType;
import org.biojava3.core.sequence.compound.AminoAcidCompound;
import org.biojava3.core.sequence.compound.NucleotideCompound;
import org.jmol.util.Logger;

import pl.poznan.put.cs.bioserver.alignment.SequenceAligner;
import pl.poznan.put.cs.bioserver.helper.Helper;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;

public class SequenceAlignmentPanel extends JPanel {
    public class ButtonPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        JButton mAddFileButton;
        JButton mAlignButton;
        JRadioButton mGlobalButton;
        JRadioButton mLocalButton;

        public ButtonPanel() {
            super();
            mAddFileButton = new JButton("Add file");
            mAlignButton = new JButton("Align");
            mGlobalButton = new JRadioButton("Global", true);
            mLocalButton = new JRadioButton("Local");

            ButtonGroup group = new ButtonGroup();
            group.add(mGlobalButton);
            group.add(mLocalButton);

            add(mAddFileButton);
            add(mAlignButton);
            add(new JLabel("Alignment type:"));
            add(mGlobalButton);
            add(mLocalButton);
        }
    }

    public class PDBPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        DefaultListModel mListModel;
        JList mList;
        DefaultComboBoxModel mComboBoxModels[] = new DefaultComboBoxModel[2];
        JComboBox mComboBoxes[] = new JComboBox[2];

        public PDBPanel() {
            super();

            mListModel = new DefaultListModel();
            mList = new JList(mListModel);
            for (int i = 0; i < 2; ++i) {
                mComboBoxModels[i] = new DefaultComboBoxModel();
                mComboBoxes[i] = new JComboBox(mComboBoxModels[i]);
            }

            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            c.gridwidth = 1;
            c.gridheight = 2;
            add(mList, c);
            c.gridx++;
            c.gridheight--;
            add(mComboBoxes[0], c);
            c.gridy++;
            add(mComboBoxes[1], c);

            mList.addKeyListener(new KeyListener() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                        int index = mList.getSelectedIndex();
                        mComboBoxModels[index].removeAllElements();
                        mListModel.remove(index);
                        refreshComboBoxes();
                    }
                }

                @Override
                public void keyReleased(KeyEvent e) {
                }

                @Override
                public void keyTyped(KeyEvent e) {
                }
            });
        }
    }

    public class SettingsPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        ButtonPanel mButtonPanel;
        PDBPanel mPdbPanel;

        public SettingsPanel() {
            super(new BorderLayout());
            mButtonPanel = new ButtonPanel();
            mPdbPanel = new PDBPanel();

            add(mButtonPanel, BorderLayout.NORTH);
            add(mPdbPanel, BorderLayout.SOUTH);
        }
    }

    private static final long serialVersionUID = 1L;
    final JFileChooser mChooser = new JFileChooser();
    PDBManager mManager;
    JTextArea mTextArea;
    SettingsPanel mSettingsPanel;

    public SequenceAlignmentPanel(PDBManager m) {
        super(new BorderLayout());
        mManager = m;

        mSettingsPanel = new SettingsPanel();
        mTextArea = new JTextArea();

        mTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 20));
        mTextArea.setEditable(false);

        add(mSettingsPanel, BorderLayout.NORTH);
        add(mTextArea, BorderLayout.CENTER);

        mChooser.addChoosableFileFilter(new FileNameExtensionFilter(
                "PDB file format", "pdb", "pdb1", "ent", "brk", "gz"));
        mChooser.setMultiSelectionEnabled(true);

        mSettingsPanel.mButtonPanel.mAddFileButton
                .addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        if (mChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
                            return;
                        for (File f : mChooser.getSelectedFiles())
                            if (!addFile(f)) {
                                break;
                            }
                    }
                });

        mSettingsPanel.mButtonPanel.mAlignButton
                .addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (mSettingsPanel.mPdbPanel.mListModel.size() != 2) {
                            warning();
                            return;
                        }

                        Structure[] structures = mManager
                                .getStructures(mSettingsPanel.mPdbPanel.mListModel
                                        .elements());
                        Chain chains[] = new Chain[2];
                        for (int i = 0; i < 2; ++i) {
                            chains[i] = structures[i]
                                    .getChain(mSettingsPanel.mPdbPanel.mComboBoxes[i]
                                            .getSelectedIndex());
                        }

                        PairwiseSequenceAlignerType type;
                        if (mSettingsPanel.mButtonPanel.mGlobalButton.isSelected()) {
                            type = PairwiseSequenceAlignerType.GLOBAL;
                        } else {
                            type = PairwiseSequenceAlignerType.LOCAL;
                        }

                        boolean isRNA = Helper.isNucleicAcid(chains[0]);
                        if (isRNA != Helper.isNucleicAcid(chains[1])) {
                            String message = "Structures meant to be aligned represent different molecule types!";
                            Logger.error(message);
                            JOptionPane.showMessageDialog(null, message,
                                    "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        if (isRNA) {
                            SequenceAligner<NucleotideCompound> aligner = new SequenceAligner<NucleotideCompound>(
                                    NucleotideCompound.class);
                            mTextArea.setText(aligner.alignSequences(chains[0],
                                    chains[1], type).toString());
                        } else {
                            SequenceAligner<AminoAcidCompound> aligner = new SequenceAligner<AminoAcidCompound>(
                                    AminoAcidCompound.class);
                            mTextArea.setText(aligner.alignSequences(chains[0],
                                    chains[1], type).toString());
                        }
                    }
                });
    }

    boolean addFile(File path) {
        if (mSettingsPanel.mPdbPanel.mListModel.size() >= 2) {
            warning();
            return false;
        }
        String absolutePath = path.getAbsolutePath();
        mManager.addStructure(absolutePath);
        mSettingsPanel.mPdbPanel.mListModel.addElement(absolutePath);

        refreshComboBoxes();
        return true;
    }

    void refreshComboBoxes() {
        mSettingsPanel.mPdbPanel.mComboBoxModels[0].removeAllElements();
        mSettingsPanel.mPdbPanel.mComboBoxModels[1].removeAllElements();

        Structure[] structures = mManager
                .getStructures(mSettingsPanel.mPdbPanel.mListModel.elements());
        for (int i = 0; i < mSettingsPanel.mPdbPanel.mListModel.getSize(); ++i) {
            for (Chain c : structures[i].getChains()) {
                mSettingsPanel.mPdbPanel.mComboBoxModels[i].addElement(c
                        .getChainID());
            }
        }
    }

    void warning() {
        JOptionPane.showMessageDialog(this,
                "You must have exactly two molecules", "Warning",
                JOptionPane.WARNING_MESSAGE);
    }
}
