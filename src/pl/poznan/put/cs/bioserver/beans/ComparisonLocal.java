package pl.poznan.put.cs.bioserver.beans;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.math3.fraction.ProperFractionFormat;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.ResidueNumber;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.StructureImpl;
import org.eclipse.jdt.annotation.Nullable;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
import org.jfree.data.xy.DefaultXYDataset;
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
import org.jzy3d.plot3d.primitives.axes.layout.providers.SmartTickProvider;
import org.jzy3d.plot3d.primitives.axes.layout.renderers.TickLabelMap;
import org.jzy3d.plot3d.rendering.canvas.Quality;

import pl.poznan.put.cs.bioserver.beans.auxiliary.Angle;
import pl.poznan.put.cs.bioserver.beans.auxiliary.RGB;
import pl.poznan.put.cs.bioserver.comparison.TorsionLocalComparison;
import pl.poznan.put.cs.bioserver.external.Matplotlib;
import pl.poznan.put.cs.bioserver.gui.MainWindow;
import pl.poznan.put.cs.bioserver.gui.TorsionAxis;
import pl.poznan.put.cs.bioserver.helper.Colors;
import pl.poznan.put.cs.bioserver.helper.Exportable;
import pl.poznan.put.cs.bioserver.helper.Helper;
import pl.poznan.put.cs.bioserver.helper.StructureManager;
import pl.poznan.put.cs.bioserver.helper.Visualizable;
import pl.poznan.put.cs.bioserver.torsion.AngleDifference;

import com.csvreader.CsvWriter;

