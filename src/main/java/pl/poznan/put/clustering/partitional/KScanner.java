package pl.poznan.put.clustering.partitional;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class KScanner {
  private static final Logger LOGGER = LoggerFactory.getLogger(KScanner.class);
  private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors() * 2;

  private KScanner() {
    super();
  }

  public static ScoredClusteringResult parallelScan(
      final PrototypeBasedClusterer clusterer,
      final double[][] matrix,
      final ScoringFunction scoringFunction) {
    final ExecutorService threadPool = Executors.newFixedThreadPool(KScanner.THREAD_COUNT);
    final CompletionService<ScoredClusteringResult> ecs =
        new ExecutorCompletionService<>(threadPool);

    for (int i = 2; i <= Math.min(12, matrix.length); i++) {
      final Callable<ScoredClusteringResult> task =
          new ClusterCallable(clusterer, matrix, scoringFunction, i);
      ecs.submit(task);
    }

    threadPool.shutdown();
    ScoredClusteringResult overallBest = null;

    for (int i = 2; i <= Math.min(12, matrix.length); i++) {
      final ScoredClusteringResult result;

      try {
        result = ecs.take().get();
      } catch (InterruptedException | ExecutionException e) {
        KScanner.LOGGER.warn("Failed to cluster the data", e);
        continue;
      }

      final PAMSIL pamsil = PAMSIL.getInstance();
      final ClusterPrototypes prototypes = result.getPrototypes();
      final double silhouette = pamsil.score(prototypes, matrix);

      if ((overallBest == null) || (silhouette > overallBest.getSilhouette())) {
        final double score = result.getScore();
        overallBest =
            new ScoredClusteringResult(
                prototypes, scoringFunction,
                score, silhouette);
      }
    }

    assert overallBest != null;
    return overallBest;
  }

  private static final class ClusterCallable implements Callable<ScoredClusteringResult> {
    private final PrototypeBasedClusterer clusterer;
    private final double[][] distanceMatrix;
    private final ScoringFunction scoringFunction;
    private final int k;

    private ClusterCallable(
        final PrototypeBasedClusterer clusterer,
        final double[][] distanceMatrix,
        final ScoringFunction scoringFunction,
        final int k) {
      super();
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
}
