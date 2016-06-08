package pl.poznan.put.comparison;

import org.biojava.nbio.structure.StructureException;
import pl.poznan.put.comparison.exception.IncomparableStructuresException;
import pl.poznan.put.comparison.global.GlobalComparator;
import pl.poznan.put.comparison.global.GlobalResult;
import pl.poznan.put.comparison.global.RMSDGlobalResult;
import pl.poznan.put.matching.FragmentSuperimposer;
import pl.poznan.put.matching.FragmentSuperimposer.AtomFilter;
import pl.poznan.put.matching.MCQMatcher;
import pl.poznan.put.matching.SelectionMatch;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.pdb.MmCifPdbIncompatibilityException;
import pl.poznan.put.protein.torsion.ProteinTorsionAngleType;
import pl.poznan.put.rna.torsion.RNATorsionAngleType;
import pl.poznan.put.torsion.MasterTorsionAngleType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of RMSD global similarity measure.
 *
 * @author Tomasz Zok (tzok[at]cs.put.poznan.pl)
 */
public class RMSD implements GlobalComparator {
    private final AtomFilter filter;
    private final boolean onlyHeavy;
    private final List<MasterTorsionAngleType> angleTypes;

    public RMSD() {
        super();
        filter = AtomFilter.ALL;
        onlyHeavy = true;
        angleTypes = RMSD.mainAngleTypes();
    }

    public RMSD(AtomFilter filter, boolean onlyHeavy) {
        super();
        this.filter = filter;
        this.onlyHeavy = onlyHeavy;
        angleTypes = RMSD.mainAngleTypes();
    }

    private static List<MasterTorsionAngleType> mainAngleTypes() {
        List<MasterTorsionAngleType> mainAngleTypes = new ArrayList<>();
        mainAngleTypes.addAll(Arrays.asList(RNATorsionAngleType.mainAngles()));
        mainAngleTypes.addAll(Arrays.asList(ProteinTorsionAngleType.mainAngles()));
        return mainAngleTypes;
    }

    @Override
    public GlobalResult compareGlobally(StructureSelection s1,
                                        StructureSelection s2) throws IncomparableStructuresException {
        MCQMatcher matcher = new MCQMatcher(angleTypes);
        SelectionMatch matches = matcher.matchSelections(s1, s2);

        if (matches == null || matches.getFragmentCount() == 0) {
            throw new IncomparableStructuresException("No matching fragments found");
        }

        try {
            FragmentSuperimposer superimposer = new FragmentSuperimposer(matches, filter, onlyHeavy);
            return new RMSDGlobalResult(getName(), matches, superimposer);
        } catch (StructureException e) {
            throw new IncomparableStructuresException("Failed to superimpose structures and calculate RMSD", e);
        } catch (MmCifPdbIncompatibilityException e) {
            throw new IncomparableStructuresException("Failed to superimpose structures and calculate RMSD", e);
        }
    }

    @Override
    public String getName() {
        return "RMSD";
    }

    @Override
    public boolean isAngularMeasure() {
        return false;
    }
}
