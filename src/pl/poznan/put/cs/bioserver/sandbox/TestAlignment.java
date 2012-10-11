package pl.poznan.put.cs.bioserver.sandbox;

import java.io.IOException;
import java.util.List;

import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.io.PDBFileReader;

import pl.poznan.put.cs.bioserver.alignment.AlignmentOutput;
import pl.poznan.put.cs.bioserver.alignment.StructureAligner;
import pl.poznan.put.cs.bioserver.helper.Helper;
import pl.poznan.put.cs.bioserver.torsion.AngleDifference;
import pl.poznan.put.cs.bioserver.torsion.DihedralAngles;
import pl.poznan.put.cs.bioserver.torsion.NucleotideDihedral;

public class TestAlignment {
    public static void main(String[] args) {
        try {
            PDBFileReader reader = new PDBFileReader();
            Structure s1 = reader
                    .getStructure("/home/tzok/pdb/1EHZ.pdb");//challenge-2/2_bujnicki_2.pdb");
            Structure s2 = reader
                    .getStructure("/home/tzok/pdb/1EVV.pdb");//challenge-2/2_bujnicki_3.pdb");

            Helper.normalizeAtomNames(s1);
            Helper.normalizeAtomNames(s2);

            AlignmentOutput output = StructureAligner.align(s1.getChain(0),
                    s2.getChain(0));

            for (NucleotideDihedral.AngleName an : NucleotideDihedral.AngleName
                    .values()) {
                System.out.println(an);
                List<AngleDifference> differences = DihedralAngles
                        .calculateAnglesDifferences(output.getAtoms(),
                                new NucleotideDihedral(an));
                for (AngleDifference d : differences) {
                    System.out.println(d);
                }
            }
        } catch (IOException | StructureException e) {
            e.printStackTrace();
        }
    }
}
