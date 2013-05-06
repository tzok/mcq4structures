package pl.poznan.put.cs.bioserver.beans;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class GlobalComparisonResults extends XMLSerializable {
    private static final long serialVersionUID = 5900586846338327108L;

    double[][] distanceMatrix;
    String[] labels;

    public static GlobalComparisonResults newInstance(
            double[][] distanceMatrix, String[] labels) {
        GlobalComparisonResults instance = new GlobalComparisonResults();
        instance.setDistanceMatrix(distanceMatrix);
        instance.setLabels(labels);
        return instance;
    }

    public double[][] getDistanceMatrix() {
        return distanceMatrix;
    }

    @XmlElementWrapper(name="distanceMatrix")
    @XmlElement(name="row")
    public void setDistanceMatrix(double[][] distanceMatrix) {
        this.distanceMatrix = distanceMatrix;
    }

    public String[] getLabels() {
        return labels;
    }

    @XmlElementWrapper(name="labels")
    @XmlElement(name="item")
    public void setLabels(String[] labels) {
        this.labels = labels;
    }
}
