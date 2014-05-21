package pl.poznan.put.matching;

import java.util.List;

import pl.poznan.put.common.TorsionAngle;

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

    public boolean isMatchChiByType() {
        return matchChiByType;
    }

    public List<TorsionAngle> getAngles() {
        return angles;
    }

    public List<FragmentMatch> getFragmentMatches() {
        return fragmentMatches;
    }

    public int getSize() {
        return fragmentMatches.size();
    }
}
