package pl.poznan.put.comparison.local;

import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;

import java.util.List;

public interface LocalComparator {
  LocalResult comparePair(StructureSelection target, StructureSelection model);

  ModelsComparisonResult compareModels(
      PdbCompactFragment target, List<? extends PdbCompactFragment> models);
}
