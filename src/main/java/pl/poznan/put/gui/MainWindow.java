package pl.poznan.put.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

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
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.apache.commons.lang3.tuple.Pair;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Structure;

import pl.poznan.put.common.MoleculeType;
import pl.poznan.put.constant.Colors;
import pl.poznan.put.gui.panel.GlobalMatrixPanel;
import pl.poznan.put.gui.panel.LocalMatrixPanel;
import pl.poznan.put.gui.panel.LocalMultiMatrixPanel;
import pl.poznan.put.gui.panel.SequenceAlignmentPanel;
import pl.poznan.put.gui.panel.StructureAlignmentPanel;
import pl.poznan.put.interfaces.Clusterable;
import pl.poznan.put.interfaces.Exportable;
import pl.poznan.put.interfaces.Visualizable;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.structure.tertiary.StructureManager;
import darrylbu.component.StayOpenCheckBoxMenuItem;
import darrylbu.component.StayOpenRadioButtonMenuItem;

public class MainWindow extends JFrame {
    private static final String RESOURCE_ICON_OPEN = "/toolbarButtonGraphics/general/Open16.gif";
    private static final String RESOURCE_ICON_SAVE = "/toolbarButtonGraphics/general/Save16.gif";

    private static final String CARD_ALIGN_SEQ = "CARD_ALIGN_SEQ";
    private static final String CARD_ALIGN_STRUC = "CARD_ALIGN_STRUC";
    private static final String CARD_GLOBAL_MATRIX = "CARD_GLOBAL_MATRIX";
    private static final String CARD_LOCAL_MATRIX = "CARD_LOCAL_MATRIX";
    private static final String CARD_LOCAL_MULTI_MATRIX = "CARD_LOCAL_MULTI_MATRIX";
    private static final String TITLE = "MCQ4Structures: computing similarity of 3D RNA / protein structures";

