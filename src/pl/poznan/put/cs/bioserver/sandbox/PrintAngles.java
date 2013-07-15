package pl.poznan.put.cs.bioserver.sandbox;

import java.io.IOException;
import java.util.List;

import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.io.PDBFileReader;

import pl.poznan.put.cs.bioserver.helper.Helper;
import pl.poznan.put.cs.bioserver.helper.UniTypeQuadruplet;
import pl.poznan.put.cs.bioserver.torsion.AngleType;
import pl.poznan.put.cs.bioserver.torsion.DihedralAngles;
import pl.poznan.put.cs.bioserver.torsion.NucleotideDihedral;
import pl.poznan.put.cs.bioserver.torsion.Quadruplet;

public class PrintAngles {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: PrintAngles <PDB>");
            return;
        }

        try {
            Structure structure = new PDBFileReader().getStructure(args[0]);
            Helper.normalizeAtomNames(structure);
            List<Atom> atomArray =
                    Helper.getAtomArray(structure,
                            NucleotideDihedral.getUsedAtoms());
            for (AngleType type : NucleotideDihedral.getAngles()) {
                for (Quadruplet quadruplet : DihedralAngles.getQuadruplets(
                        atomArray, type)) {
                    UniTypeQuadruplet<Atom> atoms = quadruplet.getAtoms();
                    double dihedral = DihedralAngles.calculateDihedral(atoms);
                    System.out.println(type.getAngleName() + " "
                            + atoms.b.getGroup().getResidueNumber() + " "
                            + dihedral + " " + Math.toDegrees(dihedral));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
