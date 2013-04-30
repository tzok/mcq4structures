package pl.poznan.put.cs.bioserver.beans;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class LocalComparisonResults extends XMLSerializable {
    private static final long serialVersionUID = 4652567875810044094L;

    Angle[] angles;
    String[] ticks;

    public Angle[] getAngles() {
        return angles;
    }

    @XmlElement
    public void setAngles(Angle[] angles) {
        this.angles = angles;
    }

    public String[] getTicks() {
        return ticks;
    }

    @XmlElement
    public void setTicks(String[] ticks) {
        this.ticks = ticks;
    }
}
