package pl.poznan.put.cs.bioserver.gui;

import javax.swing.table.AbstractTableModel;

import pl.poznan.put.cs.bioserver.beans.ComparisonLocal;
import pl.poznan.put.cs.bioserver.beans.auxiliary.Angle;

public class TableModelLocal extends AbstractTableModel {
    private static final long serialVersionUID = 1L;
    private double[][] values;
    private String[] columnNames;
    private String[] rowsNames;

    public TableModelLocal(ComparisonLocal comparisonLocal) {
        super();

        rowsNames = comparisonLocal.getTicks();

        Angle[] angles = comparisonLocal.getAngles();
        columnNames = new String[angles.length];
        for (int i = 0; i < angles.length; i++) {
            columnNames[i] = angles[i].getName();
        }

        values = new double[rowsNames.length][];
        for (int i = 0; i < rowsNames.length; i++) {
            values[i] = new double[angles.length];
            for (int j = 0; j < angles.length; j++) {
                values[i][j] = angles[j].getDeltas()[i];
            }
        }
    }

    @Override
    public int getColumnCount() {
        return columnNames.length + 1;
    }

    @Override
    public String getColumnName(int column) {
        if (column == 0) {
            return "Residue\\Angles";
        }
        return columnNames[column - 1];
    }

    @Override
    public int getRowCount() {
        return rowsNames.length;
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (column == 0) {
            return rowsNames[row];
        }
        return values[row][column - 1];
    }
}
