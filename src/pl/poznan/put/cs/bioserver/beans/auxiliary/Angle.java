package pl.poznan.put.cs.bioserver.beans.auxiliary;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import pl.poznan.put.cs.bioserver.beans.XMLSerializable;

@XmlRootElement
public class Angle extends XMLSerializable {
    private static final long serialVersionUID = 3617330297291250022L;

    private double[] deltas = new double[0];
    private String name = "";

    public double[] getDeltas() {
        return deltas.clone();
    }

    public String getName() {
        return name;
    }

    @XmlElement
    public void setDeltas(double[] deltas) {
        this.deltas = deltas.clone();
    }

    @XmlAttribute
    public void setName(String name) {
        this.name = name;
    }
}
