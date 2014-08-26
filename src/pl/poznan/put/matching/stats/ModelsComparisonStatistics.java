package pl.poznan.put.matching.stats;

import java.util.List;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import pl.poznan.put.helper.CommonNumberFormat;
import pl.poznan.put.helper.FractionAngleFormat;

public class ModelsComparisonStatistics {
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

    public TableModel histogramsAsTableModel() {
        String[] columnNames = new String[angleLimits.length + 1];
        for (int i = 0; i < angleLimits.length; i++) {
            columnNames[i + 1] = FractionAngleFormat.formatDouble(angleLimits[i]);
        }

        String[][] data = new String[statistics.size()][];
        for (int i = 0; i < statistics.size(); i++) {
            MatchStatistics match = statistics.get(i);
            data[i] = new String[angleLimits.length + 1];
            data[i][0] = match.getMatch().getModel().getName();

            for (int j = 0; j < angleLimits.length; j++) {
                data[i][j + 1] = CommonNumberFormat.formatDouble(match.getRatioOfDeltasBelowThreshold(angleLimits[j]));
            }
        }

        return new DefaultTableModel(data, columnNames);
    }

    public TableModel percentilesAsTableModel() {
        String[] columnNames = new String[percentsLimits.length + 1];
        for (int i = 0; i < percentsLimits.length; i++) {
            columnNames[i + 1] = CommonNumberFormat.formatDouble(percentsLimits[i]);
        }

        String[][] data = new String[statistics.size()][];
        for (int i = 0; i < statistics.size(); i++) {
            MatchStatistics match = statistics.get(i);
            data[i] = new String[percentsLimits.length + 1];
            data[i][0] = match.getMatch().getModel().getName();

            for (int j = 0; j < percentsLimits.length; j++) {
                double angle = match.getAngleThresholdForGivenPercentile(percentsLimits[j]);
                data[i][j + 1] = CommonNumberFormat.formatDouble(Math.toDegrees(angle));
            }
        }

        return new DefaultTableModel(data, columnNames);
    }
}
