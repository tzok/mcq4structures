package pl.poznan.put.comparison.global;

import org.immutables.value.Value;
import pl.poznan.put.circular.Angle;
import pl.poznan.put.circular.samples.AngleSample;
import pl.poznan.put.matching.AngleDeltaIterator;
import pl.poznan.put.matching.MatchCollectionDeltaIterator;
import pl.poznan.put.matching.SelectionMatch;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.pdb.analysis.PdbResidue;
import pl.poznan.put.utility.AngleFormat;
import pl.poznan.put.utility.NumberFormatUtils;

@Value.Immutable
public abstract class LCSGlobalResult implements GlobalResult {
  @Value.Parameter(order = 2)
  public abstract AngleSample angleSample();

  @Value.Parameter(order = 3)
  public abstract StructureSelection model();

  @Value.Parameter(order = 4)
  public abstract StructureSelection target();

  @Override
  public final String measureName() {
    return "LCS";
  }

  @Value.Parameter(order = 1)
  public abstract SelectionMatch selectionMatch();

  @Override
  public final double toDouble() {
    return angleSample().meanDirection().radians();
  }

  public final String cliOutput(final StructureSelection model, final StructureSelection target) {
    final SelectionMatch selectionMatch = selectionMatch();
    final AngleDeltaIterator angleDeltaIterator = new MatchCollectionDeltaIterator(selectionMatch);
    final int validCount = selectionMatch.getResidueLabels().size();
    final int length = target.getResidues().size();
    final double coverage = ((double) validCount / length) * 100.0;
    final PdbResidue residue =
        selectionMatch.getFragmentMatches().get(0).getResidueComparisons().get(0).target();
    final PdbResidue e =
        selectionMatch
            .getFragmentMatches()
            .get(0)
            .getResidueComparisons()
            .get(selectionMatch.getFragmentMatches().get(0).getResidueComparisons().size() - 1)
            .target();
    final PdbResidue s1 =
        selectionMatch.getFragmentMatches().get(0).getResidueComparisons().get(0).model();
    final PdbResidue e1 =
        selectionMatch
            .getFragmentMatches()
            .get(0)
            .getResidueComparisons()
            .get(selectionMatch.getFragmentMatches().get(0).getResidueComparisons().size() - 1)
            .model();
    return String.format(
        "MCQ value: %s\n"
            + "Number of residues: %d\n"
            + "Coverage: %s%% \n"
            + "Target name: %s\n"
            + "First target residue: %s\n"
            + "Last target residue: %s\n"
            + "Model name: %s\n"
            + "First model residue: %s\n"
            + "Last model residue: %s",
        shortDisplayName(),
        validCount,
        NumberFormatUtils.threeDecimalDigits().format(coverage),
        target.getName(),
        residue,
        e,
        model.getName(),
        s1,
        e1);
  }

  public final Angle meanDirection() {
    return angleSample().meanDirection();
  }

  public final Angle medianDirection() {
    return angleSample().medianDirection();
  }

  @Override
  public final String toString() {
    return angleSample().toString();
  }

  @Override
  public final String shortDisplayName() {
    return AngleFormat.degreesRoundedToOne(angleSample().meanDirection().radians());
  }

  @Override
  @Value.Lazy
  public String longDisplayName() {
    final SelectionMatch selectionMatch = selectionMatch();

    final int validCount = selectionMatch.getResidueLabels().size();
    final int length = target().getResidues().size();
    final double coverage = ((double) validCount / length) * 100.0;
    final PdbResidue residue =
        selectionMatch.getFragmentMatches().get(0).getResidueComparisons().get(0).target();
    final PdbResidue e =
        selectionMatch
            .getFragmentMatches()
            .get(0)
            .getResidueComparisons()
            .get(selectionMatch.getFragmentMatches().get(0).getResidueComparisons().size() - 1)
            .target();
    final PdbResidue s1 =
        selectionMatch.getFragmentMatches().get(0).getResidueComparisons().get(0).model();
    final PdbResidue e1 =
        selectionMatch
            .getFragmentMatches()
            .get(0)
            .getResidueComparisons()
            .get(selectionMatch.getFragmentMatches().get(0).getResidueComparisons().size() - 1)
            .model();

    return String.format(
        "<html>%s<br>%d<br>%s%%<br>%s<br>%s<br>%s<br>%s<br>%s<br>%s<br></html>",
        shortDisplayName(),
        validCount,
        String.format("%.4g%n", coverage),
        target().getName(),
        residue,
        e,
        model().getName(),
        s1,
        e1);
  }

  @Override
  public final String exportName() {
    return AngleFormat.degrees(angleSample().meanDirection().radians());
  }
}
