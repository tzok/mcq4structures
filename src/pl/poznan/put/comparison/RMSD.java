package pl.poznan.put.comparison;

import org.biojava.bio.structure.StructureException;

import pl.poznan.put.matching.FragmentSuperimposer;
import pl.poznan.put.matching.FragmentSuperimposer.AtomFilter;
import pl.poznan.put.matching.MCQMatcher;
import pl.poznan.put.matching.SelectionMatch;
import pl.poznan.put.structure.StructureSelection;

/**
 * Implementation of RMSD global similarity measure.
 * 
 * @author Tomasz Zok (tzok[at]cs.put.poznan.pl)
 */
public class RMSD implements GlobalComparator {
    private AtomFilter filter;
    private boolean onlyHeavy;

    public RMSD(AtomFilter filter, boolean onlyHeavy) {
        super();
        this.filter = filter;
        this.onlyHeavy = onlyHeavy;
    }

    public AtomFilter getFilter() {
        return filter;
    }

    public void setFilter(AtomFilter filter) {
        this.filter = filter;
    }

    public boolean isOnlyHeavy() {
        return onlyHeavy;
    }

    public void setOnlyHeavy(boolean onlyHeavy) {
        this.onlyHeavy = onlyHeavy;
    }

    @Override
    public GlobalComparisonResult compareGlobally(StructureSelection s1,
            StructureSelection s2) throws IncomparableStructuresException {
        MCQMatcher matcher = new MCQMatcher(true,
                MCQ.getAllAvailableTorsionAngles());
        SelectionMatch matches = matcher.matchSelections(s1, s2);

        if (matches == null || matches.getSize() == 0) {
            throw new IncomparableStructuresException("No matching fragments "
                    + "found");
        }

        try {
            FragmentSuperimposer superimposer = new FragmentSuperimposer(
                    matches, filter, onlyHeavy);
            return new GlobalComparisonResult(getName(), s1.getName(),
                    s2.getName(), matches, superimposer.getRMSD(), false);
        } catch (StructureException e) {
            throw new IncomparableStructuresException("Failed to superimpose "
                    + "structures and calculate RMSD", e);
        }
    }

    @Override
    public String getName() {
        return "RMSD";
    }
}
