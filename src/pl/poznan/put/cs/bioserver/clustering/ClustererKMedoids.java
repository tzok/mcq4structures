package pl.poznan.put.cs.bioserver.clustering;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class to allow clustering of data (either hierarchical or
 * partitional).
 * 
 * @author tzok
 */
public final class ClustererKMedoids {
    public static class Result {
        public Set<Integer> medoids;
        public double score;

        public Result(Set<Integer> medoids, double score) {
            super();
            this.medoids = medoids;
            this.score = score;
        }
    }

    public interface ScoringFunction {
        double score(Set<Integer> medoids, double[][] matrix);
    }

    public static final ScoringFunction PAMSIL = new ScoringFunction() {
        @Override
        public double score(Set<Integer> medoids, double[][] matrix) {
            Map<Integer, Set<Integer>> assignments = ClustererKMedoids.getClusterAssignments(
                    medoids, matrix);
            return ClustererKMedoids.scoreBySilhouette(assignments, matrix);
        }

        @Override
        public String toString() {
            return "PAMSIL";
        }
    };

    public static final ScoringFunction PAM = new ScoringFunction() {
        @Override
        public double score(Set<Integer> medoids, double[][] matrix) {
            Map<Integer, Set<Integer>> assignments = ClustererKMedoids.getClusterAssignments(
                    medoids, matrix);
            return ClustererKMedoids.scoreByDistance(assignments, matrix);
        }

        @Override
        public String toString() {
            return "PAM";
        }
    };

    private static final Logger LOGGER = LoggerFactory.getLogger(ClustererKMedoids.class);
    private static final Random RANDOM = new Random();

    /**
     * Assign every object to its closes medoid.
     * 
     * @param medoids
     *            Indices of objects which are medoids.
     * @param matrix
     *            A distance matrix.
     * @return A map of this form { medoid : set(objects) }
     */
    public static Map<Integer, Set<Integer>> getClusterAssignments(Set<Integer> medoids,
            double[][] matrix) {
        Map<Integer, Set<Integer>> clustering = new HashMap<>();
        for (int i : medoids) {
            clustering.put(i, new HashSet<Integer>());
        }

        // for each element, find its closest medoids
        for (int i = 0; i < matrix.length; i++) {
            double minimum = Double.POSITIVE_INFINITY;
            int minimizer = -1;
            for (int j : medoids) {
                if (matrix[i][j] < minimum) {
                    minimum = matrix[i][j];
                    minimizer = j;
                }
            }

            // minimizer == closest medoid
            // i == current element
            Set<Integer> set = clustering.get(minimizer);
            set.add(i);
        }
        return clustering;
    }

    public static ScoringFunction[] getScoringFunctions() {
        return new ScoringFunction[] { ClustererKMedoids.PAM, ClustererKMedoids.PAMSIL };
    }

    public static Result kMedoids(double[][] matrix, ScoringFunction sf, @Nullable Integer k) {
        // in this mode, search for best 'k'
        if (k == null) {
            Result overallBest = null;
            for (int i = 2; i <= matrix.length; i++) {
                Result result = ClustererKMedoids.kMedoids(matrix, sf, i);
                double score = ClustererKMedoids.PAMSIL.score(result.medoids, matrix);
                if (overallBest == null || score > overallBest.score) {
                    overallBest = result;
                    overallBest.score = score;
                }
            }
            assert overallBest != null;
            ClustererKMedoids.LOGGER.debug("Final score for clustering: " + overallBest.score);
            return overallBest;
        }

        double overallBestScore = Double.NEGATIVE_INFINITY;
        Set<Integer> overallBestMedoids = null;
        for (int trial = 0; trial < 10; trial++) {
            int[] options = new int[matrix.length];
            for (int i = 0; i < matrix.length; i++) {
                options[i] = i;
            }
            for (int i = 0; i < matrix.length; i++) {
                int j = ClustererKMedoids.RANDOM.nextInt(matrix.length);
                int tmp = options[i];
                options[i] = options[j];
                options[j] = tmp;
            }
            Set<Integer> medoids = new HashSet<>();
            for (int i = 0; i < k; i++) {
                medoids.add(options[i]);
            }

            double score = sf.score(medoids, matrix);
            while (true) {
                Set<Integer> nonmedoids = new HashSet<>();
                for (int i = 0; i < matrix.length; i++) {
                    if (!medoids.contains(i)) {
                        nonmedoids.add(i);
                    }
                }

                double bestScore = score;
                Set<Integer> bestMedoids = medoids;
                for (int i : medoids) {
                    for (int j : nonmedoids) {
                        Set<Integer> swap = new HashSet<>();
                        for (int m : medoids) {
                            swap.add(m == i ? j : m);
                        }
                        double newScore = sf.score(swap, matrix);
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

            if (score > overallBestScore) {
                overallBestScore = score;
                overallBestMedoids = medoids;
            }
        }

        assert overallBestMedoids != null;
        ClustererKMedoids.LOGGER.debug("Final score for clustering (k=" + k + "): "
                + overallBestScore);
        return new Result(overallBestMedoids, overallBestScore);
    }

    private static double scoreByDistance(Map<Integer, Set<Integer>> clustering, double[][] matrix) {
        double result = 0;
        for (Entry<Integer, Set<Integer>> entry : clustering.entrySet()) {
            int j = entry.getKey();
            for (int i : entry.getValue()) {
                result += matrix[j][i];
            }
        }
        return -result;
    }

    private static double scoreBySilhouette(Map<Integer, Set<Integer>> clustering, double[][] matrix) {
        double result = 0;
        for (Entry<Integer, Set<Integer>> e1 : clustering.entrySet()) {
            if (e1.getValue().size() == 1) {
                continue;
            }
            for (int j : e1.getValue()) {
                double aj = 0;
                for (int i : e1.getValue()) {
                    aj += matrix[j][i];
                }
                aj /= e1.getValue().size();

                double bj = Double.POSITIVE_INFINITY;
                for (Entry<Integer, Set<Integer>> e2 : clustering.entrySet()) {
                    if (e1.getKey().equals(e2.getKey())) {
                        continue;
                    }

                    double bjk = 0;
                    for (int k : e2.getValue()) {
                        bjk += matrix[j][k];
                    }
                    bjk /= e2.getValue().size();

                    if (bjk < bj) {
                        bj = bjk;
                    }
                }
                result += (bj - aj) / Math.max(aj, bj);
            }
        }
        return result;
    }

    private ClustererKMedoids() {
    }
}
