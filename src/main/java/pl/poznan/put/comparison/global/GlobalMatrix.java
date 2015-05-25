package pl.poznan.put.comparison.global;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import pl.poznan.put.datamodel.NamedPoint;
import pl.poznan.put.interfaces.Clusterable;
import pl.poznan.put.interfaces.Exportable;
import pl.poznan.put.interfaces.Tabular;
import pl.poznan.put.interfaces.Visualizable;
import pl.poznan.put.types.DistanceMatrix;
import pl.poznan.put.types.ExportFormat;
import pl.poznan.put.utility.TabularExporter;
import pl.poznan.put.utility.svg.SVGHelper;
import pl.poznan.put.visualisation.MDS;
import pl.poznan.put.visualisation.MDSDrawer;
import pl.poznan.put.visualisation.Surface3D;

public class GlobalMatrix implements Clusterable, Exportable, Visualizable, Tabular {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalResult.class);

    private final DistanceMatrix distanceMatrix;
    private final DistanceMatrix distanceMatrixWithoutIncomparables;

    private final GlobalComparator comparator;
    private final List<String> names;
    private final GlobalResult[][] resultsMatrix;

    public GlobalMatrix(GlobalComparator comparator, List<String> names,
            GlobalResult[][] resultsMatrix) {
        super();
        this.comparator = comparator;
        this.names = names;
        this.resultsMatrix = resultsMatrix.clone();

        distanceMatrix = prepareDistanceMatrix();
        distanceMatrixWithoutIncomparables = prepareDistanceMatrixWithoutIncomparables();
    }

    private DistanceMatrix prepareDistanceMatrix() {
        double[][] matrix = new double[resultsMatrix.length][];

        for (int i = 0; i < resultsMatrix.length; i++) {
            assert resultsMatrix.length == resultsMatrix[i].length;
            matrix[i] = new double[resultsMatrix.length];
            Arrays.fill(matrix[i], Double.NaN);

            for (int j = 0; j < resultsMatrix.length; j++) {
                if (i == j) {
                    matrix[i][j] = 0;
                } else if (resultsMatrix[i][j] != null) {
                    matrix[i][j] = resultsMatrix[i][j].asDouble();
                }
            }
        }

        return new DistanceMatrix(names, matrix);
    }

    private DistanceMatrix prepareDistanceMatrixWithoutIncomparables() {
        List<String> selectedNamesSubList = new ArrayList<>(names);
        double[][] selectedSubMatrix = distanceMatrix.getMatrix().clone();
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
                int[] selected = GlobalMatrix.selectAllButOne(argmax, selectedSubMatrix.length);
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

    public GlobalComparator getComparator() {
        return comparator;
    }

    public DistanceMatrix getDistanceMatrix() {
        return distanceMatrix;
    }

    public DistanceMatrix getDistanceMatrixWithoutIncomparables() {
        return distanceMatrixWithoutIncomparables;
    }

    @Override
    public DistanceMatrix getDataForClustering() {
        return distanceMatrixWithoutIncomparables;
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
        filename += comparator.getName();
        filename += ".csv";
        return new File(filename);
    }

    @Override
    public SVGDocument visualize() {
        double[][] array = distanceMatrixWithoutIncomparables.getMatrix();

        if (array.length <= 1) {
            GlobalMatrix.LOGGER.warn("Cannot visualize this distance matrix, because it contains zero valid comparisons");
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
            String name = comparator.getName();
            double[][] matrix = distanceMatrix.getMatrix();
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
            GlobalMatrix.LOGGER.error(message, e);
            JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private NavigableMap<Double, String> prepareTicksZ() {
        NavigableMap<Double, String> valueTickZ = new TreeMap<>();
        valueTickZ.put(0.0, "0");

        if (comparator.isAngularMeasure()) {
            for (double radians = Math.PI / 12.0; radians <= Math.PI + 1e-3; radians += Math.PI / 12.0) {
                valueTickZ.put(radians, Long.toString(Math.round(Math.toDegrees(radians))) + Unicode.DEGREE);
            }
        } else {
            double[][] matrix = distanceMatrix.getMatrix();
            double max = Double.NEGATIVE_INFINITY;

            for (double[] element : matrix) {
                max = Math.max(max, StatUtils.max(element));
            }

            for (double angstrom = 1.0; angstrom <= Math.ceil(max) + 1e-3; angstrom += 1.0) {
                valueTickZ.put(angstrom, Long.toString(Math.round(angstrom)) + Unicode.ANGSTROM);
            }
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

                GlobalResult result = resultsMatrix[i][j];

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
