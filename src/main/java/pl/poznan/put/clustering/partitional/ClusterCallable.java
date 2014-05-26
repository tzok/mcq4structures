package pl.poznan.put.clustering.partitional;

import java.util.concurrent.Callable;

public class ClusterCallable implements Callable<ScoredClusteringResult> {
    private final PrototypeBasedClusterer clusterer;
    private final double[][] matrix;
    private final ScoringFunction sf;
    private final int k;

    public ClusterCallable(PrototypeBasedClusterer clusterer,
            double[][] matrix, ScoringFunction sf, int k) {
        this.clusterer = clusterer;
        this.matrix = matrix.clone();
        this.sf = sf;
        this.k = k;
    }

    @Override
    public ScoredClusteringResult call() throws Exception {
        return clusterer.findPrototypes(matrix, sf, k);
    }
}