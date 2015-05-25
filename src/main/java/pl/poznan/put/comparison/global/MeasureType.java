package pl.poznan.put.comparison.global;

import pl.poznan.put.comparison.MCQ;
import pl.poznan.put.comparison.RMSD;

public enum MeasureType {
    MCQ(new MCQ()), RMSD(new RMSD());

    private final GlobalComparator comparator;

    private MeasureType(GlobalComparator comparator) {
        this.comparator = comparator;
    }

    public GlobalComparator getComparator() {
        return comparator;
    }

    public String getName() {
        return comparator.getName();
    }
}
