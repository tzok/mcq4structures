package pl.poznan.put.comparison.mapping;

import java.util.List;
import pl.poznan.put.matching.ResidueComparison;
import pl.poznan.put.torsion.MasterTorsionAngleType;
import pl.poznan.put.torsion.TorsionAngleDelta;
import pl.poznan.put.torsion.range.RangeDifference;

/** Map {@link RangeDifference} onto 0-1 scale. */
public final class RangeDifferenceMapper implements ComparisonMapper {
  private static final RangeDifferenceMapper INSTANCE = new RangeDifferenceMapper();

  public static RangeDifferenceMapper getInstance() {
    return RangeDifferenceMapper.INSTANCE;
  }

  private RangeDifferenceMapper() {
    super();
  }

  @Override
  public Double[] map(
      final List<ResidueComparison> residueComparisons,
      final List<MasterTorsionAngleType> angleTypes) {
    final Double[] result = new Double[residueComparisons.size()];

    for (int i = 0; i < residueComparisons.size(); i++) {
      final ResidueComparison residueComparison = residueComparisons.get(i);
      int value = RangeDifference.EQUAL.getValue();

      for (final MasterTorsionAngleType angleType : angleTypes) {
        final TorsionAngleDelta angleDelta = residueComparison.getAngleDelta(angleType);
        final RangeDifference rangeDifference = angleDelta.getRangeDifference();
        value = Math.max(value, rangeDifference.getValue());
      }

      result[i] = (double) value / RangeDifference.OPPOSITE.getValue();
    }

    return result;
  }
}
