package pl.poznan.put.mcq.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PatternOptionBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.poznan.put.comparison.MCQ;
import pl.poznan.put.comparison.exception.IncomparableStructuresException;
import pl.poznan.put.comparison.local.LocalComparator;
import pl.poznan.put.comparison.local.ModelsComparisonResult;
import pl.poznan.put.matching.SelectionFactory;
import pl.poznan.put.matching.SelectionQuery;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.pdb.PdbParsingException;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.rna.torsion.RNATorsionAngleType;
import pl.poznan.put.structure.tertiary.StructureManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public final class TargetModels {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(TargetModels.class);

    public static void main(final String[] args)
            throws ParseException, IOException, PdbParsingException,
                   IncomparableStructuresException {
        final TargetModels targetModels = new TargetModels();
        targetModels.process(args);
    }

    private final ResourceBundle bundle = Messages.getBundle();
    private final Options helpOptions = PatternOptionBuilder.parsePattern("h");
    private final Options options =
            PatternOptionBuilder.parsePattern("!t<T:M<h");

    private TargetModels() {
        super();
    }

    private void printHelp() {
        final String footer = bundle.getString("selection.query.syntax");
        final HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("target-models",
                                bundle.getString("compare.target.models"),
                                options, footer, true);
    }

    private void process(final String[] args)
            throws ParseException, IOException, PdbParsingException,
                   IncomparableStructuresException {
        final CommandLineParser parser = new DefaultParser();
        final CommandLine helpCommandLine =
                parser.parse(helpOptions, args, true);

        if (helpCommandLine.hasOption('h')) {
            printHelp();
            return;
        }

        final CommandLine commandLine = parser.parse(options, args);

        if (commandLine.getArgList().isEmpty()) {
            printHelp();
            return;
        }

        final File targetFile = (File) commandLine.getOptionObject('t');
        final PdbModel target = loadStructure(targetFile);

        final List<PdbModel> models = new ArrayList<>();
        for (final String modelPath : commandLine.getArgs()) {
            final File modelFile = new File(modelPath);
            final PdbModel model = loadStructure(modelFile);
            models.add(model);
        }

        final StructureSelection targetSelection = TargetModels
                .select(target, commandLine.getOptionValue("T", ""));

        final List<StructureSelection> modelSelections = new ArrayList<>();
        final List<String> modelQueries =
                readModelQueries(commandLine, modelSelections.size());
        assert modelQueries.size() == models.size();

        for (int i = 0, size = models.size(); i < size; i++) {
            final PdbModel model = models.get(i);
            final String query = modelQueries.get(i);
            modelSelections.add(TargetModels.select(model, query));
        }

        final List<PdbCompactFragment> targetFragments =
                targetSelection.getCompactFragments();
        if (targetFragments.size() != 1) {
            throw new IllegalArgumentException(
                    bundle.getString("target.consists.of.multiple.fragments"));
        }
        final PdbCompactFragment targetFragment = targetFragments.get(0);

        final List<PdbCompactFragment> modelFragments = new ArrayList<>();
        for (int i = 0, size = modelSelections.size(); i < size; i++) {
            final StructureSelection modelSelection = modelSelections.get(i);
            final List<PdbCompactFragment> fragments =
                    modelSelection.getCompactFragments();

            if (fragments.size() != 1) {
                final String modelPath = commandLine.getArgs()[i];
                final String message = MessageFormat.format(bundle.getString(
                        "model.consists.of.multiple.fragments"), modelPath);
                throw new IllegalArgumentException(message);
            }

            modelFragments.add(fragments.get(0));
        }


        final LocalComparator mcq = new MCQ(MoleculeType.RNA);
        final ModelsComparisonResult.SelectedAngle result =
                mcq.compareModels(targetFragment, modelFragments)
                   .selectAngle(RNATorsionAngleType.getAverageOverMainAngles());

        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(System.out, result);

        final File exportFile = result.suggestName();
        try (final OutputStream stream = new FileOutputStream(exportFile)) {
            result.export(stream);
        }
        System.err.println(exportFile);
    }

    private List<String> readModelQueries(final CommandLine commandLine,
                                          final int expectedSize)
            throws IOException {
        final String[] queries = new String[expectedSize];

        if (commandLine.hasOption('M')) {
            final File queryFile = (File) commandLine.getOptionObject('M');
            final List<String> lines = FileUtils.readLines(queryFile);

            if (lines.size() != expectedSize) {
                throw new IllegalArgumentException(
                        bundle.getString("file.with.model.selections"));
            }

            for (int i = 0, size = lines.size(); i < size; i++) {
                queries[i] = lines.get(i);
            }
        } else {
            Arrays.fill(queries, "");
        }

        return Arrays.asList(queries);
    }

    private PdbModel loadStructure(final File file)
            throws IOException, PdbParsingException {
        final List<? extends PdbModel> models =
                StructureManager.loadStructure(file);
        if (models.isEmpty()) {
            TargetModels.LOGGER
                    .error(bundle.getString("no.models.found.in.the.file"),
                           file);
        }
        if (models.size() > 1) {
            TargetModels.LOGGER
                    .warn(bundle.getString("more.than.1.model.found"));
        }
        return models.get(0);
    }

    private static StructureSelection select(final PdbModel structure,
                                             final String query) {
        if (StringUtils.isNotBlank(query)) {
            return SelectionFactory
                    .create("", structure, SelectionQuery.parse(query));
        }
        return SelectionFactory.create("", structure);
    }
}
