package pl.poznan.put.mcq.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PatternOptionBuilder;
import org.apache.commons.io.FileUtils;
import pl.poznan.put.comparison.MCQ;
import pl.poznan.put.comparison.RMSD;
import pl.poznan.put.comparison.exception.IncomparableStructuresException;
import pl.poznan.put.comparison.global.GlobalResult;
import pl.poznan.put.comparison.global.MCQGlobalResult;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.pdb.PdbParsingException;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.pdb.analysis.PdbParser;
import pl.poznan.put.pdb.analysis.ResidueCollection;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public final class CompareWholeStructures {
    private static final PdbParser PARSER = new PdbParser(false);

    public static void main(final String[] args)
            throws ParseException, PdbParsingException, IOException,
                   IncomparableStructuresException {
        final Options options = PatternOptionBuilder.parsePattern("!m>!t>");
        final CommandLineParser parser = new DefaultParser();
        final CommandLine commandLine = parser.parse(options, args);
        final PdbModel model = CompareWholeStructures.loadPdb(commandLine, "m");
        final PdbModel target =
                CompareWholeStructures.loadPdb(commandLine, "t");

        final StructureSelection s1 =
                CompareWholeStructures.makeSelection(target);
        final StructureSelection s2 =
                CompareWholeStructures.makeSelection(model);

        final MCQ mcq = new MCQ();
        final RMSD rmsd = new RMSD();

        final MCQGlobalResult mcqResult =
                (MCQGlobalResult) mcq.compareGlobally(s1, s2);
        final GlobalResult rmsdResult = rmsd.compareGlobally(s1, s2);
        System.out.print("MCQ:\t");
        System.out.println(mcqResult.getMeanDirection());
        System.out.print("MedCQ:\t");
        System.out.println(mcqResult.getMedianDirection());
        System.out.print("RMSD:\t");
        System.out.println(rmsdResult.asDouble());
    }

    private static StructureSelection makeSelection(
            final ResidueCollection pdb) {
        final PdbCompactFragment compactFragment =
                new PdbCompactFragment("", pdb.getResidues());
        final Collection<PdbCompactFragment> compactFragments =
                Collections.singleton(compactFragment);
        return new StructureSelection("", compactFragments);
    }

    private static PdbModel loadPdb(final CommandLine commandLine,
                                    final String opt)
            throws ParseException, PdbParsingException, IOException {
        final File modelFile = (File) commandLine.getParsedOptionValue(opt);
        final String content =
                FileUtils.readFileToString(modelFile, Charset.defaultCharset());
        return CompareWholeStructures.PARSER.parse(content).get(0);
    }

    private CompareWholeStructures() {
        super();
    }
}
