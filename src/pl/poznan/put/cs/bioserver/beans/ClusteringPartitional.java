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

import pl.poznan.put.cs.bioserver.beans.auxiliary.Cluster;
import pl.poznan.put.cs.bioserver.beans.auxiliary.Point;
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
        double[][] mds = MDS.multidimensionalScaling(distanceMatrix, 2);

        Result clustering = ClustererKMedoids.kMedoids(distanceMatrix, scoringFunction, k);
        Map<Integer, Set<Integer>> clusterMap = ClustererKMedoids.getClusterAssignments(
                clustering.medoids, distanceMatrix);

        List<Point> medoids = new ArrayList<>();
        for (int index : clusterMap.keySet()) {
            Point medoid = new Point();
            medoid.setX(mds[index][0]);
            medoid.setY(mds[index][1]);
            medoids.add(medoid);
        }

        List<Cluster> clusters = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        List<String> labelsAll = comparison.getLabels();
        for (Entry<Integer, Set<Integer>> entry : clusterMap.entrySet()) {
            List<Point> points = new ArrayList<>();
            StringBuilder builder = new StringBuilder();
            for (int index : entry.getValue()) {
                builder.append(labelsAll.get(index));
                builder.append(", ");

                Point point = new Point();
                point.setLabel(labelsAll.get(index));
                point.setX(mds[index][0]);
                point.setY(mds[index][1]);
                points.add(point);
            }
            builder.delete(builder.length() - 2, builder.length());
            labels.add(builder.toString());

            Cluster cluster = new Cluster();
            cluster.setPoints(points);
            clusters.add(cluster);
        }

        ClusteringPartitional instance = new ClusteringPartitional();
        instance.comparison = comparison;
        instance.labels = labels;
        instance.medoids = medoids;
        instance.clusters = clusters;
        instance.scoringFunction = scoringFunction.toString();
        instance.colors = Colors.toRGB();
        return instance;
    }

    ComparisonGlobal comparison;
    List<Cluster> clusters;
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visualizeHighQuality() {
        URL resource = getClass().getResource(
                "/pl/poznan/put/cs/bioserver/external/MatplotlibPartitional.xsl");
        Matplotlib.runXsltAndPython(resource, this);
    }
}
