package pl.poznan.put.clustering.partitional;

import java.util.Comparator;

/**
 * This comparator contains an array of doubles, but returns the *indices* of values.
 *
 * @author tzok
 */
public class IndexComparator implements Comparator<Integer> {
  private final double[] row;

  public IndexComparator(double[] row) {
    super();
    this.row = row.clone();
  }

  @Override
  public int compare(Integer o1, Integer o2) {
    return Double.compare(row[o1], row[o2]);
  }
}
