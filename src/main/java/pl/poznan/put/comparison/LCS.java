package pl.poznan.put.comparison;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.poznan.put.circular.Angle;
import pl.poznan.put.circular.samples.AngleSample;
import pl.poznan.put.comparison.exception.IncomparableStructuresException;
import pl.poznan.put.comparison.global.GlobalComparator;
import pl.poznan.put.comparison.global.GlobalResult;
import pl.poznan.put.comparison.global.LCSGlobalResult;
import pl.poznan.put.matching.FragmentMatch;
import pl.poznan.put.matching.MCQMatcher;
import pl.poznan.put.matching.MatchCollection;
import pl.poznan.put.matching.ResidueComparison;
import pl.poznan.put.matching.SelectionMatch;
import pl.poznan.put.matching.StructureMatcher;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.pdb.analysis.PdbResidue;
import pl.poznan.put.protein.torsion.ProteinTorsionAngleType;
import pl.poznan.put.rna.torsion.RNATorsionAngleType;
import pl.poznan.put.torsion.MasterTorsionAngleType;
import pl.poznan.put.torsion.TorsionAngleDelta;

/**
 * Implementation of LCS global similarity measure based on torsion angle representation.
 *
 * @author Tomasz Zok (tzok[at]cs.put.poznan.pl)
 * @author Jakub Wiedemann (jakub.wiedemann[at]cs.put.poznan.pl)
 */
public class LCS implements GlobalComparator {
  private final List<MasterTorsionAngleType> angleTypes;
  private final Angle threshold;

  public LCS(final Angle threshold) {
    super();
    this.threshold = threshold;
    angleTypes = new ArrayList<>();
    angleTypes.addAll(Arrays.asList(RNATorsionAngleType.mainAngles()));
    angleTypes.addAll(Arrays.asList(ProteinTorsionAngleType.mainAngles()));
  }

  @Override
  public final GlobalResult compareGlobally(
      final StructureSelection s1, final StructureSelection s2)
      throws IncomparableStructuresException {
    final StructureMatcher matcher = new MCQMatcher(angleTypes);
    final SelectionMatch matches = matcher.matchSelections(s1, s2);
    final List<Angle> deltas = getValidDeltas(matches);
    final AngleSample angleSample = new AngleSample(deltas);

    if (angleSample.getMeanDirection().compareTo(threshold) < 0) {
      return new LCSGlobalResult(getName(), matches, new AngleSample(deltas), s2, s1);
    }

    RefinementResult maxRefinementResult = new RefinementResult(matches, angleSample, s1, s2);
    int longest = 0;
    int l = 0;
    int p = s1.getResidues().size() - 1;

    while (l <= p) {
      final int s = (l + p) / 2;
      boolean found = false;

      for (int j = 0; (j + s) <= s1.getResidues().size(); j++) {
        final List<PdbResidue> fragmentResidues = s1.getResidues().subList(j, j + s);
        final StructureSelection target1 =
            StructureSelection.divideIntoCompactFragments(s1.getName(), fragmentResidues);
        final RefinementResult localRefinementResult = refinement(s2, target1);
        final Angle mcq = localRefinementResult.getAngleSample().getMeanDirection();
        final int size = localRefinementResult.getSelectionMatch().getResidueLabels().size();

        // better mcq and longer alignment
        if ((mcq.compareTo(threshold) < 0) && (size >= longest)) {
          longest = size;
          maxRefinementResult = localRefinementResult;
          found = true;
        }
      }

      if (found) {
        l = s + 1;
      } else {
        p = s - 1;
      }
    }

    return new LCSGlobalResult(
        getName(),
        maxRefinementResult.getSelectionMatch(),
        maxRefinementResult.getAngleSample(),
        maxRefinementResult.getModel(),
        maxRefinementResult.getTarget());
  }

  public final RefinementResult refinement(
      final StructureSelection target, final StructureSelection model)
      throws IncomparableStructuresException {
    final StructureMatcher matcher = new MCQMatcher(angleTypes);
    final SelectionMatch matches = matcher.matchSelections(target, model);
    final List<Angle> deltas = getValidDeltas(matches);
    return new RefinementResult(matches, new AngleSample(deltas), model, target);
  }

  private List<Angle> getValidDeltas(final MatchCollection matches)
      throws IncomparableStructuresException {
    if (matches.getFragmentMatches().isEmpty()) {
      throw new IncomparableStructuresException("No matching fragments found");
    }

    final List<Angle> deltas = new ArrayList<>();

    for (final FragmentMatch fragmentMatch : matches.getFragmentMatches()) {
      for (final ResidueComparison residueComparison : fragmentMatch.getResidueComparisons()) {
        for (final MasterTorsionAngleType angleType : angleTypes) {
          final TorsionAngleDelta angleDelta = residueComparison.getAngleDelta(angleType);

          if (angleDelta.getState() == TorsionAngleDelta.State.BOTH_VALID) {
            deltas.add(angleDelta.getDelta());
          }
        }
      }
    }
    return deltas;
  }

  @Override
  public final String getName() {
    return "LCS";
  }

  @Override
  public final boolean isAngularMeasure() {
    return true;
  }

  @Getter
  @RequiredArgsConstructor
  private static final class RefinementResult {
    private final SelectionMatch selectionMatch;
    private final AngleSample angleSample;
    private final StructureSelection model;
    private final StructureSelection target;
  }
}
