package pl.poznan.put.utility;

import java.util.ArrayList;
import java.util.List;

import pl.poznan.put.clustering.partitional.ClusterAssignment;
import pl.poznan.put.clustering.partitional.ScoredClusteringResult;
import pl.poznan.put.clustering.partitional.ScoringFunction;
import pl.poznan.put.comparison.GlobalComparisonResultMatrix;
import pl.poznan.put.gui.KMedoidsPlot;
import pl.poznan.put.interfaces.Visualizable;
import pl.poznan.put.visualisation.MDS;

public class PartitionalClustering implements Visualizable {
    public class Cluster {
        private List<Point> points = new ArrayList<>();

        public List<Point> getPoints() {
            return points;
        }

        public void setPoints(List<Point> points) {
            this.points = points;
        }
    }

    public class Cluster3D {
        private List<Point3D> points = new ArrayList<>();

        public List<Point3D> getPoints() {
            return points;
        }

        public void setPoints(List<Point3D> points) {
            this.points = points;
        }
    }

    public class Point {
        private String label = "";
        private double x;
        private double y;

        public String getLabel() {
            return label;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public void setX(double x) {
            this.x = x;
        }

        public void setY(double y) {
            this.y = y;
        }
    }

    public class Point3D {
        private String label = "";
        private double x;
        private double y;
        private double z;

        public String getLabel() {
            return label;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public double getZ() {
            return z;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public void setX(double x) {
            this.x = x;
        }

        public void setY(double y) {
            this.y = y;
        }

        public void setZ(double z) {
            this.z = z;
        }
    }

    private final List<Cluster> clusters;
    private final List<Cluster3D> clusters3d;
    private final GlobalComparisonResultMatrix matrix;
    private final List<String> labels;
    private final List<Point> medoids;
    private final ScoredClusteringResult clustering;

    public PartitionalClustering(GlobalComparisonResultMatrix matrix,
            ScoredClusteringResult clustering) throws InvalidInputException {
        this.matrix = matrix;
        this.clustering = clustering;

        double[][] distanceMatrix = matrix.getDistanceMatrix().getArray();
        double[][] mds2D = MDS.multidimensionalScaling(distanceMatrix, 2);
        double[][] mds3D = MDS.multidimensionalScaling(distanceMatrix, 3);

        ClusterAssignment assignment = ClusterAssignment.fromPrototypes(
                clustering.getPrototypes(),
                matrix.getDistanceMatrix().getArray());

        medoids = new ArrayList<>();

        for (int index : assignment.getPrototypes()) {
            Point medoid = new Point();
            medoid.setLabel(matrix.getName(index));
            medoid.setX(mds2D[index][0]);
            medoid.setY(mds2D[index][1]);
            medoids.add(medoid);
        }

        clusters = new ArrayList<>();
        clusters3d = new ArrayList<>();
        labels = new ArrayList<>();

        for (int prototype : assignment.getPrototypes()) {
            List<Point> points = new ArrayList<>();
            List<Point3D> points3D = new ArrayList<>();
            StringBuilder builder = new StringBuilder();

            for (int index : assignment.getAssignedTo(prototype)) {
                builder.append(matrix.getName(index));
                builder.append(", ");

                Point point = new Point();
                point.setLabel(matrix.getName(index));
                point.setX(mds2D[index][0]);
                point.setY(mds2D[index][1]);
                points.add(point);

                Point3D point3D = new Point3D();
                point3D.setLabel(matrix.getName(index));
                point3D.setX(mds3D[index][0]);
                point3D.setY(mds3D[index][1]);
                point3D.setZ(mds3D[index][2]);
                points3D.add(point3D);
            }

            builder.delete(builder.length() - 2, builder.length());
            labels.add(builder.toString());

            Cluster cluster = new Cluster();
            cluster.setPoints(points);
            clusters.add(cluster);
            Cluster3D cluster3D = new Cluster3D();
            cluster3D.setPoints(points3D);
            clusters3d.add(cluster3D);
        }
    }

    public List<Cluster> getClusters() {
        return clusters;
    }

    public GlobalComparisonResultMatrix getMatrix() {
        return matrix;
    }

    public List<String> getLabels() {
        return labels;
    }

    public List<Point> getMedoids() {
        return medoids;
    }

    public ScoringFunction getScoringFunction() {
        return clustering.getScoringFunction();
    }

    @Override
    public void visualize() {
        KMedoidsPlot plot = new KMedoidsPlot(this);
        plot.setVisible(true);
    }

    @Override
    public void visualize3D() {
        // double min = Double.POSITIVE_INFINITY;
        // double max = Double.NEGATIVE_INFINITY;
        // for (Cluster3D cluster : clusters3d) {
        // for (Point3D point : cluster.getPoints()) {
        // double lmin = Math.min(Math.min(point.getX(), point.getY()),
        // point.getY());
        // double lmax = Math.min(Math.min(point.getX(), point.getY()),
        // point.getY());
        // if (lmin < min) {
        // min = lmin;
        // }
        // if (lmax > max) {
        // max = lmax;
        // }
        // }
        // }
        //
        // Chart chart = new Chart(Quality.Nicest);
        // Graph graph = chart.getScene().getGraph();
        // for (int i = 0; i < clusters3d.size(); i++) {
        // Cluster3D cluster = clusters3d.get(i);
        // java.awt.Color c = Constants.COLORS.get(i + 1);
        // Color color = new Color(c.getRed(), c.getGreen(), c.getBlue());
        // boolean isLabeled = false;
        // for (Point3D point : cluster.getPoints()) {
        // Coord3d center = new Coord3d(point.getX(), point.getY(),
        // point.getZ());
        // float radius = (float) ((max - min) / matrix.getSize());
        // Sphere sphere = new Sphere(center, radius, 15, color);
        // sphere.setWireframeColor(Color.BLACK);
        // graph.add(sphere);
        // if (!isLabeled) {
        // graph.add(new DrawableTextBitmap(labels.get(i), center.add(
        // radius, radius, radius), color));
        // isLabeled = true;
        // }
        // }
        // }
        //
        // ChartLauncher.openChart(chart);
    }
}
