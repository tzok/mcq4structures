package pl.poznan.put.matching;

import java.util.Iterator;
import pl.poznan.put.circular.Angle;
import pl.poznan.put.torsion.MasterTorsionAngleType;

public class TypedDeltaIterator implements AngleDeltaIterator {
  private final Iterator<ResidueComparison> iterator;
  private final MasterTorsionAngleType masterType;

  public TypedDeltaIterator(
      final FragmentMatch fragmentMatch, final MasterTorsionAngleType masterType) {
    super();
    iterator = fragmentMatch.getResidueComparisons().iterator();
    this.masterType = masterType;
  }

  @Override
  public final boolean hasNext() {
    return iterator.hasNext();
  }

  @Override
  public final Angle next() {
    return iterator.next().angleDelta(masterType).delta();
  }

  @Override
  public final void remove() {
    iterator.remove();
  }
}
