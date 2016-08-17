package pl.poznan.put.matching;

import org.apache.commons.collections4.IteratorUtils;
import pl.poznan.put.circular.Angle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class MatchCollectionDeltaIterator implements AngleDeltaIterator {
    private final Iterator<ResidueComparison> iterator;

    public MatchCollectionDeltaIterator(MatchCollection matchCollection) {
        super();

        Collection<Iterator<? extends ResidueComparison>> iterators =
                new ArrayList<>();
        for (FragmentMatch fragmentMatch : matchCollection
                .getFragmentMatches()) {
            iterators.add(fragmentMatch.getResidueComparisons().iterator());
        }
        iterator = IteratorUtils.chainedIterator(iterators);
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