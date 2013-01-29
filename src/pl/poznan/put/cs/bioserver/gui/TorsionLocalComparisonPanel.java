package pl.poznan.put.cs.bioserver.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;

import org.biojava.bio.structure.ResidueNumber;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.StructureImpl;
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
import pl.poznan.put.cs.bioserver.gui.helper.PdbChangeListener;
import pl.poznan.put.cs.bioserver.helper.PdbManager;
import pl.poznan.put.cs.bioserver.torsion.AngleDifference;

/**
 * A panel which is a graphical interface to a local comparison measure based on
 * torsion angle representation.
 * 
 * @author Tomasz Żok (tzok[at]cs.put.poznan.pl)
 */
public class TorsionLocalComparisonPanel extends JPanel {
	private class ConfigurationDialog extends JDialog {
		private static final long serialVersionUID = 1L;
		private final String[] namesAmino = new String[] { "Phi Φ", "Psi Ψ",
				"Omega Ω", "Average" };
		private final String[] namesNucleic = new String[] { "Alpha α",
				"Beta β", "Gamma γ", "Delta δ", "Epsilon ε", "Zeta ζ", "Chi χ",
				"Tau0 τ0", "Tau1 τ1", "Tau2 τ2", "Tau3 τ3", "Tau4 τ4",
				"P (sugar pucker)", "Average" };
		public List<String> selectedNames;

		public ConfigurationDialog(Frame owner) {
			super(owner, true);

			JPanel panelAnglesAmino = new JPanel();
			panelAnglesAmino.setLayout(new BoxLayout(panelAnglesAmino,
					BoxLayout.Y_AXIS));

			final JCheckBox[] checksAmino = new JCheckBox[namesAmino.length];
			for (int i = 0; i < namesAmino.length; i++) {
				JCheckBox checkBox = new JCheckBox(namesAmino[i]);
				checksAmino[i] = checkBox;
				panelAnglesAmino.add(checkBox);
			}

			final JButton buttonSelectAllAmino = new JButton("Select all");
			final JButton buttonClearAmino = new JButton("Clear");

			JPanel panelButtonsAmino = new JPanel();
			panelButtonsAmino.add(buttonSelectAllAmino);
			panelButtonsAmino.add(buttonClearAmino);

			JPanel panelAmino = new JPanel();
			panelAmino.setLayout(new BorderLayout());
			panelAmino.add(panelAnglesAmino, BorderLayout.CENTER);
			panelAmino.add(panelButtonsAmino, BorderLayout.SOUTH);
			panelAmino.setBorder(BorderFactory
					.createTitledBorder("Amino acids"));

			JPanel panelAnglesNucleic = new JPanel();
			panelAnglesNucleic.setLayout(new BoxLayout(panelAnglesNucleic,
					BoxLayout.Y_AXIS));

			final JCheckBox[] checksNucleic = new JCheckBox[namesNucleic.length];
			for (int i = 0; i < namesNucleic.length; i++) {
				JCheckBox checkBox = new JCheckBox(namesNucleic[i]);
				checksNucleic[i] = checkBox;
				panelAnglesNucleic.add(checkBox);
			}

			final JButton buttonSelectAllNucleic = new JButton("Select all");
			JButton buttonClearNucleic = new JButton("Clear");

			JPanel panelButtonsNucleic = new JPanel();
			panelButtonsNucleic.add(buttonSelectAllNucleic);
			panelButtonsNucleic.add(buttonClearNucleic);

			JPanel panelNucleic = new JPanel();
			panelNucleic.setLayout(new BorderLayout());
			panelNucleic.add(panelAnglesNucleic, BorderLayout.CENTER);
			panelNucleic.add(panelButtonsNucleic, BorderLayout.SOUTH);
			panelNucleic.setBorder(BorderFactory
					.createTitledBorder("Nucleotides"));

			JPanel panelOptions = new JPanel();
			panelOptions.setLayout(new GridLayout(1, 2));
			panelOptions.add(panelAmino);
			panelOptions.add(panelNucleic);

			JButton buttonOk = new JButton("OK");
			JButton buttonCancel = new JButton("Cancel");

			JPanel panelOkCancel = new JPanel();
			panelOkCancel.add(buttonOk);
			panelOkCancel.add(buttonCancel);

			setLayout(new BorderLayout());
			add(panelOptions, BorderLayout.CENTER);
			add(panelOkCancel, BorderLayout.SOUTH);

			ActionListener actionListenerSelection = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					JCheckBox[] checkBoxes;
					boolean state;

					Object source = arg0.getSource();
					if (source.equals(buttonSelectAllAmino)) {
						checkBoxes = checksAmino;
						state = true;
					} else if (source.equals(buttonClearAmino)) {
						checkBoxes = checksAmino;
						state = false;
					} else if (source.equals(buttonSelectAllNucleic)) {
						checkBoxes = checksNucleic;
						state = true;
					} else { // buttonClearNucleic
						checkBoxes = checksNucleic;
						state = false;
					}

					for (JCheckBox checkBox : checkBoxes) {
						checkBox.setSelected(state);
					}
				}
			};
			buttonSelectAllAmino.addActionListener(actionListenerSelection);
			buttonClearAmino.addActionListener(actionListenerSelection);
			buttonSelectAllNucleic.addActionListener(actionListenerSelection);
			buttonClearNucleic.addActionListener(actionListenerSelection);

