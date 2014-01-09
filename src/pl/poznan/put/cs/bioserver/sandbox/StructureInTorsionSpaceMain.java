package pl.poznan.put.cs.bioserver.sandbox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.io.PDBFileReader;

import pl.poznan.put.cs.bioserver.torsion.StructureInTorsionSpace;

public class StructureInTorsionSpaceMain {
    public static void main(String[] args) throws IOException {
        PDBFileReader reader = new PDBFileReader();
        Structure structure = reader.getStructure("/home/tzok/pdb/1EHZ.pdb.gz");

        List<Group> residues = new ArrayList<>();

        for (Chain c : structure.getChains()) {
            residues.addAll(c.getAtomGroups());
        }

        StructureInTorsionSpace torsionSpace = new StructureInTorsionSpace(
                residues);
        System.out.println(torsionSpace.toString());
    }
}
