package pl.poznan.put.cs.bioserver.beans;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.jdt.annotation.Nullable;
import org.jzy3d.chart.Chart;
import org.jzy3d.chart.ChartLauncher;
import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.Sphere;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import org.jzy3d.plot3d.rendering.scene.Graph;

import pl.poznan.put.cs.bioserver.beans.auxiliary.Cluster;
import pl.poznan.put.cs.bioserver.beans.auxiliary.Cluster3D;
import pl.poznan.put.cs.bioserver.beans.auxiliary.Point;
import pl.poznan.put.cs.bioserver.beans.auxiliary.Point3D;
import pl.poznan.put.cs.bioserver.beans.auxiliary.RGB;
import pl.poznan.put.cs.bioserver.clustering.ClustererKMedoids;
import pl.poznan.put.cs.bioserver.clustering.ClustererKMedoids.Result;
import pl.poznan.put.cs.bioserver.clustering.ClustererKMedoids.ScoringFunction;
import pl.poznan.put.cs.bioserver.clustering.KMedoidsPlot;
import pl.poznan.put.cs.bioserver.external.Matplotlib;
import pl.poznan.put.cs.bioserver.helper.Colors;
import pl.poznan.put.cs.bioserver.helper.Visualizable;
import pl.poznan.put.cs.bioserver.visualisation.MDS;

import com.sun.media.sound.InvalidDataException;

@XmlRootElement
public class ClusteringPartitional extends XMLSerializable implements Visualizable {
    private static final long serialVersionUID = -7474446942015119359L;

    public static ClusteringPartitional newInstance(ComparisonGlobal comparison,
            ScoringFunction scoringFunction, @Nullable Integer k) throws InvalidDataException {
        double[][] distanceMatrix = comparison.getDistanceMatrix();
        double[][] mds2D = MDS.multidimensionalScaling(distanceMatrix, 2);
        double[][] mds3D = MDS.multidimensionalScaling(distanceMatrix, 3);

        Result clustering = ClustererKMedoids.kMedoids(distanceMatrix, scoringFunction, k);
        Map<Integer, Set<Integer>> clusterMap = ClustererKMedoids.getClusterAssignments(
                clustering.medoids, distanceMatrix);

        List<Point> medoids = new ArrayList<>();
        for (int index : clusterMap.keySet()) {
            Point medoid = new Point();
            medoid.setX(mds2D[index][0]);
            medoid.setY(mds2D[index][1]);
            medoids.add(medoid);
        }

        List<Cluster> clusters = new ArrayList<>();
        List<Cluster3D> clusters3D = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        List<String> labelsAll = comparison.getLabels();
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

        ClusteringPartitional instance = new ClusteringPartitional();
        instance.comparison = comparison;
        instance.labels = labels;
        instance.medoids = medoids;
        instance.clusters = clusters;
        instance.clusters3D = clusters3D;
        instance.scoringFunction = scoringFunction.toString();
        instance.colors = Colors.toRGB();
        return instance;
    }

    ComparisonGlobal comparison;
    List<Cluster> clusters;
    List<Cluster3D> clusters3D;
    List<Point> medoids;
    List<String> labels;
    List<RGB> colors;
    String scoringFunction;

    public List<Cluster> getClusters() {
        return clusters;
    }

    public List<RGB> getColors() {
        return colors;
    }

    public ComparisonGlobal getComparison() {
        return comparison;
    }

    public List<String> getLabels() {
        return labels;
    }

    public List<Point> getMedoids() {
        return medoids;
    }

    public String getScoringFunction() {
        return scoringFunction;
    }

    @XmlElementWrapper(name = "cluster")
    @XmlElement(name = "points")
    public void setClusters(List<Cluster> clusters) {
        this.clusters = clusters;
    }

    @XmlElementWrapper(name = "colors")
    @XmlElement(name = "item")
    public void setColors(List<RGB> colors) {
        this.colors = colors;
    }

    @XmlElement
    public void setComparison(ComparisonGlobal comparison) {
        this.comparison = comparison;
    }

    @XmlElementWrapper(name = "labels")
    @XmlElement(name = "item")
    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    @XmlElementWrapper(name = "medoids")
    @XmlElement(name = "item")
    public void setMedoids(List<Point> medoids) {
        this.medoids = medoids;
    }

    @XmlElement
    public void setScoringFunction(String method) {
        scoringFunction = method;
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
        for (Cluster3D cluster : clusters3D) {
            for (Point3D point : cluster.getPoints()) {
                double lmin = Math.min(Math.min(point.getX(), point.getY()), point.getY());
                double lmax = Math.min(Math.min(point.getX(), point.getY()), point.getY());
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
        for (Cluster3D cluster : clusters3D) {
            Color color = Color.random();
            for (Point3D point : cluster.getPoints()) {
                Sphere sphere = new Sphere(new Coord3d(point.getX(), point.getY(), point.getZ()),
                        (float) ((max - min) / comparison.labels.size()), 15, color);
                sphere.setWireframeColor(color.negative());
                graph.add(sphere);
            }
        }
        ChartLauncher.openChart(chart);
    }

    @Override
    public void visualizeHighQuality() {
        URL resource = getClass().getResource(
                "/pl/poznan/put/cs/bioserver/external/MatplotlibPartitional.xsl");
        Matplotlib.runXsltAndPython(resource, this);
    }
}
