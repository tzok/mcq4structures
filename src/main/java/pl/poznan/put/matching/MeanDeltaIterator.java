package pl.poznan.put.matching;

import java.util.Iterator;

import pl.poznan.put.circular.Angle;

public class MeanDeltaIterator implements AngleDeltaIterator {
    private final Iterator<ResidueComparison> iterator;

    public MeanDeltaIterator(FragmentMatch fragmentMatch) {
        iterator = fragmentMatch.getResidueComparisons().iterator();
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public Angle next() {
        return iterator.next().getMeanDirection();
    }

    @Override
    public void remove() {
        iterator.remove();
    }
}