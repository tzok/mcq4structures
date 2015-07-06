package pl.poznan.put.gui.component;

import java.io.File;

import org.w3c.dom.svg.SVGDocument;

public class SecondaryStructureComponent extends SVGComponent {
    public SecondaryStructureComponent(SVGDocument svg) {
        super(svg);
    }

    @Override
    public File suggestName() {
        return new File("secondary.svg");
    }
}
