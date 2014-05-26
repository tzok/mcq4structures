package pl.poznan.put.gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.DefaultXYDataset;

import pl.poznan.put.clustering.ClustererHierarchical.Cluster;
import pl.poznan.put.comparison.GlobalComparisonResultMatrix;

/**
 * Plot of dendrogram representing hierarchical clustering.
 */
public class HierarchicalPlot extends JFrame {
    private static final long serialVersionUID = 1L;

    private static String generateLabel(List<Integer> items, String[] labels) {
        StringBuilder builder = new StringBuilder();
        builder.append("[ ");
        for (int i : items) {
            builder.append(labels[i]);
            builder.append(", ");
        }
        builder.delete(builder.length() - 2, builder.length());
        builder.append(" ]");
        return builder.toString();
    }

    private JFreeChart chart;

    /**
     * Create a JFrame that shows the result of hierarchical plotting.
     * 
     * @param clustering
     * 
     * @param distance
     *            A distance matrix, NxN.
     * @param structureNames
     *            An array of labels, N.
     * @param linkage
     *            Linkage type @see Clusterer.Type;
     */
    public HierarchicalPlot(GlobalComparisonResultMatrix matrix,
            List<Cluster> clustering) {
        DefaultXYDataset dataset = new DefaultXYDataset();
        String[] labels = matrix.getNames();

        for (Cluster cluster : clustering) {
            String label = HierarchicalPlot.generateLabel(cluster.getItems(),
                    labels);
            Cluster left = cluster.getLeft();
            Cluster right = cluster.getRight();
            double[] x = new double[] { left.getX(), left.getX(), right.getX(), right.getX() };
            double[] y = new double[] { left.getY(), cluster.getY(), cluster.getY(), right.getY() };
            dataset.addSeries(label, new double[][] { x, y });
        }

        NumberAxis xAxis = new NumberAxis();
        xAxis.setTickLabelsVisible(false);
        xAxis.setTickMarksVisible(false);
        xAxis.setAutoRange(false);
        xAxis.setRange(-1, labels.length);
        NumberAxis yAxis = new NumberAxis();
        yAxis.setTickLabelsVisible(false);
        yAxis.setTickMarksVisible(false);
        XYPlot plot = new XYPlot(dataset, xAxis, yAxis,
                new XYLineAndShapeRenderer());
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
