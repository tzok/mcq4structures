package pl.poznan.put.cs.bioserver.torsion;

import org.biojava.bio.structure.Group;

/**
 * Generic dihedral angle class.
 * 
 * @author Tomasz Żok (tzok[at]cs.put.poznan.pl)
 */
public abstract class Dihedral {
    /**
     * Subtract two angles (circular values) and return the difference.
     * 
     * @param a1
     *            First angle.
     * @param a2
     *            Second angle.
     * @return Difference between angles.
     */
    public static double subtract(double a1, double a2) {
        double diff;
        // both angles are NaN, reward!
        if (Double.isNaN(a1) && Double.isNaN(a2)) {
            diff = 0;
        } else if (Double.isNaN(a1) && !Double.isNaN(a2) || !Double.isNaN(a1)
                && Double.isNaN(a2)) {
            diff = Math.PI;
        } else {
            double full = 2 * Math.PI;
            double a1_mod = (a1 + full) % full;
            double a2_mod = (a2 + full) % full;
            diff = Math.abs(a1_mod - a2_mod);
            diff = Math.min(diff, full - diff);
        }
        return diff;
    }

    public Group group;

    public double[] angles;

    public Dihedral(Group g) {
        group = g;
    }
}
