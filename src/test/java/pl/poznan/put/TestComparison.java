package pl.poznan.put;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pl.poznan.put.comparison.MCQ;
import pl.poznan.put.comparison.RMSD;
import pl.poznan.put.comparison.exception.IncomparableStructuresException;
import pl.poznan.put.comparison.global.MCQGlobalResult;
import pl.poznan.put.comparison.global.RMSDGlobalResult;
import pl.poznan.put.comparison.local.MCQLocalResult;
import pl.poznan.put.matching.FragmentMatch;
import pl.poznan.put.matching.FragmentSuperimposer.AtomFilter;
import pl.poznan.put.matching.ResidueComparison;
import pl.poznan.put.matching.SelectionFactory;
import pl.poznan.put.matching.SelectionMatch;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.pdb.PdbParsingException;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.pdb.analysis.PdbParser;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class TestComparison {
    private static final double[] LOCAL_MCQ_1EHZ_1EVV_RESULT_DEGREES =
            new double[]{36.1634864584, 22.7789260224, 8.8590706084,
                         3.6125338508, 5.0781467246, 6.7376143784, 3.9252613542,
                         4.4375952784, 5.1592191279, 5.4109133952, 6.4897447074,
                         2.8867089644, 12.4598132326, 16.6417814909,
                         5.4917746295, 78.4970859931, 96.3029872508,
                         39.5289997931, 3.2125470611, 8.2266843166,
                         36.9959920019, 3.3370542773, 4.7125448511,
                         7.4190667288, 7.076892767, 8.8118810875, 3.4248563711,
                         3.6742132227, 4.7116202078, 11.03484992, 5.5978459369,
                         12.4424013745, 12.5367560313, 14.5496472125,
                         35.5600613006, 14.2981167911, 9.9506010936,
                         13.2300521426, 12.3888690017, 12.0262563747,
                         36.9960472546, 4.4607779956, 5.3694302691,
                         4.4469967556, 3.8373832679, 3.9874894433, 6.1519646766,
                         4.7750170447, 2.6117816223, 3.6344000592, 8.4732840236,
                         3.6246501821, 4.0493667057, 4.0250993549, 2.7120169703,
                         3.1124126297, 0.987384941, 3.1874032165, 1.7374854838,
                         2.6124104969, 2.5625342417, 6.136074705, 6.9765577409,
                         3.1229396754, 2.3371963429, 3.4744854313, 4.4503384207,
                         1.5374581278, 3.537113736, 3.8619735016, 5.614124959,
                         7.1583160413, 2.7489047905, 6.3237528935,
                         17.5487958785, 71.0313243625,};

    private final PdbParser parser = new PdbParser();

    private String pdb1EHZ;
    private String pdb1EVV;
    private String pdb1ZO1;
    private String pdb1ZO3;
    private String pdb1TN1;
    private String pdb1TN2;

    @Before
    public void loadPdbFile() throws URISyntaxException, IOException {
        URI uri = getClass().getClassLoader().getResource(".").toURI();
        File dir = new File(uri);
        pdb1EHZ = FileUtils.readFileToString(
                new File(dir, "../../src/test/resources/1EHZ.pdb"), "utf-8");
        pdb1EVV = FileUtils.readFileToString(
                new File(dir, "../../src/test/resources/1EVV.pdb"), "utf-8");
        pdb1ZO1 = FileUtils.readFileToString(
                new File(dir, "../../src/test/resources/1ZO1.pdb"), "utf-8");
        pdb1ZO3 = FileUtils.readFileToString(
                new File(dir, "../../src/test/resources/1ZO3.pdb"), "utf-8");
        pdb1TN1 = FileUtils.readFileToString(
                new File(dir, "../../src/test/resources/1TN1.pdb"), "utf-8");
        pdb1TN2 = FileUtils.readFileToString(
                new File(dir, "../../src/test/resources/1TN2.pdb"), "utf-8");
    }

    @Test
    public void testMCQGlobal()
            throws PdbParsingException, IncomparableStructuresException {
        List<PdbModel> models = parser.parse(pdb1EHZ);
        Assert.assertEquals(1, models.size());
        PdbModel model1 = models.get(0);

        models = parser.parse(pdb1EVV);
        Assert.assertEquals(1, models.size());
        PdbModel model2 = models.get(0);

        StructureSelection selection1 = SelectionFactory.create("1EHZ", model1);
        StructureSelection selection2 = SelectionFactory.create("1EVV", model2);

        MCQ mcq = new MCQ(MoleculeType.RNA);
        MCQGlobalResult comparisonResult =
                (MCQGlobalResult) mcq.compareGlobally(selection1, selection2);
        SelectionMatch selectionMatch = comparisonResult.getSelectionMatch();
        List<FragmentMatch> fragmentMatches =
                selectionMatch.getFragmentMatches();
        Assert.assertEquals(1, fragmentMatches.size());
        FragmentMatch fragmentMatch = fragmentMatches.get(0);

        Assert.assertEquals(3, fragmentMatch.getBothInvalidCount());
        Assert.assertEquals(0, fragmentMatch.getTargetInvalidCount());
        Assert.assertEquals(0, fragmentMatch.getModelInvalidCount());
        Assert.assertEquals(605, fragmentMatch.getValidCount());
        Assert.assertEquals(9.177053297,
                            comparisonResult.getMeanDirection().getDegrees(),
                            0.1);
        Assert.assertEquals(0.1601697957,
                            comparisonResult.getMeanDirection().getRadians(),
                            0.01);
    }

    @Test
    public void testMCQLocal()
            throws PdbParsingException, IncomparableStructuresException {
        List<PdbModel> models = parser.parse(pdb1EHZ);
        Assert.assertEquals(1, models.size());
        PdbModel model1 = models.get(0);

        models = parser.parse(pdb1EVV);
        Assert.assertEquals(1, models.size());
        PdbModel model2 = models.get(0);

        StructureSelection selection1 = SelectionFactory.create("1EHZ", model1);
        StructureSelection selection2 = SelectionFactory.create("1EVV", model2);

        MCQ mcq = new MCQ(MoleculeType.RNA);
        MCQLocalResult comparisonResult =
                (MCQLocalResult) mcq.comparePair(selection1, selection2);
        SelectionMatch selectionMatch = comparisonResult.getSelectionMatch();
        List<FragmentMatch> fragmentMatches =
                selectionMatch.getFragmentMatches();
        Assert.assertEquals(1, fragmentMatches.size());
        FragmentMatch fragmentMatch = fragmentMatches.get(0);

        int i = 0;
        for (ResidueComparison residueComparison : fragmentMatch
                .getResidueComparisons()) {
            Assert.assertEquals(
                    TestComparison.LOCAL_MCQ_1EHZ_1EVV_RESULT_DEGREES[i],
                    residueComparison.getMeanDirection().getDegrees(), 0.1);
            i += 1;
        }
    }

    @Test
    public void testRMSD()
            throws PdbParsingException, IncomparableStructuresException {
        List<PdbModel> models = parser.parse(pdb1EHZ);
        Assert.assertEquals(1, models.size());
        PdbModel model1 = models.get(0);

        models = parser.parse(pdb1EVV);
        Assert.assertEquals(1, models.size());
        PdbModel model2 = models.get(0);

        StructureSelection selection1 = SelectionFactory.create("1EHZ", model1);
        StructureSelection selection2 = SelectionFactory.create("1EVV", model2);

        RMSD rmsd = new RMSD(AtomFilter.MAIN, true);
        RMSDGlobalResult comparisonResult =
                (RMSDGlobalResult) rmsd.compareGlobally(selection1, selection2);

        Assert.assertEquals(76, comparisonResult.getAtomCount());
        Assert.assertEquals(0.5934545967, comparisonResult.getRMSD(), 0.1);
    }

    @Test(expected = IncomparableStructuresException.class)
    public void testNoMatches()
            throws IncomparableStructuresException, PdbParsingException {
        List<PdbModel> models = parser.parse(pdb1EHZ);
        Assert.assertEquals(1, models.size());
        PdbModel model1 = models.get(0);

        models = parser.parse(pdb1EVV);
        Assert.assertEquals(1, models.size());
        PdbModel model2 = models.get(0);

        StructureSelection selection1 = SelectionFactory.create("1EHZ", model1);
        StructureSelection selection2 = SelectionFactory.create("1EVV", model2);

        MCQ mcq = new MCQ(MoleculeType.PROTEIN);
        mcq.compareGlobally(selection1, selection2);
    }

    @Test
    public void testRMSD_1ZO1_1ZO3()
            throws PdbParsingException, IncomparableStructuresException {
        List<PdbModel> models = parser.parse(pdb1ZO1);
        Assert.assertEquals(1, models.size());
        PdbModel model1 = models.get(0);

        models = parser.parse(pdb1ZO3);
        Assert.assertEquals(1, models.size());
        PdbModel model2 = models.get(0);

        StructureSelection s1 = SelectionFactory.create("1ZO1", model1);
        StructureSelection s2 = SelectionFactory.create("1ZO3", model2);

        RMSD rmsd = new RMSD();
        rmsd.compareGlobally(s1, s2);
    }

    @Test
    public void testMCQ_1TN1_1TN2()
            throws PdbParsingException, IncomparableStructuresException {
        List<PdbModel> models = parser.parse(pdb1TN1);
        Assert.assertEquals(1, models.size());
        PdbModel model1 = models.get(0);

        models = parser.parse(pdb1TN2);
        Assert.assertEquals(1, models.size());
        PdbModel model2 = models.get(0);

        StructureSelection s1 = SelectionFactory.create("1TN1", model1);
        StructureSelection s2 = SelectionFactory.create("1TN2", model2);

        MCQ mcq = new MCQ();
        MCQGlobalResult comparisonResult =
                (MCQGlobalResult) mcq.compareGlobally(s1, s2);
        Assert.assertEquals(0, comparisonResult.getMeanDirection().getRadians(),
                            0.1);
    }
}
