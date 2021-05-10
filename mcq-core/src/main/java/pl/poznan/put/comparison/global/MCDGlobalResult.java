package pl.poznan.put.comparison.global;

import org.immutables.value.Value;
import pl.poznan.put.matching.SelectionMatch;

import java.util.Locale;

@Value.Immutable
public abstract class MCDGlobalResult implements GlobalResult {
  @Value.Parameter(order = 2)
  protected abstract double value();

  @Override
  public final String toString() {
    return String.valueOf(value());
  }

  @Override
  public final String shortDisplayName() {
    return String.format(Locale.US, "%.2f", value());
  }

  @Value.Lazy
  public String longDisplayName() {
    return shortDisplayName();
  }

  @Override
  public final String exportName() {
    return shortDisplayName();
  }

  @Override
  public final String measureName() {
    return "MCD";
  }

  @Override
  @Value.Parameter(order = 1)
  public abstract SelectionMatch selectionMatch();

  @Override
  public final double toDouble() {
    return value();
  }
}
