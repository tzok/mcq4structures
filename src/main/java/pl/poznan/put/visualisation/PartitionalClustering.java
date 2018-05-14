package pl.poznan.put.visualisation;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.w3c.dom.svg.SVGDocument;
import pl.poznan.put.clustering.partitional.ClusterAssignment;
import pl.poznan.put.clustering.partitional.ClusterPrototypes;
import pl.poznan.put.clustering.partitional.ScoredClusteringResult;
import pl.poznan.put.clustering.partitional.ScoringFunction;
import pl.poznan.put.interfaces.DistanceMatrix;
import pl.poznan.put.interfaces.Visualizable;

public class PartitionalClustering implements Visualizable {
  private final Map<Integer, Color> clusterColor = new HashMap<>();
  private final Map<Integer, String> clusterText = new HashMap<>();

  private final ClusterAssignment assignment;

  private final DistanceMatrix distanceMatrix;
  private final ScoredClusteringResult clustering;

  public PartitionalClustering(
      final DistanceMatrix distanceMatrix, final ScoredClusteringResult clustering) {
    super();
    this.distanceMatrix = distanceMatrix;
    this.clustering = clustering;

    final ClusterPrototypes prototypes = clustering.getPrototypes();
    assignment = ClusterAssignment.fromPrototypes(prototypes, distanceMatrix.getMatrix());
    analyzeClusterAssignment();
  }

  private void analyzeClusterAssignment() {
    final List<String> names = distanceMatrix.getNames();
    int index = 0;

    for (final int prototype : assignment.getPrototypesIndices()) {
      final StringBuilder builder = new StringBuilder("{ ");
      for (final int i : assignment.getAssignedTo(prototype)) {
        builder.append(names.get(i)).append(", ");
      }
      builder.delete(builder.length() - 2, builder.length());
      builder.append(" }");

      clusterColor.put(prototype, ColorMaps.getDistinctColorPaired(index));
      clusterText.put(prototype, builder.toString());
      index++;
    }
  }

  public final ScoringFunction getScoringFunction() {
    return clustering.getScoringFunction();
  }

  @Override
  public final SVGDocument visualize() {
    return MDSDrawer.scale2DAndVisualizePoints(
        distanceMatrix,
        index -> {
          final int prototype = assignment.getPrototype(index);
          return clusterColor.get(prototype);
        },
        index -> {
          final int prototype = assignment.getPrototype(index);
          return clusterText.get(prototype);
        });
  }

  @Override
  public void visualize3D() {
    // do nothing
  }
}
