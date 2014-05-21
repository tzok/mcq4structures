package pl.poznan.put.comparison;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jumpmind.symmetric.csv.CsvWriter;

import pl.poznan.put.common.TorsionAngle;
import pl.poznan.put.matching.FragmentMatch;
import pl.poznan.put.matching.ResidueComparisonResult;
import pl.poznan.put.matching.SelectionMatch;
import pl.poznan.put.structure.CompactFragment;
import pl.poznan.put.structure.Residue;
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
                Residue r1 = Residue.fromGroup(bigger.getResidue(i));
                Residue r2 = Residue.fromGroup(smaller.getResidue(i));
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
        try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
            CsvWriter csvWriter = new CsvWriter(writer, ';');
            csvWriter.write(null);

            for (TorsionAngle angle : angles) {
                csvWriter.write(angle.toString());
            }

            csvWriter.endRecord();
            List<String> dataLabels = getDataLabels();
            List<ResidueComparisonResult> dataRows = getDataRows();
            assert dataLabels.size() == dataRows.size();

            for (int i = 0; i < dataLabels.size(); i++) {
                csvWriter.write(dataLabels.get(i));
                ResidueComparisonResult residueResult = dataRows.get(i);

                for (TorsionAngle angle : angles) {
                    TorsionAngleDelta delta = residueResult.getDelta(angle);
                    csvWriter.write(delta.toExportString());
                }

                csvWriter.endRecord();
            }
        }
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
        // TODO Auto-generated method stub
    }

    @Override
    public void visualize3D() {
        // TODO Auto-generated method stub
    }

    @Override
    public void visualizeHighQuality() {
        // TODO Auto-generated method stub
    }
}
