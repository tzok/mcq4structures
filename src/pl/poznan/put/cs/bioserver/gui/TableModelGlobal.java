package pl.poznan.put.cs.bioserver.gui;

import java.text.DecimalFormat;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import pl.poznan.put.cs.bioserver.beans.ComparisonGlobal;

public class TableModelGlobal extends AbstractTableModel {
    private static final long serialVersionUID = 1L;
    private DecimalFormat format;
    private String measure;
    private List<String> names;
    private double[][] values;

    public TableModelGlobal(ComparisonGlobal comparisonGlobal) {
        super();
        names = comparisonGlobal.getLabels();
        values = comparisonGlobal.getDistanceMatrix();
        measure = comparisonGlobal.getMethod();
        format = new DecimalFormat("0.000");
    }

    @Override
    public int getColumnCount() {
        if (names.size() == 0) {
            return 0;
        }
        return names.size() + 1;
    }

    @Override
    public String getColumnName(int column) {
        if (column == 0) {
            return "Global " + measure;
        }
        return names.get(column - 1);
    }

    @Override
    public int getRowCount() {
        return values.length;
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (column == 0) {
            return names.get(row);
        }
        return format.format(values[row][column - 1]);
    }
}
