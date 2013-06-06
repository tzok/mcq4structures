package pl.poznan.put.cs.bioserver.beans;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.sun.media.sound.InvalidDataException;

import pl.poznan.put.cs.bioserver.beans.auxiliary.Cluster;
import pl.poznan.put.cs.bioserver.beans.auxiliary.Point;
import pl.poznan.put.cs.bioserver.beans.auxiliary.RGB;
import pl.poznan.put.cs.bioserver.clustering.Clusterer.Result;
import pl.poznan.put.cs.bioserver.helper.Colors;
import pl.poznan.put.cs.bioserver.helper.Visualizable;
import pl.poznan.put.cs.bioserver.visualisation.MDS;

@XmlRootElement
public class ClusteringPartitional extends XMLSerializable implements Visualizable {
    private static final long serialVersionUID = -7474446942015119359L;

    public static ClusteringPartitional newInstance(ComparisonGlobal comparison, Result clustering)
            throws InvalidDataException {
        double[][] mds = MDS.multidimensionalScaling(comparison.getDistanceMatrix(), 2);
        Map<Integer, Set<Integer>> clusterMap = clustering.getClusterAssignment();

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
        instance.colors = Colors.toRGB();
        return instance;
    }

    ComparisonGlobal comparison;
    List<Cluster> clusters;
    List<Point> medoids;
    List<String> labels;
    List<RGB> colors;

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

    @Override
    public void visualize() {
        // TODO Auto-generated method stub
    }

    @Override
    public void visualizeHighQuality() {
        // TODO Auto-generated method stub
    }

    @Override
    public void visualize3D() {
        // TODO Auto-generated method stub
        
    }

}
