package pl.poznan.put.clustering.partitional;

public class ScoredClusteringResult {
  private final ClusterPrototypes prototypes;
  private final ScoringFunction scoringFunction;
  private final double score;
  private final double silhouette;

  public ScoredClusteringResult(
      final ClusterPrototypes prototypes,
      final ScoringFunction scoringFunction,
      final double score,
      final double silhouette) {
    super();
    this.prototypes = prototypes;
    this.scoringFunction = scoringFunction;
    this.score = score;
    this.silhouette = silhouette;
  }

  public final ClusterPrototypes getPrototypes() {
    return prototypes;
  }

  public final ScoringFunction getScoringFunction() {
    return scoringFunction;
  }

  public final double getScore() {
    return score;
  }

  public final double getSilhouette() {
    return silhouette;
  }
}
