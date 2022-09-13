package pl.poznan.put.comparison.local;

import java.util.List;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;

public interface LocalComparator {
  LocalResult comparePair(StructureSelection target, StructureSelection model);

  ModelsComparisonResult compareModels(PdbCompactFragment target, List<PdbCompactFragment> models);
}
