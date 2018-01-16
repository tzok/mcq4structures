package pl.poznan.put.mcq.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.svg.SVGDocument;
import pl.poznan.put.circular.Angle;
import pl.poznan.put.circular.exception.InvalidCircularOperationException;
import pl.poznan.put.circular.exception.InvalidCircularValueException;
import pl.poznan.put.circular.graphics.AngularHistogram;
import pl.poznan.put.comparison.MCQ;
import pl.poznan.put.comparison.exception.IncomparableStructuresException;
import pl.poznan.put.comparison.global.GlobalComparator;
import pl.poznan.put.comparison.global.GlobalMatrix;
import pl.poznan.put.comparison.global.ParallelGlobalComparator;
import pl.poznan.put.comparison.local.LocalComparator;
import pl.poznan.put.comparison.local.ModelsComparisonResult;
import pl.poznan.put.matching.SelectionFactory;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.pdb.PdbParsingException;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.pdb.analysis.PdbParser;
import pl.poznan.put.rna.torsion.RNATorsionAngleType;
import pl.poznan.put.torsion.AverageTorsionAngleType;
import pl.poznan.put.torsion.MasterTorsionAngleType;
import pl.poznan.put.utility.svg.Format;
import pl.poznan.put.utility.svg.SVGHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"UseOfSystemOutOrSystemErr", "CallToSystemExit"})
@Deprecated
public final class App {
    private static final MasterTorsionAngleType DEFAULT_ANGLE_TYPE =
            new AverageTorsionAngleType(MoleculeType.RNA,
                                        RNATorsionAngleType.mainAngles());
    private final Options options = new Options();
    private final Mode mode;
    private final MasterTorsionAngleType angleType;
    private final PdbParser parser = new PdbParser(false);
    private final List<PdbModel> models = new ArrayList<>();
    private final List<StructureSelection> selections = new ArrayList<>();

    private App(final String[] args)
            throws ParseException, McqProcessingException, IOException,
                   PdbParsingException {
        super();

        options.addOption("m", "mode", true, String.format(
                "(required) mode of operation, one of: %s",
                Arrays.toString(Mode.values())));
        options.addOption("a", "angle", true, String.format(
                "(optional for %s mode) torsion angle to be used for " +
                "comparison. One of: %s or default: %s", Mode.TARGET_MODELS,
                Arrays.toString(RNATorsionAngleType.mainAngles()),
                App.DEFAULT_ANGLE_TYPE.getShortDisplayName()));

        final CommandLineParser commandLineParser = new DefaultParser();
        final CommandLine commandLine = commandLineParser.parse(options, args);

        if (!commandLine.hasOption('m')) {
            printHelp();
            System.exit(1);
        }

        angleType = commandLine.hasOption('a') ? RNATorsionAngleType
                .valueOf(commandLine.getOptionValue('a'))
                                               : App.DEFAULT_ANGLE_TYPE;

        mode = Mode.valueOf(commandLine.getOptionValue('m'));

        loadAllModels(commandLine.getArgs());
    }

    private void printHelp() {
        final HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("mcq-cli", options);
    }

    private void loadAllModels(final String[] arguments)
            throws McqProcessingException, IOException, PdbParsingException {
        switch (mode) {
            case SINGLE:
                if (arguments.length != 1) {
                    throw new McqProcessingException(
                            "In SINGLE mode you need to provide exactly 1 " +
                            "structure");
                }
                loadSingleModel(arguments[0]);
                break;
            case MULTIPLE:
                if (arguments.length < 2) {
                    throw new McqProcessingException(
                            "In MULTIPLE mode you need to provide at least 2 " +
                            "structures");
                }
                for (final String argument : arguments) {
                    loadSingleModel(argument);
                }
                break;
            case TARGET_MODELS:
                if (arguments.length < 2) {
                    throw new McqProcessingException(
                            "In TARGET_MODELS mode you need to provide at " +
                            "least 2 structures");
                }
                for (final String argument : arguments) {
                    loadSingleModel(argument);
                }
                break;
        }
    }

    private void loadSingleModel(final String pdbPath)
            throws McqProcessingException, IOException, PdbParsingException {
        final File file = new File(pdbPath);

        if (!file.canRead()) {
            throw new McqProcessingException(
                    "Unreadable file provided: " + pdbPath);
        }

        final List<PdbModel> pdbModels = parser.parse(
                FileUtils.readFileToString(file, Charset.defaultCharset()));

        if (pdbModels.isEmpty()) {
            throw new McqProcessingException(
                    "Invalid PDB file (0 models): " + pdbPath);
        } else if (pdbModels.size() > 1) {
            System.err.println(
                    "WARNING: Multiple models found in a single PDB file. " +
                    "Only the first one will be processed");
        }

        PdbModel firstModel = pdbModels.get(0);
        firstModel = firstModel.filteredNewInstance(MoleculeType.RNA);
        models.add(firstModel);
        selections.add(SelectionFactory
                               .create(FilenameUtils.getBaseName(pdbPath),
                                       firstModel));
    }

    public static void main(final String[] args)
            throws ParseException, McqProcessingException, PdbParsingException,
                   IOException, IncomparableStructuresException {
        final App app = new App(args);
        app.run();
    }

    private void run() throws IOException, McqProcessingException,
                              IncomparableStructuresException {
        switch (mode) {
            case SINGLE:
                runSingle();
                break;
            case MULTIPLE:
                runMultiple();
                break;
            case TARGET_MODELS:
                runTargetModels();
                break;
        }
    }

