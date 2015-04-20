package pl.poznan.put.matching;

import pl.poznan.put.circular.Angle;
import pl.poznan.put.circular.exception.InvalidCircularValueException;
import pl.poznan.put.common.MoleculeType;
import pl.poznan.put.torsion.TorsionAngleValue;
import pl.poznan.put.torsion.type.TorsionAngleType;
import pl.poznan.put.utility.AngleFormat;

public class AngleDelta {
    public enum State {
        TORSION_TARGET_INVALID,
        TORSION_MODEL_INVALID,
        BOTH_INVALID,
        BOTH_VALID,
        DIFFERENT_CHI;
    }

    private final TorsionAngleType torsionAngle;
    private final TorsionAngleValue targetValue;
    private final TorsionAngleValue modelValue;
    private final State state;
    private final double deltaRadians;

    public static AngleDelta calculate(TorsionAngleValue target,
            TorsionAngleValue model) throws InvalidCircularValueException {
        State state;
        double delta = Double.NaN;

        if (!target.isValid() && !model.isValid()) {
            state = State.BOTH_INVALID;
        } else if (!target.isValid() && model.isValid()) {
            state = State.TORSION_TARGET_INVALID;
        } else if (target.isValid() && !model.isValid()) {
            state = State.TORSION_MODEL_INVALID;
        } else {
            state = State.BOTH_VALID;
            delta = Angle.subtract(target.getValue(), model.getValue()).getRadians();
        }

        return new AngleDelta(target, model, state, delta);
    }

    public static AngleDelta calculateChiDelta(TorsionAngleValue target,
            TorsionAngleValue model, boolean matchChiByType) throws InvalidCircularValueException {
        TorsionAngleType torL = target.getAngle();
        TorsionAngleType torR = model.getAngle();

        if (!matchChiByType && torL.getMoleculeType() == MoleculeType.RNA && !torL.equals(torR)) {
            return new AngleDelta(target, model, State.DIFFERENT_CHI, Double.NaN);
        }

        return AngleDelta.calculate(target, model);
    }

    public TorsionAngleValue getTargetValue() {
        return targetValue;
    }

    public TorsionAngleValue getModelValue() {
        return modelValue;
    }

    public State getState() {
        return state;
    }

    public double getDelta() {
        return deltaRadians;
    }

    public TorsionAngleType getTorsionAngle() {
        return torsionAngle;
    }

    @Override
    public String toString() {
        return "AngleDelta [state=" + state + ", targetValue=" + targetValue + ", modelValue=" + modelValue + ", delta=" + deltaRadians + "]";
    }

    /**
     * Represent numeric value in a way external tools understand (dot as
     * fraction point and no UNICODE_DEGREE sign).
     *
     * @return String representation of this delta object understandable by
     *         external tools.
     */
    public String toExportString() {
        return toString(false);
    }

    /**
     * Represent object as a String which will be displayed to user in the GUI.
     *
     * @return String representation of object to be shown in the GUI.
     */
    public String toDisplayString() {
        return toString(true);
    }

    public String toString(boolean isDisplayable) {
        switch (state) {
        case BOTH_INVALID:
            return isDisplayable ? "" : null;
        case BOTH_VALID:
            return isDisplayable ? AngleFormat.formatDisplayShort(deltaRadians) : AngleFormat.formatExport(deltaRadians);
        case TORSION_TARGET_INVALID:
            return "Missing atoms in target";
        case TORSION_MODEL_INVALID:
            return "Missing atoms in model";
        case DIFFERENT_CHI:
            return "Purine/pyrimidine mismatch";
        default:
            return "Error";
        }
    }

    AngleDelta(TorsionAngleType torsionAngle, TorsionAngleValue targetValue,
            TorsionAngleValue modelValue, State state, double delta) {
        super();
        this.torsionAngle = torsionAngle;
        this.targetValue = targetValue;
        this.modelValue = modelValue;
        this.state = state;
        this.deltaRadians = delta;
    }

    AngleDelta(TorsionAngleValue targetValue, TorsionAngleValue modelValue,
            State state, double delta) {
        super();
        assert targetValue.getAngle().equals(modelValue.getAngle());
        torsionAngle = targetValue.getAngle();
        this.targetValue = targetValue;
        this.modelValue = modelValue;
        this.state = state;
        this.deltaRadians = delta;
    }
}
