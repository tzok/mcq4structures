package pl.poznan.put.comparison;

import pl.poznan.put.interfaces.Exportable;
import pl.poznan.put.interfaces.Visualizable;
import pl.poznan.put.matching.SelectionMatch;

public abstract class LocalComparisonResult implements Exportable, Visualizable {
    protected final String nameLeft;
    protected final String nameRight;
    protected final SelectionMatch matches;

    public LocalComparisonResult(String nameLeft, String nameRight,
            SelectionMatch matches) {
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

    public SelectionMatch getMatches() {
        return matches;
    }
}
