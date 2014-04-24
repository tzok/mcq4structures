package pl.poznan.put.comparison.bean;

import java.util.List;

import org.biojava.bio.structure.Group;

import pl.poznan.put.torsion.StructureInTorsionAngleSpace;

public class StructureSelection {
    private String name;
    private List<Group> residues;
    private StructureInTorsionAngleSpace torsionAngleSpace;

    public StructureSelection(String name, List<Group> residues) {
        super();
        this.name = name;
        this.residues = residues;

        torsionAngleSpace = new StructureInTorsionAngleSpace(this);
    }

    public String getName() {
        return name;
    }

    public List<Group> getResidues() {
        return residues;
    }

    public StructureInTorsionAngleSpace getTorsionAngleSpace() {
        return torsionAngleSpace;
    }
}
