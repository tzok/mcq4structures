package pl.poznan.put.comparison.global;

import pl.poznan.put.interfaces.DisplayableExportable;
import pl.poznan.put.matching.SelectionMatch;

public abstract class GlobalResult implements DisplayableExportable {
  private final String measureName;
  private final SelectionMatch selectionMatch;

  GlobalResult(final String measureName, final SelectionMatch matches) {
    super();
    this.measureName = measureName;
    selectionMatch = matches;
  }

  public final String getMeasureName() {
    return measureName;
  }

  public final String getTargetName() {
    return selectionMatch.getTarget().getName();
  }

  public final String getModelName() {
    return selectionMatch.getModel().getName();
  }

  public final SelectionMatch getSelectionMatch() {
    return selectionMatch;
  }

  public abstract double asDouble();
}
