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

import org.jzy3d.analysis.AnalysisLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.svg.SVGDocument;

import pl.poznan.put.constant.Unicode;
import pl.poznan.put.gui.Surface3D;
import pl.poznan.put.matching.FragmentComparison;
import pl.poznan.put.matching.FragmentMatch;
import pl.poznan.put.matching.ResidueComparison;
import pl.poznan.put.matching.SelectionMatch;
import pl.poznan.put.torsion.MasterTorsionAngleType;
import pl.poznan.put.torsion.TorsionAngleDelta;
import pl.poznan.put.types.ExportFormat;
import pl.poznan.put.utility.TabularExporter;

public class MCQLocalResult extends LocalComparisonResult {
    private static final Logger LOGGER = LoggerFactory.getLogger(MCQLocalResult.class);

    private final List<MasterTorsionAngleType> angleTypes;

    public MCQLocalResult(SelectionMatch matches,
            List<MasterTorsionAngleType> angleTypes) {
        super(matches);
        this.angleTypes = angleTypes;
    }

    public List<MasterTorsionAngleType> getAngles() {
        return angleTypes;
    }

    public List<String> getResidueLabels() {
        return selectionMatch.getResidueLabels();
    }

    public FragmentComparison asFragmentComparison() {
        List<ResidueComparison> residueComparisons = new ArrayList<>();
        for (FragmentMatch fragmentMatch : selectionMatch.getFragmentMatches()) {
            residueComparisons.addAll(fragmentMatch.getResidueComparisons());
        }
        return FragmentComparison.fromResidueComparisons(residueComparisons, angleTypes);
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
        filename += "-Local-Distance-";
        filename += getTargetName() + "-" + getModelName();
        filename += ".csv";
        return new File(filename);
    }

    @Override
    public SVGDocument visualize() {
        throw new IllegalArgumentException("Invalid usage, please use visualize() on FragmentMatch instances");
    }

    @Override
    public void visualize3D() {
        if (angleTypes.size() <= 1) {
            JOptionPane.showMessageDialog(null, "At least two torsion angle types are required for 3D visualization", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            for (FragmentMatch fragmentMatch : selectionMatch.getFragmentMatches()) {
                String name = fragmentMatch.toString();
                double[][] matrix = prepareMatrix(fragmentMatch);
                List<String> ticksX = prepareTicksX();
                List<String> ticksY = MCQLocalResult.prepareTicksY(fragmentMatch);
                NavigableMap<Double, String> valueTickZ = MCQLocalResult.prepareTicksZ();
                String labelX = "Angle type";
                String labelY = "Residue";
                String labelZ = "Distance";
                boolean showAllTicksX = true;
                boolean showAllTicksY = false;

                Surface3D surface3d = new Surface3D(name, matrix, ticksX, ticksY, valueTickZ, labelX, labelY, labelZ, showAllTicksX, showAllTicksY);
                AnalysisLauncher.open(surface3d);
            }
        } catch (Exception e) {
            String message = "Failed to visualize in 3D";
            MCQLocalResult.LOGGER.error(message, e);
            JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private double[][] prepareMatrix(FragmentMatch fragmentMatch) {
        List<ResidueComparison> residueComparisons = fragmentMatch.getResidueComparisons();
        double[][] matrix = new double[angleTypes.size()][];

        for (int i = 0; i < angleTypes.size(); i++) {
            MasterTorsionAngleType angleType = angleTypes.get(i);
            matrix[i] = new double[residueComparisons.size()];

            for (int j = 0; j < residueComparisons.size(); j++) {
                ResidueComparison residueComparison = residueComparisons.get(j);
                matrix[i][j] = residueComparison.getAngleDelta(angleType).getDelta().getRadians();
            }
        }

        return matrix;
    }

    private List<String> prepareTicksX() {
        List<String> ticksX = new ArrayList<>();
        for (MasterTorsionAngleType angleType : angleTypes) {
            ticksX.add(angleType.getExportName());
        }
        return ticksX;
    }

    private static List<String> prepareTicksY(FragmentMatch fragmentMatch) {
        return fragmentMatch.getResidueLabels();
    }

    protected static NavigableMap<Double, String> prepareTicksZ() {
        NavigableMap<Double, String> valueTickZ = new TreeMap<>();
        valueTickZ.put(0.0, "0");

        for (double radians = Math.PI / 12.0; radians <= Math.PI + 1e-3; radians += Math.PI / 12.0) {
            valueTickZ.put(radians, Long.toString(Math.round(Math.toDegrees(radians))) + Unicode.DEGREE);
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
        String[] columnNames = new String[angleTypes.size() + 1];
        columnNames[0] = isDisplay ? "" : null;

        for (int i = 0; i < angleTypes.size(); i++) {
            MasterTorsionAngleType angle = angleTypes.get(i);
            columnNames[i + 1] = isDisplay ? angle.getLongDisplayName() : angle.getExportName();
        }

        List<ResidueComparison> residueComparisons = new ArrayList<>();
        for (FragmentMatch fragmentMatch : selectionMatch.getFragmentMatches()) {
            residueComparisons.addAll(fragmentMatch.getResidueComparisons());
        }

        List<String> labels = getResidueLabels();
        String[][] data = new String[residueComparisons.size()][];

        for (int i = 0; i < residueComparisons.size(); i++) {
            ResidueComparison residueComparison = residueComparisons.get(i);
            data[i] = new String[angleTypes.size() + 1];
            data[i][0] = labels.get(i);

            for (int j = 0; j < angleTypes.size(); j++) {
                MasterTorsionAngleType angle = angleTypes.get(j);
                TorsionAngleDelta delta = residueComparison.getAngleDelta(angle);
                data[i][j + 1] = delta.toString(isDisplay);
            }
        }

        return new DefaultTableModel(data, columnNames);
    }
}
