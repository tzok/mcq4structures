package pl.poznan.put.matching;

import java.util.List;

import pl.poznan.put.structure.CompactFragment;
import pl.poznan.put.structure.StructureSelection;

public interface StructureMatcher {
    List<FragmentMatch> match(StructureSelection s1, StructureSelection s2);

    FragmentMatch matchFragment(CompactFragment f1, CompactFragment f2);
}
