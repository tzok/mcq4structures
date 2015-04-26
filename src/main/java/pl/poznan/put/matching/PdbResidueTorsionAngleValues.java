package pl.poznan.put.matching;

import java.util.List;

import pl.poznan.put.pdb.analysis.PdbResidue;
import pl.poznan.put.torsion.TorsionAngleValue;
import pl.poznan.put.torsion.type.TorsionAngleType;

public class PdbResidueTorsionAngleValues {
    private final PdbResidue residue;
    private final List<TorsionAngleValue> torsionAngleValues;

    public PdbResidueTorsionAngleValues(PdbResidue residue,
            List<TorsionAngleValue> angleValues) {
        this.residue = residue;
        this.torsionAngleValues = angleValues;
    }

    public PdbResidue getResidue() {
        return residue;
    }

    public TorsionAngleValue getAngleValue(TorsionAngleType torsionAngle) {
        for (TorsionAngleValue angleValue : torsionAngleValues) {
            TorsionAngleType angle = angleValue.getAngleType();
            if (angle.equals(torsionAngle)) {
                return angleValue;
            }
        }
        return TorsionAngleValue.invalidInstance(torsionAngle);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(residue);

        for (TorsionAngleValue angle : torsionAngleValues) {
            builder.append('\t');
            builder.append(angle.getAngleType());
            builder.append('\t');
            builder.append(angle.getValue().getDegrees());
        }

        return builder.toString();
    }
}
