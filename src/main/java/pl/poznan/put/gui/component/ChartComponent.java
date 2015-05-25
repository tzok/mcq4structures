package pl.poznan.put.gui.component;

import java.io.File;

import org.w3c.dom.svg.SVGDocument;

public class ChartComponent extends SVGComponent {
    public ChartComponent(SVGDocument svg) {
        super(svg);
    }

    @Override
    public File suggestName() {
        return new File("chart.svg");
    }
}
