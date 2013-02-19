package pl.poznan.put.cs.bioserver.gui;

import java.io.File;

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

import pl.poznan.put.cs.bioserver.helper.Exportable;
import pl.poznan.put.cs.bioserver.visualisation.MDS;
import pl.poznan.put.cs.bioserver.visualisation.MDSPlot;

public class TableModelGlobal extends AbstractTableModel implements
        Visualizable, Clusterable, Exportable {
    private static final long serialVersionUID = 1L;
    private String[] tableNames;
    private double[][] tableValues;

    public TableModelGlobal(String[] names, double[][] values) {
        super();
        tableNames = names.clone();
        tableValues = values.clone();
    }

    @Override
    public int getColumnCount() {
        if (tableNames.length == 0) {
            return 0;
        }
        return tableNames.length + 1;
    }

    @Override
    public String getColumnName(int column) {
        if (column == 0) {
            return "";
        }
        return tableNames[column - 1];
    }

    public String[] getNames() {
        return tableNames;
    }

    @Override
    public int getRowCount() {
        return tableValues.length;
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (column == 0) {
            return tableNames[row];
        }
        return tableValues[row][column - 1];
    }

    public double[][] getValues() {
        return tableValues;
    }

    @Override
    public void visualize() {
        for (double[] value : tableValues) {
            for (double element : value) {
                if (Double.isNaN(element)) {
                    JOptionPane.showMessageDialog(null, "Results cannot be "
                            + "visualized. Some structures could not be "
                            + "compared.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        }

        double[][] mds = MDS.multidimensionalScaling(tableValues, 2);
        if (mds == null) {
            JOptionPane.showMessageDialog(null, "Cannot visualise specified "
                    + "structures in 2D space", "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        MDSPlot plot = new MDSPlot(mds, tableNames);
        plot.setVisible(true);
    }

    @Override
    public void cluster() {
        // TODO Auto-generated method stub

    }

    @Override
    public void export(File file) {
        // TODO Auto-generated method stub
        
    }
}
