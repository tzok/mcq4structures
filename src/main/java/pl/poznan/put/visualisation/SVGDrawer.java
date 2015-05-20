package pl.poznan.put.visualisation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;

import pl.poznan.put.datamodel.ColoredNamedPoint;
import pl.poznan.put.datamodel.NamedPoint;
import pl.poznan.put.utility.svg.SVGHelper;

public class SVGDrawer {
    private static final int DESIRED_WIDTH = 320;
    private static final int CIRCLE_RADIUS = 8;
    private static final int LINE_WIDTH = 2;

    public static SVGDocument drawPoints(List<? extends NamedPoint> points) {
        SVGDocument document = SVGHelper.emptyDocument();
        SVGGraphics2D svg = new SVGGraphics2D(document);

        svg.setStroke(new BasicStroke(SVGDrawer.LINE_WIDTH));
        double maxDistance = SVGDrawer.calculateMaxDistance(points);
        double scale = SVGDrawer.DESIRED_WIDTH / maxDistance;

        for (NamedPoint point : points) {
            NamedPoint scaled = point.scalarMultiply(scale);

            if (point instanceof ColoredNamedPoint) {
                Color oldValue = svg.getColor();
                Color newValue = ((ColoredNamedPoint) point).getColor();

                svg.setColor(newValue);
                svg.fillOval((int) scaled.getX(), (int) scaled.getY(), SVGDrawer.CIRCLE_RADIUS, SVGDrawer.CIRCLE_RADIUS);
                svg.setColor(oldValue);
            } else {
                svg.drawOval((int) scaled.getX(), (int) scaled.getY(), SVGDrawer.CIRCLE_RADIUS, SVGDrawer.CIRCLE_RADIUS);
            }
        }

        Element documentElement = document.getDocumentElement();
        Element root = svg.getRoot(documentElement);
        NodeList circles = document.getElementsByTagName(SVGConstants.SVG_CIRCLE_TAG);
        assert points.size() == circles.getLength();

        for (int i = 0; i < circles.getLength(); i++) {
            Element circle = (Element) circles.item(i);
            Element title = document.createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI, SVGConstants.SVG_TITLE_TAG);
            NamedPoint namedPoint = points.get(i);
            String name = namedPoint.getName();

            title.setTextContent(name);
            circle.appendChild(title);
        }

        Rectangle2D boundingBox = SVGHelper.calculateBoundingBox(document);
        root.setAttributeNS(null, "viewBox", boundingBox.getMinX() + " " + boundingBox.getMinY() + " " + boundingBox.getWidth() + " " + boundingBox.getHeight());
        root.setAttributeNS(null, "width", Double.toString(boundingBox.getWidth()));
        root.setAttributeNS(null, "height", Double.toString(boundingBox.getHeight()));
        
        return document;
    }

    private static double calculateMaxDistance(List<? extends NamedPoint> points) {
        double maxDistance = 0;

        for (int i = 0; i < points.size(); i++) {
            NamedPoint pi = points.get(i);

            for (int j = i + 1; j < points.size(); j++) {
                NamedPoint pj = points.get(j);
                double distance = pi.distance(pj);

                if (distance > maxDistance) {
                    maxDistance = distance;
                }
            }
        }

        return maxDistance;
    }

    private SVGDrawer() {
        // empty constructor
    }
}
