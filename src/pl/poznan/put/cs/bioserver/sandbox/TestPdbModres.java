package pl.poznan.put.cs.bioserver.sandbox;

import java.io.IOException;

import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.io.PDBFileReader;

@SuppressWarnings("javadoc")
public class TestPdbModres {
    public static void main(String[] args) {
        try {
            PDBFileReader reader = new PDBFileReader();
            Structure s1 = reader.getStructure("/home/tzok/pdb/1EHZ.pdb");
            for (Chain c : s1.getChains()) {
                for (Group g : c.getAtomGroups()) {
                    String pdbName = g.getPDBName();
                    System.out.println(pdbName.charAt(pdbName.length() - 1));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
