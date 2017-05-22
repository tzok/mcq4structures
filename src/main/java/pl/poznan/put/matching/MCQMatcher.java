package pl.poznan.put.matching;

import org.apache.commons.collections4.map.DefaultedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.poznan.put.circular.Angle;
import pl.poznan.put.circular.enums.RangeDifference;
import pl.poznan.put.circular.samples.AngleSample;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.pdb.analysis.PdbResidue;
import pl.poznan.put.torsion.AverageTorsionAngleType;
import pl.poznan.put.torsion.MasterTorsionAngleType;
import pl.poznan.put.torsion.TorsionAngleDelta;
import pl.poznan.put.torsion.TorsionAngleValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MCQMatcher implements StructureMatcher {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(MCQMatcher.class);

    private final List<MasterTorsionAngleType> angleTypes;

    public MCQMatcher(final List<MasterTorsionAngleType> angleTypes) {
        super();
        this.angleTypes = new ArrayList<>(angleTypes);
    }

    @Override
    public final SelectionMatch matchSelections(
            final StructureSelection s1, final StructureSelection s2) {
        if ((s1.size() == 0) || (s2.size() == 0)) {
            return new SelectionMatch(s1, s2, Collections.emptyList());
        }

        FragmentMatch[][] matrix = fillMatchingMatrix(s1, s2);
        MCQMatcher.filterMatchingMatrix(matrix);
        List<FragmentMatch> fragmentMatches =
                MCQMatcher.assignFragments(matrix);
        return new SelectionMatch(s1, s2, fragmentMatches);
    }

    private FragmentMatch[][] fillMatchingMatrix(
            final StructureSelection target, final StructureSelection model) {
        List<PdbCompactFragment> targetFragments = target.getCompactFragments();
        List<PdbCompactFragment> modelFragments = model.getCompactFragments();
        FragmentMatch[][] matrix = new FragmentMatch[targetFragments.size()][];

        for (int i = 0; i < targetFragments.size(); i++) {
            matrix[i] = new FragmentMatch[modelFragments.size()];
        }

        for (int i = 0; i < targetFragments.size(); i++) {
            PdbCompactFragment fi = targetFragments.get(i);
            for (int j = 0; j < modelFragments.size(); j++) {
                PdbCompactFragment fj = modelFragments.get(j);
                matrix[i][j] = (fi.getMoleculeType() == fj.getMoleculeType())
                               ? matchFragments(fi, fj)
                               : FragmentMatch.invalidInstance(fi, fj);
            }
        }
        return matrix;
    }

    private static void filterMatchingMatrix(final FragmentMatch[][] matrix) {
        Map<PdbCompactFragment, Integer> fragmentMaxCount =
                new DefaultedMap<>(Integer.MIN_VALUE);

        for (final FragmentMatch[] matches : matrix) {
            for (final FragmentMatch match : matches) {
                PdbCompactFragment target = match.getTargetFragment();
                PdbCompactFragment model = match.getModelFragment();
                int count = match.getResidueCount();
                fragmentMaxCount.put(target, Math.max(count, fragmentMaxCount
                        .get(target)));
                fragmentMaxCount.put(model, Math.max(count, fragmentMaxCount
                        .get(model)));
            }
        }

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                PdbCompactFragment target = matrix[i][j].getTargetFragment();
                PdbCompactFragment model = matrix[i][j].getModelFragment();
                int count = matrix[i][j].getResidueCount();
                int maxCount = Math.max(fragmentMaxCount.get(target),
                                        fragmentMaxCount.get(model));

                if (count < (maxCount * 0.9)) {
                    matrix[i][j] = FragmentMatch.invalidInstance(target, model);
                }
            }
        }
    }

    private static List<FragmentMatch> assignFragments(
            final FragmentMatch[][] matrix) {
        return MCQMatcher.assignHungarian(matrix);
    }

    private static List<FragmentMatch> assignHungarian(
            final FragmentMatch[][] matrix) {
        double[][] costMatrix = new double[matrix.length][];

        for (int i = 0; i < matrix.length; i++) {
            costMatrix[i] = new double[matrix[i].length];
            for (int j = 0; j < matrix[i].length; j++) {
                Angle delta = matrix[i][j].getMeanDelta();
                costMatrix[i][j] =
                        delta.isValid() ? delta.getRadians() : Double.MAX_VALUE;
            }
        }

        HungarianAlgorithm algorithm = new HungarianAlgorithm(costMatrix);
        int[] assignment = algorithm.execute();
        List<FragmentMatch> result = new ArrayList<>();

        for (int i = 0; i < assignment.length; i++) {
            int j = assignment[i];
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
        List<TorsionAngleDelta> angleDeltas = new ArrayList<>();

        for (final MasterTorsionAngleType masterType : angleTypes) {
            TorsionAngleDelta delta;

            if (masterType instanceof AverageTorsionAngleType) {
                delta = MCQMatcher
                        .calculateAverageOverDifferences(targetFragment,
                                                         targetResidue,
                                                         modelFragment,
                                                         modelResidue,
                                                         (AverageTorsionAngleType) masterType);
            } else {
                delta = MCQMatcher.findAndSubtractTorsionAngles(targetFragment,
                                                                targetResidue,
                                                                modelFragment,
                                                                modelResidue,
                                                                masterType);
            }

            angleDeltas.add(delta);

            if (MCQMatcher.LOGGER.isTraceEnabled()) {
                MCQMatcher.LOGGER
                        .trace("{} vs {} = {}", targetResidue, modelResidue,
                               delta);
            }
        }

        return new ResidueComparison(targetResidue, modelResidue, angleDeltas);
    }

    private static TorsionAngleDelta calculateAverageOverDifferences(
            final PdbCompactFragment targetFragment,
            final PdbResidue targetResidue,
            final PdbCompactFragment modelFragment,
            final PdbResidue modelResidue,
            final AverageTorsionAngleType angleType) {
        List<Angle> angles = new ArrayList<>();
        double value = 0.0;

        for (final MasterTorsionAngleType masterType : angleType
                .getConsideredAngles()) {
            TorsionAngleDelta delta = MCQMatcher
                    .findAndSubtractTorsionAngles(targetFragment, targetResidue,
                                                  modelFragment, modelResidue,
                                                  masterType);
            if (delta.getState() == TorsionAngleDelta.State.BOTH_VALID) {
                angles.add(delta.getDelta());
                value += delta.getRangeDifference().getValue();
            }
        }

        if (angles.isEmpty()) {
            return TorsionAngleDelta.bothInvalidInstance(angleType);
        }

        AngleSample angleSample = new AngleSample(angles);
        return new TorsionAngleDelta(angleType,
                                     TorsionAngleDelta.State.BOTH_VALID,
                                     angleSample.getMeanDirection(),
                                     RangeDifference.fromValue((int) Math
                                             .round(value / angles.size())));
    }

    private static TorsionAngleDelta findAndSubtractTorsionAngles(
            final PdbCompactFragment targetFragment,
            final PdbResidue targetResidue,
            final PdbCompactFragment modelFragment,
            final PdbResidue modelResidue,
            final MasterTorsionAngleType masterType) {

        TorsionAngleValue targetValue =
                targetFragment.getTorsionAngleValue(targetResidue, masterType);
        TorsionAngleValue modelValue =
                modelFragment.getTorsionAngleValue(modelResidue, masterType);
        return TorsionAngleDelta
                .subtractTorsionAngleValues(masterType, targetValue,
                                            modelValue);
    }

    @Override
    public final FragmentMatch matchFragments(
            final PdbCompactFragment f1, final PdbCompactFragment f2) {
        List<PdbResidue> targetResidues = f1.getResidues();
        List<PdbResidue> modelResidues = f2.getResidues();
        boolean isTargetSmaller = f1.size() < f2.size();
        int sizeDifference = isTargetSmaller ? (f2.size() - f1.size())
                                             : (f1.size() - f2.size());

        FragmentComparison bestResult = null;
        int bestShift = 0;

        for (int i = 0; i <= sizeDifference; i++) {
            List<ResidueComparison> residueComparisons = new ArrayList<>();

            if (isTargetSmaller) {
                for (int j = 0; j < f1.size(); j++) {
                    PdbResidue targetResidue = targetResidues.get(j);
                    PdbResidue modelResidue = modelResidues.get(j + i);
                    residueComparisons
                            .add(compareResidues(f1, targetResidue, f2,
                                                 modelResidue));
                }
            } else {
                for (int j = 0; j < f2.size(); j++) {
                    PdbResidue targetResidue = targetResidues.get(j + i);
                    PdbResidue modelResidue = modelResidues.get(j);
                    residueComparisons
                            .add(compareResidues(f1, targetResidue, f2,
                                                 modelResidue));
                }
            }

            FragmentComparison fragmentResult = FragmentComparison
                    .fromResidueComparisons(residueComparisons, angleTypes);

            if ((bestResult == null) || (fragmentResult.compareTo(bestResult)
                                         < 0)) {
                bestResult = fragmentResult;
                bestShift = i;
            }
        }

        return new FragmentMatch(f1, f2, isTargetSmaller, bestShift,
                                 bestResult);
    }
}
