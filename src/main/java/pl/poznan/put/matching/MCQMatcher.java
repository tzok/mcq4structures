package pl.poznan.put.matching;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.map.DefaultedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.poznan.put.circular.Angle;
import pl.poznan.put.circular.samples.AngleSample;
import pl.poznan.put.pdb.ChainNumberICode;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.pdb.analysis.PdbResidue;
import pl.poznan.put.torsion.AverageTorsionAngleType;
import pl.poznan.put.torsion.MasterTorsionAngleType;
import pl.poznan.put.torsion.TorsionAngleDelta;
import pl.poznan.put.torsion.TorsionAngleValue;
import pl.poznan.put.torsion.range.RangeDifference;

public class MCQMatcher implements StructureMatcher {
  private static final Logger LOGGER = LoggerFactory.getLogger(MCQMatcher.class);

  private final List<MasterTorsionAngleType> angleTypes;

  public MCQMatcher(final List<MasterTorsionAngleType> angleTypes) {
    super();
    this.angleTypes = new ArrayList<>(angleTypes);
  }

  @Override
  public final SelectionMatch matchSelections(
      final StructureSelection s1, final StructureSelection s2) {
    if ((s1.getResidues().isEmpty()) || (s2.getResidues().isEmpty())) {
      return new SelectionMatch(s1, s2, Collections.emptyList());
    }

    final FragmentMatch[][] matrix = fillMatchingMatrix(s1, s2);
    MCQMatcher.filterMatchingMatrix(matrix);
    final List<FragmentMatch> fragmentMatches = MCQMatcher.assignFragments(matrix);
    return new SelectionMatch(s1, s2, fragmentMatches);
  }

  private FragmentMatch[][] fillMatchingMatrix(
      final StructureSelection target, final StructureSelection model) {
    final List<PdbCompactFragment> targetFragments = target.getCompactFragments();
    final List<PdbCompactFragment> modelFragments = model.getCompactFragments();
    final FragmentMatch[][] matrix = new FragmentMatch[targetFragments.size()][];

    for (int i = 0; i < targetFragments.size(); i++) {
      matrix[i] = new FragmentMatch[modelFragments.size()];
    }

    for (int i = 0; i < targetFragments.size(); i++) {
      final PdbCompactFragment fi = targetFragments.get(i);
      for (int j = 0; j < modelFragments.size(); j++) {
        final PdbCompactFragment fj = modelFragments.get(j);
        matrix[i][j] =
            (fi.getMoleculeType() == fj.getMoleculeType())
                ? matchFragments(fi, fj)
                : FragmentMatch.invalidInstance(fi, fj);
      }
    }
    return matrix;
  }

