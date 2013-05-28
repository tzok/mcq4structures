package pl.poznan.put.cs.bioserver.beans.auxiliary;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import pl.poznan.put.cs.bioserver.beans.XMLSerializable;

@XmlRootElement
public class Angle extends XMLSerializable {
    private static final long serialVersionUID = 3617330297291250022L;

    String name;
    double[] deltas;

    public String getName() {
        return name;
    }

    @XmlAttribute
    public void setName(String name) {
        this.name = name;
    }

    public double[] getDeltas() {
        return deltas;
    }

    @XmlElement(name="item")
    public void setDeltas(double[] deltas) {
        this.deltas = deltas;
    }
}
