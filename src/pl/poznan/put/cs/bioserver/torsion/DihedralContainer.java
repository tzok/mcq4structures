package pl.poznan.put.cs.bioserver.torsion;

import java.util.List;
import java.util.Vector;

public class DihedralContainer {
    public List<AminoAcidDihedral> getAminoAcidDihedrals() {
        return aminoAcidDihedrals;
    }

    public List<NucleotideDihedral> getNucleotideDihedrals() {
        return nucleotideDihedrals;
    }

    private List<AminoAcidDihedral> aminoAcidDihedrals;
    private List<NucleotideDihedral> nucleotideDihedrals;

    public DihedralContainer() {
        aminoAcidDihedrals = new Vector<>();
        nucleotideDihedrals = new Vector<>();
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
}
