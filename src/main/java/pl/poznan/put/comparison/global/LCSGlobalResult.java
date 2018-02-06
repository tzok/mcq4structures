package pl.poznan.put.comparison.global;

import pl.poznan.put.circular.Angle;
import pl.poznan.put.circular.samples.AngleSample;
import pl.poznan.put.matching.AngleDeltaIterator;
import pl.poznan.put.matching.MatchCollectionDeltaIterator;
import pl.poznan.put.matching.SelectionMatch;
import pl.poznan.put.matching.stats.SingleMatchStatistics;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.utility.AngleFormat;
import pl.poznan.put.utility.CommonNumberFormat;


import java.util.List;
import pl.poznan.put.matching.FragmentMatch;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.pdb.PdbParsingException;
import pl.poznan.put.pdb.PdbResidueIdentifier;
import pl.poznan.put.pdb.analysis.PdbChain;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.pdb.analysis.PdbParser;
import pl.poznan.put.pdb.analysis.PdbResidue;

import java.lang.String;


public class LCSGlobalResult extends GlobalResult {
    private final AngleSample angleSample;
    private final String longDisplayName;


    public LCSGlobalResult(String measureName, SelectionMatch selectionMatch,
                           AngleSample angleSample, StructureSelection model, StructureSelection target) {
        super(measureName, selectionMatch);
        this.angleSample = angleSample;
        this.longDisplayName = prepareLongDisplayName(model, target);

    }

    private String prepareLongDisplayName(StructureSelection model, StructureSelection target) {
        SelectionMatch selectionMatch = getSelectionMatch();
        AngleDeltaIterator angleDeltaIterator =
                new MatchCollectionDeltaIterator(selectionMatch);
        SingleMatchStatistics statistics =
                SingleMatchStatistics.calculate("", angleDeltaIterator);

        int validCount = selectionMatch.getResidueLabels().size();
        int length =target.getResidues().size();
        double coverage = (double)validCount/(double)length*100.0;
        PdbResidue s;
        PdbResidue e;
        PdbResidue s1;
        PdbResidue e1;
        s = selectionMatch.getFragmentMatches().get(0).getResidueComparisons().get(0).getTarget();
        e = selectionMatch.getFragmentMatches().get(0).getResidueComparisons().get(selectionMatch.getFragmentMatches().get(0).getResidueComparisons().size() -1).getTarget();
        s1 = selectionMatch.getFragmentMatches().get(0).getResidueComparisons().get(0).getModel();
        e1 = selectionMatch.getFragmentMatches().get(0).getResidueComparisons().get(selectionMatch.getFragmentMatches().get(0).getResidueComparisons().size() -1).getModel();
        


        StringBuilder builder = new StringBuilder("<html>");
        builder.append(getShortDisplayName());
        builder.append("<br>");
        builder.append(validCount);
        builder.append("<br>");
        builder.append(String.format("%.4g%n", coverage));
        builder.append('%');
        builder.append("<br>");
        builder.append(target.getName());
        builder.append("<br>");
        builder.append(s);
        builder.append("<br>");
        builder.append(e);
        builder.append("<br>");
        builder.append(model.getName());
        builder.append("<br>");
        builder.append(s1);
        builder.append("<br>");
        builder.append(e1);
        builder.append("<br>");
        builder.append("</html>");
        return builder.toString();
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
    public String getLongDisplayName() {
        return longDisplayName;
    }

    @Override
    public String getShortDisplayName() {
        return AngleFormat.formatDisplayShort(
                angleSample.getMeanDirection().getRadians());
    }

    @Override
    public String getExportName() {
        return AngleFormat
                .formatExport(angleSample.getMeanDirection().getRadians());
    }

    @Override
    public double asDouble() {
        return angleSample.getMeanDirection().getRadians();
    }
}
