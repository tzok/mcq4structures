package pl.poznan.put.clustering.hierarchical;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public final class Clusterer {
  private final List<String> names;
  private final double[][] matrix;
  private final Linkage linkage;

  public Clusterer(List<String> names, double[][] matrix, Linkage linkage) {
    super();
    this.names = names;
    this.matrix = matrix.clone();
    this.linkage = linkage;
  }

  public HierarchicalClustering cluster() {
    List<Cluster> clusters = Clusterer.initialClusterAssignment(names);
    List<HierarchicalClusterMerge> merges = new ArrayList<>();

    while (clusters.size() > 1) {
      Pair<Integer, Integer> pair = Pair.of(-1, -1);
      double minDelta = Double.POSITIVE_INFINITY;

      for (int i = 0; i < clusters.size(); ++i) {
        Cluster c1 = clusters.get(i);

        for (int j = i + 1; j < clusters.size(); ++j) {
          Cluster c2 = clusters.get(j);
          double delta = getClusterDelta(c1, c2);

          if (delta < minDelta) {
            minDelta = delta;
            pair = Pair.of(i, j);
          }
        }
      }

      Cluster left = clusters.get(pair.getLeft());
      Cluster right = clusters.get(pair.getRight());
      Cluster merged = Cluster.merge(left, right);
      merges.add(new HierarchicalClusterMerge(pair, minDelta));

      clusters.remove(left);
      clusters.remove(right);
      clusters.add(merged);
    }

    return new HierarchicalClustering(names, merges);
  }

  public static List<Cluster> initialClusterAssignment(List<String> names) {
    List<Cluster> clusters = new ArrayList<>();
    for (int i = 0; i < names.size(); ++i) {
      clusters.add(new Cluster(i, names.get(i)));
    }
    return clusters;
  }

  private double getClusterDelta(Cluster c1, Cluster c2) {
    double delta = 0;

    switch (linkage) {
      case SINGLE:
        delta = Double.POSITIVE_INFINITY;
        for (int m : c1.getItems()) {
          for (int n : c2.getItems()) {
            if (matrix[m][n] < delta) {
              delta = matrix[m][n];
            }
          }
        }
        break;

      case COMPLETE:
        delta = Double.NEGATIVE_INFINITY;
        for (int m : c1.getItems()) {
          for (int n : c2.getItems()) {
            if (matrix[m][n] > delta) {
              delta = matrix[m][n];
            }
          }
        }
        break;

      case AVERAGE:
        int count = 0;
        for (int m : c1.getItems()) {
          for (int n : c2.getItems()) {
            delta += matrix[m][n];
            count++;
          }
        }
        delta /= count;
        break;

      default:
        throw new IllegalArgumentException(
            "Unknown type of linkage for hierarchical clustering: " + linkage);
    }

    return delta;
  }
}
