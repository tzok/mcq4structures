package pl.poznan.put;

import org.junit.Assert;
import org.junit.Test;
import pl.poznan.put.matching.SelectionFactory;
import pl.poznan.put.matching.SelectionQuery;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.pdb.PdbResidueIdentifier;
import pl.poznan.put.pdb.analysis.PdbChain;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.pdb.analysis.PdbParser;
import pl.poznan.put.pdb.analysis.PdbResidue;
import pl.poznan.put.utility.ResourcesHelper;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class SelectionTest {
  private final PdbParser parser = new PdbParser();

  @Test
  public final void testResidueBonds_74_77A_76() throws Exception {
    final String pdb1OB5 = ResourcesHelper.loadResource("1OB5.pdb");
    final List<PdbModel> models = parser.parse(pdb1OB5);
    assertEquals(1, models.size());
    final PdbModel model = models.get(0);

    final List<PdbChain> chains = model.getChains();
    assertEquals(7, chains.size());

    PdbChain chainB = null;
    for (final PdbChain chain : chains) {
      if ("B".equals(chain.getIdentifier())) {
        chainB = chain;
        break;
      }
    }
    Assert.assertNotNull(chainB);

    final StructureSelection selection =
        StructureSelection.divideIntoCompactFragments("B", chainB.getResidues());
    final List<PdbCompactFragment> compactFragments = selection.getCompactFragments();
    assertEquals(1, compactFragments.size());
    final PdbCompactFragment compactFragment = compactFragments.get(0);

    final List<PdbResidue> residues = compactFragment.getResidues();
    final int size = residues.size();
    assertEquals(
        new PdbResidueIdentifier("B", 74, " "), residues.get(size - 3).getResidueIdentifier());
    assertEquals(
        new PdbResidueIdentifier("B", 77, "A"), residues.get(size - 2).getResidueIdentifier());
    assertEquals(
        new PdbResidueIdentifier("B", 76, " "), residues.get(size - 1).getResidueIdentifier());
  }

  @Test
  public final void testSelectionQueryICode() throws Exception {
    final String pdb1FJG = ResourcesHelper.loadResource("1FJG.pdb");
    final List<PdbModel> models = parser.parse(pdb1FJG);
    assertEquals(1, models.size());
    final PdbModel model = models.get(0);

    final SelectionQuery selectionQuery = SelectionQuery.parse("A:190A:12");
    final PdbCompactFragment compactFragment = selectionQuery.apply(model);
    assertEquals(12, compactFragment.getResidues().size());

    for (final PdbResidue residue : compactFragment.getResidues()) {
      assertEquals(190, residue.getResidueNumber());
    }
  }

  @Test
  public final void testSelectionQueryGap() throws Exception {
    final String pdb1FJG = ResourcesHelper.loadResource("1FJG_5_10.pdb");
    final List<PdbModel> models = parser.parse(pdb1FJG);
    assertEquals(1, models.size());
    final PdbModel model = models.get(0);

    final StructureSelection autoSelection = SelectionFactory.create("", model);
    final List<PdbCompactFragment> autoFragments = autoSelection.getCompactFragments();
    assertEquals(2, autoFragments.size());

    final SelectionQuery selectionQuery = SelectionQuery.parse("A:5:6");
    final StructureSelection manualSelection = SelectionFactory.create("", model, selectionQuery);
    final List<PdbCompactFragment> manualFragments = manualSelection.getCompactFragments();
    assertEquals(1, manualFragments.size());

    final List<PdbResidue> autoResidues = autoSelection.getResidues();
    final List<PdbResidue> manualResidues = manualSelection.getResidues();
    assertEquals(6, autoResidues.size());
    assertEquals(6, manualResidues.size());

    for (int i = 0; i < 5; i++) {
      assertEquals(autoResidues.get(i), manualResidues.get(i));
    }
  }
}
