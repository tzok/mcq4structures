package pl.poznan.put.torsion;

import org.biojava.bio.structure.Group;

import pl.poznan.put.helper.UniTypeQuadruplet;

public class AnglePseudophasePucker extends AbstractAngleType {
    private static AnglePseudophasePucker instance = new AnglePseudophasePucker();

    public static AnglePseudophasePucker getInstance() {
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
