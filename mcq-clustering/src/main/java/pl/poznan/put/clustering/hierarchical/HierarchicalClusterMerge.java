package pl.poznan.put.clustering.hierarchical;

import org.apache.commons.lang3.tuple.Pair;

public class HierarchicalClusterMerge {
  private final Pair<Integer, Integer> indices;
  private final double distance;

  public HierarchicalClusterMerge(Pair<Integer, Integer> indices, double distance) {
    super();
    this.indices = indices;
    this.distance = distance;
  }

  public int getLeft() {
    return indices.getLeft();
  }

  public int getRight() {
    return indices.getRight();
  }

  public double getDistance() {
    return distance;
  }
}
