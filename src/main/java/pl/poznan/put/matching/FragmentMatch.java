package pl.poznan.put.matching;

import java.util.List;

import pl.poznan.put.circular.exception.InvalidCircularValueException;
import pl.poznan.put.common.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.pdb.analysis.PdbResidue;

public class FragmentMatch {
    private final PdbCompactFragment targetFragment;
    private final PdbCompactFragment modelFragment;
    private final boolean isTargetSmaller;
    private final int shift;
    private final FragmentComparison fragmentComparison;

    public FragmentMatch(PdbCompactFragment targetFragment,
            PdbCompactFragment modelFragment, boolean isTargetSmaller,
            int shift, FragmentComparison comparison) {
        super();
        this.targetFragment = targetFragment;
        this.modelFragment = modelFragment;
        this.isTargetSmaller = isTargetSmaller;
        this.shift = shift;
        this.fragmentComparison = comparison;
    }

    public PdbCompactFragment getTargetFragment() {
        return targetFragment;
    }

    public PdbCompactFragment getModelFragment() {
        return modelFragment;
    }

    public boolean isTargetSmaller() {
        return isTargetSmaller;
    }

    public int getShift() {
        return shift;
    }

    public FragmentComparison getFragmentComparison() {
        return fragmentComparison;
    }

    @Override
    public String toString() {
        PdbCompactFragment target;
        PdbCompactFragment model;

        if (isTargetSmaller) {
            target = targetFragment;
            model = modelFragment.shift(shift, targetFragment.size());
        } else {
            target = targetFragment.shift(shift, modelFragment.size());
            model = modelFragment;
        }

        return target + " & " + model;
    }

    public MoleculeType moleculeType() {
        assert targetFragment.moleculeType() == modelFragment.moleculeType();
        return targetFragment.moleculeType();
    }

    public String[] getResidueLabels() throws InvalidCircularValueException {
        PdbCompactFragment target = targetFragment;
        PdbCompactFragment model = modelFragment;

        if (isTargetSmaller) {
            model = model.shift(shift, target.size());
        } else {
            target = target.shift(shift, model.size());
        }

        List<PdbResidue> targetResidues = target.getResidues();
        List<PdbResidue> modelResidues = model.getResidues();

        String[] result = new String[target.size()];

        for (int i = 0; i < target.size(); i++) {
            PdbResidue lname = targetResidues.get(i);
            PdbResidue rname = modelResidues.get(i);

            if (lname.equals(rname)) {
                result[i] = lname.toString();
            } else {
                result[i] = lname + " - " + rname;
            }
        }

        return result;
    }
}
