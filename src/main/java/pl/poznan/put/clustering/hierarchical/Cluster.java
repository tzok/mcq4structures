package pl.poznan.put.clustering.hierarchical;

import java.util.ArrayList;
import java.util.List;

public class Cluster {
    private final List<Integer> items;

    Cluster(int item) {
        super();
        items = new ArrayList<>();
        items.add(item);
    }

    Cluster(List<Integer> items) {
        super();
        this.items = items;
    }

    public static Cluster merge(Cluster left, Cluster right) {
        List<Integer> items = new ArrayList<>();
        items.addAll(left.items);
        items.addAll(right.items);
        return new Cluster(items);
    }

    public List<Integer> getItems() {
        return items;
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
