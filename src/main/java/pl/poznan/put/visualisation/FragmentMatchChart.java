package pl.poznan.put.visualisation;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.DefaultXYDataset;

import pl.poznan.put.constant.Colors;
import pl.poznan.put.matching.FragmentComparison;
import pl.poznan.put.matching.FragmentMatch;
import pl.poznan.put.matching.ResidueComparison;
import pl.poznan.put.torsion.TorsionAngleDelta;
import pl.poznan.put.utility.AngleFormat;

public class FragmentMatchChart {
    public static ChartPanel create(FragmentMatch match) {
        DefaultXYDataset dataset = new DefaultXYDataset();
        XYItemRenderer renderer = new DefaultXYItemRenderer();
        FragmentComparison comparison = match.getFragmentComparison();

        int i = 0;
        for (TorsionAngle angle : comparison.getAngleTypes()) {
            double[][] data = new double[2][];
            data[0] = new double[comparison.size()];
            data[1] = new double[comparison.size()];

            int j = 0;
            for (ResidueComparison residue : comparison) {
                TorsionAngleDelta delta = residue.getAngleDelta(angle);
                data[0][j] = j;

                if (delta.getState() == State.BOTH_VALID) {
                    data[1][j] = delta.getDelta();
                } else {
                    data[1][j] = Double.NaN;
                }

                j++;
            }

            String displayName = angle.getLongDisplayName();
            dataset.addSeries(displayName, data);
            renderer.setSeriesPaint(i, Colors.COLORS[i]);
            i++;
        }

        String[] ticks = match.getResidueLabels();
        ValueAxis domainAxis = new TorsionAxis(ticks);
        domainAxis.setLabel("ResID");

        NumberAxis rangeAxis = new NumberAxis();
        rangeAxis.setLabel("Angular distance");
        rangeAxis.setRange(0, Math.PI);
        rangeAxis.setTickUnit(new NumberTickUnit(Math.PI / 12.0));
        rangeAxis.setNumberFormatOverride(AngleFormat.createInstance());

        Plot plot = new XYPlot(dataset, domainAxis, rangeAxis, renderer);
        JFreeChart chart = new JFreeChart(plot);
        return new ChartPanel(chart);
    }

    private FragmentMatchChart() {
    }
}
