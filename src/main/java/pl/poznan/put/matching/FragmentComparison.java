package pl.poznan.put.matching;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pl.poznan.put.circular.Angle;
import pl.poznan.put.circular.exception.InvalidCircularValueException;
import pl.poznan.put.circular.samples.AnglesSample;
import pl.poznan.put.torsion.TorsionAngleDelta;
import pl.poznan.put.torsion.type.TorsionAngleType;

public class FragmentComparison implements Comparable<FragmentComparison> {
    public static FragmentComparison fromResidueComparisons(
            List<ResidueComparison> residueResults,
            List<TorsionAngleType> angles) throws InvalidCircularValueException {
        List<Angle> deltas = new ArrayList<>();
        int targetInvalid = 0;
        int modelInvalid = 0;
        int bothInvalid = 0;

        for (ResidueComparison result : residueResults) {
            for (TorsionAngleType angle : angles) {
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
                case DIFFERENT_CHI:
                    bothInvalid++;
                default:
                    break;
                }
            }
        }

        AnglesSample sample = new AnglesSample(deltas);
        return new FragmentComparison(residueResults, angles, targetInvalid, modelInvalid, bothInvalid, deltas.size(), sample.getMeanDirection());
    }

    private final List<ResidueComparison> residueComparisons;
    private final List<TorsionAngleType> angleTypes;
    private final int targetInvalidCount;
    private final int modelInvalidCount;
    private final int bothInvalidCount;
    private final int validCount;
    private final Angle mcq;

    public FragmentComparison(List<ResidueComparison> residueResults,
            List<TorsionAngleType> angles, int firstInvalidCount,
            int secondInvalidCount, int bothInvalidCount, int validCount,
            Angle mcq) {
        super();
        this.residueComparisons = residueResults;
        this.angleTypes = angles;
        this.targetInvalidCount = firstInvalidCount;
        this.modelInvalidCount = secondInvalidCount;
        this.bothInvalidCount = bothInvalidCount;
        this.validCount = validCount;
        this.mcq = mcq;
    }

    public List<ResidueComparison> getResidueComparisons() {
        return Collections.unmodifiableList(residueComparisons);
    }

    public List<TorsionAngleType> getAngleTypes() {
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

    public Angle getMCQ() {
        return mcq;
    }

    public int getMismatchCount() {
        return targetInvalidCount + modelInvalidCount;
    }

    public int size() {
        return residueComparisons.size();
    }

    @Override
    public int compareTo(FragmentComparison o) {
        return mcq.compareTo(o.mcq);
    }
}
