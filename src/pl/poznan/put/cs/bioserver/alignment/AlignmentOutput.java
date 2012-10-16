package pl.poznan.put.cs.bioserver.alignment;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.align.StructurePairAligner;
import org.biojava.bio.structure.align.pairwise.AlternativeAlignment;

public class AlignmentOutput {
    private StructurePairAligner aligner;
    private Structure s1;
    private Structure s2;
    private AlternativeAlignment[] alignments;
    private Atom[][] atoms;

    public AlignmentOutput(StructurePairAligner aligner, Structure s1,
            Structure s2, Atom[][] atoms) {
        this.aligner = aligner;
        this.s1 = s1;
        this.s2 = s2;
        this.atoms = atoms;

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
        int[][] allIdxs = new int[][] { alignments[i].getIdx1(),
                alignments[i].getIdx2() };
        Atom[][] result = new Atom[2][];
        for (int k = 0; k < 2; k++) {
            Atom[] current = atoms[k];
            int[] idx = allIdxs[k];
            Atom[] filtered = new Atom[idx.length];
            for (int j = 0; j < idx.length; j++) {
                filtered[j] = current[idx[j]];
            }
            result[k] = filtered;
        }
        return result;
    }

    public Atom[][] getAtoms() {
        return getAtoms(0);
    }

    public Structure[] getStructures() {
        Structure alignedStructure = alignments[0].getAlignedStructure(s1, s2);
        Structure sc1 = s1.clone();
        Structure sc2 = s2.clone();
        sc1.setChains(alignedStructure.getModel(0));
        sc2.setChains(alignedStructure.getModel(1));
        Structure[] result = new Structure[] { sc1, sc2, null, null };
        for (int i = 0; i < 2; i++) {
            Map<String, Set<Integer>> map = new HashMap<>();
            for (Atom a : atoms[i]) {
                Group g = a.getGroup();
                Chain c = g.getChain();
                String chainId = c.getChainID();
                if (!map.containsKey(chainId)) {
                    map.put(chainId, new HashSet<Integer>());
                }
                Set<Integer> set = map.get(chainId);
                set.add(g.getResidueNumber().getSeqNum());
            }

            Structure clone = result[i].clone();
            List<Chain> chains = clone.getChains();
            for (int j = 0; j < chains.size(); j++) {
                if (!map.keySet().contains(chains.get(j).getChainID())) {
                    chains.remove(j);
                }
            }

            for (Chain c : chains) {
                String chainId = c.getChainID();
                List<Group> groups = c.getAtomGroups();
                Set<Integer> set = map.get(chainId);
                for (int j = 0; j < groups.size(); j++) {
                    if (!set.contains(groups.get(i).getResidueNumber()
                            .getSeqNum())) {
                        groups.remove(i);
                    }
                }
            }
            result[i + 2] = clone;
        }
        return result;
    }
}
