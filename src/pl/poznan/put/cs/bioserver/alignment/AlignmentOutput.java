package pl.poznan.put.cs.bioserver.alignment;

import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Group;

public class AlignmentOutput {
    private Chain allAtoms1st;
    private Chain allAtoms2nd;
    private Chain filtered1st;
    private Chain filtered2nd;

    private Chain[] allAtomsChains;
    private Chain[] filteredChains;

    private Group[][][] compactGroups;

    public AlignmentOutput() {
        allAtomsChains = new Chain[2];
        filteredChains = new Chain[2];
        compactGroups = new Group[2][][];
    }

    @SuppressWarnings("unused")
    public AlignmentOutput(Chain[] aligned, Group[][][] compactGroups) {
        allAtomsChains = new Chain[2];
        filteredChains = new Chain[2];
        this.compactGroups = new Group[2][][];

        setAllAtoms1st(aligned[0]);
        setAllAtoms2nd(aligned[1]);
        setFiltered1st(aligned[2]);
        setFiltered2nd(aligned[3]);
    }

    public Chain getAllAtoms1st() {
        return allAtoms1st;
    }

    public Chain getAllAtoms2nd() {
        return allAtoms2nd;
    }

    public Chain[] getAllAtomsChains() {
        return allAtomsChains;
    }

    public Group[][] getCompactGroups(int index) {
        assert index == 0 || index == 1;
        return compactGroups[index];
    }

    public Chain getFiltered1st() {
        return filtered1st;
    }

    public Chain getFiltered2nd() {
        return filtered2nd;
    }

    public Chain[] getFilteredChains() {
        return filteredChains;
    }

    public void setAllAtoms1st(Chain allAtoms1st) {
        this.allAtoms1st = allAtoms1st;
        allAtomsChains[0] = allAtoms1st;
    }

    public void setAllAtoms2nd(Chain allAtoms2nd) {
        this.allAtoms2nd = allAtoms2nd;
        allAtomsChains[1] = allAtoms2nd;
    }

    public void setFiltered1st(Chain filtered1st) {
        this.filtered1st = filtered1st;
        filteredChains[0] = filtered1st;
    }

    public void setFiltered2nd(Chain filtered2nd) {
        this.filtered2nd = filtered2nd;
        filteredChains[1] = filtered2nd;
    }

    @Override
    public String toString() {
        // TODO
        return null;
    }
}
