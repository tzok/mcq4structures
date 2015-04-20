package pl.poznan.put.matching;


public interface StructureMatcher {
    SelectionMatch matchSelections(StructureSelection s1, StructureSelection s2);

    FragmentMatch matchFragments(CompactFragment f1, CompactFragment f2);
}
