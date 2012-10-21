package pl.poznan.put.cs.bioserver.torsion;

import org.biojava.bio.structure.Atom;

public class AngleDifference {
    public Atom[] quad1;
    public Atom[] quad2;
    public double angle1;
    public double angle2;
    public double difference;

    public AngleDifference(Atom[] q1, Atom[] q2) {
        this.quad1 = q1;
        this.quad2 = q2;

        angle1 = DihedralAngles.calculateDihedral(q1);
        angle2 = DihedralAngles.calculateDihedral(q2);
        difference = DihedralAngles.subtractDihedral(angle1, angle2);
    }

    // FIXME: używa tylko torsion local comparison, które samo inicjuje
    // difference!
    public AngleDifference(Atom[] q1, Atom[] q2, double angle1, double angle2,
            double difference) {
        this.quad1 = q1;
        this.quad2 = q2;
        this.angle1 = angle1;
        this.angle2 = angle2;
        this.difference = difference;
    }

    @Override
    public String toString() {
        return String.format("Angle difference: delta(%.2f, %.2f) = %.2f",
                rad2deg(angle1), rad2deg(angle2), rad2deg(difference));
    }

    private static double rad2deg(double rad) {
        return rad * 180.0 / Math.PI;
    }
}
