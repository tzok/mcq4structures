package pl.poznan.put.comparison;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import pl.poznan.put.helper.Constants;
import pl.poznan.put.matching.FragmentMatch;

public class GlobalComparisonResult {
    private static final NumberFormat FORMAT = new DecimalFormat("0.000");

    private final String measureName;
    private final String nameLeft;
    private final String nameRight;
    private final List<FragmentMatch> matches;
    private final double value;
    private final boolean isAngle;

    public GlobalComparisonResult(String measureName, String nameLeft,
            String nameRight, List<FragmentMatch> matches, double value,
            boolean isAngle) {
        super();
        this.measureName = measureName;
        this.nameLeft = nameLeft;
        this.nameRight = nameRight;
        this.matches = matches;
        this.value = value;
        this.isAngle = isAngle;
    }

    public String getMeasureName() {
        return measureName;
    }

    public String getNameLeft() {
        return nameLeft;
    }

    public String getNameRight() {
        return nameRight;
    }

    public List<FragmentMatch> getMatches() {
        return matches;
    }

    public double getValue() {
        return value;
    }

    @Override
    public String toString() {
        return Double.toString(value);
    }

    public String toDisplayString() {
        if (isAngle) {
            return GlobalComparisonResult.FORMAT.format(Math.toDegrees(value))
                    + Constants.UNICODE_DEGREE;
        }

        return GlobalComparisonResult.FORMAT.format(value)
                + Constants.UNICODE_ANGSTROM;
    }
}
