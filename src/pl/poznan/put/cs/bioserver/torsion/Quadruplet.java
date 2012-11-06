package pl.poznan.put.cs.bioserver.torsion;

import java.util.Arrays;

import org.biojava.bio.structure.Atom;

public class Quadruplet {
    private Atom[] array;
    private int[] indices;

    public Quadruplet(Atom[] array, int[] indices) {
        this.array = array;
        this.indices = indices;
    }

    public boolean isCorresponding(Quadruplet q, boolean wasAligned) {
        if (wasAligned) {
            return Arrays.equals(indices, q.indices);
        }
        int r1 = array[0].getGroup().getResidueNumber().getSeqNum();
        int r2 = q.array[0].getGroup().getResidueNumber().getSeqNum();
        return r1 == r2;
    }

    public Atom[] getAtoms() {
        return array;
    }
}
