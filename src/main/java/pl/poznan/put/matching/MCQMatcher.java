package pl.poznan.put.matching;

import java.util.ArrayList;
import java.util.List;

import pl.poznan.put.nucleic.RNATorsionAngle;
import pl.poznan.put.torsion.AngleValue;
import pl.poznan.put.torsion.PseudophasePuckerAngle;
import pl.poznan.put.torsion.TorsionAngle;
import pl.poznan.put.torsion.type.AverageTorsionAngleType;

public class MCQMatcher implements StructureMatcher {
    private List<TorsionAngle> angles;

    public MCQMatcher(List<TorsionAngle> angles) {
        super();
        this.angles = angles;
    }

    @Override
    public SelectionMatch matchSelections(StructureSelection target,
            StructureSelection model) {
        if (target.getSize() == 0 || model.getSize() == 0) {
            return new SelectionMatch(target, model,
                    new ArrayList<FragmentMatch>());
        }

        CompactFragment[] targetFragments = target.getCompactFragments();
        CompactFragment[] modelFragments = model.getCompactFragments();
        FragmentMatch[][] matrix = new FragmentMatch[targetFragments.length][];

        for (int i = 0; i < targetFragments.length; i++) {
            matrix[i] = new FragmentMatch[modelFragments.length];
        }

        for (int i = 0; i < targetFragments.length; i++) {
            CompactFragment fi = targetFragments[i];

            for (int j = 0; j < modelFragments.length; j++) {
                CompactFragment fj = modelFragments[j];

                if (fi.getMoleculeType() != fj.getMoleculeType()) {
                    continue;
                }

                matrix[i][j] = matchFragments(fi, fj);
            }
        }

        List<FragmentMatch> fragmentMatches = MCQMatcher.assignFragments(matrix);
        return new SelectionMatch(target, model, fragmentMatches);
    }

    @Override
    public FragmentMatch matchFragments(CompactFragment target,
            CompactFragment model) {
        CompactFragment smaller = target;
        CompactFragment bigger = model;
        boolean isTargetSmaller = true;

        if (target.getSize() > model.getSize()) {
            smaller = model;
            bigger = target;
            isTargetSmaller = false;
        }

        FragmentAngles biggerAngles = bigger.getFragmentAngles();
        FragmentAngles smallerAngles = smaller.getFragmentAngles();
        int sizeDifference = bigger.getSize() - smaller.getSize();
        FragmentComparison bestResult = null;
        int bestShift = 0;

        for (int i = 0; i <= sizeDifference; i++) {
            List<ResidueComparison> residueResults = new ArrayList<>();

            for (int j = 0; j < smaller.getSize(); j++) {
                ResidueAngles a1 = smallerAngles.get(j);
                ResidueAngles a2 = biggerAngles.get(j + i);
                residueResults.add(isTargetSmaller ? compareResidues(a1, a2)
                        : compareResidues(a2, a1));
            }

            FragmentComparison fragmentResult = FragmentComparison.fromResidueComparisons(
                    residueResults, angles);

            if (bestResult == null || fragmentResult.compareTo(bestResult) < 0) {
                bestResult = fragmentResult;
                bestShift = i;
            }
        }

        return new FragmentMatch(target, model, isTargetSmaller, bestShift,
                bestResult);
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
                costMatrix[i][j] = fragmentComparison.getMcq();
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

                    if (minimum == null
                            || matchResult.getMcq() < minimum.getMcq()) {
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

    private ResidueComparison compareResidues(ResidueAngles target,
            ResidueAngles model) {
        List<AngleDelta> result = new ArrayList<>();
        List<AverageTorsionAngleType> averages = new ArrayList<>();
        boolean isPseudophasePucker = false;

        for (TorsionAngle angle : angles) {
            if (angle instanceof PseudophasePuckerAngle) {
                isPseudophasePucker = true;
                continue;
            }

            if (angle instanceof AverageTorsionAngleType) {
                averages.add((AverageTorsionAngleType) angle);
                continue;
            }

            AngleValue angleValueL = target.getAngleValue(angle);
            AngleValue angleValueR = model.getAngleValue(angle);
            AngleDelta delta = AngleDelta.calculate(angleValueL, angleValueR);
            result.add(delta);
        }

        if (isPseudophasePucker) {
            TorsionAngle[] taus = new TorsionAngle[] { RNATorsionAngle.TAU0, RNATorsionAngle.TAU1, RNATorsionAngle.TAU2, RNATorsionAngle.TAU3, RNATorsionAngle.TAU4 };
            AngleValue[] l = new AngleValue[5];
            AngleValue[] r = new AngleValue[5];

            for (int i = 0; i < 5; i++) {
                l[i] = target.getAngleValue(taus[i]);
                r[i] = model.getAngleValue(taus[i]);
            }

            AngleValue pL = PseudophasePuckerAngle.calculate(l[0], l[1], l[2],
                    l[3], l[4]);
            AngleValue pR = PseudophasePuckerAngle.calculate(r[0], r[1], r[2],
                    r[3], r[4]);
            result.add(AngleDelta.calculate(pL, pR));
        }

        for (AverageTorsionAngleType average : averages) {
            result.add(average.calculateDelta(result));
        }

        return new ResidueComparison(target, model, result);
    }
}
