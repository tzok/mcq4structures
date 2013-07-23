package pl.poznan.put.cs.bioserver.torsion;

import org.biojava.bio.structure.Group;

import pl.poznan.put.cs.bioserver.helper.UniTypeQuadruplet;

public class AnglePseudophasePucker implements AngleType {
    private static AnglePseudophasePucker instance;

    public static AnglePseudophasePucker getInstance() {
        if (AnglePseudophasePucker.instance == null) {
            AnglePseudophasePucker.instance = new AnglePseudophasePucker();
        }
        return AnglePseudophasePucker.instance;
    }

    private AnglePseudophasePucker() {
    }

    @Override
    public String getAngleDisplayName() {
        return "P (sugar pucker)";
    }

    @Override
    public String getAngleName() {
        return "P";
    }

    @Override
    public UniTypeQuadruplet<String> getAtomNames(Group residue) {
        throw new UnsupportedOperationException(
                "An average of angular values is a meta-entry "
                        + "which is not bound to specific atoms");
    }

    @Override
    public UniTypeQuadruplet<Integer> getGroupRule() {
        throw new UnsupportedOperationException(
                "An average of angular values is a meta-entry "
                        + "which is not bound to specific atoms");
    }
}
