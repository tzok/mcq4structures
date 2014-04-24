package pl.poznan.put.comparison.algorithm;

import pl.poznan.put.comparison.bean.LocalBean;
import pl.poznan.put.comparison.bean.StructureSelection;
import pl.poznan.put.helper.InvalidInputException;
import pl.poznan.put.torsion.StructureInTorsionAngleSpace;

public class McqLocalComparator implements LocalComparator {
    @Override
    public LocalBean compare(StructureSelection s1, StructureSelection s2)
            throws InvalidInputException {
        StructureInTorsionAngleSpace t1 = s1.getTorsionAngleSpace();
        StructureInTorsionAngleSpace t2 = s2.getTorsionAngleSpace();
        return t1.compareLocally(t2);
    }

}
