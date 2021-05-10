package pl.poznan.put.comparison.global;

import org.immutables.value.Value;
import pl.poznan.put.circular.Angle;
import pl.poznan.put.circular.samples.AngleSample;
import pl.poznan.put.matching.AngleDeltaIterator;
import pl.poznan.put.matching.MatchCollectionDeltaIterator;
import pl.poznan.put.matching.SelectionMatch;
import pl.poznan.put.matching.stats.SingleMatchStatistics;
import pl.poznan.put.utility.AngleFormat;
import pl.poznan.put.utility.NumberFormatUtils;

@Value.Immutable
public abstract class MCQGlobalResult implements GlobalResult {
  @Value.Parameter(order = 2)
  public abstract AngleSample angleSample();

  public final Angle meanDirection() {
    return angleSample().meanDirection();
  }

  public final Angle medianDirection() {
    return angleSample().medianDirection();
  }

  @Override
  public final String toString() {
    return angleSample().toString();
  }

  @Override
  public final String shortDisplayName() {
    return AngleFormat.degreesRoundedToHundredth(angleSample().meanDirection().radians());
  }

  @Value.Lazy
  public String longDisplayName() {
    final SelectionMatch selectionMatch = selectionMatch();
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

  @Override
  public final String exportName() {
    return AngleFormat.degrees(angleSample().meanDirection().radians());
  }

  @Override
  public final String measureName() {
    return "MCQ";
  }

  @Override
  @Value.Parameter(order = 1)
  public abstract SelectionMatch selectionMatch();

  @Override
  public final double toDouble() {
    return angleSample().meanDirection().radians();
  }
}
