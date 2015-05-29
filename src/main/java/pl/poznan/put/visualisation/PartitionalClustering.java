package pl.poznan.put.visualisation;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mdsj.MDSJ;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.w3c.dom.svg.SVGDocument;

import pl.poznan.put.clustering.partitional.ClusterAssignment;
import pl.poznan.put.clustering.partitional.ClusterPrototypes;
import pl.poznan.put.clustering.partitional.ScoredClusteringResult;
import pl.poznan.put.clustering.partitional.ScoringFunction;
import pl.poznan.put.constant.Colors;
import pl.poznan.put.datamodel.ColoredNamedPoint;
import pl.poznan.put.interfaces.Visualizable;
import pl.poznan.put.types.DistanceMatrix;

public class PartitionalClustering implements Visualizable {
    private final Map<Integer, Color> clusterColor = new HashMap<>();
    private final Map<Integer, String> clusterText = new HashMap<>();

    private final ClusterAssignment assignment;

    private final DistanceMatrix distanceMatrix;
    private final ScoredClusteringResult clustering;

    public PartitionalClustering(DistanceMatrix distanceMatrix,
            ScoredClusteringResult clustering) {
        this.distanceMatrix = distanceMatrix;
        this.clustering = clustering;

        ClusterPrototypes prototypes = clustering.getPrototypes();
        assignment = ClusterAssignment.fromPrototypes(prototypes, distanceMatrix.getMatrix());
        analyzeClusterAssignment();
    }

    private void analyzeClusterAssignment() {
        List<String> names = distanceMatrix.getNames();
        int index = 0;

        for (int prototype : assignment.getPrototypesIndices()) {
            StringBuilder builder = new StringBuilder("Cluster: { ");
            for (int i : assignment.getAssignedTo(prototype)) {
                builder.append(names.get(i));
                builder.append(", ");
            }
            builder.delete(builder.length() - 2, builder.length());
            builder.append(" }");

            clusterColor.put(prototype, Colors.DISTINCT_COLORS[index++]);
            clusterText.put(prototype, builder.toString());
        }
    }

    public ScoringFunction getScoringFunction() {
        return clustering.getScoringFunction();
    }

    @Override
    public SVGDocument visualize() {
        List<ColoredNamedPoint> points = new ArrayList<>();
        double[][] matrix = distanceMatrix.getMatrix();
        double[][] xyMatrix = MDSJ.stressMinimization(matrix);

        for (int i = 0; i < matrix.length; i++) {
            Color color = getClusterColor(i);
            String name = getClusterDescription(i);
            Vector2D point = new Vector2D(xyMatrix[0][i], xyMatrix[1][i]);
            points.add(new ColoredNamedPoint(color, name, point));
        }

        return MDSDrawer.drawPoints(points);
    }

    private Color getClusterColor(int index) {
        int prototype = assignment.getPrototype(index);
        return clusterColor.get(prototype);
    }

    private String getClusterDescription(int index) {
        int prototype = assignment.getPrototype(index);
        return distanceMatrix.getNames().get(index) + "\n\n" + clusterText.get(prototype);
    }

    @Override
    public void visualize3D() {
        // do nothing
    }
}
