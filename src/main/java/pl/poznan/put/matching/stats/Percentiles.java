package pl.poznan.put.matching.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Percentiles {
  private final Map<Double, Double> percentiles = new HashMap<>();

  public Percentiles(double[] percents, double[] percentiles) {
    super();

    assert percents.length == percentiles.length;
    for (int i = 0; i < percents.length; i++) {
      this.percentiles.put(percents[i], percentiles[i]);
    }
  }

  public Set<Double> getKeys() {
    return percentiles.keySet();
  }

  public Double getPercentile(Double key) {
    if (!percentiles.containsKey(key)) {
      return Double.NaN;
    }
    return percentiles.get(key);
  }
}
