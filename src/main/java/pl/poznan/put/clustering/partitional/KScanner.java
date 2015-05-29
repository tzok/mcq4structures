package pl.poznan.put.clustering.partitional;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KScanner {
    private static final Logger LOGGER = LoggerFactory.getLogger(KScanner.class);
    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors() * 2;

    private static class ClusterCallable implements Callable<ScoredClusteringResult> {
        private final PrototypeBasedClusterer clusterer;
        private final double[][] distanceMatrix;
        private final ScoringFunction scoringFunction;
        private final int k;

        public ClusterCallable(PrototypeBasedClusterer clusterer,
                double[][] distanceMatrix, ScoringFunction scoringFunction,
                int k) {
            this.clusterer = clusterer;
            this.distanceMatrix = distanceMatrix.clone();
            this.scoringFunction = scoringFunction;
            this.k = k;
        }

        @Override
        public ScoredClusteringResult call() throws Exception {
            return clusterer.findPrototypes(distanceMatrix, scoringFunction, k);
        }
    }

    public static ScoredClusteringResult parallelScan(
            PrototypeBasedClusterer clusterer, double[][] matrix,
            ScoringFunction scoringFunction) {
        ExecutorService threadPool = Executors.newFixedThreadPool(KScanner.THREAD_COUNT);
        ExecutorCompletionService<ScoredClusteringResult> ecs = new ExecutorCompletionService<>(threadPool);

        for (int i = 2; i <= matrix.length; i++) {
            ClusterCallable task = new ClusterCallable(clusterer, matrix, scoringFunction, i);
            ecs.submit(task);
        }

        threadPool.shutdown();
        ScoredClusteringResult overallBest = null;

        for (int i = 2; i <= matrix.length; i++) {
            ScoredClusteringResult result;

            try {
                result = ecs.take().get();
            } catch (InterruptedException | ExecutionException e) {
                KScanner.LOGGER.warn("Failed to cluster the data", e);
                continue;
            }

            PAMSIL pamsil = PAMSIL.getInstance();
            ClusterPrototypes prototypes = result.getPrototypes();
            double silhouette = pamsil.score(prototypes, matrix);

            if (overallBest == null || silhouette > overallBest.getSilhouette()) {
                double score = result.getScore();
                overallBest = new ScoredClusteringResult(prototypes, scoringFunction, score, silhouette);
            }
        }

        assert overallBest != null;
        return overallBest;
    }
}
