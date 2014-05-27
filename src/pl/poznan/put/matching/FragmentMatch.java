package pl.poznan.put.matching;

import pl.poznan.put.structure.CompactFragment;

public class FragmentMatch {
    private final CompactFragment biggerAll;
    private final CompactFragment biggerOnlyMatched;
    private final CompactFragment smaller;
    private final FragmentComparison bestResult;
    private final int bestShift;

    public FragmentMatch(CompactFragment biggerAll,
            CompactFragment biggerOnlyMatched, CompactFragment smaller,
            FragmentComparison bestResult, int bestShift) {
        super();
        this.biggerAll = biggerAll;
        this.biggerOnlyMatched = biggerOnlyMatched;
        this.smaller = smaller;
        this.bestResult = bestResult;
        this.bestShift = bestShift;
    }

    public CompactFragment getBiggerAll() {
        return biggerAll;
    }

    public CompactFragment getBiggerOnlyMatched() {
        return biggerOnlyMatched;
    }

    public CompactFragment getSmaller() {
        return smaller;
    }

    public FragmentComparison getBestResult() {
        return bestResult;
    }

    public int getBestShift() {
        return bestShift;
    }

    public int getSize() {
        return smaller.getSize();
    }

    @Override
    public String toString() {
        return biggerOnlyMatched + "\t" + smaller + "\t"
                + Math.toDegrees(bestResult.getMcq());
    }
}
