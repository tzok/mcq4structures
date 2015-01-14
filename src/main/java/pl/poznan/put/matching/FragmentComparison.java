package pl.poznan.put.matching;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import pl.poznan.put.helper.TorsionAnglesHelper;
import pl.poznan.put.torsion.AngleDelta;
import pl.poznan.put.torsion.TorsionAngle;

public class FragmentComparison implements Comparable<FragmentComparison>,
        Iterable<ResidueComparison> {
    public static FragmentComparison fromResidueComparisons(
            List<ResidueComparison> residueResults, List<TorsionAngle> angles) {
        List<Double> deltas = new ArrayList<>();
        int firstInvalid = 0;
        int secondInvalid = 0;
        int bothInvalid = 0;

        for (ResidueComparison result : residueResults) {
            for (TorsionAngle angle : angles) {
                AngleDelta delta = result.getAngleDelta(angle);

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
                case TORSION_TARGET_INVALID:
                    firstInvalid++;
                    break;
                case TORSION_MODEL_INVALID:
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
        return new FragmentComparison(residueResults, angles, firstInvalid,
                secondInvalid, bothInvalid, deltas.size(), mcq);
    }

    private final List<ResidueComparison> residueResults;
    private final List<TorsionAngle> angles;
    private final int firstInvalidCount;
    private final int secondInvalidCount;
    private final int bothInvalidCount;
    private final int validCount;
    private final double mcq;

    public FragmentComparison(List<ResidueComparison> residueResults,
            List<TorsionAngle> angles, int firstInvalidCount,
            int secondInvalidCount, int bothInvalidCount, int validCount,
            double mcq) {
        super();
        this.residueResults = residueResults;
        this.angles = angles;
        this.firstInvalidCount = firstInvalidCount;
        this.secondInvalidCount = secondInvalidCount;
        this.bothInvalidCount = bothInvalidCount;
        this.validCount = validCount;
        this.mcq = mcq;
    }

    public int getFirstInvalidCount() {
        return firstInvalidCount;
    }

    public int getSecondInvalidCount() {
        return secondInvalidCount;
    }

    public int getBothInvalidCount() {
        return bothInvalidCount;
    }

    public int getValidCount() {
        return validCount;
    }

    public double getMcq() {
        return mcq;
    }

    public int getMismatchCount() {
        return firstInvalidCount + secondInvalidCount;
    }

    public int getSize() {
        return residueResults.size();
    }

    public ResidueComparison getResidueComparison(int index) {
        return residueResults.get(index);
    }

    public List<TorsionAngle> getAngles() {
        return Collections.unmodifiableList(angles);
    }

    @Override
    public int compareTo(FragmentComparison o) {
        // TODO
        // if (getMismatchCount() != o.getMismatchCount()) {
        // return Integer.compare(getMismatchCount(), o.getMismatchCount());
        // }

        return Double.compare(mcq, o.mcq);
    }

    @Override
    public Iterator<ResidueComparison> iterator() {
        return residueResults.iterator();
    }
}
