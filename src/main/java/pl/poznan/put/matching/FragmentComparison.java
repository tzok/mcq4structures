package pl.poznan.put.matching;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.EqualsAndHashCode;
import pl.poznan.put.circular.Angle;
import pl.poznan.put.circular.samples.AngleSample;
import pl.poznan.put.torsion.MasterTorsionAngleType;
import pl.poznan.put.torsion.TorsionAngleDelta;
import pl.poznan.put.torsion.range.RangeDifference;

@EqualsAndHashCode
public class FragmentComparison implements Comparable<FragmentComparison> {
  private final List<ResidueComparison> residueComparisons;
  private final List<MasterTorsionAngleType> angleTypes;
  private final int targetInvalidCount;
  private final int modelInvalidCount;
  private final int bothInvalidCount;
  private final int validCount;
  private final Angle meanDelta;
  private final RangeDifference meanRangeDiffrence;

  public FragmentComparison(
      final List<ResidueComparison> residueComparisons,
      final List<MasterTorsionAngleType> angleTypes,
      final int targetInvalidCount,
      final int modelInvalidCount,
      final int bothInvalidCount,
      final int validCount,
      final Angle meanDelta,
      final RangeDifference meanRangeDiffrence) {
    super();
    this.residueComparisons = new ArrayList<>(residueComparisons);
    this.angleTypes = new ArrayList<>(angleTypes);
    this.targetInvalidCount = targetInvalidCount;
    this.modelInvalidCount = modelInvalidCount;
    this.bothInvalidCount = bothInvalidCount;
    this.validCount = validCount;
    this.meanDelta = meanDelta;
    this.meanRangeDiffrence = meanRangeDiffrence;
  }

  private FragmentComparison(
      final List<ResidueComparison> residueComparisons,
      final List<MasterTorsionAngleType> angleTypes,
      final int targetInvalidCount,
      final int modelInvalidCount,
      final int bothInvalidCount,
      final int validCount) {
    super();
    this.residueComparisons = new ArrayList<>(residueComparisons);
    this.angleTypes = new ArrayList<>(angleTypes);
    this.targetInvalidCount = targetInvalidCount;
    this.modelInvalidCount = modelInvalidCount;
    this.bothInvalidCount = bothInvalidCount;
    this.validCount = validCount;
    meanDelta = Angle.invalidInstance();
    meanRangeDiffrence = RangeDifference.INVALID;
  }

  public static FragmentComparison fromResidueComparisons(
      final List<ResidueComparison> residueComparisons,
      final List<MasterTorsionAngleType> angleTypes) {
    final List<Angle> deltas = new ArrayList<>();
    int targetInvalid = 0;
    int modelInvalid = 0;
    int bothInvalid = 0;
    double rangeValue = 0.0;

    for (final ResidueComparison result : residueComparisons) {
      for (final MasterTorsionAngleType angle : angleTypes) {
        final TorsionAngleDelta delta = result.getAngleDelta(angle);

        if (delta == null) {
          continue;
        }

        switch (delta.getState()) {
          case BOTH_INVALID:
            bothInvalid++;
            break;
          case BOTH_VALID:
            deltas.add(delta.getDelta());
            rangeValue += delta.getRangeDifference().getValue();
            break;
          case TARGET_INVALID:
            targetInvalid++;
            break;
          case MODEL_INVALID:
            modelInvalid++;
            break;
          default:
            break;
        }
      }
    }

    if (deltas.isEmpty()) {
      return FragmentComparison.invalidInstance(
          residueComparisons, angleTypes, targetInvalid, modelInvalid, bothInvalid, 0);
    }

    final AngleSample sample = new AngleSample(deltas);
    return new FragmentComparison(
        residueComparisons,
        angleTypes,
        targetInvalid,
        modelInvalid,
        bothInvalid,
        deltas.size(),
        sample.getMeanDirection(),
        RangeDifference.fromValue((int) Math.round(rangeValue / deltas.size())));
  }

  public static FragmentComparison invalidInstance(
      final List<ResidueComparison> residueComparisons,
      final List<MasterTorsionAngleType> angleTypes,
      final int targetInvalidCount,
      final int modelInvalidCount,
      final int bothInvalidCount,
      final int validCount) {
    return new FragmentComparison(
        residueComparisons,
        angleTypes,
        targetInvalidCount,
        modelInvalidCount,
        bothInvalidCount,
        validCount);
  }

  public final List<ResidueComparison> getResidueComparisons() {
    return Collections.unmodifiableList(residueComparisons);
  }

  public final List<MasterTorsionAngleType> getAngleTypes() {
    return Collections.unmodifiableList(angleTypes);
  }

  public final int getTargetInvalidCount() {
    return targetInvalidCount;
  }

  public final int getModelInvalidCount() {
    return modelInvalidCount;
  }

  public final int getBothInvalidCount() {
    return bothInvalidCount;
  }

  public final int getValidCount() {
    return validCount;
  }

  public final Angle getMeanDelta() {
    return meanDelta;
  }

  public RangeDifference getMeanRangeDiffrence() {
    return meanRangeDiffrence;
  }

  public final int getMismatchCount() {
    return targetInvalidCount + modelInvalidCount;
  }

  public final int getResidueCount() {
    return residueComparisons.size();
  }

  public final boolean isValid() {
    return meanDelta.isValid();
  }

  @Override
  public final int compareTo(final FragmentComparison t) {
    return meanDelta.compareTo(t.meanDelta);
  }
}
