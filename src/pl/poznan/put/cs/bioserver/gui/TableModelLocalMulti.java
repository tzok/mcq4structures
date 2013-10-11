package pl.poznan.put.cs.bioserver.gui;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import pl.poznan.put.cs.bioserver.beans.ComparisonLocal;
import pl.poznan.put.cs.bioserver.beans.ComparisonLocalMulti;
import pl.poznan.put.cs.bioserver.beans.auxiliary.Angle;
import pl.poznan.put.cs.bioserver.helper.Constants;
import pl.poznan.put.cs.bioserver.torsion.AngleType;

class TableModelLocalMulti extends AbstractTableModel {
    private static final long serialVersionUID = 49429840783357538L;

    private int columnCount;
    private List<String> columns;
    private Object[][] data;
    private DecimalFormat format;
    private int rowCount;

    TableModelLocalMulti(ComparisonLocalMulti localMulti, AngleType angleType) {
        super();

        List<ComparisonLocal> results = localMulti.getResults();
        assert results.size() > 0;

        List<String> ticks = results.get(0).getTicks();
        rowCount = ticks.size();
        columnCount = results.size() + 1;

        data = new Object[rowCount][columnCount];
        columns = new ArrayList<>();
        for (int i = 0; i < columnCount; i++) {
            if (i == 0) {
                columns.add("ResID\\Structures");
                for (int j = 0; j < rowCount; j++) {
                    data[j][0] = ticks.get(j);
                }
                continue;
            }

            ComparisonLocal comparisonLocal = results.get(i - 1);
            columns.add(comparisonLocal.getTitle());

            Map<AngleType, Angle> angles = comparisonLocal.getAngles();
            Angle angle = angles.get(angleType);
            assert angle != null;

            double[] deltas = angle.getDeltas();
            for (int j = 0; j < rowCount; j++) {
                data[j][i] = deltas[j];
            }
        }

        format = new DecimalFormat("0.000");
    }

    @Override
    public int getColumnCount() {
        return columnCount;
    }

    @Override
    public String getColumnName(int column) {
        return columns.get(column);
    }

    @Override
    public int getRowCount() {
        return rowCount;
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (column == 0) {
            return data[row][column];
        }

        double value = (double) data[row][column];
        if (Double.isNaN(value)) {
            return "-";
        }

        return format.format(Math.toDegrees(value)) + Constants.UNICODE_DEGREE;
    }
}
