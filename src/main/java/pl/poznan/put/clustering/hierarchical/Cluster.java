package pl.poznan.put.clustering.hierarchical;

import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@EqualsAndHashCode
public class Cluster {
  private final List<Integer> items;
  private final String name;

  public Cluster(final int item, final String name) {
    super();
    items = new ArrayList<>();
    items.add(item);
    this.name = name;
  }

  private Cluster(final List<Integer> items, final String name) {
    super();
    this.items = items;
    this.name = name;
  }

  public static Cluster merge(final Cluster left, final Cluster right) {
    final List<Integer> items = new ArrayList<>();
    items.addAll(left.items);
    items.addAll(right.items);
    return new Cluster(items, left.name + ", " + right.name);
  }

  private String getName() {
    return name;
  }

  public final List<Integer> getItems() {
    return Collections.unmodifiableList(items);
  }

  @Override
  public final String toString() {
    final StringBuilder builder = new StringBuilder();
    builder.append("items = ");
    for (final int i : items) {
      builder.append(i);
      builder.append(", ");
    }
    builder.delete(builder.length() - 2, builder.length());
    builder.append('\n');
    return builder.toString();
  }
}
