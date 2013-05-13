package pl.poznan.put.cs.bioserver.beans;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.biojava.bio.structure.ResidueNumber;

import pl.poznan.put.cs.bioserver.helper.Colors;
import pl.poznan.put.cs.bioserver.torsion.AngleDifference;

@XmlRootElement
public class LocalComparisonResults extends XMLSerializable {
    private static final long serialVersionUID = 4652567875810044094L;

    Angle[] angles;
    String[] ticks;
    RGB[] colors;

    public static LocalComparisonResults newInstance(
            Map<String, List<AngleDifference>> comparison) {
        Set<ResidueNumber> setResidues = new TreeSet<>();
        Angle[] angles = new Angle[comparison.size()];
        int j = 0;
        for (Entry<String, List<AngleDifference>> entry : comparison.entrySet()) {
            List<AngleDifference> list = entry.getValue();
            double[] deltas = new double[list.size()];
            for (int i = 0; i < list.size(); i++) {
                AngleDifference delta = list.get(i);
                deltas[i] = delta.getDifference();
                setResidues.add(delta.getResidue());
            }

            angles[j] = new Angle();
            angles[j].setName(entry.getKey());
            angles[j].setDeltas(deltas);
            j++;
        }

        int rowCount = setResidues.size();
        String[] ticks = new String[rowCount];
        int i = 0;
        for (ResidueNumber residue : setResidues) {
            ticks[i] = String.format("%s:%03d", residue.getChainId(),
                    residue.getSeqNum());
            i++;
        }

        LocalComparisonResults results = new LocalComparisonResults();
        results.setAngles(angles);
        results.setTicks(ticks);
        results.colors = Colors.toRGB();
        return results;
    }

    public Angle[] getAngles() {
        return angles;
    }

    @XmlElementWrapper(name = "deltas")
    @XmlElement(name = "angle")
    public void setAngles(Angle[] angles) {
        this.angles = angles;
    }

    public String[] getTicks() {
        return ticks;
    }

    @XmlElementWrapper(name = "ticks")
    @XmlElement(name = "item")
    public void setTicks(String[] ticks) {
        this.ticks = ticks;
    }

    public RGB[] getColors() {
        return colors;
    }

    @XmlElementWrapper(name = "colors")
    @XmlElement(name = "item")
    public void setColors(RGB[] colors) {
        this.colors = colors;
    }
}
