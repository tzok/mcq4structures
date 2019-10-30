package pl.poznan.put.matching;

import lombok.Data;
import lombok.EqualsAndHashCode;
import pl.poznan.put.circular.Angle;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Data
@EqualsAndHashCode
public class StructureSelection implements Exportable, Tabular, ResidueCollection {
  private static final int MINIMUM_RESIDUES_IN_FRAGMENT = 3;

  @EqualsAndHashCode.Include private final List<PdbResidue> residues;

  private String name;
  private List<PdbCompactFragment> compactFragments;

  public StructureSelection(
      final String name, final Collection<? extends PdbCompactFragment> compactFragments) {
    super();
    this.name = name;
    this.compactFragments = new ArrayList<>(compactFragments);
    residues = StructureSelection.collectResidues(compactFragments);
  }

  public static StructureSelection divideIntoCompactFragments(
      final String name, final Iterable<? extends PdbResidue> residues) {
    final List<PdbResidue> candidates = new ArrayList<>();

    for (final PdbResidue residue : residues) {
      if (!residue.isMissing()) {
        candidates.add(residue);
      }
    }

    final List<PdbCompactFragment> compactFragments = new ArrayList<>();
    List<PdbResidue> currentFragmentResidues = new ArrayList<>();
    int index = 0;

    while (!candidates.isEmpty()) {
      final PdbResidue current = candidates.get(index);
      currentFragmentResidues.add(current);
      candidates.remove(index);
      index = current.findConnectedResidueIndex(candidates);

      if (index == -1) {
        if (currentFragmentResidues.size() >= StructureSelection.MINIMUM_RESIDUES_IN_FRAGMENT) {
          final String fragmentName =
              StructureSelection.generateFragmentName(name, currentFragmentResidues);
          final PdbCompactFragment compactFragment =
              new PdbCompactFragment(fragmentName, currentFragmentResidues);
          compactFragments.add(compactFragment);
        }

        currentFragmentResidues = new ArrayList<>();
        index = 0;
      }
    }

    return new StructureSelection(name, compactFragments);
  }

  private static String generateFragmentName(
      final String name, final List<? extends PdbResidue> fragmentResidues) {
    assert !fragmentResidues.isEmpty();

    if (fragmentResidues.size() == 1) {
      final PdbResidue residue = fragmentResidues.get(0);
      return String.format(
          "%s %s.%d", name, residue.getChainIdentifier(), residue.getResidueNumber());
    }

    final PdbResidue first = fragmentResidues.get(0);
    final PdbResidue last = fragmentResidues.get(fragmentResidues.size() - 1);
    return String.format(
        "%s %s.%d-%d",
        name, first.getChainIdentifier(), first.getResidueNumber(), last.getResidueNumber());
  }

  private static List<PdbResidue> collectResidues(
      final Iterable<? extends PdbCompactFragment> compactFragments) {
    final List<PdbResidue> residues = new ArrayList<>();
    compactFragments.forEach(compactFragment -> residues.addAll(compactFragment.getResidues()));
    return residues;
  }

  public final Set<MasterTorsionAngleType> getCommonTorsionAngleTypes() {
    final Set<MasterTorsionAngleType> commonTypes = new LinkedHashSet<>();

    for (final PdbCompactFragment compactFragment : compactFragments) {
      final MoleculeType moleculeType = compactFragment.getMoleculeType();
      final Collection<? extends MasterTorsionAngleType> angleTypes;

      switch (moleculeType) {
        case PROTEIN:
          angleTypes = Arrays.asList(ProteinTorsionAngleType.values());
          break;
        case RNA:
          angleTypes = Arrays.asList(RNATorsionAngleType.values());
          break;
        case UNKNOWN:
        default:
          throw new IllegalArgumentException("Unknown molecule type: " + moleculeType);
      }

      commonTypes.addAll(angleTypes);
    }

    return commonTypes;
  }

  public final List<Angle> getValidTorsionAngleValues(final MasterTorsionAngleType masterType) {
    final List<Angle> angles = new ArrayList<>();

    for (final PdbCompactFragment fragment : compactFragments) {
      fragment.getResidues().stream()
          .map(residue -> fragment.getTorsionAngleValue(residue, masterType))
          .map(TorsionAngleValue::getValue)
          .filter(Angle::isValid)
          .forEach(angles::add);
    }

    return angles;
  }

  @Override
  public final PdbResidue findResidue(
      final String chainIdentifier, final int residueNumber, final String insertionCode) {
    return findResidue(new PdbResidueIdentifier(chainIdentifier, residueNumber, insertionCode));
  }

  @Override
  public final PdbResidue findResidue(final PdbResidueIdentifier query) {
    for (final PdbResidue residue : residues) {
      if (query.equals(residue.getResidueIdentifier())) {
        return residue;
      }
    }
    throw new IllegalArgumentException("Failed to find residue: " + query);
  }

  @Override
  public final String toString() {
    final PdbResidue first = residues.get(0);
    final PdbResidue last = residues.get(residues.size() - 1);
    return String.format("%s - %s (count: %d)", first, last, residues.size());
  }

  @Override
  public final void export(final OutputStream stream) throws IOException {
    TabularExporter.export(asExportableTableModel(), stream);
  }

  @Override
  public final File suggestName() {
    return new File(name + ".csv");
  }

  @Override
  public final TableModel asExportableTableModel() {
    return asTableModel(false);
  }

  @Override
  public final TableModel asDisplayableTableModel() {
    return asTableModel(true);
  }

  private TableModel asTableModel(final boolean isDisplayable) {
    final Set<MasterTorsionAngleType> allAngleTypes = getCommonTorsionAngleTypes();
    final Collection<String> columns = new LinkedHashSet<>(allAngleTypes.size() + 1);
    columns.add("Residue");

    for (final MasterTorsionAngleType angleType : allAngleTypes) {
      columns.add(isDisplayable ? angleType.getLongDisplayName() : angleType.getExportName());
    }

    final int rowCount =
        compactFragments.stream().map(PdbCompactFragment::getResidues).mapToInt(List::size).sum();

    final String[][] data = new String[rowCount][];
    int i = 0;

    for (final PdbCompactFragment fragment : compactFragments) {
      for (final PdbResidue residue : fragment.getResidues()) {
        final List<String> row = new ArrayList<>();
        row.add(residue.toString());

        for (final MasterTorsionAngleType angleType : allAngleTypes) {
          final TorsionAngleValue angleValue = fragment.getTorsionAngleValue(residue, angleType);
          final double radians = angleValue.getValue().getRadians();
          row.add(
              isDisplayable
                  ? AngleFormat.degreesRoundedToHundredth(radians)
                  : AngleFormat.degrees(radians));
        }

        data[i] = row.toArray(new String[0]);
        i += 1;
      }
    }

    return new DefaultTableModel(data, columns.toArray(new String[0]));
  }
}
