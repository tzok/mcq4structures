package pl.poznan.put.beans;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.jzy3d.chart.Chart;
import org.jzy3d.chart.ChartLauncher;
import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.Sphere;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import org.jzy3d.plot3d.rendering.scene.Graph;
import org.jzy3d.plot3d.text.drawable.DrawableTextBitmap;

import pl.poznan.put.beans.auxiliary.Cluster;
import pl.poznan.put.beans.auxiliary.Cluster3D;
import pl.poznan.put.beans.auxiliary.Point;
import pl.poznan.put.beans.auxiliary.Point3D;
import pl.poznan.put.clustering.ClustererKMedoids;
import pl.poznan.put.clustering.ClustererKMedoids.Result;
import pl.poznan.put.clustering.ClustererKMedoids.ScoringFunction;
import pl.poznan.put.gui.KMedoidsPlot;
import pl.poznan.put.helper.Constants;
import pl.poznan.put.helper.RGB;
import pl.poznan.put.helper.XMLSerializable;
import pl.poznan.put.interfaces.Visualizable;
import pl.poznan.put.mcqgraphics.Matplotlib;
import pl.poznan.put.utility.InvalidInputException;
import pl.poznan.put.visualisation.MDS;

@XmlRootElement
public class ClusteringPartitional extends XMLSerializable implements
        Visualizable {
    private static final long serialVersionUID = -7474446942015119359L;

    public static ClusteringPartitional newInstance(
            ComparisonGlobal comparison, ScoringFunction scoringFunction,
            Integer k) throws InvalidInputException {
        double[][] distanceMatrix = comparison.getDistanceMatrix();
        double[][] mds2D = MDS.multidimensionalScaling(distanceMatrix, 2);
        double[][] mds3D = MDS.multidimensionalScaling(distanceMatrix, 3);

        ClustererKMedoids clusterer = new ClustererKMedoids();
        Result clustering = clusterer.kMedoids(distanceMatrix, scoringFunction,
                k);
        Map<Integer, Set<Integer>> clusterMap = ClustererKMedoids.getClusterAssignments(
                clustering.getMedoids(), distanceMatrix);

        List<String> labelsAll = comparison.getLabels();
        List<Point> medoids = new ArrayList<>();
        for (int index : clusterMap.keySet()) {
            Point medoid = new Point();
            medoid.setLabel(labelsAll.get(index));
            medoid.setX(mds2D[index][0]);
            medoid.setY(mds2D[index][1]);
            medoids.add(medoid);
        }

        List<Cluster> clusters = new ArrayList<>();
        List<Cluster3D> clusters3D = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (Entry<Integer, Set<Integer>> entry : clusterMap.entrySet()) {
            List<Point> points = new ArrayList<>();
            List<Point3D> points3D = new ArrayList<>();
            StringBuilder builder = new StringBuilder();
            for (int index : entry.getValue()) {
                builder.append(labelsAll.get(index));
                builder.append(", ");

                Point point = new Point();
                point.setLabel(labelsAll.get(index));
                point.setX(mds2D[index][0]);
                point.setY(mds2D[index][1]);
                points.add(point);

                Point3D point3D = new Point3D();
                point3D.setLabel(labelsAll.get(index));
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
            clusters3D.add(cluster3D);
        }

        return new ClusteringPartitional(clusters, clusters3D,
                Constants.colorsAsRGB(), comparison, labels, medoids,
                scoringFunction);
    }

    private List<Cluster> clusters;
    private List<Cluster3D> clusters3d;
    private List<RGB> colors;
    private ComparisonGlobal comparison;
    private List<String> labels;
    private List<Point> medoids;
    private ScoringFunction scoringFunction;

    public ClusteringPartitional() {
    }

    private ClusteringPartitional(List<Cluster> clusters,
            List<Cluster3D> clusters3d, List<RGB> colors,
            ComparisonGlobal comparison, List<String> labels,
            List<Point> medoids, ScoringFunction scoringFunction) {
        super();
        this.clusters = clusters;
        this.clusters3d = clusters3d;
        this.colors = colors;
        this.comparison = comparison;
        this.labels = labels;
        this.medoids = medoids;
        this.scoringFunction = scoringFunction;
    }

    public List<Cluster> getClusters() {
        return clusters;
    }

    @XmlElement
    public void setClusters(List<Cluster> clusters) {
        this.clusters = clusters;
    }

    public List<RGB> getColors() {
        return colors;
    }

    @XmlElement
    public void setColors(List<RGB> colors) {
        this.colors = colors;
    }

    public ComparisonGlobal getComparison() {
        return comparison;
    }

    @XmlElement
    public void setComparison(ComparisonGlobal comparison) {
        this.comparison = comparison;
    }

    public List<String> getLabels() {
        return labels;
    }

    @XmlElement
    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public List<Point> getMedoids() {
        return medoids;
    }

    @XmlElement
    public void setMedoids(List<Point> medoids) {
        this.medoids = medoids;
    }

    public ScoringFunction getScoringFunction() {
        return scoringFunction;
    }

    public String getScoringFunctionName() {
        return scoringFunction.toString();
    }

    @XmlElement
    public void setScoringFunctionName(String name) {
        switch (name) {
        case ClustererKMedoids.NAME_PAM:
            scoringFunction = ClustererKMedoids.PAM;
            break;
        case ClustererKMedoids.NAME_PAMSIL:
            scoringFunction = ClustererKMedoids.PAMSIL;
            break;
        default:
            scoringFunction = null;
        }
    }

    @Override
    public void visualize() {
        KMedoidsPlot plot = new KMedoidsPlot(this);
        plot.setVisible(true);
    }

    @Override
    public void visualize3D() {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for (Cluster3D cluster : clusters3d) {
            for (Point3D point : cluster.getPoints()) {
                double lmin = Math.min(Math.min(point.getX(), point.getY()),
                        point.getY());
                double lmax = Math.min(Math.min(point.getX(), point.getY()),
                        point.getY());
                if (lmin < min) {
                    min = lmin;
                }
                if (lmax > max) {
                    max = lmax;
                }
            }
        }

        Chart chart = new Chart(Quality.Nicest);
        Graph graph = chart.getScene().getGraph();
        for (int i = 0; i < clusters3d.size(); i++) {
            Cluster3D cluster = clusters3d.get(i);
            java.awt.Color c = Constants.COLORS.get(i + 1);
            Color color = new Color(c.getRed(), c.getGreen(), c.getBlue());
            boolean isLabeled = false;
            for (Point3D point : cluster.getPoints()) {
                Coord3d center = new Coord3d(point.getX(), point.getY(),
                        point.getZ());
                float radius = (float) ((max - min) / comparison.getLabels().size());
                Sphere sphere = new Sphere(center, radius, 15, color);
                sphere.setWireframeColor(Color.BLACK);
                graph.add(sphere);
                if (!isLabeled) {
                    graph.add(new DrawableTextBitmap(labels.get(i), center.add(
                            radius, radius, radius), color));
                    isLabeled = true;
                }
            }
        }

        ChartLauncher.openChart(chart);
    }

    @Override
    public void visualizeHighQuality() {
    }
}
