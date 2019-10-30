package pl.poznan.put.matching.stats;

import org.apache.commons.math3.stat.StatUtils;
import pl.poznan.put.circular.Angle;
import pl.poznan.put.matching.AngleDeltaIterator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

public final class SingleMatchStatistics {
  static final double[] DEFAULT_PERCENTS_LIMITS = {95.0, 75.0, 50.0, 25.0};
  static final double[] DEFAULT_ANGLE_LIMITS = {
    Math.toRadians(15.0), Math.toRadians(30.0),
    Math.toRadians(45.0), Math.toRadians(60.0)
  };

  private final String name;
  private final Histogram histogram;
  private final Percentiles percentiles;

  private SingleMatchStatistics(
      final String name, final Histogram histogram, final Percentiles percentiles) {
    super();
    this.name = name;
    this.histogram = histogram;
    this.percentiles = percentiles;
  }

  public static SingleMatchStatistics calculate(
      final String name, final AngleDeltaIterator angleDeltaIterator) {
    return SingleMatchStatistics.calculate(
        name,
        angleDeltaIterator,
        SingleMatchStatistics.DEFAULT_ANGLE_LIMITS,
        SingleMatchStatistics.DEFAULT_PERCENTS_LIMITS);
  }

  public static SingleMatchStatistics calculate(
      final String name,
      final AngleDeltaIterator angleDeltaIterator,
      final double[] angleLimits,
      final double[] percentsLimits) {
    final Collection<Double> validDeltas = new ArrayList<>();
    final double[] validDeltasCountRatio = new double[angleLimits.length];

    while (angleDeltaIterator.hasNext()) {
      final Angle angle = angleDeltaIterator.next();

      if (angle.isValid()) {
        final double delta = angle.getRadians();
        validDeltas.add(delta);

        for (int j = 0; j < angleLimits.length; j++) {
          if (Double.compare(delta, angleLimits[j]) < 0) {
            validDeltasCountRatio[j] += 1.0;
          }
        }
      }
    }

    final double[] validPercents = new double[percentsLimits.length];

    if (!validDeltas.isEmpty()) {
      for (int i = 0; i < angleLimits.length; i++) {
        validDeltasCountRatio[i] /= validDeltas.size();
      }
    }

    if (!validDeltas.isEmpty()) {
      final double[] values = validDeltas.stream().mapToDouble(validDelta -> validDelta).toArray();

      for (int i = 0; i < percentsLimits.length; i++) {
        validPercents[i] = StatUtils.percentile(values, percentsLimits[i]);
      }
    }

    final Histogram histogram = new Histogram(angleLimits, validDeltasCountRatio);
    final Percentiles percentiles = new Percentiles(percentsLimits, validPercents);
    return new SingleMatchStatistics(name, histogram, percentiles);
  }

  public String getName() {
    return name;
  }

  public Set<Double> getAvailableThresholds() {
    return histogram.getKeys();
  }

  public Set<Double> getAvailablePercentiles() {
    return percentiles.getKeys();
  }

  public double getRatioOfDeltasBelowThreshold(final double threshold) {
    return histogram.getPercentage(threshold);
  }

  public double getAngleThresholdForGivenPercentile(final double percentile) {
    assert percentile > 0.0 && percentile <= 100.0;
    return percentiles.getPercentile(percentile);
  }
}
