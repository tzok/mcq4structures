package pl.poznan.put.comparison;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import pl.poznan.put.gui.LocalComparisonFrame;
import pl.poznan.put.matching.FragmentComparison;
import pl.poznan.put.matching.FragmentMatch;
import pl.poznan.put.matching.ResidueComparison;
import pl.poznan.put.matching.SelectionMatch;
import pl.poznan.put.torsion.TorsionAngleDelta;
import pl.poznan.put.torsion.type.MasterTorsionAngleType;
import pl.poznan.put.types.ExportFormat;
import pl.poznan.put.utility.TabularExporter;

public class MCQLocalComparisonResult extends LocalComparisonResult {
    private final List<MasterTorsionAngleType> angleTypes;

    public MCQLocalComparisonResult(SelectionMatch matches,
            List<MasterTorsionAngleType> angleTypes) {
        super(matches);
        this.angleTypes = angleTypes;
    }

    public List<MasterTorsionAngleType> getAngles() {
        return angleTypes;
    }

    public String[] getResidueLabels() {
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
    public void export(File file) throws IOException {
        TabularExporter.export(this, file);
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
    public void visualize() {
        LocalComparisonFrame comparisonFrame = new LocalComparisonFrame(selectionMatch);
        comparisonFrame.setVisible(true);
    }

    @Override
    public void visualize3D() {
        // TODO: required major refactoring
        // // final List<AngleDeltas> angleList = new ArrayList<>(
        // // getAngles().values());
        // List<String> ticks = getDataLabels();
        // final int maxX = angles.size();
        // final int maxY = ticks.size();
        //
        // if (maxX <= 1) {
        // JOptionPane.showMessageDialog(null,
        // "3D plot requires a comparison based on at least "
        // + "two angles", "Warning",
        // JOptionPane.WARNING_MESSAGE);
        // return;
        // }
        //
        // TickLabelMap mapX = new TickLabelMap();
        // for (int i = 0; i < angleList.size(); i++) {
        // mapX.register(i, angleList.get(i).getName());
        // }
        // TickLabelMap mapY = new TickLabelMap();
        // for (int i = 0; i < maxY; i++) {
        // mapY.register(i, ticks.get(i));
        // }
        //
        // Shape surface = Builder.buildOrthonormal(new OrthonormalGrid(new
        // Range(
        // 0, maxX - 1), maxX, new Range(0, maxY), maxY - 1),
        // new Mapper() {
        // @Override
        // public double f(double x, double y) {
        // int i = (int) Math.round(x);
        // int j = (int) Math.round(y);
        //
        // i = Math.max(Math.min(i, maxX - 1), 0);
        // j = Math.max(Math.min(j, maxY - 1), 0);
        // return angleList.get(i).getDeltas()[j];
        // }
        // });
        //
        // surface.setColorMapper(new ColorMapper(new ColorMapRainbow(), 0,
        // (float) Math.PI, new Color(1, 1, 1, .5f)));
        // surface.setFaceDisplayed(true);
        // surface.setWireframeDisplayed(false);
        //
        // Chart chart = new Chart(Quality.Nicest);
        // chart.getScene().getGraph().add(surface);
        //
        // IAxeLayout axeLayout = chart.getAxeLayout();
        // axeLayout.setXTickProvider(new RegularTickProvider(maxX));
        // axeLayout.setXTickRenderer(mapX);
        // axeLayout.setYTickProvider(new SmartTickProvider(maxY));
        // axeLayout.setYTickRenderer(mapY);
        // axeLayout.setZAxeLabel("Angular distance");
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

        String[] labels = getResidueLabels();
        String[][] data = new String[residueComparisons.size()][];

        for (int i = 0; i < residueComparisons.size(); i++) {
            ResidueComparison residueComparison = residueComparisons.get(i);
            data[i] = new String[angleTypes.size() + 1];
            data[i][0] = labels[i];

            for (int j = 0; j < angleTypes.size(); j++) {
                MasterTorsionAngleType angle = angleTypes.get(j);
                TorsionAngleDelta delta = residueComparison.getAngleDelta(angle);
                data[i][j + 1] = delta.toString(isDisplay);
            }
        }

        return new DefaultTableModel(data, columnNames);
    }
}
