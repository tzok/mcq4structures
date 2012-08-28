package pl.poznan.put.cs.bioserver.torsion;

import org.biojava.bio.structure.Group;

/**
 * Dihedral angles for protein group (amino acid).
 * 
 * @author Tomasz Żok (tzok[at]cs.put.poznan.pl)
 */
public class AminoAcidDihedral extends Dihedral {
    public static final int PHI = 0;
    public static final int PSI = 1;
    public static final int OMEGA = 2;

    public AminoAcidDihedral(double phi, double psi, double omega, Group group) {
        super(group);
        angles = new double[3];
        angles[AminoAcidDihedral.PHI] = phi;
        angles[AminoAcidDihedral.PSI] = psi;
        angles[AminoAcidDihedral.OMEGA] = omega;
    }
}
