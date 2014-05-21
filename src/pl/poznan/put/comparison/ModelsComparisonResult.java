package pl.poznan.put.comparison;

import java.io.File;
import java.io.IOException;
import java.util.List;

import pl.poznan.put.common.TorsionAngle;
import pl.poznan.put.interfaces.Exportable;
import pl.poznan.put.interfaces.Visualizable;
import pl.poznan.put.matching.FragmentComparisonResult;
import pl.poznan.put.matching.FragmentMatch;
import pl.poznan.put.matching.ResidueComparisonResult;
import pl.poznan.put.structure.CompactFragment;

public class ModelsComparisonResult implements Exportable, Visualizable {
    private final TorsionAngle torsionAngle;
    private final CompactFragment reference;
    private final List<CompactFragment> models;
    private final List<FragmentMatch> matches;

    public ModelsComparisonResult(TorsionAngle torsionAngle,
            CompactFragment reference, List<CompactFragment> models,
            List<FragmentMatch> matches) {
        super();
        this.torsionAngle = torsionAngle;
        this.reference = reference;
        this.models = models;
        this.matches = matches;
    }

    public TorsionAngle getTorsionAngle() {
        return torsionAngle;
    }

    public CompactFragment getReference() {
        return reference;
    }

    public List<CompactFragment> getModels() {
        return models;
    }

    public List<FragmentMatch> getMatches() {
        return matches;
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

    @Override
    public void visualize() {
        // TODO Auto-generated method stub

    }

    @Override
    public void visualize3D() {
        // TODO Auto-generated method stub

    }

    @Override
    public void visualizeHighQuality() {
        // TODO Auto-generated method stub

    }

    public int getModelCount() {
        return matches.size();
    }

    public int getFragmentSize() {
        return reference.getSize();
    }

    public List<ResidueComparisonResult> getResidueResults(int column) {
        FragmentMatch fragmentMatch = matches.get(column);
        FragmentComparisonResult bestResult = fragmentMatch.getBestResult();
        return bestResult.getResidueResults();
    }
}
