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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;

import org.biojava.bio.structure.Structure;

import pl.poznan.put.cs.bioserver.clustering.HierarchicalPlot;
import pl.poznan.put.cs.bioserver.clustering.KMedoidsPlot;
import pl.poznan.put.cs.bioserver.comparison.GlobalComparison;
import pl.poznan.put.cs.bioserver.comparison.IncomparableStructuresException;
import pl.poznan.put.cs.bioserver.comparison.MCQ;
import pl.poznan.put.cs.bioserver.comparison.RMSD;
import pl.poznan.put.cs.bioserver.helper.PdbManager;
import pl.poznan.put.cs.bioserver.visualisation.MDS;
import pl.poznan.put.cs.bioserver.visualisation.MDSPlot;

/**
 * Panel which allows to use all global comparison measures.
 * 
 * @author Tomasz Żok (tzok[at]cs.put.poznan.pl)
 */
public class GlobalComparisonPanel extends JPanel {
    /**
     * A dialog to set some additional options concerning clustering.
     */
    private class ClusteringDialog extends JDialog {
        private static final long serialVersionUID = 1L;

        // /////////////////////////////////////////////////////////////////////
        // constructors
        public ClusteringDialog() {
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

            Dimension preferredSize = container.getPreferredSize();
            setSize(preferredSize.width * 3 / 2, preferredSize.height * 2);
            setTitle("Clustering");

            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Dimension screenSize = toolkit.getScreenSize();
            Dimension size = getSize();
            int x = screenSize.width - size.width;
            int y = screenSize.height - size.height;
            setLocation(x / 2, y / 2);

            /*
             * choosing one clustering methods disables another one's option
             */
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

            /*
             * clicking ok makes concrete implementation of clustering to be
             * invoked
             */
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
        }
    }

    /**
     * Main panel which allows user to choose action (compare, cluster,
     * visualize) and shows visual results of those actions.
     */
    private class MainPanel extends JPanel {
        /**
         * Subpanel with action items on the left and instructions for the user
         * on the right
         */
        private class ActionInstructionsPanel extends JPanel {
            /**
             * Subpanel which shows button and method choice panels in separate
             * rows
             */
            private class ActionPanel extends JPanel {
                /**
                 * Subpanel which contains action buttons.
                 */
                private class ButtonPanel extends JPanel {
                    // ////////////////////////////////////////////////////////
                    // fields
                    private static final long serialVersionUID = 1L;
                    private JButton addPDB;
                    private JButton cluster;
                    private JButton compare;
                    private JButton visualise;

                    // ////////////////////////////////////////////////////////
                    // constructors
                    public ButtonPanel() {
                        super();
                        addPDB = new JButton("Load structure(s)");
                        compare = new JButton("Compute distance matrix");
                        cluster = new JButton("Cluster results");
                        visualise = new JButton("Visualise results");

                        compare.setEnabled(false);
                        cluster.setEnabled(false);
                        visualise.setEnabled(false);

                        add(addPDB);
                        add(compare);
                        add(visualise);
                        add(cluster);
                    }
                }

                /**
                 * Subpanel which allows to choose comparison method.
                 */
                private class MethodPanel extends JPanel {
                    // ////////////////////////////////////////////////////////
                    // fields
                    private static final long serialVersionUID = 1L;
                    private final JRadioButton rmsdRadio;
                    private JRadioButton mcqRadio;

                    // ////////////////////////////////////////////////////////
                    // constructors
                    public MethodPanel() {
                        super();
                        rmsdRadio = new JRadioButton("RMSD", true);
                        mcqRadio = new JRadioButton("MCQ", false);

                        ButtonGroup buttonGroup = new ButtonGroup();
                        buttonGroup.add(rmsdRadio);
                        buttonGroup.add(mcqRadio);

                        add(new JLabel("Select distance measure: "));
                        add(rmsdRadio);
                        add(mcqRadio);
                    }
                }

