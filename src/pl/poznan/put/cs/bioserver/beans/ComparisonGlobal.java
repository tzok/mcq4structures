package pl.poznan.put.cs.bioserver.beans;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.swing.JOptionPane;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.jzy3d.chart.Chart;
import org.jzy3d.chart.ChartLauncher;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapRainbow;
import org.jzy3d.maths.Range;
import org.jzy3d.plot3d.builder.Builder;
import org.jzy3d.plot3d.builder.Mapper;
import org.jzy3d.plot3d.builder.concrete.OrthonormalGrid;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.primitives.axes.layout.IAxeLayout;
import org.jzy3d.plot3d.primitives.axes.layout.providers.RegularTickProvider;
import org.jzy3d.plot3d.primitives.axes.layout.renderers.TickLabelMap;
import org.jzy3d.plot3d.rendering.canvas.Quality;

import pl.poznan.put.cs.bioserver.gui.DialogCluster;
import pl.poznan.put.cs.bioserver.helper.Clusterable;
import pl.poznan.put.cs.bioserver.helper.Exportable;
import pl.poznan.put.cs.bioserver.helper.Helper;
import pl.poznan.put.cs.bioserver.helper.InvalidInputException;
import pl.poznan.put.cs.bioserver.helper.Visualizable;
import pl.poznan.put.cs.bioserver.visualisation.MDS;
import pl.poznan.put.cs.bioserver.visualisation.MDSPlot;

import com.csvreader.CsvWriter;

@XmlRootElement
public class ComparisonGlobal extends XMLSerializable implements Clusterable,
        Exportable, Visualizable {
    private static final long serialVersionUID = 5900586846338327108L;

    public static ComparisonGlobal newInstance(double[][] distanceMatrix,
            List<String> labels, String method) {
        ComparisonGlobal instance = new ComparisonGlobal();
        instance.setDistanceMatrix(distanceMatrix);
        instance.setLabels(labels);
        instance.setMethod(method);
        return instance;
    }

    private double[][] distanceMatrix;
    private List<String> labels;
    private String method;

    @Override
    public void cluster() {
        for (double[] value : distanceMatrix) {
            for (double element : value) {
                if (Double.isNaN(element)) {
                    JOptionPane.showMessageDialog(null,
                            "Results cannot be clustered. Some "
                                    + "structures could not be compared.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        }

        DialogCluster dialogClustering = new DialogCluster(this);
        dialogClustering.setVisible(true);
    }

    @Override
    public void export(File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
            CsvWriter csvWriter = new CsvWriter(writer, '\t');
            csvWriter.write("Global " + method);
            for (String name : labels) {
                csvWriter.write(name);
            }
            csvWriter.endRecord();

            for (int i = 0; i < distanceMatrix.length; i++) {
                csvWriter.write(labels.get(i));
                for (int j = 0; j < distanceMatrix[i].length; j++) {
                    csvWriter.write(Double.toString(distanceMatrix[i][j]));
                }
                csvWriter.endRecord();
            }
        }
    }

    public double[][] getDistanceMatrix() {
        return distanceMatrix.clone();
    }

    public List<String> getLabels() {
        return labels;
    }

    public String getMethod() {
        return method;
    }

    @XmlElementWrapper(name = "distanceMatrix")
    @XmlElement(name = "row")
    public void setDistanceMatrix(double[][] distanceMatrix) {
        this.distanceMatrix = distanceMatrix.clone();
    }

    @XmlElementWrapper(name = "labels")
    @XmlElement(name = "item")
    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    @XmlElement
    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public File suggestName() {
        String filename = Helper.getExportPrefix();
        filename += "-Global-";
        filename += method;
        filename += ".csv";
        return new File(filename);
    }

    @Override
    public void visualize() {
        for (double[] value : distanceMatrix) {
            for (double element : value) {
                if (Double.isNaN(element)) {
                    JOptionPane.showMessageDialog(null,
                            "Results cannot be visualized. Some "
                                    + "structures could not be compared.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        }

        double[][] mds;
        try {
            mds = MDS.multidimensionalScaling(distanceMatrix, 2);
        } catch (InvalidInputException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        MDSPlot plot = new MDSPlot(mds, labels);
        plot.setTitle("MCQ4Structures: global distance diagram (" + method
                + ")");
        plot.setVisible(true);
    }

    @Override
    public void visualize3D() {
        final int max = distanceMatrix.length;

        Shape surface =
                Builder.buildOrthonormal(new OrthonormalGrid(new Range(0,
                        max - 1), max), new Mapper() {
                    @Override
                    public double f(double x, double y) {
                        int i = (int) Math.round(x);
                        int j = (int) Math.round(y);

                        i = Math.max(Math.min(i, max - 1), 0);
                        j = Math.max(Math.min(j, max - 1), 0);
                        return distanceMatrix[i][j];
                    }
                });

        surface.setColorMapper(new ColorMapper(new ColorMapRainbow(), surface
                .getBounds().getZmin(), surface.getBounds().getZmax(),
                new Color(1, 1, 1, .5f)));
        surface.setFaceDisplayed(true);
        surface.setWireframeDisplayed(false);

        Chart chart = new Chart(Quality.Nicest);
        chart.getScene().getGraph().add(surface);

        TickLabelMap map = new TickLabelMap();
        for (int i = 0; i < labels.size(); i++) {
            map.register(i, labels.get(i));
        }

        IAxeLayout axeLayout = chart.getAxeLayout();
        axeLayout.setXTickProvider(new RegularTickProvider(labels.size()));
        axeLayout.setXTickRenderer(map);
        axeLayout.setYTickProvider(new RegularTickProvider(labels.size()));
        axeLayout.setYTickRenderer(map);
        axeLayout.setZAxeLabel(method.equals("MCQ") ? "Angular distance"
                : "Distance [\u212B]");

        ChartLauncher.openChart(chart);
    }

    @Override
    public void visualizeHighQuality() {
        throw new UnsupportedOperationException("Method not implemented!");
    }
}
