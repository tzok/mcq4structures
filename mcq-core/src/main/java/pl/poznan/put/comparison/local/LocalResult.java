package pl.poznan.put.comparison.local;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.table.TableModel;
import org.apache.commons.lang3.time.DateFormatUtils;
import pl.poznan.put.interfaces.Exportable;
import pl.poznan.put.interfaces.Tabular;
import pl.poznan.put.matching.FragmentComparison;
import pl.poznan.put.matching.FragmentMatch;
import pl.poznan.put.matching.ResidueComparison;
import pl.poznan.put.matching.SelectionMatch;
import pl.poznan.put.torsion.MasterTorsionAngleType;
import pl.poznan.put.torsion.TorsionAngleDelta;
import pl.poznan.put.utility.NonEditableDefaultTableModel;
import pl.poznan.put.utility.TabularExporter;

public interface LocalResult extends Exportable, Tabular {
  SelectionMatch selectionMatch();

  List<MasterTorsionAngleType> angleTypes();

  @Override
  default void export(final OutputStream stream) throws IOException {
    TabularExporter.export(asExportableTableModel(), stream);
  }

  @Override
  default File suggestName() {
    return new File(
        DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.format(new Date())
            + "-Local-Distance.csv");
  }

  @Override
  default TableModel asExportableTableModel() {
    return asTableModel(false);
  }

  @Override
  default TableModel asDisplayableTableModel() {
    return asTableModel(true);
  }

  default FragmentComparison asFragmentComparison() {
    final List<ResidueComparison> residueComparisons =
        selectionMatch().getFragmentMatches().stream()
            .map(FragmentMatch::getResidueComparisons)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    return FragmentComparison.fromResidueComparisons(residueComparisons, angleTypes());
  }

  default TableModel asTableModel(final boolean isDisplay) {
    final String[] columnNames = new String[angleTypes().size() + 1];
    columnNames[0] = isDisplay ? "" : null;

    for (int i = 0; i < angleTypes().size(); i++) {
      final MasterTorsionAngleType angle = angleTypes().get(i);
      columnNames[i + 1] = isDisplay ? angle.longDisplayName() : angle.exportName();
    }

    final List<ResidueComparison> residueComparisons = new ArrayList<>();
    for (final FragmentMatch fragmentMatch : selectionMatch().getFragmentMatches()) {
      residueComparisons.addAll(fragmentMatch.getResidueComparisons());
    }

    final List<String> labels = selectionMatch().getResidueLabels();
    final String[][] data = new String[residueComparisons.size()][];

    for (int i = 0; i < residueComparisons.size(); i++) {
      final ResidueComparison residueComparison = residueComparisons.get(i);
      data[i] = new String[angleTypes().size() + 1];
      data[i][0] = labels.get(i);

      for (int j = 0; j < angleTypes().size(); j++) {
        final MasterTorsionAngleType angle = angleTypes().get(j);
        final TorsionAngleDelta delta = residueComparison.angleDelta(angle);
        data[i][j + 1] = delta.toString(isDisplay);
      }
    }

    return new NonEditableDefaultTableModel(data, columnNames);
  }
}
