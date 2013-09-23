package pl.poznan.put.cs.bioserver.torsion;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.map.MultiKeyMap;
import org.biojava.bio.structure.Group;

import pl.poznan.put.cs.bioserver.helper.Constants;
import pl.poznan.put.cs.bioserver.helper.UniTypeQuadruplet;

/**
 * Dihedral angles for protein group (amino acid).
 * 
 * @author Tomasz Zok (tzok[at]cs.put.poznan.pl)
 */
public final class AminoAcidDihedral implements AngleType {
    /** All names of angles in the amino acid. */
    private enum AngleName {
        CALPHA, CHI1, CHI2, CHI3, CHI4, CHI5, OMEGA, PHI, PSI
    }

    private static List<AngleType> angles = Arrays.asList(new AngleType[] {
            new AminoAcidDihedral(AngleName.PHI, Constants.UNICODE_PHI
                    + " (phi)"),
            new AminoAcidDihedral(AngleName.PSI, Constants.UNICODE_PSI
                    + " (psi)"),
            new AminoAcidDihedral(AngleName.OMEGA, Constants.UNICODE_OMEGA
                    + " (omega)"),
            new AminoAcidDihedral(AngleName.CALPHA, "C-"
                    + Constants.UNICODE_ALPHA + " (C-alpha)"),
            new AminoAcidDihedral(AngleName.CHI1, Constants.UNICODE_CHI
                    + "1 (chi1)"),
            new AminoAcidDihedral(AngleName.CHI2, Constants.UNICODE_CHI
                    + "2 (chi2)"),
            new AminoAcidDihedral(AngleName.CHI3, Constants.UNICODE_CHI
                    + "3 (chi3)"),
            new AminoAcidDihedral(AngleName.CHI4, Constants.UNICODE_CHI
                    + "4 (chi4)"),
            new AminoAcidDihedral(AngleName.CHI5, Constants.UNICODE_CHI
                    + "5 (chi5)") });

    private static List<String> atoms = Arrays.asList(new String[] {
            AminoAcidDihedral.C, AminoAcidDihedral.CA, AminoAcidDihedral.CB,
            AminoAcidDihedral.CD, AminoAcidDihedral.CD1, AminoAcidDihedral.CE,
            AminoAcidDihedral.CG, AminoAcidDihedral.CG1, AminoAcidDihedral.CZ,
            AminoAcidDihedral.N, AminoAcidDihedral.ND1, AminoAcidDihedral.NE,
            AminoAcidDihedral.NH1, AminoAcidDihedral.NZ, AminoAcidDihedral.OD1,
            AminoAcidDihedral.OE1, AminoAcidDihedral.OG, AminoAcidDihedral.OG1,
            AminoAcidDihedral.SD, AminoAcidDihedral.SG });

    private static final String C = " C  ";
    private static final String CA = " CA ";
    private static final String CB = " CB ";
    private static final String CD = " CD ";
    private static final String CD1 = " CD1";
    private static final String CE = " CE ";
    private static final String CG = " CG ";
    private static final String CG1 = " CG1";
    private static final String CZ = " CZ ";
    private static final String N = " N  ";
    private static final String ND1 = " ND1";
    private static final String NE = " NE ";
    private static final String NH1 = " NH1";
    private static final String NZ = " NZ ";
    private static final String OD1 = " OD1";
    private static final String OE1 = " OE1";
    private static final String OG = " OG ";
    private static final String OG1 = " OG1";
    private static final String SD = " SD ";
    private static final String SG = " SG ";

    private static Map<AngleName, UniTypeQuadruplet<String>> mapAngleToAtoms =
            new HashMap<>();
    private static Map<AngleName, UniTypeQuadruplet<Integer>> mapAngleToRules =
            new HashMap<>();
    private static MultiKeyMap<Object, UniTypeQuadruplet<String>> mapResidueAngleNameToAtoms =
            new MultiKeyMap<>();

