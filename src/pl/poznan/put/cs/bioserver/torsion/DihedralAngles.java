package pl.poznan.put.cs.bioserver.torsion;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.ResidueNumber;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;

/**
 * A class to calculate and manage dihedral angles for given BioJava structure.
 * 
 * @author Tomasz Żok (tzok[at]cs.put.poznan.pl)
 */
public class DihedralAngles {
    /** Atoms needed to calculate dihedrals in amino acids. */
    private enum AminoAtoms {
        C, CA, N;
    }

    /** Atoms needed to calculate dihedrals in nucleotides. */
    private enum NucleotideAtoms {
        C1P, C2, C2P, C3P, C4P, C5P, N1, N9, O3P, O4P, O5P, P, C4;
    }

    private static final Logger LOGGER = Logger.getLogger(DihedralAngles.class);

    /**
     * Implementation of torsion angle calculation for amino acids.
     * 
     * @param groups
     *            Amino acids.
     * @return Collection of calculated dihedral angles.
     */
    private static List<AminoAcidDihedral> calculateAminoAcidDihedrals(
            List<Group> groups) {
        Vector<AminoAcidDihedral> aminoDihedrals = new Vector<>();
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

            double phi = DihedralAngles.dihedral(
                    atoms[0][AminoAtoms.C.ordinal()],
                    atoms[1][AminoAtoms.N.ordinal()],
                    atoms[1][AminoAtoms.CA.ordinal()],
                    atoms[1][AminoAtoms.C.ordinal()]);
            double psi = DihedralAngles.dihedral(
                    atoms[1][AminoAtoms.N.ordinal()],
                    atoms[1][AminoAtoms.CA.ordinal()],
                    atoms[1][AminoAtoms.C.ordinal()],
                    atoms[2][AminoAtoms.N.ordinal()]);
            double omega = DihedralAngles.dihedral(
                    atoms[1][AminoAtoms.CA.ordinal()],
                    atoms[1][AminoAtoms.C.ordinal()],
                    atoms[2][AminoAtoms.N.ordinal()],
                    atoms[2][AminoAtoms.CA.ordinal()]);

            Group group = atoms[1][0].getGroup();
            atoms[0] = atoms[1];
            atoms[1] = atoms[2];

            AminoAcidDihedral dihedral = new AminoAcidDihedral(phi, psi, omega,
                    group);
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
    private static List<NucleotideDihedral> calculateNucleotideDihedrals(
            List<Group> groups) {
        Vector<NucleotideDihedral> nucleotideDihedrals = new Vector<>();
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

            double alpha = DihedralAngles.dihedral(
                    atoms[0][NucleotideAtoms.O3P.ordinal()],
                    atoms[1][NucleotideAtoms.P.ordinal()],
                    atoms[1][NucleotideAtoms.O5P.ordinal()],
                    atoms[1][NucleotideAtoms.C5P.ordinal()]);
            double beta = DihedralAngles.dihedral(
                    atoms[1][NucleotideAtoms.P.ordinal()],
                    atoms[1][NucleotideAtoms.O5P.ordinal()],
                    atoms[1][NucleotideAtoms.C5P.ordinal()],
                    atoms[1][NucleotideAtoms.C4P.ordinal()]);
            double gamma = DihedralAngles.dihedral(
                    atoms[1][NucleotideAtoms.O5P.ordinal()],
                    atoms[1][NucleotideAtoms.C5P.ordinal()],
                    atoms[1][NucleotideAtoms.C4P.ordinal()],
                    atoms[1][NucleotideAtoms.C3P.ordinal()]);
            double delta = DihedralAngles.dihedral(
                    atoms[1][NucleotideAtoms.C5P.ordinal()],
                    atoms[1][NucleotideAtoms.C4P.ordinal()],
                    atoms[1][NucleotideAtoms.C3P.ordinal()],
                    atoms[1][NucleotideAtoms.O3P.ordinal()]);
            double epsilon = DihedralAngles.dihedral(
                    atoms[1][NucleotideAtoms.C4P.ordinal()],
                    atoms[1][NucleotideAtoms.C3P.ordinal()],
                    atoms[1][NucleotideAtoms.O3P.ordinal()],
                    atoms[2][NucleotideAtoms.P.ordinal()]);
            double dzeta = DihedralAngles.dihedral(
                    atoms[1][NucleotideAtoms.C3P.ordinal()],
                    atoms[1][NucleotideAtoms.O3P.ordinal()],
                    atoms[2][NucleotideAtoms.P.ordinal()],
                    atoms[2][NucleotideAtoms.O5P.ordinal()]);

            String pdbName = groups.get(i).getPDBName();
            pdbName = pdbName.trim();
            double chi;
            if (pdbName.equals("G") || pdbName.equals("A"))
                // Guanine or Adenine
                chi = DihedralAngles.dihedral(
                        atoms[1][NucleotideAtoms.O4P.ordinal()],
                        atoms[1][NucleotideAtoms.C1P.ordinal()],
                        atoms[1][NucleotideAtoms.N9.ordinal()],
                        atoms[1][NucleotideAtoms.C4.ordinal()]);
            else
                // Uracil or Cytosine
                chi = DihedralAngles.dihedral(
                        atoms[1][NucleotideAtoms.O4P.ordinal()],
                        atoms[1][NucleotideAtoms.C1P.ordinal()],
                        atoms[1][NucleotideAtoms.N1.ordinal()],
                        atoms[1][NucleotideAtoms.C2.ordinal()]);

            double[] tau = new double[5];
            tau[0] = DihedralAngles.dihedral(
                    atoms[1][NucleotideAtoms.C4P.ordinal()],
                    atoms[1][NucleotideAtoms.O4P.ordinal()],
                    atoms[1][NucleotideAtoms.C1P.ordinal()],
                    atoms[1][NucleotideAtoms.C2P.ordinal()]);
            tau[1] = DihedralAngles.dihedral(
                    atoms[1][NucleotideAtoms.O4P.ordinal()],
                    atoms[1][NucleotideAtoms.C1P.ordinal()],
                    atoms[1][NucleotideAtoms.C2P.ordinal()],
                    atoms[1][NucleotideAtoms.C3P.ordinal()]);
            tau[2] = DihedralAngles.dihedral(
                    atoms[1][NucleotideAtoms.C1P.ordinal()],
                    atoms[1][NucleotideAtoms.C2P.ordinal()],
                    atoms[1][NucleotideAtoms.C3P.ordinal()],
                    atoms[1][NucleotideAtoms.C4P.ordinal()]);
            tau[3] = DihedralAngles.dihedral(
                    atoms[1][NucleotideAtoms.C2P.ordinal()],
                    atoms[1][NucleotideAtoms.C3P.ordinal()],
                    atoms[1][NucleotideAtoms.C4P.ordinal()],
                    atoms[1][NucleotideAtoms.O4P.ordinal()]);
            tau[4] = DihedralAngles.dihedral(
                    atoms[1][NucleotideAtoms.C3P.ordinal()],
                    atoms[1][NucleotideAtoms.C4P.ordinal()],
                    atoms[1][NucleotideAtoms.O4P.ordinal()],
                    atoms[1][NucleotideAtoms.C1P.ordinal()]);
            double pSin = tau[1] + tau[4] - tau[0] - tau[3];
            double pCos = 2.0 * tau[2];
            pCos *= Math.sin(36.0 * Math.PI / 180.0)
                    + Math.sin(72.0 * Math.PI / 180.0);
            double P = Math.atan2(pSin, pCos);

            Group group = atoms[1][0].getGroup();
            atoms[0] = atoms[1];
            atoms[1] = atoms[2];

            NucleotideDihedral dihedral = new NucleotideDihedral(alpha, beta,
                    gamma, delta, epsilon, dzeta, chi, P, group);
            nucleotideDihedrals.add(dihedral);
        }
        return nucleotideDihedrals;
    }

