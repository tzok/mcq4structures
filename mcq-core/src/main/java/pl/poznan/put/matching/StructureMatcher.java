package pl.poznan.put.matching;

import pl.poznan.put.pdb.analysis.PdbCompactFragment;

public interface StructureMatcher {
  SelectionMatch matchSelections(StructureSelection s1, StructureSelection s2);

  FragmentMatch matchFragments(PdbCompactFragment f1, PdbCompactFragment f2);
}
