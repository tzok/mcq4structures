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
    private double angle1;
    /** Second angle value. */
    private double angle2;
    /** Difference between angles. */
    private double difference;
    /**
     * Information about the residue the angle belongs to (at least for the
     * first angle).
     */
    private ResidueNumber residue;
    /** Name of the angle. */
    private String angleName;

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
    AngleDifference(Atom[] q1, Atom[] q2, String angleName) {
        angle1 = DihedralAngles.calculateDihedral(q1);
        angle2 = DihedralAngles.calculateDihedral(q2);
        difference = DihedralAngles.subtractDihedral(angle1, angle2);
        residue = q1[1].getGroup().getResidueNumber();
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
        if (equals(diff)) {
            return 0;
        }

        String chainId1 = residue.getChainId();
        String chainId2 = diff.residue.getChainId();

        if (chainId1.equals(chainId2)) {
            return residue.getSeqNum().compareTo(diff.residue.getSeqNum());
        }
        return chainId1.compareTo(chainId2);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AngleDifference other = (AngleDifference) obj;
        if (Double.doubleToLongBits(angle1) != Double
                .doubleToLongBits(other.angle1)) {
            return false;
        }
        if (Double.doubleToLongBits(angle2) != Double
                .doubleToLongBits(other.angle2)) {
            return false;
        }
        if (angleName == null) {
            if (other.angleName != null) {
                return false;
            }
        } else if (!angleName.equals(other.angleName)) {
            return false;
        }
        if (Double.doubleToLongBits(difference) != Double
                .doubleToLongBits(other.difference)) {
            return false;
        }
        if (residue == null) {
            if (other.residue != null) {
                return false;
            }
        } else if (!residue.equals(other.residue)) {
            return false;
        }
        return true;
    }

    public double getAngleFirst() {
        return angle1;
    }

    public String getAngleName() {
        return angleName;
    }

    public double getAngleSecond() {
        return angle2;
    }

    public double getDifference() {
        return difference;
    }

    public ResidueNumber getResidue() {
        return residue;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(angle1);
        result = prime * result + (int) (temp ^ temp >>> 32);
        temp = Double.doubleToLongBits(angle2);
        result = prime * result + (int) (temp ^ temp >>> 32);
        result = prime * result
                + (angleName == null ? 0 : angleName.hashCode());
        temp = Double.doubleToLongBits(difference);
        result = prime * result + (int) (temp ^ temp >>> 32);
        result = prime * result + (residue == null ? 0 : residue.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return String.format("Angle difference: delta(%.2f, %.2f) = %.2f",
                AngleDifference.rad2deg(angle1),
                AngleDifference.rad2deg(angle2),
                AngleDifference.rad2deg(difference));
    }
}
