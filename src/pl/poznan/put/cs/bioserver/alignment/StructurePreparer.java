package pl.poznan.put.cs.bioserver.alignment;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.io.PDBFileReader;
import org.biojava3.alignment.template.SequencePair;
import org.biojava3.core.sequence.compound.NucleotideCompound;
import org.biojava3.core.sequence.template.Compound;
import org.biojava3.core.sequence.template.Sequence;

public class StructurePreparer<C extends Compound> {
    private static final Logger LOGGER = Logger
            .getLogger(StructurePreparer.class);

    public static void main(String[] args) {
        if (args.length != 2) {
            StructurePreparer.LOGGER
                    .error("Incorrect number of arguments provided");
            return;
        }

        PDBFileReader reader = new PDBFileReader();
        try {
            Structure[] s = new Structure[] { reader.getStructure(args[0]),
                    reader.getStructure(args[1]) };

            StructurePreparer<Compound> preparer = new StructurePreparer<>(
                    NucleotideCompound.class);
            preparer.prepareStructures(s[0], s[1]);
        } catch (IOException e) {
            StructurePreparer.LOGGER.error(e);
        }
    }

    private static void mergeAtoms(Group g1) {
        for (Group g : g1.getAltLocs())
            for (Atom a : g.getAtoms())
                g1.addAtom(a);
    }

    private static Chain mergeChains(List<Chain> c1) {
        Chain chain = (Chain) c1.get(0).clone();
        for (int i = 1; i < c1.size(); ++i)
            for (Group g : c1.get(i).getAtomGroups())
                chain.addGroup(g);
        chain.setChainID("A");
        return chain;
    }

    private static void prepareGroups(Group g1, Group g2) {
        StructurePreparer.mergeAtoms(g1);
        StructurePreparer.mergeAtoms(g2);

        Set<String> set = new TreeSet<>();
        for (Group g : new Group[] { g1, g2 })
            for (Atom a : g.getAtoms())
                set.add(a.getName());

        Vector<Atom> v1 = new Vector<>();
        Vector<Atom> v2 = new Vector<>();
        for (String name : set)
            try {
                Atom a1 = g1.getAtom(name);
                Atom a2 = g2.getAtom(name);
                v1.add(a1);
                v2.add(a2);
            } catch (StructureException e) {
                //$FALL-THROUGH$
            }

        g1.setAtoms(v1);
        g2.setAtoms(v2);
    }

    private final Class<?> compound;

    public StructurePreparer(Class<?> clazz) {
        compound = clazz;
    }

    public void prepareAtoms(Structure s1, Structure s2) {
        prepareStructures(s1, s2);

        for (int i = 0; i < s1.size(); ++i) {
            List<Chain> c1 = s1.getChains();
            List<Chain> c2 = s2.getChains();
            for (int j = 0; j < c1.size(); ++j) {
                List<Group> g1 = c1.get(j).getAtomGroups();
                List<Group> g2 = c2.get(j).getAtomGroups();
                for (int k = 0; k < g1.size(); ++k)
                    StructurePreparer.prepareGroups(g1.get(k), g2.get(k));
            }
        }
    }

    private void prepareChains(Chain c1, Chain c2) {
        SequenceAligner<C> aligner = new SequenceAligner<>(compound);
        SequencePair<Sequence<C>, C> pair = aligner.alignSequences(c1, c2);

        List<Group> l1 = aligner.getAtomGroups(c1);
        List<Group> l2 = aligner.getAtomGroups(c2);
        StructurePreparer.LOGGER.debug("Chain sizes before: " + l1.size() + " "
                + l2.size());
        List<Group> newL1 = removeUnaligned(l1, pair, 0);
        List<Group> newL2 = removeUnaligned(l2, pair, 1);
        StructurePreparer.LOGGER.debug("Chain sizes after: " + newL1.size()
                + " " + newL2.size());

        if (l1.size() != newL1.size() || l2.size() != newL2.size())
            StructurePreparer.LOGGER.warn("The chain " + c1.getChainID()
                    + " does not match perfectly");

        c1.setAtomGroups(newL1);
        c2.setAtomGroups(newL2);
    }

    public void prepareStructures(Structure s1, Structure s2) {
        List<Chain> c1 = s1.getChains();
        List<Chain> c2 = s2.getChains();
        if (c1.size() == 0 || c2.size() == 0)
            // FIXME
            StructurePreparer.LOGGER
                    .error("At least one structure is malformed (0 chains)");
        /*
         * Merge all chains into one if required
         */
        if (c1.size() != c2.size()) {
            StructurePreparer.LOGGER
                    .warn("Structures have different number of chains. "
                            + "Will attempt to merge all chains");
            List<Chain> list = new Vector<>();
            list.add(StructurePreparer.mergeChains(c1));
            c1 = list;

            list = new Vector<>();
            list.add(StructurePreparer.mergeChains(c2));
            c2 = list;
        }
        /*
         * Sort chains by their ID
         */
        Comparator<Chain> comparator = new Comparator<Chain>() {
            @Override
            public int compare(Chain o1, Chain o2) {
                return o1.getChainID().compareTo(o2.getChainID());
            }
        };
        Collections.sort(c1, comparator);
        Collections.sort(c2, comparator);
        /*
         * Check if there is a perfect match of chain ID <-> chain ID
         */
        for (int i = 0; i < c1.size(); ++i) {
            if (c1.get(i).getChainID() != c2.get(i).getChainID())
                // FIXME
                StructurePreparer.LOGGER
                        .error("Chains IDs are different in these two structures");
            prepareChains(c1.get(i), c2.get(i));
        }
        /*
         * Fix the structures
         */
        s1.setChains(c1);
        s2.setChains(c2);
    }

    @SuppressWarnings("static-method")
    private List<Group> removeUnaligned(List<Group> list,
            SequencePair<Sequence<C>, C> pair, int index) {

        Vector<Group> newList = new Vector<>();
        char[] a1 = pair.getQuery().getSequenceAsString().toCharArray();
        char[] a2 = pair.getTarget().getSequenceAsString().toCharArray();
        for (int i = 0, j = 0; i < a1.length; ++i) {
            if (a1[i] != '-' && a2[i] != '-')
                newList.add(list.get(j++));
            if (a1[i] == '-' && index == 1 || a2[i] == '-' && index == 0)
                j++;
        }
        return newList;
    }
}
