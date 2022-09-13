package pl.poznan.put.matching;

import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;
import org.apache.commons.collections4.IteratorUtils;
import pl.poznan.put.circular.Angle;
import pl.poznan.put.circular.samples.ImmutableAngleSample;

public class MatchCollectionDeltaIterator implements AngleDeltaIterator {
  private final Iterator<ResidueComparison> iterator;

  public MatchCollectionDeltaIterator(final MatchCollection matchCollection) {
    super();

    final Collection<Iterator<? extends ResidueComparison>> iterators =
        matchCollection.getFragmentMatches().stream()
            .map(fragmentMatch -> fragmentMatch.getResidueComparisons().iterator())
            .collect(Collectors.toList());
    iterator = IteratorUtils.chainedIterator(iterators);
  }

  @Override
  public final boolean hasNext() {
    return iterator.hasNext();
  }

  @Override
  public final Angle next() {
    return ImmutableAngleSample.of(iterator.next().validDeltas()).meanDirection();
  }

  @Override
  public final void remove() {
    iterator.remove();
  }
}
