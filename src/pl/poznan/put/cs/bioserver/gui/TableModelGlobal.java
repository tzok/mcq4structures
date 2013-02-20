package pl.poznan.put.cs.bioserver.gui;

import java.io.File;

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

import pl.poznan.put.cs.bioserver.gui.windows.DialogCluster;
import pl.poznan.put.cs.bioserver.helper.Exportable;
import pl.poznan.put.cs.bioserver.visualisation.MDS;
import pl.poznan.put.cs.bioserver.visualisation.MDSPlot;

public class TableModelGlobal extends AbstractTableModel implements
        Visualizable, Clusterable, Exportable {
    private static final long serialVersionUID = 1L;
    private String[] names;
    private double[][] values;

    public TableModelGlobal(String[] names, double[][] values) {
        super();
        this.names = names.clone();
        this.values = values.clone();
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
            return "";
        }
        return names[column - 1];
    }

    public String[] getNames() {
        return names;
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

    public double[][] getValues() {
        return values;
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
        plot.setVisible(true);
    }

    @Override
    public void cluster() {
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

        DialogCluster dialogClustering = new DialogCluster(names, values);
        dialogClustering.setVisible(true);
    }

    @Override
    public void export(File file) {
        // TODO Auto-generated method stub

    }

    @Override
    public File suggestName() {
        // TODO Auto-generated method stub
        return null;
    }
}
