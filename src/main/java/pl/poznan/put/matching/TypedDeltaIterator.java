package pl.poznan.put.matching;

import pl.poznan.put.circular.Angle;
import pl.poznan.put.torsion.MasterTorsionAngleType;

import java.util.Iterator;

public class TypedDeltaIterator implements AngleDeltaIterator {
  private final Iterator<ResidueComparison> iterator;
  private final MasterTorsionAngleType masterType;

  public TypedDeltaIterator(FragmentMatch fragmentMatch, MasterTorsionAngleType masterType) {
    super();
    this.iterator = fragmentMatch.getResidueComparisons().iterator();
    this.masterType = masterType;
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }

  @Override
  public Angle next() {
    return iterator.next().getAngleDelta(masterType).getDelta();
  }

  @Override
  public void remove() {
    iterator.remove();
  }
}
