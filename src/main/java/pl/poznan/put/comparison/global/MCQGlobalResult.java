package pl.poznan.put.comparison.global;

import pl.poznan.put.circular.Angle;
import pl.poznan.put.circular.samples.AngleSample;
import pl.poznan.put.matching.SelectionMatch;
import pl.poznan.put.utility.AngleFormat;

public class MCQGlobalResult extends GlobalResult {
    private final AngleSample angleSample;

    public MCQGlobalResult(String measureName, SelectionMatch matches,
            AngleSample angleSample) {
        super(measureName, matches);
        this.angleSample = angleSample;
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
    public String getExportName() {
        return AngleFormat.formatExport(angleSample.getMeanDirection().getRadians());
    }

    @Override
    public String getLongDisplayName() {
        return AngleFormat.formatDisplayLong(angleSample.getMeanDirection().getRadians());
    }

    @Override
    public String getShortDisplayName() {
        return AngleFormat.formatDisplayShort(angleSample.getMeanDirection().getRadians());
    }

    @Override
    public double asDouble() {
        return angleSample.getMeanDirection().getRadians();
    }
}
