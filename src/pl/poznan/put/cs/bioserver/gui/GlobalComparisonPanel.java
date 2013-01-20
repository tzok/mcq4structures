package pl.poznan.put.cs.bioserver.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.biojava.bio.structure.Structure;

import pl.poznan.put.cs.bioserver.clustering.HierarchicalPlot;
import pl.poznan.put.cs.bioserver.clustering.KMedoidsPlot;
import pl.poznan.put.cs.bioserver.comparison.ComparisonListener;
import pl.poznan.put.cs.bioserver.comparison.GlobalComparison;
import pl.poznan.put.cs.bioserver.comparison.IncomparableStructuresException;
import pl.poznan.put.cs.bioserver.comparison.MCQ;
import pl.poznan.put.cs.bioserver.comparison.RMSD;
import pl.poznan.put.cs.bioserver.gui.helper.PdbFileChooser;
import pl.poznan.put.cs.bioserver.helper.PdbManager;
import pl.poznan.put.cs.bioserver.visualisation.MDS;
import pl.poznan.put.cs.bioserver.visualisation.MDSPlot;

/**
 * Panel which allows to use all global comparison measures.
 * 
 * @author Tomasz Żok (tzok[at]cs.put.poznan.pl)
 */
public class GlobalComparisonPanel extends JPanel {
    private class MatrixTableModel extends AbstractTableModel {
        private static final long serialVersionUID = 1L;
        private String[] tableNames;
        private double[][] tableValues;

        public MatrixTableModel(String[] names, double[][] values) {
            super();
            tableNames = names.clone();
            tableValues = values.clone();
        }

        @Override
        public int getColumnCount() {
            if (tableNames.length == 0) {
                return 0;
            }
            return tableNames.length + 1;
        }

        @Override
        public String getColumnName(int column) {
            if (column == 0) {
                return "";
            }
            return tableNames[column - 1];
        }

        @Override
        public int getRowCount() {
            return tableValues.length;
        }

        @Override
        public Object getValueAt(int row, int column) {
            if (column == 0) {
                return tableNames[row];
            }
            return tableValues[row][column - 1];
        }

        public String[] getNames() {
            return tableNames;
        }

        public double[][] getValues() {
            return tableValues;
        }
    }

    private class ClusteringDialog extends JDialog {
        private static final long serialVersionUID = 1L;

        public ClusteringDialog(final String[] structureNames,
                final double[][] comparisonResults) {
            super();

            final JRadioButton hierarchical = new JRadioButton("hierarchical",
                    true);
            JRadioButton kmedoids = new JRadioButton("k-medoids", false);
            ButtonGroup group = new ButtonGroup();
            group.add(hierarchical);
            group.add(kmedoids);

            final JComboBox<String> linkage = new JComboBox<>(new String[] {
                    "Single", "Complete", "Average" });
            final JComboBox<String> method = new JComboBox<>(new String[] {
                    "PAM", "PAMSIL" });
            method.setEnabled(false);
            final JCheckBox findBestK = new JCheckBox("Find best k?", true);
            findBestK.setEnabled(false);
            final JSpinner kspinner = new JSpinner();
            kspinner.setModel(new SpinnerNumberModel(2, 2, Integer.MAX_VALUE, 1));
            kspinner.setEnabled(false);

            JButton ok = new JButton("Ok");
            JButton close = new JButton("Close");

            Container container = getContentPane();
            container.setLayout(new GridBagLayout());

            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            c.gridwidth = 1;
            c.gridheight = 1;
            c.fill = GridBagConstraints.BOTH;
            container.add(hierarchical, c);

            c.gridx = 1;
            c.gridwidth = 3;
            container.add(linkage, c);

            c.gridx = 0;
            c.gridy = 1;
            c.gridwidth = 1;
            container.add(kmedoids, c);

            c.gridx = 1;
            container.add(method, c);

            c.gridx = 2;
            container.add(findBestK, c);

            c.gridx = 3;
            container.add(kspinner, c);

            c.gridx = 1;
            c.gridy = 2;
            c.gridwidth = 1;
            container.add(ok, c);

            c.gridx = 2;
            container.add(close, c);

            ActionListener radioActionListener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    boolean isHierarchical = hierarchical.isSelected();
                    linkage.setEnabled(isHierarchical);
                    method.setEnabled(!isHierarchical);
                    findBestK.setEnabled(!isHierarchical);
                    kspinner.setEnabled(!isHierarchical
                            && !findBestK.isSelected());
                }
            };
            hierarchical.addActionListener(radioActionListener);
            kmedoids.addActionListener(radioActionListener);
            findBestK.addActionListener(radioActionListener);

