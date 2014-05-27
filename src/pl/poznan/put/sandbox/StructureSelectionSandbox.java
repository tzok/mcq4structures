package pl.poznan.put.sandbox;

import java.io.IOException;

import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.io.PDBFileReader;

import pl.poznan.put.comparison.IncomparableStructuresException;
import pl.poznan.put.comparison.MCQ;
import pl.poznan.put.comparison.RMSD;
import pl.poznan.put.comparison.RMSD.AtomFilter;
import pl.poznan.put.structure.StructureSelection;
import pl.poznan.put.structure.SelectionFactory;

public class StructureSelectionSandbox {
    public static void main(String[] args) throws IOException,
            IncomparableStructuresException {
        PDBFileReader reader = new PDBFileReader();
        Structure s1 = reader.getStructure("/home/tzok/pdb/1EHZ.pdb.gz");
        Structure s2 = reader.getStructure("/home/tzok/pdb/1EVV.pdb.gz");

        StructureSelection sel1 = SelectionFactory.create("sel1", s1);
        StructureSelection sel2 = SelectionFactory.create("sel2", s2);

        MCQ mcq = new MCQ(MCQ.getAllAvailableTorsionAngles());
        System.out.println(mcq.compareGlobally(sel1, sel2).toDisplayString());

        RMSD rmsd = new RMSD(AtomFilter.ALL, false);
        System.out.println(rmsd.compareGlobally(sel1, sel2).toDisplayString());

        rmsd.setFilter(AtomFilter.BACKBONE);
        System.out.println(rmsd.compareGlobally(sel1, sel2).toDisplayString());

        rmsd.setFilter(AtomFilter.MAIN);
        System.out.println(rmsd.compareGlobally(sel1, sel2).toDisplayString());
    }
}