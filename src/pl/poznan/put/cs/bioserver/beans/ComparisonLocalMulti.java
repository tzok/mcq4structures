package pl.poznan.put.cs.bioserver.beans;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.StructureException;
import org.jzy3d.chart.Chart;
import org.jzy3d.chart.ChartLauncher;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapRainbow;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.builder.Builder;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import org.jzy3d.plot3d.rendering.scene.Graph;

import pl.poznan.put.cs.bioserver.beans.auxiliary.Angle;
import pl.poznan.put.cs.bioserver.external.Matplotlib;
import pl.poznan.put.cs.bioserver.gui.DialogColorbar;
import pl.poznan.put.cs.bioserver.gui.MainWindow;
import pl.poznan.put.cs.bioserver.helper.Exportable;
import pl.poznan.put.cs.bioserver.helper.Helper;
import pl.poznan.put.cs.bioserver.helper.InvalidInputException;
import pl.poznan.put.cs.bioserver.helper.Visualizable;

import com.csvreader.CsvWriter;

@XmlRootElement
public class ComparisonLocalMulti extends XMLSerializable implements Exportable, Visualizable {
    private static final long serialVersionUID = -6549267536864184480L;

    public static ComparisonLocalMulti newInstance(List<Chain> chains, Chain reference,
            List<String> angleNames) throws StructureException, InvalidInputException {
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
            list.add(ComparisonLocal.newInstance(reference, chain, angleNames));
        }

        ComparisonLocalMulti instance = new ComparisonLocalMulti();
        instance.setResults(list);
        return instance;
    }

    List<ComparisonLocal> results;

    @Override
    public void export(File file) throws IOException {
        double[][] deltas = new double[results.size()][];
        for (int i = 0; i < results.size(); i++) {
            ComparisonLocal comparisonLocal = results.get(i);
            Map<String, Angle> angles = comparisonLocal.getAngles();
            Angle average = angles.get("AVERAGE");
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

    public List<ComparisonLocal> getResults() {
        return results;
    }

    @XmlElement
    public void setResults(List<ComparisonLocal> results) {
        this.results = results;
    }

    @Override
    public File suggestName() {
        String filename = Helper.getExportPrefix();
        filename += "-Local-Distance-Multi-";
        for (ComparisonLocal local : results) {
            filename += local.getTitle().split(", ")[1];
        }
        filename += ".csv";
        return new File(filename);
    }

    @Override
    public void visualize() {
        DialogColorbar dialogColorbar = new DialogColorbar(this);
        dialogColorbar.setVisible(true);
    }

    @Override
    public void visualize3D() {
        List<Coord3d> coordinates = new ArrayList<>();
        int i = 0;
        for (ComparisonLocal local : results) {
            double[] deltas = local.getAngles().get("AVERAGE").getDeltas();
            for (int j = 0; j < deltas.length; j++) {
                coordinates.add(new Coord3d(i, j, deltas[j]));
            }
            i++;
        }

        Shape surface = Builder.buildDelaunay(coordinates);
        surface.setColorMapper(new ColorMapper(new ColorMapRainbow(),
                surface.getBounds().getZmin(), surface.getBounds().getZmax(), new Color(1, 1, 1,
                        .5f)));
        surface.setFaceDisplayed(true);
        surface.setWireframeDisplayed(false);
        surface.setWireframeColor(Color.BLACK);

        Chart chart = new Chart(Quality.Nicest);
        Graph graph = chart.getScene().getGraph();
        graph.add(surface);
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

        URL resource = MainWindow.class.getResource("/pl/poznan/put/cs/"
                + "bioserver/external/MatplotlibLocalMulti.xsl");
        Matplotlib.runXsltAndPython(resource, results.get(0), parameters);
    }
}
