package pl.poznan.put.clustering.hierarchical;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;

public class Cluster {
  private final List<Integer> items;
  private final String name;

  public Cluster(int item, String name) {
    super();
    items = new ArrayList<>();
    items.add(item);
    this.name = name;
  }

  public Cluster(List<Integer> items, String name) {
    super();
    this.items = items;
    this.name = name;
  }

  public static Cluster merge(Cluster left, Cluster right) {
    List<Integer> items = new ArrayList<>();
    items.addAll(left.items);
    items.addAll(right.items);
    return new Cluster(items, left.getName() + ", " + right.getName());
  }

  public String getName() {
    return name;
  }

  public List<Integer> getItems() {
    return items;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (items == null ? 0 : items.hashCode());
    result = prime * result + (name == null ? 0 : name.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Cluster other = (Cluster) obj;
    if (items == null) {
      if (other.items != null) {
        return false;
      }
    } else if (!CollectionUtils.isEqualCollection(items, other.items)) {
      return false;
    }
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("items = ");
    for (int i : items) {
      builder.append(i);
      builder.append(", ");
    }
    builder.delete(builder.length() - 2, builder.length());
    builder.append('\n');
    return builder.toString();
  }
}
