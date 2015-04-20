package pl.poznan.put.comparison;

import java.util.List;

import pl.poznan.put.matching.CompactFragment;
import pl.poznan.put.matching.StructureSelection;

/**
 * An abstraction for any local comparison measure.
 * 
 * @author Tomasz Zok (tzok[at]cs.put.poznan.pl)
 */
public interface LocalComparator {
    LocalComparisonResult comparePair(StructureSelection target,
            StructureSelection model) throws IncomparableStructuresException;

    ModelsComparisonResult compareModels(CompactFragment target,
            List<CompactFragment> models)
            throws IncomparableStructuresException;
}
