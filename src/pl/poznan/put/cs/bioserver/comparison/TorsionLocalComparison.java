package pl.poznan.put.cs.bioserver.comparison;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.ResidueNumber;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.io.PDBFileReader;

import pl.poznan.put.cs.bioserver.alignment.StructureAligner;
import pl.poznan.put.cs.bioserver.helper.Helper;
import pl.poznan.put.cs.bioserver.torsion.AminoAcidDihedral;
import pl.poznan.put.cs.bioserver.torsion.AngleDifference;
import pl.poznan.put.cs.bioserver.torsion.AngleType;
import pl.poznan.put.cs.bioserver.torsion.DihedralAngles;
import pl.poznan.put.cs.bioserver.torsion.NucleotideDihedral;

/**
 * Implementation of local dissimilarity measure based on torsion angles.
 * 
 * @author Tomasz Żok (tzok[at]cs.put.poznan.pl)
 */
public class TorsionLocalComparison extends LocalComparison {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
            .getLogger(TorsionLocalComparison.class);

    /**
     * Compare two chains.
     * 
     * @param atoms
     *            Two arrays of atoms from two structures.
     * @param angles
     *            An array of types of angles to be checked.
     * @param wasAligned
     *            Were the atoms aligned before?
     * 
     * @return A map of name of angle to the list of differences defined upon
     *         it.
     */
    public static Map<String, List<AngleDifference>> compare(Atom[][] atoms,
            AngleType[] angles, boolean wasAligned) {
        Atom[][] equalized = Helper.equalize(atoms);

        List<AngleDifference> allDiffs = new ArrayList<>();
        for (AngleType at : angles) {
            allDiffs.addAll(DihedralAngles.calculateAngleDiff(equalized, at,
                    wasAligned));
        }

        Map<ResidueNumber, List<AngleDifference>> mapResToDiffs = new HashMap<>();
        Map<ResidueNumber, AngleDifference[]> mapResToTaus = new HashMap<>();
        Map<String, List<AngleDifference>> mapNameToDiffs = new HashMap<>();
        for (AngleDifference diff : allDiffs) {
            if (!mapResToDiffs.containsKey(diff.residue)) {
                mapResToDiffs.put(diff.residue,
                        new ArrayList<AngleDifference>());
                mapResToTaus.put(diff.residue, new AngleDifference[5]);
            }
            List<AngleDifference> list = mapResToDiffs.get(diff.residue);
            list.add(diff);

            if (diff.angleName.startsWith("TAU")) {
                AngleDifference[] taus = mapResToTaus.get(diff.residue);
                int which = diff.angleName.charAt(3) - '0';
                taus[which] = diff;
            }

            if (!mapNameToDiffs.containsKey(diff.angleName)) {
                mapNameToDiffs.put(diff.angleName,
                        new ArrayList<AngleDifference>());
            }
            list = mapNameToDiffs.get(diff.angleName);
            list.add(diff);
        }

        List<AngleDifference> pAngles = new ArrayList<>();
        mapNameToDiffs.put("P", pAngles);

        double scale = 2 * (Math.sin(36.0 * Math.PI / 180.0) + Math
                .sin(72.0 * Math.PI / 180.0));
        for (ResidueNumber residue : mapResToTaus.keySet()) {
            AngleDifference[] taus = mapResToTaus.get(residue);
            if (taus[0] == null || taus[1] == null || taus[2] == null
                    || taus[3] == null || taus[4] == null) {
                pAngles.add(new AngleDifference(residue, Double.NaN,
                        Double.NaN, Double.NaN, "P"));
            } else {
                double y1 = taus[1].angle1 + taus[4].angle1 - taus[0].angle1
                        - taus[3].angle1;
                double x1 = taus[2].angle1 * scale;
                double tau1 = Math.atan2(y1, x1);

                double y2 = taus[1].angle2 + taus[4].angle2 - taus[0].angle2
                        - taus[3].angle2;
                double x2 = taus[2].angle2 * scale;
                double tau2 = Math.atan2(y2, x2);

                double tauDiff = DihedralAngles.subtractDihedral(tau1, tau2);
                pAngles.add(new AngleDifference(residue, tau1, tau2, tauDiff,
                        "P"));
            }
        }

        List<AngleDifference> mcqAngles = new ArrayList<>();
        mapNameToDiffs.put("MCQ", mcqAngles);

        for (ResidueNumber residue : mapResToDiffs.keySet()) {
            List<AngleDifference> list = mapResToDiffs.get(residue);
            double mcq = MCQ.calculate(list);
            mcqAngles.add(new AngleDifference(residue, Double.NaN, Double.NaN,
                    mcq, "MCQ"));
        }

        return mapNameToDiffs;
    }

