package pl.poznan.put.matching;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.apache.commons.collections4.CollectionUtils;

import pl.poznan.put.circular.exception.InvalidCircularValueException;
import pl.poznan.put.interfaces.Exportable;
import pl.poznan.put.interfaces.Tabular;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.pdb.analysis.PdbResidue;
import pl.poznan.put.torsion.TorsionAngleValue;
import pl.poznan.put.torsion.type.TorsionAngleType;
import pl.poznan.put.types.ExportFormat;
import pl.poznan.put.utility.AngleFormat;
import pl.poznan.put.utility.TabularExporter;

public class StructureSelection implements Exportable, Tabular {
    private final List<PdbCompactFragment> compactFragments = new ArrayList<>();

    private final String name;
    private final List<PdbResidue> residues;

    public StructureSelection(String name, List<PdbResidue> residues) throws InvalidCircularValueException {
        super();
        this.name = name;
        this.residues = residues;

        divideIntoCompactFragments();
    }

    private void divideIntoCompactFragments() throws InvalidCircularValueException {
        int fromIndex = 0;

        for (int i = 1; i < residues.size(); i++) {
            PdbResidue previous = residues.get(i - 1);
            PdbResidue current = residues.get(i);

            if (!previous.isConnectedTo(current)) {
                compactFragments.add(new PdbCompactFragment(residues.subList(fromIndex, i)));
                fromIndex = i;
            }
        }

        compactFragments.add(new PdbCompactFragment(residues.subList(fromIndex, residues.size())));
    }

    public String getName() {
        return name;
    }

    public List<PdbResidue> getResidues() {
        return Collections.unmodifiableList(residues);
    }

    public List<PdbCompactFragment> getCompactFragments() {
        return Collections.unmodifiableList(compactFragments);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (name == null ? 0 : name.hashCode());
        result = prime * result + (residues == null ? 0 : residues.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        StructureSelection other = (StructureSelection) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (residues == null) {
            if (other.residues != null) {
                return false;
            }
        } else if (!CollectionUtils.isEqualCollection(residues, other.residues)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        PdbResidue first = residues.get(0);
        PdbResidue last = residues.get(residues.size() - 1);
        return first + " - " + last + " (count: " + residues.size() + ")";
    }

    @Override
    public void export(OutputStream stream) throws IOException {
        TabularExporter.export(asExportableTableModel(), stream);
    }

    @Override
    public ExportFormat getExportFormat() {
        return ExportFormat.CSV;
    }

    @Override
    public File suggestName() {
        return new File(name + ".csv");
    }

    @Override
    public TableModel asExportableTableModel() {
        return asTableModel(false);
    }

    @Override
    public TableModel asDisplayableTableModel() {
        return asTableModel(true);
    }

    private TableModel asTableModel(boolean isDisplayable) {
        Set<TorsionAngleType> allAngleTypes = commonTorsionAngleTypes();
        Set<String> columns = new LinkedHashSet<>();
        columns.add("Residue");

        for (TorsionAngleType angleType : allAngleTypes) {
            columns.add(isDisplayable ? angleType.getLongDisplayName() : angleType.getExportName());
        }

        int rowCount = 0;

        for (PdbCompactFragment fragment : compactFragments) {
            List<PdbResidue> fragmentResidues = fragment.getResidues();
            rowCount += fragmentResidues.size();
        }

        String[][] data = new String[rowCount][];
        int i = 0;

        for (PdbCompactFragment fragment : compactFragments) {
            for (PdbResidue residue : fragment.getResidues()) {
                List<String> row = new ArrayList<>();
                row.add(residue.toString());

                for (TorsionAngleType angleType : allAngleTypes) {
                    TorsionAngleValue angleValue = fragment.getTorsionAngleValue(residue, angleType);
                    double radians = angleValue.getValue().getRadians();
                    row.add(isDisplayable ? AngleFormat.formatDisplayLong(radians) : AngleFormat.formatExport(radians));
                }

                data[i] = row.toArray(new String[row.size()]);
                i += 1;
            }
        }

        return new DefaultTableModel(data, columns.toArray(new String[columns.size()]));
    }

    private Set<TorsionAngleType> commonTorsionAngleTypes() {
        Set<TorsionAngleType> set = new LinkedHashSet<>();
        for (PdbCompactFragment compactFragment : compactFragments) {
            set.addAll(compactFragment.commonTorsionAngleTypes());
        }
        return set;
    }

    public int size() {
        return residues.size();
    }
}
