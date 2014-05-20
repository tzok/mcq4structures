package pl.poznan.put.matching;

import java.util.List;

public class FragmentComparisonResult implements
        Comparable<FragmentComparisonResult> {
    private final List<ResidueComparisonResult> residueResults;
    private final int firstInvalidCount;
    private final int secondInvalidCount;
    private final int bothInvalidCount;
    private final int validCount;
    private final double mcq;

    public FragmentComparisonResult(
            List<ResidueComparisonResult> residueResults,
            int firstInvalidCount, int secondInvalidCount,
            int bothInvalidCount, int validCount, double mcq) {
        super();
        this.residueResults = residueResults;
        this.firstInvalidCount = firstInvalidCount;
        this.secondInvalidCount = secondInvalidCount;
        this.bothInvalidCount = bothInvalidCount;
        this.validCount = validCount;
        this.mcq = mcq;
    }

    public List<ResidueComparisonResult> getResidueResults() {
        return residueResults;
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

    @Override
    public int compareTo(FragmentComparisonResult o) {
        // TODO
        // if (getMismatchCount() != o.getMismatchCount()) {
        // return Integer.compare(getMismatchCount(), o.getMismatchCount());
        // }

        return Double.compare(mcq, o.mcq);
    }
}
