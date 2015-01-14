package pl.poznan.put.matching;

import java.util.List;

import pl.poznan.put.structure.CompactFragment;

public class FragmentSuperposition {
    private final List<CompactFragment> targetFragments;
    private final List<CompactFragment> modelFragments;

    public FragmentSuperposition(List<CompactFragment> targetFragments,
            List<CompactFragment> modelFragments) {
        super();
        this.targetFragments = targetFragments;
        this.modelFragments = modelFragments;
    }

    public String toPDB() {
        StringBuilder builder = new StringBuilder();
        builder.append("MODEL        1                                                                  \n");
        for (CompactFragment fragment : targetFragments) {
            builder.append(fragment.toPDB());
        }
        builder.append("ENDMDL                                                                          \n");
        builder.append("MODEL        2                                                                  \n");
        for (CompactFragment fragment : modelFragments) {
            builder.append(fragment.toPDB());
        }
        builder.append("ENDMDL                                                                          \n");
        return builder.toString();
    }
}
