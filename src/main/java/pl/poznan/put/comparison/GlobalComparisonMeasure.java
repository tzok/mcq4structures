package pl.poznan.put.comparison;


public enum GlobalComparisonMeasure {
    MCQ(new MCQ()), RMSD(new RMSD());

    private final GlobalComparator comparator;

    private GlobalComparisonMeasure(GlobalComparator comparator) {
        this.comparator = comparator;
    }

    public GlobalComparator getComparator() {
        return comparator;
    }
}
