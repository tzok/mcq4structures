package pl.poznan.put.torsion;

import org.biojava.bio.structure.Group;

import pl.poznan.put.helper.UniTypeQuadruplet;

public class AngleAverageSelected extends AbstractAngleType {
    private static AngleAverageSelected instance = new AngleAverageSelected();

    public static AngleAverageSelected getInstance() {
        return AngleAverageSelected.instance;
    }

    private AngleAverageSelected() {
    }

    @Override
    public String getAngleDisplayName() {
        return "Average of selected angles";
    }

    @Override
    public String getAngleName() {
        return "AVG_SELECTED";
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
