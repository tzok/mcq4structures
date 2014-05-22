package pl.poznan.put.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.apache.commons.lang3.tuple.Pair;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.align.gui.jmol.JmolPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.poznan.put.common.AverageAngle;
import pl.poznan.put.common.ChiTorsionAngleType;
import pl.poznan.put.common.MoleculeType;
import pl.poznan.put.common.TorsionAngle;
import pl.poznan.put.comparison.GlobalComparator;
import pl.poznan.put.comparison.GlobalComparisonResultMatrix;
import pl.poznan.put.comparison.IncomparableStructuresException;
import pl.poznan.put.comparison.LocalComparisonResult;
import pl.poznan.put.comparison.MCQ;
import pl.poznan.put.comparison.MCQLocalComparisonResult;
import pl.poznan.put.comparison.ModelsComparisonResult;
import pl.poznan.put.comparison.ParallelGlobalComparison;
import pl.poznan.put.comparison.ParallelGlobalComparison.ComparisonListener;
import pl.poznan.put.comparison.RMSD;
import pl.poznan.put.comparison.RMSD.AtomFilter;
import pl.poznan.put.helper.Constants;
import pl.poznan.put.interfaces.Clusterable;
import pl.poznan.put.interfaces.Exportable;
import pl.poznan.put.interfaces.Visualizable;
import pl.poznan.put.nucleic.PseudophasePuckerAngle;
import pl.poznan.put.nucleic.RNATorsionAngle;
import pl.poznan.put.protein.ProteinTorsionAngle;
import pl.poznan.put.structure.CompactFragment;
import pl.poznan.put.structure.StructureSelection;
import pl.poznan.put.structure.StructureSelectionFactory;
import pl.poznan.put.utility.StructureManager;
import darrylbu.component.StayOpenCheckBoxMenuItem;
import darrylbu.component.StayOpenRadioButtonMenuItem;

public class MainWindow extends JFrame implements ComparisonListener {
    private static final String CARD_ALIGN_SEQ = "CARD_ALIGN_SEQ";
    private static final String CARD_ALIGN_STRUC = "CARD_ALIGN_STRUC";

    private static final String CARD_MATRIX = "CARD_MATRIX";
    private static final long serialVersionUID = 1L;
    private static final String TITLE = "MCQ4Structures: computing similarity "
            + "of 3D RNA / protein structures";
    static final Logger LOGGER = LoggerFactory.getLogger(MainWindow.class);

    JCheckBoxMenuItem checkBoxManager;
    TableCellRenderer colorsRenderer;
    DialogAngles dialogAngles;
    DialogChains dialogChains;

    DialogChainsMultiple dialogChainsMultiple;
    DialogManager dialogManager;
    DialogStructures dialogStructures;

    JMenuItem itemAbout;
    JMenuItem itemCluster;
    JMenuItem itemComputeAlign;
    JMenuItem itemComputeDistances;

    JMenuItem itemExit;
    JMenuItem itemGuide;
    JMenuItem itemOpen;
    JMenuItem itemSave;
    JMenuItem itemSelectStructuresAlign;
    JMenuItem itemSelectStructuresCompare;
    JMenuItem itemSelectTorsion;
    JMenuItem itemVisualise;
    JMenuItem itemVisualise3D;
    JMenuItem itemVisualiseHighQuality;
    JLabel labelAlignmentStatus;

    JTextPane labelInfoAlignSeq;
    JTextPane labelInfoAlignStruc;
    JTextPane labelInfoMatrix;
    CardLayout layoutCards;
    JPanel panelCards;

    JmolPanel panelJmolLeft;
    JmolPanel panelJmolRight;

    JPanel panelResultsAlignSeq;
    JPanel panelResultsAlignStruc;
    JPanel panelResultsMatrix;
    JProgressBar progressBar;

    JRadioButtonMenuItem radioAlignSeqGlobal;
    JRadioButtonMenuItem radioAlignSeqLocal;
    JRadioButtonMenuItem radioAlignStruc;

    JRadioButtonMenuItem radioGlobalMcq;
    JRadioButtonMenuItem radioGlobalRmsd;
    JRadioButtonMenuItem radioLocal;
    JRadioButtonMenuItem radioLocalMulti;
    JTable tableMatrix;

