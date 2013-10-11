package pl.poznan.put.cs.bioserver.torsion;

import org.biojava.bio.structure.Group;

import pl.poznan.put.cs.bioserver.helper.UniTypeQuadruplet;

public abstract class AbstractAngleType implements AngleType {
    @Override
    public abstract String getAngleDisplayName();

    @Override
    public abstract String getAngleName();

    @Override
    public abstract UniTypeQuadruplet<String> getAtomNames(Group residue);

    @Override
    public abstract UniTypeQuadruplet<Integer> getGroupRule();

    @Override
    public String toString() {
        return getAngleDisplayName();
    }
}
