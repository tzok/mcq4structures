package pl.poznan.put.clustering.hierarchical;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class Clusterer {
  private final List<String> names;
  private final double[][] matrix;
  private final Linkage linkage;

  public Clusterer(final List<String> names, final double[][] matrix, final Linkage linkage) {
    super();
    this.names = new ArrayList<>(names);
    this.matrix = matrix.clone();
    this.linkage = linkage;
  }

  public static List<Cluster> initialClusterAssignment(final List<String> names) {
    return IntStream.range(0, names.size())
        .mapToObj(i -> new Cluster(i, names.get(i)))
        .collect(Collectors.toList());
  }

  public HierarchicalClustering cluster() {
    final List<Cluster> clusters = Clusterer.initialClusterAssignment(names);
    final List<HierarchicalClusterMerge> merges = new ArrayList<>();

    while (clusters.size() > 1) {
      Pair<Integer, Integer> pair = Pair.of(-1, -1);
      double minDelta = Double.POSITIVE_INFINITY;

      for (int i = 0; i < clusters.size(); ++i) {
        final Cluster c1 = clusters.get(i);

        for (int j = i + 1; j < clusters.size(); ++j) {
          final Cluster c2 = clusters.get(j);
          final double delta = getClusterDelta(c1, c2);

          if (delta < minDelta) {
            minDelta = delta;
            pair = Pair.of(i, j);
          }
        }
      }

      final Cluster left = clusters.get(pair.getLeft());
      final Cluster right = clusters.get(pair.getRight());
      final Cluster merged = Cluster.merge(left, right);
      merges.add(new HierarchicalClusterMerge(pair, minDelta));

      clusters.remove(left);
      clusters.remove(right);
      clusters.add(merged);
    }

    return new HierarchicalClustering(names, merges);
  }

  private double getClusterDelta(final Cluster c1, final Cluster c2) {
    double delta = 0;

    switch (linkage) {
      case SINGLE:
        delta = Double.POSITIVE_INFINITY;
        for (final int m : c1.getItems()) {
          for (final int n : c2.getItems()) {
            if (matrix[m][n] < delta) {
              delta = matrix[m][n];
            }
          }
        }
        break;

      case COMPLETE:
        delta = Double.NEGATIVE_INFINITY;
        for (final int m : c1.getItems()) {
          for (final int n : c2.getItems()) {
            if (matrix[m][n] > delta) {
              delta = matrix[m][n];
            }
          }
        }
        break;

      case AVERAGE:
        int count = 0;
        for (final int m : c1.getItems()) {
          for (final int n : c2.getItems()) {
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
