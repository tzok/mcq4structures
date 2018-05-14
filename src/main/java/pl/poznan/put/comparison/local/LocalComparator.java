package pl.poznan.put.comparison.local;

import pl.poznan.put.comparison.exception.IncomparableStructuresException;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;

import java.util.List;

public interface LocalComparator {
  LocalResult comparePair(StructureSelection target, StructureSelection model)
      throws IncomparableStructuresException;

  ModelsComparisonResult compareModels(PdbCompactFragment target, List<PdbCompactFragment> models)
      throws IncomparableStructuresException;
}
