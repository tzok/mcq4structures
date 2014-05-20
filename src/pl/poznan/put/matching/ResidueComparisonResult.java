package pl.poznan.put.matching;

import java.util.List;

import pl.poznan.put.common.ChiTorsionAngle;
import pl.poznan.put.common.ChiTorsionAngleType;
import pl.poznan.put.common.TorsionAngle;
import pl.poznan.put.structure.ResidueTorsionAngles;

public class ResidueComparisonResult {
    private final ResidueTorsionAngles left;
    private final ResidueTorsionAngles right;
    private final List<TorsionAngleDelta> deltas;

    public ResidueComparisonResult(ResidueTorsionAngles left,
            ResidueTorsionAngles right, List<TorsionAngleDelta> deltas) {
        super();
        this.left = left;
        this.right = right;
        this.deltas = deltas;
    }

    public ResidueTorsionAngles getLeft() {
        return left;
    }

    public ResidueTorsionAngles getRight() {
        return right;
    }

    public TorsionAngleDelta getDelta(TorsionAngle angle) {
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
