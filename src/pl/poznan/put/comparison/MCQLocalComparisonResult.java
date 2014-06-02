package pl.poznan.put.comparison;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
import org.jfree.data.xy.DefaultXYDataset;

import pl.poznan.put.gui.TorsionAxis;
import pl.poznan.put.helper.Constants;
import pl.poznan.put.helper.FractionAngleFormat;
import pl.poznan.put.matching.FragmentComparison;
import pl.poznan.put.matching.FragmentMatch;
import pl.poznan.put.matching.ResidueComparison;
import pl.poznan.put.matching.SelectionMatch;
import pl.poznan.put.torsion.AngleDelta;
import pl.poznan.put.torsion.TorsionAngle;
import pl.poznan.put.utility.TabularExporter;

public class MCQLocalComparisonResult extends LocalComparisonResult {
    private final List<TorsionAngle> angles;

    public MCQLocalComparisonResult(SelectionMatch matches,
            List<TorsionAngle> angles) {
        super(matches);
        this.angles = angles;
    }

    public List<TorsionAngle> getAngles() {
        return angles;
    }

    public String[] getResidueLabels() {
        return matches.getResidueLabels();
    }

    public FragmentComparison asFragmentComparison() {
        List<ResidueComparison> residueComparisons = new ArrayList<>();

        for (int i = 0; i < matches.getSize(); i++) {
            FragmentMatch fragmentMatch = matches.getFragmentMatch(i);
            FragmentComparison fragmentComparison = fragmentMatch.getFragmentComparison();

            for (int j = 0; j < fragmentComparison.getSize(); j++) {
                residueComparisons.add(fragmentComparison.getResidueComparison(j));
            }
        }

        return FragmentComparison.fromResidueComparisons(residueComparisons,
                angles);
    }

    @Override
    public void export(File file) throws IOException {
        TabularExporter.export(this, file);
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
        String[] ticks = getResidueLabels();
        FragmentComparison rows = asFragmentComparison();
        double[] x = new double[ticks.length];

        for (int i = 0; i < ticks.length; i++) {
            x[i] = i;
        }

        double[][] y = new double[angles.size()][];

        for (int i = 0; i < angles.size(); i++) {
            y[i] = new double[ticks.length];
            TorsionAngle torsionAngle = angles.get(i);

            for (int j = 0; j < rows.getSize(); j++) {
                ResidueComparison result = rows.getResidueComparison(j);
                y[i][j] = result.getAngleDelta(torsionAngle).getDelta();
            }
        }

        DefaultXYDataset dataset = new DefaultXYDataset();
        DefaultXYItemRenderer renderer = new DefaultXYItemRenderer();

        for (int i = 0; i < y.length; i++) {
            dataset.addSeries(angles.get(i).toString(),
                    new double[][] { x, y[i] });
            renderer.setSeriesPaint(i, Constants.COLORS.get(i + 1));
        }

        NumberAxis xAxis = new TorsionAxis(ticks);
        xAxis.setLabel("ResID");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Angular distance");
        yAxis.setRange(0, Math.PI);
        yAxis.setTickUnit(new NumberTickUnit(Math.PI / 12.0));

        yAxis.setNumberFormatOverride(FractionAngleFormat.createInstance());
        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);

        JFrame frame = new JFrame();
        frame.setLayout(new BorderLayout());
        frame.add(new ChartPanel(new JFreeChart(plot)));

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension size = toolkit.getScreenSize();
        frame.setSize(size.width * 2 / 3, size.height * 2 / 3);
        frame.setLocation(size.width / 6, size.height / 6);
        frame.setTitle("MCQ4Structures: local distance plot");
        frame.setVisible(true);
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
        String[] columnNames = new String[angles.size() + 1];
        columnNames[0] = isDisplay ? "" : null;

        for (int i = 0; i < angles.size(); i++) {
            TorsionAngle angle = angles.get(i);
            columnNames[i + 1] = isDisplay ? angle.getDisplayName()
                    : angle.toString();
        }

        String[] labels = getResidueLabels();
        FragmentComparison rows = asFragmentComparison();
        String[][] data = new String[rows.getSize()][];

        for (int i = 0; i < rows.getSize(); i++) {
            data[i] = new String[angles.size() + 1];
            data[i][0] = labels[i];
            ResidueComparison row = rows.getResidueComparison(i);

            for (int j = 0; j < angles.size(); j++) {
                TorsionAngle angle = angles.get(j);
                AngleDelta delta = row.getAngleDelta(angle);

                if (delta == null) {
                    data[i][j + 1] = null;
                } else {
                    data[i][j + 1] = isDisplay ? delta.toDisplayString()
                            : delta.toExportString();
                }
            }
        }

        return new DefaultTableModel(data, columnNames);
    }
}
