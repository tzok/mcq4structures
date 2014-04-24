package pl.poznan.put.cs.bioserver.comparison.algorithm;

import pl.poznan.put.cs.bioserver.comparison.bean.LocalBean;
import pl.poznan.put.cs.bioserver.comparison.bean.StructureSelection;
import pl.poznan.put.cs.bioserver.helper.InvalidInputException;

public interface LocalComparator {
    LocalBean compare(StructureSelection s1, StructureSelection s2)
            throws InvalidInputException;
}
