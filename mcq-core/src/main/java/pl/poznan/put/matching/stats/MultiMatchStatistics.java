package pl.poznan.put.matching.stats;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import javax.swing.table.TableModel;
import lombok.Data;
import pl.poznan.put.matching.AngleDeltaIterator;
import pl.poznan.put.matching.AngleDeltaIteratorFactory;
import pl.poznan.put.matching.FragmentMatch;
import pl.poznan.put.matching.MatchCollection;
import pl.poznan.put.utility.AngleFormat;
import pl.poznan.put.utility.NonEditableDefaultTableModel;
import pl.poznan.put.utility.NumberFormatUtils;

@Data
public final class MultiMatchStatistics {
  @Data
  public static final class HistogramEntry implements Comparable<HistogramEntry> {
    private final double ratio;

    @Override
    public int compareTo(final HistogramEntry t) {
      return Double.compare(ratio, t.ratio);
    }

    @Override
    public String toString() {
      return NumberFormatUtils.threeDecimalDigits().format(100.0 * ratio) + '%';
    }
  }

  private static final class HistogramTableModel extends NonEditableDefaultTableModel {
    private static final long serialVersionUID = 5272719525300314601L;

    private HistogramTableModel(final Object[][] data, final String[] columnNames) {
      super(data, columnNames);
    }

    @Override
    public Class<?> getColumnClass(final int i) {
      return (i == 0) ? String.class : HistogramEntry.class;
    }
  }

  @Data
  public static final class PercentileEntry implements Comparable<PercentileEntry> {
    private final double threshold;
    private final boolean isDisplayable;

    @Override
    public int compareTo(final PercentileEntry t) {
      return Double.compare(threshold, t.threshold);
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) {
        return true;
      }
      if ((o == null) || (getClass() != o.getClass())) {
        return false;
      }
      final PercentileEntry other = (PercentileEntry) o;
      return Double.compare(other.threshold, threshold) == 0;
    }

    @Override
    public int hashCode() {
      return Objects.hash(threshold);
    }

    @Override
    public String toString() {
      return isDisplayable
          ? AngleFormat.degreesRoundedToHundredth(threshold)
          : AngleFormat.degrees(threshold);
    }
  }

  private static final class PercentileTableModel extends NonEditableDefaultTableModel {
    private static final long serialVersionUID = 5272719525300314601L;

    private PercentileTableModel(final Object[][] data, final String[] columnNames) {
      super(data, columnNames);
    }

    @Override
    public Class<?> getColumnClass(final int i) {
      return (i == 0) ? String.class : PercentileEntry.class;
    }
  }

  private final List<SingleMatchStatistics> statistics;
  private final double[] angleLimits;
  private final double[] percentsLimits;

  public static MultiMatchStatistics calculate(
      final AngleDeltaIteratorFactory iteratorFactory, final MatchCollection matchesCollection) {
    final Collection<FragmentMatch> fragmentMatches = matchesCollection.getFragmentMatches();
    final List<SingleMatchStatistics> statistics = new ArrayList<>(fragmentMatches.size());

    for (final FragmentMatch fragmentMatch : fragmentMatches) {
      final String name = fragmentMatch.getModelFragment().getName();
      final AngleDeltaIterator deltaIterator = iteratorFactory.createInstance(fragmentMatch);
      statistics.add(SingleMatchStatistics.calculate(name, deltaIterator));
    }

    return new MultiMatchStatistics(
        statistics,
        SingleMatchStatistics.DEFAULT_ANGLE_LIMITS,
        SingleMatchStatistics.DEFAULT_PERCENTS_LIMITS);
  }

  public int getSize() {
    return statistics.size();
  }

  public SingleMatchStatistics getMatchStatistics(final int index) {
    return statistics.get(index);
  }

  public TableModel histogramsAsTableModel(final boolean isDisplayable) {
    final String[] columnNames = new String[angleLimits.length + 1];
    //noinspection AssignmentToNull
    columnNames[0] = isDisplayable ? "" : null;

    for (int i = 0; i < angleLimits.length; i++) {
      columnNames[i + 1] = AngleFormat.degreesRoundedToOne(angleLimits[i]);
    }

    final Object[][] data = new Object[statistics.size()][];
    for (int i = 0; i < statistics.size(); i++) {
      final SingleMatchStatistics match = statistics.get(i);
      data[i] = new Object[angleLimits.length + 1];
      data[i][0] = match.getName();

      for (int j = 0; j < angleLimits.length; j++) {
        data[i][j + 1] = new HistogramEntry(match.getRatioOfDeltasBelowThreshold(angleLimits[j]));
      }
    }

    return new HistogramTableModel(data, columnNames);
  }

  public TableModel percentilesAsTableModel(final boolean isDisplayable) {
    final String[] columnNames = new String[percentsLimits.length + 1];
    //noinspection AssignmentToNull
    columnNames[0] = isDisplayable ? "" : null;
    for (int i = 0; i < percentsLimits.length; i++) {
      columnNames[i + 1] = NumberFormatUtils.threeDecimalDigits().format(percentsLimits[i]) + '%';
    }

    final Object[][] data = new Object[statistics.size()][];
    for (int i = 0; i < statistics.size(); i++) {
      final SingleMatchStatistics match = statistics.get(i);
      data[i] = new Object[percentsLimits.length + 1];
      data[i][0] = match.getName();

      for (int j = 0; j < percentsLimits.length; j++) {
        final double threshold = match.getAngleThresholdForGivenPercentile(percentsLimits[j]);
        data[i][j + 1] = new PercentileEntry(threshold, isDisplayable);
      }
    }

    return new PercentileTableModel(data, columnNames);
  }
}
