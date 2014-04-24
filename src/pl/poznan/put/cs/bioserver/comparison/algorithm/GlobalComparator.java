package pl.poznan.put.cs.bioserver.comparison.algorithm;

import pl.poznan.put.cs.bioserver.comparison.bean.GlobalBean;
import pl.poznan.put.cs.bioserver.comparison.bean.StructureSelection;
import pl.poznan.put.cs.bioserver.helper.InvalidInputException;

public interface GlobalComparator {
    GlobalBean compare(StructureSelection s1, StructureSelection s2)
            throws InvalidInputException;
}
