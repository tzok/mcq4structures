package pl.poznan.put.matching;

import pl.poznan.put.circular.exception.InvalidCircularValueException;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbChain;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.pdb.analysis.PdbResidue;

import java.util.ArrayList;
import java.util.List;

public class SelectionFactory {
    private SelectionFactory() {
    }

    public static StructureSelection create(String name, PdbModel structure)
            throws InvalidCircularValueException {
        return SelectionFactory.create(name, structure.getChains());
    }

    public static StructureSelection create(String name, List<PdbChain> chains)
            throws InvalidCircularValueException {
        List<PdbResidue> residues = SelectionFactory.getAllResidues(chains);
        return new StructureSelection(name, residues);
    }

    private static List<PdbResidue> getAllResidues(List<PdbChain> chains) {
        List<PdbResidue> residues = new ArrayList<>();
        for (PdbChain chain : chains) {
            residues.addAll(SelectionFactory.getAllResidues(chain));
        }
        return residues;
    }

    private static List<PdbResidue> getAllResidues(PdbChain chain) {
        List<PdbResidue> residues = new ArrayList<>();
        for (PdbResidue residue : chain.getResidues()) {
            if (residue.getMoleculeType() != MoleculeType.UNKNOWN) {
                residues.add(residue);
            }
        }
        return residues;
    }

    public static StructureSelection create(String name, PdbChain chain)
            throws InvalidCircularValueException {
        List<PdbResidue> residues = SelectionFactory.getAllResidues(chain);
        return new StructureSelection(name, residues);
    }
}
