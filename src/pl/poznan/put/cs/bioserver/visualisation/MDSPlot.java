package pl.poznan.put.cs.bioserver.visualisation;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.StringWriter;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYShapeRenderer;
import org.jfree.data.xy.DefaultXYDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Plot of data points calculated from MDS.
 */
public class MDSPlot extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(MDSPlot.class);

    /**
     * Create a JFrame which shows a plot of data after applying
     * Multidimensional Scaling.
     * 
     * @see MDS
     * @param data
     *            A distance matrix, NxN.
     * @param labels
     *            Labels for data, N.
     */
    public MDSPlot(double[][] data, String[] labels) {
        super();

        /*
         * sanity check
         */
        if (data.length != labels.length) {
            throw new IllegalArgumentException(
                    "Data row count and number of labels don't match!");
        }
        for (double[] element : data) {
            if (element.length != 2) {
                throw new IllegalArgumentException(
                        "Data must have dimensions 'n x 2'!");
            }
        }

        /*
         * prepare dataset
         */
        DefaultXYDataset dataset = new DefaultXYDataset();
        StringWriter writer = new StringWriter();
        for (int i = 0; i < data.length; ++i) {
            dataset.addSeries(labels[i], new double[][] { { data[i][0] },
                    { data[i][1] } });
            writer.append(String.format("%f %f %s%n", data[i][0], data[i][1],
                    labels[i]));
        }
        MDSPlot.LOGGER.trace("Data to plot in gnuplot:\n" + writer.toString());

        /*
         * plot it
         */
        NumberAxis xAxis = new NumberAxis();
        xAxis.setTickLabelsVisible(false);
        xAxis.setTickMarksVisible(false);
        NumberAxis yAxis = new NumberAxis();
        yAxis.setTickLabelsVisible(false);
        yAxis.setTickMarksVisible(false);
        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, new XYShapeRenderer());
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinesVisible(false);
        ChartPanel panel = new ChartPanel(new JFreeChart(plot));
        setContentPane(panel);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension size = toolkit.getScreenSize();
        setSize(size.width / 2, size.height / 2);
        setLocation(size.width / 4, size.height / 4);
    }
}
