package pl.poznan.put.mcq.cli;

import org.apache.batik.transcoder.TranscodingHints;
import org.apache.commons.cli.CommandLine;
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
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class App {
    private static final MasterTorsionAngleType DEFAULT_ANGLE_TYPE =
            new AverageTorsionAngleType(MoleculeType.RNA,
                                        RNATorsionAngleType.mainAngles());
    private final Options options = new Options();
    private final Mode mode;
    private final MasterTorsionAngleType angleType;
    private final PdbParser parser = new PdbParser(false);
    private final List<PdbModel> models = new ArrayList<PdbModel>();
    private final List<StructureSelection> selections =
            new ArrayList<StructureSelection>();
    public App(String[] args)
            throws ParseException, McqProcessingException, IOException,
                   PdbParsingException {
        options.addOption("m", "mode", true,
                          "(required) mode of operation, one of: " + Arrays
                                  .toString(Mode.values()));
        options.addOption("a", "angle", true,
                          "(optional for " + Mode.TARGET_MODELS
                          + " mode) torsion angle to be used for comparison. "
                          + "One of: "
                          + Arrays.toString(RNATorsionAngleType.mainAngles())
                          + " or default: " + DEFAULT_ANGLE_TYPE
                                  .getShortDisplayName());

        DefaultParser parser = new DefaultParser();
        CommandLine commandLine = parser.parse(options, args);

        if (!commandLine.hasOption('m')) {
            printHelp();
            System.exit(1);
        }

        if (commandLine.hasOption('a')) {
            angleType = RNATorsionAngleType
                    .valueOf(commandLine.getOptionValue('a'));
        } else {
            angleType = DEFAULT_ANGLE_TYPE;
        }

        mode = Mode.valueOf(commandLine.getOptionValue('m'));

        loadAllModels(commandLine.getArgs());
    }

    private void printHelp() {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("mcq-cli", options);
    }

    private void loadAllModels(String[] arguments)
            throws McqProcessingException, IOException, PdbParsingException {
        switch (mode) {
            case SINGLE:
                if (arguments.length != 1) {
                    throw new McqProcessingException(
                            "In SINGLE mode you need to provide exactly 1 "
                            + "structure");
                }
                loadSingleModel(arguments[0]);
                break;
            case MULTIPLE:
                if (arguments.length < 2) {
                    throw new McqProcessingException(
                            "In MULTIPLE mode you need to provide at least 2 "
                            + "structures");
                }
                for (String argument : arguments) {
                    loadSingleModel(argument);
                }
                break;
            case TARGET_MODELS:
                if (arguments.length < 2) {
                    throw new McqProcessingException(
                            "In TARGET_MODELS mode you need to provide at "
                            + "least 2 structures");
                }
                for (String argument : arguments) {
                    loadSingleModel(argument);
                }
                break;
        }
    }

    private void loadSingleModel(String pdbPath)
            throws McqProcessingException, IOException, PdbParsingException {
        File file = new File(pdbPath);

        if (!file.canRead()) {
            throw new McqProcessingException(
                    "Unreadable file provided: " + pdbPath);
        }

        List<PdbModel> pdbModels =
                parser.parse(FileUtils.readFileToString(file));

        if (pdbModels.isEmpty()) {
            throw new McqProcessingException(
                    "Invalid PDB file (0 models): " + pdbPath);
        } else if (pdbModels.size() > 1) {
            System.err.println(
                    "WARNING: Multiple models found in a single PDB file. "
                    + "Only the first one will be processed");
        }

        PdbModel firstModel = pdbModels.get(0);
        firstModel = firstModel.filteredNewInstance(MoleculeType.RNA);
        models.add(firstModel);
        selections.add(SelectionFactory
                               .create(FilenameUtils.getBaseName(pdbPath),
                                       firstModel));
    }

    public static void main(String[] args)
            throws ParseException, McqProcessingException, PdbParsingException,
                   IOException, IncomparableStructuresException {
        App app = new App(args);
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
        PdbModel model = models.get(0);
        StructureSelection selection = SelectionFactory.create("model", model);
        selection.export(System.out);
        drawTorsionAnglesHistograms(selection);
    }

    private void runMultiple() {
        GlobalComparator comparator = new MCQ(MoleculeType.RNA);
        ParallelGlobalComparator.ProgressListener progressListener =
                new ParallelGlobalComparator.ProgressListener() {
                    public void setProgress(int progress) {
                    }

                    public void complete(GlobalMatrix matrix) {
                        try {
                            matrix.export(System.out);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                };
        ParallelGlobalComparator parallelComparator =
                new ParallelGlobalComparator(comparator, selections,
                                             progressListener);
        parallelComparator.run();
    }

    private void runTargetModels()
            throws McqProcessingException, IncomparableStructuresException,
                   IOException {
        sanityCheck();

        MCQ mcq = new MCQ(Collections.singletonList(angleType));
        StructureSelection target = selections.get(0);
        List<PdbCompactFragment> targetFragments = target.getCompactFragments();

        for (int i = 0; i < targetFragments.size(); i++) {
            PdbCompactFragment targetFragment = targetFragments.get(i);
            List<PdbCompactFragment> modelFragments =
                    new ArrayList<PdbCompactFragment>();

            for (int j = 1; j < selections.size(); j++) {
                StructureSelection model = selections.get(j);
                List<PdbCompactFragment> compactFragments =
                        model.getCompactFragments();
                modelFragments.add(compactFragments.get(i));
            }

            ModelsComparisonResult comparisonResult =
                    mcq.compareModels(targetFragment, modelFragments);
            ModelsComparisonResult.SelectedAngle selectedAngleResults =
                    comparisonResult.selectAngle(angleType);

            File svgFile = File.createTempFile(
                    target.getName() + "_" + Integer.toString(i + 1) + "_",
                    ".svg");
            OutputStream stream = new FileOutputStream(svgFile);
            try {
                SVGDocument svgDocument = selectedAngleResults.visualize();
                SVGHelper.export(svgDocument, stream, Format.SVG,
                                 Collections.<TranscodingHints.Key,
                                         Object>emptyMap());
                System.err.println(
                        "Colorbar for fragment " + Integer.toString(i + 1)
                        + " is available here: " + svgFile);
            } finally {
                IOUtils.closeQuietly(stream);
            }

            File csvFile = File.createTempFile(
                    target.getName() + "_" + Integer.toString(i + 1) + "_",
                    ".csv");
            stream = new FileOutputStream(csvFile);
            try {
                selectedAngleResults.export(stream);
                System.err.println(
                        "Results for fragment " + Integer.toString(i + 1)
                        + " are available here: " + csvFile);
            } finally {
                IOUtils.closeQuietly(stream);
            }

        }
    }

    private void drawTorsionAnglesHistograms(StructureSelection selection)
            throws IOException, McqProcessingException {
        for (MasterTorsionAngleType masterType : selection
                .getCommonTorsionAngleTypes()) {
            List<Angle> angles =
                    selection.getValidTorsionAngleValues(masterType);

            if (angles.isEmpty()) {
                continue;
            }

            try {
                AngularHistogram histogram = new AngularHistogram(angles);
                histogram.draw();

                File outputFile = File.createTempFile(
                        "mcq-" + masterType.getExportName() + "-", ".svg");
                SVGDocument svgDocument = histogram.finalizeDrawingAndGetSVG();

                OutputStream stream = new FileOutputStream(outputFile);
                try {
                    SVGHelper.export(svgDocument, stream, Format.SVG,
                                     Collections.<TranscodingHints.Key,
                                             Object>emptyMap());
                    System.err.println(
                            "Histogram for " + masterType.getExportName()
                            + " is available here: " + outputFile);
                } finally {
                    IOUtils.closeQuietly(stream);
                }
            } catch (InvalidCircularValueException e) {
                throw new McqProcessingException(
                        "Failed to visualize torsion angles of type: "
                        + masterType, e);
            } catch (InvalidCircularOperationException e) {
                throw new McqProcessingException(
                        "Failed to visualize torsion angles of type: "
                        + masterType, e);
            }
        }
    }

    private void sanityCheck() throws McqProcessingException {
        StructureSelection target = selections.get(0);
        List<PdbCompactFragment> targetFragments = target.getCompactFragments();
        int targetSize = targetFragments.size();

        for (int i = 1; i < selections.size(); i++) {
            StructureSelection model = selections.get(i);
            int modelSize = model.getCompactFragments().size();

            if (targetSize != modelSize) {
                throw new McqProcessingException(
                        "The number of fragments in " + target.getName() + " ("
                        + targetSize + ") and " + model.getName() + " ("
                        + modelSize + ") does not match");
            }
        }

        for (int i = 0; i < targetSize; i++) {
            PdbCompactFragment targetFragment = targetFragments.get(i);
            int targetFragmentSize = targetFragment.size();

            for (int j = 1; j < selections.size(); j++) {
                StructureSelection model = selections.get(j);
                List<PdbCompactFragment> modelFragments =
                        model.getCompactFragments();
                PdbCompactFragment modelFragment = modelFragments.get(i);
                int modelFragmentSize = modelFragment.size();

                if (targetFragmentSize != modelFragmentSize) {
                    throw new McqProcessingException(
                            "The size of fragment " + Integer.toString(i + 1)
                            + " does not match for target " + target.getName()
                            + " (" + targetFragmentSize + ") and model " + model
                                    .getName() + " (" + modelFragmentSize
                            + ")");
                }
            }
        }
    }

    private enum Mode {
        SINGLE,
        MULTIPLE,
        TARGET_MODELS
    }
}