    /**
     * Compare two chains.
     * 
     * @param c1
     *            First chain.
     * @param c2
     *            Second chain.
     * @param alignFirst
     *            Should atoms be aligned beforehand?
     * @return A map of name of angle to the list of differences defined upon
     *         it.
     * @throws StructureException
     *             If the alignment was impossible to be computed.
     */
    public static Map<String, List<AngleDifference>> compare(Chain c1,
            Chain c2, boolean alignFirst) throws StructureException {
        Atom[][] atoms;
        if (alignFirst) {
            atoms = StructureAligner.align(c1, c2).getAtoms();
        } else {
            atoms = Helper.getCommonAtomArray(c1, c2);
        }
        AngleType[] angles = Helper.isNucleicAcid(c1) ? NucleotideDihedral.ANGLES
                : AminoAcidDihedral.ANGLES;
        return TorsionLocalComparison.compare(atoms, angles, alignFirst);
    }

    /**
     * Compare two structures.
     * 
     * @param s1
     *            First structure.
     * @param s2
     *            Second structure.
     * @param alignFirst
     *            Should atoms be aligned beforehand?
     * @return A map of name of angle to the list of differences defined upon
     *         it.
     * @throws StructureException
     *             If the alignment was impossible to be computed.
     */
    public static Map<String, List<AngleDifference>> compare(Structure s1,
            Structure s2, boolean alignFirst) throws StructureException {
        Atom[][] atoms;
        if (alignFirst) {
            atoms = StructureAligner.align(s1, s2).getAtoms();
        } else {
            atoms = Helper.getCommonAtomArray(s1, s2);
        }
        AngleType[] angles = Helper.isNucleicAcid(s1) ? NucleotideDihedral.ANGLES
                : AminoAcidDihedral.ANGLES;
        return TorsionLocalComparison.compare(atoms, angles, alignFirst);
    }

    /**
     * A command line wrapper to compare two structures locally.
     * 
     * @param args
     *            Two paths to PDB files, then angle name (eg. MCQ), then
     *            optionally two more arguments with chain names.
     */
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        if (args.length != 2 && args.length != 3 && args.length != 5) {
            System.out.println("ERROR");
            System.out.println("Incorrect number of arguments provided");
            return;
        }

        try {
            PDBFileReader reader = new PDBFileReader();
            Structure[] structures = new Structure[] {
                    reader.getStructure(args[0]), reader.getStructure(args[1]) };
            TorsionLocalComparison comparison = new TorsionLocalComparison();

            Map<String, List<AngleDifference>> result;
            if (args.length == 5) {
                result = TorsionLocalComparison.compare(
                        structures[0].getChainByPDB(args[3]),
                        structures[1].getChainByPDB(args[4]), false);
            } else {
                result = (Map<String, List<AngleDifference>>) comparison
                        .compare(structures[0], structures[1]);
            }

            String angleName = "MCQ";
            if (args.length == 3 || args.length == 5) {
                angleName = args[2];
            }
            List<AngleDifference> list = result.get(angleName);
            AngleDifference[] array = list.toArray(new AngleDifference[list
                    .size()]);
            Arrays.sort(array);
            for (AngleDifference ad : array) {
                System.out.println(ad.residue + " " + ad.difference);
            }
        } catch (IOException | IncomparableStructuresException
                | StructureException e) {
            System.out.println("ERROR");
            System.out.println(e.getMessage());
        }
    }

    @Override
    public Object compare(Structure s1, Structure s2)
            throws IncomparableStructuresException {
        try {
            return TorsionLocalComparison.compare(s1, s2, false);
        } catch (StructureException e) {
            throw new IncomparableStructuresException(e);
        }
    }
}
