package pl.poznan.put.comparison.global;

import pl.poznan.put.interfaces.DisplayableExportable;
import pl.poznan.put.matching.SelectionMatch;

public interface GlobalResult extends DisplayableExportable {
  String measureName();

  SelectionMatch selectionMatch();

  double toDouble();

  default String targetName() {
    return selectionMatch().getTarget().getName();
  }

  default String modelName() {
    return selectionMatch().getModel().getName();
  }
}
