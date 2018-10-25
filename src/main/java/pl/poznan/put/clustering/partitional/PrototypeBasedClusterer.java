package pl.poznan.put.clustering.partitional;

public interface PrototypeBasedClusterer {
  ScoredClusteringResult findPrototypes(double[][] distanceMatrix, ScoringFunction scoringFunction, int clusterCount);

  ScoredClusteringResult findPrototypes(double[][] distanceMatrix, ScoringFunction scoringFunction,
                                        ClusterPrototypes initialPrototypes);
}
