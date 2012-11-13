package pl.poznan.put.cs.bioserver.alignment;

import java.util.ArrayList;
import java.util.List;

import org.biojava.bio.structure.Structure;

/**
 * An input to structure alignment methods which makes it easier to cache and
 * remember.
 * 
 * @author tzok
 */
public class AlignmentInput {
    private List<Structure> list;

    public AlignmentInput(Structure s1, Structure s2) {
        list = new ArrayList<>();
        list.add(s1);
        list.add(s2);
    }

    @Override
    public String toString() {
        if (list == null || list.size() == 0) {
            return "AlignmentInput: structures not provided";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("AlignmentInput:\n");
        builder.append(list.get(0));
        builder.append('\n');
        builder.append(list.get(1));
        return builder.toString();
    }
}
