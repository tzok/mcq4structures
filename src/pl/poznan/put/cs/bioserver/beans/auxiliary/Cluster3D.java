package pl.poznan.put.cs.bioserver.beans.auxiliary;

import java.util.List;

import pl.poznan.put.cs.bioserver.beans.XMLSerializable;

import com.sun.xml.internal.txw2.annotation.XmlElement;

public class Cluster3D extends XMLSerializable {
    private static final long serialVersionUID = -3603148262237249360L;

    List<Point3D> points;

    public List<Point3D> getPoints() {
        return points;
    }

    @XmlElement
    public void setPoints(List<Point3D> points) {
        this.points = points;
    }
}
