package pl.poznan.put.comparison.local;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.swing.table.TableModel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jumpmind.symmetric.csv.CsvWriter;
import pl.poznan.put.interfaces.Exportable;
import pl.poznan.put.interfaces.Tabular;
import pl.poznan.put.matching.FragmentMatch;
import pl.poznan.put.matching.MatchCollection;
import pl.poznan.put.matching.ResidueComparison;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.pdb.analysis.PdbResidue;
import pl.poznan.put.structure.CanonicalStructureExtractor;
import pl.poznan.put.structure.formats.BpSeq;
import pl.poznan.put.structure.formats.Converter;
import pl.poznan.put.structure.formats.DotBracket;
import pl.poznan.put.structure.formats.ImmutableDefaultConverter;
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
  public final List<FragmentMatch> getFragmentMatches() {
    return Collections.unmodifiableList(fragmentMatches);
  }

  @Override
  public final void export(final OutputStream stream) throws IOException {
    final CsvWriter csvWriter = new CsvWriter(stream, ',', StandardCharsets.UTF_8);

    // first row
    csvWriter.write(null);
    for (final PdbResidue residue : target.residues()) {
      csvWriter.write(Integer.toString(residue.residueNumber()));
    }
    csvWriter.endRecord();

    // second row
    csvWriter.write(null);
    for (final PdbResidue residue : target.residues()) {
      csvWriter.write(Character.toString(residue.oneLetterName()));
    }
    csvWriter.endRecord();

    final BpSeq bpSeq = CanonicalStructureExtractor.bpSeq(target);
    final Converter converter = ImmutableDefaultConverter.of();
    final DotBracket dotBracket = converter.convert(bpSeq);

    // third row
    csvWriter.write(null);
    for (final char c : dotBracket.structure().toCharArray()) {
      csvWriter.write(Character.toString(c));
    }
    csvWriter.endRecord();

    final List<Pair<PdbCompactFragment, FragmentMatch>> sortedResults =
        IntStream.range(0, models.size())
            .mapToObj(i -> Pair.of(models.get(i), fragmentMatches.get(i)))
            .sorted(Comparator.comparingDouble(t -> t.getValue().getMeanDelta().radians()))
            .collect(Collectors.toList());

    for (final Pair<PdbCompactFragment, FragmentMatch> pair : sortedResults) {
      final PdbCompactFragment model = pair.getKey();
      final FragmentMatch match = pair.getValue();
      csvWriter.write(model.name());

      for (int j = 0; j < target.residues().size(); j++) {
        final ResidueComparison comparison = match.getResidueComparisons().get(j);
        final TorsionAngleDelta delta = comparison.angleDelta(angleType);
        csvWriter.write(delta.exportName());
      }

      csvWriter.endRecord();
    }

    csvWriter.close();
  }

  @Override
  public final File suggestName() {
    final String builder =
        models.stream()
            .map(model -> "-" + model)
            .collect(
                Collectors.joining(
                    "",
                    DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.format(new Date())
                        + "-Local-Distance-Multi",
                    ".csv"));

    return new File(builder);
  }

  @Override
  public final TableModel asExportableTableModel() {
    return asTableModel(false);
  }

  @Override
  public final TableModel asDisplayableTableModel() {
    return asTableModel(true);
  }

  public final Pair<Double, Double> getMinMax() {
    double min = Double.POSITIVE_INFINITY;
    double max = Double.NEGATIVE_INFINITY;

    for (final FragmentMatch match : fragmentMatches) {
      for (final ResidueComparison result : match.getResidueComparisons()) {
        final double delta = result.angleDelta(angleType).delta().radians();

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

  private TableModel asTableModel(final boolean isDisplay) {
    final String[] columnNames = new String[models.size() + 1];
    //noinspection AssignmentToNull
    columnNames[0] = isDisplay ? "" : null;
    for (int i = 0; i < models.size(); i++) {
      columnNames[i + 1] = models.get(i).name();
    }

    final String[][] data = new String[target.residues().size()][];

    for (int i = 0; i < target.residues().size(); i++) {
      data[i] = new String[models.size() + 1];
      data[i][0] = target.residues().get(i).toString();

      for (int j = 0; j < models.size(); j++) {
        final FragmentMatch fragmentMatch = fragmentMatches.get(j);
        final ResidueComparison residueComparison = fragmentMatch.getResidueComparisons().get(i);
        final TorsionAngleDelta delta = residueComparison.angleDelta(angleType);
        data[i][j + 1] = isDisplay ? delta.shortDisplayName() : delta.exportName();
      }
    }

    return new NonEditableDefaultTableModel(data, columnNames);
  }
}
