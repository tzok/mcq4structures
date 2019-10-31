package pl.poznan.put.svg;

import org.apache.commons.exec.ExecuteException;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.w3c.dom.svg.SVGDocument;
import pl.poznan.put.comparison.MCQ;
import pl.poznan.put.comparison.local.LocalComparator;
import pl.poznan.put.comparison.local.LocalResult;
import pl.poznan.put.comparison.mapping.AngleDeltaMapper;
import pl.poznan.put.matching.FragmentMatch;
import pl.poznan.put.matching.SelectionFactory;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.pdb.analysis.PdbParser;
import pl.poznan.put.utility.ExecHelper;
import pl.poznan.put.utility.ResourcesHelper;
import pl.poznan.put.utility.svg.Format;
import pl.poznan.put.utility.svg.SVGHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ExecHelper.class})
public class SecondaryStructureVisualizerTest {
  @Test
  public void visualize() throws IOException {
    final PdbParser parser = new PdbParser();
    final PdbModel targetPdb = parser.parse(ResourcesHelper.loadResource("1EHZ.pdb")).get(0);
    final StructureSelection target = SelectionFactory.create("", targetPdb);
    final PdbModel modelPdb = parser.parse(ResourcesHelper.loadResource("1EVV.pdb")).get(0);
    final StructureSelection model = SelectionFactory.create("", modelPdb);
    final LocalComparator mcq = new MCQ(MoleculeType.RNA);
    final LocalResult localResult = mcq.comparePair(target, model);
    final FragmentMatch fragmentMatch = localResult.getSelectionMatch().getFragmentMatches().get(0);
    final SVGDocument document =
        SecondaryStructureVisualizer.visualize(fragmentMatch, AngleDeltaMapper.getInstance());

    final File tempFileInkscape = File.createTempFile("mcq-inkscape-", ".png");
    final File tempFileFop = File.createTempFile("mcq-fop-", ".png");

    try {
      try (final OutputStream stream = new FileOutputStream(tempFileInkscape)) {
        stream.write(SVGHelper.export(document, Format.PNG));
      }

      PowerMockito.mockStatic(ExecHelper.class);
      when(ExecHelper.execute(any(), any())).thenThrow(ExecuteException.class);

      try (final OutputStream stream = new FileOutputStream(tempFileFop)) {
        stream.write(SVGHelper.export(document, Format.PNG));
      }
    } finally {
      FileUtils.deleteQuietly(tempFileInkscape);
      FileUtils.deleteQuietly(tempFileFop);
    }
  }
}
