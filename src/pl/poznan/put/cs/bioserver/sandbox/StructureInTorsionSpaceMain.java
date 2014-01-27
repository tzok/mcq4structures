package pl.poznan.put.cs.bioserver.sandbox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.io.PDBFileReader;

import pl.poznan.put.cs.bioserver.helper.InvalidInputException;
import pl.poznan.put.cs.bioserver.torsion.McqResult;
import pl.poznan.put.cs.bioserver.torsion.StructureInTorsionAngleSpace;

public class StructureInTorsionSpaceMain {
    public static void main(String[] args) throws IOException,
            InvalidInputException {
        PDBFileReader reader = new PDBFileReader();

        Structure structure = reader.getStructure("/home/tzok/pdb/1EHZ.pdb.gz");
        List<Group> residues = new ArrayList<>();

        for (Chain c : structure.getChains()) {
            residues.addAll(c.getAtomGroups());
        }

        StructureInTorsionAngleSpace ts1 = new StructureInTorsionAngleSpace(
                residues);

        structure = reader.getStructure("/home/tzok/pdb/1EVV.pdb.gz");
        residues = new ArrayList<>();

        for (Chain c : structure.getChains()) {
            residues.addAll(c.getAtomGroups());
        }

        StructureInTorsionAngleSpace ts2 = new StructureInTorsionAngleSpace(
                residues);

        McqResult result = ts1.compareGlobally(ts2);
        System.out.println(result);

        for (McqResult mr : ts1.compareLocally(ts2)) {
            System.out.println(mr);
        }
    }
}
