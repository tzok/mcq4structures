package pl.poznan.put.matching;

import org.apache.commons.collections4.map.DefaultedMap;
import org.apache.commons.lang3.tuple.Pair;
import org.immutables.value.Value;
import pl.poznan.put.circular.Angle;
import pl.poznan.put.pdb.PdbResidueIdentifier;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.pdb.analysis.PdbResidue;
import pl.poznan.put.torsion.MasterTorsionAngleType;
import pl.poznan.put.torsion.TorsionAngleDelta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Value.Immutable
public abstract class MCQMatcher implements StructureMatcher {
  private static List<FragmentMatch> solveAssignmentProblem(final FragmentMatch[][] matrix) {
    final double[][] costMatrix = new double[matrix.length][];

    for (int i = 0; i < matrix.length; i++) {
      costMatrix[i] = new double[matrix[i].length];
      for (int j = 0; j < matrix[i].length; j++) {
        final Angle delta = matrix[i][j].getMeanDelta();
        costMatrix[i][j] = delta.isValid() ? delta.radians() : Double.MAX_VALUE;
      }
    }

    final HungarianAlgorithm algorithm = new HungarianAlgorithm(costMatrix);
    final int[] assignment = algorithm.execute();
    final List<FragmentMatch> result = new ArrayList<>();

    for (int i = 0; i < assignment.length; i++) {
      final int j = assignment[i];
      if ((j != -1) && matrix[i][j].isValid()) {
        result.add(matrix[i][j]);
      }
    }

    return result;
  }

  @Value.Parameter(order = 1)
  public abstract MoleculeType moleculeType();

  @Override
  public final SelectionMatch matchSelections(
      final StructureSelection s1, final StructureSelection s2) {
    if ((s1.getResidues().isEmpty()) || (s2.getResidues().isEmpty())) {
      return new SelectionMatch(s1, s2, Collections.emptyList());
    }

    final FragmentMatch[][] matrix = fillMatchingMatrix(s1, s2);
    final List<FragmentMatch> fragmentMatches = MCQMatcher.solveAssignmentProblem(matrix);
    return new SelectionMatch(s1, s2, fragmentMatches);
  }

  @Override
  public final FragmentMatch matchFragments(
      final PdbCompactFragment f1, final PdbCompactFragment f2) {
    final List<PdbResidue> targetResidues = f1.residues();
    final List<PdbResidue> modelResidues = f2.residues();
    final int f1Size = f1.residues().size();
    final int f2Size = f2.residues().size();
    final boolean isTargetSmaller = f1Size < f2Size;
    final int sizeDifference = isTargetSmaller ? (f2Size - f1Size) : (f1Size - f2Size);

    FragmentComparison bestResult = null;
    int bestShift = 0;

    // sliding window
    for (int i = 0; i <= sizeDifference; i++) {
      final int fi = i;
      final List<ResidueComparison> residueComparisons =
          isTargetSmaller
              ? IntStream.range(0, f1Size)
                  .mapToObj(j -> Pair.of(targetResidues.get(j), modelResidues.get(j + fi)))
                  .map(pair -> compareResidues(f1, pair.getLeft(), f2, pair.getRight()))
                  .collect(Collectors.toList())
              : IntStream.range(0, f2Size)
                  .mapToObj(j -> Pair.of(targetResidues.get(j + fi), modelResidues.get(j)))
                  .map(pair -> compareResidues(f1, pair.getLeft(), f2, pair.getRight()))
                  .collect(Collectors.toList());

      final FragmentComparison fragmentResult =
          FragmentComparison.fromResidueComparisons(residueComparisons, angleTypes());

      if ((bestResult == null) || (fragmentResult.compareTo(bestResult) < 0)) {
        bestResult = fragmentResult;
        bestShift = i;
      }
    }

    return new FragmentMatch(f1, f2, isTargetSmaller, bestShift, bestResult);
  }

  @Value.Lazy
  protected List<MasterTorsionAngleType> angleTypes() {
    return moleculeType().allAngleTypes();
  }

  private FragmentMatch[][] fillMatchingMatrix(
      final StructureSelection target, final StructureSelection model) {
    final List<PdbCompactFragment> targetFragments = target.getCompactFragments();
    final List<PdbCompactFragment> modelFragments = model.getCompactFragments();

    final FragmentMatch[][] matrix =
        IntStream.range(0, targetFragments.size())
            .mapToObj(i -> new FragmentMatch[modelFragments.size()])
            .toArray(FragmentMatch[][]::new);

    for (int i = 0; i < targetFragments.size(); i++) {
      final PdbCompactFragment fi = targetFragments.get(i);

      for (int j = 0; j < modelFragments.size(); j++) {
        final PdbCompactFragment fj = modelFragments.get(j);
        matrix[i][j] =
            (fi.moleculeType() == fj.moleculeType())
                ? matchFragments(fi, fj)
                : FragmentMatch.invalidInstance(fi, fj, angleTypes());
      }
    }

    removeNoise(matrix);

    return matrix;
  }

  /**
   * Clears out "noise" entries from the matrix. The "noise" here is a value of comparison based on
   * a small number of residues, if another comparison based on a larger number of residues is
   * available.
   *
   * @param matrix The all-vs-all matrix of comparisions.
   */
  private void removeNoise(final FragmentMatch[][] matrix) {
    final Map<PdbCompactFragment, Integer> fragmentMaxCount = new DefaultedMap<>(Integer.MIN_VALUE);

    for (final FragmentMatch[] matches : matrix) {
      for (final FragmentMatch match : matches) {
        final PdbCompactFragment target = match.getTargetFragment();
        final PdbCompactFragment model = match.getModelFragment();
        final int count = match.getResidueCount();
        fragmentMaxCount.put(target, Math.max(count, fragmentMaxCount.get(target)));
        fragmentMaxCount.put(model, Math.max(count, fragmentMaxCount.get(model)));
      }
    }

    for (int i = 0; i < matrix.length; i++) {
      for (int j = 0; j < matrix[i].length; j++) {
        final PdbCompactFragment target = matrix[i][j].getTargetFragment();
        final PdbCompactFragment model = matrix[i][j].getModelFragment();
        final int count = matrix[i][j].getResidueCount();
        final int maxCount = Math.max(fragmentMaxCount.get(target), fragmentMaxCount.get(model));

        if (count < (maxCount * 0.9)) {
          matrix[i][j] = FragmentMatch.invalidInstance(target, model, angleTypes());
        }
      }
    }
  }

  private ResidueComparison compareResidues(
      final PdbCompactFragment targetFragment,
      final PdbResidue targetResidue,
      final PdbCompactFragment modelFragment,
      final PdbResidue modelResidue) {
    final List<TorsionAngleDelta> angleDeltas =
        angleTypes().stream()
            .map(
                masterType ->
                    TorsionAngleDelta.subtractTorsionAngleValues(
                        masterType,
                        targetFragment
                            .torsionAngles(PdbResidueIdentifier.from(targetResidue))
                            .value(masterType),
                        modelFragment
                            .torsionAngles(PdbResidueIdentifier.from(modelResidue))
                            .value(masterType)))
            .collect(Collectors.toList());

    final boolean anyValid =
        angleDeltas.stream()
            .map(TorsionAngleDelta::state)
            .anyMatch(state -> state == TorsionAngleDelta.State.BOTH_VALID);

    return anyValid
        ? ImmutableResidueComparison.of(targetResidue, modelResidue, angleDeltas)
        : ResidueComparison.invalidInstance(targetResidue, modelResidue);
  }
}
