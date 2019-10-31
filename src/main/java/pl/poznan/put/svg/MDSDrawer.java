package pl.poznan.put.svg;

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.util.SVGConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.geometry.euclidean.twod.Segment;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.geometry.euclidean.twod.hull.ConvexHull2D;
import org.apache.commons.math3.geometry.euclidean.twod.hull.ConvexHullGenerator2D;
import org.apache.commons.math3.geometry.euclidean.twod.hull.MonotoneChain;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGSVGElement;
import pl.poznan.put.types.DistanceMatrix;
import pl.poznan.put.utility.svg.SVGHelper;

import java.awt.*;
import java.awt.font.LineMetrics;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public final class MDSDrawer {
  private static final int DESIRED_WIDTH = 320;
  private static final int CIRCLE_DIAMETER = 10;
  private static final int MAX_CLUSTER_NAME = 64;
  private static final ColorProvider COLOR_PROVIDER = index -> Color.BLACK;
  private static final NameProvider NAME_PROVIDER = index -> "";
  private MDSDrawer() {
    super();
  }

  public static SVGDocument scale2DAndVisualizePoints(final DistanceMatrix distanceMatrix) {
    return MDSDrawer.scale2DAndVisualizePoints(
        distanceMatrix, MDSDrawer.COLOR_PROVIDER, MDSDrawer.NAME_PROVIDER);
  }

  public static SVGDocument scale2DAndVisualizePoints(
      final DistanceMatrix distanceMatrix,
      final ColorProvider colorProvider,
      final NameProvider nameProvider) {
    final SVGDocument document = SVGHelper.emptyDocument();
    final SVGGraphics2D graphics = new SVGGraphics2D(document);
    graphics.setFont(new Font("monospaced", Font.PLAIN, 10));

    final double[][] scaledXYMatrix = MDSDrawer.scaleTo2D(distanceMatrix);

    for (int i = 0; i < scaledXYMatrix.length; i++) {
      final double x = scaledXYMatrix[i][0];
      final double y = scaledXYMatrix[i][1];
      graphics.setColor(colorProvider.getColor(i));
      graphics.draw(
          new Ellipse2D.Double(x, y, MDSDrawer.CIRCLE_DIAMETER, MDSDrawer.CIRCLE_DIAMETER));
    }

    final Map<Color, List<Integer>> colorMap = new HashMap<>();
    for (int i = 0; i < scaledXYMatrix.length; i++) {
      final Color color = colorProvider.getColor(i);
      if (!colorMap.containsKey(color)) {
        colorMap.put(color, new ArrayList<>());
      }
      colorMap.get(color).add(i);
    }

    if (colorMap.size() > 1) {
      final LineMetrics lineMetrics = SVGHelper.getLineMetrics(graphics);
      final float lineHeight = lineMetrics.getHeight();
      float legendHeight = 0.0f;

      for (final Map.Entry<Color, List<Integer>> entry : colorMap.entrySet()) {
        final List<Integer> indices = entry.getValue();
        assert !indices.isEmpty();

        final int first = indices.get(0);
        final String clusterName = nameProvider.getName(first);
        final String nameAbbreviated =
            StringUtils.abbreviate(clusterName, MDSDrawer.MAX_CLUSTER_NAME);

        graphics.setColor(entry.getKey());
        graphics.draw(
            new Ellipse2D.Double(
                MDSDrawer.DESIRED_WIDTH + MDSDrawer.CIRCLE_DIAMETER,
                legendHeight,
                MDSDrawer.CIRCLE_DIAMETER,
                MDSDrawer.CIRCLE_DIAMETER));
        graphics.drawString(
            nameAbbreviated,
            (float) (MDSDrawer.DESIRED_WIDTH + (MDSDrawer.CIRCLE_DIAMETER * 2.5)),
            (legendHeight + lineHeight) - (MDSDrawer.CIRCLE_DIAMETER / 2.0f));
        legendHeight += lineHeight;

        if (indices.size() <= 2) {
          continue;
        }

        final ConvexHull2D convexHull = MDSDrawer.calculateConvexHull(scaledXYMatrix, indices);
        final Segment[] segments = convexHull.getLineSegments();
        final Path2D.Double path = new Path2D.Double();

        for (final Segment segment : segments) {
          final Vector2D start = segment.getStart();
          path.moveTo(start.getX(), start.getY());
          final Vector2D end = segment.getEnd();
          path.lineTo(end.getX(), end.getY());
        }

        graphics.draw(path);
      }
    }

    final SVGSVGElement rootElement = document.getRootElement();
    graphics.getRoot(rootElement);

    final Rectangle2D box = SVGHelper.calculateBoundingBox(document);
    final String viewBox =
        String.format(
            Locale.US, "%f %f %f %f", box.getX(), box.getY(), box.getWidth(), box.getHeight());
    rootElement.setAttributeNS(null, SVGConstants.SVG_VIEW_BOX_ATTRIBUTE, viewBox);
    rootElement.setAttributeNS(
        null, SVGConstants.SVG_WIDTH_ATTRIBUTE, Double.toString(box.getWidth()));
    rootElement.setAttributeNS(
        null, SVGConstants.SVG_HEIGHT_ATTRIBUTE, Double.toString(box.getHeight()));
    return document;
  }

  public static double[][] scaleTo2D(final DistanceMatrix distanceMatrix) {
    final double[][] originalDistanceMatrix = distanceMatrix.getMatrix();
    final double[][] scaledXYMatrix = MDS.multidimensionalScaling(originalDistanceMatrix, 2);
    final Rectangle2D bounds = MDSDrawer.calculateBounds(scaledXYMatrix);

    for (int i = 0; i < scaledXYMatrix.length; i++) {
      scaledXYMatrix[i][0] =
          (scaledXYMatrix[i][0] - bounds.getX()) * (MDSDrawer.DESIRED_WIDTH / bounds.getWidth());
      scaledXYMatrix[i][1] =
          (scaledXYMatrix[i][1] - bounds.getY()) * (MDSDrawer.DESIRED_WIDTH / bounds.getHeight());
    }
    return scaledXYMatrix;
  }

  private static ConvexHull2D calculateConvexHull(
      final double[][] scaledXYMatrix, final Collection<Integer> indices) {
    final List<Vector2D> points =
        indices.stream()
            .map(
                index ->
                    new Vector2D(
                        scaledXYMatrix[index][0] + (MDSDrawer.CIRCLE_DIAMETER / 2.0),
                        scaledXYMatrix[index][1] + (MDSDrawer.CIRCLE_DIAMETER / 2.0)))
            .collect(Collectors.toList());
    final ConvexHullGenerator2D generator = new MonotoneChain();
    return generator.generate(points);
  }

  private static Rectangle2D calculateBounds(final double[][] scaledXYMatrix) {
    double minX = Double.POSITIVE_INFINITY;
    double maxX = Double.NEGATIVE_INFINITY;
    double minY = Double.POSITIVE_INFINITY;
    double maxY = Double.NEGATIVE_INFINITY;

    for (final double[] aScaledXYMatrix : scaledXYMatrix) {
      final double x = aScaledXYMatrix[0];
      final double y = aScaledXYMatrix[1];

      if (x < minX) {
        minX = x;
      }
      if (x > maxX) {
        maxX = x;
      }
      if (y < minY) {
        minY = y;
      }
      if (y > maxY) {
        maxY = y;
      }
    }

    return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
  }

  @FunctionalInterface
  public interface ColorProvider {
    Color getColor(int index);
  }

  @FunctionalInterface
  public interface NameProvider {
    String getName(int index);
  }
}
