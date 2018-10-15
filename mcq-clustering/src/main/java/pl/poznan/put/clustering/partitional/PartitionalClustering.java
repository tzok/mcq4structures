package pl.poznan.put.clustering.partitional;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jcolorbrewer.ColorBrewer;
import org.w3c.dom.svg.SVGDocument;
import pl.poznan.put.interfaces.Visualizable;
import pl.poznan.put.svg.MDSDrawer;
import pl.poznan.put.types.DistanceMatrix;

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
    final Set<Integer> prototypes = assignment.getPrototypesIndices();
    final Color[] colorPalette = ColorBrewer.Paired.getColorPalette(prototypes.size());
    int index = 0;

    for (final int prototype : prototypes) {
      final StringBuilder builder = new StringBuilder("{ ");
      for (final int i : assignment.getAssignedTo(prototype)) {
        builder.append(names.get(i)).append(", ");
      }
      builder.delete(builder.length() - 2, builder.length());
      builder.append(" }");

      clusterColor.put(prototype, colorPalette[index]);
      clusterText.put(prototype, builder.toString());
      index++;
    }
  }

  public final ScoringFunction getScoringFunction() {
    return clustering.getScoringFunction();
  }

  public ClusterAssignment getAssignment() {
    return assignment;
  }

  public DistanceMatrix getDistanceMatrix() {
    return distanceMatrix;
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
