package pl.poznan.put.cs.bioserver.gui;

import javax.swing.table.AbstractTableModel;

class MatrixTableModel extends AbstractTableModel {
    private static final long serialVersionUID = 1L;
    private String[] tableNames;
    private double[][] tableValues;

    MatrixTableModel(String[] names, double[][] values) {
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
}
