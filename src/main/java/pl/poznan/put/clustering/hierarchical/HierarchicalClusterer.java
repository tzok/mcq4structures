package pl.poznan.put.clustering.hierarchical;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

public final class HierarchicalClusterer {
    public static HierarchicalClusteringResult cluster(double[][] matrix,
            Linkage linkage) {
        /*
         * initialise clusters as single elements
         */
        List<HierarchicalCluster> clusters = new ArrayList<>();
        List<HierarchicalClusterMerge> merges = new ArrayList<>();

        for (int i = 0; i < matrix.length; ++i) {
            List<Integer> c = new ArrayList<>();
            c.add(i);
            clusters.add(new HierarchicalCluster(c));
        }

        while (clusters.size() > 1) {
            /*
             * get two clusters to be merged
             */
            Pair<Integer, Integer> pair = Pair.of(-1, -1);
            double leastDiff = Double.POSITIVE_INFINITY;

            for (int i = 0; i < clusters.size(); ++i) {
                for (int j = i + 1; j < clusters.size(); ++j) {
                    List<Integer> c1 = clusters.get(i).getItems();
                    List<Integer> c2 = clusters.get(j).getItems();
                    double delta = 0;

                    switch (linkage) {
                    case SINGLE:
                        delta = Double.POSITIVE_INFINITY;
                        for (int m : c1) {
                            for (int n : c2) {
                                if (matrix[m][n] < delta) {
                                    delta = matrix[m][n];
                                }
                            }
                        }
                        break;

                    case COMPLETE:
                        delta = Double.NEGATIVE_INFINITY;
                        for (int m : c1) {
                            for (int n : c2) {
                                if (matrix[m][n] > delta) {
                                    delta = matrix[m][n];
                                }
                            }
                        }
                        break;

                    case AVERAGE:
                        int count = 0;
                        for (int m : c1) {
                            for (int n : c2) {
                                delta += matrix[m][n];
                                count++;
                            }
                        }
                        delta /= count;
                        break;

                    default:
                        throw new RuntimeException(
                                "Unknown type of linkage for hierarchical "
                                        + "clustering: " + linkage);
                    }

                    if (delta < leastDiff) {
                        pair = Pair.of(i, j);
                        leastDiff = delta;
                    }
                }
            }

            /*
             * merge clusters
             */
            HierarchicalCluster left = clusters.get(pair.getLeft());
            HierarchicalCluster right = clusters.get(pair.getRight());

            HierarchicalCluster merged = HierarchicalCluster.merge(left, right);
            merges.add(new HierarchicalClusterMerge(pair, leastDiff));

            clusters.remove(left);
            clusters.remove(right);
            clusters.add(merged);
        }

        return new HierarchicalClusteringResult(merges);
    }

    private HierarchicalClusterer() {
    }
}
