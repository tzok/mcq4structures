package pl.poznan.put.cs.bioserver.beans.auxiliary;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import pl.poznan.put.cs.bioserver.beans.XMLSerializable;

@XmlRootElement
public class Point extends XMLSerializable {
    private static final long serialVersionUID = 4083165745852207116L;

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

    @XmlElement
    public void setLabel(String label) {
        this.label = label;
    }

    @XmlElement
    public void setX(double x) {
        this.x = x;
    }

    @XmlElement
    public void setY(double y) {
        this.y = y;
    }
}
