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

    public boolean isCorresponding(Quadruplet q) {
        return Arrays.equals(indices, q.indices);
    }

    public Atom[] getAtoms() {
        return array;
    }
}
