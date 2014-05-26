package pl.poznan.put.clustering.hierarchical;

import java.util.ArrayList;
import java.util.List;

public class HierarchicalCluster {
    private final List<Integer> items;
    private final HierarchicalCluster left;
    private final HierarchicalCluster right;
    private final double x;
    private final double y;

    HierarchicalCluster(HierarchicalCluster left, HierarchicalCluster right,
            double distance) {
        super();
        items = new ArrayList<Integer>();
        items.addAll(left.items);
        items.addAll(right.items);
        x = (left.x + right.x) / 2.0;
        y = Math.max(left.y, right.y) + distance;
        this.left = left;
        this.right = right;
    }

    HierarchicalCluster(List<Integer> items, double x, double y) {
        super();
        this.items = items;
        left = null;
        right = null;
        this.x = x;
        this.y = y;
    }

    public List<Integer> getItems() {
        return items;
    }

    public HierarchicalCluster getLeft() {
        return left;
    }

    public HierarchicalCluster getRight() {
        return right;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
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
        builder.append("x = ");
        builder.append(x);
        builder.append('\n');
        builder.append("y = ");
        builder.append(y);
        builder.append('\n');
        if (left != null) {
            builder.append("left:\n");
            builder.append(left.toString());
        }
        if (right != null) {
            builder.append("right:\n");
            builder.append(right.toString());
        }
        return builder.toString();
    }
}
