package pl.poznan.put.matching;

import pl.poznan.put.circular.Angle;

import java.util.Iterator;

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
