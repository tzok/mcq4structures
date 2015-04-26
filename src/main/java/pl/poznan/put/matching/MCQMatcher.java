package pl.poznan.put.matching;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pl.poznan.put.circular.exception.InvalidCircularValueException;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.pdb.analysis.PdbResidue;
import pl.poznan.put.torsion.TorsionAngleDelta;
import pl.poznan.put.torsion.TorsionAngleValue;
import pl.poznan.put.torsion.type.TorsionAngleType;

public class MCQMatcher implements StructureMatcher {
    private List<TorsionAngleType> angleTypes;

    public MCQMatcher(List<TorsionAngleType> angleTypes) {
        super();
        this.angleTypes = angleTypes;
    }

    @Override
    public SelectionMatch matchSelections(StructureSelection target,
            StructureSelection model) throws InvalidCircularValueException {
        if (target.size() == 0 || model.size() == 0) {
            return new SelectionMatch(target, model, Collections.<FragmentMatch> emptyList());
        }

        FragmentMatch[][] matrix = fillMatchingMatrix(target, model);
        List<FragmentMatch> fragmentMatches = MCQMatcher.assignFragments(matrix);
        return new SelectionMatch(target, model, fragmentMatches);
    }

    private FragmentMatch[][] fillMatchingMatrix(StructureSelection target,
            StructureSelection model) throws InvalidCircularValueException {
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
                if (fi.moleculeType() == fj.moleculeType()) {
                    matrix[i][j] = matchFragments(fi, fj);
                }
            }
        }
        return matrix;
    }

    @Override
    public FragmentMatch matchFragments(PdbCompactFragment targetFragment,
            PdbCompactFragment modelFragment) throws InvalidCircularValueException {
        List<PdbResidue> targetResidues = targetFragment.getResidues();
        List<PdbResidue> modelResidues = modelFragment.getResidues();
        boolean isTargetSmaller = targetFragment.size() < modelFragment.size();
        int sizeDifference = isTargetSmaller ? modelFragment.size() - targetFragment.size() : targetFragment.size() - modelFragment.size();

        FragmentComparison bestResult = null;
        int bestShift = 0;

        for (int i = 0; i <= sizeDifference; i++) {
            List<ResidueComparison> residueComparisons = new ArrayList<>();

            if (isTargetSmaller) {
                for (int j = 0; j < targetFragment.size(); j++) {
                    PdbResidue targetResidue = targetResidues.get(j);
                    PdbResidue modelResidue = modelResidues.get(j + i);
                    residueComparisons.add(compareResidues(targetFragment, targetResidue, modelFragment, modelResidue));
                }
            } else {
                for (int j = 0; j < modelFragment.size(); j++) {
                    PdbResidue targetResidue = targetResidues.get(j + i);
                    PdbResidue modelResidue = modelResidues.get(j);
                    residueComparisons.add(compareResidues(targetFragment, targetResidue, modelFragment, modelResidue));
                }
            }

            FragmentComparison fragmentResult = FragmentComparison.fromResidueComparisons(residueComparisons, angleTypes);

            if (bestResult == null || fragmentResult.compareTo(bestResult) < 0) {
                bestResult = fragmentResult;
                bestShift = i;
            }
        }

        return new FragmentMatch(targetFragment, modelFragment, isTargetSmaller, bestShift, bestResult);
    }

    private ResidueComparison compareResidues(
            PdbCompactFragment targetFragment, PdbResidue targetResidue,
            PdbCompactFragment modelFragment, PdbResidue modelResidue) throws InvalidCircularValueException {
        List<TorsionAngleDelta> angleDeltas = new ArrayList<>();

        for (TorsionAngleType angleType : angleTypes) {
            TorsionAngleValue targetValue = targetFragment.getTorsionAngleValue(targetResidue, angleType);
            TorsionAngleValue modelValue = modelFragment.getTorsionAngleValue(modelResidue, angleType);
            angleDeltas.add(targetValue.subtract(modelValue));
        }

        return new ResidueComparison(targetResidue, modelResidue, angleDeltas);
    }

    private static List<FragmentMatch> assignFragments(FragmentMatch[][] matrix) {
        return MCQMatcher.assignHungarian(matrix);
    }

    private static List<FragmentMatch> assignHungarian(FragmentMatch[][] matrix) {
        double[][] costMatrix = new double[matrix.length][];

        for (int i = 0; i < matrix.length; i++) {
            costMatrix[i] = new double[matrix[i].length];
            for (int j = 0; j < matrix[i].length; j++) {
                FragmentComparison fragmentComparison = matrix[i][j].getFragmentComparison();
                costMatrix[i][j] = fragmentComparison.getMCQ().getRadians();
            }
        }

        HungarianAlgorithm algorithm = new HungarianAlgorithm(costMatrix);
        int[] assignment = algorithm.execute();
        List<FragmentMatch> result = new ArrayList<>();

        for (int i = 0; i < assignment.length; i++) {
            int j = assignment[i];
            if (j != -1) {
                result.add(matrix[i][j]);
            }
        }

        return result;
    }

    @SuppressWarnings("unused")
    private static List<FragmentMatch> assignGreedily(FragmentMatch[][] matrix) {
        List<FragmentMatch> result = new ArrayList<>();
        boolean[] usedi = new boolean[matrix.length];
        boolean[] usedj = new boolean[matrix[0].length];

        while (true) {
            FragmentComparison minimum = null;
            int mini = -1;
            int minj = -1;

            for (int i = 0; i < matrix.length; i++) {
                if (usedi[i]) {
                    continue;
                }

                for (int j = 0; j < matrix[i].length; j++) {
                    if (usedj[j]) {
                        continue;
                    }

                    FragmentMatch match = matrix[i][j];

                    if (match == null) {
                        continue;
                    }

                    FragmentComparison matchResult = match.getFragmentComparison();

                    if (minimum == null || matchResult.getMCQ().getRadians() < minimum.getMCQ().getRadians()) {
                        minimum = matchResult;
                        mini = i;
                        minj = j;
                    }
                }
            }

            if (mini == -1 || minj == -1) {
                break;
            }

            usedi[mini] = true;
            usedj[minj] = true;
            result.add(matrix[mini][minj]);
        }

        return result;
    }
}
