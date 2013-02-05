package pl.poznan.put.cs.bioserver.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import org.biojava.bio.structure.ResidueNumber;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
import org.jfree.data.xy.DefaultXYDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.poznan.put.cs.bioserver.comparison.ComparisonListener;
import pl.poznan.put.cs.bioserver.comparison.GlobalComparison;
import pl.poznan.put.cs.bioserver.comparison.IncomparableStructuresException;
import pl.poznan.put.cs.bioserver.comparison.MCQ;
import pl.poznan.put.cs.bioserver.comparison.RMSD;
import pl.poznan.put.cs.bioserver.comparison.TorsionLocalComparison;
import pl.poznan.put.cs.bioserver.gui.helper.PdbFileChooser;
import pl.poznan.put.cs.bioserver.helper.PdbManager;
import pl.poznan.put.cs.bioserver.torsion.AngleDifference;
import pl.poznan.put.cs.bioserver.visualisation.MDS;
import pl.poznan.put.cs.bioserver.visualisation.MDSPlot;

import com.csvreader.CsvWriter;

public class MainWindow extends JFrame {
    private static final String CARD_MATRIX = "MATRIX";
    private static final String CARD_MCQ_LOCAL = "MCQ_LOCAL";

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory
            .getLogger(MainWindow.class);
    protected static final String ABOUT = "MCQ4Structures is a tool for "
            + "structural similarity computation based on molecule tertiary "
            + "structure representation in torsional angle space.\nIt has been "
            + "designed to work primarily for RNA structures. Proteins are "
            + "also handled and their representation is restricted to the "
            + "backbone angles.\n\nMCQ4Structures is available as a free Java "
            + "WebStart application. The source code is licensed under BSD."
            + "\n\nCiting MCQ4Structures.\n"
            + "T. Zok, M. Popenda, M. Szachniuk. MCQ4Structures to compute "
            + "similarity of molecule structures. Central European Journal of "
            + "Operations Research, in press.\n\nAcknowledgements and funding.\n"
            + "This work has been partially supported by the European Regional "
            + "Development Fund within Innovative Economy Programme "
            + "(POIG.02.03.00-00-018/08 POWIEW)\nand grants from the National "
            + "Science Centre, Poland (2012/05/B/ST6/03026).";
    private StructureSelectionDialog structureDialog;
    private ChainSelectionDialog chainDialog;
    private TorsionAnglesSelectionDialog torsionDialog;
    protected Map<String, List<AngleDifference>> localComparisonResult;
    protected String[] globalComparisonNames;
    protected double[][] globalComparisonResults;

