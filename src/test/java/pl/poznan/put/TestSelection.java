package pl.poznan.put;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.pdb.PdbParsingException;
import pl.poznan.put.pdb.PdbResidueIdentifier;
import pl.poznan.put.pdb.analysis.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class TestSelection {
    private final PdbParser parser = new PdbParser();

    private String pdb1OB5;

    @Before
    public void loadPdbFile() throws URISyntaxException, IOException {
        URI uri = getClass().getClassLoader().getResource(".").toURI();
        File dir = new File(uri);
        pdb1OB5 = FileUtils.readFileToString(new File(dir, "../../src/test/resources/1OB5.pdb"), "utf-8");
    }

    @Test
    public void testResidueBonds_74_77A_76() throws PdbParsingException {
        List<PdbModel> models = parser.parse(pdb1OB5);
        Assert.assertEquals(1, models.size());
        PdbModel model = models.get(0);

        List<PdbChain> chains = model.getChains();
        Assert.assertEquals(7, chains.size());

        PdbChain chainB = null;
        for (PdbChain chain : chains) {
            if ("B".equals(chain.getIdentifier())) {
                chainB = chain;
                break;
            }
        }
        Assert.assertNotNull(chainB);

        StructureSelection selection = new StructureSelection("B", chainB.getResidues());
        List<PdbCompactFragment> compactFragments = selection.getCompactFragments();
        Assert.assertEquals(1, compactFragments.size());
        PdbCompactFragment compactFragment = compactFragments.get(0);

        List<PdbResidue> residues = compactFragment.getResidues();
        int size = residues.size();
        Assert.assertEquals(new PdbResidueIdentifier("B", 74, " "), residues.get(size - 3).getResidueIdentifier());
        Assert.assertEquals(new PdbResidueIdentifier("B", 77, "A"), residues.get(size - 2).getResidueIdentifier());
        Assert.assertEquals(new PdbResidueIdentifier("B", 76, " "), residues.get(size - 1).getResidueIdentifier());
    }
}
