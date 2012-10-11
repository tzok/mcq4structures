package pl.poznan.put.cs.bioserver.alignment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.StructureImpl;
import org.biojava.bio.structure.align.StrucAligParameters;
import org.biojava.bio.structure.align.StructurePairAligner;
import org.biojava.bio.structure.align.pairwise.AlternativeAlignment;

import pl.poznan.put.cs.bioserver.helper.Helper;

public class StructureAligner {
    private static Logger LOGGER = Logger.getLogger(StructureAligner.class);
    private static Map<AlignmentInput, AlignmentOutput> cache = new HashMap<>();

    public static List<Atom> toAtomList(AlternativeAlignment alignment,
            boolean first) {
        if (first)
            alignment.getIdx1();
        return null;
    }

    public static AlignmentOutput align(Chain c1, Chain c2)
            throws StructureException {
        /*
         * Check if alignment was made before
         */
        AlignmentInput input = new AlignmentInput(c1, c2);
        if (StructureAligner.cache.containsKey(input)) {
            StructureAligner.LOGGER.info("Reusing alignment data from cache");
            return StructureAligner.cache.get(input);
        }

        /*
         * Align the structures
         */
        StructurePairAligner aligner = new StructurePairAligner();
        if (Helper.isNucleicAcid(c1)) {
            StrucAligParameters parameters = new StrucAligParameters();
            // FIXME
            parameters.setUsedAtomNames(new String[] { " C1'", " C2 ", " C2'",
                    " C3'", " C4 ", " C4'", " C5 ", " C5'", " C6 ", " N1 ",
                    " N3 ", " O2'", " O3'", " O4'", " O5'", " OP1", " OP2",
                    " P  " });
            aligner.setParams(parameters);
        } // no else{} because for proteins the defaults are fine

        StructureImpl s1 = new StructureImpl(c1);
        StructureImpl s2 = new StructureImpl(c2);
        Helper.normalizeAtomNames(s1);
        Helper.normalizeAtomNames(s2);
        aligner.align(s1, s2);
        AlignmentOutput result = new AlignmentOutput(aligner, s1, s2);
        StructureAligner.cache.put(input, result);
        return result;

        // Structure structure = alignment.getAlignedStructure(s1, s2);
        //
        // /*
        // * Construct the AlignmentOuput object
        // */
        // Chain[] chains = new Chain[4];
        // chains[0] = structure.getModel(0).get(0);
        // chains[1] = structure.getModel(1).get(0);
        // chains[2] = (Chain) chains[0].clone();
        // chains[3] = (Chain) chains[1].clone();
        //
        // String[][] residuesStrings = new String[][] {
        // alignment.getPDBresnum1(), alignment.getPDBresnum2() };
        // assert residuesStrings[0].length == residuesStrings[1].length;
        //
        // int[][] residues = new int[2][];
        // int i = 0;
        // for (String[] s : residuesStrings)
        // residues[i++] = parseResidueString(s);
        //
        // for (int[] )
        //
        // // HERE
        //
        // chains[2].setAtomGroups(StructureAligner.filterGroups(chains[0],
        // residuesStrings[0]));
        // chains[3].setAtomGroups(StructureAligner.filterGroups(chains[1],
        // residuesStrings[1]));
        //
        // Map<String, Set<String>> check1st = new HashMap<>();
        // Map<String, Set<String>> check2nd = new HashMap<>();
        // for (int i = 0; i < residuesStrings[0].length; i++) {
        // for (int j = 0; j < 2; j++) {
        // String resi = residuesStrings[j][i];
        // Map<String, Set<String>> check = j == 0 ? check1st : check2nd;
        // if (!check.containsKey(resi))
        // check.put(resi, new HashSet<String>());
        // check.get(resi).add(residuesStrings[j ^ 1][i]);
        // }
        // }
        //
        // for (int j = 0; j < 2; j++) {
        // Map<String, Set<String>> check = j == 0 ? check1st : check2nd;
        // for (String key : check.keySet()) {
        // if (check.get(key).size() != 1) {
        // System.out.println("Mismatch");
        // }
        // }
        // }
        //
        // List<List<Integer>> residuesInts = new Vector<>();
        // for (int i = 0; i < 2; i++) {
        // Set<String> set = new HashSet<>();
        // List<Integer> list = new Vector<>();
        // for (int j = 0; j < residuesStrings[i].length; j++) {
        // String residueString = residuesStrings[i][j];
        // if (set.contains(residueString))
        // continue;
        // set.add(residueString);
        // int residueInt = Integer.valueOf(residueString.split(":")[0]);
        // list.add(residueInt);
        // }
        // residuesInts.add(list);
        // }
        //
        // // if (residuesInts.get(0).size() != residuesInts.get(1).size()) {
        // // for (int i = 0; i < residuesStrings[0].length; i++) {
        // //
        // // }
        // // }
        //
        // int[][] residues = new int[2][];
        // for (int i = 0; i < 2; i++) {
        // residues[i] = new int[residuesInts.get(i).size()];
        // int j = 0;
        // for (int r : residuesInts.get(i))
        // residues[i][j++] = r;
        // }
        // assert residues[0].length == residues[1].length;
        //
        // int compactGroupsCount = 1;
        // boolean[] compactness = new boolean[residues[0].length];
        // for (int i = 1; i < residues[0].length; i++)
        // if ((residues[0][i] - residues[0][i - 1] == 1)
        // && (residues[1][i] - residues[1][i - 1] == 1))
        // compactness[i] = true;
        // else
        // compactGroupsCount++;
        //
        // Group[][][] compactGroups = new Group[2][][];
        // compactGroups[0] = new Group[compactGroupsCount][];
        // compactGroups[1] = new Group[compactGroupsCount][];
        //
        // int[] starts = new int[] { residues[0][0], residues[1][0] };
        // int i = 0;
        // for (int j = 1; j < compactness.length; j++)
        // if (!compactness[j]) {
        // for (int k = 0; k < 2; k++)
        // compactGroups[k][i] = getResidues(chains[k], starts[k],
        // residues[k][j - 1]);
        // for (int k = 0; k < 2; k++)
        // starts[k] = residues[k][j];
        // i++;
        // }
        // for (int k = 0; k < 2; k++)
        // compactGroups[k][i] = getResidues(chains[k], starts[k],
        // residues[k][compactness.length - 1]);
        // assert ++i == compactGroupsCount;
        //
        // AlignmentOutput output = new AlignmentOutput(chains, compactGroups);
        // PdbManager.putAlignmentToCache(input, output);
        // return output;
    }