    static {
        AminoAcidDihedral.mapAngleToAtoms.put(AngleName.PHI,
                new UniTypeQuadruplet<>(new String[] { AminoAcidDihedral.C,
                        AminoAcidDihedral.N, AminoAcidDihedral.CA,
                        AminoAcidDihedral.C }));
        AminoAcidDihedral.mapAngleToAtoms.put(AngleName.PSI,
                new UniTypeQuadruplet<>(new String[] { AminoAcidDihedral.N,
                        AminoAcidDihedral.CA, AminoAcidDihedral.C,
                        AminoAcidDihedral.N }));
        AminoAcidDihedral.mapAngleToAtoms.put(AngleName.OMEGA,
                new UniTypeQuadruplet<>(new String[] { AminoAcidDihedral.CA,
                        AminoAcidDihedral.C, AminoAcidDihedral.N,
                        AminoAcidDihedral.CA }));
        AminoAcidDihedral.mapAngleToAtoms.put(AngleName.CALPHA,
                new UniTypeQuadruplet<>(new String[] { AminoAcidDihedral.CA,
                        AminoAcidDihedral.CA, AminoAcidDihedral.CA,
                        AminoAcidDihedral.CA }));

        AminoAcidDihedral.mapAngleToRules.put(AngleName.PHI,
                new UniTypeQuadruplet<>(new Integer[] { 0, 1, 1, 1 }));
        AminoAcidDihedral.mapAngleToRules.put(AngleName.PSI,
                new UniTypeQuadruplet<>(new Integer[] { 0, 0, 0, 1 }));
        AminoAcidDihedral.mapAngleToRules.put(AngleName.OMEGA,
                new UniTypeQuadruplet<>(new Integer[] { 0, 0, 1, 1 }));
        AminoAcidDihedral.mapAngleToRules.put(AngleName.CALPHA,
                new UniTypeQuadruplet<>(new Integer[] { 0, 1, 2, 3 }));

        AminoAcidDihedral.mapAngleToRules.put(AngleName.CHI1,
                new UniTypeQuadruplet<>(new Integer[] { 0, 0, 0, 0 }));
        AminoAcidDihedral.mapAngleToRules.put(AngleName.CHI2,
                new UniTypeQuadruplet<>(new Integer[] { 0, 0, 0, 0 }));
        AminoAcidDihedral.mapAngleToRules.put(AngleName.CHI3,
                new UniTypeQuadruplet<>(new Integer[] { 0, 0, 0, 0 }));
        AminoAcidDihedral.mapAngleToRules.put(AngleName.CHI4,
                new UniTypeQuadruplet<>(new Integer[] { 0, 0, 0, 0 }));
        AminoAcidDihedral.mapAngleToRules.put(AngleName.CHI5,
                new UniTypeQuadruplet<>(new Integer[] { 0, 0, 0, 0 }));

        // alanine, ala, A (it has zero torsion angles in side chains)
        // arginine, arg, R
        AminoAcidDihedral.mapResidueAngleNameToAtoms.put("ARG", AngleName.CHI1,
                new UniTypeQuadruplet<>(new String[] { AminoAcidDihedral.N,
                        AminoAcidDihedral.CA, AminoAcidDihedral.CB,
                        AminoAcidDihedral.CG }));
        AminoAcidDihedral.mapResidueAngleNameToAtoms.put("ARG", AngleName.CHI2,
                new UniTypeQuadruplet<>(new String[] { AminoAcidDihedral.CA,
                        AminoAcidDihedral.CB, AminoAcidDihedral.CG,
                        AminoAcidDihedral.CD }));
        AminoAcidDihedral.mapResidueAngleNameToAtoms.put("ARG", AngleName.CHI3,
                new UniTypeQuadruplet<>(new String[] { AminoAcidDihedral.CB,
                        AminoAcidDihedral.CG, AminoAcidDihedral.CD,
                        AminoAcidDihedral.NE }));
        AminoAcidDihedral.mapResidueAngleNameToAtoms.put("ARG", AngleName.CHI4,
                new UniTypeQuadruplet<>(new String[] { AminoAcidDihedral.CG,
                        AminoAcidDihedral.CD, AminoAcidDihedral.NE,
                        AminoAcidDihedral.CZ }));
        AminoAcidDihedral.mapResidueAngleNameToAtoms.put("ARG", AngleName.CHI5,
                new UniTypeQuadruplet<>(new String[] { AminoAcidDihedral.CD,
                        AminoAcidDihedral.NE, AminoAcidDihedral.CZ,
                        AminoAcidDihedral.NH1 }));
        // asparagine, asn, N
        AminoAcidDihedral.mapResidueAngleNameToAtoms.put("ASN", AngleName.CHI1,
                new UniTypeQuadruplet<>(new String[] { AminoAcidDihedral.N,
                        AminoAcidDihedral.CA, AminoAcidDihedral.CB,
                        AminoAcidDihedral.CG }));
        AminoAcidDihedral.mapResidueAngleNameToAtoms.put("ASN", AngleName.CHI2,
                new UniTypeQuadruplet<>(new String[] { AminoAcidDihedral.CA,
                        AminoAcidDihedral.CB, AminoAcidDihedral.CG,
                        AminoAcidDihedral.OD1 }));
        // aspartic acid, asp, D
        AminoAcidDihedral.mapResidueAngleNameToAtoms.put("ASP", AngleName.CHI1,
                new UniTypeQuadruplet<>(new String[] { AminoAcidDihedral.N,
                        AminoAcidDihedral.CA, AminoAcidDihedral.CB,
                        AminoAcidDihedral.CG }));
        AminoAcidDihedral.mapResidueAngleNameToAtoms.put("ASP", AngleName.CHI2,
                new UniTypeQuadruplet<>(new String[] { AminoAcidDihedral.CA,
                        AminoAcidDihedral.CB, AminoAcidDihedral.CG,
                        AminoAcidDihedral.OD1 }));
        // asparagine or aspartic acid, asx, B
        AminoAcidDihedral.mapResidueAngleNameToAtoms.put("ASX", AngleName.CHI1,
                new UniTypeQuadruplet<>(new String[] { AminoAcidDihedral.N,
                        AminoAcidDihedral.CA, AminoAcidDihedral.CB,
                        AminoAcidDihedral.CG }));
        AminoAcidDihedral.mapResidueAngleNameToAtoms.put("ASX", AngleName.CHI2,
                new UniTypeQuadruplet<>(new String[] { AminoAcidDihedral.CA,
                        AminoAcidDihedral.CB, AminoAcidDihedral.CG,
                        AminoAcidDihedral.OD1 }));
        // cysteine, cys, C
        AminoAcidDihedral.mapResidueAngleNameToAtoms.put("CYS", AngleName.CHI1,
                new UniTypeQuadruplet<>(new String[] { AminoAcidDihedral.N,
                        AminoAcidDihedral.CA, AminoAcidDihedral.CB,
                        AminoAcidDihedral.SG }));
        // glutamic acid, glu, E
        AminoAcidDihedral.mapResidueAngleNameToAtoms.put("GLU", AngleName.CHI1,
                new UniTypeQuadruplet<>(new String[] { AminoAcidDihedral.N,
                        AminoAcidDihedral.CA, AminoAcidDihedral.CB,
                        AminoAcidDihedral.CG }));
        AminoAcidDihedral.mapResidueAngleNameToAtoms.put("GLU", AngleName.CHI2,
                new UniTypeQuadruplet<>(new String[] { AminoAcidDihedral.CA,
                        AminoAcidDihedral.CB, AminoAcidDihedral.CG,
                        AminoAcidDihedral.CD }));
        AminoAcidDihedral.mapResidueAngleNameToAtoms.put("GLU", AngleName.CHI3,
                new UniTypeQuadruplet<>(new String[] { AminoAcidDihedral.CB,
                        AminoAcidDihedral.CG, AminoAcidDihedral.CD,
                        AminoAcidDihedral.OE1 }));
        // glutamine, gln, Q
        AminoAcidDihedral.mapResidueAngleNameToAtoms.put("GLN", AngleName.CHI1,
                new UniTypeQuadruplet<>(new String[] { AminoAcidDihedral.N,
                        AminoAcidDihedral.CA, AminoAcidDihedral.CB,
                        AminoAcidDihedral.CG }));
        AminoAcidDihedral.mapResidueAngleNameToAtoms.put("GLN", AngleName.CHI2,
                new UniTypeQuadruplet<>(new String[] { AminoAcidDihedral.CA,
                        AminoAcidDihedral.CB, AminoAcidDihedral.CG,
                        AminoAcidDihedral.CD }));
        AminoAcidDihedral.mapResidueAngleNameToAtoms.put("GLN", AngleName.CHI3,
                new UniTypeQuadruplet<>(new String[] { AminoAcidDihedral.CB,
                        AminoAcidDihedral.CG, AminoAcidDihedral.CD,
                        AminoAcidDihedral.OE1 }));
        // glutamine or glutamic acid, glx, Z
        AminoAcidDihedral.mapResidueAngleNameToAtoms.put("GLX", AngleName.CHI1,
                new UniTypeQuadruplet<>(new String[] { AminoAcidDihedral.N,
                        AminoAcidDihedral.CA, AminoAcidDihedral.CB,
                        AminoAcidDihedral.CG }));
        AminoAcidDihedral.mapResidueAngleNameToAtoms.put("GLX", AngleName.CHI2,
                new UniTypeQuadruplet<>(new String[] { AminoAcidDihedral.CA,
                        AminoAcidDihedral.CB, AminoAcidDihedral.CG,
                        AminoAcidDihedral.CD }));
        AminoAcidDihedral.mapResidueAngleNameToAtoms.put("GLX", AngleName.CHI3,
                new UniTypeQuadruplet<>(new String[] { AminoAcidDihedral.CB,
                        AminoAcidDihedral.CG, AminoAcidDihedral.CD,
                        AminoAcidDihedral.OE1 }));
        // glycine, gly, G (it has zero torsion angles in side chains)
        // histidine, his, H
        AminoAcidDihedral.mapResidueAngleNameToAtoms.put("HIS", AngleName.CHI1,
                new UniTypeQuadruplet<>(new String[] { AminoAcidDihedral.N,
                        AminoAcidDihedral.CA, AminoAcidDihedral.CB,
                        AminoAcidDihedral.CG }));
        AminoAcidDihedral.mapResidueAngleNameToAtoms.put("HIS", AngleName.CHI2,
                new UniTypeQuadruplet<>(new String[] { AminoAcidDihedral.CA,
                        AminoAcidDihedral.CB, AminoAcidDihedral.CG,
                        AminoAcidDihedral.ND1 }));
        // isoleucine, ile, I
        AminoAcidDihedral.mapResidueAngleNameToAtoms.put("ILE", AngleName.CHI1,
                new UniTypeQuadruplet<>(new String[] { AminoAcidDihedral.N,
                        AminoAcidDihedral.CA, AminoAcidDihedral.CB,
                        AminoAcidDihedral.CG1 }));
        AminoAcidDihedral.mapResidueAngleNameToAtoms.put("ILE", AngleName.CHI2,
                new UniTypeQuadruplet<>(new String[] { AminoAcidDihedral.CA,
                        AminoAcidDihedral.CB, AminoAcidDihedral.CG1,
                        AminoAcidDihedral.CD1 }));
        // leucine, leu, L
        AminoAcidDihedral.mapResidueAngleNameToAtoms.put("LEU", AngleName.CHI1,
                new UniTypeQuadruplet<>(new String[] { AminoAcidDihedral.N,
                        AminoAcidDihedral.CA, AminoAcidDihedral.CB,
                        AminoAcidDihedral.CG }));
        AminoAcidDihedral.mapResidueAngleNameToAtoms.put("LEU", AngleName.CHI2,
                new UniTypeQuadruplet<>(new String[] { AminoAcidDihedral.CA,
                        AminoAcidDihedral.CB, AminoAcidDihedral.CG,
                        AminoAcidDihedral.CD1 }));
        // lysine, lys, K
        AminoAcidDihedral.mapResidueAngleNameToAtoms.put("LYS", AngleName.CHI1,
                new UniTypeQuadruplet<>(new String[] { AminoAcidDihedral.N,
                        AminoAcidDihedral.CA, AminoAcidDihedral.CB,
                        AminoAcidDihedral.CG }));
        AminoAcidDihedral.mapResidueAngleNameToAtoms.put("LYS", AngleName.CHI2,
                new UniTypeQuadruplet<>(new String[] { AminoAcidDihedral.CA,
                        AminoAcidDihedral.CB, AminoAcidDihedral.CG,
                        AminoAcidDihedral.CD }));
        AminoAcidDihedral.mapResidueAngleNameToAtoms.put("LYS", AngleName.CHI3,
                new UniTypeQuadruplet<>(new String[] { AminoAcidDihedral.CB,
                        AminoAcidDihedral.CG, AminoAcidDihedral.CD,
                        AminoAcidDihedral.CE }));
        AminoAcidDihedral.mapResidueAngleNameToAtoms.put("LYS", AngleName.CHI4,
                new UniTypeQuadruplet<>(new String[] { AminoAcidDihedral.CG,
                        AminoAcidDihedral.CD, AminoAcidDihedral.CE,
                        AminoAcidDihedral.NZ }));
        // methionine, met, M
        AminoAcidDihedral.mapResidueAngleNameToAtoms.put("MET", AngleName.CHI1,
                new UniTypeQuadruplet<>(new String[] { AminoAcidDihedral.N,
                        AminoAcidDihedral.CA, AminoAcidDihedral.CB,
                        AminoAcidDihedral.CG }));
        AminoAcidDihedral.mapResidueAngleNameToAtoms.put("MET", AngleName.CHI2,
                new UniTypeQuadruplet<>(new String[] { AminoAcidDihedral.CA,
                        AminoAcidDihedral.CB, AminoAcidDihedral.CG,
                        AminoAcidDihedral.SD }));
        AminoAcidDihedral.mapResidueAngleNameToAtoms.put("MET", AngleName.CHI3,
                new UniTypeQuadruplet<>(new String[] { AminoAcidDihedral.CB,
                        AminoAcidDihedral.CG, AminoAcidDihedral.SD,
                        AminoAcidDihedral.CE }));
        // phenylalanine, phe, F
        AminoAcidDihedral.mapResidueAngleNameToAtoms.put("PHE", AngleName.CHI1,
                new UniTypeQuadruplet<>(new String[] { AminoAcidDihedral.N,
                        AminoAcidDihedral.CA, AminoAcidDihedral.CB,
                        AminoAcidDihedral.CG }));
        AminoAcidDihedral.mapResidueAngleNameToAtoms.put("PHE", AngleName.CHI2,
                new UniTypeQuadruplet<>(new String[] { AminoAcidDihedral.CA,
                        AminoAcidDihedral.CB, AminoAcidDihedral.CG,
                        AminoAcidDihedral.CD1 }));
        // proline, pro, P
        AminoAcidDihedral.mapResidueAngleNameToAtoms.put("PRO", AngleName.CHI1,
                new UniTypeQuadruplet<>(new String[] { AminoAcidDihedral.N,
                        AminoAcidDihedral.CA, AminoAcidDihedral.CB,
                        AminoAcidDihedral.CG }));
        AminoAcidDihedral.mapResidueAngleNameToAtoms.put("PRO", AngleName.CHI2,
                new UniTypeQuadruplet<>(new String[] { AminoAcidDihedral.CA,
                        AminoAcidDihedral.CB, AminoAcidDihedral.CG,
                        AminoAcidDihedral.CD }));
        // serine, ser, S
        AminoAcidDihedral.mapResidueAngleNameToAtoms.put("SER", AngleName.CHI1,
                new UniTypeQuadruplet<>(new String[] { AminoAcidDihedral.N,
                        AminoAcidDihedral.CA, AminoAcidDihedral.CB,
                        AminoAcidDihedral.OG }));
        // threonine, thr, T
        AminoAcidDihedral.mapResidueAngleNameToAtoms.put("THR", AngleName.CHI1,
                new UniTypeQuadruplet<>(new String[] { AminoAcidDihedral.N,
                        AminoAcidDihedral.CA, AminoAcidDihedral.CB,
                        AminoAcidDihedral.OG1 }));
        // tryptophan, trp, W
        AminoAcidDihedral.mapResidueAngleNameToAtoms.put("TRP", AngleName.CHI1,
                new UniTypeQuadruplet<>(new String[] { AminoAcidDihedral.N,
                        AminoAcidDihedral.CA, AminoAcidDihedral.CB,
                        AminoAcidDihedral.CG }));
        AminoAcidDihedral.mapResidueAngleNameToAtoms.put("TRP", AngleName.CHI2,
                new UniTypeQuadruplet<>(new String[] { AminoAcidDihedral.CA,
                        AminoAcidDihedral.CB, AminoAcidDihedral.CG,
                        AminoAcidDihedral.CD1 }));
        // tyrosine, tyr, Y
        AminoAcidDihedral.mapResidueAngleNameToAtoms.put("TYR", AngleName.CHI1,
                new UniTypeQuadruplet<>(new String[] { AminoAcidDihedral.N,
                        AminoAcidDihedral.CA, AminoAcidDihedral.CB,
                        AminoAcidDihedral.CG }));
        AminoAcidDihedral.mapResidueAngleNameToAtoms.put("TYR", AngleName.CHI2,
                new UniTypeQuadruplet<>(new String[] { AminoAcidDihedral.CA,
                        AminoAcidDihedral.CB, AminoAcidDihedral.CG,
                        AminoAcidDihedral.CD1 }));
        // valine, val, V
        AminoAcidDihedral.mapResidueAngleNameToAtoms.put("VAL", AngleName.CHI1,
                new UniTypeQuadruplet<>(new String[] { AminoAcidDihedral.N,
                        AminoAcidDihedral.CA, AminoAcidDihedral.CB,
                        AminoAcidDihedral.CG1 }));
    }

