package pl.poznan.put.clustering.hierarchical;

import java.util.ArrayList;
import java.util.List;

public final class HierarchicalClusterer {
    public static HierarchicalCluster[] cluster(double[][] matrix,
            Linkage linkage) {
        /*
         * initialise clusters as single elements
         */
        List<HierarchicalCluster> clusters = new ArrayList<HierarchicalCluster>();

        for (int i = 0; i < matrix.length; ++i) {
            List<Integer> c = new ArrayList<Integer>();
            c.add(i);
            clusters.add(new HierarchicalCluster(c, i, 0));
        }

        List<HierarchicalCluster> result = new ArrayList<HierarchicalCluster>();

        while (clusters.size() > 1) {
            /*
             * get two clusters to be merged
             */
            HierarchicalCluster[] toMerge = new HierarchicalCluster[2];
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
                        toMerge[0] = clusters.get(i);
                        toMerge[1] = clusters.get(j);
                        leastDiff = delta;
                    }
                }
            }

            /*
             * merge clusters
             */
            HierarchicalCluster merged = new HierarchicalCluster(toMerge[0],
                    toMerge[1], leastDiff);
            result.add(merged);

            clusters.remove(toMerge[0]);
            clusters.remove(toMerge[1]);
            clusters.add(merged);
        }

        return result.toArray(new HierarchicalCluster[result.size()]);
    }

    private HierarchicalClusterer() {
    }
}
