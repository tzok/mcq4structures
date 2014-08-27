package pl.poznan.put.clustering.hierarchical;

import java.awt.FontMetrics;
import java.awt.font.LineMetrics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.commons.lang3.tuple.Pair;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;

import pl.poznan.put.utility.SVGHelper;

public class HierarchicalClusteringResult {
    private final List<HierarchicalClusterMerge> merges;
    private final int itemsCount;

    public HierarchicalClusteringResult(List<HierarchicalClusterMerge> merges) {
        super();
        this.merges = merges;
        itemsCount = merges.size() + 1;
    }

    public SVGDocument toSVG(String[] names, boolean isRelative) {
        SVGDocument doc = SVGHelper.emptyDocument();
        SVGGraphics2D svg = new SVGGraphics2D(doc);
        FontMetrics metrics = SVGHelper.getFontMetrics(svg);
        LineMetrics lineMetrics = SVGHelper.getLineMetrics(svg);

        int fontHeight = (int) (Math.ceil(lineMetrics.getHeight()));
        int fontAscent = (int) (Math.ceil(lineMetrics.getAscent()));
        double maxDistance = Double.NEGATIVE_INFINITY;

        /*
         * Get the final clustering and also the maximum distance among all
         * merges
         */
        List<HierarchicalCluster> clusters = initializeClustering();
        for (HierarchicalClusterMerge merge : merges) {
            HierarchicalCluster left = clusters.get(merge.getLeft());
            HierarchicalCluster right = clusters.get(merge.getRight());
            HierarchicalCluster merged = HierarchicalCluster.merge(left, right);
            clusters.remove(left);
            clusters.remove(right);
            clusters.add(merged);

            if (isRelative) {
                double distance = merge.getDistance();
                if (distance > maxDistance) {
                    maxDistance = distance;
                }
            } else {
                maxDistance = 3.0;
            }
        }

        assert clusters.size() == 1;
        HierarchicalCluster last = clusters.get(0);
        List<Integer> items = last.getItems();

        /*
         * Find the longest label
         */
        int maxWidth = Integer.MIN_VALUE;
        for (int i = 0; i < items.size(); i++) {
            int item = items.get(i);
            svg.drawString(names[item], 0, (i + 1) * fontHeight);
            int width = metrics.stringWidth(names[item]);

            if (width > maxWidth) {
                maxWidth = width;
            }
        }

        /*
         * Initialize positions of all dendrogram leaves
         */
        clusters = initializeClustering();
        Map<HierarchicalCluster, Pair<Integer, Integer>> mapCoords = new HashMap<>();

        for (int i = 0; i < items.size(); i++) {
            int item = items.get(i);
            int y = (i + 1) * fontHeight - fontAscent / 2;
            mapCoords.put(clusters.get(item), Pair.of(maxWidth, y));
        }

        /*
         * Draw the dendrogram
         */
        int xf = 0;

        for (HierarchicalClusterMerge merge : merges) {
            HierarchicalCluster left = clusters.get(merge.getLeft());
            HierarchicalCluster right = clusters.get(merge.getRight());
            HierarchicalCluster merged = HierarchicalCluster.merge(left, right);

            Pair<Integer, Integer> coords = mapCoords.get(left);
            int x1 = coords.getLeft();
            int y1 = coords.getRight();
            coords = mapCoords.get(right);
            int x2 = coords.getLeft();
            int y2 = coords.getRight();

            int shift = maxWidth
                    + (int) (merge.getDistance() * (640 - maxWidth) / maxDistance);
            xf = Math.max(shift, shift);

            svg.drawLine(x1, y1, xf, y1);
            svg.drawLine(x2, y2, xf, y2);
            svg.drawLine(xf, y1, xf, y2);

            mapCoords.put(merged, Pair.of(xf, (y1 + y2) / 2));
            clusters.remove(left);
            clusters.remove(right);
            clusters.add(merged);
        }

        Element root = doc.getDocumentElement();
        root.setAttributeNS(SVGDOMImplementation.SVG_NAMESPACE_URI, "width",
                Integer.toString(xf + 10));
        root.setAttributeNS(SVGDOMImplementation.SVG_NAMESPACE_URI, "height",
                Float.toString(items.size() * fontHeight + 10.0f));
        svg.getRoot(root);

        return doc;
    }

    private List<HierarchicalCluster> initializeClustering() {
        List<HierarchicalCluster> clusters = new ArrayList<>();
        for (int i = 0; i < itemsCount; i++) {
            List<Integer> cluster = new ArrayList<>();
            cluster.add(i);
            clusters.add(new HierarchicalCluster(cluster));
        }
        return clusters;
    }
}
