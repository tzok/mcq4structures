package pl.poznan.put.cs.bioserver.torsion;

import java.util.HashMap;
import java.util.Map;

import org.biojava.bio.structure.Group;

/**
 * Dihedral angles for protein group (amino acid).
 * 
 * @author Tomasz Żok (tzok[at]cs.put.poznan.pl)
 */
public class AminoAcidDihedral implements AngleType {
    // TODO: add angles provided in Bio3D package
    /** All names of angles in the amino acid. */
    public enum AngleName {
        PHI, PSI, OMEGA
    }

    private static final String C = " C  ";
    private static final String CA = " CA ";
    private static final String N = " N  ";

    private static String[] atoms = new String[] { AminoAcidDihedral.C,
            AminoAcidDihedral.CA, AminoAcidDihedral.N };

    private static AminoAcidDihedral[] angles = new AminoAcidDihedral[] {
            new AminoAcidDihedral(AngleName.PHI),
            new AminoAcidDihedral(AngleName.PSI),
            new AminoAcidDihedral(AngleName.OMEGA) };

    private static Map<AngleName, String[]> mapAngleToAtoms;
    private static Map<AngleName, int[]> mapAngleToRules;

    static {
        AminoAcidDihedral.mapAngleToAtoms = new HashMap<>();
        AminoAcidDihedral.mapAngleToAtoms.put(AngleName.PHI, new String[] {
                AminoAcidDihedral.C, AminoAcidDihedral.N, AminoAcidDihedral.CA,
                AminoAcidDihedral.C });
        AminoAcidDihedral.mapAngleToAtoms.put(AngleName.PSI, new String[] {
                AminoAcidDihedral.N, AminoAcidDihedral.CA, AminoAcidDihedral.C,
                AminoAcidDihedral.N });
        AminoAcidDihedral.mapAngleToAtoms.put(AngleName.OMEGA, new String[] {
                AminoAcidDihedral.CA, AminoAcidDihedral.C, AminoAcidDihedral.N,
                AminoAcidDihedral.CA });

        AminoAcidDihedral.mapAngleToRules = new HashMap<>();
        AminoAcidDihedral.mapAngleToRules.put(AngleName.PHI, new int[] { 0, 1,
                1, 1 });
        AminoAcidDihedral.mapAngleToRules.put(AngleName.PSI, new int[] { 0, 0,
                0, 1 });
        AminoAcidDihedral.mapAngleToRules.put(AngleName.OMEGA, new int[] { 0,
                0, 1, 1 });
    }

    public static AngleType[] getAngles() {
        return AminoAcidDihedral.angles.clone();
    }

    public static String[] getUsedAtoms() {
        return AminoAcidDihedral.atoms.clone();
    }

    private AngleName angleName;

    public AminoAcidDihedral(AngleName angleName) {
        this.angleName = angleName;
    }

    @Override
    public String getAngleName() {
        return angleName.toString();
    }

    @Override
    public String[] getAtomNames(Group g) {
        return AminoAcidDihedral.mapAngleToAtoms.get(angleName);
    }

    @Override
    public int[] getGroupRule() {
        return AminoAcidDihedral.mapAngleToRules.get(angleName);
    }
}
