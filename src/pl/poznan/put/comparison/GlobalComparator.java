package pl.poznan.put.comparison;

import pl.poznan.put.structure.StructureSelection;

public interface GlobalComparator {
    GlobalComparisonResult compareGlobally(StructureSelection s1,
            StructureSelection s2) throws IncomparableStructuresException;

    String getName();
}
