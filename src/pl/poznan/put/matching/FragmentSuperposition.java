package pl.poznan.put.matching;

import java.util.List;

import pl.poznan.put.structure.CompactFragment;

public class FragmentSuperposition {
    private final List<CompactFragment> left;
    private final List<CompactFragment> right;
    private final double rmsd;

    public FragmentSuperposition(List<CompactFragment> left,
            List<CompactFragment> right, double rmsd) {
        super();
        this.left = left;
        this.right = right;
        this.rmsd = rmsd;
    }

    public double getRMSD() {
        return rmsd;
    }

    public String toPDB() {
        StringBuilder builder = new StringBuilder();
        builder.append("MODEL        1                                                                  \n");
        for (CompactFragment fragment : left) {
            builder.append(fragment.toPDB());
        }
        builder.append("ENDMDL                                                                          \n");
        builder.append("MODEL        2                                                                  \n");
        for (CompactFragment fragment : right) {
            builder.append(fragment.toPDB());
        }
        builder.append("ENDMDL                                                                          \n");
        return builder.toString();
    }
}
