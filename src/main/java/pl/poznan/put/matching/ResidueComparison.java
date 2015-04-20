package pl.poznan.put.matching;

import java.util.Iterator;
import java.util.List;

import pl.poznan.put.torsion.ChiTorsionAngle;
import pl.poznan.put.torsion.ChiTorsionAngleType;
import pl.poznan.put.torsion.TorsionAngle;

public class ResidueComparison implements Iterable<AngleDelta> {
    private final ResidueAngles targetAngles;
    private final ResidueAngles modelAngles;
    private final List<AngleDelta> angleDeltas;

    public ResidueComparison(ResidueAngles targetAngles,
            ResidueAngles modelAngles, List<AngleDelta> angleDeltas) {
        super();
        this.targetAngles = targetAngles;
        this.modelAngles = modelAngles;
        this.angleDeltas = angleDeltas;
    }

    public ResidueAngles getTargetAngles() {
        return targetAngles;
    }

    public ResidueAngles getModelAngles() {
        return modelAngles;
    }

    public AngleDelta getAngleDelta(TorsionAngle angle) {
        for (AngleDelta delta : angleDeltas) {
            TorsionAngle torsionAngle = delta.getTorsionAngle();

            if (angle instanceof ChiTorsionAngleType) {
                ChiTorsionAngleType type = (ChiTorsionAngleType) angle;
                if (torsionAngle instanceof ChiTorsionAngle
                        && ((ChiTorsionAngle) torsionAngle).getType() == type) {
                    return delta;
                }
            }

            if (torsionAngle.equals(angle)) {
                return delta;
            }
        }

        return null;
    }

    @Override
    public Iterator<AngleDelta> iterator() {
        return angleDeltas.iterator();
    }
}
