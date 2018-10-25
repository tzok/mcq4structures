package pl.poznan.put.matching;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import pl.poznan.put.circular.Angle;
import pl.poznan.put.circular.samples.AngleSample;
import pl.poznan.put.pdb.analysis.PdbResidue;
import pl.poznan.put.torsion.MasterTorsionAngleType;
import pl.poznan.put.torsion.TorsionAngleDelta;

public class ResidueComparison {
  public static ResidueComparison invalidInstance(final PdbResidue target, final PdbResidue model) {
    return new ResidueComparison(target, model);
  }

  private final PdbResidue target;
  private final PdbResidue model;
  private final List<TorsionAngleDelta> angleDeltas;
  private final Angle meanDirection;
  private final Angle medianDirection;

  public ResidueComparison(
      final PdbResidue target, final PdbResidue model, final List<TorsionAngleDelta> angleDeltas) {
    super();
    this.target = target;
    this.model = model;
    this.angleDeltas = new ArrayList<>(angleDeltas);

    final AngleSample angleSample = new AngleSample(extractValidDeltas());
    meanDirection = angleSample.getMeanDirection();
    medianDirection = angleSample.getMedianDirection();
  }

  // constructor for invalidInstance() static call
  private ResidueComparison(final PdbResidue target, final PdbResidue model) {
    super();
    this.target = target;
    this.model = model;
    angleDeltas = Collections.emptyList();
    meanDirection = Angle.invalidInstance();
    medianDirection = Angle.invalidInstance();
  }

  private List<Angle> extractValidDeltas() {
    return angleDeltas
        .stream()
        .filter(delta -> delta.getState() == TorsionAngleDelta.State.BOTH_VALID)
        .map(TorsionAngleDelta::getDelta)
        .collect(Collectors.toList());
  }

  public final PdbResidue getTarget() {
    return target;
  }

  public final PdbResidue getModel() {
    return model;
  }

  public final TorsionAngleDelta getAngleDelta(final MasterTorsionAngleType masterType) {
    for (final TorsionAngleDelta delta : angleDeltas) {
      if (Objects.equals(masterType, delta.getMasterTorsionAngleType())) {
        return delta;
      }
    }
    return TorsionAngleDelta.bothInvalidInstance(masterType);
  }

  public final Angle getMeanDirection() {
    return meanDirection;
  }

  public final Angle getMedianDirection() {
    return medianDirection;
  }
}
