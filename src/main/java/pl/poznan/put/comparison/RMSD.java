package pl.poznan.put.comparison;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import pl.poznan.put.comparison.exception.IncomparableStructuresException;
import pl.poznan.put.comparison.global.GlobalComparator;
import pl.poznan.put.comparison.global.GlobalResult;
import pl.poznan.put.comparison.global.RMSDGlobalResult;
import pl.poznan.put.matching.FragmentSuperimposer;
import pl.poznan.put.matching.MCQMatcher;
import pl.poznan.put.matching.SelectionMatch;
import pl.poznan.put.matching.StructureMatcher;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.protein.torsion.ProteinTorsionAngleType;
import pl.poznan.put.rna.torsion.RNATorsionAngleType;
import pl.poznan.put.torsion.MasterTorsionAngleType;

/**
 * Implementation of RMSD global similarity measure.
 *
 * @author Tomasz Zok (tzok[at]cs.put.poznan.pl)
 */
public class RMSD implements GlobalComparator {
  private final FragmentSuperimposer.AtomFilter filter;
  private final boolean onlyHeavy;
  private final List<MasterTorsionAngleType> angleTypes;

  public RMSD() {
    super();
    filter = FragmentSuperimposer.AtomFilter.ALL;
    onlyHeavy = true;
    angleTypes = RMSD.mainAngleTypes();
  }

  private static List<MasterTorsionAngleType> mainAngleTypes() {
    final List<MasterTorsionAngleType> mainAngleTypes = new ArrayList<>();
    mainAngleTypes.addAll(Arrays.asList(RNATorsionAngleType.mainAngles()));
    mainAngleTypes.addAll(Arrays.asList(ProteinTorsionAngleType.mainAngles()));
    return mainAngleTypes;
  }

  public RMSD(final FragmentSuperimposer.AtomFilter filter, final boolean onlyHeavy) {
    super();
    this.filter = filter;
    this.onlyHeavy = onlyHeavy;
    angleTypes = RMSD.mainAngleTypes();
  }

  @Override
  public final GlobalResult compareGlobally(
      final StructureSelection s1, final StructureSelection s2) {
    final StructureMatcher matcher = new MCQMatcher(angleTypes);
    final SelectionMatch matches = matcher.matchSelections(s1, s2);

    if (matches.getFragmentMatches().isEmpty()) {
      throw new IncomparableStructuresException("No matching fragments found");
    }

    final FragmentSuperimposer superimposer = new FragmentSuperimposer(matches, filter, onlyHeavy);
    return new RMSDGlobalResult(getName(), matches, superimposer);
  }

  @Override
  public final String getName() {
    return "RMSD";
  }

  @Override
  public final boolean isAngularMeasure() {
    return false;
  }
}
