package pl.poznan.put.cs.bioserver.torsion;

import org.biojava.bio.structure.Atom;

import pl.poznan.put.cs.bioserver.helper.UniTypeQuadruplet;

/**
 * An ordered four of atoms representing one torsion angle.
 * 
 * @author tzok
 */
public class Quadruplet {
    private UniTypeQuadruplet<Atom> atoms;
    private UniTypeQuadruplet<Integer> indices;

    /**
     * Create an instance of quadruplet saving the four atoms information and
     * their indices in the original list of atoms.
     * 
     * @param array
     *            Four of atoms.
     * @param indices
     *            Indices of atoms in original list of atoms.
     */
    Quadruplet(UniTypeQuadruplet<Atom> atoms, UniTypeQuadruplet<Integer> indices) {
        this.atoms = atoms;
        this.indices = indices;
    }

    public UniTypeQuadruplet<Atom> getAtoms() {
        return atoms;
    }

    /**
     * Check if the given quadruplet represents the same torsion angle. If
     * structures were aligned atom-to-atom, then indices must be equal.
     * Otherwise, check if residue numbers are correct.
     * 
     * @param q
     *            The other quadruplet.
     * @param wasAligned
     *            If atoms were aligned before.
     * @return True, if q represents the same torsion angle.
     */
    boolean isCorresponding(Quadruplet q, boolean wasAligned) {
        if (wasAligned) {
            return indices.equals(q.indices);
        }
        Atom a1 = atoms.a;
        Atom a2 = q.atoms.a;
        if (a1 == null || a2 == null) {
            return false;
        }
        int r1 = a1.getGroup().getResidueNumber().getSeqNum();
        int r2 = a2.getGroup().getResidueNumber().getSeqNum();
        return r1 == r2;
    }
}
