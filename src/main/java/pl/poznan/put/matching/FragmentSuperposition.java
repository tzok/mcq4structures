package pl.poznan.put.matching;

import pl.poznan.put.pdb.analysis.PdbCompactFragment;

import java.util.List;

public class FragmentSuperposition {
    private final List<PdbCompactFragment> targetFragments;
    private final List<PdbCompactFragment> modelFragments;

    public FragmentSuperposition(List<PdbCompactFragment> targetFragments,
                                 List<PdbCompactFragment> modelFragments) {
        super();
        this.targetFragments = targetFragments;
        this.modelFragments = modelFragments;
    }

    public String toPDB() {
        StringBuilder builder = new StringBuilder();
        builder.append(
                "MODEL        1                                              "
                + "                    \n");
        for (PdbCompactFragment fragment : targetFragments) {
            builder.append(fragment.toPdb());
        }
        builder.append(
                "ENDMDL                                                      "
                + "                    \n");
        builder.append(
                "MODEL        2                                              "
                + "                    \n");
        for (PdbCompactFragment fragment : modelFragments) {
            builder.append(fragment.toPdb());
        }
        builder.append(
                "ENDMDL                                                      "
                + "                    \n");
        return builder.toString();
    }
}