    private static DihedralContainer computeDihedrals(List<Group> continuous) {
        List<Group> aminoVector = new Vector<>();
        List<Group> nucleotideVector = new Vector<>();

        for (Group g : continuous) {
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

        List<AminoAcidDihedral> aad = DihedralAngles
                .calculateAminoAcidDihedrals(aminoVector);
        List<NucleotideDihedral> nd = DihedralAngles
                .calculateNucleotideDihedrals(nucleotideVector);
        return new DihedralContainer(aad, nd);
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
    private static double dihedral(Atom a1, Atom a2, Atom a3, Atom a4) {
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

    public static DihedralContainer getDihedrals(Chain chain) {
        List<Group> atomGroups = chain.getAtomGroups();
        Deque<Integer> compact = new LinkedList<>();
        for (int i = 0; i < atomGroups.size(); i++) {
            Group group = atomGroups.get(i);
            Integer last = compact.peekLast();
            if (last == null || group.getResidueNumber().getSeqNum() - last == 1)
                compact.add();
        }
        return null;
    }

    public static DihedralContainer getDihedrals(Chain chain,
            int[][] compactGroups) {
        DihedralContainer container = new DihedralContainer();
        for (int i = 0; i < compactGroups.length; i++) {
            List<Group> compact = new Vector<>();
            for (int j = 0; j < compactGroups[i].length; j++) {
                try {
                    ResidueNumber resi = new ResidueNumber(null,
                            compactGroups[i][j], null);
                    compact.add(chain.getGroupByPDB(resi));
                } catch (StructureException e) {
                    DihedralAngles.LOGGER.error("Failed to read residue for "
                            + "dihedral angles calculation: "
                            + compactGroups[i][j], e);
                }
            }
            container.addAll(DihedralAngles.computeDihedrals(compact));
        }
        return container;
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
    public static DihedralContainer getDihedrals(Structure structure) {
        DihedralContainer allDihedrals = new DihedralContainer();
        for (Chain c : structure.getChains())
            allDihedrals.addAll(DihedralAngles.getDihedrals(c));
        return allDihedrals;
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
}
