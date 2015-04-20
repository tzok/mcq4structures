package pl.poznan.put.comparison;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.biojava.bio.structure.jama.Matrix;

import pl.poznan.put.gui.DialogCluster;
import pl.poznan.put.interfaces.Clusterable;
import pl.poznan.put.interfaces.Exportable;
import pl.poznan.put.interfaces.Tabular;
import pl.poznan.put.interfaces.Visualizable;
import pl.poznan.put.utility.InvalidInputException;
import pl.poznan.put.utility.TabularExporter;
import pl.poznan.put.visualisation.MDS;
import pl.poznan.put.visualisation.MDSPlot;

public class GlobalComparisonResultMatrix implements Clusterable, Exportable, Visualizable, Tabular {
    private final String measureName;
    private final int size;
    private final String[] names;
    private final GlobalComparisonResult[][] results;
    private final Matrix matrix;

    public GlobalComparisonResultMatrix(String measureName, int size) {
        super();
        this.measureName = measureName;
        this.size = size;

        names = new String[size];
        results = new GlobalComparisonResult[size][];
        double[][] rawMatrix = new double[size][];

        for (int i = 0; i < size; ++i) {
            results[i] = new GlobalComparisonResult[size];
            rawMatrix[i] = new double[size];
        }

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i == j) {
                    rawMatrix[i][j] = 0;
                } else {
                    rawMatrix[i][j] = Double.NaN;
                }
            }
        }

        matrix = new Matrix(rawMatrix);
    }

    public String getMeasureName() {
        return measureName;
    }

    public int getSize() {
        return size;
    }

    public String getName(int index) {
        return names[index];
    }

    public String[] getNames() {
        return names.clone();
    }

    public GlobalComparisonResult getResult(int i, int j) {
        return results[i][j];
    }

    public void setResult(int i, int j, GlobalComparisonResult result) {
        results[i][j] = results[j][i] = result;
        matrix.set(i, j, result.getValue());
        matrix.set(j, i, result.getValue());

        if (names[i] == null) {
            names[i] = result.getTargetName();
        }

        if (names[j] == null) {
            names[j] = result.getModelName();
        }
    }

    public Matrix getDistanceMatrix() {
        return matrix;
    }

    @Override
    public void cluster() {
        for (int i = 0; i < matrix.getRowDimension(); i++) {
            for (int j = 0; j < matrix.getColumnDimension(); j++) {
                if (Double.isNaN(matrix.get(i, j))) {
                    JOptionPane.showMessageDialog(null, "Results cannot be clustered. Some structures could not be compared.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        }

        DialogCluster dialogClustering = new DialogCluster(this);
        dialogClustering.setVisible(true);
    }

    @Override
    public void export(File file) throws IOException {
        TabularExporter.export(this, file);
    }

    @Override
    public File suggestName() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
        String filename = sdf.format(new Date());
        filename += "-Global-";
        filename += measureName;
        filename += ".csv";
        return new File(filename);
    }

    @Override
    public void visualize() {
        double[][] mds;
        try {
            mds = MDS.multidimensionalScaling(matrix.getArray(), 2);
        } catch (InvalidInputException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        MDSPlot plot = new MDSPlot(mds, Arrays.asList(names));
        plot.setTitle("MCQ4Structures: global distance diagram (" + measureName + ")");
        plot.setVisible(true);
    }

    @Override
    public void visualize3D() {
        // TODO
        // Shape surface = Builder.buildOrthonormal(new OrthonormalGrid(new
        // Range(
        // 0, size - 1), size), new Mapper() {
        // @Override
        // public double f(double x, double y) {
        // int i = (int) Math.round(x);
        // int j = (int) Math.round(y);
        //
        // i = Math.max(Math.min(i, size - 1), 0);
        // j = Math.max(Math.min(j, size - 1), 0);
        // return matrix[i][j];
        // }
        // });
        //
        // surface.setColorMapper(new ColorMapper(new ColorMapRainbow(),
        // surface.getBounds().getZmin(), surface.getBounds().getZmax(),
        // new Color(1, 1, 1, .5f)));
        // surface.setFaceDisplayed(true);
        // surface.setWireframeDisplayed(false);
        //
        // Chart chart = new Chart(Quality.Nicest);
        // chart.getScene().getGraph().add(surface);
        //
        // TickLabelMap map = new TickLabelMap();
        // for (int i = 0; i < names.length; i++) {
        // map.register(i, names[i]);
        // }
        //
        // IAxeLayout axeLayout = chart.getAxeLayout();
        // axeLayout.setXTickProvider(new RegularTickProvider(size));
        // axeLayout.setXTickRenderer(map);
        // axeLayout.setYTickProvider(new RegularTickProvider(size));
        // axeLayout.setYTickRenderer(map);
        // axeLayout.setZAxeLabel(measureName.equals("MCQ") ? "Angular distance"
        // : "Distance [\u212B]");
        //
        // ChartLauncher.openChart(chart);
    }

    @Override
    public TableModel asExportableTableModel() {
        return asTableModel(false);
    }

    @Override
    public TableModel asDisplayableTableModel() {
        return asTableModel(true);
    }

    private TableModel asTableModel(boolean isDisplay) {
        String[] columnNames = new String[names.length + 1];
        columnNames[0] = isDisplay ? "" : null;
        for (int i = 0; i < names.length; i++) {
            columnNames[i + 1] = names[i];
        }

        String[][] values = new String[results.length][];

        for (int i = 0; i < values.length; i++) {
            values[i] = new String[columnNames.length];
            values[i][0] = names[i];

            for (int j = 0; j < results[i].length; j++) {
                // diagonal is empty
                if (i == j) {
                    values[i][j + 1] = isDisplay ? "" : null;
                    continue;
                }

                GlobalComparisonResult result = results[i][j];

                if (result == null) {
                    values[i][j + 1] = "Failed";
                } else {
                    values[i][j + 1] = isDisplay ? result.getShortDisplayName() : result.getExportName();
                }
            }
        }

        return new DefaultTableModel(values, columnNames);
    }
}
