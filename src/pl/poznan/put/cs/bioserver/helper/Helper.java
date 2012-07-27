package pl.poznan.put.cs.bioserver.helper;

import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Group;

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
}
