package pl.poznan.put.visualisation;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTick;
import org.jfree.ui.RectangleEdge;

public class TorsionAxis extends NumberAxis {
    private final List<String> ticksNames;
    private final double ticksRotation;

    public TorsionAxis(List<String> ticksNames, double ticksRotation,
            int fontSize) {
        super();
        this.ticksNames = ticksNames;
        this.ticksRotation = ticksRotation;
        setTickLabelFont(new Font(Font.MONOSPACED, Font.PLAIN, fontSize));
    }

    @Override
    public List<NumberTick> refreshTicks(Graphics2D g2, AxisState state,
            Rectangle2D dataArea, RectangleEdge edge) {
        List<NumberTick> ticks = super.refreshTicks(g2, state, dataArea, edge);

        List<NumberTick> visibleIntegerTicks = new ArrayList<>();
        for (NumberTick t : ticks) {
            double value = t.getValue();
            if (value == Math.floor(value)) {
                visibleIntegerTicks.add(t);
            }
        }

        List<NumberTick> result = new ArrayList<>();
        for (int i = 0; i < visibleIntegerTicks.size(); i++) {
            NumberTick nt = visibleIntegerTicks.get(i);
            int index = (int) nt.getValue();
            if (index < ticksNames.size()) {
                result.add(new NumberTick(index, ticksNames.get(index), nt.getTextAnchor(), nt.getRotationAnchor(), ticksRotation));
            }
        }
        return result;
    }
}
