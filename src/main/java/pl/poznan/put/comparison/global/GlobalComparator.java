package pl.poznan.put.comparison.global;

import pl.poznan.put.matching.StructureSelection;

public interface GlobalComparator {
  GlobalResult compareGlobally(StructureSelection s1, StructureSelection s2)
          ;

  String getName();

  boolean isAngularMeasure();
}
