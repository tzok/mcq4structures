package pl.poznan.put.comparison;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.poznan.put.circular.Angle;
import pl.poznan.put.circular.samples.AngleSample;
import pl.poznan.put.comparison.global.GlobalComparator;
import pl.poznan.put.comparison.global.GlobalMatrix;
import pl.poznan.put.comparison.global.GlobalResult;
import pl.poznan.put.comparison.global.MCQGlobalResult;
import pl.poznan.put.comparison.global.MeasureType;
import pl.poznan.put.comparison.global.ParallelGlobalComparator;
import pl.poznan.put.comparison.local.LocalComparator;
import pl.poznan.put.comparison.local.LocalComparisonResult;
import pl.poznan.put.comparison.local.MCQLocalResult;
import pl.poznan.put.comparison.local.ModelsComparisonResult;
import pl.poznan.put.matching.FragmentMatch;
import pl.poznan.put.matching.MCQMatcher;
import pl.poznan.put.matching.ResidueComparison;
import pl.poznan.put.matching.SelectionFactory;
import pl.poznan.put.matching.SelectionMatch;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.pdb.PdbParsingException;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.protein.torsion.ProteinTorsionAngleType;
import pl.poznan.put.rna.torsion.RNATorsionAngleType;
import pl.poznan.put.structure.tertiary.StructureManager;
import pl.poznan.put.torsion.MasterTorsionAngleType;
import pl.poznan.put.torsion.TorsionAngleDelta;
import pl.poznan.put.torsion.TorsionAngleDelta.State;
import pl.poznan.put.utility.TabularExporter;

/**
 * Implementation of MCQ global similarity measure based on torsion angle
 * representation.
 *
 * @author Tomasz Zok (tzok[at]cs.put.poznan.pl)
 */
public class MCQ implements GlobalComparator, LocalComparator {
    private final static Logger LOGGER = LoggerFactory.getLogger(MCQ.class);

    private final List<MasterTorsionAngleType> angleTypes;

    public MCQ() {
        super();
        angleTypes = new ArrayList<>();
        angleTypes.addAll(Arrays.asList(RNATorsionAngleType.mainAngles()));
        angleTypes.addAll(Arrays.asList(ProteinTorsionAngleType.mainAngles()));
    }

    public MCQ(MoleculeType moleculeType) {
        super();

        switch (moleculeType) {
        case PROTEIN:
            angleTypes = Arrays.asList(ProteinTorsionAngleType.mainAngles());
            break;
        case RNA:
            angleTypes = Arrays.asList(RNATorsionAngleType.mainAngles());
            break;
        case UNKNOWN:
        default:
            angleTypes = Collections.emptyList();
            break;
        }
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
    public GlobalResult compareGlobally(StructureSelection target,
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

        return new MCQGlobalResult(getName(), matches, new AngleSample(deltas));
    }

    @Override
    public LocalComparisonResult comparePair(StructureSelection s1,
            StructureSelection s2) throws IncomparableStructuresException {
        MCQMatcher matcher = new MCQMatcher(angleTypes);
        SelectionMatch matches = matcher.matchSelections(s1, s2);
        return new MCQLocalResult(matches, angleTypes);
    }

    @Override
    public ModelsComparisonResult compareModels(PdbCompactFragment target,
            List<PdbCompactFragment> models) throws IncomparableStructuresException {
        /*
         * Sanity check
         */
        for (PdbCompactFragment fragment : models) {
            if (fragment.getMoleculeType() != target.getMoleculeType() || fragment.size() != target.size()) {
                throw new IncomparableStructuresException("All models must be of the same type and size as the reference structure");
            }
        }

        MCQMatcher matcher = new MCQMatcher(angleTypes);
        List<FragmentMatch> matches = new ArrayList<>();
        List<PdbCompactFragment> modelsWithoutTarget = new ArrayList<>(models);
        modelsWithoutTarget.remove(target);

        for (PdbCompactFragment fragment : modelsWithoutTarget) {
            matches.add(matcher.matchFragments(target, fragment));
        }

        return new ModelsComparisonResult(target, modelsWithoutTarget, matches);
    }

    public static void main(String[] args) throws IOException, PdbParsingException, InterruptedException {
        if (args.length < 2) {
            System.err.println("You must specify at least 2 structures");
            return;
        }

        List<StructureSelection> selections = new ArrayList<>();

        for (String arg : args) {
            File file = new File(arg);

            if (!file.canRead()) {
                System.err.println("Failed to open file: " + file);
                return;
            }

            PdbModel structure = StructureManager.loadStructure(file).get(0);
            selections.add(SelectionFactory.create(file.getName(), structure));
        }

        ParallelGlobalComparator comparator = new ParallelGlobalComparator(MeasureType.MCQ, selections, new ParallelGlobalComparator.ProgressListener() {
            @Override
            public void setProgress(int progress) {
                // do nothing
            }

            @Override
            public void complete(GlobalMatrix matrix) {
                try {
                    TabularExporter.export(matrix.asExportableTableModel(), System.out);
                } catch (IOException e) {
                    MCQ.LOGGER.error("Failed to output distance matrix", e);
                }
            }
        });

        comparator.start();
        comparator.join();
    }
}
