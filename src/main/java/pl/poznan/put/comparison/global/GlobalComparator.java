package pl.poznan.put.comparison.global;

import pl.poznan.put.matching.StructureSelection;

public interface GlobalComparator {
  String getName();

  boolean isAngularMeasure();

  GlobalResult compareGlobally(StructureSelection s1, StructureSelection s2);
}
