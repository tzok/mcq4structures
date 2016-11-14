package pl.poznan.put.comparison.global;

import pl.poznan.put.circular.Angle;
import pl.poznan.put.circular.samples.AngleSample;
import pl.poznan.put.matching.AngleDeltaIterator;
import pl.poznan.put.matching.MatchCollectionDeltaIterator;
import pl.poznan.put.matching.SelectionMatch;
import pl.poznan.put.matching.stats.SingleMatchStatistics;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.utility.AngleFormat;
import pl.poznan.put.utility.CommonNumberFormat;


public class LCSGlobalResult extends GlobalResult {
    private final AngleSample angleSample;
    private final String longDisplayName;

    public LCSGlobalResult(String measureName, SelectionMatch selectionMatch,
                           AngleSample angleSample, StructureSelection model, StructureSelection target) {
        super(measureName, selectionMatch);
        this.angleSample = angleSample;
        this.longDisplayName = prepareLongDisplayName();
    }

    private String prepareLongDisplayName() {
        SelectionMatch selectionMatch = getSelectionMatch();
        AngleDeltaIterator angleDeltaIterator =
                new MatchCollectionDeltaIterator(selectionMatch);
        SingleMatchStatistics statistics =
                SingleMatchStatistics.calculate("", angleDeltaIterator);

        int validCount = selectionMatch.getResidueLabels().size();


        StringBuilder builder = new StringBuilder("<html>");
        builder.append(getShortDisplayName());
        builder.append("<br>");
        builder.append(validCount);
        builder.append("<br>");
        builder.append("</html>");
        return builder.toString();
    }

    public Angle getMeanDirection() {
        return angleSample.getMeanDirection();
    }

    public Angle getMedianDirection() {
        return angleSample.getMedianDirection();
    }

    @Override
    public String toString() {
        return angleSample.toString();
    }

    @Override
    public String getLongDisplayName() {
        return longDisplayName;
    }

    @Override
    public String getShortDisplayName() {
        return AngleFormat.formatDisplayShort(
                angleSample.getMeanDirection().getRadians());
    }

    @Override
    public String getExportName() {
        return AngleFormat
                .formatExport(angleSample.getMeanDirection().getRadians());
    }

    @Override
    public double asDouble() {
        return angleSample.getMeanDirection().getRadians();
    }
}
