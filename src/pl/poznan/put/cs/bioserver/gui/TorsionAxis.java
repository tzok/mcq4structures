package pl.poznan.put.cs.bioserver.gui;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.biojava.bio.structure.ResidueNumber;
import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTick;
import org.jfree.chart.axis.TickType;
import org.jfree.ui.RectangleEdge;

import pl.poznan.put.cs.bioserver.torsion.AngleDifference;

public class TorsionAxis extends NumberAxis {
    private static final long serialVersionUID = 1L;
    private Map<String, List<AngleDifference>> comparison;

    public TorsionAxis(Map<String, List<AngleDifference>> comparison) {
        this.comparison = comparison;
        setTickLabelFont(new Font(Font.DIALOG, Font.PLAIN, 8));
    }

    @Override
    public List refreshTicks(Graphics2D g2, AxisState state,
            Rectangle2D dataArea, RectangleEdge edge) {
        List<NumberTick> ticks = super.refreshTicks(g2, state, dataArea, edge);

        Map<Double, String> mapIndexLabel = new HashMap<>();
        List<AngleDifference> list = comparison.get("AVERAGE");
        for (int i = 0; i < list.size(); i++) {
            AngleDifference ad = list.get(i);
            ResidueNumber residue = ad.getResidue();
            mapIndexLabel.put(
                    (double) i,
                    String.format("%s:%03d", residue.getChainId(),
                            residue.getSeqNum()));
        }

        List<NumberTick> result = new ArrayList<>();
        for (NumberTick nt : ticks) {
            result.add(new NumberTick(TickType.MINOR, nt.getValue(),
                    mapIndexLabel.get(nt.getValue()), nt.getTextAnchor(), nt
                            .getRotationAnchor(), Math.PI / 4));
        }
        return result;
    }
}
