package pl.poznan.put;

import java.util.List;

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

import static org.hamcrest.CoreMatchers.*;

public class SelectionTest {
  private final PdbParser parser = new PdbParser();

  @Test
  public final void testResidueBonds_74_77A_76() throws Exception {
    final String pdb1OB5 = ResourcesHelper.loadResource("1OB5.pdb");
    final List<PdbModel> models = parser.parse(pdb1OB5);
    Assert.assertThat(models.size(), is(1));
    final PdbModel model = models.get(0);

    final List<PdbChain> chains = model.getChains();
    Assert.assertThat(chains.size(), is(7));

    final PdbChain chainB = chains.stream().filter(chain -> "B".equals(chain.getIdentifier())).findFirst().orElse(null);
      Assert.assertThat(chainB, notNullValue());

    final StructureSelection selection =
        StructureSelection.divideIntoCompactFragments("B", chainB.getResidues());
    final List<PdbCompactFragment> compactFragments = selection.getCompactFragments();
    Assert.assertThat(compactFragments.size(), is(1));
    final PdbCompactFragment compactFragment = compactFragments.get(0);

    final List<PdbResidue> residues = compactFragment.getResidues();
    final int size = residues.size();
    Assert.assertThat(residues.get(size - 3).getResidueIdentifier(), is(new PdbResidueIdentifier("B", 74, " ")));
    Assert.assertThat(residues.get(size - 2).getResidueIdentifier(), is(new PdbResidueIdentifier("B", 77, "A")));
    Assert.assertThat(residues.get(size - 1).getResidueIdentifier(), is(new PdbResidueIdentifier("B", 76, " ")));
  }

  @Test
  public final void testSelectionQueryICode() throws Exception {
    final String pdb1FJG = ResourcesHelper.loadResource("1FJG.pdb");
    final List<PdbModel> models = parser.parse(pdb1FJG);
    Assert.assertThat(models.size(), is(1));
    final PdbModel model = models.get(0);

    final SelectionQuery selectionQuery = SelectionQuery.parse("A:190A:12");
    final List<PdbCompactFragment> compactFragments = selectionQuery.apply(model);
    Assert.assertThat(compactFragments.size(), is(1));
    final PdbCompactFragment compactFragment = compactFragments.get(0);
    Assert.assertThat(compactFragment.getResidues().size(), is(12));

    for (final PdbResidue residue : compactFragment.getResidues()) {
      Assert.assertThat(residue.getResidueNumber(), is(190));
    }
  }

  @Test
  public final void testSelectionQueryGap() throws Exception {
    final String pdb1FJG = ResourcesHelper.loadResource("1FJG_5_10.pdb");
    final List<PdbModel> models = parser.parse(pdb1FJG);
    Assert.assertThat(models.size(), is(1));
    final PdbModel model = models.get(0);

    final StructureSelection autoSelection = SelectionFactory.create("", model);
    final List<PdbCompactFragment> autoFragments = autoSelection.getCompactFragments();
    Assert.assertThat(autoFragments.size(), is(2));

    final SelectionQuery selectionQuery = SelectionQuery.parse("A:5:6");
    final StructureSelection manualSelection = SelectionFactory.create("", model, selectionQuery);
    final List<PdbCompactFragment> manualFragments = manualSelection.getCompactFragments();
    Assert.assertThat(manualFragments.size(), is(1));

    final List<PdbResidue> autoResidues = autoSelection.getResidues();
    final List<PdbResidue> manualResidues = manualSelection.getResidues();
    Assert.assertThat(autoResidues.size(), is(6));
    Assert.assertThat(manualResidues.size(), is(6));

    for (int i = 0; i < 5; i++) {
      Assert.assertThat(manualResidues.get(i), is(autoResidues.get(i)));
    }
  }
}
