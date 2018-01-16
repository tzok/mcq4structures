package pl.poznan.put.mcq.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import pl.poznan.put.comparison.MCQ;
import pl.poznan.put.comparison.RMSD;
import pl.poznan.put.comparison.exception.IncomparableStructuresException;
import pl.poznan.put.comparison.global.GlobalComparator;
import pl.poznan.put.comparison.global.GlobalResult;
import pl.poznan.put.comparison.global.MCQGlobalResult;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.pdb.PdbParsingException;
import pl.poznan.put.rna.torsion.RNATorsionAngleType;

import java.io.IOException;
import java.util.List;

@SuppressWarnings({"UseOfSystemOutOrSystemErr",
                   "MethodWithTooExceptionsDeclared"})
public final class Global {
    private static final Options OPTIONS =
            new Options().addOption(Helper.OPTION_TARGET)
                         .addOption(Helper.OPTION_MODEL)
                         .addOption(Helper.OPTION_SELECTION_TARGET)
                         .addOption(Helper.OPTION_SELECTION_MODEL)
                         .addOption(Helper.OPTION_ANGLES);

    public static void main(final String[] args)
            throws ParseException, PdbParsingException, IOException,
                   IncomparableStructuresException {
        if (Helper.isHelpRequested(args)) {
            Helper.printHelp("global", Global.OPTIONS);
            return;
        }

        final CommandLineParser parser = new DefaultParser();
        final CommandLine commandLine = parser.parse(Global.OPTIONS, args);
        final StructureSelection s1 = Helper.selectTarget(commandLine);
        final StructureSelection s2 = Helper.selectModel(commandLine);

        final List<RNATorsionAngleType> angles =
                Helper.parseAngles(commandLine);
        final MCQ mcq = new MCQ(angles);
        final GlobalComparator rmsd = new RMSD();

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

    private Global() {
        super();
    }
}
