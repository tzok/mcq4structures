package pl.poznan.put.cs.bioserver.clustering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A utility class to allow clustering of data (either hierarchical or
 * partitional).
 * 
 * @author tzok
 */
public final class Clusterer {
    public class Result {
        Set<Integer> medoids;
        double[][] matrix;
        double score;

        public Result(double score, Set<Integer> medoids, double[][] matrix) {
            this.score = score;
            this.medoids = medoids;
            this.matrix = matrix;
        }

        public int[] clusterAssignment() {
            Map<Integer, Set<Integer>> clustering = Clusterer.getClustering(
                    medoids, matrix);
            Set<Integer> keySet = clustering.keySet();
            Integer[] keys = keySet.toArray(new Integer[keySet.size()]);
            Arrays.sort(keys);

            int[] result = new int[matrix.length];
            for (int i = 0; i < matrix.length; i++) {
                for (int j = 0; j < keys.length; j++) {
                    if (clustering.get(keys[j]).contains(i)) {
                        result[i] = j;
                        break;
                    }
                }
            }
            return result;
        }
    }

    private interface ScoringFunction {
        double score(Map<Integer, Set<Integer>> clustering, int index,
                double[][] matrix);
    }

    /** Available hierarchical clustering types. */
    public enum Type {
        SINGLE, COMPLETE, AVERAGE;
    }

    private static ScoringFunction scoringPAMSIL = new ScoringFunction() {
        public double score(Map<Integer, Set<Integer>> clustering, int index,
                double[][] matrix) {
            return Clusterer.scoreBySilhouette(clustering, index, matrix);
        }
    };

    private static ScoringFunction scoringPAM = new ScoringFunction() {
        public double score(Map<Integer, Set<Integer>> clustering, int index,
                double[][] matrix) {
            return Clusterer.scoreByDistance(clustering, index, matrix);
        }
    };

    /** Information about the results of clustering. */
    private static List<List<Integer>> clusters;

    private static Clusterer INSTANCE = new Clusterer();

    private static double averageDistance(
            Map<Integer, Set<Integer>> clustering, int from, int to,
            double[][] matrix) {
        Set<Integer> keySet = clustering.keySet();
        Integer[] keys = keySet.toArray(new Integer[keySet.size()]);
        Arrays.sort(keys);

        Set<Integer> mi = clustering.get(keys[from]);
        Set<Integer> mj = clustering.get(keys[to]);

        double average = 0;
        for (int m : mi) {
            for (int n : mj) {
                average += matrix[m][n];
            }
        }
        return average / (mi.size() * mj.size());
    }

    public static Result clusterPAM(double[][] matrix) {
        return Clusterer.kMedoids(matrix, Clusterer.scoringPAM);
    }

    public static Result clusterPAM(double[][] matrix, int k) {
        return Clusterer.kMedoids(matrix, Clusterer.scoringPAM, k);
    }

    public static Result clusterPAMSIL(double[][] matrix) {
        return Clusterer.kMedoids(matrix, Clusterer.scoringPAMSIL);
    }

    public static Result clusterPAMSIL(double[][] matrix, int k) {
        return Clusterer.kMedoids(matrix, Clusterer.scoringPAMSIL, k);
    }

    private static Map<Integer, Set<Integer>> getClustering(
            Set<Integer> medoids, double[][] matrix) {
        Map<Integer, Set<Integer>> clustering = new HashMap<>();
        for (int i : medoids) {
            clustering.put(i, new HashSet<Integer>());
        }
        for (int i = 0; i < matrix.length; i++) {
            double minimum = Double.POSITIVE_INFINITY;
            int minimizer = -1;
            for (int j : medoids) {
                if (matrix[i][j] < minimum) {
                    minimum = matrix[i][j];
                    minimizer = j;
                }
            }
            Set<Integer> set = clustering.get(minimizer);
            set.add(i);
        }
        return clustering;
    }

    public static List<List<Integer>> getClusters() {
        return Clusterer.clusters;
    }

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
    public static int[][] hierarchicalClustering(double[][] matrix,
            Clusterer.Type linkage) {
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

    public static Result kMedoids(double[][] matrix, ScoringFunction sf) {
        Result overallBest = Clusterer.INSTANCE.new Result(
                Double.NEGATIVE_INFINITY, null, matrix);
        for (int k = 2; k < matrix.length / 2; k++) {
            Result result = Clusterer.kMedoids(matrix, sf, k);
            if (result.score > overallBest.score) {
                overallBest.score = result.score;
                overallBest.medoids = result.medoids;
            }
        }
        return overallBest;
    }

    public static Result kMedoids(double[][] matrix, ScoringFunction sf, int k) {
        Set<Integer> medoids = new HashSet<>();
        Set<Integer> nonmedoids = new HashSet<>();
        for (int i = 0; i < k; i++) {
            medoids.add(i);
        }
        for (int i = k; i < matrix.length; i++) {
            nonmedoids.add(i);
        }

        double score = Clusterer.scoreCluster(medoids, matrix, sf);
        while (true) {
            double bestScore = score;
            Set<Integer> bestMedoids = medoids;
            for (int i : medoids) {
                for (int j : nonmedoids) {
                    Set<Integer> swap = new HashSet<>();
                    for (int m : medoids) {
                        swap.add(m == i ? j : m);
                    }
                    double newScore = Clusterer.scoreCluster(swap, matrix, sf);
                    if (newScore > bestScore) {
                        bestScore = newScore;
                        bestMedoids = swap;
                    }
                }
            }

            if (bestScore > score) {
                score = bestScore;
                medoids = bestMedoids;
            } else {
                break;
            }
        }
        return Clusterer.INSTANCE.new Result(score, medoids, matrix);
    }

    private static double scoreByDistance(
            Map<Integer, Set<Integer>> clustering, int j, double[][] matrix) {
        Set<Integer> set = clustering.keySet();
        Integer[] array = set.toArray(new Integer[set.size()]);
        Arrays.sort(array);

        double result = 0;
        for (int i : clustering.get(array[j])) {
            result += matrix[i][array[j]];
        }
        return -result;
    }

    private static double scoreBySilhouette(
            Map<Integer, Set<Integer>> clustering, int j, double[][] matrix) {
        int k = clustering.size();
        double aj = Clusterer.averageDistance(clustering, j, j, matrix);
        double bj = Double.MAX_VALUE;
        for (int i = 0; i < k; i++) {
            if (i == j) {
                continue;
            }
            double bij = Clusterer.averageDistance(clustering, j, i, matrix);
            if (bij < bj) {
                bj = bij;
            }
        }
        return (bj - aj) / Math.max(aj, bj);
    }

    private static double scoreCluster(Set<Integer> medoids, double[][] matrix,
            ScoringFunction sf) {
        Map<Integer, Set<Integer>> clustering = Clusterer.getClustering(
                medoids, matrix);
        int k = medoids.size();
        double sum = 0;
        for (int j = 0; j < k; j++) {
            sum += sf.score(clustering, j, matrix);
        }
        return sum;
    }

    private Clusterer() {
    }
}
