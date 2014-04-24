package pl.poznan.put.comparison.algorithm;

import pl.poznan.put.comparison.bean.GlobalBean;
import pl.poznan.put.comparison.bean.StructureSelection;
import pl.poznan.put.helper.InvalidInputException;

public interface GlobalComparator {
    GlobalBean compare(StructureSelection s1, StructureSelection s2)
            throws InvalidInputException;
}
