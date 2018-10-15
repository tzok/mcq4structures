package pl.poznan.put.clustering.partitional;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

public class Heap implements Iterable<Integer> {
  private final PriorityQueue<Integer> data;

  public Heap(double[] row) {
    super();
    IndexComparator comparator = new IndexComparator(row);
    data = new PriorityQueue<>(row.length, comparator);

    for (int i = 0; i < row.length; i++) {
      data.add(i);
    }
  }

  public static List<Heap> fromMatrix(double[][] matrix) {
    List<Heap> list = new ArrayList<>();

    for (double[] element : matrix) {
      list.add(new Heap(element));
    }

    return list;
  }

  @Override
  public Iterator<Integer> iterator() {
    return data.iterator();
  }
}
