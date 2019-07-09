package pl.poznan.put.matching;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.swing.table.TableModel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import pl.poznan.put.circular.Angle;
import pl.poznan.put.interfaces.Exportable;
import pl.poznan.put.interfaces.Tabular;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.pdb.analysis.PdbResidue;
import pl.poznan.put.structure.secondary.CanonicalStructureExtractor;
import pl.poznan.put.structure.secondary.formats.BpSeq;
import pl.poznan.put.structure.secondary.formats.Converter;
import pl.poznan.put.structure.secondary.formats.DotBracket;
import pl.poznan.put.structure.secondary.formats.InvalidStructureException;
import pl.poznan.put.structure.secondary.formats.LevelByLevelConverter;
import pl.poznan.put.structure.secondary.pseudoknots.elimination.MinGain;
import pl.poznan.put.torsion.MasterTorsionAngleType;
import pl.poznan.put.torsion.TorsionAngleDelta;
import pl.poznan.put.utility.NonEditableDefaultTableModel;
import pl.poznan.put.utility.TabularExporter;

/** Holds information about a match between two {@link PdbCompactFragment}. */
@Data
@Slf4j
public class FragmentMatch implements Exportable, Tabular {
  private static final Converter CONVERTER = new LevelByLevelConverter(new MinGain(), 1);

  private final PdbCompactFragment targetFragment;
  private final PdbCompactFragment modelFragment;
  private final boolean isTargetSmaller;
  private final int shift;
  private final FragmentComparison fragmentComparison;

  /**
   * Construct an invalid instance i.e. one which will return {@link
   * ResidueComparison#invalidInstance(PdbResidue, PdbResidue)} for all residue pairs.
   *
   * @param targetFragment First fragment.
   * @param modelFragment Second fragment.
   * @param angleTypes Torsion angle types.
   * @return An instance representing an invalid comparison of fragments.
   */
  static FragmentMatch invalidInstance(
      final PdbCompactFragment targetFragment,
      final PdbCompactFragment modelFragment,
      final List<MasterTorsionAngleType> angleTypes) {
    final List<PdbResidue> targetResidues = targetFragment.getResidues();
    final List<PdbResidue> modelResidues = modelFragment.getResidues();
    final int targetSize = targetResidues.size();
    final int modelSize = modelResidues.size();
    final boolean isTargetSmaller = targetSize < modelSize;
    final int bothInvalidCount = isTargetSmaller ? targetSize : modelSize;

    final List<ResidueComparison> residueComparisons =
        IntStream.range(0, bothInvalidCount)
            .mapToObj(
                i -> ResidueComparison.invalidInstance(targetResidues.get(i), modelResidues.get(i)))
            .collect(Collectors.toList());

    return new FragmentMatch(
        targetFragment,
        modelFragment,
        isTargetSmaller,
        0,
        FragmentComparison.invalidInstance(
            residueComparisons, angleTypes, 0, 0, bothInvalidCount, 0));
  }

  public final DotBracket getTargetDotBracket() throws InvalidStructureException {
    final BpSeq bpSeq = CanonicalStructureExtractor.bpSeq(targetFragment);
    return FragmentMatch.CONVERTER.convert(bpSeq);
  }

  public final List<ResidueComparison> getResidueComparisons() {
    return fragmentComparison.getResidueComparisons();
  }

  public final List<MasterTorsionAngleType> getAngleTypes() {
    return fragmentComparison.getAngleTypes();
  }

  public final int getTargetInvalidCount() {
    return fragmentComparison.getTargetInvalidCount();
  }

  public final int getModelInvalidCount() {
    return fragmentComparison.getModelInvalidCount();
  }

  public final int getBothInvalidCount() {
    return fragmentComparison.getBothInvalidCount();
  }

  public final int getValidCount() {
    return fragmentComparison.getValidCount();
  }

  public final Angle getMeanDelta() {
    return fragmentComparison.getMeanDelta();
  }