  private static void filterMatchingMatrix(final FragmentMatch[][] matrix) {
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
          matrix[i][j] = FragmentMatch.invalidInstance(target, model);
        }
      }
    }
  }

  private static List<FragmentMatch> assignFragments(final FragmentMatch[][] matrix) {
    return MCQMatcher.assignHungarian(matrix);
  }

  private static List<FragmentMatch> assignHungarian(final FragmentMatch[][] matrix) {
    final double[][] costMatrix = new double[matrix.length][];

    for (int i = 0; i < matrix.length; i++) {
      costMatrix[i] = new double[matrix[i].length];
      for (int j = 0; j < matrix[i].length; j++) {
        final Angle delta = matrix[i][j].getMeanDelta();
        costMatrix[i][j] = delta.isValid() ? delta.getRadians() : Double.MAX_VALUE;
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

  private ResidueComparison compareResidues(
      final PdbCompactFragment targetFragment,
      final PdbResidue targetResidue,
      final PdbCompactFragment modelFragment,
      final PdbResidue modelResidue) {
    final List<TorsionAngleDelta> angleDeltas = new ArrayList<>();

    for (final MasterTorsionAngleType masterType : angleTypes) {
      final TorsionAngleDelta delta =
          (masterType instanceof AverageTorsionAngleType)
              ? MCQMatcher.calculateAverageOverDifferences(
                  targetFragment,
                  targetResidue,
                  modelFragment,
                  modelResidue,
                  (AverageTorsionAngleType) masterType)
              : MCQMatcher.findAndSubtractTorsionAngles(
                  targetFragment, targetResidue, modelFragment, modelResidue, masterType);

      angleDeltas.add(delta);

      if (MCQMatcher.LOGGER.isTraceEnabled()) {
        MCQMatcher.LOGGER.trace("{} vs {} = {}", targetResidue, modelResidue, delta);
      }
    }

    return new ResidueComparison(targetResidue, modelResidue, angleDeltas);
  }

  private static TorsionAngleDelta calculateAverageOverDifferences(
      final PdbCompactFragment targetFragment,
      final ChainNumberICode targetResidue,
      final PdbCompactFragment modelFragment,
      final ChainNumberICode modelResidue,
      final AverageTorsionAngleType angleType) {
    final List<Angle> targetAngles = new ArrayList<>();
    final List<Angle> modelAngles = new ArrayList<>();
    final List<Angle> deltas = new ArrayList<>();
    double value = 0.0;

    for (final MasterTorsionAngleType masterType : angleType.getConsideredAngles()) {
      final TorsionAngleDelta delta =
          MCQMatcher.findAndSubtractTorsionAngles(
              targetFragment, targetResidue, modelFragment, modelResidue, masterType);
      if (delta.getState() == TorsionAngleDelta.State.BOTH_VALID) {
        targetAngles.add(delta.getTarget());
        modelAngles.add(delta.getModel());
        deltas.add(delta.getDelta());
        value += delta.getRangeDifference().getValue();
      }
    }

    if (deltas.isEmpty()) {
      return TorsionAngleDelta.bothInvalidInstance(angleType);
    }

    final AngleSample targetSample = new AngleSample(targetAngles);
    final AngleSample modelSample = new AngleSample(modelAngles);
    final AngleSample deltaSample = new AngleSample(deltas);
    return new TorsionAngleDelta(
        angleType,
        TorsionAngleDelta.State.BOTH_VALID,
        targetSample.getMeanDirection(),
        modelSample.getMeanDirection(),
        deltaSample.getMeanDirection(),
        RangeDifference.fromValue((int) Math.round(value / deltas.size())));
  }

  private static TorsionAngleDelta findAndSubtractTorsionAngles(
      final PdbCompactFragment targetFragment,
      final ChainNumberICode targetResidue,
      final PdbCompactFragment modelFragment,
      final ChainNumberICode modelResidue,
      final MasterTorsionAngleType masterType) {

    final TorsionAngleValue targetValue =
        targetFragment.getTorsionAngleValue(targetResidue, masterType);
    final TorsionAngleValue modelValue =
        modelFragment.getTorsionAngleValue(modelResidue, masterType);
    return TorsionAngleDelta.subtractTorsionAngleValues(masterType, targetValue, modelValue);
  }

  @Override
  public final FragmentMatch matchFragments(
      final PdbCompactFragment f1, final PdbCompactFragment f2) {
    final List<PdbResidue> targetResidues = f1.getResidues();
    final List<PdbResidue> modelResidues = f2.getResidues();
    final int f1Size = f1.getResidues().size();
    final int f2Size = f2.getResidues().size();
    final boolean isTargetSmaller = f1Size < f2Size;
    final int sizeDifference = isTargetSmaller ? (f2Size - f1Size) : (f1Size - f2Size);

    FragmentComparison bestResult = null;
    int bestShift = 0;

    for (int i = 0; i <= sizeDifference; i++) {
      final List<ResidueComparison> residueComparisons = new ArrayList<>();

      if (isTargetSmaller) {
        for (int j = 0; j < f1Size; j++) {
          final PdbResidue targetResidue = targetResidues.get(j);
          final PdbResidue modelResidue = modelResidues.get(j + i);
          residueComparisons.add(compareResidues(f1, targetResidue, f2, modelResidue));
        }
      } else {
        for (int j = 0; j < f2Size; j++) {
          final PdbResidue targetResidue = targetResidues.get(j + i);
          final PdbResidue modelResidue = modelResidues.get(j);
          residueComparisons.add(compareResidues(f1, targetResidue, f2, modelResidue));
        }
      }

      final FragmentComparison fragmentResult =
          FragmentComparison.fromResidueComparisons(residueComparisons, angleTypes);

      if ((bestResult == null) || (fragmentResult.compareTo(bestResult) < 0)) {
        bestResult = fragmentResult;
        bestShift = i;
      }
    }

    return new FragmentMatch(f1, f2, isTargetSmaller, bestShift, bestResult);
  }
}
