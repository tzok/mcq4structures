package pl.poznan.put.cs.bioserver.comparison;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.poznan.put.cs.bioserver.alignment.AlignerStructure;
import pl.poznan.put.cs.bioserver.helper.Helper;
import pl.poznan.put.cs.bioserver.helper.StructureManager;
import pl.poznan.put.cs.bioserver.torsion.AminoAcidDihedral;
import pl.poznan.put.cs.bioserver.torsion.AngleDifference;
import pl.poznan.put.cs.bioserver.torsion.AngleType;
import pl.poznan.put.cs.bioserver.torsion.DihedralAngles;
import pl.poznan.put.cs.bioserver.torsion.NucleotideDihedral;

/**
 * Implementation of MCQ global similarity measure based on torsion angle
 * representation.
 * 
 * @author Tomasz Zok (tzok[at]cs.put.poznan.pl)
 */
public class MCQ extends GlobalComparison {
    private static final Logger LOGGER = LoggerFactory.getLogger(MCQ.class);

    public static final List<AngleType> USED_ANGLES;
    public static final List<String> USED_ANGLES_NAMES;
    static {
        USED_ANGLES = new ArrayList<>();
        MCQ.USED_ANGLES.addAll(NucleotideDihedral.getAngles());
        MCQ.USED_ANGLES.addAll(AminoAcidDihedral.getAngles());

        USED_ANGLES_NAMES = new ArrayList<>();
        for (AngleType angleType : MCQ.USED_ANGLES) {
            MCQ.USED_ANGLES_NAMES.add(angleType.getAngleName());
        }
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
    public static double compare(Structure left, Structure right, boolean alignFirst)
            throws StructureException {
        boolean wasAligned = alignFirst;
        Pair<List<Atom>, List<Atom>> atoms;
        if (alignFirst) {
            atoms = AlignerStructure.align(left, right, "").getAtoms();
        } else {
            atoms = Helper.getCommonAtomArray(left, right, false);
            if (atoms == null) {
                atoms = Helper.getCommonAtomArray(left, right, true);
                wasAligned = true;
            }
        }

        assert atoms != null;
        return MCQ.compare(atoms.getLeft(), atoms.getRight(), wasAligned);
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
        List<Structure> list = new ArrayList<>();
        for (String arg : args) {
            try {
                list.add(StructureManager.loadStructure(new File(arg)).get(0));
            } catch (IOException e) {
                System.out.println("ERROR");
                e.printStackTrace();
            }
        }

        MCQ mcq = new MCQ();
        double[][] compare = mcq.compare(list, null);
        System.out.println("OK");
        for (int i = 0; i < compare.length; i++) {
            for (int j = i + 1; j < compare.length; j++) {
                System.out.println(compare[i][j]);
            }
        }
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
    private static double compare(List<Atom> left, List<Atom> right, boolean wasAligned) {
        Pair<List<Atom>, List<Atom>> equalized = Helper.equalize(left, right);

        List<AngleDifference> allDiffs = new ArrayList<>();
        for (AngleType at : MCQ.USED_ANGLES) {
            List<AngleDifference> diffs;
            diffs = DihedralAngles.calculateAngleDiff(equalized.getLeft(), equalized.getRight(),
                    at, wasAligned);
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
     * Calculate mean of circular quantities (MCQ) for given set of angle
     * differences.
     * 
     * @param diffs
     *            A collection of angle differences.
     * @return Mean of Circular Quantities (MCQ).
     */
    static double calculate(Iterable<AngleDifference> diffs) {
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
    public double compare(Structure s1, Structure s2) throws IncomparableStructuresException {
        try {
            return MCQ.compare(s1, s2, false);
        } catch (StructureException e) {
            MCQ.LOGGER.error("Failed to compare structures", e);
            throw new IncomparableStructuresException(e);
        }
    }

    @Override
    public String toString() {
        return "MCQ";
    }
}
