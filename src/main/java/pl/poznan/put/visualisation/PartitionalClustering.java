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
import pl.poznan.put.constant.Colors;
import pl.poznan.put.interfaces.Visualizable;
import pl.poznan.put.types.DistanceMatrix;
import pl.poznan.put.visualisation.MDSDrawer.ColorProvider;

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
        return MDSDrawer.scale2DAndVisualizePoints(distanceMatrix, new ColorProvider() {
            @Override
            public Color getColor(int index) {
                int prototype = assignment.getPrototype(index);
                return clusterColor.get(prototype);
            }
        });
    }

    @Override
    public void visualize3D() {
        // do nothing
    }
}
