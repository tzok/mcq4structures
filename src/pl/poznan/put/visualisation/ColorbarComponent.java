package pl.poznan.put.visualisation;

import pl.poznan.put.comparison.ModelsComparisonResult;

public class ColorbarComponent extends SVGComponent {
    public ColorbarComponent(ModelsComparisonResult result) {
        super(result.toSVG(0, Math.PI));
    }
}
