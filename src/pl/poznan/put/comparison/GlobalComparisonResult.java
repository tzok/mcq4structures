package pl.poznan.put.comparison;

import java.util.List;

import pl.poznan.put.helper.Constants;
import pl.poznan.put.matching.FragmentMatch;
import pl.poznan.put.utility.NumberFormatter;

public class GlobalComparisonResult {
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

    /**
     * Represent numeric value in a way external tools understand (dot as
     * fraction point and no UNICODE_DEGREE sign).
     * 
     * @return String representation of this delta object understandable by
     *         external tools.
     */
    public String toExportString() {
        if (isAngle) {
            return NumberFormatter.format(Math.toDegrees(value));
        }

        return NumberFormatter.format(value);
    }

    /**
     * Represent object as a String which will be displayed to user in the GUI.
     * 
     * @return String representation of object to be shown in the GUI.
     */
    public String toDisplayString() {
        String result = toExportString();

        if (isAngle) {
            result += Constants.UNICODE_DEGREE;
        } else {
            result += Constants.UNICODE_ANGSTROM;
        }

        return result;
    }
}
