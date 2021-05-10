package pl.poznan.put.matching.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class Percentiles {
  private final Map<Double, Double> percentiles = new HashMap<>();

  Percentiles(final double[] percents, final double[] percentiles) {
    super();

    assert percents.length == percentiles.length;
    for (int i = 0; i < percents.length; i++) {
      this.percentiles.put(percents[i], percentiles[i]);
    }
  }

  public final Set<Double> getKeys() {
    return percentiles.keySet();
  }

  public final Double getPercentile(final Double key) {
    if (!percentiles.containsKey(key)) {
      return Double.NaN;
    }
    return percentiles.get(key);
  }
}
