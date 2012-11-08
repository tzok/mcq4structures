package pl.poznan.put.cs.bioserver.clustering;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.StringWriter;
import java.util.HashSet;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYShapeRenderer;
import org.jfree.data.xy.DefaultXYDataset;

import pl.poznan.put.cs.bioserver.visualisation.MDS;

/**
 * Plot of k-medoids clustering.
 */
public class KMedoidsPlot extends JFrame {
    private static final long serialVersionUID = 1L;
    private JFreeChart chart;

    /**
     * Create an instance of JFrame which shows a k-medoid plot.
     * 
     * @param distance
     *            A distance matrix, NxN.
     * @param labels
     *            Labels for every entry, N.
     * @param k
     *            Chosen k for k-medoids method.
     */
    public KMedoidsPlot(double[][] distance, String[] labels, int k) {
        int[] medoids = Clusterer.kMedoids(distance, k);
        HashSet<Integer> medoidSet = new HashSet<>();
        for (int m : medoids) {
            medoidSet.add(m);
        }

        double[][] mds = MDS.multidimensionalScaling(distance, 2);

        DefaultXYDataset dataset = new DefaultXYDataset();
        for (int currentMedoid : medoidSet) {
            int count = 0;
            for (int medoid : medoids) {
                if (medoid == currentMedoid) {
                    count++;
                }
            }
            double[] x = new double[count];
            double[] y = new double[count];
            StringWriter writer = new StringWriter();
            writer.append("[ ");
            int j = 0;
            for (int i = 0; i < medoids.length; ++i) {
                if (medoids[i] == currentMedoid) {
                    writer.append(labels[i]);
                    writer.append(", ");
                    x[j] = mds[i][0];
                    y[j] = mds[i][1];
                    j++;
                }
            }
            writer.append(" ]");
            dataset.addSeries(writer.toString(), new double[][] { x, y });
        }

        NumberAxis xAxis = new NumberAxis();
        xAxis.setTickLabelsVisible(false);
        xAxis.setTickMarksVisible(false);
        NumberAxis yAxis = new NumberAxis();
        yAxis.setTickLabelsVisible(false);
        yAxis.setTickMarksVisible(false);
        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, new XYShapeRenderer());
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinesVisible(false);
        chart = new JFreeChart(plot);
        ChartPanel panel = new ChartPanel(chart);
        setContentPane(panel);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension size = toolkit.getScreenSize();
        setSize(size.width / 2, size.height / 2);
        setLocation(size.width / 4, size.height / 4);
    }
}
