
package pl.poznan.put.cs.bioserver.clustering;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.DefaultXYDataset;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.StringWriter;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

/**
 * Plot of dendrogram representing hierarchical clustering.
 */
public class HierarchicalPlot extends JFrame {
    private static final long serialVersionUID = 1L;

    private static String generateLabel(Vector<Vector<Integer>> ids,
            int[] pair, String[] labels) {
        Vector<Integer> a = ids.get(pair[0]);
        Vector<Integer> b = ids.get(pair[1]);

        a.addAll(b);
        ids.remove(pair[1]);

        StringWriter writer = new StringWriter();
        writer.append("[ ");
        for (int i : a) {
            writer.append(labels[i]);
            writer.append(", ");
        }
        writer.append(" ]");

        return writer.toString();
    }

    public JFreeChart chart;

    public HierarchicalPlot(double[][] distance, String[] labels, int linkage) {
        int[][] clustering = Clusterer
                .hierarchicalClustering(distance, linkage);
        Vector<Integer> allocation = Clusterer.sClusters.get(0);

        Vector<double[]> clusters = new Vector<double[]>();
        Vector<Vector<Integer>> ids = new Vector<Vector<Integer>>();
        for (int i = 0; i < distance.length; ++i) {
            clusters.add(new double[] {
                    allocation.indexOf(i), 0
            });
            Vector<Integer> vector = new Vector<Integer>();
            vector.add(i);
            ids.add(vector);
        }

        DefaultXYDataset dataset = new DefaultXYDataset();
        double y = 0.0;
        for (int[] mergedPair : clustering) {
            double[] a = clusters.get(mergedPair[0]);
            double[] b = clusters.get(mergedPair[1]);
            y = Math.max(a[1], b[1]) + mergedPair[2];

            String label = HierarchicalPlot.generateLabel(ids, mergedPair,
                    labels);
            double[][] points = new double[][] {
                    {
                            a[0], a[0], b[0], b[0]
                    },
                    {
                            a[1], y, y, b[1]
                    }
            };
            dataset.addSeries(label, points);

            a[0] = (a[0] + b[0]) / 2.0;
            a[1] = y;
            clusters.remove(mergedPair[1]);

            // y += 1.0;
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