    JTextArea textAreaAlignSeq;
    Thread threadAlignment;

    Clusterable clusterable;
    Exportable exportable;
    Visualizable visualizable;

    public MainWindow() {
        super();

        dialogManager = DialogManager.getInstance(this);
        dialogManager.setVisible(true);
        dialogStructures = DialogStructures.getInstance(this);
        dialogChains = DialogChains.getInstance(this);
        dialogChainsMultiple = DialogChainsMultiple.getInstance(this);
        dialogAngles = DialogAngles.getInstance(this);

        /*
         * Create menu
         */
        JMenuBar menuBar = new JMenuBar();

        URL resource = getClass().getResource(
                "/toolbarButtonGraphics/general/Open16.gif");
        if (resource != null) {
            itemOpen = new JMenuItem("Open structure(s)", new ImageIcon(
                    resource));
        } else {
            itemOpen = new JMenuItem("Open structure(s)");
        }
        resource = getClass().getResource(
                "/toolbarButtonGraphics/general/Save16.gif");
        if (resource != null) {
            itemSave = new JMenuItem("Save results", new ImageIcon(resource));
        } else {
            itemSave = new JMenuItem("Save results");
        }
        itemSave.setEnabled(false);
        checkBoxManager = new StayOpenCheckBoxMenuItem(
                "View structure manager", true);
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

        radioGlobalMcq = new StayOpenRadioButtonMenuItem("Global MCQ", true);
        radioGlobalRmsd = new StayOpenRadioButtonMenuItem("Global RMSD", false);
        radioLocal = new StayOpenRadioButtonMenuItem("Local distances (pair)",
                false);
        radioLocalMulti = new StayOpenRadioButtonMenuItem(
                "Local distances (multiple)", false);
        ButtonGroup group = new ButtonGroup();
        group.add(radioGlobalMcq);
        group.add(radioGlobalRmsd);
        group.add(radioLocal);
        group.add(radioLocalMulti);

        itemSelectTorsion = new JMenuItem("Select torsion angles");
        itemSelectTorsion.setEnabled(false);
        itemSelectStructuresCompare = new JMenuItem(
                "Select structures to compare");
        itemComputeDistances = new JMenuItem("Compute distance(s)");
        itemComputeDistances.setEnabled(false);
        itemVisualise = new JMenuItem("Visualise results");
        itemVisualise.setEnabled(false);
        itemVisualiseHighQuality = new JMenuItem(
                "Visualise results (high-quality)");
        itemVisualiseHighQuality.setEnabled(false);
        itemVisualise3D = new JMenuItem("Visualise results in 3D");
        itemVisualise3D.setEnabled(false);
        itemCluster = new JMenuItem("Cluster results");
        itemCluster.setEnabled(false);

        menu = new JMenu("Distance measure");
        menu.setMnemonic(KeyEvent.VK_D);
        menu.add(new JLabel("    Select distance type:"));
        menu.add(radioGlobalMcq);
        menu.add(radioGlobalRmsd);
        menu.add(radioLocal);
        menu.add(radioLocalMulti);
        menu.addSeparator();
        menu.add(itemSelectTorsion);
        menu.add(itemSelectStructuresCompare);
        menu.addSeparator();
        menu.add(itemComputeDistances);
        menu.add(itemVisualise);
        menu.add(itemVisualiseHighQuality);
        menu.add(itemVisualise3D);
        menu.add(itemCluster);
        menuBar.add(menu);

        radioAlignSeqGlobal = new StayOpenRadioButtonMenuItem(
                "Global sequence alignment", true);
        radioAlignSeqLocal = new StayOpenRadioButtonMenuItem(
                "Local sequence alignment", false);
        radioAlignStruc = new StayOpenRadioButtonMenuItem(
                "3D structure alignment", false);
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
        labelInfoMatrix = new JTextPane();
        labelInfoMatrix.setBorder(new EmptyBorder(10, 10, 10, 0));
        labelInfoMatrix.setContentType("text/html");
        labelInfoMatrix.setEditable(false);
        labelInfoMatrix.setFont(UIManager.getFont("Label.font"));
        labelInfoMatrix.setOpaque(false);
        tableMatrix = new JTable();
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);

