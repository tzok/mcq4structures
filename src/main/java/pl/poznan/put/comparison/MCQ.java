package pl.poznan.put.comparison;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pl.poznan.put.circular.Angle;
import pl.poznan.put.circular.samples.AngleSample;
import pl.poznan.put.matching.FragmentMatch;
import pl.poznan.put.matching.MCQMatcher;
import pl.poznan.put.matching.ResidueComparison;
import pl.poznan.put.matching.SelectionFactory;
import pl.poznan.put.matching.SelectionMatch;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.pdb.PdbParsingException;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.protein.torsion.ProteinTorsionAngleType;
import pl.poznan.put.rna.torsion.RNATorsionAngleType;
import pl.poznan.put.structure.tertiary.StructureManager;
import pl.poznan.put.torsion.TorsionAngleDelta;
import pl.poznan.put.torsion.TorsionAngleDelta.State;
import pl.poznan.put.torsion.type.MasterTorsionAngleType;
import pl.poznan.put.utility.TabularExporter;

/**
 * Implementation of MCQ global similarity measure based on torsion angle
 * representation.
 * 
 * @author Tomasz Zok (tzok[at]cs.put.poznan.pl)
 */
public class MCQ implements GlobalComparator, LocalComparator {
    private final List<MasterTorsionAngleType> angleTypes;

    public MCQ() {
        super();
        this.angleTypes = new ArrayList<>();
        this.angleTypes.addAll(Arrays.asList(RNATorsionAngleType.mainAngles()));
        this.angleTypes.addAll(Arrays.asList(ProteinTorsionAngleType.mainAngles()));
    }

    public MCQ(List<MasterTorsionAngleType> angleTypes) {
        super();
        this.angleTypes = angleTypes;
    }

    @Override
    public String getName() {
        return "MCQ";
    }

    @Override
    public GlobalComparisonResult compareGlobally(StructureSelection target,
            StructureSelection model) throws IncomparableStructuresException {
        MCQMatcher matcher = new MCQMatcher(angleTypes);
        SelectionMatch matches = matcher.matchSelections(target, model);

        if (matches == null || matches.size() == 0) {
            throw new IncomparableStructuresException("No matching fragments found");
        }

        List<Angle> deltas = new ArrayList<>();

        for (FragmentMatch fragmentMatch : matches.getFragmentMatches()) {
            for (ResidueComparison residueComparison : fragmentMatch.getResidueComparisons()) {
                for (MasterTorsionAngleType angleType : angleTypes) {
                    TorsionAngleDelta angleDelta = residueComparison.getAngleDelta(angleType);

                    if (angleDelta.getState() == State.BOTH_VALID) {
                        deltas.add(angleDelta.getDelta());
                    }
                }
            }
        }

        AngleSample angleSample = new AngleSample(deltas);
        Angle meanDirection = angleSample.getMeanDirection();
        return new GlobalComparisonResult(getName(), matches, meanDirection.getRadians(), true);
    }

    @Override
    public LocalComparisonResult comparePair(StructureSelection s1,
            StructureSelection s2) throws IncomparableStructuresException {
        MCQMatcher matcher = new MCQMatcher(angleTypes);
        SelectionMatch matches = matcher.matchSelections(s1, s2);
        return new MCQLocalComparisonResult(matches, angleTypes);
    }

    @Override
    public ModelsComparisonResult compareModels(PdbCompactFragment reference,
            List<PdbCompactFragment> models) throws IncomparableStructuresException {
        /*
         * Sanity check
         */
        for (PdbCompactFragment fragment : models) {
            if (fragment.getMoleculeType() != reference.getMoleculeType() || fragment.size() != reference.size()) {
                throw new IncomparableStructuresException("All models must be of the same type and size as the reference structure");
            }
        }

        MCQMatcher matcher = new MCQMatcher(angleTypes);
        List<FragmentMatch> matches = new ArrayList<>();

        for (PdbCompactFragment fragment : models) {
            matches.add(matcher.matchFragments(reference, fragment));
        }

        return new ModelsComparisonResult(reference, models, matches);
    }

    public static void main(String[] args) throws IOException, PdbParsingException {
        if (args.length < 2) {
            System.err.println("You must specify at least 2 structures");
            return;
        }

        List<StructureSelection> selections = new ArrayList<>();

        for (int i = 0; i < args.length; i++) {
            File file = new File(args[i]);

            if (!file.canRead()) {
                System.err.println("Failed to open file: " + file);
                return;
            }

            PdbModel structure = StructureManager.loadStructure(file).get(0);
            selections.add(SelectionFactory.create(file.getName(), structure));
        }

        ParallelGlobalComparator comparator = ParallelGlobalComparator.getInstance(GlobalComparisonMeasure.MCQ);
        GlobalComparisonResultMatrix matrix = comparator.run(selections, IgnoringComparisonListener.getInstance());
        TabularExporter.export(matrix.asExportableTableModel(), System.out);
    }
}
