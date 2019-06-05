package pl.poznan.put.mcq.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import pl.poznan.put.comparison.MCQ;
import pl.poznan.put.comparison.exception.IncomparableStructuresException;
import pl.poznan.put.comparison.local.LocalComparator;
import pl.poznan.put.comparison.local.SelectedAngle;
import pl.poznan.put.pdb.PdbParsingException;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.rna.torsion.RNATorsionAngleType;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public final class TargetModels {
  private static final Options OPTIONS =
      new Options().addOption(Helper.OPTION_TARGET).addOption(Helper.OPTION_MODELS);

  public static void main(final String[] args)
      throws ParseException, IOException, PdbParsingException, IncomparableStructuresException {
    if (Helper.isHelpRequested(args)) {
      Helper.printHelp("target-models", TargetModels.OPTIONS);
      return;
    }

    final CommandLineParser parser = new DefaultParser();
    final CommandLine commandLine = parser.parse(TargetModels.OPTIONS, args);

    final PdbModel target =
        Helper.loadStructure(
            (File) commandLine.getParsedOptionValue(Helper.OPTION_TARGET.getOpt()));
    final PdbCompactFragment targetFragment = new PdbCompactFragment("", target.getResidues());

    final List<PdbCompactFragment> modelFragments = new ArrayList<>();

    for (final File file :
        (File[]) commandLine.getParsedOptionValue(Helper.OPTION_MODELS.getOpt())) {
      final PdbModel model = Helper.loadStructure(file);
      modelFragments.add(new PdbCompactFragment("", model.getResidues()));
    }

    final LocalComparator mcq = new MCQ(MoleculeType.RNA);
    final SelectedAngle result =
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

  private TargetModels() {
    super();
  }
}
