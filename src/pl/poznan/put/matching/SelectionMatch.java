package pl.poznan.put.matching;

import java.util.ArrayList;
import java.util.List;

import pl.poznan.put.structure.CompactFragment;
import pl.poznan.put.structure.Residue;
import pl.poznan.put.torsion.TorsionAngle;

public class SelectionMatch {
    private final boolean matchChiByType;
    private final List<TorsionAngle> angles;
    private final List<FragmentMatch> fragmentMatches;

    public SelectionMatch(boolean matchChiByType, List<TorsionAngle> angles,
            List<FragmentMatch> fragmentMatches) {
        super();
        this.matchChiByType = matchChiByType;
        this.angles = angles;
        this.fragmentMatches = fragmentMatches;
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
}
