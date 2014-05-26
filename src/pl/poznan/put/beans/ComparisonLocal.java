package pl.poznan.put.beans;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.math3.fraction.ProperFractionFormat;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.ResidueNumber;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.StructureImpl;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
import org.jfree.data.xy.DefaultXYDataset;
import org.jumpmind.symmetric.csv.CsvWriter;
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

import pl.poznan.put.beans.auxiliary.AngleDeltas;
import pl.poznan.put.comparison.MCQ;
import pl.poznan.put.comparison.TorsionLocalComparison;
import pl.poznan.put.gui.MainWindow;
import pl.poznan.put.gui.TorsionAxis;
import pl.poznan.put.helper.Constants;
import pl.poznan.put.helper.RGB;
import pl.poznan.put.helper.XMLSerializable;
import pl.poznan.put.interfaces.Exportable;
import pl.poznan.put.interfaces.Visualizable;
import pl.poznan.put.mcqgraphics.Matplotlib;
import pl.poznan.put.torsion.AngleDifference;
import pl.poznan.put.torsion.AngleType;
import pl.poznan.put.utility.StructureManager;

@XmlRootElement
public class ComparisonLocal extends XMLSerializable implements Exportable,
        Visualizable {
    private static final long serialVersionUID = 4652567875810044094L;

    public static ComparisonLocal newInstance(Chain c1, Chain c2,
            List<AngleType> angleNames) throws StructureException {
        Structure[] s = new Structure[] { new StructureImpl((Chain) c1.clone()), new StructureImpl(
                (Chain) c2.clone()) };

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
                TorsionLocalComparison.run(s[0], s[1], angleNames), title,
                angleNames);
    }

    public static ComparisonLocal newInstance(Structure left, Structure right,
            List<Chain> leftChains, List<Chain> rightChains,
            List<AngleType> list) throws StructureException {
        Structure l = new StructureImpl();
        for (Chain c : leftChains) {
            l.addChain((Chain) c.clone());
        }
        Structure r = new StructureImpl();
        for (Chain c : rightChains) {
            r.addChain((Chain) c.clone());
        }

        String title = StructureManager.getName(left) + ", "
                + StructureManager.getName(right);
        return ComparisonLocal.newInstance(
                TorsionLocalComparison.run(l, r, list), title, list);
    }

    private static ComparisonLocal newInstance(
            Map<AngleType, List<AngleDifference>> comparison, String title,
            List<AngleType> angleNames) {
        Set<AngleType> setAngles = new HashSet<>(angleNames);

        /*
         * get a union of all sets of residues for every angle
         */
        Set<ResidueNumber> setResidue = new TreeSet<>();
        for (Entry<AngleType, List<AngleDifference>> entry : comparison.entrySet()) {
            AngleType angleName = entry.getKey();
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
        MultiKeyMap<Object, Double> mapAngleResidueDelta = new MultiKeyMap<>();
        for (Entry<AngleType, List<AngleDifference>> entry : comparison.entrySet()) {
            AngleType angleName = entry.getKey();
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
        Map<AngleType, AngleDeltas> angles = new LinkedHashMap<>();
        for (AngleType angleName : comparison.keySet()) {
            if (!setAngles.contains(angleName)) {
                continue;
            }

            double[] deltas = new double[setResidue.size()];
            int j = 0;
            for (ResidueNumber residue : setResidue) {
                deltas[j] = mapAngleResidueDelta.get(angleName, residue);
                j++;
            }

            AngleDeltas angle = new AngleDeltas();
            angle.setName(angleName.getAngleDisplayName());
            angle.setDeltas(deltas);
            angles.put(angleName, angle);
        }

        List<String> ticks = new ArrayList<>();
        for (ResidueNumber residue : setResidue) {
            ticks.add(String.format("%s:%03d", residue.getChainId(),
                    residue.getSeqNum()));
        }
        return new ComparisonLocal(angles, Constants.colorsAsRGB(), ticks,
                title);
    }

    private Map<AngleType, AngleDeltas> angles;
    private List<RGB> colors;
    private List<String> ticks;
    private String title;

    public ComparisonLocal() {
    }

    private ComparisonLocal(Map<AngleType, AngleDeltas> angles, List<RGB> colors,
            List<String> ticks, String title) {
        super();
        this.angles = angles;
        this.colors = colors;
        this.ticks = ticks;
        this.title = title;
    }

    @Override
    public void export(File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
            CsvWriter csvWriter = new CsvWriter(writer, '\t');
            csvWriter.write("");
            List<AngleDeltas> angleArray = new ArrayList<>(getAngles().values());
            for (AngleDeltas angle : angleArray) {
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

    public Map<AngleType, AngleDeltas> getAngles() {
        return angles;
    }

    public Map<String, AngleDeltas> getAnglesNames() {
        Map<String, AngleDeltas> map = new LinkedHashMap<>();
        for (Entry<AngleType, AngleDeltas> entry : angles.entrySet()) {
            map.put(entry.getKey().getAngleName(), entry.getValue());
        }
        return map;
    }

    @XmlElement
    public void setAnglesNames(Map<String, AngleDeltas> map) {
        Map<AngleType, AngleDeltas> result = new LinkedHashMap<>();
        for (Entry<String, AngleDeltas> entry : map.entrySet()) {
            for (AngleType at : MCQ.USED_ANGLES) {
                if (at.getAngleName().equals(entry.getKey())) {
                    result.put(at, entry.getValue());
                    break;
                }
            }
        }

        angles = result;
    }

    public List<RGB> getColors() {
        return colors;
    }

    @XmlElement
    public void setColors(List<RGB> colors) {
        this.colors = colors;
    }

    public List<String> getTicks() {
        return ticks;
    }

    @XmlElement
    public void setTicks(List<String> ticks) {
        this.ticks = ticks;
    }

    public String getTitle() {
        return title;
    }

    @XmlElement
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public File suggestName() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
        String filename = sdf.format(new Date());
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

        List<AngleDeltas> angleArray = new ArrayList<>(getAngles().values());
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
            dataset.addSeries(angleArray.get(i).getName(),
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
        yAxis.setNumberFormatOverride(new NumberFormat() {
            private static final long serialVersionUID = 1L;

            @Override
            public StringBuffer format(double number, StringBuffer toAppendTo,
                    FieldPosition pos) {
                assert toAppendTo != null;

                if (number == 0) {
                    return toAppendTo.append("0");
                } else if (number == Math.PI) {
                    toAppendTo.append(Constants.UNICODE_PI);
                    toAppendTo.append(" = 180");
                    toAppendTo.append(Constants.UNICODE_DEGREE);
                    return toAppendTo;
                }
                format.format(number / Math.PI, toAppendTo, pos);
                toAppendTo.append(" * ");
                toAppendTo.append(Constants.UNICODE_PI);
                toAppendTo.append(" = ");
                toAppendTo.append(Math.round(Math.toDegrees(number)));
                toAppendTo.append(Constants.UNICODE_DEGREE);
                return toAppendTo;
            }

            @Override
            public StringBuffer format(long number, StringBuffer toAppendTo,
                    FieldPosition pos) {
                return format.format(number, toAppendTo, pos);
            }

            @Override
            public Number parse(String source, ParsePosition parsePosition) {
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
        final List<AngleDeltas> angleList = new ArrayList<>(getAngles().values());
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

        Shape surface = Builder.buildOrthonormal(new OrthonormalGrid(new Range(
                0, maxX - 1), maxX, new Range(0, maxY), maxY - 1),
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
        axeLayout.setZAxeLabel("Angular distance");

        ChartLauncher.openChart(chart);
    }

    @Override
    public void visualizeHighQuality() {
        StringBuilder builder = new StringBuilder();
        builder.append("[ ");
        for (AngleType angle : angles.keySet()) {
            builder.append("'");
            builder.append(angle.getAngleDisplayName());
            builder.append("', ");
        }
        builder.append(" ]");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("angles", builder.toString());

        URL resource = MainWindow.class.getResource("/pl/poznan/put/cs/"
                + "bioserver/external/MatplotlibLocal.xsl");
        Matplotlib.runXsltAndPython(resource, this, parameters);
    }
}