@XmlRootElement
public class ComparisonLocal extends XMLSerializable implements Exportable,
        Visualizable {
    private static final String UNICODE_DEGREE = "\u00B0";
    private static final String UNICODE_PI = "\u03C0";
    private static final long serialVersionUID = 4652567875810044094L;

    public static ComparisonLocal newInstance(Chain c1, Chain c2,
            List<String> angleNames) throws StructureException {
        Structure[] s =
                new Structure[] { new StructureImpl((Chain) c1.clone()),
                        new StructureImpl((Chain) c2.clone()) };

        StringBuilder builder = new StringBuilder();
        builder.append(StructureManager.getName(c1.getParent()));
        builder.append('.');
        builder.append(c1.getChainID());
        builder.append(", ");
        builder.append(StructureManager.getName(c2.getParent()));
        builder.append('.');
        builder.append(c2.getChainID());
        String title = builder.toString();

        return ComparisonLocal.newInstance(
                TorsionLocalComparison.compare(s[0], s[1], angleNames), title,
                angleNames);
    }

    public static ComparisonLocal newInstance(Structure left, Structure right,
            List<Chain> leftChains, List<Chain> rightChains,
            List<String> angleNames) throws StructureException {
        Structure l = new StructureImpl();
        for (Chain c : leftChains) {
            l.addChain((Chain) c.clone());
        }
        Structure r = new StructureImpl();
        for (Chain c : rightChains) {
            r.addChain((Chain) c.clone());
        }

        String title =
                StructureManager.getName(left) + ", "
                        + StructureManager.getName(right);
        return ComparisonLocal.newInstance(
                TorsionLocalComparison.compare(l, r, angleNames), title,
                angleNames);
    }

    private static ComparisonLocal newInstance(
            Map<String, List<AngleDifference>> comparison, String title,
            List<String> angleNames) {
        Set<String> setAngles = new HashSet<>(angleNames);

        /*
         * get a union of all sets of residues for every angle
         */
        Set<ResidueNumber> setResidue = new TreeSet<>();
        for (Entry<String, List<AngleDifference>> entry : comparison.entrySet()) {
            String angleName = entry.getKey();
            if (!setAngles.contains(angleName)) {
                continue;
            }

            for (AngleDifference difference : entry.getValue()) {
                ResidueNumber residue = difference.getResidue();
                setResidue.add(residue);
            }
        }

        /*
         * fill a "map[angle, residue] = angle" with values (NaN if missing)
         */
        MultiKeyMap mapAngleResidueDelta = new MultiKeyMap();
        for (Entry<String, List<AngleDifference>> entry : comparison.entrySet()) {
            String angleName = entry.getKey();
            if (!setAngles.contains(angleName)) {
                continue;
            }

            for (ResidueNumber residue : setResidue) {
                mapAngleResidueDelta.put(angleName, residue, Double.NaN);
            }
            for (AngleDifference delta : entry.getValue()) {
                ResidueNumber residue = delta.getResidue();
                double difference = delta.getDifference();
                mapAngleResidueDelta.put(angleName, residue, difference);
            }
        }

        /*
         * read map data into desired format
         */
        Map<String, Angle> angles = new LinkedHashMap<>();
        for (String angleName : comparison.keySet()) {
            if (!setAngles.contains(angleName)) {
                continue;
            }

            double[] deltas = new double[setResidue.size()];
            int j = 0;
            for (ResidueNumber residue : setResidue) {
                deltas[j] =
                        (double) mapAngleResidueDelta.get(angleName, residue);
                j++;
            }

            Angle angle = new Angle();
            angle.setName(angleName);
            angle.setDeltas(deltas);
            angles.put(angleName, angle);
        }

        List<String> ticks = new ArrayList<>();
        for (ResidueNumber residue : setResidue) {
            ticks.add(String.format("%s:%03d", residue.getChainId(),
                    residue.getSeqNum()));
        }

        ComparisonLocal result = new ComparisonLocal();
        result.setAngles(angles);
        result.setTicks(ticks);
        result.setTitle(title);
        result.colors = Colors.toRGB();
        return result;
    }

    Map<String, Angle> angles;
    List<RGB> colors;
    List<String> ticks;
    String title;

    @Override
    public void export(File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
            CsvWriter csvWriter = new CsvWriter(writer, '\t');
            csvWriter.write("");
            List<Angle> angleArray = getAngleList();
            for (Angle angle : angleArray) {
                csvWriter.write(angle.getName());
            }
            csvWriter.endRecord();

            for (int i = 0; i < angles.size(); i++) {
                csvWriter.write(ticks.get(i));
                double[] deltas = angleArray.get(i).getDeltas();
                for (int j = 0; j < deltas.length - 1; j++) {
                    csvWriter.write(Double.toString(deltas[j]));
                }
                csvWriter.endRecord();
            }
        }
    }

    public List<Angle> getAngleList() {
        return new ArrayList<>(angles.values());
    }

    public Map<String, Angle> getAngles() {
        return angles;
    }

    public List<RGB> getColors() {
        return colors;
    }

    public List<String> getTicks() {
        return ticks;
    }

    public String getTitle() {
        return title;
    }

    public void setAngles(Map<String, Angle> angles) {
        this.angles = angles;
    }

    @XmlElementWrapper(name = "colors")
    @XmlElement(name = "item")
    public void setColors(List<RGB> colors) {
        this.colors = colors;
    }

    @XmlElementWrapper(name = "ticks")
    @XmlElement(name = "item")
    public void setTicks(List<String> ticks) {
        this.ticks = ticks;
    }

    @XmlElement
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public File suggestName() {
        String filename = Helper.getExportPrefix();
        filename += "-Local-Distance-";
        filename += title.replace(", ", "-");
        filename += ".csv";
        return new File(filename);
    }

    @Override
    public void visualize() {
        double[] x = new double[ticks.size()];
        for (int i = 0; i < ticks.size(); i++) {
            x[i] = i;
        }

        List<Angle> angleArray = getAngleList();
        double[][] y = new double[angleArray.size()][];
        for (int i = 0; i < angleArray.size(); i++) {
            y[i] = new double[ticks.size()];
            for (int j = 0; j < ticks.size(); j++) {
                y[i][j] = angleArray.get(i).getDeltas()[j];
            }
        }

        DefaultXYDataset dataset = new DefaultXYDataset();
        DefaultXYItemRenderer renderer = new DefaultXYItemRenderer();
        for (int i = 0; i < y.length; i++) {
            dataset.addSeries(angleArray.get(i).getName(), new double[][] { x,
                    y[i] });
            renderer.setSeriesPaint(i, Colors.ALL.get(i + 1));
        }

        NumberAxis xAxis = new TorsionAxis(ticks);
        xAxis.setLabel("ResID");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Angular distance");
        yAxis.setRange(0, Math.PI);
        yAxis.setTickUnit(new NumberTickUnit(Math.PI / 12.0));

        final ProperFractionFormat format = new ProperFractionFormat();
        yAxis.setNumberFormatOverride(new NumberFormat() {
            private static final long serialVersionUID = 1L;

            @Override
            public StringBuffer format(double number,
                    @Nullable StringBuffer toAppendTo,
                    @Nullable FieldPosition pos) {
                assert toAppendTo != null;

                if (number == 0) {
                    return toAppendTo.append("0");
                } else if (number == Math.PI) {
                    toAppendTo.append(ComparisonLocal.UNICODE_PI);
                    toAppendTo.append(" = 180");
                    toAppendTo.append(ComparisonLocal.UNICODE_DEGREE);
                    return toAppendTo;
                }
                format.format(number / Math.PI, toAppendTo, pos);
                toAppendTo.append(" * ");
                toAppendTo.append(ComparisonLocal.UNICODE_PI);
                toAppendTo.append(" = ");
                toAppendTo.append(Math.round(Math.toDegrees(number)));
                toAppendTo.append(ComparisonLocal.UNICODE_DEGREE);
                return toAppendTo;
            }

            @Override
            public StringBuffer format(long number,
                    @Nullable StringBuffer toAppendTo,
                    @Nullable FieldPosition pos) {
                return format.format(number, toAppendTo, pos);
            }

            @Override
            public Number parse(@Nullable String source,
                    @Nullable ParsePosition parsePosition) {
                return format.parse(source, parsePosition);
            }
        });
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
        final List<Angle> angleList = getAngleList();
        final int maxX = angleList.size();
        final int maxY = ticks.size();

        if (maxX <= 1) {
            JOptionPane.showMessageDialog(null,
                    "3D plot requires a comparison based on at least "
                            + "two angles", "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        TickLabelMap mapX = new TickLabelMap();
        for (int i = 0; i < angleList.size(); i++) {
            mapX.register(i, angleList.get(i).getName());
        }
        TickLabelMap mapY = new TickLabelMap();
        for (int i = 0; i < maxY; i++) {
            mapY.register(i, ticks.get(i));
        }

        Shape surface =
                Builder.buildOrthonormal(new OrthonormalGrid(new Range(0,
                        maxX - 1), maxX, new Range(0, maxY), maxY - 1),
                        new Mapper() {
                            @Override
                            public double f(double x, double y) {
                                int i = (int) Math.round(x);
                                int j = (int) Math.round(y);

                                i = Math.max(Math.min(i, maxX - 1), 0);
                                j = Math.max(Math.min(j, maxY - 1), 0);
                                return angleList.get(i).getDeltas()[j];
                            }
                        });

        surface.setColorMapper(new ColorMapper(new ColorMapRainbow(), 0,
                (float) Math.PI, new Color(1, 1, 1, .5f)));
        surface.setFaceDisplayed(true);
        surface.setWireframeDisplayed(false);

        Chart chart = new Chart(Quality.Nicest);
        chart.getScene().getGraph().add(surface);

        IAxeLayout axeLayout = chart.getAxeLayout();
        axeLayout.setXTickProvider(new RegularTickProvider(maxX));
        axeLayout.setXTickRenderer(mapX);
        axeLayout.setYTickProvider(new SmartTickProvider(maxY));
        axeLayout.setYTickRenderer(mapY);
        axeLayout.setZAxeLabel("Distance [rad]");

        ChartLauncher.openChart(chart);
    }

    @Override
    public void visualizeHighQuality() {
        StringBuilder builder = new StringBuilder();
        builder.append("[ ");
        for (String angle : angles.keySet()) {
            builder.append("'");
            builder.append(angle);
            builder.append("', ");
        }
        builder.append(" ]");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("angles", builder.toString());

        URL resource =
                MainWindow.class.getResource("/pl/poznan/put/cs/"
                        + "bioserver/external/MatplotlibLocal.xsl");
        Matplotlib.runXsltAndPython(resource, this, parameters);
    }
}
