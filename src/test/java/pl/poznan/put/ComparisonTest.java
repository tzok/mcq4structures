package pl.poznan.put;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import pl.poznan.put.comparison.MCQ;
import pl.poznan.put.comparison.RMSD;
import pl.poznan.put.comparison.exception.IncomparableStructuresException;
import pl.poznan.put.comparison.global.GlobalComparator;
import pl.poznan.put.comparison.global.MCQGlobalResult;
import pl.poznan.put.comparison.global.RMSDGlobalResult;
import pl.poznan.put.comparison.local.MCQLocalResult;
import pl.poznan.put.matching.FragmentMatch;
import pl.poznan.put.matching.FragmentSuperimposer;
import pl.poznan.put.matching.ResidueComparison;
import pl.poznan.put.matching.SelectionFactory;
import pl.poznan.put.matching.SelectionMatch;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.pdb.analysis.PdbParser;
import pl.poznan.put.utility.ResourcesHelper;

public class ComparisonTest {
  private static final double[] LOCAL_MCQ_1EHZ_1EVV_DEGREES = {
    35.4680665178865,
    25.711613706472395,
    9.8689552165022,
    3.8440007903643516,
    5.294543905056303,
    7.349824966514344,
    4.4661064521455165,
    4.202373984664651,
    3.9492683015084764,
    5.03767358449838,
    6.94472334470154,
    2.83022604928016,
    14.089151995742064,
    18.823121215659288,
    6.1195052590565036,
    67.57365636730734,
    85.96294569915169,
    43.14107327339782,
    3.29754329005005,
    7.7165014802268646,
    42.545215228703995,
    3.55858579421726,
    4.640309026702089,
    7.308696533373296,
    7.963781741629086,
    9.629071898037026,
    3.501712811117558,
    3.6273243536229534,
    5.024335862346991,
    11.941993123785712,
    5.32512680476551,
    12.725170736366461,
    13.195355859498967,
    13.532615372588657,
    34.330605942939904,
    11.286741829405045,
    9.818146283169263,
    12.202611911414234,
    13.372017171856172,
    13.482332767955809,
    38.11294071424013,
    4.870874250384593,
    5.543791296997311,
    4.763067170251511,
    4.102906814757364,
    3.556263382986494,
    6.790383822046459,
    4.7508773321128,
    2.8756440303588424,
    3.7722848192308485,
    8.845973516865573,
    3.0631371061000543,
    3.315010933026581,
    4.092849756156273,
    2.0475451534827998,
    3.3984436639681257,
    0.5766104128144754,
    2.7528580617573835,
    1.6974910698494987,
    2.195675016514746,
    2.9239345914005335,
    6.995702762712806,
    7.486897925142472,
    3.495960594298345,
    2.147257979592982,
    3.4499628728867,
    4.878834293420059,
    1.2160388719352857,
    3.8770480584974183,
    4.334518365415637,
    5.567387191635025,
    8.102065418905473,
    2.937518503967675,
    7.230646084102875,
    20.044268786812083,
    89.8967392361281
  };

  private final PdbParser parser = new PdbParser();

  @Test
  public final void testMCQGlobal() throws Exception {
    final String pdb1EHZ = ResourcesHelper.loadResource("1EHZ.pdb");
    List<PdbModel> models = parser.parse(pdb1EHZ);
    Assert.assertEquals(1, models.size());
    final PdbModel model1 = models.get(0);

    final String pdb1EVV = ResourcesHelper.loadResource("1EVV.pdb");
    models = parser.parse(pdb1EVV);
    Assert.assertEquals(1, models.size());
    final PdbModel model2 = models.get(0);

    final StructureSelection selection1 = SelectionFactory.create("1EHZ", model1);
    final StructureSelection selection2 = SelectionFactory.create("1EVV", model2);

    final MCQ mcq = new MCQ(MoleculeType.RNA);
    final MCQGlobalResult comparisonResult =
        (MCQGlobalResult) mcq.compareGlobally(selection1, selection2);
    final SelectionMatch selectionMatch = comparisonResult.getSelectionMatch();
    final List<FragmentMatch> fragmentMatches = selectionMatch.getFragmentMatches();
    Assert.assertEquals(1, fragmentMatches.size());
    final FragmentMatch fragmentMatch = fragmentMatches.get(0);

    Assert.assertEquals(3, fragmentMatch.getBothInvalidCount());
    Assert.assertEquals(0, fragmentMatch.getTargetInvalidCount());
    Assert.assertEquals(0, fragmentMatch.getModelInvalidCount());
    Assert.assertEquals(529, fragmentMatch.getValidCount());
    Assert.assertEquals(9.49, comparisonResult.getMeanDirection().getDegrees(), 0.1);
    Assert.assertEquals(0.1601697957, comparisonResult.getMeanDirection().getRadians(), 0.01);
  }

