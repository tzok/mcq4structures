package pl.poznan.put.cs.bioserver.torsion;

import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.ResidueNumber;

public class AngleDifference implements Comparable<AngleDifference> {
    public double angle1;
    public double angle2;
    public double difference;
    public ResidueNumber residue;
    public String angleName;

    public AngleDifference(Atom[] q1, Atom[] q2, String angleName) {
        angle1 = DihedralAngles.calculateDihedral(q1);
        angle2 = DihedralAngles.calculateDihedral(q2);
        difference = DihedralAngles.subtractDihedral(angle1, angle2);
        residue = q1[0].getGroup().getResidueNumber();
        this.angleName = angleName;
    }

    // FIXME: używa tylko torsion local comparison, które samo inicjuje
    // difference!
    public AngleDifference(ResidueNumber residue, double angle1, double angle2,
            double difference, String angleName) {
        this.angle1 = angle1;
        this.angle2 = angle2;
        this.difference = difference;
        this.angleName = angleName;
        this.residue = residue;
    }

    @Override
    public String toString() {
        return String.format("Angle difference: delta(%.2f, %.2f) = %.2f",
                rad2deg(angle1), rad2deg(angle2), rad2deg(difference));
    }

    private static double rad2deg(double rad) {
        return rad * 180.0 / Math.PI;
    }

    @Override
    public int compareTo(AngleDifference diff) {
        int chainCompare = residue.getChainId().compareTo(
                diff.residue.getChainId());
        if (chainCompare == 0) {
            return residue.getSeqNum().compareTo(diff.residue.getSeqNum());
        }
        return chainCompare;
    }
}
