package pl.poznan.put.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.apache.batik.swing.svg.AbstractJSVGComponent;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.svg.SVGDocument;

import pl.poznan.put.visualisation.MatrixVisualizationComponent;
import pl.poznan.put.visualisation.SVGComponent;

public class GlobalComparisonFrame extends JFrame implements EventListener {
    private final JButton saveButton = new JButton("Save");

    private final SVGComponent canvas;

    public GlobalComparisonFrame(SVGDocument svg) {
        super("MCQ4Structures: global distance plot");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        canvas = new MatrixVisualizationComponent(svg);
        canvas.setDocumentState(AbstractJSVGComponent.ALWAYS_DYNAMIC);
        canvas.setDocument(svg);

        getContentPane().add(saveButton, BorderLayout.NORTH);
        getContentPane().add(canvas, BorderLayout.CENTER);
        pack();

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        Dimension preferredSize = getPreferredSize();
        int x = screenSize.width / 2 - preferredSize.width / 2;
        int y = screenSize.height / 2 - preferredSize.height / 2;
        setLocation(x, y);

        registerEventListener(svg);

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                canvas.selectFileAndExport();
            }
        });
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
