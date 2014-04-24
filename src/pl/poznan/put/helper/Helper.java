package pl.poznan.put.helper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.lang3.tuple.Pair;
import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.ChainImpl;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.ResidueNumber;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.poznan.put.torsion.AminoAcidDihedral;
import pl.poznan.put.torsion.NucleotideDihedral;
import difflib.DeleteDelta;
import difflib.Delta;
import difflib.DiffUtils;
import difflib.InsertDelta;
import difflib.Patch;

/**
 * A utility class covering a set of helpful functions needed in various other
 * codes.
 * 
 * @author tzok
 */
public final class Helper {
    private static final Logger LOGGER = LoggerFactory.getLogger(Helper.class);
    private static final MultiKeyMap<Object, List<Atom>> MAP_GROUPS_ATOMS =
            new MultiKeyMap<>();
    private static final Set<String> SET_NUCLEOTIDE_ATOMS;

    private static final List<String> USED_ATOMS;
    static {
        USED_ATOMS = new ArrayList<>();
        Helper.USED_ATOMS.addAll(NucleotideDihedral.getUsedAtoms());
        Helper.USED_ATOMS.addAll(AminoAcidDihedral.getUsedAtoms());
    }

    static {
        SET_NUCLEOTIDE_ATOMS = new HashSet<>();
        for (String name : new String[] { " C1'", " C2 ", " C2'", " C3'",
                " C4 ", " C4'", " C5 ", " C5'", " C6 ", " N1 ", " N3 ", " O2'",
                " O3'", " O4'", " O5'", " OP1", " OP2", " P  " }) {
            Helper.SET_NUCLEOTIDE_ATOMS.add(name);
        }
    }

    public static int countResidues(Chain value, boolean isRNA) {
        int size = 0;
        for (Group group : value.getAtomGroups()) {
            assert group != null;
            if (isRNA) {
                size += Helper.isNucleotide(group) ? 1 : 0;
            } else {
                size += Helper.isAminoAcid(group) ? 1 : 0;
            }
        }
        return size;
    }

    /**
     * Given two lists of atoms (possibly of different size), make them equal
     * with minimal number of changes (using difflib).
     * 
     * @param pairAtoms
     *            Two lists of atoms.
     * @return Two lists of atoms that have equal size and each i-th atom in
     *         list A corresponds to i-th atom in list B.
     */
    public static Pair<List<Atom>, List<Atom>> equalize(List<Atom> left,
            List<Atom> right) {
        // start with the larger list (it's better to remove redundant atoms)
        if (left.size() < right.size()) {
            List<Atom> tmp = left;
            left = right;
            right = tmp;
        }

        List<String> l = new ArrayList<>();
        List<String> r = new ArrayList<>();

        for (Atom a : left) {
            l.add(a.getFullName());
        }
        for (Atom a : right) {
            r.add(a.getFullName());
        }

        List<Atom> list1 = new ArrayList<>(left);
        List<Atom> list2 = new ArrayList<>(right);

        Patch<String> patch = DiffUtils.diff(l, r);
        int cumulated1 = 0, cumulated2 = 0;
        for (Delta<String> d : patch.getDeltas()) {
            int position = d.getOriginal().getPosition();
            if (d instanceof InsertDelta) {
                int size = d.getRevised().getLines().size();
                for (int i = 0; i < size; i++) {
                    // add null atom
                    list1.add(position + cumulated1, null);
                }
                cumulated1 += size;
            } else if (d instanceof DeleteDelta) {
                int size = d.getOriginal().getLines().size();
                for (int i = 0; i < size; i++) {
                    // remove atom
                    list1.remove(position + cumulated1);
                }
                cumulated1 -= size;
            } else {
                int size = d.getOriginal().getLines().size();
                for (int i = 0; i < size; i++) {
                    // remove from list A
                    list1.remove(position + cumulated1);
                }
                cumulated1 -= size;

                position = d.getRevised().getPosition();
                size = d.getRevised().getLines().size();
                for (int i = 0; i < size; i++) {
                    // remove from list B
                    list2.remove(position + cumulated2);
                }
                cumulated2 -= size;
            }
        }

        Helper.sanityCheck(list1, list2);
        return Pair.of(list1, list2);
    }