        panelResultsMatrix = new JPanel(new BorderLayout());
        panel = new JPanel(new BorderLayout());
        panel.add(labelInfoMatrix, BorderLayout.CENTER);
        panelResultsMatrix.add(panel, BorderLayout.NORTH);
        panelResultsMatrix.add(new JScrollPane(tableMatrix),
                BorderLayout.CENTER);
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(new JLabel("Progress in computing:"));
        panel.add(progressBar);
        panelResultsMatrix.add(panel, BorderLayout.SOUTH);

        /*
         * Create panel with sequence alignment
         */
        labelInfoAlignSeq = new JTextPane();
        labelInfoAlignSeq.setBorder(new EmptyBorder(10, 10, 10, 0));
        labelInfoAlignSeq.setContentType("text/html");
        labelInfoAlignSeq.setEditable(false);
        labelInfoAlignSeq.setFont(UIManager.getFont("Label.font"));
        labelInfoAlignSeq.setOpaque(false);
        textAreaAlignSeq = new JTextArea();
        textAreaAlignSeq.setEditable(false);
        textAreaAlignSeq.setFont(new Font("Monospaced", Font.PLAIN, 20));

        panelResultsAlignSeq = new JPanel(new BorderLayout());
        panel = new JPanel(new BorderLayout());
        panel.add(labelInfoAlignSeq, BorderLayout.CENTER);
        panelResultsAlignSeq.add(panel, BorderLayout.NORTH);
        panelResultsAlignSeq.add(new JScrollPane(textAreaAlignSeq),
                BorderLayout.CENTER);

        /*
         * Create panel with structure alignment
         */
        labelInfoAlignStruc = new JTextPane();
        labelInfoAlignStruc.setBorder(new EmptyBorder(10, 10, 10, 0));
        labelInfoAlignStruc.setContentType("text/html");
        labelInfoAlignStruc.setEditable(false);
        labelInfoAlignStruc.setFont(UIManager.getFont("Label.font"));
        labelInfoAlignStruc.setOpaque(false);
        JPanel panelInfoJmol = new JPanel(new GridLayout(1, 3));
        panelInfoJmol.add(new JLabel("Whole structures (Jmol view)",
                SwingConstants.CENTER));
        labelAlignmentStatus = new JLabel("", SwingConstants.CENTER);
        panelInfoJmol.add(labelAlignmentStatus);
        panelInfoJmol.add(new JLabel("Aligned fragments (Jmol view)",
                SwingConstants.CENTER));
        panelJmolLeft = new JmolPanel();
        panelJmolLeft.executeCmd("background lightgrey; save state state_init");
        panelJmolRight = new JmolPanel();
        panelJmolRight.executeCmd("background darkgray; save state state_init");

        panelResultsAlignStruc = new JPanel(new BorderLayout());
        panel = new JPanel(new BorderLayout());
        panel.add(labelInfoAlignStruc, BorderLayout.NORTH);
        panel.add(panelInfoJmol, BorderLayout.CENTER);
        panelResultsAlignStruc.add(panel, BorderLayout.NORTH);
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
         * Prepare cell renderer for JTable
         */
        final TableCellRenderer defaultRenderer = new DefaultTableCellRenderer();
        colorsRenderer = new DefaultTableCellRenderer() {
            private static final long serialVersionUID = -7868307163707467345L;

            @Override
            public Component getTableCellRendererComponent(JTable table,
                    Object value, boolean isSelected, boolean hasFocus,
                    int row, int column) {
                Component component = defaultRenderer.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                component.setBackground(Constants.COLORS.get(column));
                return component;
            }
        };

