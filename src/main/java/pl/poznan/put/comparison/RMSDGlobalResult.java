package pl.poznan.put.comparison;

import pl.poznan.put.constant.Unicode;
import pl.poznan.put.matching.FragmentSuperimposer;
import pl.poznan.put.matching.SelectionMatch;
import pl.poznan.put.utility.CommonNumberFormat;

public class RMSDGlobalResult extends GlobalComparisonResult {
    private final FragmentSuperimposer superimposer;

    protected RMSDGlobalResult(String measureName, SelectionMatch matches,
            FragmentSuperimposer superimposer) {
        super(measureName, matches);
        this.superimposer = superimposer;
    }

    public int getAtomCount() {
        return superimposer.getAtomCount();
    }

    public double getRMSD() {
        return superimposer.getRMSD();
    }

    @Override
    public String getLongDisplayName() {
        return CommonNumberFormat.formatDouble(superimposer.getRMSD()) + Unicode.ANGSTROM;
    }

    @Override
    public String getShortDisplayName() {
        return getLongDisplayName();
    }

    @Override
    public String getExportName() {
        return Double.toString(superimposer.getRMSD());
    }
}
