package pl.poznan.put.cs.bioserver.beans.auxiliary;

import javax.xml.bind.annotation.XmlElement;

import pl.poznan.put.cs.bioserver.beans.XMLSerializable;

public class Point3D extends XMLSerializable {
    private static final long serialVersionUID = -5367548449382891738L;

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

    public void setZ(double z) {
        this.z = z;
    }
}
