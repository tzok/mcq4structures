package pl.poznan.put.cs.bioserver.torsion;

import java.util.List;
import java.util.Vector;

import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;

/**
 * A class to calculate and manage dihedral angles for given BioJava structure.
 * 
 * @author Tomasz Żok (tzok[at]cs.put.poznan.pl)
 */
public class DihedralAngles {
    /**
     * Dihedral angles for protein group (amino acid).
     * 
     * @author Tomasz Żok (tzok[at]cs.put.poznan.pl)
     */
    public class AminoAcidDihedral extends Dihedral {
        public static final int PHI = 0;
        public static final int PSI = 1;
        public static final int OMEGA = 2;

        public AminoAcidDihedral(double phi, double psi, double omega) {
            angles = new double[3];
            angles[AminoAcidDihedral.PHI] = phi;
            angles[AminoAcidDihedral.PSI] = psi;
            angles[AminoAcidDihedral.OMEGA] = omega;
        }
    }

    /** Atoms needed to calculate dihedrals in amino acids. */
    private enum AminoAtoms {
        C, CA, N;
    }

    /**
     * Generic dihedral angle class.
     * 
     * @author Tomasz Żok (tzok[at]cs.put.poznan.pl)
     */
    public abstract class Dihedral {
        public double[] angles;
    }

