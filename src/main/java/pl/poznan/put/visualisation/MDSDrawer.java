package pl.poznan.put.visualisation;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.util.SVGConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.stat.StatUtils;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;

import pl.poznan.put.clustering.hierarchical.Cluster;
import pl.poznan.put.clustering.hierarchical.Clusterer;
import pl.poznan.put.clustering.hierarchical.HierarchicalClusterMerge;
import pl.poznan.put.clustering.hierarchical.HierarchicalClustering;
import pl.poznan.put.clustering.hierarchical.Linkage;
import pl.poznan.put.constant.Colors;
import pl.poznan.put.datamodel.ColoredNamedPoint;
import pl.poznan.put.datamodel.NamedPoint;
import pl.poznan.put.types.DistanceMatrix;
import pl.poznan.put.utility.svg.SVGHelper;

public class MDSDrawer {
    public static interface ColorProvider {
        Color getColor(int index);
    }

    private static final int DESIRED_WIDTH = 640;

    private static final ColorProvider EMPTY_COLOR_PROVIDER = new ColorProvider() {
        @Override
        public Color getColor(int index) {
            return Color.BLACK;
        }
    };

    public static SVGDocument scale2DAndVisualizePoints(
            DistanceMatrix distanceMatrix) {
        return MDSDrawer.scale2DAndVisualizePoints(distanceMatrix, MDSDrawer.EMPTY_COLOR_PROVIDER);
    }

    public static SVGDocument scale2DAndVisualizePoints(
            DistanceMatrix distanceMatrix, ColorProvider colorProvider) {
        List<NamedPoint> points = new ArrayList<>();
        double[][] originalDistanceMatrix = distanceMatrix.getMatrix();
        double[][] scaledXYMatrix = MDS.multidimensionalScaling(originalDistanceMatrix, 2);
        double[][] scaledDistanceMatrix = MDSDrawer.calculateScaledDistanceMatrix(originalDistanceMatrix, scaledXYMatrix);

        double maxDistance = Double.NEGATIVE_INFINITY;

        for (double[] element : scaledDistanceMatrix) {
            maxDistance = Math.max(maxDistance, StatUtils.max(element));
        }

        Clusterer clusterer = new Clusterer(distanceMatrix.getNames(), scaledDistanceMatrix, Linkage.COMPLETE);
        HierarchicalClustering clustering = clusterer.cluster();
        List<Cluster> clusters = Clusterer.initialClusterAssignment(distanceMatrix.getNames());
        Map<Cluster, Vector2D> clusterCoords = new HashMap<>();
        Map<Cluster, Set<Color>> clusterColors = new HashMap<>();

        for (int i = 0; i < clusters.size(); i++) {
            Cluster cluster = clusters.get(i);
            clusterCoords.put(cluster, new Vector2D(scaledXYMatrix[i][0], scaledXYMatrix[i][1]));

            Set<Color> colors = new HashSet<>();
            colors.add(colorProvider.getColor(i));
            clusterColors.put(cluster, colors);
        }

        for (HierarchicalClusterMerge merge : clustering.getMerges()) {
            if (merge.getDistance() > 0.1 * maxDistance) {
                break;
            }

            Cluster left = clusters.get(merge.getLeft());
            Cluster right = clusters.get(merge.getRight());
            Cluster merged = Cluster.merge(left, right);
            clusters.remove(left);
            clusters.remove(right);
            clusters.add(merged);

            Vector2D leftCoords = clusterCoords.get(left);
            Vector2D rightCoords = clusterCoords.get(right);
            double x = (leftCoords.getX() + rightCoords.getX()) / 2.0;
            double y = (leftCoords.getY() + rightCoords.getY()) / 2.0;
            Vector2D mergedCoords = new Vector2D(x, y);
            clusterCoords.put(merged, mergedCoords);

            Set<Color> leftColors = clusterColors.get(left);
            Set<Color> rightColors = clusterColors.get(right);
            Set<Color> mergedColors = new HashSet<>();
            mergedColors.addAll(leftColors);
            mergedColors.addAll(rightColors);
            clusterColors.put(merged, mergedColors);
        }

        for (Cluster cluster : clusters) {
            Set<Color> colors = clusterColors.get(cluster);
            String name = cluster.getName();
            Vector2D coords = clusterCoords.get(cluster);
            points.add(new ColoredNamedPoint(colors, name, coords));
        }

        return MDSDrawer.drawPoints(points);
    }

    private static double[][] calculateScaledDistanceMatrix(
            double[][] originalDistanceMatrix, double[][] scaledXYMatrix) {
        double[][] scaledDistanceMatrix = new double[originalDistanceMatrix.length][];

        for (int i = 0; i < originalDistanceMatrix.length; i++) {
            scaledDistanceMatrix[i] = new double[originalDistanceMatrix.length];
        }

        for (int i = 0; i < originalDistanceMatrix.length; i++) {
            Vector2D pi = new Vector2D(scaledXYMatrix[i][0], scaledXYMatrix[i][1]);

            for (int j = i + 1; j < originalDistanceMatrix.length; j++) {
                Vector2D pj = new Vector2D(scaledXYMatrix[j][0], scaledXYMatrix[j][1]);
                scaledDistanceMatrix[i][j] = scaledDistanceMatrix[j][i] = pi.distance(pj);
            }
        }
        return scaledDistanceMatrix;
    }

