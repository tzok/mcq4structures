package pl.poznan.put.cs.bioserver.beans;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.tuple.Pair;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.ResidueNumber;
import org.biojava.bio.structure.StructureException;
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
import pl.poznan.put.cs.bioserver.external.Matplotlib;
import pl.poznan.put.cs.bioserver.gui.DialogColorbar;
import pl.poznan.put.cs.bioserver.gui.MainWindow;
import pl.poznan.put.cs.bioserver.helper.Exportable;
import pl.poznan.put.cs.bioserver.helper.Helper;
import pl.poznan.put.cs.bioserver.helper.InvalidInputException;
import pl.poznan.put.cs.bioserver.helper.Visualizable;
import pl.poznan.put.cs.bioserver.torsion.AngleType;

import com.csvreader.CsvWriter;

@XmlRootElement
public class ComparisonLocalMulti extends XMLSerializable implements
        Exportable, Visualizable {
    private static final long serialVersionUID = -6549267536864184480L;

    public static ComparisonLocalMulti newInstance(List<Chain> chains,
            Chain reference, AngleType angleType) throws StructureException,
            InvalidInputException {
        // sanity check
        if (chains.size() < 2) {
            throw new InvalidInputException("Incorrect/empty input data");
        }

        // sanity check
        boolean isRNA = Helper.isNucleicAcid(reference);
        int size = Helper.countResidues(reference, isRNA);
        for (Chain chain : chains) {
            if (Helper.countResidues(chain, isRNA) != size) {
                throw new InvalidInputException("Chains have different sizes");
            }
        }

        List<ComparisonLocal> list = new ArrayList<>();
        for (Chain chain : chains) {
            if (reference.equals(chain)) {
                continue;
            }
            list.add(ComparisonLocal.newInstance(reference, chain,
                    Arrays.asList(new AngleType[] { angleType })));
        }

        return new ComparisonLocalMulti(list, angleType,
                Helper.getSequenceFasta(reference));
    }

    List<ComparisonLocal> results = new ArrayList<>();
    AngleType angleType;
    Pair<String, List<ResidueNumber>> referenceSequence;

    private ComparisonLocalMulti(List<ComparisonLocal> results,
            AngleType angleType, Pair<String, List<ResidueNumber>> sequence) {
        this.results = results;
        this.angleType = angleType;
        this.referenceSequence = sequence;
    }

    public List<ComparisonLocal> getResults() {
        return results;
    }

    @XmlElement
    public void setResults(List<ComparisonLocal> results) {
        this.results = results;
    }

    public AngleType getAngleType() {
        return angleType;
    }

    public void setAngleType(AngleType angleType) {
        this.angleType = angleType;
    }

    public Pair<String, List<ResidueNumber>> getReferenceSequence() {
        return referenceSequence;
    }

    @XmlElement
    public void setReferenceSequence(
            Pair<String, List<ResidueNumber>> referenceSequence) {
        this.referenceSequence = referenceSequence;
    }

    @Override
    public void export(File file) throws IOException {
        double[][] deltas = new double[results.size()][];
        for (int i = 0; i < results.size(); i++) {
            ComparisonLocal comparisonLocal = results.get(i);
            Map<AngleType, Angle> angles = comparisonLocal.getAngles();
            Angle average = angles.get(angleType);
            assert average != null;
            deltas[i] = average.getDeltas();
        }

        try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
            CsvWriter csvWriter = new CsvWriter(writer, '\t');

            csvWriter.write("");
            for (ComparisonLocal local : results) {
                csvWriter.write(local.getTitle());
            }
            csvWriter.endRecord();

            if (results.size() > 0) {
                List<String> ticks = results.get(0).getTicks();
                for (int i = 0; i < ticks.size(); i++) {
                    csvWriter.write(ticks.get(i));
                    for (int j = 0; j < results.size(); j++) {
                        csvWriter.write(Double.toString(deltas[j][i]));
                    }
                    csvWriter.endRecord();
                }
            }
        }
    }

    @Override
    public File suggestName() {
        StringBuilder builder = new StringBuilder(Helper.getExportPrefix());
        builder.append("-Local-Distance-Multi-");
        for (ComparisonLocal local : results) {
            builder.append(local.getTitle().split(", ")[1]);
        }
        builder.append(".csv");
        return new File(builder.toString());
    }

    @Override
    public void visualize() {
        DialogColorbar dialogColorbar = new DialogColorbar(this);
        dialogColorbar.setVisible(true);
    }

    @Override
    public void visualize3D() {
        final int maxX = results.size();
        if (maxX <= 1) {
            JOptionPane.showMessageDialog(null,
                    "3D plot requires a comparison based on at least "
                            + "two structures", "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        ComparisonLocal reference = results.get(0);
        final int maxY = reference.ticks.size();

        Shape surface =
                Builder.buildOrthonormal(new OrthonormalGrid(new Range(0,
                        maxX - 1), maxX, new Range(0, maxY - 1), maxY),
                        new Mapper() {
                            @Override
                            public double f(double x, double y) {
                                int i = (int) Math.round(x);
                                int j = (int) Math.round(y);

                                i = Math.max(Math.min(i, maxX - 1), 0);
                                j = Math.max(Math.min(j, maxY - 1), 0);
                                // FIXME
                                return results.get(i).angles.get(
                                        angleType.getAngleName()).getDeltas()[j];
                            }
                        });

        surface.setColorMapper(new ColorMapper(new ColorMapRainbow(), 0,
                (float) Math.PI, new Color(1, 1, 1, .5f)));
        surface.setFaceDisplayed(true);
        surface.setWireframeDisplayed(false);

        TickLabelMap mapX = new TickLabelMap();
        for (int i = 0; i < maxX; i++) {
            mapX.register(i, results.get(i).title);
        }
        TickLabelMap mapY = new TickLabelMap();
        for (int i = 0; i < maxY; i++) {
            mapY.register(i, reference.ticks.get(i));
        }

        Chart chart = new Chart(Quality.Nicest);
        chart.getScene().getGraph().add(surface);

        IAxeLayout axeLayout = chart.getAxeLayout();
        axeLayout.setXTickProvider(new RegularTickProvider(maxX));
        axeLayout.setXTickRenderer(mapX);
        axeLayout.setYTickProvider(new SmartTickProvider(maxY));
        axeLayout.setYTickRenderer(mapY);

        ChartLauncher.openChart(chart);
    }

    @Override
    public void visualizeHighQuality() {
        // TODO: use different parameters
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("cmap", "matplotlib.cm.RdYlGn");
        parameters.put("interpolation", "none");
        parameters.put("min", "0");
        parameters.put("max", "3.1415");

        URL resource =
                MainWindow.class.getResource("/pl/poznan/put/cs/"
                        + "bioserver/external/MatplotlibLocalMulti.xsl");
        Matplotlib.runXsltAndPython(resource, this, parameters);
    }
}
