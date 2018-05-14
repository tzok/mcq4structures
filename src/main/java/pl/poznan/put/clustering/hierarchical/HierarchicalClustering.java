package pl.poznan.put.clustering.hierarchical;

import java.awt.FontMetrics;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.util.SVGConstants;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.tuple.Pair;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;
import pl.poznan.put.interfaces.Visualizable;
import pl.poznan.put.utility.svg.SVGHelper;

public class HierarchicalClustering implements Visualizable {
  private final List<String> names;
  private final List<HierarchicalClusterMerge> merges;

  public HierarchicalClustering(
      final List<String> names, final List<HierarchicalClusterMerge> merges) {
    super();
    this.names = new ArrayList<>(names);
    this.merges = new ArrayList<>(merges);
  }

  public final List<String> getNames() {
    return Collections.unmodifiableList(names);
  }

  public final List<HierarchicalClusterMerge> getMerges() {
    return Collections.unmodifiableList(merges);
  }

  @Override
  public final SVGDocument visualize() {
    final SVGDocument document = SVGHelper.emptyDocument();
    final SVGGraphics2D graphics = new SVGGraphics2D(document);

    // final cluster has all items in good order for drawing!
    final Cluster last = getFinalCluster();
    final List<Integer> items = last.getItems();
    drawNames(graphics, items);
    final int maxWidth = getMaxNameDrawnWidth(graphics, items);

    /*
     * Initialize positions of all dendrogram leaves
     */
    final List<Cluster> clusters = Clusterer.initialClusterAssignment(names);
    final Map<Cluster, Pair<Integer, Integer>> mapCoords = new HashMap<>();
    final LineMetrics lineMetrics = SVGHelper.getLineMetrics(graphics);
    final int fontHeight = (int) Math.ceil(lineMetrics.getHeight());
    final int fontAscent = (int) Math.ceil(lineMetrics.getAscent());

    for (int i = 0; i < items.size(); i++) {
      final int item = items.get(i);
      final int y = ((i + 1) * fontHeight) - (fontAscent / 2);
      mapCoords.put(clusters.get(item), Pair.of(maxWidth, y));
    }

    /*
     * Draw the dendrogram
     */
    final double maxDistance = getMaxClusterDistance();

    for (final HierarchicalClusterMerge merge : merges) {
      final Cluster left = clusters.get(merge.getLeft());
      final Cluster right = clusters.get(merge.getRight());
      final Cluster merged = Cluster.merge(left, right);

      final Pair<Integer, Integer> coordsFirst = mapCoords.get(left);
      final int x1 = coordsFirst.getLeft();
      final int y1 = coordsFirst.getRight();
      final Pair<Integer, Integer> coordsSecond = mapCoords.get(right);
      final int x2 = coordsSecond.getLeft();
      final int y2 = coordsSecond.getRight();

      final int xf = maxWidth + (int) ((merge.getDistance() * (640 - maxWidth)) / maxDistance);

      graphics.drawLine(x1, y1, xf, y1);
      graphics.drawLine(x2, y2, xf, y2);
      graphics.drawLine(xf, y1, xf, y2);

      mapCoords.put(merged, Pair.of(xf, (y1 + y2) / 2));
      clusters.remove(left);
      clusters.remove(right);
      clusters.add(merged);
    }

    final Element documentElement = document.getDocumentElement();
    final Element root = graphics.getRoot(documentElement);
    final Rectangle2D boundingBox = SVGHelper.calculateBoundingBox(document);
    root.setAttributeNS(
        null,
        SVGConstants.SVG_VIEW_BOX_ATTRIBUTE,
        String.format(
            "%s %s %s %s",
            boundingBox.getMinX(),
            boundingBox.getMinY(),
            boundingBox.getWidth(),
            boundingBox.getHeight()));
    root.setAttributeNS(
        null, SVGConstants.SVG_WIDTH_ATTRIBUTE, Double.toString(boundingBox.getWidth()));
    root.setAttributeNS(
        null, SVGConstants.SVG_HEIGHT_ATTRIBUTE, Double.toString(boundingBox.getHeight()));

    return document;
  }

  private Cluster getFinalCluster() {
    final List<Cluster> clusters = Clusterer.initialClusterAssignment(names);

    for (final HierarchicalClusterMerge merge : merges) {
      final Cluster left = clusters.get(merge.getLeft());
      final Cluster right = clusters.get(merge.getRight());
      final Cluster merged = Cluster.merge(left, right);
      clusters.remove(left);
      clusters.remove(right);
      clusters.add(merged);
    }

    assert clusters.size() == 1;
    return clusters.get(0);
  }

  private void drawNames(final SVGGraphics2D graphics, final List<Integer> items) {
    final LineMetrics lineMetrics = SVGHelper.getLineMetrics(graphics);
    final int fontHeight = (int) Math.ceil(lineMetrics.getHeight());

    for (int i = 0; i < items.size(); i++) {
      final int item = items.get(i);
      final String name = names.get(item);
      graphics.drawString(name, 0, (i + 1) * fontHeight);
    }
  }

  private int getMaxNameDrawnWidth(final SVGGraphics2D graphics, final Iterable<Integer> items) {
    final FontMetrics metrics = SVGHelper.getFontMetrics(graphics);
    int maxWidth = Integer.MIN_VALUE;

    for (final Integer item : items) {
      final String name = names.get(item);
      final int width = metrics.stringWidth(name);

      if (width > maxWidth) {
        maxWidth = width;
      }
    }

    return maxWidth;
  }

  private double getMaxClusterDistance() {
    double maxDistance = Double.NEGATIVE_INFINITY;

    for (final HierarchicalClusterMerge merge : merges) {
      final double distance = merge.getDistance();
      if (distance > maxDistance) {
        maxDistance = distance;
      }
    }
    return maxDistance;
  }

  @Override
  public final void visualize3D() {
    throw new NotImplementedException(
        "3D visualization is not supported for hierarchical clustering");
  }
}
