package pl.poznan.put.comparison;

import org.immutables.value.Value;
import pl.poznan.put.circular.Angle;
import pl.poznan.put.circular.samples.ImmutableAngleSample;
import pl.poznan.put.comparison.exception.IncomparableStructuresException;
import pl.poznan.put.comparison.global.GlobalComparator;
import pl.poznan.put.comparison.global.GlobalResult;
import pl.poznan.put.comparison.global.ImmutableMCQGlobalResult;
import pl.poznan.put.comparison.local.ImmutableMCQLocalResult;
import pl.poznan.put.comparison.local.ImmutableModelsComparisonResult;
import pl.poznan.put.comparison.local.LocalComparator;
import pl.poznan.put.comparison.local.LocalResult;
import pl.poznan.put.comparison.local.ModelsComparisonResult;
import pl.poznan.put.matching.FragmentMatch;
import pl.poznan.put.matching.ImmutableMCQMatcher;
import pl.poznan.put.matching.ResidueComparison;
import pl.poznan.put.matching.SelectionMatch;
import pl.poznan.put.matching.StructureMatcher;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.torsion.MasterTorsionAngleType;
import pl.poznan.put.torsion.TorsionAngleDelta;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of MCQ global similarity measure based on torsion angle representation.
 *
 * @author Tomasz Zok (tzok[at]cs.put.poznan.pl)
 */
@SuppressWarnings("UseOfSystemOutOrSystemErr")
@Value.Immutable
public abstract class MCQ implements GlobalComparator, LocalComparator {
  @Value.Parameter(order = 1)
  public abstract MoleculeType moleculeType();

  @Value.Default
  public List<MasterTorsionAngleType> angleTypes() {
    return moleculeType().mainAngleTypes();
  }

  @Override
  public final String getName() {
    return "MCQ";
  }

  @Override
  public final boolean isAngularMeasure() {
    return true;
  }

  @Override
  public final GlobalResult compareGlobally(
      final StructureSelection s1, final StructureSelection s2) {
    final StructureMatcher matcher = ImmutableMCQMatcher.of(moleculeType());
    final SelectionMatch matches = matcher.matchSelections(s1, s2);

    final List<Angle> deltas =
        matches.getFragmentMatches().stream()
            .map(FragmentMatch::getResidueComparisons)
            .flatMap(Collection::stream)
            .map(ResidueComparison::angleDeltas)
            .flatMap(Collection::stream)
            .filter(delta -> delta.state() == TorsionAngleDelta.State.BOTH_VALID)
            .filter(delta -> angleTypes().contains(delta.angleType()))
            .map(TorsionAngleDelta::delta)
            .collect(Collectors.toList());

    if (deltas.isEmpty()) {
      throw new IncomparableStructuresException("No matching fragments found");
    }

    return ImmutableMCQGlobalResult.of(matches, ImmutableAngleSample.of(deltas));
  }

  @Override
  public final LocalResult comparePair(
      final StructureSelection target, final StructureSelection model) {
    final StructureMatcher matcher = ImmutableMCQMatcher.of(moleculeType());
    final SelectionMatch matches = matcher.matchSelections(target, model);
    return ImmutableMCQLocalResult.of(matches, angleTypes());
  }

  @Override
  public final ModelsComparisonResult compareModels(
      final PdbCompactFragment target, final List<PdbCompactFragment> models) {
    // sanity check
    for (final PdbCompactFragment fragment : models) {
      if ((fragment.moleculeType() != target.moleculeType())
          || (fragment.residues().size() != target.residues().size())) {
        throw new IncomparableStructuresException(
            "All models must be of the same type and size as the reference structure");
      }
    }

    final StructureMatcher matcher = ImmutableMCQMatcher.of(moleculeType());
    final List<PdbCompactFragment> modelsWithoutTarget =
        models.stream().filter(fragment -> !fragment.equals(target)).collect(Collectors.toList());
    final List<FragmentMatch> matches =
        modelsWithoutTarget.stream()
            .map(fragment -> matcher.matchFragments(target, fragment))
            .collect(Collectors.toList());

    return ImmutableModelsComparisonResult.of(target, modelsWithoutTarget, matches, angleTypes());
  }
}
