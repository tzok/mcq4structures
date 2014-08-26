package pl.poznan.put.matching;

import pl.poznan.put.common.MoleculeType;
import pl.poznan.put.structure.CompactFragment;

public class FragmentMatch {
    private final CompactFragment target;
    private final CompactFragment model;
    private final boolean isTargetSmaller;
    private final int shift;
    private final FragmentComparison comparison;

    public FragmentMatch(CompactFragment target, CompactFragment model,
            boolean isTargetSmaller, int shift, FragmentComparison comparison) {
        super();
        this.target = target;
        this.model = model;
        this.isTargetSmaller = isTargetSmaller;
        this.shift = shift;
        this.comparison = comparison;
    }

    public FragmentComparison getFragmentComparison() {
        return comparison;
    }

    public MoleculeType getMoleculeType() {
        assert target.getMoleculeType() == model.getMoleculeType();
        return target.getMoleculeType();
    }

    public String[] getResidueLabels() {
        CompactFragment l = target;
        CompactFragment r = model;

        if (isTargetSmaller) {
            l = CompactFragment.createShifted(l, shift, r.getSize());
        } else {
            r = CompactFragment.createShifted(r, shift, l.getSize());
        }

        String[] result = new String[l.getSize()];

        for (int i = 0; i < l.getSize(); i++) {
            result[i] = l.getResidue(i) + " - " + r.getResidue(i);
        }

        return result;
    }

    @Override
    public String toString() {
        CompactFragment targetFragment;
        CompactFragment modelFragment;

        if (isTargetSmaller) {
            targetFragment = target;
            modelFragment = CompactFragment.createShifted(model, shift,
                    target.getSize());
        } else {
            targetFragment = CompactFragment.createShifted(target, shift,
                    model.getSize());
            modelFragment = model;
        }

        return isTargetSmaller + "\t" + targetFragment + "\t" + modelFragment;
    }

    public int getSize() {
        return comparison.getSize();
    }

    public CompactFragment getTarget() {
        return target;
    }

    public CompactFragment getModel() {
        return model;
    }
}
