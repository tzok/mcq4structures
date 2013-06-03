package pl.poznan.put.cs.bioserver.beans;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.StructureException;

import pl.poznan.put.cs.bioserver.beans.auxiliary.Angle;
import pl.poznan.put.cs.bioserver.gui.DialogColorbar;
import pl.poznan.put.cs.bioserver.helper.Exportable;
import pl.poznan.put.cs.bioserver.helper.Helper;
import pl.poznan.put.cs.bioserver.helper.Visualizable;

import com.csvreader.CsvWriter;

@XmlRootElement
public class ComparisonLocalMulti extends XMLSerializable implements
        Exportable, Visualizable {
    private static final long serialVersionUID = -6549267536864184480L;

    List<ComparisonLocal> results;

    public List<ComparisonLocal> getResults() {
        return results;
    }

    @XmlElement
    public void setResults(List<ComparisonLocal> results) {
        this.results = results;
    }

    public static ComparisonLocalMulti newInstance(Chain[] chains,
            Chain reference, String[] angleNames) throws StructureException {
        List<ComparisonLocal> list = new ArrayList<>();
        for (int i = 0; i < chains.length; i++) {
            if (reference.equals(chains[i])) {
                continue;
            }
            list.add(ComparisonLocal.newInstance(reference, chains[i],
                    angleNames));
        }

        ComparisonLocalMulti instance = new ComparisonLocalMulti();
        instance.setResults(list);
        return instance;
    }

    @Override
    public void visualize() {
        DialogColorbar dialogColorbar = new DialogColorbar(this);
        dialogColorbar.setVisible(true);
    }

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
                String[] ticks = results.get(0).getTicks();
                for (int i = 0; i < ticks.length; i++) {
                    csvWriter.write(ticks[i]);
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
        String filename = Helper.getExportPrefix();
        filename += "-Local-Distance-Multi-";
        for (ComparisonLocal local : results) {
            filename += local.getTitle().split(", ")[1];
        }
        filename += ".csv";
        return new File(filename);
    }
}
