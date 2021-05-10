package pl.poznan.put.clustering.hierarchical;

import org.apache.commons.lang3.tuple.Pair;

class HierarchicalClusterMerge {
  private final Pair<Integer, Integer> indices;
  private final double distance;

  HierarchicalClusterMerge(final Pair<Integer, Integer> indices, final double distance) {
    super();
    this.indices = indices;
    this.distance = distance;
  }

  public final int getLeft() {
    return indices.getLeft();
  }

  public final int getRight() {
    return indices.getRight();
  }

  public final double getDistance() {
    return distance;
  }
}
