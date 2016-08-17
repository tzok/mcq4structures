package pl.poznan.put.comparison.global;

import pl.poznan.put.constant.Unicode;
import pl.poznan.put.matching.FragmentSuperimposer;
import pl.poznan.put.matching.SelectionMatch;
import pl.poznan.put.utility.CommonNumberFormat;

public class RMSDGlobalResult extends GlobalResult {
    private final FragmentSuperimposer superimposer;
    private final String longDisplayName;

    public RMSDGlobalResult(String measureName, SelectionMatch matches,
                            FragmentSuperimposer superimposer) {
        super(measureName, matches);
        this.superimposer = superimposer;
        this.longDisplayName = prepareLongDisplayName();
    }

    private String prepareLongDisplayName() {
        SelectionMatch selectionMatch = getSelectionMatch();
        int validCount = selectionMatch.getResidueLabels().size();

        StringBuilder builder = new StringBuilder("<html>");
        builder.append(getShortDisplayName());
        builder.append("<br>");
        builder.append(validCount);
        builder.append("<br>");
        builder.append("</html>");
        return builder.toString();
    }

    public int getAtomCount() {
        return superimposer.getAtomCount();
    }

    public double getRMSD() {
        return superimposer.getRMSD();
    }

    @Override
    public String getLongDisplayName() {
        return longDisplayName;
    }

    @Override
    public String getShortDisplayName() {
        return CommonNumberFormat.formatDouble(superimposer.getRMSD())
               + Unicode.ANGSTROM;
    }

    @Override
    public String getExportName() {
        return Double.toString(superimposer.getRMSD());
    }

    @Override
    public double asDouble() {
        return superimposer.getRMSD();
    }
}
