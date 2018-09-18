package pl.poznan.put.clustering.partitional;

public class ScoredClusteringResult {
  private final ClusterPrototypes prototypes;
  private final ScoringFunction scoringFunction;
  private final double score;
  private final double silhouette;

  public ScoredClusteringResult(
      ClusterPrototypes prototypes,
      ScoringFunction scoringFunction,
      double score,
      double silhouette) {
    super();
    this.prototypes = prototypes;
    this.scoringFunction = scoringFunction;
    this.score = score;
    this.silhouette = silhouette;
  }

  public ClusterPrototypes getPrototypes() {
    return prototypes;
  }

  public ScoringFunction getScoringFunction() {
    return scoringFunction;
  }

  public double getScore() {
    return score;
  }

  public double getSilhouette() {
    return silhouette;
  }
}
