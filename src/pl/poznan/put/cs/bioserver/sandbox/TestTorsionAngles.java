package pl.poznan.put.cs.bioserver.sandbox;

import java.io.IOException;

import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.io.PDBFileReader;

import pl.poznan.put.cs.bioserver.comparison.MCQ;
import pl.poznan.put.cs.bioserver.helper.Helper;

public class TestTorsionAngles {
    public static void main(String[] args) {
        try {
            PDBFileReader reader = new PDBFileReader();
            Structure s1 = reader.getStructure("/home/tzok/pdb/1EHZ.pdb");
            Structure s2 = reader.getStructure("/home/tzok/pdb/1EVV.pdb");

            Helper.normalizeAtomNames(s1);
            Helper.normalizeAtomNames(s2);

            System.out.println(MCQ.compare(s1, s2, false));
            System.out.println("----------------------------");
            System.out.println(MCQ.compare(s1, s2, true));
        } catch (IOException | StructureException e) {
            e.printStackTrace();
        }

    }
}
