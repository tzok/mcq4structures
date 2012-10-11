package pl.poznan.put.cs.bioserver.torsion;

import java.util.ArrayList;
import java.util.List;

public class DihedralContainer {
    private List<AminoAcidDihedral> aminoAcidDihedrals;
    private List<NucleotideDihedral> nucleotideDihedrals;

    public DihedralContainer() {
        aminoAcidDihedrals = new ArrayList<>();
        nucleotideDihedrals = new ArrayList<>();
    }

    public DihedralContainer(List<AminoAcidDihedral> aad,
            List<NucleotideDihedral> nd) {
        aminoAcidDihedrals = aad;
        nucleotideDihedrals = nd;
    }

    public void addAll(DihedralContainer container) {
        aminoAcidDihedrals.addAll(container.aminoAcidDihedrals);
        nucleotideDihedrals.addAll(container.nucleotideDihedrals);
    }

    public List<AminoAcidDihedral> getAminoAcidDihedrals() {
        return aminoAcidDihedrals;
    }

    public List<NucleotideDihedral> getNucleotideDihedrals() {
        return nucleotideDihedrals;
    }
}
