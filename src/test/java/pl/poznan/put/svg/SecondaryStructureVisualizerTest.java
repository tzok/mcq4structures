package pl.poznan.put.svg;

// THIS CLASS DOES NOT WORK WITH JDK 9+ DUE TO LIMITATIONS OF POWERMOCK 2.x
// @RunWith(PowerMockRunner.class)
// @PrepareForTest({ExecHelper.class})
public class SecondaryStructureVisualizerTest {
  /*  @Test
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
  }*/
}
