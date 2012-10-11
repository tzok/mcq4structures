package pl.poznan.put.cs.bioserver.comparison;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.io.PDBFileReader;

import pl.poznan.put.cs.bioserver.alignment.AlignmentOutput;
import pl.poznan.put.cs.bioserver.alignment.StructureAligner;
import pl.poznan.put.cs.bioserver.torsion.AngleDifference;
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
        return Double.POSITIVE_INFINITY;
        // FIXME

        // /*
        // * calculate dihedral angles for both structures
        // */
        // DihedralAngles dihedralAngles = new DihedralAngles();
        // Dihedral[][][][] dihedrals = new Dihedral[2][][][];
        // dihedrals[0] = dihedralAngles.getDihedrals(s1);
        // dihedrals[1] = dihedralAngles.getDihedrals(s2);
        // /*
        // * iterate over chains and groups and gather information about
        // * differences on angle values
        // */
        // double[] sum = new double[2];
        // int count = 0;
        // for (int i = 0; i < dihedrals[0].length; ++i)
        // for (int j = 0; j < 2; ++j)
        // for (int k = 0; k < dihedrals[0][i][j].length; ++k) {
        // Dihedral d1 = dihedrals[0][i][j][k];
        // Dihedral d2 = dihedrals[1][i][j][k];
        //
        // for (int l = 0; l < d1.angles.length; ++l) {
        // double a1 = d1.angles[l];
        // double a2 = d2.angles[l];
        // double diff = DihedralAngles.subtract(a1, a2);
        //
        // // formula for MCQ:
        // // MCQ = atan2(sum(sin(diff_i))/N, sum(cos(diff_i)/N))
        // sum[0] += Math.sin(diff);
        // sum[1] += Math.cos(diff);
        // count++;
        // }
        // }
        // return Math.atan2(sum[0] / count, sum[1] / count);
    }

    public double compare(Chain c1, Chain c2)
            throws IncomparableStructuresException {
        AlignmentOutput alignmentOutput;
        try {
            alignmentOutput = StructureAligner.align(c1, c2);
        } catch (StructureException e) {
            LOGGER.error(e);
            throw new IncomparableStructuresException(e);
        }

        return 0;
    }
}
