package pl.poznan.put.comparison;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.poznan.put.matching.FragmentComparison;
import pl.poznan.put.matching.FragmentMatch;
import pl.poznan.put.matching.MCQMatcher;
import pl.poznan.put.matching.ResidueComparison;
import pl.poznan.put.matching.SelectionFactory;
import pl.poznan.put.matching.SelectionMatch;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.structure.tertiary.StructureManager;
import pl.poznan.put.torsion.TorsionAngleDelta;
import pl.poznan.put.torsion.TorsionAnglesHelper;
import pl.poznan.put.torsion.TorsionAngleDelta.State;
import pl.poznan.put.torsion.type.TorsionAngleType;

/**
 * Implementation of MCQ global similarity measure based on torsion angle
 * representation.
 * 
 * @author Tomasz Zok (tzok[at]cs.put.poznan.pl)
 */
public class MCQ implements GlobalComparator, LocalComparator {
    private static final Logger LOGGER = LoggerFactory.getLogger(MCQ.class);

    private final List<TorsionAngleType> angles;

    public MCQ(List<TorsionAngleType> angles) {
        this.angles = angles;
    }

    @Override
    public String getName() {
        return "MCQ";
    }

    @Override
    public GlobalComparisonResult compareGlobally(StructureSelection target,
            StructureSelection model) throws IncomparableStructuresException {
        MCQMatcher matcher = new MCQMatcher(angles);
        SelectionMatch matches = matcher.matchSelections(target, model);

        if (matches == null || matches.getSize() == 0) {
            throw new IncomparableStructuresException("No matching fragments found");
        }

        List<Double> deltas = new ArrayList<>();

        for (int i = 0; i < matches.getSize(); i++) {
            FragmentMatch fragment = matches.getFragmentMatch(i);
            MCQ.LOGGER.debug("Taking into account fragments: " + fragment);
            FragmentComparison fragmentComparison = fragment.getFragmentComparison();

            for (ResidueComparison residueComparison : fragmentComparison) {
                for (TorsionAngle torsionAngle : angles) {
                    TorsionAngleDelta angleDelta = residueComparison.getAngleDelta(torsionAngle);

                    if (angleDelta != null && angleDelta.getState() == State.BOTH_VALID) {
                        deltas.add(angleDelta.getDelta());
                    }
                }
            }
        }

        double mcq = TorsionAnglesHelper.calculateMean(deltas);
        return new GlobalComparisonResult(getName(), matches, mcq, true);
    }

    @Override
    public LocalComparisonResult comparePair(StructureSelection s1,
            StructureSelection s2) throws IncomparableStructuresException {
        MCQMatcher matcher = new MCQMatcher(angles);
        SelectionMatch matches = matcher.matchSelections(s1, s2);
        return new MCQLocalComparisonResult(matches, angles);
    }

    @Override
    public ModelsComparisonResult compareModels(PdbCompactFragment reference,
            List<PdbCompactFragment> models) throws IncomparableStructuresException {
        /*
         * Sanity check
         */
        for (PdbCompactFragment fragment : models) {
            if (fragment.moleculeType() != reference.moleculeType() || fragment.size() != reference.size()) {
                throw new IncomparableStructuresException("All models must be of the same type and size as the reference structure");
            }
        }

        MCQMatcher matcher = new MCQMatcher(angles);
        List<FragmentMatch> matches = new ArrayList<>();

        for (PdbCompactFragment fragment : models) {
            matches.add(matcher.matchFragments(reference, fragment));
        }

        return new ModelsComparisonResult(reference, models, matches);
    }

    public static void main(String[] args) throws IOException {
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

        GlobalComparisonResultMatrix matrix = ParallelGlobalComparison.run(new MCQ(MCQ.getAllAvailableTorsionAngles()), selections, null);
        System.out.println(TabularExporter.export(matrix.asExportableTableModel()));
    }
}
