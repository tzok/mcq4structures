package pl.poznan.put.visualisation;

import pl.poznan.put.circular.Angle;
import pl.poznan.put.circular.samples.AngleSample;
import pl.poznan.put.matching.ResidueComparison;
import pl.poznan.put.torsion.MasterTorsionAngleType;
import pl.poznan.put.torsion.TorsionAngleDelta;

import java.util.ArrayList;
import java.util.List;

/**
 * Map {@link TorsionAngleDelta} onto 0-1 scale.
 */
public final class AngleDeltaMapper implements ComparisonMapper {
    private static final AngleDeltaMapper INSTANCE = new AngleDeltaMapper();

    public static AngleDeltaMapper getInstance() {
        return AngleDeltaMapper.INSTANCE;
    }

    private AngleDeltaMapper() {
        super();
    }

    @Override
    public Double[] map(final List<ResidueComparison> residueComparisons,
                        final List<MasterTorsionAngleType> angleTypes) {
        final Double[] result = new Double[residueComparisons.size()];

        for (int i = 0; i < residueComparisons.size(); i++) {
            final ResidueComparison residueComparison =
                    residueComparisons.get(i);
            final List<Angle> deltas = new ArrayList<>(angleTypes.size());

            for (final MasterTorsionAngleType angleType : angleTypes) {
                final TorsionAngleDelta angleDelta =
                        residueComparison.getAngleDelta(angleType);
                final TorsionAngleDelta.State state = angleDelta.getState();

                if (state == TorsionAngleDelta.State.BOTH_VALID) {
                    final Angle delta = angleDelta.getDelta();
                    deltas.add(delta);
                }
            }

            final AngleSample sample = new AngleSample(deltas);
            final Angle meanDirection = sample.getMeanDirection();
            final double degrees = meanDirection.getDegrees360();

            if (degrees < 15.0) {
                result[i] = 0.0;
            } else if (degrees < 30.0) {
                result[i] = 1.0 / 3.0;
            } else if (degrees < 60.0) {
                result[i] = 2.0 / 3.0;
            } else {
                result[i] = 1.0;
            }
        }

        return result;
    }
}
