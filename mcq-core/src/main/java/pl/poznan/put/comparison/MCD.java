package pl.poznan.put.comparison;

import org.apache.commons.math3.util.FastMath;
import org.immutables.value.Value;
import pl.poznan.put.circular.Angle;
import pl.poznan.put.comparison.global.GlobalComparator;
import pl.poznan.put.comparison.global.GlobalResult;
import pl.poznan.put.comparison.global.ImmutableMCDGlobalResult;
import pl.poznan.put.matching.FragmentMatch;
import pl.poznan.put.matching.ImmutableMCQMatcher;
import pl.poznan.put.matching.SelectionMatch;
import pl.poznan.put.matching.StructureMatcher;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.pdb.analysis.ResidueTorsionAngles;
import pl.poznan.put.torsion.MasterTorsionAngleType;

import java.util.List;

@Value.Immutable
public abstract class MCD implements GlobalComparator {
  @Value.Parameter(order = 1)
  public abstract MoleculeType moleculeType();

  @Value.Default
  public List<MasterTorsionAngleType> angleTypes() {
    return moleculeType().mainAngleTypes();
  }

  @Override
  public String getName() {
    return "MCD";
  }

  @Override
  public boolean isAngularMeasure() {
    return true;
  }

  @Override
  public GlobalResult compareGlobally(final StructureSelection s1, final StructureSelection s2) {
    final StructureMatcher matcher = ImmutableMCQMatcher.of(moleculeType());
    final SelectionMatch matches = matcher.matchSelections(s1, s2);

    double sum = 0.0;
    double validCount = 0.0;

    for (final FragmentMatch match : matches.getFragmentMatches()) {
      final PdbCompactFragment target =
          match.isTargetSmaller()
              ? match.getTargetFragment()
              : match
                  .getTargetFragment()
                  .shifted(match.getShift(), match.getModelFragment().residues().size());
      final PdbCompactFragment model =
          match.isTargetSmaller()
              ? match
                  .getModelFragment()
                  .shifted(match.getShift(), match.getTargetFragment().residues().size())
              : match.getModelFragment();

      for (int i = 0; i < target.residues().size(); i++) {
        final ResidueTorsionAngles targetAngles =
            target.torsionAngles(target.residues().get(i).identifier());
        final ResidueTorsionAngles modelAngles =
            model.torsionAngles(model.residues().get(i).identifier());

        for (final MasterTorsionAngleType angleType : MoleculeType.RNA.mainAngleTypes()) {
          final Angle targetValue = targetAngles.value(angleType);
          final Angle modelValue = modelAngles.value(angleType);

          if (targetValue.isValid() && modelValue.isValid()) {
            final double distance = FastMath.atanh(targetValue.distance(modelValue) / 2.0);

            sum += distance;

            validCount += 1.0;
          }
        }
      }
    }

    final double value = sum / validCount;
    return ImmutableMCDGlobalResult.of(matches, value);
  }
}
