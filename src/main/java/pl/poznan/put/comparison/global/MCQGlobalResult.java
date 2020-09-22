package pl.poznan.put.comparison.global;

import pl.poznan.put.circular.Angle;
import pl.poznan.put.circular.samples.AngleSample;
import pl.poznan.put.matching.AngleDeltaIterator;
import pl.poznan.put.matching.MatchCollectionDeltaIterator;
import pl.poznan.put.matching.SelectionMatch;
import pl.poznan.put.matching.stats.SingleMatchStatistics;
import pl.poznan.put.utility.AngleFormat;
import pl.poznan.put.utility.NumberFormatUtils;

public class MCQGlobalResult extends GlobalResult {
  private final AngleSample angleSample;
  private final String longDisplayName;

  public MCQGlobalResult(
      final String measureName,
      final SelectionMatch selectionMatch,
      final AngleSample angleSample) {
    super(measureName, selectionMatch);
    this.angleSample = angleSample;
    longDisplayName = prepareLongDisplayName();
  }

  public final Angle getMeanDirection() {
    return angleSample.meanDirection();
  }

  public final Angle getMedianDirection() {
    return angleSample.medianDirection();
  }

  @Override
  public final String toString() {
    return angleSample.toString();
  }

  @Override
  public final String shortDisplayName() {
    return AngleFormat.degreesRoundedToHundredth(angleSample.meanDirection().radians());
  }

  @Override
  public final String longDisplayName() {
    return longDisplayName;
  }

  @Override
  public final String exportName() {
    return AngleFormat.degrees(angleSample.meanDirection().radians());
  }

  @Override
  public final double asDouble() {
    return angleSample.meanDirection().radians();
  }

  private String prepareLongDisplayName() {
    final SelectionMatch selectionMatch = getSelectionMatch();
    final AngleDeltaIterator angleDeltaIterator = new MatchCollectionDeltaIterator(selectionMatch);
    final SingleMatchStatistics statistics =
        SingleMatchStatistics.calculate("", angleDeltaIterator);

    final int validCount = selectionMatch.getResidueLabels().size();
    final double percentBelow30Deg =
        100.0 * statistics.getRatioOfDeltasBelowThreshold(Math.toRadians(30));

    return String.format(
        "<html>%s<br>%d<br>%s%%</html>",
        shortDisplayName(),
        validCount,
        NumberFormatUtils.threeDecimalDigits().format(percentBelow30Deg));
  }
}