    /** Atoms needed to calculate dihedrals in nucleotides. */
    private enum NucleotideAtoms {
        C1P, C2, C2P, C3P, C4P, C5P, N1, N9, O3P, O4P, O5P, P, C4;
    }

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
                double delta, double epsilon, double zeta, double chi, double p) {
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

    /**
     * A vector in 3D space.
     * 
     * @author Tomasz Zok (tzok[at]cs.put.poznan.pl)
     */
    private class Vector3D {
        public double x, y, z;

        public Vector3D() {
        }

        public Vector3D(Atom a1, Atom a2) {
            x = a2.getX() - a1.getX();
            y = a2.getY() - a1.getY();
            z = a2.getZ() - a1.getZ();
        }

        /**
         * Calculate cross product of two vectors.
         * 
         * @param v
         *            Input vector.
         * @return Cross product of this object and vector in parameter.
         */
        public Vector3D cross(Vector3D v) {
            Vector3D result = new Vector3D();
            result.x = y * v.z - z * v.y;
            result.y = z * v.x - x * v.z;
            result.z = x * v.y - y * v.x;
            return result;
        }

        /**
         * Calculate dot product of two vectors.
         * 
         * @param v
         *            Input vector.
         * @return Dot product of this object and vector in parameter.
         */
        public double dot(Vector3D v) {
            return x * v.x + y * v.y + z * v.z;
        }

        /**
         * Returns length of this vector in euclidean space.
         * 
         * @return Vector length.
         */
        public double length() {
            return Math.sqrt(x * x + y * y + z * z);
        }

        /**
         * Scale vector by constant factor.
         * 
         * @param factor
         *            Scaling factor.
         * @return Vector with values scaled.
         */
        public Vector3D scale(double factor) {
            Vector3D result = new Vector3D();
            result.x = x * factor;
            result.y = y * factor;
            result.z = z * factor;
            return result;
        }
    }

    /** Constant usable in retrieving amino acid dihedral angles. */
    public static final int AMINO_GROUPS = 0;
    /** Constant usable in retrieving nucleotide dihedral angles. */
    public static final int NUCLEOTIDE_GROUPS = 1;

    /**
     * Filter given atom group for specific, named atoms.
     * 
     * @param group
     *            Atom group (amino acid or nucleotide).
     * @param atomsNames
     *            Enumeration with names of atoms to filter.
     * @return An array of filtered atoms.
     */
    private static Atom[] getAtoms(Group group, Enum<?>[] atomsNames) {
        Atom[] atoms = new Atom[atomsNames.length];
        int i = 0;
        for (Enum<?> atomName : atomsNames) {
            String name = atomName.name();
            Atom atom;
            if (name.length() > 1) {
                atom = DihedralAngles
                        .tryGetAtom(group, name.replace('P', '\''));
                if (atom == null)
                    atom = DihedralAngles.tryGetAtom(group,
                            name.replace('P', '*'));
            } else
                atom = DihedralAngles.tryGetAtom(group, name);
            atoms[i++] = atom;

        }
        return atoms;
    }

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
        if (Double.isNaN(a1) && Double.isNaN(a2))
            diff = 0;
        else if (Double.isNaN(a1) && !Double.isNaN(a2) || !Double.isNaN(a1)
                && Double.isNaN(a2))
            diff = Math.PI;
        else {
            double full = 2 * Math.PI;
            a1 = (a1 + full) % full;
            a2 = (a2 + full) % full;
            diff = Math.abs(a1 - a2);
            diff = Math.min(diff, full - diff);
        }
        return diff;
    }

    /**
     * Get an atom from given group or return null in case of errors (i.e. if
     * atom is not present).
     * 
     * @param group
     *            A residue.
     * @param name
     *            Name of an atom.
     * @return Object representing this atom.
     */
    private static Atom tryGetAtom(Group group, String name) {
        try {
            return group.getAtom(name);
        } catch (StructureException e) {
            return null;
        }
    }

    /**
     * Implementation of torsion angle calculation for amino acids.
     * 
     * @param groups
     *            Amino acids.
     * @return Collection of calculated dihedral angles.
     */
    private List<Dihedral> calculateAminoAcidDihedrals(List<Group> groups) {
        Vector<Dihedral> aminoDihedrals = new Vector<>();
        if (groups.size() == 0)
            return aminoDihedrals;

        Atom[][] atoms = new Atom[3][];
        atoms[0] = new Atom[AminoAtoms.values().length];
        atoms[1] = DihedralAngles.getAtoms(groups.get(0), AminoAtoms.values());
        for (int i = 0; i < groups.size(); ++i) {
            if (i == groups.size() - 1)
                atoms[2] = new Atom[AminoAtoms.values().length];
            else
                atoms[2] = DihedralAngles.getAtoms(groups.get(i + 1),
                        AminoAtoms.values());

            double phi = dihedral(atoms[0][AminoAtoms.C.ordinal()],
                    atoms[1][AminoAtoms.N.ordinal()],
                    atoms[1][AminoAtoms.CA.ordinal()],
                    atoms[1][AminoAtoms.C.ordinal()]);
            double psi = dihedral(atoms[1][AminoAtoms.N.ordinal()],
                    atoms[1][AminoAtoms.CA.ordinal()],
                    atoms[1][AminoAtoms.C.ordinal()],
                    atoms[2][AminoAtoms.N.ordinal()]);
            double omega = dihedral(atoms[1][AminoAtoms.CA.ordinal()],
                    atoms[1][AminoAtoms.C.ordinal()],
                    atoms[2][AminoAtoms.N.ordinal()],
                    atoms[2][AminoAtoms.CA.ordinal()]);

            atoms[0] = atoms[1];
            atoms[1] = atoms[2];

            AminoAcidDihedral dihedral = new AminoAcidDihedral(phi, psi, omega);
            aminoDihedrals.add(dihedral);
        }
        return aminoDihedrals;
    }

    /**
     * Implementation of torsion angle calculation for nucleotides.
     * 
     * @param groups
     *            Nucleotides.
     * @return Collection of calculated dihedral angles.
     */
    private List<Dihedral> calculateNucleotideDihedrals(List<Group> groups) {
        Vector<Dihedral> nucleotideDihedrals = new Vector<>();
        if (groups.size() == 0)
            return nucleotideDihedrals;

        Atom[][] atoms = new Atom[3][];
        atoms[0] = new Atom[NucleotideAtoms.values().length];
        atoms[1] = DihedralAngles.getAtoms(groups.get(0),
                NucleotideAtoms.values());

        for (int i = 0; i < groups.size(); ++i) {
            if (i == groups.size() - 1)
                atoms[2] = new Atom[NucleotideAtoms.values().length];
            else
                atoms[2] = DihedralAngles.getAtoms(groups.get(i + 1),
                        NucleotideAtoms.values());

            double alpha = dihedral(atoms[0][NucleotideAtoms.O3P.ordinal()],
                    atoms[1][NucleotideAtoms.P.ordinal()],
                    atoms[1][NucleotideAtoms.O5P.ordinal()],
                    atoms[1][NucleotideAtoms.C5P.ordinal()]);
            double beta = dihedral(atoms[1][NucleotideAtoms.P.ordinal()],
                    atoms[1][NucleotideAtoms.O5P.ordinal()],
                    atoms[1][NucleotideAtoms.C5P.ordinal()],
                    atoms[1][NucleotideAtoms.C4P.ordinal()]);
            double gamma = dihedral(atoms[1][NucleotideAtoms.O5P.ordinal()],
                    atoms[1][NucleotideAtoms.C5P.ordinal()],
                    atoms[1][NucleotideAtoms.C4P.ordinal()],
                    atoms[1][NucleotideAtoms.C3P.ordinal()]);
            double delta = dihedral(atoms[1][NucleotideAtoms.C5P.ordinal()],
                    atoms[1][NucleotideAtoms.C4P.ordinal()],
                    atoms[1][NucleotideAtoms.C3P.ordinal()],
                    atoms[1][NucleotideAtoms.O3P.ordinal()]);
            double epsilon = dihedral(atoms[1][NucleotideAtoms.C4P.ordinal()],
                    atoms[1][NucleotideAtoms.C3P.ordinal()],
                    atoms[1][NucleotideAtoms.O3P.ordinal()],
                    atoms[2][NucleotideAtoms.P.ordinal()]);
            double dzeta = dihedral(atoms[1][NucleotideAtoms.C3P.ordinal()],
                    atoms[1][NucleotideAtoms.O3P.ordinal()],
                    atoms[2][NucleotideAtoms.P.ordinal()],
                    atoms[2][NucleotideAtoms.O5P.ordinal()]);

            String pdbName = groups.get(i).getPDBName();
            pdbName = pdbName.trim();
            double chi;
            if (pdbName.equals("G") || pdbName.equals("A"))
                // Guanine or Adenine
                chi = dihedral(atoms[1][NucleotideAtoms.O4P.ordinal()],
                        atoms[1][NucleotideAtoms.C1P.ordinal()],
                        atoms[1][NucleotideAtoms.N9.ordinal()],
                        atoms[1][NucleotideAtoms.C4.ordinal()]);
            else
                // Uracil or Cytosine
                chi = dihedral(atoms[1][NucleotideAtoms.O4P.ordinal()],
                        atoms[1][NucleotideAtoms.C1P.ordinal()],
                        atoms[1][NucleotideAtoms.N1.ordinal()],
                        atoms[1][NucleotideAtoms.C2.ordinal()]);

            double[] tau = new double[5];
            tau[0] = dihedral(atoms[1][NucleotideAtoms.C4P.ordinal()],
                    atoms[1][NucleotideAtoms.O4P.ordinal()],
                    atoms[1][NucleotideAtoms.C1P.ordinal()],
                    atoms[1][NucleotideAtoms.C2P.ordinal()]);
            tau[1] = dihedral(atoms[1][NucleotideAtoms.O4P.ordinal()],
                    atoms[1][NucleotideAtoms.C1P.ordinal()],
                    atoms[1][NucleotideAtoms.C2P.ordinal()],
                    atoms[1][NucleotideAtoms.C3P.ordinal()]);
            tau[2] = dihedral(atoms[1][NucleotideAtoms.C1P.ordinal()],
                    atoms[1][NucleotideAtoms.C2P.ordinal()],
                    atoms[1][NucleotideAtoms.C3P.ordinal()],
                    atoms[1][NucleotideAtoms.C4P.ordinal()]);
            tau[3] = dihedral(atoms[1][NucleotideAtoms.C2P.ordinal()],
                    atoms[1][NucleotideAtoms.C3P.ordinal()],
                    atoms[1][NucleotideAtoms.C4P.ordinal()],
                    atoms[1][NucleotideAtoms.O4P.ordinal()]);
            tau[4] = dihedral(atoms[1][NucleotideAtoms.C3P.ordinal()],
                    atoms[1][NucleotideAtoms.C4P.ordinal()],
                    atoms[1][NucleotideAtoms.O4P.ordinal()],
                    atoms[1][NucleotideAtoms.C1P.ordinal()]);
            double pSin = tau[1] + tau[4] - tau[0] - tau[3];
            double pCos = 2.0 * tau[2];
            pCos *= Math.sin(36.0 * Math.PI / 180.0)
                    + Math.sin(72.0 * Math.PI / 180.0);
            double P = Math.atan2(pSin, pCos);

            atoms[0] = atoms[1];
            atoms[1] = atoms[2];

            NucleotideDihedral dihedral = new NucleotideDihedral(alpha, beta,
                    gamma, delta, epsilon, dzeta, chi, P);
            nucleotideDihedrals.add(dihedral);
        }
        return nucleotideDihedrals;
    }

    /**
     * Calculate one dihedral angle value for given four atoms.
     * 
     * @param a1
     *            Atom 1.
     * @param a2
     *            Atom 2.
     * @param a3
     *            Atom 3.
     * @param a4
     *            Atom 4.
     * @return Dihedral angle between atoms 1-4.
     */
    private double dihedral(Atom a1, Atom a2, Atom a3, Atom a4) {
        if (a1 == null || a2 == null || a3 == null || a4 == null)
            return Double.NaN;

        Vector3D v1 = new Vector3D(a1, a2);
        Vector3D v2 = new Vector3D(a2, a3);
        Vector3D v3 = new Vector3D(a3, a4);

        Vector3D tmp1 = v1.cross(v2);
        Vector3D tmp2 = v2.cross(v3);
        Vector3D tmp3 = v1.scale(v2.length());
        return Math.atan2(tmp3.dot(tmp2), tmp1.dot(tmp2));
    }

    /**
     * Calculate dihedral angles values for specified chain.
     * 
     * @param chain
     *            Input chain.
     * @return A 2D array[][] where array[0][] are for proteins, array[1][] for
     *         RNAs, array[][i] is for i-th angle.
     */
    public Dihedral[][] getDihedrals(Chain chain) {
        Vector<Group> aminoVector = new Vector<>();
        Vector<Group> nucleotideVector = new Vector<>();
        for (Group g : chain.getAtomGroups()) {
            Group group = (Group) g.clone();
            for (Group altLoc : g.getAltLocs())
                for (Atom a : altLoc.getAtoms())
                    group.addAtom(a);
            String type = group.getType();
            if (type.equals("amino") || group.hasAminoAtoms())
                aminoVector.add(group);
            else if (type.equals("nucleotide") || group.hasAtom("P"))
                nucleotideVector.add(group);
        }

        List<Dihedral> aminoDihedrals = calculateAminoAcidDihedrals(aminoVector);
        List<Dihedral> nucleotideDihedrals = calculateNucleotideDihedrals(nucleotideVector);

        Dihedral[][] dihedrals = new Dihedral[2][];
        dihedrals[DihedralAngles.AMINO_GROUPS] = aminoDihedrals
                .toArray(new Dihedral[aminoDihedrals.size()]);
        dihedrals[DihedralAngles.NUCLEOTIDE_GROUPS] = nucleotideDihedrals
                .toArray(new Dihedral[nucleotideDihedrals.size()]);
        return dihedrals;
    }

    /**
     * Iterate through structure's chains, groups and atoms. Return the
     * collection of calculated dihedral angles for each chain.
     * 
     * @param structure
     *            Structure to calculate dihedral angles within.
     * @return A collection of dihedral angles for given chain and type. The
     *         "double[][][] result" array must be interpreted this way:
     *         result[i] is i-th chain, result[i][0] is for amino acids and
     *         result[i][1] is for nucleotides, result[i][j][k] is for k-th
     *         group.
     */
    public Dihedral[][][] getDihedrals(Structure structure) {
        Dihedral[][][] dihedrals = new Dihedral[structure.size()][][];
        int i = 0;
        for (Chain c : structure.getChains())
            dihedrals[i++] = getDihedrals(c);
        return dihedrals;
    }
}
