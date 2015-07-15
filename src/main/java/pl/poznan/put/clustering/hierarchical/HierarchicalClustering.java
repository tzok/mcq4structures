package pl.poznan.put.clustering.hierarchical;

import java.awt.FontMetrics;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.util.SVGConstants;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.tuple.Pair;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;

import pl.poznan.put.interfaces.Visualizable;
import pl.poznan.put.utility.svg.SVGHelper;

public class HierarchicalClustering implements Visualizable {
    private final List<String> names;
    private final List<HierarchicalClusterMerge> merges;

    public HierarchicalClustering(List<String> names,
            List<HierarchicalClusterMerge> merges) {
        super();
        this.names = names;
        this.merges = merges;
    }

    public List<String> getNames() {
        return Collections.unmodifiableList(names);
    }

    public List<HierarchicalClusterMerge> getMerges() {
        return Collections.unmodifiableList(merges);
    }

    @Override
    public SVGDocument visualize() {
        SVGDocument document = SVGHelper.emptyDocument();
        SVGGraphics2D graphics = new SVGGraphics2D(document);

        // final cluster has all items in good order for drawing!
        Cluster last = getFinalCluster();
        List<Integer> items = last.getItems();
        drawNames(graphics, items);
        int maxWidth = getMaxNameDrawnWidth(graphics, items);

        /*
         * Initialize positions of all dendrogram leaves
         */
        List<Cluster> clusters = Clusterer.initialClusterAssignment(names);
        Map<Cluster, Pair<Integer, Integer>> mapCoords = new HashMap<>();
        LineMetrics lineMetrics = SVGHelper.getLineMetrics(graphics);
        int fontHeight = (int) Math.ceil(lineMetrics.getHeight());
        int fontAscent = (int) Math.ceil(lineMetrics.getAscent());

        for (int i = 0; i < items.size(); i++) {
            int item = items.get(i);
            int y = (i + 1) * fontHeight - fontAscent / 2;
            mapCoords.put(clusters.get(item), Pair.of(maxWidth, y));
        }

        /*
         * Draw the dendrogram
         */
        int xf = 0;
        double maxDistance = getMaxClusterDistance();

        for (HierarchicalClusterMerge merge : merges) {
            Cluster left = clusters.get(merge.getLeft());
            Cluster right = clusters.get(merge.getRight());
            Cluster merged = Cluster.merge(left, right);

            Pair<Integer, Integer> coords = mapCoords.get(left);
            int x1 = coords.getLeft();
            int y1 = coords.getRight();
            coords = mapCoords.get(right);
            int x2 = coords.getLeft();
            int y2 = coords.getRight();

            int shift = maxWidth + (int) (merge.getDistance() * (640 - maxWidth) / maxDistance);
            xf = Math.max(shift, shift);

            graphics.drawLine(x1, y1, xf, y1);
            graphics.drawLine(x2, y2, xf, y2);
            graphics.drawLine(xf, y1, xf, y2);

            mapCoords.put(merged, Pair.of(xf, (y1 + y2) / 2));
            clusters.remove(left);
            clusters.remove(right);
            clusters.add(merged);
        }

        Element documentElement = document.getDocumentElement();
        Element root = graphics.getRoot(documentElement);
        Rectangle2D boundingBox = SVGHelper.calculateBoundingBox(document);
        root.setAttributeNS(null, SVGConstants.SVG_VIEW_BOX_ATTRIBUTE, boundingBox.getMinX() + " " + boundingBox.getMinY() + " " + boundingBox.getWidth() + " " + boundingBox.getHeight());
        root.setAttributeNS(null, SVGConstants.SVG_WIDTH_ATTRIBUTE, Double.toString(boundingBox.getWidth()));
        root.setAttributeNS(null, SVGConstants.SVG_HEIGHT_ATTRIBUTE, Double.toString(boundingBox.getHeight()));

        return document;
    }

    private int getMaxNameDrawnWidth(SVGGraphics2D graphics, List<Integer> items) {
        FontMetrics metrics = SVGHelper.getFontMetrics(graphics);
        int maxWidth = Integer.MIN_VALUE;

        for (int i = 0; i < items.size(); i++) {
            int item = items.get(i);
            String name = names.get(item);
            int width = metrics.stringWidth(name);

            if (width > maxWidth) {
                maxWidth = width;
            }
        }

        return maxWidth;
    }

    private void drawNames(SVGGraphics2D graphics, List<Integer> items) {
        LineMetrics lineMetrics = SVGHelper.getLineMetrics(graphics);
        int fontHeight = (int) Math.ceil(lineMetrics.getHeight());

        for (int i = 0; i < items.size(); i++) {
            int item = items.get(i);
            String name = names.get(item);
            graphics.drawString(name, 0, (i + 1) * fontHeight);
        }
    }

    private Cluster getFinalCluster() {
        List<Cluster> clusters = Clusterer.initialClusterAssignment(names);

        for (HierarchicalClusterMerge merge : merges) {
            Cluster left = clusters.get(merge.getLeft());
            Cluster right = clusters.get(merge.getRight());
            Cluster merged = Cluster.merge(left, right);
            clusters.remove(left);
            clusters.remove(right);
            clusters.add(merged);
        }

        assert clusters.size() == 1;
        return clusters.get(0);
    }

    private double getMaxClusterDistance() {
        double maxDistance = Double.NEGATIVE_INFINITY;

        for (HierarchicalClusterMerge merge : merges) {
            double distance = merge.getDistance();
            if (distance > maxDistance) {
                maxDistance = distance;
            }
        }
        return maxDistance;
    }

    @Override
    public void visualize3D() {
        throw new NotImplementedException("3D visualization is not supported for hierarchical clustering");
    }
}
