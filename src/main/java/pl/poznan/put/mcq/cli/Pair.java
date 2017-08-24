package pl.poznan.put.mcq.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.poznan.put.comparison.MCQ;
import pl.poznan.put.comparison.exception.IncomparableStructuresException;
import pl.poznan.put.comparison.local.LocalComparator;
import pl.poznan.put.comparison.local.LocalResult;
import pl.poznan.put.matching.SelectionFactory;
import pl.poznan.put.matching.SelectionQuery;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.pdb.PdbParsingException;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.structure.tertiary.StructureManager;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public final class Pair {
    private static final Logger LOGGER = LoggerFactory.getLogger(Pair.class);

    public static void main(final String[] args)
            throws ParseException, IOException, PdbParsingException,
                   IncomparableStructuresException {
        final Pair pair = new Pair();
        pair.process(args);
    }

    private final ResourceBundle bundle = Messages.getBundle();
    private final Options helpOptions = new Options();
    private final Options options = new Options();

    private Pair() {
        super();

        final Option helpOption =
                new Option("h", "help", false, bundle.getString("print.help"));
        helpOptions.addOption(helpOption);

        options.addRequiredOption("t", "target", true,
                                  bundle.getString("target.structure"));
        options.addRequiredOption("m", "model", true,
                                  bundle.getString("model.structure"));
        options.addOption("st", "selection-target", true, bundle.getString(
                "selection.query.for.target.structure"));
        options.addOption("sm", "selection-model", true, bundle.getString(
                "selection.query.for.model.structure"));
        options.addOption(helpOption);
    }

    private void printHelp() {
        final String footer = bundle.getString("selection.query.syntax");
        final HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("pair", bundle.getString(
                "compare.a.pair.of.structures"), options, footer, true);
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
        final File targetFile = new File(commandLine.getOptionValue('t'));
        final PdbModel target = loadStructure(targetFile);
        final File modelFile = new File(commandLine.getOptionValue('m'));
        final PdbModel model = loadStructure(modelFile);

        final StructureSelection targetSelection =
                Pair.select(target, commandLine.getOptionValue("st", ""));
        final StructureSelection modelSelection =
                Pair.select(model, commandLine.getOptionValue("sm", ""));

        final LocalComparator mcq = new MCQ(MoleculeType.RNA);
        final LocalResult result =
                mcq.comparePair(targetSelection, modelSelection);

        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(System.out, result);
    }

    private PdbModel loadStructure(final File file)
            throws IOException, PdbParsingException {
        final List<? extends PdbModel> models =
                StructureManager.loadStructure(file);
        if (models.isEmpty()) {
            Pair.LOGGER.error(bundle.getString("no.models.found.in.the.file"),
                              file);
        }
        if (models.size() > 1) {
            Pair.LOGGER.warn(bundle.getString("more.than.1.model.found"));
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
