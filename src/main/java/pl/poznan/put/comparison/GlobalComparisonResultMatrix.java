package pl.poznan.put.comparison;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.stat.StatUtils;
import org.biojava.bio.structure.jama.Matrix;
import org.jzy3d.analysis.AnalysisLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.svg.SVGDocument;

import pl.poznan.put.constant.Unicode;
import pl.poznan.put.datamodel.NamedPoint;
import pl.poznan.put.gui.DialogCluster;
import pl.poznan.put.gui.Surface3D;
import pl.poznan.put.interfaces.Clusterable;
import pl.poznan.put.interfaces.Exportable;
import pl.poznan.put.interfaces.Tabular;
import pl.poznan.put.interfaces.Visualizable;
import pl.poznan.put.types.ExportFormat;
import pl.poznan.put.utility.TabularExporter;
import pl.poznan.put.visualisation.MDS;
import pl.poznan.put.visualisation.SVGDrawer;

public class GlobalComparisonResultMatrix implements Clusterable, Exportable, Visualizable, Tabular {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalComparisonResult.class);

    private final GlobalComparator measure;
    private final List<String> names;
    private final int size;
    private final GlobalComparisonResult[][] resultsMatrix;
    private final Matrix distanceMatrix;

    public GlobalComparisonResultMatrix(GlobalComparator measureName,
            List<String> names, int size) {
        super();
        this.measure = measureName;
        this.names = names;
        this.size = size;

        GlobalComparisonResult[][] results = new GlobalComparisonResult[size][];
        double[][] distances = new double[size][];

        for (int i = 0; i < size; ++i) {
            results[i] = new GlobalComparisonResult[size];
            distances[i] = new double[size];
        }

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i == j) {
                    distances[i][j] = 0;
                } else {
                    distances[i][j] = Double.NaN;
                }
            }
        }

        this.resultsMatrix = results;
        this.distanceMatrix = new Matrix(distances);
    }

    public String getMeasureName() {
        return measure.getName();
    }

    public int getSize() {
        return size;
    }

    public String getName(int index) {
        return names.get(index);
    }

    public List<String> getNames() {
        return Collections.unmodifiableList(names);
    }

    public GlobalComparisonResult getResult(int i, int j) {
        return resultsMatrix[i][j];
    }

    public void setResult(int i, int j, GlobalComparisonResult result) {
        resultsMatrix[i][j] = resultsMatrix[j][i] = result;

        if (result instanceof MCQGlobalResult) {
            distanceMatrix.set(i, j, ((MCQGlobalResult) result).getMeanDirection().getRadians());
            distanceMatrix.set(j, i, ((MCQGlobalResult) result).getMeanDirection().getRadians());
        } else if (result instanceof RMSDGlobalResult) {
            distanceMatrix.set(i, j, ((RMSDGlobalResult) result).getRMSD());
            distanceMatrix.set(j, i, ((RMSDGlobalResult) result).getRMSD());
        } else {
            throw new IllegalArgumentException("Unsupported result type: " + result.getClass());
        }
    }

    public Matrix getDistanceMatrix() {
        return distanceMatrix;
    }

    private boolean isMatrixValid() {
        for (int i = 0; i < distanceMatrix.getRowDimension(); i++) {
            for (int j = 0; j < distanceMatrix.getColumnDimension(); j++) {
                if (Double.isNaN(distanceMatrix.get(i, j))) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    // TODO: Do not use javax.swing here, propagate error through exceptions
    public void cluster() {
        if (!isMatrixValid()) {
            JOptionPane.showMessageDialog(null, "Results cannot be clustered. Some structures could not be compared.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        DialogCluster dialogClustering = new DialogCluster(this);
        dialogClustering.setVisible(true);
    }

    @Override
    public void export(OutputStream stream) throws IOException {
        TabularExporter.export(asExportableTableModel(), stream);
    }

    @Override
    public ExportFormat getExportFormat() {
        return ExportFormat.CSV;
    }

    @Override
    public File suggestName() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
        String filename = sdf.format(new Date());
        filename += "-Global-";
        filename += measure.getName();
        filename += ".csv";
        return new File(filename);
    }

    @Override
    public SVGDocument visualize() {
        if (!isMatrixValid()) {
            throw new IllegalArgumentException("Results cannot be visualized. Some structures could not be compared.");
        }

        List<NamedPoint> points = new ArrayList<>();
        double[][] xyMatrix = MDS.multidimensionalScaling(distanceMatrix.getArray(), 2);

        for (int i = 0; i < xyMatrix.length; i++) {
            String name = names.get(i);
            Vector2D point = new Vector2D(xyMatrix[i][0], xyMatrix[i][1]);
            points.add(new NamedPoint(name, point));
        }

        return SVGDrawer.drawPoints(points);
    }

    @Override
    public void visualize3D() {
        try {
            String name = measure.getName();
            double[][] matrix = distanceMatrix.getArray();
            List<String> ticksX = names;
            List<String> ticksY = names;
            NavigableMap<Double, String> valueTickZ = prepareTicksZ();
            String labelX = "";
            String labelY = "";
            String labelZ = "Distance";
            boolean showAllTicksX = true;
            boolean showAllTicksY = true;

            Surface3D surface3d = new Surface3D(name, matrix, ticksX, ticksY, valueTickZ, labelX, labelY, labelZ, showAllTicksX, showAllTicksY);
            AnalysisLauncher.open(surface3d);
        } catch (Exception e) {
            String message = "Failed to visualize in 3D";
            GlobalComparisonResultMatrix.LOGGER.error(message, e);
            JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private NavigableMap<Double, String> prepareTicksZ() {
        NavigableMap<Double, String> valueTickZ = new TreeMap<>();
        valueTickZ.put(0.0, "0");

        if (measure instanceof MCQ) {
            for (double radians = Math.PI / 12.0; radians <= Math.PI + 1e-3; radians += Math.PI / 12.0) {
                valueTickZ.put(radians, Long.toString(Math.round(Math.toDegrees(radians))) + Unicode.DEGREE);
            }
        } else if (measure instanceof RMSD) {
            double max = StatUtils.max(distanceMatrix.getColumnPackedCopy());
            for (double angstrom = 1.0; angstrom <= Math.ceil(max) + 1e-3; angstrom += 1.0) {
                valueTickZ.put(angstrom, Long.toString(Math.round(angstrom)) + Unicode.ANGSTROM);
            }
        } else {
            throw new IllegalArgumentException("Unknown measure: " + measure.getName());
        }
        return valueTickZ;
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
        String[] columnNames = new String[names.size() + 1];
        columnNames[0] = "";
        for (int i = 0; i < names.size(); i++) {
            columnNames[i + 1] = names.get(i);
        }

        String[][] values = new String[resultsMatrix.length][];

        for (int i = 0; i < values.length; i++) {
            values[i] = new String[columnNames.length];
            values[i][0] = names.get(i);

            for (int j = 0; j < resultsMatrix[i].length; j++) {
                // diagonal is empty
                if (i == j) {
                    values[i][j + 1] = isDisplay ? "" : null;
                    continue;
                }

                GlobalComparisonResult result = resultsMatrix[i][j];

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
