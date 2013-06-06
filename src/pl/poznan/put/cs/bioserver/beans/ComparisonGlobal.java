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
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.Sphere;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import org.jzy3d.plot3d.rendering.scene.Graph;

import pl.poznan.put.cs.bioserver.gui.DialogCluster;
import pl.poznan.put.cs.bioserver.helper.Clusterable;
import pl.poznan.put.cs.bioserver.helper.Exportable;
import pl.poznan.put.cs.bioserver.helper.Helper;
import pl.poznan.put.cs.bioserver.helper.Visualizable;
import pl.poznan.put.cs.bioserver.visualisation.MDS;
import pl.poznan.put.cs.bioserver.visualisation.MDSPlot;

import com.csvreader.CsvWriter;
import com.sun.media.sound.InvalidDataException;

@XmlRootElement
public class ComparisonGlobal extends XMLSerializable implements Clusterable, Exportable,
        Visualizable {
    private static final long serialVersionUID = 5900586846338327108L;

    public static ComparisonGlobal newInstance(double[][] distanceMatrix, List<String> labels,
            String method) {
        ComparisonGlobal instance = new ComparisonGlobal();
        instance.setDistanceMatrix(distanceMatrix);
        instance.setLabels(labels);
        instance.setMethod(method);
        return instance;
    }

    double[][] distanceMatrix;
    List<String> labels;
    String method;

    @Override
    public void cluster() {
        for (double[] value : distanceMatrix) {
            for (double element : value) {
                if (Double.isNaN(element)) {
                    JOptionPane.showMessageDialog(null, "Results cannot be clustered. Some "
                            + "structures could not be compared.", "Error",
                            JOptionPane.ERROR_MESSAGE);
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
        return distanceMatrix;
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
        this.distanceMatrix = distanceMatrix;
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
                    JOptionPane.showMessageDialog(null, "Results cannot be visualized. Some "
                            + "structures could not be compared.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        }

        double[][] mds;
        try {
            mds = MDS.multidimensionalScaling(distanceMatrix, 2);
        } catch (InvalidDataException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        MDSPlot plot = new MDSPlot(mds, labels);
        plot.setTitle("MCQ4Structures: global distance diagram (" + method + ")");
        plot.setVisible(true);
    }

    @Override
    public void visualize3D() {
        double[][] mds;
        try {
            mds = MDS.multidimensionalScaling(distanceMatrix, 3);
        } catch (InvalidDataException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for (double[] point : mds) {
            for (double coord : point) {
                if (coord < min) {
                    min = coord;
                }
                if (coord > max) {
                    max = coord;
                }
            }
        }

        Chart chart = new Chart(Quality.Advanced);
        Graph graph = chart.getScene().getGraph();
        for (double[] point : mds) {
            Sphere sphere = new Sphere(new Coord3d(point[0], point[1], point[2]),
                    (float) ((max - min) / 10.0), 10, Color.random());
            graph.add(sphere);
        }
        ChartLauncher.openChart(chart);
    }

    @Override
    public void visualizeHighQuality() {
        throw new UnsupportedOperationException("Method not implemented!");
    }
}
