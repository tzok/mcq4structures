package pl.poznan.put.matching;

import java.util.ArrayList;
import java.util.List;

import pl.poznan.put.common.MoleculeType;
import pl.poznan.put.helper.TorsionAnglesHelper;
import pl.poznan.put.nucleic.PseudophasePuckerAngle;
import pl.poznan.put.nucleic.RNATorsionAngle;
import pl.poznan.put.structure.CompactFragment;
import pl.poznan.put.structure.ResidueTorsionAngles;
import pl.poznan.put.structure.StructureSelection;
import pl.poznan.put.torsion.AngleValue;
import pl.poznan.put.torsion.AverageAngle;
import pl.poznan.put.torsion.ChiTorsionAngleType;
import pl.poznan.put.torsion.TorsionAngle;
import pl.poznan.put.utility.TorsionAngleDelta;

public class MCQMatcher implements StructureMatcher {
    private boolean matchChiByType;
    private List<TorsionAngle> angles;

    public MCQMatcher(boolean matchChiByType, List<TorsionAngle> angles) {
        super();
        this.matchChiByType = matchChiByType;
        this.angles = angles;
    }

    @Override
    public SelectionMatch matchSelections(StructureSelection s1,
            StructureSelection s2) {
        if (s1.getSize() == 0 || s2.getSize() == 2) {
            return new SelectionMatch(matchChiByType, angles,
                    new ArrayList<FragmentMatch>());
        }

        CompactFragment[] fr1 = s1.getCompactFragments();
        CompactFragment[] fr2 = s2.getCompactFragments();

        FragmentMatch[][] matrix = new FragmentMatch[fr1.length][];
        for (int i = 0; i < fr1.length; i++) {
            matrix[i] = new FragmentMatch[fr2.length];
        }

        for (int i = 0; i < fr1.length; i++) {
            CompactFragment fi = fr1[i];

            for (int j = 0; j < fr2.length; j++) {
                CompactFragment fj = fr2[j];

                if (fi.getMoleculeType() != fj.getMoleculeType()) {
                    continue;
                }

                matrix[i][j] = matchFragments(fi, fj);
            }
        }

        List<FragmentMatch> fragmentMatches = MCQMatcher.assignFragments(matrix);
        return new SelectionMatch(matchChiByType, angles, fragmentMatches);
    }

    @Override
    public FragmentMatch matchFragments(CompactFragment f1, CompactFragment f2) {
        CompactFragment smaller = f1;
        CompactFragment bigger = f2;

        if (f1.getSize() > f2.getSize()) {
            smaller = f2;
            bigger = f1;
        }

        List<ResidueTorsionAngles> biggerAngles = bigger.getTorsionAngles();
        List<ResidueTorsionAngles> smallerAngles = smaller.getTorsionAngles();
        int sizeDifference = bigger.getSize() - smaller.getSize();
        FragmentComparisonResult bestResult = null;
        int bestShift = 0;

        for (int i = 0; i <= sizeDifference; i++) {
            List<ResidueComparisonResult> residueResults = new ArrayList<>();

            for (int j = 0; j < smaller.getSize(); j++) {
                ResidueTorsionAngles a1 = smallerAngles.get(j);
                ResidueTorsionAngles a2 = biggerAngles.get(j + i);
                residueResults.add(compareResidues(a1, a2));
            }

            FragmentComparisonResult fragmentResult = aggregateResidueResults(residueResults);
            if (bestResult == null || fragmentResult.compareTo(bestResult) < 0) {
                bestResult = fragmentResult;
                bestShift = i;
            }
        }

        return new FragmentMatch(bigger, CompactFragment.shift(bigger,
                bestShift, smaller.getSize()), smaller, bestResult, bestShift);
    }

