package pl.poznan.put.matching;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pl.poznan.put.circular.Angle;
import pl.poznan.put.circular.exception.InvalidCircularValueException;
import pl.poznan.put.circular.samples.AngleSample;
import pl.poznan.put.torsion.TorsionAngleDelta;
import pl.poznan.put.torsion.type.MasterTorsionAngleType;

public class FragmentComparison implements Comparable<FragmentComparison> {
    private static final FragmentComparison INVALID_INSTANCE = new FragmentComparison(Collections.<ResidueComparison> emptyList(), Collections.<MasterTorsionAngleType> emptyList(), 0, 0, 0, 0, Angle.invalidInstance());

    public static FragmentComparison invalidInstance() {
        return FragmentComparison.INVALID_INSTANCE;
    }

    public static FragmentComparison fromResidueComparisons(
            List<ResidueComparison> residueResults,
            List<MasterTorsionAngleType> angleTypes) throws InvalidCircularValueException {
        List<Angle> deltas = new ArrayList<>();
        int targetInvalid = 0;
        int modelInvalid = 0;
        int bothInvalid = 0;

        for (ResidueComparison result : residueResults) {
            for (MasterTorsionAngleType angle : angleTypes) {
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
        return new FragmentComparison(residueResults, angleTypes, targetInvalid, modelInvalid, bothInvalid, deltas.size(), sample.getMeanDirection());
    }

    private final List<ResidueComparison> residueComparisons;
    private final List<MasterTorsionAngleType> angleTypes;
    private final int targetInvalidCount;
    private final int modelInvalidCount;
    private final int bothInvalidCount;
    private final int validCount;
    private final Angle meanDelta;

    public FragmentComparison(List<ResidueComparison> residueResults,
            List<MasterTorsionAngleType> angles, int firstInvalidCount,
            int secondInvalidCount, int bothInvalidCount, int validCount,
            Angle meanDelta) {
        super();
        this.residueComparisons = residueResults;
        this.angleTypes = angles;
        this.targetInvalidCount = firstInvalidCount;
        this.modelInvalidCount = secondInvalidCount;
        this.bothInvalidCount = bothInvalidCount;
        this.validCount = validCount;
        this.meanDelta = meanDelta;
    }

    public List<ResidueComparison> getResidueComparisons() {
        return Collections.unmodifiableList(residueComparisons);
    }

    public List<MasterTorsionAngleType> getAngleTypes() {
        return Collections.unmodifiableList(angleTypes);
    }

    public int getTargetInvalidCount() {
        return targetInvalidCount;
    }

    public int getModelInvalidCount() {
        return modelInvalidCount;
    }

    public int getBothInvalidCount() {
        return bothInvalidCount;
    }

    public int getValidCount() {
        return validCount;
    }

    public Angle getMeanDelta() {
        return meanDelta;
    }

    public int getMismatchCount() {
        return targetInvalidCount + modelInvalidCount;
    }

    public int size() {
        return residueComparisons.size();
    }

    public boolean isValid() {
        return meanDelta.isValid();
    }

    @Override
    public int compareTo(FragmentComparison o) {
        return meanDelta.compareTo(o.meanDelta);
    }
}
