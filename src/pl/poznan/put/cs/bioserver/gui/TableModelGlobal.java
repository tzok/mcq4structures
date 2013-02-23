package pl.poznan.put.cs.bioserver.gui;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

import pl.poznan.put.cs.bioserver.comparison.GlobalComparison;
import pl.poznan.put.cs.bioserver.helper.Clusterable;
import pl.poznan.put.cs.bioserver.helper.Exportable;
import pl.poznan.put.cs.bioserver.helper.Helper;
import pl.poznan.put.cs.bioserver.helper.Visualizable;
import pl.poznan.put.cs.bioserver.visualisation.MDS;
import pl.poznan.put.cs.bioserver.visualisation.MDSPlot;

import com.csvreader.CsvWriter;

public class TableModelGlobal extends AbstractTableModel implements
        Visualizable, Clusterable, Exportable {
    private static final long serialVersionUID = 1L;
    private String[] names;
    private double[][] values;
    private String measure;

    public TableModelGlobal(String[] names, double[][] values,
            GlobalComparison measure) {
        super();
        this.names = names.clone();
        this.values = values.clone();
        this.measure = measure.toString();
    }

    @Override
    public void cluster() {
        for (double[] value : values) {
            for (double element : value) {
                if (Double.isNaN(element)) {
                    JOptionPane.showMessageDialog(null, "Results cannot be "
                            + "clustered. Some structures could not be "
                            + "compared.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        }

        DialogCluster dialogClustering = new DialogCluster(names, values,
                "MCQ4Structures: global distance (" + measure
                        + ") clusters by ");
        dialogClustering.setVisible(true);
    }

    @Override
    public void export(File file) {
        try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
            CsvWriter csvWriter = new CsvWriter(writer, '\t');
            csvWriter.write("Global " + measure);
            for (String name : names) {
                csvWriter.write(name);
            }
            csvWriter.endRecord();

            for (int i = 0; i < values.length; i++) {
                csvWriter.write(names[i]);
                for (int j = 0; j < values[i].length; j++) {
                    csvWriter.write(Double.toString(values[i][j]));
                }
                csvWriter.endRecord();
            }
        } catch (IOException e) {
            // TODO
            e.printStackTrace();
        }
    }

    @Override
    public int getColumnCount() {
        if (names.length == 0) {
            return 0;
        }
        return names.length + 1;
    }

    @Override
    public String getColumnName(int column) {
        if (column == 0) {
            return "Global " + measure;
        }
        return names[column - 1];
    }

    @Override
    public int getRowCount() {
        return values.length;
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (column == 0) {
            return names[row];
        }
        return values[row][column - 1];
    }

    @Override
    public File suggestName() {
        String filename = Helper.getExportPrefix();
        filename += "-Global-";
        filename += measure;
        filename += ".csv";
        return new File(filename);
    }

    @Override
    public void visualize() {
        for (double[] value : values) {
            for (double element : value) {
                if (Double.isNaN(element)) {
                    JOptionPane.showMessageDialog(null, "Results cannot be "
                            + "visualized. Some structures could not be "
                            + "compared.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        }

        double[][] mds = MDS.multidimensionalScaling(values, 2);
        if (mds == null) {
            JOptionPane.showMessageDialog(null, "Cannot visualise specified "
                    + "structures in 2D space", "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        MDSPlot plot = new MDSPlot(mds, names);
        plot.setTitle("MCQ4Structures: global distance diagram (" + measure
                + ")");
        plot.setVisible(true);
    }
}
