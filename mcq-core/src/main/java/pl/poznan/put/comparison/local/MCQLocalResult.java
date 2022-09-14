package pl.poznan.put.comparison.local;

import java.util.List;
import org.immutables.value.Value;
import pl.poznan.put.matching.SelectionMatch;
import pl.poznan.put.torsion.MasterTorsionAngleType;

@Value.Immutable
public abstract class MCQLocalResult implements LocalResult {
  @Override
  @Value.Parameter(order = 1)
  public abstract SelectionMatch selectionMatch();

  @Override
  @Value.Parameter(order = 2)
  public abstract List<MasterTorsionAngleType> angleTypes();
}
