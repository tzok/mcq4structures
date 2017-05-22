package pl.poznan.put.matching;

import pl.poznan.put.circular.Angle;
import pl.poznan.put.circular.enums.RangeDifference;
import pl.poznan.put.circular.samples.AngleSample;
import pl.poznan.put.torsion.MasterTorsionAngleType;
import pl.poznan.put.torsion.TorsionAngleDelta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FragmentComparison implements Comparable<FragmentComparison> {
    private static final FragmentComparison INVALID_INSTANCE =
            new FragmentComparison(Collections.emptyList(),
                                   Collections.emptyList(), 0, 0, 0, 0,
                                   Angle.invalidInstance(),
                                   RangeDifference.INVALID);
    private final List<ResidueComparison> residueComparisons;
    private final List<MasterTorsionAngleType> angleTypes;
    private final int targetInvalidCount;
    private final int modelInvalidCount;
    private final int bothInvalidCount;
    private final int validCount;
    private final Angle meanDelta;
    private final RangeDifference meanRangeDiffrence;

    public FragmentComparison(
            final List<ResidueComparison> residueResults,
            final List<MasterTorsionAngleType> angles,
            final int firstInvalidCount, final int secondInvalidCount,
            final int bothInvalidCount, final int validCount,
            final Angle meanDelta, final RangeDifference meanRangeDiffrence) {
        super();
        residueComparisons = new ArrayList<>(residueResults);
        angleTypes = new ArrayList<>(angles);
        targetInvalidCount = firstInvalidCount;
        modelInvalidCount = secondInvalidCount;
        this.bothInvalidCount = bothInvalidCount;
        this.validCount = validCount;
        this.meanDelta = meanDelta;
        this.meanRangeDiffrence = meanRangeDiffrence;
    }

    public static FragmentComparison invalidInstance() {
        return FragmentComparison.INVALID_INSTANCE;
    }

    public static FragmentComparison fromResidueComparisons(
            final List<ResidueComparison> residueResults,
            final List<MasterTorsionAngleType> angleTypes) {
        List<Angle> deltas = new ArrayList<>();
        int targetInvalid = 0;
        int modelInvalid = 0;
        int bothInvalid = 0;
        double rangeValue = 0.0;

        for (final ResidueComparison result : residueResults) {
            for (final MasterTorsionAngleType angle : angleTypes) {
                TorsionAngleDelta delta = result.getAngleDelta(angle);

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

        AngleSample sample = new AngleSample(deltas);
        return new FragmentComparison(residueResults, angleTypes, targetInvalid,
                                      modelInvalid, bothInvalid, deltas.size(),
                                      sample.getMeanDirection(), RangeDifference
                                              .fromValue((int) Math
                                                      .round(rangeValue / deltas
                                                              .size())));
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
