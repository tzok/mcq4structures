package pl.poznan.put.cs.bioserver.gui;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import pl.poznan.put.cs.bioserver.beans.ComparisonLocal;
import pl.poznan.put.cs.bioserver.beans.auxiliary.Angle;
import pl.poznan.put.cs.bioserver.helper.Constants;

class TableModelLocal extends AbstractTableModel {
    private static final long serialVersionUID = 1L;
    private List<String> columnNames;
    private DecimalFormat format;
    private List<String> rowsNames;
    private double[][] values;

    TableModelLocal(ComparisonLocal comparisonLocal) {
        super();

        rowsNames = comparisonLocal.getTicks();

        List<Angle> angles =
                new ArrayList<>(comparisonLocal.getAngles().values());
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

        format = new DecimalFormat("0.000");
    }

    @Override
    public int getColumnCount() {
        return columnNames.size() + 1;
    }

    @Override
    public String getColumnName(int column) {
        if (column == 0) {
            return "ResID\\Angles";
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

        double value = values[row][column - 1];
        if (Double.isNaN(value)) {
            return "-";
        }

        return format.format(Math.toDegrees(value)) + Constants.UNICODE_DEGREE;
    }
}
