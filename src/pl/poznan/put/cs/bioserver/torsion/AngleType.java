package pl.poznan.put.cs.bioserver.torsion;

import org.biojava.bio.structure.Group;

public interface AngleType {
    public String[] getAtomNames(Group residue);
    public String getAngleName();
    public int[] getGroupRule();
}
