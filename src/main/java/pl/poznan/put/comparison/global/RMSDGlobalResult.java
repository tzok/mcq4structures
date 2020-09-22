package pl.poznan.put.comparison.global;

import pl.poznan.put.constant.Unicode;
import pl.poznan.put.matching.FragmentSuperimposer;
import pl.poznan.put.matching.SelectionMatch;
import pl.poznan.put.utility.NumberFormatUtils;

public class RMSDGlobalResult extends GlobalResult {
  private final FragmentSuperimposer superimposer;
  private final String longDisplayName;

  public RMSDGlobalResult(
      final String measureName,
      final SelectionMatch matches,
      final FragmentSuperimposer superimposer) {
    super(measureName, matches);
    this.superimposer = superimposer;
    longDisplayName = prepareLongDisplayName();
  }

  public final int getAtomCount() {
    return superimposer.getAtomCount();
  }

  public final double getRMSD() {
    return superimposer.getRMSD();
  }

  @Override
  public final String shortDisplayName() {
    return NumberFormatUtils.threeDecimalDigits().format(superimposer.getRMSD()) + Unicode.ANGSTROM;
  }

  @Override
  public final String longDisplayName() {
    return longDisplayName;
  }

  @Override
  public final String exportName() {
    return Double.toString(superimposer.getRMSD());
  }

  @Override
  public final double asDouble() {
    return superimposer.getRMSD();
  }

  private String prepareLongDisplayName() {
    final SelectionMatch selectionMatch = getSelectionMatch();
    final int validCount = selectionMatch.getResidueLabels().size();

    return String.format("<html>%s<br>%d<br></html>", shortDisplayName(), validCount);
  }
}
