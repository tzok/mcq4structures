package pl.poznan.put.matching;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.biojava.bio.structure.StructureException;

import pl.poznan.put.interfaces.Exportable;
import pl.poznan.put.matching.FragmentSuperimposer.AtomFilter;
import pl.poznan.put.structure.StructureSelection;
import pl.poznan.put.torsion.TorsionAngle;

public class SelectionMatch implements Exportable {
    private final StructureSelection target;
    private final StructureSelection model;
    private final boolean matchChiByType;
    private final List<TorsionAngle> angles;
    private final List<FragmentMatch> fragmentMatches;

    public SelectionMatch(StructureSelection target, StructureSelection model,
            boolean matchChiByType, List<TorsionAngle> angles,
            List<FragmentMatch> fragmentMatches) {
        super();
        this.target = target;
        this.model = model;
        this.matchChiByType = matchChiByType;
        this.angles = angles;
        this.fragmentMatches = fragmentMatches;
    }

    public StructureSelection getTarget() {
        return target;
    }

    public StructureSelection getModel() {
        return model;
    }

    public FragmentMatch getFragmentMatch(int index) {
        return fragmentMatches.get(index);
    }

    public int getSize() {
        return fragmentMatches.size();
    }

    public String[] getResidueLabels() {
        List<String> result = new ArrayList<>();

        for (FragmentMatch fragment : fragmentMatches) {
            result.addAll(Arrays.asList(fragment.getResidueLabels()));
        }

        return result.toArray(new String[result.size()]);
    }

    public String toPDB(boolean onlyMatched) throws StructureException {
        FragmentSuperimposer superimposer = new FragmentSuperimposer(this,
                AtomFilter.ALL, true);
        FragmentSuperposition superposition = onlyMatched ? superimposer.getOnlyMatched()
                : superimposer.getWholeSelections();
        return superposition.toPDB();
    }

    @Override
    public void export(File file) throws IOException {
        // TODO Auto-generated method stub
    }

    @Override
    public File suggestName() {
        // TODO Auto-generated method stub
        return null;
    }
}
