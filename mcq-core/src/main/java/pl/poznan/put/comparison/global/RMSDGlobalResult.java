package pl.poznan.put.comparison.global;

import org.immutables.value.Value;
import pl.poznan.put.constant.Unicode;
import pl.poznan.put.matching.FragmentSuperimposer;
import pl.poznan.put.matching.SelectionMatch;
import pl.poznan.put.utility.NumberFormatUtils;

@Value.Immutable
public abstract class RMSDGlobalResult implements GlobalResult {
  @Value.Parameter(order = 2)
  public abstract FragmentSuperimposer superimposer();

  @Override
  public final String measureName() {
    return "RMSD";
  }

  @Override
  @Value.Parameter(order = 1)
  public abstract SelectionMatch selectionMatch();

  @Override
  public final double toDouble() {
    return superimposer().getRMSD();
  }

  public final int atomCount() {
    return superimposer().getAtomCount();
  }

  @Override
  public final String shortDisplayName() {
    return NumberFormatUtils.threeDecimalDigits().format(superimposer().getRMSD())
        + Unicode.ANGSTROM;
  }

  @Override
  @Value.Lazy
  public String longDisplayName() {
    final SelectionMatch selectionMatch = selectionMatch();
    final int validCount = selectionMatch.getResidueLabels().size();
    return String.format("<html>%s<br>%d<br></html>", shortDisplayName(), validCount);
  }

  @Override
  public final String exportName() {
    return Double.toString(superimposer().getRMSD());
  }
}
