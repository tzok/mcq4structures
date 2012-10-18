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

import pl.poznan.put.cs.bioserver.alignment.AlignmentOutput;
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
            MCQ mcq = new MCQ();
            double result = mcq.compare(s[0], s[1]);
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
        try {
            return MCQ.compare(s1, s2, false);
        } catch (StructureException e) {
            LOGGER.error(e);
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
            if (atoms[0].length != atoms[1].length) {
                LOGGER.info("Atom sets have different sizes. Must use alignment before calculating MCQ");
                AlignmentOutput output = StructureAligner.align(s1, s2);
                atoms = output.getAtoms();
            }
        }
        AngleType[] angles = Helper.isNucleicAcid(s1) ? NucleotideDihedral.ANGLES
                : AminoAcidDihedral.ANGLES;
        return compare(atoms, angles);
    }

    public static double compare(Chain c1, Chain c2, boolean alignFirst)
            throws StructureException {
        Atom[][] atoms;
        if (alignFirst) {
            atoms = StructureAligner.align(c1, c2).getAtoms();
        } else {
            atoms = Helper.getCommonAtomArray(c1, c2);
            if (atoms[0].length != atoms[1].length) {
                LOGGER.info("Atom sets have different sizes. Must use alignment before calculating MCQ");
                AlignmentOutput output = StructureAligner.align(c1, c2);
                atoms = output.getAtoms();
            }
        }
        AngleType[] angles = Helper.isNucleicAcid(c1) ? NucleotideDihedral.ANGLES
                : AminoAcidDihedral.ANGLES;
        return compare(atoms, angles);
    }

    private static double compare(Atom[][] atoms, AngleType[] angles) {
        assert atoms[0].length == atoms[1].length;
        List<AngleDifference> allDiffs = new ArrayList<>();
        for (AngleType at : angles) {
            List<AngleDifference> diffs = DihedralAngles.calculateAngleDiff(
                    atoms, at);
            allDiffs.addAll(diffs);
        }
        return MCQ.calculate(allDiffs);
    }

    public static double calculate(List<AngleDifference> diffs) {
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
