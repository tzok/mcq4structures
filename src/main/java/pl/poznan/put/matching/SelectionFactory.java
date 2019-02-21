package pl.poznan.put.matching;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbChain;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.pdb.analysis.PdbResidue;

public final class SelectionFactory {
  public static StructureSelection create(final String name, final PdbModel structure) {
    return SelectionFactory.create(name, structure.getChains());
  }

  public static StructureSelection create(final String name, final Iterable<PdbChain> chains) {
    final List<PdbResidue> residues = SelectionFactory.getAllResidues(chains);
    return StructureSelection.divideIntoCompactFragments(name, residues);
  }

  public static StructureSelection create(
      final String name, final PdbModel structure, final SelectionQuery... selectionQueries) {
    try {
      final List<PdbCompactFragment> compactFragments = new ArrayList<>(selectionQueries.length);

      for (final SelectionQuery selectionQuery : selectionQueries) {
        compactFragments.addAll(selectionQuery.apply(structure));
      }

      return new StructureSelection(name, compactFragments);
    } catch (final InvalidSelectionException e) {
      throw new IllegalArgumentException(
          String.format("Failed to create selection in %s", name), e);
    }
  }

  private static List<PdbResidue> getAllResidues(final Iterable<PdbChain> chains) {
    final List<PdbResidue> residues = new ArrayList<>();
    for (final PdbChain chain : chains) {
      residues.addAll(SelectionFactory.getAllResidues(chain));
    }
    return residues;
  }

  private static Collection<PdbResidue> getAllResidues(final PdbChain chain) {
    final List<PdbResidue> chainResidues = chain.getResidues();
    final Collection<PdbResidue> residues = new ArrayList<>(chainResidues.size());

    for (final PdbResidue residue : chainResidues) {
      if (residue.getMoleculeType() != MoleculeType.UNKNOWN) {
        residues.add(residue);
      }
    }
    return residues;
  }

  private SelectionFactory() {
    super();
  }
}
