package pl.poznan.put.mcq.cli;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import pl.poznan.put.comparison.ImmutableMCD;
import pl.poznan.put.comparison.global.GlobalResult;
import pl.poznan.put.matching.SelectionFactory;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.pdb.analysis.PdbParser;

public class MCD {
  public static void main(final String[] args) throws ParseException, IOException {
    final Options options = new Options();
    options.addOption(Option.builder("t").hasArg().build());
    options.addOption(Option.builder("m").hasArg().build());

    final DefaultParser parser = new DefaultParser();
    final CommandLine commandLine = parser.parse(options, args);
    final String targetPath = commandLine.getOptionValue("t");
    final String modelPath = commandLine.getOptionValue("m");
    final PdbModel target = MCD.parsePdb(targetPath);
    final PdbModel model = MCD.parsePdb(modelPath);

    final ImmutableMCD mcd = ImmutableMCD.of(MoleculeType.RNA);
    final GlobalResult result =
        mcd.compareGlobally(
            SelectionFactory.create("target", target), SelectionFactory.create("model", model));
    System.out.println(result.toDouble());
  }

  private static PdbModel parsePdb(final String path) throws IOException {
    final String structureContent =
        FileUtils.readFileToString(new File(path), StandardCharsets.UTF_8);
    final PdbParser pdbParser = new PdbParser();
    return pdbParser.parse(structureContent).get(0).filteredNewInstance(MoleculeType.RNA);
  }
}