    public static String getExportPrefix() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
        return sdf.format(new Date());
    }

    public static Pair<String, List<ResidueNumber>> getSequenceFasta(Chain chain) {
        /*
         * Iterate over the structure and prepare a sequence string in FASTA
         * format
         */
        StringBuilder builder = new StringBuilder();
        List<ResidueNumber> resids = new ArrayList<>();

        for (Group g : chain.getAtomGroups()) {
            String type = g.getType();
            if (type.equals("nucleotide") || type.equals("amino")
                    || g.hasAminoAtoms() || g.hasAtom("P")) {
                String fasta = g.getChemComp().getOne_letter_code();
                if (fasta.equals("?")) {
                    fasta = g.getPDBName();
                    fasta = fasta.substring(fasta.length() - 1, fasta.length());
                }
                builder.append(fasta);
                resids.add(g.getResidueNumber());
            }
        }
        String seqString = builder.toString();
        Helper.LOGGER.trace("Parsed sequence: " + seqString);
        return Pair.of(seqString, resids);
    }

    private static boolean isAminoAcid(Group g) {
        return g.getType().equals("amino") || g.hasAminoAtoms();
    }

    public static boolean isNucleicAcid(Chain c) {
        int amino = 0;
        int nucleotide = 0;
        for (Group g : c.getAtomGroups()) {
            if (Helper.isAminoAcid(g)) {
                amino++;
            } else if (Helper.isNucleotide(g)) {
                nucleotide++;
            } else {
                Helper.LOGGER.warn("Group is neither amino acid, nor nucleotide: "
                        + g);
            }
        }
        return nucleotide > amino;
    }

    public static boolean isNucleicAcid(Structure structure) {
        boolean flag = true;
        for (Chain c : structure.getChains()) {
            flag &= Helper.isNucleicAcid(c);
        }
        return flag;
    }

    private static boolean isNucleotide(Group g) {
        if (g.getType().equals("nucleotide")) {
            return true;
        }

        int count = 0;
        for (Atom a : g.getAtoms()) {
            if (Helper.SET_NUCLEOTIDE_ATOMS.contains(a.getFullName())) {
                count++;
            }
        }
        return count > Helper.SET_NUCLEOTIDE_ATOMS.size() / 2;
    }

    /**
     * Normalize the names of atoms i.e. change all asterisks into apostrophes.
     * 
     * @param s
     *            Input structure.
     */
    public static void normalizeAtomNames(List<Chain> chains) {
        for (Chain c : chains) {
            Helper.normalizeAtomNames(c);
        }
    }

    /**
     * Get atoms needed for torsion angle analysis from both structures. If
     * their chain count or naming differ, then streamline all residues and
     * treat them as a single chain.
     * 
     * @param s1
     *            First structure.
     * @param s2
     *            Second structure.
     * @return Two arrays of atoms.
     * @throws StructureException
     *             If there were problems with resolution of chain names.
     */
    public static Pair<List<Atom>, List<Atom>> getCommonAtomArray(
            Structure left, Structure right, boolean streamGroups)
            throws StructureException {
        if (streamGroups) {
            List<Group> g1 = Helper.streamGroups(left);
            List<Group> g2 = Helper.streamGroups(right);
            return Helper.getCommonAtomArray(g1, g2);
        }

        List<Chain> c1 = left.getChains();
        List<Chain> c2 = right.getChains();
        int size = c1.size();
        if (size == c2.size()) {
            String[] ids1 = new String[size];
            String[] ids2 = new String[size];
            for (int i = 0; i < size; i++) {
                ids1[i] = c1.get(i).getChainID();
                ids2[i] = c2.get(i).getChainID();
            }
            Arrays.sort(ids1);
            Arrays.sort(ids2);

            if (Arrays.equals(ids1, ids2)) {
                List<Atom> resultLeft = new ArrayList<>();
                List<Atom> resultRight = new ArrayList<>();
                for (String id : ids1) {
                    Pair<List<Atom>, List<Atom>> common =
                            Helper.getCommonAtomArray(left.getChainByPDB(id),
                                    right.getChainByPDB(id));
                    resultLeft.addAll(common.getLeft());
                    resultRight.addAll(common.getRight());
                }
                return Pair.of(resultLeft, resultRight);
            }
        }
        // null means that it is impossible to get common atom array without
        // streaming of the groups
        return null;
    }

    /**
     * Get atoms needed for torsion angle analysis from both chains.
     * 
     * @param c1
     *            First chain.
     * @param c2
     *            Second chain.
     * @return Two arrays of atoms.
     */
    private static Pair<List<Atom>, List<Atom>> getCommonAtomArray(Chain left,
            Chain right) {
        return Helper.getCommonAtomArray(left.getAtomGroups(),
                right.getAtomGroups());
    }

    private static Pair<List<Atom>, List<Atom>> getCommonAtomArray(
            List<Group> left, List<Group> right) {
        Helper.removeInsertedResidues(left);
        Helper.removeInsertedResidues(right);

        Helper.normalizeAtomNamesInResidues(left);
        Helper.normalizeAtomNamesInResidues(right);

        List<Atom> leftAtoms =
                Helper.getAtomArrayInResidues(left, Helper.USED_ATOMS);
        List<Atom> rightAtoms =
                Helper.getAtomArrayInResidues(right, Helper.USED_ATOMS);
        return Pair.of(leftAtoms, rightAtoms);
    }

    public static List<Atom> getAtomArray(List<Chain> chains,
            List<String> atomNames) {
        List<Atom> list = new ArrayList<>();
        for (Chain c : chains) {
            list.addAll(Helper.getAtomArrayInResidues(c.getAtomGroups(),
                    atomNames));
        }
        return list;
    }

    private static List<Atom> getAtomArrayInResidues(List<Group> residues,
            List<String> atomNames) {
        if (!Helper.MAP_GROUPS_ATOMS.containsKey(residues, atomNames)) {
            List<Atom> atoms = new ArrayList<>();
            for (Group g : residues) {
                if (!(Helper.isNucleotide(g) || Helper.isAminoAcid(g))) {
                    continue;
                }

                for (String name : atomNames) {
                    try {
                        Atom atom = g.getAtom(name);
                        atoms.add(atom);
                    } catch (StructureException e) {
                        // do nothing
                    }
                }
            }
            // FIXME
            // Helper.MAP_GROUPS_ATOMS.put(residues, atomNames, atoms);
            return atoms;
        }

        return Helper.MAP_GROUPS_ATOMS.get(residues, atomNames);
    }

    private static void normalizeAtomNames(Chain c) {
        Helper.normalizeAtomNamesInResidues(c.getAtomGroups());
    }

    private static void normalizeAtomNamesInResidues(List<Group> residues) {
        for (Group g : residues) {
            for (Atom a : g.getAtoms()) {
                a.setName(a.getName().replace('*', '\''));
                a.setFullName(a.getFullName().replace('*', '\''));
            }
        }
    }

    private static void removeInsertedResidues(List<Group> g) {
        Iterator<Group> iterator = g.iterator();
        while (iterator.hasNext()) {
            Group group = iterator.next();
            if (group.getResidueNumber().getInsCode() != null) {
                Helper.LOGGER.warn("Temporarily removing an inserted residue: "
                        + group);
                iterator.remove();
            }
        }
    }

    private static void sanityCheck(List<Atom> left, List<Atom> right) {
        assert left.size() == right.size();
        for (int i = 0; i < left.size(); i++) {
            assert left.get(i) == null
                    || left.get(i).getFullName().equals(
                            right.get(i).getFullName());
        }
    }

    private static List<Group> streamGroups(Structure s)
            throws StructureException {
        List<String> idsList = new ArrayList<>();
        for (Chain c : s.getChains()) {
            idsList.add(c.getChainID());
        }
        String[] ids = idsList.toArray(new String[idsList.size()]);
        Helper.LOGGER.trace("Chain IDs before sorting: " + Arrays.toString(ids));
        Arrays.sort(ids);
        Helper.LOGGER.trace("Chain IDs after sorting:  " + Arrays.toString(ids));

        List<Group> result = new ArrayList<>();
        for (String chainId : ids) {
            Chain c = s.getChainByPDB(chainId);
            result.addAll(c.getAtomGroups());
        }

        Chain c = new ChainImpl();
        c.setChainID("A");
        for (int i = 0; i < result.size(); i++) {
            Group g = result.get(i);
            g.setChain(c);
            g.setResidueNumber(new ResidueNumber("A", i + 1, null));
        }
        return result;
    }

    private Helper() {
    }
}