                // ////////////////////////////////////////////////////////////
                // fields
                private static final long serialVersionUID = 1L;
                private ButtonPanel buttonPanel;
                private MethodPanel methodPanel;

                // ////////////////////////////////////////////////////////////
                // constructors
                public ActionPanel() {
                    super();
                    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
                    buttonPanel = new ButtonPanel();
                    methodPanel = new MethodPanel();

                    add(buttonPanel);
                    add(methodPanel);

                    final JFileChooser chooser = new JFileChooser();
                    chooser.addChoosableFileFilter(new FileNameExtensionFilter(
                            "PDB file format", "pdb", "pdb1", "ent", "brk",
                            "pdb.gz"));
                    chooser.addChoosableFileFilter(new FileNameExtensionFilter(
                            "mmCif file format", "cif", "cif.gz"));
                    chooser.setMultiSelectionEnabled(true);

                    buttonPanel.addPDB.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent event) {
                            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                                for (File f : chooser.getSelectedFiles()) {
                                    addFile(f.getAbsolutePath());
                                }
                            }
                        }
                    });

                    buttonPanel.compare.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            compare();
                        }
                    });

                    buttonPanel.visualise
                            .addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    visualise();
                                }
                            });

                    buttonPanel.cluster.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            cluster();
                        }
                    });
                }
            }

            /**
             * Subpanel with instructions to the user
             */
            private class InstructionsPanel extends JPanel {
                private static final long serialVersionUID = 1L;

                // ////////////////////////////////////////////////////////////
                // fields
                private JEditorPane editorPane;

                // ////////////////////////////////////////////////////////////
                // constructors
                public InstructionsPanel() {
                    editorPane = new JEditorPane();
                    editorPane.setBackground(new Color(0, 0, 0, 0));
                    editorPane.setContentType("text/html");
                    editorPane.setEditable(false);
                    editorPane
                            .setText("Instructions:<ol>"
                                    + "<li>Load structure(s) from files (PDB or mmCif)</li>"
                                    + "<li>Select distance measure</li>"
                                    + "<li>Compute distance matrix</li>"
                                    + "<li>Visualise or cluster results</li></ol>");

                    setLayout(new GridLayout(1, 1));
                    add(editorPane);
                }
            }

            // ////////////////////////////////////////////////////////////////
            // fields
            private static final long serialVersionUID = 1L;
            private ActionPanel actionPanel;
            private InstructionsPanel instructionsPanel;

            // ////////////////////////////////////////////////////////////////
            // constructors
            public ActionInstructionsPanel() {
                actionPanel = new ActionPanel();
                instructionsPanel = new InstructionsPanel();

                setLayout(new GridLayout(1, 2));
                add(actionPanel);
                add(instructionsPanel);
            }
        }

        /**
         * Table model needed to represent distance matrix.
         */
        private class MatrixTableModel extends AbstractTableModel {
            // ////////////////////////////////////////////////////////////
            // fields
            private static final long serialVersionUID = 1L;

            // ////////////////////////////////////////////////////////////
            // constructors
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

            // ////////////////////////////////////////////////////////////
            // methods
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
        }

        // ////////////////////////////////////////////////////////////////////
        // fields
        private static final long serialVersionUID = 1L;
        private final JTable resultsTable;
        private double[][] tableValues;
        private String[] tableNames;
        private ActionInstructionsPanel actionInstructionsPanel;

        // ////////////////////////////////////////////////////////////////////
        // constructors
        public MainPanel() {
            super(new BorderLayout());

            tableNames = new String[0];
            tableValues = new double[0][];

            resultsTable = new JTable(new MatrixTableModel(tableNames,
                    tableValues));
            actionInstructionsPanel = new ActionInstructionsPanel();

            add(actionInstructionsPanel, BorderLayout.PAGE_START);
            add(new JScrollPane(resultsTable), BorderLayout.CENTER);
        }

        // ////////////////////////////////////////////////////////////////////
        // methods
        public void displayResults(String[] names, double[][] results) {
            tableNames = names.clone();
            tableValues = results.clone();
            resultsTable.setModel(new MatrixTableModel(names, results));
        }
    }

    // ////////////////////////////////////////////////////////////////////////
    // fields
    private static final long serialVersionUID = 1L;
    private final MainPanel mainPanel;
    private double[][] comparisonResults;
    private JList<String> list;
    private DefaultListModel<String> listModel;
    private String[] structureNames;

    // ////////////////////////////////////////////////////////////////////////
    // constructors
    public GlobalComparisonPanel() {
        super(new BorderLayout());

        listModel = new DefaultListModel<>();
        list = new JList<>(listModel);
        mainPanel = new MainPanel();

        add(mainPanel, BorderLayout.CENTER);
        add(list, BorderLayout.EAST);

        list.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    int index = list.getSelectedIndex();
                    if (index != -1) {
                        listModel.remove(index);
                        if (listModel.getSize() == 0) {
                            mainPanel.actionInstructionsPanel.actionPanel.buttonPanel.compare
                                    .setEnabled(false);
                            mainPanel.actionInstructionsPanel.actionPanel.buttonPanel.cluster
                                    .setEnabled(false);
                            mainPanel.actionInstructionsPanel.actionPanel.buttonPanel.visualise
                                    .setEnabled(false);
                        }
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent arg0) {
                // do nothing
            }

            @Override
            public void keyTyped(KeyEvent e) {
                // do nothing
            }
        });
    }

    // ////////////////////////////////////////////////////////////////////////
    // methods
    /**
     * Check if selected file is correct PDB and if so, add it to local cache.
     * 
     * @param text
     *            A path to PDB file.
     */
    public void addFile(String text) {
        if (PdbManager.loadStructure(text) != null) {
            listModel.addElement(text);
            mainPanel.actionInstructionsPanel.actionPanel.buttonPanel.compare
                    .setEnabled(true);
        } else {
            JOptionPane.showMessageDialog(null,
                    "Specified file is not a valid PDB file",
                    "Invalid PDB file", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Get data from comparison and cluster it
     */
    public void cluster() {
        if (comparisonResults != null) {
            ClusteringDialog dialog = new ClusteringDialog();
            dialog.setVisible(true);
        }
    }

    /**
     * Get picked structures, compare them using selected method and display the
     * results.
     */
    public void compare() {
        GlobalComparison[] methods = new GlobalComparison[] { new RMSD(),
                new MCQ() };
        // choose RMSD by default
        int chosen = 0;
        if (mainPanel.actionInstructionsPanel.actionPanel.methodPanel.mcqRadio
                .isSelected()) {
            chosen = 1;
        }

        Enumeration<String> elements = listModel.elements();
        List<String> vector = new ArrayList<>();
        while (elements.hasMoreElements()) {
            String element = elements.nextElement();
            vector.add(element);
        }
        Structure[] structures = PdbManager.getStructures(vector);
        structureNames = PdbManager.getNames(vector);

        try {
            comparisonResults = methods[chosen].compare(structures);
            mainPanel.displayResults(structureNames, comparisonResults);
            mainPanel.actionInstructionsPanel.actionPanel.buttonPanel.cluster
                    .setEnabled(true);
            mainPanel.actionInstructionsPanel.actionPanel.buttonPanel.visualise
                    .setEnabled(true);
        } catch (IncomparableStructuresException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(),
                    "Error during structure comparison",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Get data from comparison and visualise it on scatter plot.
     */
    public void visualise() {
        if (comparisonResults != null) {
            double[][] mds = MDS.multidimensionalScaling(comparisonResults, 2);
            if (mds == null) {
                JOptionPane.showMessageDialog(null,
                        "Cannot visualise specified structures in 2D",
                        "Problem during comparison visualisation",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            MDSPlot plot = new MDSPlot(mds, structureNames);
            plot.setVisible(true);
        }
    }
}
