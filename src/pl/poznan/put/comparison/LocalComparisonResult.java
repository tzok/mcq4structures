package pl.poznan.put.comparison;

import java.util.List;

import pl.poznan.put.interfaces.Exportable;
import pl.poznan.put.interfaces.Visualizable;
import pl.poznan.put.matching.FragmentMatch;

public abstract class LocalComparisonResult implements Exportable, Visualizable {
    protected final String nameLeft;
    protected final String nameRight;
    protected final List<FragmentMatch> matches;

    public LocalComparisonResult(String nameLeft, String nameRight,
            List<FragmentMatch> matches) {
        super();
        this.nameLeft = nameLeft;
        this.nameRight = nameRight;
        this.matches = matches;
    }

    public String getNameLeft() {
        return nameLeft;
    }

    public String getNameRight() {
        return nameRight;
    }

    public List<FragmentMatch> getMatches() {
        return matches;
    }
}
