package pl.poznan.put.cs.bioserver.gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.log4j.Logger;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.ResidueNumber;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTick;
import org.jfree.chart.axis.TickType;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;

import pl.poznan.put.cs.bioserver.comparison.TorsionLocalComparison;
import pl.poznan.put.cs.bioserver.helper.PdbManager;
import pl.poznan.put.cs.bioserver.torsion.AminoAcidDihedral;
import pl.poznan.put.cs.bioserver.torsion.AngleDifference;
import pl.poznan.put.cs.bioserver.torsion.AngleType;
import pl.poznan.put.cs.bioserver.torsion.NucleotideDihedral;

/**
 * A panel which is a graphical interface to a local comparison measure based on
 * torsion angle representation.
 * 
 * @author Tomasz Żok (tzok[at]cs.put.poznan.pl)
 */
public class TorsionLocalComparisonPanel extends JPanel {
    private class TorsionAxis extends NumberAxis {
        private static final long serialVersionUID = 1L;
        private Map<String, List<AngleDifference>> comparison;

        public TorsionAxis(Map<String, List<AngleDifference>> comparison) {
            this.comparison = comparison;
        }

        @Override
        public List refreshTicks(Graphics2D g2, AxisState state,
                Rectangle2D dataArea, RectangleEdge edge) {
            List<NumberTick> ticks = super.refreshTicks(g2, state, dataArea,
                    edge);

            Map<Double, String> mapIndexLabel = new HashMap<>();
            List<AngleDifference> list = comparison.get("MCQ");
            for (int i = 0; i < list.size(); i++) {
                AngleDifference ad = list.get(i);
                ResidueNumber residue = ad.getResidue();
                mapIndexLabel.put((double) i, residue.getChainId() + ":"
                        + residue.getSeqNum());
            }

            List<NumberTick> result = new ArrayList<>();
            for (NumberTick nt : ticks) {
                result.add(new NumberTick(TickType.MINOR, nt.getValue(),
                        mapIndexLabel.get(nt.getValue()),
                        TextAnchor.BASELINE_RIGHT, nt.getRotationAnchor(),
                        Math.PI / 2));
            }
            return result;
        }
    }

