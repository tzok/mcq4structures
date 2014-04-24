package pl.poznan.put.comparison.algorithm;

import pl.poznan.put.comparison.bean.LocalBean;
import pl.poznan.put.comparison.bean.StructureSelection;
import pl.poznan.put.helper.InvalidInputException;

public interface LocalComparator {
    LocalBean compare(StructureSelection s1, StructureSelection s2)
            throws InvalidInputException;
}
