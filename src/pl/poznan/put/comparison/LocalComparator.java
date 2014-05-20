package pl.poznan.put.comparison;

import pl.poznan.put.structure.StructureSelection;

/**
 * An abstraction for any local comparison measure.
 * 
 * @author Tomasz Zok (tzok[at]cs.put.poznan.pl)
 */
public interface LocalComparator {
    LocalComparisonResult compareLocally(StructureSelection s1,
            StructureSelection s2) throws IncomparableStructuresException;
}
