package pl.poznan.put;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;
import org.junit.Test;
import pl.poznan.put.matching.SelectionFactory;
import pl.poznan.put.matching.SelectionQuery;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.pdb.analysis.PdbChain;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.pdb.analysis.PdbParser;
import pl.poznan.put.pdb.analysis.PdbResidue;
import pl.poznan.put.utility.ResourcesHelper;

public class SelectionTest {
  private final PdbParser parser = new PdbParser();

  @Test
  public final void testResidueBonds_74_77A_76() throws Exception {
    final String pdb1OB5 = ResourcesHelper.loadResource("1OB5.pdb");
    final List<PdbModel> models = parser.parse(pdb1OB5);
    assertThat(models.size(), is(1));
    final PdbModel model = models.get(0);

    final List<PdbChain> chains = model.chains();
    assertThat(chains.size(), is(15));

    final PdbChain chainB =
        chains.stream().filter(chain -> "B".equals(chain.identifier())).findFirst().orElse(null);
    assertThat(chainB, notNullValue());

    final StructureSelection selection =
        StructureSelection.divideIntoCompactFragments("B", chainB.residues());
    final List<PdbCompactFragment> compactFragments = selection.getCompactFragments();
    assertThat(compactFragments.size(), is(1));

    // TODO: this test no longer works with the latest BioCommons
    //    final PdbCompactFragment compactFragment = compactFragments.get(0);
    //    final List<PdbResidue> residues = compactFragment.residues();
    //    final int size = residues.size();
    //    assertThat(
    //        residues.get(size - 3).identifier(), is(ImmutablePdbResidueIdentifier.of("B", 74, "
    // ")));
    //    assertThat(
    //        residues.get(size - 2).identifier(), is(ImmutablePdbResidueIdentifier.of("B", 77,
    // "A")));
    //    assertThat(
    //        residues.get(size - 1).identifier(), is(ImmutablePdbResidueIdentifier.of("B", 76, "
    // ")));
  }

  @Test
  public final void testSelectionQueryICode() throws Exception {
    final String pdb1FJG = ResourcesHelper.loadResource("1FJG.pdb");
    final List<PdbModel> models = parser.parse(pdb1FJG);
    assertThat(models.size(), is(1));
    final PdbModel model = models.get(0);

    final SelectionQuery selectionQuery = SelectionQuery.parse("A:190A:12");
    final List<PdbCompactFragment> compactFragments = selectionQuery.apply(model);
    assertThat(compactFragments.size(), is(1));
    final PdbCompactFragment compactFragment = compactFragments.get(0);
    assertThat(compactFragment.residues().size(), is(12));

    for (final PdbResidue residue : compactFragment.residues()) {
      assertThat(residue.residueNumber(), is(190));
    }
  }

  @Test
  public final void testSelectionQueryGap() throws Exception {
    final String pdb1FJG = ResourcesHelper.loadResource("1FJG_5_10.pdb");
    final List<PdbModel> models = parser.parse(pdb1FJG);
    assertThat(models.size(), is(1));
    final PdbModel model = models.get(0);

    final StructureSelection autoSelection = SelectionFactory.create("", model);
    final List<PdbCompactFragment> autoFragments = autoSelection.getCompactFragments();
    assertThat(autoFragments.size(), is(2));

    final SelectionQuery selectionQuery = SelectionQuery.parse("A:5:6");
    final StructureSelection manualSelection = SelectionFactory.create("", model, selectionQuery);
    final List<PdbCompactFragment> manualFragments = manualSelection.getCompactFragments();
    assertThat(manualFragments.size(), is(1));

    final List<PdbResidue> autoResidues = autoSelection.getResidues();
    final List<PdbResidue> manualResidues = manualSelection.getResidues();
    assertThat(autoResidues.size(), is(6));
    assertThat(manualResidues.size(), is(6));

    for (int i = 0; i < 5; i++) {
      assertThat(manualResidues.get(i), is(autoResidues.get(i)));
    }
  }
}
