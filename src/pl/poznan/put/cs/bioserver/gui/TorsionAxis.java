package pl.poznan.put.cs.bioserver.gui;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.biojava.bio.structure.ResidueNumber;
import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTick;
import org.jfree.ui.RectangleEdge;

import pl.poznan.put.cs.bioserver.torsion.AngleDifference;

class TorsionAxis extends NumberAxis {
    private static final long serialVersionUID = 1L;
    private Map<String, List<AngleDifference>> comparisonResults;

    TorsionAxis(Map<String, List<AngleDifference>> comparison) {
        super();
        comparisonResults = comparison;
        setTickLabelFont(new Font(Font.DIALOG, Font.PLAIN, 8));
    }

    @Override
    public List refreshTicks(Graphics2D g2, AxisState state,
            Rectangle2D dataArea, RectangleEdge edge) {
        List<NumberTick> ticks = super.refreshTicks(g2, state, dataArea, edge);

        List<NumberTick> visibleIntegerTicks = new ArrayList<>();
        for (NumberTick t : ticks) {
            double value = t.getValue();
            if (value == Math.floor(value)) {
                visibleIntegerTicks.add(t);
            }
        }

        List<AngleDifference> list = comparisonResults.get("AVERAGE");
        int size = list.size();
        String[] labels = new String[size];
        for (int i = 0; i < size; i++) {
            ResidueNumber residue = list.get(i).getResidue();
            labels[i] = String.format("%s:%03d", residue.getChainId(),
                    residue.getSeqNum());
        }
        Arrays.sort(labels);

        List<NumberTick> result = new ArrayList<>();
        for (int i = 0; i < visibleIntegerTicks.size(); i++) {
            NumberTick nt = visibleIntegerTicks.get(i);
            int index = (int) nt.getValue();
            if (index < labels.length) {
                result.add(new NumberTick(index, labels[index], nt
                        .getTextAnchor(), nt.getRotationAnchor(), Math.PI / 4));
            }
        }
        return result;
    }
}
