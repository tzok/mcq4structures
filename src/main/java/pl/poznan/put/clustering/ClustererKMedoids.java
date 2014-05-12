package pl.poznan.put.clustering;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class to allow clustering of data (either hierarchical or
 * partitional).
 * 
 * @author tzok
 */
public class ClustererKMedoids {
    public static final String NAME_PAM = "PAM";
    public static final String NAME_PAMSIL = "PAMSIL";

    public class ClusterCallable implements Callable<Result> {
        private double[][] matrix;
        private ScoringFunction sf;
        private int k;

        public ClusterCallable(double[][] matrix, ScoringFunction sf, int k) {
            this.matrix = matrix.clone();
            this.sf = sf;
            this.k = k;
        }

        @Override
        public Result call() throws Exception {
            return kMedoids(matrix, sf, k);
        }

    }

    public static class Result {
        private Set<Integer> medoids;
        private double score;

        Result(Set<Integer> medoids, double score) {
            super();
            this.medoids = medoids;
            this.score = score;
        }

        public Set<Integer> getMedoids() {
            return medoids;
        }

        public double getScore() {
            return score;
        }
    }

    public interface ScoringFunction {
        double score(Set<Integer> medoids, double[][] matrix);
    }

    private static class MatrixComparator implements Comparator<Integer> {
        private int index;
        private double[][] matrix;

        public MatrixComparator(int index, double[][] matrix) {
            super();
            this.index = index;
            this.matrix = matrix;
        }

        @Override
        public int compare(Integer o1, Integer o2) {
            return Double.compare(matrix[index][o1], matrix[index][o2]);
        }

    }

    public static final ScoringFunction PAM = new ScoringFunction() {
        @Override
        public double score(Set<Integer> medoids, double[][] matrix) {
            List<Heap> asHeaps = matrixAsHeaps(matrix);
            double result = 0;

            for (int i = 0; i < matrix.length; i++) {
                Heap heap = asHeaps.get(i);
                Iterator<Integer> iterator = heap.iterator();
                while (iterator.hasNext()) {
                    Integer closest = iterator.next();
                    if (medoids.contains(closest)) {
                        result += matrix[closest][i];
                        break;
                    }
                }
            }
            return -result;
        }

        @Override
        public String toString() {
            return ClustererKMedoids.NAME_PAM;
        }
    };

