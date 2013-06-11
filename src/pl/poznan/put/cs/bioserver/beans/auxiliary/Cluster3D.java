package pl.poznan.put.cs.bioserver.beans.auxiliary;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import pl.poznan.put.cs.bioserver.beans.XMLSerializable;

@XmlRootElement
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
