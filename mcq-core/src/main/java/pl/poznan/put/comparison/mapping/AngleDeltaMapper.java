package pl.poznan.put.comparison.mapping;

import pl.poznan.put.circular.Angle;
import pl.poznan.put.circular.samples.AngleSample;
import pl.poznan.put.circular.samples.ImmutableAngleSample;
import pl.poznan.put.matching.ResidueComparison;
import pl.poznan.put.torsion.MasterTorsionAngleType;
import pl.poznan.put.torsion.TorsionAngleDelta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Map {@link TorsionAngleDelta} onto 0-1 scale. */
public final class AngleDeltaMapper implements ComparisonMapper {
  private static final AngleDeltaMapper INSTANCE = new AngleDeltaMapper();

  private AngleDeltaMapper() {
    super();
  }

  public static AngleDeltaMapper getInstance() {
    return AngleDeltaMapper.INSTANCE;
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
   * @return An array of 0-1 values for each residue.
   */
  @Override
  public Double[] map(
      final List<? extends ResidueComparison> residueComparisons,
      final List<? extends MasterTorsionAngleType> angleTypes) {
    final Double[] result = new Double[residueComparisons.size()];

    for (int i = 0; i < residueComparisons.size(); i++) {
      final ResidueComparison residueComparison = residueComparisons.get(i);
      final Collection<Angle> deltas = new ArrayList<>(angleTypes.size());

      for (final MasterTorsionAngleType angleType : angleTypes) {
        final TorsionAngleDelta angleDelta = residueComparison.angleDelta(angleType);
        final TorsionAngleDelta.State state = angleDelta.state();

        if (state == TorsionAngleDelta.State.BOTH_VALID) {
          final Angle delta = angleDelta.delta();
          deltas.add(delta);
        }
      }

      if (deltas.isEmpty()) {
        result[i] = 1.0;
        continue;
      }

      final AngleSample sample = ImmutableAngleSample.of(deltas);
      final Angle meanDirection = sample.meanDirection();
      final double degrees = meanDirection.degrees360();
      result[i] = AngleDeltaMapper.map(degrees);
    }

    return result;
  }
}
