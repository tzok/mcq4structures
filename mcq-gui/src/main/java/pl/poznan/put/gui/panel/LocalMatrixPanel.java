package pl.poznan.put.gui.panel;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.svg.SVGDocument;
import pl.poznan.put.comparison.ImmutableMCQ;
import pl.poznan.put.comparison.MCQ;
import pl.poznan.put.comparison.local.MCQLocalResult;
import pl.poznan.put.comparison.mapping.AngleDeltaMapper;
import pl.poznan.put.comparison.mapping.RangeDifferenceMapper;
import pl.poznan.put.constant.Colors;
import pl.poznan.put.datamodel.ProcessingResult;
import pl.poznan.put.gui.component.SVGComponent;
import pl.poznan.put.matching.FragmentMatch;
import pl.poznan.put.matching.SelectionFactory;
import pl.poznan.put.matching.SelectionMatch;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbChain;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.structure.StructureManager;
import pl.poznan.put.svg.SecondaryStructureVisualizer;
import pl.poznan.put.torsion.MasterTorsionAngleType;
import pl.poznan.put.visualisation.VisualizableFragmentMatch;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.List;

public final class LocalMatrixPanel extends JPanel {
  private static final long serialVersionUID = -1143002202021225397L;
  private static final Logger LOGGER = LoggerFactory.getLogger(LocalMatrixPanel.class);

  private final JTextPane labelInfoMatrix = new JTextPane();
  private final JTable tableMatrix = new JTable();
  private final JTabbedPane tabbedPane = new JTabbedPane();

  private Pair<? extends PdbModel, ? extends PdbModel> structures = null;
  private Pair<? extends List<PdbChain>, ? extends List<PdbChain>> chains = null;

  public LocalMatrixPanel() {
    super(new BorderLayout());

    labelInfoMatrix.setBorder(new EmptyBorder(10, 10, 10, 0));
    labelInfoMatrix.setContentType("text/html");
    labelInfoMatrix.setEditable(false);
    labelInfoMatrix.setFont(UIManager.getFont("Label.font"));
    labelInfoMatrix.setOpaque(false);

    tableMatrix.setDefaultRenderer(Object.class, new ColorTableCellRenderer());

    final JPanel panelInfo = new JPanel(new BorderLayout());
    panelInfo.add(labelInfoMatrix, BorderLayout.CENTER);

    final JScrollPane scrollPane = new JScrollPane(tableMatrix);
    tabbedPane.add("Results", scrollPane);

    add(panelInfo, BorderLayout.PAGE_START);
    add(tabbedPane, BorderLayout.CENTER);
  }

  public void setStructuresAndChains(
      final Pair<? extends PdbModel, ? extends PdbModel> structures,
      final Pair<? extends List<PdbChain>, ? extends List<PdbChain>> chains) {
    this.structures = structures;
    this.chains = chains;
    removeAllButFirstTab();
    tableMatrix.setModel(new DefaultTableModel());
    updateHeader(false);
  }

  public ProcessingResult compareAndDisplayTable(
      final List<MasterTorsionAngleType> selectedAngles) {
    final StructureSelection selectionL =
        SelectionFactory.create(StructureManager.getName(structures.getLeft()), chains.getLeft());
    final StructureSelection selectionR =
        SelectionFactory.create(StructureManager.getName(structures.getRight()), chains.getRight());
    final MCQ mcq = ImmutableMCQ.of(MoleculeType.RNA);
    final MCQLocalResult result = (MCQLocalResult) mcq.comparePair(selectionL, selectionR);
    final SelectionMatch selectionMatch = result.selectionMatch();
    removeAllButFirstTab();

    for (final FragmentMatch fragmentMatch : selectionMatch.getFragmentMatches()) {
      final VisualizableFragmentMatch visualizableFragmentMatch =
          new VisualizableFragmentMatch(fragmentMatch);
      final SVGDocument chart = visualizableFragmentMatch.visualize(1024, 576);
      final SVGComponent chartComponent = new SVGComponent(chart, "chart");
      tabbedPane.add(fragmentMatch.toString(), chartComponent);

      if (fragmentMatch.getTargetFragment().moleculeType() == MoleculeType.RNA) {
        final SVGDocument angles =
            SecondaryStructureVisualizer.visualize(fragmentMatch, AngleDeltaMapper.getInstance());
        final SVGComponent anglesComponent = new SVGComponent(angles, "secondary");
        tabbedPane.add(
            String.format("%s (secondary structure, angles)", fragmentMatch), anglesComponent);

        final SVGDocument ranges =
            SecondaryStructureVisualizer.visualize(
                fragmentMatch, RangeDifferenceMapper.getInstance());
        final SVGComponent rangesComponent = new SVGComponent(ranges, "secondary");
        tabbedPane.add(
            String.format("%s (secondary structure, ranges)", fragmentMatch), rangesComponent);
      }

      final SVGDocument percentiles = visualizableFragmentMatch.visualizePercentiles(1024, 576);
      final SVGComponent percentilesComponent = new SVGComponent(percentiles, "percentiles");
      tabbedPane.add(fragmentMatch + " (percentiles)", percentilesComponent);
    }

    tableMatrix.setModel(result.asDisplayableTableModel());
    updateHeader(true);

    return new ProcessingResult(result);
  }

  private void removeAllButFirstTab() {
    while (tabbedPane.getComponentCount() > 1) {
      tabbedPane.remove(1);
    }
  }

  private void updateHeader(final boolean readyResults) {
    final PdbModel left = structures.getLeft();
    final PdbModel right = structures.getRight();

    final StringBuilder builder = new StringBuilder();
    builder.append("<html>Structures selected for local distance measure: ");
    builder.append("<span style=\"color: blue\">");
    builder.append(StructureManager.getName(left));
    builder.append('.');

    for (final PdbChain chain : chains.getLeft()) {
      builder.append(chain.identifier());
    }

    builder.append("</span>, <span style=\"color: green\">");
    builder.append(StructureManager.getName(right));
    builder.append('.');

    for (final PdbChain chain : chains.getRight()) {
      builder.append(chain.identifier());
    }

    builder.append("</span>");

    if (readyResults) {
      builder.append("<br>Local distance vector(s):");
    }

    builder.append("</html>");
    labelInfoMatrix.setText(builder.toString());
  }

  private static class ColorTableCellRenderer extends DefaultTableCellRenderer {
    private final TableCellRenderer defaultRenderer = new DefaultTableCellRenderer();

    @Override
    public final Component getTableCellRendererComponent(
        final JTable jTable,
        final Object o,
        final boolean b,
        final boolean b1,
        final int i,
        final int i1) {
      final Component component =
          defaultRenderer.getTableCellRendererComponent(jTable, o, b, b1, i, i1);
      if (i1 == 0) {
        component.setBackground(Color.WHITE);
        component.setForeground(Color.BLACK);
      } else {
        component.setBackground(Colors.getDistinctColors()[i1]);
      }
      return component;
    }
  }
}
