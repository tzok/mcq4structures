package pl.poznan.put.clustering.partitional;

public interface ScoringFunction {
  double score(ClusterPrototypes prototypes, double[][] distanceMatrix);
}
