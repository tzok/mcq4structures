package pl.poznan.put.matching.stats;

import java.util.Set;

import pl.poznan.put.matching.FragmentMatch;

public class MatchStatistics {
    public static final double[] DEFAULT_PERCENTS_LIMITS = new double[] { 95.0, 75.0, 50.0, 25.0 };
    public static final double[] DEFAULT_ANGLE_LIMITS = new double[] { Math.toRadians(15), Math.toRadians(30), Math.toRadians(45), Math.toRadians(60) };

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
