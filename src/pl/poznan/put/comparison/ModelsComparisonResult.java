package pl.poznan.put.comparison;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.biojava.bio.structure.Group;
import org.jumpmind.symmetric.csv.CsvWriter;

import pl.poznan.put.common.TorsionAngle;
import pl.poznan.put.interfaces.Exportable;
import pl.poznan.put.interfaces.Tabular;
import pl.poznan.put.interfaces.Visualizable;
import pl.poznan.put.matching.FragmentComparisonResult;
import pl.poznan.put.matching.FragmentMatch;
import pl.poznan.put.matching.ResidueComparisonResult;
import pl.poznan.put.structure.CompactFragment;
import pl.poznan.put.structure.Residue;
import pl.poznan.put.utility.TorsionAngleDelta;

public class ModelsComparisonResult implements Exportable, Visualizable,
        Tabular {
    private final TorsionAngle torsionAngle;
    private final CompactFragment reference;
    private final List<CompactFragment> models;
    private final List<FragmentMatch> matches;

    public ModelsComparisonResult(TorsionAngle torsionAngle,
            CompactFragment reference, List<CompactFragment> models,
            List<FragmentMatch> matches) {
        super();
        this.torsionAngle = torsionAngle;
        this.reference = reference;
        this.models = models;
        this.matches = matches;
    }

    public TorsionAngle getTorsionAngle() {
        return torsionAngle;
    }

    public CompactFragment getReference() {
        return reference;
    }

    public List<CompactFragment> getModels() {
        return models;
    }

    public List<FragmentMatch> getMatches() {
        return matches;
    }

    public int getModelCount() {
        return matches.size();
    }

    public int getFragmentSize() {
        return reference.getSize();
    }

    public List<ResidueComparisonResult> getResidueResults(int column) {
        FragmentMatch fragmentMatch = matches.get(column);
        FragmentComparisonResult bestResult = fragmentMatch.getBestResult();
        return bestResult.getResidueResults();
    }

    @Override
    public void export(File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
            CsvWriter csvWriter = new CsvWriter(writer, ';');
            csvWriter.write(null);

            for (CompactFragment model : models) {
                csvWriter.write(model.getName());
            }

            csvWriter.endRecord();

            for (int i = 0; i < reference.getSize(); i++) {
                Group group = reference.getGroup(i);
                Residue residue = Residue.fromGroup(group);
                csvWriter.write(residue.toString());

                for (int j = 0; j < models.size(); j++) {
                    List<ResidueComparisonResult> residueResults = getResidueResults(j);
                    ResidueComparisonResult result = residueResults.get(i);
                    TorsionAngleDelta delta = result.getDelta(torsionAngle);
                    csvWriter.write(delta.toExportString());
                }

                csvWriter.endRecord();
            }
        }
    }

    @Override
    public File suggestName() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
        StringBuilder builder = new StringBuilder(sdf.format(new Date()));
        builder.append("-Local-Distance-Multi");

        for (CompactFragment model : models) {
            builder.append('-');
            builder.append(model.getParentName());
        }

        builder.append(".csv");
        return new File(builder.toString());
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

    @Override
    public TableModel asExportableTableModel() {
        return asTableModel(false);
    }

    @Override
    public TableModel asDisplayableTableModel() {
        return asTableModel(true);
    }

    private TableModel asTableModel(boolean isDisplay) {
        String[] columnNames = new String[models.size() + 1];
        columnNames[0] = isDisplay ? "" : null;
        for (int i = 0; i < models.size(); i++) {
            columnNames[i + 1] = models.get(i).getName();
        }

        String[][] data = new String[reference.getSize()][];

        for (int i = 0; i < reference.getSize(); i++) {
            data[i] = new String[models.size() + 1];
            data[i][0] = reference.getResidue(i).toString();

            for (int j = 0; j < models.size(); j++) {
                List<ResidueComparisonResult> residueResults = getResidueResults(j);
                ResidueComparisonResult result = residueResults.get(i);
                TorsionAngleDelta delta = result.getDelta(torsionAngle);

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
