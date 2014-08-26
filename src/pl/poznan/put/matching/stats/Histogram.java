package pl.poznan.put.matching.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Histogram {
    private final Map<Double, Double> percentages = new HashMap<>();

    public Histogram(double[] limits, double[] percentages) {
        super();

        assert limits.length == percentages.length;
        for (int i = 0; i < limits.length; i++) {
            this.percentages.put(limits[i], percentages[i]);
        }
    }

    public Set<Double> getKeys() {
        return percentages.keySet();
    }

    public Double getPercentage(Double key) {
        if (!percentages.containsKey(key)) {
            return Double.NaN;
        }
        return percentages.get(key);
    }
}
