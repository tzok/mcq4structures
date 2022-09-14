package pl.poznan.put.comparison.local;

import java.util.List;
import java.util.stream.Collectors;
import org.immutables.value.Value;
import pl.poznan.put.matching.FragmentMatch;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.torsion.ImmutableAverageTorsionAngleType;
import pl.poznan.put.torsion.MasterTorsionAngleType;

@Value.Immutable
public abstract class ModelsComparisonResult {
  @Value.Parameter(order = 1)
  public abstract PdbCompactFragment target();

  @Value.Parameter(order = 2)
  public abstract List<PdbCompactFragment> models();

  @Value.Parameter(order = 3)
  public abstract List<FragmentMatch> inputFragmentMatches();

  @Value.Parameter(order = 4)
  public abstract List<MasterTorsionAngleType> angleTypes();

  @Value.Lazy
  public MoleculeType moleculeType() {
    return target().moleculeType();
  }

  @Value.Lazy
  public List<FragmentMatch> fragmentMatches() {
    return inputFragmentMatches().stream()
        .map(fragmentMatch -> fragmentMatch.filteredByAngleTypes(angleTypes()))
        .collect(Collectors.toList());
  }

  public final SelectedAngle selectAngle(final MasterTorsionAngleType torsionAngle) {
    return new SelectedAngle(torsionAngle, target(), models(), fragmentMatches());
  }

  public final SelectedAngle selectAverageOfAngles() {
    return new SelectedAngle(
        ImmutableAverageTorsionAngleType.of(moleculeType(), angleTypes()),
        target(),
        models(),
        fragmentMatches().stream()
            .map(fragmentMatch -> fragmentMatch.averagedOverAngleValues(angleTypes()))
            .collect(Collectors.toList()));
  }
}
