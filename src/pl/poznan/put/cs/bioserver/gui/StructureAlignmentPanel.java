package pl.poznan.put.cs.bioserver.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.StructureImpl;
import org.biojava.bio.structure.align.gui.jmol.JmolPanel;
import org.jmol.util.Logger;

import pl.poznan.put.cs.bioserver.alignment.StructureAligner;
import pl.poznan.put.cs.bioserver.helper.Helper;

public class StructureAlignmentPanel extends JPanel {
    private class ActionListenerAlign implements ActionListener {
        private Thread thread;
        boolean isAllChainsMode;

        public ActionListenerAlign(boolean isAllChainsMode) {
            this.isAllChainsMode = isAllChainsMode;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (thread != null && thread.isAlive())
                JOptionPane.showMessageDialog(null,
                        "The alignment is still being calculated", "Warning",
                        JOptionPane.WARNING_MESSAGE);

            if (settingsPanel.pdbPanel.listModel.size() != 2) {
                warning();
                return;
            }

            final Structure[] structures = pdbManager
                    .getStructures(settingsPanel.pdbPanel.listModel.elements());
            final Chain chains[] = new Chain[2];
            if (!isAllChainsMode) {
                chains[0] = structures[0]
                        .getChain(settingsPanel.pdbPanel.comboBoxFirst
                                .getSelectedIndex());
                chains[1] = structures[1]
                        .getChain(settingsPanel.pdbPanel.comboBoxSecond
                                .getSelectedIndex());

                boolean isRNA = Helper.isNucleicAcid(chains[0]);
                if (isRNA != Helper.isNucleicAcid(chains[1])) {
                    String message = "Structures meant to be aligned "
                            + "represent different molecule types!";
                    Logger.error(message);
                    JOptionPane.showMessageDialog(null, message, "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Helper.normalizeAtomNames(chains[0]);
                Helper.normalizeAtomNames(chains[1]);
            } else {
                Helper.normalizeAtomNames(structures[0]);
                Helper.normalizeAtomNames(structures[1]);
            }

            thread = new Thread(new Runnable() {
                public String generateJmolScript(File pdb1, File pdb2) {
                    StringBuilder builder = new StringBuilder();
                    builder.append("load FILES \"");
                    builder.append(pdb1);
                    builder.append("\" \"");
                    builder.append(pdb2);
                    builder.append("\"; ");
                    builder.append("frame 0.0; ");
                    builder.append("cartoon only; ");
                    builder.append("select model=1.1; color green; ");
                    builder.append("select model=2.1; color red; ");
                    return builder.toString();
                }

                @Override
                public void run() {
                    try {
                        Structure[] aligned = new StructureImpl[4];
                        if (!isAllChainsMode) {
                            Chain[] alignedChains = StructureAligner.align(
                                    chains[0], chains[1]);
                            for (int i = 0; i < 4; i++)
                                aligned[i] = new StructureImpl(alignedChains[i]);
                        } else
                            aligned = StructureAligner.align(structures[0],
                                    structures[1]);

                        File[] pdbFiles = new File[4];
                        for (int i = 0; i < 4; i++) {
                            pdbFiles[i] = File.createTempFile("mcq", ".pdb");
                            try (FileOutputStream stream = new FileOutputStream(
                                    pdbFiles[i])) {
                                String pdb = aligned[i].toPDB();
                                stream.write(pdb.getBytes());
                            }
                        }

                        alignmentShowPanel.jmolLeftPanel
                                .executeCmd(generateJmolScript(pdbFiles[0],
                                        pdbFiles[1]));
                        alignmentShowPanel.jmolRightPanel
                                .executeCmd(generateJmolScript(pdbFiles[2],
                                        pdbFiles[3]));
                    } catch (StructureException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    } finally {
                        settingsPanel.buttonPanel.timer.cancel();
                        try {
                            Thread.sleep(4 * StructureAlignmentPanel.PROCESSING_UPDATE_INTERVAL);
                        } catch (InterruptedException ex) {
                            // do nothing
                        }
                        settingsPanel.label.setText("Ready");
                    }
                }
            });
            thread.start();

            settingsPanel.buttonPanel.timer = new Timer();
            settingsPanel.buttonPanel.timer.scheduleAtFixedRate(
                    new TimerTask() {
                        private int step;

                        @Override
                        public void run() {
                            StringBuilder builder = new StringBuilder();
                            builder.append("Processing");
                            for (int i = 0; i < step; i++)
                                builder.append('.');
                            settingsPanel.label.setText(builder.toString());

                            step++;
                            if (step >= ButtonPanel.PROCESSING_MAX_STEP)
                                step = 0;
                        }
                    }, 0, StructureAlignmentPanel.PROCESSING_UPDATE_INTERVAL);
        }
    }

    public class AlignmentShowPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        JmolPanel jmolLeftPanel, jmolRightPanel;

        public AlignmentShowPanel() {
            setLayout(new GridLayout(1, 2));

            jmolLeftPanel = new JmolPanel();
            jmolRightPanel = new JmolPanel();

            add(jmolLeftPanel);
            add(jmolRightPanel);
        }
    }

