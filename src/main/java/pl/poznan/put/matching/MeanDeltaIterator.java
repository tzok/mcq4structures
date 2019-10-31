package pl.poznan.put.matching;

import pl.poznan.put.circular.Angle;

import java.util.Iterator;

class MeanDeltaIterator implements AngleDeltaIterator {
  private final Iterator<ResidueComparison> iterator;

  MeanDeltaIterator(final FragmentMatch fragmentMatch) {
    super();
    iterator = fragmentMatch.getResidueComparisons().iterator();
  }

  @Override
  public final boolean hasNext() {
    return iterator.hasNext();
  }

  @Override
  public final Angle next() {
    return iterator.next().getMeanDirection();
  }

  @Override
  public final void remove() {
    iterator.remove();
  }
}
