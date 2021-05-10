package pl.poznan.put.comparison;

import pl.poznan.put.comparison.exception.IncomparableStructuresException;
import pl.poznan.put.comparison.global.GlobalComparator;
import pl.poznan.put.comparison.global.GlobalResult;
import pl.poznan.put.comparison.global.ImmutableRMSDGlobalResult;
import pl.poznan.put.matching.FragmentSuperimposer;
import pl.poznan.put.matching.ImmutableMCQMatcher;
import pl.poznan.put.matching.SelectionMatch;
import pl.poznan.put.matching.StructureMatcher;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.pdb.analysis.MoleculeType;

/**
 * Implementation of RMSD global similarity measure.
 *
 * @author Tomasz Zok (tzok[at]cs.put.poznan.pl)
 */
public class RMSD implements GlobalComparator {
  private final FragmentSuperimposer.AtomFilter filter;
  private final boolean onlyHeavy;
  private final MoleculeType moleculeType;

  public RMSD(final MoleculeType moleculeType) {
    super();
    filter = FragmentSuperimposer.AtomFilter.ALL;
    onlyHeavy = true;
    this.moleculeType = moleculeType;
  }

  public RMSD(
      final FragmentSuperimposer.AtomFilter filter,
      final boolean onlyHeavy,
      final MoleculeType moleculeType) {
    super();
    this.filter = filter;
    this.onlyHeavy = onlyHeavy;
    this.moleculeType = moleculeType;
  }

  @Override
  public final String getName() {
    return "RMSD";
  }

  @Override
  public final boolean isAngularMeasure() {
    return false;
  }

  @Override
  public final GlobalResult compareGlobally(
      final StructureSelection s1, final StructureSelection s2) {
    final StructureMatcher matcher = ImmutableMCQMatcher.of(moleculeType);
    final SelectionMatch matches = matcher.matchSelections(s1, s2);

    if (matches.getFragmentMatches().isEmpty()) {
      throw new IncomparableStructuresException("No matching fragments found");
    }

    final FragmentSuperimposer superimposer = new FragmentSuperimposer(matches, filter, onlyHeavy);
    return ImmutableRMSDGlobalResult.of(matches, superimposer);
  }
}
