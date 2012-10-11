package pl.poznan.put.cs.bioserver.alignment;

import java.util.ArrayList;
import java.util.List;

import org.biojava.bio.structure.Chain;

public class AlignmentInput {
    private List<Chain> list;

    public AlignmentInput(Chain c1, Chain c2) {
        list = new ArrayList<>();
        list.add(c1);
        list.add(c2);
    }

    public Chain[] getChains() {
        return list.toArray(new Chain[list.size()]);
    }

    @Override
    public String toString() {
        if (list == null || list.size() == 0)
            return "AlignmentInput: chains not provided";
        StringBuilder builder = new StringBuilder();
        builder.append("AlignmentInput:\n");
        builder.append(list.get(0));
        builder.append('\n');
        builder.append(list.get(1));
        return builder.toString();
    }
}