    public class ButtonPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        protected static final int PROCESSING_MAX_STEP = 5;
        JButton buttonAddFile;
        JButton buttonAlignChain;
        JButton buttonAlignAll;
        Timer timer;

        public ButtonPanel() {
            super();
            buttonAddFile = new JButton("Add file");
            buttonAlignChain = new JButton("Align chain");
            buttonAlignAll = new JButton("Align all chains");

            add(buttonAddFile);
            add(buttonAlignChain);
            add(buttonAlignAll);
        }
    }

    public class PdbPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        DefaultListModel<String> listModel;
        JList<String> list;
        DefaultComboBoxModel<String> comboBoxModelFirst, comboBoxModelSecond;
        JComboBox<String> comboBoxFirst, comboBoxSecond;

        public PdbPanel() {
            super();

            listModel = new DefaultListModel<>();
            list = new JList<>(listModel);
            comboBoxModelFirst = new DefaultComboBoxModel<>();
            comboBoxModelSecond = new DefaultComboBoxModel<>();
            comboBoxFirst = new JComboBox<>(comboBoxModelFirst);
            comboBoxSecond = new JComboBox<>(comboBoxModelSecond);

            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            c.gridwidth = 1;
            c.gridheight = 2;
            add(list, c);
            c.gridx++;
            c.gridheight--;
            add(comboBoxFirst, c);
            c.gridy++;
            add(comboBoxSecond, c);

            list.addKeyListener(new KeyListener() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                        int index = list.getSelectedIndex();
                        if (index == 0)
                            comboBoxModelFirst.removeAllElements();
                        else
                            comboBoxModelSecond.removeAllElements();
                        listModel.remove(index);
                        refreshComboBoxes();
                    }
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    // do nothing
                }

                @Override
                public void keyTyped(KeyEvent e) {
                    // do nothing
                }
            });
        }
    }

    public class SettingsPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        ButtonPanel buttonPanel;
        PdbPanel pdbPanel;
        JLabel label;

        public SettingsPanel() {
            super(new BorderLayout());
            buttonPanel = new ButtonPanel();
            pdbPanel = new PdbPanel();
            label = new JLabel("Ready");

            add(buttonPanel, BorderLayout.NORTH);
            add(pdbPanel, BorderLayout.CENTER);
            add(label, BorderLayout.SOUTH);
            label.setHorizontalAlignment(SwingConstants.CENTER);
        }
    }

    private static final int PROCESSING_UPDATE_INTERVAL = 250;

    private static final long serialVersionUID = 1L;
    final JFileChooser chooser = new JFileChooser();
    PdbManager pdbManager;
    SettingsPanel settingsPanel;
    AlignmentShowPanel alignmentShowPanel;

    public StructureAlignmentPanel(PdbManager m) {
        super(new BorderLayout());
        pdbManager = m;

        settingsPanel = new SettingsPanel();
        alignmentShowPanel = new AlignmentShowPanel();

        add(settingsPanel, BorderLayout.NORTH);
        add(alignmentShowPanel, BorderLayout.CENTER);

        chooser.addChoosableFileFilter(new FileNameExtensionFilter(
                "PDB file format", "pdb", "pdb1", "ent", "brk", "gz"));
        chooser.setMultiSelectionEnabled(true);

        settingsPanel.buttonPanel.buttonAddFile
                .addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
                            return;
                        for (File f : chooser.getSelectedFiles())
                            if (!addFile(f))
                                break;
                    }
                });

        settingsPanel.buttonPanel.buttonAlignChain
                .addActionListener(new ActionListenerAlign(false));
        settingsPanel.buttonPanel.buttonAlignAll
                .addActionListener(new ActionListenerAlign(true));
    }

    boolean addFile(File path) {
        if (settingsPanel.pdbPanel.listModel.size() >= 2) {
            warning();
            return false;
        }
        String absolutePath = path.getAbsolutePath();
        pdbManager.addStructure(absolutePath);
        settingsPanel.pdbPanel.listModel.addElement(absolutePath);

        refreshComboBoxes();
        return true;
    }

    void refreshComboBoxes() {
        settingsPanel.pdbPanel.comboBoxModelFirst.removeAllElements();
        settingsPanel.pdbPanel.comboBoxModelSecond.removeAllElements();

        Structure[] structures = pdbManager
                .getStructures(settingsPanel.pdbPanel.listModel.elements());
        for (int i = 0; i < settingsPanel.pdbPanel.listModel.getSize(); ++i)
            for (Chain c : structures[i].getChains())
                if (i == 0)
                    settingsPanel.pdbPanel.comboBoxModelFirst.addElement(c
                            .getChainID());
                else
                    settingsPanel.pdbPanel.comboBoxModelSecond.addElement(c
                            .getChainID());

    }

    void warning() {
        JOptionPane.showMessageDialog(this,
                "You must have exactly two molecules", "Warning",
                JOptionPane.WARNING_MESSAGE);
    }
}
