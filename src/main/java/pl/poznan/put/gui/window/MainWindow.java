package pl.poznan.put.gui.window;

import darrylbu.component.StayOpenCheckBoxMenuItem;
import darrylbu.component.StayOpenRadioButtonMenuItem;
import org.apache.commons.lang3.tuple.Pair;
import pl.poznan.put.comparison.MCQ;
import pl.poznan.put.comparison.RMSD;
import pl.poznan.put.comparison.LCS;
import pl.poznan.put.comparison.global.GlobalComparator;
import pl.poznan.put.datamodel.ProcessingResult;
import pl.poznan.put.gui.panel.GlobalMatrixPanel;
import pl.poznan.put.gui.panel.LocalMatrixPanel;
import pl.poznan.put.gui.panel.LocalMultiMatrixPanel;
import pl.poznan.put.gui.panel.SequenceAlignmentPanel;
import pl.poznan.put.gui.panel.StructureAlignmentPanel;
import pl.poznan.put.gui.panel.TorsionAngleValuesMatrixPanel;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbChain;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.structure.tertiary.StructureManager;
import pl.poznan.put.types.DistanceMatrix;

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
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainWindow extends JFrame {
    private static final String RESOURCE_ICON_OPEN =
            "/toolbarButtonGraphics/general/Open16.gif";
    private static final String RESOURCE_ICON_SAVE =
            "/toolbarButtonGraphics/general/Save16.gif";

    private static final String CARD_TORSION = "CARD_TORSION";
    private static final String CARD_ALIGN_SEQ = "CARD_ALIGN_SEQ";
    private static final String CARD_ALIGN_STRUC = "CARD_ALIGN_STRUC";
    private static final String CARD_GLOBAL_MATRIX = "CARD_GLOBAL_MATRIX";
    private static final String CARD_LOCAL_MATRIX = "CARD_LOCAL_MATRIX";
    private static final String CARD_LOCAL_MULTI_MATRIX =
            "CARD_LOCAL_MULTI_MATRIX";
    private static final String TITLE =
            "MCQ4Structures: computing similarity of 3D RNA / protein "
            + "structures";
    private final JMenu menuFile = new JMenu("File");
    private final JMenuItem itemOpen = new JMenuItem("Open structure(s)",
                                                     new ImageIcon(getClass()
                                                                           .getResource(
                                                                                   MainWindow.RESOURCE_ICON_OPEN)));
    private final JMenuItem itemSave = new JMenuItem("Save results",
                                                     new ImageIcon(getClass()
                                                                           .getResource(
                                                                                   MainWindow.RESOURCE_ICON_SAVE)));
    private final JCheckBoxMenuItem checkBoxManager =
            new StayOpenCheckBoxMenuItem("View structure manager", false);
    private final JMenuItem itemExit = new JMenuItem("Exit");
    private final JMenu menuTorsionAngles = new JMenu("Torsion angles");
    private final JMenuItem itemSelectStructureTorsionAngles = new JMenuItem(
            "Select structure to represent in torsion angle space");
    private final JMenu menuDistanceMeasure = new JMenu("Distance measure");
    private final JRadioButtonMenuItem radioGlobalMcq =
            new StayOpenRadioButtonMenuItem("Global MCQ", true);
    private final JRadioButtonMenuItem radioGlobalRmsd =
            new StayOpenRadioButtonMenuItem("Global RMSD", false);
    private final JRadioButtonMenuItem radioGlobalLcs =
            new StayOpenRadioButtonMenuItem("Global LCS", false);
    private final JRadioButtonMenuItem radioLocal =
            new StayOpenRadioButtonMenuItem("Local distances (pair)", false);
    private final JRadioButtonMenuItem radioLocalMulti =
            new StayOpenRadioButtonMenuItem("Local distances (multiple)",
                                            false);
    private final JMenuItem itemSelectStructuresCompare =
            new JMenuItem("Select structures to compare");
    private final JMenuItem itemVisualise3D =
            new JMenuItem("Visualise results in 3D");
    private final JMenuItem itemCluster = new JMenuItem("Cluster results");
    private final ActionListener radioActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent arg0) {
            itemVisualise3D.setEnabled(false);
            itemCluster.setEnabled(false);
        }
    };
    private final JMenu menuAlignment = new JMenu("Alignment");
    private final JRadioButtonMenuItem radioAlignSeqGlobal =
            new StayOpenRadioButtonMenuItem("Global sequence alignment", true);
    private final JRadioButtonMenuItem radioAlignSeqLocal =
            new StayOpenRadioButtonMenuItem("Local sequence alignment", false);
    private final JRadioButtonMenuItem radioAlignStruc =
            new StayOpenRadioButtonMenuItem("3D structure alignment", false);
    private final JMenuItem itemSelectStructuresAlign =
            new JMenuItem("Select structures to align");
    private final JMenu menuHelp = new JMenu("Help");
    private final JMenuItem itemGuide = new JMenuItem("Quick guide");
    private final JMenuItem itemAbout = new JMenuItem("About");
    private final CardLayout layoutCards = new CardLayout();
    private final JPanel panelCards = new JPanel();
    private final TorsionAngleValuesMatrixPanel panelTorsionAngles =
            new TorsionAngleValuesMatrixPanel();
    private final GlobalMatrixPanel panelResultsGlobalMatrix =
            new GlobalMatrixPanel();
    private final LocalMatrixPanel panelResultsLocalMatrix =
            new LocalMatrixPanel();
    private final LocalMultiMatrixPanel panelResultsLocalMultiMatrix =
            new LocalMultiMatrixPanel();
    private final SequenceAlignmentPanel panelResultsAlignSeq =
            new SequenceAlignmentPanel();
    private final StructureAlignmentPanel panelResultsAlignStruc =
            new StructureAlignmentPanel();
    private final JFileChooser fileChooser = new JFileChooser();
    private final DialogManager dialogManager;
    private final DialogSelectStructures dialogStructures;
    private final DialogSelectChains dialogChains;
    private final DialogSelectChainsMultiple dialogChainsMultiple;
    private final DialogSelectAngles dialogAngles;
    private ProcessingResult currentResult = ProcessingResult.emptyInstance();
    private final ActionListener selectActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            assert e != null;
            JMenuItem source = (JMenuItem) e.getSource();

            if (source.equals(itemSelectStructureTorsionAngles)) {
                selectSingleStructure();
            } else if (source.equals(itemSelectStructuresCompare)) {
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

    public MainWindow(List<File> pdbs) {
        super();

        dialogManager = new DialogManager(this);
        dialogStructures = new DialogSelectStructures(this);
        dialogChains = new DialogSelectChains(this);
        dialogChainsMultiple = new DialogSelectChainsMultiple(this);
        dialogAngles = new DialogSelectAngles(this);

        dialogManager.loadStructures(pdbs);

        createMenu();
        initializeMenu();
        registerMenuActionListeners();

        panelCards.setLayout(layoutCards);
        panelCards.add(new JPanel());
        panelCards.add(panelTorsionAngles, MainWindow.CARD_TORSION);
        panelCards.add(panelResultsGlobalMatrix, MainWindow.CARD_GLOBAL_MATRIX);
        panelCards.add(panelResultsLocalMatrix, MainWindow.CARD_LOCAL_MATRIX);
        panelCards.add(panelResultsLocalMultiMatrix,
                       MainWindow.CARD_LOCAL_MULTI_MATRIX);
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
        JMenuBar menuBar = new JMenuBar();

        menuFile.setMnemonic(KeyEvent.VK_F);
        menuFile.add(itemOpen);
        menuFile.add(itemSave);
        menuFile.addSeparator();
        menuFile.add(checkBoxManager);
        menuFile.addSeparator();
        menuFile.add(itemExit);
        menuBar.add(menuFile);

        menuTorsionAngles.setMnemonic(KeyEvent.VK_T);
        menuTorsionAngles.add(itemSelectStructureTorsionAngles);
        menuBar.add(menuTorsionAngles);

        menuDistanceMeasure.setMnemonic(KeyEvent.VK_D);
        menuDistanceMeasure.add(new JLabel("    Select distance type:"));
        menuDistanceMeasure.add(radioGlobalMcq);
        menuDistanceMeasure.add(radioGlobalRmsd);
        menuDistanceMeasure.add(radioGlobalLcs);
        menuDistanceMeasure.add(radioLocal);
        menuDistanceMeasure.add(radioLocalMulti);
        menuDistanceMeasure.addSeparator();
        menuDistanceMeasure.add(itemSelectStructuresCompare);
        menuDistanceMeasure.addSeparator();
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
        menuBar.add(menuAlignment);

        menuHelp.setMnemonic(KeyEvent.VK_H);
        menuHelp.add(itemGuide);
        menuHelp.add(itemAbout);
        menuBar.add(menuHelp);

        setJMenuBar(menuBar);
    }

    private void initializeMenu() {
        itemSave.setEnabled(false);
        itemVisualise3D.setEnabled(false);
        itemCluster.setEnabled(false);

        ButtonGroup group = new ButtonGroup();
        group.add(radioGlobalMcq);
        group.add(radioGlobalRmsd);
        group.add(radioGlobalLcs);
        group.add(radioLocal);
        group.add(radioLocalMulti);

        ButtonGroup groupAlign = new ButtonGroup();
        groupAlign.add(radioAlignSeqGlobal);
        groupAlign.add(radioAlignSeqLocal);
        groupAlign.add(radioAlignStruc);
    }

    private void registerMenuActionListeners() {
        radioGlobalMcq.addActionListener(radioActionListener);
        radioGlobalRmsd.addActionListener(radioActionListener);
        radioGlobalLcs.addActionListener(radioActionListener);
        radioLocal.addActionListener(radioActionListener);
        radioLocalMulti.addActionListener(radioActionListener);

        itemSelectStructureTorsionAngles
                .addActionListener(selectActionListener);
        itemSelectStructuresCompare.addActionListener(selectActionListener);
        itemSelectStructuresAlign.addActionListener(selectActionListener);

        radioAlignSeqGlobal.addActionListener(radioActionListener);

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
                dispatchEvent(new WindowEvent(MainWindow.this,
                                              WindowEvent.WINDOW_CLOSING));
            }
        });

        itemVisualise3D.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentResult.canVisualize()) {
                    currentResult.visualize3D();
                }
            }
        });

        itemCluster.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (currentResult.canCluster()) {
                    DistanceMatrix distanceMatrix =
                            currentResult.getDataForClustering();
                    double[][] array = distanceMatrix.getMatrix();

                    if (array.length <= 1) {
                        String message =
                                "Cannot cluster this distance matrix, because"
                                + " it contains zero valid comparisons";
                        JOptionPane.showMessageDialog(null, message, "Warning",
                                                      JOptionPane
                                                              .WARNING_MESSAGE);
                        return;
                    }

                    DialogCluster dialogClustering =
                            new DialogCluster(distanceMatrix);
                    dialogClustering.setVisible(true);
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

    private void saveResults() {
        if (currentResult.canExport()) {
            File suggestedName = currentResult.suggestName();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setSelectedFile(suggestedName);

            if (fileChooser.showSaveDialog(this)
                == JFileChooser.APPROVE_OPTION) {
                try (OutputStream stream = new FileOutputStream(
                        fileChooser.getSelectedFile())) {
                    currentResult.export(stream);
                    JOptionPane.showMessageDialog(MainWindow.this,
                                                  "Successfully exported the "
                                                  + "results!",
                                                  "Information",
                                                  JOptionPane
                                                          .INFORMATION_MESSAGE);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(MainWindow.this,
                                                  "Failed to export the "
                                                  + "results, reason: "
                                                  + e.getMessage(), "Error",
                                                  JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    public static void main(final String[] args) {
        final List<File> pdbs = new ArrayList<>();

        for (String argument : args) {
            File file = new File(argument);
            if (file.canRead()) {
                pdbs.add(file);
            }
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                /*
                 * Set L&F
                 */
                for (LookAndFeelInfo info : UIManager
                        .getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        try {
                            UIManager.setLookAndFeel(info.getClassName());
                        } catch (ClassNotFoundException |
                                InstantiationException |
                                IllegalAccessException |
                                UnsupportedLookAndFeelException e) {
                            // do nothing
                        }
                        break;
                    }
                }

                MainWindow window = new MainWindow(pdbs);
                window.setVisible(true);
            }
        });
    }

    private void selectSingleStructure() {
        itemSave.setEnabled(false);

        List<String> names = StructureManager.getAllNames();
        String[] selectionValues = names.toArray(new String[names.size()]);
        String name = (String) JOptionPane
                .showInputDialog(this, "Select structure",
                                 "Represent structure in torsion angle space",
                                 JOptionPane.QUESTION_MESSAGE, null,
                                 selectionValues, null);

        if (name != null) {
            PdbModel structure = StructureManager.getStructure(name);
            currentResult =
                    panelTorsionAngles.calculateTorsionAngles(structure);
            layoutCards.show(panelCards, MainWindow.CARD_TORSION);
            updateMenuEnabledStates();
        }
    }

    private void updateMenuEnabledStates() {
        itemSave.setEnabled(currentResult.canExport());
        itemCluster.setEnabled(currentResult.canCluster());
        itemVisualise3D.setEnabled(currentResult.canVisualize());

        if (currentResult.canExport()) {
            itemSave.setText(
                    "Save results (" + currentResult.getExportFormat() + ")");
        }
    }

    private void selectStructures() {
        if (dialogStructures.showDialog() != DialogSelectStructures.OK) {
            return;
        }

        List<PdbModel> structures = dialogStructures.getStructures();
        if (structures.size() < 2) {
            JOptionPane.showMessageDialog(MainWindow.this,
                                          "At least two structures must be "
                                          + "selected to compute global "
                                          + "distance", "Information",
                                          JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        itemSave.setEnabled(false);
        itemVisualise3D.setEnabled(false);
        itemCluster.setEnabled(false);

        panelResultsGlobalMatrix.setStructures(structures);
        layoutCards.show(panelCards, MainWindow.CARD_GLOBAL_MATRIX);
        compareGlobal();
    }

    private void compareGlobal() {

        GlobalComparator comparator =
                (radioGlobalMcq.isSelected() ? new MCQ() :
                 radioGlobalRmsd.isSelected() ? new RMSD() : 
                                                new LCS());
        panelResultsGlobalMatrix.compareAndDisplayMatrix(comparator,
                                                         new GlobalMatrixPanel.Callback() {
                                                             @Override
                                                             public void
                                                             complete(
                                                                     ProcessingResult processingResult) {
                                                                 currentResult =
                                                                         processingResult;
                                                                 layoutCards
                                                                         .show(panelCards,
                                                                               MainWindow.CARD_GLOBAL_MATRIX);
                                                                 updateMenuEnabledStates();

                                                             }
                                                         });
    }

    private void selectChains(JMenuItem source) {
        if (dialogChains.showDialog() != DialogSelectChains.OK) {
            return;
        }

        Pair<PdbModel, PdbModel> structures = dialogChains.getStructures();
        Pair<List<PdbChain>, List<PdbChain>> chains = dialogChains.getChains();

        if (chains.getLeft().size() == 0 || chains.getRight().size() == 0) {
            String message =
                    "No chains specified for structure: " + StructureManager
                            .getName(structures.getLeft()) + " or "
                    + StructureManager.getName(structures.getRight());
            JOptionPane
                    .showMessageDialog(MainWindow.this, message, "Information",
                                       JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (source.equals(itemSelectStructuresCompare)) {
            itemSave.setEnabled(false);
            itemVisualise3D.setEnabled(false);
            itemCluster.setEnabled(false);

            panelResultsLocalMatrix.setStructuresAndChains(structures, chains);
            layoutCards.show(panelCards, MainWindow.CARD_LOCAL_MATRIX);
            compareLocalPair();
        } else if (source.equals(itemSelectStructuresAlign)) {
            itemSave.setEnabled(false);
            itemVisualise3D.setEnabled(false);
            itemCluster.setEnabled(false);

            panelResultsAlignStruc.setStructuresAndChains(structures, chains);
            layoutCards.show(panelCards, MainWindow.CARD_ALIGN_STRUC);
            alignStructures();
        }
    }

    private void compareLocalPair() {
        if (dialogAngles.showDialog() == DialogSelectAngles.OK) {
            currentResult = panelResultsLocalMatrix
                    .compareAndDisplayTable(dialogAngles.getAngles());
            layoutCards.show(panelCards, MainWindow.CARD_LOCAL_MATRIX);
            updateMenuEnabledStates();
        }
    }

    private void alignStructures() {
        currentResult = panelResultsAlignStruc.alignAndDisplayStructures();
        layoutCards.show(panelCards, MainWindow.CARD_ALIGN_STRUC);
        updateMenuEnabledStates();
    }

    private void selectChainsMultiple(JMenuItem source) {
        if (dialogChainsMultiple
                    .showDialog(source.equals(itemSelectStructuresCompare))
            != DialogSelectChainsMultiple.OK) {
            return;
        }

        if (dialogChainsMultiple.getChains().size() < 2) {
            JOptionPane.showMessageDialog(this,
                                          "You have to select at least two "
                                          + "chains", "Warning",
                                          JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<PdbCompactFragment> fragments = dialogChainsMultiple.getChains();
        MoleculeType type = fragments.get(0).getMoleculeType();

        for (PdbCompactFragment c : fragments) {
            if (type != c.getMoleculeType()) {
                JOptionPane.showMessageDialog(this,
                                              "Cannot align/compare "
                                              + "structures: different types",
                                              "Error",
                                              JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        if (source.equals(itemSelectStructuresCompare)) {
            itemSave.setEnabled(false);
            itemVisualise3D.setEnabled(false);
            itemCluster.setEnabled(false);

            panelResultsLocalMultiMatrix.setFragments(fragments);
            layoutCards.show(panelCards, MainWindow.CARD_LOCAL_MULTI_MATRIX);
            compareLocalMulti();
        } else if (source.equals(itemSelectStructuresAlign)) {
            itemSave.setEnabled(false);
            itemVisualise3D.setEnabled(false);
            itemCluster.setEnabled(false);

            panelResultsAlignSeq
                    .setFragments(fragments, radioAlignSeqGlobal.isSelected());
            layoutCards.show(panelCards, MainWindow.CARD_ALIGN_SEQ);
            alignSequences();
        }
    }

    private void compareLocalMulti() {
        currentResult = panelResultsLocalMultiMatrix.compareAndDisplayTable();
        layoutCards.show(panelCards, MainWindow.CARD_LOCAL_MULTI_MATRIX);
        updateMenuEnabledStates();
    }

    private void alignSequences() {
        currentResult = panelResultsAlignSeq.alignAndDisplaySequences();
        layoutCards.show(panelCards, MainWindow.CARD_ALIGN_SEQ);
        updateMenuEnabledStates();
    }
}
