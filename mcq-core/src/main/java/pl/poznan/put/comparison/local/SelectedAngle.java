package pl.poznan.put.comparison.local;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.swing.table.TableModel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.jumpmind.symmetric.csv.CsvWriter;
import pl.poznan.put.interfaces.Exportable;
import pl.poznan.put.interfaces.Tabular;
import pl.poznan.put.matching.FragmentMatch;
import pl.poznan.put.matching.MatchCollection;
import pl.poznan.put.matching.ResidueComparison;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.pdb.analysis.PdbResidue;
import pl.poznan.put.torsion.MasterTorsionAngleType;
import pl.poznan.put.torsion.TorsionAngleDelta;
import pl.poznan.put.utility.NonEditableDefaultTableModel;

@Data
@Slf4j
public class SelectedAngle implements Exportable, Tabular, MatchCollection {
  private final MasterTorsionAngleType angleType;
  private final PdbCompactFragment target;
  private final List<PdbCompactFragment> models;
  private final List<FragmentMatch> fragmentMatches;

  @Override
  public List<FragmentMatch> getFragmentMatches() {
    return Collections.unmodifiableList(fragmentMatches);
  }

  @Override
  public void export(final OutputStream stream) throws IOException {
    final CsvWriter csvWriter = new CsvWriter(stream, ',', Charset.forName("UTF-8"));
    csvWriter.write(null);

    for (final PdbCompactFragment model : models) {
      csvWriter.write(model.toString());
    }

    csvWriter.endRecord();

    for (int i = 0; i < target.getResidues().size(); i++) {
      final PdbResidue residue = target.getResidues().get(i);
      csvWriter.write(residue.toString());

      for (int j = 0; j < models.size(); j++) {
        final FragmentMatch fragmentMatch = fragmentMatches.get(j);
        final ResidueComparison residueComparison = fragmentMatch.getResidueComparisons().get(i);
        final TorsionAngleDelta delta = residueComparison.getAngleDelta(angleType);
        csvWriter.write(delta.toExportString());
      }

      csvWriter.endRecord();
    }

    csvWriter.close();
  }

  @Override
  public File suggestName() {
    final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
    final StringBuilder builder = new StringBuilder(sdf.format(new Date()));
    builder.append("-Local-Distance-Multi");

    for (final PdbCompactFragment model : models) {
      builder.append('-');
      builder.append(model);
    }

    builder.append(".csv");
    return new File(builder.toString());
  }

  @Override
  public TableModel asExportableTableModel() {
    return asTableModel(false);
  }

  @Override
  public TableModel asDisplayableTableModel() {
    return asTableModel(true);
  }

  private TableModel asTableModel(final boolean isDisplay) {
    final String[] columnNames = new String[models.size() + 1];
    //noinspection AssignmentToNull
    columnNames[0] = isDisplay ? "" : null;
    for (int i = 0; i < models.size(); i++) {
      columnNames[i + 1] = models.get(i).getName();
    }

    final String[][] data = new String[target.getResidues().size()][];

    for (int i = 0; i < target.getResidues().size(); i++) {
      data[i] = new String[models.size() + 1];
      data[i][0] = target.getResidues().get(i).toString();

      for (int j = 0; j < models.size(); j++) {
        final FragmentMatch fragmentMatch = fragmentMatches.get(j);
        final ResidueComparison residueComparison = fragmentMatch.getResidueComparisons().get(i);
        final TorsionAngleDelta delta = residueComparison.getAngleDelta(angleType);

        if (delta == null) {
          data[i][j + 1] = null;
        } else {
          data[i][j + 1] = isDisplay ? delta.toDisplayString() : delta.toExportString();
        }
      }
    }

    return new NonEditableDefaultTableModel(data, columnNames);
  }

  public Pair<Double, Double> getMinMax() {
    double min = Double.POSITIVE_INFINITY;
    double max = Double.NEGATIVE_INFINITY;

    for (final FragmentMatch match : fragmentMatches) {
      for (final ResidueComparison result : match.getResidueComparisons()) {
        final double delta = result.getAngleDelta(angleType).getDelta().getRadians();

        if (delta < min) {
          min = delta;
        }

        if (delta > max) {
          max = delta;
        }
      }
    }

    return Pair.of(min, max);
  }
}