        /*
         * Set action listeners
         */
        dialogManager.addWindowListener(new WindowAdapter() {
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
                    dialogManager.loadStructure(f);
                }
            }
        });

        itemSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Exportable exportableLocal = exportable;
                if (exportableLocal != null) {
                    JFileChooser chooser = new JFileChooser(
                            PdbChooser.getCurrentDirectory());
                    chooser.setSelectedFile(exportableLocal.suggestName());
                    int option = chooser.showSaveDialog(MainWindow.this);
                    if (option == JFileChooser.APPROVE_OPTION) {
                        try {
                            exportableLocal.export(chooser.getSelectedFile());
                            JOptionPane.showMessageDialog(MainWindow.this,
                                    "Successfully exported the results!",
                                    "Information",
                                    JOptionPane.INFORMATION_MESSAGE);
                        } catch (IOException exception) {
                            String message = "Failed to export results, reason: "
                                    + exception.getMessage();
                            JOptionPane.showMessageDialog(MainWindow.this,
                                    message, "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
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

        ActionListener radioActionListener = new ActionListener() {
            private Object sourcePrev = radioGlobalMcq;

            @Override
            public void actionPerformed(ActionEvent arg0) {
                assert arg0 != null;

                Object source = arg0.getSource();
                itemSelectTorsion.setEnabled(source.equals(radioLocal));
                itemVisualise.setEnabled(false);
                itemVisualiseHighQuality.setEnabled(false);
                itemVisualise3D.setEnabled(false);
                itemCluster.setEnabled(false);

                boolean globalCurr = source.equals(radioGlobalMcq)
                        || source.equals(radioGlobalRmsd);
                boolean globalPrev = sourcePrev.equals(radioGlobalMcq)
                        || sourcePrev.equals(radioGlobalRmsd);
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
                DialogAngles.selectAngles();
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
                final Visualizable visualizable2 = visualizable;
                if (visualizable2 != null) {
                    visualizable2.visualize();
                }
            }
        });

        itemVisualiseHighQuality.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                final Visualizable visualizable2 = visualizable;
                if (visualizable2 != null) {
                    visualizable2.visualizeHighQuality();
                }
            }
        });

        itemVisualise3D.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final Visualizable visualizable2 = visualizable;
                if (visualizable2 != null) {
                    visualizable2.visualize3D();
                }
            }
        });

        itemCluster.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                final Clusterable clusterable2 = clusterable;
                if (clusterable2 != null) {
                    clusterable2.cluster();
                }
            }
        });

        ActionListener radioAlignListener = new ActionListener() {
            private boolean isSequencePrevious = true;

            @Override
            public void actionPerformed(ActionEvent arg0) {
                assert arg0 != null;

                Object source = arg0.getSource();
                boolean isSequenceNow = source.equals(radioAlignSeqGlobal)
                        || source.equals(radioAlignSeqLocal);
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

    void alignSequences() {
        // textAreaAlignSeq.setText("");
        // layoutCards.show(panelCards, MainWindow.CARD_ALIGN_SEQ);
        //
        // List<CompactFragment> chains =
        // dialogChainsMultiple.getChains();
        // boolean isGlobal = radioAlignSeqGlobal.isSelected();
        // AlignmentSequence alignment = AlignmentSequence.newInstance(chains,
        // isGlobal);
        //
        // exportable = alignment;
        // textAreaAlignSeq.setText(alignment.getAlignment());
        // itemSave.setEnabled(true);
        // itemSave.setText("Save results (TXT)");
        //
        // if (isGlobal) {
        // labelInfoAlignSeq.setText("<html>"
        // + "Structures selected for global sequence alignment: "
        // + alignment.getTitle() + "<br>"
        // + "Global sequence alignment results:" + "</html>");
        // } else {
        // labelInfoAlignSeq.setText("<html>"
        // + "Structures selected for local sequence alignment: "
        // + alignment.getTitle() + "<br>"
        // + "Local sequence alignment results:" + "</html>");
        // }
    }

    void alignStructures() {
        // if (threadAlignment != null && threadAlignment.isAlive()) {
        // JOptionPane.showMessageDialog(null,
        // "3D structure alignment computation has not "
        // + "finished yet!", "Information",
        // JOptionPane.INFORMATION_MESSAGE);
        // return;
        // }
        //
        // Pair<Structure, Structure> structures = dialogChains.getStructures();
        // MoleculeType typeL =
        // MoleculeType.detect(structures.getLeft().getChain(
        // 0));
        // MoleculeType typeR =
        // MoleculeType.detect(structures.getRight().getChain(
        // 0));
        // if (typeL != typeR) {
        // JOptionPane.showMessageDialog(this,
        // "Cannot align structures: different molecular " + "types",
        // "Error", JOptionPane.ERROR_MESSAGE);
        // return;
        // }
        //
        // Pair<List<Chain>, List<Chain>> chains = dialogChains.getChains();
        // final Structure left = new StructureImpl();
        // left.setChains(chains.getLeft());
        // left.setPDBCode(StructureManager.getName(chains.getLeft().get(0).getParent()));
        // final Structure right = new StructureImpl();
        // right.setChains(chains.getRight());
        // right.setPDBCode(StructureManager.getName(chains.getRight().get(0).getParent()));
        //
        // panelJmolLeft.executeCmd("restore state state_init");
        // panelJmolRight.executeCmd("restore state state_init");
        // layoutCards.show(panelCards, MainWindow.CARD_ALIGN_STRUC);
        //
        // labelAlignmentStatus.setText("Processing");
        // final Timer timer = new Timer(100, new ActionListener() {
        // @Override
        // public void actionPerformed(ActionEvent arg0) {
        // String text = labelAlignmentStatus.getText();
        // int count = StringUtils.countMatches(text, ".");
        // if (count < 5) {
        // labelAlignmentStatus.setText(text + ".");
        // } else {
        // labelAlignmentStatus.setText("Processing");
        // }
        // }
        // });
        // timer.start();
        //
        // threadAlignment = new Thread(new Runnable() {
        // AlignmentOutput output;
        //
        // @Override
        // public void run() {
        // try {
        // // FIXME
        // // Helper.normalizeAtomNames(left);
        // // Helper.normalizeAtomNames(right);
        // output = AlignerStructure.align(left, right,
        // dialogChains.getSelectionDescription());
        // exportable = output;
        // } catch (StructureException e1) {
        // JOptionPane.showMessageDialog(MainWindow.this,
        // e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        // } finally {
        // timer.stop();
        //
        // SwingUtilities.invokeLater(new Runnable() {
        // private static final String JMOL_SCRIPT = "frame 0.0; "
        // + "cartoon only; "
        // + "select model=1.1; color green; "
        // + "select model=1.2; color red; ";
        //
        // @Override
        // public void run() {
        // AlignmentOutput outputLocal = output;
        // if (outputLocal == null) {
        // return;
        // }
        // StructuresAligned aligned;
        // try {
        // aligned = outputLocal.getStructures();
        // } catch (StructureException e) {
        // JOptionPane.showMessageDialog(MainWindow.this,
        // e.getMessage(), "Error",
        // JOptionPane.ERROR_MESSAGE);
        // return;
        // }
        //
        // StringBuilder builder = new StringBuilder();
        // builder.append("MODEL        1                                                                  \n");
        // builder.append(aligned.wholeLeft.toPDB());
        // builder.append("ENDMDL                                                                          \n");
        // builder.append("MODEL        2                                                                  \n");
        // builder.append(aligned.wholeRight.toPDB());
        // builder.append("ENDMDL                                                                          \n");
        //
        // JmolViewer viewer = panelJmolLeft.getViewer();
        // viewer.openStringInline(builder.toString());
        // panelJmolLeft.executeCmd(JMOL_SCRIPT);
        //
        // builder = new StringBuilder();
        // builder.append("MODEL        1                                                                  \n");
        // builder.append(aligned.filteredLeft.toPDB());
        // builder.append("ENDMDL                                                                          \n");
        // builder.append("MODEL        2                                                                  \n");
        // builder.append(aligned.filteredRight.toPDB());
        // builder.append("ENDMDL                                                                          \n");
        //
        // viewer = panelJmolRight.getViewer();
        // viewer.openStringInline(builder.toString());
        // panelJmolRight.executeCmd(JMOL_SCRIPT);
        //
        // itemSave.setEnabled(true);
        // itemSave.setText("Save results (PDB)");
        //
        // labelAlignmentStatus.setText("Computation finished");
        // labelInfoAlignStruc.setText("<html>"
        // + "Structures selected for 3D structure alignment: "
        // + dialogChains.getSelectionDescription()
        // + "<br>"
        // + "3D structure alignment results:"
        // + "</html>");
        // }
        // });
        //
        // }
        // }
        // });
        // threadAlignment.start();
    }

    void compareGlobal() {
        final GlobalComparator comparator;
        if (radioGlobalMcq.isSelected()) {
            comparator = new MCQ(MCQ.getAllAvailableTorsionAngles());
        } else { // radioRmsd.isSelected() == true
            comparator = new RMSD(AtomFilter.ALL, true);
        }

        final List<Structure> structures = dialogStructures.getStructures();
        final List<String> names = StructureManager.getNames(structures);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                List<StructureSelection> selections = new ArrayList<>();

                for (int i = 0; i < structures.size(); i++) {
                    selections.add(StructureSelectionFactory.create(
                            names.get(i), structures.get(i)));
                }

                final GlobalComparisonResultMatrix matrix = ParallelGlobalComparison.run(
                        comparator, selections, MainWindow.this);

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        clusterable = matrix;
                        exportable = matrix;
                        visualizable = matrix;

                        tableMatrix.setModel(matrix.asDisplayableTableModel());
                        tableMatrix.setDefaultRenderer(Object.class,
                                new DefaultTableCellRenderer());

                        itemSave.setEnabled(true);
                        itemSave.setText("Save results (CSV)");
                        itemVisualise.setEnabled(true);
                        itemVisualise3D.setEnabled(true);
                        itemCluster.setEnabled(true);

                        labelInfoMatrix.setText("<html>"
                                + "Structures selected for global distance measure: "
                                + dialogStructures.getSelectionDescription()
                                + "<br>" + "Global distance matrix ("
                                + matrix.getMeasureName() + "):" + "</html>");
                    }
                });
            }
        });
        thread.start();
    }

    void compareLocalPair() {
        Pair<Structure, Structure> structures = dialogChains.getStructures();
        Pair<List<Chain>, List<Chain>> chains = dialogChains.getChains();

        StructureSelection selectionL = StructureSelectionFactory.create(
                StructureManager.getName(structures.getLeft()),
                chains.getLeft());
        StructureSelection selectionR = StructureSelectionFactory.create(
                StructureManager.getName(structures.getRight()),
                chains.getRight());

        LocalComparisonResult comparisonLocal;

        try {
            progressBar.setValue(0);
            progressBar.setMaximum(1);

            MCQ mcq = new MCQ(dialogAngles.getAngles());
            comparisonLocal = mcq.compareLocally(selectionL, selectionR);
        } catch (IncomparableStructuresException e) {
            JOptionPane.showMessageDialog(MainWindow.this, e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        } finally {
            progressBar.setValue(1);
        }

        if (comparisonLocal instanceof MCQLocalComparisonResult) {
            tableMatrix.setModel(comparisonLocal.asDisplayableTableModel());
            tableMatrix.setDefaultRenderer(Object.class, colorsRenderer);
        } else {
            JOptionPane.showMessageDialog(MainWindow.this, "Cannot continue, "
                    + "the result of comparison is invalid", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        exportable = comparisonLocal;
        visualizable = comparisonLocal;

        itemSave.setEnabled(true);
        itemSave.setText("Save results (CSV)");
        itemVisualise.setEnabled(true);
        itemVisualiseHighQuality.setEnabled(true);
        itemVisualise3D.setEnabled(true);
        itemCluster.setEnabled(false);

        labelInfoMatrix.setText("<html>" + "Structures selected for local "
                + "distance measure: " + dialogChains.getSelectionDescription()
                + "<br>" + "Local distance vector(s):" + "</html>");
    }

    void compareLocalMulti() {
        List<CompactFragment> selections = dialogChainsMultiple.getChains();
        CompactFragment[] array = selections.toArray(new CompactFragment[selections.size()]);
        MoleculeType moleculeType = array[0].getChainType();
        List<TorsionAngle> angles = new ArrayList<>();

        CompactFragment reference = (CompactFragment) JOptionPane.showInputDialog(
                MainWindow.this, "Select your reference structure",
                "Reference structure", JOptionPane.INFORMATION_MESSAGE, null,
                array, array[0]);
        if (reference == null) {
            return;
        }

        if (moleculeType == MoleculeType.PROTEIN) {
            angles.addAll(Arrays.asList(ProteinTorsionAngle.values()));
            angles.addAll(ChiTorsionAngleType.getChiTorsionAngles(MoleculeType.PROTEIN));
            angles.add(AverageAngle.getInstance(MoleculeType.PROTEIN));
        } else if (moleculeType == MoleculeType.RNA) {
            angles.addAll(Arrays.asList(RNATorsionAngle.values()));
            angles.addAll(ChiTorsionAngleType.getChiTorsionAngles(MoleculeType.RNA));
            angles.add(PseudophasePuckerAngle.getInstance());
            angles.add(AverageAngle.getInstance(MoleculeType.RNA));
        }

        TorsionAngle angleType = (TorsionAngle) JOptionPane.showInputDialog(
                MainWindow.this, "Select torsion angle", "Torsion angle",
                JOptionPane.INFORMATION_MESSAGE, null,
                angles.toArray(new TorsionAngle[angles.size()]),
                AverageAngle.getInstance(moleculeType));
        if (angleType == null) {
            return;
        }

        List<TorsionAngle> selectedAngles = new ArrayList<>();

        if (angleType.equals(AverageAngle.getInstance(moleculeType))) {
            selectedAngles.addAll(moleculeType.getBackboneTorsionAngles());
            selectedAngles.addAll(ChiTorsionAngleType.getChiTorsionAngles(moleculeType));
        } else if (angleType.equals(PseudophasePuckerAngle.getInstance())) {
            selectedAngles.add(RNATorsionAngle.TAU0);
            selectedAngles.add(RNATorsionAngle.TAU1);
            selectedAngles.add(RNATorsionAngle.TAU2);
            selectedAngles.add(RNATorsionAngle.TAU3);
            selectedAngles.add(RNATorsionAngle.TAU4);
        }

        selectedAngles.add(angleType);
        selections.remove(reference);
        ModelsComparisonResult result;

        progressBar.setMaximum(1);
        progressBar.setValue(0);

        try {
            MCQ mcq = new MCQ(selectedAngles);
            result = mcq.compareModels(reference, selections);
        } catch (IncomparableStructuresException e) {
            JOptionPane.showMessageDialog(MainWindow.this, e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        progressBar.setValue(1);
        exportable = result;
        visualizable = result;

        tableMatrix.setModel(result.asDisplayableTableModel());
        tableMatrix.setDefaultRenderer(Object.class,
                new DefaultTableCellRenderer());

        itemSave.setEnabled(true);
        itemSave.setText("Save results (CSV)");
        itemVisualise.setEnabled(true);
        itemVisualiseHighQuality.setEnabled(true);
        itemVisualise3D.setEnabled(true);
        itemCluster.setEnabled(false);

        labelInfoMatrix.setText("<html>"
                + "Structures selected for local distance measure: "
                + dialogChainsMultiple.getSelectionDescription() + "<br>"
                + "Local distance vector(s):" + "</html>");

    }

    void selectChains(Object source) {
        if (dialogChains.showDialog() != DialogChains.OK) {
            return;
        }

        Pair<Structure, Structure> structures = dialogChains.getStructures();
        Pair<List<Chain>, List<Chain>> chains = dialogChains.getChains();
        if (chains.getLeft().size() == 0 || chains.getRight().size() == 0) {
            String message = "No chains specified for structure: "
                    + StructureManager.getName(structures.getLeft()) + " or "
                    + StructureManager.getName(structures.getRight());
            JOptionPane.showMessageDialog(MainWindow.this, message,
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (source.equals(itemSelectStructuresCompare)) {
            tableMatrix.setModel(new DefaultTableModel());
            layoutCards.show(panelCards, MainWindow.CARD_MATRIX);

            itemSave.setEnabled(false);
            itemComputeDistances.setEnabled(true);
            itemVisualise.setEnabled(false);
            itemVisualiseHighQuality.setEnabled(false);
            itemVisualise3D.setEnabled(false);
            itemCluster.setEnabled(false);
            itemComputeAlign.setEnabled(false);

            labelInfoMatrix.setText("<html>Structures selected for local distance measure: "
                    + dialogChains.getSelectionDescription() + "</html>");
        } else { // source.equals(itemSelectChainsAlignStruc)
            panelJmolLeft.executeCmd("restore state " + "state_init");
            panelJmolRight.executeCmd("restore state " + "state_init");
            layoutCards.show(panelCards, MainWindow.CARD_ALIGN_STRUC);

            itemSave.setEnabled(false);
            itemComputeDistances.setEnabled(false);
            itemVisualise.setEnabled(false);
            itemVisualiseHighQuality.setEnabled(false);
            itemVisualise3D.setEnabled(false);
            itemCluster.setEnabled(false);
            itemComputeAlign.setEnabled(true);

            labelInfoAlignStruc.setText("<html>Structures selected for 3D structure alignment: "
                    + dialogChains.getSelectionDescription() + "</html>");
        }
    }

    void selectChainsMultiple(Object source) {
        if (dialogChainsMultiple.showDialog() != DialogChainsMultiple.OK) {
            return;
        }

        if (dialogChainsMultiple.getChains().size() < 2) {
            JOptionPane.showMessageDialog(this,
                    "You have to select at least two chains", "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<CompactFragment> selections = dialogChainsMultiple.getChains();
        MoleculeType type = selections.get(0).getChainType();

        for (CompactFragment c : selections) {
            if (type != c.getChainType()) {
                JOptionPane.showMessageDialog(this, "Cannot align/compare "
                        + "structures: different types", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        if (source.equals(itemSelectStructuresCompare)) {
            tableMatrix.setModel(new DefaultTableModel());
            layoutCards.show(panelCards, MainWindow.CARD_MATRIX);

            itemSave.setEnabled(false);
            itemComputeDistances.setEnabled(true);
            itemVisualise.setEnabled(false);
            itemVisualiseHighQuality.setEnabled(false);
            itemVisualise3D.setEnabled(false);
            itemCluster.setEnabled(false);
            itemComputeAlign.setEnabled(false);

            labelInfoMatrix.setText("<html>Structures selected for local distance measure: "
                    + dialogChainsMultiple.getSelectionDescription()
                    + "</html>");
        } else { // source.equals(itemSelectStructuresAlign)
            textAreaAlignSeq.setText("");
            layoutCards.show(panelCards, MainWindow.CARD_ALIGN_SEQ);

            itemSave.setEnabled(false);
            itemComputeDistances.setEnabled(false);
            itemVisualise.setEnabled(false);
            itemVisualiseHighQuality.setEnabled(false);
            itemVisualise3D.setEnabled(false);
            itemCluster.setEnabled(false);
            itemComputeAlign.setEnabled(true);

            labelInfoAlignSeq.setText("<html>Structures selected for "
                    + (radioAlignSeqGlobal.isSelected() ? "global" : "local")
                    + " sequence alignment: "
                    + dialogChainsMultiple.getSelectionDescription()
                    + "</html>");
        }
    }

    void selectStructures() {
        if (dialogStructures.showDialog() != DialogStructures.OK) {
            return;
        }
        List<Structure> structures = dialogStructures.getStructures();
        if (structures.size() < 2) {
            JOptionPane.showMessageDialog(MainWindow.this, "At "
                    + "least two structures must be selected to "
                    + "compute global distance", "Information",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        tableMatrix.setModel(new DefaultTableModel());
        layoutCards.show(panelCards, MainWindow.CARD_MATRIX);

        itemSave.setEnabled(false);
        itemComputeDistances.setEnabled(true);
        itemVisualise.setEnabled(false);
        itemVisualiseHighQuality.setEnabled(false);
        itemVisualise3D.setEnabled(false);
        itemCluster.setEnabled(false);

        labelInfoMatrix.setText("<html>Structures selected for global distance "
                + "measure: "
                + dialogStructures.getSelectionDescription()
                + "</html>");
    }

    @Override
    public void stateChanged(long all, long completed) {
        progressBar.setMaximum((int) all);
        progressBar.setValue((int) completed);
    }
}
