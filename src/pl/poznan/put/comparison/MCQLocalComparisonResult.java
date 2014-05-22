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

import org.apache.commons.math3.fraction.ProperFractionFormat;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
import org.jfree.data.xy.DefaultXYDataset;

import pl.poznan.put.common.TorsionAngle;
import pl.poznan.put.gui.TorsionAxis;
import pl.poznan.put.helper.Constants;
import pl.poznan.put.matching.FragmentMatch;
import pl.poznan.put.matching.ResidueComparisonResult;
import pl.poznan.put.matching.SelectionMatch;
import pl.poznan.put.structure.CompactFragment;
import pl.poznan.put.structure.Residue;
import pl.poznan.put.utility.TabularExporter;
import pl.poznan.put.utility.TorsionAngleDelta;

public class MCQLocalComparisonResult extends LocalComparisonResult {
    private final List<TorsionAngle> angles;

    public MCQLocalComparisonResult(String nameLeft, String nameRight,
            SelectionMatch matches, List<TorsionAngle> angles) {
        super(nameLeft, nameRight, matches);
        this.angles = angles;
    }

    public List<TorsionAngle> getAngles() {
        return angles;
    }

    public List<String> getDataLabels() {
        List<String> result = new ArrayList<>();

        for (FragmentMatch fragment : matches.getFragmentMatches()) {
            CompactFragment bigger = fragment.getBiggerOnlyMatched();
            CompactFragment smaller = fragment.getSmaller();

            for (int i = 0; i < fragment.getSize(); i++) {
                Residue r1 = Residue.fromGroup(bigger.getGroup(i));
                Residue r2 = Residue.fromGroup(smaller.getGroup(i));
                result.add(r1 + " - " + r2);
            }
        }

        return result;
    }

    public List<ResidueComparisonResult> getDataRows() {
        List<ResidueComparisonResult> allResults = new ArrayList<>();

        for (FragmentMatch fragment : matches.getFragmentMatches()) {
            allResults.addAll(fragment.getBestResult().getResidueResults());
        }

        return allResults;
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
        filename += nameLeft + "-" + nameRight;
        filename += ".csv";
        return new File(filename);
    }

    @Override
    public void visualize() {
        List<String> ticks = getDataLabels();
        List<ResidueComparisonResult> rows = getDataRows();
        double[] x = new double[ticks.size()];

        for (int i = 0; i < ticks.size(); i++) {
            x[i] = i;
        }

        double[][] y = new double[angles.size()][];

        for (int i = 0; i < angles.size(); i++) {
            y[i] = new double[ticks.size()];
            TorsionAngle torsionAngle = angles.get(i);

            for (int j = 0; j < ticks.size(); j++) {
                ResidueComparisonResult result = rows.get(j);
                y[i][j] = result.getDelta(torsionAngle).getDelta();
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

        final ProperFractionFormat format = new ProperFractionFormat();
        yAxis.setNumberFormatOverride(new FractionAngleFormat(format));
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
        // TODO Auto-generated method stub
    }

    @Override
    public void visualizeHighQuality() {
        // TODO Auto-generated method stub
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

        List<String> labels = getDataLabels();
        List<ResidueComparisonResult> rows = getDataRows();
        String[][] data = new String[rows.size()][];

        for (int i = 0; i < rows.size(); i++) {
            data[i] = new String[angles.size() + 1];
            data[i][0] = labels.get(i);
            ResidueComparisonResult row = rows.get(i);

            for (int j = 0; j < angles.size(); j++) {
                TorsionAngle angle = angles.get(j);
                TorsionAngleDelta delta = row.getDelta(angle);

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
