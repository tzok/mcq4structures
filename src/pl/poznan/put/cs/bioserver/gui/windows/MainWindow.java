package pl.poznan.put.cs.bioserver.gui.windows;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang3.StringUtils;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.StructureImpl;
import org.biojava.bio.structure.align.gui.jmol.JmolPanel;
import org.jmol.api.JmolViewer;

import pl.poznan.put.cs.bioserver.alignment.AlignerSequence;
import pl.poznan.put.cs.bioserver.alignment.AlignerStructure;
import pl.poznan.put.cs.bioserver.alignment.AlignmentOutput;
import pl.poznan.put.cs.bioserver.alignment.OutputAlignSeq;
import pl.poznan.put.cs.bioserver.comparison.ComparisonListener;
import pl.poznan.put.cs.bioserver.comparison.GlobalComparison;
import pl.poznan.put.cs.bioserver.comparison.MCQ;
import pl.poznan.put.cs.bioserver.comparison.RMSD;
import pl.poznan.put.cs.bioserver.comparison.TorsionLocalComparison;
import pl.poznan.put.cs.bioserver.gui.Clusterable;
import pl.poznan.put.cs.bioserver.gui.PdbChooser;
import pl.poznan.put.cs.bioserver.gui.TableModelGlobal;
import pl.poznan.put.cs.bioserver.gui.TableModelLocal;
import pl.poznan.put.cs.bioserver.gui.Visualizable;
import pl.poznan.put.cs.bioserver.helper.Exportable;
import pl.poznan.put.cs.bioserver.helper.Helper;
import pl.poznan.put.cs.bioserver.helper.PdbManager;
import pl.poznan.put.cs.bioserver.torsion.AngleDifference;