    public MainWindow() throws HeadlessException {
        super();
        /*
         * Set L&F
         */
        for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) {
                try {
                    UIManager.setLookAndFeel(info.getClassName());
                } catch (ClassNotFoundException | InstantiationException
                        | IllegalAccessException
                        | UnsupportedLookAndFeelException e) {
                    // do nothing
                }
                break;
            }
        }

        structureDialog = new StructureSelectionDialog(this);
        chainDialog = new ChainSelectionDialog(this);
        torsionDialog = new TorsionAnglesSelectionDialog(this);

        /*
         * Create menu
         */
        JMenuItem itemOpen = new JMenuItem("Open structure(s)",
                loadIcon("/toolbarButtonGraphics/general/Open16.gif"));
        final JMenuItem itemSave = new JMenuItem("Save results",
                loadIcon("/toolbarButtonGraphics/general/Save16.gif"));
        itemSave.setEnabled(false);
        JMenuItem itemExit = new JMenuItem("Exit");
        JMenu menuFile = new JMenu("File");
        menuFile.setMnemonic(KeyEvent.VK_F);
        menuFile.add(itemOpen);
        menuFile.add(itemSave);
        menuFile.add(itemExit);

        final JRadioButton radioMcq = new JRadioButton("MCQ", true);
        JRadioButton radioRmsd = new JRadioButton("RMSD", false);
        ButtonGroup group = new ButtonGroup();
        group.add(radioMcq);
        group.add(radioRmsd);
        final JMenu menuMeasure = new JMenu("Distance measure");
        menuMeasure.setEnabled(false);
        menuMeasure.add(radioMcq);
        menuMeasure.add(radioRmsd);

        final JMenuItem itemSelectStructures = new JMenuItem(
                "Select structures");
        final JMenuItem itemComputeGlobal = new JMenuItem(
                "Compute distance matrix");
        itemComputeGlobal.setEnabled(false);
        final JMenuItem itemVisualise = new JMenuItem("Visualise results");
        itemVisualise.setEnabled(false);
        final JMenuItem itemCluster = new JMenuItem("Cluster results");
        itemCluster.setEnabled(false);
        JMenu menuGlobal = new JMenu("Global comparison");
        menuGlobal.add(itemSelectStructures);
        menuGlobal.add(menuMeasure);
        menuGlobal.add(itemComputeGlobal);
        menuGlobal.add(itemVisualise);
        menuGlobal.add(itemVisualise);
        menuGlobal.add(itemCluster);

        final JMenuItem itemSelectChainsCompare = new JMenuItem("Select chains");
        final JMenuItem itemSelectTorsion = new JMenuItem(
                "Select torsion angles");
        itemSelectTorsion.setEnabled(false);
        final JMenuItem itemComputeLocal = new JMenuItem("Compute distances");
        itemComputeLocal.setEnabled(false);
        JMenu menuLocal = new JMenu("Local comparison");
        menuLocal.add(itemSelectChainsCompare);
        menuLocal.add(itemSelectTorsion);
        menuLocal.add(itemComputeLocal);

        JMenu menuDistance = new JMenu("Distance computation");
        menuDistance.setMnemonic(KeyEvent.VK_D);
        menuDistance.add(menuGlobal);
        menuDistance.addSeparator();
        menuDistance.add(menuLocal);

        JRadioButton radioAlignGlobal = new JRadioButton("Global", true);
        JRadioButton radioAlignLocal = new JRadioButton("Local", false);
        ButtonGroup groupAlign = new ButtonGroup();
        groupAlign.add(radioAlignGlobal);
        groupAlign.add(radioAlignLocal);
        JMenu menuSelectAlignType = new JMenu("Select alignment type");
        menuSelectAlignType.setEnabled(false);
        menuSelectAlignType.add(radioAlignGlobal);
        menuSelectAlignType.add(radioAlignLocal);

        final JMenuItem itemSelectChainsAlignSeq = new JMenuItem(
                "Select chains");
        JMenuItem itemComputeAlignSeq = new JMenuItem("Compute alignment");
        itemComputeAlignSeq.setEnabled(false);
        JMenu menuAlignSeq = new JMenu("Sequence");
        menuAlignSeq.add(itemSelectChainsAlignSeq);
        menuAlignSeq.add(menuSelectAlignType);
        menuAlignSeq.add(itemComputeAlignSeq);

        final JMenuItem itemSelectChainsAlignStruc = new JMenuItem(
                "Select chains");
        JMenuItem itemComputeAlignStruc = new JMenuItem("Compute alignment");
        itemComputeAlignStruc.setEnabled(false);
        JMenu menuAlignStruc = new JMenu("3D structure");
        menuAlignStruc.add(itemSelectChainsAlignStruc);
        menuAlignStruc.add(itemComputeAlignStruc);

        JMenu menuAlignment = new JMenu("Alignment");
        menuAlignment.setMnemonic(KeyEvent.VK_A);
        menuAlignment.add(menuAlignSeq);
        menuAlignment.addSeparator();
        menuAlignment.add(menuAlignStruc);

        JMenuItem itemGuide = new JMenuItem("Quick guide");
        itemGuide.setMnemonic(KeyEvent.VK_Q);
        itemGuide.setMaximumSize(new Dimension(
                itemGuide.getPreferredSize().width,
                itemGuide.getMaximumSize().height));

        JMenuItem itemAbout = new JMenuItem("About");
        itemAbout.setMnemonic(KeyEvent.VK_O);
        itemAbout.setMaximumSize(new Dimension(
                itemAbout.getPreferredSize().width,
                itemAbout.getMaximumSize().height));

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(menuFile);
        menuBar.add(menuDistance);
        menuBar.add(menuAlignment);
        menuBar.add(itemGuide);
        menuBar.add(itemAbout);

        setJMenuBar(menuBar);

        /*
         * Create card layout
         */
        final JTable tableMatrix = new JTable();
        final JPanel panelTorsionChart = new JPanel(new GridLayout(1, 1));
        final CardLayout cardLayout = new CardLayout();
        final JPanel panelCards = new JPanel();
        panelCards.setLayout(cardLayout);
        panelCards.add(new JScrollPane(tableMatrix), CARD_MATRIX);
        panelCards.add(panelTorsionChart, CARD_MCQ_LOCAL);

        setLayout(new BorderLayout());
        add(panelCards, BorderLayout.CENTER);

        /*
         * Set window properties
         */
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("MCQ4Structures: computing similarity of 3D RNA / protein structures");

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension size = toolkit.getScreenSize();
        setSize(size.width * 3 / 4, size.height * 3 / 4);
        setLocation(size.width / 8, size.height / 8);

        /*
         * Set action listeners
         */
        itemOpen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File[] files = PdbFileChooser.getSelectedFiles(MainWindow.this);
                for (File f : files) {
                    PdbManager.loadStructure(f);
                }
            }
        });

        itemSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                int chosenOption = chooser.showSaveDialog(MainWindow.this);
                if (chosenOption != JFileChooser.APPROVE_OPTION) {
                    return;
                }

                Component current = getCurrentCard(panelCards);
                if (current.equals(tableMatrix)) {
                    try (FileOutputStream stream = new FileOutputStream(chooser
                            .getSelectedFile())) {
                        CsvWriter writer = new CsvWriter(stream, ';', Charset
                                .forName("UTF-8"));

                        int length = globalComparisonNames.length;
                        writer.write("");
                        for (int i = 0; i < length; i++) {
                            writer.write(globalComparisonNames[i]);
                        }
                        writer.endRecord();

                        for (int i = 0; i < length; i++) {
                            writer.write(globalComparisonNames[i]);
                            for (int j = 0; j < length; j++) {
                                writer.write(Double
                                        .toString(globalComparisonResults[i][j]));
                            }
                            writer.endRecord();
                        }

                        writer.close();
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                } else if (current.equals(panelTorsionChart)) {
                    SortedMap<String, Map<String, Double>> map = new TreeMap<>();
                    for (Entry<String, List<AngleDifference>> pair : localComparisonResult
                            .entrySet()) {
                        String angleName = pair.getKey();
                        List<AngleDifference> value = pair.getValue();

                        for (AngleDifference ad : value) {
                            ResidueNumber residue = ad.getResidue();
                            String residueName = String.format("%s:%03d",
                                    residue.getChainId(), residue.getSeqNum());

                            if (!map.containsKey(residueName)) {
                                map.put(residueName,
                                        new LinkedHashMap<String, Double>());
                            }
                            Map<String, Double> angleValues = map
                                    .get(residueName);
                            angleValues.put(angleName, ad.getDifference());
                        }
                    }

                    try (FileOutputStream stream = new FileOutputStream(chooser
                            .getSelectedFile())) {
                        CsvWriter writer = new CsvWriter(stream, ';', Charset
                                .forName("UTF-8"));

                        writer.write("");
                        Set<String> keySetReference = map.get(map.firstKey())
                                .keySet();
                        for (String angleName : keySetReference) {
                            writer.write(angleName);
                        }
                        writer.endRecord();

                        for (String residueName : map.keySet()) {
                            writer.write(residueName);

                            Map<String, Double> mapAngles = map
                                    .get(residueName);
                            for (String angleName : keySetReference) {
                                if (mapAngles.containsKey(angleName)) {
                                    String angleValue = Double
                                            .toString(mapAngles.get(angleName));
                                    writer.write(angleValue);
                                } else {
                                    writer.write("");
                                }
                            }
                            writer.endRecord();
                        }

                        writer.close();
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }
            }
        });

        itemExit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        itemSelectStructures.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Enumeration<File> elements = PdbManagerDialog.model.elements();
                while (elements.hasMoreElements()) {
                    File path = elements.nextElement();
                    if (!structureDialog.modelAll.contains(path)
                            && !structureDialog.modelSelected.contains(path)) {
                        structureDialog.modelAll.addElement(path);
                    }
                }

                elements = structureDialog.modelAll.elements();
                while (elements.hasMoreElements()) {
                    File path = elements.nextElement();
                    if (PdbManager.getStructure(path) == null) {
                        structureDialog.modelAll.removeElement(path);
                    }
                }

                elements = structureDialog.modelSelected.elements();
                while (elements.hasMoreElements()) {
                    File path = elements.nextElement();
                    if (PdbManager.getStructure(path) == null) {
                        structureDialog.modelSelected.removeElement(path);
                    }
                }

                structureDialog.setVisible(true);
                if (structureDialog.selectedStructures != null
                        && structureDialog.selectedStructures.size() >= 2) {
                    menuMeasure.setEnabled(true);
                    itemComputeGlobal.setEnabled(true);
                }
            }
        });

        itemComputeGlobal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (structureDialog.selectedStructures == null
                        || structureDialog.selectedStructures.size() < 2) {
                    JOptionPane.showMessageDialog(MainWindow.this,
                            "You must open at least two structures",
                            "Information", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                GlobalComparison comparison;
                if (radioMcq.isSelected()) {
                    comparison = new MCQ();
                } else { // radioRmsd.isSelected() == true
                    comparison = new RMSD();
                }

                globalComparisonNames = PdbManager
                        .getSelectedStructuresNames(structureDialog.selectedStructures);
                Structure[] structures = PdbManager
                        .getSelectedStructures(structureDialog.selectedStructures);
                try {
                    globalComparisonResults = comparison.compare(structures,
                            new ComparisonListener() {
                                @Override
                                public void stateChanged(long all,
                                        long completed) {
                                    // TODO
                                    MainWindow.LOGGER.debug(completed + "/"
                                            + all);
                                }
                            });

                    MatrixTableModel model = new MatrixTableModel(
                            globalComparisonNames, globalComparisonResults);
                    tableMatrix.setModel(model);
                    cardLayout.show(panelCards, CARD_MATRIX);

                    itemSave.setEnabled(true);
                    itemCluster.setEnabled(true);
                    itemVisualise.setEnabled(true);
                } catch (IncomparableStructuresException e1) {
                    JOptionPane.showMessageDialog(
                            MainWindow.this,
                            "Failed to compute distance matrix: "
                                    + e1.getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        });

        itemVisualise.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MatrixTableModel model = (MatrixTableModel) tableMatrix
                        .getModel();
                String[] names = model.getNames();
                double[][] values = model.getValues();

                for (double[] value : values) {
                    for (double element : value) {
                        if (Double.isNaN(element)) {
                            JOptionPane.showMessageDialog(MainWindow.this,
                                    "Cannot visualize, because some "
                                            + "of the structures were "
                                            + "incomparable", "Error",
                                    JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                }

                double[][] mds = MDS.multidimensionalScaling(values, 2);
                if (mds == null) {
                    JOptionPane.showMessageDialog(null,
                            "Cannot visualise specified structures in 2D",
                            "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                MDSPlot plot = new MDSPlot(mds, names);
                plot.setVisible(true);
            }
        });

        itemCluster.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                MatrixTableModel model = (MatrixTableModel) tableMatrix
                        .getModel();
                String[] names = model.getNames();
                double[][] values = model.getValues();

                for (double[] value : values) {
                    for (double element : value) {
                        if (Double.isNaN(element)) {
                            JOptionPane.showMessageDialog(MainWindow.this,
                                    "Cannot cluster, because some "
                                            + "of the structures were "
                                            + "incomparable", "Error",
                                    JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                }

                ClusteringDialog dialogClustering = new ClusteringDialog(names,
                        values);
                dialogClustering.setVisible(true);
            }
        });

        itemSelectChainsCompare.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                chainDialog.modelLeft.removeAllElements();
                chainDialog.modelRight.removeAllElements();

                Enumeration<File> elements = PdbManagerDialog.model.elements();
                while (elements.hasMoreElements()) {
                    File path = elements.nextElement();
                    chainDialog.modelLeft.addElement(path);
                    chainDialog.modelRight.addElement(path);
                }

                chainDialog.setVisible(true);

                if (chainDialog.selectedStructures != null
                        && chainDialog.selectedChains != null) {
                    for (int i = 0; i < 2; i++) {
                        if (chainDialog.selectedChains[i].length == 0) {
                            JOptionPane
                                    .showMessageDialog(
                                            MainWindow.this,
                                            "No chains specified for structure: "
                                                    + chainDialog.selectedStructures[i],
                                            "Error", JOptionPane.ERROR_MESSAGE);
                            chainDialog.selectedStructures = null;
                            chainDialog.selectedChains = null;
                            return;
                        }
                    }

                    itemSelectTorsion.setEnabled(true);
                }
            }
        });

        itemSelectTorsion.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                torsionDialog.setVisible(true);
                itemComputeLocal.setEnabled(true);
            }
        });

        itemComputeLocal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                final Structure[] structures = new Structure[] {
                        PdbManager
                                .getStructure(chainDialog.selectedStructures[0]),
                        PdbManager
                                .getStructure(chainDialog.selectedStructures[1]) };

                try {
                    localComparisonResult = TorsionLocalComparison.compare(
                            structures[0], structures[1], false);
                } catch (StructureException e1) {
                    JOptionPane.showMessageDialog(null, e1.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                DefaultXYDataset dataset = new DefaultXYDataset();
                for (String angle : torsionDialog.selectedNames) {
                    if (!localComparisonResult.containsKey(angle)) {
                        continue;
                    }
                    List<AngleDifference> diffs = localComparisonResult
                            .get(angle);
                    Collections.sort(diffs);
                    double[] x = new double[diffs.size()];
                    double[] y = new double[diffs.size()];
                    for (int i = 0; i < diffs.size(); i++) {
                        AngleDifference ad = diffs.get(i);
                        x[i] = i;
                        y[i] = ad.getDifference();
                    }
                    dataset.addSeries(angle, new double[][] { x, y });
                }
                NumberAxis xAxis = new TorsionAxis(localComparisonResult);
                xAxis.setLabel("Residue");

                NumberAxis yAxis = new NumberAxis();
                yAxis.setAutoRange(false);
                yAxis.setRange(0, Math.PI);
                yAxis.setLabel("Distance [rad]");

                XYPlot plot = new XYPlot(dataset, xAxis, yAxis,
                        new DefaultXYItemRenderer());

                panelTorsionChart.removeAll();
                panelTorsionChart.add(new ChartPanel(new JFreeChart(plot)));
                panelTorsionChart.revalidate();

                cardLayout.show(panelCards, CARD_MCQ_LOCAL);
                itemSave.setEnabled(true);
            }
        });

        itemSelectChainsAlignSeq.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO
            }
        });

        itemComputeAlignSeq.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO
            }
        });

        itemSelectChainsAlignStruc.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub
            }
        });

        itemComputeAlignStruc.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub
            }
        });

        itemGuide.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub
            }
        });

        itemAbout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(MainWindow.this, ABOUT, "About",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    protected static Component getCurrentCard(JPanel panel) {
        for (Component component : panel.getComponents()) {
            if (component.isVisible()) {
                return component;
            }
        }
        return null;
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
