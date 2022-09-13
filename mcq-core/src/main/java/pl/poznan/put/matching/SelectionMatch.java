package pl.poznan.put.matching;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import pl.poznan.put.interfaces.Exportable;

@Data
@NoArgsConstructor
public class SelectionMatch implements Exportable, MatchCollection {
  private StructureSelection target;
  private StructureSelection model;
  private List<FragmentMatch> fragmentMatches;
  private List<String> residueLabels;

  public SelectionMatch(
      final StructureSelection target,
      final StructureSelection model,
      final List<? extends FragmentMatch> fragmentMatches) {
    super();
    this.target = target;
    this.model = model;
    this.fragmentMatches = new ArrayList<>(fragmentMatches);
    makeResidueLabelsList();
  }

  public final void setFragmentMatches(final List<? extends FragmentMatch> fragmentMatches) {
    this.fragmentMatches = new ArrayList<>(fragmentMatches);
    makeResidueLabelsList();
  }

  private void makeResidueLabelsList() {
    final List<String> result = new ArrayList<>();
    for (final FragmentMatch fragment : fragmentMatches) {
      result.addAll(fragment.matchedResidueNames());
    }
    residueLabels = result;
  }

  @Override
  public final void export(final OutputStream stream) throws IOException {
    IOUtils.write(toPDB(false), stream, "UTF-8");
  }

  public final String toPDB(final boolean onlyMatched) {
    if (fragmentMatches.isEmpty()) {
      return "";
    }

    final FragmentSuperimposer fragmentSuperimposer =
        new FragmentSuperimposer(this, FragmentSuperimposer.AtomFilter.ALL, true);
    final FragmentSuperposition superposition =
        onlyMatched ? fragmentSuperimposer.getMatched() : fragmentSuperimposer.getWhole();
    return superposition.toPDB();
  }

  @Override
  public final File suggestName() {
    return new File(
        String.format(
            "%s-3DSTRA-%s-%s.pdb",
            DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.format(new Date()),
            target.getName(),
            model.getName()));
  }
}
