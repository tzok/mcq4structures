package pl.poznan.put.visualisation;

import java.io.File;

import pl.poznan.put.comparison.ModelsComparisonResult;

public class ColorbarComponent extends SVGComponent {
    public ColorbarComponent(ModelsComparisonResult.SelectedAngle result) {
        super(result.toSVG(0, Math.PI));
    }

    @Override
    public File suggestName() {
        return new File("colorbar.svg");
    }
}