    private void runSingle() throws IOException, McqProcessingException {
        final PdbModel model = models.get(0);
        final StructureSelection selection =
                SelectionFactory.create("model", model);
        selection.export(System.out);
        App.drawTorsionAnglesHistograms(selection);
    }

    private void runMultiple() {
        final GlobalComparator comparator = new MCQ(MoleculeType.RNA);
        final ParallelGlobalComparator.ProgressListener progressListener =
                new MyProgressListener();
        final ParallelGlobalComparator parallelComparator =
                new ParallelGlobalComparator(comparator, selections,
                                             progressListener);
        parallelComparator.start();
    }

    private void runTargetModels()
            throws McqProcessingException, IncomparableStructuresException,
                   IOException {
        sanityCheck();

        final LocalComparator mcq =
                new MCQ(Collections.singletonList(angleType));
        final StructureSelection target = selections.get(0);
        final List<PdbCompactFragment> targetFragments =
                target.getCompactFragments();

        for (int i = 0; i < targetFragments.size(); i++) {
            final PdbCompactFragment targetFragment = targetFragments.get(i);
            final List<PdbCompactFragment> modelFragments = new ArrayList<>();

            for (int j = 1; j < selections.size(); j++) {
                final StructureSelection model = selections.get(j);
                final List<PdbCompactFragment> compactFragments =
                        model.getCompactFragments();
                modelFragments.add(compactFragments.get(i));
            }

            final ModelsComparisonResult comparisonResult =
                    mcq.compareModels(targetFragment, modelFragments);
            final ModelsComparisonResult.SelectedAngle selectedAngleResults =
                    comparisonResult.selectAngle(angleType);

            final File svgFile = File.createTempFile(
                    target.getName() + '_' + Integer.toString(i + 1) + '_',
                    ".svg");
            final SVGDocument svgDocument = selectedAngleResults.visualize();
            final byte[] bytes = SVGHelper.export(svgDocument, Format.SVG);
            FileUtils.writeByteArrayToFile(svgFile, bytes);
            System.err.println(
                    "Colorbar for fragment " + Integer.toString(i + 1) +
                    " is available here: " + svgFile);

            final File csvFile = File.createTempFile(
                    target.getName() + '_' + Integer.toString(i + 1) + '_',
                    ".csv");
            final FileOutputStream stream = new FileOutputStream(csvFile);
            try {
                selectedAngleResults.export(stream);
                System.err.println(
                        "Results for fragment " + Integer.toString(i + 1) +
                        " are available here: " + csvFile);
            } finally {
                IOUtils.closeQuietly(stream);
            }

        }
    }

    private static void drawTorsionAnglesHistograms(
            final StructureSelection selection)
            throws IOException, McqProcessingException {
        for (final MasterTorsionAngleType masterType : selection
                .getCommonTorsionAngleTypes()) {
            final List<Angle> angles =
                    selection.getValidTorsionAngleValues(masterType);

            if (angles.isEmpty()) {
                continue;
            }

            try {
                final AngularHistogram histogram = new AngularHistogram(angles);
                histogram.draw();

                final File outputFile = File.createTempFile(
                        "mcq-" + masterType.getExportName() + '-', ".svg");
                final SVGDocument svgDocument = histogram.finalizeDrawing();

                final byte[] bytes = SVGHelper.export(svgDocument, Format.SVG);
                FileUtils.writeByteArrayToFile(outputFile, bytes);
                System.err.println(
                        "Histogram for " + masterType.getExportName() +
                        " is available here: " + outputFile);
            } catch (final InvalidCircularValueException |
                    InvalidCircularOperationException e) {
                throw new McqProcessingException(
                        "Failed to visualize torsion angles of type: " +
                        masterType, e);
            }
        }
    }

    private void sanityCheck() throws McqProcessingException {
        final StructureSelection target = selections.get(0);
        final List<PdbCompactFragment> targetFragments =
                target.getCompactFragments();
        final int targetSize = targetFragments.size();

        for (int i = 1; i < selections.size(); i++) {
            final StructureSelection model = selections.get(i);
            final int modelSize = model.getCompactFragments().size();

            if (targetSize != modelSize) {
                throw new McqProcessingException(
                        "The number of fragments in " + target.getName() +
                        " (" + targetSize + ") and " + model.getName() + " (" +
                        modelSize + ") does not match");
            }
        }

        for (int i = 0; i < targetSize; i++) {
            final PdbCompactFragment targetFragment = targetFragments.get(i);
            final int targetFragmentSize = targetFragment.getResidues().size();

            for (int j = 1; j < selections.size(); j++) {
                final StructureSelection model = selections.get(j);
                final List<PdbCompactFragment> modelFragments =
                        model.getCompactFragments();
                final PdbCompactFragment modelFragment = modelFragments.get(i);
                final int modelFragmentSize =
                        modelFragment.getResidues().size();

                if (targetFragmentSize != modelFragmentSize) {
                    throw new McqProcessingException(
                            "The size of fragment " + Integer.toString(i + 1) +
                            " does not match for target " + target.getName() +
                            " (" + targetFragmentSize + ") and model " +
                            model.getName() + " (" + modelFragmentSize + ')');
                }
            }
        }
    }

    private enum Mode {
        SINGLE,
        MULTIPLE,
        TARGET_MODELS
    }

    private static class MyProgressListener
            implements ParallelGlobalComparator.ProgressListener {
        public void setProgress(final int progress) {
            // do nothing
        }

        public final void complete(final GlobalMatrix matrix) {
            try {
                matrix.export(System.out);
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }
}