    private final class CompareStructures implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            Map<String, List<AngleDifference>> compare = compareStructures();
            if (compare != null) {
                drawResults(compare);
            }
        }

        private Map<String, List<AngleDifference>> compareStructures() {
            /*
             * check structure count
             */
            if (controlPanel.listModel.size() != 2) {
                JOptionPane.showMessageDialog(null,
                        "You need exactly two structures"
                                + " to compare them locally",
                        "Incorrect number of structures " + "to compare",
                        JOptionPane.INFORMATION_MESSAGE);
                return null;
            }
            /*
             * get specified structures
             */
            String[] names = new String[2];
            // int[] indices = new int[2];
            names[0] = controlPanel.listModel.get(0);
            // indices[0] = controlPanel.optionsPanel.comboBoxFirst
            // .getSelectedIndex();
            names[1] = controlPanel.listModel.get(1);
            // indices[1] = controlPanel.optionsPanel.comboBoxSecond
            // .getSelectedIndex();

            Structure[] structures = PdbManager.getStructures(Arrays
                    .asList(names));
            // Chain[] chains = new Chain[2];
            // for (int i = 0; i < 2; ++i) {
            // chains[i] = structures[i].getChain(indices[i]);
            // }

            // if (Helper.isNucleicAcid(chains[0]) != Helper
            // .isNucleicAcid(chains[1])) {
            // JOptionPane.showMessageDialog(null, "Cannot "
            // + "compare structures of different type", "Error",
            // JOptionPane.ERROR_MESSAGE);
            // return null;
            // }

            /*
             * compare them
             */
            try {
                return TorsionLocalComparison.compare(structures[0],
                        structures[1], false);
                // return TorsionLocalComparison.compare(new StructureImpl(
                // chains[0]), new StructureImpl(chains[1]), false);
            } catch (StructureException e) {
                TorsionLocalComparisonPanel.LOGGER.error(e);
                JOptionPane.showMessageDialog(null, e.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
                return null;
            }
        }

        private void drawResults(Map<String, List<AngleDifference>> compare) {
            /*
             * read options from GUI
             */
            int type = controlPanel.optionsPanel.groupChoiceRadios[0]
                    .isSelected() ? 0 : 1;
            /*
             * read angles that have to be plotted
             */
            List<String> anglesToShow = new ArrayList<>();
            for (JCheckBox b : controlPanel.optionsPanel.angleChoiceChecks[type]) {
                if (b.isSelected()) {
                    anglesToShow.add(b.getText());
                }
            }
            /*
             * prepare dataset with points
             */
            DefaultXYDataset dataset = new DefaultXYDataset();
            for (String angle : anglesToShow) {
                if (!compare.containsKey(angle)) {
                    JOptionPane.showMessageDialog(null, "The " + "angle "
                            + angle + " is not defined "
                            + "for loaded molecules", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                List<AngleDifference> diffs = compare.get(angle);
                double[] x = new double[diffs.size()];
                double[] y = new double[diffs.size()];
                for (int i = 0; i < diffs.size(); i++) {
                    AngleDifference ad = diffs.get(i);
                    x[i] = i;
                    y[i] = ad.getDifference();
                }
                dataset.addSeries(angle, new double[][] { x, y });
            }
            NumberAxis xAxis = new TorsionAxis(compare);
            xAxis.setLabel("Group index");

            NumberAxis yAxis = new NumberAxis();
            yAxis.setAutoRange(false);
            yAxis.setRange(0, Math.PI);
            yAxis.setLabel("Distance [rad]");

            XYPlot plot = new XYPlot(dataset, xAxis, yAxis,
                    new DefaultXYItemRenderer());
            remove(1);
            add(new ChartPanel(new JFreeChart(plot)));
            SwingUtilities
                    .updateComponentTreeUI(TorsionLocalComparisonPanel.this);
        }
    }

    /**
     * Subpanel containing file list and another panel with all options.
     */
    private static class ControlPanel extends JPanel {
        private static final long serialVersionUID = 1L;

        // /////////////////////////////////////////////////////////////////////
        // fields
        private InstructionsPanel instructionsPanel;
        private JList<String> list;
        private DefaultListModel<String> listModel;
        private OptionsPanel optionsPanel;

        // /////////////////////////////////////////////////////////////////////
        // constructors
        public ControlPanel() {
            super(new BorderLayout());

            listModel = new DefaultListModel<>();
            list = new JList<>(listModel);
            optionsPanel = new OptionsPanel();
            instructionsPanel = new InstructionsPanel();

            add(list, BorderLayout.EAST);
            add(optionsPanel, BorderLayout.CENTER);
            add(instructionsPanel, BorderLayout.SOUTH);

            list.addKeyListener(new KeyListener() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                        listModel.remove(list.getSelectedIndex());
                    }
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    // do nothing
                }

                @Override
                public void keyTyped(KeyEvent e) {
                    // do nothing
                }
            });

            final JFileChooser chooser = new JFileChooser();
            chooser.addChoosableFileFilter(new FileNameExtensionFilter(
                    "PDB file format", "pdb", "pdb1", "ent", "brk", "gz"));
            chooser.setMultiSelectionEnabled(true);

            optionsPanel.addFile.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
                        return;
                    }
                    for (File f : chooser.getSelectedFiles()) {
                        if (!addFile(f)) {
                            break;
                        }
                    }
                }
            });
        }

        boolean addFile(File file) {
            if (listModel.size() >= 2) {
                JOptionPane.showMessageDialog(null,
                        "Only two structures are allowed for"
                                + " local comparison measures",
                        "Maximum number of structures reached",
                        JOptionPane.WARNING_MESSAGE);
                return false;
            }

            String path = file.getAbsolutePath();
            if (PdbManager.loadStructure(path) != null) {
                listModel.addElement(path);
            } else {
                JOptionPane.showMessageDialog(null, "Specified file is not a "
                        + "valid PDB file", "Invalid PDB file",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }

            /*
             * if that was the first file added, then update the list of chains
             */
            loadChainsNames(listModel.size() - 1);
            if (listModel.size() == 2) {
                instructionsPanel
                        .setInstruction(InstructionsPanel.INSTRUCTION_SELECT_CHAIN);
            }
            return true;
        }

        private void loadChainsNames(int index) {
            DefaultComboBoxModel<String> model = null;
            JComboBox<String> comboBox = null;
            if (index == 0) {
                model = optionsPanel.comboBoxModelFirst;
                comboBox = optionsPanel.comboBoxFirst;
            } else {
                model = optionsPanel.comboBoxModelSecond;
                comboBox = optionsPanel.comboBoxSecond;
            }

            List<String> vector = new ArrayList<>();
            vector.add(listModel.getElementAt(index));
            Structure[] structures = PdbManager.getStructures(vector);
            model.removeAllElements();
            for (Chain c : structures[0].getChains()) {
                model.addElement(c.getChainID());
            }
            comboBox.setSelectedIndex(0);
        }
    }

    /**
     * Subpanel containing instructions for the user.
     */
    private static class InstructionsPanel extends JPanel {
        private static final int FONT_SIZE = 12;
        public static final int INSTRUCTION_ADD_FILE = 0;
        public static final int INSTRUCTION_SELECT_CHAIN = 1;
        // /////////////////////////////////////////////////////////////////////
        // fields
        private static final long serialVersionUID = 1L;
        private final String[] instructions = {
                "Click \"Add file\" to select exactly two structures to compare",
                "<html><ol><li>From the dropdown combo box select chain to compare</li>"
                        + "<li>Select comparison mode: \"Amino acids\" or "
                        + "\"Nucleotides\"</li>"
                        + "<li>Select group names to compare and plot.<br>"
                        + "MCQ stands for Mean of Circular Quantities - an average "
                        + "value for each group</li></html>" };
        private final JLabel instructionsLabel;

        // /////////////////////////////////////////////////////////////////////
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
     * Subpanel containing all buttons and boxes concerning local comparison
     * measure options.
     */
    private static class OptionsPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private JButton addFile;
        private JCheckBox[][] angleChoiceChecks;
        // /////////////////////////////////////////////////////////////////////
        // fields
        private JComboBox<String> comboBoxFirst, comboBoxSecond;
        private DefaultComboBoxModel<String> comboBoxModelFirst,
                comboBoxModelSecond;
        private JButton compare;
        private JRadioButton[] groupChoiceRadios;

        // /////////////////////////////////////////////////////////////////////
        // constructors
        public OptionsPanel() {
            setLayout(new GridBagLayout());
            addFile = new JButton("Add file");
            compare = new JButton("Compare");

            comboBoxModelFirst = new DefaultComboBoxModel<>();
            comboBoxModelSecond = new DefaultComboBoxModel<>();
            comboBoxFirst = new JComboBox<>(comboBoxModelFirst);
            comboBoxSecond = new JComboBox<>(comboBoxModelSecond);

            groupChoiceRadios = new JRadioButton[] {
                    new JRadioButton("Amino acids", true),
                    new JRadioButton("Nucleotides", false) };
            ButtonGroup group = new ButtonGroup();
            group.add(groupChoiceRadios[0]);
            group.add(groupChoiceRadios[1]);

            angleChoiceChecks = new JCheckBox[2][];

            AngleType[] angles = AminoAcidDihedral.getAngles();
            angleChoiceChecks[0] = new JCheckBox[angles.length + 1];
            for (int i = 0; i < angles.length; i++) {
                angleChoiceChecks[0][i] = new JCheckBox(
                        angles[i].getAngleName());
            }
            angleChoiceChecks[0][angles.length] = new JCheckBox("MCQ");

            angles = NucleotideDihedral.getAngles();
            angleChoiceChecks[1] = new JCheckBox[angles.length + 2];
            for (int i = 0; i < angles.length; i++) {
                angleChoiceChecks[1][i] = new JCheckBox(
                        angles[i].getAngleName());
            }
            angleChoiceChecks[1][angles.length] = new JCheckBox("P");
            angleChoiceChecks[1][angles.length + 1] = new JCheckBox("MCQ");

            for (JCheckBox b : angleChoiceChecks[1]) {
                b.setEnabled(false);
            }

            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 3;
            c.gridy = 0;
            c.gridwidth = 1;
            c.gridheight = 1;
            c.fill = GridBagConstraints.BOTH;
            add(addFile, c);

            c.gridx = 4;
            add(compare, c);

            c.gridx = 2;
            c.gridy = 1;
            c.gridwidth = 4;
            add(comboBoxFirst, c);

            c.gridy = 2;
            add(comboBoxSecond, c);

            c.gridx = 0;
            c.gridy = 3;
            c.gridwidth = 1;
            for (JRadioButton b : groupChoiceRadios) {
                add(b, c);
                c.gridy++;
            }

            c.gridx = 1;
            c.gridy = 3;
            for (JCheckBox b : angleChoiceChecks[0]) {
                add(b, c);
                c.gridx++;
            }

            c.gridx = 1;
            c.gridy = 4;
            for (JCheckBox b : angleChoiceChecks[1]) {
                add(b, c);
                c.gridx++;
            }

            ActionListener radioActionListener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    for (int i = 0; i < 2; ++i) {
                        if (groupChoiceRadios[i].isSelected()) {
                            for (JCheckBox b : angleChoiceChecks[i]) {
                                b.setEnabled(true);
                            }
                            for (JCheckBox b : angleChoiceChecks[i ^ 1]) {
                                b.setEnabled(false);
                            }
                            break;
                        }
                    }
                }
            };
            groupChoiceRadios[0].addActionListener(radioActionListener);
            groupChoiceRadios[1].addActionListener(radioActionListener);
        }
    }

    // /////////////////////////////////////////////////////////////////////////
    // fields
    private static final long serialVersionUID = 1L;
    static final Logger LOGGER = Logger
            .getLogger(TorsionLocalComparisonPanel.class);
    private ControlPanel controlPanel;

    // /////////////////////////////////////////////////////////////////////////
    // constructors
    public TorsionLocalComparisonPanel() {
        super(new BorderLayout());
        JPanel chartPanel = new JPanel();

        controlPanel = new ControlPanel();
        add(controlPanel, BorderLayout.NORTH);
        add(chartPanel, BorderLayout.CENTER);

        controlPanel.optionsPanel.compare
                .addActionListener(new CompareStructures());

    }
}