    public static SVGDocument drawPoints(List<? extends NamedPoint> points) {
        DOMImplementation dom = SVGDOMImplementation.getDOMImplementation();
        SVGDocument document = (SVGDocument) dom.createDocument(SVGDOMImplementation.SVG_NAMESPACE_URI, "svg", null);
        Element svgRoot = document.getDocumentElement();

        MDSDrawer.createAndAddLinearGradients(document, svgRoot, points);
        MDSDrawer.createAndAddTextElements(document, svgRoot, points);

        Rectangle2D boundingBox = SVGHelper.calculateBoundingBox(document);
        svgRoot.setAttributeNS(null, SVGConstants.SVG_VIEW_BOX_ATTRIBUTE, boundingBox.getMinX() + " " + boundingBox.getMinY() + " " + boundingBox.getWidth() + " " + boundingBox.getHeight());
        svgRoot.setAttributeNS(null, SVGConstants.SVG_WIDTH_ATTRIBUTE, Double.toString(boundingBox.getWidth()));
        svgRoot.setAttributeNS(null, SVGConstants.SVG_HEIGHT_ATTRIBUTE, Double.toString(boundingBox.getHeight()));
        return document;
    }

    private static void createAndAddTextElements(SVGDocument document,
            Element svgRoot, List<? extends NamedPoint> points) {
        double maxDistance = MDSDrawer.calculateMaxDistance(points);
        double scale = MDSDrawer.DESIRED_WIDTH / maxDistance;

        for (NamedPoint point : points) {
            NamedPoint scaled = point.scalarMultiply(scale);
            double x = scaled.getX();
            double y = scaled.getY();

            Element element = document.createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI, SVGConstants.SVG_TEXT_TAG);
            element.setAttributeNS(null, SVGConstants.SVG_X_ATTRIBUTE, Double.toString(x));
            element.setAttributeNS(null, SVGConstants.SVG_Y_ATTRIBUTE, Double.toString(y));
            element.setAttributeNS(null, SVGConstants.SVG_TEXT_ANCHOR_ATTRIBUTE, "middle");

            if (point instanceof ColoredNamedPoint) {
                Set<Color> colors = ((ColoredNamedPoint) point).getColors();

                if (colors.size() == 1) {
                    element.setAttributeNS(null, SVGConstants.SVG_FILL_ATTRIBUTE, Colors.toHexString(colors.iterator().next()));
                } else if (colors.size() > 1) {
                    element.setAttributeNS(null, SVGConstants.SVG_FILL_ATTRIBUTE, "url(#" + StringUtils.deleteWhitespace(point.getName()) + ")");
                }
            }

            element.setTextContent(point.getName());
            svgRoot.appendChild(element);
        }
    }

    private static void createAndAddLinearGradients(SVGDocument document,
            Element svgRoot, List<? extends NamedPoint> points) {
        Element defs = document.createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI, SVGConstants.SVG_DEFS_TAG);

        for (NamedPoint point : points) {
            if (point instanceof ColoredNamedPoint) {
                Set<Color> colors = ((ColoredNamedPoint) point).getColors();

                if (colors.size() > 1) {
                    Element linearGradient = document.createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI, SVGConstants.SVG_LINEAR_GRADIENT_TAG);
                    linearGradient.setAttributeNS(null, SVGConstants.SVG_ID_ATTRIBUTE, StringUtils.deleteWhitespace(point.getName()));
                    linearGradient.setAttributeNS(null, SVGConstants.SVG_X1_ATTRIBUTE, "0%");
                    linearGradient.setAttributeNS(null, SVGConstants.SVG_Y1_ATTRIBUTE, "0%");
                    linearGradient.setAttributeNS(null, SVGConstants.SVG_X2_ATTRIBUTE, "100%");
                    linearGradient.setAttributeNS(null, SVGConstants.SVG_Y2_ATTRIBUTE, "0%");

                    int i = 0;
                    int step = 100 / (colors.size() - 1);

                    for (Color color : colors) {
                        Element stop = document.createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI, SVGConstants.SVG_STOP_TAG);
                        stop.setAttributeNS(null, SVGConstants.SVG_OFFSET_ATTRIBUTE, Integer.toString(i * step) + "%");
                        stop.setAttributeNS(null, SVGConstants.SVG_STYLE_ATTRIBUTE, "stop-color: " + Colors.toSvgString(color) + "; stop-opacity: 1");
                        linearGradient.appendChild(stop);
                        i += 1;
                    }

                    defs.appendChild(linearGradient);
                }
            }
        }

        svgRoot.appendChild(defs);
    }

    private static double calculateMaxDistance(List<? extends NamedPoint> points) {
        double maxDistance = 0;

        for (int i = 0; i < points.size(); i++) {
            NamedPoint pi = points.get(i);

            for (int j = i + 1; j < points.size(); j++) {
                NamedPoint pj = points.get(j);
                double distance = pi.distance(pj);

                if (distance > maxDistance) {
                    maxDistance = distance;
                }
            }
        }

        return maxDistance;
    }

    private MDSDrawer() {
        // empty constructor
    }
}
