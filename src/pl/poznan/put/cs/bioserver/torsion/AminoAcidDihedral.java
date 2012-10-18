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
    public enum AngleName {
        PHI, PSI, OMEGA
    }

    public static final String C = " C  ";
    public static final String CA = " CA ";
    public static final String N = " N  ";
    public static final String[] USED_ATOMS = { C, CA, N };
    public static final AngleType[] ANGLES = new AminoAcidDihedral[] {
            new AminoAcidDihedral(AngleName.PHI),
            new AminoAcidDihedral(AngleName.PSI),
            new AminoAcidDihedral(AngleName.OMEGA) };

    private AngleName angleName;

    private static Map<AngleName, String[]> mapAngleToAtoms;
    private static Map<AngleName, int[]> mapAngleToRules;
    static {
        mapAngleToAtoms = new HashMap<>();
        mapAngleToAtoms.put(AngleName.PHI, new String[] { C, N, CA, C });
        mapAngleToAtoms.put(AngleName.PSI, new String[] { N, CA, C, N });
        mapAngleToAtoms.put(AngleName.OMEGA, new String[] { CA, C, N, CA });

        mapAngleToRules = new HashMap<>();
        mapAngleToRules.put(AngleName.PHI, new int[] { 0, 1, 1, 1 });
        mapAngleToRules.put(AngleName.PSI, new int[] { 0, 0, 0, 1 });
        mapAngleToRules.put(AngleName.OMEGA, new int[] { 0, 0, 1, 1 });
    }

    public AminoAcidDihedral(AngleName angleName) {
        this.angleName = angleName;
    }

    @Override
    public String[] getAtomNames(Group g) {
        return mapAngleToAtoms.get(angleName);
    }

    @Override
    public int[] getGroupRule() {
        return mapAngleToRules.get(angleName);
    }

    @Override
    public String getAngleName() {
        return angleName.toString();
    }
}
