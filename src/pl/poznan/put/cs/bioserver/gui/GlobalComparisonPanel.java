package pl.poznan.put.cs.bioserver.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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
import javax.swing.JComboBox;
import javax.swing.JDialog;
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
            c.gridwidth = 2;
            container.add(linkage, c);

            c.gridx = 0;
            c.gridy = 1;
            c.gridwidth = 1;
            container.add(kmedoids, c);

            c.gridx = 1;
            c.gridwidth = 2;
            container.add(kspinner, c);

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
                    kspinner.setEnabled(!isHierarchical);
                }
            };
            hierarchical.addActionListener(radioActionListener);
            kmedoids.addActionListener(radioActionListener);

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
                        int k = (Integer) kspinner.getValue();
                        plot = new KMedoidsPlot(comparisonResults,
                                structureNames, k);
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
         * Subpanel which shows button and method choice panels in separate rows
         */
        private class ActionPanel extends JPanel {
            // /////////////////////////////////////////////////////////////////
            // fields
            private static final long serialVersionUID = 1L;
            private InstructionsPanel instructionsPanel;
            private MethodPanel methodPanel;

            // /////////////////////////////////////////////////////////////////
            // constructors
            public ActionPanel() {
                super();
                setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
                ButtonPanel buttonPanel = new ButtonPanel();
                methodPanel = new MethodPanel();
                instructionsPanel = new InstructionsPanel();

                add(buttonPanel);
                add(methodPanel);
                add(instructionsPanel);

                final JFileChooser chooser = new JFileChooser();
                chooser.addChoosableFileFilter(new FileNameExtensionFilter(
                        "PDB file format", "pdb", "pdb1", "ent", "brk", "gz"));
                chooser.setMultiSelectionEnabled(true);

                buttonPanel.addPDB.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                            for (File f : chooser.getSelectedFiles()) {
                                addFile(f.getAbsolutePath());
                            }
                            instructionsPanel
                                    .setInstruction(InstructionsPanel.INSTRUCTION_COMPARE);
                        }
                    }
                });

                buttonPanel.compare.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        compare();
                        instructionsPanel
                                .setInstruction(InstructionsPanel.INSTRUCTION_VISUALIZE_CLUSTER);
                    }
                });

                buttonPanel.visualise.addActionListener(new ActionListener() {
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
         * Subpanel which contains action buttons.
         */
        private class ButtonPanel extends JPanel {
            // ////////////////////////////////////////////////////////////////
            // fields
            private static final long serialVersionUID = 1L;
            private JButton addPDB;
            private JButton cluster;
            private JButton compare;
            private JButton visualise;

            // ////////////////////////////////////////////////////////////////
            // constructors
            public ButtonPanel() {
                super();
                addPDB = new JButton("Add file");
                compare = new JButton("Compare");
                cluster = new JButton("Cluster");
                visualise = new JButton("Visualise");

                add(addPDB);
                add(compare);
                add(visualise);
                add(cluster);
            }
        }

        /**
         * Subpanel containing instructions for user.
         */
        private class InstructionsPanel extends JPanel {
            private static final int FONT_SIZE = 12;
            public static final int INSTRUCTION_ADD_FILE = 0;
            public static final int INSTRUCTION_COMPARE = 1;
            public static final int INSTRUCTION_VISUALIZE_CLUSTER = 2;
            // /////////////////////////////////////////////////////////////////
            // fields
            private static final long serialVersionUID = 1L;
            private final String[] instructions = {
                    "Click on \"Add file\" to select at least two structures "
                            + "to compare",
                    "Select comparison method and click \"Compare\" when you "
                            + "are ready",
                    "Having the distance matrix calculated, you can now click "
                            + "on \"Visualize\" or \"Cluster\"" };
            private final JLabel instructionsLabel;

            // /////////////////////////////////////////////////////////////////
            // constructors
            public InstructionsPanel() {
                super();
                instructionsLabel = new JLabel(
                        instructions[InstructionsPanel.INSTRUCTION_ADD_FILE]);
                instructionsLabel.setFont(new Font(Font.DIALOG, Font.BOLD
                        | Font.ITALIC, InstructionsPanel.FONT_SIZE));
                add(instructionsLabel);
            }

            // /////////////////////////////////////////////////////////////////
            // methods
            /**
             * Sets text containing instructions for user to take.
             * 
             * @param index
             *            Index of instruction in the set.
             */
            public void setInstruction(int index) {
                instructionsLabel.setText(instructions[index]);
            }
        }

        /**
         * Table model needed to represent distance matrix.
         */
        private class MatrixTableModel extends AbstractTableModel {
            // ////////////////////////////////////////////////////////////////
            // fields
            private static final long serialVersionUID = 1L;

            // ////////////////////////////////////////////////////////////////
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

            // ////////////////////////////////////////////////////////////////
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

        /**
         * Subpanel which allows to choose comparison method.
         */
        private class MethodPanel extends JPanel {
            // ////////////////////////////////////////////////////////////////
            // fields
            private static final long serialVersionUID = 1L;
            private final JRadioButton rmsdRadio;
            private JRadioButton mcqRadio;

            // ////////////////////////////////////////////////////////////////
            // constructors
            public MethodPanel() {
                super();
                rmsdRadio = new JRadioButton("RMSD", true);
                mcqRadio = new JRadioButton("MCQ", false);

                ButtonGroup buttonGroup = new ButtonGroup();
                buttonGroup.add(rmsdRadio);
                buttonGroup.add(mcqRadio);

                add(new JLabel("Choose comparison method: "));
                add(rmsdRadio);
                add(mcqRadio);
            }
        }

        // ////////////////////////////////////////////////////////////////////
        // fields
        private static final long serialVersionUID = 1L;
        private final JTable resultsTable;
        private double[][] tableValues;
        private String[] tableNames;
        private ActionPanel actionPanel;

        // ////////////////////////////////////////////////////////////////////
        // constructors
        public MainPanel() {
            super(new BorderLayout());

            tableNames = new String[0];
            tableValues = new double[0][];

            resultsTable = new JTable(new MatrixTableModel(tableNames,
                    tableValues));
            actionPanel = new ActionPanel();

            add(actionPanel, BorderLayout.PAGE_START);
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
                    listModel.remove(list.getSelectedIndex());
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
        if (PdbManager.addStructure(text)) {
            listModel.addElement(text);
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
        if (mainPanel.actionPanel.methodPanel.mcqRadio.isSelected()) {
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
