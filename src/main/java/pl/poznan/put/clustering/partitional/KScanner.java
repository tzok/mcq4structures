package pl.poznan.put.clustering.partitional;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KScanner {
    private static final Logger LOGGER = LoggerFactory.getLogger(KScanner.class);
    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors() * 2;

    public static ScoredClusteringResult parallelScan(
            PrototypeBasedClusterer clusterer, double[][] matrix,
            ScoringFunction sf) {
        ExecutorService threadPool = Executors.newFixedThreadPool(KScanner.THREAD_COUNT);
        ExecutorCompletionService<ScoredClusteringResult> ecs = new ExecutorCompletionService<>(threadPool);

        for (int i = 2; i <= matrix.length; i++) {
            ClusterCallable task = new ClusterCallable(clusterer, matrix, sf, i);
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

            double score = PAMSIL.getInstance().score(result.getPrototypes(), matrix);

            if (overallBest == null || score > overallBest.getScore()) {
                overallBest = new ScoredClusteringResult(result.getPrototypes(), result.getScoringFunction(), score, score);
            }
        }

        assert overallBest != null;
        return overallBest;
    }
}
