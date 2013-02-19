package pl.poznan.put.cs.bioserver.gui;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.table.AbstractTableModel;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.biojava.bio.structure.ResidueNumber;

import pl.poznan.put.cs.bioserver.helper.Exportable;
import pl.poznan.put.cs.bioserver.torsion.AngleDifference;

public class TableModelLocal extends AbstractTableModel implements
        Visualizable, Exportable {
    private static final long serialVersionUID = 1L;
    private int columnCount;
    private int rowCount;
    private double[][] values;
    private String[] columnNames;
    private String[] rowsNames;

    public TableModelLocal(Map<String, List<AngleDifference>> results) {
        super();

        Set<String> setNames = new LinkedHashSet<>();
        Set<ResidueNumber> setResidues = new TreeSet<>();
        Map<MultiKey, Double> mapValues = new HashMap<>();

        for (Entry<String, List<AngleDifference>> entry : results.entrySet()) {
            for (AngleDifference difference : entry.getValue()) {
                double value = difference.getDifference();
                if (Double.isNaN(value)) {
                    continue;
                }

                String name = difference.getAngleName();
                ResidueNumber residue = difference.getResidue();
                MultiKey key = new MultiKey(name, residue);
                setNames.add(name);
                setResidues.add(residue);
                mapValues.put(key, value);
            }
        }

        rowCount = setResidues.size();
        columnCount = setNames.size() + 1;

        columnNames = new String[columnCount];
        columnNames[0] = "Residue";
        int i = 1;
        for (String name : setNames) {
            columnNames[i] = name;
            i++;
        }

        rowsNames = new String[rowCount];
        i = 0;
        for (ResidueNumber residue : setResidues) {
            rowsNames[i] = String.format("%s:%03d", residue.getChainId(),
                    residue.getSeqNum());
            i++;
        }

        values = new double[rowCount][];
        for (i = 0; i < rowCount; i++) {
            values[i] = new double[columnCount];
        }
        i = 0;
        for (ResidueNumber residue : setResidues) {
            int j = 0;
            for (String name : setNames) {
                MultiKey key = new MultiKey(name, residue);
                if (mapValues.containsKey(key)) {
                    values[i][j] = mapValues.get(key);
                } else {
                    values[i][j] = Double.NaN;
                }
                j++;
            }
            i++;
        }
    }

    @Override
    public int getColumnCount() {
        return columnCount;
    }

    @Override
    public int getRowCount() {
        return rowCount;
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (column == 0) {
            return rowsNames[row];
        }
        return values[row][column - 1];
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public void visualize() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void export(File file) {
        // TODO Auto-generated method stub
        
    }
}
