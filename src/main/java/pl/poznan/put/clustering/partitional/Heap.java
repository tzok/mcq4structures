package pl.poznan.put.clustering.partitional;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

public class Heap implements Iterable<Integer> {
    public static List<Heap> fromMatrix(double[][] matrix) {
        List<Heap> list = new ArrayList<Heap>();

        for (int i = 0; i < matrix.length; i++) {
            list.add(new Heap(matrix[i]));
        }

        return list;
    }

    private final PriorityQueue<Integer> data;

    public Heap(double[] row) {
        super();
        IndexComparator comparator = new IndexComparator(row);
        this.data = new PriorityQueue<Integer>(row.length, comparator);

        for (int i = 0; i < row.length; i++) {
            data.add(i);
        }
    }

    @Override
    public Iterator<Integer> iterator() {
        return data.iterator();
    }
}
