package pl.poznan.put.gui.component;

import java.io.File;

import org.w3c.dom.svg.SVGDocument;

public class MatrixVisualizationComponent extends SVGComponent {
    public MatrixVisualizationComponent(SVGDocument svg) {
        super(svg);
    }

    @Override
    public File suggestName() {
        return new File("visualization.svg");
    }
}
