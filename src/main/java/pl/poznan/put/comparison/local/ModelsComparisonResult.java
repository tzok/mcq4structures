package pl.poznan.put.comparison.local;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import pl.poznan.put.matching.FragmentMatch;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.torsion.MasterTorsionAngleType;

import java.util.List;

@Data
@Slf4j
public class ModelsComparisonResult {
  private final PdbCompactFragment target;
  private final List<PdbCompactFragment> models;
  private final List<FragmentMatch> fragmentMatches;

  public final SelectedAngle selectAngle(final MasterTorsionAngleType torsionAngle) {
    return new SelectedAngle(torsionAngle, target, models, fragmentMatches);
  }
}
