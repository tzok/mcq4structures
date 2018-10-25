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
import pl.poznan.put.utility.NumberFormatUtils;

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
    final PdbResidue residue =
        selectionMatch.getFragmentMatches().get(0).getResidueComparisons().get(0).getTarget();
    final PdbResidue e =
        selectionMatch
            .getFragmentMatches()
            .get(0)
            .getResidueComparisons()
            .get(selectionMatch.getFragmentMatches().get(0).getResidueComparisons().size() - 1)
            .getTarget();
    final PdbResidue s1 =
        selectionMatch.getFragmentMatches().get(0).getResidueComparisons().get(0).getModel();
    final PdbResidue e1 =
        selectionMatch
            .getFragmentMatches()
            .get(0)
            .getResidueComparisons()
            .get(selectionMatch.getFragmentMatches().get(0).getResidueComparisons().size() - 1)
            .getModel();

    return String.format(
        "<html>%s<br>%d<br>%s%%<br>%s<br>%s<br>%s<br>%s<br>%s<br>%s<br></html>",
        getShortDisplayName(),
        validCount,
        String.format("%.4g%n", coverage),
        target.getName(),
        residue,
        e,
        model.getName(),
        s1,
        e1);
  }

  public final String cliOutput(final StructureSelection model, final StructureSelection target) {
    final SelectionMatch selectionMatch = getSelectionMatch();
    final AngleDeltaIterator angleDeltaIterator = new MatchCollectionDeltaIterator(selectionMatch);
    final SingleMatchStatistics statistics =
        SingleMatchStatistics.calculate("", angleDeltaIterator);

    final int validCount = selectionMatch.getResidueLabels().size();
    final int length = target.getResidues().size();
    final double coverage = ((double) validCount / length) * 100.0;
    final PdbResidue residue =
        selectionMatch.getFragmentMatches().get(0).getResidueComparisons().get(0).getTarget();
    final PdbResidue e =
        selectionMatch
            .getFragmentMatches()
            .get(0)
            .getResidueComparisons()
            .get(selectionMatch.getFragmentMatches().get(0).getResidueComparisons().size() - 1)
            .getTarget();
    final PdbResidue s1 =
        selectionMatch.getFragmentMatches().get(0).getResidueComparisons().get(0).getModel();
    final PdbResidue e1 =
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
        NumberFormatUtils.threeDecimalDigits().format(coverage),
        target.getName(),
        residue,
        e,
        model.getName(),
        s1,
        e1);
  }

  public final Angle getMeanDirection() {
    return angleSample.getMeanDirection();
  }

  public final Angle getMedianDirection() {
    return angleSample.getMedianDirection();
  }

  @Override
  public final String toString() {
    return angleSample.toString();
  }

  @Override
  public final String getLongDisplayName() {
    return longDisplayName;
  }

  @Override
  public final String getShortDisplayName() {
    return AngleFormat.degreesRoundedToOne(angleSample.getMeanDirection().getRadians());
  }

  @Override
  public final String getExportName() {
    return AngleFormat.degrees(angleSample.getMeanDirection().getRadians());
  }

  @Override
  public final double asDouble() {
    return angleSample.getMeanDirection().getRadians();
  }
}
