package pl.poznan.put.visualisation;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.w3c.dom.svg.SVGDocument;

import pl.poznan.put.clustering.partitional.ClusterAssignment;
import pl.poznan.put.clustering.partitional.ScoredClusteringResult;
import pl.poznan.put.clustering.partitional.ScoringFunction;
import pl.poznan.put.comparison.GlobalComparisonResultMatrix;
import pl.poznan.put.constant.Colors;
import pl.poznan.put.datamodel.ColoredNamedPoint;
import pl.poznan.put.interfaces.Visualizable;

public class PartitionalClustering implements Visualizable {
    private final Map<Integer, Color> clusterColor = new HashMap<>();
    private final Map<Integer, String> clusterText = new HashMap<>();

    private final ClusterAssignment assignment;

    private final GlobalComparisonResultMatrix matrix;
    private final ScoredClusteringResult clustering;

    public PartitionalClustering(GlobalComparisonResultMatrix matrix,
            ScoredClusteringResult clustering) {
        this.matrix = matrix;
        this.clustering = clustering;

        assignment = ClusterAssignment.fromPrototypes(clustering.getPrototypes(), matrix.getDistanceMatrix().getArray());
        analyzeClusterAssignment();
    }

    private void analyzeClusterAssignment() {
        int index = 0;

        for (int prototype : assignment.getPrototypes()) {
            StringBuilder builder = new StringBuilder("Cluster: { ");
            for (int i : assignment.getAssignedTo(prototype)) {
                builder.append(matrix.getName(i));
                builder.append(", ");
            }
            builder.delete(builder.length() - 2, builder.length());
            builder.append(" }");

            clusterColor.put(prototype, Colors.DISTINCT_COLORS[index++]);
            clusterText.put(prototype, builder.toString());
        }
    }

    public GlobalComparisonResultMatrix getMatrix() {
        return matrix;
    }

    public ScoringFunction getScoringFunction() {
        return clustering.getScoringFunction();
    }

    @Override
    public SVGDocument visualize() {
        List<ColoredNamedPoint> points = new ArrayList<>();
        double[][] xyMatrix = MDS.multidimensionalScaling(matrix.getDistanceMatrix().getArray(), 2);

        for (int i = 0; i < xyMatrix.length; i++) {
            Color color = getClusterColor(i);
            String name = getClusterDescription(i);
            Vector2D point = new Vector2D(xyMatrix[i][0], xyMatrix[i][1]);
            points.add(new ColoredNamedPoint(color, name, point));
        }

        return SVGDrawer.drawPoints(points);
    }

    private Color getClusterColor(int index) {
        int prototype = assignment.getCluster(index);
        return clusterColor.get(prototype);
    }

    private String getClusterDescription(int index) {
        int prototype = assignment.getCluster(index);
        return matrix.getName(index) + "\n\n" + clusterText.get(prototype);
    }

    @Override
    public void visualize3D() {
        // do nothing
    }
}
