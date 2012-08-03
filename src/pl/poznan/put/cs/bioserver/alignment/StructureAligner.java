package pl.poznan.put.cs.bioserver.alignment;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.StructureImpl;
import org.biojava.bio.structure.align.StrucAligParameters;
import org.biojava.bio.structure.align.StructurePairAligner;
import org.biojava.bio.structure.align.pairwise.AlternativeAlignment;

import pl.poznan.put.cs.bioserver.helper.Helper;

public class StructureAligner {
    public static Structure[] align(Structure s1, Structure s2)
            throws StructureException {
        Set<String> c1 = new TreeSet<>();
        Set<String> c2 = new TreeSet<>();
        for (Chain c : s1.getChains())
            c1.add(c.getChainID());
        for (Chain c : s2.getChains())
            c2.add(c.getChainID());
        c1.retainAll(c2);

        Vector<Chain> chains1 = new Vector<>();
        Vector<Chain> chains2 = new Vector<>();
        for (String id : c1) {
            Chain[] aligned = align(s1.getChainByPDB(id), s2.getChainByPDB(id));
            chains1.add(aligned[0]);
            chains2.add(aligned[1]);
        }

        Structure s1c = s1.clone();
        s1c.setChains(chains1);
        Structure s2c = s2.clone();
        s2c.setChains(chains2);
        return new Structure[] { s1c, s2c };
    }

    public static Chain[] align(Chain c1, Chain c2) throws StructureException {
        StructurePairAligner aligner = new StructurePairAligner();
        if (Helper.isNucleicAcid(c1)) {
            StrucAligParameters parameters = new StrucAligParameters();
            parameters.setUsedAtomNames(new String[] { " C1'", " C2 ", " C2'",
                    " C3'", " C4 ", " C4'", " C5 ", " C5'", " C6 ", " N1 ",
                    " N3 ", " O2'", " O3'", " O4'", " O5'", " OP1", " OP2",
                    " P  " });
            aligner.setParams(parameters);
        }

        StructureImpl s1 = new StructureImpl(c1);
        StructureImpl s2 = new StructureImpl(c2);
        aligner.align(s1, s2);
        AlternativeAlignment alignment = aligner.getAlignments()[0];
        Structure structure = alignment.getAlignedStructure(s1, s2);

        c1 = structure.getModel(0).get(0);
        c2 = structure.getModel(1).get(0);
        Chain c3 = (Chain) c1.clone();
        Chain c4 = (Chain) c2.clone();

        // FIXME
        c3.setAtomGroups(filterGroups(c1, alignment.getPDBresnum1()));
        c4.setAtomGroups(filterGroups(c2, alignment.getPDBresnum2()));

        return new Chain[] { c1, c2, c3, c4 };
    }

    private static List<Group> filterGroups(Chain c1, String[] indices) {
        Set<Integer> set = new HashSet<>();
        for (String s : indices)
            set.add(Integer.valueOf(s.split(":")[0]));

        List<Group> list = new Vector<>();
        for (Group g : c1.getAtomGroups()) {
            if (set.contains(g.getResidueNumber().getSeqNum()))
                list.add(g);
        }
        return list;
    }
}
