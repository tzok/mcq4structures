package pl.poznan.put.cs.bioserver.comparison;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
        AngleType[] array1 = NucleotideDihedral.ANGLES;
        int array2 = AminoAcidDihedral.ANGLES.length;
        USED_ANGLES = new AngleType[array1.length + array2];
        System.arraycopy(array1, 0, USED_ANGLES, 0, array1.length);
        System.arraycopy(AminoAcidDihedral.ANGLES, 0, USED_ANGLES,
                array1.length, array2);
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("ERROR");
            System.out.println("Incorrect number of arguments provided");
            return;
        }
        PDBFileReader reader = new PDBFileReader();
        try {
            List<Structure> list = new ArrayList<>();
            for (int i = 0; i < args.length; i++) {
                list.add(reader.getStructure(args[i]));
            }

            MCQ mcq = new MCQ();
            double[][] compare = mcq.compare(list.toArray(new Structure[list
                    .size()]));
            System.out.println("OK");
            for (int i = 0; i < compare.length; i++) {
                System.out.println(Arrays.toString(compare[i]));
            }
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
        try {
            return MCQ.compare(s1, s2, false);
        } catch (StructureException e) {
            LOGGER.error(e, e);
            throw new IncomparableStructuresException(e);
        }
    }

    public static double compare(Structure s1, Structure s2, boolean alignFirst)
            throws StructureException {
        Atom[][] atoms;
        if (alignFirst) {
            atoms = StructureAligner.align(s1, s2).getAtoms();
        } else {
            atoms = Helper.getCommonAtomArray(s1, s2);
            // if (atoms[0].length != atoms[1].length) {
            // LOGGER.info("Atom sets have different sizes. Must use "
            // + "alignment before calculating MCQ");
            // AlignmentOutput output = StructureAligner.align(s1, s2);
            // atoms = output.getAtoms();
            // }
        }
        return compare(atoms);
    }

    public static double compare(Chain c1, Chain c2, boolean alignFirst)
            throws StructureException {
        Atom[][] atoms;
        if (alignFirst) {
            atoms = StructureAligner.align(c1, c2).getAtoms();
        } else {
            atoms = Helper.getCommonAtomArray(c1, c2);
            // if (atoms[0].length != atoms[1].length) {
            // LOGGER.info("Atom sets have different sizes. Must use "
            // + "alignment before calculating MCQ");
            // AlignmentOutput output = StructureAligner.align(c1, c2);
            // atoms = output.getAtoms();
            // }
        }
        return compare(atoms);
    }

    private static double compare(Atom[][] atoms) {
        Atom[][] equalized = Helper.equalize(atoms);

        List<AngleDifference> allDiffs = new ArrayList<>();
        for (AngleType at : USED_ANGLES) {
            List<AngleDifference> diffs;
            diffs = DihedralAngles.calculateAngleDiff(equalized, at);
            allDiffs.addAll(diffs);
        }
        if (LOGGER.isTraceEnabled()) {
            StringBuilder builder = new StringBuilder("All differences:\n");
            for (AngleDifference ad : allDiffs) {
                builder.append(ad);
                builder.append('\n');
            }
            LOGGER.trace(builder.toString());
        }
        return MCQ.calculate(allDiffs);
    }

    public static double calculate(Iterable<AngleDifference> diffs) {
        int counter = 0;
        double sines = 0.0;
        double cosines = 0.0;
        for (AngleDifference ad : diffs) {
            sines += Math.sin(ad.difference);
            cosines += Math.cos(ad.difference);
            counter++;
        }
        return Math.atan2(sines / counter, cosines / counter);
    }
}
