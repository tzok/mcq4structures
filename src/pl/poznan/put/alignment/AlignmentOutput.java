package pl.poznan.put.alignment;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.Calc;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.align.model.AFPChain;
import org.biojava.bio.structure.jama.Matrix;

import pl.poznan.put.interfaces.Exportable;

/**
 * A class that holds the results of structural alignment.
 * 
 * @author tzok
 * 
 */
public class AlignmentOutput implements Exportable {
    public static class StructuresAligned {
        public final Structure filteredLeft;
        public final Structure filteredRight;
        public final Structure wholeLeft;
        public final Structure wholeRight;

        StructuresAligned(Structure wholeLeft, Structure wholeRight,
                Structure filteredLeft, Structure filteredRight) {
            this.wholeLeft = wholeLeft;
            this.wholeRight = wholeRight;
            this.filteredLeft = filteredLeft;
            this.filteredRight = filteredRight;
        }
    }

    private static Structure filterStructure(Structure structure, Atom[] atoms) {
        Map<String, Set<Integer>> map = new HashMap<>();
        for (Atom a : atoms) {
            Group g = a.getGroup();
            Chain c = g.getChain();
            String chainId = c.getChainID();
            if (!map.containsKey(chainId)) {
                map.put(chainId, new HashSet<Integer>());
            }
            Set<Integer> set = map.get(chainId);
            set.add(g.getResidueNumber().getSeqNum());
        }

        Structure clone = structure.clone();
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
                if (!set.contains(groups.get(j).getResidueNumber().getSeqNum())) {
                    groups.remove(j);
                }
            }
        }
        return clone;
    }

    private final AFPChain afpChain;
    private final String description;
    private final Atom[] listAtomsLeft;
    private final Atom[] listAtomsRight;
    private final Structure structureLeft;
    private final Structure structureRight;

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
    AlignmentOutput(AFPChain afpChain, Structure structureLeft,
            Structure structureRight, Atom[] listAtomsLeft,
            Atom[] listAtomsRight, String description) {
        this.afpChain = afpChain;
        this.structureLeft = structureLeft;
        this.structureRight = structureRight;
        this.listAtomsLeft = listAtomsLeft.clone();
        this.listAtomsRight = listAtomsRight.clone();
        this.description = description;
    }

    @Override
    public void export(File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
            StructuresAligned aligned = getStructures();
            writer.write("MODEL        1                                                                  \n");
            writer.write(aligned.wholeLeft.toPDB());
            writer.write("ENDMDL                                                                          \n");
            writer.write("MODEL        2                                                                  \n");
            writer.write(aligned.wholeRight.toPDB());
            writer.write("ENDMDL                                                                          \n");
        } catch (StructureException e) {
            throw new IOException(e);
        }
    }

    public AFPChain getAFPChain() {
        return afpChain;
    }

    /**
     * Get atoms from the default resulting alignment.
     * 
     * @return Two arrays of atoms.
     */
    public Pair<Atom[], Atom[]> getAtoms() {
        List<Atom> l = new ArrayList<>();
        List<Atom> r = new ArrayList<>();

        int[][][] optAln = afpChain.getOptAln();
        for (int i = 0; i < 2; i++) {
            for (int[][] element : optAln) {
                Atom[] from = i == 0 ? listAtomsLeft : listAtomsRight;
                List<Atom> to = i == 0 ? l : r;

                for (int k = 0; k < element[i].length; k++) {
                    to.add(from[k]);
                }
            }
        }

        return Pair.of(l.toArray(new Atom[l.size()]),
                r.toArray(new Atom[r.size()]));
    }

    /**
     * Recreate the structures after alignment. The first two are directly the
     * two structures, just superposed on each other. The second two contains
     * only residues that were aligned.
     * 
     * @return Four structures.
     * @throws StructureException
     */
    public StructuresAligned getStructures() throws StructureException {
        Structure leftWhole = structureLeft.clone();
        Structure rightWhole = structureRight.clone();
        Matrix matrix = afpChain.getBlockRotationMatrix()[0];
        Atom c1 = Calc.getCentroid(listAtomsLeft);
        Atom c2 = Calc.getCentroid(listAtomsRight);
        Calc.shift(leftWhole, Calc.invert(c1));
        Calc.shift(rightWhole, Calc.invert(c2));
        Calc.rotate(rightWhole, matrix);

        Pair<Atom[], Atom[]> aligned = getAtoms();
        Structure leftFiltered = AlignmentOutput.filterStructure(leftWhole,
                aligned.getLeft());
        Structure rightFiltered = AlignmentOutput.filterStructure(rightWhole,
                aligned.getRight());
        return new StructuresAligned(leftWhole, rightWhole, leftFiltered,
                rightFiltered);
    }

    @Override
    public File suggestName() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
        String filename = sdf.format(new Date());
        filename += "-3DSTRA-";
        filename += description.replace(", ", "-");
        filename += ".pdb";
        return new File(filename);
    }

    @Override
    public String toString() {
        return afpChain.toString();
    }
}