    // private static Group[] getResidues(Chain chain, int begin, int end)
    // throws StructureException {
    // ResidueNumber beginIndex = new ResidueNumber(null, begin, null);
    // ResidueNumber endIndex = new ResidueNumber(null, end, null);
    // return chain.getGroupsByPDB(beginIndex, endIndex);
    // }

    @SuppressWarnings("unused")
    public static Structure[] align(Structure s1, Structure s2)
            throws StructureException {
        // TODO
        return null;
        // StructureAligner.LOGGER.info("Aligning the following structures: "
        // + s1.getPDBCode() + " and " + s2.getPDBCode());
        // Set<String> c1 = new TreeSet<>();
        // Set<String> c2 = new TreeSet<>();
        // for (Chain c : s1.getChains())
        // c1.add(c.getChainID());
        // for (Chain c : s2.getChains())
        // c2.add(c.getChainID());
        // c1.retainAll(c2);
        //
        // if (StructureAligner.LOGGER.isDebugEnabled()) {
        // StringBuilder builder = new StringBuilder();
        // for (String chainName : c1) {
        // builder.append(chainName);
        // builder.append(' ');
        // }
        // StructureAligner.LOGGER
        // .debug("The following chain names are common for both "
        // + "structures: " + builder.toString());
        // }
        //
        // AlignmentOutput[] output = new AlignmentOutput[c1.size()];
        // int i = 0;
        // for (String id : c1) {
        // output[i++] = StructureAligner.align(s1.getChainByPDB(id),
        // s2.getChainByPDB(id));
        // StructureAligner.LOGGER.trace("Aligned chain: " + id);
        // }
        //
        // Structure[] structures = new Structure[] { s1.clone(), s2.clone(),
        // s1.clone(), s2.clone() };
        // for (i = 0; i < 2; i++) {
        // Vector<Chain> vector = new Vector<>();
        // for (AlignmentOutput chains : output)
        // vector.add(chains.getAllAtomsChains()[i]);
        // structures[i].setChains(vector);
        // }
        // for (i = 0; i < 2; i++) {
        // Vector<Chain> vector = new Vector<>();
        // for (AlignmentOutput chains : output)
        // vector.add(chains.getFilteredChains()[i]);
        // structures[i + 2].setChains(vector);
        // }
        // return structures;
    }

    // private static List<Group> filterGroups(Chain c1, String[] indices) {
    // Set<Integer> set = new HashSet<>();
    // for (String s : indices)
    // set.add(Integer.valueOf(s.split(":")[0]));
    //
    // List<Group> list = new Vector<>();
    // for (Group g : c1.getAtomGroups())
    // if (set.contains(g.getResidueNumber().getSeqNum()))
    // list.add(g);
    // return list;
    // }
}
