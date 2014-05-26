package pl.poznan.put.clustering.partitional;

public interface PrototypeBasedClusterer {
    ScoredClusteringResult findPrototypes(double[][] matrix,
            ScoringFunction sf, int k);
}
