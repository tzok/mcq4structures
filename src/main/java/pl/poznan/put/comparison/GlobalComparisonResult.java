package pl.poznan.put.comparison;

import pl.poznan.put.interfaces.DisplayableExportable;
import pl.poznan.put.matching.SelectionMatch;

public abstract class GlobalComparisonResult implements DisplayableExportable {
    private final String measureName;
    private final SelectionMatch matches;

    protected GlobalComparisonResult(String measureName, SelectionMatch matches) {
        super();
        this.measureName = measureName;
        this.matches = matches;
    }

    public String getMeasureName() {
        return measureName;
    }

    public String getTargetName() {
        return matches.getTarget().getName();
    }

    public String getModelName() {
        return matches.getModel().getName();
    }

    public SelectionMatch getSelectionMatch() {
        return matches;
    }
}
