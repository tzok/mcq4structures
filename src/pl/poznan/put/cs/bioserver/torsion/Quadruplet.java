package pl.poznan.put.cs.bioserver.torsion;

import java.util.Arrays;

import org.biojava.bio.structure.Atom;

/**
 * An ordered four of atoms representing one torsion angle.
 * 
 * @author tzok
 */
public class Quadruplet {
    private Atom[] array;
    private int[] indices;

    /**
     * Create an instance of quadruplet saving the four atoms information and
     * their indices in the original list of atoms.
     * 
     * @param array
     *            Four of atoms.
     * @param indices
     *            Indices of atoms in original list of atoms.
     */
    public Quadruplet(Atom[] array, int[] indices) {
        this.array = array.clone();
        this.indices = indices;
    }

    @SuppressWarnings("javadoc")
    public Atom[] getAtoms() {
        return array;
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
    public boolean isCorresponding(Quadruplet q, boolean wasAligned) {
        if (wasAligned) {
            return Arrays.equals(indices, q.indices);
        }
        int r1 = array[0].getGroup().getResidueNumber().getSeqNum();
        int r2 = q.array[0].getGroup().getResidueNumber().getSeqNum();
        return r1 == r2;
    }
}
