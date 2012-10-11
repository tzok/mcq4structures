package pl.poznan.put.cs.bioserver.alignment;

import java.util.ArrayList;
import java.util.List;

import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Structure;

public class AlignmentInput {
    private List<Structure> list;

    public AlignmentInput(Structure s1, Structure s2) {
        list = new ArrayList<>();
        list.add(s1);
        list.add(s2);
    }

    public Chain[] getChains() {
        return list.toArray(new Chain[list.size()]);
    }

    @Override
    public String toString() {
        if (list == null || list.size() == 0)
            return "AlignmentInput: structures not provided";
        StringBuilder builder = new StringBuilder();
        builder.append("AlignmentInput:\n");
        builder.append(list.get(0));
        builder.append('\n');
        builder.append(list.get(1));
        return builder.toString();
    }
}