    private static List<FragmentMatch> assignFragments(FragmentMatch[][] matrix) {
        List<FragmentMatch> result = new ArrayList<>();
        boolean[] usedi = new boolean[matrix.length];
        boolean[] usedj = new boolean[matrix[0].length];

        while (true) {
            FragmentComparisonResult minimum = null;
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

                    FragmentComparisonResult matchResult = match.getBestResult();
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

    private ResidueComparisonResult compareResidues(ResidueTorsionAngles a1,
            ResidueTorsionAngles a2) {
        List<TorsionAngleDelta> result = new ArrayList<>();
        boolean isPseudophasePucker = false;
        boolean isAverageProtein = false;
        boolean isAverageRNA = false;

        for (TorsionAngle angle : angles) {
            if (angle instanceof PseudophasePuckerAngle) {
                isPseudophasePucker = true;
                continue;
            }

            if (angle instanceof AverageAngle) {
                if (angle.getMoleculeType() == MoleculeType.PROTEIN) {
                    isAverageProtein = true;
                } else if (angle.getMoleculeType() == MoleculeType.RNA) {
                    isAverageRNA = true;
                }
                continue;
            }

            if (angle instanceof ChiTorsionAngleType) {
                AngleValue angleValueL = a1.getChiAngleValue((ChiTorsionAngleType) angle);
                AngleValue angleValueR = a2.getChiAngleValue((ChiTorsionAngleType) angle);
                TorsionAngleDelta delta = TorsionAngleDelta.calculateChiDelta(
                        angleValueL, angleValueR, matchChiByType);
                result.add(delta);
                continue;
            }

            AngleValue angleValueL = a1.getAngleValue(angle);
            AngleValue angleValueR = a2.getAngleValue(angle);
            TorsionAngleDelta delta = TorsionAngleDelta.calculate(angleValueL,
                    angleValueR);
            result.add(delta);
        }

        if (isAverageProtein) {
            TorsionAngleDelta average = TorsionAngleDelta.calculateAverage(
                    MoleculeType.PROTEIN, result);
            result.add(average);
        }

        if (isAverageRNA) {
            TorsionAngleDelta average = TorsionAngleDelta.calculateAverage(
                    MoleculeType.RNA, result);
            result.add(average);
        }

        if (isPseudophasePucker) {
            TorsionAngle[] taus = new TorsionAngle[] { RNATorsionAngle.TAU0, RNATorsionAngle.TAU1, RNATorsionAngle.TAU2, RNATorsionAngle.TAU3, RNATorsionAngle.TAU4 };
            AngleValue[] l = new AngleValue[5];
            AngleValue[] r = new AngleValue[5];

            for (int i = 0; i < 5; i++) {
                l[i] = a1.getAngleValue(taus[i]);
                r[i] = a2.getAngleValue(taus[i]);
            }

            AngleValue pL = PseudophasePuckerAngle.calculate(l[0], l[1], l[2],
                    l[3], l[4]);
            AngleValue pR = PseudophasePuckerAngle.calculate(r[0], r[1], r[2],
                    r[3], r[4]);
            result.add(TorsionAngleDelta.calculate(pL, pR));
        }

        return new ResidueComparisonResult(a1, a2, result);
    }

    private FragmentComparisonResult aggregateResidueResults(
            List<ResidueComparisonResult> residueResults) {
        List<Double> deltas = new ArrayList<>();
        int firstInvalid = 0;
        int secondInvalid = 0;
        int bothInvalid = 0;

        for (ResidueComparisonResult result : residueResults) {
            for (TorsionAngle angle : angles) {
                TorsionAngleDelta delta = result.getDelta(angle);

                if (delta == null) {
                    continue;
                }

                switch (delta.getState()) {
                case BOTH_INVALID:
                    bothInvalid++;
                    break;
                case BOTH_VALID:
                    deltas.add(delta.getDelta());
                    break;
                case TORSION_LEFT_INVALID:
                    firstInvalid++;
                    break;
                case TORSION_RIGHT_INVALID:
                    secondInvalid++;
                    break;
                case DIFFERENT_CHI:
                    bothInvalid++;
                default:
                    break;
                }
            }
        }

        double mcq = TorsionAnglesHelper.calculateMean(deltas);
        return new FragmentComparisonResult(residueResults, firstInvalid,
                secondInvalid, bothInvalid, deltas.size(), mcq);
    }
}
