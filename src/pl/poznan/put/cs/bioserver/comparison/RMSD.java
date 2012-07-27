
package pl.poznan.put.cs.bioserver.comparison;

import org.apache.log4j.Logger;
import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.Calc;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.SVDSuperimposer;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.io.PDBFileReader;
import org.biojava3.core.sequence.compound.NucleotideCompound;

import pl.poznan.put.cs.bioserver.alignment.StructurePreparer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of RMSD global similarity measure.
 * 
 * @author Tomasz Żok (tzok[at]cs.put.poznan.pl)
 */
public class RMSD extends GlobalComparison {
    private static final Logger logger = Logger.getLogger(RMSD.class);

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("ERROR");
            System.out.println("Incorrect number of arguments provided");
            return;
        }
        PDBFileReader reader = new PDBFileReader();
        try {
            Structure[] s = new Structure[] {
                    reader.getStructure(args[0]),
                    reader.getStructure(args[1])
            };
            RMSD rmsd = new RMSD();
            rmsd.checkValidity(s);
            double result = rmsd.compare(s[0], s[1]);
            System.out.println("OK");
            System.out.println(result);
        } catch (IOException e) {
            System.out.println("ERROR");
            System.out.println(e.getMessage());
        } catch (IncomparableStructuresException e) {
            System.out.println("ERROR");
            System.out.println(e.getMessage());
        }
    }

    private static List<Atom> getAllAtoms(Structure structure) {
        List<Atom> list = new ArrayList<Atom>();
        for (Chain c : structure.getChains()) {
            for (Group g : c.getAtomGroups()) {
                for (Group altLoc : g.getAltLocs()) {
                    for (Atom a : altLoc.getAtoms()) {
                        g.addAtom(a);
                    }
                }
                for (Atom a : g.getAtoms()) {
                    list.add(a);
                }
            }
        }
        return list;
    }

    @Override
    public double compare(Structure s1, Structure s2)
            throws IncomparableStructuresException {
        RMSD.logger.debug("Comparing: " + s1.getPDBCode() + " and "
                + s2.getPDBCode());

        Structure[] structures = new Structure[] {
                s1.clone(), s2.clone()
        };
        new StructurePreparer<NucleotideCompound>(NucleotideCompound.class)
                .prepareAtoms(structures[0], structures[1]); // FIXME

        List<Atom> l1 = RMSD.getAllAtoms(structures[0]);
        List<Atom> l2 = RMSD.getAllAtoms(structures[1]);
        Atom[][] atoms = new Atom[][] {
                l1.toArray(new Atom[l1.size()]),
                l2.toArray(new Atom[l2.size()])
        };
        RMSD.logger.debug("Atom sets sizes: " + atoms[0].length + " "
                + atoms[1].length);

        try {
            SVDSuperimposer superimposer = new SVDSuperimposer(atoms[0],
                    atoms[1]);
            Calc.rotate(structures[1], superimposer.getRotation());
            Calc.shift(structures[1], superimposer.getTranslation());
            return SVDSuperimposer.getRMS(atoms[0], atoms[1]);
        } catch (StructureException e) {
            RMSD.logger.error("Failed to compare structures", e);
            throw new IncomparableStructuresException(e.getMessage());
        }
    }
}
