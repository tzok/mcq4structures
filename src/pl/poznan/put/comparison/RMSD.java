package pl.poznan.put.comparison;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.Calc;
import org.biojava.bio.structure.SVDSuperimposer;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.poznan.put.alignment.AlignerStructure;
import pl.poznan.put.alignment.AlignmentOutput;
import pl.poznan.put.helper.Helper;
import pl.poznan.put.helper.InvalidInputException;
import pl.poznan.put.helper.StructureManager;

/**
 * Implementation of RMSD global similarity measure.
 * 
 * @author Tomasz Zok (tzok[at]cs.put.poznan.pl)
 */
public class RMSD extends GlobalComparison {
    public static final String NAME = "RMSD";

    private static final Logger LOGGER = LoggerFactory.getLogger(RMSD.class);

    /**
     * A command line wrapper to calculate RMSD for given structures. It outputs
     * the upper half of the dissimilarity matrix. For example, for 4 structures
     * the output will like this:
     * 
     * OK 1-vs-2 1-vs-3 1-vs-4 2-vs-3 2-vs-4 3-vs-4
     * 
     * @param args
     *            A list of paths to PDB files.
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("ERROR");
            System.out.println("Incorrect number of arguments provided");
            return;
        }
        List<Structure> list = new ArrayList<>();
        for (String arg : args) {
            try {
                list.add(StructureManager.loadStructure(new File(arg)).get(0));
            } catch (IOException | InvalidInputException e) {
                System.out.println("ERROR");
                e.printStackTrace();
            }
        }

        RMSD rmsd = new RMSD();
        double[][] compare = rmsd.compare(list, null);
        System.out.println("OK");
        for (double[] element : compare) {
            System.out.println(Arrays.toString(element));
        }
    }

    /**
     * Compare two given structures. By default, do not try to align based on
     * atoms, but if impossible to compare then try the alignment.
     * 
     * @param s1
     *            First structure.
     * @param s2
     *            Second structure.
     * @return RMSD.
     */
    @Override
    public double compare(Structure s1, Structure s2)
            throws IncomparableStructuresException {
        RMSD.LOGGER.debug("Comparing: " + s1.getPDBCode() + " and "
                + s2.getPDBCode());

        if (Helper.isNucleicAcid(s1) != Helper.isNucleicAcid(s2)) {
            return Double.NaN;
        }

        try {
            Structure[] structures = new Structure[] { s1.clone(), s2.clone() };
            Pair<List<Atom>, List<Atom>> atoms =
                    Helper.getCommonAtomArray(structures[0], structures[1],
                            false);
            if (atoms == null) {
                atoms =
                        Helper.getCommonAtomArray(structures[0], structures[1],
                                true);
            }
            assert atoms != null;

            List<Atom> left = atoms.getLeft();
            List<Atom> right = atoms.getRight();

            if (left.size() != right.size()) {
                RMSD.LOGGER.info("Atom sets have different sizes. Must use "
                        + "alignment before calculating RMSD");
                AlignmentOutput output =
                        AlignerStructure.align(structures[0], structures[1], "");
                return output.getAFPChain().getTotalRmsdOpt();
            }
            RMSD.LOGGER.debug("Atom set size: " + left.size());

            Atom[] leftArray = left.toArray(new Atom[left.size()]);
            Atom[] rightArray = right.toArray(new Atom[right.size()]);
            SVDSuperimposer superimposer =
                    new SVDSuperimposer(leftArray, rightArray);
            Calc.rotate(structures[1], superimposer.getRotation());
            Calc.shift(structures[1], superimposer.getTranslation());
            return SVDSuperimposer.getRMS(leftArray, rightArray);
        } catch (StructureException e) {
            RMSD.LOGGER.error("Failed to compare structures", e);
            throw new IncomparableStructuresException(e);
        }
    }

    @Override
    public String toString() {
        return RMSD.NAME;
    }
}
