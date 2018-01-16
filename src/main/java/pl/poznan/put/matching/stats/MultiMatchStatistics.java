package pl.poznan.put.matching.stats;

import pl.poznan.put.gui.component.NonEditableDefaultTableModel;
import pl.poznan.put.matching.AngleDeltaIterator;
import pl.poznan.put.matching.AngleDeltaIteratorFactory;
import pl.poznan.put.matching.FragmentMatch;
import pl.poznan.put.matching.MatchCollection;
import pl.poznan.put.utility.AngleFormat;
import pl.poznan.put.utility.CommonNumberFormat;

import javax.annotation.Nonnull;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class MultiMatchStatistics {
  public static final class HistogramEntry implements Comparable<HistogramEntry> {
    private final double ratio;

    private HistogramEntry(final double ratio) {
      super();
      this.ratio = ratio;
    }

    @Override
    public int compareTo(@Nonnull final HistogramEntry t) {
      return Double.compare(ratio, t.ratio);
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) {
        return true;
      }
      if ((o == null) || (getClass() != o.getClass())) {
        return false;
      }
      final HistogramEntry other = (HistogramEntry) o;
      return Double.compare(other.ratio, ratio) == 0;
    }

    @Override
    public int hashCode() {
      return Objects.hash(ratio);
    }

    @Override
    public String toString() {
      return CommonNumberFormat.formatDouble(100.0 * ratio) + '%';
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

  public static final class PercentileEntry implements Comparable<PercentileEntry> {
    private final double threshold;
    private final boolean isDisplayable;

    private PercentileEntry(final double threshold, final boolean isDisplayable) {
      super();
      this.threshold = threshold;
      this.isDisplayable = isDisplayable;
    }

    @Override
    public int compareTo(@Nonnull final PercentileEntry t) {
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
          ? AngleFormat.formatDisplayShort(threshold)
          : AngleFormat.formatExport(threshold);
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

  private MultiMatchStatistics(
      final List<SingleMatchStatistics> statistics,
      final double[] angleLimits,
      final double[] percentsLimits) {
    super();
    this.statistics = statistics;
    this.angleLimits = angleLimits.clone();
    this.percentsLimits = percentsLimits.clone();
  }

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
      columnNames[i + 1] = AngleFormat.formatDisplayLong(angleLimits[i]);
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
      columnNames[i + 1] =
          String.format("%s%%", CommonNumberFormat.formatDouble(percentsLimits[i]));
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
