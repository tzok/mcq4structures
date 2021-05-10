package pl.poznan.put.clustering.partitional;

import java.io.Serializable;
import java.util.Comparator;

/**
 * This comparator contains an array of doubles, but returns the *indices* of values.
 *
 * @author tzok
 */
class IndexComparator implements Comparator<Integer>, Serializable {
  private final double[] row;

  IndexComparator(final double[] row) {
    super();
    this.row = row.clone();
  }

  @Override
  public final int compare(final Integer t, final Integer t1) {
    return Double.compare(row[t], row[t1]);
  }
}
