package pl.poznan.put.cs.bioserver.torsion;

import org.biojava.bio.structure.Group;

import pl.poznan.put.cs.bioserver.helper.UniTypeQuadruplet;

/**
 * A set of methods that every type of angle has to implement.
 * 
 * @author tzok
 */
public interface AngleType {
    String getAngleName();

    /**
     * Get names of atoms that make up for the given angle. Note that this is
     * residue dependent, because for example CHI angle has different definition
     * for purines and pyrimidines.
     * 
     * @param residue
     *            Residue containing this angle.
     * @return An array of Strings with names of atom, size 4.
     */
    UniTypeQuadruplet<String> getAtomNames(Group residue);

    /**
     * A group rule is a 4-tuple of integers which represents relation between
     * residue numbers between four atoms if they are to form a torsion angle.
     * 
     * @return An array of integers, size 4.
     */
    UniTypeQuadruplet<Integer> getGroupRule();
}
