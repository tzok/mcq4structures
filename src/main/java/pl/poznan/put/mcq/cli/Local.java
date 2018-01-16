package pl.poznan.put.mcq.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.svg.SVGDocument;
import pl.poznan.put.comparison.MCQ;
import pl.poznan.put.comparison.local.MCQLocalResult;
import pl.poznan.put.matching.FragmentMatch;
import pl.poznan.put.matching.ResidueComparison;
import pl.poznan.put.matching.SelectionMatch;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.pdb.PdbParsingException;
import pl.poznan.put.rna.torsion.RNATorsionAngleType;
import pl.poznan.put.torsion.TorsionAngleDelta;
import pl.poznan.put.utility.ExecHelper;
import pl.poznan.put.utility.TabularExporter;
import pl.poznan.put.utility.svg.Format;
import pl.poznan.put.utility.svg.SVGHelper;
import pl.poznan.put.visualisation.AngleDeltaMapper;
import pl.poznan.put.visualisation.SecondaryStructureVisualizer;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

@SuppressWarnings({"UseOfSystemOutOrSystemErr",
                   "MethodWithTooExceptionsDeclared"})
public final class Local {
    private static final Options OPTIONS =
            new Options().addOption(Helper.OPTION_TARGET)
                         .addOption(Helper.OPTION_MODEL)
                         .addOption(Helper.OPTION_SELECTION_TARGET)
                         .addOption(Helper.OPTION_SELECTION_MODEL)
                         .addOption(Helper.OPTION_ANGLES);

    public static void main(final String[] args)
            throws ParseException, IOException, PdbParsingException {
        if (Helper.isHelpRequested(args)) {
            Helper.printHelp("local", Local.OPTIONS);
            return;
        }

        final CommandLineParser parser = new DefaultParser();
        final CommandLine commandLine = parser.parse(Local.OPTIONS, args);
        final StructureSelection s1 = Helper.selectTarget(commandLine);
        final StructureSelection s2 = Helper.selectModel(commandLine);

        final List<RNATorsionAngleType> angleTypes =
                Helper.parseAngles(commandLine);
        final MCQ mcq = new MCQ(angleTypes);
        final MCQLocalResult result = (MCQLocalResult) mcq.comparePair(s1, s2);

        final File directory = ExecHelper.createRandomDirectory();
        final File csvFile = new File(directory, "results.csv");

        try (OutputStream stream = new FileOutputStream(csvFile)) {
            result.export(stream);
        }

        final SelectionMatch selectionMatch = result.getSelectionMatch();
        final List<FragmentMatch> fragmentMatches =
                selectionMatch.getFragmentMatches();

        for (int i = 0, fragmentMatchesSize = fragmentMatches.size();
             i < fragmentMatchesSize; i++) {
            final FragmentMatch fragmentMatch = fragmentMatches.get(i);
            final File chartFile =
                    new File(directory, String.format("chart-%02d.svg", i));
            final SVGDocument chartSvg = fragmentMatch.visualize(1052, 744);
            FileUtils.writeByteArrayToFile(chartFile, SVGHelper
                    .export(chartSvg, Format.SVG));

            final File secondaryFile =
                    new File(directory, String.format("secondary-%02d.svg", i));
            final SVGDocument secondarySvg = SecondaryStructureVisualizer
                    .visualize(fragmentMatch, AngleDeltaMapper.getInstance());
            FileUtils.writeByteArrayToFile(secondaryFile, SVGHelper
                    .export(secondarySvg, Format.SVG));

            for (final RNATorsionAngleType angleType : angleTypes) {
                final File angleRangesFile = new File(directory, String.format(
                        "angle-%s-%02d.csv", angleType.getExportName(), i));
                final List<ResidueComparison> residueComparisons =
                        fragmentMatch.getResidueComparisons();
                final Object[][] data =
                        new Object[fragmentMatch.getResidueCount()][];

                for (int j = 0, size = residueComparisons.size(); j < size;
                     j++) {
                    final ResidueComparison residueComparison =
                            residueComparisons.get(j);
                    final TorsionAngleDelta angleDelta =
                            residueComparison.getAngleDelta(angleType);

                    data[j] = new Object[8];
                    data[j][0] = residueComparison.getTarget();
                    data[j][1] = angleDelta.getTarget().getDegrees();
                    data[j][2] = angleType.getRange(angleDelta.getTarget())
                                          .getDisplayName();
                    data[j][3] = residueComparison.getModel();
                    data[j][4] = angleDelta.getModel().getDegrees();
                    data[j][5] = angleType.getRange(angleDelta.getModel())
                                          .getDisplayName();
                    data[j][6] = angleDelta.getDelta().getDegrees();
                    data[j][7] = angleDelta.getRangeDifference();
                }

                final String[] columns =
                        {"Target Residue", "Target Value", "Target Range",
                         "Model Residue", "Model Value", "Model Range",
                         "Value Difference", "Range Difference"};
                final TableModel tableModel =
                        new DefaultTableModel(data, columns);

                try (OutputStream stream = new FileOutputStream(
                        angleRangesFile)) {
                    TabularExporter.export(tableModel, stream);
                }
            }
        }

        System.out.println("Results are available in: " + directory);
    }

    private Local() {
        super();
    }
}