    public static List<AngleType> getAngles() {
        return AminoAcidDihedral.angles;
    }

    public static List<String> getUsedAtoms() {
        return AminoAcidDihedral.atoms;
    }

    private AngleName angleName;
    private String displayName;

    private AminoAcidDihedral(AngleName angleName, String displayName) {
        this.angleName = angleName;
        this.displayName = displayName;
    }

    @Override
    public String getAngleDisplayName() {
        return displayName;
    }

    @Override
    public String getAngleName() {
        return angleName.toString();
    }

    @Override
    public UniTypeQuadruplet<String> getAtomNames(Group g) {
        if (angleName.equals(AngleName.OMEGA)
                || angleName.equals(AngleName.PHI)
                || angleName.equals(AngleName.PSI)
                || angleName.equals(AngleName.CALPHA)) {
            return AminoAcidDihedral.mapAngleToAtoms.get(angleName);
        }

        UniTypeQuadruplet<String> names =
                AminoAcidDihedral.mapResidueAngleNameToAtoms.get(
                        g.getPDBName(), angleName);
        if (names == null) {
            names = new UniTypeQuadruplet<>(null, null, null, null);
        }
        return names;
    }

    @Override
    public UniTypeQuadruplet<Integer> getGroupRule() {
        return AminoAcidDihedral.mapAngleToRules.get(angleName);
    }
}
