package pl.poznan.put.matching;

import org.immutables.value.Value;
import pl.poznan.put.circular.Angle;
import pl.poznan.put.circular.ImmutableAngle;
import pl.poznan.put.circular.samples.ImmutableAngleSample;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbResidue;
import pl.poznan.put.torsion.ImmutableAverageTorsionAngleType;
import pl.poznan.put.torsion.MasterTorsionAngleType;
import pl.poznan.put.torsion.TorsionAngleDelta;
import pl.poznan.put.torsion.TorsionAngleType;
import pl.poznan.put.torsion.range.RangeDifference;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Value.Immutable
public abstract class ResidueComparison {
  public static ResidueComparison invalidInstance(final PdbResidue target, final PdbResidue model) {
    return ImmutableResidueComparison.of(target, model, Collections.emptyList());
  }

  @Value.Parameter(order = 1)
  public abstract PdbResidue target();

  @Value.Parameter(order = 2)
  public abstract PdbResidue model();

  @Value.Parameter(order = 3)
  public abstract List<TorsionAngleDelta> angleDeltas();

  public final ResidueComparison filteredByAngleTypes(
      final Collection<MasterTorsionAngleType> angleTypes) {
    return ImmutableResidueComparison.copyOf(this)
        .withAngleDeltas(
            angleDeltas().stream()
                .filter(angleDelta -> angleTypes.contains(angleDelta.getMasterTorsionAngleType()))
                .collect(Collectors.toList()));
  }

  public ResidueComparison averagedOverAngleValues(
      final Collection<MasterTorsionAngleType> angleTypes) {
    final ImmutableAngleSample angleSample =
        ImmutableAngleSample.of(
            angleDeltas().stream()
                .filter(delta -> angleTypes.contains(delta.getMasterTorsionAngleType()))
                .map(TorsionAngleDelta::getDelta)
                .filter(Angle::isValid)
                .collect(Collectors.toList()));
    final MoleculeType moleculeType =
        angleTypes.stream()
            .map(MasterTorsionAngleType::angleTypes)
            .flatMap(Collection::stream)
            .map(TorsionAngleType::moleculeType)
            .findFirst()
            .orElse(MoleculeType.UNKNOWN);
    return ImmutableResidueComparison.copyOf(this)
        .withAngleDeltas(
            new TorsionAngleDelta(
                ImmutableAverageTorsionAngleType.of(moleculeType, angleTypes),
                TorsionAngleDelta.State.BOTH_VALID,
                ImmutableAngle.of(Double.NaN),
                ImmutableAngle.of(Double.NaN),
                angleSample.meanDirection(),
                RangeDifference.fromValue((int) (angleSample.meanDirection().degrees360() / 60))));
  }

  public final List<Angle> validDeltas() {
    return angleDeltas().stream()
        .filter(delta -> delta.getState() == TorsionAngleDelta.State.BOTH_VALID)
        .map(TorsionAngleDelta::getDelta)
        .collect(Collectors.toList());
  }

  public final TorsionAngleDelta angleDelta(final MasterTorsionAngleType masterType) {
    return angleDeltas().stream()
        .filter(delta -> delta.getMasterTorsionAngleType().equals(masterType))
        .findFirst()
        .orElse(TorsionAngleDelta.bothInvalidInstance(masterType));
  }
}
