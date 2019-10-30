package pl.poznan.put.comparison.local;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.table.TableModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.poznan.put.matching.FragmentComparison;
import pl.poznan.put.matching.FragmentMatch;
import pl.poznan.put.matching.ResidueComparison;
import pl.poznan.put.matching.SelectionMatch;
import pl.poznan.put.torsion.MasterTorsionAngleType;
import pl.poznan.put.torsion.TorsionAngleDelta;
import pl.poznan.put.utility.NonEditableDefaultTableModel;
import pl.poznan.put.utility.TabularExporter;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class MCQLocalResult extends LocalResult {
  private static final Logger LOGGER = LoggerFactory.getLogger(MCQLocalResult.class);

  private List<MasterTorsionAngleType> angleTypes;

  public MCQLocalResult(
      final SelectionMatch selectionMatch, final List<? extends MasterTorsionAngleType> angleTypes) {
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
}
