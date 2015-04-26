package pl.poznan.put.matching;

import java.util.List;

import pl.poznan.put.pdb.analysis.PdbResidue;
import pl.poznan.put.torsion.TorsionAngleDelta;
import pl.poznan.put.torsion.type.TorsionAngleType;

public class ResidueComparison {
    private final PdbResidue target;
    private final PdbResidue model;
    private final List<TorsionAngleDelta> angleDeltas;

    public ResidueComparison(PdbResidue target, PdbResidue model,
            List<TorsionAngleDelta> angleDeltas) {
        super();
        this.target = target;
        this.model = model;
        this.angleDeltas = angleDeltas;
    }

    public PdbResidue getTarget() {
        return target;
    }

    public PdbResidue getModel() {
        return model;
    }

    public TorsionAngleDelta getAngleDelta(TorsionAngleType angleType) {
        for (TorsionAngleDelta delta : angleDeltas) {
            if (angleType.equals(delta.getAngleType())) {
                return delta;
            }
        }
        return TorsionAngleDelta.bothInvalidInstance(angleType);
    }
}
