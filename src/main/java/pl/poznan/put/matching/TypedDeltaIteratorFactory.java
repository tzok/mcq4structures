package pl.poznan.put.matching;

import pl.poznan.put.torsion.MasterTorsionAngleType;

public class TypedDeltaIteratorFactory implements AngleDeltaIteratorFactory {
  private final MasterTorsionAngleType masterType;

  public TypedDeltaIteratorFactory(MasterTorsionAngleType masterType) {
    super();
    this.masterType = masterType;
  }

  @Override
  public AngleDeltaIterator createInstance(FragmentMatch fragmentMatch) {
    return new TypedDeltaIterator(fragmentMatch, masterType);
  }
}
