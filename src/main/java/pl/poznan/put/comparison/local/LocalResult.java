package pl.poznan.put.comparison.local;

import pl.poznan.put.interfaces.Exportable;
import pl.poznan.put.interfaces.Tabular;
import pl.poznan.put.interfaces.Visualizable;
import pl.poznan.put.matching.SelectionMatch;

public abstract class LocalResult implements Exportable, Visualizable, Tabular {
    protected final SelectionMatch selectionMatch;

    public LocalResult(SelectionMatch matches) {
        super();
        selectionMatch = matches;
    }

    public String getTargetName() {
        return selectionMatch.getTarget().getName();
    }

    public String getModelName() {
        return selectionMatch.getModel().getName();
    }

    public SelectionMatch getSelectionMatch() {
        return selectionMatch;
    }
}
