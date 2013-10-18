package pl.poznan.put.cs.bioserver.clustering;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYShapeRenderer;
import org.jfree.data.xy.DefaultXYDataset;

import pl.poznan.put.cs.bioserver.beans.ClusteringPartitional;
import pl.poznan.put.cs.bioserver.beans.auxiliary.Cluster;
import pl.poznan.put.cs.bioserver.beans.auxiliary.Point;

/**
 * Plot of k-medoids clustering.
 */
public class KMedoidsPlot extends JFrame {
    private static final long serialVersionUID = 1L;

    public KMedoidsPlot(ClusteringPartitional clustering) {
        DefaultXYDataset dataset = new DefaultXYDataset();
        for (Cluster cluster : clustering.getClusters()) {
            StringBuilder builder = new StringBuilder();
            builder.append("[ ");

            int size = cluster.getPoints().size();
            double[] x = new double[size];
            double[] y = new double[size];
            int i = 0;
            for (Point point : cluster.getPoints()) {
                builder.append(point.getLabel());
                builder.append(", ");
                x[i] = point.getX();
                y[i] = point.getY();
                i++;
            }
            builder.append(" ]");
            dataset.addSeries(builder.toString(), new double[][] { x, y });
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
        JFreeChart chart = new JFreeChart(plot);
        ChartPanel panel = new ChartPanel(chart);
        setContentPane(panel);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension size = toolkit.getScreenSize();
        setSize(size.width / 2, size.height / 2);
        setLocation(size.width / 4, size.height / 4);

        StringBuilder builder = new StringBuilder();
        builder.append("MCQ4Structures: global distance (");
        builder.append(clustering.getComparison().getMethodName());
        builder.append(") clusters by k-medoids (");
        builder.append(clustering.getScoringFunction());
        builder.append(", k = ");
        builder.append(clustering.getMedoids().size());
        builder.append(')');
        setTitle(builder.toString());
    }
}
