package pl.poznan.put.matching;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.biojava.bio.structure.StructureException;

import pl.poznan.put.interfaces.Exportable;
import pl.poznan.put.matching.FragmentSuperimposer.AtomFilter;
import pl.poznan.put.types.ExportFormat;

public class SelectionMatch implements Exportable, MatchCollection {
    private final List<String> residueLabels;

    private final StructureSelection target;
    private final StructureSelection model;
    private final List<FragmentMatch> fragmentMatches;

    public SelectionMatch(StructureSelection target, StructureSelection model,
            List<FragmentMatch> fragmentMatches) {
        super();
        this.target = target;
        this.model = model;
        this.fragmentMatches = fragmentMatches;

        residueLabels = makeResidueLabelsList();
    }

    private List<String> makeResidueLabelsList() {
        List<String> result = new ArrayList<>();
        for (FragmentMatch fragment : fragmentMatches) {
            result.addAll(fragment.generateLabelsWithResidueNames());
        }
        return result;
    }

    public StructureSelection getTarget() {
        return target;
    }

    public StructureSelection getModel() {
        return model;
    }

    @Override
    public List<FragmentMatch> getFragmentMatches() {
        return Collections.unmodifiableList(fragmentMatches);
    }

    public int getFragmentCount() {
        return fragmentMatches.size();
    }

    public List<String> getResidueLabels() {
        return Collections.unmodifiableList(residueLabels);
    }

    public String toPDB(boolean onlyMatched) throws StructureException {
        if (fragmentMatches.size() == 0) {
            return "";
        }

        FragmentSuperimposer superimposer = new FragmentSuperimposer(this, AtomFilter.ALL, true);
        FragmentSuperposition superposition = onlyMatched ? superimposer.getMatched() : superimposer.getWhole();
        return superposition.toPDB();
    }

    @Override
    public void export(OutputStream stream) throws IOException {
        try {
            IOUtils.write(toPDB(false), stream, "UTF-8");
        } catch (StructureException e) {
            throw new IOException("Failed to export the match to a PDB file", e);
        }
    }

    @Override
    public ExportFormat getExportFormat() {
        return ExportFormat.PDB;
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
