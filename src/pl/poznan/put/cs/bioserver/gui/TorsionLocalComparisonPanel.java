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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
			setTickLabelFont(new Font(Font.DIALOG, Font.PLAIN, 8));
		}

		@Override
		public List<NumberTick> refreshTicks(Graphics2D g2, AxisState state,
				Rectangle2D dataArea, RectangleEdge edge) {
			List<NumberTick> ticks = super.refreshTicks(g2, state, dataArea,
					edge);

			Map<Double, String> mapIndexLabel = new HashMap<>();
			List<AngleDifference> list = comparison.get("MCQ");
			for (int i = 0; i < list.size(); i++) {
				AngleDifference ad = list.get(i);
				ResidueNumber residue = ad.getResidue();
				mapIndexLabel.put(
						(double) i,
						String.format("%s:%03d", residue.getChainId(),
								residue.getSeqNum()));
			}

			List<NumberTick> result = new ArrayList<>();
			for (NumberTick nt : ticks) {
				result.add(new NumberTick(TickType.MINOR, nt.getValue(),
						mapIndexLabel.get(nt.getValue()), nt.getTextAnchor(),
						nt.getRotationAnchor(), Math.PI / 4));
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
			names[0] = controlPanel.listModel.get(0);
			names[1] = controlPanel.listModel.get(1);

			Structure[] structures = PdbManager.getStructures(Arrays
					.asList(names));

			/*
			 * compare them
			 */
			try {
				return TorsionLocalComparison.compare(structures[0],
						structures[1], false);
			} catch (StructureException e) {
				TorsionLocalComparisonPanel.LOGGER.error(
						"Failed to compare structures", e);
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
					JOptionPane.showMessageDialog(null, "The angle " + angle
							+ " is not defined for loaded molecules", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				List<AngleDifference> diffs = compare.get(angle);
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

			add(list, BorderLayout.EAST);
			add(optionsPanel, BorderLayout.CENTER);

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
			if (listModel.size() == 2) {
				// TODO
			}
			return true;
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
		private JButton compare;
		private JRadioButton[] groupChoiceRadios;

		// /////////////////////////////////////////////////////////////////////
		// constructors
		public OptionsPanel() {
			setLayout(new GridBagLayout());
			addFile = new JButton("Add file");
			compare = new JButton("Compare");

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

			c.gridx = 0;
			c.gridy = 2;
			c.gridwidth = 1;
			for (JRadioButton b : groupChoiceRadios) {
				add(b, c);
				c.gridy++;
			}

			c.gridx = 1;
			c.gridy = 2;
			for (JCheckBox b : angleChoiceChecks[0]) {
				add(b, c);
				c.gridx++;
			}

			c.gridx = 1;
			c.gridy = 3;
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
	static final Logger LOGGER = LoggerFactory
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
