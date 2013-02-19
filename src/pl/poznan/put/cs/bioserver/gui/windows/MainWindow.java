package pl.poznan.put.cs.bioserver.gui.windows;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
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

import org.apache.commons.lang3.StringUtils;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.StructureImpl;
import org.biojava.bio.structure.align.gui.jmol.JmolPanel;
import org.jmol.api.JmolViewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.poznan.put.cs.bioserver.alignment.AlignmentOutput;
import pl.poznan.put.cs.bioserver.alignment.OutputAlignSeq;
import pl.poznan.put.cs.bioserver.alignment.SequenceAligner;
import pl.poznan.put.cs.bioserver.alignment.StructureAligner;
import pl.poznan.put.cs.bioserver.comparison.ComparisonListener;
import pl.poznan.put.cs.bioserver.comparison.GlobalComparison;
import pl.poznan.put.cs.bioserver.comparison.MCQ;
import pl.poznan.put.cs.bioserver.comparison.RMSD;
import pl.poznan.put.cs.bioserver.comparison.TorsionLocalComparison;
import pl.poznan.put.cs.bioserver.gui.PdbFileChooser;
import pl.poznan.put.cs.bioserver.gui.TableModelGlobal;
import pl.poznan.put.cs.bioserver.gui.TableModelLocal;
import pl.poznan.put.cs.bioserver.gui.Visualizable;
import pl.poznan.put.cs.bioserver.helper.Exportable;
import pl.poznan.put.cs.bioserver.helper.Helper;
import pl.poznan.put.cs.bioserver.helper.PdbManager;
import pl.poznan.put.cs.bioserver.torsion.AngleDifference;

public class MainWindow extends JFrame {
    private static final String TITLE = "MCQ4Structures: computing similarity of 3D RNA / protein structures";
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory
            .getLogger(MainWindow.class);

    private static final char CSV_DELIMITER = ';';

    private static final String CARD_GLOBAL = "CARD_GLOBAL";
    private static final String CARD_LOCAL = "CARD_LOCAL";
    private static final String CARD_ALIGN_SEQ = "CARD_ALIGN_SEQ";
    private static final String CARD_ALIGN_STRUC = "CARD_ALIGN_STRUC";

    private static Component getCurrentCard(JPanel panel) {
        for (Component component : panel.getComponents()) {
            if (component.isVisible()) {
                return component;
            }
        }
        return null;
    }

    private JFileChooser chooserSaveFile;
    private DialogPdbs managerDialog;
    private DialogAngles torsionDialog;

    private String[] resultGlobalNames;
    private double[][] resultGlobalMatrix;
    private Map<String, List<AngleDifference>> resultLocal;
    private String resultAlignStruc;
    private String resultAlignSeq;

