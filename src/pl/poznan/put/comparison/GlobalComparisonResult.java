package pl.poznan.put.comparison;

import pl.poznan.put.constant.Unicode;
import pl.poznan.put.interfaces.DisplayableExportable;
import pl.poznan.put.matching.SelectionMatch;
import pl.poznan.put.utility.CommonNumberFormat;

public class GlobalComparisonResult implements DisplayableExportable {
    private final String measureName;
    private final SelectionMatch matches;
    private final double value;
    private final boolean isAngle;

    public GlobalComparisonResult(String measureName, SelectionMatch matches,
            double value, boolean isAngle) {
        super();
        this.measureName = measureName;
        this.matches = matches;
        this.value = value;
        this.isAngle = isAngle;
    }

    public String getMeasureName() {
        return measureName;
    }

    public String getTargetName() {
        return matches.getTarget().getName();
    }

    public String getModelName() {
        return matches.getModel().getName();
    }

    public SelectionMatch getSelectionMatch() {
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
    @Override
    public String getExportName() {
        if (isAngle) {
            return CommonNumberFormat.formatDouble(Math.toDegrees(value));
        }

        return CommonNumberFormat.formatDouble(value);
    }

    /**
     * Represent object as a String which will be displayed to user in the GUI.
     * 
     * @return String representation of object to be shown in the GUI.
     */
    @Override
    public String getDisplayName() {
        String result = getExportName();

        if (isAngle) {
            result += Unicode.DEGREE;
        } else {
            result += Unicode.ANGSTROM;
        }

        return result;
    }
}
