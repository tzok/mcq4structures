package pl.poznan.put.cs.bioserver.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.ChainImpl;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.ResidueNumber;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;

import pl.poznan.put.cs.bioserver.torsion.AminoAcidDihedral;
import pl.poznan.put.cs.bioserver.torsion.NucleotideDihedral;
import difflib.DeleteDelta;
import difflib.Delta;
import difflib.DiffUtils;
import difflib.InsertDelta;
import difflib.Patch;

public class Helper {
    private static final Logger LOGGER = Logger.getLogger(Helper.class);
    private static final String[] USED_ATOMS;
    static {
        String[] array1 = NucleotideDihedral.USED_ATOMS;
        String[] array2 = AminoAcidDihedral.USED_ATOMS;
        USED_ATOMS = new String[array1.length + array2.length];
        System.arraycopy(array1, 0, USED_ATOMS, 0, array1.length);
        System.arraycopy(array2, 0, USED_ATOMS, array1.length, array2.length);
    }

    public static List<Atom> getAtomArray(List<Group> groups, String[] atomNames) {
        List<Atom> list = new ArrayList<>();
        for (Group g : groups) {
            if (!(Helper.isNucleotide(g) || Helper.isAminoAcid(g)))
                continue;

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

    // public static List<Atom> getAtomArray(List<Group> groups, String[]
    // atomNames) {
    // Map<Group, List<Atom>> map = new HashMap<>();
    // for (Group g : groups) {
    // if (!(Helper.isNucleotide(g) || Helper.isAminoAcid(g)))
    // continue;
    //
    // List<Atom> list = new ArrayList<>();
    // boolean foundAny = false;
    // for (String name : atomNames)
    // try {
    // Atom atom = g.getAtom(name);
    // list.add(atom);
    // foundAny = true;
    // } catch (StructureException e) {
    // list.add(null);
    // }
    // if (foundAny)
    // map.put(g, list);
    // }
    //
    // List<Atom> result = new ArrayList<>();
    // for (Group g : groups)
    // if (map.containsKey(g))
    // result.addAll(map.get(g));
    // return result;
    // }

    public static Atom[][] getCommonAtomArray(Chain c1, Chain c2) {
        return getCommonAtomArray(c1.getAtomGroups(), c2.getAtomGroups());
    }

    // public static Atom[][] getCommonAtomArray(Chain c1, Chain c2,
    // String[] usedAtoms) {
    // return Helper.getCommonAtomArray(c1, c2, usedAtoms, true);
    // }
    //
    // public static Atom[][] getCommonAtomArray(Chain c1, Chain c2,
    // String[] usedAtoms, boolean doEqualize) {
    // Helper.normalizeAtomNames(c1);
    // Helper.normalizeAtomNames(c2);
    //
    // List<Atom> l1 = Helper.getAtomArray(c1, usedAtoms);
    // List<Atom> l2 = Helper.getAtomArray(c2, usedAtoms);
    // Helper.LOGGER.debug("Sizes of common atom arrays: " + l1.size() + " "
    // + l2.size());
    //
    // if (doEqualize && l1.size() != l2.size())
    // Helper.LOGGER
    // .error("Will not equalize, because arrays are of different sizes");
    // if (doEqualize && l1.size() == l2.size())
    // for (int i = 0; i < l1.size();)
    // if (l1.get(i) == null || l2.get(i) == null) {
    // l1.remove(i);
    // l2.remove(i);
    // } else
    // i++;
    // while (l1.remove(null)) {
    // // just remove nulls
    // }
    // while (l2.remove(null)) {
    // // just remove nulls
    // }
    // return new Atom[][] { l1.toArray(new Atom[l1.size()]),
    // l2.toArray(new Atom[l2.size()]) };
    // }

    public static Atom[][] getCommonAtomArray(Structure s1, Structure s2)
            throws StructureException {
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

        List<Group> g1 = Helper.streamGroups(s1);
        List<Group> g2 = Helper.streamGroups(s2);
        return Helper.getCommonAtomArray(g1, g2);
    }

    private static Atom[][] getCommonAtomArray(List<Group> g1, List<Group> g2) {
        Helper.normalizeAtomNames(g1);
        Helper.normalizeAtomNames(g2);

        Atom[][] result = new Atom[2][];
        List<Atom> list = getAtomArray(g1, USED_ATOMS);
        result[0] = list.toArray(new Atom[list.size()]);
        list = getAtomArray(g2, USED_ATOMS);
        result[1] = list.toArray(new Atom[list.size()]);
        return result;
    }

    // public static Atom[][] getCommonAtomArray(Structure s1, Structure s2,
    // boolean doEqualize) throws StructureException {
    // boolean isRNA = Helper.isNucleicAcid(s1);
    // if (isRNA != Helper.isNucleicAcid(s2)) {
    // String message = "Trying to get common atoms from RNA and protein!";
    // Helper.LOGGER.warn(message);
    // throw new StructureException(message);
    // }
    // String[] usedAtoms = isRNA ? NucleotideDihedral.USED_ATOMS
    // : AminoAcidDihedral.USED_ATOMS;
    // return Helper.getCommonAtomArray(s1, s2, usedAtoms, doEqualize, false);
    // }
    //
    // public static Atom[][] getCommonAtomArray(Structure s1, Structure s2,
    // String[] usedAtoms, boolean doEqualize, boolean doStreaming)
    // throws StructureException {
    // if (doStreaming) {
    // List<Group> stream1 = Helper.streamGroups(s1);
    // List<Group> stream2 = Helper.streamGroups(s2);
    //
    // ChainImpl c1 = new ChainImpl();
    // ChainImpl c2 = new ChainImpl();
    // c1.setAtomGroups(stream1);
    // c2.setAtomGroups(stream2);
    // return Helper.getCommonAtomArray(c1, c2, usedAtoms);
    // }
    //
    // List<Chain> chains1 = s1.getChains();
    // List<Chain> chains2 = s2.getChains();
    // if (chains1.size() != chains2.size()
    // || (!Arrays.equals(chains1.toArray(new Chain[chains1.size()]),
    // chains2.toArray(new Chain[chains2.size()])))) {
    // Helper.LOGGER.warn("Chains count of names do not match. Will try "
    // + "to process group-wise. Chains 1: " + chains1
    // + ". Chains 2: " + chains2);
    // return Helper.getCommonAtomArray(s1, s2, usedAtoms, doEqualize,
    // true);
    // }
    //
    // Atom[][] result = new Atom[][] { new Atom[0], new Atom[0] };
    // for (Chain c1 : chains1) {
    // Atom[][] common = Helper.getCommonAtomArray(c1,
    // s2.getChainByPDB(c1.getChainID()), usedAtoms);
    //
    // for (int i = 0; i < 2; i++) {
    // Atom[] tmp = new Atom[common[i].length + result[i].length];
    // System.arraycopy(result[i], 0, tmp, 0, result[i].length);
    // System.arraycopy(common[i], 0, tmp, result[i].length,
    // common[i].length);
    // result[i] = tmp;
    // }
    // }
    // return result;
    // }

    private static void normalizeAtomNames(List<Group> groups) {
        for (Group g : groups)
            for (Atom a : g.getAtoms()) {
                a.setName(a.getName().replace('*', '\''));
                a.setFullName(a.getFullName().replace('*', '\''));
            }
    }

    public static boolean isAminoAcid(Group g) {
        return g.getType().equals("amino") || g.hasAminoAtoms();
    }

    public static boolean isNucleicAcid(Chain c) {
        int amino = 0;
        int nucleotide = 0;
        for (Group g : c.getAtomGroups())
            if (Helper.isAminoAcid(g))
                amino++;
            else if (Helper.isNucleotide(g))
                nucleotide++;
            else
                Helper.LOGGER.warn("Group is neither amino acid, nor "
                        + "nucleotide: " + g);
        return nucleotide > amino;
    }

    public static boolean isNucleicAcid(Structure structure) {
        boolean flag = true;
        for (Chain c : structure.getChains())
            flag &= Helper.isNucleicAcid(c);
        return flag;
    }

    public static boolean isNucleotide(Group g) {
        return g.getType().equals("nucleotide") || g.hasAtom("P");
    }

    public static void normalizeAtomNames(Chain c) {
        Helper.normalizeAtomNames(c.getAtomGroups());
    }

    public static void normalizeAtomNames(Structure s) {
        for (Chain c : s.getChains())
            Helper.normalizeAtomNames(c);
    }

    private static List<Group> streamGroups(Structure s)
            throws StructureException {
        List<String> idsList = new ArrayList<>();
        for (Chain c : s.getChains())
            idsList.add(c.getChainID());
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

    public static Atom[][] equalize(Atom[][] atoms) {
        List<String> l1 = new ArrayList<>();
        List<String> l2 = new ArrayList<>();
        for (Atom a : atoms[0]) {
            l1.add(a.getFullName());
        }
        for (Atom a : atoms[1]) {
            l2.add(a.getFullName());
        }

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
                    list1.add(position + cumulated1, null);
                }
                cumulated1 += size;
            } else if (d instanceof DeleteDelta) {
                int size = d.getOriginal().getLines().size();
                for (int i = 0; i < size; i++) {
                    list1.remove(position + cumulated1);
                }
                cumulated1 -= size;
            } else {
                int size = d.getOriginal().getLines().size();
                for (int i = 0; i < size; i++) {
                    list1.remove(position + cumulated1);
                }
                cumulated1 -= size;

                position = d.getRevised().getPosition();
                size = d.getRevised().getLines().size();
                for (int i = 0; i < size; i++) {
                    list2.remove(position + cumulated2);
                }
                cumulated2 -= size;
            }
        }

        // SANITY CHECK
        // FIXME
        Atom[][] result = new Atom[][] { list1.toArray(new Atom[list1.size()]),
                list2.toArray(new Atom[list2.size()]) };
        assert result[0].length == result[1].length;
        for (int i = 0; i < result[0].length; i++) {
            assert result[0][i] == null
                    || result[0][i].getFullName().equals(
                            result[1][i].getFullName());
        }
        return result;
    }
}