    public MainWindow() {
        super();

        chooserSaveFile = new JFileChooser();
        managerDialog = DialogPdbs.getInstance(this);
        managerDialog.setVisible(true);
        DialogStructures.getInstance(this);
        DialogChains.getInstance(this);
        torsionDialog = DialogAngles.getInstance(this);

        /*
         * Create menu
         */
        JMenuBar menuBar = new JMenuBar();

        final JMenuItem itemOpen = new JMenuItem("Open structure(s)",
                loadIcon("/toolbarButtonGraphics/general/Open16.gif"));
        final JMenuItem itemSave = new JMenuItem("Save results",
                loadIcon("/toolbarButtonGraphics/general/Save16.gif"));
        itemSave.setEnabled(false);
        final JCheckBoxMenuItem checkBoxManager = new JCheckBoxMenuItem(
                "View structure manager", true);
        final JMenuItem itemExit = new JMenuItem("Exit");
        JMenu menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        menu.add(itemOpen);
        menu.add(itemSave);
        menu.addSeparator();
        menu.add(checkBoxManager);
        menu.addSeparator();
        menu.add(itemExit);
        menuBar.add(menu);

        final JRadioButtonMenuItem radioGlobalMcq = new JRadioButtonMenuItem(
                "Global MCQ", true);
        final JRadioButtonMenuItem radioGlobalRmsd = new JRadioButtonMenuItem(
                "Global RMSD", false);
        final JRadioButtonMenuItem radioLocal = new JRadioButtonMenuItem(
                "Local distances", false);
        ButtonGroup group = new ButtonGroup();
        group.add(radioGlobalMcq);
        group.add(radioGlobalRmsd);
        group.add(radioLocal);

        final JMenuItem itemSelectTorsion = new JMenuItem(
                "Select torsion angles");
        itemSelectTorsion.setEnabled(false);
        final JMenuItem itemSelectStructuresCompare = new JMenuItem(
                "Select structures to compare");
        final JMenuItem itemComputeDistances = new JMenuItem(
                "Compute distance(s)");
        itemComputeDistances.setEnabled(false);
        final JMenuItem itemVisualise = new JMenuItem("Visualise results");
        itemVisualise.setEnabled(false);
        final JMenuItem itemCluster = new JMenuItem("Cluster results");
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

        final JRadioButtonMenuItem radioAlignSeqGlobal = new JRadioButtonMenuItem(
                "Global sequence alignment", true);
        final JRadioButtonMenuItem radioAlignSeqLocal = new JRadioButtonMenuItem(
                "Local sequence alignment", false);
        final JRadioButtonMenuItem radioAlignStruc = new JRadioButtonMenuItem(
                "3D structure alignment", false);
        ButtonGroup groupAlign = new ButtonGroup();
        groupAlign.add(radioAlignSeqGlobal);
        groupAlign.add(radioAlignSeqLocal);
        groupAlign.add(radioAlignStruc);

        final JMenuItem itemSelectStructuresAlign = new JMenuItem(
                "Select structures to align");
        final JMenuItem itemComputeAlign = new JMenuItem("Compute alignment");
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

        JMenuItem itemGuide = new JMenuItem("Quick guide");
        JMenuItem itemAbout = new JMenuItem("About");

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
        final JLabel labelInfoGlobal = new JLabel(
                "Global comparison results: distance matrix");
        final JTable tableMatrix = new JTable();
        final JProgressBar progressBar = new JProgressBar();
        progressBar.setStringPainted(true);

        final JPanel panelResultsGlobal = new JPanel(new BorderLayout());
        panel = new JPanel(new BorderLayout());
        panel.add(labelInfoGlobal, BorderLayout.WEST);
        panelResultsGlobal.add(panel, BorderLayout.NORTH);
        panelResultsGlobal.add(new JScrollPane(tableMatrix),
                BorderLayout.CENTER);
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(new JLabel("Progress in computing:"));
        panel.add(progressBar);

        /*
         * Create panel with local comparison results
         */
        final JLabel labelInfoLocal = new JLabel(
                "Local comparison results: distance plot");
        final JPanel panelLocalPlot = new JPanel(new GridLayout(1, 1));

        final JPanel panelResultsLocal = new JPanel(new BorderLayout());
        panel = new JPanel(new BorderLayout());
        panel.add(labelInfoLocal, BorderLayout.WEST);
        panelResultsLocal.add(panel, BorderLayout.NORTH);
        panelResultsLocal.add(panelLocalPlot, BorderLayout.CENTER);

        /*
         * Create panel with sequence alignment
         */
        final JLabel labelInfoAlignSeq = new JLabel(
                "Sequence alignment results");
        final JTextArea textAreaAlignSeq = new JTextArea();
        textAreaAlignSeq.setEditable(false);
        textAreaAlignSeq.setFont(new Font("Monospaced", Font.PLAIN, 20));

        final JPanel panelResultsAlignSeq = new JPanel(new BorderLayout());
        panel = new JPanel(new BorderLayout());
        panel.add(labelInfoAlignSeq, BorderLayout.WEST);
        panelResultsAlignSeq.add(panel, BorderLayout.NORTH);
        panelResultsAlignSeq.add(new JScrollPane(textAreaAlignSeq),
                BorderLayout.CENTER);

        /*
         * Create panel with structure alignment
         */
        JPanel panelAlignStrucInfo = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 0.5;
        panelAlignStrucInfo.add(new JLabel("Whole structures (Jmol view)"),
                constraints);
        constraints.gridx++;
        constraints.weightx = 0;
        final JLabel labelInfoAlignStruc = new JLabel(
                "3D structure alignment results");
        panelAlignStrucInfo.add(labelInfoAlignStruc, constraints);
        constraints.gridx++;
        constraints.weightx = 0.5;
        panelAlignStrucInfo.add(new JLabel("Aligned fragments (Jmol view)"),
                constraints);

        final JmolPanel panelJmolLeft = new JmolPanel();
        panelJmolLeft.executeCmd("background lightgrey; save state state_init");
        final JmolPanel panelJmolRight = new JmolPanel();
        panelJmolRight.executeCmd("background darkgray; save state state_init");

        final JPanel panelResultsAlignStruc = new JPanel(new BorderLayout());
        panelResultsAlignStruc.add(panelAlignStrucInfo, BorderLayout.NORTH);
        panel = new JPanel(new GridLayout(1, 2));
        panel.add(panelJmolLeft);
        panel.add(panelJmolRight);
        panelResultsAlignStruc.add(panel, BorderLayout.CENTER);

        /*
         * Create card layout
         */
        final CardLayout layoutCards = new CardLayout();
        final JPanel panelCards = new JPanel();
        panelCards.setLayout(layoutCards);
        panelCards.add(new JPanel());
        panelCards.add(panelResultsGlobal, MainWindow.CARD_GLOBAL);
        panelCards.add(panelResultsLocal, MainWindow.CARD_LOCAL);
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
                File[] files = PdbFileChooser.getSelectedFiles(MainWindow.this);
                for (File f : files) {
                    DialogPdbs.loadStructure(f);
                }
            }
        });

        itemSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-DD-HH-mm");
                String prefix = sdf.format(new Date());

                Component current = MainWindow.getCurrentCard(panelCards);
                File proposedName = null;
                if (current.equals(panelResultsGlobal)) {
                    proposedName = new File(prefix + "-global.csv");
                } else {
                    String[] names = DialogChains.getNames();
                    prefix += String.format("-%s-%s", names[0], names[1]);
                    if (current.equals(panelResultsLocal)) {
                        proposedName = new File(prefix + "-local.csv");
                    } else if (current.equals(panelResultsAlignSeq)) {
                        proposedName = new File(prefix + "-alignseq.txt");
                    } else { // current.equals(panelResultsAlignStruc)
                        proposedName = new File(prefix + "-alignstruc.pdb");
                    }
                }
                chooserSaveFile.setSelectedFile(proposedName);

                int chosenOption = chooserSaveFile
                        .showSaveDialog(MainWindow.this);
                if (chosenOption != JFileChooser.APPROVE_OPTION) {
                    return;
                }

                if (current.equals(panelResultsGlobal)
                        || current.equals(panelResultsLocal)) {
                    Exportable model = (Exportable) tableMatrix.getModel();
                    model.export(chooserSaveFile.getSelectedFile());
                } else if (current.equals(panelResultsAlignSeq)) {
                    try (FileOutputStream stream = new FileOutputStream(
                            chooserSaveFile.getSelectedFile())) {
                        // TODO: output structure names
                        stream.write(resultAlignSeq.getBytes("UTF-8"));
                    } catch (IOException e1) {
                        MainWindow.LOGGER.error(
                                "Failed to save aligned sequences", e1);
                        JOptionPane.showMessageDialog(MainWindow.this,
                                e1.getMessage(), "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } else { // current.equals(panelResultsAlignStruc)
                    try (FileOutputStream stream = new FileOutputStream(
                            chooserSaveFile.getSelectedFile())) {
                        stream.write(resultAlignStruc.getBytes("UTF-8"));
                    } catch (IOException e1) {
                        MainWindow.LOGGER.error(
                                "Failed to save PDB of aligned structures", e1);
                        JOptionPane.showMessageDialog(MainWindow.this,
                                e1.getMessage(), "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
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
            }
        };
        radioGlobalMcq.addActionListener(radioActionListener);
        radioGlobalRmsd.addActionListener(radioActionListener);
        radioLocal.addActionListener(radioActionListener);

        itemSelectTorsion.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                torsionDialog.setVisible(true);
            }
        });

        ActionListener selectActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object source = e.getSource();
                if (source.equals(itemComputeDistances)
                        && (radioGlobalMcq.isSelected() || radioGlobalRmsd
                                .isSelected())) {

                    if (DialogStructures.showDialog() != DialogStructures.OK) {
                        return;
                    }

                    File[] files = DialogStructures.getFiles();
                    if (files.length < 2) {
                        JOptionPane.showMessageDialog(MainWindow.this, "At "
                                + "least two structures must be selected to "
                                + "compute global distance", "Information",
                                JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }

                    String[] names = new String[files.length];
                    for (int i = 0; i < files.length; i++) {
                        names[i] = PdbManager.getName(files[i]);
                    }

                    tableMatrix.setModel(new TableModelGlobal(names,
                            new double[0][]));
                    layoutCards.show(panelCards, MainWindow.CARD_GLOBAL);

                    itemSave.setEnabled(false);
                    radioGlobalMcq.setEnabled(true);
                    radioGlobalRmsd.setEnabled(true);
                    itemComputeDistances.setEnabled(true);
                } else {
                    if (DialogChains.showDialog() != DialogChains.OK) {
                        return;
                    }

                    File[] structures = DialogChains.getFiles();
                    Chain[][] chains = DialogChains.getChains();
                    for (int i = 0; i < 2; i++) {
                        if (chains[i].length == 0) {
                            String message = "No chains specified for structure: "
                                    + structures[i];
                            JOptionPane.showMessageDialog(MainWindow.this,
                                    message, "Information",
                                    JOptionPane.INFORMATION_MESSAGE);
                            return;
                        }
                    }

                    if (source.equals(itemSelectStructuresCompare)) {
                        panelLocalPlot.removeAll();
                        panelLocalPlot.revalidate();
                        layoutCards.show(panelCards, MainWindow.CARD_LOCAL);

                        itemComputeDistances.setEnabled(true);
                        itemComputeAlign.setEnabled(false);
                    } else if (radioAlignSeqGlobal.isSelected()
                            || radioAlignSeqLocal.isSelected()) {
                        if (chains[0].length != 1 || chains[1].length != 1) {
                            JOptionPane.showMessageDialog(MainWindow.this,
                                    "A single chain should be "
                                            + "selected from each "
                                            + "structure in "
                                            + "sequence alignment.",
                                    "Information",
                                    JOptionPane.INFORMATION_MESSAGE);
                            return;
                        }

                        textAreaAlignSeq.setText("");
                        layoutCards.show(panelCards, MainWindow.CARD_ALIGN_SEQ);

                        itemComputeDistances.setEnabled(false);
                        itemComputeAlign.setEnabled(true);
                    } else { // source.equals(itemSelectChainsAlignStruc)
                        panelJmolLeft.executeCmd("restore state "
                                + "state_init");
                        panelJmolRight.executeCmd("restore state "
                                + "state_init");
                        layoutCards.show(panelCards,
                                MainWindow.CARD_ALIGN_STRUC);

                        itemComputeDistances.setEnabled(false);
                        itemComputeAlign.setEnabled(true);
                    }
                }
            }
        };
        itemSelectStructuresCompare.addActionListener(selectActionListener);
        itemSelectStructuresAlign.addActionListener(selectActionListener);

        itemComputeDistances.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (radioGlobalMcq.isSelected() || radioGlobalRmsd.isSelected()) {
                    final GlobalComparison comparison;
                    if (radioGlobalMcq.isSelected()) {
                        comparison = new MCQ();
                    } else { // radioRmsd.isSelected() == true
                        comparison = new RMSD();
                    }

                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Structure[] structures = PdbManager
                                    .getSelectedStructures(DialogStructures
                                            .getFiles());

                            resultGlobalNames = PdbManager
                                    .getSelectedStructuresNames(DialogStructures
                                            .getFiles());
                            resultGlobalMatrix = comparison.compare(structures,
                                    new ComparisonListener() {
                                        @Override
                                        public void stateChanged(long all,
                                                long completed) {
                                            progressBar.setMaximum((int) all);
                                            progressBar
                                                    .setValue((int) completed);
                                        }
                                    });

                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    TableModelGlobal model = new TableModelGlobal(
                                            resultGlobalNames,
                                            resultGlobalMatrix);
                                    tableMatrix.setModel(model);

                                    itemSave.setEnabled(true);
                                    itemSave.setText("Save results (CSV)");
                                    itemCluster.setEnabled(true);
                                    itemVisualise.setEnabled(true);

                                    labelInfoGlobal.setText("Global comparison "
                                            + "results: distance matrix for "
                                            + (radioGlobalMcq.isSelected() ? "MCQ"
                                                    : "RMSD"));
                                }
                            });
                        }
                    });
                    thread.start();
                } else {
                    layoutCards.show(panelCards, MainWindow.CARD_LOCAL);

                    final Structure[] structures = new Structure[2];
                    for (int i = 0; i < 2; i++) {
                        structures[i] = new StructureImpl();
                        // FIXME: NPE after hitting Cancel on chain selection
                        structures[i].setChains(Arrays.asList(DialogChains
                                .getChains()[i]));
                    }

                    try {
                        resultLocal = TorsionLocalComparison.compare(
                                structures[0], structures[1], false);
                    } catch (StructureException e1) {
                        JOptionPane.showMessageDialog(MainWindow.this,
                                e1.getMessage(), "Error",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    layoutCards.show(panelCards, MainWindow.CARD_GLOBAL);
                    TableModelLocal model = new TableModelLocal(resultLocal);
                    tableMatrix.setModel(model);
                    return;

                    // Set<ResidueNumber> set = new HashSet<>();
                    // for (String angle : torsionDialog.selectedNames) {
                    // if (!resultLocal.containsKey(angle)) {
                    // continue;
                    // }
                    // for (AngleDifference ad : resultLocal.get(angle)) {
                    // set.add(ad.getResidue());
                    // }
                    // }
                    // List<ResidueNumber> list = new ArrayList<>(set);
                    // Collections.sort(list);
                    //
                    // DefaultXYDataset dataset = new DefaultXYDataset();
                    // for (String angle : torsionDialog.selectedNames) {
                    // if (!resultLocal.containsKey(angle)) {
                    // continue;
                    // }
                    // List<AngleDifference> diffs = resultLocal.get(angle);
                    // Collections.sort(diffs);
                    //
                    // double[] x = new double[diffs.size()];
                    // double[] y = new double[diffs.size()];
                    // for (int i = 0; i < diffs.size(); i++) {
                    // AngleDifference ad = diffs.get(i);
                    // x[i] = list.indexOf(ad.getResidue());
                    // y[i] = ad.getDifference();
                    // }
                    // dataset.addSeries(angle, new double[][] { x, y });
                    // }
                    //
                    // NumberAxis xAxis = new TorsionAxis(resultLocal);
                    // xAxis.setLabel("Residue");
                    // NumberAxis yAxis = new NumberAxis();
                    // yAxis.setAutoRange(false);
                    // yAxis.setRange(0, Math.PI);
                    // yAxis.setLabel("Distance [rad]");
                    // XYPlot plot = new XYPlot(dataset, xAxis, yAxis,
                    // new DefaultXYItemRenderer());
                    //
                    // panelLocalPlot.removeAll();
                    // panelLocalPlot.add(new ChartPanel(new JFreeChart(plot)));
                    // panelLocalPlot.revalidate();
                    //
                    // itemSave.setEnabled(true);
                    // itemSave.setText("Save results (CSV)");
                    //
                    // File[] pdbs = new File[] {
                    // chainDialog.selectedStructures[0],
                    // chainDialog.selectedStructures[1] };
                    // String[] names = new String[] {
                    // PdbManager.getStructureName(pdbs[0]),
                    // PdbManager.getStructureName(pdbs[1]) };
                    // labelInfoLocal
                    // .setText("Local comparison results: distance "
                    // + "plot for " + names[0] + " and "
                    // + names[1]);

                }
            }
        });

        itemVisualise.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Visualizable model = (Visualizable) tableMatrix.getModel();
                model.visualize();
            }
        });

        itemCluster.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                TableModelGlobal model = (TableModelGlobal) tableMatrix
                        .getModel();
                String[] names = model.getNames();
                double[][] values = model.getValues();

                for (double[] value : values) {
                    for (double element : value) {
                        if (Double.isNaN(element)) {
                            JOptionPane.showMessageDialog(MainWindow.this, ""
                                    + "Results cannot be visualized. Some "
                                    + "structures could not be compared.",
                                    "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                }

                DialogCluster dialogClustering = new DialogCluster(names,
                        values);
                dialogClustering.setVisible(true);
            }
        });

        itemComputeAlign.addActionListener(new ActionListener() {
            private Thread thread;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (radioAlignSeqGlobal.isSelected()
                        || radioAlignSeqLocal.isSelected()) {
                    layoutCards.show(panelCards, MainWindow.CARD_ALIGN_SEQ);

                    Chain chains[] = new Chain[] {
                            DialogChains.getChains()[0][0],
                            DialogChains.getChains()[1][0] };

                    boolean isRNA = Helper.isNucleicAcid(chains[0]);
                    if (isRNA != Helper.isNucleicAcid(chains[1])) {
                        String message = "Cannot align structures: different molecular types";
                        MainWindow.LOGGER.error(message);
                        JOptionPane.showMessageDialog(null, message, "Error",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    OutputAlignSeq alignment = SequenceAligner.align(chains[0],
                            chains[1], radioAlignSeqGlobal.isSelected());
                    resultAlignSeq = alignment.toString();
                    textAreaAlignSeq.setText(resultAlignSeq);

                    itemSave.setEnabled(true);
                    itemSave.setText("Save results (TXT)");

                    File[] pdbs = new File[] { DialogChains.getFiles()[0],
                            DialogChains.getFiles()[1] };
                    String[] names = new String[] {
                            PdbManager.getName(pdbs[0]),
                            PdbManager.getName(pdbs[1]) };
                    labelInfoAlignSeq.setText("Sequence alignment results for "
                            + names[0] + " and " + names[1]);
                } else {
                    if (thread != null && thread.isAlive()) {
                        JOptionPane.showMessageDialog(null,
                                "3D structure alignment computation has not "
                                        + "finished yet!", "Information",
                                JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }

                    layoutCards.show(panelCards, MainWindow.CARD_ALIGN_STRUC);

                    final Structure[] structures = new Structure[2];
                    for (int i = 0; i < 2; i++) {
                        structures[i] = new StructureImpl();
                        structures[i].setChains(Arrays.asList(DialogChains
                                .getChains()[i]));
                    }

                    boolean isRNA = Helper.isNucleicAcid(structures[0]);
                    if (isRNA != Helper.isNucleicAcid(structures[1])) {
                        String message = "Cannot align structures: different molecular types";
                        MainWindow.LOGGER.error(message);
                        JOptionPane.showMessageDialog(null, message, "Error",
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

                    thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Helper.normalizeAtomNames(structures[0]);
                                Helper.normalizeAtomNames(structures[1]);

                                AlignmentOutput output = StructureAligner
                                        .align(structures[0], structures[1]);
                                final Structure[] aligned = output
                                        .getStructures();

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
                                        resultAlignStruc = builder.toString();

                                        JmolViewer viewer = panelJmolLeft
                                                .getViewer();
                                        viewer.openStringInline(builder
                                                .toString());
                                        panelJmolLeft.executeCmd(JMOL_SCRIPT);

                                        builder = new StringBuilder();
                                        builder.append("MODEL        1                                                                  \n");
                                        builder.append(aligned[2].toPDB());
                                        builder.append("ENDMDL                                                                          \n");
                                        builder.append("MODEL        2                                                                  \n");
                                        builder.append(aligned[3].toPDB());
                                        builder.append("ENDMDL                                                                          \n");

                                        viewer = panelJmolRight.getViewer();
                                        viewer.openStringInline(builder
                                                .toString());
                                        panelJmolRight.executeCmd(JMOL_SCRIPT);

                                        itemSave.setEnabled(true);
                                        itemSave.setText("Save results (PDB)");
                                    }
                                });
                            } catch (StructureException e1) {
                                MainWindow.LOGGER.error(
                                        "Failed to align structures", e1);
                                JOptionPane.showMessageDialog(getParent(),
                                        e1.getMessage(), "Error",
                                        JOptionPane.ERROR_MESSAGE);
                            } finally {
                                timer.stop();

                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        File[] pdbs = new File[] {
                                                DialogChains.getFiles()[0],
                                                DialogChains.getFiles()[1] };
                                        String[] names = new String[] {
                                                PdbManager.getName(pdbs[0]),
                                                PdbManager.getName(pdbs[1]) };
                                        labelInfoAlignStruc
                                                .setText("3D structure "
                                                        + "alignments results for "
                                                        + names[0] + " and "
                                                        + names[1]);
                                    }
                                });
                            }
                        }
                    });
                    thread.start();

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

    private ImageIcon loadIcon(String name) {
        URL resource = getClass().getResource(name);
        if (resource == null) {
            MainWindow.LOGGER.error("Failed to load icon: " + name);
            return null;
        }
        return new ImageIcon(resource);
    }
}
