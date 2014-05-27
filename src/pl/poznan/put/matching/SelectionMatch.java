package pl.poznan.put.matching;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pl.poznan.put.comparison.IncomparableStructuresException;
import pl.poznan.put.interfaces.Exportable;
import pl.poznan.put.matching.FragmentSuperimposer.AtomFilter;
import pl.poznan.put.structure.CompactFragment;
import pl.poznan.put.structure.Residue;
import pl.poznan.put.structure.StructureSelection;
import pl.poznan.put.torsion.TorsionAngle;

public class SelectionMatch implements Exportable {
    private final StructureSelection parentLeft;
    private final StructureSelection parentRight;
    private final boolean matchChiByType;
    private final List<TorsionAngle> angles;
    private final List<FragmentMatch> fragmentMatches;

    public SelectionMatch(StructureSelection parentLeft,
            StructureSelection parentRight, boolean matchChiByType,
            List<TorsionAngle> angles, List<FragmentMatch> fragmentMatches) {
        super();
        this.parentLeft = parentLeft;
        this.parentRight = parentRight;
        this.matchChiByType = matchChiByType;
        this.angles = angles;
        this.fragmentMatches = fragmentMatches;
    }

    public StructureSelection getParentLeft() {
        return parentLeft;
    }

    public StructureSelection getParentRight() {
        return parentRight;
    }

    public FragmentMatch getFragmentMatch(int index) {
        return fragmentMatches.get(index);
    }

    public int getSize() {
        return fragmentMatches.size();
    }

    public String[] getResidueLabels() {
        List<String> result = new ArrayList<>();

        for (FragmentMatch fragment : fragmentMatches) {
            CompactFragment bigger = fragment.getBiggerOnlyMatched();
            CompactFragment smaller = fragment.getSmaller();

            for (int i = 0; i < fragment.getSize(); i++) {
                Residue r1 = Residue.fromGroup(bigger.getGroup(i));
                Residue r2 = Residue.fromGroup(smaller.getGroup(i));
                result.add(r1 + " - " + r2);
            }
        }

        return result.toArray(new String[result.size()]);
    }

    public String toPDB(boolean onlyMatched)
            throws IncomparableStructuresException {
        FragmentSuperposition superposition = FragmentSuperimposer.superimpose(
                this, AtomFilter.ALL, true);
        return superposition.toPDB();
    }

    @Override
    public void export(File file) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public File suggestName() {
        // TODO Auto-generated method stub
        return null;
    }
}
