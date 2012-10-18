package pl.poznan.put.cs.bioserver.comparison;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.Calc;
import org.biojava.bio.structure.SVDSuperimposer;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.io.PDBFileReader;

import pl.poznan.put.cs.bioserver.helper.Helper;

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
            Structure[] s = new Structure[] { reader.getStructure(args[0]),
                    reader.getStructure(args[1]) };
            RMSD rmsd = new RMSD();
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

    @Override
    public double compare(Structure s1, Structure s2)
            throws IncomparableStructuresException {
        RMSD.logger.debug("Comparing: " + s1.getPDBCode() + " and "
                + s2.getPDBCode());

        try {
            Structure[] structures = new Structure[] { s1.clone(), s2.clone() };
            Atom[][] atoms = Helper.getCommonAtomArray(structures[0],
                    structures[1]);
            RMSD.logger.debug("Atom set size: " + atoms[0].length);

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