  @Test
  public final void testMCQLocal() throws Exception {
    final String pdb1EHZ = ResourcesHelper.loadResource("1EHZ.pdb");
    List<PdbModel> models = parser.parse(pdb1EHZ);
    Assert.assertEquals(1, models.size());
    final PdbModel model1 = models.get(0);

    final String pdb1EVV = ResourcesHelper.loadResource("1EVV.pdb");
    models = parser.parse(pdb1EVV);
    Assert.assertEquals(1, models.size());
    final PdbModel model2 = models.get(0);

    final StructureSelection selection1 = SelectionFactory.create("1EHZ", model1);
    final StructureSelection selection2 = SelectionFactory.create("1EVV", model2);

    final MCQ mcq = new MCQ(MoleculeType.RNA);
    final MCQLocalResult comparisonResult =
        (MCQLocalResult) mcq.comparePair(selection1, selection2);
    final SelectionMatch selectionMatch = comparisonResult.getSelectionMatch();
    final List<FragmentMatch> fragmentMatches = selectionMatch.getFragmentMatches();
    Assert.assertEquals(1, fragmentMatches.size());
    final FragmentMatch fragmentMatch = fragmentMatches.get(0);

    int i = 0;
    for (final ResidueComparison residueComparison : fragmentMatch.getResidueComparisons()) {
      Assert.assertEquals(
          ComparisonTest.LOCAL_MCQ_1EHZ_1EVV_DEGREES[i],
          residueComparison.getMeanDirection().getDegrees(),
          0.1);
      i += 1;
    }
  }

  @Test
  public final void testRMSD() throws Exception {
    final String pdb1EHZ = ResourcesHelper.loadResource("1EHZ.pdb");
    List<PdbModel> models = parser.parse(pdb1EHZ);
    Assert.assertEquals(1, models.size());
    final PdbModel model1 = models.get(0);

    final String pdb1EVV = ResourcesHelper.loadResource("1EVV.pdb");
    models = parser.parse(pdb1EVV);
    Assert.assertEquals(1, models.size());
    final PdbModel model2 = models.get(0);

    final StructureSelection selection1 = SelectionFactory.create("1EHZ", model1);
    final StructureSelection selection2 = SelectionFactory.create("1EVV", model2);

    final RMSD rmsd = new RMSD(FragmentSuperimposer.AtomFilter.MAIN, true);
    final RMSDGlobalResult comparisonResult =
        (RMSDGlobalResult) rmsd.compareGlobally(selection1, selection2);

    Assert.assertEquals(76, comparisonResult.getAtomCount());
    Assert.assertEquals(0.5934545967, comparisonResult.getRMSD(), 0.1);
  }

  @Test(expected = IncomparableStructuresException.class)
  public final void testNoMatches() throws Exception {
    final String pdb1EHZ = ResourcesHelper.loadResource("1EHZ.pdb");
    List<PdbModel> models = parser.parse(pdb1EHZ);
    Assert.assertEquals(1, models.size());
    final PdbModel model1 = models.get(0);

    final String pdb1EVV = ResourcesHelper.loadResource("1EVV.pdb");
    models = parser.parse(pdb1EVV);
    Assert.assertEquals(1, models.size());
    final PdbModel model2 = models.get(0);

    final StructureSelection selection1 = SelectionFactory.create("1EHZ", model1);
    final StructureSelection selection2 = SelectionFactory.create("1EVV", model2);

    final GlobalComparator mcq = new MCQ(MoleculeType.PROTEIN);
    mcq.compareGlobally(selection1, selection2);
  }

  @Test
  public final void testRMSD_1ZO1_1ZO3() throws Exception {
    final String pdb1ZO1 = ResourcesHelper.loadResource("1ZO1.pdb");
    List<PdbModel> models = parser.parse(pdb1ZO1);
    Assert.assertEquals(1, models.size());
    final PdbModel model1 = models.get(0);

    final String pdb1ZO3 = ResourcesHelper.loadResource("1ZO3.pdb");
    models = parser.parse(pdb1ZO3);
    Assert.assertEquals(1, models.size());
    final PdbModel model2 = models.get(0);

    final StructureSelection s1 = SelectionFactory.create("1ZO1", model1);
    final StructureSelection s2 = SelectionFactory.create("1ZO3", model2);

    final GlobalComparator rmsd = new RMSD();
    rmsd.compareGlobally(s1, s2);
  }

  @Test
  public final void testMCQ_1TN1_1TN2() throws Exception {
    final String pdb1TN1 = ResourcesHelper.loadResource("1TN1.pdb");
    List<PdbModel> models = parser.parse(pdb1TN1);
    Assert.assertEquals(1, models.size());
    final PdbModel model1 = models.get(0);

    final String pdb1TN2 = ResourcesHelper.loadResource("1TN2.pdb");
    models = parser.parse(pdb1TN2);
    Assert.assertEquals(1, models.size());
    final PdbModel model2 = models.get(0);

    final StructureSelection s1 = SelectionFactory.create("1TN1", model1);
    final StructureSelection s2 = SelectionFactory.create("1TN2", model2);

    final MCQ mcq = new MCQ();
    final MCQGlobalResult comparisonResult = (MCQGlobalResult) mcq.compareGlobally(s1, s2);
    Assert.assertEquals(0, comparisonResult.getMeanDirection().getRadians(), 0.1);
  }
}