            ok.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JFrame plot;
                    if (hierarchical.isSelected()) {
                        plot = new HierarchicalPlot(comparisonResults,
                                structureNames, linkage.getSelectedIndex());
                    } else {
                        int k;
                        if (findBestK.isSelected()) {
                            k = 0;
                        } else {
                            k = (Integer) kspinner.getValue();
                            if (k > comparisonResults.length) {
                                JOptionPane.showMessageDialog(null,
                                        "k in k-medoids must be less or equal "
                                                + "to the number of input "
                                                + "structures", "Error!",
                                        JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        }
                        plot = new KMedoidsPlot(comparisonResults,
                                structureNames, k, (String) method
                                        .getSelectedItem());
                    }
                    plot.setVisible(true);
                }
            });

            close.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            });

            pack();
            int width = getPreferredSize().width;
            int height = getPreferredSize().height;

            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int x = screenSize.width - width;
            int y = screenSize.height - height;
            setSize(width, height);
            setLocation(x / 2, y / 2);

            setTitle("Clustering");
        }
    }

    private static final long serialVersionUID = 1L;

    public GlobalComparisonPanel() {
        super();

        JButton buttonLoad = new JButton("Load structure(s)");
        final JButton buttonCompare = new JButton("Compute distance matrix");
        buttonCompare.setEnabled(false);
        final JButton buttonVisualize = new JButton("Visualize results");
        buttonVisualize.setEnabled(false);
        final JButton buttonCluster = new JButton("Cluster results");
        buttonCluster.setEnabled(false);

        JPanel panelButtons = new JPanel();
        panelButtons.add(buttonLoad);
        panelButtons.add(buttonCompare);
        panelButtons.add(buttonVisualize);
        panelButtons.add(buttonCluster);

        final JRadioButton radioRmsd = new JRadioButton("RMSD", true);
        JRadioButton radioMcq = new JRadioButton("MCQ", false);

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(radioRmsd);
        buttonGroup.add(radioMcq);

        JPanel panelMethod = new JPanel();
        panelMethod.add(new JLabel("Select distance measure:"));
        panelMethod.add(radioRmsd);
        panelMethod.add(radioMcq);

        JPanel panelButtonsMethod = new JPanel();
        panelButtonsMethod.setLayout(new GridLayout(2, 1));
        panelButtonsMethod.add(panelButtons);
        panelButtonsMethod.add(panelMethod);

        JEditorPane editorPane = new JEditorPane();
        editorPane.setBackground(new Color(0, 0, 0, 0));
        editorPane.setContentType("text/html");
        editorPane.setEditable(false);
        editorPane.setText("Instructions:<ol>"
                + "<li>Load structure(s) from files (PDB or mmCif)</li>"
                + "<li>Select distance measure</li>"
                + "<li>Compute distance matrix</li>"
                + "<li>Visualise or cluster results</li></ol>");

        JPanel panelOptions = new JPanel();
        panelOptions.setLayout(new GridLayout(1, 2));
        panelOptions.add(panelButtonsMethod);
        panelOptions.add(editorPane);

        final JProgressBar progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        JPanel panelStatus = new JPanel();
        panelStatus.add(progressBar);

        JPanel panelOptionsStatus = new JPanel();
        panelOptionsStatus.setLayout(new BorderLayout());
        panelOptionsStatus.add(panelOptions, BorderLayout.NORTH);
        panelOptionsStatus.add(progressBar, BorderLayout.SOUTH);

        final DefaultListModel<File> listModel = new DefaultListModel<>();
        final JList<File> listFiles = new JList<>(listModel);

        TableModel tableModel = new MatrixTableModel(new String[0],
                new double[0][]);
        final JTable tableMatrix = new JTable(tableModel);

        setLayout(new BorderLayout());
        add(panelOptionsStatus, BorderLayout.NORTH);
        add(listFiles, BorderLayout.EAST);
        add(new JScrollPane(tableMatrix), BorderLayout.CENTER);

        buttonLoad.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File[] files = PdbFileChooser
                        .getSelectedFiles(GlobalComparisonPanel.this);
                for (File f : files) {
                    PdbManager.loadStructure(f);
                    listModel.addElement(f);
                }
                if (files.length > 0) {
                    buttonCompare.setEnabled(true);
                }
            }
        });

        buttonCompare.addActionListener(new ActionListener() {
            private Thread thread;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (thread != null && thread.isAlive()) {
                    JOptionPane.showMessageDialog(null,
                            "Comparison calculation underway!", "Information",
                            JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int size = listModel.getSize();
                        final String[] names = new String[size];
                        Structure[] structures = new Structure[size];
                        for (int i = 0; i < size; i++) {
                            File file = listModel.getElementAt(i);
                            names[i] = PdbManager.getStructureName(file);
                            structures[i] = PdbManager.getStructure(file);
                        }

                        GlobalComparison algorithm;
                        if (radioRmsd.isSelected()) {
                            algorithm = new RMSD();
                        } else {
                            algorithm = new MCQ();
                        }

                        final double[][] result;
                        try {
                            result = algorithm.compare(structures,
                                    new ComparisonListener() {
                                        @Override
                                        public void stateChanged(long all,
                                                long completed) {
                                            progressBar.setMaximum((int) all);
                                            progressBar
                                                    .setValue((int) completed);
                                        }
                                    });
                        } catch (IncomparableStructuresException e1) {
                            JOptionPane.showMessageDialog(
                                    GlobalComparisonPanel.this,
                                    e1.getMessage(), "Error",
                                    JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                MatrixTableModel model = new MatrixTableModel(
                                        names, result);
                                tableMatrix.setModel(model);

                                buttonVisualize.setEnabled(true);
                                buttonCluster.setEnabled(true);
                            }
                        });
                    }
                });
                thread.start();
            }
        });

        buttonVisualize.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MatrixTableModel model = (MatrixTableModel) tableMatrix
                        .getModel();
                String[] names = model.getNames();
                double[][] values = model.getValues();

                for (double[] value : values) {
                    for (double element : value) {
                        if (Double.isNaN(element)) {
                            JOptionPane.showMessageDialog(
                                    GlobalComparisonPanel.this,
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

        buttonCluster.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        MatrixTableModel model = (MatrixTableModel) tableMatrix
                                .getModel();
                        String[] names = model.getNames();
                        double[][] values = model.getValues();

                        for (double[] value : values) {
                            for (double element : value) {
                                if (Double.isNaN(element)) {
                                    JOptionPane.showMessageDialog(
                                            GlobalComparisonPanel.this,
                                            "Cannot cluster, because some "
                                                    + "of the structures were "
                                                    + "incomparable", "Error",
                                            JOptionPane.ERROR_MESSAGE);
                                    return;
                                }
                            }
                        }

                        ClusteringDialog dialog = new ClusteringDialog(names,
                                values);
                        dialog.setVisible(true);
                    }
                });
            }
        });

        listFiles.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    int index = listFiles.getSelectedIndex();
                    if (index != -1) {
                        listModel.remove(index);
                        if (listModel.getSize() == 0) {
                            buttonCompare.setEnabled(false);
                            buttonVisualize.setEnabled(false);
                            buttonCluster.setEnabled(false);
                        }
                    }
                }

            }
        });
    }
}
