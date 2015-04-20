package pl.poznan.put.matching;

import java.util.Iterator;
import java.util.List;

import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.io.mmcif.chem.ResidueType;

import pl.poznan.put.torsion.TorsionAngleValue;
import pl.poznan.put.torsion.type.TorsionAngleType;

public class ResidueAngles implements Iterable<TorsionAngleValue> {
    private final CompactFragment fragment;
    private final Group group;
    private final ResidueType residueType;
    private final List<TorsionAngleValue> angles;

    public ResidueAngles(CompactFragment fragment, Group group,
            ResidueType residueType, List<TorsionAngleValue> angles) {
        this.fragment = fragment;
        this.group = group;
        this.residueType = residueType;
        this.angles = angles;
    }

    public CompactFragment getFragment() {
        return fragment;
    }

    public Group getGroup() {
        return group;
    }

    public ResidueType getResidueType() {
        return residueType;
    }

    public int getSize() {
        return angles.size();
    }

    public TorsionAngleValue getAngleValue(TorsionAngleType torsionAngle) {
        for (TorsionAngleValue angleValue : angles) {
            TorsionAngleType angle = angleValue.getAngle();

            if (torsionAngle instanceof ChiTorsionAngleType) {
                ChiTorsionAngleType type = (ChiTorsionAngleType) torsionAngle;

                if (angle instanceof ChiTorsionAngle && ((ChiTorsionAngle) angle).getAngleType() == type) {
                    return angleValue;
                }
            } else if (angle.equals(torsionAngle)) {
                return angleValue;
            }
        }

        return TorsionAngleValue.getInvalidInstance(torsionAngle);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(Residue.fromGroup(group));

        for (TorsionAngleValue tav : angles) {
            builder.append('\t');
            builder.append(tav.getAngle());
            builder.append('\t');
            builder.append(Math.toDegrees(tav.getValue()));
        }

        return builder.toString();
    }

    @Override
    public Iterator<TorsionAngleValue> iterator() {
        return angles.iterator();
    }
}