    private final TableCellRenderer colorsRenderer = new DefaultTableCellRenderer() {
        private final TableCellRenderer defaultRenderer = new DefaultTableCellRenderer();

        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row,
                int column) {
            Component component = defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (column == 0) {
                component.setBackground(Color.WHITE);
                component.setForeground(Color.BLACK);
            } else {
                component.setBackground(Colors.COLORS[column - 1]);
            }
            return component;
        }
    };

    private final JMenu menuFile = new JMenu("File");
    private final JMenuItem itemOpen = new JMenuItem("Open structure(s)", new ImageIcon(getClass().getResource(MainWindow.RESOURCE_ICON_OPEN)));
    private final JMenuItem itemSave = new JMenuItem("Save results", new ImageIcon(getClass().getResource(MainWindow.RESOURCE_ICON_SAVE)));
    private final JCheckBoxMenuItem checkBoxManager = new StayOpenCheckBoxMenuItem("View structure manager", false);
    private final JMenuItem itemExit = new JMenuItem("Exit");

    private final JMenu menuDistanceMeasure = new JMenu("Distance measure");
    private final JRadioButtonMenuItem radioGlobalMcq = new StayOpenRadioButtonMenuItem("Global MCQ", true);
    private final JRadioButtonMenuItem radioGlobalRmsd = new StayOpenRadioButtonMenuItem("Global RMSD", false);
    private final JRadioButtonMenuItem radioLocal = new StayOpenRadioButtonMenuItem("Local distances (pair)", false);
    private final JRadioButtonMenuItem radioLocalMulti = new StayOpenRadioButtonMenuItem("Local distances (multiple)", false);
    private final JMenuItem itemSelectTorsion = new JMenuItem("Select torsion angles");
    private final JMenuItem itemSelectStructuresCompare = new JMenuItem("Select structures to compare");
    private final JMenuItem itemComputeDistances = new JMenuItem("Compute distance(s)");
    private final JMenuItem itemVisualise = new JMenuItem("Visualise results");
    private final JMenuItem itemVisualise3D = new JMenuItem("Visualise results in 3D");
    private final JMenuItem itemCluster = new JMenuItem("Cluster results");

    private final JMenu menuAlignment = new JMenu("Alignment");
    private final JRadioButtonMenuItem radioAlignSeqGlobal = new StayOpenRadioButtonMenuItem("Global sequence alignment", true);
    private final JRadioButtonMenuItem radioAlignSeqLocal = new StayOpenRadioButtonMenuItem("Local sequence alignment", false);
    private final JRadioButtonMenuItem radioAlignStruc = new StayOpenRadioButtonMenuItem("3D structure alignment", false);
    private final JMenuItem itemSelectStructuresAlign = new JMenuItem("Select structures to align");
    private final JMenuItem itemComputeAlign = new JMenuItem("Compute alignment");

    private final JMenu menuHelp = new JMenu("Help");
    private final JMenuItem itemGuide = new JMenuItem("Quick guide");
    private final JMenuItem itemAbout = new JMenuItem("About");

    private final CardLayout layoutCards = new CardLayout();
    private final JPanel panelCards = new JPanel();
    private final GlobalMatrixPanel panelResultsGlobalMatrix = new GlobalMatrixPanel();
    private final LocalMatrixPanel panelResultsLocalMatrix = new LocalMatrixPanel();
    private final LocalMultiMatrixPanel panelResultsLocalMultiMatrix = new LocalMultiMatrixPanel();
    private final SequenceAlignmentPanel panelResultsAlignSeq = new SequenceAlignmentPanel();
    private final StructureAlignmentPanel panelResultsAlignStruc = new StructureAlignmentPanel();

    private final PdbChooser pdbChooser = PdbChooser.getInstance();
    private final DialogManager dialogManager;
    private final DialogSelectStructures dialogStructures;
    private final DialogSelectChains dialogChains;
    private DialogSelectChainsMultiple dialogChainsMultiple;

    private Clusterable clusterable;
    private Exportable exportable;
    private Visualizable visualizable;

    public MainWindow(List<File> pdbs) {
        super();

        dialogManager = new DialogManager(this);
        dialogStructures = new DialogSelectStructures(this);
        dialogChains = new DialogSelectChains(this);
        dialogChainsMultiple = new DialogSelectChainsMultiple(this);

        dialogManager.loadStructures(pdbs);
        createMenu();

        panelCards.setLayout(layoutCards);
        panelCards.add(new JPanel());
        panelCards.add(panelResultsGlobalMatrix, MainWindow.CARD_GLOBAL_MATRIX);
        panelCards.add(panelResultsLocalMatrix, MainWindow.CARD_LOCAL_MATRIX);
        panelCards.add(panelResultsLocalMultiMatrix, MainWindow.CARD_LOCAL_MULTI_MATRIX);
        panelCards.add(panelResultsAlignSeq, MainWindow.CARD_ALIGN_SEQ);
        panelCards.add(panelResultsAlignStruc, MainWindow.CARD_ALIGN_STRUC);

        setLayout(new BorderLayout());
        add(panelCards, BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle(MainWindow.TITLE);

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension size = toolkit.getScreenSize();
        setSize(size.width * 3 / 4, size.height * 3 / 4);
        setLocation(size.width / 8, size.height / 8);

        dialogManager.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                checkBoxManager.setSelected(false);
            }
        });
    }

    private void createMenu() {
        itemSave.setEnabled(false);
        itemSelectTorsion.setEnabled(false);
        itemComputeDistances.setEnabled(false);
        itemVisualise.setEnabled(false);
        itemVisualise3D.setEnabled(false);
        itemCluster.setEnabled(false);
        itemComputeAlign.setEnabled(false);

        ButtonGroup group = new ButtonGroup();
        group.add(radioGlobalMcq);
        group.add(radioGlobalRmsd);
        group.add(radioLocal);
        group.add(radioLocalMulti);

        ButtonGroup groupAlign = new ButtonGroup();
        groupAlign.add(radioAlignSeqGlobal);
        groupAlign.add(radioAlignSeqLocal);
        groupAlign.add(radioAlignStruc);

        JMenuBar menuBar = new JMenuBar();

        menuFile.setMnemonic(KeyEvent.VK_F);
        menuFile.add(itemOpen);
        menuFile.add(itemSave);
        menuFile.addSeparator();
        menuFile.add(checkBoxManager);
        menuFile.addSeparator();
        menuFile.add(itemExit);
        menuBar.add(menuFile);

        menuDistanceMeasure.setMnemonic(KeyEvent.VK_D);
        menuDistanceMeasure.add(new JLabel("    Select distance type:"));
        menuDistanceMeasure.add(radioGlobalMcq);
        menuDistanceMeasure.add(radioGlobalRmsd);
        menuDistanceMeasure.add(radioLocal);
        menuDistanceMeasure.add(radioLocalMulti);
        menuDistanceMeasure.addSeparator();
        menuDistanceMeasure.add(itemSelectTorsion);
        menuDistanceMeasure.add(itemSelectStructuresCompare);
        menuDistanceMeasure.addSeparator();
        menuDistanceMeasure.add(itemComputeDistances);
        menuDistanceMeasure.add(itemVisualise);
        menuDistanceMeasure.add(itemVisualise3D);
        menuDistanceMeasure.add(itemCluster);
        menuBar.add(menuDistanceMeasure);

        menuAlignment.setMnemonic(KeyEvent.VK_A);
        menuAlignment.add(new JLabel("    Select alignment type:"));
        menuAlignment.add(radioAlignSeqGlobal);
        menuAlignment.add(radioAlignSeqLocal);
        menuAlignment.add(radioAlignStruc);
        menuAlignment.addSeparator();
        menuAlignment.add(itemSelectStructuresAlign);
        menuAlignment.add(itemComputeAlign);
        menuBar.add(menuAlignment);

        menuHelp.setMnemonic(KeyEvent.VK_H);
        menuHelp.add(itemGuide);
        menuHelp.add(itemAbout);
        menuBar.add(menuHelp);

        setJMenuBar(menuBar);

        itemOpen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialogManager.selectAndLoadStructures();
            }
        });

        itemSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveResults();
            }
        });

        checkBoxManager.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialogManager.setVisible(checkBoxManager.isSelected());
            }
        });

        itemExit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispatchEvent(new WindowEvent(MainWindow.this, WindowEvent.WINDOW_CLOSING));
            }
        });

        ActionListener radioActionListener = new ActionListener() {
            private Object sourcePrev = radioGlobalMcq;

            @Override
            public void actionPerformed(ActionEvent arg0) {
                assert arg0 != null;

                Object source = arg0.getSource();
                itemSelectTorsion.setEnabled(source.equals(radioLocal));
                itemVisualise.setEnabled(false);
                itemVisualise3D.setEnabled(false);
                itemCluster.setEnabled(false);

                boolean globalCurr = source.equals(radioGlobalMcq) || source.equals(radioGlobalRmsd);
                boolean globalPrev = sourcePrev.equals(radioGlobalMcq) || sourcePrev.equals(radioGlobalRmsd);
                if (!globalCurr || !globalPrev) {
                    itemComputeDistances.setEnabled(false);
                }
                sourcePrev = source;
            }
        };
        radioGlobalMcq.addActionListener(radioActionListener);
        radioGlobalRmsd.addActionListener(radioActionListener);
        radioLocal.addActionListener(radioActionListener);
        radioLocalMulti.addActionListener(radioActionListener);

        itemSelectTorsion.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                DialogSelectAngles.selectAngles();
            }
        });

        ActionListener selectActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                assert e != null;
                Object source = e.getSource();
                if (source.equals(itemSelectStructuresCompare)) {
                    if (radioLocal.isSelected()) {
                        selectChains(source);
                    } else if (radioLocalMulti.isSelected()) {
                        selectChainsMultiple(source);
                    } else {
                        selectStructures();
                    }
                } else {
                    if (radioAlignStruc.isSelected()) {
                        selectChains(source);
                    } else {
                        selectChainsMultiple(source);
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
                    compareGlobal();
                } else if (radioLocal.isSelected()) {
                    compareLocalPair();
                } else { // radioLocalMulti.isSelected() == true
                    compareLocalMulti();
                }
            }
        });

        itemVisualise.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (visualizable != null) {
                    visualizable.visualize();
                }
            }
        });

        itemVisualise3D.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (visualizable != null) {
                    visualizable.visualize3D();
                }
            }
        });

        itemCluster.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (clusterable != null) {
                    clusterable.cluster();
                }
            }
        });

        ActionListener radioAlignListener = new ActionListener() {
            private boolean isSequencePrevious = true;

            @Override
            public void actionPerformed(ActionEvent arg0) {
                assert arg0 != null;

                Object source = arg0.getSource();
                boolean isSequenceNow = source.equals(radioAlignSeqGlobal) || source.equals(radioAlignSeqLocal);
                if (isSequenceNow != isSequencePrevious) {
                    itemComputeAlign.setEnabled(false);
                }
                isSequencePrevious = isSequenceNow;
            }
        };
        radioAlignSeqGlobal.addActionListener(radioActionListener);
        radioAlignSeqLocal.addActionListener(radioAlignListener);
        radioAlignStruc.addActionListener(radioAlignListener);

        itemComputeAlign.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (radioAlignSeqGlobal.isSelected() || radioAlignSeqLocal.isSelected()) {
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

    void alignSequences() {
        // TODO
        // textAreaAlignSeq.setText("");
        // layoutCards.show(panelCards, MainWindow.CARD_ALIGN_SEQ);
        //
        // List<CompactFragment> fragments = dialogChainsMultiple.getChains();
        // boolean isGlobal = radioAlignSeqGlobal.isSelected();
        // SequenceAligner aligner = new SequenceAligner(fragments, isGlobal);
        // SequenceAlignment alignment = aligner.align();
        //
        // exportable = alignment;
        // textAreaAlignSeq.setText(alignment.toString());
        // itemSave.setEnabled(true);
        // itemSave.setText("Save results (TXT)");
        //
        // if (isGlobal) {
        // labelInfoAlignSeq.setText("<html>" +
        // "Structures selected for global sequence alignment: " +
        // alignment.getTitle() + "<br>" + "Global sequence alignment results:"
        // + "</html>");
        // } else {
        // labelInfoAlignSeq.setText("<html>" +
        // "Structures selected for local sequence alignment: " +
        // alignment.getTitle() + "<br>" + "Local sequence alignment results:" +
        // "</html>");
        // }
    }

    void alignStructures() {
        // TODO
        // panelJmolLeft.executeCmd("restore state state_init");
        // panelJmolRight.executeCmd("restore state state_init");
        // layoutCards.show(panelCards, MainWindow.CARD_ALIGN_STRUC);
        //
        // Pair<Structure, Structure> pair = dialogChains.getStructures();
        // Structure left = pair.getLeft();
        // Structure right = pair.getRight();
        // StructureSelection s1 =
        // SelectionFactory.create(StructureManager.getName(left), left);
        // StructureSelection s2 =
        // SelectionFactory.create(StructureManager.getName(right), right);
        //
        // MCQMatcher matcher = new
        // MCQMatcher(MCQ.getAllAvailableTorsionAngles());
        // SelectionMatch selectionMatch = matcher.matchSelections(s1, s2);
        // exportable = selectionMatch;
        //
        // try {
        // String jmolScript = "frame 0.0; cartoon only; " +
        // "select model=1.1; color green; " + "select model=1.2; color red; ";
        //
        // JmolViewer viewer = panelJmolLeft.getViewer();
        // String pdb = selectionMatch.toPDB(false);
        // try {
        // FileUtils.write(new File("/tmp/whole.pdb"), pdb);
        // } catch (IOException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // viewer.openStringInline(pdb);
        // panelJmolLeft.executeCmd(jmolScript);
        //
        // viewer = panelJmolRight.getViewer();
        // pdb = selectionMatch.toPDB(true);
        // try {
        // FileUtils.write(new File("/tmp/matched.pdb"), pdb);
        // } catch (IOException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // viewer.openStringInline(pdb);
        // panelJmolRight.executeCmd(jmolScript);
        // } catch (StructureException e) {
        // JOptionPane.showMessageDialog(this, "Failed to align structures. " +
        // "Reason: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        // return;
        // }
        //
        // itemSave.setEnabled(true);
        // itemSave.setText("Save results (PDB)");
        //
        // labelAlignmentStatus.setText("Computation finished");
        // labelInfoAlignStruc.setText("<html>" +
        // "Structures selected for 3D structure alignment: " +
        // dialogChains.getSelectionDescription() + "<br>" +
        // "3D structure alignment results:" + "</html>");
    }

    void compareGlobal() {
        // TODO
        // final GlobalComparator comparator;
        // if (radioGlobalMcq.isSelected()) {
        // comparator = new MCQ(MCQ.getAllAvailableTorsionAngles());
        // } else { // radioRmsd.isSelected() == true
        // comparator = new RMSD(AtomFilter.ALL, true);
        // }
        //
        // final List<Structure> structures = dialogStructures.getStructures();
        // final List<String> names = StructureManager.getNames(structures);
        //
        // Thread thread = new Thread(new Runnable() {
        // @Override
        // public void run() {
        // List<StructureSelection> selections = new ArrayList<>();
        //
        // for (int i = 0; i < structures.size(); i++) {
        // selections.add(SelectionFactory.create(names.get(i),
        // structures.get(i)));
        // }
        //
        // final GlobalComparisonResultMatrix matrix =
        // ParallelGlobalComparison.run(comparator, selections,
        // MainWindow.this);
        //
        // SwingUtilities.invokeLater(new Runnable() {
        // @Override
        // public void run() {
        // clusterable = matrix;
        // exportable = matrix;
        // visualizable = matrix;
        //
        // tableMatrix.setModel(matrix.asDisplayableTableModel());
        // tableMatrix.setDefaultRenderer(Object.class, new
        // DefaultTableCellRenderer());
        //
        // itemSave.setEnabled(true);
        // itemSave.setText("Save results (CSV)");
        // itemVisualise.setEnabled(true);
        // itemVisualise3D.setEnabled(true);
        // itemCluster.setEnabled(true);
        //
        // labelInfoMatrix.setText("<html>" +
        // "Structures selected for global distance measure: " +
        // dialogStructures.getSelectionDescription() + "<br>" +
        // "Global distance matrix (" + matrix.getMeasureName() + "):" +
        // "</html>");
        // }
        // });
        // }
        // });
        // thread.start();
    }

    void compareLocalPair() {
        // TODO
        // Pair<Structure, Structure> structures = dialogChains.getStructures();
        // Pair<List<Chain>, List<Chain>> chains = dialogChains.getChains();
        //
        // StructureSelection selectionL =
        // SelectionFactory.create(StructureManager.getName(structures.getLeft()),
        // chains.getLeft());
        // StructureSelection selectionR =
        // SelectionFactory.create(StructureManager.getName(structures.getRight()),
        // chains.getRight());
        //
        // LocalComparisonResult comparisonLocal;
        //
        // try {
        // progressBar.setValue(0);
        // progressBar.setMaximum(1);
        //
        // MCQ mcq = new MCQ(Arrays.asList(dialogAngles.getAngles()));
        // comparisonLocal = mcq.comparePair(selectionL, selectionR);
        // } catch (IncomparableStructuresException e) {
        // JOptionPane.showMessageDialog(MainWindow.this, e.getMessage(),
        // "Error", JOptionPane.ERROR_MESSAGE);
        // return;
        // } finally {
        // progressBar.setValue(1);
        // }
        //
        // if (comparisonLocal instanceof MCQLocalComparisonResult) {
        // tableMatrix.setModel(comparisonLocal.asDisplayableTableModel());
        // tableMatrix.setDefaultRenderer(Object.class, colorsRenderer);
        // } else {
        // JOptionPane.showMessageDialog(MainWindow.this, "Cannot continue, " +
        // "the result of comparison is invalid", "Error",
        // JOptionPane.ERROR_MESSAGE);
        // return;
        // }
        //
        // exportable = comparisonLocal;
        // visualizable = comparisonLocal;
        //
        // itemSave.setEnabled(true);
        // itemSave.setText("Save results (CSV)");
        // itemVisualise.setEnabled(true);
        // itemVisualise3D.setEnabled(true);
        // itemCluster.setEnabled(false);
        //
        // labelInfoMatrix.setText("<html>" + "Structures selected for local " +
        // "distance measure: " + dialogChains.getSelectionDescription() +
        // "<br>" + "Local distance vector(s):" + "</html>");
    }

    void compareLocalMulti() {
        // TODO
        // List<CompactFragment> selections = dialogChainsMultiple.getChains();
        // CompactFragment[] array = selections.toArray(new
        // CompactFragment[selections.size()]);
        // MoleculeType moleculeType = array[0].getMoleculeType();
        // List<TorsionAngle> angles = new ArrayList<>();
        //
        // CompactFragment reference = (CompactFragment)
        // JOptionPane.showInputDialog(MainWindow.this,
        // "Select your reference structure", "Reference structure",
        // JOptionPane.INFORMATION_MESSAGE, null, array, array[0]);
        // if (reference == null) {
        // return;
        // }
        //
        // if (moleculeType == MoleculeType.PROTEIN) {
        // angles.addAll(Arrays.asList(ProteinTorsionAngle.values()));
        // angles.addAll(Arrays.asList(ChiTorsionAngleType.getChiTorsionAngles(MoleculeType.PROTEIN)));
        // } else if (moleculeType == MoleculeType.RNA) {
        // angles.addAll(Arrays.asList(RNATorsionAngle.values()));
        // angles.addAll(Arrays.asList(ChiTorsionAngleType.getChiTorsionAngles(MoleculeType.RNA)));
        // angles.add(PseudophasePuckerAngle.getInstance());
        // }
        //
        // AverageTorsionAngleType averageAngle =
        // AverageTorsionAngleType.getInstanceMainAngles(moleculeType);
        // angles.add(averageAngle);
        //
        // TorsionAngle angleType = (TorsionAngle)
        // JOptionPane.showInputDialog(MainWindow.this, "Select torsion angle",
        // "Torsion angle", JOptionPane.INFORMATION_MESSAGE, null,
        // angles.toArray(new TorsionAngle[angles.size()]), averageAngle);
        // if (angleType == null) {
        // return;
        // }
        //
        // List<TorsionAngle> selectedAngles = new ArrayList<>();
        //
        // if (angleType.equals(averageAngle)) {
        // selectedAngles.addAll(averageAngle.getConsideredAngles());
        // if (moleculeType == MoleculeType.RNA) {
        // selectedAngles.addAll(Arrays.asList(PseudophasePuckerAngle.requiredAngles()));
        // }
        // } else if (angleType.equals(PseudophasePuckerAngle.getInstance())) {
        // selectedAngles.add(RNATorsionAngle.TAU0);
        // selectedAngles.add(RNATorsionAngle.TAU1);
        // selectedAngles.add(RNATorsionAngle.TAU2);
        // selectedAngles.add(RNATorsionAngle.TAU3);
        // selectedAngles.add(RNATorsionAngle.TAU4);
        // }
        //
        // selectedAngles.add(angleType);
        // selections.remove(reference);
        // ModelsComparisonResult result;
        // SelectedAngle selectedAngle;
        //
        // progressBar.setMaximum(1);
        // progressBar.setValue(0);
        //
        // try {
        // MCQ mcq = new MCQ(selectedAngles);
        // result = mcq.compareModels(reference, selections);
        // selectedAngle = result.selectAngle(angleType);
        // } catch (IncomparableStructuresException e) {
        // JOptionPane.showMessageDialog(MainWindow.this, e.getMessage(),
        // "Error", JOptionPane.ERROR_MESSAGE);
        // return;
        // }
        //
        // progressBar.setValue(1);
        // exportable = selectedAngle;
        // visualizable = selectedAngle;
        //
        // tableMatrix.setModel(selectedAngle.asDisplayableTableModel());
        // tableMatrix.setDefaultRenderer(Object.class, new
        // DefaultTableCellRenderer());
        //
        // itemSave.setEnabled(true);
        // itemSave.setText("Save results (CSV)");
        // itemVisualise.setEnabled(true);
        // itemVisualise3D.setEnabled(true);
        // itemCluster.setEnabled(false);
        //
        // labelInfoMatrix.setText("<html>" +
        // "Structures selected for local distance measure: " +
        // dialogChainsMultiple.getSelectionDescription() + "<br>" +
        // "Local distance vector(s):" + "</html>");
    }

    void selectStructures() {
        if (dialogStructures.showDialog() != DialogSelectStructures.OK) {
            return;
        }

        List<Structure> structures = dialogStructures.getStructures();
        if (structures.size() < 2) {
            JOptionPane.showMessageDialog(MainWindow.this, "At least two structures must be selected to compute global distance", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        itemSave.setEnabled(false);
        itemComputeDistances.setEnabled(true);
        itemVisualise.setEnabled(false);
        itemVisualise3D.setEnabled(false);
        itemCluster.setEnabled(false);

        panelResultsGlobalMatrix.setStructures(structures);
        layoutCards.show(panelCards, MainWindow.CARD_GLOBAL_MATRIX);
    }

    void selectChains(Object source) {
        if (dialogChains.showDialog() != DialogSelectChains.OK) {
            return;
        }

        Pair<Structure, Structure> structures = dialogChains.getStructures();
        Pair<List<Chain>, List<Chain>> chains = dialogChains.getChains();

        if (chains.getLeft().size() == 0 || chains.getRight().size() == 0) {
            String message = "No chains specified for structure: " + StructureManager.getName(structures.getLeft()) + " or " + StructureManager.getName(structures.getRight());
            JOptionPane.showMessageDialog(MainWindow.this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (source.equals(itemSelectStructuresCompare)) {
            itemSave.setEnabled(false);
            itemComputeDistances.setEnabled(true);
            itemVisualise.setEnabled(false);
            itemVisualise3D.setEnabled(false);
            itemCluster.setEnabled(false);
            itemComputeAlign.setEnabled(false);

            panelResultsLocalMatrix.setStructuresAndChains(structures, chains);
            layoutCards.show(panelCards, MainWindow.CARD_LOCAL_MATRIX);
        } else if (source.equals(itemSelectStructuresAlign)) {
            itemSave.setEnabled(false);
            itemComputeDistances.setEnabled(false);
            itemVisualise.setEnabled(false);
            itemVisualise3D.setEnabled(false);
            itemCluster.setEnabled(false);
            itemComputeAlign.setEnabled(true);

            panelResultsAlignStruc.setStructuresAndChains(structures, chains);
            layoutCards.show(panelCards, MainWindow.CARD_ALIGN_STRUC);
        }
    }

    void selectChainsMultiple(Object source) {
        if (dialogChainsMultiple.showDialog() != DialogSelectChainsMultiple.OK) {
            return;
        }

        if (dialogChainsMultiple.getChains().size() < 2) {
            JOptionPane.showMessageDialog(this, "You have to select at least two chains", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<PdbCompactFragment> fragments = dialogChainsMultiple.getChains();
        MoleculeType type = fragments.get(0).getMoleculeType();

        for (PdbCompactFragment c : fragments) {
            if (type != c.getMoleculeType()) {
                JOptionPane.showMessageDialog(this, "Cannot align/compare structures: different types", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        if (source.equals(itemSelectStructuresCompare)) {
            itemSave.setEnabled(false);
            itemComputeDistances.setEnabled(true);
            itemVisualise.setEnabled(false);
            itemVisualise3D.setEnabled(false);
            itemCluster.setEnabled(false);
            itemComputeAlign.setEnabled(false);

            panelResultsLocalMultiMatrix.setFragments(fragments);
            layoutCards.show(panelCards, MainWindow.CARD_LOCAL_MULTI_MATRIX);
        } else if (source.equals(itemSelectStructuresAlign)) {
            itemSave.setEnabled(false);
            itemComputeDistances.setEnabled(false);
            itemVisualise.setEnabled(false);
            itemVisualise3D.setEnabled(false);
            itemCluster.setEnabled(false);
            itemComputeAlign.setEnabled(true);

            panelResultsAlignSeq.setFragments(fragments, radioAlignSeqGlobal.isSelected());
            layoutCards.show(panelCards, MainWindow.CARD_ALIGN_SEQ);
        }
    }

    private void saveResults() {
        if (exportable != null) {
            pdbChooser.setSelectedFile(exportable.suggestName());
            int option = pdbChooser.showSaveDialog(MainWindow.this);

            if (option == JFileChooser.APPROVE_OPTION) {
                try {
                    exportable.export(pdbChooser.getSelectedFile());
                    JOptionPane.showMessageDialog(MainWindow.this, "Successfully exported the results!", "Information", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException exception) {
                    JOptionPane.showMessageDialog(MainWindow.this, "Failed to export results, reason: " + exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
