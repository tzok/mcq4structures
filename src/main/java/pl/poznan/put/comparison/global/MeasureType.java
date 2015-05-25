package pl.poznan.put.comparison.global;

import pl.poznan.put.comparison.IncomparableStructuresException;
import pl.poznan.put.comparison.MCQ;
import pl.poznan.put.comparison.RMSD;
import pl.poznan.put.matching.StructureSelection;

public enum MeasureType {
    MCQ(new MCQ()), RMSD(new RMSD());

    private final GlobalComparator comparator;

    private MeasureType(GlobalComparator comparator) {
        this.comparator = comparator;
    }

    public GlobalResult compareGlobally(StructureSelection s1,
            StructureSelection s2) throws IncomparableStructuresException {
        return comparator.compareGlobally(s1, s2);
    }

    public String getName() {
        return comparator.getName();
    }
}