public class MainWindow extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final String TITLE = "MCQ4Structures: computing similarity "
            + "of 3D RNA / protein structures";

    private static final String CARD_MATRIX = "CARD_MATRIX";
    private static final String CARD_ALIGN_SEQ = "CARD_ALIGN_SEQ";
    private static final String CARD_ALIGN_STRUC = "CARD_ALIGN_STRUC";

    private JFileChooser chooserSaveFile;
    private DialogPdbs managerDialog;

    private Exportable exportableResults;
    private Thread threadAlignment;

    private JMenuItem itemOpen;
    private JMenuItem itemSave;
    private JCheckBoxMenuItem checkBoxManager;
    private JMenuItem itemExit;

    private JRadioButtonMenuItem radioGlobalMcq;
    private JRadioButtonMenuItem radioGlobalRmsd;
    private JRadioButtonMenuItem radioLocal;
    private JMenuItem itemSelectTorsion;
    private JMenuItem itemSelectStructuresCompare;
    private JMenuItem itemComputeDistances;
    private JMenuItem itemVisualise;
    private JMenuItem itemCluster;

    private JRadioButtonMenuItem radioAlignSeqGlobal;
    private JRadioButtonMenuItem radioAlignSeqLocal;
    private JRadioButtonMenuItem radioAlignStruc;
    private JMenuItem itemSelectStructuresAlign;
    private JMenuItem itemComputeAlign;

    private JMenuItem itemGuide;
    private JMenuItem itemAbout;

    private JLabel labelInfoMatrix;
    private JTable tableMatrix;
    private JProgressBar progressBar;
    private JPanel panelResultsMatrix;

    private JLabel labelInfoAlignSeq;
    private JTextArea textAreaAlignSeq;
    private JPanel panelResultsAlignSeq;

    private JLabel labelInfoAlignStruc;
    private JPanel panelAlignStrucInfo;
    private JmolPanel panelJmolLeft;
    private JmolPanel panelJmolRight;
    private JPanel panelResultsAlignStruc;

    private CardLayout layoutCards;
    private JPanel panelCards;

    public MainWindow() {
        super();

        chooserSaveFile = new JFileChooser();
        managerDialog = DialogPdbs.getInstance(this);
        managerDialog.setVisible(true);
        DialogStructures.getInstance(this);
        DialogChains.getInstance(this);
        DialogAngles.getInstance(this);

        /*
         * Create menu
         */
        JMenuBar menuBar = new JMenuBar();

        itemOpen = new JMenuItem("Open structure(s)",
                loadIcon("/toolbarButtonGraphics/general/Open16.gif"));
        itemSave = new JMenuItem("Save results",
                loadIcon("/toolbarButtonGraphics/general/Save16.gif"));
        itemSave.setEnabled(false);
        checkBoxManager = new JCheckBoxMenuItem("View structure manager", true);
        itemExit = new JMenuItem("Exit");
        JMenu menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        menu.add(itemOpen);
        menu.add(itemSave);
        menu.addSeparator();
        menu.add(checkBoxManager);
        menu.addSeparator();
        menu.add(itemExit);
        menuBar.add(menu);

        radioGlobalMcq = new JRadioButtonMenuItem("Global MCQ", true);
        radioGlobalRmsd = new JRadioButtonMenuItem("Global RMSD", false);
        radioLocal = new JRadioButtonMenuItem("Local distances", false);
        ButtonGroup group = new ButtonGroup();
        group.add(radioGlobalMcq);
        group.add(radioGlobalRmsd);
        group.add(radioLocal);

        itemSelectTorsion = new JMenuItem("Select torsion angles");
        itemSelectTorsion.setEnabled(false);
        itemSelectStructuresCompare = new JMenuItem(
                "Select structures to compare");
        itemComputeDistances = new JMenuItem("Compute distance(s)");
        itemComputeDistances.setEnabled(false);
        itemVisualise = new JMenuItem("Visualise results");
        itemVisualise.setEnabled(false);
        itemCluster = new JMenuItem("Cluster results");
        itemCluster.setEnabled(false);

        menu = new JMenu("Distance measure");
        menu.setMnemonic(KeyEvent.VK_D);
        menu.add(new JLabel("    Select distance type:"));
        menu.add(radioGlobalMcq);
        menu.add(radioGlobalRmsd);
        menu.add(radioLocal);
        menu.addSeparator();
        menu.add(itemSelectTorsion);
        menu.add(itemSelectStructuresCompare);
        menu.addSeparator();
        menu.add(itemComputeDistances);
        menu.add(itemVisualise);
        menu.add(itemCluster);
        menuBar.add(menu);

        radioAlignSeqGlobal = new JRadioButtonMenuItem(
                "Global sequence alignment", true);
        radioAlignSeqLocal = new JRadioButtonMenuItem(
                "Local sequence alignment", false);
        radioAlignStruc = new JRadioButtonMenuItem("3D structure alignment",
                false);
        ButtonGroup groupAlign = new ButtonGroup();
        groupAlign.add(radioAlignSeqGlobal);
        groupAlign.add(radioAlignSeqLocal);
        groupAlign.add(radioAlignStruc);

        itemSelectStructuresAlign = new JMenuItem("Select structures to align");
        itemComputeAlign = new JMenuItem("Compute alignment");
        itemComputeAlign.setEnabled(false);

        menu = new JMenu("Alignment");
        menu.setMnemonic(KeyEvent.VK_A);
        menu.add(new JLabel("    Select alignment type:"));
        menu.add(radioAlignSeqGlobal);
        menu.add(radioAlignSeqLocal);
        menu.add(radioAlignStruc);
        menu.addSeparator();
        menu.add(itemSelectStructuresAlign);
        menu.add(itemComputeAlign);
        menuBar.add(menu);

        itemGuide = new JMenuItem("Quick guide");
        itemAbout = new JMenuItem("About");

        menu = new JMenu("Help");
        menu.setMnemonic(KeyEvent.VK_H);
        menu.add(itemGuide);
        menu.add(itemAbout);
        menuBar.add(menu);

        setJMenuBar(menuBar);

        /*
         * Create panel with global comparison results
         */
        JPanel panel;
        labelInfoMatrix = new JLabel("Global/local comparison results: matrix");
        tableMatrix = new JTable();
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);

        panelResultsMatrix = new JPanel(new BorderLayout());
        panel = new JPanel(new BorderLayout());
        panel.add(labelInfoMatrix, BorderLayout.WEST);
        panelResultsMatrix.add(panel, BorderLayout.NORTH);
        panelResultsMatrix.add(new JScrollPane(tableMatrix),
                BorderLayout.CENTER);
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(new JLabel("Progress in computing:"));
        panel.add(progressBar);

        /*
         * Create panel with sequence alignment
         */
        labelInfoAlignSeq = new JLabel("Sequence alignment results");
        textAreaAlignSeq = new JTextArea();
        textAreaAlignSeq.setEditable(false);
        textAreaAlignSeq.setFont(new Font("Monospaced", Font.PLAIN, 20));

        panelResultsAlignSeq = new JPanel(new BorderLayout());
        panel = new JPanel(new BorderLayout());
        panel.add(labelInfoAlignSeq, BorderLayout.WEST);
        panelResultsAlignSeq.add(panel, BorderLayout.NORTH);
        panelResultsAlignSeq.add(new JScrollPane(textAreaAlignSeq),
                BorderLayout.CENTER);

        /*
         * Create panel with structure alignment
         */
        panelAlignStrucInfo = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 0.5;
        panelAlignStrucInfo.add(new JLabel("Whole structures (Jmol view)"),
                constraints);
        constraints.gridx++;
        constraints.weightx = 0;
        labelInfoAlignStruc = new JLabel("3D structure alignment results");
        panelAlignStrucInfo.add(labelInfoAlignStruc, constraints);
        constraints.gridx++;
        constraints.weightx = 0.5;
        panelAlignStrucInfo.add(new JLabel("Aligned fragments (Jmol view)"),
                constraints);

        panelJmolLeft = new JmolPanel();
        panelJmolLeft.executeCmd("background lightgrey; save state state_init");
        panelJmolRight = new JmolPanel();
        panelJmolRight.executeCmd("background darkgray; save state state_init");

        panelResultsAlignStruc = new JPanel(new BorderLayout());
        panelResultsAlignStruc.add(panelAlignStrucInfo, BorderLayout.NORTH);
        panel = new JPanel(new GridLayout(1, 2));
        panel.add(panelJmolLeft);
        panel.add(panelJmolRight);
        panelResultsAlignStruc.add(panel, BorderLayout.CENTER);

        /*
         * Create card layout
         */
        layoutCards = new CardLayout();
        panelCards = new JPanel();
        panelCards.setLayout(layoutCards);
        panelCards.add(new JPanel());
        panelCards.add(panelResultsMatrix, MainWindow.CARD_MATRIX);
        panelCards.add(panelResultsAlignSeq, MainWindow.CARD_ALIGN_SEQ);
        panelCards.add(panelResultsAlignStruc, MainWindow.CARD_ALIGN_STRUC);

        setLayout(new BorderLayout());
        add(panelCards, BorderLayout.CENTER);

        /*
         * Set window properties
         */
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle(MainWindow.TITLE);

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension size = toolkit.getScreenSize();
        setSize(size.width * 3 / 4, size.height * 3 / 4);
        setLocation(size.width / 8, size.height / 8);

        /*
         * Set action listeners
         */
        managerDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                checkBoxManager.setSelected(false);
            }
        });

        itemOpen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File[] files = PdbChooser.getSelectedFiles(MainWindow.this);
                for (File f : files) {
                    DialogPdbs.loadStructure(f);
                }
            }
        });

        itemSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooserSaveFile.setSelectedFile(exportableResults.suggestName());
                int option = chooserSaveFile.showSaveDialog(MainWindow.this);
                if (option == JFileChooser.APPROVE_OPTION) {
                    exportableResults.export(chooserSaveFile.getSelectedFile());
                }
            }
        });

        checkBoxManager.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                managerDialog.setVisible(checkBoxManager.isSelected());
            }
        });

        itemExit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispatchEvent(new WindowEvent(MainWindow.this,
                        WindowEvent.WINDOW_CLOSING));
            }
        });

        ActionListener radioActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                Object source = arg0.getSource();
                itemSelectTorsion.setEnabled(source.equals(radioLocal));
                itemComputeDistances.setEnabled(false);
                itemVisualise.setEnabled(false);
                itemCluster.setEnabled(false);
            }
        };
        radioGlobalMcq.addActionListener(radioActionListener);
        radioGlobalRmsd.addActionListener(radioActionListener);
        radioLocal.addActionListener(radioActionListener);

        itemSelectTorsion.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                DialogAngles.selectAngles();
            }
        });

        ActionListener selectActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object source = e.getSource();
                if (source.equals(itemSelectStructuresCompare)
                        && !radioLocal.isSelected()) {
                    selectStructures();
                } else {
                    selectChains(source);
                }
            }
        };
        itemSelectStructuresCompare.addActionListener(selectActionListener);
        itemSelectStructuresAlign.addActionListener(selectActionListener);

        itemComputeDistances.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (radioGlobalMcq.isSelected() || radioGlobalRmsd.isSelected()) {
                    compareGlobally();
                } else {
                    compareLocally();
                }
            }
        });

        itemVisualise.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Visualizable visualizable = (Visualizable) tableMatrix
                        .getModel();
                visualizable.visualize();
            }
        });

        itemCluster.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                Clusterable clusterable = (Clusterable) tableMatrix.getModel();
                clusterable.cluster();
            }
        });

        itemComputeAlign.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (radioAlignSeqGlobal.isSelected()
                        || radioAlignSeqLocal.isSelected()) {
                    alignSequences();
                } else {
                    alignStructures();
                }
            }
        });

        itemGuide.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DialogGuide dialog = new DialogGuide(MainWindow.this);
                dialog.setVisible(true);
            }
        });

        itemAbout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DialogAbout dialog = new DialogAbout(MainWindow.this);
                dialog.setVisible(true);
            }
        });
    }

    private void alignSequences() {
        Chain[][] chains = DialogChains.getChains();
        boolean isRNA = Helper.isNucleicAcid(chains[0][0]);
        if (isRNA != Helper.isNucleicAcid(chains[1][0])) {
            JOptionPane.showMessageDialog(this, "Cannot align structures: "
                    + "different molecular types", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        OutputAlignSeq alignment = AlignerSequence.align(chains[0][0],
                chains[1][0], radioAlignSeqGlobal.isSelected());
        exportableResults = alignment;

        File[] pdbs = DialogChains.getFiles();
        labelInfoAlignSeq.setText("Sequence alignment results for "
                + PdbManager.getName(pdbs[0]) + " and "
                + PdbManager.getName(pdbs[1]));
        textAreaAlignSeq.setText(alignment.toString());

        itemSave.setEnabled(true);
        itemSave.setText("Save results (TXT)");
    }

    private void alignStructures() {
        if (threadAlignment != null && threadAlignment.isAlive()) {
            JOptionPane.showMessageDialog(null,
                    "3D structure alignment computation has not "
                            + "finished yet!", "Information",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        final Structure[] structures = new Structure[2];
        final File[] files = DialogChains.getFiles();
        Chain[][] chains = DialogChains.getChains();
        for (int i = 0; i < 2; i++) {
            structures[i] = new StructureImpl();
            structures[i].setChains(Arrays.asList(chains[i]));
            structures[i].setPDBCode(PdbManager.getName(files[i]));
        }

        boolean isRNA = Helper.isNucleicAcid(structures[0]);
        if (isRNA != Helper.isNucleicAcid(structures[1])) {
            JOptionPane.showMessageDialog(this, "Cannot align structures: "
                    + "different molecular types", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        labelInfoAlignStruc.setText("Processing");
        final Timer timer = new Timer(250, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                String text = labelInfoAlignStruc.getText();
                int count = StringUtils.countMatches(text, ".");
                if (count < 5) {
                    labelInfoAlignStruc.setText(text + ".");
                } else {
                    labelInfoAlignStruc.setText("Processing");
                }
            }
        });
        timer.start();

        threadAlignment = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Helper.normalizeAtomNames(structures[0]);
                    Helper.normalizeAtomNames(structures[1]);

                    AlignmentOutput output = AlignerStructure.align(
                            structures[0], structures[1]);
                    exportableResults = output;

                    final Structure[] aligned = output.getStructures();
                    SwingUtilities.invokeLater(new Runnable() {
                        private static final String JMOL_SCRIPT = "frame 0.0; "
                                + "cartoon only; "
                                + "select model=1.1; color green; "
                                + "select model=1.2; color red; ";

                        @Override
                        public void run() {
                            StringBuilder builder = new StringBuilder();
                            builder.append("MODEL        1                                                                  \n");
                            builder.append(aligned[0].toPDB());
                            builder.append("ENDMDL                                                                          \n");
                            builder.append("MODEL        2                                                                  \n");
                            builder.append(aligned[1].toPDB());
                            builder.append("ENDMDL                                                                          \n");

                            JmolViewer viewer = panelJmolLeft.getViewer();
                            viewer.openStringInline(builder.toString());
                            panelJmolLeft.executeCmd(JMOL_SCRIPT);

                            builder = new StringBuilder();
                            builder.append("MODEL        1                                                                  \n");
                            builder.append(aligned[2].toPDB());
                            builder.append("ENDMDL                                                                          \n");
                            builder.append("MODEL        2                                                                  \n");
                            builder.append(aligned[3].toPDB());
                            builder.append("ENDMDL                                                                          \n");

                            viewer = panelJmolRight.getViewer();
                            viewer.openStringInline(builder.toString());
                            panelJmolRight.executeCmd(JMOL_SCRIPT);

                            File[] pdbs = DialogChains.getFiles();
                            labelInfoAlignStruc.setText("3D structure "
                                    + "alignments results for "
                                    + PdbManager.getName(pdbs[0]) + " and "
                                    + PdbManager.getName(pdbs[1]));

                            itemSave.setEnabled(true);
                            itemSave.setText("Save results (PDB)");
                        }
                    });
                } catch (StructureException e1) {
                    JOptionPane.showMessageDialog(MainWindow.this,
                            e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    timer.stop();
                }
            }
        });
        threadAlignment.start();
    }

    private void compareGlobally() {
        final GlobalComparison comparison;
        if (radioGlobalMcq.isSelected()) {
            comparison = new MCQ();
        } else { // radioRmsd.isSelected() == true
            comparison = new RMSD();
        }

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                final File[] files = DialogStructures.getFiles();
                Structure[] structures = PdbManager.getStructures(files);

                final double[][] matrix = comparison.compare(structures,
                        new ComparisonListener() {
                            @Override
                            public void stateChanged(long all, long completed) {
                                progressBar.setMaximum((int) all);
                                progressBar.setValue((int) completed);
                            }
                        });

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        String[] names = PdbManager.getNames(files);
                        TableModelGlobal model = new TableModelGlobal(names,
                                matrix, comparison);
                        exportableResults = model;
                        tableMatrix.setModel(model);

                        itemSave.setEnabled(true);
                        itemSave.setText("Save results (CSV)");
                        itemVisualise.setEnabled(true);
                        itemCluster.setEnabled(true);

                        labelInfoMatrix.setText("Global comparison results: "
                                + "distance matrix for "
                                + (radioGlobalMcq.isSelected() ? "MCQ" : "RMSD"));
                    }
                });
            }
        });
        thread.start();
    }

    private void compareLocally() {
        final Structure[] structures = new Structure[2];
        for (int i = 0; i < 2; i++) {
            structures[i] = new StructureImpl();
            structures[i].setChains(Arrays.asList(DialogChains.getChains()[i]));
        }

        try {
            Map<String, List<AngleDifference>> result = TorsionLocalComparison
                    .compare(structures[0], structures[1], false);

            File[] files = DialogChains.getFiles();
            TableModelLocal model = new TableModelLocal(result,
                    DialogAngles.getAngles(), PdbManager.getNames(files));
            exportableResults = model;
            tableMatrix.setModel(model);

            itemSave.setEnabled(true);
            itemSave.setText("Save results (CSV)");
            itemVisualise.setEnabled(true);
            itemCluster.setEnabled(false);

            labelInfoMatrix.setText("Local comparison results for: "
                    + PdbManager.getName(files[0]) + " and "
                    + PdbManager.getName(files[1]));
        } catch (StructureException e1) {
            JOptionPane.showMessageDialog(MainWindow.this, e1.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private ImageIcon loadIcon(String name) {
        URL resource = getClass().getResource(name);
        if (resource == null) {
            return null;
        }
        return new ImageIcon(resource);
    }

    private void selectChains(Object source) {
        if (DialogChains.showDialog() != DialogChains.OK) {
            return;
        }
        File[] structures = DialogChains.getFiles();
        Chain[][] chains = DialogChains.getChains();
        for (int i = 0; i < 2; i++) {
            if (chains[i].length == 0) {
                String message = "No chains specified for structure: "
                        + structures[i];
                JOptionPane.showMessageDialog(MainWindow.this, message,
                        "Information", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }

        if (source.equals(itemSelectStructuresCompare)) {
            tableMatrix.setModel(new DefaultTableModel());
            layoutCards.show(panelCards, MainWindow.CARD_MATRIX);

            itemComputeDistances.setEnabled(true);
            itemVisualise.setEnabled(false);
            itemCluster.setEnabled(false);
            itemComputeAlign.setEnabled(false);
        } else if (radioAlignSeqGlobal.isSelected()
                || radioAlignSeqLocal.isSelected()) {
            if (chains[0].length != 1 || chains[1].length != 1) {
                JOptionPane.showMessageDialog(MainWindow.this,
                        "A single chain should be " + "selected from each "
                                + "structure in " + "sequence alignment.",
                        "Information", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            textAreaAlignSeq.setText("");
            layoutCards.show(panelCards, MainWindow.CARD_ALIGN_SEQ);

            itemComputeDistances.setEnabled(false);
            itemVisualise.setEnabled(false);
            itemCluster.setEnabled(false);
            itemComputeAlign.setEnabled(true);
        } else { // source.equals(itemSelectChainsAlignStruc)
            panelJmolLeft.executeCmd("restore state " + "state_init");
            panelJmolRight.executeCmd("restore state " + "state_init");
            layoutCards.show(panelCards, MainWindow.CARD_ALIGN_STRUC);

            itemComputeDistances.setEnabled(false);
            itemVisualise.setEnabled(false);
            itemCluster.setEnabled(false);
            itemComputeAlign.setEnabled(true);
        }
    }

    private void selectStructures() {
        if (DialogStructures.showDialog() != DialogStructures.OK) {
            return;
        }
        File[] files = DialogStructures.getFiles();
        if (files == null || files.length < 2) {
            JOptionPane.showMessageDialog(MainWindow.this, "At "
                    + "least two structures must be selected to "
                    + "compute global distance", "Information",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        layoutCards.show(panelCards, MainWindow.CARD_MATRIX);
        tableMatrix.setModel(new DefaultTableModel());
        itemSave.setEnabled(false);
        itemComputeDistances.setEnabled(true);
    }
}
