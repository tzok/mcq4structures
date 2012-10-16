package pl.poznan.put.cs.bioserver.clustering;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Clusterer {
    public static final int SINGLE = 0;
    public static final int COMPLETE = 1;
    public static final int AVERAGE = 2;
    public static ArrayList<List<Integer>> clusters;

    /**
     * Perform agglomerative, hierarchical clustering using SINGLE, COMPLETE or
     * AVERAGE linkage.
     * 
     * @param matrix
     *            Distance matrix.
     * @param linkage
     *            SINGLE, COMPLETE or AVERAGE.
     * @return An array of triplets in form (A, B, d(A, B)), where A and B are
     *         cluster IDs and d(A, B) is a scaled distance between them..
     */
    public static int[][] hierarchicalClustering(double[][] matrix, int linkage) {
        /*
         * sanity check -- each matrix symmetric, all matrices of the same
         * dimensions
         */
        for (int i = 0; i < matrix.length; ++i) {
            for (int j = 0; j < matrix[i].length; ++j) {
                if (matrix[i][j] != matrix[j][i]) {
                    throw new IllegalArgumentException(
                            "Distance matrix must be symmetric!");
                }
            }
        }
        if (linkage != Clusterer.SINGLE && linkage != Clusterer.COMPLETE
                && linkage != Clusterer.AVERAGE) {
            throw new IllegalArgumentException(
                    "Linkage must be one of: SINGLE, COMPLETE or AVERAGE");
        }
        /*
         * initialise clusters as single elements
         */
        Clusterer.clusters = new ArrayList<>();
        for (int i = 0; i < matrix.length; ++i) {
            List<Integer> c = new ArrayList<>();
            c.add(i);
            Clusterer.clusters.add(c);
        }

        List<int[]> result = new ArrayList<>();
        while (Clusterer.clusters.size() > 1) {
            /*
             * get two clusters to be merged
             */
            int[] toMerge = new int[2];
            double leastDiff = Double.POSITIVE_INFINITY;
            for (int i = 0; i < Clusterer.clusters.size(); ++i) {
                for (int j = i + 1; j < Clusterer.clusters.size(); ++j) {
                    List<Integer> c1 = Clusterer.clusters.get(i);
                    List<Integer> c2 = Clusterer.clusters.get(j);
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
                        // TODO: fill the default case
                        break;
                    }

                    if (delta < leastDiff) {
                        toMerge[0] = i;
                        toMerge[1] = j;
                        leastDiff = delta;
                    }
                }
            }

            /*
             * merge clusters
             */
            List<Integer> c1 = Clusterer.clusters.get(toMerge[0]);
            List<Integer> c2 = Clusterer.clusters.get(toMerge[1]);
            c1.addAll(c2);
            Clusterer.clusters.remove(toMerge[1]);

            result.add(new int[] { toMerge[0], toMerge[1],
                    (int) (1000.0 * leastDiff) });
        }
        return result.toArray(new int[result.size()][]);
    }

    /**
     * Perform clustering using k-medoid method (Partition Around Medoids
     * implementation).
     * 
     * @param distance
     *            Distance matrix.
     * @param k
     *            Number of clusters.
     * @return An array A, such that element a_i contains information about
     *         cluster assignment of i-th data point. Note that clusters are not
     *         numbered from 0 to k. Value in a_i corresponds to the closest
     *         medoid (center of the cluster).
     */
    public static int[] kMedoids(double[][] distance, int k) {
        /*
         * sanity check -- symmetric matrix
         */
        for (int i = 0; i < distance.length; ++i) {
            for (int j = 0; j < distance[i].length; ++j) {
                if (distance[i][j] != distance[j][i]) {
                    throw new IllegalArgumentException(
                            "Distance matrix must be symmetric!");
                }
            }
        }
        /*
         * sanity check -- k correctly bound
         */
        if (k < 2 || k > distance.length) {
            throw new IllegalArgumentException("k in k-medoids algorithm "
                    + "must be at least 2 and at most n, where n is the "
                    + "number of datapoints");
        }
        /*
         * primary choice of medoids
         */
        HashSet<Integer> medoidSet = new HashSet<>();
        for (int i = 0; i < k; ++i) {
            medoidSet.add(i);
        }
        /*
         * implementation of Partition Around Medoids (PAM)
         */
        while (true) {
            int[] bestChangePair = new int[2];
            double bestChangeCost = Double.POSITIVE_INFINITY;
            // iterate over each medoid
            for (int i : medoidSet) {
                // check every non-selected if it's worth to change it with
                // medoid
                for (int h = 0; h < distance.length; ++h) {
                    if (!medoidSet.contains(h)) {
                        double cost = 0;
                        // check the cost of such transition
                        for (int j = 0; j < distance.length; ++j) {
                            if (j != h && !medoidSet.contains(j)) {
                                int j2 = Clusterer.medoid(distance[j],
                                        medoidSet, -1);
                                if (j2 == i) {
                                    j2 = Clusterer.medoid(distance[j],
                                            medoidSet, i);
                                    if (distance[j][h] >= distance[j][j2]) {
                                        cost += distance[j][j2]; // first case
                                    } else {
                                        cost += distance[j][h]; // second case
                                    }
                                    cost -= distance[j][i];
                                } else {
                                    double changeCost = distance[j][h]
                                            - distance[j][j2];
                                    if (changeCost > 0) {
                                        cost += 0; // third case
                                    } else {
                                        cost += changeCost; // fourth case
                                    }
                                }
                            }
                        }
                        if (cost < bestChangeCost) {
                            bestChangePair[0] = i;
                            bestChangePair[1] = h;
                            bestChangeCost = cost;
                        }
                    }
                }
            }
            // make transition only if it enhances clustering
            if (bestChangeCost < 0) {
                medoidSet.remove(bestChangePair[0]);
                medoidSet.add(bestChangePair[1]);
            } else {
                break;
            }
        }
        /*
         * final assignment to clusters
         */
        int[] clusterAssignment = new int[distance.length];
        for (int i = 0; i < distance.length; ++i) {
            clusterAssignment[i] = Clusterer.medoid(distance[i], medoidSet, -1);
        }
        return clusterAssignment;
    }

    /**
     * Find which medoid/cluster is the given element assigned to.
     * 
     * @param row
     *            Row taken out from distance matrix.
     * @param candidates
     *            A set of medoids to be checked.
     * @param exclude
     *            One specified medoid to be excluded from search (set to -1 if
     *            none should be excluded)
     * @return Index of medoid to which the given element is assigned.
     */
    private static int medoid(double[] row, Iterable<Integer> candidates,
            int exclude) {
        int index = -1;
        double min = Double.POSITIVE_INFINITY;
        for (int i : candidates) {
            if (exclude == -1 || i != exclude) {
                if (row[i] < min) {
                    min = row[i];
                    index = i;
                }
            }
        }
        return index;
    }
}
