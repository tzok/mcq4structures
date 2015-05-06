package pl.poznan.put;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import pl.poznan.put.common.MoleculeType;
import pl.poznan.put.comparison.IncomparableStructuresException;
import pl.poznan.put.comparison.MCQ;
import pl.poznan.put.comparison.MCQGlobalResult;
import pl.poznan.put.comparison.RMSD;
import pl.poznan.put.comparison.RMSDGlobalResult;
import pl.poznan.put.matching.FragmentMatch;
import pl.poznan.put.matching.FragmentSuperimposer.AtomFilter;
import pl.poznan.put.matching.SelectionFactory;
import pl.poznan.put.matching.SelectionMatch;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.pdb.PdbParsingException;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.pdb.analysis.PdbParser;

public class TestGlobal {
    private final PdbParser parser = new PdbParser();

    private String pdb1EHZ;
    private String pdb1EVV;

    @Before
    public void loadPdbFile() throws URISyntaxException, IOException {
        URI uri = getClass().getClassLoader().getResource(".").toURI();
        File dir = new File(uri);
        pdb1EHZ = FileUtils.readFileToString(new File(dir, "../../src/test/resources/1EHZ.pdb"), "utf-8");
        pdb1EVV = FileUtils.readFileToString(new File(dir, "../../src/test/resources/1EVV.pdb"), "utf-8");
    }

    @Test
    public void testMCQ() throws PdbParsingException, IncomparableStructuresException {
        List<PdbModel> models = parser.parse(pdb1EHZ);
        assertEquals(1, models.size());
        PdbModel model1 = models.get(0);

        models = parser.parse(pdb1EVV);
        assertEquals(1, models.size());
        PdbModel model2 = models.get(0);

        StructureSelection selection1 = SelectionFactory.create("1EHZ", model1);
        StructureSelection selection2 = SelectionFactory.create("1EVV", model2);

        MCQ mcq = new MCQ(MoleculeType.RNA);
        MCQGlobalResult comparisonResult = (MCQGlobalResult) mcq.compareGlobally(selection1, selection2);
        SelectionMatch selectionMatch = comparisonResult.getSelectionMatch();
        List<FragmentMatch> fragmentMatches = selectionMatch.getFragmentMatches();
        assertEquals(1, fragmentMatches.size());
        FragmentMatch fragmentMatch = fragmentMatches.get(0);

        assertEquals(3, fragmentMatch.getBothInvalidCount());
        assertEquals(0, fragmentMatch.getTargetInvalidCount());
        assertEquals(0, fragmentMatch.getModelInvalidCount());
        assertEquals(605, fragmentMatch.getValidCount());
        assertEquals(9.177053297, comparisonResult.getMeanDirection().getDegrees(), 0.1);
        assertEquals(0.1601697957, comparisonResult.getMeanDirection().getRadians(), 0.01);
    }

    @Test
    public void testRMSD() throws PdbParsingException, IncomparableStructuresException {
        List<PdbModel> models = parser.parse(pdb1EHZ);
        assertEquals(1, models.size());
        PdbModel model1 = models.get(0);

        models = parser.parse(pdb1EVV);
        assertEquals(1, models.size());
        PdbModel model2 = models.get(0);

        StructureSelection selection1 = SelectionFactory.create("1EHZ", model1);
        StructureSelection selection2 = SelectionFactory.create("1EVV", model2);

        RMSD rmsd = new RMSD(AtomFilter.MAIN, true);
        RMSDGlobalResult comparisonResult = (RMSDGlobalResult) rmsd.compareGlobally(selection1, selection2);

        assertEquals(76, comparisonResult.getAtomCount());
        assertEquals(0.593, comparisonResult.getRMSD(), 0.1);
    }
}
