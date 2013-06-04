package pl.poznan.put.cs.bioserver.alignment;

import org.biojava.bio.structure.Structure;

/**
 * An input to structure alignment methods which makes it easier to cache and
 * remember.
 * 
 * @author tzok
 */
class AlignmentInput {
    private Structure left;
    private Structure right;

    AlignmentInput(Structure left, Structure right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public String toString() {
        if (left == null || right == null) {
            return "AlignmentInput: structures not provided";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("AlignmentInput:\n");
        builder.append(left);
        builder.append('\n');
        builder.append(right);
        return builder.toString();
    }
}
