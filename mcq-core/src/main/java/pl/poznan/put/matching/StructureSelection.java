package pl.poznan.put.matching;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import pl.poznan.put.circular.Angle;
import pl.poznan.put.circular.ImmutableAngle;
import pl.poznan.put.interfaces.Exportable;
import pl.poznan.put.interfaces.Tabular;
import pl.poznan.put.pdb.analysis.ImmutablePdbCompactFragment;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.pdb.analysis.PdbResidue;
import pl.poznan.put.pdb.analysis.ResidueCollection;
import pl.poznan.put.pdb.analysis.ResidueTorsionAngles;
import pl.poznan.put.pdb.analysis.SingleTypedResidueCollection;
import pl.poznan.put.torsion.MasterTorsionAngleType;
import pl.poznan.put.utility.AngleFormat;
import pl.poznan.put.utility.TabularExporter;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
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
    residues =
        compactFragments.stream()
            .map(PdbCompactFragment::residues)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
  }

  public static StructureSelection divideIntoCompactFragments(
      final String name, final List<PdbResidue> residues) {
    if (residues.size() == 1) {
      return new StructureSelection(
          name, Collections.singletonList(ImmutablePdbCompactFragment.of(residues).withName(name)));
    }

    final List<PdbResidue> candidates = new ArrayList<>(residues);
    final List<PdbResidue> fragment = new ArrayList<>();
    final List<PdbCompactFragment> compactFragments = new ArrayList<>();

    while (!candidates.isEmpty()) {
      if (fragment.isEmpty()) {
        fragment.add(candidates.get(0));
        candidates.remove(0);
        continue;
      }

      final PdbResidue last = fragment.get(fragment.size() - 1);
      final Optional<PdbResidue> connected =
          candidates.stream().filter(last::isConnectedTo).findFirst();

      if (connected.isPresent()) {
        fragment.add(connected.get());
        candidates.remove(connected.get());
      } else {
        if (fragment.size() >= StructureSelection.MINIMUM_RESIDUES_IN_FRAGMENT) {
          compactFragments.add(
              ImmutablePdbCompactFragment.of(fragment)
                  .withName(StructureSelection.generateFragmentName(name, fragment)));
        }
        fragment.clear();
      }
    }

    if (fragment.size() >= StructureSelection.MINIMUM_RESIDUES_IN_FRAGMENT) {
      compactFragments.add(
          ImmutablePdbCompactFragment.of(fragment)
              .withName(StructureSelection.generateFragmentName(name, fragment)));
    }

    return new StructureSelection(name, compactFragments);
  }

  private static String generateFragmentName(
      final String name, final List<? extends PdbResidue> fragmentResidues) {
    assert !fragmentResidues.isEmpty();

    if (fragmentResidues.size() == 1) {
      final PdbResidue residue = fragmentResidues.get(0);
      return String.format("%s %s.%d", name, residue.chainIdentifier(), residue.residueNumber());
    }

    final PdbResidue first = fragmentResidues.get(0);
    final PdbResidue last = fragmentResidues.get(fragmentResidues.size() - 1);
    return String.format(
        "%s %s.%d-%d", name, first.chainIdentifier(), first.residueNumber(), last.residueNumber());
  }

  @Override
  public List<PdbResidue> residues() {
    return Collections.unmodifiableList(residues);
  }

  public final Set<MasterTorsionAngleType> getCommonTorsionAngleTypes() {
    return compactFragments.stream()
        .map(SingleTypedResidueCollection::moleculeType)
        .map(MoleculeType::mainAngleTypes)
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());
  }

  public final List<Angle> getValidTorsionAngleValues(final MasterTorsionAngleType masterType) {
    final List<Angle> angles = new ArrayList<>();

    for (final PdbCompactFragment fragment : compactFragments) {
      fragment.residues().stream()
          .map(PdbResidue::identifier)
          .map(fragment::torsionAngles)
          .map(
              residueTorsionAngles ->
                  masterType.angleTypes().stream()
                      .map(residueTorsionAngles::value)
                      .filter(Angle::isValid)
                      .findFirst()
                      .orElse(ImmutableAngle.of(Double.NaN)))
          .filter(Angle::isValid)
          .forEach(angles::add);
    }

    return angles;
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
      columns.add(isDisplayable ? angleType.longDisplayName() : angleType.exportName());
    }

    final int rowCount =
        compactFragments.stream().map(PdbCompactFragment::residues).mapToInt(List::size).sum();

    final String[][] data = new String[rowCount][];
    int i = 0;

    for (final PdbCompactFragment fragment : compactFragments) {
      for (final PdbResidue residue : fragment.residues()) {
        final ResidueTorsionAngles torsionAngles = fragment.torsionAngles(residue.identifier());

        final List<String> row = new ArrayList<>();
        row.add(residue.toString());

        for (final MasterTorsionAngleType masterType : allAngleTypes) {
          final double radians =
              masterType.angleTypes().stream()
                  .map(torsionAngles::value)
                  .filter(Angle::isValid)
                  .map(Angle::radians)
                  .findFirst()
                  .orElse(Double.NaN);
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
