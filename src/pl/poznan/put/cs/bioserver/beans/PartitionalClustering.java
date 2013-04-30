package pl.poznan.put.cs.bioserver.beans;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import pl.poznan.put.cs.bioserver.clustering.Clusterer.Result;
import pl.poznan.put.cs.bioserver.visualisation.MDS;

@XmlRootElement
public class PartitionalClustering extends XMLSerializable {
    private static final long serialVersionUID = -7474446942015119359L;

    GlobalComparisonResults comparison;
    Point[][] points;
    Point[] medoids;
    String[] labels;

    public static PartitionalClustering newInstance(
            GlobalComparisonResults comparison, Result clustering) {
        double[][] mds = MDS.multidimensionalScaling(
                comparison.getDistanceMatrix(), 2);
        Map<Integer, Set<Integer>> clusters = clustering.getClusterAssignment();

        Point[] medoids = new Point[clusters.size()];
        int i = 0;
        for (int index : clusters.keySet()) {
            medoids[i] = new Point();
            medoids[i].x = mds[index][0];
            medoids[i].y = mds[index][1];
            i++;
        }

        Point[][] points = new Point[clusters.size()][];
        String[] labels = new String[clusters.size()];
        i = 0;
        for (Entry<Integer, Set<Integer>> entry : clusters.entrySet()) {
            points[i] = new Point[entry.getValue().size()];
            StringBuilder builder = new StringBuilder();

            int j = 0;
            for (int index : entry.getValue()) {
                String[] labelsAll = comparison.getLabels();
                builder.append(labelsAll[index]);
                builder.append(", ");

                points[i][j] = new Point();
                points[i][j].x = mds[index][0];
                points[i][j].y = mds[index][1];
                j++;
            }
            builder.delete(builder.length() - 2, builder.length());
            labels[i] = builder.toString();
            i++;
        }

        PartitionalClustering instance = new PartitionalClustering();
        instance.comparison = comparison;
        instance.labels = labels;
        instance.medoids = medoids;
        instance.points = points;
        return instance;
    }

    public GlobalComparisonResults getComparison() {
        return comparison;
    }

    @XmlElement
    public void setComparison(GlobalComparisonResults comparison) {
        this.comparison = comparison;
    }

    public Point[][] getPoints() {
        return points;
    }

    @XmlElementWrapper(name = "cluster")
    @XmlElement(name = "points")
    public void setPoints(Point[][] points) {
        this.points = points;
    }

    public Point[] getMedoids() {
        return medoids;
    }

    @XmlElementWrapper(name = "medoids")
    public void setMedoids(Point[] medoids) {
        this.medoids = medoids;
    }

    public String[] getLabels() {
        return labels;
    }

    @XmlElementWrapper(name = "labels")
    @XmlElement(name = "item")
    public void setLabels(String[] labels) {
        this.labels = labels;
    }
}
