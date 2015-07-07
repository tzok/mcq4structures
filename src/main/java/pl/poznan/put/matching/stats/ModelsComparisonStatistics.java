package pl.poznan.put.matching.stats;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import pl.poznan.put.comparison.local.ModelsComparisonResult;
import pl.poznan.put.matching.FragmentMatch;
import pl.poznan.put.torsion.MasterTorsionAngleType;
import pl.poznan.put.utility.AngleFormat;
import pl.poznan.put.utility.CommonNumberFormat;

public class ModelsComparisonStatistics {
    public static ModelsComparisonStatistics calculate(
            ModelsComparisonResult.SelectedAngle selectedAngle) {
        List<MatchStatistics> statistics = new ArrayList<>();
        MasterTorsionAngleType angleType = selectedAngle.getAngleType();

        for (FragmentMatch fragmentMatch : selectedAngle.getFragmentMatches()) {
            statistics.add(MatchStatistics.calculate(fragmentMatch, angleType));
        }

        return new ModelsComparisonStatistics(statistics, MatchStatistics.DEFAULT_ANGLE_LIMITS, MatchStatistics.DEFAULT_PERCENTS_LIMITS);
    }

    private final List<MatchStatistics> statistics;
    private final double[] angleLimits;
    private final double[] percentsLimits;

    public ModelsComparisonStatistics(List<MatchStatistics> statistics,
            double[] angleLimits, double[] percentsLimits) {
        super();
        this.statistics = statistics;
        this.angleLimits = angleLimits.clone();
        this.percentsLimits = percentsLimits.clone();
    }

    public int getSize() {
        return statistics.size();
    }

    public MatchStatistics getMatchStatistics(int index) {
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
            MatchStatistics match = statistics.get(i);
            data[i] = new String[angleLimits.length + 1];
            data[i][0] = match.getMatch().getModelFragment().getName();

            for (int j = 0; j < angleLimits.length; j++) {
                data[i][j + 1] = CommonNumberFormat.formatDouble(100.0 * match.getRatioOfDeltasBelowThreshold(angleLimits[j])) + "%";
            }
        }

        return new DefaultTableModel(data, columnNames);
    }

    public TableModel percentilesAsTableModel(boolean isDisplayable) {
        String[] columnNames = new String[percentsLimits.length + 1];
        columnNames[0] = isDisplayable ? "" : null;
        for (int i = 0; i < percentsLimits.length; i++) {
            columnNames[i + 1] = CommonNumberFormat.formatDouble(percentsLimits[i]) + "%";
        }

        String[][] data = new String[statistics.size()][];
        for (int i = 0; i < statistics.size(); i++) {
            MatchStatistics match = statistics.get(i);
            data[i] = new String[percentsLimits.length + 1];
            data[i][0] = match.getMatch().getModelFragment().getName();

            for (int j = 0; j < percentsLimits.length; j++) {
                double angle = match.getAngleThresholdForGivenPercentile(percentsLimits[j]);
                if (isDisplayable) {
                    data[i][j + 1] = AngleFormat.formatDisplayShort(angle);
                } else {
                    data[i][j + 1] = AngleFormat.formatExport(angle);
                }
            }
        }

        return new DefaultTableModel(data, columnNames);
    }
}
