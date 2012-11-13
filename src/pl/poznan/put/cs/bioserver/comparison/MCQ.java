package pl.poznan.put.cs.bioserver.comparison;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.io.PDBFileReader;

import pl.poznan.put.cs.bioserver.alignment.StructureAligner;
import pl.poznan.put.cs.bioserver.helper.Helper;
import pl.poznan.put.cs.bioserver.torsion.AminoAcidDihedral;
import pl.poznan.put.cs.bioserver.torsion.AngleDifference;
import pl.poznan.put.cs.bioserver.torsion.AngleType;
import pl.poznan.put.cs.bioserver.torsion.DihedralAngles;
import pl.poznan.put.cs.bioserver.torsion.NucleotideDihedral;

/**
 * Implementation of MCQ global similarity measure based on torsion angle
 * representation.
 * 
 * @author Tomasz Żok (tzok[at]cs.put.poznan.pl)
 */
public class MCQ extends GlobalComparison {
    private static final Logger LOGGER = Logger.getLogger(MCQ.class);

    private static final AngleType[] USED_ANGLES;
    static {
        AngleType[] array1 = NucleotideDihedral.getAngles();
        AngleType[] array2 = AminoAcidDihedral.getAngles();
        USED_ANGLES = new AngleType[array1.length + array2.length];
        System.arraycopy(array1, 0, MCQ.USED_ANGLES, 0, array1.length);
        System.arraycopy(array2, 0, MCQ.USED_ANGLES, array1.length,
                array2.length);
    }

    /**
     * Calculate mean of circular quantities (MCQ) for given set of angle
     * differences.
     * 
     * @param diffs
     *            A collection of angle differences.
     * @return Mean of Circular Quantities (MCQ).
     */
    public static double calculate(Iterable<AngleDifference> diffs) {
        int counter = 0;
        double sines = 0.0;
        double cosines = 0.0;
        for (AngleDifference ad : diffs) {
            double difference = ad.getDifference();
            sines += Math.sin(difference);
            cosines += Math.cos(difference);
            counter++;
        }
        return Math.atan2(sines / counter, cosines / counter);
    }

    /**
     * Compare two sets of atoms using MCQ.
     * 
     * @param atoms
     *            Two arrays of atoms with corresponding indices.
     * @param wasAligned
     *            True. if atoms were aligned beforehand.
     * @return Mean of Circular Quantities (MCQ) for differences between torsion
     *         angles defined upon the given atoms.
     */
    private static double compare(Atom[][] atoms, boolean wasAligned) {
        Atom[][] equalized = Helper.equalize(atoms);

        List<AngleDifference> allDiffs = new ArrayList<>();
        for (AngleType at : MCQ.USED_ANGLES) {
            List<AngleDifference> diffs;
            diffs = DihedralAngles
                    .calculateAngleDiff(equalized, at, wasAligned);
            allDiffs.addAll(diffs);
        }
        if (MCQ.LOGGER.isTraceEnabled()) {
            StringBuilder builder = new StringBuilder("All differences:\n");
            for (AngleDifference ad : allDiffs) {
                builder.append(ad);
                builder.append('\n');
            }
            MCQ.LOGGER.trace(builder.toString());
        }
        return MCQ.calculate(allDiffs);
    }

    /**
     * Compare two given chains.
     * 
     * @param c1
     *            First chain.
     * @param c2
     *            Second chain.
     * @param alignFirst
     *            Should atoms be aligned first?
     * @return Mean of Circular Quantities (MCQ).
     * @throws StructureException
     *             If the alignment was impossible to make.
     */
    public static double compare(Chain c1, Chain c2, boolean alignFirst)
            throws StructureException {
        Atom[][] atoms;
        if (alignFirst) {
            atoms = StructureAligner.align(c1, c2).getAtoms();
        } else {
            atoms = Helper.getCommonAtomArray(c1, c2);
        }
        return MCQ.compare(atoms, alignFirst);
    }

    /**
     * Compare two given structures.
     * 
     * @param s1
     *            First structure.
     * @param s2
     *            Second structure.
     * @param alignFirst
     *            Should atoms be aligned first?
     * @return Mean of Circular Quantities (MCQ).
     * @throws StructureException
     *             If the alignment was impossible to make.
     */
    public static double compare(Structure s1, Structure s2, boolean alignFirst)
            throws StructureException {
        Atom[][] atoms;
        if (alignFirst) {
            atoms = StructureAligner.align(s1, s2).getAtoms();
        } else {
            atoms = Helper.getCommonAtomArray(s1, s2);
        }
        return MCQ.compare(atoms, alignFirst);
    }

    /**
     * A command line wrapper to calculate MCQ for given structures. It outputs
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
        PDBFileReader reader = new PDBFileReader();
        try {
            List<Structure> list = new ArrayList<>();
            for (String arg : args) {
                list.add(reader.getStructure(arg));
            }

            MCQ mcq = new MCQ();
            double[][] compare = mcq.compare(list.toArray(new Structure[list
                    .size()]));
            System.out.println("OK");
            for (int i = 0; i < compare.length; i++) {
                for (int j = i + 1; j < compare.length; j++) {
                    System.out.println(compare[i][j]);
                }
            }
        } catch (IOException e) {
            System.out.println("ERROR");
            System.out.println(e.getMessage());
        } catch (IncomparableStructuresException e) {
            System.out.println("ERROR");
            System.out.println(e.getMessage());
        }
    }

    /**
     * Compare two given structures. By default, do not try to align based on
     * atoms.
     * 
     * @param s1
     *            First structure.
     * @param s2
     *            Second structure.
     * @return Mean of Circular Quantities (MCQ).
     */
    @Override
    public double compare(Structure s1, Structure s2)
            throws IncomparableStructuresException {
        try {
            return MCQ.compare(s1, s2, false);
        } catch (StructureException e) {
            MCQ.LOGGER.error(e, e);
            throw new IncomparableStructuresException(e);
        }
    }
}
