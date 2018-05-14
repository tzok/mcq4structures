package pl.poznan.put.comparison.local;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.jzy3d.analysis.AnalysisLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.svg.SVGDocument;
import pl.poznan.put.constant.Unicode;
import pl.poznan.put.gui.component.NonEditableDefaultTableModel;
import pl.poznan.put.interfaces.ExportFormat;
import pl.poznan.put.matching.FragmentComparison;
import pl.poznan.put.matching.FragmentMatch;
import pl.poznan.put.matching.ResidueComparison;
import pl.poznan.put.matching.SelectionMatch;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.structure.secondary.formats.InvalidStructureException;
import pl.poznan.put.torsion.MasterTorsionAngleType;
import pl.poznan.put.torsion.TorsionAngleDelta;
import pl.poznan.put.utility.TabularExporter;
import pl.poznan.put.visualisation.Surface3D;

import javax.swing.JOptionPane;
import javax.swing.table.TableModel;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@XmlRootElement
public class MCQLocalResult extends LocalResult {
  private static final Logger LOGGER = LoggerFactory.getLogger(MCQLocalResult.class);

  @XmlElement private List<MasterTorsionAngleType> angleTypes;

  public MCQLocalResult(
      final SelectionMatch selectionMatch, final List<MasterTorsionAngleType> angleTypes) {
    super(selectionMatch);
    this.angleTypes = new ArrayList<>(angleTypes);
  }

  public final FragmentComparison asFragmentComparison() {
    final List<ResidueComparison> residueComparisons = new ArrayList<>();
    selectionMatch
        .getFragmentMatches()
        .forEach(fragmentMatch -> residueComparisons.addAll(fragmentMatch.getResidueComparisons()));
    return FragmentComparison.fromResidueComparisons(residueComparisons, angleTypes);
  }

  @Override
  public final void export(final OutputStream stream) throws IOException {
    TabularExporter.export(asExportableTableModel(), stream);
  }

  @Override
  public final ExportFormat getExportFormat() {
    return ExportFormat.CSV;
  }

  @Override
  public final File suggestName() {
    return new File(
        DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.format(new Date())
            + "-Local-Distance.csv");
  }

  @Override
  public final TableModel asExportableTableModel() {
    return asTableModel(false);
  }

  @Override
  public final TableModel asDisplayableTableModel() {
    return asTableModel(true);
  }

  private TableModel asTableModel(final boolean isDisplay) {
    final String[] columnNames = new String[angleTypes.size() + 1];
    columnNames[0] = isDisplay ? "" : null;

    for (int i = 0; i < angleTypes.size(); i++) {
      final MasterTorsionAngleType angle = angleTypes.get(i);
      columnNames[i + 1] = isDisplay ? angle.getLongDisplayName() : angle.getExportName();
    }

    final List<ResidueComparison> residueComparisons = new ArrayList<>();
    for (final FragmentMatch fragmentMatch : selectionMatch.getFragmentMatches()) {
      residueComparisons.addAll(fragmentMatch.getResidueComparisons());
    }

    final List<String> labels = selectionMatch.getResidueLabels();
    final String[][] data = new String[residueComparisons.size()][];

    for (int i = 0; i < residueComparisons.size(); i++) {
      final ResidueComparison residueComparison = residueComparisons.get(i);
      data[i] = new String[angleTypes.size() + 1];
      data[i][0] = labels.get(i);

      for (int j = 0; j < angleTypes.size(); j++) {
        final MasterTorsionAngleType angle = angleTypes.get(j);
        final TorsionAngleDelta delta = residueComparison.getAngleDelta(angle);
        data[i][j + 1] = delta.toString(isDisplay);
      }
    }

    return new NonEditableDefaultTableModel(data, columnNames);
  }

  @Override
  public final SVGDocument visualize() {
    throw new IllegalArgumentException(
        "Invalid usage, please use visualize() on FragmentMatch " + "instances");
  }

  @Override
  public final void visualize3D() {
    if (angleTypes.size() <= 1) {
      JOptionPane.showMessageDialog(
          null,
          "At least two torsion angle types " + "are required for 3D visualization",
          "Error",
          JOptionPane.ERROR_MESSAGE);
      return;
    }

    try {
      for (final FragmentMatch fragmentMatch : selectionMatch.getFragmentMatches()) {
        final PdbCompactFragment target = fragmentMatch.getTargetFragment();
        List<String> ticksY = null;
        String labelY = null;

        if (target.getMoleculeType() == MoleculeType.RNA) {
          try {
            ticksY = fragmentMatch.generateLabelsWithDotBracket();
            labelY = "Secondary structure";
          } catch (final InvalidStructureException e) {
            MCQLocalResult.LOGGER.warn("Failed to extract canonical secondary " + "structure", e);
          }
        }

        if (ticksY == null) {
          ticksY = fragmentMatch.generateLabelsWithResidueNames();
          labelY = "ResID";
        }

        final String name = fragmentMatch.toString();
        final double[][] matrix = prepareMatrix(fragmentMatch);
        final List<String> ticksX = prepareTicksX();
        final NavigableMap<Double, String> valueTickZ = MCQLocalResult.prepareTicksZ();
        final String labelX = "Angle type";
        final String labelZ = "Distance";
        final boolean showAllTicksX = true;
        final boolean showAllTicksY = false;

        final Surface3D surface3d =
            new Surface3D(
                name,
                matrix,
                ticksX,
                ticksY,
                valueTickZ,
                labelX,
                labelY,
                labelZ,
                showAllTicksX,
                showAllTicksY);
        AnalysisLauncher.open(surface3d);
      }
    } catch (final Exception e) {
      final String message = "Failed to visualize in 3D";
      MCQLocalResult.LOGGER.error(message, e);
      JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private double[][] prepareMatrix(final FragmentMatch fragmentMatch) {
    final List<ResidueComparison> residueComparisons = fragmentMatch.getResidueComparisons();
    final double[][] matrix = new double[angleTypes.size()][];

    for (int i = 0; i < angleTypes.size(); i++) {
      final MasterTorsionAngleType angleType = angleTypes.get(i);
      matrix[i] = new double[residueComparisons.size()];

      for (int j = 0; j < residueComparisons.size(); j++) {
        final ResidueComparison residueComparison = residueComparisons.get(j);
        matrix[i][j] = residueComparison.getAngleDelta(angleType).getDelta().getRadians();
      }
    }

    return matrix;
  }

  private List<String> prepareTicksX() {
    final List<String> ticksX = new ArrayList<>();
    for (final MasterTorsionAngleType angleType : angleTypes) {
      ticksX.add(angleType.getExportName());
    }
    return ticksX;
  }

  protected static NavigableMap<Double, String> prepareTicksZ() {
    final NavigableMap<Double, String> valueTickZ = new TreeMap<>();
    valueTickZ.put(0.0, "0");

    for (double radians = Math.PI / 12.0;
        radians <= (Math.PI + 1.0e-3);
        radians += Math.PI / 12.0) {
      valueTickZ.put(radians, Long.toString(Math.round(Math.toDegrees(radians))) + Unicode.DEGREE);
    }

    return valueTickZ;
  }
}
