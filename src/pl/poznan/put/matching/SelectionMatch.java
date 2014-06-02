package pl.poznan.put.matching;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.biojava.bio.structure.StructureException;

import pl.poznan.put.interfaces.Exportable;
import pl.poznan.put.matching.FragmentSuperimposer.AtomFilter;
import pl.poznan.put.structure.StructureSelection;

public class SelectionMatch implements Exportable {
    private final StructureSelection target;
    private final StructureSelection model;
    private final List<FragmentMatch> fragmentMatches;

    public SelectionMatch(StructureSelection target, StructureSelection model,
            List<FragmentMatch> fragmentMatches) {
        super();
        this.target = target;
        this.model = model;
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
        FragmentSuperposition superposition = onlyMatched ? superimposer.getMatched()
                : superimposer.getWhole();
        return superposition.toPDB();
    }

    @Override
    public void export(File file) throws IOException {
        try {
            FileUtils.write(file, toPDB(false));
        } catch (StructureException e) {
            throw new IOException("Failed to export the match to a PDB file", e);
        }
    }

    @Override
    public File suggestName() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
        StringBuilder filename = new StringBuilder();
        filename.append(sdf.format(new Date()));
        filename.append("-3DSTRA-");
        filename.append(target.getName());
        filename.append("-");
        filename.append(model.getName());
        filename.append(".pdb");
        return new File(filename.toString());
    }
}
