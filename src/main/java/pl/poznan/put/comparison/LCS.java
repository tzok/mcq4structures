package pl.poznan.put.comparison;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.math3.util.FastMath;
import pl.poznan.put.circular.Angle;
import pl.poznan.put.circular.ImmutableAngle;
import pl.poznan.put.circular.samples.AngleSample;
import pl.poznan.put.circular.samples.ImmutableAngleSample;
import pl.poznan.put.comparison.exception.IncomparableStructuresException;
import pl.poznan.put.comparison.global.GlobalComparator;
import pl.poznan.put.comparison.global.GlobalResult;
import pl.poznan.put.comparison.global.LCSGlobalResult;
import pl.poznan.put.matching.FragmentMatch;
import pl.poznan.put.matching.ImmutableMCQMatcher;
import pl.poznan.put.matching.MatchCollection;
import pl.poznan.put.matching.ResidueComparison;
import pl.poznan.put.matching.SelectionMatch;
import pl.poznan.put.matching.StructureMatcher;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbResidue;
import pl.poznan.put.torsion.MasterTorsionAngleType;
import pl.poznan.put.torsion.TorsionAngleDelta;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of LCS global similarity measure based on torsion angle representation.
 *
 * @author Tomasz Zok (tzok[at]cs.put.poznan.pl)
 * @author Jakub Wiedemann (jakub.wiedemann[at]cs.put.poznan.pl)
 */
public class LCS implements GlobalComparator {
  private final MoleculeType moleculeType;
  private final Angle threshold;

  public LCS(final MoleculeType moleculeType, final Angle threshold) {
    super();
    this.moleculeType = moleculeType;
    this.threshold = threshold;
  }

  @Override
  public final String getName() {
    return "LCS";
  }

  @Override
  public final boolean isAngularMeasure() {
    return true;
  }

  @Override
  public final GlobalResult compareGlobally(
      final StructureSelection s1, final StructureSelection s2) {
    final StructureMatcher matcher = ImmutableMCQMatcher.of(moleculeType);
    final SelectionMatch matches = matcher.matchSelections(s1, s2);
    final List<Angle> deltas = getValidDeltas(matches);
    final AngleSample angleSample = ImmutableAngleSample.of(deltas);

    // if global mcq is < threshold, then 100% residues are in result
    final Angle globalMcq = angleSample.meanDirection();
    if (globalMcq.compareTo(threshold) < 0) {
      return new LCSGlobalResult(getName(), matches, ImmutableAngleSample.of(deltas), s2, s1);
    }

    Angle currentThreshold = threshold;
    Optional<RefinementResult> refinement = findLongestContinuousSegment(s1, s2, currentThreshold);
    while (!refinement.isPresent()) {
      currentThreshold = ImmutableAngle.of(currentThreshold.radians() + FastMath.toRadians(1.0));
      refinement = findLongestContinuousSegment(s1, s2, currentThreshold);
    }

    return new LCSGlobalResult(
        getName(),
        refinement.get().getSelectionMatch(),
        refinement.get().getAngleSample(),
        refinement.get().getModel(),
        refinement.get().getTarget());
  }

  private Optional<RefinementResult> findLongestContinuousSegment(
      final StructureSelection s1, final StructureSelection s2, final Angle currentThreshold) {
    RefinementResult bestRefinement = null;
    int longest = 0;
    int l = 0;
    int p = s1.getResidues().size() - 1;

    while (l <= p && p > 0) {
      final int s = Math.max((l + p) / 2, 1);
      boolean found = false;

      for (int j = 0; (j + s) <= s1.getResidues().size(); j++) {
        final List<PdbResidue> fragmentResidues = s1.getResidues().subList(j, j + s);
        final StructureSelection target1 =
            StructureSelection.divideIntoCompactFragments(s1.getName(), fragmentResidues);
        final Optional<RefinementResult> optionalRefinement = refinement(s2, target1);

        if (optionalRefinement.isPresent()) {
          final RefinementResult localRefinement = optionalRefinement.get();
          final Angle mcq = localRefinement.getAngleSample().meanDirection();
          final int size = localRefinement.getSelectionMatch().getResidueLabels().size();

          // if mcq < threshold and size >= longest
          if ((mcq.compareTo(currentThreshold) <= 0) && (size > longest)) {
            longest = size;
            bestRefinement = localRefinement;
            found = true;
          }
        }
      }

      if (found) {
        l = s + 1;
      } else {
        p = s - 1;
      }
    }

    return Optional.ofNullable(bestRefinement);
  }

  private Optional<RefinementResult> refinement(
      final StructureSelection target, final StructureSelection model) {
    final StructureMatcher matcher = ImmutableMCQMatcher.of(moleculeType);
    final SelectionMatch matches = matcher.matchSelections(target, model);
    return matches.getFragmentMatches().isEmpty()
        ? Optional.empty()
        : Optional.of(
            new RefinementResult(
                matches, ImmutableAngleSample.of(getValidDeltas(matches)), model, target));
  }

  private List<Angle> getValidDeltas(final MatchCollection matches) {
    if (matches.getFragmentMatches().isEmpty()) {
      throw new IncomparableStructuresException("No matching fragments found");
    }

    final List<Angle> deltas = new ArrayList<>();

    for (final FragmentMatch fragmentMatch : matches.getFragmentMatches()) {
      for (final ResidueComparison residueComparison : fragmentMatch.getResidueComparisons()) {
        for (final MasterTorsionAngleType angleType : moleculeType.allAngleTypes()) {
          final TorsionAngleDelta angleDelta = residueComparison.angleDelta(angleType);

          if (angleDelta.getState() == TorsionAngleDelta.State.BOTH_VALID) {
            deltas.add(angleDelta.getDelta());
          }
        }
      }
    }
    return deltas;
  }

  @Getter
  @RequiredArgsConstructor
  private static class RefinementResult {
    private final SelectionMatch selectionMatch;
    private final AngleSample angleSample;
    private final StructureSelection model;
    private final StructureSelection target;
  }
}
