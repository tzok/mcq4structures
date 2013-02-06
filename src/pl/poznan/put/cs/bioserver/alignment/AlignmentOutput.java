package pl.poznan.put.cs.bioserver.alignment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.Calc;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.align.model.AFPChain;
import org.biojava.bio.structure.jama.Matrix;

/**
 * A class that holds the results of structural alignment.
 * 
 * @author tzok
 * 
 */
public class AlignmentOutput {
    private Structure s1;
    private Structure s2;
    private Atom[][] atoms;
    private AFPChain afpChain;

    /**
     * Create an instance which stores information about the computed alignment,
     * input structures and atoms that were used in the process.
     * 
     * @param aligner
     *            Aligner that was used (it contains also the results).
     * @param s1
     *            First structure.
     * @param s2
     *            Second structure.
     * @param atoms
     *            Atoms that were used in the alignment process.
     */
    AlignmentOutput(AFPChain afpChain, Structure s1, Structure s2,
            Atom[][] atoms) {
        this.afpChain = afpChain;
        this.s1 = s1;
        this.s2 = s2;
        this.atoms = atoms.clone();
    }

    public AFPChain getAFPChain() {
        return afpChain;
    }

    /**
     * Get atoms from the default resulting alignment.
     * 
     * @return Two arrays of atoms.
     */
    public Atom[][] getAtoms() {
        Atom[][] result = new Atom[2][];
        int[][][] optAln = afpChain.getOptAln();
        for (int i = 0; i < 2; i++) {
            List<Atom> list = new ArrayList<>();
            for (int[][] element : optAln) {
                for (int k = 0; k < element[i].length; k++) {
                    list.add(atoms[i][k]);
                }
            }
            result[i] = list.toArray(new Atom[list.size()]);
        }
        return result;
    }

    /**
     * Recreate the structures after alignment. The first two are directly the
     * two structures, just superposed on each other. The second two contains
     * only residues that were aligned.
     * 
     * @return Four structures.
     */
    public Structure[] getStructures() {
        Structure[] result = new Structure[4];
        result[0] = s1.clone();
        result[1] = s2.clone();
        Matrix matrix = afpChain.getBlockRotationMatrix()[0];
        try {
            Calc.shift(result[0], Calc.invert(Calc.getCentroid(atoms[0])));
            Calc.shift(result[1], Calc.invert(Calc.getCentroid(atoms[1])));
        } catch (StructureException e) {
            // TODO
            e.printStackTrace();
        }
        Calc.rotate(result[1], matrix);

        Atom[][] aligned = getAtoms();
        for (int i = 0; i < 2; i++) {
            Map<String, Set<Integer>> map = new HashMap<>();
            for (Atom a : aligned[i]) {
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
                    if (!set.contains(groups.get(j).getResidueNumber()
                            .getSeqNum())) {
                        groups.remove(j);
                    }
                }
            }
            result[i + 2] = clone;
        }
        return result;
    }

    @Override
    public String toString() {
        return afpChain.toString();
    }
}
