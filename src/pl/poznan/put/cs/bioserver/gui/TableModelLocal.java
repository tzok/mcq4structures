package pl.poznan.put.cs.bioserver.gui;

import java.awt.BorderLayout;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JFrame;
import javax.swing.table.AbstractTableModel;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.biojava.bio.structure.ResidueNumber;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
import org.jfree.data.xy.DefaultXYDataset;

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
    private Set<String> setAngles;

    public TableModelLocal(Map<String, List<AngleDifference>> results,
            String[] angles) {
        super();
        this.setAngles = new LinkedHashSet<>(Arrays.asList(angles));

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
                if (!setAngles.contains(name)) {
                    continue;
                }

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
        double[] x = new double[rowCount];
        for (int i = 0; i < rowCount; i++) {
            x[i] = i;
        }
        double[][] y = new double[columnCount][rowCount];
        for (int i = 0; i < columnCount; i++) {
            y[i] = new double[rowCount];
            for (int j = 0; j < rowCount; j++) {
                y[i][j] = values[j][i];
            }
        }

        DefaultXYDataset dataset = new DefaultXYDataset();
        for (int i = 0; i < y.length - 1; i++) {
            dataset.addSeries(columnNames[i + 1], new double[][] { x, y[i] });
        }

        NumberAxis xAxis = new TorsionAxis(rowsNames);
        xAxis.setLabel("Residue");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setAutoRange(false);
        yAxis.setRange(0, Math.PI);
        yAxis.setLabel("Distance [rad]");
        XYPlot plot = new XYPlot(dataset, xAxis, yAxis,
                new DefaultXYItemRenderer());

        JFrame frame = new JFrame();
        frame.setLayout(new BorderLayout());
        frame.add(new ChartPanel(new JFreeChart(plot)));
        frame.setSize(640, 480);
        frame.setVisible(true);
    }

    @Override
    public void export(File file) {
        // TODO Auto-generated method stub

    }

    @Override
    public File suggestName() {
        // TODO Auto-generated method stub
        return null;
    }
}