			buttonOk.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					selectedNames = new ArrayList<>();
					for (JCheckBox[] array : new JCheckBox[][] { checksAmino,
							checksNucleic }) {
						for (JCheckBox checkBox : array) {
							if (checkBox.isSelected()) {
								String text = checkBox.getText();
								text = text.split(" ")[0];
								text = text.toUpperCase();
								selectedNames.add(text);
							}
						}
					}
					dispose();
				}
			});

			buttonCancel.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					selectedNames = null;
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

			setTitle("Configure local comparison");
		}
	}

	private class TorsionAxis extends NumberAxis {
		private static final long serialVersionUID = 1L;
		private Map<String, List<AngleDifference>> comparison;

		public TorsionAxis(Map<String, List<AngleDifference>> comparison) {
			this.comparison = comparison;
			setTickLabelFont(new Font(Font.DIALOG, Font.PLAIN, 8));
		}

		@Override
		public List refreshTicks(Graphics2D g2, AxisState state,
				Rectangle2D dataArea, RectangleEdge edge) {
			List<NumberTick> ticks = super.refreshTicks(g2, state, dataArea,
					edge);

			Map<Double, String> mapIndexLabel = new HashMap<>();
			List<AngleDifference> list = comparison.get("AVERAGE");
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

	private static final long serialVersionUID = 1L;
	static final Logger LOGGER = LoggerFactory
			.getLogger(TorsionLocalComparisonPanel.class);
	protected ConfigurationDialog dialog;

	// /////////////////////////////////////////////////////////////////////////
	// constructors
	public TorsionLocalComparisonPanel() {
		super();

		JButton buttonLoad = new JButton("Load structure(s)");
		final JButton buttonConfigure = new JButton("Configure");
		buttonConfigure.setEnabled(false);
		final JButton buttonCompareChain = new JButton("Compare selected chain");
		buttonCompareChain.setEnabled(false);
		final JButton buttonCompareAll = new JButton("Compare all chains");
		buttonCompareAll.setEnabled(false);

		JPanel panelButtons = new JPanel();
		panelButtons.add(buttonLoad);
		panelButtons.add(buttonConfigure);
		panelButtons.add(buttonCompareChain);
		panelButtons.add(buttonCompareAll);

		final PdbPanel panelPdb = new PdbPanel(new PdbChangeListener() {
			@Override
			public void pdbListChanged() {
				buttonConfigure.setEnabled(false);
				buttonCompareChain.setEnabled(false);
				buttonCompareAll.setEnabled(false);
			}
		});

		JPanel panelButtonsPdb = new JPanel();
		panelButtonsPdb.setLayout(new GridLayout(2, 1));
		panelButtonsPdb.add(panelButtons);
		panelButtonsPdb.add(panelPdb);

		JEditorPane editorPane = new JEditorPane();
		editorPane.setBackground(new Color(0, 0, 0, 0));
		editorPane.setContentType("text/html");
		editorPane.setEditable(false);
		editorPane
				.setText("Instructions:<ol>"
						+ "<li>Load structure(s) from files (PDB or mmCif)</li>"
						+ "<li>Configure the comparison properties</li>"
						+ "<li>Calculate differences of torsion angles for specified chains or for whole structures</li></ol>");

		JPanel panelOptions = new JPanel();
		panelOptions.setLayout(new GridLayout(1, 2));
		panelOptions.add(panelButtonsPdb);
		panelOptions.add(editorPane);

		final JPanel panelChart = new JPanel();
		panelChart.setLayout(new GridLayout(1, 1));

		setLayout(new BorderLayout());
		add(panelOptions, BorderLayout.NORTH);
		add(panelChart, BorderLayout.CENTER);

		buttonLoad.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				panelPdb.loadStructuresWithOpenDialog();
				if (panelPdb.getListModel().size() >= 2) {
					buttonConfigure.setEnabled(true);
				}
			}
		});

		buttonConfigure.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (dialog == null) {
					Container c = getParent();
					while (!(c instanceof Frame)) {
						c = c.getParent();
					}
					dialog = new ConfigurationDialog((Frame) c);
				}

				dialog.setVisible(true);
				if (dialog.selectedNames != null) {
					buttonCompareChain.setEnabled(true);
					buttonCompareAll.setEnabled(true);
				}
			}
		});

		ActionListener actionListenerComparison = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ListModel<File> model = panelPdb.getListModel();
				final Structure[] structures = new Structure[] {
						PdbManager.getStructure(model.getElementAt(0)),
						PdbManager.getStructure(model.getElementAt(1)) };

				if (e.getSource().equals(buttonCompareChain)) {
					int chainIndexFirst = panelPdb.getComboBoxFirst()
							.getSelectedIndex();
					int chainIndexSecond = panelPdb.getComboBoxSecond()
							.getSelectedIndex();
					structures[0] = new StructureImpl(
							structures[0].getChain(chainIndexFirst));
					structures[1] = new StructureImpl(
							structures[1].getChain(chainIndexSecond));
				}

				Map<String, List<AngleDifference>> result;
				try {
					result = TorsionLocalComparison.compare(structures[0],
							structures[1], false);
				} catch (StructureException e1) {
					JOptionPane.showMessageDialog(null, e1.getMessage(),
							"Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				DefaultXYDataset dataset = new DefaultXYDataset();
				for (String angle : dialog.selectedNames) {
					if (!result.containsKey(angle)) {
						continue;
					}
					List<AngleDifference> diffs = result.get(angle);
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
				NumberAxis xAxis = new TorsionAxis(result);
				xAxis.setLabel("Residue");

				NumberAxis yAxis = new NumberAxis();
				yAxis.setAutoRange(false);
				yAxis.setRange(0, Math.PI);
				yAxis.setLabel("Distance [rad]");

				XYPlot plot = new XYPlot(dataset, xAxis, yAxis,
						new DefaultXYItemRenderer());
				final ChartPanel chart = new ChartPanel(new JFreeChart(plot));

				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						panelChart.removeAll();
						panelChart.add(chart);
						panelChart.validate();
					}
				});
			}
		};
		buttonCompareChain.addActionListener(actionListenerComparison);
		buttonCompareAll.addActionListener(actionListenerComparison);
	}
}
