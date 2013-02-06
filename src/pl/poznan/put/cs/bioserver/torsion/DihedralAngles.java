package pl.poznan.put.cs.bioserver.torsion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to calculate and manage dihedral angles for given BioJava structure.
 * 
 * @author Tomasz Żok (tzok[at]cs.put.poznan.pl)
 */
public final class DihedralAngles {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(DihedralAngles.class);
    private static Map<Integer, Map<Atom, Integer>> reverseMapCache = new HashMap<>();

    /**
     * Calculate all angle differences for given angle type.
     * 
     * @param atoms
     *            Two arrays of atoms for each structure.
     * @param angleType
     *            Which angle should be checked.
     * @param wasAligned
     *            Was there alignment before?
     * @return A list of angle differences.
     */
    public static List<AngleDifference> calculateAngleDiff(Atom[][] atoms,
            AngleType angleType, boolean wasAligned) {
        List<Quadruplet> quads1 = DihedralAngles.getQuadruplets(atoms[0],
                angleType);
        List<Quadruplet> quads2 = DihedralAngles.getQuadruplets(atoms[1],
                angleType);
        DihedralAngles.LOGGER.debug("Processing angle: "
                + angleType.getAngleName() + ". Atom count: " + atoms[0].length
                + " " + atoms[1].length + ". Quadruplets found: "
                + quads1.size() + " " + quads2.size());

        if (quads1.size() < quads2.size()) {
            List<Quadruplet> quadsTmp = quads1;
            quads1 = quads2;
            quads2 = quadsTmp;
        }

        // FIXME: this all can be greatly improved!
        List<AngleDifference> differences = new ArrayList<>();
        for (int i = 0; i < quads1.size(); i++) {
            Quadruplet q1 = quads1.get(i);
            boolean found = false;
            for (int j = 0; j < quads2.size(); j++) {
                Quadruplet q2 = quads2.get(j);

                if (q1.isCorresponding(q2, wasAligned)) {
                    AngleDifference diff = new AngleDifference(q1.getAtoms(),
                            q2.getAtoms(), angleType.getAngleName());
                    differences.add(diff);
                    found = true;
                    break;
                }
            }

            if (!found) {
                AngleDifference diff = new AngleDifference(q1.getAtoms(),
                        new Atom[4], angleType.getAngleName());
                differences.add(diff);
            }
        }
        return differences;
    }

    /**
     * Calculate one dihedral angle value. By default use the atan method.
     * 
     * @param a1
     *            Atom 1.
     * @param a2
     *            Atom 2.
     * @param a3
     *            Atom 3.
     * @param a4
     *            Atom 4.
     * @return Value of the torsion angle.
     */
    private static double calculateDihedral(Atom a1, Atom a2, Atom a3, Atom a4) {
        return DihedralAngles.calculateDihedralAtan(a1, a2, a3, a4);
    }

    /**
     * Calculate one dihedral angle value. By default use the atan method.
     * 
     * @param atoms
     *            A 4-tuple of atoms.
     * @return Value of the tosion angle.
     */
    static double calculateDihedral(Atom[] atoms) {
        return DihedralAngles.calculateDihedral(atoms[0], atoms[1], atoms[2],
                atoms[3]);
    }

