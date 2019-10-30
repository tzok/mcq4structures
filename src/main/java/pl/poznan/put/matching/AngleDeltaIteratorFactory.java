package pl.poznan.put.matching;

@FunctionalInterface
public interface AngleDeltaIteratorFactory {
  AngleDeltaIterator createInstance(FragmentMatch fragmentMatch);
}
