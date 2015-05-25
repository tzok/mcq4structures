package pl.poznan.put.comparison;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.StatUtils;
import org.jzy3d.analysis.AnalysisLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.svg.SVGDocument;

import pl.poznan.put.constant.Unicode;
import pl.poznan.put.datamodel.DistanceMatrix;
import pl.poznan.put.datamodel.NamedPoint;
import pl.poznan.put.gui.DialogCluster;
import pl.poznan.put.gui.Surface3D;
import pl.poznan.put.interfaces.Clusterable;
import pl.poznan.put.interfaces.Exportable;
import pl.poznan.put.interfaces.Tabular;
import pl.poznan.put.interfaces.Visualizable;
import pl.poznan.put.types.ExportFormat;
import pl.poznan.put.utility.TabularExporter;
import pl.poznan.put.utility.svg.SVGHelper;
import pl.poznan.put.visualisation.MDS;
import pl.poznan.put.visualisation.MDSDrawer;

public class GlobalComparisonResultMatrix implements Clusterable, Exportable, Visualizable, Tabular {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalComparisonResult.class);

    private final GlobalComparator measure;
    private final List<String> names;
    private final int size;
    private final GlobalComparisonResult[][] resultsMatrix;
    private final double[][] matrix;

    public GlobalComparisonResultMatrix(GlobalComparator measureName,
            List<String> names, int size) {
        super();
        this.measure = measureName;
        this.names = names;
        this.size = size;

        resultsMatrix = new GlobalComparisonResult[size][];
        matrix = new double[size][];

        for (int i = 0; i < size; ++i) {
            resultsMatrix[i] = new GlobalComparisonResult[size];
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
        return measure.getName();
    }

    public int getSize() {
        return size;
    }

    public GlobalComparisonResult getResult(int i, int j) {
        return resultsMatrix[i][j];
    }

    public void setResult(int i, int j, GlobalComparisonResult result) {
        resultsMatrix[i][j] = resultsMatrix[j][i] = result;

        if (result instanceof MCQGlobalResult) {
            matrix[i][j] = matrix[j][i] = ((MCQGlobalResult) result).getMeanDirection().getRadians();
        } else if (result instanceof RMSDGlobalResult) {
            matrix[i][j] = matrix[j][i] = ((RMSDGlobalResult) result).getRMSD();
        } else {
            throw new IllegalArgumentException("Unsupported result type: " + result.getClass());
        }
    }

    public DistanceMatrix getDistanceMatrix() {
        return new DistanceMatrix(names, matrix);
    }

    public DistanceMatrix getDistanceMatrixWithoutIncomparables() {
        List<String> selectedNamesSubList = new ArrayList<>(names);
        double[][] selectedSubMatrix = matrix.clone();
        int maxErrorCount;

        do {
            maxErrorCount = Integer.MIN_VALUE;
            int argmax = -1;

            for (int i = 0; i < selectedSubMatrix.length; i++) {
                assert selectedSubMatrix.length == selectedSubMatrix[i].length;
                int errorCount = 0;

                for (int j = 0; j < selectedSubMatrix.length; j++) {
                    errorCount += Double.isNaN(selectedSubMatrix[i][j]) ? 1 : 0;
                }

                if (errorCount > maxErrorCount) {
                    maxErrorCount = errorCount;
                    argmax = i;
                }
            }

            if (maxErrorCount > 0) {
                int[] selected = GlobalComparisonResultMatrix.selectAllButOne(argmax, selectedSubMatrix.length);
                RealMatrix realMatrix = new Array2DRowRealMatrix(selectedSubMatrix);
                RealMatrix subMatrix = realMatrix.getSubMatrix(selected, selected);
                selectedSubMatrix = subMatrix.getData();
                selectedNamesSubList.remove(argmax);
            }
        } while (maxErrorCount > 0);

        return new DistanceMatrix(selectedNamesSubList, selectedSubMatrix);
    }

    private static int[] selectAllButOne(int excluded, int count) {
        int[] selected = new int[count - 1];
        for (int i = 0, j = 0; i < count; i++) {
            if (i != excluded) {
                selected[j++] = i;
            }
        }
        return selected;
    }

    @Override
    public void cluster() {
        DistanceMatrix distanceMatrix = getDistanceMatrixWithoutIncomparables();
        double[][] array = distanceMatrix.getMatrix();

        if (array.length <= 1) {
            String message = "Cannot cluster this distance matrix, because it contains zero valid comparisons";
            GlobalComparisonResultMatrix.LOGGER.warn(message);
            JOptionPane.showMessageDialog(null, message, "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        DialogCluster dialogClustering = new DialogCluster(distanceMatrix);
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
        DistanceMatrix distanceMatrix = getDistanceMatrixWithoutIncomparables();
        double[][] array = distanceMatrix.getMatrix();

        if (array.length <= 1) {
            GlobalComparisonResultMatrix.LOGGER.warn("Cannot visualize this distance matrix, because it contains zero valid comparisons");
            return SVGHelper.emptyDocument();
        }

        List<NamedPoint> points = new ArrayList<>();
        double[][] xyMatrix = MDS.multidimensionalScaling(array, 2);

        for (int i = 0; i < xyMatrix.length; i++) {
            String name = names.get(i);
            Vector2D point = new Vector2D(xyMatrix[i][0], xyMatrix[i][1]);
            points.add(new NamedPoint(name, point));
        }

        return MDSDrawer.drawPoints(points);
    }

    @Override
    public void visualize3D() {
        try {
            String name = measure.getName();
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
            double max = Double.NEGATIVE_INFINITY;
            for (int i = 0; i < matrix.length; i++) {
                max = Math.max(max, StatUtils.max(matrix[i]));
            }
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
