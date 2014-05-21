package pl.poznan.put.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.biojava.bio.structure.Group;

import pl.poznan.put.common.TorsionAngle;
import pl.poznan.put.comparison.ModelsComparisonResult;
import pl.poznan.put.matching.ResidueComparisonResult;
import pl.poznan.put.structure.CompactFragment;
import pl.poznan.put.structure.Residue;
import pl.poznan.put.utility.TorsionAngleDelta;

class TableModelLocalMulti extends AbstractTableModel {
    private static final long serialVersionUID = 49429840783357538L;

    private final ModelsComparisonResult result;
    private final int rowCount;
    private final List<String> columns;
    private final TorsionAngle angle;

    TableModelLocalMulti(ModelsComparisonResult result) {
        super();
        this.result = result;

        rowCount = result.getFragmentSize();

        columns = new ArrayList<>();
        columns.add("");
        for (CompactFragment model : result.getModels()) {
            columns.add(model.getName());
        }

        angle = result.getTorsionAngle();
    }

    @Override
    public int getColumnCount() {
        return columns.size();
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
            CompactFragment reference = result.getReference();
            Group group = reference.getResidue(row);
            return Residue.fromGroup(group);
        }

        List<ResidueComparisonResult> residueResults = result.getResidueResults(column - 1);
        ResidueComparisonResult comparisonResult = residueResults.get(row);
        TorsionAngleDelta delta = comparisonResult.getDelta(angle);
        return delta.toDisplayString();
    }
}
