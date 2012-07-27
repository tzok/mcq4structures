package pl.poznan.put.cs.bioserver.sandbox;

import java.io.FileOutputStream;
import java.io.IOException;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.Atom;
//import java.util.List;
//
//import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.StructureTools;
import org.biojava.bio.structure.align.StructureAlignment;
import org.biojava.bio.structure.align.StructureAlignmentFactory;
import org.biojava.bio.structure.align.StructurePairAligner;
import org.biojava.bio.structure.align.gui.StructureAlignmentDisplay;
import org.biojava.bio.structure.align.model.AFPChain;
//import org.biojava.bio.structure.align.StrucAligParameters;
//import org.biojava.bio.structure.align.StructurePairAligner;
//import org.biojava.bio.structure.align.pairwise.AlternativeAlignment;
import org.biojava.bio.structure.io.PDBFileReader;

import pl.poznan.put.cs.bioserver.alignment.StructureAligner;

public class TestAlignment {

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            PDBFileReader reader = new PDBFileReader();
            Structure s1 = reader
                    .getStructure("/home/tzok/pdb/challenge-3/3_bujnicki_1.pdb");
            Structure s2 = reader
                    .getStructure("/home/tzok/pdb/challenge-3/3OXJ.pdb");
            
            
            StructureTools.getAtomArray(s1, new String[] { "P", "C1'", "C2'",
                    "C3'", "C4'", "C5'", "O2'", "O3'", "O4'", "O5'", });

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

            //
            // int count = 0;
            // for (Group g : aligned[0].getChain(0).getAtomGroups())
            // count += g.getAtoms().size();
            // System.out.println(count);
            //
            // count = 0;
            // for (Group g : aligned[1].getChain(0).getAtomGroups())
            // count += g.getAtoms().size();
            // System.out.println(count);

            // Chain c1 = s1.findChain("A");
            // Chain c2 = s2.findChain("A");
            // System.out.println(c1.getAtomGroups().size() + " "
            // + c1.getAtomLength());
            // System.out.println(c2.getAtomGroups().size() + " "
            // + c2.getAtomLength());
            //
            // StrucAligParameters parameters = new StrucAligParameters();
            // parameters.setUsedAtomNames(new String[] { "P" });
            //
            // StructurePairAligner aligner = new StructurePairAligner();
            // aligner.setParams(parameters);
            // aligner.align(s1, s2, parameters);
            // AlternativeAlignment[] alignments = aligner.getAlignments();
            // // int i = 0;
            // for (AlternativeAlignment aa : alignments) {
            // System.out.println(aa);
            //
            // Structure str = aa.getAlignedStructure(s1, s2);
            // for (int j = 0; j < 2; j++) {
            // List<Chain> model = str.getModel(0);
            // Chain chain = model.get(0);
            // System.out.println(chain.getAtomGroups().size() + " "
            // + chain.getAtomLength());
            // }
            // }
        } catch (IOException | StructureException e) {
            e.printStackTrace();
        }
    }
}
