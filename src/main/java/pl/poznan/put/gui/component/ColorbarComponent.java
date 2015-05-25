package pl.poznan.put.gui.component;

import java.io.File;

import org.w3c.dom.svg.SVGDocument;

public class ColorbarComponent extends SVGComponent {
    public ColorbarComponent(SVGDocument document) {
        super(document);
    }

    @Override
    public File suggestName() {
        return new File("colorbar.svg");
    }
}
