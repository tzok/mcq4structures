package pl.poznan.put.gui;

import javax.swing.table.AbstractTableModel;

import pl.poznan.put.comparison.GlobalComparisonResult;
import pl.poznan.put.comparison.GlobalComparisonResultMatrix;

class TableModelGlobal extends AbstractTableModel {
    private static final long serialVersionUID = 1L;

    private final int size;
    private final String measure;
    private final String[] names;
    private final GlobalComparisonResult[][] values;

    TableModelGlobal(GlobalComparisonResultMatrix comparisonGlobal) {
        super();
        size = comparisonGlobal.getSize();
        names = comparisonGlobal.getNames();
        values = comparisonGlobal.getResults();
        measure = comparisonGlobal.getMeasureName();
    }

    @Override
    public int getColumnCount() {
        if (size == 0) {
            return 0;
        }
        return size + 1;
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
        return size;
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (column == 0) {
            return names[row];
        }

        if (row == column - 1) {
            return "";
        }

        if (values[row][column - 1] == null) {
            return "FAILED";
        }

        return values[row][column - 1].toDisplayString();
    }
}
