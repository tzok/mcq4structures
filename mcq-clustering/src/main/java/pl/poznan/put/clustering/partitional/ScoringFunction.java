package pl.poznan.put.clustering.partitional;

@FunctionalInterface
public interface ScoringFunction {
  double score(ClusterPrototypes prototypes, double[][] distanceMatrix);
}
