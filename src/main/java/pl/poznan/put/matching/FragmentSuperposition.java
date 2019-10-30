package pl.poznan.put.matching;

import java.util.ArrayList;
import java.util.List;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;

public class FragmentSuperposition {
  private final List<? extends PdbCompactFragment> targetFragments;
  private final List<? extends PdbCompactFragment> modelFragments;

  public FragmentSuperposition(
          final List<? extends PdbCompactFragment> targetFragments, final List<? extends PdbCompactFragment> modelFragments) {
    super();
    this.targetFragments = new ArrayList<>(targetFragments);
    this.modelFragments = new ArrayList<>(modelFragments);
  }

  public final String toPDB() {
    final StringBuilder builder = new StringBuilder();
    builder.append(
        "MODEL        1                                              " + "                    \n");
    for (final PdbCompactFragment fragment : targetFragments) {
      builder.append(fragment.toPdb());
    }
    builder.append(
        "ENDMDL                                                      " + "                    \n");
    builder.append(
        "MODEL        2                                              " + "                    \n");
    for (final PdbCompactFragment fragment : modelFragments) {
      builder.append(fragment.toPdb());
    }
    builder.append(
        "ENDMDL                                                      " + "                    \n");
    return builder.toString();
  }
}
