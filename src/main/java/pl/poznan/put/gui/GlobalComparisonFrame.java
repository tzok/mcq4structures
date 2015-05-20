package pl.poznan.put.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.svg.SVGDocument;

import pl.poznan.put.comparison.GlobalComparisonResultMatrix;

public class GlobalComparisonFrame extends JFrame implements EventListener {
    public GlobalComparisonFrame(GlobalComparisonResultMatrix matrix) {
        super("MCQ4Structures: global distance plot");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        SVGDocument svg = matrix.toSVG();
        JSVGCanvas canvas = new JSVGCanvas();
        canvas.setDocument(svg);
        registerEventListener(svg);

        getContentPane().add(canvas, BorderLayout.CENTER);
        pack();

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        Dimension preferredSize = getPreferredSize();
        int x = screenSize.width / 2 - preferredSize.width / 2;
        int y = screenSize.height / 2 - preferredSize.height / 2;
        setLocation(x, y);
    }

    private void registerEventListener(SVGDocument svg) {
        NodeList childNodes = svg.getElementsByTagName(SVGConstants.SVG_CIRCLE_TAG);

        for (int i = 0; i < childNodes.getLength(); i++) {
            EventTarget eventTarget = (EventTarget) childNodes.item(i);
            eventTarget.addEventListener(SVGConstants.SVG_MOUSEOVER_EVENT_TYPE, this, false);
        }
    }

    @Override
    public void handleEvent(Event event) {
        // do nothing currently
    }
}
