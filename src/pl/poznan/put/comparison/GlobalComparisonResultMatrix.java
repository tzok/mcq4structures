package pl.poznan.put.comparison;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.jzy3d.chart.Chart;
import org.jzy3d.chart.ChartLauncher;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapRainbow;
import org.jzy3d.maths.Range;
import org.jzy3d.plot3d.builder.Builder;
import org.jzy3d.plot3d.builder.Mapper;
import org.jzy3d.plot3d.builder.concrete.OrthonormalGrid;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.primitives.axes.layout.IAxeLayout;
import org.jzy3d.plot3d.primitives.axes.layout.providers.RegularTickProvider;
import org.jzy3d.plot3d.primitives.axes.layout.renderers.TickLabelMap;
import org.jzy3d.plot3d.rendering.canvas.Quality;

import pl.poznan.put.gui.DialogCluster;
import pl.poznan.put.interfaces.Clusterable;
import pl.poznan.put.interfaces.Exportable;
import pl.poznan.put.interfaces.Tabular;
import pl.poznan.put.interfaces.Visualizable;
import pl.poznan.put.utility.InvalidInputException;
import pl.poznan.put.utility.TabularExporter;
import pl.poznan.put.visualisation.MDS;
import pl.poznan.put.visualisation.MDSPlot;

public class GlobalComparisonResultMatrix implements Clusterable, Exportable,
        Visualizable, Tabular {
    private final String measureName;
    private final int size;
    private final String[] names;
    private final GlobalComparisonResult[][] results;
    private final double[][] matrix;

    public GlobalComparisonResultMatrix(String measureName, int size) {
        super();
        this.measureName = measureName;
        this.size = size;

        names = new String[size];
        results = new GlobalComparisonResult[size][];
        matrix = new double[size][];
        for (int i = 0; i < size; ++i) {
            results[i] = new GlobalComparisonResult[size];
            matrix[i] = new double[size];
        }

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i == j) {
                    matrix[i][j] = 0;
                } else {
                    matrix[i][j] = Double.NaN;
                }
            }
        }
    }

    public String getMeasureName() {
        return measureName;
    }

    public int getSize() {
        return size;
    }

    public String[] getNames() {
        return names;
    }

    public GlobalComparisonResult[][] getResults() {
        return results;
    }

    public double[][] getMatrix() {
        return matrix;
    }

    public void set(int i, int j, GlobalComparisonResult result) {
        results[i][j] = results[j][i] = result;
        matrix[i][j] = matrix[j][i] = result.getValue();

        if (names[i] == null) {
            names[i] = result.getNameLeft();
        }

        if (names[j] == null) {
            names[j] = result.getNameRight();
        }
    }

    @Override
    public void cluster() {
        for (double[] value : matrix) {
            for (double element : value) {
                if (Double.isNaN(element)) {
                    JOptionPane.showMessageDialog(null, "Results cannot be "
                            + "clustered. Some structures could not be "
                            + "compared.", "Error", JOptionPane.ERROR_MESSAGE);
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
        for (double[] value : matrix) {
            for (double element : value) {
                if (Double.isNaN(element)) {
                    JOptionPane.showMessageDialog(null,
                            "Results cannot be visualized. Some "
                                    + "structures could not be compared.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        }

        double[][] mds;
        try {
            mds = MDS.multidimensionalScaling(matrix, 2);
        } catch (InvalidInputException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        MDSPlot plot = new MDSPlot(mds, Arrays.asList(names));
        plot.setTitle("MCQ4Structures: global distance diagram (" + measureName
                + ")");
        plot.setVisible(true);
    }

    @Override
    public void visualize3D() {
        Shape surface = Builder.buildOrthonormal(new OrthonormalGrid(new Range(
                0, size - 1), size), new Mapper() {
            @Override
            public double f(double x, double y) {
                int i = (int) Math.round(x);
                int j = (int) Math.round(y);

                i = Math.max(Math.min(i, size - 1), 0);
                j = Math.max(Math.min(j, size - 1), 0);
                return matrix[i][j];
            }
        });

        surface.setColorMapper(new ColorMapper(new ColorMapRainbow(),
                surface.getBounds().getZmin(), surface.getBounds().getZmax(),
                new Color(1, 1, 1, .5f)));
        surface.setFaceDisplayed(true);
        surface.setWireframeDisplayed(false);

        Chart chart = new Chart(Quality.Nicest);
        chart.getScene().getGraph().add(surface);

        TickLabelMap map = new TickLabelMap();
        for (int i = 0; i < names.length; i++) {
            map.register(i, names[i]);
        }

        IAxeLayout axeLayout = chart.getAxeLayout();
        axeLayout.setXTickProvider(new RegularTickProvider(size));
        axeLayout.setXTickRenderer(map);
        axeLayout.setYTickProvider(new RegularTickProvider(size));
        axeLayout.setYTickRenderer(map);
        axeLayout.setZAxeLabel(measureName.equals("MCQ") ? "Angular distance"
                : "Distance [\u212B]");

        ChartLauncher.openChart(chart);
    }

    @Override
    public void visualizeHighQuality() {
        throw new UnsupportedOperationException("Method not implemented!");
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
                    values[i][j + 1] = isDisplay ? result.toDisplayString()
                            : result.toExportString();
                }
            }
        }

        return new DefaultTableModel(values, columnNames);
    }
}
