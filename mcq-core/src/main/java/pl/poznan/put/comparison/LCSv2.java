package pl.poznan.put.comparison;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.math3.util.FastMath;
import pl.poznan.put.circular.Angle;
import pl.poznan.put.circular.samples.AngleSample;
import pl.poznan.put.circular.samples.ImmutableAngleSample;
import pl.poznan.put.comparison.exception.IncomparableStructuresException;
import pl.poznan.put.comparison.global.GlobalComparator;
import pl.poznan.put.comparison.global.GlobalResult;
import pl.poznan.put.comparison.global.ImmutableLCSGlobalResult;
import pl.poznan.put.matching.*;
import pl.poznan.put.pdb.analysis.*;
import pl.poznan.put.torsion.TorsionAngleDelta;

/**
 * Implementation of LCS global similarity measure based on torsion angle representation.
 *
 * @author Tomasz Zok (tzok[at]cs.put.poznan.pl)
 * @author Jakub Wiedemann (jakub.wiedemann[at]cs.put.poznan.pl)
 */
public class LCSv2 implements GlobalComparator {
  private final MoleculeType moleculeType;
  private final Angle threshold;

  public LCSv2(final MoleculeType moleculeType, final Angle threshold) {
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
    assert s1.getCompactFragments().size() == 1;
    assert s2.getCompactFragments().size() == 1;

    final PdbCompactFragment c1 = s1.getCompactFragments().get(0);
    final PdbCompactFragment c2 = s2.getCompactFragments().get(0);
    final List<PdbResidue> r1 = c1.residues();
    final List<PdbResidue> r2 = c2.residues();
    assert r1.size() == r2.size();

    final var allDeltas = gatherAngleDeltas(r1, r2);

    if (allDeltas.meanDirection().compareTo(threshold) < 0) {
      return createResult(r1, r2, allDeltas, 0, r1.size());
    }

    int minLCS = 0;
    int maxLCS = r1.size() - 1;
    int bestBegin = -1;
    int bestLength = -1;
    AngleSample bestDeltas = null;

    while (minLCS <= maxLCS && maxLCS > 0) {
      int length = (int) FastMath.ceil((minLCS + maxLCS) / 2.0);
      boolean found = false;

      for (int begin = 0; begin + length <= r1.size(); begin++) {
        final var target = r1.subList(begin, begin + length);
        final var model = r2.subList(begin, begin + length);
        final var deltas = gatherAngleDeltas(target, model);

        if (deltas.meanDirection().compareTo(threshold) < 0) {
          found = true;

          if (length > bestLength) {
            bestBegin = begin;
            bestLength = length;
            bestDeltas = deltas;
          } else if (length == bestLength
              && bestDeltas != null
              && deltas.meanDirection().compareTo(bestDeltas.meanDirection()) < 0) {
            bestBegin = begin;
            bestDeltas = deltas;
          }
        }
      }

      if (found) {
        minLCS = length + 1;
      } else {
        maxLCS = length - 1;
      }
    }

    if (bestBegin == -1) {
      throw new IncomparableStructuresException("No LCS found");
    }

    return createResult(r1, r2, bestDeltas, bestBegin, bestLength);
  }

  private GlobalResult createResult(
      List<PdbResidue> target, List<PdbResidue> model, AngleSample deltas, int begin, int length) {
    final var targetFragment =
        ImmutablePdbCompactFragment.of(target.subList(begin, begin + length));
    final var modelFragment = ImmutablePdbCompactFragment.of(model.subList(begin, begin + length));
    final var selectionModel = new StructureSelection("", Collections.singletonList(modelFragment));
    final var selectionTarget =
        new StructureSelection("", Collections.singletonList(targetFragment));
    final List<ResidueComparison> residueComparisons =
        IntStream.range(begin, begin + length)
            .mapToObj(
                i ->
                    ImmutableResidueComparison.of(
                        target.get(i), model.get(i), Collections.emptyList()))
            .collect(Collectors.toList());
    final var fragmentComparison =
        FragmentComparison.fromResidueComparisons(
            residueComparisons, moleculeType.mainAngleTypes());
    final var fragmentMatch =
        new FragmentMatch(targetFragment, modelFragment, false, 0, fragmentComparison);
    final var selectionMatch =
        new SelectionMatch(
            selectionTarget, selectionModel, Collections.singletonList(fragmentMatch));
    return ImmutableLCSGlobalResult.of(selectionMatch, deltas, selectionModel, selectionTarget);
  }

  private AngleSample gatherAngleDeltas(List<PdbResidue> target, List<PdbResidue> model) {
    assert target.size() == model.size();

    final var targetFragment = ImmutablePdbCompactFragment.of(target);
    final var modelFragment = ImmutablePdbCompactFragment.of(model);
    final var deltas = new ArrayList<Angle>();

    for (int i = 0; i < target.size(); i++) {
      final var targetAngles = targetFragment.torsionAngles(target.get(i).identifier());
      final var modelAngles = modelFragment.torsionAngles(model.get(i).identifier());

      for (final var angleType : moleculeType.mainAngleTypes()) {
        final var subtracted =
            TorsionAngleDelta.subtractTorsionAngleValues(
                angleType, targetAngles.value(angleType), modelAngles.value(angleType));
        if (subtracted.state() == TorsionAngleDelta.State.BOTH_VALID) {
          deltas.add(subtracted.delta());
        }
      }
    }

    return ImmutableAngleSample.of(deltas);
  }
}
