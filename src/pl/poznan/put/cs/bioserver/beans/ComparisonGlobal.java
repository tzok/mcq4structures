package pl.poznan.put.cs.bioserver.beans;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JOptionPane;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.csvreader.CsvWriter;

import pl.poznan.put.cs.bioserver.gui.DialogCluster;
import pl.poznan.put.cs.bioserver.helper.Clusterable;
import pl.poznan.put.cs.bioserver.helper.Exportable;
import pl.poznan.put.cs.bioserver.helper.Helper;
import pl.poznan.put.cs.bioserver.helper.Visualizable;
import pl.poznan.put.cs.bioserver.visualisation.MDS;
import pl.poznan.put.cs.bioserver.visualisation.MDSPlot;

@XmlRootElement
public class ComparisonGlobal extends XMLSerializable implements Clusterable,
        Exportable, Visualizable {
    private static final long serialVersionUID = 5900586846338327108L;

    double[][] distanceMatrix;
    String[] labels;
    String method;

    public static ComparisonGlobal newInstance(double[][] distanceMatrix,
            String[] labels, String method) {
        ComparisonGlobal instance = new ComparisonGlobal();
        instance.setDistanceMatrix(distanceMatrix);
        instance.setLabels(labels);
        instance.setMethod(method);
        return instance;
    }

    public double[][] getDistanceMatrix() {
        return distanceMatrix;
    }

    @XmlElementWrapper(name = "distanceMatrix")
    @XmlElement(name = "row")
    public void setDistanceMatrix(double[][] distanceMatrix) {
        this.distanceMatrix = distanceMatrix;
    }

    public String[] getLabels() {
        return labels;
    }

    @XmlElementWrapper(name = "labels")
    @XmlElement(name = "item")
    public void setLabels(String[] labels) {
        this.labels = labels;
    }

    public String getMethod() {
        return method;
    }

    @XmlElement
    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public void visualize() {
        for (double[] value : distanceMatrix) {
            for (double element : value) {
                if (Double.isNaN(element)) {
                    JOptionPane.showMessageDialog(null, "Results cannot be "
                            + "visualized. Some structures could not be "
                            + "compared.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        }

        double[][] mds = MDS.multidimensionalScaling(distanceMatrix, 2);
        if (mds == null) {
            JOptionPane.showMessageDialog(null, "Cannot visualise specified "
                    + "structures in 2D space", "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        MDSPlot plot = new MDSPlot(mds, labels);
        plot.setTitle("MCQ4Structures: global distance diagram (" + method
                + ")");
        plot.setVisible(true);
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
                csvWriter.write(labels[i]);
                for (int j = 0; j < distanceMatrix[i].length; j++) {
                    csvWriter.write(Double.toString(distanceMatrix[i][j]));
                }
                csvWriter.endRecord();
            }
        }
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
    public void cluster() {
        for (double[] value : distanceMatrix) {
            for (double element : value) {
                if (Double.isNaN(element)) {
                    JOptionPane.showMessageDialog(null, "Results cannot be "
                            + "clustered. Some structures could not be "
                            + "compared.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        }

        DialogCluster dialogClustering = new DialogCluster(this,
                "MCQ4Structures: global distance (" + method + ") clusters by ");
        dialogClustering.setVisible(true);
    }
}
