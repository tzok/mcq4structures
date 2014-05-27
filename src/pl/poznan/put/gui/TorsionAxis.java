package pl.poznan.put.gui;

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
    private static final long serialVersionUID = 1L;
    private String[] ticksNames;

    public TorsionAxis(String[] ticks) {
        super();
        ticksNames = ticks;
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

        List<NumberTick> result = new ArrayList<>();
        for (int i = 0; i < visibleIntegerTicks.size(); i++) {
            NumberTick nt = visibleIntegerTicks.get(i);
            int index = (int) nt.getValue();
            if (index < ticksNames.length) {
                result.add(new NumberTick(index, ticksNames[index],
                        nt.getTextAnchor(), nt.getRotationAnchor(), Math.PI / 4));
            }
        }
        return result;
    }
}
