package pl.poznan.put.comparison;

import pl.poznan.put.interfaces.Exportable;
import pl.poznan.put.interfaces.Tabular;
import pl.poznan.put.interfaces.Visualizable;
import pl.poznan.put.matching.SelectionMatch;

public abstract class LocalComparisonResult implements Exportable, Visualizable, Tabular {
    protected final SelectionMatch matches;

    public LocalComparisonResult(SelectionMatch matches) {
        super();
        this.matches = matches;
    }

    public String getTargetName() {
        return matches.getTarget().getName();
    }

    public String getModelName() {
        return matches.getModel().getName();
    }

    public SelectionMatch getMatches() {
        return matches;
    }
}
