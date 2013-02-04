package pl.poznan.put.cs.bioserver.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
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
import java.util.Enumeration;

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

import org.biojava.bio.structure.Structure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.poznan.put.cs.bioserver.comparison.ComparisonListener;
import pl.poznan.put.cs.bioserver.comparison.GlobalComparison;
import pl.poznan.put.cs.bioserver.comparison.IncomparableStructuresException;
import pl.poznan.put.cs.bioserver.comparison.MCQ;
import pl.poznan.put.cs.bioserver.comparison.RMSD;
import pl.poznan.put.cs.bioserver.gui.helper.PdbFileChooser;
import pl.poznan.put.cs.bioserver.helper.PdbManager;
import pl.poznan.put.cs.bioserver.visualisation.MDS;
import pl.poznan.put.cs.bioserver.visualisation.MDSPlot;

import com.csvreader.CsvWriter;

public class MainWindow extends JFrame {
    private static final String CARD_MATRIX = "MATRIX";
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory
            .getLogger(MainWindow.class);
    protected StructureSelectionDialog dialog;

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

        dialog = new StructureSelectionDialog(this);

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
        JMenuItem itemSelectTorsion = new JMenuItem("Select torsion angles");
        itemSelectTorsion.setEnabled(false);
        JMenuItem itemComputeLocal = new JMenuItem("Compute distances");
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
        final CardLayout cardLayout = new CardLayout();
        final JPanel panelCards = new JPanel();
        panelCards.setLayout(cardLayout);
        panelCards.add(new JScrollPane(tableMatrix), CARD_MATRIX);

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
                MatrixTableModel model = (MatrixTableModel) tableMatrix
                        .getModel();
                String[] names = model.getNames();
                double[][] values = model.getValues();

                JFileChooser chooser = new JFileChooser();
                int chosenOption = chooser.showSaveDialog(MainWindow.this);
                if (chosenOption != JFileChooser.APPROVE_OPTION) {
                    return;
                }

                try (FileOutputStream stream = new FileOutputStream(chooser
                        .getSelectedFile())) {
                    CsvWriter writer = new CsvWriter(stream, ';', Charset
                            .forName("UTF-8"));

                    int length = names.length;
                    writer.write("");
                    for (int i = 0; i < length; i++) {
                        writer.write(names[i]);
                    }
                    writer.endRecord();

                    for (int i = 0; i < length; i++) {
                        writer.write(names[i]);
                        for (int j = 0; j < length; j++) {
                            writer.write(Double.toString(values[i][j]));
                        }
                        writer.endRecord();
                    }

                    writer.close();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
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
                    if (!dialog.modelAll.contains(path)
                            && !dialog.modelSelected.contains(path)) {
                        dialog.modelAll.addElement(path);
                    }
                }

                elements = dialog.modelAll.elements();
                while (elements.hasMoreElements()) {
                    File path = elements.nextElement();
                    if (PdbManager.getStructure(path) == null) {
                        dialog.modelAll.removeElement(path);
                    }
                }

                elements = dialog.modelSelected.elements();
                while (elements.hasMoreElements()) {
                    File path = elements.nextElement();
                    if (PdbManager.getStructure(path) == null) {
                        dialog.modelSelected.removeElement(path);
                    }
                }

                dialog.setVisible(true);
                if (dialog.selectedStructures != null
                        && dialog.selectedStructures.size() >= 2) {
                    menuMeasure.setEnabled(true);
                    itemComputeGlobal.setEnabled(true);
                }
            }
        });

        itemComputeGlobal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (dialog.selectedStructures == null
                        || dialog.selectedStructures.size() < 2) {
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

                String[] names = PdbManager
                        .getSelectedStructuresNames(dialog.selectedStructures);
                Structure[] structures = PdbManager
                        .getSelectedStructures(dialog.selectedStructures);
                try {
                    double[][] result = comparison.compare(structures,
                            new ComparisonListener() {
                                @Override
                                public void stateChanged(long all,
                                        long completed) {
                                    // TODO
                                    MainWindow.LOGGER.debug(completed + "/"
                                            + all);
                                }
                            });

                    MatrixTableModel model = new MatrixTableModel(names, result);
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
