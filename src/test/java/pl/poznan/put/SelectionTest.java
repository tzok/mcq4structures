package pl.poznan.put;

import org.junit.Assert;
import org.junit.Test;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.pdb.PdbResidueIdentifier;
import pl.poznan.put.pdb.analysis.PdbChain;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.pdb.analysis.PdbParser;
import pl.poznan.put.pdb.analysis.PdbResidue;
import pl.poznan.put.utility.ResourcesHelper;

import java.util.List;

public class SelectionTest {
    private final PdbParser parser = new PdbParser();

    @Test
    public final void testResidueBonds_74_77A_76() throws Exception {
        final String pdb1OB5 = ResourcesHelper.loadResource("1OB5.pdb");
        final List<PdbModel> models = parser.parse(pdb1OB5);
        Assert.assertEquals(1, models.size());
        final PdbModel model = models.get(0);

        final List<PdbChain> chains = model.getChains();
        Assert.assertEquals(7, chains.size());

        PdbChain chainB = null;
        for (final PdbChain chain : chains) {
            if ("B".equals(chain.getIdentifier())) {
                chainB = chain;
                break;
            }
        }
        Assert.assertNotNull(chainB);

        final StructureSelection selection = StructureSelection
                .divideIntoCompactFragments("B", chainB.getResidues());
        final List<PdbCompactFragment> compactFragments =
                selection.getCompactFragments();
        Assert.assertEquals(1, compactFragments.size());
        final PdbCompactFragment compactFragment = compactFragments.get(0);

        final List<PdbResidue> residues = compactFragment.getResidues();
        final int size = residues.size();
        Assert.assertEquals(new PdbResidueIdentifier("B", 74, " "),
                            residues.get(size - 3).getResidueIdentifier());
        Assert.assertEquals(new PdbResidueIdentifier("B", 77, "A"),
                            residues.get(size - 2).getResidueIdentifier());
        Assert.assertEquals(new PdbResidueIdentifier("B", 76, " "),
                            residues.get(size - 1).getResidueIdentifier());
    }
}
