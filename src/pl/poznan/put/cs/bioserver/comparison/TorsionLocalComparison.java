package pl.poznan.put.cs.bioserver.comparison;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.ResidueNumber;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.StructureImpl;
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
    private static final int TAU_COUNT = 5;

    private static List<AngleDifference> calcAngleP(
            Map<ResidueNumber, AngleDifference[]> mapResToTaus) {
        List<AngleDifference> pAngles = new ArrayList<>();
        // the formula taken from literature
        double scale = 2 * (Math.sin(36.0 * Math.PI / 180.0) + Math
                .sin(72.0 * Math.PI / 180.0));
        for (Entry<ResidueNumber, AngleDifference[]> entry : mapResToTaus
                .entrySet()) {
            ResidueNumber residue = entry.getKey();
            AngleDifference[] taus = entry.getValue();

            // if any of TAUx angle is null, abort calculations
            boolean flag = false;
            for (int i = 0; i < TorsionLocalComparison.TAU_COUNT; i++) {
                flag |= taus[i] == null;
            }

            if (flag) {
                pAngles.add(new AngleDifference(residue, Double.NaN,
                        Double.NaN, Double.NaN, "P"));
            } else {
                double y1 = taus[1].getAngleFirst() + taus[4].getAngleFirst()
                        - taus[0].getAngleFirst() - taus[3].getAngleFirst();
                double x1 = taus[2].getAngleFirst() * scale;
                double tau1 = Math.atan2(y1, x1);

                double y2 = taus[1].getAngleSecond() + taus[4].getAngleSecond()
                        - taus[0].getAngleSecond() - taus[3].getAngleSecond();
                double x2 = taus[2].getAngleSecond() * scale;
                double tau2 = Math.atan2(y2, x2);

                double tauDiff = DihedralAngles.subtractDihedral(tau1, tau2);
                pAngles.add(new AngleDifference(residue, tau1, tau2, tauDiff,
                        "P"));
            }
        }
        return pAngles;
    }

    private static List<AngleDifference> calcMcqPerResidue(
            Map<ResidueNumber, List<AngleDifference>> mapResToDiffs) {
        List<AngleDifference> mcqAngles = new ArrayList<>();
        for (Entry<ResidueNumber, List<AngleDifference>> entry : mapResToDiffs
                .entrySet()) {
            ResidueNumber residue = entry.getKey();
            List<AngleDifference> list = entry.getValue();

            double mcq = MCQ.calculate(list);
            mcqAngles.add(new AngleDifference(residue, Double.NaN, Double.NaN,
                    mcq, "MCQ"));
        }
        return mcqAngles;
    }

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
    private static Map<String, List<AngleDifference>> compare(Atom[][] atoms,
            AngleType[] angles, boolean wasAligned) {
        Atom[][] equalized = Helper.equalize(atoms);

        List<AngleDifference> allDiffs = new ArrayList<>();
        for (AngleType at : angles) {
            allDiffs.addAll(DihedralAngles.calculateAngleDiff(equalized, at,
                    wasAligned));
        }

        Map<ResidueNumber, List<AngleDifference>> mapResToDiffs = new HashMap<>();
        Map<ResidueNumber, AngleDifference[]> mapResToTaus = new HashMap<>();
        Map<String, List<AngleDifference>> mapNameToDiffs = new LinkedHashMap<>();
        for (AngleDifference diff : allDiffs) {
            ResidueNumber residue = diff.getResidue();
            String angleName = diff.getAngleName();

            if (!mapResToDiffs.containsKey(residue)) {
                mapResToDiffs.put(residue, new ArrayList<AngleDifference>());
                mapResToTaus.put(residue,
                        new AngleDifference[TorsionLocalComparison.TAU_COUNT]);
            }
            List<AngleDifference> list = mapResToDiffs.get(residue);
            list.add(diff);

            if (angleName.startsWith("TAU")) {
                AngleDifference[] taus = mapResToTaus.get(residue);
                int which = angleName.charAt(3) - '0';
                taus[which] = diff;
            }

            if (!mapNameToDiffs.containsKey(angleName)) {
                mapNameToDiffs.put(angleName, new ArrayList<AngleDifference>());
            }
            list = mapNameToDiffs.get(angleName);
            list.add(diff);
        }

        mapNameToDiffs
                .put("P", TorsionLocalComparison.calcAngleP(mapResToTaus));
        mapNameToDiffs.put("AVERAGE",
                TorsionLocalComparison.calcMcqPerResidue(mapResToDiffs));
        return mapNameToDiffs;
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
        boolean wasAligned = alignFirst;
        Atom[][] atoms;
        if (alignFirst) {
            atoms = StructureAligner.align(s1, s2).getAtoms();
        } else {
            atoms = Helper.getCommonAtomArray(s1, s2, false);
            if (atoms == null) {
                atoms = Helper.getCommonAtomArray(s1, s2, true);
                wasAligned = true;
            }
        }
        AngleType[] angles = Helper.isNucleicAcid(s1) ? NucleotideDihedral
                .getAngles() : AminoAcidDihedral.getAngles();
        return TorsionLocalComparison.compare(atoms, angles, wasAligned);
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
                result = TorsionLocalComparison
                        .compare(
                                new StructureImpl(structures[0]
                                        .getChainByPDB(args[3])),
                                new StructureImpl(structures[1]
                                        .getChainByPDB(args[4])), false);
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
                System.out.println(ad.getResidue() + " " + ad.getDifference());
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
