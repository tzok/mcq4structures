package pl.poznan.put.matching;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import pl.poznan.put.circular.Angle;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.pdb.analysis.PdbResidue;
import pl.poznan.put.structure.secondary.CanonicalStructureExtractor;
import pl.poznan.put.structure.secondary.DotBracketSymbol;
import pl.poznan.put.structure.secondary.formats.BpSeq;
import pl.poznan.put.structure.secondary.formats.Converter;
import pl.poznan.put.structure.secondary.formats.DotBracket;
import pl.poznan.put.structure.secondary.formats.InvalidStructureException;
import pl.poznan.put.structure.secondary.formats.LevelByLevelConverter;
import pl.poznan.put.structure.secondary.pseudoknots.elimination.MinGain;
import pl.poznan.put.torsion.MasterTorsionAngleType;

@Data
@Slf4j
public class FragmentMatch {
  private final PdbCompactFragment targetFragment;
  private final PdbCompactFragment modelFragment;
  private final boolean isTargetSmaller;
  private final int shift;
  private final FragmentComparison fragmentComparison;

  public static FragmentMatch invalidInstance(
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

  public final int getMismatchCount() {
    return fragmentComparison.getMismatchCount();
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

  public final List<String> generateLabelsWithDotBracket() throws InvalidStructureException {
    final PdbCompactFragment target =
        isTargetSmaller
            ? targetFragment
            : targetFragment.shift(shift, modelFragment.getResidues().size());
    final List<String> result = new ArrayList<>();
    final List<PdbResidue> targetResidues = target.getResidues();
    final BpSeq bpSeq = CanonicalStructureExtractor.getCanonicalSecondaryStructure(target);

    final Converter converter = new LevelByLevelConverter(new MinGain(), 0);
    final DotBracket dotBracket = converter.convert(bpSeq);

    for (int i = 0; i < targetResidues.size(); i++) {
      final DotBracketSymbol symbol = dotBracket.getSymbol(i);
      result.add(Character.toString(symbol.getStructure()));
    }

    return result;
  }

  public final List<String> generateLabelsWithResidueNames() {
    final PdbCompactFragment target =
        isTargetSmaller
            ? targetFragment
            : targetFragment.shift(shift, modelFragment.getResidues().size());
    final List<String> result = new ArrayList<>();
    for (final PdbResidue lname : target.getResidues()) {
      result.add(lname.toString());
    }
    return result;
  }
}
