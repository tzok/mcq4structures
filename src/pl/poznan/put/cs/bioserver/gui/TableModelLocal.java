package pl.poznan.put.cs.bioserver.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import pl.poznan.put.cs.bioserver.beans.ComparisonLocal;
import pl.poznan.put.cs.bioserver.beans.auxiliary.Angle;

public class TableModelLocal extends AbstractTableModel {
    private static final long serialVersionUID = 1L;
    private double[][] values;
    private List<String> columnNames;
    private List<String> rowsNames;

    public TableModelLocal(ComparisonLocal comparisonLocal) {
        super();

        rowsNames = comparisonLocal.getTicks();

        List<Angle> angles = comparisonLocal.getAngleList();
        columnNames = new ArrayList<>();
        for (Angle a : angles) {
            columnNames.add(a.getName());
        }

        values = new double[rowsNames.size()][];
        for (int i = 0; i < rowsNames.size(); i++) {
            values[i] = new double[angles.size()];
            for (int j = 0; j < angles.size(); j++) {
                values[i][j] = angles.get(j).getDeltas()[i];
            }
        }
    }

    @Override
    public int getColumnCount() {
        return columnNames.size() + 1;
    }

    @Override
    public String getColumnName(int column) {
        if (column == 0) {
            return "Residue\\Angles";
        }
        return columnNames.get(column - 1);
    }

    @Override
    public int getRowCount() {
        return rowsNames.size();
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (column == 0) {
            return rowsNames.get(row);
        }
        return values[row][column - 1];
    }
}
