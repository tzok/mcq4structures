package pl.poznan.put.cs.bioserver.beans.auxiliary;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import pl.poznan.put.cs.bioserver.beans.XMLSerializable;

@XmlRootElement
public class Cluster extends XMLSerializable {
    private static final long serialVersionUID = 4584994465498507561L;

    private List<Point> points;

    public List<Point> getPoints() {
        return points;
    }

    @XmlElement
    public void setPoints(List<Point> points) {
        this.points = points;
    }
}
