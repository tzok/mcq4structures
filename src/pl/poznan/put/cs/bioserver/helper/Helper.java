package pl.poznan.put.cs.bioserver.helper;

import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.Structure;

public class Helper {
    public static boolean isNucleicAcid(Chain c) {
        int amino = 0;
        int nucleotide = 0;
        for (Group g : c.getAtomGroups()) {
            String type = g.getType();
            if (type.equals("amino") || g.hasAminoAtoms())
                amino++;
            else if (type.equals("nucleotide") || g.hasAtom("P"))
                nucleotide++;
        }
        return nucleotide > amino;
    }

    public static void normalizeAtomNames(Structure s) {
        for (Chain c : s.getChains())
            normalizeAtomNames(c);
    }

    public static void normalizeAtomNames(Chain c) {
        for (Group g : c.getAtomGroups())
            for (Atom a : g.getAtoms()) {
                a.setName(a.getName().replace('*', '\''));
                a.setFullName(a.getFullName().replace('*', '\''));
            }
    }
}
