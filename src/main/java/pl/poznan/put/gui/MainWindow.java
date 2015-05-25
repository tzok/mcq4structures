package pl.poznan.put.gui;

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

import org.apache.commons.lang3.tuple.Pair;
import org.w3c.dom.svg.SVGDocument;

import pl.poznan.put.comparison.global.MeasureType;
import pl.poznan.put.gui.dialog.DialogAbout;
import pl.poznan.put.gui.dialog.DialogGuide;
import pl.poznan.put.gui.dialog.DialogManager;
import pl.poznan.put.gui.dialog.DialogSelectAngles;
import pl.poznan.put.gui.dialog.DialogSelectChains;
import pl.poznan.put.gui.dialog.DialogSelectChainsMultiple;
import pl.poznan.put.gui.dialog.DialogSelectStructures;
import pl.poznan.put.gui.panel.GlobalMatrixPanel;
import pl.poznan.put.gui.panel.LocalMatrixPanel;
import pl.poznan.put.gui.panel.LocalMultiMatrixPanel;
import pl.poznan.put.gui.panel.SequenceAlignmentPanel;
import pl.poznan.put.gui.panel.StructureAlignmentPanel;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbChain;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.structure.tertiary.StructureManager;
import pl.poznan.put.utility.svg.Format;
import pl.poznan.put.utility.svg.SVGHelper;
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
                for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        try {
                            UIManager.setLookAndFeel(info.getClassName());
                        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
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

    private final ActionListener radioActionListener = new ActionListener() {
        private Object sourcePrev = radioGlobalMcq;

        @Override
        public void actionPerformed(ActionEvent arg0) {
            assert arg0 != null;

            Object source = arg0.getSource();
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
    private final ActionListener radioAlignListener = new ActionListener() {
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
    private final ActionListener selectActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            assert e != null;
            JMenuItem source = (JMenuItem) e.getSource();

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

    private final JMenu menuFile = new JMenu("File");
    private final JMenuItem itemOpen = new JMenuItem("Open structure(s)", new ImageIcon(getClass().getResource(MainWindow.RESOURCE_ICON_OPEN)));
    private final JMenuItem itemSave = new JMenuItem("Save results", new ImageIcon(getClass().getResource(MainWindow.RESOURCE_ICON_SAVE)));
    private final JMenuItem itemSaveVisualization = new JMenuItem("Save visualization", new ImageIcon(getClass().getResource(MainWindow.RESOURCE_ICON_SAVE)));
    private final JCheckBoxMenuItem checkBoxManager = new StayOpenCheckBoxMenuItem("View structure manager", false);
    private final JMenuItem itemExit = new JMenuItem("Exit");

    private final JMenu menuDistanceMeasure = new JMenu("Distance measure");
    private final JRadioButtonMenuItem radioGlobalMcq = new StayOpenRadioButtonMenuItem("Global MCQ", true);
    private final JRadioButtonMenuItem radioGlobalRmsd = new StayOpenRadioButtonMenuItem("Global RMSD", false);
    private final JRadioButtonMenuItem radioLocal = new StayOpenRadioButtonMenuItem("Local distances (pair)", false);
    private final JRadioButtonMenuItem radioLocalMulti = new StayOpenRadioButtonMenuItem("Local distances (multiple)", false);
    private final JMenuItem itemSelectStructuresCompare = new JMenuItem("Select structures to compare");
    private final JMenuItem itemComputeDistances = new JMenuItem("Compute distance(s)");
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

    private final JFileChooser fileChooser = new JFileChooser();

    private final DialogManager dialogManager;
    private final DialogSelectStructures dialogStructures;
    private final DialogSelectChains dialogChains;
    private final DialogSelectChainsMultiple dialogChainsMultiple;
    private final DialogSelectAngles dialogAngles;

    private ProcessingResult currentResult = ProcessingResult.emptyInstance();

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
        JMenuBar menuBar = new JMenuBar();

        menuFile.setMnemonic(KeyEvent.VK_F);
        menuFile.add(itemOpen);
        menuFile.add(itemSave);
        menuFile.add(itemSaveVisualization);
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
        menuDistanceMeasure.add(itemSelectStructuresCompare);
        menuDistanceMeasure.addSeparator();
        menuDistanceMeasure.add(itemComputeDistances);
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
    }

    private void initializeMenu() {
        itemSave.setEnabled(false);
        itemSaveVisualization.setEnabled(false);
        itemComputeDistances.setEnabled(false);
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
    }

    private void registerMenuActionListeners() {
        radioGlobalMcq.addActionListener(radioActionListener);
        radioGlobalRmsd.addActionListener(radioActionListener);
        radioLocal.addActionListener(radioActionListener);
        radioLocalMulti.addActionListener(radioActionListener);

        itemSelectStructuresCompare.addActionListener(selectActionListener);
        itemSelectStructuresAlign.addActionListener(selectActionListener);

        radioAlignSeqGlobal.addActionListener(radioActionListener);
        radioAlignSeqLocal.addActionListener(radioAlignListener);
        radioAlignStruc.addActionListener(radioAlignListener);

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

        itemSaveVisualization.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveVisualizations();
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
                    currentResult.cluster();
                }
            }
        });

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

    private void alignSequences() {
        currentResult = panelResultsAlignSeq.alignAndDisplaySequences();
        layoutCards.show(panelCards, MainWindow.CARD_ALIGN_SEQ);
        updateMenuEnabledStates();
    }

    private void alignStructures() {
        currentResult = panelResultsAlignStruc.alignAndDisplayStructures();
        layoutCards.show(panelCards, MainWindow.CARD_ALIGN_STRUC);
        updateMenuEnabledStates();
    }

    private void compareGlobal() {
        MeasureType measure = radioGlobalMcq.isSelected() ? MeasureType.MCQ : MeasureType.RMSD;
        panelResultsGlobalMatrix.compareAndDisplayMatrix(measure, new GlobalMatrixPanel.Callback() {
            @Override
            public void complete(ProcessingResult processingResult) {
                currentResult = processingResult;
                layoutCards.show(panelCards, MainWindow.CARD_GLOBAL_MATRIX);
                updateMenuEnabledStates();

            }
        });
    }

    private void compareLocalPair() {
        if (dialogAngles.showDialog() == DialogSelectAngles.OK) {
            currentResult = panelResultsLocalMatrix.compareAndDisplayTable(dialogAngles.getAngles());
            layoutCards.show(panelCards, MainWindow.CARD_LOCAL_MATRIX);
            updateMenuEnabledStates();
        }
    }

    private void compareLocalMulti() {
        currentResult = panelResultsLocalMultiMatrix.compareAndDisplayTable();
        layoutCards.show(panelCards, MainWindow.CARD_LOCAL_MULTI_MATRIX);
        updateMenuEnabledStates();
    }

    private void selectStructures() {
        if (dialogStructures.showDialog() != DialogSelectStructures.OK) {
            return;
        }

        List<PdbModel> structures = dialogStructures.getStructures();
        if (structures.size() < 2) {
            JOptionPane.showMessageDialog(MainWindow.this, "At least two structures must be selected to compute global distance", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        itemSave.setEnabled(false);
        itemSaveVisualization.setEnabled(false);
        itemComputeDistances.setEnabled(true);
        itemVisualise3D.setEnabled(false);
        itemCluster.setEnabled(false);

        panelResultsGlobalMatrix.setStructures(structures);
        layoutCards.show(panelCards, MainWindow.CARD_GLOBAL_MATRIX);
    }

    private void selectChains(JMenuItem source) {
        if (dialogChains.showDialog() != DialogSelectChains.OK) {
            return;
        }

        Pair<PdbModel, PdbModel> structures = dialogChains.getStructures();
        Pair<List<PdbChain>, List<PdbChain>> chains = dialogChains.getChains();

        if (chains.getLeft().size() == 0 || chains.getRight().size() == 0) {
            String message = "No chains specified for structure: " + StructureManager.getName(structures.getLeft()) + " or " + StructureManager.getName(structures.getRight());
            JOptionPane.showMessageDialog(MainWindow.this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (source.equals(itemSelectStructuresCompare)) {
            itemSave.setEnabled(false);
            itemSaveVisualization.setEnabled(false);
            itemComputeDistances.setEnabled(true);
            itemVisualise3D.setEnabled(false);
            itemCluster.setEnabled(false);
            itemComputeAlign.setEnabled(false);

            panelResultsLocalMatrix.setStructuresAndChains(structures, chains);
            layoutCards.show(panelCards, MainWindow.CARD_LOCAL_MATRIX);
        } else if (source.equals(itemSelectStructuresAlign)) {
            itemSave.setEnabled(false);
            itemSaveVisualization.setEnabled(false);
            itemComputeDistances.setEnabled(false);
            itemVisualise3D.setEnabled(false);
            itemCluster.setEnabled(false);
            itemComputeAlign.setEnabled(true);

            panelResultsAlignStruc.setStructuresAndChains(structures, chains);
            layoutCards.show(panelCards, MainWindow.CARD_ALIGN_STRUC);
        }
    }

    private void selectChainsMultiple(JMenuItem source) {
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
            itemSaveVisualization.setEnabled(false);
            itemComputeDistances.setEnabled(true);
            itemVisualise3D.setEnabled(false);
            itemCluster.setEnabled(false);
            itemComputeAlign.setEnabled(false);

            panelResultsLocalMultiMatrix.setFragments(fragments);
            layoutCards.show(panelCards, MainWindow.CARD_LOCAL_MULTI_MATRIX);
        } else if (source.equals(itemSelectStructuresAlign)) {
            itemSave.setEnabled(false);
            itemSaveVisualization.setEnabled(false);
            itemComputeDistances.setEnabled(false);
            itemVisualise3D.setEnabled(false);
            itemCluster.setEnabled(false);
            itemComputeAlign.setEnabled(true);

            panelResultsAlignSeq.setFragments(fragments, radioAlignSeqGlobal.isSelected());
            layoutCards.show(panelCards, MainWindow.CARD_ALIGN_SEQ);
        }
    }

    private void saveResults() {
        if (currentResult.canExport()) {
            File suggestedName = currentResult.suggestName();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setSelectedFile(suggestedName);

            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try (OutputStream stream = new FileOutputStream(fileChooser.getSelectedFile())) {
                    currentResult.export(stream);
                    JOptionPane.showMessageDialog(MainWindow.this, "Successfully exported the results!", "Information", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(MainWindow.this, "Failed to export the results, reason: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void saveVisualizations() {
        if (currentResult.canVisualize()) {
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                List<SVGDocument> visualizations = currentResult.getVisualizations();
                File directory = fileChooser.getSelectedFile();

                for (int i = 0; i < visualizations.size(); i++) {
                    String filename = visualizations.size() > 1 ? String.format("visualization-%d.svg", i) : "visualization.svg";
                    SVGDocument document = visualizations.get(i);

                    try (OutputStream stream = new FileOutputStream(new File(directory, filename))) {
                        SVGHelper.export(document, stream, Format.SVG, null);
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(MainWindow.this, "Failed to export the visualizations, reason: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                JOptionPane.showMessageDialog(MainWindow.this, "Successfully exported the visualizations!", "Information", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void updateMenuEnabledStates() {
        itemSave.setEnabled(currentResult.canExport());
        itemSaveVisualization.setEnabled(currentResult.canVisualize());
        itemCluster.setEnabled(currentResult.canCluster());
        itemVisualise3D.setEnabled(currentResult.canVisualize());

        if (currentResult.canExport()) {
            itemSave.setText("Save results (" + currentResult.getExportFormat() + ")");
        }
    }
}
