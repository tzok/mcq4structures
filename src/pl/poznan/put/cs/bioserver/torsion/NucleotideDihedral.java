package pl.poznan.put.cs.bioserver.torsion;

import org.biojava.bio.structure.Group;

/**
 * Dihedral angles for nucleic acid group (nucleotide).
 * 
 * @author Tomasz Żok (tzok[at]cs.put.poznan.pl)
 */
public class NucleotideDihedral extends Dihedral {
    public static final int ALPHA = 0;
    public static final int BETA = 1;
    public static final int GAMMA = 2;
    public static final int DELTA = 3;
    public static final int EPSILON = 4;
    public static final int ZETA = 5;
    public static final int CHI = 6;
    public static final int P = 7;

    public NucleotideDihedral(double alpha, double beta, double gamma,
            double delta, double epsilon, double zeta, double chi, double p,
            Group group) {
        super(group);
        angles = new double[8];
        angles[NucleotideDihedral.ALPHA] = alpha;
        angles[NucleotideDihedral.BETA] = beta;
        angles[NucleotideDihedral.GAMMA] = gamma;
        angles[NucleotideDihedral.DELTA] = delta;
        angles[NucleotideDihedral.EPSILON] = epsilon;
        angles[NucleotideDihedral.ZETA] = zeta;
        angles[NucleotideDihedral.CHI] = chi;
        angles[NucleotideDihedral.P] = p;
    }
}
