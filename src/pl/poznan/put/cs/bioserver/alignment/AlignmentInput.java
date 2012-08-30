package pl.poznan.put.cs.bioserver.alignment;

import java.util.List;
import java.util.Vector;

import org.biojava.bio.structure.Chain;

public class AlignmentInput {
    private List<Chain> list;

    public AlignmentInput(Chain c1, Chain c2) {
        list = new Vector<>();
        list.add(c1);
        list.add(c2);
    }

    public Chain[] getChains() {
        return list.toArray(new Chain[list.size()]);
    }
    
    @Override
    public String toString() {
        // TODO
        return null;
    }
}
