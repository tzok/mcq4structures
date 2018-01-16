package pl.poznan.put.gui.panel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.svg.SVGDocument;
import pl.poznan.put.comparison.MCQ;
import pl.poznan.put.comparison.exception.IncomparableStructuresException;
import pl.poznan.put.comparison.local.LocalComparator;
import pl.poznan.put.comparison.local.ModelsComparisonResult;
import pl.poznan.put.datamodel.ProcessingResult;
import pl.poznan.put.gui.Messages;
import pl.poznan.put.gui.component.SVGComponent;
import pl.poznan.put.matching.AngleDeltaIteratorFactory;
import pl.poznan.put.matching.TypedDeltaIteratorFactory;
import pl.poznan.put.matching.stats.MultiMatchStatistics;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.protein.torsion.ProteinTorsionAngleType;
import pl.poznan.put.rna.torsion.RNATorsionAngleType;
import pl.poznan.put.torsion.AverageTorsionAngleType;
import pl.poznan.put.torsion.MasterTorsionAngleType;
import pl.poznan.put.utility.svg.SVGHelper;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.BorderLayout;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LocalMultiMatrixPanel extends JPanel {
  private static final long serialVersionUID = 1743569049211593671L;
  private static final Logger LOGGER = LoggerFactory.getLogger(LocalMultiMatrixPanel.class);

  private final JTextPane labelInfoMatrix = new JTextPane();
  private final JTable tableMatrix = new JTable();
  private final JTable histogramMatrix = new JTable();
  private final JTable percentileMatrix = new JTable();
  private final SVGComponent visualization =
      new SVGComponent(SVGHelper.emptyDocument(), "colorbar"); // NON-NLS
  private List<PdbCompactFragment> fragments = Collections.emptyList();

  public LocalMultiMatrixPanel() {
    super(new BorderLayout());

    labelInfoMatrix.setBorder(new EmptyBorder(10, 10, 10, 0));
    labelInfoMatrix.setContentType("text/html"); // NON-NLS
    labelInfoMatrix.setEditable(false);
    labelInfoMatrix.setFont(UIManager.getFont("Label.font"));
    labelInfoMatrix.setOpaque(false);

    histogramMatrix.setAutoCreateRowSorter(true);
    percentileMatrix.setAutoCreateRowSorter(true);

    final JPanel panelInfo = new JPanel(new BorderLayout());
    panelInfo.add(labelInfoMatrix, BorderLayout.CENTER);

    final JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.add(Messages.getString("results"), new JScrollPane(tableMatrix));
    tabbedPane.add(Messages.getString("histograms"), new JScrollPane(histogramMatrix));
    tabbedPane.add(Messages.getString("percentiles"), new JScrollPane(percentileMatrix));
    tabbedPane.add(Messages.getString("visualization"), new JScrollPane(visualization));

    add(panelInfo, BorderLayout.PAGE_START);
    add(tabbedPane, BorderLayout.CENTER);
  }

  public final void setFragments(final List<PdbCompactFragment> fragments) {
    this.fragments = new ArrayList<>(fragments);
    final TableModel emptyDataModel = new DefaultTableModel();
    tableMatrix.setModel(emptyDataModel);
    histogramMatrix.setModel(emptyDataModel);
    percentileMatrix.setModel(emptyDataModel);
    visualization.setSVGDocument(SVGHelper.emptyDocument());
    updateHeader(false);
  }

  private void updateHeader(final boolean readyResults) {
    final StringBuilder builder = new StringBuilder("<html>"); // NON-NLS
    builder.append(Messages.getString("structures.selected.for.local.distance.measure"));
    int i = 0;

    for (final PdbCompactFragment fragment : fragments) {
      builder.append(
          String.format(
              "<span style=\"color: %s\">", // NON-NLS
              ((i % 2) == 0)
                  ? "blue" // NON-NLS
                  : "green")); // NON-NLS
      builder.append(fragment.getName());
      builder.append("</span>, "); // NON-NLS
      i++;
    }

    builder.delete(builder.length() - 2, builder.length());

    if (readyResults) {
      builder.append("<br>"); // NON-NLS
      builder.append(Messages.getString("local.distance.vector.s"));
    }

    builder.append("</html>"); // NON-NLS
    labelInfoMatrix.setText(builder.toString());
  }

  public final ProcessingResult compareAndDisplayTable() {
    try {
      final PdbCompactFragment reference = selectReferenceStructure();
      if (reference == null) {
        return ProcessingResult.emptyInstance();
      }

      final MasterTorsionAngleType selectedAngleType = selectReferenceTorsionAngleType(reference);
      if (selectedAngleType == null) {
        return ProcessingResult.emptyInstance();
      }

      final LocalComparator mcq = new MCQ(Collections.singletonList(selectedAngleType));
      final ModelsComparisonResult result = mcq.compareModels(reference, fragments);
      final ModelsComparisonResult.SelectedAngle selectedAngle =
          result.selectAngle(selectedAngleType);
      final AngleDeltaIteratorFactory iteratorFactory =
          new TypedDeltaIteratorFactory(selectedAngleType);
      final MultiMatchStatistics statistics =
          MultiMatchStatistics.calculate(iteratorFactory, selectedAngle);
      final SVGDocument document = selectedAngle.visualize();

      tableMatrix.setModel(selectedAngle.asDisplayableTableModel());
      histogramMatrix.setModel(statistics.histogramsAsTableModel(true));
      percentileMatrix.setModel(statistics.percentilesAsTableModel(true));
      visualization.setSVGDocument(document);
      updateHeader(true);

      return new ProcessingResult(selectedAngle);
    } catch (final IncomparableStructuresException e) {
      final String message = Messages.getString("failed.to.compare.structures");
      LocalMultiMatrixPanel.LOGGER.error(message, e);
      JOptionPane.showMessageDialog(
          this, message, Messages.getString("error"), JOptionPane.ERROR_MESSAGE);
    }

    return ProcessingResult.emptyInstance();
  }

  private PdbCompactFragment selectReferenceStructure() {
    final PdbCompactFragmentWrapper[] fragmentArray =
        new PdbCompactFragmentWrapper[fragments.size()];
    for (int i = 0; i < fragments.size(); i++) {
      fragmentArray[i] = new PdbCompactFragmentWrapper(fragments.get(i));
    }
    final Object o =
        JOptionPane.showInputDialog(
            this,
            Messages.getString("select.your.reference.structure"),
            Messages.getString("reference.structure"),
            JOptionPane.INFORMATION_MESSAGE,
            null,
            fragmentArray,
            fragmentArray[0]);
    final PdbCompactFragmentWrapper wrapper = (PdbCompactFragmentWrapper) o;
    return wrapper.getFragment();
  }

  private MasterTorsionAngleType selectReferenceTorsionAngleType(
      final PdbCompactFragment reference) {
    final MoleculeType moleculeType = reference.getMoleculeType();
    final MasterTorsionAngleType[] mainAngles;
    final AverageTorsionAngleType averageTorsionAngleType;

    switch (moleculeType) {
      case PROTEIN:
        mainAngles = ProteinTorsionAngleType.mainAngles();
        averageTorsionAngleType = ProteinTorsionAngleType.getAverageOverMainAngles();
        break;
      case RNA:
        mainAngles = RNATorsionAngleType.mainAngles();
        averageTorsionAngleType = RNATorsionAngleType.getAverageOverMainAngles();
        break;
      case UNKNOWN:
      default:
        final String message =
            MessageFormat.format(Messages.getString("unknown.molecule.type.0"), moleculeType);
        throw new IllegalArgumentException(message);
    }

    final MasterTorsionAngleType[] anglesToSelectFrom =
        Arrays.copyOf(mainAngles, mainAngles.length + 1);
    anglesToSelectFrom[mainAngles.length] = averageTorsionAngleType;

    final String title = Messages.getString("torsion.angle");
    final String message = Messages.getString("select.torsion.angle");
    final int type = JOptionPane.INFORMATION_MESSAGE;
    final Object o =
        JOptionPane.showInputDialog(
            this, message, title, type, null, anglesToSelectFrom, averageTorsionAngleType);
    return (MasterTorsionAngleType) o;
  }

  private static final class PdbCompactFragmentWrapper {
    private final PdbCompactFragment fragment;

    private PdbCompactFragmentWrapper(final PdbCompactFragment fragment) {
      super();
      this.fragment = fragment;
    }

    private PdbCompactFragment getFragment() {
      return fragment;
    }

    @Override
    public String toString() {
      return fragment.getName();
    }
  }
}
