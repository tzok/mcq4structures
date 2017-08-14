package pl.poznan.put.comparison;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.poznan.put.circular.Angle;
import pl.poznan.put.circular.samples.AngleSample;
import pl.poznan.put.comparison.exception.IncomparableStructuresException;
import pl.poznan.put.comparison.global.GlobalComparator;
import pl.poznan.put.comparison.global.GlobalMatrix;
import pl.poznan.put.comparison.global.GlobalResult;
import pl.poznan.put.comparison.global.MCQGlobalResult;
import pl.poznan.put.comparison.global.ParallelGlobalComparator;
import pl.poznan.put.comparison.local.LocalComparator;
import pl.poznan.put.comparison.local.LocalResult;
import pl.poznan.put.comparison.local.MCQLocalResult;
import pl.poznan.put.comparison.local.ModelsComparisonResult;
import pl.poznan.put.matching.FragmentMatch;
import pl.poznan.put.matching.MCQMatcher;
import pl.poznan.put.matching.ResidueComparison;
import pl.poznan.put.matching.SelectionFactory;
import pl.poznan.put.matching.SelectionMatch;
import pl.poznan.put.matching.StructureMatcher;
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
import pl.poznan.put.utility.TabularExporter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of MCQ global similarity measure based on torsion angle
 * representation.
 *
 * @author Tomasz Zok (tzok[at]cs.put.poznan.pl)
 */
@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class MCQ implements GlobalComparator, LocalComparator {
    private static final Logger LOGGER = LoggerFactory.getLogger(MCQ.class);

    private final List<MasterTorsionAngleType> angleTypes;

    public MCQ() {
        super();
        angleTypes = new ArrayList<>();
        angleTypes.addAll(Arrays.asList(RNATorsionAngleType.mainAngles()));
        angleTypes.addAll(Arrays.asList(ProteinTorsionAngleType.mainAngles()));
    }

    public MCQ(final MoleculeType moleculeType) {
        super();

        switch (moleculeType) {
            case PROTEIN:
                angleTypes =
                        Arrays.asList(ProteinTorsionAngleType.mainAngles());
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

    public MCQ(final List<MasterTorsionAngleType> angleTypes) {
        super();
        this.angleTypes = new ArrayList<>(angleTypes);
    }

    public static void main(final String[] args)
            throws IOException, PdbParsingException, InterruptedException {
        if (args.length < 2) {
            System.err.println("You must specify at least 2 structures");
            return;
        }

        final List<StructureSelection> selections = new ArrayList<>();

        for (final String arg : args) {
            final File file = new File(arg);

            if (!file.canRead()) {
                System.err.println("Failed to open file: " + file);
                return;
            }

            final PdbModel structure =
                    StructureManager.loadStructure(file).get(0);
            selections.add(SelectionFactory.create(file.getName(), structure));
        }

        final ParallelGlobalComparator.ProgressListener progressListener =
                new ParallelGlobalComparator.ProgressListener() {
                    @Override
                    public void setProgress(final int progress) {
                        // do nothing
                    }

                    @Override
                    public void complete(final GlobalMatrix matrix) {
                        try {
                            TabularExporter
                                    .export(matrix.asExportableTableModel(),
                                            System.out);
                        } catch (final IOException e) {
                            MCQ.LOGGER.error("Failed to output distance matrix",
                                             e);
                        }
                    }
                };
        final ParallelGlobalComparator comparator =
                new ParallelGlobalComparator(new MCQ(), selections,
                                             progressListener);

        comparator.start();
        comparator.join();
    }

    @Override
    public final GlobalResult compareGlobally(final StructureSelection s1,
                                              final StructureSelection s2)
            throws IncomparableStructuresException {
        final StructureMatcher matcher = new MCQMatcher(angleTypes);
        final SelectionMatch matches = matcher.matchSelections(s1, s2);

        if (matches.getFragmentMatches().isEmpty()) {
            throw new IncomparableStructuresException(
                    "No matching fragments found");
        }

        final List<Angle> deltas = new ArrayList<>();

        for (final FragmentMatch fragmentMatch : matches.getFragmentMatches()) {
            for (final ResidueComparison residueComparison : fragmentMatch
                    .getResidueComparisons()) {
                for (final MasterTorsionAngleType angleType : angleTypes) {
                    final TorsionAngleDelta angleDelta =
                            residueComparison.getAngleDelta(angleType);

                    if (angleDelta.getState() ==
                        TorsionAngleDelta.State.BOTH_VALID) {
                        deltas.add(angleDelta.getDelta());
                    }
                }
            }
        }

        return new MCQGlobalResult(getName(), matches, new AngleSample(deltas));
    }

    @Override
    public final String getName() {
        return "MCQ";
    }

    @Override
    public final boolean isAngularMeasure() {
        return true;
    }

    @Override
    public final LocalResult comparePair(final StructureSelection target,
                                         final StructureSelection model) {
        final StructureMatcher matcher = new MCQMatcher(angleTypes);
        final SelectionMatch matches = matcher.matchSelections(target, model);
        return new MCQLocalResult(matches, angleTypes);
    }

    @Override
    public final ModelsComparisonResult compareModels(
            final PdbCompactFragment target,
            final List<PdbCompactFragment> models)
            throws IncomparableStructuresException {
        /*
         * Sanity check
         */
        for (final PdbCompactFragment fragment : models) {
            if ((fragment.getMoleculeType() != target.getMoleculeType()) ||
                (fragment.getResidues().size() !=
                 target.getResidues().size())) {
                throw new IncomparableStructuresException(
                        "All models must be of the same type and size as the " +
                        "reference structure");
            }
        }

        final StructureMatcher matcher = new MCQMatcher(angleTypes);
        final List<PdbCompactFragment> modelsWithoutTarget =
                new ArrayList<>(models);
        modelsWithoutTarget.remove(target);

        final List<FragmentMatch> matches = new ArrayList<>();
        for (final PdbCompactFragment fragment : modelsWithoutTarget) {
            matches.add(matcher.matchFragments(target, fragment));
        }

        return new ModelsComparisonResult(target, modelsWithoutTarget, matches);
    }
}
