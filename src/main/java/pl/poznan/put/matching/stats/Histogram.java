package pl.poznan.put.matching.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class Histogram {
  private final Map<Double, Double> percentages = new HashMap<>();

  Histogram(final double[] limits, final double[] percentages) {
    super();

    assert limits.length == percentages.length;
    for (int i = 0; i < limits.length; i++) {
      this.percentages.put(limits[i], percentages[i]);
    }
  }

  public final Set<Double> getKeys() {
    return percentages.keySet();
  }

  public final Double getPercentage(final Double key) {
    if (!percentages.containsKey(key)) {
      return Double.NaN;
    }
    return percentages.get(key);
  }
}
