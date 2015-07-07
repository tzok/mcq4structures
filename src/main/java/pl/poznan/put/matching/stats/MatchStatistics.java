package pl.poznan.put.matching.stats;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.stat.StatUtils;

import pl.poznan.put.matching.FragmentMatch;
import pl.poznan.put.matching.ResidueComparison;
import pl.poznan.put.torsion.MasterTorsionAngleType;
import pl.poznan.put.torsion.TorsionAngleDelta;
import pl.poznan.put.torsion.TorsionAngleDelta.State;

public class MatchStatistics {
    public static final double[] DEFAULT_PERCENTS_LIMITS = new double[] { 95.0, 75.0, 50.0, 25.0 };
    public static final double[] DEFAULT_ANGLE_LIMITS = new double[] { Math.toRadians(15), Math.toRadians(30), Math.toRadians(45), Math.toRadians(60) };
    // @formatter:off
    public static final double[] PERCENTS_FROM_1_TO_100 = new double[] {
         1,  2,  3,  4,  5,  6,  7,  8,  9,  10,
        11, 12, 13, 14, 15, 16, 17, 18, 19,  20,
        21, 22, 23, 24, 25, 26, 27, 28, 29,  30,
        31, 32, 33, 34, 35, 36, 37, 38, 39,  40,
        41, 42, 43, 44, 45, 46, 47, 48, 49,  50,
        51, 52, 53, 54, 55, 56, 57, 58, 59,  60,
        61, 62, 63, 64, 65, 66, 67, 68, 69,  70,
        71, 72, 73, 74, 75, 76, 77, 78, 79,  80,
        81, 82, 83, 84, 85, 86, 87, 88, 89,  90,
        91, 92, 93, 94, 95, 96, 97, 98, 99, 100
    };
    // @formatter:on

    public static MatchStatistics calculate(FragmentMatch fragmentMatch,
            MasterTorsionAngleType angleType) {
        return MatchStatistics.calculate(fragmentMatch, angleType, MatchStatistics.DEFAULT_ANGLE_LIMITS, MatchStatistics.DEFAULT_PERCENTS_LIMITS);
    }

    public static MatchStatistics calculate(FragmentMatch fragmentMatch,
            MasterTorsionAngleType angleType, double[] angleLimits,
            double[] percentsLimits) {
        List<Double> validDeltas = new ArrayList<>();
        double[] validDeltasCountRatio = new double[angleLimits.length];

        for (int i = 0; i < fragmentMatch.size(); i++) {
            ResidueComparison residueComparison = fragmentMatch.getResidueComparisons().get(i);
            TorsionAngleDelta angleDelta = residueComparison.getAngleDelta(angleType);

            if (angleDelta.getState() == State.BOTH_VALID) {
                double delta = angleDelta.getDelta().getRadians();
                validDeltas.add(delta);

                for (int j = 0; j < angleLimits.length; j++) {
                    if (Double.compare(delta, angleLimits[j]) < 0) {
                        validDeltasCountRatio[j] += 1.0;
                    }
                }
            }
        }

        double[] validPercents = new double[percentsLimits.length];

        if (validDeltas.size() > 0) {
            for (int i = 0; i < angleLimits.length; i++) {
                validDeltasCountRatio[i] /= validDeltas.size();
            }

            double[] values = new double[validDeltas.size()];
            for (int i = 0; i < validDeltas.size(); i++) {
                values[i] = validDeltas.get(i);
            }

            for (int i = 0; i < percentsLimits.length; i++) {
                validPercents[i] = StatUtils.percentile(values, percentsLimits[i]);
            }
        }

        Histogram histogram = new Histogram(angleLimits, validDeltasCountRatio);
        Percentiles percentiles = new Percentiles(percentsLimits, validPercents);
        return new MatchStatistics(fragmentMatch, histogram, percentiles);
    }

    private final FragmentMatch match;
    private final Histogram histogram;
    private final Percentiles percentiles;

    public MatchStatistics(FragmentMatch match, Histogram histogram,
            Percentiles percentiles) {
        super();
        this.match = match;
        this.histogram = histogram;
        this.percentiles = percentiles;
    }

    public FragmentMatch getMatch() {
        return match;
    }

    public Set<Double> getAvailableThresholds() {
        return histogram.getKeys();
    }

    public Set<Double> getAvailablePercentiles() {
        return percentiles.getKeys();
    }

    public double getRatioOfDeltasBelowThreshold(double threshold) {
        return histogram.getPercentage(threshold);
    }

    public double getAngleThresholdForGivenPercentile(double percentile) {
        assert percentile >= 0.0 && percentile <= 100.0;
        return percentiles.getPercentile(percentile);
    }
}
