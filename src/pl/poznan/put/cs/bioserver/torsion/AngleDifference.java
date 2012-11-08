package pl.poznan.put.cs.bioserver.torsion;

import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.ResidueNumber;

/**
 * A class representing a difference between two torsion angles.
 * 
 * @author tzok
 */
public class AngleDifference implements Comparable<AngleDifference> {
    private static double rad2deg(double rad) {
        return rad * 180.0 / Math.PI;
    }

    /** First angle value. */
    public double angle1;
    /** Second angle value. */
    public double angle2;
    /** Difference between angles. */
    public double difference;
    /**
     * Information about the residue the angle belongs to (at least for the
     * first angle).
     */
    public ResidueNumber residue;
    /** Name of the angle. */
    public String angleName;

    /**
     * Construct an instance for given two fours of atoms and the name of angle.
     * 
     * @param q1
     *            First quadruplet of atoms.
     * @param q2
     *            Second quadruplet of atoms.
     * @param angleName
     *            The name of the angle.
     */
    public AngleDifference(Atom[] q1, Atom[] q2, String angleName) {
        angle1 = DihedralAngles.calculateDihedral(q1);
        angle2 = DihedralAngles.calculateDihedral(q2);
        difference = DihedralAngles.subtractDihedral(angle1, angle2);
        residue = q1[0].getGroup().getResidueNumber();
        this.angleName = angleName;
    }

    /**
     * Construct an instance of angle difference which is not necessarily
     * defined on fours of atoms. This should only be used for difference based
     * on pseudo-pucker phase (P) or MCQ where the angle value is calculated in
     * specific way.
     * 
     * @param residue
     *            Residue where the angle belongs to.
     * @param angle1
     *            First angle - with this constructor can be Double.NaN.
     * @param angle2
     *            Second angle - with this constructor can be Double.NaN.
     * @param difference
     *            Difference between angles.
     * @param angleName
     *            The name of the angle.
     */
    public AngleDifference(ResidueNumber residue, double angle1, double angle2,
            double difference, String angleName) {
        this.angle1 = angle1;
        this.angle2 = angle2;
        this.difference = difference;
        this.angleName = angleName;
        this.residue = residue;
    }

    /**
     * Implementation of Comparable interface to allow sorting based on chain ID
     * and residue number.
     */
    @Override
    public int compareTo(AngleDifference diff) {
        int chainCompare = residue.getChainId().compareTo(
                diff.residue.getChainId());
        if (chainCompare == 0) {
            return residue.getSeqNum().compareTo(diff.residue.getSeqNum());
        }
        return chainCompare;
    }

    @Override
    public String toString() {
        return String.format("Angle difference: delta(%.2f, %.2f) = %.2f",
                AngleDifference.rad2deg(angle1),
                AngleDifference.rad2deg(angle2),
                AngleDifference.rad2deg(difference));
    }
}
