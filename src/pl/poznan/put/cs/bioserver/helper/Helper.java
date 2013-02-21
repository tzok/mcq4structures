package pl.poznan.put.cs.bioserver.helper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.ChainImpl;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.ResidueNumber;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.poznan.put.cs.bioserver.torsion.AminoAcidDihedral;
import pl.poznan.put.cs.bioserver.torsion.NucleotideDihedral;
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
    private static final String[] USED_ATOMS;
    static {
        String[] array1 = NucleotideDihedral.getUsedAtoms();
        String[] array2 = AminoAcidDihedral.getUsedAtoms();
        USED_ATOMS = new String[array1.length + array2.length];
        System.arraycopy(array1, 0, Helper.USED_ATOMS, 0, array1.length);
        System.arraycopy(array2, 0, Helper.USED_ATOMS, array1.length,
                array2.length);
    }

    /**
     * Given two lists of atoms (possibly of different size), make them equal
     * with minimal number of changes (using difflib).
     * 
     * @param atoms
     *            Two lists of atoms.
     * @return Two lists of atoms that have equal size and each i-th atom in
     *         list A corresponds to i-th atom in list B.
     */
    public static Atom[][] equalize(Atom[][] atoms) {
        List<String> l1 = new ArrayList<>();
        List<String> l2 = new ArrayList<>();

        for (Atom a : atoms[0]) {
            l1.add(a.getFullName());
        }
        for (Atom a : atoms[1]) {
            l2.add(a.getFullName());
        }
        // start with the larger list (it's better to remove redundant atoms)
        if (l1.size() > l2.size()) {
            List<String> tmpStr = l1;
            l1 = l2;
            l2 = tmpStr;

            Atom[] tmpAtom = atoms[0];
            atoms[0] = atoms[1];
            atoms[1] = tmpAtom;
        }

        List<Atom> list1 = new ArrayList<>(Arrays.asList(atoms[0]));
        List<Atom> list2 = new ArrayList<>(Arrays.asList(atoms[1]));
        Patch patch = DiffUtils.diff(l1, l2);
        int cumulated1 = 0, cumulated2 = 0;
        for (Delta d : patch.getDeltas()) {
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

        Atom[][] result = new Atom[][] { list1.toArray(new Atom[list1.size()]),
                list2.toArray(new Atom[list2.size()]) };
        Helper.sanityCheck(result);
        return result;
    }

    /**
     * TODO
     * 
     * @param structure
     * @param atomNames
     * @return
     */
    public static List<Atom> getAtomArray(Structure structure,
            String[] atomNames) {
        List<Atom> list = new ArrayList<>();
        for (Chain c : structure.getChains()) {
            list.addAll(Helper.getAtomArray(c.getAtomGroups(), atomNames));
        }
        return list;
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
    public static Atom[][] getCommonAtomArray(Structure s1, Structure s2,
            boolean streamGroups) throws StructureException {
        if (streamGroups) {
            List<Group> g1 = Helper.streamGroups(s1);
            List<Group> g2 = Helper.streamGroups(s2);
            return Helper.getCommonAtomArray(g1, g2);
        }

        List<Chain> c1 = s1.getChains();
        List<Chain> c2 = s2.getChains();
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
                Atom[][] result = new Atom[][] { new Atom[0], new Atom[0] };
                for (int i = 0; i < size; i++) {
                    String id = ids1[i];
                    Atom[][] common = Helper.getCommonAtomArray(
                            s1.getChainByPDB(id), s2.getChainByPDB(id));

                    for (int j = 0; j < 2; j++) {
                        Atom[] tmp = new Atom[common[j].length
                                + result[j].length];
                        System.arraycopy(result[j], 0, tmp, 0, result[j].length);
                        System.arraycopy(common[j], 0, tmp, result[j].length,
                                common[j].length);
                        result[j] = tmp;
                    }
                }
                return result;
            }
        }
        // null means that it is impossible to get common atom array without
        // streaming of the groups
        return null;
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
                Helper.LOGGER.warn("Group is neither amino acid, nor "
                        + "nucleotide: " + g);
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

    /**
     * Normalize the names of atoms i.e. change all asterisks into apostrophes.
     * 
     * @param s
     *            Input structure.
     */
    public static void normalizeAtomNames(Structure s) {
        for (Chain c : s.getChains()) {
            Helper.normalizeAtomNames(c);
        }
    }

    /**
     * Extract atoms of given names from a list of groups.
     * 
     * @param groups
     *            A list of groups (in most cases, a single chain).
     * @param atomNames
     *            An array of names to be accepted.
     * @return A list of atoms.
     */
    private static List<Atom> getAtomArray(List<Group> groups,
            String[] atomNames) {
        List<Atom> list = new ArrayList<>();
        for (Group g : groups) {
            if (!(Helper.isNucleotide(g) || Helper.isAminoAcid(g))) {
                continue;
            }

            for (String name : atomNames) {
                try {
                    Atom atom = g.getAtom(name);
                    list.add(atom);
                } catch (StructureException e) {
                    // do nothing
                }
            }
        }
        return list;
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
    private static Atom[][] getCommonAtomArray(Chain c1, Chain c2) {
        return Helper
                .getCommonAtomArray(c1.getAtomGroups(), c2.getAtomGroups());
    }

    private static Atom[][] getCommonAtomArray(List<Group> g1, List<Group> g2) {
        Helper.normalizeAtomNames(g1);
        Helper.normalizeAtomNames(g2);

        Atom[][] result = new Atom[2][];
        List<Atom> list = Helper.getAtomArray(g1, Helper.USED_ATOMS);
        result[0] = list.toArray(new Atom[list.size()]);
        list = Helper.getAtomArray(g2, Helper.USED_ATOMS);
        result[1] = list.toArray(new Atom[list.size()]);
        return result;
    }

    private static boolean isAminoAcid(Group g) {
        return g.getType().equals("amino") || g.hasAminoAtoms();
    }

    private static boolean isNucleotide(Group g) {
        return g.getType().equals("nucleotide") || g.hasAtom("P");
    }

    /**
     * Change all asterisks (*) in atom names into apostrophes (').
     * 
     * @param c
     *            Input chain.
     */
    private static void normalizeAtomNames(Chain c) {
        Helper.normalizeAtomNames(c.getAtomGroups());
    }

    private static void normalizeAtomNames(List<Group> groups) {
        for (Group g : groups) {
            for (Atom a : g.getAtoms()) {
                a.setName(a.getName().replace('*', '\''));
                a.setFullName(a.getFullName().replace('*', '\''));
            }
        }
    }

    private static void sanityCheck(Atom[][] result) {
        assert result[0].length == result[1].length;
        for (int i = 0; i < result[0].length; i++) {
            assert result[0][i] == null
                    || result[0][i].getFullName().equals(
                            result[1][i].getFullName());
        }
    }

    private static List<Group> streamGroups(Structure s)
            throws StructureException {
        List<String> idsList = new ArrayList<>();
        for (Chain c : s.getChains()) {
            idsList.add(c.getChainID());
        }
        String[] ids = idsList.toArray(new String[idsList.size()]);
        Helper.LOGGER
                .trace("Chain IDs before sorting: " + Arrays.toString(ids));
        Arrays.sort(ids);
        Helper.LOGGER
                .trace("Chain IDs after sorting:  " + Arrays.toString(ids));

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

    public static String getExportPrefix() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
        return sdf.format(new Date());
    }
}