  public final int getResidueCount() {
    return fragmentComparison.getResidueCount();
  }

  public final boolean isValid() {
    return fragmentComparison.isValid();
  }

  @Override
  public final String toString() {
    final PdbCompactFragment target;
    final PdbCompactFragment model;

    if (isTargetSmaller) {
      target = targetFragment;
      model = modelFragment.shift(shift, targetFragment.getResidues().size());
    } else {
      target = targetFragment.shift(shift, modelFragment.getResidues().size());
      model = modelFragment;
    }

    return target.getName() + " & " + model.getName();
  }

  public final MoleculeType moleculeType() {
    assert targetFragment.getMoleculeType() == modelFragment.getMoleculeType();
    return targetFragment.getMoleculeType();
  }

  public final DotBracket matchedSecondaryStructure() throws InvalidStructureException {
    final PdbCompactFragment target =
        isTargetSmaller
            ? targetFragment
            : targetFragment.shift(shift, modelFragment.getResidues().size());
    final BpSeq bpSeq = CanonicalStructureExtractor.bpSeq(target);
    return FragmentMatch.CONVERTER.convert(bpSeq);
  }

  public final List<String> matchedResidueNames() {
    final PdbCompactFragment target =
        isTargetSmaller
            ? targetFragment
            : targetFragment.shift(shift, modelFragment.getResidues().size());
    return target.getResidues().stream().map(PdbResidue::toString).collect(Collectors.toList());
  }

  @Override
  public final File suggestName() {
    return new File(
        DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.format(new Date()) + "-fragment.csv");
  }

  @Override
  public final void export(final OutputStream stream) throws IOException {
    TabularExporter.export(asExportableTableModel(), stream);
  }

  @Override
  public final TableModel asExportableTableModel() {
    return asTableModel(false);
  }

  @Override
  public final TableModel asDisplayableTableModel() {
    return asTableModel(true);
  }

  @SuppressWarnings("AssignmentToNull")
  private TableModel asTableModel(final boolean isDisplay) {
    final List<MasterTorsionAngleType> angleTypes = fragmentComparison.getAngleTypes();
    final int size = angleTypes.size();

    final String[] columnNames = new String[(size * 2) + 2];
    columnNames[0] = columnNames[1] = isDisplay ? "" : null;

    for (int i = 0; i < size; i++) {
      final MasterTorsionAngleType angle = angleTypes.get(i);
      final String angleName = isDisplay ? angle.getLongDisplayName() : angle.getExportName();
      columnNames[i + 2] = angleName;
      columnNames[size + i + 2] = angleName;
    }

    final List<ResidueComparison> residueComparisons = fragmentComparison.getResidueComparisons();
    final String[][] data = new String[residueComparisons.size()][];
    final char[] dotBracket = dotBracketChars();

    for (int i = 0; i < residueComparisons.size(); i++) {
      final ResidueComparison residueComparison = residueComparisons.get(i);
      data[i] = new String[(size * 2) + 2];
      data[i][0] =
          String.format("%s / %s", residueComparison.getTarget(), residueComparison.getModel());
      data[i][1] = String.valueOf(dotBracket[i]);

      for (int j = 0; j < size; j++) {
        final MasterTorsionAngleType angle = angleTypes.get(j);
        final TorsionAngleDelta delta = residueComparison.getAngleDelta(angle);
        data[i][j + 2] = delta.toString(isDisplay);
        data[i][size + j + 2] = delta.getRangeDifference().toString();
      }
    }

    return new NonEditableDefaultTableModel(data, columnNames);
  }

  private char[] dotBracketChars() {
    try {
      final DotBracket dotBracket = getTargetDotBracket();
      return dotBracket.getStructure().toCharArray();
    } catch (InvalidStructureException e) {
      FragmentMatch.log.warn("Failed to extract 2D structure", e);
    }
    return StringUtils.repeat('.', targetFragment.getResidues().size()).toCharArray();
  }
}
