package pl.poznan.put.clustering.partitional;

import org.jcolorbrewer.ColorBrewer;
import org.jumpmind.symmetric.csv.CsvWriter;
import org.w3c.dom.svg.SVGDocument;
import pl.poznan.put.interfaces.Exportable;
import pl.poznan.put.interfaces.Visualizable;
import pl.poznan.put.svg.MDSDrawer;
import pl.poznan.put.types.DistanceMatrix;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PartitionalClustering implements Visualizable, Exportable {
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
    assignment = ClusterAssignment.fromPrototypes(prototypes, distanceMatrix.matrix());
    analyzeClusterAssignment();
  }

  public final ScoringFunction getScoringFunction() {
    return clustering.getScoringFunction();
  }

  public final ClusterAssignment getAssignment() {
    return assignment;
  }

  public final DistanceMatrix getDistanceMatrix() {
    return distanceMatrix;
  }

  public final int getClusterCount() {
    return clustering.getPrototypes().getPrototypesIndices().size();
  }

  public final double getSilhouette() {
    return clustering.getSilhouette();
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

  @Override
  public final void export(final OutputStream stream) throws IOException {
    final double[][] scaledXYMatrix = MDSDrawer.scaleTo2D(distanceMatrix);
    final List<String> names = distanceMatrix.names();
    final CsvWriter writer = new CsvWriter(stream, ',', StandardCharsets.UTF_8);
    writer.writeRecord(new String[] {"Name", "X", "Y", "Label"});

    for (int i = 0; i < scaledXYMatrix.length; i++) {
      writer.writeRecord(
          new String[] {
            names.get(i),
            Double.toString(scaledXYMatrix[i][0]),
            Double.toString(scaledXYMatrix[i][1]),
            clusterText.get(assignment.getPrototype(i))
          });
    }

    writer.close();
  }

  @Override
  public final File suggestName() {
    return new File("clustering.csv");
  }

  private void analyzeClusterAssignment() {
    final List<String> names = distanceMatrix.names();
    final Set<Integer> prototypes = assignment.getPrototypesIndices();
    final Color[] colorPalette = ColorBrewer.Set1.getColorPalette(prototypes.size());
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
}
