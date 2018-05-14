package pl.poznan.put.matching;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.biojava.nbio.structure.StructureException;
import pl.poznan.put.interfaces.ExportFormat;
import pl.poznan.put.interfaces.Exportable;
import pl.poznan.put.pdb.CifPdbIncompatibilityException;

@Data
@NoArgsConstructor
@XmlRootElement
public class SelectionMatch implements Exportable, MatchCollection {
  @XmlElement private StructureSelection target;
  @XmlElement private StructureSelection model;
  @XmlElement private List<FragmentMatch> fragmentMatches;
  @XmlTransient private List<String> residueLabels;

  public SelectionMatch(
      final StructureSelection target,
      final StructureSelection model,
      final List<FragmentMatch> fragmentMatches) {
    super();
    this.target = target;
    this.model = model;
    this.fragmentMatches = new ArrayList<>(fragmentMatches);
    makeResidueLabelsList();
  }

  public final void setFragmentMatches(final List<FragmentMatch> fragmentMatches) {
    this.fragmentMatches = new ArrayList<>(fragmentMatches);
    makeResidueLabelsList();
  }

  private void makeResidueLabelsList() {
    final List<String> result = new ArrayList<>();
    for (final FragmentMatch fragment : fragmentMatches) {
      result.addAll(fragment.generateLabelsWithResidueNames());
    }
    residueLabels = result;
  }

  @Override
  public final void export(final OutputStream stream) throws IOException {
    try {
      IOUtils.write(toPDB(false), stream, "UTF-8");
    } catch (final StructureException | CifPdbIncompatibilityException e) {
      throw new IOException("Failed to export the match to a PDB file", e);
    }
  }

  public final String toPDB(final boolean onlyMatched)
      throws StructureException, CifPdbIncompatibilityException {
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
  public final ExportFormat getExportFormat() {
    return ExportFormat.PDB;
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
