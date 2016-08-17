package pl.poznan.put.matching;

import org.apache.commons.collections4.CollectionUtils;
import pl.poznan.put.circular.Angle;
import pl.poznan.put.circular.exception.InvalidCircularValueException;
import pl.poznan.put.interfaces.Exportable;
import pl.poznan.put.interfaces.Tabular;
import pl.poznan.put.pdb.PdbResidueIdentifier;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.pdb.analysis.PdbResidue;
import pl.poznan.put.pdb.analysis.ResidueCollection;
import pl.poznan.put.protein.torsion.ProteinTorsionAngleType;
import pl.poznan.put.rna.torsion.RNATorsionAngleType;
import pl.poznan.put.torsion.MasterTorsionAngleType;
import pl.poznan.put.torsion.TorsionAngleValue;
import pl.poznan.put.types.ExportFormat;
import pl.poznan.put.utility.AngleFormat;
import pl.poznan.put.utility.TabularExporter;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class StructureSelection
        implements Exportable, Tabular, ResidueCollection {
    private static final int MINIMUM_RESIDUES_IN_A_COMPACT_FRAGMENT = 3;

    private final List<PdbCompactFragment> compactFragments = new ArrayList<>();

    private final String name;
    private final List<PdbResidue> residues;

    public StructureSelection(String name, List<PdbResidue> residues)
            throws InvalidCircularValueException {
        super();
        this.name = name;
        this.residues = residues;

        divideIntoCompactFragments();
    }

    private void divideIntoCompactFragments()
            throws InvalidCircularValueException {
        List<PdbResidue> candidates = new ArrayList<>();

        for (PdbResidue residue : residues) {
            if (!residue.isMissing()) {
                candidates.add(residue);
            }
        }

        List<PdbResidue> currentFragmentResidues = new ArrayList<>();
        int index = 0;

        while (candidates.size() > 0) {
            PdbResidue current = candidates.get(index);
            currentFragmentResidues.add(current);
            candidates.remove(index);
            index = current.findConnectedResidueIndex(candidates);

            if (index == -1) {
                if (currentFragmentResidues.size()
                    > StructureSelection
                            .MINIMUM_RESIDUES_IN_A_COMPACT_FRAGMENT) {
                    String fragmentName =
                            generateFragmentName(currentFragmentResidues);
                    PdbCompactFragment compactFragment =
                            new PdbCompactFragment(fragmentName,
                                                   currentFragmentResidues);
                    compactFragments.add(compactFragment);
                }

                currentFragmentResidues = new ArrayList<>();
                index = 0;
            }
        }
    }

    private String generateFragmentName(List<PdbResidue> fragmentResidues) {
        assert fragmentResidues.size() > 0;

        if (fragmentResidues.size() == 1) {
            PdbResidue residue = fragmentResidues.get(0);
            return name + " " + residue.getChainIdentifier() + "." + residue
                    .getResidueNumber();
        }

        PdbResidue first = fragmentResidues.get(0);
        PdbResidue last = fragmentResidues.get(fragmentResidues.size() - 1);
        return name + " " + first.getChainIdentifier() + "." + first
                .getResidueNumber() + "-" + last.getResidueNumber();
    }

    public String getName() {
        return name;
    }

    public List<Angle> getValidTorsionAngleValues(
            MasterTorsionAngleType masterType) {
        List<Angle> angles = new ArrayList<>();

        for (PdbCompactFragment fragment : compactFragments) {
            for (PdbResidue residue : fragment.getResidues()) {
                TorsionAngleValue angleValue =
                        fragment.getTorsionAngleValue(residue, masterType);
                Angle angle = angleValue.getValue();
                if (angle.isValid()) {
                    angles.add(angle);
                }
            }
        }

        return angles;
    }

    @Override
    public List<PdbResidue> getResidues() {
        return Collections.unmodifiableList(residues);
    }

    @Override
    public PdbResidue findResidue(String chainIdentifier, int residueNumber,
                                  String insertionCode) {
        return findResidue(
                new PdbResidueIdentifier(chainIdentifier, residueNumber,
                                         insertionCode));
    }

    @Override
    public PdbResidue findResidue(PdbResidueIdentifier query) {
        for (PdbResidue residue : residues) {
            if (query.equals(residue.getResidueIdentifier())) {
                return residue;
            }
        }
        throw new IllegalArgumentException("Failed to find residue: " + query);
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
        } else if (!CollectionUtils
                .isEqualCollection(residues, other.residues)) {
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
        Set<MasterTorsionAngleType> allAngleTypes =
                getCommonTorsionAngleTypes();
        Set<String> columns = new LinkedHashSet<>();
        columns.add("Residue");

        for (MasterTorsionAngleType angleType : allAngleTypes) {
            columns.add(isDisplayable ? angleType.getLongDisplayName()
                                      : angleType.getExportName());
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

                for (MasterTorsionAngleType angleType : allAngleTypes) {
                    TorsionAngleValue angleValue =
                            fragment.getTorsionAngleValue(residue, angleType);
                    double radians = angleValue.getValue().getRadians();
                    row.add(isDisplayable ? AngleFormat
                            .formatDisplayShort(radians)
                                          : AngleFormat.formatExport(radians));
                }

                data[i] = row.toArray(new String[row.size()]);
                i += 1;
            }
        }

        return new DefaultTableModel(data, columns.toArray(
                new String[columns.size()]));
    }

    public Set<MasterTorsionAngleType> getCommonTorsionAngleTypes() {
        Set<MasterTorsionAngleType> commonTypes = new LinkedHashSet<>();

        for (PdbCompactFragment compactFragment : compactFragments) {
            MoleculeType moleculeType = compactFragment.getMoleculeType();
            Collection<? extends MasterTorsionAngleType> angleTypes;

            switch (moleculeType) {
                case PROTEIN:
                    angleTypes =
                            Arrays.asList(ProteinTorsionAngleType.values());
                    break;
                case RNA:
                    angleTypes = Arrays.asList(RNATorsionAngleType.values());
                    break;
                case UNKNOWN:
                default:
                    throw new IllegalArgumentException(
                            "Unknown molecule type: " + moleculeType);
            }

            commonTypes.addAll(angleTypes);
        }

        return commonTypes;
    }

    public int size() {
        return residues.size();
    }
}