    /**
     * Calculate one dihedral angle value for given four atoms. Use cos^-1 and a
     * check for pseudovector
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
    public static double calculateDihedralAcos(Atom a1, Atom a2, Atom a3,
            Atom a4) {
        if (a1 == null || a2 == null || a3 == null || a4 == null) {
            return Double.NaN;
        }

        Vector3D d1 = new Vector3D(a1, a2);
        Vector3D d2 = new Vector3D(a2, a3);
        Vector3D d3 = new Vector3D(a3, a4);

        Vector3D u1 = d1.cross(d2);
        Vector3D u2 = d2.cross(d3);

        double ctor = u1.dot(u2) / Math.sqrt(u1.dot(u1) * u2.dot(u2));
        ctor = ctor < -1 ? -1 : ctor > 1 ? 1 : ctor;
        double torp = Math.acos(ctor);
        if (u1.dot(u2.cross(d2)) < 0) {
            torp = -torp;
        }
        return torp;
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
    private static double calculateDihedralAtan(Atom a1, Atom a2, Atom a3,
            Atom a4) {
        if (a1 == null || a2 == null || a3 == null || a4 == null) {
            return Double.NaN;
        }

        Vector3D v1 = new Vector3D(a1, a2);
        Vector3D v2 = new Vector3D(a2, a3);
        Vector3D v3 = new Vector3D(a3, a4);

        Vector3D tmp1 = v1.cross(v2);
        Vector3D tmp2 = v2.cross(v3);
        Vector3D tmp3 = v1.scale(v2.length());
        return Math.atan2(tmp3.dot(tmp2), tmp1.dot(tmp2));
    }

    private static List<Quadruplet> getQuadruplets(Atom[] atoms,
            AngleType angleType) {
        int hashCode = Arrays.hashCode(atoms);
        if (!DihedralAngles.reverseMapCache.containsKey(hashCode)) {
            DihedralAngles.reverseMapCache.put(hashCode,
                    DihedralAngles.makeReverseMap(atoms));
        }
        Map<Atom, Integer> reverseMap = DihedralAngles.reverseMapCache
                .get(hashCode);

        int[] groupRule = angleType.getGroupRule();
        List<List<Atom>> found = new ArrayList<>();
        for (int j = 0; j < 4; j++) {
            List<Atom> list = new ArrayList<>();
            found.add(list);
        }

        for (Atom atom : atoms) {
            if (atom == null) {
                continue;
            }
            String name = atom.getFullName();
            String[] atomNames = angleType.getAtomNames(atom.getGroup());
            for (int k = 0; k < 4; k++) {
                if (name.equals(atomNames[k])) {
                    found.get(k).add(atom);
                }
            }
        }

        List<Quadruplet> filtered = new ArrayList<>();
        int size = found.get(0).size();
        for (int j = 0; j < size; j++) {
            Atom refAtom = found.get(0).get(j);
            Group refGroup = refAtom.getGroup();
            int refId = refGroup.getResidueNumber().getSeqNum();
            String refChain = refGroup.getChainId();

            List<Atom> quad = new ArrayList<>();
            quad.add(refAtom);
            for (int k = 1; k < 4; k++) {
                for (Atom atom : found.get(k)) {
                    Group group = atom.getGroup();
                    int id = group.getResidueNumber().getSeqNum();
                    String chain = group.getChainId();
                    if (id - refId == groupRule[k] && refChain.equals(chain)) {
                        quad.add(atom);
                        break;
                    }
                }
            }

            Atom[] array = quad.toArray(new Atom[quad.size()]);
            if (quad.size() == 4) {
                int[] indices = new int[4];
                for (int k = 0; k < 4; k++) {
                    indices[k] = reverseMap.get(array[k]);
                }
                filtered.add(new Quadruplet(array, indices));
            } else {
                DihedralAngles.LOGGER.debug("Quad not found, got only "
                        + quad.size() + " atoms. Angle: "
                        + angleType.getAngleName()
                        + ". Residue of first atom: "
                        + quad.get(0).getGroup().getResidueNumber()
                        + ". Atoms: " + Arrays.toString(array));
            }
        }
        return filtered;
    }

    private static Map<Atom, Integer> makeReverseMap(Atom[] atoms) {
        Map<Atom, Integer> map = new HashMap<>();
        for (int i = 0; i < atoms.length; i++) {
            map.put(atoms[i], i);
        }
        return map;
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
    public static double subtractDihedral(double a1, double a2) {
        double diff;
        // both angles are NaN, reward!
        if (Double.isNaN(a1) && Double.isNaN(a2)) {
            diff = 0;
        } else if (Double.isNaN(a1) && !Double.isNaN(a2) || !Double.isNaN(a1)
                && Double.isNaN(a2)) {
            diff = Math.PI;
        } else {
            double full = 2 * Math.PI;
            double a1Mod = (a1 + full) % full;
            double a2Mod = (a2 + full) % full;
            diff = Math.abs(a1Mod - a2Mod);
            diff = Math.min(diff, full - diff);
        }
        return diff;
    }

    private DihedralAngles() {
    }
}