    public static final ScoringFunction PAMSIL = new ScoringFunction() {
        @Override
        public double score(Set<Integer> medoids, double[][] matrix) {
            Map<Integer, Set<Integer>> assignments =
                    getClusterAssignments(medoids, matrix);
            return ClustererKMedoids.scoreBySilhouette(assignments, matrix);
        }

        @Override
        public String toString() {
            return ClustererKMedoids.NAME_PAMSIL;
        }
    };

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ClustererKMedoids.class);
    private static final Random RANDOM = new Random();

    /**
     * Assign every object to its closest medoid.
     * 
     * @param medoids
     *            Indices of objects which are medoids.
     * @param matrix
     *            A distance matrix.
     * @return A map of this form { medoid : set(objects) }
     */
    public static Map<Integer, Set<Integer>> getClusterAssignments(
            Set<Integer> medoids, final double[][] matrix) {
        Map<Integer, Set<Integer>> clustering =
                new HashMap<Integer, Set<Integer>>();
        for (int i : medoids) {
            clustering.put(i, new HashSet<Integer>());
        }

        List<Heap> binaryHeaps = matrixAsHeaps(matrix);

        for (int i = 0; i < matrix.length; i++) {
            Heap heap = binaryHeaps.get(i);
            Iterator<Integer> iterator = heap.iterator();

            while (iterator.hasNext()) {
                Integer closest = iterator.next();
                if (medoids.contains(closest)) {
                    clustering.get(closest).add(i);
                    break;
                }
            }
        }

        return clustering;
    }

    public static ScoringFunction[] getScoringFunctions() {
        return new ScoringFunction[] { PAM, PAMSIL };
    }

    public Result kMedoids(double[][] matrix, ScoringFunction sf, Integer k) {
        // in this mode, search for best 'k'
        if (k == null) {
            // return ClustererKMedoids.parallelScan(matrix, sf);
            return serialScan(matrix, sf);
        }

        double overallBestScore = Double.NEGATIVE_INFINITY;
        Set<Integer> overallBestMedoids = null;
        for (int trial = 0; trial < 1; trial++) {
            Set<Integer> medoids = initializeMedoids(matrix, k);

            double score = sf.score(medoids, matrix);
            while (true) {
                Set<Integer> nonmedoids = new HashSet<Integer>();
                for (int i = 0; i < matrix.length; i++) {
                    if (!medoids.contains(i)) {
                        nonmedoids.add(i);
                    }
                }

                double bestScore = score;
                Set<Integer> bestMedoids = medoids;
                for (int i : medoids) {
                    for (int j : nonmedoids) {
                        Set<Integer> swap = new HashSet<Integer>();
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

        DecimalFormat format = new DecimalFormat("0.000");
        ClustererKMedoids.LOGGER.debug("Final score for clustering (k=" + k
                + "): PAM=" + format.format(overallBestScore) + " PAMSIL="
                + format.format(PAMSIL.score(overallBestMedoids, matrix)));
        return new Result(overallBestMedoids, overallBestScore);
    }

    // http://en.wikipedia.org/wiki/K-means%2B%2B#Initialization_algorithm
    private static Set<Integer> initializeMedoids(double[][] matrix, int k) {
        Set<Integer> setMedoids = new HashSet<Integer>();
        setMedoids.add(ClustererKMedoids.RANDOM.nextInt(matrix.length));

        List<Heap> listHeaps = matrixAsHeaps(matrix);
        assert listHeaps.size() == matrix.length : "listHeaps.size() = "
                + listHeaps.size() + ", matrix.length = " + matrix.length;

        for (int i = 1; i < k; i++) {
            LinkedHashMap<Integer, Double> mapElementNearest =
                    new LinkedHashMap<Integer, Double>();
            double total = 0;

            for (int j = 0; j < matrix.length; j++) {
                if (setMedoids.contains(j)) {
                    continue;
                }

                Iterator<Integer> iterator = listHeaps.get(j).iterator();
                while (iterator.hasNext()) {
                    Integer nearest = iterator.next();
                    if (setMedoids.contains(nearest)) {
                        double distance = matrix[j][nearest];
                        total = total + distance * distance;
                        mapElementNearest.put(j, total);
                        break;
                    }
                }
            }

            Set<Integer> setCandidates = new HashSet<Integer>();
            for (int j = 0; j < matrix.length; j++) {
                setCandidates.add(j);
            }
            setCandidates.removeAll(setMedoids);

            double randomToken = ClustererKMedoids.RANDOM.nextDouble() * total;
            for (Entry<Integer, Double> entry : mapElementNearest.entrySet()) {
                if (randomToken < entry.getValue()) {
                    setMedoids.add(entry.getKey());
                    break;
                }
            }
        }

        return setMedoids;
    }

    private Result serialScan(double[][] matrix, ScoringFunction sf) {
        Result bestResult = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (int i = 2; i <= matrix.length; i++) {
            Result result = kMedoids(matrix, sf, i);
            double score = PAMSIL.score(result.medoids, matrix);

            if (score > bestScore) {
                bestScore = score;
                bestResult = result;
            }
        }

        return bestResult;
    }

    @SuppressWarnings("unused")
    private Result parallelScan(double[][] matrix, ScoringFunction sf) {
        int countThreads = Runtime.getRuntime().availableProcessors() * 2;
        ExecutorService threadPool = Executors.newFixedThreadPool(countThreads);
        ExecutorCompletionService<Result> ecs =
                new ExecutorCompletionService<Result>(threadPool);

        for (int i = 2; i <= matrix.length; i++) {
            ClusterCallable task = new ClusterCallable(matrix, sf, i);
            ecs.submit(task);
        }
        threadPool.shutdown();

        Result overallBest = null;
        for (int i = 2; i <= matrix.length; i++) {
            Result result;
            try {
                result = ecs.take().get();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                continue;
            } catch (ExecutionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                continue;
            }

            double score = PAMSIL.score(result.medoids, matrix);
            if (overallBest == null || score > overallBest.score) {
                overallBest = result;
                overallBest.score = score;
            }
        }

        assert overallBest != null;
        ClustererKMedoids.LOGGER.debug("Final score for clustering: "
                + overallBest.score);
        return overallBest;
    }

    private static List<Heap> matrixAsHeaps(final double[][] matrix) {
        List<Heap> list = new ArrayList<Heap>();

        for (int i = 0; i < matrix.length; i++) {
            PriorityQueue<Integer> data =
                    new PriorityQueue<Integer>(matrix.length,
                            new MatrixComparator(i, matrix));

            for (int j = 0; j < matrix.length; j++) {
                data.add(j);
            }

            list.add(new Heap(data));
        }

        return list;
    }

    static double scoreByDistance(Map<Integer, Set<Integer>> clustering,
            double[][] matrix) {
        double result = 0;
        for (Entry<Integer, Set<Integer>> entry : clustering.entrySet()) {
            int j = entry.getKey();
            for (int i : entry.getValue()) {
                result += matrix[j][i];
            }
        }
        return -result;
    }

    static double scoreBySilhouette(Map<Integer, Set<Integer>> clustering,
            double[][] matrix) {
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
                ClustererKMedoids.LOGGER.trace("medoid=" + e1.getKey()
                        + " element=" + j + " self=" + aj + " other=" + bj);
                result += (bj - aj) / Math.max(aj, bj);
            }
        }
        return result;
    }
}
