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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JFrame;
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
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
import org.jfree.data.xy.DefaultXYDataset;

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
    private static final long serialVersionUID = 4652567875810044094L;

    public static ComparisonLocal newInstance(Chain c1, Chain c2,
            Collection<String> angleNames) throws StructureException {
        Structure[] s = new Structure[] {
                new StructureImpl((Chain) c1.clone()),
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
    public static ComparisonLocal newInstance(Structure[] structures,
            Chain[][] chains, Collection<String> angleNames)
            throws StructureException {
        Structure[] s = new Structure[2];
        for (int i = 0; i < 2; i++) {
            s[i] = new StructureImpl();
            s[i].setChains(Arrays.asList(chains[i]));
        }

        String[] names = StructureManager.getNames(structures);
        String title = names[0] + ", " + names[1];

        return ComparisonLocal.newInstance(
                TorsionLocalComparison.compare(s[0], s[1], angleNames), title,
                angleNames);
    }
    private static ComparisonLocal newInstance(
            Map<String, List<AngleDifference>> comparison, String title,
            Collection<String> angleNames) {
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
        for (String angleName : comparison.keySet()) {
            if (!setAngles.contains(angleName)) {
                continue;
            }

            for (ResidueNumber residue : setResidue) {
                mapAngleResidueDelta.put(angleName, residue, Double.NaN);
            }
            for (AngleDifference delta : comparison.get(angleName)) {
                ResidueNumber residue = delta.getResidue();
                double difference = delta.getDifference();
                mapAngleResidueDelta.put(angleName, residue, difference);
            }
        }

        /*
         * read map data into desired format
         */
        Map<String, Angle> angles = new LinkedHashMap<>();
        int i = 0;
        for (String angleName : comparison.keySet()) {
            if (!setAngles.contains(angleName)) {
                continue;
            }

            double[] deltas = new double[setResidue.size()];
            int j = 0;
            for (ResidueNumber residue : setResidue) {
                deltas[j] = (double) mapAngleResidueDelta.get(angleName,
                        residue);
                j++;
            }

            Angle angle = new Angle();
            angle.setName(angleName);
            angle.setDeltas(deltas);
            angles.put(angleName, angle);
            i++;
        }

        String[] ticks = new String[setResidue.size()];
        i = 0;
        for (ResidueNumber residue : setResidue) {
            ticks[i] = String.format("%s:%03d", residue.getChainId(),
                    residue.getSeqNum());
            i++;
        }

        ComparisonLocal result = new ComparisonLocal();
        result.setAngles(angles);
        result.setTicks(ticks);
        result.setTitle(title);
        result.colors = Colors.toRGB();
        return result;
    }
    Map<String, Angle> angles;

    String[] ticks;

    RGB[] colors;

    String title;

    @Override
    public void export(File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
            CsvWriter csvWriter = new CsvWriter(writer, '\t');
            csvWriter.write("");
            Angle[] angleArray = getAngleArray();
            for (Angle angle : angleArray) {
                csvWriter.write(angle.getName());
            }
            csvWriter.endRecord();

            for (int i = 0; i < angles.size(); i++) {
                csvWriter.write(ticks[i]);
                double[] deltas = angleArray[i].getDeltas();
                for (int j = 0; j < deltas.length - 1; j++) {
                    csvWriter.write(Double.toString(deltas[j]));
                }
                csvWriter.endRecord();
            }
        }
    }

    public Angle[] getAngleArray() {
        Angle[] angleArray = new Angle[angles.size()];
        int i = 0;
        for (Entry<String, Angle> entry : angles.entrySet()) {
            angleArray[i] = entry.getValue();
            i++;
        }
        return angleArray;
    }

    public Map<String, Angle> getAngles() {
        return angles;
    }

    public RGB[] getColors() {
        return colors;
    }

    public String[] getTicks() {
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
    public void setColors(RGB[] colors) {
        this.colors = colors;
    }

    @XmlElementWrapper(name = "ticks")
    @XmlElement(name = "item")
    public void setTicks(String[] ticks) {
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
        double[] x = new double[ticks.length];
        for (int i = 0; i < ticks.length; i++) {
            x[i] = i;
        }

        Angle[] angleArray = getAngleArray();
        double[][] y = new double[angleArray.length][];
        for (int i = 0; i < angleArray.length; i++) {
            y[i] = new double[ticks.length];
            for (int j = 0; j < ticks.length; j++) {
                y[i][j] = angleArray[i].getDeltas()[j];
            }
        }

        DefaultXYDataset dataset = new DefaultXYDataset();
        DefaultXYItemRenderer renderer = new DefaultXYItemRenderer();
        for (int i = 0; i < y.length; i++) {
            dataset.addSeries(angleArray[i].getName(),
                    new double[][] { x, y[i] });
            renderer.setSeriesPaint(i, Colors.ALL[i + 1]);
        }

        NumberAxis xAxis = new TorsionAxis(ticks);
        xAxis.setLabel("Residue");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Distance [rad]");
        yAxis.setRange(0, Math.PI);
        yAxis.setTickUnit(new NumberTickUnit(Math.PI / 12.0));

        final ProperFractionFormat format = new ProperFractionFormat();
        yAxis.setNumberFormatOverride(new NumberFormat() {
            private static final long serialVersionUID = 1L;

            @Override
            public StringBuffer format(double number, StringBuffer toAppendTo,
                    FieldPosition pos) {
                if (number == 0) {
                    return toAppendTo.append("0");
                } else if (number == Math.PI) {
                    return toAppendTo.append("\u03C0");
                }
                return format.format(number / Math.PI, toAppendTo, pos).append(
                        " * \u03C0");
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

        URL resource = MainWindow.class.getResource("/pl/poznan/put/cs/"
                + "bioserver/external/MatplotlibLocal.xsl");
        Matplotlib.runXsltAndPython(resource, this, parameters);
    }
}
