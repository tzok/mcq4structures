package pl.poznan.put.matching;

import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbChain;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.pdb.analysis.PdbResidue;

import java.util.ArrayList;
import java.util.List;

public final class SelectionFactory {
    private SelectionFactory() {
        super();
    }

    public static StructureSelection create(final String name,
                                            final PdbModel structure) {
        return SelectionFactory.create(name, structure.getChains());
    }

    public static StructureSelection create(final String name,
                                            final Iterable<PdbChain> chains) {
        final List<PdbResidue> residues =
                SelectionFactory.getAllResidues(chains);
        return new StructureSelection(name, residues);
    }

    private static List<PdbResidue> getAllResidues(
            final Iterable<PdbChain> chains) {
        final List<PdbResidue> residues = new ArrayList<>();
        for (final PdbChain chain : chains) {
            residues.addAll(SelectionFactory.getAllResidues(chain));
        }
        return residues;
    }

    private static List<PdbResidue> getAllResidues(final PdbChain chain) {
        final List<PdbResidue> chainResidues = chain.getResidues();
        final List<PdbResidue> residues = new ArrayList<>(chainResidues.size());

        for (final PdbResidue residue : chainResidues) {
            if (residue.getMoleculeType() != MoleculeType.UNKNOWN) {
                residues.add(residue);
            }
        }
        return residues;
    }

    public static StructureSelection create(final String name,
                                            final PdbChain chain) {
        final List<PdbResidue> residues =
                SelectionFactory.getAllResidues(chain);
        return new StructureSelection(name, residues);
    }

    public static StructureSelection select(final Iterable<SelectionQuery> selectionQueries) {
        return null;
    }
}
