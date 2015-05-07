package pl.poznan.put.matching;

import java.util.ArrayList;
import java.util.List;

import pl.poznan.put.circular.Angle;
import pl.poznan.put.circular.samples.AngleSample;
import pl.poznan.put.pdb.analysis.PdbResidue;
import pl.poznan.put.torsion.TorsionAngleDelta;
import pl.poznan.put.torsion.TorsionAngleDelta.State;
import pl.poznan.put.torsion.type.MasterTorsionAngleType;

public class ResidueComparison {
    private final PdbResidue target;
    private final PdbResidue model;
    private final List<TorsionAngleDelta> angleDeltas;
    private final AngleSample angleSample;

    public ResidueComparison(PdbResidue target, PdbResidue model,
            List<TorsionAngleDelta> angleDeltas) {
        super();
        this.target = target;
        this.model = model;
        this.angleDeltas = angleDeltas;
        this.angleSample = new AngleSample(extractValidDeltas());
    }

    private List<Angle> extractValidDeltas() {
        List<Angle> angles = new ArrayList<>();
        for (TorsionAngleDelta angleDelta : angleDeltas) {
            if (angleDelta.getState() == State.BOTH_VALID) {
                angles.add(angleDelta.getDelta());
            }
        }
        return angles;
    }

    public PdbResidue getTarget() {
        return target;
    }

    public PdbResidue getModel() {
        return model;
    }

    public TorsionAngleDelta getAngleDelta(MasterTorsionAngleType masterType) {
        for (TorsionAngleDelta delta : angleDeltas) {
            if (masterType.equals(delta.getMasterTorsionAngleType())) {
                return delta;
            }
        }
        return TorsionAngleDelta.bothInvalidInstance(masterType);
    }

    public Angle getMeanDirection() {
        return angleSample.getMeanDirection();
    }

    public Angle getMedianDirection() {
        return angleSample.getMedianDirection();
    }
}
