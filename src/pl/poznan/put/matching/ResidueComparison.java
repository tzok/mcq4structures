package pl.poznan.put.matching;

import java.util.List;

import pl.poznan.put.structure.ResidueAngles;
import pl.poznan.put.torsion.ChiTorsionAngle;
import pl.poznan.put.torsion.ChiTorsionAngleType;
import pl.poznan.put.torsion.TorsionAngle;
import pl.poznan.put.utility.TorsionAngleDelta;

public class ResidueComparison {
    private final ResidueAngles left;
    private final ResidueAngles right;
    private final List<TorsionAngleDelta> deltas;

    public ResidueComparison(ResidueAngles left, ResidueAngles right,
            List<TorsionAngleDelta> deltas) {
        super();
        this.left = left;
        this.right = right;
        this.deltas = deltas;
    }

    public ResidueAngles getLeft() {
        return left;
    }

    public ResidueAngles getRight() {
        return right;
    }

    public TorsionAngleDelta getAngleDelta(TorsionAngle angle) {
        for (TorsionAngleDelta delta : deltas) {
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
}
