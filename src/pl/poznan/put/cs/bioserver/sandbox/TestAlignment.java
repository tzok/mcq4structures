package pl.poznan.put.cs.bioserver.sandbox;

import java.io.FileOutputStream;
import java.io.IOException;

import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.io.PDBFileReader;

import pl.poznan.put.cs.bioserver.alignment.StructureAligner;
import pl.poznan.put.cs.bioserver.helper.Helper;

public class TestAlignment {
    public static void main(String[] args) {
        try {
            PDBFileReader reader = new PDBFileReader();
            Structure s1 = reader
                    .getStructure("/home/tzok/pdb/challenge-3/3_bujnicki_1.pdb");
            Structure s2 = reader
                    .getStructure("/home/tzok/pdb/challenge-3/3OXJ.pdb");

            Helper.normalizeAtomNames(s1);
            Helper.normalizeAtomNames(s2);

            Structure[] aligned = StructureAligner.align(s1, s2);
            System.out.println(aligned[0]);
            System.out.println(aligned[1]);
            System.out.println(aligned[0].getChain(0).getAtomLength());
            System.out.println(aligned[1].getChain(0).getAtomLength());

            try (FileOutputStream stream = new FileOutputStream("/tmp/out0.pdb")) {
                stream.write(aligned[0].toPDB().getBytes());
            }
            try (FileOutputStream stream = new FileOutputStream("/tmp/out1.pdb")) {
                stream.write(aligned[1].toPDB().getBytes());
            }
        } catch (IOException | StructureException e) {
            e.printStackTrace();
        }
    }
}
