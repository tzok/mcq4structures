package pl.poznan.put.visualisation;

import org.apache.commons.math3.util.Precision;
import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTick;
import org.jfree.chart.ui.RectangleEdge;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class TorsionAxis extends NumberAxis {
    private static final long serialVersionUID = 688243119045973269L;

    private final List<String> ticksNames;
    private final double ticksRotation;

    public TorsionAxis(final List<String> ticksNames,
                       final double ticksRotation, final int fontSize) {
        super();
        this.ticksNames = new ArrayList<>(ticksNames);
        this.ticksRotation = ticksRotation;
        setTickLabelFont(new Font(Font.MONOSPACED, Font.PLAIN, fontSize));
    }

    @Override
    public List<NumberTick> refreshTicks(final Graphics2D g2,
                                         final AxisState state,
                                         final Rectangle2D dataArea,
                                         final RectangleEdge edge) {
        final List<NumberTick> ticks =
                super.refreshTicks(g2, state, dataArea, edge);

        final Collection<NumberTick> visibleIntegerTicks = new ArrayList<>();
        for (final NumberTick t : ticks) {
            final double value = t.getValue();
            if (Precision.equals(value, Math.floor(value))) {
                visibleIntegerTicks.add(t);
            }
        }

        final List<NumberTick> result = new ArrayList<>();
        for (final NumberTick nt : visibleIntegerTicks) {
            final int index = (int) nt.getValue();
            if (index < ticksNames.size()) {
                result.add(new NumberTick(index, ticksNames.get(index),
                                          nt.getTextAnchor(),
                                          nt.getRotationAnchor(),
                                          ticksRotation));
            }
        }
        return result;
    }
}
