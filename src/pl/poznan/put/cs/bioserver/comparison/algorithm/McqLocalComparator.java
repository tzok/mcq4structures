package pl.poznan.put.cs.bioserver.comparison.algorithm;

import pl.poznan.put.cs.bioserver.comparison.bean.LocalBean;
import pl.poznan.put.cs.bioserver.comparison.bean.StructureSelection;
import pl.poznan.put.cs.bioserver.helper.InvalidInputException;
import pl.poznan.put.cs.bioserver.torsion.StructureInTorsionAngleSpace;

public class McqLocalComparator implements LocalComparator {
    @Override
    public LocalBean compare(StructureSelection s1, StructureSelection s2)
            throws InvalidInputException {
        StructureInTorsionAngleSpace t1 = s1.getTorsionAngleSpace();
        StructureInTorsionAngleSpace t2 = s2.getTorsionAngleSpace();
        return t1.compareLocally(t2);
    }

}
