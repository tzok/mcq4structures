package pl.poznan.put.clustering;

import java.util.Iterator;
import java.util.PriorityQueue;

public class Heap {
    private PriorityQueue<Integer> data;

    public Heap(PriorityQueue<Integer> data) {
        this.data = data;
    }

    public Iterator<Integer> iterator() {
        return data.iterator();
    }
}
