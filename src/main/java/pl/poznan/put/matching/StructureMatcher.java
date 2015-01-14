package pl.poznan.put.matching;

import pl.poznan.put.structure.CompactFragment;
import pl.poznan.put.structure.StructureSelection;

public interface StructureMatcher {
    SelectionMatch matchSelections(StructureSelection s1, StructureSelection s2);

    FragmentMatch matchFragments(CompactFragment f1, CompactFragment f2);
}
