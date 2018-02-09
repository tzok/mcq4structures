package pl.poznan.put.comparison.global;

import pl.poznan.put.circular.Angle;
import pl.poznan.put.circular.samples.AngleSample;
import pl.poznan.put.matching.AngleDeltaIterator;
import pl.poznan.put.matching.MatchCollectionDeltaIterator;
import pl.poznan.put.matching.SelectionMatch;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.matching.stats.SingleMatchStatistics;
import pl.poznan.put.pdb.analysis.PdbResidue;
import pl.poznan.put.utility.AngleFormat;
import pl.poznan.put.utility.CommonNumberFormat;

public class LCSGlobalResult extends GlobalResult {
  private final AngleSample angleSample;
  private final String longDisplayName;

  public LCSGlobalResult(
      final String measureName,
      final SelectionMatch selectionMatch,
      final AngleSample angleSample,
      final StructureSelection model,
      final StructureSelection target) {
    super(measureName, selectionMatch);
    this.angleSample = angleSample;
    longDisplayName = prepareLongDisplayName(model, target);
  }

  private String prepareLongDisplayName(
      final StructureSelection model, final StructureSelection target) {
    final SelectionMatch selectionMatch = getSelectionMatch();

    final int validCount = selectionMatch.getResidueLabels().size();
    final int length = target.getResidues().size();
    final double coverage = ((double) validCount / length) * 100.0;
    final PdbResidue s;
    final PdbResidue e;
    final PdbResidue s1;
    final PdbResidue e1;
    s = selectionMatch.getFragmentMatches().get(0).getResidueComparisons().get(0).getTarget();
    e =
        selectionMatch
            .getFragmentMatches()
            .get(0)
            .getResidueComparisons()
            .get(selectionMatch.getFragmentMatches().get(0).getResidueComparisons().size() - 1)
            .getTarget();
    s1 = selectionMatch.getFragmentMatches().get(0).getResidueComparisons().get(0).getModel();
    e1 =
        selectionMatch
            .getFragmentMatches()
            .get(0)
            .getResidueComparisons()
            .get(selectionMatch.getFragmentMatches().get(0).getResidueComparisons().size() - 1)
            .getModel();

    final StringBuilder builder = new StringBuilder("<html>");
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

  public String cliOutput(final StructureSelection model, final StructureSelection target) {
    final SelectionMatch selectionMatch = getSelectionMatch();
    final AngleDeltaIterator angleDeltaIterator = new MatchCollectionDeltaIterator(selectionMatch);
    final SingleMatchStatistics statistics =
        SingleMatchStatistics.calculate("", angleDeltaIterator);

    final int validCount = selectionMatch.getResidueLabels().size();
    final int length = target.getResidues().size();
    final double coverage = ((double) validCount / (double) length) * 100.0;
    final PdbResidue s;
    final PdbResidue e;
    final PdbResidue s1;
    final PdbResidue e1;
    s = selectionMatch.getFragmentMatches().get(0).getResidueComparisons().get(0).getTarget();
    e =
        selectionMatch
            .getFragmentMatches()
            .get(0)
            .getResidueComparisons()
            .get(selectionMatch.getFragmentMatches().get(0).getResidueComparisons().size() - 1)
            .getTarget();
    s1 = selectionMatch.getFragmentMatches().get(0).getResidueComparisons().get(0).getModel();
    e1 =
        selectionMatch
            .getFragmentMatches()
            .get(0)
            .getResidueComparisons()
            .get(selectionMatch.getFragmentMatches().get(0).getResidueComparisons().size() - 1)
            .getModel();
    return String.format(
        "MCQ value: %s\nNumber of residues: %d\nCoverage: %s%% \nTarget name: %s\nFirst target residue: %s\nLast target residue: %s\nModel name: %s\nFirst model residue: %s\nLast model residue: %s",
        getShortDisplayName(),
        validCount,
        CommonNumberFormat.formatDouble(coverage),
        target.getName(),
        s,
        e,
        model.getName(),
        s1,
        e1);
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
    return AngleFormat.formatDisplayShort(angleSample.getMeanDirection().getRadians());
  }

  @Override
  public String getExportName() {
    return AngleFormat.formatExport(angleSample.getMeanDirection().getRadians());
  }

  @Override
  public double asDouble() {
    return angleSample.getMeanDirection().getRadians();
  }
}
