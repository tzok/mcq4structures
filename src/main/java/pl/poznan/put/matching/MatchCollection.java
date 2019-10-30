package pl.poznan.put.matching;

import java.util.Collection;

@FunctionalInterface
public interface MatchCollection {
  Collection<FragmentMatch> getFragmentMatches();
}
