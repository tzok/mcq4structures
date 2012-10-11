package pl.poznan.put.cs.bioserver.alignment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.align.StructurePairAligner;
import org.biojava.bio.structure.align.pairwise.AlternativeAlignment;

public class AlignmentOutput {
    private StructurePairAligner aligner;
    private Structure s1;
    private Structure s2;
    private AlternativeAlignment[] alignments;

    public AlignmentOutput(StructurePairAligner aligner, Structure s1,
            Structure s2) {
        this.aligner = aligner;
        this.s1 = s1;
        this.s2 = s2;

        this.alignments = aligner.getAlignments();
    }

    public StructurePairAligner getAligner() {
        return aligner;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(aligner);
        for (AlternativeAlignment a : alignments) {
            builder.append('\n');
            builder.append(a);
        }
        return builder.toString();
    }

    public Atom[][] getAtoms(int i) {
        Atom[][] allAtoms = new Atom[][] { aligner.getAlignmentAtoms(s1),
                aligner.getAlignmentAtoms(s2) };
        int[][] allIdxs = new int[][] { alignments[i].getIdx1(),
                alignments[i].getIdx2() };
        Atom[][] result = new Atom[2][];
        for (int k = 0; k < 2; k++) {
            Atom[] atoms = allAtoms[k];
            int[] idx = allIdxs[k];
            Atom[] filtered = new Atom[idx.length];
            for (int j = 0; j < idx.length; j++) {
                filtered[j] = atoms[idx[j]];
            }
            result[k] = filtered;
        }
        return result;
    }

    public Atom[][] getAtoms() {
        return getAtoms(0);
    }

    public Group[][] getGroups(int i) {
        Atom[][] atoms = getAtoms(i);
        Group[][] result = new Group[2][];
        for (int j = 0; j < 2; j++) {
            List<Group> list = new ArrayList<>();
            Set<Integer> set = new HashSet<>();
            for (Atom a : atoms[j]) {
                Group group = a.getGroup();
                Integer seqNum = group.getResidueNumber().getSeqNum();
                if (set.contains(seqNum))
                    continue;
                set.add(seqNum);
                list.add(group);
            }
            result[j] = list.toArray(new Group[list.size()]);
        }
        return result;
    }
}
