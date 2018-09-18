package pl.poznan.put.comparison.global;

import pl.poznan.put.interfaces.DisplayableExportable;
import pl.poznan.put.matching.SelectionMatch;

public abstract class GlobalResult implements DisplayableExportable {
  private final String measureName;
  private final SelectionMatch selectionMatch;

  protected GlobalResult(String measureName, SelectionMatch matches) {
    super();
    this.measureName = measureName;
    this.selectionMatch = matches;
  }

  public String getMeasureName() {
    return measureName;
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

  public abstract double asDouble();
}
