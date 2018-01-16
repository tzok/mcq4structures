package pl.poznan.put.visualisation;

import pl.poznan.put.circular.Angle;
import pl.poznan.put.circular.samples.AngleSample;
import pl.poznan.put.matching.ResidueComparison;
import pl.poznan.put.torsion.MasterTorsionAngleType;
import pl.poznan.put.torsion.TorsionAngleDelta;

import java.util.ArrayList;
import java.util.List;

/** Map {@link TorsionAngleDelta} onto 0-1 scale. */
public final class AngleDeltaMapper implements ComparisonMapper {
  private static final AngleDeltaMapper INSTANCE = new AngleDeltaMapper();

  public static AngleDeltaMapper getInstance() {
    return AngleDeltaMapper.INSTANCE;
  }

  private AngleDeltaMapper() {
    super();
  }

  public static double map(final double degrees) {
    if (degrees < 15.0) {
      return 0.0;
    }
    if (degrees < 30.0) {
      return 1.0 / 3.0;
    }
    if (degrees < 60.0) {
      return 2.0 / 3.0;
    }
    return 1.0;
  }

  /**
   * For each residue return a value in range [0; 1]. MCQ of angle differences < 15 degrees is a 0.
   * MCQ of angle differences < 30 degrees is a 0.33. MCQ < 60 is a 0.66. The rest is 1.0.
   *
   * @param residueComparisons List of results of comparison for single residues.
   * @param angleTypes List of angle types available for each residue.
   * @return
   */
  @Override
  public Double[] map(
      final List<ResidueComparison> residueComparisons,
      final List<MasterTorsionAngleType> angleTypes) {
    final Double[] result = new Double[residueComparisons.size()];

    for (int i = 0; i < residueComparisons.size(); i++) {
      final ResidueComparison residueComparison = residueComparisons.get(i);
      final List<Angle> deltas = new ArrayList<>(angleTypes.size());

      for (final MasterTorsionAngleType angleType : angleTypes) {
        final TorsionAngleDelta angleDelta = residueComparison.getAngleDelta(angleType);
        final TorsionAngleDelta.State state = angleDelta.getState();

        if (state == TorsionAngleDelta.State.BOTH_VALID) {
          final Angle delta = angleDelta.getDelta();
          deltas.add(delta);
        }
      }

      final AngleSample sample = new AngleSample(deltas);
      final Angle meanDirection = sample.getMeanDirection();
      final double degrees = meanDirection.getDegrees360();
      result[i] = AngleDeltaMapper.map(degrees);
    }

    return result;
  }
}
