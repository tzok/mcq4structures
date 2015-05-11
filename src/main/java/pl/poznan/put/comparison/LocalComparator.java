package pl.poznan.put.comparison;

import java.util.List;

import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;

/**
 * An abstraction for any local comparison measure.
 * 
 * @author Tomasz Zok (tzok[at]cs.put.poznan.pl)
 */
public interface LocalComparator {
    LocalComparisonResult comparePair(StructureSelection target,
            StructureSelection model) throws IncomparableStructuresException;

    ModelsComparisonResult compareModels(PdbCompactFragment target,
            List<PdbCompactFragment> models)
            throws IncomparableStructuresException;
}
