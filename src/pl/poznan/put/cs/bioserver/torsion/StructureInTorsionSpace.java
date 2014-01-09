package pl.poznan.put.cs.bioserver.torsion;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections15.map.MultiKeyMap;
import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.Calc;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.ResidueNumber;
import org.biojava.bio.structure.StructureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.poznan.put.cs.bioserver.helper.UniTypeQuadruplet;

public class StructureInTorsionSpace {
    // allow the distance between two centers of mass of residues to be at most
    // 12 anstroms
    private static final double MAX_ALLOWED_DISTANCE_SQUARED = 12 * 12;
    private static final Logger LOGGER = LoggerFactory
            .getLogger(StructureInTorsionSpace.class);

    // two keys: int indexOfResidue, AngleType angleType
    // value: double torsionAngle
    private MultiKeyMap<Object, Double> torsionAngles = new MultiKeyMap<>();

    private List<Group> residues;
    private List<AngleType> angleTypes;

    public StructureInTorsionSpace(List<Group> residues) {
        super();
        this.residues = new ArrayList<>(residues);

        analyze();
    }

    private void analyze() {
        Set<String> atomNames = new HashSet<>();
        atomNames.addAll(NucleotideDihedral.getUsedAtoms());
        atomNames.addAll(AminoAcidDihedral.getUsedAtoms());

        /*
         * Filter out all residues which does not contain any useful atom
         */
        List<Integer> toDelete = new ArrayList<>();

        for (int i = 0; i < residues.size(); i++) {
            Group g = residues.get(i);
            boolean isUseful = false;

            for (Atom a : g.getAtoms()) {
                if (atomNames.contains(a.getFullName())) {
                    isUseful = true;
                    break;
                }
            }

            if (!isUseful) {
                toDelete.add(i);
            }
        }

        for (int i = toDelete.size() - 1; i >= 0; i--) {
            int index = toDelete.get(i);
            residues.remove(index);
        }

        /*
         * Store information about atoms (which and where)
         */
        MultiKeyMap<Object, Atom> nameIndexAtom = new MultiKeyMap<>();

        for (int i = 0; i < residues.size(); i++) {
            Group g = residues.get(i);

            for (Atom a : g.getAtoms()) {
                String name = a.getFullName();

                if (atomNames.contains(name)) {
                    nameIndexAtom.put(name, i, a);
                }
            }
        }

        /*
         * if isConnected[i] == true, then residue i-th and (i+1)-th are linked!
         */
        boolean[] isConnected = new boolean[residues.size() - 1];

        for (int i = 0; i < residues.size() - 1; i++) {
            Group g1 = residues.get(i);
            Group g2 = residues.get(i + 1);

            Atom c1 = Calc.getCentroid(g1.getAtoms().toArray(new Atom[0]));
            Atom c2 = Calc.getCentroid(g2.getAtoms().toArray(new Atom[0]));

            try {
                isConnected[i] = Calc.getDistanceFast(c1, c2) < StructureInTorsionSpace.MAX_ALLOWED_DISTANCE_SQUARED;

                if (!isConnected[i]) {
                    StructureInTorsionSpace.LOGGER
                            .debug("These residues were found to break the helix: "
                                    + g1 + " " + g2);
                }
            } catch (StructureException e) {
                StructureInTorsionSpace.LOGGER.warn("Failed to calculate "
                        + "distance between residues", e);
                // do nothing
            }
        }

        /*
         * look for every residue and every supported angle type... try to get
         * four required atoms (if present and if residues required are
         * connected)
         */
        angleTypes = new ArrayList<>();
        angleTypes.addAll(NucleotideDihedral.getAngles());
        angleTypes.addAll(AminoAcidDihedral.getAngles());

        for (int i = 0; i < residues.size(); i++) {
            Group g = residues.get(i);

            for (AngleType at : angleTypes) {
                UniTypeQuadruplet<String> q1 = at.getAtomNames(g);
                UniTypeQuadruplet<Integer> q2 = at.getGroupRule();

                Atom[] atoms = new Atom[4];
                int j = 0;

                for (j = 0; j < 4; j++) {
                    int next = q2.get(j);

                    if (next != 0) {
                        int k = next + i;
                        if (k < 0 || k >= isConnected.length || !isConnected[k]) {
                            break;
                        }
                    }

                    atoms[j] = nameIndexAtom.get(q1.get(j), next + i);
                    if (atoms[j] == null) {
                        break;
                    }
                }

                if (j != 4) {
                    torsionAngles.put(i, at, Double.NaN);
                    continue;
                }

                double angleValue = DihedralAngles
                        .calculateDihedral(new UniTypeQuadruplet<>(atoms));
                torsionAngles.put(i, at, angleValue);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < residues.size(); i++) {
            Group g = residues.get(i);
            ResidueNumber residueNumber = g.getResidueNumber();
            builder.append(residueNumber.toPDB());
            builder.append('\t');

            for (AngleType at : angleTypes) {
                Double angle = torsionAngles.get(i, at);
                builder.append(Math.toDegrees(angle));
                builder.append('\t');
            }

            builder.append('\n');
        }

        return builder.toString();
    }
}
