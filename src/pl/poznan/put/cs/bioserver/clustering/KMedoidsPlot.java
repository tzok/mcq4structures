package pl.poznan.put.cs.bioserver.clustering;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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

import pl.poznan.put.cs.bioserver.visualisation.MDS;

/**
 * Plot of k-medoids clustering.
 */
public class KMedoidsPlot extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(KMedoidsPlot.class);

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
    public KMedoidsPlot(double[][] distance, String[] labels, int k, String method) {
        Map<Integer, Set<Integer>> medoids = null;
        if (method.equals("PAM")) {
            if (k == 0) {
                medoids = Clusterer.clusterPAM(distance).getClusterAssignment();
            } else {
                medoids = Clusterer.clusterPAM(distance, k).getClusterAssignment();
            }
        } else if (method.equals("PAMSIL")) {
            if (k == 0) {
                medoids = Clusterer.clusterPAMSIL(distance).getClusterAssignment();
            } else {
                medoids = Clusterer.clusterPAMSIL(distance, k).getClusterAssignment();
            }
        } else {
            throw new UnsupportedOperationException("Clustering method " + method
                    + " not supported");
        }

        double[][] mds = MDS.multidimensionalScaling(distance, 2);

        StringBuilder dumper = new StringBuilder();
        DefaultXYDataset dataset = new DefaultXYDataset();
        for (Entry<Integer, Set<Integer>> entry : medoids.entrySet()) {
            StringBuilder builder = new StringBuilder();
            builder.append("[ ");

            Set<Integer> value = entry.getValue();
            double[] x = new double[value.size()];
            double[] y = new double[value.size()];
            int i = 0;
            for (int index : value) {
                builder.append(labels[index]);
                builder.append(", ");
                x[i] = mds[index][0];
                y[i] = mds[index][1];
                if (KMedoidsPlot.LOGGER.isTraceEnabled()) {
                    dumper.append(labels[index]);
                    dumper.append(' ');
                    dumper.append(x[i]);
                    dumper.append(' ');
                    dumper.append(y[i]);
                    dumper.append(' ');
                    dumper.append(entry.getKey());
                    dumper.append('\n');
                }
                i++;
            }
            builder.append(" ]");
            dataset.addSeries(builder.toString(), new double[][] { x, y });
        }
        KMedoidsPlot.LOGGER.trace(dumper.toString());

        NumberAxis xAxis = new NumberAxis();
        xAxis.setTickLabelsVisible(false);
        xAxis.setTickMarksVisible(false);
        NumberAxis yAxis = new NumberAxis();
        yAxis.setTickLabelsVisible(false);
        yAxis.setTickMarksVisible(false);
        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, new XYShapeRenderer());
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinesVisible(false);
        JFreeChart chart = new JFreeChart(plot);
        ChartPanel panel = new ChartPanel(chart);
        setContentPane(panel);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension size = toolkit.getScreenSize();
        setSize(size.width / 2, size.height / 2);
        setLocation(size.width / 4, size.height / 4);
    }
}
