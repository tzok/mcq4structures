package pl.poznan.put.matching;

import pl.poznan.put.circular.Angle;
import pl.poznan.put.circular.samples.AngleSample;
import pl.poznan.put.pdb.analysis.PdbResidue;
import pl.poznan.put.torsion.MasterTorsionAngleType;
import pl.poznan.put.torsion.TorsionAngleDelta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ResidueComparison {
  private final PdbResidue target;
  private final PdbResidue model;
  private final List<TorsionAngleDelta> angleDeltas;
  private final AngleSample angleSample;

  public ResidueComparison(
      final PdbResidue target, final PdbResidue model, final List<TorsionAngleDelta> angleDeltas) {
    super();
    this.target = target;
    this.model = model;
    this.angleDeltas = new ArrayList<>(angleDeltas);
    angleSample = new AngleSample(extractValidDeltas());
  }

  private List<Angle> extractValidDeltas() {
    List<Angle> angles = new ArrayList<>(angleDeltas.size());
    for (final TorsionAngleDelta angleDelta : angleDeltas) {
      if (angleDelta.getState() == TorsionAngleDelta.State.BOTH_VALID) {
        angles.add(angleDelta.getDelta());
      }
    }
    return angles;
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
    return angleSample.getMeanDirection();
  }

  public final Angle getMedianDirection() {
    return angleSample.getMedianDirection();
  }
}
