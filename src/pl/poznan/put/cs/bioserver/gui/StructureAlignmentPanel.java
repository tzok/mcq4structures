package pl.poznan.put.cs.bioserver.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.log4j.Logger;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.StructureImpl;
import org.biojava.bio.structure.align.gui.jmol.JmolPanel;

import pl.poznan.put.cs.bioserver.alignment.AlignmentOutput;
import pl.poznan.put.cs.bioserver.alignment.StructureAligner;
import pl.poznan.put.cs.bioserver.helper.Helper;
import pl.poznan.put.cs.bioserver.helper.PdbManager;

/**
 * A panel that contains all means necessary to align two structures.
 * 
 * @author tzok
 */
public class StructureAlignmentPanel extends JPanel implements
        PdbChangeListener {
    private class ActionListenerAlign implements ActionListener {
        private final class AlignerThread implements Runnable {
            private static final int ALIGNMENT_PDB_OUTPUTS = 4;
            private final Structure[] structures;

            AlignerThread(Structure[] structures) {
                this.structures = structures.clone();
            }

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
                    Helper.normalizeAtomNames(structures[0]);
                    Helper.normalizeAtomNames(structures[1]);

                    AlignmentOutput output = StructureAligner.align(
                            structures[0], structures[1]);
                    Structure[] aligned = output.getStructures();

                    File[] pdbFiles = new File[AlignerThread.ALIGNMENT_PDB_OUTPUTS];
                    for (int i = 0; i < AlignerThread.ALIGNMENT_PDB_OUTPUTS; i++) {
                        pdbFiles[i] = File.createTempFile("mcq", ".pdb");
                        try (FileOutputStream stream = new FileOutputStream(
                                pdbFiles[i])) {
                            String pdb = aligned[i].toPDB();
                            stream.write(pdb.getBytes(Charset.forName("UTF-8")));
                        }
                    }

                    alignmentShowPanel.jmolLeftPanel
                            .executeCmd(generateJmolScript(pdbFiles[0],
                                    pdbFiles[1]));
                    alignmentShowPanel.jmolRightPanel
                            .executeCmd(generateJmolScript(pdbFiles[2],
                                    pdbFiles[3]));
                } catch (StructureException | IOException e1) {
                    StructureAlignmentPanel.LOGGER.error(e1);
                    JOptionPane.showMessageDialog(getParent(), e1.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    timer.cancel();
                    try {
                        Thread.sleep(4 * StructureAlignmentPanel.PROCESSING_UPDATE_INTERVAL);
                    } catch (InterruptedException ex) {
                        // do nothing
                    }
                    settingsPanel.label.setText("Ready");
                }
            }
        }

        private Thread thread;
        private boolean isAllChainsMode;
        private Timer timer;

        public ActionListenerAlign(boolean isAllChainsMode) {
            this.isAllChainsMode = isAllChainsMode;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (thread != null && thread.isAlive()) {
                JOptionPane.showMessageDialog(null,
                        "The alignment is still being calculated", "Warning",
                        JOptionPane.WARNING_MESSAGE);
            }

            if (settingsPanel.pdbPanel.getListModel().size() != 2) {
                warning();
                return;
            }

            final Structure[] structures = PdbManager.getStructures(Collections
                    .list(settingsPanel.pdbPanel.getListModel().elements()));

            if (!isAllChainsMode) {
                Chain chains[] = new Chain[2];
                chains[0] = structures[0].getChain(settingsPanel.pdbPanel
                        .getComboBoxFirst().getSelectedIndex());
                chains[1] = structures[1].getChain(settingsPanel.pdbPanel
                        .getComboBoxSecond().getSelectedIndex());
                structures[0] = new StructureImpl(chains[0]);
                structures[1] = new StructureImpl(chains[1]);
            }

            boolean isRNA = Helper.isNucleicAcid(structures[0]);
            if (isRNA != Helper.isNucleicAcid(structures[1])) {
                String message = "Structures meant to be aligned "
                        + "represent different molecule types!";
                StructureAlignmentPanel.LOGGER.error(message);
                JOptionPane.showMessageDialog(null, message, "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            thread = new Thread(new AlignerThread(structures));
            thread.start();

            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                private int step;

                @Override
                public void run() {
                    StringBuilder builder = new StringBuilder();
                    builder.append("Processing");
                    for (int i = 0; i < step; i++) {
                        builder.append('.');
                    }
                    settingsPanel.label.setText(builder.toString());

                    step++;
                    if (step >= SettingsPanel.ButtonPanel.PROCESSING_MAX_STEP) {
                        step = 0;
                    }
                }
            }, 0, StructureAlignmentPanel.PROCESSING_UPDATE_INTERVAL);
        }
    }

    private static class AlignmentShowPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private JmolPanel jmolLeftPanel, jmolRightPanel;

        public AlignmentShowPanel() {
            setLayout(new GridLayout(1, 2));

            jmolLeftPanel = new JmolPanel();
            jmolRightPanel = new JmolPanel();

            add(jmolLeftPanel);
            add(jmolRightPanel);
        }
    }

    private class SettingsPanel extends JPanel {
        private class ButtonPanel extends JPanel {
            private static final long serialVersionUID = 1L;
            protected static final int PROCESSING_MAX_STEP = 5;
            private JButton buttonAddFile;
            private JButton buttonAlignChain;
            private JButton buttonAlignAll;

            public ButtonPanel() {
                super();
                buttonAddFile = new JButton("Load structure(s)");
                buttonAlignChain = new JButton("Align selected chain");
                buttonAlignAll = new JButton("Align all chains");

                buttonAlignChain.setEnabled(false);
                buttonAlignAll.setEnabled(false);

                add(buttonAddFile);
                add(buttonAlignChain);
                add(buttonAlignAll);
            }
        }

        private static final long serialVersionUID = 1L;
        private ButtonPanel buttonPanel;
        private PdbPanel pdbPanel;
        private JLabel label;

        public SettingsPanel() {
            super(new BorderLayout());
            buttonPanel = new ButtonPanel();
            pdbPanel = new PdbPanel(StructureAlignmentPanel.this);
            label = new JLabel("Ready");

            add(buttonPanel, BorderLayout.NORTH);
            add(pdbPanel, BorderLayout.CENTER);
            add(label, BorderLayout.SOUTH);
            label.setHorizontalAlignment(SwingConstants.CENTER);
        }
    }

    static final Logger LOGGER = Logger
            .getLogger(StructureAlignmentPanel.class);

    private static final int PROCESSING_UPDATE_INTERVAL = 250;

    private static final long serialVersionUID = 1L;
    private final JFileChooser chooser = new JFileChooser();
    private SettingsPanel settingsPanel;
    private AlignmentShowPanel alignmentShowPanel;

    public StructureAlignmentPanel() {
        super(new BorderLayout());

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

        settingsPanel.buttonPanel.buttonAlignChain
                .addActionListener(new ActionListenerAlign(false));
        settingsPanel.buttonPanel.buttonAlignAll
                .addActionListener(new ActionListenerAlign(true));
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

        if (settingsPanel.pdbPanel.getListModel().size() == 2) {
            settingsPanel.buttonPanel.buttonAlignAll.setEnabled(true);
            settingsPanel.buttonPanel.buttonAlignChain.setEnabled(true);
        }
        return true;
    }

    void warning() {
        JOptionPane.showMessageDialog(this,
                "You must select exactly two molecules", "Warning",
                JOptionPane.WARNING_MESSAGE);
    }

    @Override
    public void pdbListChanged() {
        settingsPanel.buttonPanel.buttonAlignAll.setEnabled(false);
        settingsPanel.buttonPanel.buttonAlignChain.setEnabled(false);
    }
}
