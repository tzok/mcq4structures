package pl.poznan.put.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import pl.poznan.put.common.TorsionAngle;
import pl.poznan.put.comparison.MCQLocalComparisonResult;
import pl.poznan.put.matching.ResidueComparisonResult;
import pl.poznan.put.matching.TorsionAngleDelta;

class TableModelLocal extends AbstractTableModel {
    private static final long serialVersionUID = 1L;

    private final List<String> rowsNames;
    private final List<String> columnNames;
    private final List<TorsionAngle> columnAngles;
    private final List<ResidueComparisonResult> values;

    TableModelLocal(MCQLocalComparisonResult result) {
        super();

        rowsNames = result.getDataLabels();
        columnNames = new ArrayList<>();
        for (TorsionAngle angle : result.getAngles()) {
            columnNames.add(angle.getDisplayName());
        }

        columnAngles = result.getAngles();
        values = result.getDataRows();
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

        ResidueComparisonResult residueResult = values.get(row);
        TorsionAngle torsionAngle = columnAngles.get(column - 1);
        TorsionAngleDelta delta = residueResult.getDelta(torsionAngle);
        return delta != null ? delta.toDisplayString() : null;
    }
}
