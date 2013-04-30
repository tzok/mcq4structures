package pl.poznan.put.cs.bioserver.beans;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Point extends XMLSerializable {
    private static final long serialVersionUID = 4083165745852207116L;

    double x;
    double y;

    public double getX() {
        return x;
    }

    @XmlElement
    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    @XmlElement
    public void setY(double y) {
        this.y = y;
    }
}
