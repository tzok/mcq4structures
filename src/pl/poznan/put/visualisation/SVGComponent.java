package pl.poznan.put.visualisation;

import java.awt.Dimension;

import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.swing.JSVGCanvas;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;

public abstract class SVGComponent extends JSVGCanvas {
    private final int svgWidth;
    private final int svgHeight;

    public SVGComponent(SVGDocument svg) {
        setSVGDocument(svg);

        Element rootElement = svg.getDocumentElement();
        svgWidth = (int) Math.ceil(Double.parseDouble(rootElement.getAttributeNS(
                SVGDOMImplementation.SVG_NAMESPACE_URI, "width")));
        svgHeight = (int) Math.ceil(Double.parseDouble(rootElement.getAttributeNS(
                SVGDOMImplementation.SVG_NAMESPACE_URI, "height")));
        setPreferredSize(new Dimension(svgWidth, svgHeight));
    }

    public int getSvgWidth() {
        return svgWidth;
    }

    public int getSvgHeight() {
        return svgHeight;
    }
}
