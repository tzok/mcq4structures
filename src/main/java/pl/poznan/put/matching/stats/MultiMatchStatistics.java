package pl.poznan.put.matching.stats;

import pl.poznan.put.gui.component.NonEditableDefaultTableModel;
import pl.poznan.put.matching.AngleDeltaIterator;
import pl.poznan.put.matching.AngleDeltaIteratorFactory;
import pl.poznan.put.matching.FragmentMatch;
import pl.poznan.put.matching.MatchCollection;
import pl.poznan.put.utility.AngleFormat;
import pl.poznan.put.utility.CommonNumberFormat;

import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.List;

public class MultiMatchStatistics {
    private final List<SingleMatchStatistics> statistics;
    private final double[] angleLimits;
    private final double[] percentsLimits;

    public MultiMatchStatistics(List<SingleMatchStatistics> statistics,
                                double[] angleLimits, double[] percentsLimits) {
        super();
        this.statistics = statistics;
        this.angleLimits = angleLimits.clone();
        this.percentsLimits = percentsLimits.clone();
    }

    public static MultiMatchStatistics calculate(
            AngleDeltaIteratorFactory iteratorFactory,
            MatchCollection matchesCollection) {
        List<SingleMatchStatistics> statistics = new ArrayList<>();

        for (FragmentMatch fragmentMatch : matchesCollection
                .getFragmentMatches()) {
            String name = fragmentMatch.getModelFragment().getName();
            AngleDeltaIterator deltaIterator =
                    iteratorFactory.createInstance(fragmentMatch);
            statistics
                    .add(SingleMatchStatistics.calculate(name, deltaIterator));
        }

        return new MultiMatchStatistics(statistics,
                                        SingleMatchStatistics
                                                .DEFAULT_ANGLE_LIMITS,
                                        SingleMatchStatistics
                                                .DEFAULT_PERCENTS_LIMITS);
    }

    public int getSize() {
        return statistics.size();
    }

    public SingleMatchStatistics getMatchStatistics(int index) {
        return statistics.get(index);
    }

    public TableModel histogramsAsTableModel(boolean isDisplayable) {
        String[] columnNames = new String[angleLimits.length + 1];
        columnNames[0] = isDisplayable ? "" : null;

        for (int i = 0; i < angleLimits.length; i++) {
            columnNames[i + 1] = AngleFormat.formatDisplayLong(angleLimits[i]);
        }

        String[][] data = new String[statistics.size()][];
        for (int i = 0; i < statistics.size(); i++) {
            SingleMatchStatistics match = statistics.get(i);
            data[i] = new String[angleLimits.length + 1];
            data[i][0] = match.getName();

            for (int j = 0; j < angleLimits.length; j++) {
                data[i][j + 1] = CommonNumberFormat.formatDouble(100.0 * match
                        .getRatioOfDeltasBelowThreshold(angleLimits[j])) + "%";
            }
        }

        return new NonEditableDefaultTableModel(data, columnNames);
    }

    public TableModel percentilesAsTableModel(boolean isDisplayable) {
        String[] columnNames = new String[percentsLimits.length + 1];
        columnNames[0] = isDisplayable ? "" : null;
        for (int i = 0; i < percentsLimits.length; i++) {
            columnNames[i + 1] =
                    CommonNumberFormat.formatDouble(percentsLimits[i]) + "%";
        }

        String[][] data = new String[statistics.size()][];
        for (int i = 0; i < statistics.size(); i++) {
            SingleMatchStatistics match = statistics.get(i);
            data[i] = new String[percentsLimits.length + 1];
            data[i][0] = match.getName();

            for (int j = 0; j < percentsLimits.length; j++) {
                double angle = match.getAngleThresholdForGivenPercentile(
                        percentsLimits[j]);
                if (isDisplayable) {
                    data[i][j + 1] = AngleFormat.formatDisplayShort(angle);
                } else {
                    data[i][j + 1] = AngleFormat.formatExport(angle);
                }
            }
        }

        return new NonEditableDefaultTableModel(data, columnNames);
    }
}
