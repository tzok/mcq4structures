package pl.poznan.put.cs.bioserver.clustering;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class ClustererHierarchical {
    public static class Cluster implements Serializable {
        private static final long serialVersionUID = -6276493161058681710L;

        public final List<Integer> items;
        public final double x;
        public final double y;
        public Cluster left;
        public Cluster right;

        public Cluster(Cluster left, Cluster right, double distance) {
            super();
            items = new ArrayList<>();
            items.addAll(left.items);
            items.addAll(right.items);
            x = (left.x + right.x) / 2.0;
            y = Math.max(left.y, right.y) + distance;
            this.left = left;
            this.right = right;
        }

        public Cluster(List<Integer> items, double x, double y) {
            super();
            this.items = items;
            this.x = x;
            this.y = y;
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

    /** Available hierarchical clustering types. */
    public enum Linkage {
        Complete, Single, Average;
    }

    public static List<Cluster> hierarchicalClustering(double[][] matrix, Linkage linkage) {
        /*
         * initialise clusters as single elements
         */
        List<Cluster> clusters = new ArrayList<>();
        for (int i = 0; i < matrix.length; ++i) {
            List<Integer> c = new ArrayList<>();
            c.add(i);
            clusters.add(new Cluster(c, i, 0));
        }

        List<Cluster> result = new ArrayList<>();
        while (clusters.size() > 1) {
            /*
             * get two clusters to be merged
             */
            Cluster[] toMerge = new Cluster[2];
            double leastDiff = Double.POSITIVE_INFINITY;
            for (int i = 0; i < clusters.size(); ++i) {
                for (int j = i + 1; j < clusters.size(); ++j) {
                    List<Integer> c1 = clusters.get(i).items;
                    List<Integer> c2 = clusters.get(j).items;
                    double delta = 0;

                    switch (linkage) {
                    case Single:
                        delta = Double.POSITIVE_INFINITY;
                        for (int m : c1) {
                            for (int n : c2) {
                                if (matrix[m][n] < delta) {
                                    delta = matrix[m][n];
                                }
                            }
                        }
                        break;
                    case Complete:
                        delta = Double.NEGATIVE_INFINITY;
                        for (int m : c1) {
                            for (int n : c2) {
                                if (matrix[m][n] > delta) {
                                    delta = matrix[m][n];
                                }
                            }
                        }
                        break;
                    case Average:
                        int count = 0;
                        for (int m : c1) {
                            for (int n : c2) {
                                delta += matrix[m][n];
                                count++;
                            }
                        }
                        delta /= count;
                        break;
                    default:
                        throw new RuntimeException("Unknown type of linkage for hierarchical "
                                + "clustering: " + linkage);
                    }

                    if (delta < leastDiff) {
                        toMerge[0] = clusters.get(i);
                        toMerge[1] = clusters.get(j);
                        leastDiff = delta;
                    }
                }
            }

            /*
             * merge clusters
             */
            Cluster merged = new Cluster(toMerge[0], toMerge[1], leastDiff);
            result.add(merged);

            clusters.remove(toMerge[0]);
            clusters.remove(toMerge[1]);
            clusters.add(merged);
        }
        return result;
    }

    private ClustererHierarchical() {
    }
}
