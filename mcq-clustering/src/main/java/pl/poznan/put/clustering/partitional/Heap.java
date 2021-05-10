package pl.poznan.put.clustering.partitional;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

final class Heap implements Iterable<Integer> {
  private final PriorityQueue<Integer> data;

  private Heap(final double[] row) {
    super();
    final IndexComparator comparator = new IndexComparator(row);
    data = new PriorityQueue<>(row.length, comparator);

    for (int i = 0; i < row.length; i++) {
      data.add(i);
    }
  }

  public static List<Heap> fromMatrix(final double[][] matrix) {

    return Arrays.stream(matrix).map(Heap::new).collect(Collectors.toList());
  }

  @Override
  public Iterator<Integer> iterator() {
    return data.iterator();
  }
}
